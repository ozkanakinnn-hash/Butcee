import type { Element } from '../game/types';

export const EL_COLOR: Record<Element, string> = {
  // Pastel ama ayrışan tonlar (renk körlüğü doğrulayıcısıyla seçildi; simgeler ikincil kodlama)
  water: '#4aa8ff',
  leaf: '#2bb5a0',
  sun: '#ffc23d',
  stone: '#b3b6c2',
  purple: '#d9a3fb',
  red: '#ff7a6b',
};

/** Mevsim arka plan tonları: ilkbahar, yaz, sonbahar, kış (üst, alt). */
export const SEASON_TINT: [string, string][] = [
  ['#c8f0d2', '#ffd6e6'],
  ['#fff1b8', '#ffd9a8'],
  ['#ffd2b0', '#f2b99e'],
  ['#d8ecff', '#f4f8ff'],
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
