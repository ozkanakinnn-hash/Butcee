// "Bahçem" sahnesi: çimen, çiçekler, dolaşan hayvanlar ve dekorlar.
import type { AnimalKind } from '../game/types';
import type { DecorId } from '../storage';
import { drawAnimal, drawFlower, roundRect } from './draw';

type C2D = CanvasRenderingContext2D;

export function drawDecor(ctx: C2D, id: DecorId, cx: number, by: number, s: number) {
  ctx.save();
  ctx.translate(cx, by);
  const u = s / 20;
  ctx.lineCap = 'round';
  switch (id) {
    case 'fence':
      ctx.fillStyle = '#e9d2a6';
      ctx.strokeStyle = '#8a5a35';
      ctx.lineWidth = u * 0.8;
      for (let i = -2; i <= 2; i++) {
        ctx.beginPath();
        ctx.moveTo(i * 4 * u - 1.4 * u, 0); ctx.lineTo(i * 4 * u - 1.4 * u, -10 * u); ctx.lineTo(i * 4 * u, -12 * u);
        ctx.lineTo(i * 4 * u + 1.4 * u, -10 * u); ctx.lineTo(i * 4 * u + 1.4 * u, 0); ctx.closePath();
        ctx.fill(); ctx.stroke();
      }
      ctx.fillRect(-10 * u, -8 * u, 20 * u, 1.6 * u); ctx.strokeRect(-10 * u, -8 * u, 20 * u, 1.6 * u);
      ctx.fillRect(-10 * u, -4 * u, 20 * u, 1.6 * u); ctx.strokeRect(-10 * u, -4 * u, 20 * u, 1.6 * u);
      break;
    case 'fountain':
      ctx.fillStyle = '#b8b2a7';
      ctx.beginPath(); ctx.ellipse(0, -2 * u, 9 * u, 3 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#5db7f0';
      ctx.beginPath(); ctx.ellipse(0, -3 * u, 7.5 * u, 2 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#9d978c';
      ctx.fillRect(-1.2 * u, -11 * u, 2.4 * u, 8 * u);
      ctx.beginPath(); ctx.ellipse(0, -11 * u, 4 * u, 1.4 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.strokeStyle = 'rgba(140,210,255,0.9)';
      ctx.lineWidth = u * 0.7;
      for (const d of [-1, 1]) {
        ctx.beginPath(); ctx.moveTo(0, -12 * u); ctx.quadraticCurveTo(d * 5 * u, -18 * u, d * 6 * u, -4 * u); ctx.stroke();
      }
      break;
    case 'bench':
      ctx.fillStyle = '#8a5a35';
      ctx.fillRect(-8 * u, -4 * u, 1.4 * u, 4 * u); ctx.fillRect(6.6 * u, -4 * u, 1.4 * u, 4 * u);
      ctx.fillStyle = '#c98b4f';
      roundRect(ctx, -10 * u, -6 * u, 20 * u, 2.2 * u, u * 0.6); ctx.fill();
      roundRect(ctx, -10 * u, -11 * u, 20 * u, 2 * u, u * 0.6); ctx.fill();
      ctx.fillStyle = '#8a5a35';
      ctx.fillRect(-8 * u, -11 * u, 1.2 * u, 5 * u); ctx.fillRect(6.8 * u, -11 * u, 1.2 * u, 5 * u);
      break;
    case 'lantern':
      ctx.fillStyle = '#3b3b3b';
      ctx.fillRect(-0.7 * u, -16 * u, 1.4 * u, 16 * u);
      ctx.fillRect(-3 * u, -0.8 * u, 6 * u, 0.8 * u);
      ctx.fillStyle = 'rgba(255,215,90,0.35)';
      ctx.beginPath(); ctx.arc(0, -18 * u, 6 * u, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#ffe58a';
      roundRect(ctx, -2.4 * u, -21 * u, 4.8 * u, 5.6 * u, u); ctx.fill();
      ctx.strokeStyle = '#3b3b3b'; ctx.lineWidth = u * 0.6; ctx.stroke();
      ctx.fillStyle = '#3b3b3b';
      ctx.beginPath(); ctx.moveTo(-3.4 * u, -21 * u); ctx.lineTo(0, -23.5 * u); ctx.lineTo(3.4 * u, -21 * u); ctx.fill();
      break;
    case 'birdhouse':
      ctx.fillStyle = '#7a5230';
      ctx.fillRect(-0.8 * u, -10 * u, 1.6 * u, 10 * u);
      ctx.fillStyle = '#e3a857';
      ctx.fillRect(-4 * u, -17 * u, 8 * u, 7 * u);
      ctx.fillStyle = '#b3462f';
      ctx.beginPath(); ctx.moveTo(-5.5 * u, -16.5 * u); ctx.lineTo(0, -21.5 * u); ctx.lineTo(5.5 * u, -16.5 * u); ctx.closePath(); ctx.fill();
      ctx.fillStyle = '#3a2414';
      ctx.beginPath(); ctx.arc(0, -14 * u, 1.4 * u, 0, Math.PI * 2); ctx.fill();
      // kuş
      ctx.fillStyle = '#4fa3e0';
      ctx.beginPath(); ctx.ellipse(4.5 * u, -22.5 * u, 1.8 * u, 1.3 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#ffb02e';
      ctx.beginPath(); ctx.moveTo(6.2 * u, -22.8 * u); ctx.lineTo(7.3 * u, -22.4 * u); ctx.lineTo(6.2 * u, -22 * u); ctx.fill();
      break;
  }
  ctx.restore();
}

export interface GardenData {
  animals: Record<AnimalKind, number>;
  sprouts: number;
  slots: (DecorId | null)[];
}

/** Dekor yuvalarının (sahne oranına göre) konumları. */
export const SLOT_POS = [
  { x: 0.18, y: 0.56 }, { x: 0.82, y: 0.54 }, { x: 0.5, y: 0.62 }, { x: 0.24, y: 0.83 }, { x: 0.76, y: 0.84 },
];

function rnd(i: number) {
  const x = Math.sin(i * 127.1 + 311.7) * 43758.5453;
  return x - Math.floor(x);
}

export function drawGarden(ctx: C2D, w: number, h: number, t: number, d: GardenData, highlightSlots: boolean) {
  // gökyüzü
  const sky = ctx.createLinearGradient(0, 0, 0, h * 0.45);
  sky.addColorStop(0, '#8fd0f5');
  sky.addColorStop(1, '#dff3ff');
  ctx.fillStyle = sky;
  ctx.fillRect(0, 0, w, h);
  // güneş + bulutlar
  ctx.fillStyle = 'rgba(255,230,120,0.9)';
  ctx.beginPath(); ctx.arc(w * 0.82, h * 0.13, Math.min(w, h) * 0.06, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = 'rgba(255,255,255,0.85)';
  for (let i = 0; i < 3; i++) {
    const x = ((t * 8 + i * w * 0.4) % (w + 160)) - 80, y = h * (0.12 + i * 0.07);
    for (const [dx, r] of [[0, 18], [20, 24], [44, 17]]) { ctx.beginPath(); ctx.arc(x + dx, y, r, 0, Math.PI * 2); ctx.fill(); }
  }
  // tepeler
  ctx.fillStyle = '#8fcf6a';
  ctx.beginPath(); ctx.moveTo(0, h * 0.42);
  ctx.quadraticCurveTo(w * 0.3, h * 0.33, w * 0.6, h * 0.41); ctx.quadraticCurveTo(w * 0.85, h * 0.47, w, h * 0.38);
  ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.fill();
  const grass = ctx.createLinearGradient(0, h * 0.45, 0, h);
  grass.addColorStop(0, '#6db84a');
  grass.addColorStop(1, '#3f8a2e');
  ctx.fillStyle = grass;
  ctx.beginPath(); ctx.moveTo(0, h * 0.48); ctx.quadraticCurveTo(w * 0.5, h * 0.43, w, h * 0.49); ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.fill();
  // patika
  ctx.fillStyle = '#d9b98a';
  ctx.beginPath(); ctx.moveTo(w * 0.44, h * 0.47); ctx.quadraticCurveTo(w * 0.3, h * 0.75, w * 0.38, h);
  ctx.lineTo(w * 0.62, h); ctx.quadraticCurveTo(w * 0.62, h * 0.72, w * 0.52, h * 0.47); ctx.fill();
  // çimen tutamları
  ctx.strokeStyle = 'rgba(30,90,20,0.5)';
  ctx.lineWidth = 1.5;
  for (let i = 0; i < 60; i++) {
    const x = rnd(i) * w, y = h * (0.5 + rnd(i + 99) * 0.5);
    ctx.beginPath(); ctx.moveTo(x, y); ctx.lineTo(x - 3, y - 7); ctx.moveTo(x, y); ctx.lineTo(x + 3, y - 8); ctx.stroke();
  }

  type Item = { y: number; draw: () => void };
  const items: Item[] = [];
  const unit = Math.min(w, h * 0.6);
  // çiçekler (filizlenen tohumlar)
  const colors = ['#ff8fb1', '#fff1a8', '#c79bff', '#ffb36b', '#ffffff'];
  const nFlowers = Math.min(60, d.sprouts);
  for (let i = 0; i < nFlowers; i++) {
    const x = rnd(i * 3 + 1) * w * 0.92 + w * 0.04, y = h * (0.5 + rnd(i * 3 + 2) * 0.46);
    const sway = Math.sin(t * 1.5 + i) * 0.06;
    items.push({ y, draw: () => { ctx.save(); ctx.translate(x, y); ctx.rotate(sway); drawFlower(ctx, 0, -unit * 0.04, unit * 0.06, colors[i % colors.length]); ctx.restore(); } });
  }
  // dekorlar ve yuvalar
  SLOT_POS.forEach((p, i) => {
    const x = p.x * w, y = p.y * h, id = d.slots[i];
    items.push({
      y, draw: () => {
        if (id) drawDecor(ctx, id, x, y, unit * 0.2);
        else if (highlightSlots) {
          ctx.save();
          ctx.setLineDash([6, 5]);
          ctx.strokeStyle = 'rgba(255,255,255,0.8)';
          ctx.lineWidth = 2;
          ctx.beginPath(); ctx.ellipse(x, y - 4, unit * 0.08, unit * 0.035, 0, 0, Math.PI * 2); ctx.stroke();
          ctx.fillStyle = 'rgba(255,255,255,0.9)';
          ctx.font = `800 ${unit * 0.06}px system-ui`;
          ctx.textAlign = 'center';
          ctx.fillText('+', x, y + unit * 0.012);
          ctx.restore();
        }
      },
    });
  });
  // hayvanlar
  const kinds: AnimalKind[] = ['hedgehog', 'turtle', 'ladybug'];
  let idx = 0;
  for (const k of kinds) {
    const n = Math.min(6, d.animals[k]);
    for (let i = 0; i < n; i++, idx++) {
      const speed = k === 'turtle' ? 0.05 : k === 'hedgehog' ? 0.09 : 0.14;
      const ph = rnd(idx * 7 + 3) * Math.PI * 2;
      const baseY = h * (0.55 + rnd(idx * 5 + 1) * 0.38);
      const x = w * 0.5 + Math.sin(t * speed + ph) * w * 0.38;
      const dir = Math.cos(t * speed + ph) >= 0 ? 1 : -1;
      const hop = k === 'ladybug' ? Math.abs(Math.sin(t * 4 + ph)) * unit * 0.02 : 0;
      items.push({
        y: baseY, draw: () => {
          ctx.save();
          ctx.fillStyle = 'rgba(0,0,0,0.18)';
          ctx.beginPath(); ctx.ellipse(x, baseY, unit * 0.04, unit * 0.012, 0, 0, Math.PI * 2); ctx.fill();
          ctx.translate(x, baseY - unit * 0.035 - hop);
          ctx.scale(dir, 1);
          drawAnimal(ctx, k, 0, 0, unit * 0.09, t);
          ctx.restore();
        },
      });
    }
  }
  items.sort((a, b) => a.y - b.y).forEach((it) => it.draw());
}
