#!/usr/bin/env python3
"""The MargyT bot.

Three things, and they are all lookups: whose badges are these, whose TikTok
is this, and here is an icon I drew.

It talks to Telegram by long polling rather than a webhook, which needs no
certificate and no port open -- the bot asks Telegram for what happened and
Telegram answers when something has. On a box that is already serving badges
over plain http, that is one less thing to get wrong.

The token lives in `bot.txt` beside the database, written by hand, never here.

On looking up a TikTok account by its @name: there is no API for that, so the
profile page is read and the id taken out of the blob the page carries for its
own use. That works today and is nobody's promise -- when it stops, the bot
says it cannot find them rather than pretending.
"""

import html
import json
import os
import re
import sqlite3
import sys
import time
import urllib.parse
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import badges as service

TOKEN_FILE = os.path.join(HERE, "bot.txt")

#: who receives an icon somebody offers
OWNER = 7826361017

API = "https://api.telegram.org/bot%s/%s"

BROWSER = ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
           " (KHTML, like Gecko) Chrome/124.0 Safari/537.36")


def token():
    with open(TOKEN_FILE, encoding="utf-8") as handle:
        return handle.read().strip()


# ---------------------------------------------------------------- the store

def prepare():
    service.prepare()
    db = service.connect()
    db.executescript("""
        CREATE TABLE IF NOT EXISTS linked (
            tg       INTEGER PRIMARY KEY,
            tg_name  TEXT NOT NULL DEFAULT '',
            uid      TEXT NOT NULL,
            username TEXT NOT NULL DEFAULT '',
            nickname TEXT NOT NULL DEFAULT '',
            avatar   TEXT NOT NULL DEFAULT '',
            updated  INTEGER NOT NULL DEFAULT 0
        );
        CREATE TABLE IF NOT EXISTS seen_update (
            id INTEGER PRIMARY KEY
        );
    """)
    db.commit()
    db.close()


def badges_of(uid):
    """Which badges an account wears, in its own order."""
    db = service.connect()
    rows = db.execute(
        "SELECT b.id, b.text_ru, b.text, h.shown FROM held h"
        " JOIN badge b ON b.id = h.badge WHERE h.uid = ?"
        " ORDER BY h.place, h.badge", (uid,)).fetchall()
    db.close()
    return [{"id": r[0], "text": r[1] or r[2] or r[0], "shown": bool(r[3])}
            for r in rows]


def link(tg, tg_name, uid, who):
    db = service.connect()
    db.execute(
        "INSERT INTO linked (tg, tg_name, uid, username, nickname, avatar, updated)"
        " VALUES (?,?,?,?,?,?,?) ON CONFLICT(tg) DO UPDATE SET tg_name=excluded.tg_name,"
        " uid=excluded.uid, username=excluded.username, nickname=excluded.nickname,"
        " avatar=excluded.avatar, updated=excluded.updated",
        (tg, tg_name or "", uid, who.get("username", ""), who.get("nickname", ""),
         who.get("avatar", ""), int(time.time())))
    db.commit()
    db.close()


def linked_by_tg(tg):
    db = service.connect()
    row = db.execute(
        "SELECT uid, username, nickname, avatar FROM linked WHERE tg = ?",
        (tg,)).fetchone()
    db.close()
    return None if not row else {"uid": row[0], "username": row[1],
                                 "nickname": row[2], "avatar": row[3]}


def linked_by_name(name):
    db = service.connect()
    row = db.execute(
        "SELECT uid, username, nickname, avatar FROM linked"
        " WHERE lower(tg_name) = lower(?)", (name.lstrip("@"),)).fetchone()
    db.close()
    return None if not row else {"uid": row[0], "username": row[1],
                                 "nickname": row[2], "avatar": row[3]}


# ------------------------------------------------------------ looking up

def account(which):
    """An account from an id or an @name, as far as anything can tell.

    An id is taken as it is: TikTok will not confirm one and nothing here
    needs it to. A name is looked up on the profile page, which is the only
    way there is.
    """
    which = which.strip().lstrip("@")
    if which.isdigit() and 6 <= len(which) <= 24:
        return {"uid": which, "username": "", "nickname": "", "avatar": ""}
    return by_name(which)


