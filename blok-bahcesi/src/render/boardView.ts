// Tahta, hayalet parça, el ve önizleme çizimi + dokunma için yerleşim.
import { shapeCells, shapeSize } from '../game/pieces';
import { COLS, ROWS, type Grid, type Piece } from '../game/types';
import { EL_COLOR } from './colors';
import { drawCell, roundRect } from './draw';

export interface Rect { x: number; y: number; w: number; h: number }

export interface ViewInput {
  grid: Grid;
  hand: (Piece | null)[];
  queue: Piece[];
  selected: number | null;
  rot: number;
  col: number;
  ghostRow: number | null;
  /** Klasik Hız: parçanın tahtadaki gerçek satırı. null → tahtanın üstünde süzülür. */
  pieceRow: number | null;
  /** Düşüş animasyonu 0..1 (null: yok) */
  drop: { piece: Piece; rot: number; col: number; row: number; k: number } | null;
  flash: { rows: number[]; k: number } | null;
  symbols: boolean;
  t: number;
  attackWarn: boolean;
  dimHand: boolean;
  /** Kurtarma bölümü: alttan su seviyesi (satır, ondalıklı animasyon) */
  water?: number;
}

export class BoardView {
  cell = 40;
  board: Rect = { x: 0, y: 0, w: 0, h: 0 };
  slots: Rect[] = [];
  previews: Rect[] = [];
  strip = 0;
  w = 0;
  h = 0;

  layout(w: number, h: number, y0 = 0) {
    this.w = w;
    this.h = h;
    const pad = 6;
    const stripU = 0.75, handU = 2.75, gapU = 0.25;
    const cell = Math.floor(Math.min((w - pad * 2) / COLS, (h - pad * 2) / (ROWS + stripU + handU + gapU)));
    this.cell = Math.max(12, cell);
    const c = this.cell;
    const total = c * (ROWS + stripU + handU + gapU);
    const top = y0 + Math.max(pad, (h - total) / 2);
    this.strip = c * stripU;
    const bw = c * COLS;
    this.board = { x: Math.round((w - bw) / 2), y: Math.round(top + this.strip), w: bw, h: c * ROWS };
    const hy = this.board.y + this.board.h + c * gapU;
    const hh = Math.min(c * handU, y0 + h - hy - pad);
    const handW = Math.min(w - pad * 2, bw + c * 0.6);
    const hx = (w - handW) / 2;
    const prevW = handW * 0.2;
    const slotW = (handW - prevW - 8) / 3;
    this.slots = [0, 1, 2].map((i) => ({ x: hx + i * slotW, y: hy, w: slotW - 6, h: hh }));
    const px = hx + slotW * 3 + 2;
    this.previews = [0, 1].map((i) => ({ x: px, y: hy + 14 + i * ((hh - 14) / 2), w: prevW, h: (hh - 14) / 2 - 3 }));
  }

  hitHand(x: number, y: number): number | null {
    for (let i = 0; i < this.slots.length; i++) {
      const s = this.slots[i];
      if (x >= s.x - 4 && x <= s.x + s.w + 4 && y >= s.y - 6 && y <= s.y + s.h + 6) return i;
    }
    return null;
  }

  inPlayArea(_x: number, y: number): boolean {
    return y >= this.board.y - this.strip - 10 && y <= this.board.y + this.board.h;
  }

  /** Süzülen parçanın ekran dikdörtgeni */
  floatRect(p: Piece, rot: number, col: number): Rect {
    const { w, h } = shapeSize(shapeCells(p.kind, rot));
    const c = this.cell;
    const y = this.board.y - this.strip - Math.max(0, h - 1) * c * 0.15;
    return { x: this.board.x + col * c, y, w: w * c, h: h * c };
  }

  private drawPiece(ctx: CanvasRenderingContext2D, p: Piece, rot: number, x: number, y: number, s: number, symbols: boolean) {
    for (const [r, c] of shapeCells(p.kind, rot)) drawCell(ctx, { k: 'block', el: p.el }, x + c * s, y + r * s, s, symbols);
  }

