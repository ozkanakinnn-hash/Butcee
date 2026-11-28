---
name: magaza-yayini
description: Uygulamayı Google Play ve Apple App Store'a yayınlama — imzalı AAB (telefon + Wear OS), test kanalına güncelleme olarak yükleme, Mac/iPhone olmadan GitHub Actions ile App Store'a imzalı yükleme (geçici dağıtım sertifikası), App Store Connect ve Play Console'da kullanıcıyı adım adım yönlendirme, mağaza metinleri ve ekran görüntüleri. "Play'e yükle", "App Store'a gönder", "yeni sürüm/kod hazırla", "AAB gönder", "TestFlight" isteklerinde kullan.
---

# Mağaza yayını (Play + App Store)

Kullanıcı: Özkan (Türkçe konuş). Cihazı Android telefon; **Mac ve iPhone yok** — iOS işlemleri bulutta, kullanıcı tarayıcıdan yapar.
Uygulama kodu Android'de Kotlin/Compose, iOS'ta SwiftUI + XcodeGen (`ios/project.yml`). Ayrıntılı geçmiş: `android-compose-app` becerisi.

## Başka depoya taşıma
Bu klasör kendi kendine yeter. Yeni depoda: `bash .claude/skills/magaza-yayini/kur.sh <hedef-depo-yolu>`
→ beceriyi `<hedef>/.claude/skills/` altına, iOS şablonlarını `<hedef>/ios/scripts/` ve `.github/workflows/` altına kopyalar (var olanın üzerine yazmaz).

## Kalıcı kurallar
- Paket adı: `akin.ai<N>` (Android applicationId = iOS bundle id; widget `akin.ai<N>.widget`, App Group `group.akin.ai<N>`).
  1) Haber Akışı (`akinai.app1` — eski düzen; Flutter, kodu `claude/haber-akisi-2026-11` dalında `news-aggregator/`) 2) Ezan & Vakit (`akin.ai2`) → yeni uygulama `akin.ai3`. Yayından sonra değişmez: kullanıcıya teyit ettir.
- Gizli dosyalar ASLA depoya girmez, şifre sohbete/loga yazılmaz: `*.jks`, şifre dosyası, `google-services.json`, `.p8`. Kullanıcıya "değerleri sohbete yazma, yalnızca GitHub sırlarına gir" de.
- Gizlilik/destek sayfaları: `ozkanakinnn-hash/Butcee` deposu `gh-pages` dalı, uygulama başına klasör → `https://ozkanakinnn-hash.github.io/Butcee/<uygulama>/privacy-policy.html` ve `support.html`. gh-pages'e göndermek için kullanıcıdan açık izin al.
- Açıklamada yalnız o platformda olan özellikleri yaz (Apple 2.3.1). Başka markanın adını anahtar kelimeye koyma.
- Kullanıcı yönlendirirken: tek seferde 3–5 kısa adım, düğme adlarını ekranda göründüğü dilde (çoğunlukla İngilizce) **kalın** yaz, kopyalanacak metni ayrı kod bloğunda ver. Ekran görüntüsü atarsa önce ne gördüğünü söyle, sonra tek sonraki adımı ver.

## A) Google Play — yeni sürüm (güncelleme olarak inmesi için)
1. `gradle.properties`: `APP_VERSION_CODE` +1 (öncekinden **büyük** olmalı; reddedilen yüklemeden sonra da artır), `APP_VERSION_NAME`.
   Wear aynı ilanda: wear kodu `1_000_000 + APP_VERSION_CODE`.
2. Testler: `./gradlew --max-workers=1 :app:testDebugUnitTest` (Maven 429 / Robolectric indirme hatasında 45 sn bekleyip tekrar).
3. İmzalı paket (anahtar ve şifre kullanıcının yüklediği dosyalardan; şifreyi `grep -oP` ile değişkene al, ekrana basma):
   `KEYSTORE_PATH=$PWD/my-upload-key.jks STORE_PASSWORD=$PW KEY_PASSWORD=$PW ./gradlew --max-workers=1 :app:bundleRelease :wear:bundleRelease`
   build.gradle.kts: `signingConfigs.release { storeFile=file(env KEYSTORE_PATH); keyAlias="upload"; şifreler env'den }`.
4. **Güncelleme kontrolü (her teslimde tabloyla kullanıcıya bildir):**
   - paket adı aynı: `grep -oE 'package=|versionCode=' <modül>/build/intermediates/merged_manifest*/release/**/AndroidManifest.xml`
   - imza aynı: `keytool -printcert -jarfile x.aab | grep SHA1` → önceki sürümle aynı parmak izi (Ezan & Vakit: `E5:7E:0E:27:…:0F:A5`)
   - veri kaybı yok: Room/SQLite şeması değiştiyse migration yaz; SharedPreferences anahtarlarını yeniden adlandırma.
