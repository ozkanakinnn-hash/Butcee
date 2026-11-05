// Parçacıklar ve uçan yazılar.
import type { AnimalKind, Cell } from '../game/types';
import { drawAnimal, drawCell, drawFlower, roundRect } from './draw';

interface Particle {
  kind: 'spark' | 'petal' | 'leaf' | 'square' | 'animal' | 'flower' | 'ring' | 'cell';
  x: number; y: number; vx: number; vy: number;
  life: number; max: number; size: number; color: string; rot: number; vr: number;
  animal?: AnimalKind; cell?: Cell; gravity: number;
}

interface FloatText { text: string; x: number; y: number; life: number; max: number; color: string; size: number }

const PETALS = ['#ffb3c7', '#ff8fb1', '#fff1a8', '#ffffff', '#b8e986'];
const SEASON_PARTS = [
  ['#ffb3c7', '#ff8fb1', '#ffffff'],
  ['#ffe066', '#ffb703', '#8bd35a'],
  ['#e76f2a', '#c0392b', '#f4a261'],
  ['#ffffff', '#d6ecff', '#a8d4ff'],
];

export class Effects {
  parts: Particle[] = [];
  texts: FloatText[] = [];
  shake = 0;

  private add(p: Partial<Particle> & Pick<Particle, 'kind' | 'x' | 'y'>) {
    this.parts.push({
      vx: 0, vy: 0, life: 0, max: 1, size: 6, color: '#fff', rot: Math.random() * 6, vr: (Math.random() - 0.5) * 6,
      gravity: 300, ...p,
    });
  }

  cellBurst(x: number, y: number, s: number, color: string) {
    for (let i = 0; i < 5; i++) {
      this.add({
        kind: 'square', x: x + s / 2, y: y + s / 2, vx: (Math.random() - 0.5) * 260, vy: -Math.random() * 260 - 40,
        max: 0.6 + Math.random() * 0.3, size: s * (0.12 + Math.random() * 0.12), color,
      });
    }
    this.add({ kind: 'spark', x: x + s / 2, y: y + s / 2, max: 0.35, size: s * 0.9, color: '#fff', gravity: 0 });
  }

  flowerBloom(x: number, y: number, w: number) {
    for (let i = 0; i < 26; i++) {
      this.add({
        kind: 'petal', x: x + Math.random() * w, y, vx: (Math.random() - 0.5) * 160, vy: -120 - Math.random() * 220,
        max: 1.2 + Math.random() * 0.6, size: 5 + Math.random() * 5, color: PETALS[i % PETALS.length], gravity: 160,
      });
    }
    for (let i = 0; i < 6; i++) {
      this.add({ kind: 'flower', x: x + (i + 0.5) * (w / 6), y, vy: -60, max: 1.1, size: 26, color: PETALS[i % 3], gravity: 0, vr: 0, rot: 0 });
    }
  }

  animalFly(a: AnimalKind, x: number, y: number, s: number) {
    this.add({ kind: 'animal', animal: a, x: x + s / 2, y: y + s / 2, vx: (Math.random() > 0.5 ? 1 : -1) * 90, vy: -330, max: 1.6, size: s, gravity: -40, vr: 0, rot: 0 });
    for (let i = 0; i < 8; i++) {
      this.add({ kind: 'spark', x: x + s / 2, y: y + s / 2, vx: (Math.random() - 0.5) * 200, vy: (Math.random() - 0.5) * 200, max: 0.5, size: s * 0.25, color: '#fff7b0', gravity: 0 });
    }
  }

  sprout(x: number, y: number, s: number) {
    this.add({ kind: 'flower', x: x + s / 2, y: y + s / 2, vy: -80, max: 1.2, size: s * 0.9, color: '#ffd1e0', gravity: 0, vr: 0, rot: 0 });
    for (let i = 0; i < 8; i++) {
      this.add({ kind: 'leaf', x: x + s / 2, y: y + s / 2, vx: (Math.random() - 0.5) * 160, vy: -100 - Math.random() * 120, max: 0.9, size: 7, color: '#6ccf50', gravity: 220 });
    }
  }

  ring(x: number, y: number, s: number, color: string) {
    this.add({ kind: 'ring', x, y, max: 0.5, size: s, color, gravity: 0 });
  }

  seasonBurst(w: number, h: number, season: number) {
    const cols = SEASON_PARTS[season] ?? SEASON_PARTS[0];
    for (let i = 0; i < 70; i++) {
      this.add({
        kind: Math.random() > 0.5 ? 'petal' : 'leaf', x: w / 2, y: h * 0.45,
        vx: (Math.random() - 0.5) * 700, vy: (Math.random() - 0.8) * 600, max: 1.6 + Math.random(), size: 6 + Math.random() * 6,
        color: cols[i % cols.length], gravity: 220,
      });
    }
  }

