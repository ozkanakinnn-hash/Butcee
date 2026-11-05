import { t } from '../i18n';
import { sfx } from '../audio';
import { drawDecor, drawGarden, SLOT_POS } from '../render/garden';
import { iconCanvas, drawAnimal } from '../render/draw';
import { DECORS, persist, save, totalStars, unlockedDecors, type DecorId } from '../storage';
import { h } from './dom';
import type { Router, Screen } from './router';

function decorIcon(id: DecorId) {
  const dpr = Math.min(3, window.devicePixelRatio || 1);
  const cv = document.createElement('canvas');
  cv.width = cv.height = 48 * dpr;
  cv.style.width = cv.style.height = '48px';
  const c = cv.getContext('2d')!;
  c.scale(dpr, dpr);
  drawDecor(c, id, 24, 44, 40);
  return cv;
}

function animalIcon(k: 'hedgehog' | 'turtle' | 'ladybug') {
  const dpr = Math.min(3, window.devicePixelRatio || 1);
  const cv = document.createElement('canvas');
  cv.width = cv.height = 26 * dpr;
  cv.style.width = cv.style.height = '26px';
  const c = cv.getContext('2d')!;
  c.scale(dpr, dpr);
  drawAnimal(c, k, 13, 13, 24);
  return cv;
}

export function gardenScreen(router: Router): Screen {
  const el = h('div', { class: 'screen garden', style: { padding: '0' } });
  const canvas = h('canvas', { class: 'scene' });
  let raf = 0;
  let w = 0, hgt = 0, dpr = 1;
  let chooser: HTMLElement | null = null;

  const top = h('div', { style: { padding: 'calc(var(--sat) + 8px) calc(var(--sar) + 12px) 0 calc(var(--sal) + 12px)' } },
    h('div', { class: 'topbar' },
      h('button', { class: 'btn icon-btn', onclick: () => router.back(), 'aria-label': t('btn.back') }, '‹'),
      h('h2', { style: { color: '#fff' } }, t('garden.title')),
      h('span', { class: 'chip' }, iconCanvas('star', 22), String(totalStars())),
    ),
    h('div', { class: 'stats' },
      h('span', { class: 'chip' }, animalIcon('hedgehog'), String(save.animals.hedgehog)),
      h('span', { class: 'chip' }, animalIcon('turtle'), String(save.animals.turtle)),
      h('span', { class: 'chip' }, animalIcon('ladybug'), String(save.animals.ladybug)),
      h('span', { class: 'chip' }, iconCanvas('seeds', 24), String(save.sprouts)),
    ),
  );
  el.append(canvas, top, h('div', { class: 'tip' }, t('garden.tapSlot')));

  function resize() {
    dpr = Math.min(2, window.devicePixelRatio || 1);
    w = el.clientWidth || window.innerWidth;
    hgt = el.clientHeight || window.innerHeight;
    canvas.width = w * dpr;
    canvas.height = hgt * dpr;
  }

  function loop(now: number) {
    const ctx = canvas.getContext('2d')!;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    drawGarden(ctx, w, hgt, now / 1000, { animals: save.animals, sprouts: save.sprouts, slots: save.garden }, true);
    raf = requestAnimationFrame(loop);
  }

  function openChooser(slot: number) {
    sfx.click();
    const unlocked = new Set(unlockedDecors());
    const list = h('div', { class: 'decor-list' });
    const choose = (id: DecorId | null) => {
      // aynı dekor iki yuvada olmasın
      if (id) save.garden = save.garden.map((d) => (d === id ? null : d));
      save.garden[slot] = id;
      persist();
      sfx.select();
      close();
    };
    for (const d of DECORS) {
      const ok = unlocked.has(d.id);
      list.append(h('button', { class: 'decor-item', disabled: !ok, onclick: () => ok && choose(d.id) },
        decorIcon(d.id),
        h('span', {}, t('decor.' + d.id), h('small', {}, ok ? '' : t('garden.unlockAt', { n: d.stars }))),
      ));
    }
    list.append(h('button', { class: 'decor-item', onclick: () => choose(null) }, h('span', { style: { width: '48px', textAlign: 'center' } }, '✕'), t('garden.empty')));
    chooser = h('div', { class: 'overlay', onclick: (e: MouseEvent) => { if (e.target === chooser) close(); } },
      h('div', { class: 'panel' }, h('h2', {}, t('garden.choose')), list));
    el.append(chooser);
  }
  function close() { chooser?.remove(); chooser = null; }

  canvas.addEventListener('pointerdown', (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = (e.clientX - rect.left) / w, y = (e.clientY - rect.top) / hgt;
    let best = -1, bd = 0.12;
    SLOT_POS.forEach((p, i) => {
      const d = Math.hypot(p.x - x, (p.y - 0.03 - y) * (hgt / w));
      if (d < bd) { bd = d; best = i; }
    });
    if (best >= 0) openChooser(best);
  });

  return {
    el,
    onShow: () => { resize(); cancelAnimationFrame(raf); raf = requestAnimationFrame(loop); window.addEventListener('resize', resize); },
    onHide: () => cancelAnimationFrame(raf),
    destroy: () => { cancelAnimationFrame(raf); window.removeEventListener('resize', resize); },
    onBack: () => { if (chooser) { close(); return true; } return false; },
  };
}
