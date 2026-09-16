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
HEADS = [(40.7, 71.2, 8.6, 6.2), (67.3, 64.9, 8.6, 6.2)]   # cx, cy, rx, ry
STEMS = [(44.6, 35.3, 4.7, 35.9), (71.2, 29.0, 4.7, 35.9)]  # x, y, w, h
BEAM = [(44.6, 34.5), (75.8, 26.7), (75.8, 36.1), (44.6, 43.9)]

#: how round the square is when it is drawn as a plain bitmap. An adaptive icon
#: is masked by the launcher and must not be rounded here; everything older is
#: shown as it comes, and a hard square looks like a mistake beside the rest.
CORNER = 0.18


def _ellipse(cx: float, cy: float, rx: float, ry: float) -> str:
    return ("M%.1f,%.1f a%.1f,%.1f 0 1,0 %.1f,0 a%.1f,%.1f 0 1,0 -%.1f,0 z"
            % (cx - rx, cy, rx, ry, rx * 2, rx, ry, rx * 2))


def _rect(x: float, y: float, w: float, h: float) -> str:
    return "M%.1f,%.1f h%.1f v%.1f h-%.1f z" % (x, y, w, h, w)


def _polygon(points) -> str:
    out = "M%.1f,%.1f" % points[0]
    for point in points[1:]:
        out += " L%.1f,%.1f" % point
    return out + " Z"


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
