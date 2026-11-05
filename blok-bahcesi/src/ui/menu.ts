import { t } from '../i18n';
import { sfx } from '../audio';
import { drawAnimal, drawCell, drawFlower, iconCanvas } from '../render/draw';
import type { Element } from '../game/types';
import { totalStars } from '../storage';
import { h } from './dom';
import type { Router, Screen } from './router';
import { mapScreen } from './mapScreen';
import { settingsScreen } from './settings';
import { gardenScreen } from './garden';
import { classicScreen, dailyScreen } from './modes';
import { playScreen } from './playScreen';

function logoCanvas(): HTMLCanvasElement {
  const dpr = Math.min(3, window.devicePixelRatio || 1);
  const W = 220, H = 96;
  const cv = document.createElement('canvas');
  cv.width = W * dpr; cv.height = H * dpr;
  cv.style.width = W + 'px'; cv.style.height = H + 'px';
  const ctx = cv.getContext('2d')!;
  ctx.scale(dpr, dpr);
  const s = 30;
  const cells: [number, number, Element][] = [
    [0, 2, 'leaf'], [1, 1, 'water'], [1, 2, 'leaf'], [1, 3, 'sun'], [2, 0, 'purple'], [2, 1, 'water'], [2, 2, 'red'], [2, 3, 'sun'], [2, 4, 'stone'], [2, 5, 'leaf'],
  ];
  const ox = (W - 6 * s) / 2, oy = H - 3 * s;
  for (const [r, c, el] of cells) drawCell(ctx, { k: 'block', el }, ox + c * s, oy + r * s, s, true);
  drawFlower(ctx, ox + 5.5 * s, oy + 1.1 * s, 26, '#ff8fb1');
  drawFlower(ctx, ox + 4.3 * s, oy + 1.4 * s, 20, '#fff1a8');
  drawAnimal(ctx, 'ladybug', ox + 0.5 * s, oy + 1.4 * s, 26);
  return cv;
}

export function menuScreen(router: Router): Screen {
  const el = h('div', { class: 'screen menu' });
  const go = (fn: () => void) => () => { sfx.click(); fn(); };

  function render() {
    el.innerHTML = '';
    const star = iconCanvas('star', 26);
    el.append(
      h('div', { class: 'logo' }, logoCanvas(), h('h1', {}, t('app.title'))),
      h('div', { class: 'menu-list' },
        h('button', { class: 'btn green', style: { minHeight: '64px', fontSize: '24px' }, onclick: go(() => router.push(mapScreen(router))) },
          t('menu.adventure'), h('span', { class: 'chip', style: { fontSize: '15px', color: '#fff' } }, star, String(totalStars()))),
        h('button', { class: 'btn orange', onclick: go(() => router.push(dailyScreen(router))) }, iconCanvas('seeds', 28), t('menu.daily')),
        h('div', { class: 'menu-row' },
          h('button', { class: 'btn', onclick: go(() => router.push(playScreen(router, { mode: 'zen' }))) }, t('menu.zen')),
          h('button', { class: 'btn', onclick: go(() => router.push(classicScreen(router))) }, t('menu.classic')),
        ),
        h('div', { class: 'menu-row' },
          h('button', { class: 'btn', onclick: go(() => router.push(gardenScreen(router))) }, iconCanvas('animals', 26), t('menu.garden')),
          h('button', { class: 'btn', onclick: go(() => router.push(settingsScreen(router))) }, t('menu.settings')),
        ),
      ),
    );
  }
  render();
  return { el, onShow: render };
}
