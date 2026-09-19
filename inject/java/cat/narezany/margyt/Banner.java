package cat.narezany.margyt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.InputStream;

/**
 * The picture across the top of a supporter's profile.
 *
 * Whose profile is open is known from the name the profile just wrote, and
 * the picture is put in behind the header. TikTok has no such thing, so it is
 * a view of the mod's own, tagged so it goes in once and comes out when the
 * next profile is somebody without one.
 */
public final class Banner {

    /** Five megabytes, the same as the server accepts. */
    public static final int MOST = 5 * 1024 * 1024;

    /** How tall the picture is drawn, in density pixels. */
    private static final int TALL = 132;

    private static final int TAG = 0x4D617242;   // "MarB"

    private Banner() {}

    public interface Said {
        void said(boolean ok, String trouble);
    }

    // ------------------------------------------------------------- sending

    /** Read a picture the person picked, refusing anything too big. */
    public static void send(final Context context, final Uri uri, final Said then) {
        final String uid = Account.id();
        final String token = Mine.token();
        if (uid == null || token.length() == 0) {
            if (then != null) then.said(false, Text.GRADIENT_NO_ACCOUNT);
            return;
        }
        Net.away("banner: send", new Runnable() {
            @Override
            public void run() {
                boolean ok = false;
                String trouble = Text.BANNER_REFUSED;
                try {
                    String kind = context.getContentResolver().getType(uri);
                    if (kind == null) kind = "image/jpeg";
                    byte[] blob = read(context, uri);
                    if (blob == null) {
                        trouble = Text.BANNER_UNREADABLE;
                    } else if (blob.length > MOST) {
                        trouble = Text.BANNER_TOO_BIG;
                    } else {
                        String said = Net.send(Badges.SERVER + "/banner?uid=" + uid
                                + "&token=" + token, kind, blob);
                        if (said != null) {
                            ok = true;
                            Badges.refresh();
                        }
                    }
                } catch (Throwable error) {
                    Diary.note("banner: " + error);
                }
                answer(then, ok, ok ? "" : trouble);
            }
        });
    }

    public static void drop(final Said then) {
        final String uid = Account.id();
        final String token = Mine.token();
        if (uid == null || token.length() == 0) {
            if (then != null) then.said(false, Text.GRADIENT_NO_ACCOUNT);
            return;
        }
        Net.away("banner: drop", new Runnable() {
            @Override
            public void run() {
                String said = Net.send(Badges.SERVER + "/banner?uid=" + uid
                        + "&token=" + token, "image/jpeg", new byte[0]);
                boolean ok = said != null;
                if (ok) Badges.refresh();
                answer(then, ok, ok ? "" : Text.BANNER_REFUSED);
            }
        });
    }

    private static byte[] read(Context context, Uri uri) {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) return null;
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[65536];
            int got;
            while ((got = in.read(buffer)) > 0) {
                out.write(buffer, 0, got);
                if (out.size() > MOST) {
                    in.close();
                    return out.toByteArray();
                }
            }
            in.close();
            return out.toByteArray();
        } catch (Throwable error) {
            Diary.note("banner: " + error);
            return null;
        }
    }

    private static void answer(final Said then, final boolean ok, final String trouble) {
        if (then == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                then.said(ok, trouble);
            }
        });
    }

    // ------------------------------------------------------------- showing

    /**
     * Put somebody's banner behind the profile they just opened.
     *
     * The name's own view is the way in: walk up until something is as wide
     * as the screen, and that is the header. Anything unexpected and nothing
     * happens, which is the right amount of stubbornness for decoration.
     */
    public static void show(final View nameView, final String uid) {
        if (nameView == null || uid == null) return;
        try {
            ViewGroup header = headerOf(nameView);
            if (header == null) return;
            ImageView had = (ImageView) header.findViewWithTag(TAG);
            String address = Looks.banner(uid);
            if (address == null) {
                if (had != null) header.removeView(had);
                return;
            }

            final Context context = nameView.getContext();

            ImageView view = had;
            if (view == null) {
                view = new ImageView(context);
                view.setTag(TAG);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                view.setAdjustViewBounds(false);
                header.addView(view, 0, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (TALL * context.getResources().getDisplayMetrics().density)));
            }
            picture(context, uid, address, view);
        } catch (Throwable error) {
            Diary.note("banner: " + error);
        }
    }

    private static ViewGroup headerOf(View from) {
        View at = from;
        int wide = from.getResources().getDisplayMetrics().widthPixels;
        for (int up = 0; up < 6 && at != null; up++) {
            View parent = at.getParent() instanceof View ? (View) at.getParent() : null;
            if (parent == null) return null;
            if (parent instanceof ViewGroup && parent.getWidth() >= wide * 0.9f) {
                return (ViewGroup) parent;
            }
            at = parent;
        }
        return null;
    }

    private static final java.util.Map<String, Bitmap> kept =
            new java.util.HashMap<String, Bitmap>();

    /**
     * The picture itself, from memory, from the cache, or from the server.
     *
     * Its own fetch rather than the badges' one: that builds an address under
     * /icon/, and handing it a whole address gave /icon/http://... and nothing
     * on screen. Nothing waits here, and the view is filled in whenever the
     * picture arrives.
     */
    private static void picture(final Context context, final String uid,
                                final String address, final ImageView into) {
        final String key = uid + "-" + Looks.bannerVersion(uid);
        Bitmap known;
        synchronized (kept) {
            if (kept.containsKey(key)) {
                known = kept.get(key);
                if (known != null) into.setImageBitmap(known);
                return;
            }
            kept.put(key, null);
        }

        final java.lang.ref.WeakReference<ImageView> waiting =
                new java.lang.ref.WeakReference<ImageView>(into);
        final File cache = new File(context.getFilesDir(), "margyt/banners/" + key);
        Net.away("banner: fetch", new Runnable() {
            @Override
            public void run() {
                byte[] blob = Net.read(cache);
                if (blob == null) {
                    blob = Net.bytes(address);
                    if (blob != null) Net.save(cache, blob);
                }
                final Bitmap picture = decode(blob);
                if (picture == null) return;
                synchronized (kept) {
                    kept.put(key, picture);
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView view = waiting.get();
                        if (view != null) view.setImageBitmap(picture);
                    }
                });
            }
        });
    }

    /**
     * Decoded no larger than a phone screen. A banner may be five megabytes,
     * and the full size of one is memory nobody needs.
     */
    static Bitmap decode(byte[] blob) {
        if (blob == null) return null;
        try {
            BitmapFactory.Options measure = new BitmapFactory.Options();
            measure.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(blob, 0, blob.length, measure);
            int step = 1;
            while (measure.outWidth / step > 1600) step *= 2;
            BitmapFactory.Options real = new BitmapFactory.Options();
            real.inSampleSize = step;
            return BitmapFactory.decodeByteArray(blob, 0, blob.length, real);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
