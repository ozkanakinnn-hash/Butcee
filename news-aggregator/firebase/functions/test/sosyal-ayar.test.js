'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const { parse } = require('../scripts/sosyal-ayar');

test('sosyal-ayar: yalnızca izinli anahtarlar ve doğru türler', () => {
  assert.deepEqual(parse('make_daily_cap=7, akis_dry_run=false'), { make_daily_cap: 7, akis_dry_run: false });
  assert.throws(() => parse('x_webhook=https://kotu'), /izin verilmeyen/);
  assert.throws(() => parse('make_daily_cap=yedi'), /sayı/);
  assert.throws(() => parse('send_akis=evet'), /true\/false/);
  assert.throws(() => parse(''), /verilmedi/);
});
