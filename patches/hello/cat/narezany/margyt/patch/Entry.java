package cat.narezany.margyt.patch;

import android.app.Activity;
import android.content.Context;

import cat.narezany.margyt.Diary;
import cat.narezany.margyt.Mend;

/**
 * The patch that proves the road works, and does nothing else.
 *
 * It writes one line in the diary when it starts. Every patch after this one
 * is written the same way: one class here, `Mend` implemented, built and
 * signed with `tools/make_patch.py`, uploaded in the panel.
 */
public final class Entry implements Mend {

    @Override
    public void started(Context context) {
        Diary.note("patch: hello from a patch, the mod was not reinstalled");
    }

    @Override
    public void resumed(Activity activity) {
    }

    @Override
    public String name(String uid, String plain) {
        return null;
    }

    @Override
    public Object ask(String what, Object[] with) {
        return null;
    }
}