5. Dosya adı: `<Ad>-<paket>-v<ad>-kod<kod>.aab` → scratchpad/out → SendUserFile (display attach).
6. Kullanıcıya: Play Console → **Test ve yayınla → Kapalı test → (mevcut kanal) → Yeni sürüm oluştur** → AAB yükle (saat AAB'si önceki yere) → sürüm notu → incelemeye gönder. Yeni kanal açtırma (testçiler eski kanalda).
   Yeni kişisel hesapta üretim öncesi 12 testçi / 14 gün kapalı test zorunlu.
7. Mağaza metni değiştiyse `docs/store/magaza-metni.md`'yi güncelle ve hangi satırı değiştireceğini söyle.

Play hataları: Billing kitaplığı ≥ 8 (`billing-ktx` 8.x); sürüm notu etiketi varsayılan dille aynı (`<tr-TR>`); yerel sembol uyarısı engelleyici değil; `USE_EXACT_ALARM` yalnız alarm uygulamalarına.

## B) App Store — Mac'siz yükleme (ÇALIŞAN yöntem, Kasım 2026)
Şablonlar: `templates/ios-release.yml`, `templates/asc_signing.py`.
- `macos-15-intel` + en yeni Xcode → XcodeGen → `asc_signing.py setup`: App Store Connect API ile geçici **DISTRIBUTION** sertifikası + her bundle id için **IOS_APP_STORE** profili + geçici keychain → `xcodebuild archive` (Manual, "Apple Distribution") → `-exportArchive` (destination upload) → `cleanup` sertifika/profilleri siler.
- `project.yml`: hedeflerde `PROVISIONING_PROFILE_SPECIFIER: "$(APP_PROFILE)"`, uzantılarda `"$(PROFILE_2)"` …; `CODE_SIGN_STYLE: Automatic` kalabilir (CI komut satırında Manual'a çevrilir).
- **Otomatik imza kullanma**: cihaz kaydı olmayan hesapta "Your team has no devices" hatası verir.
- Sırlar (depo başına): `APPLE_TEAM_ID`, `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_PRIVATE_KEY`. API anahtarı **Admin**. `.p8` yalnız bir kez indirilir — kullanıcıya sakla dedirt. Aynı anahtar hesaptaki bütün uygulamalarda çalışır.
- Telefondan yapıştırılan .p8 tek satıra döner → iş akışı PEM'i yeniden sarar. Android .p8'i açamaz: uzantıyı `.txt` yaptır.
- Tetik: Actions → Run workflow, ya da `ios/RELEASE_VERSION` değiştirip göndermek (kullanıcı yerine başlatmak için). Yapı no = `github.run_number`.
- Sertifika sınırı hatası → developer.apple.com → Certificates'ta eski "Apple Distribution"ları iptal ettir.
- İşlem 20–40 dk + Apple işlemesi 10–30 dk; yapı TestFlight'ta görünür. İnceleme genelde 24–48 saat.

### Flutter uygulamasında (Haber Akışı kalıbı: `.github/workflows/news-ios-release.yml`) — ÇALIŞTI: 1.0.0 (yapı 6) 20 Kasım 2026, ~24 dk
- `runs-on: macos-26` (Firebase iOS SDK Swift 6.2 → Xcode 26). `flutter build ios --release --no-codesign --build-name/--build-number --dart-define=…`
  (Pod'lar + Generated.xcconfig) → imza ayarları **yalnızca Runner hedefine** pbxproj'a yazılır (CODE_SIGN_STYLE Manual, DEVELOPMENT_TEAM,
  "Apple Distribution", PROVISIONING_PROFILE_SPECIFIER; komut satırından verilirse Pod hedefleri "does not support provisioning profiles" der)
  → `xcodebuild -workspace Runner.xcworkspace archive` → exportArchive upload. Entitlements `aps-environment` CI'da `production` yapılır.
- `asc_signing.py` ek özellikler: `BUNDLE_CAPABILITIES=PUSH_NOTIFICATIONS` → bundle kimliği yoksa API ile kaydedilir, yetenek açılır;
  `REQUIRE_APP_RECORD=1` → App Store Connect'te uygulama kaydı yoksa "UYGULAMA_KAYDI_YOK" ile durur (uygulama kaydı API ile oluşturulamaz → kullanıcı New App).
- Firebase iOS ayarı elle kopyalanmaz: `firebase_ios_config.py` servis hesabıyla (FIREBASE_SERVICE_ACCOUNT sırrı) Firebase Management API'den
  iOS uygulamasını oluşturur/bulur, `GoogleService-Info.plist` değerlerini maskeli GITHUB_ENV'e yazar → `--dart-define=FIREBASE_IOS_*`.
  APNs anahtarı (.p8) Firebase'e yalnızca konsoldan yüklenir (kullanıcı işi).
  Telefondan yapıştırılan servis hesabı JSON'u bozuk gelir (sarılmış satırlar, `\` + satır sonu + `n`): katı JSON okuma yerine
  sunucudaki `scripts/init-admin.js` onarım mantığı (olduğu gibi → satır sonu kaçışla → sarmayı sil, anahtarı base64'ten yeniden kur).
- `macos-26` etiketi Kasım 2026'da kuyrukta 12+ dk sunucu alamadı → `macos-15-intel` (Xcode 26 var) kullan.
- App Store görselleri: Play için `flutter test tool/promo/capture.dart` ile yakalanan gerçek ekranlar → Playwright ile 440×956 @3x
  iPhone çerçevesi (dynamic island, 9:41) → 1320×2868. Çerçeve ekranı yakalanan ekranın oranında olsun (alt menü kesilmesin).

### Kotlin Multiplatform (Compose MP) uygulamasında (Minik Gurme kalıbı: `.github/workflows/minik-gurme-ios-release.yml`) — ÇALIŞTI 21 Kasım 2026
- `macos-15-intel` + `actions/setup-java` 21 + XcodeGen; `local.properties`'e `sdk.dir=$ANDROID_HOME` (Gradle projesi Android eklentisi ister).
  Xcode ön-derleme betiği `./gradlew :shared:embedAndSignAppleFrameworkForXcode` çalıştırır; tek hedef olduğu için imza ayarları komut satırından
  (`CODE_SIGN_STYLE=Manual … PROVISIONING_PROFILE_SPECIFIER=$APP_PROFILE`). Kotlin/Native yayın derlemesi Intel'de ~30 dk → timeout 120.
- İlk sürüm aboneliksiz: ortak kodda `PlusManager.purchasesEnabled` bayrağı; iOS'ta kapalıyken kilitli/satın alınamayan öğe gösterme (Apple 2.1/3.1.1).
- Mağaza görselleri: Robolectric ekran testinin kopyası `qualifiers = "w402dp-h874dp-xxhdpi"` → doğrudan 1206×2622 ham ekran.
'te kullanıcıyı yönlendirme (2026 arayüzü)
Sıra (`templates/APPLE-HESAP-REHBERI.md`): Developer Program (Bireysel, 99$) → Team ID → Identifiers (App Group, App ID'ler, widget) → **Business**: Paid Apps sözleşmesi, banka, W-8BEN, DSA → New App → API anahtarı → GitHub sırları → AdMob iOS → sürüm sayfası → yükleme → Submit.
- Menü: uygulama adının yanındaki **›** → General (App Information, App Compliance, **App Review**), Trust & Safety (**App Privacy**). Sürüm: "1.0.0 Prepare for Submission".
- "Add for Review" eksik listesi: Content Rights, Primary Category (boş kalabiliyor), **Privacy Policy URL her dil için ayrı**, "Sign-in required" işaretli kalmışsa kaldır. Sonra **Draft Submission → Submit for Review**.
- Açıklamada emoji ve `→` "invalid characters" hatası verir. Ekran görüntüsü 6,9" **1320×2868**; yalnız "medium display" yuvası açıksa **1206×2622**.
- Banka (Türkiye): IBAN değil **Banka kodu + Şube kodu + 7 haneli hesap no**; hesap sahibi adı Apple hesabıyla aynı.
- W-8BEN: Foreign TIN = TCKN, Treaty ülkesi Türkiye. DSA: reklam/satın alma varsa **Trader** (AB'de iletişim bilgisi görünür).
- App Privacy (AdMob varsa): yaklaşık konum, cihaz kimliği (tracking **Evet**), ürün etkileşimi, reklam verisi, çökme/performans/diğer tanılama; hiçbiri kimliğe bağlı değil.
- Yaş: hepsi None, reklam Evet → 4+. Ürünü olmayan Premium ekranını ilk sürümde bayrakla gizle (2.1 reddi). Abonelikte EULA + gizlilik bağlantısı + "Geri yükle" şart (3.1.2).
- In-App Purchase için önce Paid Apps sözleşmesi + banka onayı (~24 saat).

## D) Ekran görüntüleri
- iOS: `templates/appstore_shots.ornek.sh` kalıbı — simülatör "iPhone … Pro Max", `simctl status_bar override --time 9:41`, @AppStorage değerleri `-anahtar DEĞER` başlatma argümanıyla (tema, demo verisi, saat), ısınma açılışı + boş kare kontrolü. İş akışı `macos-15-intel`, sonuç `docs/appstore/raw/{tr,en}` dalına commit.
- Görüntülerde kişisel ad olmasın ("Merhaba Özkan" yerine saate göre "Hayırlı Akşamlar"); uygulamanın renk temalarını görüntülere dağıt.
- Play: Robolectric ile ekran render testleri (`StoreShotsTest` kalıbı) + tanıtım videosu (`android-compose-app` → "Play tanıtım videosu").

## Teslim kontrol listesi
- [ ] Testler geçti, sayısını söyle
- [ ] Sürüm kodu arttı, paket adı ve imza aynı (tabloyla)
- [ ] Değişiklikler commit + çalışılan dala push
- [ ] AAB'ler gönderildi + Play adımları + sürüm notu metni
- [ ] Mağaza metni değiştiyse kopyalanabilir blok
- [ ] Öğrenilen yeni hata/çözümü bu dosyaya ekle

## App Review "2.1 Information Needed" (ekran kaydı + 6 soru) — kanıtlanmış yol
- Apple **gerçek cihazda** ekran kaydı ister; simülatör kaydı (XCUITest/integration_test + `simctl recordVideo`) 1,5 saat sürüyor ve klavye tanıtımı, boş topluluk gibi engellere takılıyor → **kullanıcıya TestFlight ile ödünç iPhone'da çektir**.
- TestFlight: ASC → App → TestFlight → **Create Group** (Internal) → Testers (+) kendisi → davet maili; bağlantıyı herhangi bir tarayıcıda açınca 8 haneli kod çıkar. iPhone'da TestFlight → **Kodu Kullan** (ikinci uygulamada: sağ üst profil simgesi → Kodu Kullan). Telefondaki Apple hesabı başkasının olabilir.
- Videoya giren adımları madde madde ver: uygulamayı kapalıyken kaydı başlat, ilk açılış, ana ekranlar, UGC varsa **başka kullanıcının** gönderisinde Şikâyet/Engelle (kullanıcı Android'den gönderi yazar) ve hesap silmeyi **onaylayarak** göster.
- Video alma: Google Drive bağlantısı 10 MB'a kadar indirir; büyükse dosyayı "Bağlantıya sahip herkes" yapıp GitHub Actions'ta `drive.usercontent.google.com/download?id=..&confirm=t` ile indir (bu ortamda drive.google.com engelli). WhatsApp'tan geçen video 384×832'ye düşer ama kabul edilir.
- Not + ek: ASC API (`appStoreReviewDetails` PATCH notes, `appStoreReviewAttachments`) çalışıyor. Notta "physical iPhone, TestFlight build X" yaz.
- **Yeniden gönderim API'den olmuyor** (409 "Version is not ready"): sürümü videodaki derlemeye bağla (Build → eskiyi (–), yeniyi ekle), App Review'da Apple mesajına **Reply**, sonra **Resubmit to App Review** (kullanıcı UI'da yapar) → "Waiting for Review".
- `workflow_dispatch`-only iş akışı varsayılan olmayan dalda kayıtlı değildir (404): bir kez `push` tetikleyicisiyle gönder, sonra sayısal workflow id ile dispatch et.

## App Review "4.3(a) Spam" (kalabalık kategori: ezan/namaz vakti, hava durumu, fener vb.)
- Kod kopyası iddiası değildir: "konsept ve meta veri, kategorideki çok sayıda uygulamaya benziyor" demektir. Az değiştirip tekrar göndermek hesabı riske atar → **tek seferde** belirgin farkla git.
- Etkili fark: platforma özgü özellikler (Canlı Etkinlik + Dynamic Island, etkileşimli widget "Kıldım", Siri/Kestirmeler), ürünü tek fikre daraltma (ör. "vakit + namaz takibi + seri + kaza"), türün herkesteki ikincil içeriğini (tebrik kartı, kandil listesi, esmaül hüsna) ilk sürümden çıkarma.
- Mağaza: türün genel kelimeleri olmayan yeni ad + alt başlık, cami/hilal dışı ikon, ilk 3 ekran görüntüsünde farklı özellikler, açıklamanın ilk cümlesi "neden farklı".
- Gönderirken App Review'a **Reply**: yeni sürümde neyin değiştiğini madde madde yaz (ekran görüntüsüyle). Ret devam ederse App Review Board'a itiraz formu.
- Önizleme yöntemi: HTML + Playwright ile telefon çerçeveli mock (Inter fontu kurulu), `docs/yeni-tasarim/` altına PNG; kullanıcı onayından sonra SwiftUI'ye geçilir.
