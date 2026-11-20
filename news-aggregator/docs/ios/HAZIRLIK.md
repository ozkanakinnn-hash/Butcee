# Haber Akışı — iOS (App Store) hazırlık rehberi

Bundle ID: **`akinai.app1`** (Android paket adıyla aynı) · Sürüm 1.0.0 · iPhone, iOS 15+ · Uygulama klasörü `flutter_app/`.

## Kodda hazır olanlar
- `flutter_app/ios/`: Info.plist (TR/EN, konum/ATT metinleri, `remote-notification`, SKAdNetwork, AdMob), `Runner.entitlements` (`aps-environment`), 19 simge (`docs/brand/logo.svg`), iPhone-only.
- Bildirim izni + FCM konuları (iOS'ta APNs jetonu beklenir), ön planda yerel bildirim, App Check (App Attest), platforma göre AdMob kimlikleri.
- Sunucu: maç olay uyarıları (gol, kırmızı kart, devre arası, maç sonu) iOS'a `apns` ile normal bildirim gider; gece 00:00–08:00 sessiz/pasif.
- Derleme: `.github/workflows/news-ios-build.yml` (imzasız doğrulama) ve `codemagic.yaml` (imzalı → TestFlight).

## iOS'ta olmayanlar (dürüst liste)
- **Canlı skor paneli (kilit ekranı, zaman çizelgesi) yalnızca Android'dedir.** iOS'ta aynı olay uyarıları normal bildirim olarak gelir. iOS karşılığı "Live Activities" ayrı bir yerel uzantı gerektirir (sonraki sürüm işi).
- Gol sesi/düdük özel sesleri Android kanallarına bağlıdır; iOS'ta varsayılan bildirim sesi.
- Bildirimdeki "Daha az bildirim" düğmesi yok (ayar uygulama içinden).
- Mağaza açıklamasında canlı panel iOS için **yazılmamalı** (aşağıdaki metin buna göre).

## SENİN yapacakların (sırayla)
1. **Apple Developer Program** (99 $/yıl): developer.apple.com/programs → kayıt. Onay 1–2 gün sürebilir.
2. **App Store Connect → Uygulamalarım → +** : ad "Haber Akışı", dil Türkçe, **Bundle ID `akinai.app1`** (önce developer.apple.com → Identifiers'ta oluştur; **Push Notifications** yeteneğini işaretle), SKU `akinai-app1`.
3. **APNs anahtarı**: developer.apple.com → Keys → + → "Apple Push Notifications service (APNs)" → indir (**.p8**, bir kez indirilir) + Key ID + Team ID not al.
4. **Firebase Console** (`haber-68a3c`) → Proje ayarları → Uygulamalarım → **iOS uygulaması ekle** (bundle `akinai.app1`). Ardından Cloud Messaging sekmesi → **APNs kimlik doğrulama anahtarı** → .p8 + Key ID + Team ID yükle. `GoogleService-Info.plist` içinden şu 4 değeri not et: `API_KEY`, `GOOGLE_APP_ID`, `GCM_SENDER_ID`, `STORAGE_BUCKET` (bunları sohbete yazma; Codemagic'e gir).
5. **AdMob** → Uygulamalar → Uygulama ekle → **iOS** → uygulama kimliği + 3 reklam birimi (banner, yerel gelişmiş, geçiş). *(Hazır olana kadar test reklamı çıkar.)*
6. **Codemagic** (codemagic.io, GitHub ile giriş, depoyu bağla): Team integrations → App Store Connect → API anahtarı (App Store Connect → Kullanıcılar ve Erişim → Anahtarlar, rol "App Manager") oluşturup adını `Codemagic` ver. Environment variables → grup **`haber_akisi_ios`** (secure): `FIREBASE_IOS_API_KEY`, `FIREBASE_IOS_APP_ID`, `FIREBASE_IOS_SENDER_ID`, `FIREBASE_IOS_STORAGE_BUCKET`, `ADMOB_IOS_APP_ID`, `ADMOB_BANNER_ID_IOS`, `ADMOB_NATIVE_ID_IOS`, `ADMOB_INTERSTITIAL_ID_IOS`, `APP_STORE_APPLE_ID` (App Store Connect → uygulama → Genel bilgiler → Apple ID, sayı). Code signing identities → iOS → "Automatic" ile dağıtım sertifikası/profil oluştur.
7. Codemagic'te **haber-akisi-ios** iş akışını başlat → IPA TestFlight'a gider. (Birkaç iterasyon gerekebilir; hata günlüğünü bana gönder.)
8. **TestFlight**: kendi iPhone'unda dene (bildirim izni, haber akışı, reklam, hava durumu).
9. Mağaza girişini doldur (aşağıdaki metinler), ekran görüntülerini yükle, **İncelemeye gönder**.

## App Store girişi

| Alan | Değer |
|---|---|
| Ad (≤30) | Haber Akışı |
| Alt başlık (≤30) | Gündem, skor ve piyasa tek akışta |
| Kategori | Haberler (ikincil: Spor) |
| Yaş derecelendirmesi | 4+ (şiddet/uygunsuz içerik yok; haber bağlantıları web içeriğidir → "sınırsız web erişimi" **Hayır**, uygulama içi web görünümü yalnız yayıncı haber sayfaları) |
| Gizlilik politikası URL | https://haber-68a3c.web.app/gizlilik |
| Destek URL | https://haber-68a3c.web.app/kunye |
| Pazarlama URL | https://haber-68a3c.web.app |
| Telif | © 2026 Özkan Akın |
| Fiyat | Ücretsiz |
| Dağıtım | Türkiye (+ isteğe bağlı diğer ülkeler) |

**Anahtar kelimeler (≤100):** `haber,son dakika,gündem,canlı skor,süper lig,döviz,altın,hava durumu,spor,ekonomi`

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
App Store iPhone 6,9" (1320×2868) zorunlu; 6,5" (1284×2778) de kabul. Android tanıtım görselleri (`docs/play-store/tanitim-gorselleri/`, 1080×1920) iOS'ta **kullanılamaz** (oran farklı, canlı panel görseli yanıltıcı olur). iOS için gerçek ekranlar iPhone/simülatörden alınmalı ya da `tool/promo` yöntemi 1320×2868 boyutuyla yeniden çalıştırılabilir (canlı panel karesi hariç).

## Sonra
- App Check: Firebase → App Check → iOS uygulaması → **App Attest** sağlayıcısını etkinleştir (zorunlu kılma kapalı kalsın).
- AdMob: iOS uygulamasını App Store girişine bağla; `app-ads.txt` zaten yayında.
- Sürüm numarası: `pubspec.yaml` `1.0.0+100`; Codemagic yapı numarasını otomatik artırır.