def by_name(name):
    if not re.match(r"^[A-Za-z0-9._]{2,24}$", name):
        return None
    try:
        request = urllib.request.Request(
            "https://www.tiktok.com/@" + urllib.parse.quote(name),
            headers={"User-Agent": BROWSER, "Accept-Language": "en"})
        with urllib.request.urlopen(request, timeout=20) as answer:
            page = answer.read(900000).decode("utf-8", "replace")
    except Exception:
        return None

    found = re.search(r'"user":\{"id":"(\d+)"', page)
    if not found:
        found = re.search(r'"id":"(\d{6,24})","shortId"', page)
    if not found:
        return None

    return {
        "uid": found.group(1),
        "username": name,
        "nickname": one(page, r'"nickname":"(.*?)"'),
        "avatar": one(page, r'"avatarMedium":"(.*?)"').replace("\\u002F", "/"),
    }


def one(page, pattern):
    found = re.search(pattern, page)
    if not found:
        return ""
    try:
        return json.loads('"%s"' % found.group(1))
    except Exception:
        return found.group(1)


# ------------------------------------------------------------ what is said

def badge_lines(uid):
    worn = badges_of(uid)
    if not worn:
        return "Значков нет."
    out = []
    for badge in worn:
        mark = "" if badge["shown"] else "  (скрыт)"
        out.append("• %s%s" % (badge["text"], mark))
    return "\n".join(out)


def fresher(who):
    """The stored profile, with a new avatar address if one can be had.

    A TikTok avatar address is signed and stops working after a while, so the
    one saved when somebody linked their account is no use a week later. The
    name is enough to look the current one up.
    """
    if not who.get("username"):
        return who
    now = by_name(who["username"])
    return now or who


def profile_card(who):
    lines = []
    if who.get("nickname"):
        lines.append("<b>%s</b>" % html.escape(who["nickname"]))
    if who.get("username"):
        lines.append("@%s" % html.escape(who["username"]))
    lines.append("<code>%s</code>" % html.escape(who["uid"]))
    lines.append("")
    lines.append(badge_lines(who["uid"]))
    return "\n".join(lines)


# ------------------------------------------------------------- the talking

