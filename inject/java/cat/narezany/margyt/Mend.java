package cat.narezany.margyt;

import android.app.Activity;
import android.content.Context;

/**
 * What a patch is, as far as the mod is concerned.
 *
 * A patch is a small dex the mod downloads and loads at start-up, carrying one
 * class that implements this. It exists so a fix does not have to be a three
 * hundred and eighty megabyte apk: the mod calls the patch at a handful of
 * places, and what the patch does there is what changes.
 *
 * What a patch can do is bounded, and honestly so. The hooks into TikTok's own
 * code are rewritten call sites inside the installed apk, and no file arriving
 * afterwards can add one or move one. A patch can act at the places listed
 * here, and it can reach everything the mod makes public -- the badges, the
 * banner, the settings, the feed filters -- because it is loaded into the same
 * process. It cannot rewrite a method that is already compiled into the apk,
 * and a class it carries under a name the apk already has is never reached.
 *
 * Every method here is called inside a guard: a patch that throws is dropped
 * for the rest of the run, and one that throws while starting is deleted, so a
 * bad patch costs a restart rather than an app.
 */
public interface Mend {

    /** Once, when the app comes up, before TikTok has drawn anything. */
    void started(Context context);

    /** Every time a screen comes to the front. */
    void resumed(Activity activity);

    /**
     * A name on its way to being drawn, before the mod adds its own marks.
     *
     * Return the name to use, or null to leave it as it was.
     */
    String name(String uid, String name);

    /**
     * Anything else, at the places the mod asks.
     *
     * `what` says where the question comes from -- "hdr", "feed", "rate",
     * "banner" -- and `with` is whatever that place has to hand. Return null
     * to say nothing and let the mod carry on as it would have.
     *
     * It is deliberately shapeless. A patch is written for a problem that was
     * not foreseen, and a strongly typed hook for every future problem is not
     * a thing that can be written in advance.
     */
    Object ask(String what, Object[] with);
}
