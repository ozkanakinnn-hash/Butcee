'use strict';

/**
 * Zamanlayıcıyı beklemeden RSS çekimini bir kez çalıştırır (test / ilk doldurma için).
 *   GOOGLE_APPLICATION_CREDENTIALS=... npm run fetch:once
 *   veya emülatörde: FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 GCLOUD_PROJECT=demo-haber npm run fetch:once
 */

const { initAdmin } = require('./init-admin');
const { getFirestore } = require('firebase-admin/firestore');
const { ingestAllSources } = require('../src/ingest');

initAdmin();

ingestAllSources(getFirestore())
  .then(({ summary, results }) => {
    for (const r of results) {
      const status = r.error ? `HATA: ${r.error}` : r.notModified ? 'değişiklik yok' : `${r.added}/${r.fetched} yeni`;
      console.log(`- ${r.source}: ${status}`);
    }
    console.log('Özet:', summary);
  })
  .catch((err) => {
    console.error(err);
    process.exit(1);
  });
