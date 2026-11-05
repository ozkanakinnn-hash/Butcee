import type { Element, PieceKind } from './types';

export type Offset = [number, number]; // [satır, sütun]

const BASE: Record<PieceKind, Offset[]> = {
  I: [[0, 0], [0, 1], [0, 2], [0, 3]],
  O: [[0, 0], [0, 1], [1, 0], [1, 1]],
  T: [[0, 0], [0, 1], [0, 2], [1, 1]],
  S: [[0, 1], [0, 2], [1, 0], [1, 1]],
  Z: [[0, 0], [0, 1], [1, 1], [1, 2]],
  J: [[0, 0], [1, 0], [1, 1], [1, 2]],
  L: [[0, 2], [1, 0], [1, 1], [1, 2]],
  D1: [[0, 0]],
  D2: [[0, 0], [0, 1]],
  L3: [[0, 0], [1, 0], [1, 1]],
};

export const PIECE_KINDS = Object.keys(BASE) as PieceKind[];

/** Ağırlıklı rastgele seçim için varsayılan ağırlıklar. */
export const PIECE_WEIGHTS: Record<PieceKind, number> = {
  I: 9, O: 10, T: 10, S: 8, Z: 8, J: 9, L: 9, D1: 6, D2: 8, L3: 9,
};

export const DEFAULT_COLORS: Record<Element, number> = {
  water: 10, leaf: 10, sun: 10, stone: 7, purple: 10, red: 10,
};

function normalize(cells: Offset[]): Offset[] {
  const minR = Math.min(...cells.map((c) => c[0]));
  const minC = Math.min(...cells.map((c) => c[1]));
  return cells
    .map(([r, c]) => [r - minR, c - minC] as Offset)
    .sort((a, b) => a[0] - b[0] || a[1] - b[1]);
}

function rotateCW(cells: Offset[]): Offset[] {
  const h = Math.max(...cells.map((c) => c[0])) + 1;
  return normalize(cells.map(([r, c]) => [c, h - 1 - r] as Offset));
}

const cache = new Map<string, Offset[]>();

/** Parçanın `rot` kez saat yönünde döndürülmüş hücreleri (sol üst = 0,0). */
export function shapeCells(kind: PieceKind, rot: number): Offset[] {
  const key = kind + ((rot % 4) + 4) % 4;
  let v = cache.get(key);
  if (!v) {
    v = normalize(BASE[kind]);
    for (let i = 0; i < ((rot % 4) + 4) % 4; i++) v = rotateCW(v);
    cache.set(key, v);
  }
  return v;
}

export function shapeSize(cells: Offset[]): { w: number; h: number } {
  return {
    h: Math.max(...cells.map((c) => c[0])) + 1,
    w: Math.max(...cells.map((c) => c[1])) + 1,
  };
}

/** Birbirinden farklı görünen döndürmeler (O için 1, I için 2 …). */
export function uniqueRotations(kind: PieceKind): number[] {
  const seen = new Set<string>();
  const out: number[] = [];
  for (let r = 0; r < 4; r++) {
    const k = JSON.stringify(shapeCells(kind, r));
    if (!seen.has(k)) {
      seen.add(k);
      out.push(r);
    }
  }
  return out;
}
