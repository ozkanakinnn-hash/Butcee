"""Son dakika sesi — 13. tur: iki notalı "du-dum" fikri (özgün; Samsung'un sesi KOPYALANMADI) + haber unsurları.

T1 — Du-dum haber: iki yuvarlak nota (alçak→yüksek, "du-DUM") + tok vuruş + haber çanı
T2 — Du-dum çift: "du-dum" iki kez (ikincisi daha yüksek, haber sinyali gibi) + kapanış çanı
T3 — Du-dum tok: "DU-dum" (yüksek→alçak, oturan) + derin tok vuruş + kısa yankı
Kullanım: python3 design/make_breaking_sound_v13.py ÇIKTI_KLASÖRÜ
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v5 import big_reverb  # noqa: E402
from make_breaking_sound_v7 import drum, lowpass  # noqa: E402
from make_breaking_sound_v12 import bell  # noqa: E402

MALLET = [(1, 1.0, 0), (2, 0.4, 2.5), (3, 0.18, 5), (4.2, 0.06, 9)]  # yuvarlak tokmak tınısı


def du(f, dur=0.35, gain=0.55, decay=5.5):
    return lowpass(note(f, dur, MALLET, decay, a=0.004, gain=gain), 0.5)


def option_t1():
    parts = [(0.00, du(392.0, 0.3, 0.5, 7)), (0.17, du(587.3, 0.9, 0.6, 3.5)),
             (0.17, drum(65.0, 0.8, decay=5.5, gain=0.8)), (0.19, bell(1174.7, 0.8, 0.18, 4))]
    return big_reverb(mix(parts, 1.15), wet=0.22, tail=0.6, fb=0.76)


def option_t2():
    parts = [(0.00, du(392.0, 0.25, 0.45, 8)), (0.15, du(523.3, 0.3, 0.5, 7)),
             (0.40, du(440.0, 0.25, 0.45, 8)), (0.55, du(659.3, 0.9, 0.6, 3.5)),
             (0.55, drum(65.0, 0.8, decay=5.5, gain=0.75)), (0.57, bell(1318.5, 0.8, 0.18, 4))]
    return big_reverb(mix(parts, 1.5), wet=0.22, tail=0.6, fb=0.76)


def option_t3():
    parts = [(0.00, du(587.3, 0.3, 0.55, 7)), (0.18, du(392.0, 0.9, 0.6, 3.2)),
             (0.18, drum(55.0, 1.0, decay=4.6, gain=1.0)), (0.18, drum(110.0, 0.6, decay=8, gain=0.45))]
    return big_reverb(mix(parts, 1.25), wet=0.26, tail=0.8, fb=0.78)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("T1-Dudum-haber", option_t1), ("T2-Dudum-cift", option_t2), ("T3-Dudum-tok", option_t3)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
