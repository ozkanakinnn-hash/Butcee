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

async function main() {
  initAdmin();
  const db = getFirestore();
  const batch = db.batch();

  for (const c of data.categories) {
    const { id, ...fields } = c;
    batch.set(db.collection('categories').doc(id), fields, { merge: true });
  }

  for (const s of data.sources) {
    batch.set(
      db.collection('sources').doc(s.id),
      {
        source_name: s.source_name,
        rss_url: s.rss_url,
        category_id: db.collection('categories').doc(s.category),
        logo_url: `https://www.google.com/s2/favicons?domain=${s.domain}&sz=128`,
        is_active: s.is_active !== false,
        is_breaking: s.is_breaking === true,
        allow_images: s.allow_images !== false, // telif: false → yayıncı görseli kullanılmaz
        created_at: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  // Süper Lig takımları (API anahtarı olmadan da seçilebilsin)
  const { SUPER_LIG_TEAMS } = require('../src/teams');
  for (const t of SUPER_LIG_TEAMS) {
    batch.set(
      db.collection('football_teams').doc(t.id),
      { name: t.name, short: t.short, colors: t.colors, league_id: 203, active: true },
      { merge: true }
    );
  }

  await batch.commit();
  console.log(`✔ ${data.categories.length} kategori, ${data.sources.length} kaynak ve Süper Lig takımları yazıldı.`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
