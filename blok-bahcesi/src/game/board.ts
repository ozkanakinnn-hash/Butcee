import { COLS, ROWS, type AnimalKind, type Cell, type Element, type Grid } from './types';
import type { Offset } from './pieces';
import type { Rng } from './rng';

export function emptyGrid(): Grid {
  return Array.from({ length: ROWS }, () => Array<Cell | null>(COLS).fill(null));
}

export function cloneGrid(g: Grid): Grid {
  return g.map((row) => row.slice()); // hücre nesneleri değişmez kabul edilir
}

const CHAR_TO_EL: Record<string, Element> = {
  b: 'water', g: 'leaf', y: 'sun', s: 'stone', p: 'purple', r: 'red',
};
const CHAR_TO_ANIMAL: Record<string, AnimalKind> = { H: 'hedgehog', P: 'turtle', U: 'ladybug' };

/** Metin haritasını ızgaraya çevirir. Eksik satırlar üstten boş sayılır. */
export function parseMap(map: string[]): Grid {
  const g = emptyGrid();
  const rows = map.slice(-ROWS);
  const offset = ROWS - rows.length;
  rows.forEach((line, i) => {
    for (let c = 0; c < COLS; c++) {
      const ch = line[c] ?? '.';
      g[i + offset][c] = charToCell(ch);
    }
  });
  return g;
}

function charToCell(ch: string): Cell | null {
  if (ch === 'T') return { k: 'soil' };
  if (ch === 'K') return { k: 'rock' };
  if (ch === 'S') return { k: 'seed' };
  if (ch === 'G') return { k: 'gardener' };
  if (ch === 'O') return { k: 'weed', hp: 2 };
  if (ch === 'I') return { k: 'ice', hp: 2 };
  if (CHAR_TO_ANIMAL[ch]) return { k: 'animal', a: CHAR_TO_ANIMAL[ch] };
  if (CHAR_TO_EL[ch]) return { k: 'block', el: CHAR_TO_EL[ch] };
  return null;
}

export function countCells(g: Grid, pred: (c: Cell) => boolean): number {
  let n = 0;
  for (const row of g) for (const c of row) if (c && pred(c)) n++;
  return n;
}

/** Parça (row,col) sol üst konumunda sığar mı? Tahtanın üstü (r<0) boş sayılır. */
export function fitsAt(g: Grid, cells: Offset[], row: number, col: number): boolean {
  for (const [dr, dc] of cells) {
    const r = row + dr;
    const c = col + dc;
    if (c < 0 || c >= COLS || r >= ROWS) return false;
    if (r >= 0 && g[r][c]) return false;
  }
  return true;
}

/** Parça yukarıdan düz düşürülürse duracağı satır; tahtaya tamamen sığmıyorsa null. */
export function landingRow(g: Grid, cells: Offset[], col: number): number | null {
  const h = Math.max(...cells.map((c) => c[0])) + 1;
  let row = -h;
  if (!fitsAt(g, cells, row, col)) return null;
  while (fitsAt(g, cells, row + 1, col)) row++;
  return row >= 0 ? row : null;
}

export function placeCells(g: Grid, cells: Offset[], row: number, col: number, el: Element): Grid {
  const out = cloneGrid(g);
  for (const [dr, dc] of cells) out[row + dr][col + dc] = { k: 'block', el };
  return out;
}

/** Tamamen dolu satırlar. Yalnızca kaya/tohumdan oluşan (hiçbir şey silinmeyecek) satırlar sayılmaz. */
export function findFullRows(g: Grid): number[] {
  const out: number[] = [];
  for (let r = 0; r < ROWS; r++) {
    const row = g[r];
    if (row.every((c) => c !== null) && row.some((c) => c!.k !== 'rock' && c!.k !== 'seed')) out.push(r);
  }
  return out;
}

export interface Pos { r: number; c: number }

export interface ClearStep {
  /** Bu adım başlamadan önceki tahta (animasyon için). */
  before: Grid;
  rows: number[];
  flowerRows: number[];
  stoneCounts: number[];
  rescued: (Pos & { a: AnimalKind })[];
  sprouted: Pos[];
  sunBreaks: Pos[];
  weedHits: Pos[];
  /** Bahçıvan bu adımda kurtarıldıysa konumu */
  gardener: Pos | null;
  /** Fırtınaya verilen hasar (satır başı 1, çiçek satırı 2). */
  damage: number;
}

const isEl = (c: Cell | null, el: Element) => !!c && c.k === 'block' && c.el === el;

