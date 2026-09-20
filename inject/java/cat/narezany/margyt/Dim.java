package cat.narezany.margyt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fading what is drawn over the video, so it does not burn into the screen.
 *
 * The buttons down the right, the caption, the record, the scrubbing bar: they
 * sit in the same place for hours and an OLED panel remembers them. Turning
 * them down does not make them unusable -- a tap works on a view at a tenth of
 * its brightness exactly as it works at full -- and it is the whole of what
 * stops the wear.
 *
 * Which views those are is not a question of names. Every one of them is
 * renamed each release, and none of the names above would survive. What does
 * survive is where they are: the video is a SurfaceView or a TextureView,
 * which are the framework's own classes, and everything drawn on top of the
 * video is a sibling that comes after it. So the video is found and its later
 * siblings are faded -- the video itself never is.
 */
public final class Dim {

    private Dim() {}

    public static final String KEY_ON = "dim_on";
    public static final String KEY_HOW = "dim_how";

    /** Per cent of the way to invisible. A third is enough to be worth doing. */
    public static final int DEFAULT = 35;

    private static volatile Boolean on;
    private static volatile int how = -1;

    public static boolean isEnabled() {
        Boolean known = on;
        if (known != null) return known.booleanValue();
        SharedPreferences prefs = prefs();
        // nothing to read yet means nothing to remember: writing "off" down
        // here left the setting off for the rest of the run
        if (prefs == null) return false;
        boolean value = prefs.getBoolean(KEY_ON, false);
        on = Boolean.valueOf(value);
        return value;
    }

