# Haber Akışı — iOS (App Store) hazırlık rehberi

Bundle ID: **`akinai.app1`** (Android paket adıyla aynı) · Sürüm 1.0.0 · iPhone, iOS 15+ · Uygulama klasörü `flutter_app/`.

## Kodda hazır olanlar
- `flutter_app/ios/`: Info.plist (TR/EN, konum/ATT metinleri, `remote-notification`, SKAdNetwork, AdMob), `Runner.entitlements` (`aps-environment`), 19 simge (`docs/brand/logo.svg`), iPhone-only.
- Bildirim izni + FCM konuları (iOS'ta APNs jetonu beklenir), ön planda yerel bildirim, App Check (App Attest), platforma göre AdMob kimlikleri.
- Sunucu: maç olay uyarıları (gol, kırmızı kart, devre arası, maç sonu) iOS'a `apns` ile normal bildirim gider; gece 00:00–08:00 sessiz/pasif.
- Derleme: `.github/workflows/news-ios-build.yml` (imzasız doğrulama) ve **`.github/workflows/news-ios-release.yml`** (imzalı → App Store Connect/TestFlight; Mac, iPhone ve Codemagic gerekmez). `codemagic.yaml` yedek olarak duruyor, kullanılmıyor.
- İmzalı yükleme Ezan & Vakit'teki çalışan yöntemle aynı: App Store Connect API anahtarıyla geçici dağıtım sertifikası + App Store profili oluşturulur, iş bitince silinir (`ios/scripts/asc_signing.py`). Bundle kimliği `akinai.app1` yoksa **Push Notifications** yeteneğiyle kendiliğinden kaydedilir.
- Firebase iOS ayarları (`GoogleService-Info.plist` değerleri) derleme sırasında depodaki `FIREBASE_SERVICE_ACCOUNT` ile otomatik alınır; Firebase'de iOS uygulaması yoksa oluşturulur (`ios/scripts/firebase_ios_config.py`). Elle kopyalama yok.
- App Store ekran görüntüleri hazır: `docs/ios/ekran-goruntuleri/` (7 adet, 1320×2868; `olustur.js` ile yeniden üretilir).

## iOS'ta olmayanlar (dürüst liste)
- **Canlı skor paneli (kilit ekranı, zaman çizelgesi) yalnızca Android'dedir.** iOS'ta aynı olay uyarıları normal bildirim olarak gelir. iOS karşılığı "Live Activities" ayrı bir yerel uzantı gerektirir (sonraki sürüm işi).
- Gol sesi/düdük özel sesleri Android kanallarına bağlıdır; iOS'ta varsayılan bildirim sesi.
- Bildirimdeki "Daha az bildirim" düğmesi yok (ayar uygulama içinden).
- Mağaza açıklamasında canlı panel iOS için **yazılmamalı** (aşağıdaki metin buna göre).

## SENİN yapacakların (sırayla)
Apple geliştirici hesabı, sözleşmeler, banka/vergi ve GitHub sırları (APPLE_TEAM_ID, ASC_KEY_ID, ASC_ISSUER_ID, ASC_PRIVATE_KEY)
Ezan & Vakit için zaten yapıldı; aynı depoda olduğu için **tekrar gerekmez**.

1. **Uygulama kaydı:** App Store Connect → **Apps → + → New App**
   - Platform iOS · Ad **Haber Akışı** (alınmışsa *Haber Akışı: Gündem*) · Dil Türkçe
   - Bundle ID: listeden **akinai.app1** (ilk yükleme denemesi kimliği kendisi kaydeder; listede yoksa bana söyle)
   - SKU `akinai-app1` · Kullanıcı erişimi: Tam erişim
2. **Bildirim anahtarı (APNs):** developer.apple.com → **Certificates, Identifiers & Profiles → Keys → +**
   - Ad `Haber Akisi Push`, **Apple Push Notifications service (APNs)** kutusunu işaretle → Continue → Register → **Download** (.p8, bir kez iner)
   - Firebase Console → `haber-68a3c` → ⚙︎ **Proje ayarları → Cloud Messaging** → **Apple uygulaması yapılandırması** (akinai.app1)
     → **APNs kimlik doğrulama anahtarı → Yükle**: .p8 dosyası + Key ID + Team ID. (Firebase iOS uygulamasını ilk yükleme kendisi ekler.)
   - Bu adım olmadan iOS'ta bildirim gelmez; uygulamanın geri kalanı çalışır.
3. **AdMob (iOS):** admob.google.com → Uygulamalar → Uygulama ekle → **iOS** ("App Store'da yayında mı?" → Hayır), ad Haber Akışı
   → uygulama kimliği + 3 reklam birimi (**Banner**, **Yerel gelişmiş**, **Geçiş**). Kimlikleri bana gönder (gizli değiller);
   gelene kadar Google test reklamı çıkar.
4. Yüklemeyi bana "başlat" diyerek ya da `news-aggregator/flutter_app/ios/RELEASE_VERSION` dosyasındaki sürümü değiştirerek başlat.
   20–40 dk sürer, sonra yapı **TestFlight**'ta görünür.
5. Sürüm sayfasını aşağıdaki metinlerle doldur, `docs/ios/ekran-goruntuleri/` görsellerini yükle, yapıyı seç → **Add for Review → Submit for Review**.

## App Store girişi

| Alan | Değer |
|---|---|
| Ad (≤30) | Haber Akışı |
| Alt başlık (≤30) | Son dakika, gündem, deprem |
| Kategori | Haberler (ikincil: Spor) |
| Yaş derecelendirmesi | 4+ (şiddet/uygunsuz içerik yok; haber bağlantıları web içeriğidir → "sınırsız web erişimi" **Hayır**, uygulama içi web görünümü yalnız yayıncı haber sayfaları) |
| Gizlilik politikası URL | https://haber-68a3c.web.app/gizlilik |
| Destek URL | https://haber-68a3c.web.app/kunye |
| Pazarlama URL | https://haber-68a3c.web.app |
| Telif | © 2026 Özkan Akın |
| Fiyat | Ücretsiz |
| Dağıtım | Türkiye (+ isteğe bağlı diğer ülkeler) |

**Anahtar kelimeler (≤100):** `haber,son dakika,gündem,deprem,canlı skor,süper lig,döviz,altın,spor,ekonomi`

**Tanıtım metni (≤170):** Önemli haberler az ve öz bildirimle gelir. İlgi alanlarını seç; gün içinde sadece gerçekten önemli gelişmeleri al.

**Açıklama (TR, ≤4000):**
```
Haber Akışı, Türkiye'nin önde gelen haber kaynaklarından başlıkları tek, sade ve hızlı bir akışta toplar. İlgi alanlarınızı seçin; size uygun haberler gelsin.

TEK AKIŞTA GÜNDEM
• Gündem, ekonomi, dünya, spor, teknoloji, sağlık ve daha fazlası
• Haberin tamamı yayıncının kendi sitesinde, uygulama içinde açılır
• Aynı haberin tekrarları ayıklanır, akış temiz kalır
• Başlıkta arama, kaynak seçimi ve farklı görünüm seçenekleri

AZ AMA ÖNEMLİ BİLDİRİMLER
• Birden çok kaynağın doğruladığı önemli son dakika gelişmeleri
• Sabah, öğle ve akşam günün öne çıkanlarını özetleyen bültenler
• Bildirim yoğunluğunu siz seçin: Sadece önemli, Dengeli veya Tümü
• Gece 00:00–08:00 arası sessiz; deprem ve afet gibi olağanüstü durumlar hariç

SPOR BİLDİRİMLERİ
• Takip ettiğiniz Süper Lig takımlarının ve A Milli Takım'ın gol, kırmızı kart, devre arası ve maç sonu bildirimleri

PİYASA VE HAVA DURUMU
• Dolar ve Euro (TCMB gösterge kurları), gram ve ons altın
• Bulunduğunuz yerin hava durumu (isteğe bağlı, yaklaşık konum)

GİZLİLİĞİNİZE SAYGILI
• Üyelik gerekmez; ad, e-posta veya telefon istenmez
• Tercihleriniz yalnızca telefonunuzda saklanır

Haber Akışı ücretsizdir ve reklamlarla desteklenir. Haber içerikleri ilgili yayın kuruluşlarına aittir; uygulama yalnızca başlık, kısa özet ve bağlantıyı gösterir. İçerik kaldırma talepleri için: haberakisinfo@gmail.com

Piyasa bilgileri bilgilendirme amaçlıdır, yatırım tavsiyesi değildir.
```

**Açıklama (EN):** `docs/play-store/magaza-girisi.md` İngilizce metni; "LIVE SCORE PANEL" bölümünü şu satırla değiştir: *"SPORTS ALERTS • Goals, red cards, half-time and full-time alerts for your Süper Lig teams and the Turkish national team."*

### Uygulama gizliliği ("App Privacy") cevapları
Takip (tracking): **Evet** — AdMob reklam kimliği (IDFA), ATT izniyle. Toplanan veriler:
| Veri | Bağlı mı | Takip | Amaç |
|---|---|---|---|
| Yaklaşık konum (hava durumu; saklanmaz, sunucuya gitmez) | Hayır | Hayır | Uygulama işlevi |
| Cihaz kimliği / reklam verisi (AdMob) | Hayır | **Evet** | Üçüncü taraf reklam |
| Çökme verisi, performans (Crashlytics) | Hayır | Hayır | Analitik/uygulama işlevi |
| Ürün etkileşimi (reklam SDK) | Hayır | Evet | Reklam |
Hesap, ad, e-posta, telefon, satın alma: **toplanmıyor**.

### Şifreleme / ihracat
`ITSAppUsesNonExemptEncryption = false` (yalnızca standart HTTPS) → ihracat sorusu otomatik "muaf".

### İnceleme notları (App Review → Notes)
```
Haber Akışı herkese açık RSS/haber kaynaklarından başlık ve özetleri toplar; haberin tamamı yayıncının sitesinde açılır. Giriş/hesap yoktur. Konum izni yalnızca yaklaşık hava durumu içindir ve saklanmaz. Bildirimler isteğe bağlıdır (ilk açılışta sorulur). Reklamlar Google AdMob; izleme için ATT penceresi gösterilir. İçerik kaldırma: haberakisinfo@gmail.com
```
Demo hesap: gerekmez.

### İnceleme riski: 4.2 "minimum işlevsellik"
Haber toplayıcılar bazen "yalnızca web sarmalayıcı" itirazı alır. Önlem: uygulamanın yerel özellikleri (kişiselleştirilmiş akış, bildirim yoğunluğu, tekilleştirme, piyasa/hava panelleri, spor bildirimleri) inceleme notuna ve açıklamaya yazıldı.

## Ekran görüntüleri
**Hazır:** `docs/ios/ekran-goruntuleri/` — 7 görsel, 1320×2868 (6,9"), uygulamanın gerçek ekranlarından (sahte örnek veriyle). Canlı skor paneli iOS görsellerinde yok.
App Store iPhone 6,9" (1320×2868) zorunlu; 6,5" (1284×2778) de kabul. Android tanıtım görselleri (`docs/play-store/tanitim-gorselleri/`, 1080×1920) iOS'ta **kullanılamaz** (oran farklı, canlı panel görseli yanıltıcı olur). iOS için gerçek ekranlar iPhone/simülatörden alınmalı ya da `tool/promo` yöntemi 1320×2868 boyutuyla yeniden çalıştırılabilir (canlı panel karesi hariç).

## Sonra
- App Check: Firebase → App Check → iOS uygulaması → **App Attest** sağlayıcısını etkinleştir (zorunlu kılma kapalı kalsın).
- AdMob: iOS uygulamasını App Store girişine bağla; `app-ads.txt` zaten yayında.
- Sürüm: `ios/RELEASE_VERSION` (App Store sürümü), yapı numarası = iş akışının çalışma sayısı (otomatik artar).
