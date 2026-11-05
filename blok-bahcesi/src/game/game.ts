import {
  bossAttack, cloneGrid, countCells, emptyGrid, findFullRows, landingRow, fitsAt, parseMap, placeCells,
  resolveAll, type ClearStep,
} from './board';
import { DEFAULT_COLORS, PIECE_WEIGHTS, shapeCells, uniqueRotations } from './pieces';
import { Rng } from './rng';
import { comboMultiplier, seasonForCombo, starsFor, stepPoints } from './scoring';
import {
  COLS, ROWS, type AnimalKind, type Element, type Grid, type LevelDef, type LoseReason, type Mode, type Piece,
  type PieceKind, type Status,
} from './types';

export const BOSS_EVERY = 4;
export const UNDO_LIMIT = 3;

export interface GameState {
  grid: Grid;
  hand: (Piece | null)[];
  queue: Piece[];
  rng: number;
  nextId: number;
  recent: PieceKind[];
  score: number;
  combo: number;
  maxCombo: number;
  season: number;
  movesUsed: number;
  lines: number;
  rescued: Record<AnimalKind, number>;
  sprouted: number;
  bossHp: number;
  status: Status;
  loseReason: LoseReason;
  undoLeft: number;
}

export interface MoveResult {
  piece: Piece;
  rot: number;
  col: number;
  row: number;
  /** Parça yerleştikten hemen sonraki, temizleme öncesi tahta. */
  placed: Grid;
  steps: ClearStep[];
  points: number;
  comboBefore: number;
  combo: number;
  seasonChanged: boolean;
  attack: { gap: number; before: Grid } | null;
  zenRelief: number[];
  final: Grid;
  status: Status;
}

export class Game {
  readonly mode: Mode;
  readonly level: LevelDef;
  readonly moveLimit: number | null;
  readonly totalAnimals: number;
  readonly totalSeeds: number;
  state: GameState;
  private history: string[] = [];
  private colorW: Record<Element, number>;

  constructor(mode: Mode, level: LevelDef, seed: number) {
    this.mode = mode;
    this.level = level;
    this.moveLimit = mode === 'zen' || mode === 'classic' ? null : level.moves;
    this.colorW = { ...DEFAULT_COLORS, ...(level.colors ?? {}) };
    const grid = parseMap(level.map);
    this.totalAnimals = countCells(grid, (c) => c.k === 'animal');
    this.totalSeeds = countCells(grid, (c) => c.k === 'seed');
    this.state = {
      grid, hand: [], queue: [], rng: seed >>> 0, nextId: 1, recent: [], score: 0, combo: 0, maxCombo: 0,
      season: 0, movesUsed: 0, lines: 0, rescued: { hedgehog: 0, turtle: 0, ladybug: 0 }, sprouted: 0,
      bossHp: level.bossHp ?? 0, status: 'playing', loseReason: null, undoLeft: UNDO_LIMIT,
    };
    const start = level.startHand ?? [];
    for (let i = 0; i < 3; i++) this.state.hand.push(this.genPiece(start[i]));
    for (let i = 0; i < 2; i++) this.state.queue.push(this.genPiece(start[3 + i]));
  }

  // ---------- parça üretimi ----------
  private genPiece(forced?: PieceKind): Piece {
    const rng = new Rng(this.state.rng);
    let kind: PieceKind;
    if (forced) kind = forced;
    else {
      const r = this.state.recent;
      // Aynı parça arka arkaya 3 kez gelmesin.
      const ex = r.length >= 2 && r[r.length - 1] === r[r.length - 2] ? new Set([r[r.length - 1]]) : undefined;
      kind = rng.pickWeighted(PIECE_WEIGHTS, ex);
    }
    const el = rng.pickWeighted(this.colorW);
    this.state.rng = rng.s;
    this.state.recent = [...this.state.recent.slice(-1), kind];
    return { id: this.state.nextId++, kind, el };
  }

  // ---------- sorgular ----------
  get grid() { return this.state.grid; }
  get movesLeft(): number | null {
    return this.moveLimit === null ? null : Math.max(0, this.moveLimit - this.state.movesUsed);
  }
  get isBoss() { return this.level.goal.type === 'boss'; }
  get rescuedTotal() { const r = this.state.rescued; return r.hedgehog + r.turtle + r.ladybug; }

  /** Hedefte kalan sayı (hedef yoksa null). */
  goalRemaining(): number | null {
    const g = this.level.goal;
    switch (g.type) {
      case 'animals': return Math.max(0, (g.count ?? this.totalAnimals) - this.rescuedTotal);
      case 'seeds': return Math.max(0, (g.count ?? this.totalSeeds) - this.state.sprouted);
      case 'lines': return Math.max(0, (g.count ?? 0) - this.state.lines);
      case 'boss': return Math.max(0, this.state.bossHp);
      default: return null;
    }
  }

  /** Bir sonraki hamleden sonra fırtına saldıracak mı? (1 hamle önceden uyarı) */
  attackSoon(): boolean {
    return this.isBoss && this.state.status === 'playing' && (this.state.movesUsed + 1) % BOSS_EVERY === 0;
  }

