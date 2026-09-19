package cat.narezany.margyt;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

/**
 * What a test build does that a release does not.
 *
 * Test builds go to the people who paid for the work before anyone else sees
 * them, which means the build is out of the author's hands and in a dozen
 * other people's. Two things follow from that.
 *
 * A screenshot of an unreleased build carries the account id of whoever took
 * it, drawn so faintly that nobody watching a video will see it and plainly
 * enough that a screenshot will. It is redrawn every ten seconds because a
 * still frame is the thing that leaks, and a stale id on a shared phone would
 * name the wrong person.
 *
 * And the mod's own settings are closed to anyone without the supporter badge.
 * The app itself is left entirely alone: sign in, watch, post, all of it. A
 * leaked test build is simply a TikTok with nothing extra in it.
 */
public final class Tester {

    /** The badge that makes a test build yours. */
    static final String SUPPORTER = "supporter";

    /** Faint enough to disappear into a video, solid enough to survive a jpeg. */
    private static final int INK = 0x14FFFFFF;

    private static final long AGAIN = 10_000L;

    private static final int TAG = 0x4D617254;   // "MarT"

    private static final Handler HAND = new Handler(Looper.getMainLooper());

    private Tester() {}

    /** Whether this build is a test build at all. */
    public static boolean on() {
        return Version.TEST;
    }

    /**
     * Whether whoever is signed in paid for this.
     *
     * The badge is read from the account's own list, which carries the hidden
     * ones too: somebody who keeps their badge turned off is still somebody
     * who has it.
     */
    public static boolean allowed() {
        if (!on()) return true;
        try {
            List<Mine.Held> held = Mine.held();
            for (Mine.Held one : held) {
                if (SUPPORTER.equals(one.id)) return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /** Whether the answer is known yet, as opposed to known to be no. */
    public static boolean asked() {
        try {
            return Mine.anything() || Mine.everAsked();
        } catch (Throwable ignored) {
            return false;
        }
    }

    // ------------------------------------------------------------ the mark




    /**
     * Put the id on the screen, and keep it there.
     *
     * The view goes into the window's own root rather than into any of
     * TikTok's layouts: nothing of TikTok's knows about it, nothing it does to
     * its own views takes it away, and it cannot be tapped -- a mark that
     * swallowed a tap would be a mark people work around.
     */
    static void mark(final Activity activity) {
        if (!on()) return;
        View root = activity.getWindow() == null ? null
                : activity.getWindow().getDecorView();
        if (!(root instanceof ViewGroup)) return;
        ViewGroup decor = (ViewGroup) root;
        if (decor.findViewWithTag(TAG) != null) return;

        final TextView mark = new TextView(activity);
        mark.setTag(TAG);
        mark.setText(who());
        mark.setTextSize(22);
        mark.setTextColor(INK);
        mark.setTypeface(Typeface.DEFAULT_BOLD);
        mark.setGravity(Gravity.CENTER);
        mark.setShadowLayer(1.5f, 0, 0, 0x10000000);
        mark.setClickable(false);
        mark.setFocusable(false);
        mark.setDuplicateParentStateEnabled(false);

        FrameLayout.LayoutParams where = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        decor.addView(mark, where);

        HAND.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mark.getParent() == null) return;
                mark.setText(who());
                mark.bringToFront();
                HAND.postDelayed(this, AGAIN);
            }
        }, AGAIN);
    }

    /** The account id, or a plain word when nobody is signed in yet. */
    private static String who() {
        try {
            String id = Account.id();
            if (id != null && id.length() > 0) return id;
        } catch (Throwable ignored) {
        }
        return Text.TEST_NOBODY;
    }

    // ---------------------------------------------------------- the refusal

    /**
     * Say why the settings will not open, on a screen of the mod's own.
     *
     * Told plainly rather than hidden: somebody holding a leaked build should
     * know what it is and what to do about it, and somebody who paid and is
     * signed into the wrong account should know that is all that happened.
     */
    public static void refuse(Activity activity) {
        try {
            Popup.told(activity, Text.TEST_TITLE, Text.TEST_TEXT, Text.TEST_CLOSE,
                    new Runnable() {
                        @Override
                        public void run() {
                            activity.finish();
                        }
                    });
        } catch (Throwable error) {
            Diary.note("test: " + error);
            activity.finish();
        }
    }
}
