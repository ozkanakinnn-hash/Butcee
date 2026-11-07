'use strict';

/**
 * Telif "Uyar-Kaldır" — GitHub Actions → "Haber Akışı - Telif Kaldırma" → Run workflow.
 *   node scripts/takedown.js article <haber-adresi> [not]
 *   node scripts/takedown.js source <kaynak-kimliği> [not]
 *   node scripts/takedown.js images <kaynak-kimliği> [not]
 */

const { initAdmin } = require('./init-admin');
const { getFirestore } = require('firebase-admin/firestore');
const t = require('../src/takedown');

async function main() {
  const [mode, target, ...rest] = process.argv.slice(2);
  const note = rest.join(' ');
  if (!mode || !target) throw new Error('Kullanım: takedown.js article|source|images <hedef> [not]');
  initAdmin();
  const db = getFirestore();
  const fn = { article: t.removeArticleByUrl, source: t.removeSource, images: t.disableSourceImages }[mode];
  if (!fn) throw new Error(`Bilinmeyen işlem: ${mode}`);
  console.log(`${mode} → ${target}:`, JSON.stringify(await fn(db, target, { note })));
}

main().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
