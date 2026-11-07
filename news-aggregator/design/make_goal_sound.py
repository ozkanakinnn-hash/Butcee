"""Gol bildirim sesi: hakem düdüğü (iki kısa + bir uzun) ve kalabalık coşkusu, ~2,8 sn.

Çıktı: flutter_app/android/app/src/main/res/raw/goal_whistle.wav (22.05 kHz, mono, 16-bit)
Telif sorunu olmaması için tamamen sentezlenir. Kullanım: python3 design/make_goal_sound.py
"""
import math
import random
import struct
import wave
from pathlib import Path

RATE = 22050
OUT = Path(__file__).resolve().parent.parent / "flutter_app/android/app/src/main/res/raw/goal_whistle.wav"
random.seed(7)


def whistle(dur, freq=3150.0):
    """Düdük: titreşimli (trill) yüksek ton + hafif nefes gürültüsü, yumuşak atak/bırakma."""
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        trill = 1 + 0.035 * math.sin(2 * math.pi * 28 * t)  # bezelye düdüğü titreşimi
        tone = math.sin(2 * math.pi * freq * trill * t) + 0.35 * math.sin(2 * math.pi * 2 * freq * trill * t)
        breath = (random.random() * 2 - 1) * 0.08
        env = min(1.0, t / 0.02, (dur - t) / 0.04)
        out.append((tone * 0.55 + breath) * env)
    return out


def silence(dur):
    return [0.0] * int(dur * RATE)


def crowd(dur):
    """Kalabalık: filtrelenmiş gürültü, yükselen sonra azalan zarf."""
    n = int(dur * RATE)
    out, lp, lp2 = [], 0.0, 0.0
    for i in range(n):
        t = i / RATE
        noise = random.random() * 2 - 1
        lp += 0.12 * (noise - lp)      # alçak geçiren
        lp2 += 0.02 * (noise - lp2)    # daha derin uğultu
        swell = math.sin(math.pi * min(1.0, t / dur)) ** 0.6
        out.append((lp * 1.6 + lp2 * 2.2) * swell)
    return out


def mix(a, b, offset):
    out = a + [0.0] * max(0, offset + len(b) - len(a))
    for i, v in enumerate(b):
        out[offset + i] += v
    return out


def main():
    track = whistle(0.16) + silence(0.07) + whistle(0.16) + silence(0.07) + whistle(0.75)
    track = mix(track, crowd(2.1), int(0.55 * RATE))
    peak = max(abs(v) for v in track) or 1.0
    frames = b"".join(struct.pack("<h", int(max(-1, min(1, v / peak * 0.9)) * 32767)) for v in track)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(OUT), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(frames)
    print(f"{OUT} ({len(track) / RATE:.1f} sn, {OUT.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
