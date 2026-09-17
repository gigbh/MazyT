# The badge server

Badges used to be `badges.json` in this repository, re-read by every phone
every five minutes. That worked and cost nothing. It had two limits worth
leaving it for: granting a badge meant a commit, and nobody could decide
anything about their own -- which of theirs to show, and in what order.

## What is here

    badges.py    the service: the standard library, one sqlite file
    seed.py      fills it from badges.json, once
    badgectl     grant, take back, look up
    icons/       the pictures badges are drawn with

No framework, because there is nothing a framework would do here. nginx sits
in front for rate limiting; the service listens on localhost only.

## What it answers

    GET  /badges            what every phone reads, with an ETag
    POST /claim             a phone says which account it is, and is given a key
    POST /profile           that account decides what to show and in what order
    POST /old               the badge anyone running the mod before the 24th gets
    GET  /icon/<name>.png   the pictures

`/badges` is the same shape `badges.json` had, so the mod reads it the same
way. It carries `free_until` and `now` as well, which is how the app knows
whether the free badge is still being given out -- by the server's clock,
never by the phone's.

## On authentication

There is none, and there cannot be: TikTok will not tell a third party that
somebody is who they say they are. The first phone to claim an account id is
given a key for it and keeps it. That is enough to stop a passer-by
rearranging somebody else's badges, and not enough to stop somebody
determined. It is a picture beside a name.

What the server does not trust is anything that decides what is *given*:

- which badge `/old` grants is written here, not sent by the caller, so no
  request can ask for `owner` or any other;
- whether the day has passed is the server's clock;
- once a minute per account **and** per address, counted in the database, so
  a client that stopped counting gains nothing;
- nginx limits requests per address as well: reads and writes separately,
  since a flood would aim at writes.

## Running it

    systemctl status margyt-badges
    journalctl -u margyt-badges -f

    badgectl list
    badgectl grant supporter 7551880794956989495
    badgectl who   7551880794956989495
    badgectl take  supporter 7551880794956989495

## What is left to do

It answers over plain http. A domain and a certificate would fix that; until
then the badges are public anyway, and the one thing worth protecting -- the
key a phone is given -- is worth protecting properly rather than partly.
