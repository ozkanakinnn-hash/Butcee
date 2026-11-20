# Devir notu — Ezan & Vakit (önceki sohbetin devamı)

**Tarih:** 13 Kasım 2026 • **Depo:** `ozkanakinnn-hash/Butcee` • **Çalışma dalı:** `claude/taslak-uygulamasi-vn0y2s`
**Kullanıcı tercihleri:** Türkçe yanıt; öğrenilen beceriler `.claude/skills/android-compose-app/SKILL.md`'ye kaydedilir; İngilizce destek zorunlu; iş bitince test APK/AAB bağlantısı gönderilir; görsel yeniden tasarımlarda önce önizleme + onay.

## Uygulama
- **Ad:** Ezan & Vakit (Play başlığı: "Ezan & Vakit: Ramazan Rehberi"). Kotlin/Compose telefon + Wear OS + iOS (SwiftUI).
- **Paket adı:** `akin.ai2` (Android telefon + saat + iOS bundle id). Kural: kullanıcının N. uygulaması `akin.ai<N>` (Haber Akışı = 1. uygulama).
- **Sürüm:** `gradle.properties` → `APP_VERSION_NAME=1.0.0`, `APP_VERSION_CODE=4` (her Play yüklemesinde +1).

## Yayın durumu (Google Play Console, hesap "Akin Ai", kişisel)
- Dahili test ve **Kapalı test – alpha** yayında (1.0.0, kod 1). Kod 2–4 kullanıcıya gönderildi; **kod 4 (Firebase'li) kapalı teste yüklenecek**.
- Test kullanıcıları: **E-posta listesi "Aile"** (4 kişi). 12 kişi × 14 gün şartı sürüyor. Davet sayfası: https://ozkanakinnn-hash.github.io/Butcee/ezan-vakit/test.html (e-posta akışı). Metinler: `docs/test-davet/paylasim-metinleri.md`.
- **Açık sorun:** test kullanıcıları katılım sayfasında "Test kullanıcısı ol" düğmesini görmüyor / "öğe bulunamadı". Gizli sekmede doğru hesapla da denendi. Sıradaki adım: **Yayın özeti**'nde gönderilmemiş değişiklik / yönetilen yayınlama kontrolü + katılım sayfası ekran görüntüsü istenecek.
- Gizlilik politikası: https://ozkanakinnn-hash.github.io/Butcee/ezan-vakit/privacy-policy.html (GitHub Pages, `gh-pages` dalı, klasör `ezan-vakit/`; aynı sitede `minik-gurme/` da var — Pages kaynağını değiştirme). İletişim: ozkan.akinnn@gmail.com.
- Mağaza metinleri: `docs/store/magaza-metni.md`; görseller: `docs/store/ezan-vakit-play-gorselleri.zip` (8 ekran + öne çıkan + ikon), üretici `scripts/store/`.

## Gizli / depoda OLMAYAN dosyalar (yeni oturumda kullanıcıdan tekrar iste)
- **Yükleme anahtarı:** `my-upload-key.jks` + şifre dosyası `YUKLEME-ANAHTARI-SIFRE.txt` (alias `upload`, 4296 bayt). Parmak izi SHA1 `E5:7E:0E:27:04:FD:AA:EF:E3:F3:78:F1:83:06:CC:0D:74:FE:0F:A5`, SHA256 `5B:99:FB:12:00:50:FE:23:41:2A:F9:2A:9A:96:73:BE:4A:5E:0A:9D:5B:96:E4:E5:10:2D:84:8E:21:16:12:F2`. Kullanıcıya 13 Kasım 00:57'de (TR) "Ramazan rehperi" oturumunda dosya kartı olarak gönderildi; şifre hiçbir kayıtta açık yazmıyor. Derleme: `KEYSTORE_PATH=… STORE_PASSWORD=… KEY_PASSWORD=… ./gradlew :app:bundleRelease`.
- **Firebase:** `app/google-services.json` (proje `ezan-vakit-10190`, paket `akin.ai2`) — `.gitignore`'da; kullanıcıda kopyası var.
- AdMob kimlikleri `gradle.properties`'te (gizli değil). Gerçek reklam yalnızca `-PprodAds=true` üretim paketinde; kapalı test paketi test reklamlı.

## Derleme ortamı notları
- Android SDK `/root/android-sdk` (`local.properties` gitignore'da: `sdk.dir=/root/android-sdk`); `--max-workers=1-2` (Maven Central 429 verir, tekrar dene). Robolectric testleri: `:app:testDebugUnitTest` (44 test geçiyor).
- Ağ izinleri ortamda: dl.google.com, Diyanet/ezanvakti/aladhan, wikimedia.

## Son yapılanlar (bu sohbet)
Kıble modülü sıfırdan (Diyanet yöntemi, 141° birebir), vakitler Diyanet ilçe tablosundan (ezanvakti.emushaf.net), Cuma hatırlatıcısı, canlı iftar sayacı, fitre/fidye/zekât, imsakiye PDF, yedekleme, paket adı akin.ai2, ad Ezan & Vakit, SCHEDULE_EXACT_ALARM, Play Billing 8.3.0, edge-to-edge düzeltmeleri, fragment 1.8.9, Firebase + FCM kimliği kopyalama.

## 20 Kasım 2026
- Bildirim düzeltmesi 1.0.1 (kod 5) imzalı AAB'leri kullanıcıya gönderildi (Play'e yüklenecek).
- Kıble ekranı sadeleştirildi + açılışta 8 çizme kalibrasyon animasyonu (Android + iOS). Önizleme: `docs/kible-sade-onizleme.png`, `docs/kalibrasyon-animasyonu.gif`. Henüz yeni Android sürümüne girmedi.
- App Store hazırlığı: `.github/workflows/ios-release.yml` (TestFlight'a yükleme; sırlar GitHub'da), `ios-appstore-screenshots.yml` (6,9" TR/EN → `docs/appstore/raw/`), mağaza metinleri `docs/appstore/app-store-metni.md`, kullanıcı rehberi `docs/appstore/APPLE-HESAP-REHBERI.md`. iOS AdMob kimlikleri `ios/project.yml` (GAD_APP_ID / GAD_BANNER_ID) — gerçekleri kullanıcıdan bekleniyor.
- Gizlilik politikası iOS'u kapsayacak şekilde + `support.html` hazırlandı; gh-pages'e gönderilmesi kullanıcı onayı bekliyor.

## Bekleyen işler
1. Kapalı test "Test kullanıcısı ol" sorununu çöz (ekran görüntüleri iste).
2. Kod 4'ü kapalı teste yükle → Firebase "Test mesajı gönder" ile bildirimi doğrula.
3. 12 test kullanıcısına ulaş → 14 gün → üretim erişimi başvurusu.
4. Üretim paketi (`-PprodAds=true`) ve app-ads.txt (kök alan adı için `ozkanakinnn-hash.github.io` deposu gerekir).
5. Meal telif kararı (Diyanet izni ya da telifsiz meal); tam ezan sesleri lisanslı kayıtla.
6. Yeni özelliklerin iOS'a aktarılması (Cuma, canlı sayaç, hesaplayıcılar, imsakiye, yedekleme).
