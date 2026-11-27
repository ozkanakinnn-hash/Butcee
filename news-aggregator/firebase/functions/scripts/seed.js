'use strict';

/**
 * categories ve sources koleksiyonlarını örnek verilerle doldurur (tekrar çalıştırmak güvenlidir).
 *
 * Gerçek projede:
 *   1) Firebase Console → Proje Ayarları → Hizmet Hesapları → "Yeni özel anahtar oluştur"
 *   2) export GOOGLE_APPLICATION_CREDENTIALS=/yol/service-account.json   (depoya EKLEMEYİN)
 *   3) npm run seed
 *
 * Emülatörde:
 *   FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 GCLOUD_PROJECT=demo-haber npm run seed
 */

const { initAdmin } = require('./init-admin');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const data = require('./seed-data.json');

async function commitChunks(db, ops, label) {
  const CHUNK = 50;
  for (let i = 0; i < ops.length; i += CHUNK) {
    const batch = db.batch();
    for (const { ref, doc, opts } of ops.slice(i, i + CHUNK)) batch.set(ref, doc, opts);
    await batch.commit();
    process.stdout.write(`  ${label} ${Math.min(i + CHUNK, ops.length)}/${ops.length}\r`);
  }
  process.stdout.write('\n');
}

async function main() {
  initAdmin();
  const db = getFirestore();

  const catOps = data.categories.map((c) => {
    const { id, ...fields } = c;
    return { ref: db.collection('categories').doc(id), doc: fields, opts: { merge: true } };
  });

  const srcOps = data.sources.map((s) => ({
    ref: db.collection('sources').doc(s.id),
    doc: {
      source_name: s.source_name,
      rss_url: s.rss_url,
      category_id: db.collection('categories').doc(s.category),
      logo_url: `https://www.google.com/s2/favicons?domain=${s.domain}&sz=128`,
      is_active: s.is_active !== false,
      is_breaking: s.is_breaking === true,
      allow_images: s.allow_images !== false,
      created_at: FieldValue.serverTimestamp(),
    },
    opts: { merge: true },
  }));

  const { SUPER_LIG_TEAMS } = require('../src/teams');
  const teamOps = SUPER_LIG_TEAMS.map((t) => ({
    ref: db.collection('football_teams').doc(t.id),
    doc: { name: t.name, short: t.short, colors: t.colors, league_id: 203, active: true },
    opts: { merge: true },
  }));

  await commitChunks(db, catOps, 'Kategoriler');
  await commitChunks(db, srcOps, 'Kaynaklar');
  await commitChunks(db, teamOps, 'Takımlar');

  console.log(`✔ ${data.categories.length} kategori, ${data.sources.length} kaynak ve Süper Lig takımları yazıldı.`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
