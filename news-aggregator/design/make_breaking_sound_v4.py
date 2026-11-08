"""Son dakika sesi — 4. tur: TV "SON DAKİKA" jeneriği hissi (aciliyet, minör ton, ritmik vuruş, güçlü kapanış).
Tamamen sentez, telifsiz; hiçbir kanalın jeneriği kopyalanmadı. 3. turdaki not/yankı araçlarını kullanır.

J — Flaş haber: 4 hızlı acil darbe (ostinato) + yükselen ses + orkestral kapanış vuruşu (minör akor + derin bas)
K — Son dakika jeneriği: dramatik bakır vurgu + kısa timpani gümbürtüsü + minör akor ve çan
L — Ajans alarmı: iki tonlu hızlı uyarı motifi (3 kez) + net kapanış vuruşu
Kullanım: python3 design/make_breaking_sound_v4.py ÇIKTI_KLASÖRÜ
"""
import math
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_breaking_sound_v3 import RATE, mix, note, reverb, save  # noqa: E402

random.seed(42)

BRASS = [(1, 1.0, 0), (2, 0.8, 0.5), (3, 0.6, 1), (4, 0.45, 1.5), (5, 0.3, 2), (6, 0.2, 3)]
PULSE = [(1, 1.0, 0), (3, 0.3, 4), (5, 0.12, 8)]
BELL = [(1, 1.0, 0), (2.76, 0.4, 2), (5.4, 0.2, 4)]


def hit(freq, dur, gain=1.0):
    """Orkestral vuruş: perdesi düşen derin ton + üst harmonikler (telefonda duyulsun) + gürültü gövdesi."""
    n = int(dur * RATE)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = freq * (1 - 0.3 * min(1.0, t / 0.1))
        ph += 2 * math.pi * f / RATE
        env = min(1.0, t / 0.003) * math.exp(-4.5 * t)
        s = (math.sin(ph) + 0.6 * math.sin(2 * ph) + 0.5 * math.sin(3 * ph) * math.exp(-2 * t)
             + 0.35 * math.sin(4 * ph) * math.exp(-3 * t))
        noise = (random.random() * 2 - 1) * math.exp(-45 * t) * 0.5
        out.append(s * env * gain + noise * gain)
    return out


def roll(dur, gain=0.35):
    """Kısa timpani gümbürtüsü: hızlı tekrarlanan alçak vuruşlar, giderek yükselen."""
    parts = []
    k = 0
    while k * 0.035 < dur:
        g = gain * (0.4 + 0.6 * (k * 0.035) / dur)
        parts.append((k * 0.035, hit(110, 0.15, gain=g)))
        k += 1
    return mix(parts, dur + 0.15)


def riser(f0, f1, dur, gain=0.2):
    n = int(dur * RATE)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = f0 * (f1 / f0) ** (t / dur)
        ph += 2 * math.pi * f / RATE
        e = (t / dur) ** 2
        out.append((math.sin(ph) + 0.4 * math.sin(2 * ph) + 0.15 * (random.random() * 2 - 1)) * e * gain)
    return out


A_MINOR = [220.0, 261.6, 329.6, 440.0]  # La minör


def option_j():  # Flaş haber
    parts = [(0.09 * k, note(880.0 if k < 3 else 987.8, 0.08, PULSE, 20, a=0.002, gain=0.45)) for k in range(4)]
    parts.append((0.0, riser(300, 1200, 0.42)))
    parts.append((0.42, hit(55.0, 1.0, gain=0.9)))
    for f in A_MINOR:
        parts.append((0.42, note(f, 1.1, BRASS, 2.8, a=0.01, gain=0.28)))
    return reverb(mix(parts, 1.6), wet=0.25)


def option_k():  # Son dakika jeneriği
    parts = [(0.0, roll(0.35)), (0.38, hit(55.0, 0.9, gain=1.0))]
    for f in (220.0, 261.6, 329.6):
        parts.append((0.38, note(f, 1.0, BRASS, 2.5, a=0.008, gain=0.3)))
    parts.append((0.40, note(1760.0, 1.0, BELL, 3, gain=0.22)))
    return reverb(mix(parts, 1.5), wet=0.28)


def option_l():  # Ajans alarmı: Mi5–La5 iki ton, 3 kez, sonra vuruş
    parts = []
    for k in range(3):
        t0 = k * 0.2
        parts.append((t0, note(659.3, 0.09, PULSE, 12, a=0.002, gain=0.45)))
        parts.append((t0 + 0.1, note(880.0, 0.09, PULSE, 12, a=0.002, gain=0.45)))
    parts.append((0.62, hit(73.4, 0.8, gain=0.8)))
    parts.append((0.62, note(587.3, 0.8, BRASS, 3.5, gain=0.3)))
    parts.append((0.62, note(880.0, 0.8, BRASS, 3.5, gain=0.25)))
    return reverb(mix(parts, 1.5), wet=0.2)


if __name__ == "__main__":
    out = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in (("J-Flas-haber", option_j), ("K-Son-dakika-jenerigi", option_k), ("L-Ajans-alarmi", option_l)):
        buf = fn()
        save(buf, out / f"Ses-{name}.wav")
        print(name, f"{len(buf) / RATE:.2f} sn")
