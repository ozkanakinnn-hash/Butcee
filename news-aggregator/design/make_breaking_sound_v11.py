"""Son dakika sesi — 11. tur: Q4d (derli toplu) + daha fazla "haber" hissi (~1,9 sn).

R1 — Haber çanı: Q4d + kapanışta orta perdede net haber "ding"i, sinyaller bir tık daha belirgin
R2 — Teleksli: Q4d sinyallerinin altında hafif ajans teleksi tıkırtısı (haber masası hissi)
R3 — Yükselen motif: sinyaller yükselir (Do–Re–Mi → Sol vuruşla), haber jeneriği imzası gibi
Kullanım: python3 design/make_breaking_sound_v11.py ÇIKTI_KLASÖRÜ
"""
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v4 import BELL  # noqa: E402
from make_breaking_sound_v5 import big_reverb  # noqa: E402
from make_breaking_sound_v7 import dark_chord, drum, lowpass  # noqa: E402
from make_breaking_sound_v8 import tick  # noqa: E402
from make_breaking_sound_v9 import ROUND  # noqa: E402
from make_breaking_sound_v10 import BRIGHTER, C_MINOR_LOW, pip  # noqa: E402

random.seed(11)


def q4d_end(t0, top=523.3):
    return [(t0, drum(62.0, 0.75, decay=6, gain=1.0)), (t0, pip(top, 0.7, ROUND, 0.4, 0.38)),
            (t0 + 0.02, dark_chord(C_MINOR_LOW, 0.8, gain=0.3))]


def option_r1():
    pips = [(0.16 * k, pip(523.3, 0.1, BRIGHTER, 0.55, 0.5)) for k in range(3)]
    parts = pips + q4d_end(0.48) + [(0.48, lowpass(note(1046.5, 0.8, BELL, 4, gain=0.22), 0.55))]
    return big_reverb(mix(parts, 1.3), wet=0.22, tail=0.6, fb=0.76)


def option_r2():
    pips = [(0.16 * k, pip(523.3, 0.1)) for k in range(3)]
    ticks = [(0.035 * k + random.uniform(0, 0.006), tick(0.18)) for k in range(13)]
    return big_reverb(mix(pips + ticks + q4d_end(0.48), 1.3), wet=0.22, tail=0.6, fb=0.76)


def option_r3():
    pips = [(0.0, pip(523.3, 0.1, BRIGHTER, 0.5)), (0.16, pip(587.3, 0.1, BRIGHTER, 0.5)),
            (0.32, pip(659.3, 0.1, BRIGHTER, 0.5))]
    return big_reverb(mix(pips + q4d_end(0.48, top=784.0), 1.3), wet=0.22, tail=0.6, fb=0.76)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("R1-Haber-cani", option_r1), ("R2-Teleksli", option_r2), ("R3-Yukselen-motif", option_r3)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
