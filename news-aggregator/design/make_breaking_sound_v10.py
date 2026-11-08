"""Son dakika sesi — 10. tur: Q4 (tok haber saati) üzerine çeşitlemeler.

Q4a — Belirgin: sinyaller biraz daha net/parlak (yine yuvarlak), kapanış vuruşu daha derin, yankı biraz daha geniş
Q4b — Yükselen: üç sinyal + dördüncüsü bir oktav yukarıda vuruşla birlikte (haber saati "tam saat" hissi)
Q4c — Güçlü kapanış: sinyallerde hafif yankı, kapanışta çift katmanlı tok vuruş + açılan koyu akor
Q4d — Derli toplu: aynı karakter, daha kısa (~1,8 sn), sinyaller sıkı
Kullanım: python3 design/make_breaking_sound_v10.py ÇIKTI_KLASÖRÜ
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v5 import big_reverb, echo  # noqa: E402
from make_breaking_sound_v7 import dark_chord, drum, lowpass  # noqa: E402
from make_breaking_sound_v9 import ROUND  # noqa: E402

BRIGHTER = [(1, 1.0, 0), (2, 0.45, 3), (3, 0.2, 5), (4, 0.08, 8)]
C_MINOR_LOW = (130.8, 155.6, 196.0)


def pip(freq, dur=0.12, tim=ROUND, lp=0.4, gain=0.45):
    return lowpass(note(freq, dur, tim, 6, a=0.006, gain=gain), lp)


def option_q4a():
    pips = [(0.22 * k, pip(523.3, tim=BRIGHTER, lp=0.55, gain=0.5)) for k in range(3)]
    end = [(0.66, drum(55.0, 1.0, decay=4.6, gain=1.0)), (0.66, drum(110.0, 0.6, decay=8, gain=0.5)),
           (0.66, pip(523.3, 0.9, BRIGHTER, 0.5, 0.4)), (0.68, dark_chord(C_MINOR_LOW, 1.1, gain=0.33))]
    return big_reverb(mix(pips + end, 1.8), wet=0.3, tail=1.0, fb=0.8)


def option_q4b():
    pips = [(0.22 * k, pip(523.3)) for k in range(3)]
    end = [(0.66, drum(62.0, 0.9, decay=5, gain=1.0)), (0.66, pip(1046.5, 0.9, ROUND, 0.45, 0.4)),
           (0.66, pip(523.3, 0.9, ROUND, 0.4, 0.3)), (0.68, dark_chord(C_MINOR_LOW, 1.0, gain=0.3))]
    return big_reverb(mix(pips + end, 1.7), wet=0.25, tail=0.9, fb=0.78)


def option_q4c():
    pips = echo(mix([(0.22 * k, pip(523.3)) for k in range(3)], 0.6), delay=0.11, fb=0.3, repeats=1)
    end = [(0.66, drum(58.0, 1.0, decay=4.4, gain=1.0)), (0.66, drum(98.0, 0.7, decay=6.5, gain=0.7)),
           (0.66, pip(523.3, 0.9, ROUND, 0.4, 0.38)),
           (0.70, dark_chord((130.8, 155.6, 196.0, 261.6), 1.3, gain=0.36, attack=0.12))]
    return big_reverb(mix([(0.0, pips)] + end, 2.0), wet=0.3, tail=1.0, fb=0.8)


def option_q4d():
    pips = [(0.16 * k, pip(523.3, 0.1)) for k in range(3)]
    end = [(0.48, drum(62.0, 0.75, decay=6, gain=1.0)), (0.48, pip(523.3, 0.7, ROUND, 0.4, 0.38)),
           (0.50, dark_chord(C_MINOR_LOW, 0.8, gain=0.3))]
    return big_reverb(mix(pips + end, 1.3), wet=0.22, tail=0.6, fb=0.76)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("Q4a-Belirgin", option_q4a), ("Q4b-Yukselen", option_q4b),
                     ("Q4c-Guclu-kapanis", option_q4c), ("Q4d-Derli-toplu", option_q4d)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
