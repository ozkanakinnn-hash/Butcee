"""Son dakika sesi — 12. tur (son): R1 (haber çanı) → haber kanalı bildirimi/geçiş sesi gibi.

S1 — Kanal geçişi: daha sıkı sinyaller + vuruştan hemen önce kısa "whoosh" + tok vuruş + parlak haber çanı
S2 — Ding-dong: R1 ama kapanışta inen iki notalı haber çanı (ding–dong), kanal imzası gibi
S3 — Bakır vurgulu: R1 + vuruşa bakır "stab" katmanı (haber kanalı jeneriği vurgusu) + çan
Kullanım: python3 design/make_breaking_sound_v12.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v4 import BELL, BRASS  # noqa: E402
from make_breaking_sound_v5 import big_reverb  # noqa: E402
from make_breaking_sound_v7 import lowpass  # noqa: E402
from make_breaking_sound_v10 import BRIGHTER, pip  # noqa: E402
from make_breaking_sound_v11 import q4d_end  # noqa: E402

random.seed(12)


def whoosh(dur, gain=0.3):
    """Kısa yükselen hava sesi: bant genişliği artan gürültü, sona doğru yükselen zarf."""
    n = int(dur * RATE)
    out, y = [], 0.0
    for i in range(n):
        t = i / RATE
        a = 0.05 + 0.5 * (t / dur) ** 2
        y += a * ((random.random() * 2 - 1) - y)
        out.append(y * math.sin(math.pi * 0.5 * t / dur) ** 2 * gain)
    return out


def bell(f, dur=0.8, gain=0.22, decay=4):
    return lowpass(note(f, dur, BELL, decay, gain=gain), 0.6)


def option_s1():
    pips = [(0.13 * k, pip(523.3, 0.09, BRIGHTER, 0.6, 0.5)) for k in range(3)]
    parts = pips + [(0.30, whoosh(0.12, 0.35))] + q4d_end(0.42) + [(0.42, bell(1046.5, gain=0.26))]
    return big_reverb(mix(parts, 1.25), wet=0.22, tail=0.6, fb=0.76)


def option_s2():
    pips = [(0.16 * k, pip(523.3, 0.1, BRIGHTER, 0.55, 0.5)) for k in range(3)]
    parts = pips + q4d_end(0.48) + [(0.48, bell(1046.5, 0.45, 0.24, 6)), (0.66, bell(784.0, 0.8, 0.24, 3.5))]
    return big_reverb(mix(parts, 1.45), wet=0.22, tail=0.6, fb=0.76)


def option_s3():
    pips = [(0.16 * k, pip(523.3, 0.1, BRIGHTER, 0.55, 0.5)) for k in range(3)]
    stab = [(0.48, lowpass(note(f, 0.35, BRASS, 7, a=0.004, gain=0.28), 0.4)) for f in (261.6, 392.0, 523.3)]
    parts = pips + q4d_end(0.48) + stab + [(0.48, bell(1046.5, gain=0.2))]
    return big_reverb(mix(parts, 1.3), wet=0.22, tail=0.6, fb=0.76)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("S1-Kanal-gecisi", option_s1), ("S2-Ding-dong", option_s2), ("S3-Bakir-vurgulu", option_s3)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
