import { t } from '../i18n';
import { haptic, sfx } from '../audio';
import { showRewardedAd } from '../ads';
import { REWARDS } from '../economy';
import { dateKey } from '../game/levelgen';
import { diamondIcon, gardenerIcon } from '../render/characters';
import { adsLeftToday, onDiamonds, recordAd, save, spendDiamonds } from '../storage';
import { h, toast } from './dom';

/** Canlı güncellenen elmas sayacı; dokununca "Elmas Kazan" açılır. */
export function diamondChip(parent: () => HTMLElement): HTMLElement {
  const num = h('span', {}, String(save.diamonds));
  const el = h('button', { class: 'chip gem-chip', 'aria-label': t('dia.title'), onclick: () => openEarnDialog(parent()) },
    diamondIcon(22), num, h('span', { class: 'plus' }, '+'));
  let last = save.diamonds;
  const off = onDiamonds(() => {
    num.textContent = String(save.diamonds);
    if (save.diamonds > last) { el.classList.remove('bump'); void el.offsetWidth; el.classList.add('bump'); }
    last = save.diamonds;
  });
  // DOM'dan çıkınca dinleyiciyi bırak
  const mo = new MutationObserver(() => { if (!el.isConnected) { off(); mo.disconnect(); } });
  requestAnimationFrame(() => { if (el.parentElement) mo.observe(document.body, { childList: true, subtree: true }); });
  return el;
}

/** Ödüllü reklamı oynatır; tarayıcıda 5 sn'lik simülasyon gösterir. */
export async function watchAdForDiamonds(parent: HTMLElement): Promise<boolean> {
  const today = dateKey();
  if (adsLeftToday(today) <= 0) { toast(parent, t('ad.none')); return false; }
  sfx.whoosh();
  const res = await showRewardedAd();
  if (res === 'simulate') {
    const ok = await simulateAd(parent);
    if (!ok) return false;
  } else if (res !== 'rewarded') {
    toast(parent, t('ad.failed'));
    return false;
  }
  recordAd(today);
  sfx.gem();
  haptic('medium');
  rewardPopup(parent, [{ label: t('menu.earn'), amount: REWARDS.ad }]);
  return true;
}

function simulateAd(parent: HTMLElement): Promise<boolean> {
  return new Promise((resolve) => {
    let n = 5;
    const count = h('div', { class: 'ad-count' }, String(n));
    const ov = h('div', { class: 'overlay ad-sim' },
      h('div', { class: 'panel' }, h('h2', {}, t('ad.sim')), gardenerIcon(90, 'cheer'), count,
        h('button', { class: 'btn', onclick: () => { clearInterval(iv); ov.remove(); resolve(false); } }, t('btn.cancel'))));
    parent.append(ov);
    const iv = setInterval(() => {
      n--;
      count.textContent = String(n);
      if (n <= 0) { clearInterval(iv); ov.remove(); resolve(true); }
    }, 1000);
  });
}

export function rewardPopup(parent: HTMLElement, items: { label: string; amount: number }[], title = t('dia.reward')) {
  const total = items.reduce((a, i) => a + i.amount, 0);
  if (total <= 0) return;
  const ov = h('div', { class: 'overlay', onclick: (e: MouseEvent) => { if (e.target === ov) ov.remove(); } },
    h('div', { class: 'panel reward' },
      h('h2', {}, title),
      h('div', { class: 'gem-burst' }, diamondIcon(84)),
      h('div', { class: 'score' }, `+${total}`),
      ...items.map((i) => h('p', {}, `${i.label}: +${i.amount}`)),
      h('button', { class: 'btn green', onclick: () => { sfx.click(); ov.remove(); } }, t('btn.ok')),
    ));
  parent.append(ov);
  sfx.gem();
}

export function openEarnDialog(parent: HTMLElement) {
  sfx.click();
  const left = adsLeftToday(dateKey());
  const ov = h('div', { class: 'overlay', onclick: (e: MouseEvent) => { if (e.target === ov) ov.remove(); } });
  const adBtn = h('button', { class: 'btn orange', disabled: left <= 0, onclick: async () => { ov.remove(); await watchAdForDiamonds(parent); } },
    '▶ ', t('ad.watch'), h('span', { class: 'chip', style: { fontSize: '14px' } }, diamondIcon(18), `+${REWARDS.ad}`));
  ov.append(h('div', { class: 'panel' },
    h('h2', {}, t('earn.title')),
    h('div', { class: 'gem-total' }, diamondIcon(40), String(save.diamonds)),
    h('p', {}, t('ad.desc', { n: REWARDS.adsPerDay })),
    adBtn,
    h('small', {}, left > 0 ? t('ad.left', { n: left }) : t('ad.none')),
    h('div', { class: 'howto' },
      h('b', {}, t('earn.how')),
      h('ul', {},
        h('li', {}, t('earn.h1', { n: REWARDS.rescue })),
        h('li', {}, t('earn.h2', { n: REWARDS.boss })),
        h('li', {}, t('earn.h3')),
        h('li', {}, t('earn.h4', { a: REWARDS.firstClear, b: REWARDS.threeStars })),
      ),
      h('b', {}, t('earn.spend')),
      h('ul', {}, h('li', {}, t('earn.s1')), h('li', {}, t('earn.s2'))),
    ),
    h('button', { class: 'btn', onclick: () => ov.remove() }, t('btn.ok')),
  ));
  parent.append(ov);
}

/** Elmas yeterliyse harcar ve onPay çağırır; yetmiyorsa kazanma penceresini açar. */
export function payDiamonds(parent: HTMLElement, cost: number, onPay: () => void): boolean {
  if (spendDiamonds(cost)) { sfx.buy(); haptic('light'); onPay(); return true; }
  toast(parent, t('dia.notEnough'));
  openEarnDialog(parent);
  return false;
}

export function priceTag(cost: number) {
  return h('span', { class: 'price' }, diamondIcon(18), String(cost));
}
