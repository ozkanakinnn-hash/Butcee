// Açgözlü (greedy) bot: bölüm zorluğunu ölçmek ve hamle sınırlarını kalibre etmek için.
import { findCell, landingRow, placeCells, resolveAll } from './board';
import type { Game } from './game';
import { shapeCells, uniqueRotations } from './pieces';
import { COLS, ROWS, type Grid } from './types';

function surface(g: Grid) {
  let holes = 0, agg = 0, maxH = 0, bump = 0;
  const hs: number[] = [];
  for (let c = 0; c < COLS; c++) {
    let top = ROWS;
    for (let r = 0; r < ROWS; r++) if (g[r][c] && g[r][c]!.k !== 'rock') { top = r; break; }
    const h = ROWS - top;
    hs.push(h);
    agg += h;
    maxH = Math.max(maxH, h);
    for (let r = top + 1; r < ROWS; r++) if (!g[r][c]) holes++;
  }
  for (let c = 1; c < COLS; c++) bump += Math.abs(hs[c] - hs[c - 1]);
  return { holes, agg, maxH, bump };
}

const rowFill = (g: Grid, r: number) => g[r].filter((c) => c).length;

export interface BotMove { hand: number; rot: number; col: number; score: number }

/** Eldeki tüm parça/döndürme/sütun kombinasyonlarını dener, en iyisini seçer. */
export function bestMove(game: Game): BotMove | null {
  const s = game.state;
  const goal = game.level.goal.type;
  let best: BotMove | null = null;
  const gBefore = findCell(s.grid, 'gardener');
  s.hand.forEach((p, hi) => {
    if (!p) return;
    for (const rot of uniqueRotations(p.kind)) {
      const cells = shapeCells(p.kind, rot);
      for (let col = 0; col < COLS; col++) {
        const row = landingRow(s.grid, cells, col);
        if (row === null) continue;
        const placed = placeCells(s.grid, cells, row, col, p.el);
        const { grid, steps } = resolveAll(placed);
        let score = 0;
        for (const st of steps) {
          score += st.rows.length * 120;
          score += st.rescued.length * 1500 * (goal === 'animals' ? 1 : 0.1);
          score += st.sprouted.length * 1300 * (goal === 'seeds' ? 1 : 0.1);
          score += st.damage * (goal === 'boss' ? 400 : 0);
          if (st.gardener) score += 1e6;
        }
        const sf = surface(grid);
        score -= sf.holes * 45 + sf.agg * 3 + sf.maxH * 12 + sf.bump * 4;
        if (sf.maxH >= ROWS - 2) score -= 3000;
        if (goal === 'animals' || goal === 'seeds') {
          for (let r = 0; r < ROWS; r++) {
            if (grid[r].some((c) => c && (c.k === 'animal' || c.k === 'seed'))) score += rowFill(grid, r) * 18;
          }
        }
        if (goal === 'rescue') {
          const gp = findCell(grid, 'gardener');
          if (gp && gBefore) {
            score -= (gp.r - gBefore.r) * 600; // bahçıvanı suya yaklaştırma
            score += rowFill(grid, gp.r) * 70;
          }
        }
        score += Math.random() * 2; // eşitlikleri boz
        if (!best || score > best.score) best = { hand: hi, rot, col, score };
      }
    }
  });
  return best;
}

/** Botu bölüm bitene kadar oynatır. */
export function playOut(game: Game, maxMoves = 200): { won: boolean; moves: number } {
  while (game.state.status === 'playing' && game.state.movesUsed < maxMoves) {
    const m = bestMove(game);
    if (!m) break;
    game.drop(m.hand, m.rot, m.col);
  }
  return { won: game.state.status === 'won', moves: game.state.movesUsed };
}
