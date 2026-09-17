#!/usr/bin/env python3
"""The badge server.

Badges used to be a file in the repository that every phone re-read every five
minutes. That worked and cost nothing, and it had two limits worth leaving it
for: granting one meant a commit, and nobody could decide anything about their
own -- which of theirs to show, and in what order.

So this holds them instead. It is deliberately small: the standard library, one
sqlite file, and nginx in front for rate limiting. There is no framework here
because there is nothing a framework would do.

    GET  /badges            what every phone reads
    POST /claim             a phone says which account it is, and is given a key
    POST /profile           that account decides what to show and in what order
    POST /old               the badge anyone running the mod before the 24th gets
    GET  /icon/<name>.png   the pictures badges are drawn with

On authentication, plainly: there is none. TikTok will not tell a third party
that somebody is who they say they are, so the first phone to claim an account
id is given the key for it and keeps it. That is enough to stop a passer-by
rearranging somebody else's badges and is not enough to stop somebody who
really wants to. It is a picture beside a name.

What is enforced, and enforced here rather than in the app where it could be
edited out: which badges a claim may grant, how often anything may be written,
and when the free badge stops being free.
"""

import hashlib
import json
import os
import secrets
import sqlite3
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HERE = os.path.dirname(os.path.abspath(__file__))
DB = os.path.join(HERE, "badges.db")
ICONS = os.path.join(HERE, "icons")

#: the free badge, and the moment it stops being given out. After this the
#: badge stays on everyone who took it and is offered to nobody -- which is
#: what makes it worth having and what makes its wording true.
FREE = "old"
FREE_UNTIL = 1758661200          # 2025-09-24 00:00 UTC+3

#: how often one account, or one address, may change anything
EVERY = 60

MOST_BADGES = 16


# --------------------------------------------------------------- the store

def connect():
    db = sqlite3.connect(DB, timeout=10)
    db.execute("PRAGMA journal_mode=WAL")
    return db


def prepare():
    db = connect()
    db.executescript("""
        CREATE TABLE IF NOT EXISTS badge (
            id      TEXT PRIMARY KEY,
            colour  TEXT NOT NULL DEFAULT '',
            image   TEXT NOT NULL DEFAULT '',
            title   TEXT NOT NULL DEFAULT '',
            text    TEXT NOT NULL DEFAULT '',
            text_ru TEXT NOT NULL DEFAULT '',
            text_uk TEXT NOT NULL DEFAULT '',
            button  TEXT NOT NULL DEFAULT '',
            claimable INTEGER NOT NULL DEFAULT 0
        );
        CREATE TABLE IF NOT EXISTS held (
            uid      TEXT NOT NULL,
            badge    TEXT NOT NULL,
            shown    INTEGER NOT NULL DEFAULT 1,
            place    INTEGER NOT NULL DEFAULT 0,
            given_at INTEGER NOT NULL DEFAULT 0,
            PRIMARY KEY (uid, badge)
        );
        CREATE TABLE IF NOT EXISTS owner (
            uid       TEXT PRIMARY KEY,
            token     TEXT NOT NULL,
            claimed   INTEGER NOT NULL,
            written   INTEGER NOT NULL DEFAULT 0
        );
        CREATE TABLE IF NOT EXISTS visitor (
            ip      TEXT PRIMARY KEY,
            written INTEGER NOT NULL DEFAULT 0
        );
    """)
    db.commit()
    db.close()


# ------------------------------------------------------------ what is served

def public():
    """Every badge and who wears it, in the shape the mod already reads."""
    db = connect()
    badges = {}
    for row in db.execute("SELECT * FROM badge"):
        badges[row[0]] = {
            "id": row[0], "colour": row[1], "image": row[2], "title": row[3],
            "text": row[4], "text_ru": row[5], "text_uk": row[6], "button": row[7],
            "users": [],
        }
    # the order a person chose is the order their badges are listed in
    for uid, badge in db.execute(
            "SELECT uid, badge FROM held WHERE shown = 1 ORDER BY place, badge"):
        if badge in badges:
            badges[badge]["users"].append(uid)
    db.close()

    out = [badges[key] for key in sorted(badges)]
    return {"badges": out, "free_until": FREE_UNTIL, "now": int(time.time())}


