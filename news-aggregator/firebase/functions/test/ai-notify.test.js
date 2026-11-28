'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const a = require('../src/ai-notify');

test('ai-notify: istem, ayrıştırma, yedek karar ve budama', async () => {
  const p = a.buildJudgePrompt([{ id: 'x1', title: 'Başlık', srcs: ['A', 'B'], br: true }], ['Önceki bildirim'], { now: Date.parse('2026-11-28T09:00:00Z') });
  assert.match(p, /Önceki bildirim/);
  assert.match(p, /"id":"x1"/);
  assert.match(p, /taht savaşı/);
  const d = a.parseDecisions('```json\n{"kararlar":[{"id":"x1","ok":false,"kritik":true,"gece":true,"neden":"mecaz"}]}\n```', 5);
  assert.deepEqual(d.x1, { ok: false, kritik: false, gece: false, neden: 'mecaz', at: 5 }, 'ok=false iken kritik/gece de false');
  assert.equal(a.fallbackDecision({ level: 1, night: true, reason: 'r' }, 1).gece, true);
  assert.deepEqual(Object.keys(a.pruneDecisions({ eski: { at: 0 }, yeni: { at: 90_000_000 } }, 100_000_000)), ['yeni']);
  // Gemini hatası → kurallarla yedek karar
  const r = await a.judgeCandidates([{ story: { id: 's1', title: 'T', srcs: [] }, score: { level: 2, night: false, reason: 'k' } }], [], {
    generate: async () => ({ text: '', error: 'HTTP 429' }),
  });
  assert.equal(r.error, 'HTTP 429');
  assert.equal(r.decisions.s1.fallback, true);
  assert.equal(r.decisions.s1.ok, true);
});
