'use strict';

/** Aynı haber hem Son Dakika/Gündem hem Ekonomi akışında: Ekonomi kategorisine taşınmalı. */
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const skip = !process.env.FIRESTORE_EMULATOR_HOST && 'FIRESTORE_EMULATOR_HOST tanımlı değil';

test('genel akıştan önce kaydedilen haber konu kategorisine taşınır; Son Dakika işareti kalır', { skip }, async () => {
  const { initializeApp } = require('firebase-admin/app');
  const { getFirestore } = require('firebase-admin/firestore');
  const { ingestAllSources } = require('../src/ingest');
  const { rescanTopicSources } = require('../src/maintenance');

  initializeApp({ projectId: 'demo-topic' });
  const db = getFirestore();
  const fixture = fs.readFileSync(path.join(__dirname, 'fixtures', 'windows-1254.xml'));
  const fetchImpl = async () => new Response(fixture, { status: 200 });
  const now = new Date('2026-11-06T12:00:00Z');

  await db.doc('categories/son-dakika').set({ name: 'Son Dakika' });
  await db.doc('categories/ekonomi').set({ name: 'Ekonomi' });
  await db.doc('sources/sd').set({
    source_name: 'SD', rss_url: 'https://a/rss', category_id: db.doc('categories/gundem'), is_active: true, is_breaking: true,
  });
  const r1 = await ingestAllSources(db, { fetchImpl, now, notify: false });
  assert.equal(r1.summary.added, 4);

  // Ekonomi kaynağı sonradan eklenir (ya da son görülen işareti sıfırlanmış) → aynı haberleri görür.
  await db.doc('sources/ekonomi').set({
    source_name: 'Eko', rss_url: 'https://b/rss', category_id: db.doc('categories/ekonomi'), is_active: true,
    last_seen_published_at: now, // eski işaret: rescan sıfırlamalı
  });
  assert.deepEqual(await rescanTopicSources(db), { done: true, reset: 1 });
  assert.deepEqual(await rescanTopicSources(db), { done: true, reset: 0 }, 'bir kerelik');

  const r2 = await ingestAllSources(db, { fetchImpl, now, notify: false });
  assert.equal(r2.summary.added, 0);
  assert.equal(r2.results.find((r) => r.source === 'Eko').movedToCategory, 4);

  const docs = (await db.collection('articles').get()).docs;
  assert.ok(docs.every((d) => d.get('category_id').id === 'ekonomi'));
  assert.ok(docs.every((d) => d.get('breaking_at')), 'Son Dakika sekmesinde kalır');

  // Son Dakika kaynağı tekrar görünce kategoriyi geri almaz.
  await db.doc('sources/sd').update({ last_seen_published_at: null, etag: null });
  await ingestAllSources(db, { fetchImpl, now, notify: false });
  const after = (await db.collection('articles').get()).docs;
  assert.ok(after.every((d) => d.get('category_id').id === 'ekonomi'));
});
