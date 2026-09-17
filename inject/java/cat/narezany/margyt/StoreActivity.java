package cat.narezany.margyt;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugins somebody has put in the store, and a button to take one.
 *
 * A screen of its own rather than another heading in the settings: the list
 * comes from the network, every row has a picture and three lines of its own,
 * and folding all that under a chevron in a page that is already long is how
 * something ends up never being opened.
 *
 * What the store knows about a plugin is what that plugin's own manifest says
 * -- the server reads it out of the packed file rather than taking anybody's
 * word for it -- so the name here is the name the phone will load.
 */
public class StoreActivity extends Activity {

    private Skin skin;
    private LinearLayout column;
    private List<Plugin> offered = new ArrayList<Plugin>();
    private String trouble = "";

    /** One plugin, as the store describes it. */
    private static final class Plugin {
        String id = "", name = "", version = "", author = "", about = "", tiktok = "";
        String url = "", icon = "";
        long size;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Margy.attach(this);
        skin = Skin.remembered(this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(skin.page);

        ScrollView scroll = new ScrollView(this);
        column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(0, statusBar(), 0, dp(32));
        scroll.addView(column, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);
        setContentView(root);

        try {
            getWindow().setStatusBarColor(skin.page);
            getWindow().setNavigationBarColor(skin.page);
        } catch (Throwable ignored) {
        }

        rebuild();
        fetch();
    }

    // ------------------------------------------------------------ the list

