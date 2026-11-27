'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { Timestamp } = require('firebase-admin/firestore');
const r = require('../src/rapor');

const T = (iso) => Date.parse(iso);

test('reportDue: 12:00 TSİ sonrası günde bir kez; rapor_simdi hemen; sohbet yoksa hiç', () => {
  const cfg = { owner_chat_id: '1' };
  assert.equal(r.reportDue({ now: T('2026-11-28T08:50:00Z'), config: cfg }), false); // 11:50
  assert.equal(r.reportDue({ now: T('2026-11-28T09:05:00Z'), config: cfg }), true); // 12:05
  assert.equal(r.reportDue({ now: T('2026-11-28T15:00:00Z'), state: { last_day: '2026-11-28' }, config: cfg }), false);
  assert.equal(r.reportDue({ now: T('2026-11-27T23:15:00Z'), config: { ...cfg, rapor_simdi: true } }), true);
  assert.equal(r.reportDue({ now: T('2026-11-28T09:05:00Z'), config: {} }), false);
  assert.equal(r.reportDue({ now: T('2026-11-28T09:05:00Z'), config: { ...cfg, rapor_enabled: false } }), false);
});

test('summarizeArticles ve countNotifications', () => {
  const cats = r.summarizeArticles([
    { title: 'Merkez Bankası faiz kararını açıkladı', source: 'A', category: 'ekonomi' },
    { title: 'Merkez Bankası faiz kararı açıklandı', source: 'B', category: 'ekonomi' },
    { title: 'Borsa günü yükselişle kapattı', source: 'A', category: 'ekonomi' },
    { title: 'Rizespor Kasımpaşa maçı', source: 'C', category: 'spor' },
  ], { ekonomi: 'Ekonomi', spor: 'Spor' });
  assert.equal(cats[0].name, 'Ekonomi');
  assert.equal(cats[0].count, 3);
  assert.equal(cats[0].top[0].sources, 2);
  const n = r.countNotifications([
    { type: 'article', topics: ['news_son-dakika'] }, { type: 'article', topics: ['bulletin'] },
    { type: 'article', topics: ['news_ekonomi'] }, { type: 'goal', topics: ['team_x'] },
  ]);
  assert.deepEqual({ total: n.total, news: n.news, breaking: n.breaking, bulletin: n.bulletin, gol: n.byType.Gol }, { total: 4, news: 3, breaking: 1, bulletin: 1, gol: 1 });
});

function fakeDb(seed) {
  const docs = new Map(Object.entries(seed));
  const docRef = (p) => ({
    get: async () => ({ data: () => docs.get(p), get: (k) => (docs.get(p) || {})[k] }),
    set: async (v) => docs.set(p, { ...(docs.get(p) || {}), ...v }),
  });
  const q = (col, f = []) => ({
    where: (k, op, v) => q(col, [...f, [k, v]]), select: () => q(col, f), limit: () => q(col, f),
    get: async () => ({
      docs: [...docs.entries()].filter(([p]) => p.split('/')[0] === col && p.split('/').length === 2)
        .filter(([, d]) => f.every(([k, v]) => d[k] && d[k].toMillis() >= v.toMillis()))
        .map(([p, d]) => ({ id: p.split('/')[1], get: (k) => d[k] })),
    }),
  });
  return { docs, doc: docRef, collection: (c) => q(c) };
}

test('runRapor: özel sohbete gönderir, rapor_simdi sıfırlanır, aynı gün tekrar göndermez', async () => {
  const now = T('2026-11-27T23:15:00Z');
  const ts = (min) => Timestamp.fromMillis(now - min * 60_000);
  const db = fakeDb({
    'app_meta/social': { owner_chat_id: '42', channel: '@kanal', rapor_simdi: true },
    'categories/ekonomi': { name: 'Ekonomi' },
    'articles/1': { title: 'Merkez Bankası faiz kararını açıkladı', source_name: 'A', category_id: { id: 'ekonomi' }, created_at: ts(30) },
    'articles/2': { title: 'Eski haber', source_name: 'A', category_id: { id: 'ekonomi' }, created_at: ts(60 * 30) },
    'notifications/a': { type: 'article', topics: ['news_son-dakika'], title: 'x', sent_at: ts(10) },
  });
  const sent = [];
  const fetchImpl = async (url, opt) => { sent.push(JSON.parse(opt.body)); return { ok: true, json: async () => ({ ok: true }) }; };
  const generate = async () => ({ text: '{"ozetler":{"ekonomi":"Faiz kararı günün gündemindeydi."}}', error: '' });
  const out = await r.runRapor(db, { now, token: 't', fetchImpl, generate });
  assert.equal(out.status, 'gönderildi');
  assert.equal(sent.length, 1);
  assert.equal(sent[0].chat_id, '42', 'kanala değil özel sohbete');
  assert.match(sent[0].text, /Sisteme giren haber: 1/);
  assert.match(sent[0].text, /Ekonomi: 1/);
  assert.match(sent[0].text, /giden bildirim: 1/);
  assert.match(sent[0].text, /Ekonomi: Faiz kararı günün gündemindeydi\./);
  assert.equal(db.docs.get('app_meta/social').rapor_simdi, false);
  // Elle gönderim aynı günün 12:00 raporunu engellemez; 12:00 raporu sonrası aynı gün tekrar gitmez.
  const noon = await r.runRapor(db, { now: T('2026-11-28T09:10:00Z'), token: 't', fetchImpl, generate });
  assert.equal(noon.status, 'gönderildi');
  const again = await r.runRapor(db, { now: T('2026-11-28T12:10:00Z'), token: 't', fetchImpl, generate });
  assert.equal(again.status, 'bekle');
});
