"""Son dakika sesi — 3. tur adaylar (tamamen sentez, telifsiz). Hepsine Schroeder yankısı uygulanır.

F — Marimba: sıcak, ahşap tınılı üç nota (ding-ding-dong), yumuşak ama net
G — Radyo bülteni: üç kısa sinyal + daha uzun dördüncü (radyo haber saati hissi)
H — Cam çınlaması: iki parlak, kristal tını; zarif ve modern
I — Haber jingle: hızlı yükselen arpej + bas vuruşu, enerjik kapanış
Kullanım: python3 design/make_breaking_sound_v3.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import struct
import sys
import wave
from pathlib import Path

RATE = 22050
random.seed(5)


def note(freq, dur, partials, decay, a=0.003, gain=0.5):
    """partials: (oran, genlik, ek_sönüm) listesi."""
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        s = sum(g * math.sin(2 * math.pi * freq * r * t) * math.exp(-(decay + d) * t) for r, g, d in partials)
        out.append(s * min(1.0, t / a) * gain)
    return out


MARIMBA = [(1.0, 1.0, 0), (3.93, 0.35, 9), (9.2, 0.12, 25)]
GLASS = [(1.0, 1.0, 0), (2.76, 0.5, 2), (5.4, 0.25, 5), (8.93, 0.12, 9)]
PIP = [(1.0, 1.0, 0), (2.0, 0.08, 0)]
SYNTH = [(1.0, 1.0, 0), (2.0, 0.5, 2), (3.0, 0.33, 4), (4.0, 0.25, 6)]


def mix(parts, total):
    buf = [0.0] * int(total * RATE)
    for start, p in parts:
        i0 = int(start * RATE)
        for i, v in enumerate(p):
            if i0 + i < len(buf):
                buf[i0 + i] += v
    return buf


def reverb(buf, wet=0.28, tail=0.6):
    """Basit Schroeder yankısı: 4 paralel tarak + 2 seri tüm-geçiren süzgeç."""
    out_len = len(buf) + int(tail * RATE)
    x = buf + [0.0] * (out_len - len(buf))
    combs = []
    for d_ms, g in ((29.7, 0.78), (37.1, 0.76), (41.1, 0.74), (43.7, 0.72)):
        d = int(d_ms / 1000 * RATE)
        y = [0.0] * out_len
        for i in range(out_len):
            y[i] = x[i] + (g * y[i - d] if i >= d else 0.0)
        combs.append(y)
    y = [sum(c[i] for c in combs) / 4 for i in range(out_len)]
    for d_ms, g in ((5.0, 0.7), (1.7, 0.7)):
        d = int(d_ms / 1000 * RATE)
        z = [0.0] * out_len
        for i in range(out_len):
            xd = y[i - d] if i >= d else 0.0
            zd = z[i - d] if i >= d else 0.0
            z[i] = -g * y[i] + xd + g * zd
        y = z
    return [x[i] * (1 - wet) + y[i] * wet for i in range(out_len)]


def save(buf, path):
    peak = max(abs(v) for v in buf) or 1.0
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(v / peak * 0.9 * 32767)) for v in buf))


def option_f():  # Marimba: E5, G#5 → B5 (majör, sıcak)
    return reverb(mix([(0.00, note(659.3, 0.5, MARIMBA, 6)), (0.14, note(830.6, 0.5, MARIMBA, 6)),
                       (0.30, note(987.8, 0.9, MARIMBA, 3.5, gain=0.6))], 1.2), wet=0.25)


def option_g():  # Radyo bülteni: 3 kısa 1 kHz + uzun 1.25 kHz
    parts = [(0.25 * k, note(1000, 0.1, PIP, 2, a=0.004, gain=0.45)) for k in range(3)]
    parts.append((0.75, note(1250, 0.45, PIP, 2.5, a=0.004, gain=0.5)))
    return reverb(mix(parts, 1.25), wet=0.18, tail=0.4)


def option_h():  # Cam: iki parlak tını (C7, G6) üst üste binen
    return reverb(mix([(0.00, note(2093.0, 1.0, GLASS, 4, gain=0.45)),
                       (0.16, note(1568.0, 1.2, GLASS, 3, gain=0.5))], 1.4), wet=0.32, tail=0.8)


def option_i():  # Jingle: hızlı arpej D5-F#5-A5-D6 + bas D3 vuruşu + kapanış akoru
    arp = [587.3, 740.0, 880.0, 1174.7]
    parts = [(0.07 * k, note(f, 0.25, SYNTH, 10, gain=0.35)) for k, f in enumerate(arp)]
    parts.append((0.30, note(146.8, 0.6, [(1, 1, 0), (2, 0.7, 1), (3, 0.5, 2), (4, 0.4, 3)], 5, gain=0.6)))
    for f in (587.3, 740.0, 880.0):
        parts.append((0.30, note(f, 0.9, SYNTH, 3, gain=0.25)))
    return reverb(mix(parts, 1.3), wet=0.25)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("F-Marimba", option_f), ("G-Radyo-bulteni", option_g),
                     ("H-Cam-cinlamasi", option_h), ("I-Haber-jingle", option_i)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
