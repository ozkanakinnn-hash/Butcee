// Saf oyun tipleri — DOM'dan bağımsız.
export const COLS = 8;
export const ROWS = 12;

export type Element = 'water' | 'leaf' | 'sun' | 'stone' | 'purple' | 'red';
export const ELEMENTS: Element[] = ['water', 'leaf', 'sun', 'stone', 'purple', 'red'];

export type AnimalKind = 'hedgehog' | 'turtle' | 'ladybug';
export const ANIMALS: AnimalKind[] = ['hedgehog', 'turtle', 'ladybug'];

export type Cell =
  | { k: 'block'; el: Element }
  | { k: 'soil' }
  | { k: 'rock' }
  | { k: 'animal'; a: AnimalKind }
  | { k: 'gardener' }
  | { k: 'seed' }
  | { k: 'weed'; hp: number }
  | { k: 'ice'; hp: number };

export type Grid = (Cell | null)[][];

export type PieceKind = 'I' | 'O' | 'T' | 'S' | 'Z' | 'J' | 'L' | 'D1' | 'D2' | 'L3';

export interface Piece {
  id: number;
  kind: PieceKind;
  el: Element;
}

export type GoalType = 'animals' | 'seeds' | 'lines' | 'boss' | 'rescue' | 'none';

export interface LevelDef {
  id: number;
  /** 12 satır x 8 karakter. . boş, T toprak, K kaya, H/P/U hayvan, S tohum, O yabani ot, I buz,
   *  G bahçıvan (kurtarma bölümü)
 *  b/g/y/s/p/r önceden yerleştirilmiş renkli blok (su/yaprak/güneş/taş/mor/kırmızı) */
  map: string[];
  goal: { type: GoalType; count?: number };
  /** null = sınırsız */
  moves: number | null;
  bossHp?: number;
  /** Bahçıvanı Kurtar bölümü: su her `waterEvery` hamlede 1 satır yükselir. */
  waterEvery?: number;
  /** Bölüm adı anahtarı (kurtarma bölümleri için) */
  title?: string;
  /** Element ağırlıkları (verilmezse eşit, taş biraz daha az) */
  colors?: Partial<Record<Element, number>>;
  /** Öğretici: elde başlayan parçalar */
  startHand?: PieceKind[];
  /** Ekranda gösterilecek ipucu anahtarları (i18n) */
  hints?: string[];
}

export type Mode = 'adventure' | 'daily' | 'zen' | 'classic';
export type Status = 'playing' | 'won' | 'lost';
export type LoseReason = 'moves' | 'stuck' | 'overflow' | 'flooded' | null;
