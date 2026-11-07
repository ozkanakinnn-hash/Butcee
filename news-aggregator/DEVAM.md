# Haber Akışı — Bilgisayarda devam (Claude Desktop için başlangıç notu)

Bu klasörü Claude Desktop'ta açtıktan sonra yeni sohbete şunu yaz:
"news-aggregator/DEVAM.md dosyasını oku ve oradan devam edelim."

## Durum (7 Kasım 2026)
- Uygulama: Flutter (news-aggregator/flutter_app), paket `com.haberakisi.haber_akisi`, Firebase projesi `haber-68a3c`.
- Botlar Firebase Cloud Functions'ta (Blaze) çalışıyor: fetchRssFeeds (20 dk), cleanupOldArticles (04:00),
  footballTick (1 dk). Kod: news-aggregator/firebase/functions. Dağıtım: `npx -y firebase-tools@15 deploy --only functions,firestore`
  (news-aggregator/firebase klasöründe; önce `firebase login` ve `firebase use haber-68a3c`).
- GitHub hesabı askıya alındı → GitHub Actions'ta ZAMANLANMIŞ GÖREV ÇALIŞTIRMA (Actions yalnızca derleme/test).
- Kod yedeği: Google Drive'da tarihli zip'ler.
- Beceri notları: .claude/skills/flutter-firebase-app/SKILL.md (kullanıcı tercihleri ve öğrenilen dersler).

## Bilgisayarda ilk yapılacaklar
1. Flutter SDK (stable) + Android Studio (Android SDK 36, NDK 28.2.13676358) + Node.js 22 kur. `flutter doctor` temiz olsun.
2. Firebase Console → Proje ayarları → Android uygulaması → `google-services.json` indir →
   `news-aggregator/flutter_app/android/app/google-services.json` olarak koy (depoya eklenmez).
3. APK (test reklamlarıyla):
   ```
   cd news-aggregator/flutter_app
   flutter pub get
   flutter build apk --release --split-per-abi --target-platform android-arm64 --build-number=100 ^
     --dart-define=ADMOB_BANNER_ID=ca-app-pub-3940256099942544/6300978111 ^
     --dart-define=ADMOB_NATIVE_ID=ca-app-pub-3940256099942544/2247696110 ^
     --dart-define=ADMOB_INTERSTITIAL_ID=ca-app-pub-3940256099942544/1033173712
   ```
   Çıktı: build/app/outputs/flutter-apk/app-arm64-v8a-release.apk (sabit test anahtarıyla imzalı; önceki
   sürümün üzerine kurulur). Telefon USB ile bağlıysa: `flutter install`.
4. Testler: `flutter analyze && flutter test`; Functions: `cd news-aggregator/firebase/functions && npm ci && npm test`
   (emülatör testleri: `npx -y firebase-tools@15 emulators:exec --only firestore --project demo-haber "npm test"`).

## Bekleyen işler
- Yeni APK (Ekonomi sekmesi düzeltmesi, Investing yalnızca Ekonomi'de, görselsiz haberde kaynak logosu).
- Investing RSS'lerinin Firebase kayıtlarında hata verip vermediğini kontrol (`functions:log --only fetchRssFeeds`).
- Eski hizmet hesabı anahtarını silme (Firebase → Hizmet hesapları → Anahtarlar).
- Fikir: "Günün Özeti" (yapay zekâ, yalnızca başlık + kısa özetten, telif güvenceli) — onay bekliyor.
- API-Football ücretli plan + `functions:secrets:set FOOTBALL_API_KEY` ile canlı skor.
- Gerçek AdMob kimlikleri (mağaza sürümü) ve Play Console beyanları.
