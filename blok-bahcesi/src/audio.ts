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
      // Telefon hoparlöründe duyulsun diye: yüksek kazanç + kompresör (patlamayı önler)
      const comp = ctx.createDynamicsCompressor();
      comp.threshold.value = -18;
      comp.knee.value = 12;
      comp.ratio.value = 6;
      comp.attack.value = 0.003;
      comp.release.value = 0.2;
      master = ctx.createGain();
      master.gain.value = 1.4;
      master.connect(comp).connect(ctx.destination);
    }
    if (ctx.state === 'suspended') void ctx.resume();
    return ctx;
  } catch {
    return null;
  }
}

/** Her dokunuşta: ses bağlamı askıdaysa sürdür (mobil tarayıcı / WebView kısıtı). */
export function unlockAudio() {
  const a = ac();
  if (a && a.state !== 'running') void a.resume().then(() => music.resume());
}

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
  g.gain.exponentialRampToValueAtTime(Math.min(1, (opts.vol ?? 0.5) * 1.8), t0 + 0.01);
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

let lastClick = 0;
const PENTA = [523.25, 587.33, 659.25, 783.99, 880, 1046.5, 1174.66, 1318.5];

export const sfx = {
  click: () => {
    const now = performance.now();
    if (now - lastClick < 80) return; // aynı dokunuşta iki kez çalmasın
    lastClick = now;
    tone(880, 0.05, { type: 'triangle', vol: 0.25 });
    tone(1320, 0.04, { type: 'sine', vol: 0.12, delay: 0.02 });
  },
  select: () => tone(520, 0.07, { type: 'triangle', vol: 0.3, slide: 1.3 }),
  rotate: () => tone(740, 0.05, { type: 'square', vol: 0.12 }),
  move: () => tone(620, 0.03, { type: 'triangle', vol: 0.14 }),
  land: () => { tone(330, 0.1, { type: 'triangle', vol: 0.45, slide: 0.55 }); noise(0.07, 0.3, 0, 1400); },
  clear: (n: number, combo: number) => {
    for (let i = 0; i < Math.min(5, n + 1); i++) tone(PENTA[Math.min(7, i + combo)], 0.18, { type: 'triangle', delay: i * 0.06, vol: 0.35 });
  },
  flower: () => [0, 2, 4, 7].forEach((s, i) => tone(PENTA[0] * Math.pow(2, s / 12) * 2, 0.3, { delay: i * 0.07, vol: 0.25 })),
  rescue: () => tone(880, 0.25, { type: 'sine', vol: 0.3, slide: 1.8 }),
  sprout: () => tone(600, 0.2, { type: 'sine', vol: 0.3, slide: 1.5, delay: 0.05 }),
  sunBreak: () => noise(0.12, 0.25, 0, 3000),
  season: () => [0, 4, 7, 12].forEach((s, i) => tone(392 * Math.pow(2, s / 12), 0.4, { delay: i * 0.09, vol: 0.2 })),
  warn: () => { tone(220, 0.25, { type: 'sawtooth', vol: 0.12 }); tone(208, 0.25, { type: 'sawtooth', vol: 0.12, delay: 0.25 }); },
  attack: () => { noise(0.6, 0.55, 0, 900); tone(220, 0.5, { type: 'sawtooth', vol: 0.22, slide: 0.5 }); },
  undo: () => tone(500, 0.12, { type: 'triangle', vol: 0.25, slide: 0.6 }),
  win: () => [0, 4, 7, 12, 16].forEach((s, i) => tone(523 * Math.pow(2, s / 12), 0.35, { type: 'triangle', delay: i * 0.11, vol: 0.3 })),
  lose: () => [0, -3, -7].forEach((s, i) => tone(330 * Math.pow(2, s / 12), 0.4, { type: 'triangle', delay: i * 0.18, vol: 0.25 })),
  star: (i: number) => tone(784 * Math.pow(2, (i * 4) / 12), 0.25, { type: 'triangle', vol: 0.3 }),
  gem: () => { tone(1568, 0.12, { type: 'sine', vol: 0.25 }); tone(2093, 0.18, { type: 'sine', vol: 0.2, delay: 0.07 }); tone(2637, 0.25, { type: 'triangle', vol: 0.15, delay: 0.14 }); },
  buy: () => { tone(880, 0.08, { type: 'square', vol: 0.12 }); tone(1320, 0.18, { type: 'triangle', vol: 0.25, delay: 0.08 }); },
  splash: () => { noise(0.35, 0.3, 0, 900); tone(300, 0.3, { type: 'sine', vol: 0.15, slide: 0.5 }); },
  bubble: () => [0, 0.08, 0.17].forEach((d, i) => tone(500 + i * 180, 0.08, { type: 'sine', vol: 0.18, slide: 1.8, delay: d })),
  cheer: () => [0, 4, 7, 12, 7, 12, 16].forEach((st, i) => tone(392 * Math.pow(2, st / 12), 0.2, { type: 'triangle', vol: 0.25, delay: i * 0.08 })),
  drop2: () => tone(320, 0.09, { type: 'triangle', vol: 0.25, slide: 0.5 }),
  whoosh: () => noise(0.25, 0.2, 0, 2500),
  start: () => [0, 4, 7, 12].forEach((st, i) => tone(523.25 * Math.pow(2, st / 12), 0.22, { type: 'triangle', vol: 0.3, delay: i * 0.07 })),
};

