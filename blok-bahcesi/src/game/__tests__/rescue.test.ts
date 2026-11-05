import { describe, expect, it } from 'vitest';
import { Game } from '../game';
import { playOut } from '../bot';
import { decodeGarden, dueMilestones, encodeGarden, levelReward, CATALOG } from '../../economy';
import type { LevelDef } from '../types';

const rescue = (map: string[], waterEvery: number, startHand?: LevelDef['startHand']): LevelDef =>
  ({ id: 101, map, goal: { type: 'rescue' }, moves: null, waterEvery, startHand, colors: { stone: 0 } });

describe('Bahçıvanı Kurtar', () => {
  it('bahçıvanın satırı temizlenince kurtarılır ve bölüm kazanılır', () => {
    const g = new Game('adventure', rescue(['TGTTTTT.'], 3, ['D1']), 1);
    expect(g.goalRemaining()).toBe(1);
    const r = g.drop(0, 0, 7)!;
    expect(r.steps[0].gardener).toEqual({ r: 11, c: 1 });
    expect(r.status).toBe('won');
  });
  it('su her N hamlede 1 satır yükselir, bahçıvana ulaşınca kaybedilir', () => {
    const g = new Game('adventure', rescue(['.G......', 'T.......', 'T.......'], 2, ['D1', 'D1', 'D1', 'D1', 'D1']), 1);
    expect(g.gardenerMargin()).toBe(2); // satır 9, su 0
    g.drop(0, 0, 7);
    expect(g.state.water).toBe(0);
    expect(g.waterRisesNext()).toBe(true);
    const r = g.drop(0, 0, 6)!;
    expect(r.waterRose).toBe(true);
    expect(g.state.water).toBe(1);
    g.drop(0, 0, 5); g.drop(0, 0, 4);
    expect(g.gardenerMargin()).toBe(0);
    g.drop(0, 0, 3); g.drop(0, 0, 2);
    expect(g.state.status).toBe('lost');
    expect(g.state.loseReason).toBe('flooded');
  });
  it('elmasla su çekilince oyun devam eder', () => {
    const g = new Game('adventure', rescue(['.G......', 'T.......', 'T.......'], 1, ['D1', 'D1', 'D1', 'D1', 'D1']), 1);
    for (let i = 0; i < 3; i++) g.drop(0, 0, 7 - i);
    expect(g.state.loseReason).toBe('flooded');
    g.recedeWater(2);
    expect(g.state.status).toBe('playing');
    expect(g.state.water).toBe(1);
  });
  it('bot ilk kurtarma bölümünü çoğunlukla çözer (su yavaşken)', async () => {
    const { RESCUE_MAPS } = await import('../../levels-base');
    let w = 0;
    for (let i = 0; i < 10; i++) if (playOut(new Game('adventure', rescue(RESCUE_MAPS[0].map, 3), 50 + i)).won) w++;
    expect(w).toBeGreaterThanOrEqual(7);
  });
});

describe('elmasla devam', () => {
  it('+5 hamle hamle bitince oyunu sürdürür', () => {
    const g = new Game('adventure', { id: 9, map: [], goal: { type: 'lines', count: 5 }, moves: 1 }, 1);
    g.drop(0, 0, 0);
    expect(g.state.loseReason).toBe('moves');
    g.addMoves(5);
    expect(g.state.status).toBe('playing');
    expect(g.movesLeft).toBe(5);
  });
  it('ek geri alma hakkı', () => {
    const g = new Game('adventure', { id: 9, map: [], goal: { type: 'lines', count: 5 }, moves: 10 }, 1);
    g.state.undoLeft = 0;
    g.drop(0, 0, 0);
    expect(g.canUndo()).toBe(false);
    g.grantUndo();
    expect(g.undo()).toBe(true);
  });
});

describe('ekonomi', () => {
  it('bahçe kodlaması 64 karakteri aşmaz ve geri çözülür', () => {
    const snap = { house: 3, slots: ['fence', null, 'gazebo', 'pond', null, null, 'tree', 'bench'] as const,
      animals: { hedgehog: 12, turtle: 3, ladybug: 40 }, sprouts: 321 };
    const tag = encodeGarden({ ...snap, slots: [...snap.slots] });
    expect(tag.length).toBeLessThanOrEqual(64);
    expect(tag).toMatch(/^[0-9a-z\-]+$/);
    expect(decodeGarden(tag)).toEqual({ ...snap, slots: [...snap.slots] });
    expect(decodeGarden('çöp')).toBeNull();
  });
  it('kritik eşikler bir kez verilir', () => {
    expect(dueMilestones([], 26, 10).map((m) => m.key)).toEqual(['s10', 's25', 'l10']);
    expect(dueMilestones(['s10', 's25', 'l10'], 26, 10)).toEqual([]);
  });
  it('bölüm ödülleri', () => {
    expect(levelReward('rescue', true, false)).toBe(30);
    expect(levelReward('boss', true, true)).toBe(23);
    expect(levelReward('normal', false, false)).toBe(0);
  });
  it('katalog fiyatları artan', () => {
    for (let i = 1; i < CATALOG.length; i++) expect(CATALOG[i].price).toBeGreaterThanOrEqual(CATALOG[i - 1].price);
  });
});