    private void fetch() {
        Net.away("store", new Runnable() {
            @Override
            public void run() {
                final List<Plugin> found = new ArrayList<Plugin>();
                String trouble = "";
                try {
                    byte[] raw = Net.bytes(Badges.SERVER + "/plugins");
                    if (raw == null) {
                        trouble = Text.STORE_OFFLINE;
                    } else {
                        JSONArray list = new JSONObject(new String(raw, "UTF-8"))
                                .optJSONArray("plugins");
                        for (int i = 0; list != null && i < list.length(); i++) {
                            JSONObject one = list.optJSONObject(i);
                            if (one == null) continue;
                            Plugin plugin = new Plugin();
                            plugin.id = one.optString("id", "");
                            plugin.name = one.optString("name", plugin.id);
                            plugin.version = one.optString("version", "");
                            plugin.author = one.optString("author", "");
                            plugin.about = one.optString("about", "");
                            plugin.tiktok = one.optString("tiktok", "");
                            plugin.url = one.optString("url", "");
                            plugin.icon = one.optString("icon", "");
                            plugin.size = one.optLong("size", 0);
                            found.add(plugin);
                        }
                    }
                } catch (Throwable error) {
                    trouble = String.valueOf(error);
                    Diary.note("store: " + error);
                }
                final String said = trouble;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        offered = found;
                        StoreActivity.this.trouble = said;
                        rebuild();
                    }
                });
            }
        });
    }

    private void rebuild() {
        column.removeAllViews();
        column.addView(back());
        column.addView(title(Text.STORE));

        LinearLayout card = card();
        if (offered.isEmpty()) {
            card.addView(quiet(trouble.length() > 0 ? trouble : Text.STORE_WAIT));
        } else {
            for (int i = 0; i < offered.size(); i++) {
                if (i > 0) card.addView(line());
                card.addView(row(offered.get(i)));
            }
        }
        column.addView(wrap(card));
        column.addView(quiet(Text.STORE_NOTE));
    }

    private View row(final Plugin plugin) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));

        ImageView picture = new ImageView(this);
        picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(44), dp(44));
        size.rightMargin = dp(14);
        row.addView(picture, size);
        paint(picture, plugin.icon);

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(plugin.name + "  " + plugin.version);
        name.setTextColor(skin.text);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        text.addView(name);

        TextView under = new TextView(this);
        String said = plugin.author;
        if (plugin.about.length() > 0) said += "  ·  " + plugin.about;
        under.setText(said);
        under.setTextColor(skin.muted());
        under.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        text.addView(under);

        boolean fits = plugin.tiktok.length() == 0 || plugin.tiktok.equals(Version.TIKTOK);
        if (!fits) {
            TextView warn = new TextView(this);
            warn.setText(Text.STORE_WRONG_VERSION + "  ·  " + plugin.tiktok);
            warn.setTextColor(skin.muted());
            warn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            text.addView(warn);
        }

        row.addView(text, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView get = new TextView(this);
        get.setText(fits ? Text.STORE_GET : Text.STORE_ANYWAY);
        get.setTextColor(fits ? onAccent() : skin.text);
        get.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        get.setPadding(dp(16), dp(9), dp(16), dp(9));
        GradientDrawable pill = new GradientDrawable();
        pill.setCornerRadius(dp(12));
        if (fits) {
            pill.setColor(Accent.colour());
        } else {
            pill.setColor(0x00000000);
            pill.setStroke(dp(1), skin.muted());
        }
        get.setBackground(pill);
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                install(plugin);
            }
        });
        row.addView(get);
        return row;
    }

    private void paint(final ImageView view, final String path) {
        if (path == null || path.length() == 0) {
            view.setImageBitmap(Badges.note());
            return;
        }
        Net.away("store picture", new Runnable() {
            @Override
            public void run() {
                final byte[] raw = Net.bytes(Badges.SERVER + path);
                if (raw == null) return;
                final Bitmap bitmap = android.graphics.BitmapFactory
                        .decodeByteArray(raw, 0, raw.length);
                if (bitmap == null) return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    // --------------------------------------------------------- taking one

    private void install(final Plugin plugin) {
        Screen.progress(Text.STORE_GETTING, 0);
        Net.away("store: " + plugin.id, new Runnable() {
            @Override
            public void run() {
                final File into = new File(getCacheDir(), plugin.id + ".mtp");
                boolean got = Net.download(Badges.SERVER + plugin.url, into,
                        new Net.Along() {
                            @Override
                            public void at(int percent, long some, long all) {
                                Screen.progress(Text.STORE_GETTING, percent);
                            }
                        });
                Screen.progressGone();
                final boolean ok = got && put(into);
                into.delete();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Screen.say(ok ? Text.STORE_GOT : Text.STORE_FAILED);
                    }
                });
            }
        });
    }

    private boolean put(File file) {
        try {
            Plugins.install(this, android.net.Uri.fromFile(file));
            return true;
        } catch (Throwable error) {
            Diary.note("store: " + error);
            return false;
        }
    }

    // ------------------------------------------------------------- the look

    private View back() {
        TextView view = new TextView(this);
        view.setText("←");
        view.setTextColor(skin.text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        view.setPadding(skin.margin, dp(14), skin.margin, dp(6));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return view;
    }

    private View title(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(skin.text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        view.setPadding(skin.margin, dp(8), skin.margin, dp(20));
        return view;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable background = new GradientDrawable();
        background.setColor(skin.card);
        background.setCornerRadius(skin.radius);
        card.setBackground(background);
        return card;
    }

    private View wrap(View card) {
        LinearLayout holder = new LinearLayout(this);
        holder.setPadding(skin.margin, 0, skin.margin, dp(8));
        holder.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return holder;
    }

    private View line() {
        View view = new View(this);
        view.setBackgroundColor(skin.muted() & 0x33FFFFFF);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(1) / 2)));
        return view;
    }

    private TextView quiet(String said) {
        TextView view = new TextView(this);
        view.setText(said);
        view.setTextColor(skin.muted());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        view.setPadding(skin.margin + dp(4), dp(12), skin.margin + dp(4), dp(12));
        return view;
    }

    private int onAccent() {
        int colour = Accent.colour();
        int light = ((colour >> 16) & 0xFF) * 299 + ((colour >> 8) & 0xFF) * 587
                + (colour & 0xFF) * 114;
        return light / 1000 > 140 ? 0xFF10221F : 0xFFFFFFFF;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int statusBar() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24);
    }
}
