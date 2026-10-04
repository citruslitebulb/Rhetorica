#!/usr/bin/env python3
"""Rasterize Rhetorica PWA icons: a gold column on burgundy ink."""

from __future__ import annotations

import math
import struct
import zlib
from pathlib import Path

GOLD = (212, 175, 55)
GOLD_DEEP = (122, 86, 28)
CREAM = (245, 240, 230)
INK = (28, 36, 51)


def clamp(value: float, low: float = 0.0, high: float = 1.0) -> float:
    return max(low, min(high, value))


def mix(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))  # type: ignore[return-value]


def sd_circle(u: float, v: float, cx: float, cy: float, r: float) -> float:
    return math.hypot(u - cx, v - cy) - r


def sd_box(u: float, v: float, cx: float, cy: float, hx: float, hy: float) -> float:
    dx = abs(u - cx) - hx
    dy = abs(v - cy) - hy
    return math.hypot(max(dx, 0.0), max(dy, 0.0)) + min(max(dx, dy), 0.0)


def cover(sd_norm: float, size: int) -> float:
    return clamp(0.5 - sd_norm * size)


def over(dst: tuple[int, int, int], src: tuple[int, int, int], alpha: float) -> tuple[int, int, int]:
    if alpha <= 0:
        return dst
    if alpha >= 1:
        return src
    return mix(dst, src, alpha)


def sample(x: float, y: float, size: int, maskable: bool) -> tuple[int, int, int]:
    u = x / size
    v = y / size
    t = v
    color = mix((58, 28, 36), (16, 12, 11), t)
    nx = u * 2 - 1
    ny = v * 2 - 1
    vignette = clamp((nx * nx + ny * ny) / 1.35)
    color = mix(color, (8, 6, 6), vignette * 0.55)

    scale = 0.72 if maskable else 0.86

    def sx(du: float) -> float:
        return 0.5 + du * scale

    def sy(dv: float) -> float:
        return 0.52 + dv * scale

    medal = sd_circle(u, v, 0.5, 0.50, 0.34 * scale)
    color = over(color, INK, cover(medal, size))
    ring = abs(sd_circle(u, v, 0.5, 0.50, 0.34 * scale)) - 0.012 * scale
    color = over(color, GOLD, cover(ring, size))

    def column(du: float, dv: float, hx: float, hy: float, fill: tuple[int, int, int]) -> None:
        nonlocal color
        sd = sd_box(u, v, sx(du), sy(dv), hx * scale, hy * scale)
        color = over(color, fill, cover(sd, size))

    column(0, 0.30, 0.22, 0.028, GOLD)
    column(0, 0.24, 0.17, 0.018, CREAM)
    column(0, 0.02, 0.078, 0.175, CREAM)
    # Shaft highlight and flutes.
    shade = sd_box(u, v, sx(0), sy(0.02), 0.078 * scale, 0.175 * scale)
    if shade < 0:
        flute_xs = (-0.045, 0.0, 0.045)
        for flute in flute_xs:
            sd = sd_box(u, v, sx(flute), sy(0.02), 0.006 * scale, 0.15 * scale)
            color = over(color, GOLD_DEEP, cover(sd, size) * 0.85)
        highlight = sd_box(u, v, sx(-0.03), sy(0.02), 0.012 * scale, 0.15 * scale)
        color = over(color, (255, 236, 196), cover(highlight, size) * 0.35)

    column(0, -0.16, 0.15, 0.02, GOLD)
    column(0, -0.19, 0.20, 0.016, CREAM)
    for side in (-1, 1):
        sd = sd_circle(u, v, sx(0.16 * side), sy(-0.155), 0.048 * scale)
        color = over(color, GOLD, cover(sd, size))
        inner = sd_circle(u, v, sx(0.16 * side), sy(-0.155), 0.02 * scale)
        color = over(color, INK, cover(inner, size))
    column(0, -0.22, 0.22, 0.014, GOLD)
    return color


def render(size: int, maskable: bool) -> bytes:
    samples = 2
    raw = bytearray()
    step = 1 / samples
    for y in range(size):
        raw.append(0)
        for x in range(size):
            acc = [0, 0, 0]
            for oy in range(samples):
                for ox in range(samples):
                    px = x + (ox + 0.5) * step
                    py = y + (oy + 0.5) * step
                    pixel = sample(px, py, size, maskable)
                    acc[0] += pixel[0]
                    acc[1] += pixel[1]
                    acc[2] += pixel[2]
            count = samples * samples
            raw.extend((acc[0] // count, acc[1] // count, acc[2] // count, 255))
    return bytes(raw)


def write_png(path: Path, size: int, rgba: bytes) -> None:
    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(rgba, 9)) + chunk(b"IEND", b"")
    path.write_bytes(png)


def main() -> None:
    out = Path(__file__).resolve().parents[1] / "public" / "icons"
    out.mkdir(parents=True, exist_ok=True)
    jobs = [
        ("icon-192.png", 192, False),
        ("icon-512.png", 512, False),
        ("icon-maskable-512.png", 512, True),
        ("apple-touch-icon.png", 180, False),
    ]
    for name, size, maskable in jobs:
        print(f"rendering {name}")
        write_png(out / name, size, render(size, maskable))
    print("wrote", out)


if __name__ == "__main__":
    main()
