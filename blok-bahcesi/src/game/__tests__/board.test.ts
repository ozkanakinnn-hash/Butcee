import { describe, expect, it } from 'vitest';
import {
  bossAttack, collapse, findFullRows, landingRow, parseMap, resolveAll, resolveStep,
} from '../board';
import { shapeCells, uniqueRotations } from '../pieces';
import { Rng } from '../rng';
import { COLS, ROWS, type Grid } from '../types';

const toStr = (g: Grid) =>
  g.map((row) => row.map((c) => {
    if (!c) return '.';
    switch (c.k) {
      case 'soil': return 'T';
      case 'rock': return 'K';
      case 'seed': return 'S';
      case 'weed': return c.hp > 1 ? 'O' : 'o';
      case 'ice': return c.hp > 1 ? 'I' : 'i';
      case 'animal': return 'H';
      case 'block': return { water: 'b', leaf: 'g', sun: 'y', stone: 's', purple: 'p', red: 'r' }[c.el];
    }
  }).join(''));

const bottom = (g: Grid, n: number) => toStr(g).slice(ROWS - n);

describe('parçalar', () => {
  it('benzersiz döndürmeler', () => {
    expect(uniqueRotations('O')).toEqual([0]);
    expect(uniqueRotations('I')).toEqual([0, 1]);
    expect(uniqueRotations('D1')).toEqual([0]);
    expect(uniqueRotations('T').length).toBe(4);
    expect(uniqueRotations('S').length).toBe(2);
  });
  it('I dikey döndürülünce 4 satır yüksekliğinde', () => {
    expect(shapeCells('I', 1)).toEqual([[0, 0], [1, 0], [2, 0], [3, 0]]);
  });
});

describe('düşme', () => {
  it('parça ilk çarptığı yerde durur', () => {
    const g = parseMap(['T.......', 'TT......']);
    // O parçası sütun 0'da: sütun 0'ın tepesi satır 10 → O 8-9'a oturur
    expect(landingRow(g, shapeCells('O', 0), 0)).toBe(8);
    expect(landingRow(g, shapeCells('O', 0), 2)).toBe(10);
  });
  it('dolu sütuna sığmayan parça null döner', () => {
    const g = parseMap(Array(12).fill('T.......'));
    expect(landingRow(g, shapeCells('D1', 0), 0)).toBeNull();
    expect(landingRow(g, shapeCells('D1', 0), 1)).toBe(11);
  });
});

describe('satır temizleme ve kaydırma', () => {
  it('dolu satır silinir, üstündekiler iner (boşluklar korunur)', () => {
    const g = parseMap(['r.......', '.p......', 'TTTTTTTT']);
    expect(findFullRows(g)).toEqual([11]);
    const { grid, steps } = resolveAll(g);
    expect(steps).toHaveLength(1);
    expect(bottom(grid, 2)).toEqual(['r.......', '.p......']);
  });

  it('zincir: kaydıktan sonra oluşan yeni dolu satır da temizlenir (kaya sayesinde)', () => {
    // Satır 11 silinir; 1-7. sütunlar kayaya dayalı olduğu için inmez, 0. sütundaki 'r' iner
    // ve satır 10'u tamamlar → ikinci adım (zincir).
    const g = parseMap([
      'r.......',
      '.TTTTTTT',
      'TKKKKKKK',
    ]);
    const res = resolveAll(g);
    expect(res.steps).toHaveLength(2);
    expect(res.steps[1].rows).toEqual([10]);
    expect(bottom(res.grid, 2)).toEqual(['........', '.KKKKKKK']);
  });

  it('çoklu satır aynı adımda temizlenir', () => {
    const g = parseMap(['bb......', 'TTTTTTTT', 'TTTTTTTT']);
    const { steps, grid } = resolveAll(g);
    expect(steps[0].rows).toEqual([10, 11]);
    expect(bottom(grid, 1)).toEqual(['bb......']);
  });
});

