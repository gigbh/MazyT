package cat.narezany.margyt;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Proving that an account is yours, once.
 *
 * TikTok will not tell anybody that somebody is who they say they are, so the
 * mod asks the one thing TikTok does say out loud: what a profile page holds.
 * The server gives out a short code, the person puts it in their bio, the
 * server reads the page and sees it. Nobody can write into somebody else's
 * bio, so nobody else gets the key.
 *
 * The key that comes back is kept on the phone and used for everything after
 * that: the order of badges, a gradient, a banner. Proving is not something
 * to do twice.
 *
 * It replaces handing the key to whoever asked first, which held up until
 * somebody wrote a loop and gave the free badge to fifty accounts that had
 * never run the mod.
 */
public final class Proof {

    private Proof() {}

    private static final String KEY_PROVED = "proved_uid";

    /** The code the server last gave, kept only while the screen is open. */
    private static volatile String code = "";

    public interface Then {
        void then(boolean ok, String trouble);
    }

    // ------------------------------------------------------------ what is known

    /** Whether this account has proved itself on this phone. */
    public static boolean proved() {
        String uid = Account.id();
        if (uid == null || uid.length() == 0) return false;
        SharedPreferences prefs = prefs();
        if (prefs == null) return false;
        return uid.equals(prefs.getString(KEY_PROVED, ""))
                && Mine.token().length() > 0;
    }

    /** Remember what the server said when it was asked what this account is. */
    static void heard(boolean isProved) {
        if (isProved) return;
        // the server is the one that decides; a phone saying yes to itself
        // after the key was thrown away helps nobody
        forget();
    }

    /** A write came back asking to be proved, so the key is no good any more. */
    static void lost() {
        forget();
    }

    private static void forget() {
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().remove(KEY_PROVED).apply();
    }

    public static String waiting() {
        return code;
    }

    // ------------------------------------------------------------- the asking

    /** Ask for a code to put in the bio. Answers on the main thread. */
    public static void want(final Then then) {
        final String uid = Account.id();
        if (uid == null || uid.length() == 0) {
            answer(then, false, Text.PROVE_NO_ACCOUNT);
            return;
        }
        Net.away("proof: code", new Runnable() {
            @Override
            public void run() {
                Net.Said said = Net.talk(Badges.SERVER + "/prove",
                        json("uid", uid));
                if (!said.ok()) {
                    answer(then, false, trouble(said));
                    return;
                }
                try {
                    code = new JSONObject(said.body).optString("code", "");
                } catch (Throwable error) {
                    Diary.note("proof: " + error);
                }
                answer(then, code.length() > 0, code.length() > 0 ? "" : Text.PROVE_FAILED);
            }
        });
    }

    /**
     * Have the server read this account's profile page and look for the code.
     *
     * Nothing is sent but the account id. TikTok's own share link says which
     * name an id belongs to, so the server finds the page itself rather than
     * making somebody type their own name into a box.
     */
    public static void check(final Then then) {
        final String uid = Account.id();
        if (uid == null || uid.length() == 0) {
            answer(then, false, Text.PROVE_NO_ACCOUNT);
            return;
        }
        Net.away("proof: check", new Runnable() {
            @Override
            public void run() {
                Net.Said said = Net.talk(Badges.SERVER + "/prove/check",
                        json("uid", uid));
                if (!said.ok()) {
                    // a code that ran out is not something to tell anybody
                    // off about: the next one is fetched before answering
                    if (said.code == 410) mint(uid);
                    answer(then, false, trouble(said));
                    return;
                }
                boolean ok = false;
                try {
                    JSONObject told = new JSONObject(said.body);
                    String token = told.optString("token", "");
                    if (token.length() > 0) {
                        Mine.gotKey(uid, token);
                        SharedPreferences prefs = prefs();
                        if (prefs != null) {
                            prefs.edit().putString(KEY_PROVED, uid).apply();
                        }
                        Mine.read(told);
                        code = "";
                        ok = true;
                    }
                } catch (Throwable error) {
                    Diary.note("proof: " + error);
                }
                answer(then, ok, ok ? "" : Text.PROVE_FAILED);
            }
        });
    }

    /** Take a fresh code, in the thread that just found the old one spent. */
    private static void mint(String uid) {
        try {
            Net.Said said = Net.talk(Badges.SERVER + "/prove", json("uid", uid));
            code = said.ok() ? new JSONObject(said.body).optString("code", "") : "";
        } catch (Throwable error) {
            Diary.note("proof: " + error);
            code = "";
        }
    }

    /** What the server refused with, in words rather than in a number. */
    private static String trouble(Net.Said said) {
        String what = "";
        try {
            if (said.body.length() > 0) {
                what = new JSONObject(said.body).optString("error", "");
            }
        } catch (Throwable ignored) {
        }
        if (said.code == 0) return Text.PROVE_NO_SERVER;
        if ("the code is not in that profile yet".equals(what)) return Text.PROVE_NOT_THERE;
        if ("that name belongs to another account".equals(what)) return Text.PROVE_OTHER_NAME;
        if ("tiktok did not answer".equals(what)) return Text.PROVE_NO_TIKTOK;
        if (said.code == 410) return Text.PROVE_STALE;
        if (said.code == 429) return Text.PROVE_TOO_OFTEN;
        return Text.PROVE_FAILED;
    }

    private static String json(String... pairs) {
        try {
            JSONObject out = new JSONObject();
            for (int i = 0; i + 1 < pairs.length; i += 2) out.put(pairs[i], pairs[i + 1]);
            return out.toString();
        } catch (Throwable error) {
            return "{}";
        }
    }

    private static void answer(final Then then, final boolean ok, final String trouble) {
        if (then == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                then.then(ok, trouble);
            }
        });
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
}
