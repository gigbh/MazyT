package cat.narezany.margyt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.Window;

/**
 * HDR video, shown at the brightness of everything else.
 *
 * An HDR video is allowed to ask the screen for more brightness than the
 * screen normally gives, and phones grant it: the video goes searingly bright
 * while the rest of the interface stays where it was. Fine in a cinema, less
 * fine when it arrives unannounced in a feed at night.
 *
 * Nothing here blocks HDR videos or changes which videos play. What it does
 * is take away the extra brightness -- the window asks for a headroom of one,
 * which means "no more than anything else gets" -- so the video plays,
 * correctly, at the brightness of the screen it is on.
 *
 * That headroom is Android 15 and up. Below it, the window is asked for the
 * ordinary colour mode, which some phones honour and some do not, and the
 * setting says so rather than pretending.
 */
public final class Hdr {

    static final String KEY = "no_hdr";

    /** As much brightness as anything else on the screen, and no more. */
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


    public static void apply(Activity activity) {
        if (activity == null) return;
        try {
            Window window = activity.getWindow();
            if (window == null) return;
            boolean flatten = isEnabled();

            if (Build.VERSION.SDK_INT >= 35) {
                // zero would mean "whatever you like", which is the opposite
                window.setDesiredHdrHeadroom(flatten ? FLAT : 0f);
            }
            window.setColorMode(flatten ? ActivityInfo.COLOR_MODE_DEFAULT
                    : ActivityInfo.COLOR_MODE_HDR);
        } catch (Throwable error) {
            Diary.note("hdr: " + error);
        }
    }


}
