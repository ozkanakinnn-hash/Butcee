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
        is_active: true,
        created_at: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  await batch.commit();
  console.log(`✔ ${data.categories.length} kategori ve ${data.sources.length} kaynak yazıldı.`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
