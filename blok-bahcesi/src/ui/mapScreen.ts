import { t } from '../i18n';
import { music, sfx } from '../audio';
import { LEVELS, RESCUE_LEVELS, RESCUE_AFTER } from '../levels';
import { gardenerIcon } from '../render/characters';
import { diamondChip } from './diamonds';
import { highestUnlocked, save, totalStars } from '../storage';
import { drawCloud, iconCanvas } from '../render/draw';
import { h } from './dom';
import type { Router, Screen } from './router';
import { playScreen } from './playScreen';

const SPACING = 118;

export function mapScreen(router: Router): Screen {
  const el = h('div', { class: 'screen' });
  let scroller: HTMLElement;

  function render() {
    el.innerHTML = '';
    const unlocked = highestUnlocked();
    const n = LEVELS.length;
    const height = n * SPACING + 150;
    const wrap = h('div', { class: 'map-wrap', style: { height: height + 'px' } });
    // Bölüm 1 en altta; yol yukarı kıvrılarak çıkar.
    const pos = LEVELS.map((_, i) => ({ x: 50 + Math.sin(i * 0.9) * 28, y: height - 100 - i * SPACING }));
    const ns = 'http://www.w3.org/2000/svg';
    const svg = document.createElementNS(ns, 'svg');
    svg.setAttribute('class', 'path');
    svg.setAttribute('viewBox', `0 0 100 ${height}`);
    svg.setAttribute('preserveAspectRatio', 'none');
    svg.setAttribute('width', '100%');
    svg.setAttribute('height', String(height));
    let d = `M ${pos[0].x} ${pos[0].y}`;
    for (let i = 1; i < n; i++) {
      const a = pos[i - 1], b = pos[i];
      d += ` C ${a.x} ${a.y - SPACING / 2}, ${b.x} ${b.y + SPACING / 2}, ${b.x} ${b.y}`;
    }
    for (const [stroke, width, dash] of [['#4a2c19', 16, ''], ['#e6c48f', 10, ''], ['#c99a62', 3, '2 5']] as const) {
      const p = document.createElementNS(ns, 'path');
      p.setAttribute('d', d);
      p.setAttribute('fill', 'none');
      p.setAttribute('stroke', stroke);
      p.setAttribute('stroke-width', String(width));
      p.setAttribute('vector-effect', 'non-scaling-stroke');
      p.setAttribute('stroke-linecap', 'round');
      if (dash) p.setAttribute('stroke-dasharray', dash);
      svg.append(p);
    }
    wrap.append(svg);
    LEVELS.forEach((lvl, i) => {
      const locked = lvl.id > unlocked;
      const boss = lvl.goal.type === 'boss';
      const stars = save.levels[lvl.id]?.stars ?? 0;
      const node = h('button', {
        class: `node${boss ? ' boss' : ''}${locked ? ' locked' : ''}${lvl.id === unlocked ? ' current' : ''}`,
        style: { left: pos[i].x + '%', top: pos[i].y + 'px' },
        'aria-label': `${t('map.level')} ${lvl.id}`,
        onclick: () => {
          if (locked) { sfx.lose(); return; }
          sfx.click();
          router.push(playScreen(router, { mode: 'adventure', levelId: lvl.id }));
        },
      }, locked ? '' : String(lvl.id));
      if (locked) node.append(lockIcon());
      if (boss && !locked) {
        const cv = document.createElement('canvas');
        const dpr = Math.min(3, window.devicePixelRatio || 1);
        cv.width = cv.height = 40 * dpr;
        Object.assign(cv.style, { width: '40px', height: '40px', top: '-30px', left: '12px' });
        const c = cv.getContext('2d')!;
        c.scale(dpr, dpr);
        drawCloud(c, 20, 18, 34);
        node.append(cv);
      }
      if (!locked) {
        const st = h('div', { class: 'stars' });
        for (let k = 0; k < 3; k++) st.append(h('span', { class: k < stars ? 'on' : '' }, '★'));
        node.append(st);
      }
      wrap.append(node);
    });
    // Bölüm (chapter) tabelaları
    for (let c = 0; c * 10 < n; c++) {
      const p = pos[c * 10];
      wrap.append(h('div', { class: 'chapter', style: { top: (p.y + 30) + 'px', left: p.x > 50 ? '4%' : 'auto', right: p.x > 50 ? 'auto' : '4%' } },
        h('small', {}, `${c * 10 + 1}–${c * 10 + 10}`), t('map.chapter.' + (c + 1))));
    }
    // Bahçıvanı Kurtar düğümleri: ilgili bölümün yanında, yola dal ile bağlı
    for (const r of RESCUE_LEVELS) {
      const after = RESCUE_AFTER[r.id];
      const i = after - 1;
      if (!pos[i]) continue;
      const side = pos[i].x > 50 ? -1 : 1;
      const x = Math.max(12, Math.min(88, pos[i].x + side * 34)), y = pos[i].y - SPACING * 0.45;
      const open = !!save.levels[after]?.stars;
      const done = !!save.rescues[r.id];
      const branch = document.createElementNS(ns, 'path');
      branch.setAttribute('d', `M ${pos[i].x} ${pos[i].y} Q ${(pos[i].x + x) / 2} ${pos[i].y}, ${x} ${y}`);
      branch.setAttribute('fill', 'none'); branch.setAttribute('stroke', open ? '#7fd3f7' : '#8a7a68');
      branch.setAttribute('stroke-width', '6'); branch.setAttribute('stroke-dasharray', '2 8'); branch.setAttribute('stroke-linecap', 'round');
      branch.setAttribute('vector-effect', 'non-scaling-stroke');
      svg.append(branch);
      wrap.append(h('button', {
        class: `node rescue${open ? '' : ' locked'}${open && !done ? ' current' : ''}${done ? ' done' : ''}`,
        style: { left: x + '%', top: y + 'px' },
        'aria-label': t('goal.rescue'),
        onclick: () => {
          if (!open) { sfx.lose(); return; }
          sfx.click();
          router.push(playScreen(router, { mode: 'adventure', levelId: r.id }));
        },
      }, gardenerIcon(54, done ? 'cheer' : open ? 'scared' : 'happy'),
        h('span', { class: 'rescue-label' }, done ? '✓' : t('rescue.node'))));
    }
    scroller = h('div', { class: 'scroll' }, wrap);
    el.append(
      h('div', { class: 'topbar' },
        h('button', { class: 'btn icon-btn', onclick: () => router.back(), 'aria-label': t('btn.back') }, '‹'),
        h('h2', {}, t('map.title')),
        h('span', { class: 'chip' }, iconCanvas('star', 22), `${totalStars()}`),
        diamondChip(() => el),
      ),
      scroller,
    );
    // Açık olan son bölüme kaydır
    requestAnimationFrame(() => {
      const idx = Math.min(unlocked, n) - 1;
      scroller.scrollTop = pos[idx].y - scroller.clientHeight / 2;
    });
  }

  function lockIcon() {
    const ns = 'http://www.w3.org/2000/svg';
    const s = document.createElementNS(ns, 'svg');
    s.setAttribute('viewBox', '0 0 24 24');
    s.setAttribute('width', '28');
    s.setAttribute('height', '28');
    s.innerHTML = '<rect x="5" y="10" width="14" height="11" rx="2" fill="#d8cbb8"/><path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="#d8cbb8" stroke-width="2.5" fill="none"/>';
    return s;
  }

  render();
  return { el, onShow: () => { render(); music.play('calm'); } };
}
