"""What MargyT looks like.

The mint is the Margy banner's background, straight off `assets/banner.png` in
narezany/Margelet; the glyph is a note where Margy has a paper plane. The paths
are drawn in a 108-unit square, the same viewport an adaptive icon uses, and
all of the glyph sits inside the middle 72 units -- the safe zone, the part no
launcher mask can cut off.
"""

MINT = 0xFF8DD1B0
INK = 0xFF1C2C24
WHITE = 0xFFFFFFFF

VIEWPORT = 108

BACKGROUND = [(MINT, "M0,0 h108 v108 h-108 z")]

# The shapes as numbers, because two things need them: the vector the adaptive
# icon is built from, and the bitmap everything older is scaled down from. The
# paths below are written out of these rather than beside them, so the two can
# never drift apart.
#
# Two heads, two stems, one beam, in the 108-unit square.
#: The note, bigger and set a touch to the left and down.
#:
#: It was drawn to fit inside a circle of radius 36 from the centre, which is
#: the safe zone, and that left it looking lost in the square. It is now the
#: same shape scaled by a fifth, which still clears the mask, and moved a
#: little towards the lower left -- a note is heavier on its right and its
#: heads sit low, so centring it by its bounding box makes it look as though it
#: is drifting up and right. Off-centre by the numbers, centred to the eye.
_GROW = 1.1
_SHIFT = (-1.6, 1.8)


def _place(x: float, y: float):
    """A point of the original drawing, scaled and moved about the centre."""
    middle = VIEWPORT / 2.0
    return (middle + (x - middle) * _GROW + _SHIFT[0],
            middle + (y - middle) * _GROW + _SHIFT[1])


def _placed_ellipse(cx, cy, rx, ry):
    x, y = _place(cx, cy)
    return (x, y, rx * _GROW, ry * _GROW)


def _placed_rect(x, y, w, h):
    left, top = _place(x, y)
    return (left, top, w * _GROW, h * _GROW)


HEADS = [_placed_ellipse(40.7, 71.2, 8.6, 6.2),
         _placed_ellipse(67.3, 64.9, 8.6, 6.2)]          # cx, cy, rx, ry
STEMS = [_placed_rect(44.6, 35.3, 4.7, 35.9),
         _placed_rect(71.2, 29.0, 4.7, 35.9)]            # x, y, w, h
BEAM = [_place(44.6, 34.5), _place(75.8, 26.7),
        _place(75.8, 36.1), _place(44.6, 43.9)]

#: how round the square is when it is drawn as a plain bitmap. An adaptive icon
#: is masked by the launcher and must not be rounded here; everything older is
#: shown as it comes, and a hard square looks like a mistake beside the rest.
CORNER = 0.18


def _ellipse(cx: float, cy: float, rx: float, ry: float) -> str:
    return ("M%.1f,%.1f a%.1f,%.1f 0 1,0 %.1f,0 a%.1f,%.1f 0 1,0 -%.1f,0 z"
            % (cx - rx, cy, rx, ry, rx * 2, rx, ry, rx * 2))


#: how much is taken off the note's own corners, in the 108-unit square.
#:
#: The bitmap gets this by blurring the whole shape and cutting the blur in
#: half, which rounds the inside of a join as well as the outside of a tip.
#: A vector has no such trick, so each corner is drawn as an arc -- which
#: covers the corners that show and leaves the joins, where one shape is
#: already inside another and nothing shows anyway.
ROUND = 1.4


def _rect(x: float, y: float, w: float, h: float, r: float = ROUND) -> str:
    r = min(r, w / 2.0, h / 2.0)
    return ("M%.2f,%.2f h%.2f a%.2f,%.2f 0 0 1 %.2f,%.2f v%.2f "
            "a%.2f,%.2f 0 0 1 -%.2f,%.2f h-%.2f a%.2f,%.2f 0 0 1 -%.2f,-%.2f "
            "v-%.2f a%.2f,%.2f 0 0 1 %.2f,-%.2f z"
            % (x + r, y, w - 2 * r, r, r, r, r, h - 2 * r,
               r, r, r, r, w - 2 * r, r, r, r, r,
               h - 2 * r, r, r, r, r))


def _polygon(points, r: float = ROUND) -> str:
    """A shape whose corners are arcs rather than points."""
    import math

    many = len(points)
    out = ""
    for i in range(many):
        before = points[(i - 1) % many]
        here = points[i]
        after = points[(i + 1) % many]

        into = _towards(here, before, r)
        away = _towards(here, after, r)
        if not out:
            out = "M%.2f,%.2f" % into
        else:
            out += " L%.2f,%.2f" % into
        # which way the corner turns decides which of the two arcs to draw
        turn = ((here[0] - before[0]) * (after[1] - here[1])
                - (here[1] - before[1]) * (after[0] - here[0]))
        out += " a%.2f,%.2f 0 0 %d %.2f,%.2f" % (
            r, r, 1 if turn > 0 else 0, away[0] - into[0], away[1] - into[1])
    return out + " Z"


def _towards(here, there, r: float):
    import math

    dx, dy = there[0] - here[0], there[1] - here[1]
    length = math.hypot(dx, dy) or 1.0
    step = min(r, length / 2.0)
    return (here[0] + dx / length * step, here[1] + dy / length * step)


# The beam is slanted, so its top edge at the right stem's left side once sat
# *below* that stem's flat top and a corner stuck out above it -- it now clears
# both stems along their whole width. And the glyph sits inside a radius of 36
# from the centre, the safe zone, so no launcher mask reaches it.
GLYPH = (
    [(WHITE, _ellipse(*head)) for head in HEADS]
    + [(WHITE, _rect(*stem)) for stem in STEMS]
    + [(WHITE, _polygon(BEAM))]
)

# for an icon that is one drawable rather than two layers
COMBINED = BACKGROUND + GLYPH

# the bitmap the legacy densities are scaled down from
MASTER_PNG = "icon_out/play/ic_launcher-512.png"
