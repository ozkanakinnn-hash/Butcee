#!/usr/bin/env bash
# Tanıtım ekranlarını tek tek yakalar (her ekran ayrı işlemde; bkz. capture.dart).
# Kullanım: PROMO_OUT=... PROMO_IMAGES=... tool/promo/yakala.sh
set -u
cd "$(dirname "$0")/../.."
mkdir -p "${PROMO_OUT:-build/promo}"
for name in ${PROMO_LIST:-01-akis 02-canli-skor 03-bildirim-ayarlari 04-piyasa-hava 05-ilgi-alanlari 06-bildirimler 07-akis-koyu 08-arama 09-son-dakika}; do
  PROMO_ONLY="$name" flutter test -r compact tool/promo/capture.dart > "${PROMO_OUT:-build/promo}/$name.log" 2>&1
  if [ -f "${PROMO_OUT:-build/promo}/$name.png" ]; then echo "✔ $name"; else echo "�‼ $name YAKALANAMADI"; fi
done
