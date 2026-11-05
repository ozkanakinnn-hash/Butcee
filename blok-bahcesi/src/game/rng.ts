/** mulberry32 — durumu tek bir sayı olduğu için geri almada kolayca saklanır. */
export class Rng {
  constructor(public s: number) {
    this.s = s >>> 0;
  }
  next(): number {
    this.s = (this.s + 0x6d2b79f5) >>> 0;
    let t = this.s;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  }
  int(n: number): number {
    return Math.floor(this.next() * n);
  }
  pickWeighted<T extends string>(w: Partial<Record<T, number>>, exclude?: Set<T>): T {
    const entries = (Object.entries(w) as [T, number][]).filter(([k, v]) => v > 0 && !exclude?.has(k));
    const total = entries.reduce((a, [, v]) => a + v, 0);
    let x = this.next() * total;
    for (const [k, v] of entries) {
      x -= v;
      if (x < 0) return k;
    }
    return entries[entries.length - 1][0];
  }
}

/** Metinden (ör. tarih) sabit tohum üret. */
export function hashSeed(str: string): number {
  let h = 2166136261 >>> 0;
  for (let i = 0; i < str.length; i++) {
    h ^= str.charCodeAt(i);
    h = Math.imul(h, 16777619) >>> 0;
  }
  return h >>> 0;
}
