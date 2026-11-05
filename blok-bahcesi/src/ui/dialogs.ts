import { t } from '../i18n';
import { h } from './dom';

export function confirmDialog(parent: HTMLElement, text: string, onYes: () => void) {
  const ov = h('div', { class: 'overlay' },
    h('div', { class: 'panel' },
      h('p', { style: { fontSize: '18px', fontWeight: '700' } }, text),
      h('div', { class: 'row' },
        h('button', { class: 'btn', onclick: () => ov.remove() }, t('btn.cancel')),
        h('button', { class: 'btn orange', onclick: () => { ov.remove(); onYes(); } }, t('btn.ok')),
      ),
    ),
  );
  parent.append(ov);
  return ov;
}
