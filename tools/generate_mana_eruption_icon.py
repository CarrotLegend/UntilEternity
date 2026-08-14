from pathlib import Path
import struct
import zlib

WIDTH = 18
HEIGHT = 18
OUTPUT = (
    Path(__file__).resolve().parents[1]
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "until_eternity"
    / "textures"
    / "mob_effect"
    / "mana_eruption.png"
)

TRANSPARENT = (0, 0, 0, 0)
DEEP_PURPLE = (0x26, 0x10, 0x4F, 0xFF)
PURPLE = (0x5B, 0x2C, 0xCF, 0xFF)
BRIGHT_PURPLE = (0x86, 0x5C, 0xFF, 0xFF)
CYAN = (0x42, 0xE5, 0xFF, 0xFF)
BRIGHT_CYAN = (0xA8, 0xF7, 0xFF, 0xFF)
WHITE = (0xF4, 0xFF, 0xFF, 0xFF)


def paint(
        pixels: list[tuple[int, int, int, int]],
        color: tuple[int, int, int, int],
        points: set[tuple[int, int]]) -> None:
    for x, y in points:
        pixels[y * WIDTH + x] = color


def png_chunk(kind: bytes, data: bytes) -> bytes:
    return (
        struct.pack(">I", len(data))
        + kind
        + data
        + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)
    )


def save_rgba_png(
        path: Path,
        pixels: list[tuple[int, int, int, int]]) -> None:
    rows = bytearray()
    for y in range(HEIGHT):
        rows.append(0)
        for pixel in pixels[y * WIDTH:(y + 1) * WIDTH]:
            rows.extend(pixel)
    png = (
        b"\x89PNG\r\n\x1a\n"
        + png_chunk(
            b"IHDR",
            struct.pack(">IIBBBBB", WIDTH, HEIGHT, 8, 6, 0, 0, 0))
        + png_chunk(b"IDAT", zlib.compress(bytes(rows), level=9))
        + png_chunk(b"IEND", b"")
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def main() -> None:
    pixels = [TRANSPARENT] * (WIDTH * HEIGHT)

    paint(pixels, DEEP_PURPLE, {
        (8, 0), (9, 0), (7, 1), (10, 1),
        (3, 2), (14, 2), (4, 3), (13, 3),
        (1, 6), (16, 6), (0, 8), (17, 8), (0, 9), (17, 9),
        (2, 13), (15, 13), (3, 15), (14, 15),
        (7, 16), (10, 16), (8, 17), (9, 17),
        (5, 5), (6, 4), (7, 4), (8, 3), (9, 3), (10, 4), (11, 4), (12, 5),
        (4, 6), (13, 6), (3, 8), (3, 9), (14, 8), (14, 9),
        (4, 11), (13, 11), (5, 12), (6, 13), (7, 13), (8, 14),
        (9, 14), (10, 13), (11, 13), (12, 12),
    })
    paint(pixels, PURPLE, {
        (8, 1), (9, 1), (8, 2), (9, 2),
        (4, 2), (13, 2), (4, 4), (13, 4),
        (1, 7), (2, 7), (15, 7), (16, 7),
        (1, 10), (2, 10), (15, 10), (16, 10),
        (3, 13), (14, 13), (4, 14), (13, 14),
        (7, 15), (8, 16), (9, 16), (10, 15),
        (6, 5), (7, 5), (8, 4), (9, 4), (10, 5), (11, 5),
        (5, 6), (12, 6), (4, 8), (4, 9), (13, 8), (13, 9),
        (5, 11), (12, 11), (6, 12), (7, 12), (8, 13),
        (9, 13), (10, 12), (11, 12),
    })
    paint(pixels, BRIGHT_PURPLE, {
        (5, 3), (12, 3), (3, 5), (14, 5),
        (2, 8), (2, 9), (15, 8), (15, 9),
        (4, 12), (13, 12), (6, 14), (11, 14),
        (7, 6), (8, 5), (9, 5), (10, 6),
        (6, 7), (11, 7), (5, 8), (12, 8),
        (5, 9), (12, 9), (6, 10), (11, 10),
        (7, 11), (8, 12), (9, 12), (10, 11),
    })
    paint(pixels, CYAN, {
        (8, 6), (9, 6), (7, 7), (10, 7),
        (6, 8), (11, 8), (6, 9), (11, 9),
        (7, 10), (10, 10), (8, 11), (9, 11),
    })
    paint(pixels, BRIGHT_CYAN, {
        (8, 7), (9, 7), (7, 8), (10, 8),
        (7, 9), (10, 9), (8, 10), (9, 10),
    })
    paint(pixels, WHITE, {
        (8, 8), (9, 8), (8, 9), (9, 9),
    })

    save_rgba_png(OUTPUT, pixels)


if __name__ == "__main__":
    main()
