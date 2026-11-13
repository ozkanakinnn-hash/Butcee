# Minik Gurme — Devir Notu (yeni sohbet için)

Son güncelleme: 2026-11-14 · Dal: `ccr-bceb93e8-r8e22i` (PR #17) · Depo: ozkanakinnn-hash/Butcee · Klasör: `minik_gurme/`
Önce oku: kökteki `CLAUDE.md` (kullanıcı kuralları) ve `.claude/skills/android-compose-app/SKILL.md` (öğrenilen kalıplar, hata çözümleri).

## Kullanıcı kuralları (kalıcı)
- **APK/AAB yalnızca kullanıcı "APK derle" (veya "AAB derle") deyince** üretilir ve gönderilir. CI'dan APK artifact'ı yükleme.
- Her uygulamada **Türkçe + İngilizce**, ayarlarda dil seçici; veri içeriği de çevrilir (`shared/.../composeResources/files/i18n/en.json`).
- Her uygulama depoda **kendi klasöründe**. Öğrenilen kalıplar `.claude/skills/` altına yazılır.
- Dilde Türkçe konuş. Kısa, net, adım adım (kullanıcı çoğunlukla **telefondan** çalışıyor, ekran görüntüsü gönderiyor).
- İmza anahtarı (`minikgurme-upload.jks`, `keystore.properties`) **depoda yok, asla commit edilmez** (kullanıcıya zip olarak verildi).
- Commit mesajı sonuna attribution satırları (sistem hatırlatması neyse) eklenir; commit'lerde model kimliği yazılmaz.

## Uygulama
Android (Kotlin/Compose) + iPhone (Kotlin Multiplatform / Compose Multiplatform). Paket: `akinai.minikgurme`, sürüm 2.2 (14).
```
minik_gurme/
  app/      Android: MainActivity, GooglePlayBilling, AndroidReminders, AndroidPlatformServices, Firebase topluluk, yedek/PDF
  shared/   ORTAK kod: tüm ekranlar, Room DB, i18n, tema, topluluk, DateKit, BackupCore, StoreEvents/PlusManager
            iosMain: IosPlatformServices, IosReminders, IosStore (StoreKit köprüsü), MainViewController, IosFiles
  iosApp/   XcodeGen project.yml, iOSApp.swift, StoreBridge.swift (StoreKit 2), AppIcon, GoogleService-Info.plist
  docs/     YAYIN-HAZIRLIK.md, firestore.rules, site/ (web sayfaları), store/ (Play görselleri + metinler), DEVIR-NOTU.md
  tool/     build_site.py, store_graphics.py, promo_graphics.py, smoke.sh
```
Özellikler: Bugün, 100 Gıda (100 ayrı 3D görsel), Tarifler, Rehber (Bu Ay + bilgi bankası), Topluluk (Firebase forum; okumak herkese açık, yazmak Plus'a özel), büyüme takibi (WHO), alerjen planı, çoklu bebek (Plus), haftalık menü (Plus), doktor raporu PDF (Plus), yedek/geri yükle, bildirimler, açık/koyu tema.
Test: `./gradlew --no-daemon --no-configuration-cache --max-workers=2 :app:testDebugUnitTest` (ANDROID_HOME=/opt/android-sdk; Maven 429 olursa yeniden dene) → ~45 test, hepsi yeşil. iOS klib kontrolü: `:shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`.

## Durum
**Bitti:** Android uygulaması; Play Store web sayfaları (GitHub Pages yayında: https://ozkanakinnn-hash.github.io/Butcee/minik-gurme/ — gizlilik, koşullar, hesap-silme); mağaza görselleri + metinleri (`docs/store`, TR/EN, 7 ekran görüntüsü, öne çıkan görsel, tanıtım görselleri); iPhone uygulaması **Codemagic simülatöründe derlendi ve açıldı** (ilk ekran doğrulandı); iPhone'da bildirimler, topluluk (Firebase REST), yedek, PDF, Plus kodu (StoreKit 2 + `StoreEvents`) yazıldı.
**Doğrulanmadı:** iPhone'da onboarding sonrası ekranlar, bildirim izni, yedek, PDF, gerçek Plus satın alma (Apple üyeliği gerekir). Android release (R8) APK'sı iOS hazırlığı değişikliklerinden sonra **hiç derlenip telefonda denenmedi** (CI emülatör duman testi yeşildi).

## Bekleyenler (kullanıcıdaki bağımlılıklar)
1. **Apple Developer ödemesi onayı** → sonra: App Store Connect'te uygulama (`akinai.minikgurme`, ad "Minik Gurme") + abonelik grubu + ürünler `minikgurme_plus_monthly`, `minikgurme_plus_yearly`; Codemagic `ios-testflight` iş akışı (App Store Connect API anahtarı entegrasyonu "Minik Gurme ASC", `APP_STORE_APPLE_ID` değişkeni); TestFlight'ta satın alma denemesi.
2. **Google Play kimlik doğrulaması** (Google e-posta gönderecek) + "satıcı statüsü" + telefon doğrulama → sonra Play Console'da uygulama oluştur, `docs/store` içeriklerini yükle, abonelik ürünleri (aynı iki kimlik), veri güvenliği formu, **12 test kullanıcısıyla 14 gün kapalı test**, yükleme için **AAB** ("AAB derle" denince).
3. Firebase: Authentication › Anonim ve Firestore kuralları (`docs/firestore.rules`) **yayında**; kendini moderatör yapmak için `mods/<uid>` belgesi (uygulamada Ayarlar › Topluluk › Topluluk kimliğim).
4. `LegalTexts.CONTACT_EMAIL` dolu; ayrı destek adresi açılırsa değiştirilip `python3 tool/build_site.py` çalıştırılır.

## Sıradaki önerilen işler
- Codemagic simülatör testini genişlet (formu doldur → ana ekran, 100 Gıda, Tarifler, Topluluk ekran görüntüleri). `codemagic.yaml` → `ios-derleme-kontrolu`.
- iPhone'da alt güvenli alan boşluğunu kontrol et (onboarding ekranı altında dar boş şerit).
- Kullanıcı isterse: Tarifler simgesi alternatifi, kuzu eti/tavuk ciğeri ve bal kabağı/bezelye görselleri, Android'e "APK derle".

## Önemli teknik dersler (ayrıntı SKILL.md'de)
- GitHub Actions macOS makineleri bu hesapta atanmıyor → iPhone derlemesi **yalnızca Codemagic** (`codemagic.yaml`, kökte olmak zorunda). Codemagic bu depoya bağlı; dal `ccr-bceb93e8-r8e22i`, iş akışı "iOS derleme kontrolü (imzasız)". Varsayılan dalın eski `codemagic.yaml`'ı başka uygulamaya ait, onu seçme.
- iOS simülatör derlemesi `ARCHS=arm64` ile (x86_64 hedefi yok; yoksa `syncComposeResourcesForIos` "no value" hatası).
- CI'da `cmd | xcpretty` hatayı yutar → `set -o pipefail`. Yeşil derleme kanıt değildir; uygulamayı simülatörde aç + ekran görüntüsü.
- Compose BOM 2025.10.01 ↔ Compose Multiplatform 1.9.3 eşleşmeli. Room 2.8.4 KMP: `@Database` companion'ında kod olmasın.
- Robolectric: `PdfDocument` ve dialog+TextField (ekran niteleyicili) sorunlu; Compose kaynakları için `TestResources.init()`.
- CI botu `docs/screenshots` commit'i atar → push reddedilirse `git pull --rebase origin ccr-bceb93e8-r8e22i`.
- Test/ekran görüntüleri: `StoreScreenshotsTest` (mağaza), `AppSmokeTest` (genel), `CommunityTest`, `RestCommunityEmulatorTest` (Firebase emülatörü ile; `npx firebase emulators:exec`).
