#!/usr/bin/env bash
# Mağaza yayını becerisini ve iOS yükleme şablonlarını başka bir depoya kopyalar.
# Kullanım: bash .claude/skills/magaza-yayini/kur.sh /yol/hedef-depo
# Var olan dosyaların üzerine yazmaz.
set -euo pipefail
SRC="$(cd "$(dirname "$0")" && pwd)"
DST="${1:?Hedef depo yolunu verin}"
[ -d "$DST/.git" ] || { echo "Uyarı: $DST bir git deposu görünmüyor." >&2; }

copy() {  # kaynak hedef
  if [ -e "$2" ]; then echo "atlandı (zaten var): ${2#$DST/}"; else mkdir -p "$(dirname "$2")"; cp -r "$1" "$2"; echo "eklendi: ${2#$DST/}"; fi
}

copy "$SRC" "$DST/.claude/skills/magaza-yayini"
copy "$SRC/templates/asc_signing.py" "$DST/ios/scripts/asc_signing.py"
copy "$SRC/templates/ios-release.yml" "$DST/.github/workflows/ios-release.yml"
copy "$SRC/templates/APPLE-HESAP-REHBERI.md" "$DST/docs/appstore/APPLE-HESAP-REHBERI.md"
[ -e "$DST/ios/RELEASE_VERSION" ] || { mkdir -p "$DST/ios"; echo "1.0.0" > "$DST/ios/RELEASE_VERSION"; echo "eklendi: ios/RELEASE_VERSION"; }

cat <<'EOF'

Sonraki adımlar:
 1) .github/workflows/ios-release.yml → AYARLAR: IOS_DIR, PROJECT, SCHEME, BUNDLE_IDS
 2) ios/project.yml → hedeflere PROVISIONING_PROFILE_SPECIFIER: "$(APP_PROFILE)" (uzantılara "$(PROFILE_2)" …)
 3) GitHub → Settings → Secrets → APPLE_TEAM_ID, ASC_KEY_ID, ASC_ISSUER_ID, ASC_PRIVATE_KEY
 4) docs/appstore/APPLE-HESAP-REHBERI.md içindeki uygulama adlarını/kimlikleri güncelle
EOF
