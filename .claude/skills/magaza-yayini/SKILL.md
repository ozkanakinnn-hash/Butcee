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

## C) App Store Connect'te kullanıcıyı yönlendirme (2026 arayüzü)
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