  draw(ctx: CanvasRenderingContext2D, v: ViewInput) {
    const { board: b, cell: c } = this;
    // tahta zemini
    ctx.save();
    ctx.shadowColor = 'rgba(0,0,0,0.45)';
    ctx.shadowBlur = 18;
    ctx.shadowOffsetY = 6;
    roundRect(ctx, b.x - 6, b.y - 6, b.w + 12, b.h + 12, 14);
    ctx.fillStyle = '#c99a62';
    ctx.fill();
    ctx.restore();
    roundRect(ctx, b.x - 6, b.y - 6, b.w + 12, b.h + 12, 14);
    ctx.strokeStyle = '#f0d3a2';
    ctx.lineWidth = 2;
    ctx.stroke();
    roundRect(ctx, b.x - 1, b.y - 1, b.w + 2, b.h + 2, 9);
    const bg = ctx.createLinearGradient(0, b.y, 0, b.y + b.h);
    bg.addColorStop(0, '#3a2414');
    bg.addColorStop(1, '#2a180c');
    ctx.fillStyle = bg;
    ctx.fill();
    // hücre yuvaları
    ctx.fillStyle = 'rgba(255,230,190,0.05)';
    for (let r = 0; r < ROWS; r++)
      for (let col = 0; col < COLS; col++)
        if ((r + col) % 2 === 0) ctx.fillRect(b.x + col * c, b.y + r * c, c, c);

    // saldırı uyarısı
    if (v.attackWarn) {
      const a = 0.35 + 0.25 * Math.sin(v.t * 8);
      ctx.fillStyle = `rgba(220,40,40,${a})`;
      ctx.fillRect(b.x, b.y + b.h - c * 0.35, b.w, c * 0.35);
    }

    // hücreler
    for (let r = 0; r < ROWS; r++) {
      for (let col = 0; col < COLS; col++) {
        const cell = v.grid[r][col];
        if (cell) drawCell(ctx, cell, b.x + col * c, b.y + r * c, c, v.symbols, r * 3 + col * 5);
      }
    }

    // yükselen su
    if (v.water && v.water > 0.01) {
      const top = b.y + b.h - v.water * c;
      ctx.save();
      roundRect(ctx, b.x, b.y, b.w, b.h, 9);
      ctx.clip();
      const g = ctx.createLinearGradient(0, top, 0, b.y + b.h);
      g.addColorStop(0, 'rgba(90,190,255,0.55)');
      g.addColorStop(1, 'rgba(20,90,180,0.7)');
      ctx.fillStyle = g;
      ctx.beginPath();
      ctx.moveTo(b.x, b.y + b.h);
      for (let x = 0; x <= b.w; x += 6) ctx.lineTo(b.x + x, top + Math.sin(x / c * 2.2 + v.t * 3) * c * 0.08);
      ctx.lineTo(b.x + b.w, b.y + b.h);
      ctx.closePath();
      ctx.fill();
      ctx.strokeStyle = 'rgba(220,245,255,0.85)';
      ctx.lineWidth = 2;
      ctx.beginPath();
      for (let x = 0; x <= b.w; x += 6) ctx.lineTo(b.x + x, top + Math.sin(x / c * 2.2 + v.t * 3) * c * 0.08);
      ctx.stroke();
      ctx.fillStyle = 'rgba(255,255,255,0.5)';
      for (let i = 0; i < 10; i++) {
        const k = (v.t * 0.4 + i * 0.137) % 1;
        const bx = b.x + ((i * 0.618) % 1) * b.w, by = b.y + b.h - k * v.water * c;
        ctx.beginPath(); ctx.arc(bx, by, c * 0.05 + (i % 3) * 1.2, 0, Math.PI * 2); ctx.fill();
      }
      ctx.restore();
    }

    // temizlenen satır parlaması
    if (v.flash) {
      const a = Math.sin(Math.min(1, v.flash.k) * Math.PI);
      ctx.save();
      ctx.globalCompositeOperation = 'lighter';
      for (const r of v.flash.rows) {
        ctx.fillStyle = `rgba(255,250,220,${0.75 * a})`;
        roundRect(ctx, b.x, b.y + r * c, b.w, c, c * 0.2);
        ctx.fill();
      }
      ctx.restore();
    }

    const sel = v.selected !== null ? v.hand[v.selected] : null;
    // hayalet + ışın
    if (sel && v.ghostRow !== null && !v.drop) {
      const cells = shapeCells(sel.kind, v.rot);
      const color = EL_COLOR[sel.el];
      const fr = this.floatRect(sel, v.rot, v.col);
      const startY = v.pieceRow === null ? fr.y + fr.h : b.y + (v.pieceRow + 1) * c;
      // ışın: her sütunda parçadan hayalete
      const colsUsed = new Map<number, number>();
      for (const [r, cc] of cells) colsUsed.set(cc, Math.min(colsUsed.get(cc) ?? 99, r));
      ctx.save();
      for (const [cc, topR] of colsUsed) {
        const gy = b.y + (v.ghostRow + topR) * c;
        if (gy <= startY) continue;
        const g = ctx.createLinearGradient(0, startY, 0, gy);
        const pulse = 0.12 + 0.06 * Math.sin(v.t * 4);
        g.addColorStop(0, `rgba(255,245,200,${pulse})`);
        g.addColorStop(1, `rgba(255,245,200,${pulse * 2.2})`);
        ctx.fillStyle = g;
        ctx.fillRect(b.x + (v.col + cc) * c + c * 0.12, startY, c * 0.76, gy - startY);
      }
      // hayalet hücreler
      for (const [r, cc] of cells) {
        const x = b.x + (v.col + cc) * c, y = b.y + (v.ghostRow + r) * c;
        ctx.globalAlpha = 0.32;
        drawCell(ctx, { k: 'block', el: sel.el }, x, y, c, v.symbols);
        ctx.globalAlpha = 0.9;
        roundRect(ctx, x + c * 0.08, y + c * 0.08, c * 0.84, c * 0.84, c * 0.18);
        ctx.setLineDash([c * 0.14, c * 0.1]);
        ctx.strokeStyle = color;
        ctx.lineWidth = 2;
        ctx.stroke();
        ctx.setLineDash([]);
      }
      ctx.restore();
    }

    // seçili parça (süzülen veya klasikte tahtadaki)
    if (v.drop) {
      const { piece, rot, col, row, k } = v.drop;
      const fr = this.floatRect(piece, rot, col);
      const endY = b.y + row * c;
      const e = k * k;
      this.drawPiece(ctx, piece, rot, fr.x, fr.y + (endY - fr.y) * e, c, v.symbols);
    } else if (sel) {
      ctx.save();
      if (v.pieceRow === null) {
        const fr = this.floatRect(sel, v.rot, v.col);
        const bob = Math.sin(v.t * 3) * c * 0.05;
        ctx.shadowColor = 'rgba(0,0,0,0.5)';
        ctx.shadowBlur = 14;
        ctx.shadowOffsetY = 8;
        this.drawPiece(ctx, sel, v.rot, fr.x, fr.y + bob, c, v.symbols);
      } else {
        this.drawPiece(ctx, sel, v.rot, b.x + v.col * c, b.y + v.pieceRow * c, c, v.symbols);
      }
      ctx.restore();
    }

    // el
    v.hand.forEach((p, i) => {
      const s = this.slots[i];
      const isSel = v.selected === i;
      roundRect(ctx, s.x, s.y, s.w, s.h, 12);
      ctx.fillStyle = isSel ? 'rgba(255,236,190,0.30)' : 'rgba(255,236,190,0.13)';
      ctx.fill();
      ctx.lineWidth = isSel ? 3 : 1.5;
      ctx.strokeStyle = isSel ? '#ffd56b' : 'rgba(255,230,190,0.35)';
      ctx.stroke();
      if (!p) return;
      const cells = shapeCells(p.kind, isSel ? v.rot : 0);
      const { w, h } = shapeSize(cells);
      const m = Math.min((s.w - 12) / 4, (s.h - 12) / 4, c * 0.62);
      ctx.save();
      ctx.globalAlpha = isSel ? 0.4 : v.dimHand ? 0.5 : 1;
      this.drawPiece(ctx, p, isSel ? v.rot : 0, s.x + (s.w - w * m) / 2, s.y + (s.h - h * m) / 2, m, v.symbols);
      ctx.restore();
    });

    // sıradaki parçalar
    ctx.save();
    ctx.fillStyle = 'rgba(255,236,200,0.75)';
    ctx.font = `700 ${Math.max(10, c * 0.26)}px system-ui, sans-serif`;
    ctx.textAlign = 'center';
    if (this.previews.length) ctx.fillText(nextLabel, this.previews[0].x + this.previews[0].w / 2, this.previews[0].y - 3);
    v.queue.slice(0, 2).forEach((p, i) => {
      const s = this.previews[i];
      roundRect(ctx, s.x, s.y, s.w, s.h, 8);
      ctx.fillStyle = 'rgba(0,0,0,0.18)';
      ctx.fill();
      const cells = shapeCells(p.kind, 0);
      const { w, h } = shapeSize(cells);
      const m = Math.min((s.w - 6) / 4, (s.h - 6) / 2.4, c * 0.32);
      ctx.globalAlpha = i === 0 ? 0.95 : 0.7;
      this.drawPiece(ctx, p, 0, s.x + (s.w - w * m) / 2, s.y + (s.h - h * m) / 2, m, false);
      ctx.globalAlpha = 1;
    });
    ctx.restore();
  }
}

let nextLabel = 'Sıra';
export function setNextLabel(s: string) { nextLabel = s; }
