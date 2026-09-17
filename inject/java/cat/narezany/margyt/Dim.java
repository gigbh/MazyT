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
        boolean value = prefs != null && prefs.getBoolean(KEY_ON, false);
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
        int value = prefs == null ? DEFAULT : prefs.getInt(KEY_HOW, DEFAULT);
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

    /** The video, which is the one thing here the framework still names. */
    private static View surface(View view, int depth) {
        if (view == null || depth > 40) return null;
        if (view instanceof SurfaceView || view instanceof TextureView) return view;
        if (!(view instanceof ViewGroup)) return null;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            View found = surface(group.getChildAt(i), depth + 1);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Everything on the screen except the video and what holds it.
     *
     * The first version faded the video's later siblings, three levels up.
     * That reached the buttons down the right and missed the row of tabs along
     * the top, which is not a sibling of the video at all -- it is in another
     * branch entirely, above the pager the videos live in.
     *
     * So the rule is the other way round now: walk from the top of the screen,
     * and fade every branch that does not contain the video. What holds the
     * video keeps its own brightness, because fading a parent fades everything
     * inside it, the video included.
     */
    private static void over(View video, View root, float alpha) {
        // Back to what this was to begin with: what is drawn after the video,
        // a few levels up, and nothing else. Walking the whole window and
        // fading every branch without the video in it meant half of TikTok --
        // the comments, a profile, every panel that opens over the feed --
        // none of which sits still long enough to wear a screen, and all of
        // which somebody is trying to read.
        View keep = video;
        ViewGroup parent = (ViewGroup) video.getParent();
        int up = 0;
        while (parent != null && up < 3) {
            int at = parent.indexOfChild(keep);
            for (int i = at + 1; i < parent.getChildCount(); i++) {
                fade(parent.getChildAt(i), alpha);
            }
            keep = parent;
            Object above = parent.getParent();
            parent = above instanceof ViewGroup ? (ViewGroup) above : null;
            up++;
        }

        topBar(root, alpha);
    }

    /**
     * The row along the top: Подписки, Магазин, Рекомендации, the search.
     *
     * It is not a sibling of the video -- it lives above the pager the videos
     * are in -- so the walk never reaches it. TikTok builds it in a class
     * called `HomepageToolBar`, which is a real name and a dead end: it is not
     * a view, it is a factory, and the view it fills is an ordinary
     * FrameLayout handed to it. There is nothing in the tree with a name worth
     * matching.
     *
     * What the row does have is a shape nothing else on the screen has: the
     * full width, pinned to the very top, a tenth of the height at most, and
     * several pieces of text inside it. A panel somebody opened is never that
     * short, and the video is never that high.
     */
    private static void topBar(View root, float alpha) {
        try {
            View content = root.findViewById(android.R.id.content);
            if (!(content instanceof ViewGroup)) return;
            int tall = root.getHeight();
            int wide = root.getWidth();
            if (tall <= 0 || wide <= 0) return;
            look((ViewGroup) content, alpha, tall, wide, 0);
        } catch (Throwable ignored) {
        }
    }

    /** An eighth of the screen: taller than that and it is not the row. */
    private static final float THIN = 0.14f;

    private static void look(ViewGroup group, float alpha, int tall, int wide, int depth) {
        if (depth > 4) return;
        int[] where = new int[2];
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;

            int height = child.getHeight();
            child.getLocationOnScreen(where);
            boolean thin = height > 0 && height < tall * THIN;
            boolean full = child.getWidth() > wide * 0.85f;
            boolean atTop = where[1] < tall * 0.2f;

            if (thin && full && atTop && words(child, 0) >= 2) {
                fade(child, alpha);
                return;
            }
            if (child instanceof ViewGroup) {
                look((ViewGroup) child, alpha, tall, wide, depth + 1);
            }
        }
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

    /** What was turned down and to what, so it can be put back exactly. */
    private static final java.util.Map<View, Float> faded =
            new java.util.WeakHashMap<View, Float>();

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
            for (java.util.Map.Entry<View, Float> entry : faded.entrySet()) {
                View view = entry.getKey();
                if (view == null) continue;
                Float was = entry.getValue();
                if (was != null && Math.abs(view.getAlpha() - was.floatValue()) < 0.001f) {
                    view.setAlpha(1f);
                }
            }
            faded.clear();
        } catch (Throwable ignored) {
        }
    }

    private static SharedPreferences prefs() {
        Context context = Margy.context();
        if (context == null) return null;
        return context.getSharedPreferences(Margy.PREFS, Context.MODE_PRIVATE);
    }
}
