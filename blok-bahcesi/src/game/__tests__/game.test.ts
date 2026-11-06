import { describe, expect, it } from 'vitest';
import { Game, BOSS_EVERY } from '../game';
import { findFullRows, parseMap } from '../board';
import { lineBase, seasonForCombo, starsFor, stepPoints } from '../scoring';
import { generateDaily, generateZen } from '../levelgen';
import { LEVELS, RESCUE_LEVELS } from '../../levels';
import { COLS, ROWS, type LevelDef } from '../types';

const lvl = (map: string[], extra: Partial<LevelDef> = {}): LevelDef => ({
  id: 99, map, goal: { type: 'lines', count: 99 }, moves: 30, ...extra,
});

describe('puan ve kombo', () => {
  it('çoklu satır tablosu', () => {
    expect([1, 2, 3, 4, 5].map(lineBase)).toEqual([100, 300, 600, 1000, 1000]);
  });
  it('taş puan vermez, çiçek 2 kat', () => {
    const base = { before: [], rescued: [], sprouted: [], sunBreaks: [], weedHits: [], damage: 1, gardener: null };
    expect(stepPoints({ ...base, rows: [11], flowerRows: [], stoneCounts: [0] }, 0, 0)).toBe(100);
    expect(stepPoints({ ...base, rows: [11], flowerRows: [], stoneCounts: [8] }, 0, 0)).toBe(0);
    expect(stepPoints({ ...base, rows: [11], flowerRows: [], stoneCounts: [4] }, 0, 0)).toBe(50);
    expect(stepPoints({ ...base, rows: [11], flowerRows: [11], stoneCounts: [0] }, 0, 0)).toBe(200);
    expect(stepPoints({ ...base, rows: [11], flowerRows: [], stoneCounts: [0] }, 2, 0)).toBe(200);
  });
  it('ardışık temizlemelerde kombo artar, temizlemesiz hamlede sıfırlanır', () => {
    // her satır 7 dolu, 7. sütun boş → D1 ile satır satır temizlenir
    const g = new Game('adventure', lvl([
      'TTTTTTT.', 'TTTTTTT.', 'TTTTTTT.', 'TTTTTTT.',
    ], { startHand: ['D1', 'D1', 'D1', 'D1', 'D1'], colors: { stone: 0 } }), 1);
    const r1 = g.drop(0, 0, 7)!;
    expect(r1.points).toBe(100);
    expect(g.state.combo).toBe(1);
    const r2 = g.drop(1, 0, 7)!;
    expect(r2.points).toBe(150); // x1.5
    expect(g.state.combo).toBe(2);
    const r3 = g.drop(2, 0, 0)!; // temizleme yok
    expect(r3.points).toBe(0);
    expect(g.state.combo).toBe(0);
  });
  it('mevsim kombo 3, 6, 9 da ilerler', () => {
    expect([0, 2, 3, 5, 6, 9, 12].map(seasonForCombo)).toEqual([0, 0, 1, 1, 2, 3, 3]);
  });
  it('yıldızlar', () => {
    expect(starsFor(0, 20)).toBe(1);
    expect(starsFor(5, 20)).toBe(2);
    expect(starsFor(10, 20)).toBe(3);
  });
});

describe('oyun akışı', () => {
  it('kullanılan parçanın yerine önizlemedeki ilk parça gelir', () => {
    const g = new Game('adventure', lvl([], { startHand: ['O', 'I', 'T', 'L', 'J'] }), 3);
    const q0 = g.state.queue[0];
    g.drop(1, 0, 0);
    expect(g.state.hand[1]).toEqual(q0);
    expect(g.state.queue).toHaveLength(2);
  });
  it('aynı parça arka arkaya 3 kez gelmez', () => {
    const g = new Game('zen', lvl([], { moves: null }), 7);
    const seq: string[] = [];
    for (let i = 0; i < 400; i++) {
      const p = (g as unknown as { genPiece: () => { kind: string } }).genPiece();
      seq.push(p.kind);
    }
    for (let i = 2; i < seq.length; i++) {
      expect(seq[i] === seq[i - 1] && seq[i] === seq[i - 2]).toBe(false);
    }
  });
  it('geri al hamleyi tamamen geri alır, 3 hak ile sınırlı', () => {
    const g = new Game('adventure', lvl(['TTTTTTT.'], { startHand: ['D1', 'O', 'T', 'I', 'L'] }), 5);
    const snap = JSON.stringify(g.state);
    g.drop(0, 0, 7);
    expect(g.state.lines).toBe(1);
    expect(g.undo()).toBe(true);
    const after = { ...JSON.parse(snap), undoLeft: 2 };
    expect(g.state).toEqual(after);
    for (let i = 0; i < 3; i++) { g.drop(0, 0, 0); g.undo(); }
    expect(g.state.undoLeft).toBe(0);
    g.drop(0, 0, 0);
    expect(g.undo()).toBe(false);
  });
  it('hamle bitince hedef tamamlanmamışsa kaybedilir', () => {
    const g = new Game('adventure', lvl([], { moves: 1 }), 1);
    const r = g.drop(0, 0, 0)!;
    expect(r.status).toBe('lost');
    expect(g.state.loseReason).toBe('moves');
  });
  it('hedef tamamlanınca kazanılır ve yıldız hesaplanır', () => {
    const g = new Game('adventure', lvl(['THTTTTT.'], { goal: { type: 'animals' }, moves: 10, startHand: ['D1'] }), 1);
    const r = g.drop(0, 0, 7)!;
    expect(r.status).toBe('won');
    expect(g.stars()).toBe(3);
  });
});

