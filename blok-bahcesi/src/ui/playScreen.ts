import { t } from '../i18n';
import { haptic, sfx } from '../audio';
import { fitsAt, landingRow } from '../game/board';
import { Game, type MoveResult } from '../game/game';
import { classicInterval, dailySeed, dateKey, generateClassic, generateDaily, generateZen } from '../game/levelgen';
import { shapeCells, shapeSize } from '../game/pieces';
import { COLS, type Cell, type Grid, type LevelDef, type Mode } from '../game/types';
import { getLevel, LEVELS } from '../levels';
import { drawBackground } from '../render/background';
import { BoardView, setNextLabel, type Rect } from '../render/boardView';
import { EL_COLOR } from '../render/colors';
import { clearSpriteCache, iconCanvas } from '../render/draw';
import { Effects } from '../render/effects';
import { recordClassic, recordCollection, recordLevel, persist, save } from '../storage';
import { h } from './dom';
import type { Router, Screen } from './router';

export interface PlayOpts { mode: Mode; levelId?: number }

interface Step { dur: number; start?: () => void; update?: (k: number) => void; end?: () => void }

const cellColor = (c: Cell): string => {
  switch (c.k) {
    case 'block': return EL_COLOR[c.el];
    case 'soil': return '#8a5a35';
    case 'animal': return '#cfe6a8';
    case 'weed': return '#4f8a35';
    case 'ice': return '#bfe6fa';
    case 'seed': return '#d9b27a';
    default: return '#888';
  }
};

