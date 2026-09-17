package cat.narezany.margyt.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

/**
 * The mod, from a plugin's side.
 *
 * A plugin gets one of these before its first hook and keeps it. Everything a
 * plugin is likely to want that it should not have to find for itself: the
 * application context, somewhere to write, settings of its own that no other
 * plugin can collide with, and a line in the mod's diary.
 */
public final class PluginContext {

    private final Context context;
    private final String id;
    private final File folder;
    private final Diarist diarist;

    /** How the loader writes into the mod's diary without exporting it. */
    public interface Diarist {
        void note(String line);
    }

    public PluginContext(Context context, String id, File folder, Diarist diarist) {
        this.context = context;
        this.id = id;
        this.folder = folder;
        this.diarist = diarist;
    }

    /** The application context. Never an activity, so it is safe to keep. */
    public Context context() {
        return context;
    }

    /** The plugin's own id, as its manifest spells it. */
    public String id() {
        return id;
    }

    /** Where the plugin was unpacked: its own files are here, read-only. */
    public File folder() {
        return folder;
    }

    /** Settings of the plugin's own, in a file named after its id. */
    public SharedPreferences prefs() {
        return context.getSharedPreferences("margyt_plugin_" + id, Context.MODE_PRIVATE);
    }

    /**
     * A line in the mod's diary, which the person can read and copy out of the
     * settings screen. This is the plugin's way of saying anything at all: an
     * app repacked from a release has no log anyone is watching.
     */
    // ------------------------------------------------------------ the screen

    /**
     * Ways of putting something on the screen.
     *
     * A plugin cannot declare an activity of its own -- components are read
     * out of the manifest when the app is installed, and the mod is not going
     * to rewrite somebody's installed apk. What it can do is everything short
     * of that: a row in the mod's settings, a window over whatever is showing,
     * a button that floats above a screen, and an ordinary Android intent if
     * it really does want its own screen from another app.
     */
    public interface Tapped {
        void tapped();
    }

    /** A row of the mod's own settings, under the plugin's own heading. */
    public void addSettingsRow(String title, String detail, Tapped action) {
        cat.narezany.margyt.Plugins.addRow(id, title, detail, action);
    }

    /** The mod's own window: the same card TikTok's own dialogs are drawn as. */
    public void showWindow(String title, String message) {
        cat.narezany.margyt.Popup.show(context, title, message);
    }

    /** A button over whatever is on screen, for as long as that screen is. */
    public void offer(String label, final Tapped action) {
        cat.narezany.margyt.Screen.offer(label, new Runnable() {
            @Override
            public void run() {
                action.tapped();
            }
        });
    }

    /** A line along the top of the screen, for something that takes a while. */
    public void progress(String what, int percent) {
        cat.narezany.margyt.Screen.progress(what, percent);
    }

    public void progressGone() {
        cat.narezany.margyt.Screen.progressGone();
    }

    /** Whatever screen is up, or null between screens. */
    public android.app.Activity screen() {
        return cat.narezany.margyt.Screen.now();
    }

    /** Fetch something, off the main thread. Answers null rather than throwing. */
    public byte[] fetch(String url) {
        return cat.narezany.margyt.Net.bytes(url);
    }

    /** Run something off the main thread, named so the diary can say whose. */
    public void away(String what, Runnable work) {
        cat.narezany.margyt.Net.away(id + ": " + what, work);
    }

    public void log(String line) {
        diarist.note(id + ": " + line);
    }
}
