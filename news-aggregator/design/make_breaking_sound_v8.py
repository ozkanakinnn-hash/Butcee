"""Son dakika sesi — 8. tur: TOK vuruş + "haber bildirimi" işareti (kullanıcı: tok olsun ama haber bildirimini andırsın).

P1 — Acil darbe + tok: üç hızlı acil darbe (haber uyarısı) → ağır tok vuruş + koyu minör akor
P2 — Teleks + taiko: kısa ajans teleksi tıkırtısı + yükselen gerilim → taiko vuruşu + akor + hafif haber çanı
P3 — Haber vurgusu + tok: iki kısa bakır vurgusu (haber jeneriği imzası) → tok vuruş + akor
Kullanım: python3 design/make_breaking_sound_v8.py ÇIKTI_KLASÖRÜ
"""
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v4 import BELL, BRASS, PULSE, riser  # noqa: E402
from make_breaking_sound_v5 import big_reverb  # noqa: E402
from make_breaking_sound_v7 import G_MINOR_LOW, dark_chord, drum, lowpass  # noqa: E402

random.seed(8)


def tick(gain=0.4):
    n = int(0.025 * RATE)
    out, prev = [], 0.0
    for i in range(n):
        t = i / RATE
        x = random.random() * 2 - 1
        out.append((x - prev) * (2.718 ** (-150 * t)) * gain)
        prev = x
    return out


def tok_end(t0, gain=1.0):
    return [(t0, drum(62.0, 0.9, decay=5.5, gain=1.0 * gain)), (t0, drum(110.0, 0.7, decay=7, gain=0.7 * gain)),
            (t0 + 0.02, dark_chord(G_MINOR_LOW, 1.2, gain=0.4 * gain))]


def option_p1():
    pulses = [(0.11 * k, lowpass(note(784.0, 0.09, PULSE, 18, a=0.002, gain=0.5), 0.6)) for k in range(3)]
    return big_reverb(mix(pulses + tok_end(0.36), 1.65), wet=0.28, tail=0.9, fb=0.8)


def option_p2():
    ticks = [(0.028 * k + random.uniform(0, 0.006), tick(0.35 + 0.02 * k)) for k in range(10)]
    parts = ticks + [(0.0, riser(220, 700, 0.32, gain=0.15)),
                     (0.32, drum(75.0, 1.0, decay=4.5, gain=1.0, drop=0.3)),
                     (0.34, dark_chord((98.0, 146.8, 196.0), 1.1, gain=0.35)),
                     (0.34, lowpass(note(1174.7, 0.9, BELL, 3, gain=0.18), 0.5))]
    return big_reverb(mix(parts, 1.6), wet=0.28, tail=0.9, fb=0.8)


def option_p3():
    stab = lambda f: lowpass(note(f, 0.16, BRASS, 9, a=0.006, gain=0.35), 0.35)  # noqa: E731
    parts = [(0.00, stab(392.0)), (0.00, stab(587.3)), (0.17, stab(440.0)), (0.17, stab(659.3))]
    return big_reverb(mix(parts + tok_end(0.36), 1.65), wet=0.28, tail=0.9, fb=0.8)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("P1-Acil-darbe-tok", option_p1), ("P2-Teleks-taiko", option_p2), ("P3-Haber-vurgusu-tok", option_p3)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
