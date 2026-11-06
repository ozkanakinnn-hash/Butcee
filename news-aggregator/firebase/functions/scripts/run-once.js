'use strict';

/**
 * Zamanlayıcıyı beklemeden RSS çekimini bir kez çalıştırır (test / ilk doldurma için).
 *   GOOGLE_APPLICATION_CREDENTIALS=... npm run fetch:once
 *   veya emülatörde: FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 GCLOUD_PROJECT=demo-haber npm run fetch:once
 */

const { initAdmin } = require('./init-admin');
const { getFirestore } = require('firebase-admin/firestore');
const { ingestAllSources } = require('../src/ingest');
const { runMaintenance } = require('../src/maintenance');
const { updateMarket } = require('../src/market');

initAdmin();

const db = getFirestore();
ingestAllSources(db)
  .then(async ({ summary, results }) => {
    for (const r of results) {
      const status = r.error ? `HATA: ${r.error}` : r.notModified ? 'değişiklik yok' : `${r.added}/${r.fetched} yeni`;
      console.log(`- ${r.source}: ${status}`);
    }
    console.log('Özet:', summary);
    // Ek işler: hata verirlerse haber çekimini başarısız saymayalım.
    try {
      console.log('Bakım:', JSON.stringify(await runMaintenance(db)));
    } catch (err) {
      console.log('Bakım hatası:', err.message);
    }
    try {
      console.log('Piyasa:', JSON.stringify(await updateMarket(db)));
    } catch (err) {
      console.log('Piyasa hatası:', err.message);
    }
  })
  .catch((err) => {
    console.error(err);
    process.exit(1);
  });
