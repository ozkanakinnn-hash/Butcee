// Hücre, hayvan ve simge çizimleri — tamamen kodla.
import type { AnimalKind, Cell, Element } from '../game/types';
import { EL_COLOR, shade } from './colors';

type C2D = CanvasRenderingContext2D;

export function roundRect(ctx: C2D, x: number, y: number, w: number, h: number, r: number) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}

/** Kabartmalı yuvarlak köşeli taban: üst açık kenar, alt koyu kenar. */
function bevelTile(ctx: C2D, s: number, base: string, inset = 0.05) {
  const p = s * inset, w = s - 2 * p, r = s * 0.2;
  roundRect(ctx, p, p, w, w, r);
  ctx.fillStyle = shade(base, -0.35);
  ctx.fill();
  roundRect(ctx, p, p, w, w - s * 0.07, r);
  const g = ctx.createLinearGradient(0, p, 0, p + w);
  g.addColorStop(0, shade(base, 0.25));
  g.addColorStop(1, base);
  ctx.fillStyle = g;
  ctx.fill();
  // üst parlak kenar
  ctx.save();
  roundRect(ctx, p + s * 0.08, p + s * 0.05, w - s * 0.16, s * 0.13, s * 0.07);
  ctx.fillStyle = 'rgba(255,255,255,0.35)';
  ctx.fill();
  ctx.restore();
}

function rng(seed: number) {
  let x = seed || 1;
  return () => ((x = (x * 16807) % 2147483647) / 2147483647);
}

// ---------- element simgeleri (renk körlüğü) ----------
export function drawSymbol(ctx: C2D, el: Element, cx: number, cy: number, s: number, color = 'rgba(255,255,255,0.85)') {
  ctx.save();
  ctx.fillStyle = color;
  ctx.strokeStyle = color;
  ctx.lineWidth = Math.max(1, s * 0.07);
  ctx.lineCap = 'round';
  const r = s * 0.2;
  switch (el) {
    case 'water': // damla
      ctx.beginPath();
      ctx.moveTo(cx, cy - r * 1.3);
      ctx.bezierCurveTo(cx + r * 1.1, cy - r * 0.1, cx + r, cy + r, cx, cy + r);
      ctx.bezierCurveTo(cx - r, cy + r, cx - r * 1.1, cy - r * 0.1, cx, cy - r * 1.3);
      ctx.fill();
      break;
    case 'leaf': // yaprak
      ctx.beginPath();
      ctx.moveTo(cx - r * 1.1, cy + r * 1.1);
      ctx.quadraticCurveTo(cx - r * 1.1, cy - r * 1.1, cx + r * 1.1, cy - r * 1.1);
      ctx.quadraticCurveTo(cx + r * 1.1, cy + r * 1.1, cx - r * 1.1, cy + r * 1.1);
      ctx.fill();
      ctx.strokeStyle = 'rgba(0,0,0,0.25)';
      ctx.beginPath();
      ctx.moveTo(cx - r * 0.9, cy + r * 0.9);
      ctx.lineTo(cx + r * 0.6, cy - r * 0.6);
      ctx.stroke();
      break;
    case 'sun': // daire + ışın
      ctx.beginPath();
      ctx.arc(cx, cy, r * 0.6, 0, Math.PI * 2);
      ctx.fill();
      for (let i = 0; i < 8; i++) {
        const a = (i * Math.PI) / 4;
        ctx.beginPath();
        ctx.moveTo(cx + Math.cos(a) * r * 0.95, cy + Math.sin(a) * r * 0.95);
        ctx.lineTo(cx + Math.cos(a) * r * 1.4, cy + Math.sin(a) * r * 1.4);
        ctx.stroke();
      }
      break;
    case 'stone': // nokta deseni
      for (const [dx, dy] of [[-1, -1], [1, -1], [0, 0], [-1, 1], [1, 1]]) {
        ctx.beginPath();
        ctx.arc(cx + dx * r * 0.75, cy + dy * r * 0.75, r * 0.26, 0, Math.PI * 2);
        ctx.fill();
      }
      break;
    case 'purple': // elmas
      ctx.beginPath();
      ctx.moveTo(cx, cy - r * 1.2);
      ctx.lineTo(cx + r * 0.9, cy);
      ctx.lineTo(cx, cy + r * 1.2);
      ctx.lineTo(cx - r * 0.9, cy);
      ctx.closePath();
      ctx.fill();
      break;
    case 'red': // üçgen
      ctx.beginPath();
      ctx.moveTo(cx, cy - r * 1.1);
      ctx.lineTo(cx + r * 1.1, cy + r * 0.9);
      ctx.lineTo(cx - r * 1.1, cy + r * 0.9);
      ctx.closePath();
      ctx.fill();
      break;
  }
  ctx.restore();
}