def call(what, **fields):
    data = json.dumps(fields).encode("utf-8")
    request = urllib.request.Request(
        API % (token(), what), data=data,
        headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(request, timeout=70) as answer:
            return json.loads(answer.read().decode("utf-8"))
    except Exception as trouble:
        if what != "getUpdates":
            print("%s: %s" % (what, trouble), flush=True)
        return {}


def say(chat, text, reply=None):
    call("sendMessage", chat_id=chat, text=text, parse_mode="HTML",
         disable_web_page_preview=True,
         **({"reply_to_message_id": reply} if reply else {}))


def show(chat, who, reply=None):
    """A profile with its picture, when there is one to show.

    The avatar comes off the same page the id did, so there is nothing extra
    to fetch and nothing to store: the address is handed to Telegram and
    Telegram fetches it. If that fails -- the address is signed and does
    expire -- the words go out on their own rather than nothing going out.
    """
    card = profile_card(who)
    avatar = who.get("avatar") or ""
    if avatar.startswith("http"):
        answer = call("sendPhoto", chat_id=chat, photo=avatar, caption=card,
                      parse_mode="HTML",
                      **({"reply_to_message_id": reply} if reply else {}))
        if answer.get("ok"):
            return
    say(chat, card, reply)


HELP = (
    "<b>MargyT</b>\n\n"
    "<b>бейджи</b> &lt;айди или @имя&gt; — чьи это значки\n"
    "<b>я</b> &lt;айди или @имя&gt; — привязать свой профиль\n"
    "<b>что за профиль</b> — ответом на сообщение, или с @именем в телеграме\n"
    "<b>иконка</b> — пришли картинку документом и свой айди, попадёт владельцу\n\n"
    "В любом чате можно написать <code>@%s имя</code> — покажет то же самое."
)


def handle(update):
    if "inline_query" in update:
        return inline(update["inline_query"])

    message = update.get("message") or update.get("channel_post")
    if not message:
        return

    chat = message["chat"]["id"]
    from_who = message.get("from") or {}
    text = (message.get("text") or message.get("caption") or "").strip()
    low = text.lower()

    # A picture is an offered icon only where it can be nothing else: in a
    # private chat, or with the word in the caption. In a group every photo
    # anybody posts is not an offer, and answering each one with instructions
    # is the bot being in the way.
    if message.get("document") or message.get("photo"):
        private = (message["chat"].get("type") == "private")
        if private or low.startswith("иконка") or low.startswith("icon"):
            return offered(message, chat, from_who, text)
        return

    if low in ("/start", "/help", "помощь"):
        me = call("getMe").get("result", {}).get("username", "margyt_bot")
        return say(chat, HELP % me)

    if low.startswith("бейджи") or low.startswith("значки") or low.startswith("/badges"):
        which = text.split(maxsplit=1)[1].strip() if len(text.split()) > 1 else ""
        if not which:
            return say(chat, "Кого смотрим? Напиши айди или @имя.")
        who = account(which)
        if not who:
            return say(chat, "Не нашёл такого.")
        return show(chat, who, message["message_id"])

    if low.startswith("я ") or low.startswith("/link"):
        which = text.split(maxsplit=1)[1].strip() if len(text.split()) > 1 else ""
        who = account(which) if which else None
        if not who:
            return say(chat, "Напиши так: <code>я 7551880794956989495</code> "
                             "или <code>я @narezany</code>")
        link(from_who.get("id"), from_who.get("username", ""), who["uid"], who)
        return show(chat, who)

    if low.startswith("что за профиль") or low.startswith("/who"):
        reply = message.get("reply_to_message")
        if reply:
            found = linked_by_tg((reply.get("from") or {}).get("id"))
            if not found:
                return say(chat, "Этот человек не привязывал профиль.",
                           message["message_id"])
            return show(chat, fresher(found), message["message_id"])

        parts = text.split()
        if len(parts) >= 4:
            found = linked_by_name(parts[3])
            if not found:
                return say(chat, "Не знаю такого.", message["message_id"])
            return show(chat, fresher(found), message["message_id"])
        return say(chat, "Ответь этой командой на чьё-нибудь сообщение "
                         "или добавь @имя в телеграме.", message["message_id"])

    if low.startswith("иконка"):
        return say(chat, "Пришли картинку <b>документом</b>, а в подписи — "
                         "свой айди в тиктоке.")


def offered(message, chat, from_who, caption):
    """An icon somebody drew, on its way to whoever decides."""
    which = ""
    for piece in caption.replace(",", " ").split():
        if piece.strip().isdigit() and 6 <= len(piece.strip()) <= 24:
            which = piece.strip()
            break

    if not which:
        return say(chat, "Добавь в подпись свой айди в тиктоке — иначе непонятно, "
                         "кому потом выдавать значок.", message["message_id"])

    who = "@" + from_who["username"] if from_who.get("username") else str(from_who.get("id"))
    call("forwardMessage", chat_id=OWNER, from_chat_id=chat,
         message_id=message["message_id"])
    say(OWNER, "Иконка от %s\nТикток: <code>%s</code>" % (html.escape(who), which))
    return say(chat, "Отправил владельцу. Если возьмут — значок за иконку твой.",
               message["message_id"])


def inline(query):
    which = (query.get("query") or "").strip()
    results = []
    if which:
        who = account(which) or linked_by_name(which)
        if who:
            worn = badges_of(who["uid"])
            title = who.get("nickname") or ("@" + who["username"] if who.get("username")
                                            else who["uid"])
            results.append({
                "type": "article",
                "id": who["uid"][:60],
                "title": title,
                "description": ("%d значк(ов)" % len(worn)) if worn else "значков нет",
                "thumb_url": who.get("avatar") or "",
                "input_message_content": {
                    "message_text": profile_card(who),
                    "parse_mode": "HTML",
                    "disable_web_page_preview": True,
                },
            })
    call("answerInlineQuery", inline_query_id=query["id"], results=results,
         cache_time=30, is_personal=False)


def main():
    prepare()
    print("bot up", flush=True)
    offset = 0
    while True:
        answer = call("getUpdates", offset=offset, timeout=50,
                      allowed_updates=["message", "inline_query"])
        for update in answer.get("result", []):
            offset = update["update_id"] + 1
            try:
                handle(update)
            except Exception as trouble:
                print("update %s: %s" % (update.get("update_id"), trouble), flush=True)
        if not answer:
            time.sleep(3)


if __name__ == "__main__":
    main()