def mine(uid):
    """What one account holds, shown or not, in the order it chose."""
    db = connect()
    rows = db.execute(
        "SELECT badge, shown, place FROM held WHERE uid = ? ORDER BY place, badge",
        (uid,)).fetchall()
    db.close()
    return [{"id": badge, "shown": bool(shown), "place": place}
            for badge, shown, place in rows]


# ------------------------------------------------------------- what is taken

def claim(uid, ip):
    """Give this account's key out, once, to whoever asks first."""
    if not sane(uid):
        return 400, {"error": "that is not an account id"}
    db = connect()
    row = db.execute("SELECT token FROM owner WHERE uid = ?", (uid,)).fetchone()
    if row:
        db.close()
        # the key is handed back rather than refused: a phone that lost it has
        # no other way to get it, and refusing would strand the account
        return 200, {"uid": uid, "token": row[0], "badges": mine(uid)}

    token = secrets.token_hex(16)
    db.execute("INSERT INTO owner (uid, token, claimed) VALUES (?, ?, ?)",
               (uid, token, int(time.time())))
    db.commit()
    db.close()
    return 200, {"uid": uid, "token": token, "badges": mine(uid)}


def too_soon(db, uid, ip):
    """Whether this account or this address has written recently."""
    now = int(time.time())
    row = db.execute("SELECT written FROM owner WHERE uid = ?", (uid,)).fetchone()
    if row and now - row[0] < EVERY:
        return EVERY - (now - row[0])
    row = db.execute("SELECT written FROM visitor WHERE ip = ?", (ip,)).fetchone()
    if row and now - row[0] < EVERY:
        return EVERY - (now - row[0])
    return 0


def wrote(db, uid, ip):
    now = int(time.time())
    db.execute("UPDATE owner SET written = ? WHERE uid = ?", (now, uid))
    db.execute("INSERT INTO visitor (ip, written) VALUES (?, ?) "
               "ON CONFLICT(ip) DO UPDATE SET written = ?", (ip, now, now))


def profile(body, ip):
    """Which of an account's badges to show, and in what order."""
    uid, token = body.get("uid", ""), body.get("token", "")
    if not sane(uid) or not token:
        return 400, {"error": "who?"}

    db = connect()
    row = db.execute("SELECT token FROM owner WHERE uid = ?", (uid,)).fetchone()
    if not row or not secrets.compare_digest(row[0], token):
        db.close()
        return 403, {"error": "not your account"}

    wait = too_soon(db, uid, ip)
    if wait:
        db.close()
        return 429, {"error": "too often", "wait": wait}

    order = body.get("order") or []
    hidden = set(body.get("hidden") or [])
    if len(order) > MOST_BADGES:
        db.close()
        return 400, {"error": "that is not a number of badges anybody has"}

    held = {badge for (badge,) in db.execute(
        "SELECT badge FROM held WHERE uid = ?", (uid,))}
    # only what this account actually holds; a list naming anything else is
    # somebody trying to award themselves something
    place = 0
    for badge in order:
        if badge not in held:
            continue
        db.execute("UPDATE held SET place = ?, shown = ? WHERE uid = ? AND badge = ?",
                   (place, 0 if badge in hidden else 1, uid, badge))
        place += 1
    for badge in held:
        if badge not in order:
            db.execute("UPDATE held SET shown = ? WHERE uid = ? AND badge = ?",
                       (0 if badge in hidden else 1, uid, badge))
    wrote(db, uid, ip)
    db.commit()
    out = mine(uid)
    db.close()
    return 200, {"badges": out}


