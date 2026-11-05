import type { Element } from '../game/types';

export const EL_COLOR: Record<Element, string> = {
  water: '#2f8cf0',
  leaf: '#43b649',
  sun: '#ff9322',
  stone: '#8f9398',
  purple: '#9b5bd8',
  red: '#e5444d',
};

/** Mevsim arka plan tonları: ilkbahar, yaz, sonbahar, kış (üst, alt). */
export const SEASON_TINT: [string, string][] = [
  ['#7a9a4a', '#f2c6d6'],
  ['#e8b93c', '#f7e08a'],
  ['#d0672a', '#8a3b1d'],
  ['#9cc1e0', '#e8f2fb'],
];

export function shade(hex: string, amt: number): string {
  const n = parseInt(hex.slice(1), 16);
  let r = (n >> 16) & 255, g = (n >> 8) & 255, b = n & 255;
  if (amt >= 0) {
    r += (255 - r) * amt; g += (255 - g) * amt; b += (255 - b) * amt;
  } else {
    r *= 1 + amt; g *= 1 + amt; b *= 1 + amt;
  }
  return `rgb(${r | 0},${g | 0},${b | 0})`;
}
