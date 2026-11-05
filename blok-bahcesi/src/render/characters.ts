// Bahçıvan Dede, elmas ve madalya çizimleri — tamamen kodla (özgün tasarım).
import { roundRect, setGardenerCellDrawer } from './draw';

type C2D = CanvasRenderingContext2D;
export type GardenerMood = 'happy' | 'scared' | 'cheer' | 'wet';

const SKIN = '#f2c19a';

/** (cx, by) ayak ortası; s ≈ toplam yükseklik. */
export function drawGardener(ctx: C2D, cx: number, by: number, s: number, mood: GardenerMood = 'happy', t = 0, wave = false) {
  const u = s / 24;
  ctx.save();
  const jump = mood === 'cheer' ? -Math.abs(Math.sin(t * 6)) * 2.2 * u : 0;
  const shiver = mood === 'scared' ? Math.sin(t * 40) * 0.25 * u : 0;
  ctx.translate(cx + shiver, by);
  // gölge
  ctx.fillStyle = 'rgba(0,0,0,0.22)';
  ctx.beginPath(); ctx.ellipse(0, 0, 6 * u, 1.3 * u, 0, 0, Math.PI * 2); ctx.fill();
  ctx.translate(0, jump);
  ctx.lineJoin = 'round';
  ctx.lineCap = 'round';

  // botlar + bacaklar
  for (const sx of [-1, 1]) {
    ctx.fillStyle = '#2f5592';
    roundRect(ctx, sx * 2.4 * u - 1.2 * u, -6 * u, 2.4 * u, 5 * u, 0.6 * u); ctx.fill();
    ctx.fillStyle = '#6b3f22';
    ctx.beginPath(); ctx.ellipse(sx * 2.6 * u, -0.9 * u, 2 * u, 1.1 * u, 0, 0, Math.PI * 2); ctx.fill();
  }
  // gömlek (gövde arkası)
  ctx.fillStyle = '#e0623a';
  roundRect(ctx, -4.6 * u, -14.5 * u, 9.2 * u, 7 * u, 2.4 * u); ctx.fill();
  // ekose çizgiler
  ctx.strokeStyle = 'rgba(120,20,10,0.35)';
  ctx.lineWidth = 0.35 * u;
  for (let i = -3; i <= 3; i += 1.5) { ctx.beginPath(); ctx.moveTo(i * u, -14.3 * u); ctx.lineTo(i * u, -9 * u); ctx.stroke(); }
  // tulum
  ctx.fillStyle = '#3d6fb6';
  roundRect(ctx, -4.4 * u, -11 * u, 8.8 * u, 6.2 * u, 2 * u); ctx.fill();
  roundRect(ctx, -2.8 * u, -13.2 * u, 5.6 * u, 3.5 * u, 0.8 * u); ctx.fill();
  ctx.strokeStyle = '#2f5592';
  ctx.lineWidth = 0.9 * u;
  ctx.beginPath(); ctx.moveTo(-2.4 * u, -12.8 * u); ctx.lineTo(-3.4 * u, -14.6 * u); ctx.moveTo(2.4 * u, -12.8 * u); ctx.lineTo(3.4 * u, -14.6 * u); ctx.stroke();
  ctx.fillStyle = '#ffd54a';
  for (const sx of [-1, 1]) { ctx.beginPath(); ctx.arc(sx * 2.2 * u, -12.6 * u, 0.55 * u, 0, Math.PI * 2); ctx.fill(); }
  // cep
  ctx.strokeStyle = 'rgba(255,255,255,0.35)';
  ctx.lineWidth = 0.35 * u;
  roundRect(ctx, -1.4 * u, -11.6 * u, 2.8 * u, 2 * u, 0.4 * u); ctx.stroke();

  // kollar
  const armsUp = mood === 'cheer' || mood === 'scared';
  const arm = (sx: number, ang: number) => {
    ctx.save();
    ctx.translate(sx * 4.2 * u, -13.2 * u);
    ctx.rotate(ang);
    ctx.fillStyle = '#e0623a';
    roundRect(ctx, -1.1 * u, 0, 2.2 * u, 4.6 * u, 1 * u); ctx.fill();
    ctx.fillStyle = SKIN;
    ctx.beginPath(); ctx.arc(0, 5.2 * u, 1.25 * u, 0, Math.PI * 2); ctx.fill();
    ctx.restore();
  };
  if (armsUp) {
    const sh = mood === 'cheer' ? Math.sin(t * 12) * 0.25 : Math.sin(t * 25) * 0.15;
    arm(-1, Math.PI - 0.5 + sh);
    arm(1, -Math.PI + 0.5 - sh);
  } else {
    // sol kol: sulama kabı
    arm(-1, 0.35);
    drawCan(ctx, -7.2 * u, -6.2 * u, u);
    arm(1, wave ? -Math.PI + 0.6 + Math.sin(t * 7) * 0.45 : -0.25);
  }

  // kafa
  const hy = -18.4 * u;
  ctx.fillStyle = SKIN;
  ctx.beginPath(); ctx.arc(-4.1 * u, hy + 0.4 * u, 1.1 * u, 0, Math.PI * 2); ctx.arc(4.1 * u, hy + 0.4 * u, 1.1 * u, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(0, hy, 4.3 * u, 0, Math.PI * 2); ctx.fill();
  // yanaklar
  ctx.fillStyle = 'rgba(240,110,110,0.45)';
  ctx.beginPath(); ctx.arc(-2.5 * u, hy + 1.2 * u, 1 * u, 0, Math.PI * 2); ctx.arc(2.5 * u, hy + 1.2 * u, 1 * u, 0, Math.PI * 2); ctx.fill();
  // gözler
  ctx.fillStyle = '#3a2414';
  ctx.strokeStyle = '#3a2414';
  ctx.lineWidth = 0.5 * u;
  if (mood === 'cheer') {
    for (const sx of [-1, 1]) { ctx.beginPath(); ctx.arc(sx * 1.6 * u, hy - 0.4 * u, 0.8 * u, Math.PI * 1.1, Math.PI * 1.9); ctx.stroke(); }
  } else if (mood === 'scared') {
    for (const sx of [-1, 1]) {
      ctx.fillStyle = '#fff'; ctx.beginPath(); ctx.arc(sx * 1.6 * u, hy - 0.5 * u, 1 * u, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#3a2414'; ctx.beginPath(); ctx.arc(sx * 1.6 * u, hy - 0.3 * u, 0.45 * u, 0, Math.PI * 2); ctx.fill();
    }
  } else {
    const blink = Math.sin(t * 1.3) > 0.97 ? 0.15 : 1;
    for (const sx of [-1, 1]) { ctx.beginPath(); ctx.ellipse(sx * 1.6 * u, hy - 0.4 * u, 0.5 * u, 0.6 * u * blink, 0, 0, Math.PI * 2); ctx.fill(); }
  }
  // kaşlar
  ctx.strokeStyle = '#eeeeee';
  ctx.lineWidth = 0.6 * u;
  for (const sx of [-1, 1]) {
    ctx.beginPath();
    const lift = mood === 'scared' ? -0.6 : 0;
    ctx.moveTo(sx * 0.8 * u, hy + (-1.9 + lift) * u); ctx.lineTo(sx * 2.4 * u, hy + (-2.1 - lift * 0.5) * u); ctx.stroke();
  }
  // burun
  ctx.fillStyle = '#e8957a';
  ctx.beginPath(); ctx.ellipse(0, hy + 0.9 * u, 1 * u, 0.85 * u, 0, 0, Math.PI * 2); ctx.fill();
  // ağız
  ctx.fillStyle = '#7a2a1a';
  if (mood === 'scared' || mood === 'wet') { ctx.beginPath(); ctx.ellipse(0, hy + 2.9 * u, 0.8 * u, 1 * u, 0, 0, Math.PI * 2); ctx.fill(); }
  else { ctx.beginPath(); ctx.arc(0, hy + 2.2 * u, 1.3 * u, 0.15 * Math.PI, 0.85 * Math.PI); ctx.fill(); }
  // bıyık
  ctx.fillStyle = '#f4f4f4';
  for (const sx of [-1, 1]) { ctx.beginPath(); ctx.ellipse(sx * 1.25 * u, hy + 1.9 * u, 1.5 * u, 0.75 * u, sx * -0.35, 0, Math.PI * 2); ctx.fill(); }

  // hasır şapka
  ctx.fillStyle = '#e9c46a';
  ctx.strokeStyle = '#b8902f';
  ctx.lineWidth = 0.35 * u;
  ctx.beginPath(); ctx.ellipse(0, hy - 2.9 * u, 7.2 * u, 1.7 * u, 0, 0, Math.PI * 2); ctx.fill(); ctx.stroke();
  roundRect(ctx, -3.8 * u, hy - 7.4 * u, 7.6 * u, 4.8 * u, 1.8 * u); ctx.fill(); ctx.stroke();
  ctx.fillStyle = '#4c9a3a';
  ctx.fillRect(-3.8 * u, hy - 4.3 * u, 7.6 * u, 1.2 * u);
  // şapkada çiçek
  ctx.fillStyle = '#ff8fb1';
  for (let i = 0; i < 5; i++) { const a = (i / 5) * Math.PI * 2; ctx.beginPath(); ctx.arc(2.6 * u + Math.cos(a) * 0.6 * u, hy - 3.7 * u + Math.sin(a) * 0.6 * u, 0.5 * u, 0, Math.PI * 2); ctx.fill(); }
  ctx.fillStyle = '#ffd54a'; ctx.beginPath(); ctx.arc(2.6 * u, hy - 3.7 * u, 0.4 * u, 0, Math.PI * 2); ctx.fill();

  // ter / su damlaları
  if (mood === 'scared' || mood === 'wet') {
    ctx.fillStyle = '#7cc8ff';
    const drops = mood === 'wet' ? [[-5, -14], [5.5, -10], [-3, -22], [4, -20]] : [[5.2, -20.5]];
    for (const [x, y] of drops) {
      const yy = y + ((t * 6 + x) % 2);
      ctx.beginPath(); ctx.moveTo(x * u, (yy - 1.2) * u);
      ctx.quadraticCurveTo((x + 0.9) * u, yy * u, x * u, (yy + 0.6) * u);
      ctx.quadraticCurveTo((x - 0.9) * u, yy * u, x * u, (yy - 1.2) * u); ctx.fill();
    }
  }
  ctx.restore();
}

function drawCan(ctx: C2D, x: number, y: number, u: number) {
  ctx.save();
  ctx.translate(x, y);
  ctx.fillStyle = '#4caf50';
  ctx.strokeStyle = '#2e7d32';
  ctx.lineWidth = 0.35 * u;
  roundRect(ctx, -2 * u, -2 * u, 4 * u, 3.6 * u, 0.6 * u); ctx.fill(); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(-2 * u, -0.8 * u); ctx.lineTo(-4.2 * u, -2.6 * u); ctx.lineTo(-4.6 * u, -2.2 * u); ctx.lineTo(-2 * u, 0.2 * u); ctx.fill(); ctx.stroke();
  ctx.beginPath(); ctx.arc(0, -2.4 * u, 1.4 * u, Math.PI, 0); ctx.stroke();
  ctx.restore();
}

export function drawDiamond(ctx: C2D, cx: number, cy: number, r: number) {
  ctx.save();
  ctx.translate(cx, cy);
  const top = -r * 0.62, mid = -r * 0.12, bot = r * 0.85, w = r * 0.95, tw = r * 0.55;
  const g = ctx.createLinearGradient(0, top, 0, bot);
  g.addColorStop(0, '#b6f4ff'); g.addColorStop(0.5, '#3cc8f0'); g.addColorStop(1, '#1576c4');
  ctx.fillStyle = g;
  ctx.beginPath();
  ctx.moveTo(-tw, top); ctx.lineTo(tw, top); ctx.lineTo(w, mid); ctx.lineTo(0, bot); ctx.lineTo(-w, mid); ctx.closePath();
  ctx.fill();
  ctx.strokeStyle = 'rgba(10,60,110,0.75)';
  ctx.lineWidth = Math.max(1, r * 0.08);
  ctx.stroke();
  ctx.strokeStyle = 'rgba(255,255,255,0.7)';
  ctx.lineWidth = Math.max(0.8, r * 0.06);
  ctx.beginPath();
  ctx.moveTo(-w, mid); ctx.lineTo(w, mid);
  ctx.moveTo(-tw * 0.35, top); ctx.lineTo(-w * 0.45, mid); ctx.lineTo(0, bot);
  ctx.moveTo(tw * 0.35, top); ctx.lineTo(w * 0.45, mid); ctx.lineTo(0, bot);
  ctx.stroke();
  ctx.fillStyle = 'rgba(255,255,255,0.85)';
  ctx.beginPath(); ctx.ellipse(-r * 0.35, top + r * 0.2, r * 0.18, r * 0.08, -0.4, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

export const MEDAL_COLORS = [
  { fill: '#ffcc33', dark: '#b07800', ribbon: '#d93a3a' }, // altın
  { fill: '#d6dde4', dark: '#7d8892', ribbon: '#3a73d9' }, // gümüş
  { fill: '#dc8b4a', dark: '#8a4a1c', ribbon: '#3aa35a' }, // bronz
];

export function drawMedal(ctx: C2D, cx: number, cy: number, r: number, place: number) {
  const m = MEDAL_COLORS[place];
  ctx.save();
  ctx.translate(cx, cy);
  ctx.fillStyle = m.ribbon;
  ctx.beginPath(); ctx.moveTo(-r * 0.9, -r * 1.9); ctx.lineTo(-r * 0.15, -r * 0.6); ctx.lineTo(-r * 0.6, -r * 0.4); ctx.lineTo(-r * 1.3, -r * 1.7); ctx.fill();
  ctx.beginPath(); ctx.moveTo(r * 0.9, -r * 1.9); ctx.lineTo(r * 0.15, -r * 0.6); ctx.lineTo(r * 0.6, -r * 0.4); ctx.lineTo(r * 1.3, -r * 1.7); ctx.fill();
  const g = ctx.createRadialGradient(-r * 0.3, -r * 0.3, r * 0.1, 0, 0, r);
  g.addColorStop(0, '#ffffff'); g.addColorStop(0.35, m.fill); g.addColorStop(1, m.dark);
  ctx.fillStyle = g;
  ctx.beginPath(); ctx.arc(0, 0, r, 0, Math.PI * 2); ctx.fill();
  ctx.strokeStyle = m.dark; ctx.lineWidth = r * 0.12; ctx.stroke();
  ctx.beginPath(); ctx.arc(0, 0, r * 0.72, 0, Math.PI * 2); ctx.strokeStyle = 'rgba(255,255,255,0.5)'; ctx.lineWidth = r * 0.06; ctx.stroke();
  ctx.fillStyle = m.dark;
  ctx.font = `900 ${r * 1.05}px system-ui, sans-serif`;
  ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
  ctx.fillText(String(place + 1), 0, r * 0.06);
  ctx.restore();
}

/** Küçük DOM simgesi üretici */
export function smallCanvas(size: number, draw: (ctx: C2D, s: number) => void): HTMLCanvasElement {
  const dpr = Math.min(3, window.devicePixelRatio || 1);
  const cv = document.createElement('canvas');
  cv.width = cv.height = Math.round(size * dpr);
  cv.style.width = cv.style.height = size + 'px';
  const ctx = cv.getContext('2d')!;
  ctx.scale(dpr, dpr);
  draw(ctx, size);
  return cv;
}

export const diamondIcon = (size = 22) => smallCanvas(size, (c, s) => drawDiamond(c, s / 2, s / 2, s * 0.44));
export const medalIcon = (place: number, size = 34) => smallCanvas(size, (c, s) => drawMedal(c, s / 2, s * 0.6, s * 0.34, place));
export const gardenerIcon = (size = 40, mood: GardenerMood = 'happy') => smallCanvas(size, (c, s) => drawGardener(c, s / 2, s * 0.98, s * 0.95, mood));

// Tahtadaki bahçıvan hücresi: büyük yüz + şapka (hücre içinde okunaklı olsun diye yakın plan)
setGardenerCellDrawer((ctx, s) => {
  ctx.save();
  ctx.beginPath();
  ctx.rect(s * 0.05, s * 0.05, s * 0.9, s * 0.9);
  ctx.clip();
  drawGardener(ctx, s / 2, s * 1.55, s * 1.75, 'happy', 0);
  ctx.restore();
});
