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

/** Macera bölümü türü: 10'un katları boss, diğerleri sırayla döner. */
export function adventureGoalType(id: number): 'boss' | 'lines' | 'animals' | 'seeds' {
  if (id % 10 === 0) return 'boss';
  return (['lines', 'animals', 'seeds'] as const)[id % 3];
}

/**
 * 21+ bölümler için parametrik üretici. d = 0..1 zorluk: daha yüksek yığın, kapalı boşluk,
 * kaya, ot/buz, daha seyrek satırlarda hayvan ve daha az su.
 */
export function generateAdventure(id: number, seed: number, d: number): LevelDef {
  const rng = new Rng(seed);
  const type = adventureGoalType(id);
  const maxH = 3 + Math.round(d * 3);
  const heights: number[] = [];
  for (let c = 0; c < COLS; c++) heights.push(1 + rng.int(maxH));
  heights[rng.int(COLS)] = rng.int(2);
  const g: string[][] = Array.from({ length: ROWS }, () => Array(COLS).fill('.'));
  for (let c = 0; c < COLS; c++) for (let i = 0; i < heights[c]; i++) g[ROWS - 1 - i][c] = 'T';
  const filled = () => {
    const out: [number, number][] = [];
    g.forEach((row, r) => row.forEach((ch, c) => { if (ch !== '.') out.push([r, c]); }));
    return out;
  };
  // kapalı boşluklar
  const holes = Math.round(d * 4);
  for (let i = 0; i < holes; i++) {
    const cand = filled().filter(([r, c]) => r > 0 && g[r - 1][c] !== '.' && r < ROWS - 0);
    if (!cand.length) break;
    const [r, c] = cand[rng.int(cand.length)];
    g[r][c] = '.';
  }
  const soil = () => filled().filter(([r, c]) => g[r][c] === 'T');
  const put = (ch: string, n: number, preferHigh = 0) => {
    for (let i = 0; i < n; i++) {
      let cand = soil();
      if (!cand.length) return;
      if (preferHigh > 0) {
        cand.sort((a, b) => a[0] - b[0]);
        cand = cand.slice(0, Math.max(1, Math.ceil(cand.length * (1 - preferHigh * 0.7))));
      }
      const [r, c] = cand[rng.int(cand.length)];
      g[r][c] = ch;
    }
  };
  put('K', 1 + Math.round(d * 2));
  put(rng.next() < 0.5 ? 'O' : 'I', Math.round(d * 4));
  const level: LevelDef = { id, map: [], goal: { type }, moves: 30 };
  if (type === 'animals') {
    const n = 3 + Math.round(d * 3);
    const kinds = ['H', 'P', 'U'];
    for (let i = 0; i < n; i++) put(kinds[i % 3], 1, d);
  } else if (type === 'seeds') {
    const n = 4 + Math.round(d * 3);
    put('S', n, d * 0.6);
    level.goal = { type, count: n - 1 };
    level.colors = { water: Math.round(20 - d * 8), sun: 12 };
  } else if (type === 'lines') {
    level.goal = { type, count: 8 + Math.round(d * 10) };
    if (id % 4 === 1) level.colors = { leaf: 18 };
  } else {
    level.bossHp = 12 + Math.round(d * 6);
    level.colors = { stone: 11, leaf: 12 };
  }
  // başlangıçta dolu satır kalmasın
  for (let r = 0; r < ROWS; r++) {
    if (g[r].every((ch) => ch !== '.')) {
      const opts = g[r].map((ch, c) => (ch === 'T' ? c : -1)).filter((c) => c >= 0);
      g[r][opts.length ? opts[rng.int(opts.length)] : rng.int(COLS)] = '.';
    }
  }
  // tamamen boş üst satırları at
  const rows = g.map((r) => r.join(''));
  while (rows.length && rows[0] === '........') rows.shift();
  level.map = rows;
  return level;
}