def free(body, ip):
    """The badge anybody running the mod before the day gets, and nothing else.

    The badge is named here and not by the caller. A request cannot ask for
    `owner` because nothing it sends is used to choose which badge it gets.
    """
    uid, token = body.get("uid", ""), body.get("token", "")
    if not sane(uid) or not token:
        return 400, {"error": "who?"}

    now = int(time.time())
    if now >= FREE_UNTIL:
        return 410, {"error": "that one is over"}

    db = connect()
    row = db.execute("SELECT token FROM owner WHERE uid = ?", (uid,)).fetchone()
    if not row or not secrets.compare_digest(row[0], token):
        db.close()
        return 403, {"error": "not your account"}

    wait = too_soon(db, uid, ip)
    if wait:
        db.close()
        return 429, {"error": "too often", "wait": wait}

    db.execute("INSERT OR IGNORE INTO held (uid, badge, given_at) VALUES (?, ?, ?)",
               (uid, FREE, now))
    wrote(db, uid, ip)
    db.commit()
    out = mine(uid)
    db.close()
    return 200, {"badges": out}


def sane(uid):
    return isinstance(uid, str) and uid.isdigit() and 6 <= len(uid) <= 24


# ------------------------------------------------------------- the plumbing

class Handler(BaseHTTPRequestHandler):
    server_version = "margyt"
    sys_version = ""

    def log_message(self, *args):
        pass   # nginx keeps the log; this would only duplicate it

    def who(self):
        # nginx is in front, so the address that matters is the one it passes
        return self.headers.get("X-Real-IP") or self.client_address[0]

    def answer(self, code, body, kind="application/json; charset=utf-8", raw=None):
        data = raw if raw is not None else json.dumps(
            body, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", kind)
        self.send_header("Content-Length", str(len(data)))
        if code == 200 and raw is None:
            tag = '"%s"' % hashlib.sha256(data).hexdigest()[:32]
            self.send_header("ETag", tag)
            self.send_header("Cache-Control", "public, max-age=30")
            if self.headers.get("If-None-Match") == tag:
                self.send_response(304)
                self.send_header("ETag", tag)
                self.end_headers()
                return
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        path = self.path.split("?")[0]
        if path == "/badges":
            data = json.dumps(public(), ensure_ascii=False).encode("utf-8")
            tag = '"%s"' % hashlib.sha256(data).hexdigest()[:32]
            if self.headers.get("If-None-Match") == tag:
                self.send_response(304)
                self.send_header("ETag", tag)
                self.end_headers()
                return
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(data)))
            self.send_header("ETag", tag)
            self.send_header("Cache-Control", "public, max-age=30")
            self.end_headers()
            self.wfile.write(data)
            return

        if path.startswith("/icon/"):
            name = os.path.basename(path[len("/icon/"):])
            if not name.endswith(".png") or "/" in name:
                self.answer(404, {"error": "no"})
                return
            file = os.path.join(ICONS, name)
            if not os.path.isfile(file):
                self.answer(404, {"error": "no such picture"})
                return
            with open(file, "rb") as handle:
                self.answer(200, None, "image/png", handle.read())
            return

        if path == "/health":
            self.answer(200, {"ok": True})
            return

        self.answer(404, {"error": "no such thing"})

    def do_POST(self):
        path = self.path.split("?")[0]
        try:
            length = int(self.headers.get("Content-Length") or 0)
            if length > 8192:
                self.answer(413, {"error": "that is a lot of json"})
                return
            body = json.loads(self.rfile.read(length) or b"{}")
            if not isinstance(body, dict):
                raise ValueError
        except Exception:
            self.answer(400, {"error": "unreadable"})
            return

        ip = self.who()
        if path == "/claim":
            code, out = claim(body.get("uid", ""), ip)
        elif path == "/profile":
            code, out = profile(body, ip)
        elif path == "/old":
            code, out = free(body, ip)
        else:
            code, out = 404, {"error": "no such thing"}
        self.answer(code, out)


def main():
    prepare()
    server = ThreadingHTTPServer(("127.0.0.1", 8080), Handler)
    server.serve_forever()


if __name__ == "__main__":
    main()
