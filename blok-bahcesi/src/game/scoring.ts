import { COLS } from './types';
import type { ClearStep } from './board';

/** Aynı hamlede temizlenen satır sayısına göre taban puan: 1→100, 2→300, 3→600, 4+→1000 */
export function lineBase(n: number): number {
  if (n <= 0) return 0;
  return [0, 100, 300, 600][n] ?? 1000;
}

export function comboMultiplier(combo: number): number {
  return 1 + combo * 0.5;
}

/**
 * Bir temizleme adımının puanı.
 * - Taban puan satırlara eşit bölünür; her satırın payı taş olmayan hücre oranıyla çarpılır (taş = 0 katkı).
 * - Tamamen yeşil (çiçek) satır x2.
 * - Kombo çarpanı x(1 + kombo*0.5); zincirin her ek adımı +%50.
 */
export function stepPoints(step: ClearStep, combo: number, stepIndex: number): number {
  const n = step.rows.length;
  const share = lineBase(n) / n;
  let pts = 0;
  step.rows.forEach((r, i) => {
    const factor = (COLS - step.stoneCounts[i]) / COLS;
    pts += share * factor * (step.flowerRows.includes(r) ? 2 : 1);
  });
  pts *= comboMultiplier(combo) * (1 + 0.5 * stepIndex);
  return Math.round(pts / 10) * 10;
}

/** Kalan hamle oranına göre yıldız: bitirmek 1, %25+ → 2, %50+ → 3. */
export function starsFor(movesLeft: number | null, moveLimit: number | null): number {
  if (moveLimit === null || movesLeft === null) return 3;
  const ratio = movesLeft / moveLimit;
  if (ratio >= 0.5) return 3;
  if (ratio >= 0.25) return 2;
  return 1;
}

/** Kombo 3, 6, 9'da mevsim ilerler: 0 ilkbahar, 1 yaz, 2 sonbahar, 3 kış. */
export function seasonForCombo(combo: number): number {
  return Math.min(3, Math.floor(combo / 3));
}
