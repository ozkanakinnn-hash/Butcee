"""Son dakika sesi — 5. tur: BASLI ve YANKILI (kullanıcı isteği). Tamamen sentez, telifsiz.

M — Yankılanan vuruş: derin vuruş + üstte parlak sinyal, ardından giderek azalan yankı tekrarları (delay)
N — Salon: alçak bas düşüşü + minör akorun büyük salon yankısında açılması
O — Çift bas: "dum-DUM" iki derin vuruş + katedral yankısı + çan
Telefon hoparlörü için vuruşlara üst harmonikler eklendi (bas "hissedilsin").
Kullanım: python3 design/make_breaking_sound_v5.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, save  # noqa: E402
from make_breaking_sound_v4 import BELL, BRASS, hit  # noqa: E402

random.seed(99)


def big_reverb(buf, wet=0.45, tail=1.6, fb=0.86):
    """Uzun salon yankısı: 6 tarak (uzun gecikmeler, yüksek geri besleme) + 2 tüm-geçiren; tarakta hafif alçak geçiren (sıcak kuyruk)."""
    out_len = len(buf) + int(tail * RATE)
    x = buf + [0.0] * (out_len - len(buf))
    acc = [0.0] * out_len
    for d_ms in (31.3, 37.9, 41.7, 47.3, 53.1, 59.9):
        d = int(d_ms / 1000 * RATE)
        y = [0.0] * out_len
        lp = 0.0
        for i in range(out_len):
            fbv = y[i - d] if i >= d else 0.0
            lp += 0.35 * (fbv - lp)  # yüksek frekanslar kuyrukta sönsün
            y[i] = x[i] + fb * lp
            acc[i] += y[i]
    y = [v / 6 for v in acc]
    for d_ms, g in ((7.0, 0.7), (2.3, 0.7)):
        d = int(d_ms / 1000 * RATE)
        z = [0.0] * out_len
        for i in range(out_len):
            xd = y[i - d] if i >= d else 0.0
            zd = z[i - d] if i >= d else 0.0
            z[i] = -g * y[i] + xd + g * zd
        y = z
    return [x[i] * (1 - wet) + y[i] * wet for i in range(out_len)]


def echo(buf, delay=0.24, fb=0.45, repeats=4):
    out = buf + [0.0] * int(delay * RATE * repeats)
    d = int(delay * RATE)
    for k in range(1, repeats + 1):
        g = fb ** k
        for i, v in enumerate(buf):
            out[i + d * k] += v * g * (0.85 ** k)
    return out


def sub_drop(f0, f1, dur, gain=0.8):
    n = int(dur * RATE)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = f0 * (f1 / f0) ** (min(1.0, t / (dur * 0.6)))
        ph += 2 * math.pi * f / RATE
        env = min(1.0, t / 0.01) * math.exp(-2.2 * t)
        out.append((math.sin(ph) + 0.5 * math.sin(2 * ph) + 0.35 * math.sin(3 * ph) + 0.2 * math.sin(4 * ph)) * env * gain)
    return out


def option_m():  # Yankılanan vuruş
    dry = mix([(0.0, hit(55.0, 0.7, gain=1.0)), (0.0, note(1318.5, 0.25, BELL, 7, gain=0.3)),
               (0.0, note(659.3, 0.35, BRASS, 6, gain=0.25))], 0.7)
    return big_reverb(echo(dry, delay=0.26, fb=0.5, repeats=4), wet=0.3, tail=1.0)


def option_n():  # Salon: bas düşüşü + minör akor
    parts = [(0.0, sub_drop(140, 45, 1.2)), (0.0, hit(49.0, 0.9, gain=0.7))]
    for f in (196.0, 233.1, 293.7, 392.0):  # Sol minör
        parts.append((0.05, note(f, 1.4, BRASS, 2.0, a=0.06, gain=0.22)))
    return big_reverb(mix(parts, 1.6), wet=0.5, tail=1.8)


def option_o():  # Çift bas + katedral yankısı + çan
    parts = [(0.0, hit(61.7, 0.6, gain=0.75)), (0.24, hit(46.2, 1.0, gain=1.0)),
             (0.24, note(1396.9, 1.0, BELL, 2.5, gain=0.25)), (0.24, note(277.2, 1.0, BRASS, 2.5, gain=0.25))]
    return big_reverb(mix(parts, 1.3), wet=0.5, tail=2.0, fb=0.88)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("M-Yankilanan-vurus", option_m), ("N-Salon", option_n), ("O-Cift-bas", option_o)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