describe('oyun sonu tespiti', () => {
  it('eldeki hiçbir parça hiçbir döndürme ve sütunda sığmıyorsa tıkanmış', () => {
    // Tüm sütunlar tepeye kadar dolu, yalnızca 0. sütunda 1 boşluk var.
    const map = Array(12).fill('.TTTTTTT');
    map[0] = '.KKKKKKK';
    const m2 = map.map((r, i) => (i === 0 ? r : 'KKKKKKKK'));
    m2[0] = '.KKKKKKK';
    const g = new Game('adventure', lvl(m2, { startHand: ['O', 'I', 'T'] }), 1);
    expect(g.isStuck()).toBe(true);
    const g2 = new Game('adventure', lvl(m2, { startHand: ['O', 'D1', 'T'] }), 1);
    expect(g2.isStuck()).toBe(false);
  });
  it('Zen modunda tıkanınca üst 4 satır temizlenir, kaybedilmez', () => {
    const map = Array(12).fill('KKKKKKK.');
    const g = new Game('zen', lvl(map, { moves: null, startHand: ['D1', 'O', 'O', 'O', 'O'] }), 1);
    const r = g.drop(0, 0, 7)!;
    expect(r.status).toBe('playing');
    expect(r.zenRelief.length).toBeGreaterThan(0);
    expect(g.isStuck()).toBe(false);
  });
});

describe('boss', () => {
  it('her 4 hamlede saldırır, 1 hamle önce uyarır; satırlar hasar verir', () => {
    const g = new Game('adventure', lvl(['TTTTTTT.'], {
      goal: { type: 'boss' }, bossHp: 12, moves: 40, startHand: ['D1', 'D1', 'D1', 'D1', 'D1'],
    }), 9);
    g.drop(0, 0, 7);
    expect(g.state.bossHp).toBe(11);
    expect(g.attackSoon()).toBe(false);
    g.drop(0, 0, 0);
    g.drop(0, 0, 1);
    expect(g.attackSoon()).toBe(true);
    const r = g.drop(0, 0, 2)!;
    expect(g.state.movesUsed).toBe(BOSS_EVERY);
    expect(r.attack).not.toBeNull();
    const last = g.grid[ROWS - 1];
    expect(last.filter((c) => c?.k === 'weed').length).toBe(COLS - 1);
  });
  it('taşma olursa bölüm kaybedilir', () => {
    const map = Array(12).fill('T.......');
    const g = new Game('adventure', lvl(map, {
      goal: { type: 'boss' }, bossHp: 12, moves: 40, startHand: ['D1', 'D1', 'D1', 'D1', 'D1'],
    }), 9);
    for (let i = 0; i < 4 && g.state.status === 'playing'; i++) g.drop(0, 0, 5 + (i % 3));
    expect(g.state.status).toBe('lost');
    expect(g.state.loseReason).toBe('overflow');
  });
});

describe('bölüm verisi', () => {
  it.each([...LEVELS, ...RESCUE_LEVELS].map((l) => [l.id, l] as const))('bölüm %i geçerli', (_id, l) => {
    l.map.forEach((row) => expect(row).toHaveLength(COLS));
    expect(l.map.length).toBeLessThanOrEqual(ROWS);
    const grid = parseMap(l.map);
    expect(findFullRows(grid)).toEqual([]);
    const g = new Game('adventure', l, 1);
    expect(g.isStuck()).toBe(false);
    if (l.goal.type === 'seeds') expect(g.totalSeeds).toBeGreaterThanOrEqual(l.goal.count!);
    if (l.goal.type === 'animals') expect(g.totalAnimals).toBeGreaterThan(0);
    if (l.goal.type === 'boss') expect(l.bossHp).toBeGreaterThan(0);
    if (l.goal.type === 'rescue') { expect(g.gardenerMargin()).toBeGreaterThan(0); expect(l.waterEvery).toBeGreaterThan(0); }
  });
  it('100 bölüm, her 10. bölüm boss; 10 kurtarma bölümü', () => {
    expect(LEVELS).toHaveLength(100);
    expect(LEVELS.map((l) => l.id)).toEqual(Array.from({ length: 100 }, (_, i) => i + 1));
    expect(LEVELS.filter((l) => l.goal.type === 'boss').map((l) => l.id)).toEqual([10, 20, 30, 40, 50, 60, 70, 80, 90, 100]);
    expect(RESCUE_LEVELS).toHaveLength(10);
  });
  it('hamle sınırları kademeli artar (ilk 10 ortalaması < son 10 ortalaması)', () => {
    const avg = (a: typeof LEVELS) => a.reduce((x, l) => x + (l.moves ?? 0), 0) / a.length;
    expect(avg(LEVELS.slice(0, 10))).toBeLessThan(avg(LEVELS.slice(90)));
  });
  it('günlük bölüm aynı tohumla aynıdır ve geçerlidir', () => {
    for (let s = 0; s < 50; s++) {
      const a = generateDaily(s);
      expect(generateDaily(s)).toEqual(a);
      expect(findFullRows(parseMap(a.map))).toEqual([]);
      expect(findFullRows(parseMap(generateZen(s).map))).toEqual([]);
    }
  });
});
