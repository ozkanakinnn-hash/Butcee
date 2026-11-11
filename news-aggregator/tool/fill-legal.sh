#!/usr/bin/env bash
# Yasal sayfalardaki ve config.dart'taki [[...]] yer tutucularını doldurur.
# Kullanım: tool/fill-legal.sh "Ad Soyad" "eposta@ornek.com" "+90 5xx xxx xx xx" "İlçe / İl, Türkiye"
# (E-posta zaten işlendiyse ikinci argüman yok sayılır.)
set -euo pipefail
cd "$(dirname "$0")/.."
[ $# -eq 4 ] || { echo "Kullanım: $0 \"Ad Soyad\" \"eposta\" \"telefon\" \"adres\""; exit 1; }
NAME=$1 MAIL=$2 PHONE=$3 ADDR=$4 DATE=$(TZ=Europe/Istanbul date +%d.%m.%Y)
LINK=$(printf '%s' "$PHONE" | tr -cd '+0-9')
export NAME MAIL PHONE ADDR DATE LINK
for f in firebase/hosting/public/*.html flutter_app/lib/config.dart; do
  python3 - "$f" <<'PY'
import os, sys, html
p = sys.argv[1]; s = open(p, encoding='utf-8').read()
esc = (lambda v: html.escape(v)) if p.endswith('.html') else (lambda v: v)
for k, env in [('AD_SOYAD','NAME'),('EPOSTA','MAIL'),('TELEFON_LINK','LINK'),('TELEFON','PHONE'),('ADRES','ADDR'),('TARIH','DATE')]:
    s = s.replace('[[%s]]' % k, esc(os.environ[env]))
open(p, 'w', encoding='utf-8').write(s)
PY
done
if grep -rn '\[\[' firebase/hosting/public flutter_app/lib/config.dart; then echo "✗ doldurulmamış alan var"; exit 1; fi
echo "✔ Yasal sayfalar ve uygulama ayarları dolduruldu ($DATE)."
