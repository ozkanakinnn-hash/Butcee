import { t } from '../i18n';
import { sfx } from '../audio';
import { medalIcon } from '../render/characters';
import { drawGarden } from '../render/garden';
import { iconCanvas, drawAnimal } from '../render/draw';
import type { GardenSnapshot } from '../economy';
import { demoLeaderboard, gardenScore, loadLeaderboard, pgStatus, signIn, submitGardenScore, type LbEntry } from '../playgames';
import { gardenSnapshot, save } from '../storage';
import { h } from './dom';
import type { Router, Screen } from './router';
import { diamondChip } from './diamonds';

const fmt = (n: number) => n.toLocaleString('tr-TR');

export function leaderboardScreen(router: Router): Screen {
  const el = h('div', { class: 'screen' });
  const body = h('div', { class: 'scroll' });
  el.append(
    h('div', { class: 'topbar' },
      h('button', { class: 'btn icon-btn', onclick: () => router.back(), 'aria-label': t('btn.back') }, '‹'),
      h('h2', {}, t('lb.title')),
      diamondChip(() => el),
    ),
    body,
  );

  function podium(top: LbEntry[]) {
    const order = [1, 0, 2]; // gümüş, altın, bronz
    return h('div', { class: 'podium' }, ...order.map((i) => {
      const e = top[i];
      if (!e) return h('div', { class: 'pod empty' });
      return h('button', { class: `pod p${i + 1}`, onclick: () => visit(e) },
        medalIcon(i, i === 0 ? 58 : 46),
        h('b', {}, e.name),
        h('small', {}, fmt(e.score)),
        h('div', { class: 'step' }, String(i + 1)),
        e.garden ? h('span', { class: 'visit' }, t('lb.visit')) : null,
      );
    }));
  }

  function list(top: LbEntry[], mine: { rank: number; score: number } | null) {
    const ul = h('div', { class: 'lb-list' });
    top.forEach((e) => {
      const medal = e.rank <= 3 ? medalIcon(e.rank - 1, 32) : h('span', { class: 'rank' }, `#${e.rank}`);
      ul.append(h('button', {
        class: 'lb-row' + (e.me ? ' me' : '') + (e.rank <= 3 ? ' top' : ''),
        onclick: () => { if (e.rank <= 3) visit(e); },
      }, medal, h('span', { class: 'name' }, e.me ? `${e.name} (${t('lb.you')})` : e.name), h('b', {}, fmt(e.score)),
        e.rank <= 3 && e.garden ? h('span', { class: 'go' }, '›') : null));
    });
    if (mine && !top.some((e) => e.me)) {
      ul.append(h('div', { class: 'lb-sep' }, '⋯'),
        h('div', { class: 'lb-row me' }, h('span', { class: 'rank' }, `#${mine.rank}`), h('span', { class: 'name' }, `${save.player?.name ?? ''} (${t('lb.you')})`), h('b', {}, fmt(mine.score))));
    }
    return ul;
  }

  function myCard() {
    return h('div', { class: 'card info-card' },
      h('h3', {}, t('lb.score')),
      h('div', { style: { fontSize: '30px', fontWeight: '900', color: '#3d6e1f' } }, fmt(gardenScore())),
      h('button', { class: 'btn', onclick: () => visit({ rank: 0, name: t('lb.you'), score: gardenScore(), playerId: 'me', garden: gardenSnapshot() }) }, t('lb.visit')),
    );
  }

  async function render() {
    body.innerHTML = '';
    const stack = h('div', { class: 'stack' });
    body.append(stack);
    const st = await pgStatus();
    if (st === 'web') {
      // Tarayıcıda gerçek sıralama yok: açıkça etiketlenmiş örnek veriyle arayüz önizlemesi
      const demo = demoLeaderboard();
      stack.append(h('div', { class: 'demo-flag' }, t('lb.demo')), h('p', { class: 'note' }, t('lb.web')), podium(demo), list(demo, null), myCard());
      return;
    }
    if (st === 'not_configured') { stack.append(h('p', { class: 'note' }, t('lb.offline')), myCard()); return; }
    if (st === 'signed_out') {
      stack.append(h('p', { class: 'note' }, t('lb.medals')),
        h('button', { class: 'btn green', onclick: async () => { sfx.click(); if (await signIn()) { await submitGardenScore(); render(); } } }, t('lb.signin')), myCard());
      return;
    }
    stack.append(h('p', { class: 'note' }, t('lb.loading')));
    try {
      await submitGardenScore();
      const { top, mine } = await loadLeaderboard(25);
      stack.innerHTML = '';
      stack.append(h('p', { class: 'note' }, t('lb.signedAs', { name: save.player?.name ?? '' })), podium(top), list(top, mine));
    } catch {
      stack.innerHTML = '';
      stack.append(h('p', { class: 'note' }, t('lb.error')), h('button', { class: 'btn', onclick: () => render() }, t('lb.retry')), myCard());
    }
  }

  function visit(e: LbEntry) {
    if (!e.garden) return;
    sfx.click();
    router.push(visitGardenScreen(router, e.name, e.garden, e.rank));
  }

  void render();
  return { el };
}

