import { t } from '../i18n';
import { haptic, music, sfx } from '../audio';
import { CATALOG, HOUSE_LEVELS, HOUSE_UPGRADE, type DecorId } from '../economy';
import { drawDecor, drawGarden, drawHouse, HOUSE_POS, SLOT_POS, sceneUnit } from '../render/garden';
import { iconCanvas, drawAnimal } from '../render/draw';
import { smallCanvas } from '../render/characters';
import { Effects } from '../render/effects';
import { gardenSnapshot, persist, save, totalStars } from '../storage';
import { h, toast } from './dom';
import type { Router, Screen } from './router';
import { diamondChip, payDiamonds, priceTag } from './diamonds';

const decorIcon = (id: DecorId, size = 52) => smallCanvas(size, (c, s) => drawDecor(c, id, s / 2, s * 0.94, s * 0.78));
const houseIcon = (lvl: number, size = 120) => smallCanvas(size, (c, s) => drawHouse(c, lvl, s / 2, s * 0.95, s * 0.52));
const animalIcon = (k: 'hedgehog' | 'turtle' | 'ladybug') => smallCanvas(24, (c, s) => drawAnimal(c, k, s / 2, s / 2, s * 0.92));

export function gardenScreen(router: Router): Screen {
  const el = h('div', { class: 'screen garden', style: { padding: '0' } });
  const canvas = h('canvas', { class: 'scene' });
  const fx = new Effects();
  let raf = 0, w = 0, hgt = 0, dpr = 1, last = 0;
  let sheet: HTMLElement | null = null;
  let selected: number | null = null;
  const stats = h('div', { class: 'stats' });
  const top = h('div', { style: { position: 'relative', zIndex: '2', padding: 'calc(var(--sat) + 8px) calc(var(--sar) + 12px) 0 calc(var(--sal) + 12px)' } },
    h('div', { class: 'topbar' },
      h('button', { class: 'btn icon-btn', onclick: () => router.back(), 'aria-label': t('btn.back') }, '‹'),
      h('h2', { style: { color: '#fff' } }, t('garden.title')),
      diamondChip(() => el),
    ),
    stats,
  );
  const bar = h('div', { class: 'garden-bar' },
    h('button', { class: 'btn', onclick: () => openHouse() }, t('garden.house')),
    h('button', { class: 'btn green', onclick: () => openShop(firstEmpty()) }, t('garden.shop')),
  );
  const tip = h('div', { class: 'tip' }, t('garden.tapSlot'));
  el.append(canvas, top, tip, bar);
  setTimeout(() => tip.classList.add('fade'), 5000);

  function renderStats() {
    stats.innerHTML = '';
    stats.append(
      h('span', { class: 'chip' }, iconCanvas('star', 20), String(totalStars())),
      h('span', { class: 'chip' }, t('house.' + save.house)),
      h('span', { class: 'chip' }, animalIcon('hedgehog'), String(save.animals.hedgehog)),
      h('span', { class: 'chip' }, animalIcon('turtle'), String(save.animals.turtle)),
      h('span', { class: 'chip' }, animalIcon('ladybug'), String(save.animals.ladybug)),
      h('span', { class: 'chip' }, iconCanvas('seeds', 22), String(save.sprouts)),
    );
  }

  const firstEmpty = () => { const i = save.garden.findIndex((x) => !x); return i < 0 ? 0 : i; };

  function resize() {
    dpr = Math.min(3, window.devicePixelRatio || 1);
    w = el.clientWidth || window.innerWidth;
    hgt = el.clientHeight || window.innerHeight;
    canvas.width = w * dpr;
    canvas.height = hgt * dpr;
  }

  function loop(now: number) {
    const dt = Math.min(0.05, (now - (last || now)) / 1000);
    last = now;
    fx.update(dt);
    const ctx = canvas.getContext('2d')!;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    drawGarden(ctx, w, hgt, now / 1000, gardenSnapshot(), { slotHints: true, selected });
    fx.draw(ctx, false);
    raf = requestAnimationFrame(loop);
  }

  function celebrate(x: number, y: number) {
    for (let i = 0; i < 3; i++) fx.cellBurst(x - 20 + i * 20, y - 30, 26, ['#ffd54a', '#7ee0ff', '#ff8fb1'][i]);
    fx.flowerBloom(x - 60, y - 20, 120);
    haptic('medium');
  }

  function closeSheet() { sheet?.remove(); sheet = null; selected = null; }

  function openSheet(title: string, ...content: (Node | null)[]) {
    closeSheet();
    sheet = h('div', { class: 'sheet-wrap', onclick: (e: MouseEvent) => { if (e.target === sheet) closeSheet(); } },
      h('div', { class: 'sheet' }, h('div', { class: 'grab' }), h('h2', {}, title), ...content));
    el.append(sheet);
  }

  function openShop(slot: number) {
    sfx.click();
    selected = slot;
    const stars = totalStars();
    const list = h('div', { class: 'shop-grid' });
    const cur = save.garden[slot];
    for (const item of CATALOG) {
      const owned = save.owned.includes(item.id);
      const locked = !owned && stars < item.stars;
      const placedHere = cur === item.id;
      let action: HTMLElement;
      if (placedHere) action = h('span', { class: 'tag ok' }, '✓');
      else if (owned) action = h('button', { class: 'btn small green', onclick: () => place(slot, item.id) }, t('garden.place'));
      else if (locked) action = h('span', { class: 'tag' }, t('garden.needStars', { n: item.stars }));
      else action = h('button', { class: 'btn small orange', onclick: () => buy(slot, item.id, item.price) }, priceTag(item.price));
      list.append(h('div', { class: 'shop-item' + (locked ? ' locked' : '') + (placedHere ? ' here' : '') },
        decorIcon(item.id), h('b', {}, t('decor.' + item.id)), owned && !placedHere ? h('small', {}, t('garden.owned')) : null, action));
    }
    openSheet(t('garden.shop'), list,
      cur ? h('button', { class: 'btn', onclick: () => { save.garden[slot] = null; persist(); sfx.click(); closeSheet(); } }, t('garden.remove')) : null);
  }

  function place(slot: number, id: DecorId) {
    save.garden = save.garden.map((d) => (d === id ? null : d)); // her obje tek yuvada
    save.garden[slot] = id;
    persist();
    sfx.select();
    const p = SLOT_POS[slot];
    celebrate(p.x * w, p.y * hgt);
    closeSheet();
  }

  function buy(slot: number, id: DecorId, price: number) {
    payDiamonds(el, price, () => {
      save.owned.push(id);
      persist();
      toast(el, t('garden.bought'));
      place(slot, id);
    });
  }

  function openHouse() {
    sfx.click();
    const lvl = save.house;
    if (lvl >= HOUSE_LEVELS - 1) {
      openSheet(t('house.' + lvl), houseIcon(lvl, 150), h('p', {}, t('garden.maxHouse')));
      return;
    }
    const up = HOUSE_UPGRADE[lvl];
    const can = totalStars() >= up.stars;
    openSheet(t('garden.upgrade'),
      h('div', { class: 'house-cmp' },
        h('div', {}, houseIcon(lvl), h('small', {}, t('house.' + lvl))),
        h('span', { class: 'arrow' }, '➜'),
        h('div', {}, houseIcon(lvl + 1), h('b', {}, t('house.' + (lvl + 1)))),
      ),
      can
        ? h('button', { class: 'btn orange', onclick: () => payDiamonds(el, up.price, () => {
          save.house = lvl + 1;
          persist();
          sfx.cheer();
          toast(el, t('garden.upgraded'));
          closeSheet();
          renderStats();
          celebrate(HOUSE_POS.x * w, HOUSE_POS.y * hgt - sceneUnit(w, hgt) * 0.15);
        }) }, t('garden.upgrade'), ' ', priceTag(up.price))
        : h('p', { class: 'tag' }, t('garden.needStars', { n: up.stars })),
    );
  }

  canvas.addEventListener('pointerdown', (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left, y = e.clientY - rect.top;
    const unit = sceneUnit(w, hgt);
    // yuva
    let best = -1, bd = unit * 0.13;
    SLOT_POS.forEach((p, i) => {
      const d = Math.hypot(p.x * w - x, p.y * hgt - unit * 0.05 - y);
      if (d < bd) { bd = d; best = i; }
    });
    if (best >= 0) { openShop(best); return; }
    // ev
    const hx = HOUSE_POS.x * w, hy = HOUSE_POS.y * hgt;
    if (Math.abs(x - hx) < unit * 0.25 && y < hy + 10 && y > hy - unit * 0.4) openHouse();
  });

  return {
    el,
    onShow: () => { resize(); renderStats(); cancelAnimationFrame(raf); last = 0; raf = requestAnimationFrame(loop); window.addEventListener('resize', resize); music.play('calm'); },
    onHide: () => cancelAnimationFrame(raf),
    destroy: () => { cancelAnimationFrame(raf); window.removeEventListener('resize', resize); },
    onBack: () => { if (sheet) { closeSheet(); return true; } return false; },
  };
}
