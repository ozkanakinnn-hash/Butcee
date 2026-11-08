"""Son dakika sesi — 6. tur: N (Salon) temel alınarak.

N2 — Tok: N'nin akoru ve salonu; bas daha dolgun ve oturaklı (100–250 Hz gövde), vuruş daha sıkı, kuyruk daha kısa
N3 — Top atışı: N'nin akoru + top patlaması (gürültü patlaması + derin gümbürtü) ve uzaktan dönen yankı
Kullanım: python3 design/make_breaking_sound_v6.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v4 import BRASS  # noqa: E402
from make_breaking_sound_v5 import big_reverb, echo, sub_drop  # noqa: E402

random.seed(7)
G_MINOR = (196.0, 233.1, 293.7, 392.0)


def thick_hit(freq, dur, gain=1.0):
    """Tok vuruş: perdesi az düşen gövde (temel + 2-3-4. harmonik uzun), sert atak, sıkı sönüm, kısa 'tık'."""
    n = int(dur * RATE)
    out, ph, lp = [], 0.0, 0.0
    for i in range(n):
        t = i / RATE
        f = freq * (1 - 0.18 * min(1.0, t / 0.08))
        ph += 2 * math.pi * f / RATE
        env = min(1.0, t / 0.002) * math.exp(-6.5 * t)
        body = math.sin(ph) + 0.8 * math.sin(2 * ph) + 0.6 * math.sin(3 * ph) + 0.4 * math.sin(4 * ph) * math.exp(-4 * t)
        click = (random.random() * 2 - 1) * math.exp(-160 * t) * 0.6
        s = body * env * gain + click * gain
        lp += 0.5 * (s - lp)  # hafif yumuşatma → "tok", cızırtısız
        out.append(lp)
    return out


def cannon(dur, gain=1.0):
    """Top patlaması: güçlü geniş bant gürültü patlaması (hızla kararan) + derin gümbürtü."""
    n = int(dur * RATE)
    out, lp1, lp2, ph = [], 0.0, 0.0, 0.0
    for i in range(n):
        t = i / RATE
        x = random.random() * 2 - 1
        cutoff = 0.9 * math.exp(-9 * t) + 0.04  # patlama parlak başlar, koyulaşarak söner
        lp1 += cutoff * (x - lp1)
        lp2 += cutoff * (lp1 - lp2)
        blast = lp2 * min(1.0, t / 0.002) * math.exp(-3.2 * t) * 3.0
        f = 60 * (1 - 0.4 * min(1.0, t / 0.3))
        ph += 2 * math.pi * f / RATE
        rumble = (math.sin(ph) + 0.6 * math.sin(2 * ph) + 0.4 * math.sin(3 * ph)) * math.exp(-2.5 * t) * 0.8
        out.append((blast + rumble) * gain)
    return out


def option_n2():  # Tok
    parts = [(0.0, thick_hit(82.4, 0.9, gain=1.0)), (0.0, sub_drop(130, 55, 0.9, gain=0.5))]
    for f in G_MINOR:
        parts.append((0.03, note(f, 1.2, BRASS, 2.6, a=0.03, gain=0.24)))
    return big_reverb(mix(parts, 1.3), wet=0.35, tail=1.1, fb=0.82)


def option_n3():  # Top atışı
    dry = mix([(0.0, cannon(1.2, gain=1.0))] +
              [(0.08, note(f, 1.3, BRASS, 2.2, a=0.05, gain=0.2)) for f in G_MINOR], 1.4)
    return big_reverb(echo(dry, delay=0.42, fb=0.38, repeats=2), wet=0.4, tail=1.5)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("N2-Tok", option_n2), ("N3-Top-atisi", option_n3)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
