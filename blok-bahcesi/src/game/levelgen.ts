import { Rng, hashSeed } from './rng';
import { COLS, ROWS, type LevelDef } from './types';

/** Yerel tarihten "YYYY-AA-GG" anahtarı. */
export function dateKey(d = new Date()): string {
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export function dailySeed(key: string): number {
  return hashSeed('blok-bahcesi:' + key);
}

/**
 * Sütun yükseklikleriyle (üstü açık, kapalı boşluk yok) bir başlangıç tahtası üretir;
 * böylece her boşluğa yukarıdan ulaşılabilir.
 */
function skylineMap(rng: Rng, maxH: number, animals: number, seeds: number): string[] {
  const heights: number[] = [];
  for (let c = 0; c < COLS; c++) heights.push(1 + rng.int(maxH));
  // her yükseklik seviyesinde en az bir boşluk olsun (başta dolu satır yok)
  heights[rng.int(COLS)] = 0;
  const g: string[][] = Array.from({ length: ROWS }, () => Array(COLS).fill('.'));
  for (let c = 0; c < COLS; c++) for (let i = 0; i < heights[c]; i++) g[ROWS - 1 - i][c] = 'T';
  const soil: [number, number][] = [];
  g.forEach((row, r) => row.forEach((ch, c) => { if (ch === 'T') soil.push([r, c]); }));
  const take = (ch: string, n: number) => {
    for (let i = 0; i < n && soil.length; i++) {
      const [r, c] = soil.splice(rng.int(soil.length), 1)[0];
      g[r][c] = ch;
    }
  };
  take('H', Math.ceil(animals / 3));
  take('P', Math.ceil((animals - 1) / 3));
  take('U', Math.floor(animals / 3));
  take('S', seeds);
  if (rng.next() < 0.5) take('K', 1);
  return g.map((r) => r.join(''));
}

/** Günlük Tohum: tarihten üretilen, herkes için aynı bölüm. */
export function generateDaily(seed: number): LevelDef {
  const rng = new Rng(seed);
  const map = skylineMap(rng, 4, 3, 2);
  const kinds = ['lines', 'animals', 'seeds'] as const;
  const type = kinds[rng.int(kinds.length)];
  const goal = type === 'lines' ? { type, count: 10 } : type === 'seeds' ? { type, count: 2 } : { type };
  return {
    id: 1000, map, goal, moves: 30,
    colors: type === 'seeds' ? { water: 18 } : undefined,
  };
}

export function generateZen(seed: number): LevelDef {
  const rng = new Rng(seed);
  return { id: 2000, map: skylineMap(rng, 3, 2, 2), goal: { type: 'none' }, moves: null, colors: { water: 14 } };
}

export function generateClassic(): LevelDef {
  return { id: 3000, map: [], goal: { type: 'none' }, moves: null };
}

/** Klasik Hız: her 10 satırda hızlanan düşüş aralığı (ms). */
export function classicInterval(lines: number): number {
  const lvl = Math.floor(lines / 10);
  return Math.max(110, Math.round(850 * Math.pow(0.82, lvl)));
}