  stars(): number { return starsFor(this.movesLeft, this.moveLimit); }

  canFitAnywhere(p: Piece, grid = this.state.grid): boolean {
    for (const rot of uniqueRotations(p.kind)) {
      const cells = shapeCells(p.kind, rot);
      for (let col = 0; col < COLS; col++) if (landingRow(grid, cells, col) !== null) return true;
    }
    return false;
  }

  isStuck(grid = this.state.grid): boolean {
    return !this.state.hand.some((p) => p && this.canFitAnywhere(p, grid));
  }

  landing(handIdx: number, rot: number, col: number): number | null {
    const p = this.state.hand[handIdx];
    if (!p) return null;
    return landingRow(this.state.grid, shapeCells(p.kind, rot), col);
  }

  // ---------- hamleler ----------
  /** Parçayı (rot, col) ile düz aşağı bırakır. */
  drop(handIdx: number, rot: number, col: number): MoveResult | null {
    const row = this.landing(handIdx, rot, col);
    if (row === null) return null;
    return this.placeAt(handIdx, rot, col, row);
  }

  /** Parçayı belirli konuma yerleştirir (Klasik Hız modunda otomatik düşüş için de kullanılır). */
  placeAt(handIdx: number, rot: number, col: number, row: number): MoveResult | null {
    const s = this.state;
    const piece = s.hand[handIdx];
    if (s.status !== 'playing' || !piece) return null;
    const cells = shapeCells(piece.kind, rot);
    if (row < 0 || !fitsAt(s.grid, cells, row, col)) return null;

    this.history.push(JSON.stringify(s));
    if (this.history.length > 10) this.history.shift();

    const placed = placeCells(s.grid, cells, row, col, piece.el);
    const { grid: cleared, steps } = resolveAll(placed);
    const comboBefore = s.combo;
    let points = 0;
    steps.forEach((st, i) => { points += stepPoints(st, comboBefore, i); });
    let lines = 0;
    for (const st of steps) {
      lines += st.rows.length;
      s.sprouted += st.sprouted.length;
      for (const a of st.rescued) s.rescued[a.a]++;
      if (this.isBoss) s.bossHp = Math.max(0, s.bossHp - st.damage);
    }
    s.lines += lines;
    s.score += points;
    s.combo = lines > 0 ? s.combo + 1 : 0;
    s.maxCombo = Math.max(s.maxCombo, s.combo);
    const newSeason = Math.max(s.season, seasonForCombo(s.combo));
    const seasonChanged = newSeason > s.season;
    s.season = newSeason;
    s.movesUsed++;
    s.grid = cleared;

    // Eli yenile: önizlemedeki ilk parça boşalan yere geçer.
    s.hand[handIdx] = s.queue.shift() ?? null;
    s.queue.push(this.genPiece());

    // Hedef kontrolü
    let attack: MoveResult['attack'] = null;
    const zenRelief: number[] = [];
    if (this.goalRemaining() === 0) {
      s.status = 'won';
    } else {
      if (this.isBoss && s.movesUsed % BOSS_EVERY === 0) {
        const rng = new Rng(s.rng);
        const before = s.grid;
        const res = bossAttack(s.grid, rng);
        s.rng = rng.s;
        s.grid = res.grid;
        attack = { gap: res.gap, before };
        if (res.overflow) this.lose('overflow');
      }
      if (s.status === 'playing') {
        if (this.mode === 'zen') {
          // Zen: tahta tıkanırsa üstten 4'er satır yumuşakça temizlenir.
          for (let start = 0; start < ROWS && this.isStuck(); start += 4) {
            const g = cloneGrid(s.grid);
            for (let r = start; r < Math.min(ROWS, start + 4); r++) {
              g[r] = Array(COLS).fill(null);
              zenRelief.push(r);
            }
            s.grid = g;
          }
        } else if (this.movesLeft === 0) this.lose('moves');
        else if (this.isStuck()) this.lose('stuck');
      }
    }

    return {
      piece, rot, col, row, placed, steps, points, comboBefore, combo: s.combo, seasonChanged, attack,
      zenRelief, final: s.grid, status: s.status,
    };
  }

  private lose(reason: LoseReason) {
    this.state.status = 'lost';
    this.state.loseReason = reason;
  }

  canUndo(): boolean {
    return this.history.length > 0 && this.state.undoLeft > 0 && this.state.status !== 'won' && this.mode !== 'classic';
  }

  /** Son hamleyi tümüyle geri alır. */
  undo(): boolean {
    if (!this.canUndo()) return false;
    const left = this.state.undoLeft - 1;
    this.state = JSON.parse(this.history.pop()!);
    this.state.undoLeft = left;
    return true;
  }

  /** Klasik Hız: yeni parçanın başlama satırı (sığmazsa null → oyun biter). */
  spawnFits(handIdx: number, rot: number, col: number): boolean {
    const p = this.state.hand[handIdx];
    return !!p && fitsAt(this.state.grid, shapeCells(p.kind, rot), 0, col);
  }

  forceLose(reason: LoseReason) { this.lose(reason); }

  static comboMultiplier = comboMultiplier;
}

export { emptyGrid, findFullRows };
