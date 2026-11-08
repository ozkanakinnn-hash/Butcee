"""Sinematik son dakika sesi adayları (tamamen sentez; tescilli seslerin kopyası DEĞİL, yalnızca tarz).

D — Sinematik vuruş: iki derin "boom" (alçak sinüs + gövde) ve ardından geniş, yavaşça açılan akor.
E — Sinematik kısa: tek güçlü vuruş + yükselen parıltı (shimmer) ile biten kısa imza.
Kullanım: python3 design/make_cinematic_sound.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import struct
import sys
import wave
from pathlib import Path

RATE = 22050
random.seed(23)


def boom(freq, dur, gain=1.0, drop=0.35):
    """Derin vuruş: perdesi hafifçe düşen sinüs + 2. harmonik + kısa gövde gürültüsü."""
    n = int(dur * RATE)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = freq * (1 - drop * min(1.0, t / 0.12))
        ph += 2 * math.pi * f / RATE
        env = min(1.0, t / 0.004) * math.exp(-5.5 * t)
        body = (random.random() * 2 - 1) * math.exp(-60 * t) * 0.25
        # Telefon hoparlörü 200 Hz altını zor verir → üst harmonikler vuruşu "duyulur" kılar (eksik temel etkisi)
        harm = 0.55 * math.sin(3 * ph) * math.exp(-3 * t) + 0.4 * math.sin(4 * ph) * math.exp(-4 * t) + 0.25 * math.sin(6 * ph) * math.exp(-7 * t)
        out.append((math.sin(ph) + 0.5 * math.sin(2 * ph) + harm) * env * gain + body)
    return out


def swell(freqs, dur, gain=0.5, attack=0.35):
    """Geniş akor: hafif detune'lu testere benzeri tını, yavaş açılış, uzun sönüm (sinematik 'ped')."""
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        env = min(1.0, (t / attack) ** 1.5) * math.exp(-1.6 * max(0.0, t - attack))
        s = 0.0
        for f in freqs:
            for det in (-0.6, 0.0, 0.6):
                ff = f + det
                s += sum(math.sin(2 * math.pi * ff * k * t) / k for k in range(1, 6))
        out.append(s / (len(freqs) * 3) * env * gain)
    return out


def shimmer(f0, f1, dur, gain=0.25):
    n = int(dur * RATE)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = f0 * (f1 / f0) ** (t / dur)
        ph += 2 * math.pi * f / RATE
        env = math.sin(math.pi * t / dur) ** 2
        trem = 0.7 + 0.3 * math.sin(2 * math.pi * 14 * t)
        out.append(math.sin(ph) * env * trem * gain)
    return out


def mix(parts, total):
    buf = [0.0] * int(total * RATE)
    for start, p in parts:
        i0 = int(start * RATE)
        for i, v in enumerate(p):
            if i0 + i < len(buf):
                buf[i0 + i] += v
    return buf


def lowpass(buf, a=0.35):
    out, y = [], 0.0
    for x in buf:
        y += a * (x - y)
        out.append(y)
    return out


def save(buf, path):
    peak = max(abs(v) for v in buf) or 1.0
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(v / peak * 0.9 * 32767)) for v in buf))


def option_d():  # iki vuruş (G1 → D2) + Re minör-ish geniş akor
    parts = [(0.00, boom(98.0, 0.5, gain=0.9)), (0.26, boom(73.4, 0.9, gain=1.0)),
             (0.30, swell([146.8, 220.0, 293.7, 349.2], 1.6, gain=0.55))]
    return lowpass(mix(parts, 1.9), a=0.45)


def option_e():  # tek vuruş + yükselen parıltı + parlak kapanış
    parts = [(0.00, boom(82.4, 0.8, gain=1.0)),
             (0.05, swell([164.8, 246.9, 329.6], 1.1, gain=0.4, attack=0.2)),
             (0.15, shimmer(600, 2400, 0.7, gain=0.18))]
    return lowpass(mix(parts, 1.3), a=0.55)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("D-Sinematik", option_d), ("E-Sinematik-kisa", option_e)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
