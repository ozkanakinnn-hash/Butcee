'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { readServiceAccount } = require('../scripts/init-admin');

const { privateKey } = crypto.generateKeyPairSync('rsa', {
  modulusLength: 2048,
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
  publicKeyEncoding: { type: 'spki', format: 'pem' },
});
const SA = {
  type: 'service_account',
  project_id: 'haber-12345',
  private_key_id: '0123456789abcdef0123456789abcdef01234567',
  private_key: privateKey,
  client_email: 'firebase-adminsdk-abcde@haber-12345.iam.gserviceaccount.com',
  client_id: '123456789012345678901',
  auth_uri: 'https://accounts.google.com/o/oauth2/auth',
  token_uri: 'https://oauth2.googleapis.com/token',
};
const ORIGINAL = JSON.stringify(SA, null, 2);

/** Metin düzenleyici sarması: her satırı `width` karakterde böler. */
const wrap = (text, width, eol = '\n') =>
  text.split('\n').map((line) => line.match(new RegExp(`.{1,${width}}`, 'g')) || ['']).flat().join(eol);

function read(text) {
  const file = path.join(fs.mkdtempSync(path.join(os.tmpdir(), 'sa-')), 'sa.json');
  fs.writeFileSync(file, text);
  try {
    return readServiceAccount(file);
  } finally {
    fs.rmSync(path.dirname(file), { recursive: true, force: true });
  }
}

function assertKeyOk(sa) {
  assert.equal(sa.project_id, 'haber-12345');
  assert.equal(sa.client_email, SA.client_email);
  const signer = crypto.createSign('SHA256').update('x');
  assert.ok(signer.sign(sa.private_key).length > 0, 'anahtar imza atabilmeli');
}

const scenarios = {
  'bozulmamış': ORIGINAL,
  'BOM + boşluk': '﻿\n  ' + ORIGINAL + '\n\n',
  '\\n kaçışları gerçek satır sonu': ORIGINAL.replace(/\\n/g, '\n'),
  '\\n kaçışları CRLF': ORIGINAL.replace(/\\n/g, '\r\n'),
  'sarma 40': wrap(ORIGINAL, 40),
  'sarma 64': wrap(ORIGINAL, 64),
  'sarma 76 CRLF': wrap(ORIGINAL, 76, '\r\n'),
  'sarma 100': wrap(ORIGINAL, 100),
  'tek satır + sarma 50': wrap(JSON.stringify(SA), 50),
  'kaçışlar açılmış + sarma 45': wrap(ORIGINAL.replace(/\\n/g, '\n'), 45),
};

for (const [name, text] of Object.entries(scenarios)) {
  test(`anahtar onarımı: ${name}`, () => assertKeyOk(read(text)));
}

test('kesik anahtar onarılamaz ve hata mesajı anahtar içeriği içermez', () => {
  const truncated = ORIGINAL.slice(0, 1200);
  assert.throws(() => read(truncated), (err) => {
    const body = privateKey.split('\n')[3];
    assert.ok(!err.message.includes(body), 'anahtar parçası sızmamalı');
    assert.ok(!err.message.includes(SA.private_key_id), 'anahtar kimliği sızmamalı');
    assert.match(err.message, /okunamadı/);
    return true;
  });
});

test('private_key ortasından karakter eksikse hata verir', () => {
  const lines = ORIGINAL.split('\\n');
  lines[5] = lines[5].slice(0, 30); // bir satırın yarısı kayıp
  assert.throws(() => read(lines.join('\\n')), /private_key geçerli bir anahtar değil|okunamadı/);
});
