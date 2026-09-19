package cat.narezany.margyt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

/**
 * How many frames a second, decided here rather than by TikTok.
 *
 * TikTok picks a rate per screen and per situation: a feed at 120 on one
 * phone, 60 on the next, back to 90 when something else is on. Which is
 * reasonable of it and invisible until you are the one watching a 120 Hz
 * screen run at 60.
 *
 * Two things are needed to take that decision away. The window is asked for a
 * display mode with the rate wanted -- a request the system honours where the
 * screen can do it -- and TikTok's own calls asking for a rate are answered
 * with ours instead, because a preference the app overrides a moment later is
 * not a preference.
 *
 * Left alone, nothing here does anything: no mode is asked for and every call
 * goes through untouched.
 */
public final class Rate {

    static final String KEY = "fps_lock";

    /** Whatever TikTok decides, which is how it arrives. */
    public static final String AUTO = "";

    public static final String[] CHOICES = {AUTO, "60", "90", "120"};

    /** A mode counts as the one asked for within this much. */
    private static final float CLOSE = 1.5f;

    private static volatile String chosen;

    private Rate() {}

    // ------------------------------------------------------------ the choice

    public static String name() {
        String known = chosen;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        String value = prefs == null ? AUTO : prefs.getString(KEY, AUTO);
        chosen = value;
        return value;
    }

    public static void choose(String value) {
        if (value == null) value = AUTO;
        chosen = value;
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putString(KEY, value).apply();
    }

    /** The rate wanted, or zero for TikTok's own choice. */
    public static int wanted() {
        String value = name();
        if (value == null || value.length() == 0) return 0;
        try {
            return Integer.parseInt(value);
        } catch (Throwable ignored) {
            return 0;
        }
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

    // ------------------------------------------------------ asking the window


    /**
     * Ask this window for the rate that was chosen.
     *
     * A display mode is asked for by id where one matches, because a bare
     * preferred rate is a hint the system is free to ignore and often does.
     * Only modes the screen is already showing at this size are considered:
     * asking for a different resolution to get a rate would be changing
     * something nobody asked to change.
     */
    public static void apply(Activity activity) {
        int rate = wanted();
        if (rate <= 0 || activity == null) return;
        try {
            Window window = activity.getWindow();
            if (window == null) return;
            WindowManager.LayoutParams params = window.getAttributes();

            if (Build.VERSION.SDK_INT >= 23) {
                Display display = window.getWindowManager().getDefaultDisplay();
                Display.Mode now = display.getMode();
                Display.Mode best = null;
                for (Display.Mode mode : display.getSupportedModes()) {
                    if (mode.getPhysicalWidth() != now.getPhysicalWidth()
                            || mode.getPhysicalHeight() != now.getPhysicalHeight()) {
                        continue;
                    }
                    if (Math.abs(mode.getRefreshRate() - rate) > CLOSE) continue;
                    if (best == null || mode.getRefreshRate() > best.getRefreshRate()) {
                        best = mode;
                    }
                }
                if (best != null) params.preferredDisplayModeId = best.getModeId();
            }
            params.preferredRefreshRate = rate;
            window.setAttributes(params);
        } catch (Throwable error) {
            Diary.note("fps: " + error);
        }
    }

    /**
     * Whether the screen can actually show what was chosen.
     *
     * Asked so the settings can say so plainly rather than letting somebody
     * pick 120 on a 90 Hz phone and wonder why nothing changed.
     */
    public static boolean reachable(Activity activity) {
        int rate = wanted();
        if (rate <= 0 || activity == null || Build.VERSION.SDK_INT < 23) return true;
        try {
            Display display = activity.getWindowManager().getDefaultDisplay();
            for (Display.Mode mode : display.getSupportedModes()) {
                if (Math.abs(mode.getRefreshRate() - rate) <= CLOSE) return true;
            }
            return false;
        } catch (Throwable ignored) {
            return true;
        }
    }



    // ------------------------------------------- where TikTok asks for a rate

    /**
     * TikTok telling the system what it would like, answered with what was
     * chosen here. Untouched when nothing was.
     */
    public static void setFrameRate(Surface surface, float rate, int compatibility) {
        int ours = wanted();
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                surface.setFrameRate(ours > 0 ? ours : rate, compatibility);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void setFrameRate(Surface surface, float rate, int compatibility,
                                    int strategy) {
        int ours = wanted();
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                surface.setFrameRate(ours > 0 ? ours : rate, compatibility, strategy);
            } else if (Build.VERSION.SDK_INT >= 30) {
                surface.setFrameRate(ours > 0 ? ours : rate, compatibility);
            }
        } catch (Throwable ignored) {
        }
    }
}
