# Haber Akışı — Android Haber Toplayıcı (Flutter + Firebase)

Herkese açık RSS kaynaklarından haberleri otomatik toplayan, kategorilere ayıran ve haberin detayını
**orijinal sitesinde, uygulama içi WebView ile** açan Android uygulaması. Haber metni kopyalanmaz;
yalnızca başlık, kısa RSS özeti, görsel adresi ve bağlantı saklanır.

```
news-aggregator/
├── firebase/                  # Backend
│   ├── firestore.rules        # Okuma herkese açık, yazma yalnızca Cloud Function
│   ├── firestore.indexes.json # category_id + published_at birleşik dizini
│   └── functions/             # Node.js 22 Cloud Functions
│       ├── index.js           # fetchRssFeeds (15 dk'da bir), cleanupOldArticles (gece 04:00)
│       ├── src/feed.js        # İndirme, Türkçe karakter kodlaması, ETag/304 desteği
│       ├── src/text.js        # HTML temizleme, URL normalleştirme, görsel bulma, hash
│       ├── src/ingest.js      # Mükerrer kontrolü + Firestore'a yazma
│       ├── scripts/seed.js    # Örnek kategori ve kaynakları yükler
│       └── test/              # Birim testleri + Firestore emülatörü uçtan uca testi
├── flutter_app/               # Çalışan Flutter uygulaması (FlutterFlow'suz da kullanılabilir)
│   └── lib/
│       ├── backend/           # ArticlesRecord / CategoriesRecord / SourcesRecord + sorgular
│       ├── pages/home_page.dart            # Kategori sekmeleri, sonsuz kaydırma, yenileme
│       ├── pages/article_webview_page.dart # WebView okuma ekranı (X / kaynak + kilit / paylaş)
│       └── pages/settings_page.dart        # Dil, KVKK/gizlilik, telif "Uyar-Kaldır"
├── flutterflow/custom_widgets/article_web_view.dart  # FlutterFlow'a yapıştırılacak özel bileşen
└── docs/FLUTTERFLOW_KURULUM.md                      # FlutterFlow veri bağlama adımları
```

## Uygulama özellikleri
- **İlgi alanları:** İlk açılışta kullanıcı takip edeceği kategorileri seçer (önerilenler hazır gelir).
  Sağ üstteki ⚙︎ düğmesinin yanındaki **ayar (tune) simgesi** veya Ayarlar → *İlgi alanlarını düzenle*:
  takip edilenler sürüklenerek sıralanır, **−** ile çıkarılır; *Keşfet* bölümündeki **+** ile eklenir.
  Sekmeler bu sırayla görünür. Seçim yalnızca cihazda saklanır.