// ---------- hayvanlar ----------
export function drawAnimal(ctx: C2D, a: AnimalKind, cx: number, cy: number, s: number, t = 0) {
  ctx.save();
  ctx.translate(cx, cy);
  const u = s / 10;
  if (a === 'hedgehog') {
    // dikenler
    ctx.fillStyle = '#5a3b22';
    ctx.beginPath();
    for (let i = 0; i <= 8; i++) {
      const ang = Math.PI + (i / 8) * Math.PI;
      const rr = i % 2 ? 3.2 * u : 4.3 * u;
      ctx.lineTo(Math.cos(ang) * rr - 0.6 * u, Math.sin(ang) * rr + 1.6 * u);
    }
    ctx.closePath();
    ctx.fill();
    ctx.fillStyle = '#b07a4a';
    ctx.beginPath();
    ctx.ellipse(-0.3 * u, 1.8 * u, 3.4 * u, 2 * u, 0, 0, Math.PI * 2);
    ctx.fill();
    // yüz
    ctx.fillStyle = '#e8c49a';
    ctx.beginPath();
    ctx.ellipse(2.6 * u, 1.4 * u, 1.9 * u, 1.4 * u, -0.2, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = '#222';
    ctx.beginPath(); ctx.arc(4.3 * u, 1.3 * u, 0.55 * u, 0, Math.PI * 2); ctx.fill();
    ctx.beginPath(); ctx.arc(2.5 * u, 0.7 * u, 0.4 * u, 0, Math.PI * 2); ctx.fill();
  } else if (a === 'turtle') {
    const leg = Math.sin(t * 6) * 0.4 * u;
    ctx.fillStyle = '#8fbf5a';
    for (const [x, y] of [[-2.6, 2.6], [2.2, 2.6]]) {
      ctx.beginPath(); ctx.ellipse(x * u + leg, y * u, 0.9 * u, 0.7 * u, 0, 0, Math.PI * 2); ctx.fill();
    }
    ctx.beginPath(); ctx.ellipse(4 * u, 0.9 * u, 1.3 * u, 1.1 * u, 0, 0, Math.PI * 2); ctx.fill();
    ctx.fillStyle = '#222';
    ctx.beginPath(); ctx.arc(4.4 * u, 0.6 * u, 0.32 * u, 0, Math.PI * 2); ctx.fill();
    ctx.fillStyle = '#3f7d3a';
    ctx.beginPath();
    ctx.ellipse(-0.3 * u, 1.2 * u, 3.6 * u, 3 * u, 0, Math.PI, 0);
    ctx.closePath();
    ctx.fill();
    ctx.strokeStyle = '#a6d06a';
    ctx.lineWidth = 0.45 * u;
    ctx.beginPath();
    ctx.moveTo(-2.2 * u, 1.2 * u); ctx.lineTo(-1.2 * u, -0.6 * u); ctx.lineTo(0.6 * u, -0.6 * u); ctx.lineTo(1.6 * u, 1.2 * u);
    ctx.stroke();
  } else {
    // uğur böceği
    ctx.fillStyle = '#222';
    ctx.beginPath(); ctx.arc(0, -2.6 * u, 1.6 * u, 0, Math.PI * 2); ctx.fill();
    ctx.strokeStyle = '#222';
    ctx.lineWidth = 0.4 * u;
    ctx.beginPath(); ctx.moveTo(-0.6 * u, -3.8 * u); ctx.lineTo(-1.5 * u, -4.8 * u);
    ctx.moveTo(0.6 * u, -3.8 * u); ctx.lineTo(1.5 * u, -4.8 * u); ctx.stroke();
    ctx.fillStyle = '#e53935';
    ctx.beginPath(); ctx.arc(0, 0.6 * u, 3.4 * u, 0, Math.PI * 2); ctx.fill();
    ctx.strokeStyle = '#222';
    ctx.lineWidth = 0.45 * u;
    ctx.beginPath(); ctx.moveTo(0, -2.6 * u); ctx.lineTo(0, 4 * u); ctx.stroke();
    ctx.fillStyle = '#222';
    for (const [x, y] of [[-1.6, -0.6], [1.6, -0.6], [-1.8, 1.8], [1.8, 1.8], [-0.8, 3], [0.8, 3]]) {
      ctx.beginPath(); ctx.arc(x * u, y * u, 0.6 * u, 0, Math.PI * 2); ctx.fill();
    }
    ctx.fillStyle = 'rgba(255,255,255,0.5)';
    ctx.beginPath(); ctx.arc(-1.4 * u, -1.4 * u, 0.6 * u, 0, Math.PI * 2); ctx.fill();
  }
  ctx.restore();
}

export function drawSeed(ctx: C2D, cx: number, cy: number, s: number, sprout = 0) {
  const u = s / 10;
  ctx.save();
  ctx.translate(cx, cy);
  ctx.fillStyle = '#d9b27a';
  ctx.strokeStyle = '#7a5428';
  ctx.lineWidth = 0.5 * u;
  ctx.beginPath(); ctx.ellipse(0, 1.5 * u, 2.2 * u, 3 * u, 0.3, 0, Math.PI * 2); ctx.fill(); ctx.stroke();
  ctx.strokeStyle = '#7a5428';
  ctx.beginPath(); ctx.moveTo(-0.5 * u, -0.6 * u); ctx.quadraticCurveTo(0.4 * u, 1.5 * u, 0.4 * u, 4 * u); ctx.stroke();
  // minik filiz
  ctx.strokeStyle = '#5fbf45';
  ctx.lineWidth = 0.7 * u;
  ctx.beginPath(); ctx.moveTo(0.3 * u, -1.2 * u); ctx.quadraticCurveTo(0.5 * u, -2.8 * u - sprout * u, 1.6 * u, -3.4 * u - sprout * u); ctx.stroke();
  ctx.fillStyle = '#6ccf50';
  ctx.beginPath(); ctx.ellipse(2.2 * u, -3.6 * u - sprout * u, 1.2 * u, 0.6 * u, -0.4, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

export function drawFlower(ctx: C2D, cx: number, cy: number, s: number, color = '#ff8fb1') {
  const u = s / 10;
  ctx.save();
  ctx.translate(cx, cy);
  ctx.strokeStyle = '#4c9a3a';
  ctx.lineWidth = 0.8 * u;
  ctx.beginPath(); ctx.moveTo(0, 0); ctx.lineTo(0, 5 * u); ctx.stroke();
  ctx.fillStyle = '#5fb84a';
  ctx.beginPath(); ctx.ellipse(1.4 * u, 3.4 * u, 1.4 * u, 0.6 * u, -0.5, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = color;
  for (let i = 0; i < 5; i++) {
    const a = (i / 5) * Math.PI * 2;
    ctx.beginPath(); ctx.arc(Math.cos(a) * 1.5 * u, Math.sin(a) * 1.5 * u, 1.3 * u, 0, Math.PI * 2); ctx.fill();
  }
  ctx.fillStyle = '#ffd54a';
  ctx.beginPath(); ctx.arc(0, 0, 1.1 * u, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

function drawWeed(ctx: C2D, s: number, hp: number) {
  bevelTile(ctx, s, hp > 1 ? '#355e2b' : '#5d6e3a');
  const u = s / 10;
  ctx.save();
  ctx.translate(s / 2, s * 0.85);
  ctx.strokeStyle = hp > 1 ? '#8bd35a' : '#b5bd6a';
  ctx.lineWidth = 0.9 * u;
  ctx.lineCap = 'round';
  for (const [dx, h, bend] of [[-2.5, 5.5, -2], [0, 6.5, 1], [2.5, 5, 2], [-1, 4, -1]] as const) {
    ctx.beginPath();
    ctx.moveTo(dx * u, 0);
    ctx.quadraticCurveTo(dx * u + bend * u, -h * u * 0.6, dx * u + bend * 1.4 * u, -h * u);
    ctx.stroke();
  }
  ctx.restore();
  if (hp <= 1) crack(ctx, s);
}

function crack(ctx: C2D, s: number) {
  ctx.save();
  ctx.strokeStyle = 'rgba(255,255,255,0.7)';
  ctx.lineWidth = Math.max(1, s * 0.03);
  ctx.beginPath();
  ctx.moveTo(s * 0.25, s * 0.2); ctx.lineTo(s * 0.45, s * 0.45); ctx.lineTo(s * 0.35, s * 0.6); ctx.lineTo(s * 0.6, s * 0.8);
  ctx.moveTo(s * 0.45, s * 0.45); ctx.lineTo(s * 0.7, s * 0.35);
  ctx.stroke();
  ctx.restore();
}

function drawIce(ctx: C2D, s: number, hp: number) {
  const p = s * 0.05;
  roundRect(ctx, p, p, s - 2 * p, s - 2 * p, s * 0.18);
  const g = ctx.createLinearGradient(0, 0, s, s);
  g.addColorStop(0, '#e6f7ff');
  g.addColorStop(1, '#7cc4e8');
  ctx.fillStyle = g;
  ctx.fill();
  ctx.strokeStyle = 'rgba(255,255,255,0.9)';
  ctx.lineWidth = s * 0.05;
  ctx.stroke();
  ctx.fillStyle = 'rgba(255,255,255,0.75)';
  ctx.beginPath();
  ctx.moveTo(s * 0.2, s * 0.25); ctx.lineTo(s * 0.45, s * 0.2); ctx.lineTo(s * 0.22, s * 0.5); ctx.closePath();
  ctx.fill();
  // kar tanesi simgesi
  ctx.strokeStyle = 'rgba(40,110,160,0.6)';
  ctx.lineWidth = s * 0.045;
  for (let i = 0; i < 3; i++) {
    const a = (i * Math.PI) / 3;
    ctx.beginPath();
    ctx.moveTo(s / 2 + Math.cos(a) * s * 0.2, s * 0.58 + Math.sin(a) * s * 0.2);
    ctx.lineTo(s / 2 - Math.cos(a) * s * 0.2, s * 0.58 - Math.sin(a) * s * 0.2);
    ctx.stroke();
  }
  if (hp <= 1) crack(ctx, s);
}

function drawSoil(ctx: C2D, s: number, seed: number) {
  bevelTile(ctx, s, '#8a5a35', 0.03);
  const r = rng(seed * 7 + 3);
  for (let i = 0; i < 7; i++) {
    ctx.fillStyle = r() > 0.5 ? 'rgba(60,35,18,0.45)' : 'rgba(200,150,100,0.35)';
    ctx.beginPath();
    ctx.arc(s * (0.18 + r() * 0.64), s * (0.25 + r() * 0.55), s * (0.03 + r() * 0.04), 0, Math.PI * 2);
    ctx.fill();
  }
}

function drawRock(ctx: C2D, s: number) {
  const p = s * 0.06;
  ctx.beginPath();
  ctx.moveTo(p + s * 0.12, s - p);
  ctx.lineTo(p, s * 0.55);
  ctx.lineTo(s * 0.22, s * 0.16);
  ctx.lineTo(s * 0.62, p);
  ctx.lineTo(s - p, s * 0.35);
  ctx.lineTo(s - p, s - p);
  ctx.closePath();
  const g = ctx.createLinearGradient(0, 0, 0, s);
  g.addColorStop(0, '#9a9a96');
  g.addColorStop(1, '#4e4c48');
  ctx.fillStyle = g;
  ctx.fill();
  ctx.strokeStyle = '#33312e';
  ctx.lineWidth = s * 0.04;
  ctx.stroke();
  ctx.fillStyle = 'rgba(255,255,255,0.25)';
  ctx.beginPath();
  ctx.moveTo(s * 0.25, s * 0.22); ctx.lineTo(s * 0.6, s * 0.12); ctx.lineTo(s * 0.45, s * 0.3); ctx.closePath();
  ctx.fill();
}

// ---------- sprite önbelleği ----------
const cache = new Map<string, HTMLCanvasElement>();
let cacheDpr = 1;

function cellKey(cell: Cell, symbols: boolean, variant: number): string {
  switch (cell.k) {
    case 'block': return `b:${cell.el}:${symbols ? 1 : 0}`;
    case 'animal': return `a:${cell.a}`;
    case 'weed': return `w:${Math.min(2, cell.hp)}`;
    case 'ice': return `i:${Math.min(2, cell.hp)}`;
    case 'soil': return `t:${variant % 4}`;
    default: return cell.k;
  }
}

export function clearSpriteCache(dpr: number) {
  cache.clear();
  cacheDpr = dpr;
}

function render(cell: Cell, s: number, symbols: boolean, variant: number, ctx: C2D) {
  switch (cell.k) {
    case 'block':
      bevelTile(ctx, s, EL_COLOR[cell.el]);
      if (symbols) drawSymbol(ctx, cell.el, s / 2, s * 0.48, s);
      break;
    case 'soil': drawSoil(ctx, s, variant % 4 + 1); break;
    case 'rock': drawRock(ctx, s); break;
    case 'animal':
      bevelTile(ctx, s, '#cfe6a8', 0.04);
      drawAnimal(ctx, cell.a, s / 2, s / 2, s * 0.8);
      break;
    case 'seed':
      drawSoil(ctx, s, 9);
      drawSeed(ctx, s / 2, s * 0.55, s * 0.8);
      break;
    case 'weed': drawWeed(ctx, s, cell.hp); break;
    case 'ice': drawIce(ctx, s, cell.hp); break;
  }
}

/** Hücreyi önbellekten çizer (her boyut/tür için bir kez oluşturulur). */
export function drawCell(ctx: C2D, cell: Cell, x: number, y: number, s: number, symbols: boolean, variant = 0) {
  const px = Math.max(4, Math.round(s * cacheDpr));
  const key = `${cellKey(cell, symbols, variant)}@${px}`;
  let spr = cache.get(key);
  if (!spr) {
    spr = document.createElement('canvas');
    spr.width = spr.height = px;
    const c = spr.getContext('2d')!;
    c.scale(px / s, px / s);
    render(cell, s, symbols, variant, c);
    cache.set(key, spr);
  }
  ctx.drawImage(spr, x, y, s, s);
}

/** Küçük HUD/menü simgesi üretir. */
export function iconCanvas(kind: 'animals' | 'seeds' | 'lines' | 'boss' | 'star' | 'score', size = 28): HTMLCanvasElement {
  const dpr = Math.min(3, window.devicePixelRatio || 1);
  const cv = document.createElement('canvas');
  cv.width = cv.height = size * dpr;
  cv.style.width = cv.style.height = size + 'px';
  const ctx = cv.getContext('2d')!;
  ctx.scale(dpr, dpr);
  const s = size;
  if (kind === 'animals') drawAnimal(ctx, 'hedgehog', s / 2, s / 2, s * 0.95);
  else if (kind === 'seeds') drawSeed(ctx, s / 2, s * 0.6, s);
  else if (kind === 'lines') {
    for (let i = 0; i < 3; i++) {
      ctx.save();
      ctx.translate(s * 0.08 + i * s * 0.29, s * 0.35);
      bevelTile(ctx, s * 0.3, ['#2f8cf0', '#43b649', '#ff9322'][i]);
      ctx.restore();
    }
  } else if (kind === 'boss') drawCloud(ctx, s / 2, s / 2, s);
  else if (kind === 'star') drawStar(ctx, s / 2, s / 2, s * 0.45, '#ffcc33');
  return cv;
}

export function drawCloud(ctx: C2D, cx: number, cy: number, s: number) {
  ctx.save();
  ctx.fillStyle = '#5b6576';
  for (const [x, y, r] of [[-0.22, 0.05, 0.2], [0.02, -0.08, 0.26], [0.24, 0.06, 0.19], [0, 0.12, 0.2]]) {
    ctx.beginPath(); ctx.arc(cx + x * s, cy + y * s, r * s, 0, Math.PI * 2); ctx.fill();
  }
  ctx.fillStyle = '#ffd23a';
  ctx.beginPath();
  ctx.moveTo(cx + 0.02 * s, cy + 0.15 * s); ctx.lineTo(cx - 0.1 * s, cy + 0.42 * s); ctx.lineTo(cx + 0.02 * s, cy + 0.38 * s);
  ctx.lineTo(cx - 0.04 * s, cy + 0.6 * s); ctx.lineTo(cx + 0.14 * s, cy + 0.3 * s); ctx.lineTo(cx + 0.02 * s, cy + 0.33 * s);
  ctx.closePath();
  ctx.fill();
  ctx.restore();
}

export function drawStar(ctx: C2D, cx: number, cy: number, r: number, fill: string, stroke = 'rgba(120,70,0,0.6)') {
  ctx.save();
  ctx.beginPath();
  for (let i = 0; i < 10; i++) {
    const a = -Math.PI / 2 + (i * Math.PI) / 5;
    const rr = i % 2 ? r * 0.45 : r;
    ctx.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
  }
  ctx.closePath();
  ctx.fillStyle = fill;
  ctx.fill();
  ctx.lineWidth = Math.max(1, r * 0.12);
  ctx.strokeStyle = stroke;
  ctx.stroke();
  ctx.restore();
}
