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
6. Hatalı kaynaklarda `sources/{id}.last_error` alanı doldurulur; Firebase Console'dan takip edebilirsiniz.

## Kurulum

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
