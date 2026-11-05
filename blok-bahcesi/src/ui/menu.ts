import { t, getLang } from '../i18n';
import { music, sfx } from '../audio';
import { drawCell, drawFlower, drawAnimal, iconCanvas } from '../render/draw';
import { drawGardener, medalIcon, type GardenerMood } from '../render/characters';
import type { Element } from '../game/types';
import { claimMilestones, totalStars } from '../storage';
import { MILESTONES } from '../economy';
import { h } from './dom';
import type { Router, Screen } from './router';
import { mapScreen } from './mapScreen';
import { settingsScreen } from './settings';
import { gardenScreen } from './garden';
import { classicScreen, dailyScreen } from './modes';
import { playScreen } from './playScreen';
import { leaderboardScreen } from './leaderboard';
import { diamondChip, openEarnDialog, rewardPopup } from './diamonds';

const LINES_TR = ['Hoş geldin! Bahçemizi birlikte güzelleştirelim.', 'Mavi bloklar tohumlarımı sular!', 'Turuncu güneş yabani otları kurutur.', 'Elmaslarla evimizi büyütebiliriz!', 'Bazen başım belaya girer… beni kurtarır mısın?'];
const LINES_EN = ['Welcome! Let\'s make our garden beautiful.', 'Blue blocks water my seeds!', 'Orange sun dries up the weeds.', 'Gems can grow our house!', 'Sometimes I get into trouble… will you rescue me?'];

/** Menünün kahraman sahnesi: bloklar, çiçekler ve el sallayan Bahçıvan Dede. */
function heroCanvas(onTap: () => void) {
  const dpr = Math.min(3, window.devicePixelRatio || 1);
  const W = 340, H = 190;
  const cv = document.createElement('canvas');
  cv.width = W * dpr; cv.height = H * dpr;
  cv.style.width = W + 'px'; cv.style.height = H + 'px';
  cv.style.maxWidth = '100%';
  const ctx = cv.getContext('2d')!;
  let mood: GardenerMood = 'happy';
  let moodUntil = 0;
  let raf = 0;
  const cells: [number, number, Element][] = [
    [0, 2, 'leaf'], [1, 1, 'water'], [1, 2, 'leaf'], [1, 3, 'sun'], [2, 0, 'purple'], [2, 1, 'water'], [2, 2, 'red'], [2, 3, 'sun'], [2, 4, 'stone'], [2, 5, 'leaf'],
  ];
  const frame = (now: number) => {
    const tt = now / 1000;
    if (mood !== 'happy' && now > moodUntil) mood = 'happy';
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.clearRect(0, 0, W, H);
    // çimen tümseği
    ctx.fillStyle = 'rgba(95,170,70,0.9)';
    ctx.beginPath(); ctx.ellipse(W * 0.5, H - 6, W * 0.48, 18, 0, Math.PI, 0); ctx.fill();
    const s = 30, ox = 150, oy = H - 3 * s - 10;
    for (const [r, c, el] of cells) drawCell(ctx, { k: 'block', el }, ox + c * s, oy + r * s + Math.sin(tt * 2 + c) * 1.5, s, true);
    drawFlower(ctx, ox + 5.5 * s, oy + 1.2 * s, 26, '#ff8fb1');
    drawFlower(ctx, ox + 4.3 * s, oy + 1.5 * s, 20, '#fff1a8');
    drawAnimal(ctx, 'ladybug', ox + 0.6 * s, oy + 1.5 * s + Math.abs(Math.sin(tt * 3)) * -4, 24, tt);
    drawGardener(ctx, 78, H - 8, 150, mood, tt, mood === 'happy' && Math.sin(tt * 0.9) > 0.2);
    raf = requestAnimationFrame(frame);
  };
  cv.addEventListener('pointerdown', () => { mood = 'cheer'; moodUntil = performance.now() + 1400; sfx.cheer(); onTap(); });
  return { el: cv, start: () => { cancelAnimationFrame(raf); raf = requestAnimationFrame(frame); }, stop: () => cancelAnimationFrame(raf) };
}

export function menuScreen(router: Router): Screen {
  const el = h('div', { class: 'screen menu' });
  const go = (fn: () => void) => () => { sfx.click(); fn(); };
  let hero: ReturnType<typeof heroCanvas> | null = null;
  let lineIdx = 0;

  function render() {
    el.innerHTML = '';
    const bubble = h('div', { class: 'speech' }, (getLang() === 'tr' ? LINES_TR : LINES_EN)[lineIdx]);
    hero?.stop();
    hero = heroCanvas(() => {
      lineIdx = (lineIdx + 1) % LINES_TR.length;
      bubble.textContent = (getLang() === 'tr' ? LINES_TR : LINES_EN)[lineIdx];
      bubble.classList.remove('pop'); void bubble.offsetWidth; bubble.classList.add('pop');
    });
    const star = iconCanvas('star', 22);
    el.append(
      h('div', { class: 'menu-top' }, diamondChip(() => el)),
      h('div', { class: 'logo' }, h('div', { class: 'hero' }, hero.el, bubble), h('h1', {}, t('app.title'))),
      h('div', { class: 'menu-list' },
        h('button', { class: 'btn green', style: { minHeight: '62px', fontSize: '24px' }, onclick: go(() => router.push(mapScreen(router))) },
          t('menu.adventure'), h('span', { class: 'chip', style: { fontSize: '15px', color: '#fff' } }, star, String(totalStars()))),
        h('div', { class: 'menu-row' },
          h('button', { class: 'btn orange', onclick: go(() => router.push(dailyScreen(router))) }, iconCanvas('seeds', 24), t('menu.daily')),
          h('button', { class: 'btn', onclick: go(() => router.push(gardenScreen(router))) }, iconCanvas('animals', 24), t('menu.garden')),
        ),
        h('div', { class: 'menu-row' },
          h('button', { class: 'btn', onclick: go(() => router.push(playScreen(router, { mode: 'zen' }))) }, t('menu.zen')),
          h('button', { class: 'btn', onclick: go(() => router.push(classicScreen(router))) }, t('menu.classic')),
        ),
        h('div', { class: 'menu-row' },
          h('button', { class: 'btn', onclick: go(() => router.push(leaderboardScreen(router))) }, medalIcon(0, 26), t('menu.leaderboard')),
          h('button', { class: 'btn', onclick: () => openEarnDialog(el) }, t('menu.earn')),
        ),
        h('button', { class: 'btn small', onclick: go(() => router.push(settingsScreen(router))) }, '⚙ ', t('menu.settings')),
      ),
    );
    hero.start();
  }

  /** Kritik eşik ödülleri (ekran gösterildiğinde, çizimden sonra) */
  function claimRewards() {
    const got = claimMilestones();
    if (got.length) {
      rewardPopup(el, got.map((g) => {
        const m = MILESTONES.find((x) => x.key === g.key)!;
        return { label: m.kind === 'stars' ? t('dia.stars', { n: m.at }) : t('dia.levels', { n: m.at }), amount: g.reward };
      }), t('dia.milestone'));
    }
  }
  return { el, onShow: () => { render(); claimRewards(); music.play('calm'); }, onHide: () => hero?.stop() };
}
