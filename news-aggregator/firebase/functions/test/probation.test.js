'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { ingestSource } = require('../src/ingest');

const doc = (data, updates) => ({ id: 'x', data: () => data, ref: { update: async (u) => { updates.push(u); } } });
const broken = async () => new Response('yok', { status: 500 });

test('deneme kaynağı: art arda 6 hatada kapatılır; normal kaynak kapatılmaz', async () => {
  const u1 = [];
  await ingestSource({}, doc({ source_name: 'Yeni', rss_url: 'https://ornek.com/rss', probation: true, error_streak: 5 }, u1), new Map(), { fetchImpl: broken });
  assert.equal(u1[0].error_streak, 6);
  assert.equal(u1[0].is_active, false);
  assert.match(u1[0].disabled_reason, /art arda 6 hata/);

  const u2 = [];
  await ingestSource({}, doc({ source_name: 'Yeni', rss_url: 'https://ornek.com/rss', probation: true, error_streak: 1 }, u2), new Map(), { fetchImpl: broken });
  assert.equal(u2[0].error_streak, 2);
  assert.equal(u2[0].is_active, undefined, 'henüz kapatılmaz');

  const u3 = [];
  await ingestSource({}, doc({ source_name: 'Eski', rss_url: 'https://ornek.com/rss', error_streak: 50 }, u3), new Map(), { fetchImpl: broken });
  assert.equal(u3[0].is_active, undefined, 'deneme modunda olmayan kaynak kapatılmaz');
});
