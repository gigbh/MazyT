package cat.narezany.margyt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Fetching things, off the main thread and without dragging in a library.
 *
 * The mod needs very little of this -- a json file it reads every few minutes,
 * a picture now and then -- and `HttpURLConnection` is already in every
 * Android. Anything larger would mean shipping a networking stack inside
 * somebody else's apk, which is a great deal of weight for two requests.
 */
public final class Net {

    private Net() {}

    private static final int TIMEOUT = 15000;
    private static final int LIMIT = 4 * 1024 * 1024;

    /** Run something on a thread of the mod's own, named so it is findable. */
    public static void away(final String what, final Runnable work) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    work.run();
                } catch (Throwable error) {
                    Diary.note(what + ": " + error);
                }
            }
        }, "margyt-" + what);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    /** The bytes at a url, or null. Never throws, never runs on the main thread. */
    /** What came back, and whether it was worth coming back at all. */
    public static final class Answer {
        public final byte[] body;
        public final String tag;
        public final boolean unchanged;

        Answer(byte[] body, String tag, boolean unchanged) {
            this.body = body;
            this.tag = tag;
            this.unchanged = unchanged;
        }
    }

    /**
     * Fetch, saying what was fetched last time.
     *
     * A server that keeps an ETag can answer "the same as before" in a few
     * bytes, and a list of badges asked for every couple of minutes is the
     * same as before almost every time. Without this the mod would download
     * the whole list all day to learn nothing.
     */
    public static Answer fetch(String url, String tag) {
        java.net.HttpURLConnection link = null;
        try {
            link = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            link.setConnectTimeout(15000);
            link.setReadTimeout(20000);
            link.setRequestProperty("User-Agent", "MargyT");
            if (tag != null && tag.length() > 0) {
                link.setRequestProperty("If-None-Match", tag);
            }
            int code = link.getResponseCode();
            if (code == 304) return new Answer(null, tag, true);
            if (code != 200) return null;

            String fresh = link.getHeaderField("ETag");
            java.io.InputStream in = link.getInputStream();
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[16384];
            int got;
            while ((got = in.read(buffer)) > 0) out.write(buffer, 0, got);
            in.close();
            return new Answer(out.toByteArray(), fresh, false);
        } catch (Throwable error) {
            Diary.note("fetch: " + error);
            return null;
        } finally {
            if (link != null) link.disconnect();
        }
    }

    /**
     * Send some json and read the answer.
     *
     * Used for the few things a phone tells the server: which account it is,
     * and what that account wants shown.
     */
    public static String post(String url, String json) {
        java.net.HttpURLConnection link = null;
        try {
            link = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            link.setConnectTimeout(15000);
            link.setReadTimeout(20000);
            link.setRequestMethod("POST");
            link.setDoOutput(true);
            link.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            link.setRequestProperty("User-Agent", "MargyT");

            byte[] body = json.getBytes("UTF-8");
            link.setFixedLengthStreamingMode(body.length);
            java.io.OutputStream out = link.getOutputStream();
            out.write(body);
            out.close();

            int code = link.getResponseCode();
            java.io.InputStream in = code >= 400 ? link.getErrorStream()
                    : link.getInputStream();
            if (in == null) return null;
            java.io.ByteArrayOutputStream read = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int got;
            while ((got = in.read(buffer)) > 0) read.write(buffer, 0, got);
            in.close();
            String said = new String(read.toByteArray(), "UTF-8");
            if (code != 200) Diary.note("post " + url + ": " + code + " " + said);
            return code == 200 ? said : null;
        } catch (Throwable error) {
            Diary.note("post: " + error);
            return null;
        } finally {
            if (link != null) link.disconnect();
        }
    }

    /** A POST whose body is the thing itself, not json wrapped around it. */
    public static String send(String url, String kind, byte[] body) {
        java.net.HttpURLConnection link = null;
        try {
            link = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            link.setConnectTimeout(15000);
            link.setReadTimeout(60000);
            link.setRequestMethod("POST");
            link.setDoOutput(true);
            link.setRequestProperty("Content-Type", kind);
            link.setRequestProperty("User-Agent", "MargyT");
            link.setFixedLengthStreamingMode(body.length);
            java.io.OutputStream out = link.getOutputStream();
            out.write(body);
            out.close();

            int code = link.getResponseCode();
            java.io.InputStream in = code >= 400 ? link.getErrorStream() : link.getInputStream();
            if (in == null) return null;
            java.io.ByteArrayOutputStream read = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int got;
            while ((got = in.read(buffer)) > 0) read.write(buffer, 0, got);
            in.close();
            String said = new String(read.toByteArray(), "UTF-8");
            return code >= 400 ? null : said;
        } catch (Throwable error) {
            Diary.note("net: " + error);
            return null;
        } finally {
            if (link != null) link.disconnect();
        }
    }

    public static byte[] bytes(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "MargyT");
            if (connection.getResponseCode() / 100 != 2) return null;

            InputStream in = connection.getInputStream();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[16384];
                int read, total = 0;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > LIMIT) return null;  // nothing the mod wants is this big
                    out.write(buffer, 0, read);
                }
                return out.toByteArray();
            } finally {
                in.close();
            }
        } catch (Throwable ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static String text(String url) {
        byte[] raw = bytes(url);
        if (raw == null) return null;
        try {
            return new String(raw, "UTF-8");
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Told how far along a download is, in whole percent. */
    public interface Along {
        void at(int percent, long got, long total);
    }

    /**
     * Fetch straight to a file, saying how it is going.
     *
     * An apk is tens of megabytes, which is the one thing here worth watching
     * and the one thing not to hold in memory.
     */
    public static boolean download(String url, File into, Along along) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "MargyT");
            if (connection.getResponseCode() / 100 != 2) return false;

            long total = connection.getContentLength();
            File parent = into.getParentFile();
            if (parent != null && !parent.isDirectory()) parent.mkdirs();

            InputStream in = connection.getInputStream();
            OutputStream out = new FileOutputStream(into);
            try {
                byte[] buffer = new byte[65536];
                long got = 0;
                int read, told = -1;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    got += read;
                    int percent = total > 0 ? (int) (got * 100 / total) : -1;
                    if (along != null && percent != told) {
                        told = percent;
                        along.at(percent, got, total);
                    }
                }
            } finally {
                in.close();
                out.close();
            }
            return true;
        } catch (Throwable error) {
            Diary.note("download: " + error);
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /** Write bytes where they will still be after a restart. */
    public static boolean save(File file, byte[] data) {
        if (data == null) return false;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory()) parent.mkdirs();
            OutputStream out = new FileOutputStream(file);
            try {
                out.write(data);
            } finally {
                out.close();
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static byte[] read(File file) {
        try {
            if (!file.isFile()) return null;
            java.io.InputStream in = new java.io.FileInputStream(file);
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[16384];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                return out.toByteArray();
            } finally {
                in.close();
            }
        } catch (Throwable ignored) {
            return null;
        }
    }
}
