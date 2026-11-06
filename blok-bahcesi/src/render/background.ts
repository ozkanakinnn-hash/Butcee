// Sıcak ahşap/toprak arka plan + mevsim tonu.
import { SEASON_TINT } from './colors';

let woodCache: HTMLCanvasElement | null = null;
let woodKey = '';

function buildWood(w: number, h: number, dpr: number): HTMLCanvasElement {
  // Pastel zemin: şeftali → lavanta → nane, üstünde çok hafif açık ahşap şeritleri ve yumuşak ışık lekeleri
  const cv = document.createElement('canvas');
  cv.width = Math.ceil(w * dpr);
  cv.height = Math.ceil(h * dpr);
  const ctx = cv.getContext('2d')!;
  ctx.scale(dpr, dpr);
  const g = ctx.createLinearGradient(0, 0, w * 0.3, h);
  g.addColorStop(0, '#ffe6d6');
  g.addColorStop(0.5, '#efe2fb');
  g.addColorStop(1, '#d9f3ea');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, w, h);
  const plank = Math.max(64, w / 5);
  for (let x = 0; x < w; x += plank) {
    ctx.fillStyle = 'rgba(160,120,170,0.06)';
    ctx.fillRect(x, 0, 2, h);
    ctx.fillStyle = 'rgba(255,255,255,0.35)';
    ctx.fillRect(x + 2, 0, 2, h);
  }
  let seed = 7;
  const r = () => ((seed = (seed * 16807) % 2147483647) / 2147483647);
  ctx.lineWidth = 1.2;
  for (let i = 0; i < 46; i++) {
    const x0 = r() * w, amp = 3 + r() * 8, freq = 0.004 + r() * 0.01;
    ctx.strokeStyle = r() > 0.5 ? 'rgba(170,130,190,0.07)' : 'rgba(255,255,255,0.25)';
    ctx.beginPath();
    for (let y = 0; y <= h; y += 8) ctx.lineTo(x0 + Math.sin(y * freq + i) * amp, y);
    ctx.stroke();
  }
  // yumuşak bokeh lekeleri
  for (let i = 0; i < 9; i++) {
    const x = r() * w, y = r() * h, rad = 40 + r() * 110;
    const rg = ctx.createRadialGradient(x, y, 0, x, y, rad);
    const col = ['255,255,255', '255,214,231', '207,236,255', '214,255,226'][i % 4];
    rg.addColorStop(0, `rgba(${col},0.45)`);
    rg.addColorStop(1, `rgba(${col},0)`);
    ctx.fillStyle = rg;
    ctx.beginPath(); ctx.arc(x, y, rad, 0, Math.PI * 2); ctx.fill();
  }
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
  ctx.globalCompositeOperation = 'multiply';
  tintOf(s0, 0.35 * (1 - f));
  if (f > 0) tintOf(s1, 0.35 * f);
  ctx.restore();
  ctx.globalAlpha = 1;
}
