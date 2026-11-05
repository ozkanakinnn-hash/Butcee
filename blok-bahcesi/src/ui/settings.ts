import { getLang, setLang, t, type Lang } from '../i18n';
import { sfx } from '../audio';
import { clearSpriteCache } from '../render/draw';
import { persist, resetProgress, save } from '../storage';
import { h, toast } from './dom';
import type { Router, Screen } from './router';
import { confirmDialog } from './dialogs';

export function settingsScreen(router: Router): Screen {
  const el = h('div', { class: 'screen' });

  function toggle(key: 'sound' | 'haptics' | 'symbols') {
    const b = h('button', { class: 'toggle' + (save.settings[key] ? ' on' : ''), role: 'switch', 'aria-checked': String(save.settings[key]) });
    b.addEventListener('click', () => {
      save.settings[key] = !save.settings[key];
      persist();
      b.classList.toggle('on', save.settings[key]);
      b.setAttribute('aria-checked', String(save.settings[key]));
      if (key === 'symbols') clearSpriteCache(Math.min(3, window.devicePixelRatio || 1));
      sfx.click();
    });
    return b;
  }

  function render() {
    el.innerHTML = '';
    const langSeg = h('div', { class: 'seg' });
    (['tr', 'en'] as Lang[]).forEach((l) => {
      langSeg.append(h('button', {
        class: getLang() === l ? 'on' : '',
        onclick: () => { save.settings.lang = l; persist(); setLang(l); sfx.click(); render(); },
      }, l === 'tr' ? 'Türkçe' : 'English'));
    });
    el.append(
      h('div', { class: 'topbar' },
        h('button', { class: 'btn icon-btn', onclick: () => router.back(), 'aria-label': t('btn.back') }, '‹'),
        h('h2', {}, t('settings.title')),
      ),
      h('div', { class: 'scroll' },
        h('div', { class: 'stack' },
          h('div', { class: 'card' },
            h('div', { class: 'setting' }, t('settings.sound'), toggle('sound')),
            h('div', { class: 'setting' }, t('settings.haptics'), toggle('haptics')),
            h('div', { class: 'setting' }, t('settings.symbols'), toggle('symbols')),
            h('div', { class: 'setting' }, t('settings.language'), langSeg),
          ),
          h('div', { class: 'card' },
            h('button', {
              class: 'setting danger', style: { width: '100%', background: 'transparent', fontFamily: 'inherit' },
              onclick: () => confirmDialog(el, t('settings.resetConfirm'), () => { resetProgress(); toast(el, t('settings.resetDone')); }),
            }, t('settings.reset'), '⟲'),
          ),
          h('p', { style: { textAlign: 'center', opacity: '.6', fontSize: '13px' } }, 'Blok Bahçesi v0.1.0'),
        ),
      ),
    );
  }
  render();
  return { el };
}
