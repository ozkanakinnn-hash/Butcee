'use strict';
const test = require('node:test');
const assert = require('node:assert');
const { isCritical, fold } = require('../src/quiet-hours');
const { ROUTINE } = require('../src/scoring');

const routine = (t) => ROUTINE.some((re) => re.test(fold(t)));

test('büyüklük yazan gerçek deprem haberi kritik kalır, rutine düşmez', () => {
  for (const t of ['Az önce 5,8 büyüklüğünde deprem! İstanbul sallandı', "Deprem mi oldu? İstanbul'da 4,9 büyüklüğünde deprem"]) {
    assert.equal(isCritical(t), true, t);
    assert.equal(routine(t), false, t);
  }
});

test('"Deprem mi oldu?" sorgu başlıkları kritik değil ve rutindir', () => {
  for (const t of ['Son dakika deprem mi oldu?', 'Nerede deprem oldu? Kandilli son depremler listesi']) {
    assert.equal(isCritical(t), false, t);
    assert.equal(routine(t), true, t);
  }
  assert.equal(isCritical('Deprem! İstanbul sallandı'), true);
});

test('aktüel fırsatlar ve dizi izleme linkleri rutindir', () => {
  assert.equal(routine('BİM AKTÜEL FIRSATLARI BELLİ OLDU'), true);
  assert.equal(routine('DAHA 17 26. BÖLÜM İZLEME LİNKİ KANAL D'), true);
  assert.equal(routine('Merkez Bankası faiz kararını açıkladı'), false);
});
