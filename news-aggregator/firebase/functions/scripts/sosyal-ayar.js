'use strict';

/**
 * app_meta/social belgesinde izin verilen ayarları değiştirir (Firestore Console'a girmeden).
 *   node scripts/sosyal-ayar.js "make_daily_cap=7, akis_dry_run=false"
 * Yalnızca aşağıdaki anahtarlar yazılabilir; webhook adresi, kanal gibi alanlara dokunulmaz.
 */

const { getFirestore } = require('firebase-admin/firestore');
const { initAdmin } = require('./init-admin');

const ALLOWED = {
  make_daily_cap: 'number', make_gap_min: 'number', breaking_gap_min: 'number', gundem_min_sources: 'number',
  send_akis: 'boolean', akis_dry_run: 'boolean', akis_bulten_birlikte: 'boolean',
  send_breaking: 'boolean', send_gundem: 'boolean', send_bulletin: 'boolean', test_ping: 'boolean',
};

function parse(input) {
  const out = {};
  for (const part of String(input || '').split(',').map((s) => s.trim()).filter(Boolean)) {
    const [k, v] = part.split('=').map((s) => s.trim());
    const type = ALLOWED[k];
    if (!type) throw new Error(`izin verilmeyen ayar: ${k}`);
    if (type === 'number') {
      const n = Number(v);
      if (!Number.isFinite(n) || n < 0 || n > 1000) throw new Error(`${k} sayı olmalı`);
      out[k] = n;
    } else {
      if (!['true', 'false'].includes(v)) throw new Error(`${k} true/false olmalı`);
      out[k] = v === 'true';
    }
  }
  if (!Object.keys(out).length) throw new Error('ayar verilmedi');
  return out;
}

if (require.main === module) {
  (async () => {
    const changes = parse(process.argv[2]);
    initAdmin();
    const ref = getFirestore().doc('app_meta/social');
    const before = (await ref.get()).data() || {};
    await ref.set(changes, { merge: true });
    for (const [k, v] of Object.entries(changes)) console.log(`${k}: ${JSON.stringify(before[k])} → ${JSON.stringify(v)}`);
  })().catch((err) => { console.error(err.message); process.exit(1); });
}

module.exports = { parse, ALLOWED };
