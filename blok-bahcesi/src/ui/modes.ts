import { t, getLang } from '../i18n';
import { sfx } from '../audio';
import { dailySeed, dateKey, generateDaily } from '../game/levelgen';
import { iconCanvas } from '../render/draw';
import { save } from '../storage';
import { h } from './dom';
import type { Router, Screen } from './router';
import { playScreen } from './playScreen';

function header(router: Router, title: string) {
  return h('div', { class: 'topbar' },
    h('button', { class: 'btn icon-btn', onclick: () => router.back(), 'aria-label': t('btn.back') }, '‹'),
    h('h2', {}, title),
  );
}

export function goalText(type: string, count?: number | null): string {
  const base = t('goal.' + type);
  return count ? `${base}: ${count}` : base;
}

export function dailyScreen(router: Router): Screen {
  const el = h('div', { class: 'screen' });
  function render() {
    el.innerHTML = '';
    const key = dateKey();
    const lvl = generateDaily(dailySeed(key));
    const best = save.daily[key] ?? 0;
    const date = new Date().toLocaleDateString(getLang() === 'tr' ? 'tr-TR' : 'en-US', { day: 'numeric', month: 'long', year: 'numeric' });
    const goalCount = lvl.goal.type === 'animals' ? null : lvl.goal.count;
    el.append(
      header(router, t('daily.title')),
      h('div', { class: 'stack', style: { marginTop: '20px' } },
        h('div', { class: 'card info-card' },
          h('h3', {}, date),
          h('div', { class: 'goal' }, iconCanvas(lvl.goal.type as 'lines', 30), goalText(lvl.goal.type, goalCount)),
          h('div', {}, `${t('hud.moves')}: ${lvl.moves}`),
          h('div', { style: { fontWeight: '800', fontSize: '20px' } }, `${t('daily.best')}: ${best}`),
          h('button', { class: 'btn green', onclick: () => { sfx.click(); router.push(playScreen(router, { mode: 'daily' })); } }, t('btn.play')),
        ),
      ),
    );
  }
  render();
  return { el, onShow: render };
}

export function classicScreen(router: Router): Screen {
  const el = h('div', { class: 'screen' });
  function render() {
    el.innerHTML = '';
    const rows = save.classic.length
      ? h('table', {}, ...save.classic.map((s, i) => h('tr', {}, h('td', {}, `${i + 1}.`), h('td', {}, String(s)))))
      : h('p', {}, t('classic.none'));
    el.append(
      header(router, t('menu.classic')),
      h('div', { class: 'stack', style: { marginTop: '20px' } },
        h('div', { class: 'card info-card' },
          h('h3', {}, t('classic.scores')),
          h('div', { class: 'panel', style: { boxShadow: 'none', background: 'transparent', padding: '0' } }, rows),
          h('button', { class: 'btn green', onclick: () => { sfx.click(); router.push(playScreen(router, { mode: 'classic' })); } }, t('btn.play')),
        ),
      ),
    );
  }
  render();
  return { el, onShow: render };
}
