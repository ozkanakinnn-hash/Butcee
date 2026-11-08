"""Son dakika sesi — 9. tur: tok + haber, dört farklı yaklaşım.

Q1 — Ritimli tok: "dum-ta-DUM" davul kalıbı + orta perdede tek haber "ding"i
Q2 — Orkestral flaş: yaylı tremolo yükselişi + timpani + bakır vuruşu (TV son dakika dramı)
Q3 — Kısa tok: tek, çok kısa ve sert tok vuruş + orta perdede yuvarlak "ding" (≈1 sn, bildirim gibi)
Q4 — Haber saati (tok): alçak, yuvarlak üç sinyal + son vuruş (radyo haber saati hissi, tiz değil)
Kullanım: python3 design/make_breaking_sound_v9.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v4 import BELL, BRASS  # noqa: E402
from make_breaking_sound_v5 import big_reverb  # noqa: E402
from make_breaking_sound_v7 import dark_chord, drum, lowpass  # noqa: E402

random.seed(9)
ROUND = [(1, 1.0, 0), (2, 0.35, 3), (3, 0.12, 6)]  # yuvarlak, tiz olmayan tını


def strings_tremolo(freqs, dur, gain=0.25):
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        env = (t / dur) ** 1.5
        trem = 0.6 + 0.4 * math.sin(2 * math.pi * 16 * t)
        s = sum(sum(math.sin(2 * math.pi * f * k * t) / k for k in range(1, 5)) for f in freqs) / len(freqs)
        out.append(s * env * trem * gain)
    return lowpass(out, 0.3)


def option_q1():
    parts = [(0.00, drum(70.0, 0.4, decay=8, gain=0.8)), (0.16, drum(130.0, 0.25, decay=12, gain=0.5)),
             (0.30, drum(60.0, 1.0, decay=4.8, gain=1.0)), (0.32, dark_chord((98.0, 146.8, 196.0), 1.0, gain=0.3)),
             (0.32, lowpass(note(880.0, 0.9, BELL, 3.5, gain=0.22), 0.45))]
    return big_reverb(mix(parts, 1.5), wet=0.28, tail=0.9, fb=0.8)


def option_q2():
    parts = [(0.00, strings_tremolo((196.0, 233.1, 293.7), 0.55, gain=0.35))]
    parts += [(0.03 * k, drum(90.0, 0.12, decay=25, gain=0.15 + 0.03 * k, harmonics=(1, 0.6, 0.3))) for k in range(16)]
    parts += [(0.55, drum(58.0, 1.0, decay=4.2, gain=1.0)),
              (0.56, dark_chord((98.0, 146.8, 196.0, 233.1), 1.2, gain=0.4)),
              (0.56, lowpass(note(392.0, 1.0, BRASS, 3, a=0.005, gain=0.3), 0.3))]
    return big_reverb(mix(parts, 1.9), wet=0.3, tail=1.0, fb=0.82)


def option_q3():
    parts = [(0.00, drum(68.0, 0.6, decay=7, gain=1.0)), (0.00, drum(120.0, 0.4, decay=10, gain=0.6)),
             (0.05, lowpass(note(659.3, 0.8, ROUND, 4, gain=0.35), 0.5))]
    return big_reverb(mix(parts, 0.9), wet=0.22, tail=0.5, fb=0.75)


def option_q4():
    pips = [(0.22 * k, lowpass(note(523.3, 0.12, ROUND, 6, a=0.006, gain=0.45), 0.4)) for k in range(3)]
    end = [(0.66, drum(62.0, 0.9, decay=5, gain=1.0)), (0.66, lowpass(note(523.3, 0.9, ROUND, 3, gain=0.4), 0.4)),
           (0.68, dark_chord((130.8, 155.6, 196.0), 1.0, gain=0.3))]
    return big_reverb(mix(pips + end, 1.7), wet=0.25, tail=0.8, fb=0.78)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("Q1-Ritimli-tok", option_q1), ("Q2-Orkestral-flas", option_q2),
                     ("Q3-Kisa-tok", option_q3), ("Q4-Haber-saati-tok", option_q4)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
