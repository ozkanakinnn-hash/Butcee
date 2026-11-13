#!/usr/bin/env bash
# Tanıtım ekranlarını tek tek yakalar (her ekran ayrı işlemde; bkz. capture.dart).
# Kullanım: PROMO_OUT=... PROMO_IMAGES=... tool/promo/yakala.sh
set -u
cd "$(dirname "$0")/../.."
for name in 01-akis 02-canli-skor 03-bildirim-ayarlari 04-piyasa-hava 05-ilgi-alanlari 06-bildirimler 07-akis-koyu; do
  PROMO_ONLY="$name" flutter test -r compact tool/promo/capture.dart >/dev/null 2>&1
  if [ -f "${PROMO_OUT:-build/promo}/$name.png" ]; then echo "✔ $name"; else echo "�‼ $name YAKALANAMADI"; fi
done
