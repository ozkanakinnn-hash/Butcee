'use strict';

/**
 * Admin SDK'yı başlatır. Proje kimliği sırasıyla GCLOUD_PROJECT ortam değişkeninden
 * veya GOOGLE_APPLICATION_CREDENTIALS ile verilen hizmet hesabı JSON'undaki project_id'den alınır.
 */
const fs = require('node:fs');
const { initializeApp } = require('firebase-admin/app');

function initAdmin() {
  let projectId = process.env.GCLOUD_PROJECT;
  const keyFile = process.env.GOOGLE_APPLICATION_CREDENTIALS;
  if (!projectId && keyFile && fs.existsSync(keyFile)) {
    try {
      projectId = JSON.parse(fs.readFileSync(keyFile, 'utf8')).project_id;
    } catch {
      throw new Error('Hizmet hesabı JSON dosyası okunamadı: ' + keyFile);
    }
  }
  return initializeApp(projectId ? { projectId } : undefined);
}

module.exports = { initAdmin };