export function playScreen(router: Router, opts: PlayOpts): Screen {
  const mode = opts.mode;
  const el = h('div', { class: 'screen play' });
  const canvas = h('canvas', { class: 'stage' });
  const hud = h('div', { class: 'hud' });
  const bossbar = h('div', { class: 'bossbar', style: { display: 'none' } });
  const banner = h('div', { class: 'banner', style: { display: 'none' } }, t('fx.attackSoon'));
  const controls = h('div', { class: 'controls' + (mode === 'classic' ? ' two' : '') });
  el.append(canvas, hud, bossbar, banner, controls);

  const view = new BoardView();
  const fx = new Effects();
  setNextLabel(t('hud.next'));

  let game!: Game;
  let level!: LevelDef;
  let displayGrid: Grid = [];
  let sel: number | null = null;
  let rot = 0;
  let col = 0;
  let pieceRow: number | null = null; // klasik
  let lastSlot = 0;
  let classicTimer = 0;
  let flash: { rows: number[]; k: number } | null = null;
  let drop: { piece: NonNullable<Game['state']['hand'][number]>; rot: number; col: number; row: number; k: number } | null = null;
  let seasonVis = 0;
  let seasonTarget = 0;
  let paused = false;
  let overlay: HTMLElement | null = null;
  let finalized = false;
  let warned = false;
  let raf = 0;
  let lastT = 0;
  let w = 0, hgt = 0, dpr = 1;
  let queue: Step[] = [];
  let cur: Step | null = null;
  let curT = 0;
  const animating = () => !!cur || queue.length > 0;

  // ---------- kurulum ----------
  function setup() {
    const rnd = (Math.random() * 2 ** 32) >>> 0;
    if (mode === 'adventure') {
      level = getLevel(opts.levelId ?? 1)!;
      game = new Game(mode, level, rnd);
    } else if (mode === 'daily') {
      const seed = dailySeed(dateKey());
      level = generateDaily(seed);
      game = new Game(mode, level, seed); // herkes için aynı parça sırası
    } else if (mode === 'zen') {
      level = generateZen(rnd);
      game = new Game(mode, level, rnd);
    } else {
      level = generateClassic();
      game = new Game(mode, level, rnd);
    }
    displayGrid = game.grid;
    sel = null; rot = 0; col = 0; pieceRow = null; lastSlot = 0; classicTimer = 0;
    flash = null; drop = null; queue = []; cur = null;
    seasonVis = seasonTarget = 0;
    finalized = false; warned = false; paused = false;
    closeOverlay();
    buildControls();
    updateHud();
    if (mode === 'adventure' && level.hints?.length && (level.id <= 3 || !save.levels[level.id])) showHints(level.hints);
  }

  // ---------- HUD ----------
  function chip(...children: (Node | string)[]) { return h('span', { class: 'chip' }, ...children); }

  function updateHud() {
    hud.innerHTML = '';
    const s = game.state;
    hud.append(h('button', { class: 'btn icon-btn', 'aria-label': t('pause.title'), onclick: () => openPause() }, '❚❚'));
    const right = h('div', { class: 'grow' });
    if (mode === 'adventure' || mode === 'daily') {
      const g = level.goal.type;
      if (g !== 'boss' && g !== 'none') right.append(chip(iconCanvas(g, 26), String(game.goalRemaining())));
      right.append(chip(h('small', {}, t('hud.moves')), String(game.movesLeft)));
    } else if (mode === 'classic') {
      right.append(chip(h('small', {}, t('hud.lines')), String(s.lines)));
      right.append(chip(h('small', {}, t('hud.speed')), String(Math.floor(s.lines / 10) + 1)));
    } else {
      right.append(chip(h('small', {}, t('hud.best')), String(Math.max(save.zenBest, s.score))));
    }
    right.append(chip(h('small', {}, t('hud.score')), String(s.score)));
    hud.append(right);

    if (game.isBoss) {
      bossbar.style.display = '';
      const max = level.bossHp ?? 1;
      bossbar.innerHTML = '';
      bossbar.append(iconCanvas('boss', 30),
        h('div', { class: 'bar' }, h('div', { class: 'fill', style: { width: `${(100 * s.bossHp) / max}%` } })),
        h('b', {}, `${s.bossHp}/${max}`));
    }
    const soon = game.attackSoon();
    banner.style.display = soon ? '' : 'none';
    if (soon && !warned) { sfx.warn(); haptic('medium'); }
    warned = soon;
    updateButtons();
    requestAnimationFrame(relayout);
  }

  let btnUndo: HTMLButtonElement | null = null, btnRot: HTMLButtonElement, btnDrop: HTMLButtonElement;
  function buildControls() {
    controls.innerHTML = '';
    if (mode !== 'classic') {
      btnUndo = h('button', { class: 'btn', onclick: () => doUndo() });
      controls.append(btnUndo);
    } else btnUndo = null;
    btnRot = h('button', { class: 'btn', onclick: () => rotate() }, '⟳ ' + t('btn.rotate'));
    btnDrop = h('button', { class: 'btn green', onclick: () => doDrop() }, '⤓ ' + t('btn.drop'));
    controls.append(btnRot, btnDrop);
  }
  function updateButtons() {
    if (btnUndo) {
      btnUndo.textContent = `↶ ${game.state.undoLeft}`;
      btnUndo.setAttribute('aria-label', `${t('btn.undo')} (${game.state.undoLeft})`);
      btnUndo.disabled = !game.canUndo();
    }
    btnRot.disabled = sel === null;
    btnDrop.disabled = sel === null;
  }

  // ---------- yerleşim ----------
  function resize() {
    dpr = Math.min(2.5, window.devicePixelRatio || 1);
    w = el.clientWidth || window.innerWidth;
    hgt = el.clientHeight || window.innerHeight;
    canvas.width = Math.round(w * dpr);
    canvas.height = Math.round(hgt * dpr);
    clearSpriteCache(dpr);
    relayout();
  }
  function relayout() {
    const top = Math.max(hud.getBoundingClientRect().bottom, game?.isBoss ? bossbar.getBoundingClientRect().bottom : 0) + 2;
    const bottom = controls.getBoundingClientRect().top - 2;
    view.layout(w, Math.max(100, bottom - top), top);
  }

  // ---------- seçim / döndürme / hareket ----------
  const pieceW = (r: number) => (sel === null ? 1 : shapeSize(shapeCells(game.state.hand[sel]!.kind, r)).w);
  const clampCol = (c: number, r = rot) => Math.max(0, Math.min(COLS - pieceW(r), c));
  const blocked = () => animating() || paused || !!overlay || game.state.status !== 'playing';

  function selectSlot(i: number) {
    if (blocked() || !game.state.hand[i]) return;
    if (sel === i) { rotate(); return; }
    const p = game.state.hand[i]!;
    if (mode === 'classic') {
      // Klasik: yalnızca mevcut konumda sığıyorsa parça değiştirilebilir.
      const cells = shapeCells(p.kind, 0);
      const nc = Math.max(0, Math.min(COLS - shapeSize(cells).w, col));
      if (!fitsAt(game.grid, cells, pieceRow ?? 0, nc)) { sfx.move(); return; }
      sel = i; rot = 0; col = nc;
    } else {
      const prev = sel === null ? null : col;
      sel = i; rot = 0;
      col = clampCol(prev ?? Math.floor((COLS - pieceW(0)) / 2));
    }
    sfx.select();
    haptic('light');
    updateButtons();
  }

  function rotate() {
    if (blocked() || sel === null) return;
    const p = game.state.hand[sel]!;
    const nr = (rot + 1) % 4;
    if (mode === 'classic') {
      const cells = shapeCells(p.kind, nr);
      for (const dc of [0, -1, 1, -2, 2]) {
        const nc = Math.max(0, Math.min(COLS - shapeSize(cells).w, col + dc));
        if (fitsAt(game.grid, cells, pieceRow ?? 0, nc)) { rot = nr; col = nc; sfx.rotate(); return; }
      }
      return;
    }
    rot = nr;
    col = clampCol(col, nr);
    sfx.rotate();
  }

  function moveTo(nc: number) {
    nc = clampCol(nc);
    if (nc === col || sel === null) return;
    if (mode === 'classic' && !fitsAt(game.grid, shapeCells(game.state.hand[sel]!.kind, rot), pieceRow ?? 0, nc)) {
      // Klasik: engel varsa engele kadar kay
      const step = Math.sign(nc - col);
      let c = col;
      while (c !== nc && fitsAt(game.grid, shapeCells(game.state.hand[sel]!.kind, rot), pieceRow ?? 0, c + step)) c += step;
      if (c === col) return;
      nc = c;
    }
    col = nc;
    sfx.move();
  }

  function ghostRow(): number | null {
    if (sel === null) return null;
    const cells = shapeCells(game.state.hand[sel]!.kind, rot);
    if (mode === 'classic' && pieceRow !== null) {
      let r = pieceRow;
      while (fitsAt(game.grid, cells, r + 1, col)) r++;
      return r;
    }
    return landingRow(game.grid, cells, col);
  }

  function pieceRect(): Rect | null {
    if (sel === null) return null;
    const p = game.state.hand[sel]!;
    if (mode === 'classic' && pieceRow !== null) {
      const { w: pw, h: ph } = shapeSize(shapeCells(p.kind, rot));
      return { x: view.board.x + col * view.cell, y: view.board.y + pieceRow * view.cell, w: pw * view.cell, h: ph * view.cell };
    }
    return view.floatRect(p, rot, col);
  }

  // ---------- hamle ----------
  function doDrop() {
    if (blocked() || sel === null) return;
    const row = ghostRow();
    if (row === null) { fx.shake = 0.25; sfx.move(); haptic('light'); return; }
    const p = game.state.hand[sel]!;
    const fromFloat = mode !== 'classic';
    const res = mode === 'classic' ? game.placeAt(sel, rot, col, row) : game.drop(sel, rot, col);
    if (!res) return;
    play(res, fromFloat ? { piece: p, rot, col, row } : null);
  }

  function lockClassic() {
    if (sel === null || pieceRow === null) return;
    const res = game.placeAt(sel, rot, col, pieceRow);
    if (res) play(res, null);
  }

  function play(res: MoveResult, dropFrom: { piece: typeof drop extends null ? never : NonNullable<typeof drop>['piece']; rot: number; col: number; row: number } | null) {
    lastSlot = sel ?? 0;
    sel = null;
    pieceRow = null;
    updateButtons();
    const c = view.cell;
    const bx = () => view.board.x, by = () => view.board.y;

    if (dropFrom) {
      queue.push({
        dur: 0.15,
        start: () => { drop = { ...dropFrom, k: 0 }; },
        update: (k) => { if (drop) drop.k = k; },
        end: () => { drop = null; },
      });
    }
    queue.push({
      dur: 0.05,
      start: () => {
        displayGrid = res.placed;
        sfx.land();
        haptic('light');
        for (const [r, cc] of shapeCells(res.piece.kind, res.rot)) {
          if (r === shapeSize(shapeCells(res.piece.kind, res.rot)).h - 1)
            fx.ring(bx() + (res.col + cc + 0.5) * c, by() + (res.row + r + 1) * c, c * 0.5, 'rgba(255,240,200,0.7)');
        }
      },
    });

    const last = res.steps.length - 1;
    res.steps.forEach((st, i) => {
      queue.push({
        dur: 0.34,
        start: () => {
          displayGrid = st.before;
          flash = { rows: st.rows, k: 0 };
          for (const r of st.rows) {
            for (let x = 0; x < COLS; x++) {
              const cell = st.before[r][x];
              if (cell && cell.k !== 'rock' && cell.k !== 'animal') fx.cellBurst(bx() + x * c, by() + r * c, c, cellColor(cell));
            }
          }
          for (const a of st.rescued) fx.animalFly(a.a, bx() + a.c * c, by() + a.r * c, c);
          for (const s of st.sprouted) fx.sprout(bx() + s.c * c, by() + s.r * c, c);
          for (const s of st.sunBreaks) {
            fx.ring(bx() + (s.c + 0.5) * c, by() + (s.r + 0.5) * c, c, '#ffb300');
            fx.cellBurst(bx() + s.c * c, by() + s.r * c, c, '#ffcf5a');
          }
          for (const r of st.flowerRows) {
            fx.flowerBloom(bx(), by() + r * c, view.board.w);
            fx.text(t('fx.flower'), w / 2, by() + r * c - c * 0.6, '#ffb3d0', 30, 1.4);
          }
          if (st.rescued.length) { sfx.rescue(); fx.text(t('fx.rescued'), w / 2, by() + st.rows[0] * c - c, '#fff7b0', 24); }
          if (st.sprouted.length) { sfx.sprout(); fx.text(t('fx.sprout'), w / 2, by() + st.sprouted[0].r * c - c * 0.5, '#b9f59a', 24); }
          if (st.sunBreaks.length) sfx.sunBreak();
          if (st.flowerRows.length) sfx.flower();
          sfx.clear(st.rows.length, res.comboBefore + i);
          haptic(st.rows.length >= 2 ? 'medium' : 'light');
          if (i > 0) fx.text(`${t('fx.chain')} x${i + 1}!`, w / 2, by() + view.board.h * 0.35, '#ffd56b', 30);
          if (i === last) {
            const midY = by() + st.rows[0] * c;
            fx.text(`+${res.points}`, w / 2, midY, '#ffffff', 34, 1.3);
            if (res.comboBefore >= 1) fx.text(`${t('fx.combo')} x${Game.comboMultiplier(res.comboBefore)}`, w / 2, midY + c * 1.1, '#9be7ff', 24, 1.3);
          }
        },
        update: (k) => { if (flash) flash.k = k; },
        end: () => {
          flash = null;
          displayGrid = i < last ? res.steps[i + 1].before : res.afterClear;
        },
      });
    });

    if (res.seasonChanged) {
      queue.push({
        dur: 0.1,
        start: () => {
          seasonTarget = game.state.season;
          fx.seasonBurst(w, hgt, game.state.season);
          fx.text(t('season.' + game.state.season), w / 2, hgt * 0.4, '#fff', 40, 1.8);
          sfx.season();
          haptic('medium');
        },
      });
    }

    if (res.attack) {
      queue.push({
        dur: 0.5,
        start: () => {
          displayGrid = res.final;
          fx.shake = 0.8;
          sfx.attack();
          haptic('heavy');
          fx.text(t('fx.attack'), w / 2, by() + view.board.h * 0.5, '#ff9a9a', 30, 1.4);
        },
      });
    }
    if (res.zenRelief.length) {
      queue.push({
        dur: 0.55,
        start: () => {
          for (const r of res.zenRelief)
            for (let x = 0; x < COLS; x++) {
              const cell = res.afterClear[r][x];
              if (cell) fx.ghostCell(cell, bx() + x * c, by() + r * c, c);
            }
          displayGrid = res.final;
          fx.text(t('fx.zen'), w / 2, by() + c * 3, '#e6fff0', 22, 2);
          sfx.season();
        },
      });
    }
    queue.push({
      dur: 0,
      start: () => {
        displayGrid = game.grid;
        updateHud();
        if (res.status === 'won') setTimeout(() => endGame(true), 550);
        else if (res.status === 'lost') setTimeout(() => endGame(false), 450);
      },
    });
  }

  function doUndo() {
    if (animating() || paused) return;
    if (!game.undo()) return;
    sfx.undo();
    displayGrid = game.grid;
    sel = null;
    seasonTarget = game.state.season;
    closeOverlay();
    updateHud();
  }

  // ---------- Klasik Hız ----------
  function spawnClassic() {
    const hand = game.state.hand;
    const idx = hand[lastSlot] ? lastSlot : hand.findIndex((p) => p);
    if (idx < 0) return;
    sel = idx; rot = 0; pieceRow = 0;
    col = Math.floor((COLS - pieceW(0)) / 2);
    classicTimer = 0;
    if (!fitsAt(game.grid, shapeCells(hand[idx]!.kind, 0), 0, col)) {
      const alt = [...Array(COLS).keys()].find((c) => c <= COLS - pieceW(0) && fitsAt(game.grid, shapeCells(hand[idx]!.kind, 0), 0, c));
      if (alt === undefined) {
        game.forceLose('stuck');
        sel = null;
        endGame(false);
        return;
      }
      col = alt;
    }
    updateButtons();
  }

  // ---------- giriş ----------
  let drag: { id: number; x0: number; y0: number; col0: number; t0: number; moved: boolean } | null = null;

  canvas.addEventListener('pointerdown', (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left, y = e.clientY - rect.top;
    const slot = view.hitHand(x, y);
    if (slot !== null) { selectSlot(slot); return; }
    if (blocked() || sel === null || !view.inPlayArea(x, y)) return;
    canvas.setPointerCapture(e.pointerId);
    drag = { id: e.pointerId, x0: x, y0: y, col0: col, t0: performance.now(), moved: false };
  });
  canvas.addEventListener('pointermove', (e) => {
    if (!drag || e.pointerId !== drag.id || blocked()) return;
    const rect = canvas.getBoundingClientRect();
    const dx = e.clientX - rect.left - drag.x0, dy = e.clientY - rect.top - drag.y0;
    if (Math.abs(dx) > 8 || Math.abs(dy) > 8) drag.moved = true;
    if (Math.abs(dy) > view.cell * 0.9 && Math.abs(dy) > Math.abs(dx) * 1.3) return; // aşağı kaydırma niyeti
    moveTo(drag.col0 + Math.round(dx / view.cell));
  });
  const endDrag = (e: PointerEvent) => {
    if (!drag || e.pointerId !== drag.id) return;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left, y = e.clientY - rect.top;
    const dx = x - drag.x0, dy = y - drag.y0;
    const d = drag;
    drag = null;
    if (e.type === 'pointercancel' || blocked()) return;
    if (dy > view.cell * 1.1 && dy > Math.abs(dx) * 1.2 && performance.now() - d.t0 < 600) { doDrop(); return; }
    if (!d.moved) {
      const pr = pieceRect();
      const m = view.cell * 0.4;
      if (pr && x >= pr.x - m && x <= pr.x + pr.w + m && y >= pr.y - m && y <= pr.y + pr.h + m) rotate();
      else if (x >= view.board.x && x <= view.board.x + view.board.w) {
        moveTo(Math.floor((x - view.board.x) / view.cell - pieceW(rot) / 2 + 0.5));
      }
    }
  };
  canvas.addEventListener('pointerup', endDrag);
  canvas.addEventListener('pointercancel', endDrag);

  // ---------- döngü ----------
  function frame(now: number) {
    const dt = Math.min(0.05, (now - (lastT || now)) / 1000);
    lastT = now;
    if (!paused) {
      // zaman çizelgesi
      let guard = 0;
      do {
        if (!cur) {
          cur = queue.shift() ?? null;
          if (cur) { curT = 0; cur.start?.(); }
        }
        if (cur) {
          curT += cur.dur === 0 ? 0 : dt;
          const k = cur.dur === 0 ? 1 : Math.min(1, curT / cur.dur);
          cur.update?.(k);
          if (k >= 1) { cur.end?.(); cur = null; continue; }
        }
        break;
      } while (++guard < 20);

      if (mode === 'classic' && !animating() && !overlay && game.state.status === 'playing') {
        if (sel === null) spawnClassic();
        else {
          classicTimer += dt * 1000;
          if (classicTimer >= classicInterval(game.state.lines)) {
            classicTimer = 0;
            const cells = shapeCells(game.state.hand[sel]!.kind, rot);
            if (fitsAt(game.grid, cells, (pieceRow ?? 0) + 1, col)) pieceRow = (pieceRow ?? 0) + 1;
            else lockClassic();
          }
        }
      }
      fx.update(dt);
      seasonVis += (seasonTarget - seasonVis) * Math.min(1, dt * 1.5);
    }
    draw(now / 1000);
    raf = requestAnimationFrame(frame);
  }

  function draw(time: number) {
    const ctx = canvas.getContext('2d')!;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    drawBackground(ctx, w, hgt, dpr, seasonVis);
    ctx.save();
    if (fx.shake > 0) ctx.translate((Math.random() - 0.5) * 14 * fx.shake, (Math.random() - 0.5) * 10 * fx.shake);
    const showSel = !animating() && game.state.status === 'playing';
    view.draw(ctx, {
      grid: displayGrid,
      hand: game.state.hand,
      queue: game.state.queue,
      selected: showSel ? sel : null,
      rot, col,
      ghostRow: showSel ? ghostRow() : null,
      pieceRow: mode === 'classic' ? pieceRow : null,
      drop, flash,
      symbols: save.settings.symbols,
      t: time,
      attackWarn: game.attackSoon() && !animating(),
      dimHand: animating(),
    });
    fx.draw(ctx, save.settings.symbols);
    ctx.restore();
  }

  // ---------- katmanlar ----------
  function closeOverlay() { overlay?.remove(); overlay = null; }
  function showOverlay(panel: HTMLElement) {
    closeOverlay();
    overlay = h('div', { class: 'overlay' }, panel);
    el.append(overlay);
  }

  function showHints(keys: string[]) {
    let i = 0;
    const next = () => {
      closeOverlay();
      if (i >= keys.length) return;
      const k = keys[i++];
      overlay = h('div', { class: 'hint', onclick: () => { sfx.click(); next(); } },
        h('div', { class: 'bubble' }, t(k), h('small', {}, `${t('hint.tap')} (${i}/${keys.length})`)));
      el.append(overlay);
    };
    next();
  }

  function openPause() {
    if (overlay || game.state.status !== 'playing') return;
    sfx.click();
    paused = true;
    showOverlay(h('div', { class: 'panel' },
      h('h2', {}, t('pause.title')),
      h('button', { class: 'btn green', onclick: () => resume() }, t('pause.resume')),
      h('button', { class: 'btn', onclick: () => { finalize(); setup(); } }, t('pause.restart')),
      h('button', { class: 'btn', onclick: () => { finalize(); router.popToRoot(); } }, t('pause.menu')),
    ));
  }
  function resume() { paused = false; lastT = 0; closeOverlay(); sfx.click(); }

  /** Kalıcı kayıtlar (bir oyun için bir kez). */
  function finalize() {
    if (finalized) return;
    finalized = true;
    const s = game.state;
    if (s.movesUsed === 0) return;
    recordCollection(s.rescued, s.sprouted);
    if (mode === 'adventure' && s.status === 'won') recordLevel(level.id, game.stars(), s.score);
    if (mode === 'daily') {
      const key = dateKey();
      save.daily[key] = Math.max(save.daily[key] ?? 0, s.score);
      persist();
    }
    if (mode === 'zen' && s.score > save.zenBest) { save.zenBest = s.score; persist(); }
  }

  function endGame(won: boolean) {
    const s = game.state;
    const prevBest = mode === 'adventure' ? save.levels[level.id]?.best ?? 0 : mode === 'daily' ? save.daily[dateKey()] ?? 0 : 0;
    let classicRank = -1;
    if (won) finalize();
    if (mode === 'classic') { finalize(); classicRank = recordClassic(s.score); }
    won ? (sfx.win(), haptic('medium')) : (sfx.lose(), haptic('heavy'));

    const panel = h('div', { class: 'panel' }, h('h2', {}, won ? t('end.won') : t('end.lost')));
    if (won && mode !== 'classic') {
      const stars = game.stars();
      const row = h('div', { class: 'big-stars' }, h('span', {}, '★'), h('span', {}, '★'), h('span', {}, '★'));
      panel.append(row);
      [...row.children].forEach((sp, i) => {
        if (i < stars) setTimeout(() => { sp.classList.add('on'); sfx.star(i); haptic('light'); }, 350 + i * 380);
      });
    }
    if (!won && s.loseReason) panel.append(h('p', {}, t('end.reason.' + s.loseReason)));
    panel.append(h('div', {}, t('end.score')), h('div', { class: 'score' }, String(s.score)));
    if ((mode === 'adventure' || mode === 'daily') && s.score > prevBest && (won || mode === 'daily') && s.movesUsed > 0)
      panel.append(h('p', { style: { fontWeight: '800', color: '#c0571b' } }, t('end.newBest')));
    if (mode === 'daily') panel.append(h('p', {}, `${t('daily.best')}: ${Math.max(prevBest, s.score)}`));
    if (mode === 'classic') {
      const tb = h('table', {});
      save.classic.slice(0, 5).forEach((sc, i) => tb.append(h('tr', { class: i === classicRank ? 'me' : '' }, h('td', {}, `${i + 1}.`), h('td', {}, String(sc)))));
      panel.append(h('b', {}, t('classic.scores')), tb);
    }
    if (!won && game.canUndo()) {
      panel.append(h('p', { style: { fontSize: '14px', opacity: '.8' } }, t('end.undoHint')),
        h('button', { class: 'btn orange', onclick: () => doUndo() }, `↶ ${t('btn.undo')} (${s.undoLeft})`));
    }
    const btns = h('div', { class: 'row' });
    btns.append(h('button', { class: 'btn', onclick: () => { sfx.click(); finalize(); setup(); } }, t('end.retry')));
    if (mode === 'adventure') {
      const next = LEVELS.find((l) => l.id === level.id + 1);
      if (won && next) btns.append(h('button', { class: 'btn green', onclick: () => { sfx.click(); router.replace(playScreen(router, { mode, levelId: next.id })); } }, t('end.next')));
      btns.append(h('button', { class: 'btn', onclick: () => { sfx.click(); finalize(); router.back(); } }, t('end.map')));
    } else {
      btns.append(h('button', { class: 'btn', onclick: () => { sfx.click(); finalize(); router.popToRoot(); } }, t('end.menu')));
    }
    panel.append(btns);
    showOverlay(panel);
  }

  setup();
  // Hata ayıklama (yalnızca geliştirme sunucusunda)
  if (import.meta.env.DEV) (window as unknown as Record<string, unknown>).__bb = () => ({ game, sel, rot, col, pieceRow, animating: animating(), overlay: !!overlay, paused });

  return {
    el,
    onShow: () => {
      resize();
      window.addEventListener('resize', resize);
      cancelAnimationFrame(raf);
      lastT = 0;
      raf = requestAnimationFrame(frame);
    },
    onHide: () => cancelAnimationFrame(raf),
    destroy: () => {
      finalize();
      cancelAnimationFrame(raf);
      window.removeEventListener('resize', resize);
    },
    onBack: () => {
      // Oyundayken geri tuşu: duraklat menüsünü aç / kapat
      if (overlay?.classList.contains('hint')) { closeOverlay(); return true; }
      if (paused) { resume(); return true; }
      if (overlay) {
        // bitiş ekranında geri: haritaya/menüye dön
        finalize();
        if (mode === 'adventure') return false;
        router.popToRoot();
        return true;
      }
      openPause();
      return true;
    },
  };
}