describe('kaya', () => {
  it('satır doldurmaya sayılır ama silinmez; üstündekiler kayaya oturur', () => {
    const g = parseMap([
      'pr......',
      'pr......',
      'KTTTTTTT',
    ]);
    const { grid, steps } = resolveAll(g);
    expect(steps).toHaveLength(1);
    // Kaya yerinde, sütun 0'daki bloklar kayanın üstünde kalır; sütun 1 iner.
    expect(bottom(grid, 3)).toEqual(['p.......', 'pr......', 'Kr......']);
  });
  it('yalnızca kayalardan oluşan satır sonsuz döngüye girmez', () => {
    const g = parseMap(['KKKKKKKK']);
    expect(findFullRows(g)).toEqual([]);
  });
});

describe('element etkileri', () => {
  it('SU: üst ve alt satırdaki tohumlar filizlenir', () => {
    const g = parseMap([
      '.S......',
      'bTTTTTTT',
      'TS.....S',
    ]);
    const { step, grid } = resolveStep(g, [10]);
    expect(step.sprouted).toHaveLength(3);
    expect(toStr(grid).join('')).not.toContain('S');
  });
  it('SU yoksa tohum filizlenmez ve temizlenen satırda hayatta kalır', () => {
    const g = parseMap(['.S......', 'rTTSTTTT']);
    const { step, grid } = resolveStep(g, [11]);
    expect(step.sprouted).toHaveLength(0);
    expect(bottom(grid, 1)).toEqual(['.S.S....']);
  });
  it('YAPRAK: tamamen yeşil satır çiçek açar ve 2 hasar verir', () => {
    const g = parseMap(['gggggggg']);
    const { step } = resolveStep(g, [11]);
    expect(step.flowerRows).toEqual([11]);
    expect(step.damage).toBe(2);
    const g2 = parseMap(['gggggggr']);
    expect(resolveStep(g2, [11]).step.flowerRows).toEqual([]);
  });
  it('GÜNEŞ: kendi sütunundaki en yakın yabani otu/buzu kırar', () => {
    const g = parseMap([
      'O.......',
      'r.......',
      'I.......',
      'yTTTTTTT',
    ]);
    const { step, grid } = resolveStep(g, [11]);
    expect(step.sunBreaks).toEqual([{ r: 10, c: 0 }]);
    // buz kırıldı, satır silindi → r ve O 2 satır değil 1 satır iner (buz deliği kalır)
    expect(bottom(grid, 3)).toEqual(['O.......', 'r.......', '........']);
  });
  it('yabani ot iki temizleme ister', () => {
    const g = parseMap(['OTTTTTTT']);
    const r1 = resolveStep(g, [11]);
    expect(bottom(r1.grid, 1)).toEqual(['o.......']);
    const g2 = parseMap(['oTTTTTTT']);
    expect(bottom(resolveStep(g2, [11]).grid, 1)).toEqual(['........']);
  });
  it('hayvan bulunduğu satır temizlenince kurtarılır', () => {
    const g = parseMap(['THTTTTTP']);
    const { step } = resolveStep(g, [11]);
    expect(step.rescued.map((a) => a.a)).toEqual(['hedgehog', 'turtle']);
  });
});

describe('boss saldırısı', () => {
  it('1 boşluklu ot satırı ekler ve her şeyi yukarı iter; taş sütunlarında ot oluşmaz', () => {
    const g = parseMap(['..s.....', 'T.......']);
    const res = bossAttack(g, new Rng(42));
    expect(res.overflow).toBe(false);
    const rows = toStr(res.grid);
    expect(rows[ROWS - 3]).toBe('..s.....');
    expect(rows[ROWS - 2]).toBe('T.......');
    const last = rows[ROWS - 1];
    expect(last[2]).toBe('.'); // taş sütunu
    expect(last[res.gap]).toBe('.');
    expect([...last].filter((ch) => ch === 'O').length).toBe(res.gap === 2 ? COLS - 1 : COLS - 2);
  });
  it('tepeden taşma tespit edilir', () => {
    const g = parseMap(Array(12).fill('T.......'));
    expect(bossAttack(g, new Rng(1)).overflow).toBe(true);
  });
  it('collapse kayaları hareket ettirmez', () => {
    const g = parseMap(['K', 'T']);
    const cleared = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
    cleared[11][0] = true;
    expect(bottom(collapse(g, cleared), 2)).toEqual(['K.......', '........']);
  });
});
