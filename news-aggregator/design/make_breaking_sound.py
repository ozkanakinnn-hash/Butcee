"""Son dakika bildirim sesi: haber bülteni tarzı, ciddi ve kısa üç tonlu uyarı (~1,3 sn).

Çıktı: flutter_app/android/app/src/main/res/raw/breaking_alert.wav (22.05 kHz, mono, 16-bit)
Telif sorunu olmaması için tamamen sentezlenir. Kullanım: python3 design/make_breaking_sound.py

Tasarım: iki kısa yüksek "bip" + bir uzun, alçalan çan tonu (haber jeneriği hissi); sert değil,
ama dikkat çekici. Harmonikli sinüs + üstel sönüm (çan / vibrafon tınısı).
"""
import math
import struct
import wave
from pathlib import Path

RATE = 22050
OUT = Path(__file__).resolve().parent.parent / "flutter_app/android/app/src/main/res/raw/breaking_alert.wav"


def bell(freq, dur, decay=4.5, gain=0.6):
    """Çan tınısı: temel + 2. ve 3. harmonik, hızlı atak, üstel sönüm."""
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        env = min(1.0, t / 0.006) * math.exp(-decay * t)
        s = (math.sin(2 * math.pi * freq * t)
             + 0.45 * math.sin(2 * math.pi * 2 * freq * t) * math.exp(-2 * t)
             + 0.2 * math.sin(2 * math.pi * 3.01 * freq * t) * math.exp(-6 * t))
        out.append(s * env * gain)
    return out


def silence(dur):
    return [0.0] * int(dur * RATE)


def mix_at(base, part, start):
    i0 = int(start * RATE)
    need = i0 + len(part)
    if len(base) < need:
        base.extend([0.0] * (need - len(base)))
    for i, v in enumerate(part):
        base[i0 + i] += v
    return base


# A5–A5 kısa (dikkat), ardından E5 + A4 birlikte uzun (oturan kapanış)
track = []
mix_at(track, bell(880.0, 0.22, decay=9), 0.00)
mix_at(track, bell(880.0, 0.22, decay=9), 0.18)
mix_at(track, bell(659.25, 1.0, decay=3.2, gain=0.55), 0.38)
mix_at(track, bell(440.0, 1.0, decay=2.8, gain=0.45), 0.38)
track.extend(silence(0.05))

peak = max(abs(v) for v in track) or 1.0
OUT.parent.mkdir(parents=True, exist_ok=True)
with wave.open(str(OUT), "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(RATE)
    w.writeframes(b"".join(struct.pack("<h", int(v / peak * 0.85 * 32767)) for v in track))
print(f"{OUT} ({len(track) / RATE:.2f} sn)")
