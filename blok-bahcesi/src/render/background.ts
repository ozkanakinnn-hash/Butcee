// Sıcak ahşap/toprak arka plan + mevsim tonu.
import { SEASON_TINT } from './colors';

let woodCache: HTMLCanvasElement | null = null;
let woodKey = '';

function buildWood(w: number, h: number, dpr: number): HTMLCanvasElement {
  const cv = document.createElement('canvas');
  cv.width = Math.ceil(w * dpr);
  cv.height = Math.ceil(h * dpr);
  const ctx = cv.getContext('2d')!;
  ctx.scale(dpr, dpr);
  const g = ctx.createLinearGradient(0, 0, 0, h);
  g.addColorStop(0, '#6b4329');
  g.addColorStop(0.45, '#8a5a36');
  g.addColorStop(1, '#4a2c19');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, w, h);
  // tahta şeritleri
  const plank = Math.max(60, w / 5);
  for (let x = 0; x < w; x += plank) {
    ctx.fillStyle = 'rgba(0,0,0,0.12)';
    ctx.fillRect(x, 0, 2, h);
    ctx.fillStyle = 'rgba(255,220,170,0.06)';
    ctx.fillRect(x + 2, 0, 2, h);
  }
  // ahşap damar çizgileri
  let seed = 7;
  const r = () => ((seed = (seed * 16807) % 2147483647) / 2147483647);
  ctx.lineWidth = 1.2;
  for (let i = 0; i < 70; i++) {
    const x0 = r() * w;
    const amp = 3 + r() * 8;
    const freq = 0.004 + r() * 0.01;
    ctx.strokeStyle = r() > 0.5 ? 'rgba(40,20,8,0.13)' : 'rgba(255,215,160,0.07)';
    ctx.beginPath();
    for (let y = 0; y <= h; y += 8) ctx.lineTo(x0 + Math.sin(y * freq + i) * amp, y);
    ctx.stroke();
  }
  // budaklar
  for (let i = 0; i < 4; i++) {
    const x = r() * w, y = r() * h;
    for (let k = 4; k > 0; k--) {
      ctx.strokeStyle = 'rgba(40,20,8,0.12)';
      ctx.beginPath();
      ctx.ellipse(x, y, k * 4, k * 9, 0, 0, Math.PI * 2);
      ctx.stroke();
    }
  }
  // kenar gölgesi
  const v = ctx.createRadialGradient(w / 2, h / 2, Math.min(w, h) * 0.3, w / 2, h / 2, Math.max(w, h) * 0.75);
  v.addColorStop(0, 'rgba(0,0,0,0)');
  v.addColorStop(1, 'rgba(0,0,0,0.35)');
  ctx.fillStyle = v;
  ctx.fillRect(0, 0, w, h);
  return cv;
}

/** season: 0..3 arası (ondalıklı geçiş destekli) */
export function drawBackground(ctx: CanvasRenderingContext2D, w: number, h: number, dpr: number, season: number) {
  const key = `${w}x${h}@${dpr}`;
  if (!woodCache || woodKey !== key) {
    woodCache = buildWood(w, h, dpr);
    woodKey = key;
  }
  ctx.drawImage(woodCache, 0, 0, w, h);
  const s0 = Math.floor(season), s1 = Math.min(3, s0 + 1), f = season - s0;
  const tintOf = (i: number, a: number) => {
    const [top, bot] = SEASON_TINT[i];
    const g = ctx.createLinearGradient(0, 0, 0, h);
    g.addColorStop(0, top);
    g.addColorStop(1, bot);
    ctx.globalAlpha = a;
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, w, h);
  };
  ctx.save();
  ctx.globalCompositeOperation = 'soft-light';
  tintOf(s0, 0.55 * (1 - f));
  if (f > 0) tintOf(s1, 0.55 * f);
  ctx.restore();
  ctx.globalAlpha = 1;
}
