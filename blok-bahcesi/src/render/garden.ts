// "Bahçem" sahnesi: ev (5 seviye), 12 dekor, 8 yuva, çiçekler, dolaşan hayvanlar ve Bahçıvan Dede.
import type { AnimalKind } from '../game/types';
import type { DecorId, GardenSnapshot } from '../economy';
import { drawAnimal, drawFlower, roundRect } from './draw';
import { drawGardener } from './characters';

type C2D = CanvasRenderingContext2D;

function rnd(i: number) {
  const x = Math.sin(i * 127.1 + 311.7) * 43758.5453;
  return x - Math.floor(x);
}

// ---------------- dekorlar ----------------
export function drawDecor(ctx: C2D, id: DecorId, cx: number, by: number, s: number, t = 0) {
  ctx.save();
  ctx.translate(cx, by);
  const u = s / 20;
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';
  // zemin gölgesi
  ctx.fillStyle = 'rgba(0,0,0,0.18)';
  ctx.beginPath(); ctx.ellipse(0, 0, 9 * u, 2 * u, 0, 0, Math.PI * 2); ctx.fill();
  switch (id) {
    case 'fence':
      ctx.fillStyle = '#f3e1bd'; ctx.strokeStyle = '#8a5a35'; ctx.lineWidth = u * 0.7;
      for (let i = -2; i <= 2; i++) {
        ctx.beginPath();
        ctx.moveTo(i * 4 * u - 1.4 * u, 0); ctx.lineTo(i * 4 * u - 1.4 * u, -10 * u); ctx.lineTo(i * 4 * u, -12 * u);
        ctx.lineTo(i * 4 * u + 1.4 * u, -10 * u); ctx.lineTo(i * 4 * u + 1.4 * u, 0); ctx.closePath(); ctx.fill(); ctx.stroke();
      }
      for (const y of [-8, -4]) { ctx.fillRect(-10 * u, y * u, 20 * u, 1.6 * u); ctx.strokeRect(-10 * u, y * u, 20 * u, 1.6 * u); }
      break;
    case 'flowerbed': {
      ctx.fillStyle = '#7a4a2a';
      roundRect(ctx, -9 * u, -3.5 * u, 18 * u, 4 * u, 1.5 * u); ctx.fill();
      ctx.fillStyle = '#5c3820';
      roundRect(ctx, -8 * u, -3.5 * u, 16 * u, 1.6 * u, 0.8 * u); ctx.fill();
      const cols = ['#ff6b8b', '#ffd54a', '#c79bff', '#ff9f43', '#ffffff'];
      for (let i = 0; i < 7; i++) {
        const sway = Math.sin(t * 1.6 + i) * 0.4 * u;
        drawFlower(ctx, (-7 + i * 2.3) * u + sway, (-7 - (i % 2) * 1.5) * u, 4.5 * u, cols[i % cols.length]);
      }
      break;
    }
    case 'bench':
      ctx.fillStyle = '#6b4329';
      ctx.fillRect(-8 * u, -4 * u, 1.4 * u, 4 * u); ctx.fillRect(6.6 * u, -4 * u, 1.4 * u, 4 * u);
      ctx.fillStyle = '#c98b4f';
      roundRect(ctx, -10 * u, -6 * u, 20 * u, 2.2 * u, u * 0.6); ctx.fill();
      roundRect(ctx, -10 * u, -11 * u, 20 * u, 2 * u, u * 0.6); ctx.fill();
      roundRect(ctx, -10 * u, -13.5 * u, 20 * u, 1.8 * u, u * 0.6); ctx.fill();
      ctx.fillStyle = '#6b4329';
      ctx.fillRect(-8 * u, -13.5 * u, 1.2 * u, 7.5 * u); ctx.fillRect(6.8 * u, -13.5 * u, 1.2 * u, 7.5 * u);
      break;
    case 'lantern': {
      const glow = 0.3 + 0.1 * Math.sin(t * 3);
      ctx.fillStyle = `rgba(255,215,90,${glow})`;
      ctx.beginPath(); ctx.arc(0, -19 * u, 8 * u, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#3b3b3b';
      ctx.fillRect(-0.7 * u, -16 * u, 1.4 * u, 16 * u);
      ctx.fillRect(-3 * u, -0.8 * u, 6 * u, 0.8 * u);
      ctx.fillStyle = '#ffe58a';
      roundRect(ctx, -2.4 * u, -21.5 * u, 4.8 * u, 5.6 * u, u); ctx.fill();
      ctx.strokeStyle = '#3b3b3b'; ctx.lineWidth = u * 0.6; ctx.stroke();
      ctx.fillStyle = '#3b3b3b';
      ctx.beginPath(); ctx.moveTo(-3.4 * u, -21.5 * u); ctx.lineTo(0, -24 * u); ctx.lineTo(3.4 * u, -21.5 * u); ctx.fill();
      break;
    }
    case 'birdhouse': {
      ctx.fillStyle = '#7a5230';
      ctx.fillRect(-0.8 * u, -12 * u, 1.6 * u, 12 * u);
      ctx.fillStyle = '#e3a857';
      ctx.fillRect(-4 * u, -19 * u, 8 * u, 7 * u);
      ctx.fillStyle = '#b3462f';
      ctx.beginPath(); ctx.moveTo(-5.5 * u, -18.5 * u); ctx.lineTo(0, -23.5 * u); ctx.lineTo(5.5 * u, -18.5 * u); ctx.closePath(); ctx.fill();
      ctx.fillStyle = '#3a2414';
      ctx.beginPath(); ctx.arc(0, -16 * u, 1.4 * u, 0, Math.PI * 2); ctx.fill();
      const hop = Math.abs(Math.sin(t * 3)) * u;
      ctx.fillStyle = '#4fa3e0';
      ctx.beginPath(); ctx.ellipse(4.5 * u, -24.5 * u - hop, 1.8 * u, 1.3 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#ffb02e';
      ctx.beginPath(); ctx.moveTo(6.2 * u, -24.8 * u - hop); ctx.lineTo(7.3 * u, -24.4 * u - hop); ctx.lineTo(6.2 * u, -24 * u - hop); ctx.fill();
      break;
    }
    case 'scarecrow': {
      const sw = Math.sin(t * 1.2) * 0.05;
      ctx.rotate(sw);
      ctx.fillStyle = '#7a5230';
      ctx.fillRect(-0.7 * u, -20 * u, 1.4 * u, 20 * u);
      ctx.fillRect(-8 * u, -15 * u, 16 * u, 1.3 * u);
      ctx.fillStyle = '#c0392b';
      roundRect(ctx, -4 * u, -16 * u, 8 * u, 8 * u, 1.5 * u); ctx.fill();
      ctx.fillStyle = '#f1c40f';
      for (const x of [-8.5, 7]) { ctx.beginPath(); ctx.moveTo(x * u, -15 * u); ctx.lineTo((x + 1.5) * u, -12.5 * u); ctx.lineTo((x - 0.5) * u, -12.5 * u); ctx.fill(); }
      ctx.fillStyle = '#f5deb3';
      ctx.beginPath(); ctx.arc(0, -19.5 * u, 3.2 * u, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#333';
      ctx.fillRect(-1.6 * u, -20.3 * u, 0.9 * u, 0.9 * u); ctx.fillRect(0.7 * u, -20.3 * u, 0.9 * u, 0.9 * u);
      ctx.fillStyle = '#8d6e3f';
      ctx.beginPath(); ctx.ellipse(0, -22.3 * u, 5 * u, 1.2 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillRect(-2.5 * u, -25 * u, 5 * u, 2.8 * u);
      break;
    }
    case 'tree': {
      ctx.fillStyle = '#7a4a2a';
      ctx.beginPath(); ctx.moveTo(-1.6 * u, 0); ctx.lineTo(-1 * u, -12 * u); ctx.lineTo(1 * u, -12 * u); ctx.lineTo(1.6 * u, 0); ctx.fill();
      const sway = Math.sin(t * 1.1) * 0.4 * u;
      const greens = ['#3f8f2f', '#4fae3c', '#62c24c'];
      for (const [x, y, r, k] of [[-4, -15, 5.5, 0], [4, -15, 5.5, 0], [0, -20, 6.5, 1], [-2, -16, 4.5, 2], [2.5, -18.5, 4, 2]]) {
        ctx.fillStyle = greens[k]; ctx.beginPath(); ctx.arc(x * u + sway, y * u, r * u, 0, Math.PI * 2); ctx.fill();
      }
      ctx.fillStyle = '#e53935';
      for (const [x, y] of [[-4, -14], [3, -19], [1, -14], [-1.5, -21]]) { ctx.beginPath(); ctx.arc(x * u + sway, y * u, 0.9 * u, 0, Math.PI * 2); ctx.fill(); }
      break;
    }
    case 'beehive': {
      ctx.fillStyle = '#7a5230';
      ctx.fillRect(-0.6 * u, -6 * u, 1.2 * u, 6 * u);
      ctx.fillStyle = '#f2b632';
      ctx.strokeStyle = '#b07800'; ctx.lineWidth = 0.5 * u;
      for (let i = 0; i < 4; i++) {
        const w = 7 - Math.abs(i - 1.2) * 1.4;
        roundRect(ctx, -w * u, (-8 - i * 2.6) * u, 2 * w * u, 2.8 * u, 1.3 * u); ctx.fill(); ctx.stroke();
      }
      ctx.fillStyle = '#3a2414'; ctx.beginPath(); ctx.arc(0, -9 * u, 1 * u, 0, Math.PI * 2); ctx.fill();
      for (let i = 0; i < 3; i++) {
        const a = t * 2 + i * 2.1;
        const x = Math.cos(a) * 7 * u, y = -14 * u + Math.sin(a * 1.7) * 3 * u;
        ctx.fillStyle = '#ffd54a'; ctx.beginPath(); ctx.ellipse(x, y, 0.9 * u, 0.6 * u, 0, 0, Math.PI * 2); ctx.fill();
        ctx.fillStyle = '#333'; ctx.fillRect(x - 0.1 * u, y - 0.6 * u, 0.3 * u, 1.2 * u);
      }
      break;
    }
    case 'swing': {
      ctx.strokeStyle = '#6b4329'; ctx.lineWidth = 1.4 * u;
      ctx.beginPath(); ctx.moveTo(-8 * u, 0); ctx.lineTo(-6 * u, -18 * u); ctx.lineTo(6 * u, -18 * u); ctx.lineTo(8 * u, 0); ctx.stroke();
      const a = Math.sin(t * 2) * 0.25;
      ctx.save(); ctx.translate(0, -18 * u); ctx.rotate(a);
      ctx.strokeStyle = '#d7b98a'; ctx.lineWidth = 0.4 * u;
      ctx.beginPath(); ctx.moveTo(-3 * u, 0); ctx.lineTo(-3 * u, 13 * u); ctx.moveTo(3 * u, 0); ctx.lineTo(3 * u, 13 * u); ctx.stroke();
      ctx.fillStyle = '#e74c3c'; roundRect(ctx, -4 * u, 12.5 * u, 8 * u, 1.4 * u, 0.5 * u); ctx.fill();
      ctx.restore();
      break;
    }
    case 'pond': {
      ctx.fillStyle = '#9a8f80';
      ctx.beginPath(); ctx.ellipse(0, -2 * u, 10 * u, 4 * u, 0, 0, Math.PI * 2); ctx.fill();
      const g = ctx.createLinearGradient(0, -6 * u, 0, 2 * u);
      g.addColorStop(0, '#7fd3f7'); g.addColorStop(1, '#2f8cc9');
      ctx.fillStyle = g;
      ctx.beginPath(); ctx.ellipse(0, -2.2 * u, 8.6 * u, 3.1 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.strokeStyle = 'rgba(255,255,255,0.6)'; ctx.lineWidth = 0.4 * u;
      const rr = (t * 2) % 3;
      ctx.beginPath(); ctx.ellipse(-2 * u, -2.4 * u, (1 + rr) * u, (0.4 + rr * 0.3) * u, 0, 0, Math.PI * 2); ctx.stroke();
      ctx.fillStyle = '#4caf50'; ctx.beginPath(); ctx.ellipse(4 * u, -2 * u, 1.8 * u, 0.8 * u, 0, 0.3, Math.PI * 2 - 0.3); ctx.fill();
      ctx.fillStyle = '#ff8fb1'; ctx.beginPath(); ctx.arc(4 * u, -2.6 * u, 0.7 * u, 0, Math.PI * 2); ctx.fill();
      break;
    }
    case 'fountain': {
      ctx.fillStyle = '#b8b2a7';
      ctx.beginPath(); ctx.ellipse(0, -2 * u, 9 * u, 3 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#5db7f0';
      ctx.beginPath(); ctx.ellipse(0, -3 * u, 7.5 * u, 2 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#9d978c';
      ctx.fillRect(-1.2 * u, -11 * u, 2.4 * u, 8 * u);
      ctx.beginPath(); ctx.ellipse(0, -11 * u, 4 * u, 1.4 * u, 0, 0, Math.PI * 2); ctx.fill();
      ctx.strokeStyle = 'rgba(140,210,255,0.9)'; ctx.lineWidth = u * 0.7;
      const k = Math.sin(t * 4) * 0.6;
      for (const d of [-1, 1]) { ctx.beginPath(); ctx.moveTo(0, -12 * u); ctx.quadraticCurveTo(d * (5 + k) * u, -19 * u, d * 6.5 * u, -4 * u); ctx.stroke(); }
      break;
    }
    case 'gazebo': {
      ctx.fillStyle = '#efe3c8';
      for (const x of [-8, -3, 3, 8]) ctx.fillRect((x - 0.6) * u, -14 * u, 1.2 * u, 14 * u);
      ctx.fillStyle = '#d9c7a0'; ctx.fillRect(-9.5 * u, -2 * u, 19 * u, 2 * u);
      ctx.fillStyle = '#c0392b';
      ctx.beginPath(); ctx.moveTo(-11 * u, -14 * u); ctx.lineTo(0, -22 * u); ctx.lineTo(11 * u, -14 * u); ctx.closePath(); ctx.fill();
      ctx.fillStyle = '#a93226'; ctx.fillRect(-11 * u, -14.5 * u, 22 * u, 1.2 * u);
      ctx.strokeStyle = '#5fb84a'; ctx.lineWidth = 0.7 * u;
      ctx.beginPath(); ctx.moveTo(-8 * u, -13 * u); for (let i = 0; i <= 16; i++) ctx.lineTo((-8 + i) * u, (-13 + Math.sin(i) * 0.8) * u); ctx.stroke();
      ctx.fillStyle = '#ffd54a'; ctx.beginPath(); ctx.arc(0, -22.5 * u, 0.9 * u, 0, Math.PI * 2); ctx.fill();
      break;
    }
  }
  ctx.restore();
}

// ---------------- ev ----------------
interface HouseStyle { w: number; h: number; wall: string; wallD: string; roof: string; roofD: string; stone?: boolean; floors: number; tower?: boolean; porch?: boolean; patched?: boolean }
const HOUSES: HouseStyle[] = [
  { w: 22, h: 12, wall: '#a77b4f', wallD: '#7a5432', roof: '#8a6a45', roofD: '#5e4428', floors: 1, patched: true },
  { w: 26, h: 14, wall: '#c98b4f', wallD: '#8a5a35', roof: '#c0392b', roofD: '#922b21', floors: 1 },
  { w: 30, h: 15, wall: '#b9b3a6', wallD: '#86807a', roof: '#3b6ea5', roofD: '#2a5180', stone: true, floors: 1 },
  { w: 34, h: 22, wall: '#f4e3c3', wallD: '#c9b28a', roof: '#c4553a', roofD: '#963d27', floors: 2, porch: true },
  { w: 38, h: 24, wall: '#fbe7ef', wallD: '#d6b7c4', roof: '#6c5ce7', roofD: '#4b3fbf', floors: 2, porch: true, tower: true },
];

function windowAt(ctx: C2D, x: number, y: number, w: number, h: number, u: number, lit: boolean) {
  ctx.fillStyle = '#6b4329';
  roundRect(ctx, x - 0.5 * u, y - 0.5 * u, w + u, h + u, 0.6 * u); ctx.fill();
  ctx.fillStyle = lit ? '#ffe9a0' : '#a9d8f5';
  ctx.fillRect(x, y, w, h);
  ctx.strokeStyle = '#6b4329'; ctx.lineWidth = 0.45 * u;
  ctx.beginPath(); ctx.moveTo(x + w / 2, y); ctx.lineTo(x + w / 2, y + h); ctx.moveTo(x, y + h / 2); ctx.lineTo(x + w, y + h / 2); ctx.stroke();
  ctx.fillStyle = '#5fb84a';
  roundRect(ctx, x - 0.6 * u, y + h + 0.4 * u, w + 1.2 * u, 1.4 * u, 0.5 * u); ctx.fill();
  ctx.fillStyle = '#ff6b8b';
  for (let i = 0; i < 3; i++) { ctx.beginPath(); ctx.arc(x + (i + 0.5) * (w / 3), y + h + 0.4 * u, 0.55 * u, 0, Math.PI * 2); ctx.fill(); }
}

/** (cx, by) evin taban ortası; s ölçek (≈ 20 birim). */
export function drawHouse(ctx: C2D, level: number, cx: number, by: number, s: number, t = 0) {
  const st = HOUSES[Math.max(0, Math.min(HOUSES.length - 1, level))];
  const u = s / 20;
  ctx.save();
  ctx.translate(cx, by);
  const W = st.w * u, H = st.h * u;
  ctx.fillStyle = 'rgba(0,0,0,0.2)';
  ctx.beginPath(); ctx.ellipse(0, 0, W * 0.62, 2.4 * u, 0, 0, Math.PI * 2); ctx.fill();
  // kule
  if (st.tower) {
    const tx = W / 2 - 3 * u;
    ctx.fillStyle = st.wallD; ctx.fillRect(tx - 4 * u, -H - 12 * u, 8 * u, H + 12 * u);
    ctx.fillStyle = st.wall; ctx.fillRect(tx - 3.5 * u, -H - 12 * u, 7 * u, H + 12 * u);
    ctx.fillStyle = st.roof;
    ctx.beginPath(); ctx.moveTo(tx - 5 * u, -H - 12 * u); ctx.lineTo(tx, -H - 22 * u); ctx.lineTo(tx + 5 * u, -H - 12 * u); ctx.closePath(); ctx.fill();
    ctx.strokeStyle = '#555'; ctx.lineWidth = 0.4 * u;
    ctx.beginPath(); ctx.moveTo(tx, -H - 22 * u); ctx.lineTo(tx, -H - 26 * u); ctx.stroke();
    ctx.fillStyle = '#ff6b8b';
    ctx.beginPath(); ctx.moveTo(tx, -H - 26 * u); ctx.lineTo(tx + 3.5 * u + Math.sin(t * 4) * 0.5 * u, -H - 25 * u); ctx.lineTo(tx, -H - 24 * u); ctx.fill();
    windowAt(ctx, tx - 1.6 * u, -H - 9 * u, 3.2 * u, 3.6 * u, u, true);
  }
  // baca + duman
  if (level >= 1) {
    const chx = -W * 0.28;
    ctx.fillStyle = st.stone ? '#8a847c' : '#9b4a35';
    ctx.fillRect(chx - 1.8 * u, -H - 9 * u, 3.6 * u, 7 * u);
    for (let i = 0; i < 3; i++) {
      const k = ((t * 0.5 + i / 3) % 1);
      ctx.fillStyle = `rgba(240,240,240,${0.6 * (1 - k)})`;
      ctx.beginPath(); ctx.arc(chx + Math.sin(k * 6 + i) * 1.5 * u, -H - 10 * u - k * 9 * u, (1.2 + k * 2) * u, 0, Math.PI * 2); ctx.fill();
    }
  }
  // duvar
  ctx.fillStyle = st.wallD;
  ctx.fillRect(-W / 2, -H, W, H);
  ctx.fillStyle = st.wall;
  ctx.fillRect(-W / 2 + 0.6 * u, -H, W - 1.2 * u, H - 0.6 * u);
  if (st.stone) {
    ctx.save();
    ctx.beginPath(); ctx.rect(-W / 2, -H, W, H); ctx.clip();
    ctx.strokeStyle = 'rgba(80,70,60,0.35)'; ctx.lineWidth = 0.35 * u;
    for (let r = 0; r < st.h / 2.5; r++) for (let c = -1; c < st.w / 4; c++) {
      ctx.strokeRect(-W / 2 + (c * 4 + (r % 2) * 2) * u, -H + r * 2.5 * u, 4 * u, 2.5 * u);
    }
    ctx.restore();
  } else if (level <= 1) {
    ctx.strokeStyle = 'rgba(60,30,10,0.3)'; ctx.lineWidth = 0.35 * u;
    for (let y = 2; y < st.h; y += 2) { ctx.beginPath(); ctx.moveTo(-W / 2, -y * u); ctx.lineTo(W / 2, -y * u); ctx.stroke(); }
  }
  if (st.patched) {
    ctx.fillStyle = '#8f6a42'; ctx.fillRect(W * 0.18, -H * 0.55, 3 * u, 2.4 * u);
    ctx.strokeStyle = '#5e4428'; ctx.lineWidth = 0.3 * u; ctx.strokeRect(W * 0.18, -H * 0.55, 3 * u, 2.4 * u);
  }
  if (st.floors === 2) { ctx.fillStyle = st.wallD; ctx.fillRect(-W / 2, -H / 2 - 0.4 * u, W, 0.8 * u); }
  // pencereler
  const lit = level >= 3;
  const winY = st.floors === 2 ? [-H + 3 * u, -H / 2 + 2.5 * u] : [-H + 3 * u];
  for (const y of winY) {
    const n = level >= 2 ? 2 : 1;
    for (let i = 0; i < n; i++) {
      const x = n === 1 ? W * 0.18 - 2 * u : (i === 0 ? -W / 2 + 3 * u : W / 2 - 7 * u);
      if (y === winY[winY.length - 1] && Math.abs(x) < 4 * u) continue;
      windowAt(ctx, x, y, 4 * u, 4 * u, u, lit);
    }
    if (st.floors === 2 && y === winY[0]) windowAt(ctx, -2 * u, y, 4 * u, 4 * u, u, lit);
  }
  // kapı
  const dw = 5 * u, dh = Math.min(H - 1 * u, 8 * u);
  ctx.fillStyle = '#6b3f22';
  roundRect(ctx, -dw / 2, -dh, dw, dh, dw * 0.45); ctx.fill();
  ctx.fillStyle = '#8a5a35';
  roundRect(ctx, -dw / 2 + 0.6 * u, -dh + 0.6 * u, dw - 1.2 * u, dh - 0.6 * u, dw * 0.4); ctx.fill();
  ctx.fillStyle = '#ffd54a'; ctx.beginPath(); ctx.arc(dw * 0.25, -dh * 0.45, 0.45 * u, 0, Math.PI * 2); ctx.fill();
  // veranda
  if (st.porch) {
    ctx.fillStyle = '#d9c7a0'; ctx.fillRect(-W * 0.35, -1.2 * u, W * 0.7, 1.2 * u);
    ctx.fillStyle = st.roofD;
    ctx.beginPath(); ctx.moveTo(-W * 0.36, -dh - 1.5 * u); ctx.lineTo(W * 0.36, -dh - 1.5 * u); ctx.lineTo(W * 0.3, -dh - 4 * u); ctx.lineTo(-W * 0.3, -dh - 4 * u); ctx.fill();
    ctx.fillStyle = '#efe3c8';
    for (const x of [-W * 0.33, W * 0.33 - u]) ctx.fillRect(x, -dh - 1.5 * u, u, dh + 0.3 * u);
  }
  // çatı
  const rh = (8 + level * 1.5) * u;
  ctx.fillStyle = st.roofD;
  ctx.beginPath(); ctx.moveTo(-W / 2 - 2.5 * u, -H + 0.5 * u); ctx.lineTo(0, -H - rh); ctx.lineTo(W / 2 + 2.5 * u, -H + 0.5 * u); ctx.closePath(); ctx.fill();
  ctx.fillStyle = st.roof;
  ctx.beginPath(); ctx.moveTo(-W / 2 - 1.5 * u, -H - 0.5 * u); ctx.lineTo(0, -H - rh + 0.6 * u); ctx.lineTo(W / 2 + 1.5 * u, -H - 0.5 * u); ctx.closePath(); ctx.fill();
  ctx.strokeStyle = 'rgba(0,0,0,0.15)'; ctx.lineWidth = 0.35 * u;
  for (let i = 1; i < 5; i++) {
    const k = i / 5;
    ctx.beginPath(); ctx.moveTo(-(W / 2 + 1.5 * u) * (1 - k), -H - 0.5 * u - (rh - 1) * k); ctx.lineTo((W / 2 + 1.5 * u) * (1 - k), -H - 0.5 * u - (rh - 1) * k); ctx.stroke();
  }
  if (level >= 2) { // çatı penceresi
    ctx.fillStyle = '#fff3c4'; ctx.beginPath(); ctx.arc(0, -H - rh * 0.45, 1.6 * u, 0, Math.PI * 2); ctx.fill();
    ctx.strokeStyle = st.roofD; ctx.lineWidth = 0.5 * u; ctx.stroke();
  }
  ctx.restore();
}

// ---------------- sahne ----------------
/** Yuvaların sahne oranına göre konumları (ev etrafında ön bahçe). */
export const SLOT_POS = [
  { x: 0.13, y: 0.6 }, { x: 0.87, y: 0.6 }, { x: 0.3, y: 0.67 }, { x: 0.7, y: 0.67 },
  { x: 0.16, y: 0.8 }, { x: 0.84, y: 0.8 }, { x: 0.33, y: 0.9 }, { x: 0.67, y: 0.9 },
];
export const HOUSE_POS = { x: 0.5, y: 0.54 };

export function sceneUnit(w: number, h: number) { return Math.min(w, h * 0.62); }

export function drawGarden(ctx: C2D, w: number, h: number, t: number, d: GardenSnapshot, opts: { slotHints?: boolean; selected?: number | null; gardener?: boolean } = {}) {
  const unit = sceneUnit(w, h);
  // gökyüzü
  const sky = ctx.createLinearGradient(0, 0, 0, h * 0.5);
  sky.addColorStop(0, '#6ec3f2');
  sky.addColorStop(1, '#e3f5ff');
  ctx.fillStyle = sky;
  ctx.fillRect(0, 0, w, h);
  // güneş ışınları
  const sx = w * 0.84, sy = h * 0.12;
  ctx.save();
  ctx.translate(sx, sy);
  ctx.rotate(t * 0.05);
  ctx.fillStyle = 'rgba(255,240,170,0.35)';
  for (let i = 0; i < 12; i++) { ctx.rotate(Math.PI / 6); ctx.beginPath(); ctx.moveTo(0, 0); ctx.lineTo(unit * 0.5, -unit * 0.03); ctx.lineTo(unit * 0.5, unit * 0.03); ctx.fill(); }
  ctx.restore();
  ctx.fillStyle = '#ffe58a';
  ctx.beginPath(); ctx.arc(sx, sy, unit * 0.065, 0, Math.PI * 2); ctx.fill();
  // bulutlar
  ctx.fillStyle = 'rgba(255,255,255,0.92)';
  for (let i = 0; i < 4; i++) {
    const x = ((t * (6 + i * 2) + i * w * 0.33) % (w + 200)) - 100, y = h * (0.07 + i * 0.055);
    const k = 0.7 + rnd(i) * 0.6;
    for (const [dx, r] of [[0, 16], [20, 22], [44, 15], [24, -0]]) if (r) { ctx.beginPath(); ctx.arc(x + dx * k, y, r * k, 0, Math.PI * 2); ctx.fill(); }
  }
  // uzak tepeler
  ctx.fillStyle = '#a6d98a';
  ctx.beginPath(); ctx.moveTo(0, h * 0.4);
  ctx.quadraticCurveTo(w * 0.25, h * 0.3, w * 0.5, h * 0.38); ctx.quadraticCurveTo(w * 0.8, h * 0.45, w, h * 0.34);
  ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.fill();
  // uzak ağaçlar
  for (let i = 0; i < 9; i++) {
    const x = rnd(i + 40) * w, y = h * (0.38 + rnd(i + 50) * 0.04), r = unit * (0.025 + rnd(i + 60) * 0.02);
    ctx.fillStyle = '#5e9f45'; ctx.beginPath(); ctx.arc(x, y - r, r, 0, Math.PI * 2); ctx.fill();
  }
  // çimen
  const grass = ctx.createLinearGradient(0, h * 0.42, 0, h);
  grass.addColorStop(0, '#79c454');
  grass.addColorStop(1, '#3f8a2e');
  ctx.fillStyle = grass;
  ctx.beginPath(); ctx.moveTo(0, h * 0.45); ctx.quadraticCurveTo(w * 0.5, h * 0.41, w, h * 0.46); ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.fill();
  // çim desenleri (biçilmiş şeritler)
  ctx.fillStyle = 'rgba(255,255,255,0.05)';
  for (let i = 0; i < 6; i++) ctx.fillRect(0, h * (0.5 + i * 0.09), w, h * 0.045);
  // patika (taş döşeli)
  const hx = HOUSE_POS.x * w, hy = HOUSE_POS.y * h;
  ctx.fillStyle = '#e2c79a';
  ctx.beginPath(); ctx.moveTo(hx - unit * 0.05, hy); ctx.quadraticCurveTo(hx - unit * 0.14, h * 0.8, hx - unit * 0.12, h);
  ctx.lineTo(hx + unit * 0.12, h); ctx.quadraticCurveTo(hx + unit * 0.14, h * 0.8, hx + unit * 0.05, hy); ctx.fill();
  ctx.fillStyle = 'rgba(160,120,70,0.35)';
  for (let i = 0; i < 9; i++) {
    const k = i / 9, y = hy + (h - hy) * k + unit * 0.02;
    ctx.beginPath(); ctx.ellipse(hx + Math.sin(i * 1.7) * unit * 0.03, y, unit * (0.03 + k * 0.03), unit * (0.01 + k * 0.012), 0, 0, Math.PI * 2); ctx.fill();
  }
  // çimen tutamları
  ctx.strokeStyle = 'rgba(30,90,20,0.45)'; ctx.lineWidth = 1.4;
  for (let i = 0; i < 70; i++) {
    const x = rnd(i) * w, y = h * (0.48 + rnd(i + 99) * 0.52);
    ctx.beginPath(); ctx.moveTo(x, y); ctx.lineTo(x - 3, y - 7); ctx.moveTo(x, y); ctx.lineTo(x + 3, y - 8); ctx.stroke();
  }

  type Item = { y: number; draw: () => void };
  const items: Item[] = [];
  items.push({ y: hy, draw: () => drawHouse(ctx, d.house, hx, hy, unit * 0.36, t) });
  // çiçekler (filizlenen tohumlar)
  const fcol = ['#ff8fb1', '#fff1a8', '#c79bff', '#ffb36b', '#ffffff', '#ff6b6b'];
  const nFlowers = Math.min(80, d.sprouts);
  for (let i = 0; i < nFlowers; i++) {
    const side = i % 2 ? 1 : -1;
    const x = w / 2 + side * (unit * 0.17 + rnd(i * 3 + 1) * w * 0.33), y = h * (0.5 + rnd(i * 3 + 2) * 0.48);
    const sway = Math.sin(t * 1.5 + i) * 0.08;
    items.push({ y, draw: () => { ctx.save(); ctx.translate(x, y); ctx.rotate(sway); drawFlower(ctx, 0, -unit * 0.035, unit * 0.05, fcol[i % fcol.length]); ctx.restore(); } });
  }
  // dekorlar ve yuvalar
  SLOT_POS.forEach((p, i) => {
    const x = p.x * w, y = p.y * h, id = d.slots[i];
    const sc = unit * (0.15 + (p.y - 0.6) * 0.15);
    items.push({
      y, draw: () => {
        if (opts.selected === i) {
          ctx.save(); ctx.fillStyle = 'rgba(255,240,150,0.45)';
          ctx.beginPath(); ctx.ellipse(x, y, sc * 0.6, sc * 0.18, 0, 0, Math.PI * 2); ctx.fill(); ctx.restore();
        }
        if (id) drawDecor(ctx, id, x, y, sc, t);
        else if (opts.slotHints) {
          ctx.save();
          const pulse = 1 + Math.sin(t * 3 + i) * 0.06;
          ctx.setLineDash([6, 5]);
          ctx.strokeStyle = 'rgba(255,255,255,0.9)'; ctx.lineWidth = 2;
          ctx.beginPath(); ctx.ellipse(x, y - 4, sc * 0.45 * pulse, sc * 0.16 * pulse, 0, 0, Math.PI * 2); ctx.stroke();
          ctx.fillStyle = 'rgba(255,255,255,0.95)';
          ctx.font = `900 ${sc * 0.35}px system-ui`; ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
          ctx.fillText('+', x, y - 4);
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
      const baseY = h * (0.6 + rnd(idx * 5 + 1) * 0.36);
      const x = w * 0.5 + Math.sin(t * speed + ph) * w * 0.42;
      const dir = Math.cos(t * speed + ph) >= 0 ? 1 : -1;
      const hop = k === 'ladybug' ? Math.abs(Math.sin(t * 4 + ph)) * unit * 0.02 : 0;
      items.push({
        y: baseY, draw: () => {
          ctx.save();
          ctx.fillStyle = 'rgba(0,0,0,0.18)';
          ctx.beginPath(); ctx.ellipse(x, baseY, unit * 0.035, unit * 0.01, 0, 0, Math.PI * 2); ctx.fill();
          ctx.translate(x, baseY - unit * 0.03 - hop);
          ctx.scale(dir, 1);
          drawAnimal(ctx, k, 0, 0, unit * 0.08, t);
          ctx.restore();
        },
      });
    }
  }
  // Bahçıvan Dede: patikada gezinir, ara ara el sallar
  if (opts.gardener !== false) {
    const gx = w * 0.5 + Math.sin(t * 0.18) * w * 0.22;
    const gy = h * 0.74 + Math.cos(t * 0.18) * h * 0.03;
    items.push({ y: gy, draw: () => drawGardener(ctx, gx, gy, unit * 0.17, 'happy', t, Math.sin(t * 0.5) > 0.6) });
  }
  items.sort((a, b) => a.y - b.y).forEach((it) => it.draw());
}