// ---------------- prosedürel müzik ----------------
// Pentatonik, yumuşak bir bahçe ezgisi: bas + arpej + ara sıra çan. Mod: sakin (menü/bahçe), oyun, gergin (boss/kurtarma).
type MusicMode = 'calm' | 'game' | 'tense';
const SCALES: Record<MusicMode, number[]> = {
  calm: [0, 2, 4, 7, 9, 12, 14, 16],
  game: [0, 2, 4, 7, 9, 12, 14, 16],
  tense: [0, 3, 5, 7, 10, 12, 15, 17],
};
const CHORDS: Record<MusicMode, number[]> = { calm: [0, -3, -7, -5], game: [0, -5, -3, -7], tense: [0, -2, -4, -2] };

class Music {
  private timer: number | null = null;
  private mode: MusicMode = 'calm';
  private nextTime = 0;
  private step = 0;
  private gain: GainNode | null = null;
  private seed = 1;
  private wanted = false;

  play(mode: MusicMode) {
    this.mode = mode;
    this.wanted = true;
    if (!save.settings.music || !save.settings.sound) { this.stop(); return; }
    const a = ac();
    if (!a || !master) return;
    if (!this.gain) {
      this.gain = a.createGain();
      this.gain.gain.value = 0.0001;
      this.gain.connect(master);
    }
    this.gain.gain.cancelScheduledValues(a.currentTime);
    this.gain.gain.setTargetAtTime(mode === 'calm' ? 0.55 : 0.48, a.currentTime, 0.4);
    if (this.timer !== null) return;
    this.nextTime = a.currentTime + 0.1;
    this.timer = window.setInterval(() => this.schedule(), 120);
  }

  /** Uygulama arka plana gidince durur, dönünce aynı modda sürer. */
  pause() { const w = this.wanted; this.stop(); this.wanted = w; }
  resume() { if (this.wanted) this.play(this.mode); }
  /** Ayar değişince */
  refresh() { if (this.wanted) this.play(this.mode); }

  stop() {
    this.wanted = false;
    if (this.timer !== null) { clearInterval(this.timer); this.timer = null; }
    if (ctx && this.gain) this.gain.gain.setTargetAtTime(0.0001, ctx.currentTime, 0.3);
  }

  private rnd() { this.seed = (this.seed * 16807) % 2147483647; return this.seed / 2147483647; }

  private note(freq: number, t: number, dur: number, type: OscillatorType, vol: number) {
    if (!ctx || !this.gain) return;
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = type;
    o.frequency.setValueAtTime(freq, t);
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(vol, t + 0.02);
    g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    o.connect(g).connect(this.gain);
    o.start(t);
    o.stop(t + dur + 0.05);
  }

  private schedule() {
    if (!ctx) return;
    const bpm = this.mode === 'tense' ? 118 : this.mode === 'game' ? 100 : 84;
    const eighth = 60 / bpm / 2;
    // Kök nota orta bölgede: telefon hoparlörleri 200 Hz altını neredeyse hiç çalamaz
    const root = this.mode === 'tense' ? 293.66 : 349.23;
    const scale = SCALES[this.mode];
    while (this.nextTime < ctx.currentTime + 0.35) {
      const t = this.nextTime;
      const bar = Math.floor(this.step / 8) % 4;
      const chord = CHORDS[this.mode][bar];
      const pos = this.step % 8;
      if (pos === 0 || (this.mode !== 'calm' && pos === 4)) {
        // "bas": bir oktav aşağıdaki kök + beşli (yumuşak pad)
        this.note(root * Math.pow(2, (chord - 12) / 12), t, eighth * 3.8, 'triangle', 0.32);
        this.note(root * Math.pow(2, (chord - 5) / 12), t, eighth * 3.8, 'sine', 0.18);
      }
      const arp = [0, 2, 4, 5, 4, 2, 1, 3][pos];
      if (this.mode !== 'calm' || pos % 2 === 0) {
        const deg = scale[(arp + bar) % scale.length];
        const f = root * Math.pow(2, (deg + chord) / 12);
        this.note(f, t, eighth * 1.7, 'triangle', 0.3); // marimba benzeri tını: temel + oktav
        this.note(f * 2, t, eighth * 0.8, 'sine', 0.09);
      }
      if (pos === 6 && this.rnd() < 0.5) this.note(root * 2 * Math.pow(2, scale[Math.floor(this.rnd() * 5)] / 12), t, 0.9, 'sine', 0.14);
      this.nextTime += eighth;
      this.step++;
    }
  }
}

export const music = new Music();
document.addEventListener('visibilitychange', () => {
  if (document.hidden) { music.pause(); void ctx?.suspend(); }
  else { void ctx?.resume(); music.resume(); }
});

const native = Capacitor.isNativePlatform();

export function haptic(kind: 'light' | 'medium' | 'heavy' = 'light') {
  if (!save.settings.haptics || !native) return; // web'de sessizce atla
  const style = kind === 'heavy' ? ImpactStyle.Heavy : kind === 'medium' ? ImpactStyle.Medium : ImpactStyle.Light;
  Haptics.impact({ style }).catch(() => {});
}