/** Tek bir temizleme adımını uygular (element etkileri + yerçekimi). */
export function resolveStep(g: Grid, rows: number[]): { grid: Grid; step: ClearStep } {
  const full = new Set(rows);
  const work = cloneGrid(g);
  const cleared: boolean[][] = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
  const step: ClearStep = {
    before: g, rows: [...rows], flowerRows: [], stoneCounts: [], rescued: [], sprouted: [],
    sunBreaks: [], weedHits: [], damage: 0, gardener: null,
  };

  // GÜNEŞ: satırdaki her turuncu hücre kendi sütunundaki en yakın ot/buzu kırar.
  for (const r of rows) {
    for (let c = 0; c < COLS; c++) {
      if (!isEl(g[r][c], 'sun')) continue;
      for (let d = 1; d < ROWS; d++) {
        let hit = -1;
        for (const rr of [r + d, r - d]) {
          const cell = rr >= 0 && rr < ROWS ? work[rr][c] : null;
          if (cell && (cell.k === 'weed' || cell.k === 'ice')) { hit = rr; break; }
        }
        if (hit >= 0) {
          work[hit][c] = null;
          if (full.has(hit)) cleared[hit][c] = true;
          step.sunBreaks.push({ r: hit, c });
          break;
        }
      }
    }
  }

  // SU: satırda mavi varsa, üst/alt (ve kendi) satırındaki tohumlar filizlenir.
  for (const r of rows) {
    if (!g[r].some((c) => isEl(c, 'water'))) continue;
    for (const rr of [r - 1, r, r + 1]) {
      if (rr < 0 || rr >= ROWS) continue;
      for (let c = 0; c < COLS; c++) {
        if (work[rr][c]?.k === 'seed') {
          work[rr][c] = null;
          if (full.has(rr)) cleared[rr][c] = true;
          step.sprouted.push({ r: rr, c });
        }
      }
    }
  }

  for (const r of rows) {
    const flower = g[r].every((c) => isEl(c, 'leaf'));
    if (flower) step.flowerRows.push(r);
    step.stoneCounts.push(g[r].filter((c) => isEl(c, 'stone')).length);
    step.damage += flower ? 2 : 1;
    for (let c = 0; c < COLS; c++) {
      const cell = work[r][c];
      if (!cell) continue;
      switch (cell.k) {
        case 'rock':
        case 'seed':
          break; // kaya yerinde kalır; sulanmamış tohum hayatta kalır
        case 'weed':
        case 'ice':
          step.weedHits.push({ r, c });
          if (cell.hp <= 1) cleared[r][c] = true;
          else work[r][c] = { ...cell, hp: cell.hp - 1 };
          break;
        case 'animal':
          step.rescued.push({ r, c, a: cell.a });
          cleared[r][c] = true;
          break;
        case 'gardener':
          step.gardener = { r, c };
          cleared[r][c] = true;
          break;
        default:
          cleared[r][c] = true;
      }
    }
  }

  return { grid: collapse(work, cleared), step };
}

/**
 * Silinen hücrelerin üstündekiler, altlarındaki silinen hücre sayısı kadar iner (Tetris tarzı).
 * Kaya bir sınırdır: üstündekiler kayanın üstüne oturur, kaya hiç hareket etmez.
 */
export function collapse(g: Grid, cleared: boolean[][]): Grid {
  const out = emptyGrid();
  for (let c = 0; c < COLS; c++) {
    let drop = 0;
    for (let r = ROWS - 1; r >= 0; r--) {
      const cell = g[r][c];
      if (cell?.k === 'rock') {
        out[r][c] = cell;
        drop = 0;
        continue;
      }
      if (cleared[r][c]) {
        drop++;
        continue;
      }
      if (cell) out[r + drop][c] = cell;
    }
  }
  return out;
}

/** Tüm zincirleme temizlemeleri çözer. */
export function resolveAll(g: Grid): { grid: Grid; steps: ClearStep[] } {
  const steps: ClearStep[] = [];
  let grid = g;
  for (let i = 0; i < 24; i++) {
    const rows = findFullRows(grid);
    if (!rows.length) break;
    const res = resolveStep(grid, rows);
    steps.push(res.step);
    grid = res.grid;
  }
  return { grid, steps };
}

/** Fırtına saldırısı: en alta 1 boşluklu yabani ot satırı ekler, her şeyi 1 yukarı iter. */
export function bossAttack(g: Grid, rng: Rng): { grid: Grid; overflow: boolean; gap: number } {
  const overflow = g[0].some((c) => c !== null);
  const stoneCols = new Set<number>();
  for (const row of g) row.forEach((c, i) => { if (isEl(c, 'stone')) stoneCols.add(i); });
  const gap = rng.int(COLS);
  const newRow: (Cell | null)[] = [];
  for (let c = 0; c < COLS; c++) newRow.push(c === gap || stoneCols.has(c) ? null : { k: 'weed', hp: 2 });
  return { grid: [...cloneGrid(g).slice(1), newRow], overflow, gap };
}

export function findCell(g: Grid, k: Cell['k']): Pos | null {
  for (let r = 0; r < ROWS; r++) for (let c = 0; c < COLS; c++) if (g[r][c]?.k === k) return { r, c };
  return null;
}
