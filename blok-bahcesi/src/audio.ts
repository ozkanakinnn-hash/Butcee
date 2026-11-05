// Web Audio API ile koddan üretilen kısa efektler + Capacitor Haptics.
import { Capacitor } from '@capacitor/core';
import { Haptics, ImpactStyle } from '@capacitor/haptics';
import { save } from './storage';

let ctx: AudioContext | null = null;
let master: GainNode | null = null;

function ac(): AudioContext | null {
  if (!save.settings.sound) return null;
  try {
    if (!ctx) {
      const C = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      ctx = new C();
      master = ctx.createGain();
      master.gain.value = 0.35;
      master.connect(ctx.destination);
    }
    if (ctx.state === 'suspended') void ctx.resume();
    return ctx;
  } catch {
    return null;
  }
}

/** İlk dokunuşta ses bağlamını aç (mobil tarayıcı kısıtı). */
export function unlockAudio() { ac(); }

function tone(freq: number, dur: number, opts: { type?: OscillatorType; delay?: number; vol?: number; slide?: number } = {}) {
  const a = ac();
  if (!a || !master) return;
  const t0 = a.currentTime + (opts.delay ?? 0);
  const o = a.createOscillator();
  const g = a.createGain();
  o.type = opts.type ?? 'sine';
  o.frequency.setValueAtTime(freq, t0);
  if (opts.slide) o.frequency.exponentialRampToValueAtTime(Math.max(30, freq * opts.slide), t0 + dur);
  g.gain.setValueAtTime(0.0001, t0);
  g.gain.exponentialRampToValueAtTime(opts.vol ?? 0.5, t0 + 0.01);
  g.gain.exponentialRampToValueAtTime(0.0001, t0 + dur);
  o.connect(g).connect(master);
  o.start(t0);
  o.stop(t0 + dur + 0.02);
}

function noise(dur: number, vol = 0.3, delay = 0, lowpass = 1200) {
  const a = ac();
  if (!a || !master) return;
  const len = Math.floor(a.sampleRate * dur);
  const buf = a.createBuffer(1, len, a.sampleRate);
  const d = buf.getChannelData(0);
  for (let i = 0; i < len; i++) d[i] = (Math.random() * 2 - 1) * (1 - i / len);
  const src = a.createBufferSource();
  src.buffer = buf;
  const f = a.createBiquadFilter();
  f.type = 'lowpass';
  f.frequency.value = lowpass;
  const g = a.createGain();
  g.gain.value = vol;
  src.connect(f).connect(g).connect(master);
  src.start(a.currentTime + delay);
}

const PENTA = [523.25, 587.33, 659.25, 783.99, 880, 1046.5, 1174.66, 1318.5];

export const sfx = {
  click: () => tone(660, 0.06, { type: 'triangle', vol: 0.25 }),
  select: () => tone(520, 0.07, { type: 'triangle', vol: 0.3, slide: 1.3 }),
  rotate: () => tone(740, 0.05, { type: 'square', vol: 0.12 }),
  move: () => tone(400, 0.025, { type: 'triangle', vol: 0.08 }),
  land: () => { tone(140, 0.12, { type: 'sine', vol: 0.6, slide: 0.6 }); noise(0.06, 0.15, 0, 600); },
  clear: (n: number, combo: number) => {
    for (let i = 0; i < Math.min(5, n + 1); i++) tone(PENTA[Math.min(7, i + combo)], 0.18, { type: 'triangle', delay: i * 0.06, vol: 0.35 });
  },
  flower: () => [0, 2, 4, 7].forEach((s, i) => tone(PENTA[0] * Math.pow(2, s / 12) * 2, 0.3, { delay: i * 0.07, vol: 0.25 })),
  rescue: () => tone(880, 0.25, { type: 'sine', vol: 0.3, slide: 1.8 }),
  sprout: () => tone(600, 0.2, { type: 'sine', vol: 0.3, slide: 1.5, delay: 0.05 }),
  sunBreak: () => noise(0.12, 0.25, 0, 3000),
  season: () => [0, 4, 7, 12].forEach((s, i) => tone(392 * Math.pow(2, s / 12), 0.4, { delay: i * 0.09, vol: 0.2 })),
  warn: () => { tone(220, 0.25, { type: 'sawtooth', vol: 0.12 }); tone(208, 0.25, { type: 'sawtooth', vol: 0.12, delay: 0.25 }); },
  attack: () => { noise(0.6, 0.45, 0, 500); tone(90, 0.5, { type: 'sawtooth', vol: 0.25, slide: 0.5 }); },
  undo: () => tone(500, 0.12, { type: 'triangle', vol: 0.25, slide: 0.6 }),
  win: () => [0, 4, 7, 12, 16].forEach((s, i) => tone(523 * Math.pow(2, s / 12), 0.35, { type: 'triangle', delay: i * 0.11, vol: 0.3 })),
  lose: () => [0, -3, -7].forEach((s, i) => tone(330 * Math.pow(2, s / 12), 0.4, { type: 'triangle', delay: i * 0.18, vol: 0.25 })),
  star: (i: number) => tone(784 * Math.pow(2, (i * 4) / 12), 0.25, { type: 'triangle', vol: 0.3 }),
};

const native = Capacitor.isNativePlatform();

export function haptic(kind: 'light' | 'medium' | 'heavy' = 'light') {
  if (!save.settings.haptics || !native) return; // web'de sessizce atla
  const style = kind === 'heavy' ? ImpactStyle.Heavy : kind === 'medium' ? ImpactStyle.Medium : ImpactStyle.Light;
  Haptics.impact({ style }).catch(() => {});
}