  ghostCell(cell: Cell, x: number, y: number, s: number) {
    this.add({ kind: 'cell', cell, x, y, max: 0.35, size: s, gravity: 0, vr: 0, rot: 0 });
  }

  text(text: string, x: number, y: number, color = '#fff', size = 26, max = 1.2) {
    this.texts.push({ text, x, y, life: 0, max, color, size });
  }

  update(dt: number) {
    for (const p of this.parts) {
      p.life += dt;
      p.vy += p.gravity * dt;
      p.x += p.vx * dt;
      p.y += p.vy * dt;
      p.rot += p.vr * dt;
    }
    this.parts = this.parts.filter((p) => p.life < p.max);
    for (const t of this.texts) { t.life += dt; t.y -= 40 * dt; }
    this.texts = this.texts.filter((t) => t.life < t.max);
    this.shake = Math.max(0, this.shake - dt * 2.5);
  }

  get busy() { return this.parts.length > 0 || this.texts.length > 0; }

  draw(ctx: CanvasRenderingContext2D, symbols: boolean) {
    for (const p of this.parts) {
      const k = p.life / p.max;
      ctx.save();
      ctx.globalAlpha = Math.max(0, 1 - k);
      ctx.translate(p.x, p.y);
      ctx.rotate(p.rot);
      switch (p.kind) {
        case 'square':
          ctx.fillStyle = p.color;
          roundRect(ctx, -p.size / 2, -p.size / 2, p.size, p.size, p.size * 0.25);
          ctx.fill();
          break;
        case 'spark': {
          ctx.globalCompositeOperation = 'lighter';
          const r = p.size * (0.4 + k * 0.8);
          const g = ctx.createRadialGradient(0, 0, 0, 0, 0, r);
          g.addColorStop(0, p.color);
          g.addColorStop(1, 'rgba(255,255,255,0)');
          ctx.fillStyle = g;
          ctx.beginPath(); ctx.arc(0, 0, r, 0, Math.PI * 2); ctx.fill();
          break;
        }
        case 'petal':
          ctx.fillStyle = p.color;
          ctx.beginPath(); ctx.ellipse(0, 0, p.size, p.size * 0.5, 0, 0, Math.PI * 2); ctx.fill();
          break;
        case 'leaf':
          ctx.fillStyle = p.color;
          ctx.beginPath();
          ctx.moveTo(-p.size, 0);
          ctx.quadraticCurveTo(0, -p.size, p.size, 0);
          ctx.quadraticCurveTo(0, p.size, -p.size, 0);
          ctx.fill();
          break;
        case 'animal':
          ctx.globalAlpha = k < 0.7 ? 1 : 1 - (k - 0.7) / 0.3;
          ctx.rotate(Math.sin(p.life * 10) * 0.15);
          drawAnimal(ctx, p.animal!, 0, 0, p.size * (1 + k * 0.4), p.life);
          break;
        case 'flower':
          drawFlower(ctx, 0, 0, p.size * Math.min(1, k * 4), p.color);
          break;
        case 'ring':
          ctx.strokeStyle = p.color;
          ctx.lineWidth = 4 * (1 - k);
          ctx.beginPath(); ctx.arc(0, 0, p.size * (0.3 + k), 0, Math.PI * 2); ctx.stroke();
          break;
        case 'cell': {
          const sc = 1 + k * 0.3;
          ctx.rotate(-p.rot);
          ctx.translate(p.size / 2, p.size / 2);
          ctx.scale(sc, sc);
          drawCell(ctx, p.cell!, -p.size / 2, -p.size / 2, p.size, symbols);
          ctx.globalCompositeOperation = 'lighter';
          ctx.globalAlpha = (1 - k) * 0.8;
          ctx.fillStyle = '#fff';
          roundRect(ctx, -p.size / 2, -p.size / 2, p.size, p.size, p.size * 0.2);
          ctx.fill();
          break;
        }
      }
      ctx.restore();
    }
    for (const t of this.texts) {
      const k = t.life / t.max;
      const pop = k < 0.15 ? 0.6 + (k / 0.15) * 0.5 : k < 0.25 ? 1.1 - ((k - 0.15) / 0.1) * 0.1 : 1;
      ctx.save();
      ctx.globalAlpha = k > 0.75 ? 1 - (k - 0.75) / 0.25 : 1;
      ctx.translate(t.x, t.y);
      ctx.scale(pop, pop);
      ctx.font = `800 ${t.size}px system-ui, -apple-system, "Segoe UI", Roboto, sans-serif`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.lineWidth = Math.max(3, t.size * 0.18);
      ctx.strokeStyle = 'rgba(50,25,10,0.85)';
      ctx.lineJoin = 'round';
      ctx.strokeText(t.text, 0, 0);
      ctx.fillStyle = t.color;
      ctx.fillText(t.text, 0, 0);
      ctx.restore();
    }
  }
}
