'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const ai = require('../src/ai');

const payload = {
  slot_label: 'Akşam Akışı', emoji: '🌆', date: '27 Kasım 2026', daily_recap: false, app_link: '',
  events: [{ title: 'Karadağ vatandaşlarının Türkiye\'ye vizesiz girişleri durduruldu', summary: 'x'.repeat(300), sources: ['A'], category: 'dunya', breaking: true }],
};

test('buildUserMessage geçmişi ve haberleri içerir, özeti kısaltır', () => {
  const m = ai.buildUserMessage(payload, [{ konu_anahtari: 'butce-2027', son_durum: 'Komisyonda' }], { summaryChars: 50 });
  assert.match(m, /butce-2027 \| Komisyonda/);
  assert.match(m, /GÜNÜN BÜLTENİ MODU: false/);
  assert.ok(!m.includes('x'.repeat(51)));
  assert.match(ai.buildUserMessage(payload, []), /\(yok\)/);
});

test('parseModelJson çitleri temizler', () => {
  assert.deepEqual(ai.parseModelJson('```json\n{"paylas": false}\n```'), { paylas: false });
  assert.throws(() => ai.parseModelJson('üzgünüm'), /JSON/);
});

test('normalizeThread 4 parça ve 280 karakter sınırını zorlar', () => {
  const lines = Array.from({ length: 20 }, (_, i) => `🔸 Madde ${i} ${'a'.repeat(100)}`);
  const out = ai.normalizeThread([lines.join('\n') + '\n#Gündem']);
  assert.ok(out.length <= ai.MAX_PARTS);
  for (const p of out) assert.ok(p.length <= 280, `uzun parça: ${p.length}`);
  assert.match(out[0], /\(1\/4\)$/);
  assert.match(out[out.length - 1], /#Gündem/);
  assert.deepEqual(ai.normalizeThread(['kısa']), ['kısa']);
  assert.deepEqual(ai.normalizeThread(['a', 'b']), ['a (1/2)', 'b (2/2)']);
  assert.deepEqual(ai.normalizeThread(null), []);
});

test('findCopiedPhrases başlıktan kopyalanan 6 kelimeyi bulur', () => {
  const titles = ['Karadağ vatandaşlarının Türkiye\'ye vizesiz girişleri durduruldu bugün itibarıyla'];
  assert.equal(ai.findCopiedPhrases(['🔸 Karadağ vatandaşları için vizesiz giriş askıya alındı.'], titles).length, 0);
  assert.ok(ai.findCopiedPhrases(['Karadağ vatandaşlarının Türkiye\'ye vizesiz girişleri durduruldu bugün.'], titles).length > 0);
});

test('writeBulletin gemini hata verirse GitHub Models\'e düşer', async () => {
  const calls = [];
  const fetchImpl = async (url) => {
    calls.push(url);
    if (url.includes('googleapis')) return { ok: false, status: 429, json: async () => ({ error: { message: 'kota' } }) };
    return {
      ok: true, status: 200,
      json: async () => ({ choices: [{ message: { content: '{"paylas":true,"zincir":["🌆 Akşam\\n🔸 Madde"],"konular":[{"konu_anahtari":"k","durum":"yeni","son_durum":"s"}],"atlananlar":[]}' } }] }),
    };
  };
  const r = await ai.writeBulletin(payload, [], { GEMINI_API_KEY: 'g', GITHUB_TOKEN: 't' }, fetchImpl);
  assert.equal(calls.length, 2);
  assert.match(r.provider, /^github:/);
  assert.equal(r.paylas, true);
  assert.equal(r.konular.length, 1);
  await assert.rejects(ai.writeBulletin(payload, [], {}, fetchImpl), /anahtar/);
});

test('writeBulletin paylas:false ise boş zincir döner', async () => {
  const fetchImpl = async () => ({ ok: true, status: 200, json: async () => ({ candidates: [{ content: { parts: [{ text: '{"paylas":false,"zincir":["x"],"konular":[],"atlananlar":["a"]}' }] } }] }) });
  const r = await ai.writeBulletin(payload, [], { GEMINI_API_KEY: 'g' }, fetchImpl);
  assert.equal(r.paylas, false);
  assert.deepEqual(r.atlananlar, ['a']);
});
