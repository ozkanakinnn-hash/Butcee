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

async function githubModels(prompt) {
  const res = await fetch('https://models.github.ai/inference/chat/completions', {
    method: 'POST',
    headers: { 'content-type': 'application/json', accept: 'application/vnd.github+json', authorization: `Bearer ${process.env.GITHUB_TOKEN}` },
    body: JSON.stringify({ model: process.env.GITHUB_MODEL || 'openai/gpt-4.1-mini', temperature: 0.3, response_format: { type: 'json_object' }, messages: [{ role: 'user', content: prompt }] }),
    signal: AbortSignal.timeout(60_000),
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) return { text: '', model: '', error: `GitHub Models HTTP ${res.status}: ${body.error?.message || body.message || ''}` };
  return { text: body.choices?.[0]?.message?.content || '', model: 'github-models', error: '' };
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
