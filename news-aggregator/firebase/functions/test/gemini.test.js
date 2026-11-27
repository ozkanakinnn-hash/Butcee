'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { generate, extractText, modelOrder, summarizeBriefing, summarizeStory, AI_DEFAULTS } = require('../src/gemini');

const ok = (text) => new Response(JSON.stringify({ candidates: [{ content: { parts: [{ text }] } }] }));

test('model sırası: ayardaki model önce, yedekler tekrarsız', () => {
  const order = modelOrder({ model: 'gemini-9-flash' });
  assert.equal(order[0], 'gemini-9-flash');
  assert.equal(new Set(order).size, order.length);
  assert.ok(order.includes('gemini-flash-latest'));
});

test('yanıt metni: düşünce parçaları atılır', () => {
  assert.equal(extractText({ candidates: [{ content: { parts: [{ text: 'iç', thought: true }, { text: ' Özet. ' }] } }] }), 'Özet.');
  assert.equal(extractText({}), '');
});

test('kapatılmış model (404) → sıradaki model denenir', async () => {
  const urls = [];
  const fetchImpl = async (url) => {
    urls.push(url);
    return urls.length === 1 ? new Response('model not found', { status: 404 }) : ok('Gündem özeti.');
  };
  const r = await generate('x', { config: { ...AI_DEFAULTS, model: 'gemini-eski' }, fetchImpl, apiKey: 'k' });
  assert.equal(r.text, 'Gündem özeti.');
  assert.match(urls[0], /gemini-eski/);
  assert.notEqual(r.model, 'gemini-eski');
});

test('anahtar hatasında diğer modeller boşuna denenmez; anahtar yoksa istek atılmaz', async () => {
  let calls = 0;
  const r = await generate('x', { config: AI_DEFAULTS, fetchImpl: async () => { calls += 1; return new Response('API key not valid', { status: 400 }); }, apiKey: 'k' });
  assert.equal(calls, 1);
  assert.match(r.error, /HTTP 400/);
  const none = await generate('x', { config: AI_DEFAULTS, fetchImpl: async () => { throw new Error('çağrılmamalı'); }, apiKey: '' });
  assert.match(none.error, /GEMINI_API_KEY/);
});

test('özetler: durum ayar belgesine yazılır; tek haber özeti kapatılabilir', async () => {
  const docs = { 'app_meta/ai': { model: 'gemini-flash-latest' } };
  const db = {
    doc: (p) => ({ get: async () => ({ data: () => docs[p] }), set: async (v) => { docs[p] = { ...(docs[p] || {}), ...v }; } }),
  };
  const s = await summarizeBriefing([{ title: 'A', source: 'AA' }, { title: 'B', source: 'NTV' }], { db, fetchImpl: async () => ok('**Bugün** iki gelişme var.'), apiKey: 'k' });
  assert.equal(s, 'Bugün iki gelişme var.');
  assert.equal(docs['app_meta/ai'].active_model, 'gemini-flash-latest');
  assert.equal(docs['app_meta/ai'].last_error, '');

  docs['app_meta/ai'].story_summary = false;
  const one = await summarizeStory({ title: 'A', srcs: ['AA'] }, { db, fetchImpl: async () => ok('x'), apiKey: 'k' });
  assert.equal(one, '');
});
