'use strict';

/**
 * Bir akışı HEMEN yazdırıp Make'e gönderir (elle deneme). Ayarlar ve webhook `app_meta/social`'dan okunur.
 *   node scripts/akis-simdi.js gece            → yazar ve gönderir (akis_dry_run yok sayılır)
 *   node scripts/akis-simdi.js gece --onizle   → yalnızca metni yazar, göndermez
 * Yapay zekâ: GEMINI_API_KEY varsa Gemini, yoksa GitHub Models (GITHUB_TOKEN, ücretsiz).
 */

const { getFirestore } = require('firebase-admin/firestore');
const { initAdmin } = require('./init-admin');
const { runAkis } = require('../src/akis');

const slot = process.argv[2] || 'gece';
const preview = process.argv.includes('--onizle');

const MODELS = (process.env.GITHUB_MODEL || 'openai/gpt-4.1-mini,openai/gpt-4o-mini,meta/Llama-3.3-70B-Instruct').split(',');

/** GitHub Models: sırayla modelleri dener; boş/filtreli yanıtın nedenini kayda yazar. */
async function githubModels(prompt) {
  let lastError = '';
  for (const model of MODELS) {
    try {
      const res = await fetch('https://models.github.ai/inference/chat/completions', {
        method: 'POST',
        headers: { 'content-type': 'application/json', accept: 'application/vnd.github+json', authorization: `Bearer ${process.env.GITHUB_TOKEN}` },
        body: JSON.stringify({ model, temperature: 0.3, max_tokens: 2000, messages: [{ role: 'user', content: prompt }] }),
        signal: AbortSignal.timeout(90_000),
      });
      const raw = await res.text();
      let body = {};
      try { body = JSON.parse(raw); } catch { /* düz metin */ }
      const choice = body.choices && body.choices[0];
      const text = (choice && choice.message && choice.message.content) || '';
      if (res.ok && text) {
        console.log(`Yapay zekâ: ${model}`);
        return { text, model, error: '' };
      }
      lastError = `${model}: HTTP ${res.status} ${choice ? `finish=${choice.finish_reason}` : ''} ${raw.slice(0, 300).replace(/\s+/g, ' ')}`;
      console.log(`Deneme başarısız → ${lastError}`);
    } catch (err) {
      lastError = `${model}: ${err.message}`;
      console.log(`Deneme başarısız → ${lastError}`);
    }
  }
  return { text: '', model: '', error: lastError || 'yanıt yok' };
}

(async () => {
  initAdmin();
  const db = getFirestore();
  const config = (await db.doc('app_meta/social').get()).data() || {};
  const generate = process.env.GEMINI_API_KEY ? undefined : githubModels;
  const r = await runAkis(db, {
    slot, ignoreDryRun: true, generate,
    config: preview ? { ...config, akis_dry_run: true, dry_run: true } : config,
  });
  console.log(`Akış: ${slot} → ${r.status}${r.error ? ` (${r.error})` : ''}`);
  if (r.metin) console.log(`\n──── X metni ────\n${r.metin}\n─────────────────`);
  if (/hata/.test(r.status)) process.exit(1);
})().catch((err) => { console.error(err.message || err); process.exit(1); });
