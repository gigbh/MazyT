package cat.narezany.margyt;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * One callback for everything that needs to know a screen came up: the frame
 * rate, the HDR headroom, the test mark. Three registrations would be three
 * times the work on every screen change, and this app changes screens often.
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

    @Override public void onActivityPaused(Activity activity) {}

    @Override public void onActivityCreated(Activity activity, Bundle state) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle out) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
