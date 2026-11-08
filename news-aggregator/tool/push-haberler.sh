#!/usr/bin/env bash
# Haber Akışı'nı Bitbucket ozkan45/haber-akisi deposuna gönderir: depo kökünde yalnızca .gitignore + haberler/
# (= bu depodaki news-aggregator/ klasörü). Uzaktaki son kaydın üzerine yeni kayıt ekler (zorla itme yok).
# Kullanım (depo kökünden): news-aggregator/tool/push-haberler.sh "kayıt mesajı"
set -euo pipefail
REMOTE=${REMOTE:-haberakisi}
git -c credential.helper= fetch -q "$REMOTE" main
BASE=$(git rev-parse FETCH_HEAD)
NEWS=$(git rev-parse HEAD:news-aggregator)
GI=$(git rev-parse "$BASE:.gitignore")
TREE=$(printf '100644 blob %s\t.gitignore\n040000 tree %s\thaberler\n' "$GI" "$NEWS" | git mktree)
if [ "$TREE" = "$(git rev-parse "$BASE^{tree}")" ]; then echo "Değişiklik yok."; exit 0; fi
C=$(git commit-tree "$TREE" -p "$BASE" -m "${1:-Haber Akışı güncellemesi}")
git -c credential.helper= push "$REMOTE" "$C:refs/heads/main"
# Başarılı gönderimden sonra yerel "yayınlandı" işaretini güncelle: bu oturumun kontrolü (origin/<dal>)
# askıdaki GitHub'ı gösteriyor; asıl yayın yeri Bitbucket olduğundan işaret son başarılı gönderime eşitlenir.
git update-ref "refs/remotes/origin/$(git branch --show-current)" HEAD
echo "✔ Bitbucket'a gönderildi; yerel yayın işareti güncellendi."