- **Tema:** Ayarlar → Görünüm → Sistem / Açık / Koyu. Koyu tema tam siyah (#000000, OLED dostu);
  vurgu rengi #0A84FF. Tema kodu: `flutter_app/lib/theme.dart`.
- **Son Dakika:** `is_breaking: true` kaynakların haberleri `breaking_at` alanıyla işaretlenir; aynı haber
  başka bir akıştan önce kaydedilmişse o da işaretlenir. Sekme `orderBy('breaking_at')` ile listelenir
  (ek dizin gerekmez).
- **Uygulama ikonu:** `design/make_icon.py` üretir (mavi degrade + beyaz akış simgesi; uyarlanabilir ikon,
  yuvarlak ikon, Android 13 tema ikonu ve `design/play_store_icon_512.png`). Önizleme: `design/icon_preview.png`.
- **Bildirimler (FCM, ücretsiz):** Uygulama her ilgi alanı için `news_<kategori>` konusuna abone olur.
  RSS botu her çalışmada kategori başına en fazla 2 saatte bir özet gönderir (en yeni başlık + "ve N haber daha");
  Son Dakika beklemeden gider (`firebase/functions/src/notify.js`). Kanallar: news, breaking, goals (özel ses), match.
- **İzinler (Ayarlar):** Bildirim izni (Android 13+) ve yaklaşık konum izni; durum görünür, reddedildiyse sistem
  ayarlarına yönlendirir. Konum yalnızca **hava durumu kartı** için kullanılır (MET Norway, ücretsiz, ticari kullanıma açık).
- **Futbol:** "Futbol" ilgi alanı seçilince takım seçimi açılır (Süper Lig, en fazla 10 takım). Takımların maçları
  Tümü ve Futbol sekmelerinin üstünde canlı skorla görünür (Firestore'dan anlık). Golde sesli bildirim
  (`res/raw/goal_whistle.wav`, `design/make_goal_sound.py` ile sentezlenir), ayrıca maç başladı/devre/sonuç.
  Veri: API-Football; `news-football.yml` 15 dakikada bir başlar, maç varsa bitene kadar izler
  (`scripts/football-watch.js`). Ücretsiz planda günde 100 istek → güncelleme aralığı kalan isteğe göre
  otomatik ayarlanır (tek maçta ~1 dk, yoğun günde 3–5 dk). Daha sık güncelleme için ücretli plan gerekir.
- **Alt menü:** Ana Sayfa · Ara · Bildirimler · İlgi Alanları.
- **Arama:** Haber başlığı ve kaynakta, Türkçe karakterden bağımsız ("istanbul" = "İSTANBUL").
  Bot her habere `search_keywords` (kelimelerin 3–10 harflik ön ekleri) yazar; eski haberlere bir kerelik eklenir.
  Firestore'da `search_keywords (array-contains) + published_at DESC` birleşik dizini gerekir.
- **Bildirimler sekmesi:** Bot gönderdiği her bildirimi `notifications` koleksiyonuna yazar (7 gün saklanır);
  uygulama kullanıcının konularını (`news_*`, `team_*`) süzerek yeniden eskiye listeler.
- **Sağ üst panel (☰):** Dolar/TL, Euro/TL, gram altın (TL), ons altın ($), BIST 100, Brent; bot 30 dakikada bir
  Truncgil Finans'tan (ücretsiz, anahtarsız) çeker (`src/market.js`) → `market/latest`. Ons fiyatı gram altın ve
  dolardan hesaplanır; Brent'in fiyatı bu kaynakta çoğu zaman yok → fiyat yoksa gösterilmez.
  (Yahoo Finance GitHub sunucularını HTTP 429 ile reddediyor; koruması aşılmaz.)
  Kaynakları denemek için: Actions → **Piyasa Kaynak Kontrolü** (`scripts/probe-market.js`).
  Altında konumdaki hava durumu. Panelden Ayarlar'a da gidilir.
- **Haber dizilimi:** Ayarlar → Haber dizilimi: Standart (ilk haber büyük) / Büyük kart / Sade liste.
- **Kategoriler (14):** Son Dakika, Gündem, Ekonomi, Teknoloji, Spor, Futbol, Dünya, Sağlık, Bilim, Kültür-Sanat,
  Magazin, Otomobil, Eğitim, Yaşam.

## Canlı skor kurulumu (API-Football)
1. [dashboard.api-football.com](https://dashboard.api-football.com/register) → ücretsiz hesap → **Account → My Access**
   sayfasındaki API anahtarını kopyalayın.
2. GitHub → Settings → Secrets and variables → Actions → **New repository secret**: `FOOTBALL_API_KEY`.
3. Actions → **Haber Akışı - Canlı Skor** → Run workflow → mode: **check**. Kayıtta planınız, kalan istek ve
   "Güncel sezona erişim VAR/YOK" yazar. YOK ise ücretsiz plan bu sezonu kapsamıyordur.
4. mode: **sync-teams** ile takım listesini yükleyin (uygulamadaki takım seçimi bu listeyi kullanır).
5. Firestore kurallarına `football_teams` ve `matches` okuma izni ekleyin (`docs/UCRETSIZ_KURULUM.md` Adım 3).

## Yeni RSS kaynağı eklemek
1. Adayı `firebase/functions/scripts/feed-candidates.json` dosyasına ekleyin ve gönderin (push).
2. GitHub Actions → **Haber Akışı - RSS Kaynak Kontrolü** her adres için durum, haber sayısı ve görselli
   haber oranını yazar (bu bulut ortamından haber sitelerine erişilemediği için kontrol orada yapılır).
3. Çalışanları `scripts/seed-data.json` içine ekleyin → **Ücretsiz RSS Çekimi** iş akışını
   "örnek kategori ve kaynakları yükle" işaretli çalıştırın.

## Nasıl çalışır?

1. **Cloud Function `fetchRssFeeds`** her 15 dakikada `sources` koleksiyonundaki `is_active == true` kaynakları
   (aynı anda en fazla 5) indirir.
2. Her haber için: HTML etiketleri ve bozuk karakterler temizlenir, özet 300 karaktere kısaltılır,
   kapak görseli `enclosure → media:content → media:thumbnail → içerikteki ilk <img>` sırasıyla aranır,
   bulunamazsa kategorinin `placeholder_image_url` görseli atanır. 7 günden eski haberler alınmaz.
3. **Mükerrer kontrolü** (3 katman):
   - Belge kimliği = normalleştirilmiş `original_url`'nin hash'i (`utm_*`, `#yorumlar` gibi ekler silinir).
     Firestore `create()` aynı kimliği ikinci kez yazmaz.
   - Yazmadan önce kimlikler topluca okunur, var olanlar atlanır (gereksiz yazma maliyeti yok).
   - `content_hash` = başlık + yayın günü → aynı haber farklı adresle (ör. mobil site) gelirse de eklenmez.
4. Sitenin döndürdüğü `ETag`/`Last-Modified` saklanır; akış değişmemişse (HTTP 304) hiç işlem yapılmaz.
5. **`cleanupOldArticles`** her gece 30 günden eski haberleri siler (veritabanı boyutu ve maliyet sabit kalır).
6. **Okuma kotası koruması:** her kaynak, önceki çalışmada gördüğü en yeni haberin tarihini saklar
   (`last_seen_published_at`); bundan 45 dakikadan eski haberler veritabanına sorulmadan atlanır.
7. Hatalı kaynaklarda `sources/{id}.last_error` alanı doldurulur; Firebase Console'dan takip edebilirsiniz.

## Kurulum

> **Ücret ödemeden test etmek için:** [`docs/UCRETSIZ_KURULUM.md`](docs/UCRETSIZ_KURULUM.md) — Spark (ücretsiz) plan,
> RSS botu GitHub Actions'ta 30 dakikada bir çalışır. Aşağıdaki adımlar Blaze (Cloud Functions) içindir.

### 1. Firebase
1. [console.firebase.google.com](https://console.firebase.google.com) → yeni proje → **Firestore** (europe-west1).
2. **Blaze** planına geçin (zamanlanmış fonksiyon için şart) ve bir bütçe uyarısı koyun.
3. ```bash
   cd news-aggregator/firebase
   cp .firebaserc.example .firebaserc   # proje kimliğinizi yazın
   cd functions && npm install && cd ..
   npx firebase-tools login
   npx firebase-tools deploy --only firestore,functions
   ```

### 1b. Otomatik kurulum (bilgisayara bir şey kurmadan, GitHub Actions ile)
Yalnızca şu 4 adımı siz yaparsınız, gerisini iş akışı yapar:
1. **Firebase projesi** açın: [console.firebase.google.com](https://console.firebase.google.com) → Proje ekle.
   Proje kimliğini not edin (ör. `haber-akisi-12345`).
2. **Blaze planına** geçin (sol alttaki "Spark" → Yükselt) ve bir bütçe uyarısı koyun.
3. **Hizmet hesabı anahtarı** oluşturun:
   [console.cloud.google.com/iam-admin/serviceaccounts](https://console.cloud.google.com/iam-admin/serviceaccounts)
   → projenizi seçin → **Hizmet hesabı oluştur** → ad: `github-deploy` → roller:
   **Düzenleyici (Editor)**, **Hizmet Hesabı Kullanıcısı (Service Account User)**, **Firebase Admin** →
   Bitti → hesabı açın → **Anahtarlar → Anahtar ekle → JSON**. İnen dosyayı kimseyle paylaşmayın.
4. GitHub deposu → **Settings → Secrets and variables → Actions → New repository secret** →
   ad: `FIREBASE_SERVICE_ACCOUNT`, değer: JSON dosyasının **tüm içeriği**.

Sonra GitHub → **Actions → "Haber Akışı - Firebase'e Kur" → Run workflow** → proje kimliğini yazın → Çalıştır.
İş akışı; Firestore veritabanını (europe-west1) oluşturur, kuralları/dizinleri ve fonksiyonları yayınlar,
örnek kategori/kaynakları yükler ve ilk haberleri çeker. Kod değiştikçe aynı düğmeyle tekrar yayınlayabilirsiniz.

### 2. İlk verileri yükleme
Firebase Console → Proje Ayarları → Hizmet hesapları → **Yeni özel anahtar oluştur** (dosyayı depoya koymayın).
```bash
cd news-aggregator/firebase/functions
export GOOGLE_APPLICATION_CREDENTIALS=~/indirilenler/service-account.json
npm run seed          # 5 kategori + 7 örnek kaynak
npm run fetch:once    # zamanlayıcıyı beklemeden ilk haberleri çeker
```
Kaynak eklemek/çıkarmak için Firestore Console'da `sources` belgesini düzenlemeniz yeterli
(`is_active: false` yaparak geçici olarak kapatabilirsiniz). `scripts/seed-data.json` içindeki RSS adreslerini
yayından önce tarayıcıda açıp doğrulayın.

### 3a. Uygulama — FlutterFlow ile
[`docs/FLUTTERFLOW_KURULUM.md`](docs/FLUTTERFLOW_KURULUM.md) rehberini izleyin.

### 3b. Uygulama — Flutter koduyla (FlutterFlow'suz)
```bash
cd news-aggregator/flutter_app
# Firebase Console → Android uygulaması ekle (paket: com.haberakisi.haber_akisi)
# indirilen google-services.json → android/app/google-services.json
flutter pub get
flutter run
```
`google-services.json` olmadan da derlenir; uygulama açılışta "Firebase yapılandırılmadı" uyarısı gösterir.
Yayın öncesi `lib/config.dart` içindeki telif e-postası ve gizlilik/kullanım koşulları adreslerini değiştirin.

## Testler
```bash
cd news-aggregator/firebase/functions
npm test                                   # birim testleri
npx firebase-tools emulators:exec --only firestore --project demo-haber "npm test"   # + uçtan uca

cd ../../flutter_app && flutter analyze && flutter test
```
GitHub Actions (`.github/workflows/news-aggregator.yml`) bunları her push'ta çalıştırır ve debug APK üretir.

## Sonraki adımlar
- **AdMob** (akış içi native reklam her 6 haberde bir, 4–5 okumada bir geçiş reklamı) — Google UMP onay formu ile.
- `users` / `user_preferences`: favori kategoriler ve kaydedilen haberler (Firebase Auth ile; kurallar hazır).
- Play Store: uygulama kimliği (`applicationId`) ilk yüklemeden sonra değişmez — yayından önce kesinleştirin.
