package cat.narezany.margyt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.Window;

/**
 * HDR video at the brightness of everything else.
 *
 * The video still plays. What goes away is the extra brightness: the window
 * asks for a headroom of one. That needs Android 15. Below it only the plain
 * colour mode can be asked for, and not every phone listens.
 */
public final class Hdr {

    static final String KEY = "no_hdr";

    /** No more brightness than anything else on screen. */
    private static final float FLAT = 1.0f;

    private static volatile Boolean cached;

    private Hdr() {}

    public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        if (prefs == null) return false;
        boolean on = prefs.getBoolean(KEY, false);
        cached = on;
        return on;
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY, enabled).apply();
    }

    /** Whether this phone can actually be told to flatten it. */
    public static boolean reachable() {
        return Build.VERSION.SDK_INT >= 35;
    }

    private static SharedPreferences prefs() {
        Context context = Margy.context();
        if (context == null) return null;
        try {
            return context.getSharedPreferences(Margy.PREFS, Context.MODE_PRIVATE);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // ------------------------------------------------------------ the window


    /** Set on any window this has touched, so turning it off can undo it. */
    private static volatile boolean touched;

    /**
     * Flatten HDR on this window, or leave the window alone.
     *
     * Leaving it alone is the point of the second half. Asking for
     * COLOR_MODE_HDR when the setting is off is not "as it was": it puts the
     * window into a mode TikTok never asked for, and every ordinary colour in
     * it gets mapped through that -- which is why screens went pale and white
     * came out tinted.
     */
    public static void apply(Activity activity) {
        if (activity == null) return;
        try {
            Window window = activity.getWindow();
            if (window == null) return;

            if (!isEnabled()) {
                if (touched && Build.VERSION.SDK_INT >= 35) {
                    // zero is "whatever you like", which is where it started
                    window.setDesiredHdrHeadroom(0f);
                }
                return;
            }

            touched = true;
            if (Build.VERSION.SDK_INT >= 35) {
                window.setDesiredHdrHeadroom(FLAT);
            }
            window.setColorMode(ActivityInfo.COLOR_MODE_DEFAULT);
        } catch (Throwable error) {
            Diary.note("hdr: " + error);
        }
    }


}
