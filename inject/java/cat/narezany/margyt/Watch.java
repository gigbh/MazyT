package cat.narezany.margyt;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * One pair of eyes on the app's screens, for everything that needs them.
 *
 * Several parts of the mod want to know when a screen comes up: the frame
 * rate has to be asked for again on each window, the HDR headroom likewise, a
 * test build stamps its mark, and the region steps aside while a sign-in form
 * is open. Registering four callbacks to learn the same thing four times is
 * four times the work on every screen change, and this app changes screens
 * constantly.
 */
public final class Watch implements Application.ActivityLifecycleCallbacks {

    private Watch() {}

    public static void start(Application application) {
        try {
            application.registerActivityLifecycleCallbacks(new Watch());
        } catch (Throwable error) {
            Diary.note("watch: " + error);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        try {
            Region.notice(activity);
        } catch (Throwable ignored) {
        }
        try {
            Rate.apply(activity);
        } catch (Throwable ignored) {
        }
        try {
            Hdr.apply(activity);
        } catch (Throwable ignored) {
        }
        try {
            Tester.mark(activity);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        try {
            Region.leaving(activity);
        } catch (Throwable ignored) {
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle out) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
