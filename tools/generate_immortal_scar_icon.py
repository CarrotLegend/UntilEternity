from pathlib import Path
import struct
import zlib

WIDTH = 18
HEIGHT = 18
OUTPUT = (
    Path(__file__).resolve().parents[1]
    / "src" / "main" / "resources" / "assets"
    / "until_eternity" / "textures" / "mob_effect"
    / "immortal_scar.png"
)

TRANSPARENT = (0, 0, 0, 0)
SHADOW = (0x20, 0x08, 0x1D, 0xFF)
DEEP_VIOLET = (0x4B, 0x14, 0x3F, 0xFF)
CRIMSON = (0x86, 0x20, 0x4D, 0xFF)
MAGENTA = (0xCC, 0x3B, 0x79, 0xFF)
CORE = (0xFF, 0xA8, 0xCE, 0xFF)


def paint(pixels, color, points):
    for x, y in points:
        pixels[y * WIDTH + x] = color


def png_chunk(kind, data):
    return (struct.pack(">I", len(data)) + kind + data
            + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF))


def save_png(path, pixels):
    rows = bytearray()
    for y in range(HEIGHT):
        rows.append(0)
        for pixel in pixels[y * WIDTH:(y + 1) * WIDTH]:
            rows.extend(pixel)
    data = (b"\x89PNG\r\n\x1a\n"
            + png_chunk(b"IHDR", struct.pack(">IIBBBBB", WIDTH, HEIGHT,
                                                8, 6, 0, 0, 0))
            + png_chunk(b"IDAT", zlib.compress(bytes(rows), 9))
            + png_chunk(b"IEND", b""))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def main():
    pixels = [TRANSPARENT] * (WIDTH * HEIGHT)
    paint(pixels, SHADOW, {
        (7, 1), (8, 1), (6, 2), (7, 2), (8, 2), (9, 2),
        (5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3),
        (4, 4), (5, 4), (6, 4), (9, 4), (10, 4), (11, 4),
        (3, 5), (4, 5), (5, 5), (10, 5), (11, 5), (12, 5),
        (2, 6), (3, 6), (4, 6), (11, 6), (12, 6), (13, 6),
        (2, 7), (3, 7), (12, 7), (13, 7), (14, 7),
        (2, 8), (3, 8), (13, 8), (14, 8), (15, 8),
        (2, 9), (3, 9), (13, 9), (14, 9), (15, 9),
        (2, 10), (3, 10), (12, 10), (13, 10), (14, 10),
        (3, 11), (4, 11), (11, 11), (12, 11), (13, 11),
        (4, 12), (5, 12), (10, 12), (11, 12), (12, 12),
        (5, 13), (6, 13), (9, 13), (10, 13), (11, 13),
        (6, 14), (7, 14), (8, 14), (9, 14), (10, 14),
        (7, 15), (8, 15), (9, 15), (8, 16),
    })
    paint(pixels, DEEP_VIOLET, {
        (7, 2), (6, 3), (8, 3), (10, 3), (5, 4), (7, 4),
        (9, 4), (11, 4), (4, 5), (6, 5), (10, 5), (12, 5),
        (3, 6), (5, 6), (11, 6), (13, 6), (3, 7), (12, 7),
        (14, 7), (3, 8), (13, 8), (14, 9), (3, 10), (12, 10),
        (4, 11), (11, 11), (5, 12), (10, 12), (6, 13),
        (9, 13), (7, 14), (9, 14), (8, 15),
    })
    paint(pixels, CRIMSON, {
        (8, 4), (7, 5), (9, 5), (6, 6), (8, 6), (10, 6),
        (5, 7), (7, 7), (9, 7), (11, 7), (4, 8), (6, 8),
        (8, 8), (10, 8), (12, 8), (5, 9), (7, 9), (9, 9),
        (11, 9), (6, 10), (8, 10), (10, 10), (7, 11),
        (9, 11), (8, 12),
    })
    paint(pixels, MAGENTA, {
        (8, 5), (7, 6), (9, 6), (6, 7), (8, 7), (10, 7),
        (5, 8), (7, 8), (9, 8), (11, 8), (6, 9), (8, 9),
        (10, 9), (7, 10), (9, 10), (8, 11),
    })
    paint(pixels, CORE, {
        (8, 6), (7, 7), (8, 8), (7, 9), (8, 10),
    })
    save_png(OUTPUT, pixels)


if __name__ == "__main__":
    main()