/** Başka bir oyuncunun bahçesi (salt okunur). */
export function visitGardenScreen(router: Router, name: string, g: GardenSnapshot, rank: number): Screen {
  const el = h('div', { class: 'screen garden', style: { padding: '0' } });
  const canvas = h('canvas', { class: 'scene' });
  let raf = 0, w = 0, hh = 0, dpr = 1;
  const animalIcon = (k: 'hedgehog' | 'turtle' | 'ladybug') => {
    const c = document.createElement('canvas'); const d = Math.min(3, devicePixelRatio || 1);
    c.width = c.height = 24 * d; c.style.width = c.style.height = '24px';
    const x = c.getContext('2d')!; x.scale(d, d); drawAnimal(x, k, 12, 12, 22); return c;
  };
  el.append(canvas, h('div', { style: { position: 'relative', zIndex: '2', padding: 'calc(var(--sat) + 8px) calc(var(--sar) + 12px) 0 calc(var(--sal) + 12px)' } },
    h('div', { class: 'topbar' },
      h('button', { class: 'btn icon-btn', onclick: () => router.back(), 'aria-label': t('btn.back') }, '‹'),
      rank >= 1 && rank <= 3 ? medalIcon(rank - 1, 40) : null,
      h('h2', { style: { color: '#fff' } }, t('lb.gardenOf', { name })),
    ),
    h('div', { class: 'stats' },
      h('span', { class: 'chip' }, t('house.' + g.house)),
      h('span', { class: 'chip' }, animalIcon('hedgehog'), String(g.animals.hedgehog)),
      h('span', { class: 'chip' }, animalIcon('turtle'), String(g.animals.turtle)),
      h('span', { class: 'chip' }, animalIcon('ladybug'), String(g.animals.ladybug)),
      h('span', { class: 'chip' }, iconCanvas('seeds', 22), String(g.sprouts)),
    )));
  const resize = () => { dpr = Math.min(2, devicePixelRatio || 1); w = el.clientWidth; hh = el.clientHeight; canvas.width = w * dpr; canvas.height = hh * dpr; };
  const loop = (now: number) => {
    const ctx = canvas.getContext('2d')!;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    drawGarden(ctx, w, hh, now / 1000, g, { gardener: false });
    raf = requestAnimationFrame(loop);
  };
  return {
    el,
    onShow: () => { resize(); cancelAnimationFrame(raf); raf = requestAnimationFrame(loop); addEventListener('resize', resize); },
    onHide: () => cancelAnimationFrame(raf),
    destroy: () => { cancelAnimationFrame(raf); removeEventListener('resize', resize); },
  };
}
