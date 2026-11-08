"""Son dakika sesi için 3 aday (tamamen sentez, telifsiz). Kullanım: python3 design/make_breaking_sound_options.py ÇIKTI_KLASÖRÜ

A — Haber jeneriği: parlak, yükselen 4 notalı akor vuruşu (TV haber açılışı hissi)
B — Teleks: hızlı ajans teleksi tıkırtısı + iki net bip (klasik "flaş haber")
C — Dijital uyarı: alçak darbe + yükselen süpürme + parlak ping (modern haber kanalı geçişi)
"""
import math
import random
import struct
import sys
import wave
from pathlib import Path

RATE = 22050
random.seed(11)


def env_ad(t, a, d):
    return min(1.0, t / a) * math.exp(-d * t)


def tone(freq, dur, a=0.005, d=4.0, harm=(1.0, 0.5, 0.25), gain=0.5, saw=False):
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        s = 0.0
        for k, h in enumerate(harm, start=1):
            s += h * math.sin(2 * math.pi * freq * k * t)
        out.append(s * env_ad(t, a, d) * gain)
    return out


def noise(dur, a=0.002, d=30, gain=0.4, hp=True):
    n = int(dur * RATE)
    out, prev = [], 0.0
    for i in range(n):
        t = i / RATE
        x = random.random() * 2 - 1
        y = x - prev if hp else x  # basit yüksek geçiren → "tık"
        prev = x
        out.append(y * env_ad(t, a, d) * gain)
    return out


def sweep(f0, f1, dur, gain=0.35):
    n = int(dur * RATE)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = f0 * (f1 / f0) ** (t / dur)
        ph += 2 * math.pi * f / RATE
        e = math.sin(math.pi * t / dur) ** 2
        out.append((math.sin(ph) + 0.3 * (random.random() * 2 - 1)) * e * gain)
    return out


def mix(parts, total):
    buf = [0.0] * int(total * RATE)
    for start, p in parts:
        i0 = int(start * RATE)
        for i, v in enumerate(p):
            if i0 + i < len(buf):
                buf[i0 + i] += v
    return buf


def save(buf, path):
    peak = max(abs(v) for v in buf) or 1.0
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(v / peak * 0.88 * 32767)) for v in buf))


def option_a():  # Haber jeneriği: C5-E5-G5 hızlı, sonra C6 akoru tutulur
    brass = (1.0, 0.7, 0.5, 0.3, 0.2)
    parts = [(0.00, tone(523.25, 0.25, d=7, harm=brass)),
             (0.11, tone(659.25, 0.25, d=7, harm=brass)),
             (0.22, tone(783.99, 0.25, d=7, harm=brass))]
    for f in (523.25, 659.25, 783.99, 1046.5):
        parts.append((0.36, tone(f, 0.9, d=2.6, harm=brass, gain=0.35)))
    parts.append((0.36, noise(0.08, d=40, gain=0.25)))
    return mix(parts, 1.3)


def option_b():  # Teleks: 10 hızlı tık + iki net bip (1.2 kHz)
    parts = [(0.03 * k + random.uniform(0, 0.008), noise(0.03, d=120, gain=0.55)) for k in range(10)]
    parts += [(0.40, tone(1200, 0.16, d=8, harm=(1.0, 0.2))), (0.62, tone(1200, 0.30, d=5, harm=(1.0, 0.2)))]
    return mix(parts, 1.05)


def option_c():  # Dijital uyarı: alçak darbe + süpürme + parlak ping
    parts = [(0.00, tone(65, 0.6, a=0.003, d=6, harm=(1.0, 0.5), gain=0.9)),
             (0.00, noise(0.12, d=25, gain=0.35, hp=False)),
             (0.05, sweep(300, 2400, 0.45, gain=0.25)),
             (0.48, tone(1568, 0.8, d=4, harm=(1.0, 0.35, 0.1), gain=0.55)),
             (0.48, tone(2349, 0.6, d=6, harm=(1.0,), gain=0.25))]
    return mix(parts, 1.35)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("A-Haber-jenerigi", option_a), ("B-Teleks", option_b), ("C-Dijital-uyari", option_c)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