    public static void setEnabled(boolean enabled) {
        on = Boolean.valueOf(enabled);
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY_ON, enabled).apply();
        if (!enabled) restore();
    }

    public static int strength() {
        int known = how;
        if (known >= 0) return known;
        SharedPreferences prefs = prefs();
        if (prefs == null) return DEFAULT;
        int value = prefs.getInt(KEY_HOW, DEFAULT);
        how = Math.max(0, Math.min(90, value));
        return how;
    }

    public static void setStrength(int percent) {
        how = Math.max(0, Math.min(90, percent));
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putInt(KEY_HOW, how).apply();
    }

    // -------------------------------------------------------------- the work

    /**
     * Fade whatever is over the video on this screen.
     *
     * Called from the same pass that repaints a screen, so it costs nothing on
     * screens with no video in them: no surface, nothing to do.
     */
    public static void apply(Activity activity) {
        if (activity == null) return;
        long now = android.os.SystemClock.uptimeMillis();
        if (now - last < QUIET) return;
        last = now;
        try {
            // Everything turned down last time is turned back up first.
            //
            // A feed is a recycler: the view that was the button column on a
            // video is handed to the next cell to be something else, and if it
            // is still carrying our brightness it carries it into whatever it
            // becomes -- which is how a photo post ended up faded. Nothing is
            // left dimmed between passes, and each pass dims what is in front
            // of it now.
            restore();
            if (!isEnabled()) return;

            View root = activity.getWindow().getDecorView();
            View video = surface(root, 0);
            if (video == null) return;   // a photo post has no video in it

            float alpha = 1f - strength() / 100f;
            over(video, root, alpha);
        } catch (Throwable error) {
            Diary.note("dim: " + error);
        }
    }

    private static volatile long last;

    /**
     * Runs on every layout, so it has a pause of its own -- short, because a
     * swipe builds a whole new cell and nothing of the old one carries over.
     */
    private static final long QUIET = 120;

    /**
     * The video that is actually being watched.
     *
     * Not the first one in the tree, which is what this used to take: a feed
     * keeps the cells on either side of the one you are looking at, so the
     * first surface it found was as often the video above as the one on
     * screen -- and the cell it dimmed was that one. The one being watched is
     * the one covering the middle of the screen.
     */
    private static View surface(View root, int unused) {
        java.util.List<View> all = new java.util.ArrayList<View>();
        gather(root, all, 0);
        if (all.isEmpty()) return null;

        int middleX = root.getWidth() / 2;
        int middleY = root.getHeight() / 2;
        android.graphics.Rect where = new android.graphics.Rect();
        View biggest = null;
        long widest = 0;
        for (View one : all) {
            if (!one.getGlobalVisibleRect(where)) continue;
            if (where.contains(middleX, middleY)) return one;
            long area = (long) where.width() * where.height();
            if (area > widest) {
                widest = area;
                biggest = one;
            }
        }
        return biggest;
    }

    private static void gather(View view, java.util.List<View> out, int depth) {
        if (view == null || depth > 40 || out.size() > 8) return;
        if (view instanceof SurfaceView || view instanceof TextureView) {
            out.add(view);
            return;
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            gather(group.getChildAt(i), out, depth + 1);
        }
    }

    /**
     * Everything in this video's own cell except the video.
     *
     * The cell is found by walking up from the video until the list the videos
     * are in -- that list is the edge, and above it are the other videos and
     * the screen itself, which are not ours. Inside the cell, anything that is
     * not the video and does not contain it is over the video by definition:
     * the buttons, the caption, the record, the scrubbing bar.
     *
     * This replaced "the siblings drawn after the video", which reached
     * whatever happened to be listed after it and missed whatever was not.
     */
    private static void over(View video, View root, float alpha) {
        View cell = video;
        java.util.List<View> spine = new java.util.ArrayList<View>();
        spine.add(cell);
        while (true) {
            Object parent = cell.getParent();
            if (!(parent instanceof ViewGroup)) break;
            ViewGroup group = (ViewGroup) parent;
            if (scrolls(group)) break;
            cell = group;
            spine.add(cell);
        }
        if (cell == video) return;

        inside(cell, spine, alpha, 0);
        topBar(root, alpha);
    }

    private static void inside(View view, java.util.List<View> spine,
                               float alpha, int depth) {
        if (view == null || depth > 12) return;
        if (!spine.contains(view)) {
            fade(view, alpha);
            return;          // its children go with it
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            inside(group.getChildAt(i), spine, alpha, depth + 1);
        }
    }

    /** Whether this is the thing the videos are listed in. */
    private static boolean scrolls(View view) {
        String name = view.getClass().getName();
        return name.contains("RecyclerView") || name.contains("ViewPager")
                || name.contains("ListView");
    }

    /**
     * The row along the top: Подписки, Магазин, Рекомендации, the search.
     *
     * Looked for once and then remembered. TikTok builds it in a class called
     * `HomepageToolBar`, which is a real name and a dead end -- it is not a
     * view, it is a factory, and what it fills is an ordinary FrameLayout. So
     * it is found by its shape instead, and searching a whole screen for that
     * shape on every layout is what was making the app slow to start.
     */
    private static void topBar(View root, float alpha) {
        View known = bar == null ? null : bar.get();
        if (known != null && known.isAttachedToWindow()) {
            fade(known, alpha);
            return;
        }
        if (searched) return;
        searched = true;
        try {
            View content = root.findViewById(android.R.id.content);
            if (!(content instanceof ViewGroup)) return;
            int tall = root.getHeight();
            int wide = root.getWidth();
            if (tall <= 0 || wide <= 0) {
                searched = false;   // the screen has no size yet; try again later
                return;
            }
            View found = look((ViewGroup) content, tall, wide, 0);
            if (found != null) {
                bar = new java.lang.ref.WeakReference<View>(found);
                fade(found, alpha);
            }
        } catch (Throwable ignored) {
        }
    }

    private static volatile java.lang.ref.WeakReference<View> bar;
    private static volatile boolean searched;

    /** A tenth of the screen: taller than that and it is not the row. */
    private static final float THIN = 0.14f;

    private static View look(ViewGroup group, int tall, int wide, int depth) {
        if (depth > 4) return null;
        int[] where = new int[2];
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;

            int height = child.getHeight();
            child.getLocationOnScreen(where);
            boolean thin = height > 0 && height < tall * THIN;
            boolean full = child.getWidth() > wide * 0.85f;
            boolean atTop = where[1] < tall * 0.2f;

            if (thin && full && atTop && words(child, 0) >= 2) return child;
            if (child instanceof ViewGroup) {
                View found = look((ViewGroup) child, tall, wide, depth + 1);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** How many pieces of text are inside, which is what makes it a row of tabs. */
    private static int words(View view, int depth) {
        if (depth > 6) return 0;
        if (view instanceof android.widget.TextView) {
            CharSequence text = ((android.widget.TextView) view).getText();
            return text != null && text.length() > 0 ? 1 : 0;
        }
        if (!(view instanceof ViewGroup)) return 0;
        ViewGroup group = (ViewGroup) view;
        int many = 0;
        for (int i = 0; i < group.getChildCount(); i++) {
            many += words(group.getChildAt(i), depth + 1);
        }
        return many;
    }

    private static void fade(View view, float alpha) {
        try {
            if (view == null) return;
            faded.put(view, Float.valueOf(alpha));
            if (view.getAlpha() != alpha) view.setAlpha(alpha);
        } catch (Throwable ignored) {
        }
    }

    /**
     * How bright a view should end up when TikTok asks for a brightness.
     *
     * Multiplied rather than replaced: TikTok fades its overlay away when a
     * panel opens and back when it closes, and answering with a fixed number
     * would either keep a hidden overlay visible or leave it hidden. Half of
     * TikTok's own is half of ours.
     */
    public static float alphaFor(View view, float asked) {
        try {
            if (!isEnabled() || !faded.containsKey(view)) return asked;
            return asked * (1f - strength() / 100f);
        } catch (Throwable ignored) {
            return asked;
        }
    }

    /**
     * What was turned down and to what, so it can be put back exactly.
     *
     * Wrapped, because `alphaFor` is called from wherever TikTok happens to
     * set a brightness from, and a WeakHashMap read while another thread
     * writes it is a crash in somebody else's app.
     */
    private static final java.util.Map<View, Float> faded =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<View, Float>());

    /**
     * Put back what the mod turned down, and only that.
     *
     * A view is restored only if it is still wearing the brightness the mod
     * gave it: if TikTok has since faded it away itself -- a panel opening, a
     * video pausing -- then that is TikTok's business and setting it back to
     * full would put a hidden overlay on screen.
     */
    private static void restore() {
        try {
            synchronized (faded) {
                for (java.util.Map.Entry<View, Float> entry : faded.entrySet()) {
                    View view = entry.getKey();
                    if (view == null) continue;
                    Float was = entry.getValue();
                    if (was != null
                            && Math.abs(view.getAlpha() - was.floatValue()) < 0.001f) {
                        view.setAlpha(1f);
                    }
                }
                faded.clear();
            }
        } catch (Throwable ignored) {
        }
    }

    private static SharedPreferences prefs() {
        Context context = Margy.context();
        if (context == null) return null;
        return context.getSharedPreferences(Margy.PREFS, Context.MODE_PRIVATE);
    }
}
