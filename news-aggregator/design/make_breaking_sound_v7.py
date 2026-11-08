"""Son dakika sesi — 7. tur: N2'den DAHA TOK (kullanıcı: "daha tok bir ses").
Tok = dolgun, yuvarlak, ağır; tiz parlaklık az, 100–400 Hz gövde güçlü, kuyruk orta.

N4 — Ağır tok: katmanlı kick + tom vuruşu, bir oktav aşağıda kısılmış (alçak geçiren) minör akor
N5 — Taiko: büyük davul (taiko) vuruşu iki kez + alçak bakır, oda yankısı
N6 — Gong: derin, yuvarlak gong/tam-tam vuruşu (uyumsuz alçak kısmi tonlar) + bas dalgası
Kullanım: python3 design/make_breaking_sound_v7.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v4 import BRASS  # noqa: E402
from make_breaking_sound_v5 import big_reverb  # noqa: E402

random.seed(3)


def lowpass(buf, a):
    out, y = [], 0.0
    for x in buf:
        y += a * (x - y)
        out.append(y)
    return out


def drum(freq, dur, decay, drop=0.25, gain=1.0, harmonics=(1.0, 0.9, 0.7, 0.45, 0.25), click=0.25):
    """Davul gövdesi: perdesi hafif düşen, güçlü 2–5. harmonikli (telefonda dolgun duyulur) yuvarlak vuruş."""
    n = int(dur * RATE)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = freq * (1 - drop * min(1.0, t / 0.06))
        ph += 2 * math.pi * f / RATE
        env = min(1.0, t / 0.0025) * math.exp(-decay * t)
        s = sum(h * math.sin(k * ph) * math.exp(-0.8 * (k - 1) * t) for k, h in enumerate(harmonics, start=1))
        c = (random.random() * 2 - 1) * math.exp(-200 * t) * click
        out.append((s * env + c) * gain)
    return lowpass(out, 0.55)


def gong(freq, dur, gain=1.0):
    """Gong: uyumsuz alçak kısmi tonlar (1, 1.47, 2.09, 2.56, 3.39), yavaş sönüm, hafif vurgu dalgası."""
    partials = ((1.0, 1.0, 1.6), (1.47, 0.7, 2.0), (2.09, 0.55, 2.6), (2.56, 0.35, 3.2), (3.39, 0.2, 4.0))
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        att = min(1.0, t / 0.01)
        s = sum(g * math.sin(2 * math.pi * freq * r * t + 0.3 * math.sin(2 * math.pi * 3 * t)) * math.exp(-d * t)
                for r, g, d in partials)
        out.append(s * att * gain)
    return lowpass(out, 0.5)


def dark_chord(freqs, dur, gain=0.25, attack=0.04, decay=2.4):
    parts = [(0.0, note(f, dur, BRASS, decay, a=attack, gain=gain)) for f in freqs]
    return lowpass(mix(parts, dur), 0.18)  # parlaklığı kıs → koyu, tok akor


G_MINOR_LOW = (98.0, 116.5, 146.8, 196.0)  # Sol minör, bir oktav aşağı


def option_n4():
    parts = [(0.0, drum(62.0, 0.9, decay=5.5, gain=1.0)), (0.0, drum(110.0, 0.7, decay=7, gain=0.7)),
             (0.02, dark_chord(G_MINOR_LOW, 1.3, gain=0.4))]
    return big_reverb(mix(parts, 1.35), wet=0.3, tail=0.9, fb=0.8)


def option_n5():
    parts = [(0.0, drum(75.0, 0.7, decay=6, gain=0.75, drop=0.3)),
             (0.22, drum(68.0, 1.0, decay=4.5, gain=1.0, drop=0.3)),
             (0.24, dark_chord((98.0, 146.8, 196.0), 1.1, gain=0.35))]
    return big_reverb(mix(parts, 1.4), wet=0.28, tail=0.9, fb=0.8)


def option_n6():
    parts = [(0.0, gong(73.4, 1.8, gain=0.9)), (0.0, drum(55.0, 0.9, decay=4.5, gain=0.8)),
             (0.05, dark_chord((110.0, 146.8, 174.6), 1.4, gain=0.3, attack=0.15))]
    return big_reverb(mix(parts, 1.9), wet=0.3, tail=1.0, fb=0.8)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("N4-Agir-tok", option_n4), ("N5-Taiko", option_n5), ("N6-Gong", option_n6)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
