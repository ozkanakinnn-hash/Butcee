---
name: flutter-firebase-app
description: Flutter/FlutterFlow + Firebase (Firestore, Cloud Functions) uygulaması kurma — RSS/haber toplayıcı, WebView okuma ekranı, zamanlanmış Node.js fonksiyonları, mükerrer kontrolü, emülatör testleri ve CI. Flutter veya FlutterFlow ile yeni uygulama başlatırken ya da Firebase arka ucu yazarken kullan.
---

# Flutter + Firebase Uygulaması — Öğrenilenler (news-aggregator projesinden)

## 0. Kullanıcı kuralları (her uygulamada)
- **Türkçe + İngilizce** arayüz, Ayarlar'da dil seçici (Sistem/Türkçe/English). Flutter'da küçük `S` sınıfı
  (`S.of(context)`, `S.forLanguage('tr')`) + `flutter_localizations` + SharedPreferences ile kalıcı dil yeterli.
  FlutterFlow'da Settings → Languages (tr birincil, en ek) ve "Set App Language" aksiyonu.
- Mevcut bir depoya ikinci uygulama eklenirken kökü bozma: ayrı klasör (`news-aggregator/`) + yollara göre
  filtrelenen ayrı CI iş akışı (`on.push.paths`).
- FlutterFlow görsel araçtır, ajan tıklayamaz: adım adım rehber (`docs/FLUTTERFLOW_KURULUM.md`) + aynı mantığın
  çalışan Flutter kodu + FlutterFlow'a yapıştırılacak tek dosyalık özel bileşen ver; bunu kullanıcıya dürüstçe söyle.

## 1. Bulut konteynerinde Flutter
- SDK indirilebilir: `storage.googleapis.com/flutter_infra_release/releases/releases_linux.json` → `current_release.stable`
  → tar.xz'i scratchpad'e aç (arka planda, ~1 GB). pub.dev erişilebilir.
- `flutter create --platforms=android --org com.x --project-name y app` → `flutter analyze` + `flutter test` yerelde çalışır;
  APK derlemesi (Android SDK, dl.google.com engelli) CI'da: `subosito/flutter-action@v2` + `flutter build apk --debug`.
- FlutterFlow özel bileşenini doğrulamak için: FF içe aktarmalarını (`/backend`, `/flutter_flow`, `/custom_code`)
  `sed` ile silip geçici olarak `lib/` altına kopyala → `flutter analyze` → sil.
- FF özel bileşen kalıbı: "Automatic FlutterFlow imports ... DO NOT REMOVE" başlığı, sınıf adı = bileşen adı,
  `width`/`height` parametreleri, ek importlar başlığın altına; bağımlılıklar Widget Settings → Dependencies.

## 2. Firebase arka ucu (Node.js 22, firebase-functions v7, firebase-admin v14)
- Zamanlanmış iş: `onSchedule({schedule:'every 15 minutes', timeZone:'Europe/Istanbul', retryCount:0})`,
  `setGlobalOptions({region:'europe-west1', maxInstances:1})`. Blaze planı gerekir → bütçe uyarısı öner.
- **Mükerrer kontrolü**: belge kimliği = hash(normalleştirilmiş URL) + `bulkWriter.create()` (ALREADY_EXISTS=kod 6'yı
  yut) + önceden `db.getAll(refs, {fieldMask})` + `where('content_hash','in', ≤30'luk parçalar)`.
  URL normalleştirme: `utm_*`, `fbclid`, `#fragment` sil, host küçük harf.
- **Türkçe RSS kodlaması**: `fetch` → `arrayBuffer` → charset'i Content-Type'tan, yoksa `<?xml encoding>`'den bul →
  `new TextDecoder('windows-1254')` (Node ICU destekler) → `rss-parser.parseString`. Doğrudan URL ile ayrıştırma "�" üretir.
- ETag / Last-Modified'ı kaynak belgesinde sakla, `If-None-Match` gönder, 304'te ayrıştırma yapma. `AbortSignal.timeout`.
- HTML temizliği: script/style blokları → etiketler → `html-entities` decode (çift kodlama için 2 tur) → U+FFFD ve
  kontrol karakterleri → boşluk tekilleştirme → kelime sınırında kısalt. Görsel: enclosure → media:content →
  media:thumbnail → içerikteki ilk `<img>`; http→https yükselt (Android şifresiz trafiği engeller).
- Gelecek tarihli/geçersiz `pubDate` → şimdi; çok eski haberleri alma; gece eski kayıtları silen ikinci zamanlayıcı.
- Kaynak hatasını `sources/{id}.last_error`'a yaz (Console'dan izlenir), bir kaynağın hatası diğerlerini durdurmasın;
  eşzamanlılığı sınırlı tut (5).
- Güvenlik kuralları: içerik koleksiyonları `read: true, write: false` (Admin SDK kuralları atlar);
  `allow list: if request.query.limit <= 50` maliyet koruması — FlutterFlow'da sayfa boyutunu buna göre ayarla.
  Kuralları/dizinleri depodan `firebase deploy --only firestore` ile yükle; FF'nin kural yüklemesine basma uyarısı yaz.
- Kategori filtresi + tarih sıralaması için birleşik dizin (`category_id ASC, published_at DESC`) şart.

## 3. Test
- Saf fonksiyonlar `node --test test/*.test.js` (dizin değil glob ver; `node --test test/` MODULE_NOT_FOUND verir).
- Ağ yerine `fetchImpl` enjekte et: `new Response(bytes, {headers})`. Python ile `cp1254` kodlu fikstür üret.
- Uçtan uca: `npx -y firebase-tools@15 emulators:exec --only firestore --project demo-haber "npm test"`
  (Java gerekir; `demo-` önekli proje gerçek servislere gitmez). Emülatör testini `FIRESTORE_EMULATOR_HOST` yoksa atla.
  İki ardışık çalıştırmada ikincinin 0 yazdığını doğrula.

## 4. Flutter kalıpları
- `Firebase.initializeApp()`'i try/catch ile sar; `google-services.json` yoksa çökme yerine "yapılandırılmadı" ekranı.
  Gradle: `if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")`, json `.gitignore`'da,
  CI'da gizli değişkenden yaz.
- Ana manifest'e `INTERNET` izni (flutter create yalnızca debug/profile'a koyar) ve url_launcher için `<queries>`
  (VIEW https, SENDTO mailto, DIAL).
- Sonsuz kaydırma: `limit(20)` + `startAfterDocument(last)`, `extentAfter < 600` ile tetikle, `RefreshIndicator` +
  `AlwaysScrollableScrollPhysics` (boş listede de çekilebilsin), sekmelerde `AutomaticKeepAliveClientMixin`.
  Kategori sayısı değişince `DefaultTabController`'a `ValueKey` ver.
- WebView (`webview_flutter` 4.x): `NavigationDelegate` → http/https dışı şemaları `launchUrl(externalApplication)`;
  `onWebResourceError` yalnızca `isForMainFrame` ise hata ekranı; `PopScope(canPop:false)` + `canGoBack()` ile geri tuşu;
  `fullscreenDialog: true` rota → solda X. Paylaşım: `SharePlus.instance.share(ShareParams(text:, subject:))` (share_plus 11+ API).
- Flutter 3.4x: `Radio` için `RadioGroup` (groupValue/onChanged eski Radio'da kullanımdan kalktı); kullanılmayan
  closure parametreleri `(_, _)` yazılabilir.

## 5. Haber/içerik uygulamaları — yasal
- Tam metin kopyalama yok: başlık + kısa RSS özeti + bağlantı, detay orijinal sitede WebView'de.
- Ayarlar'da KVKK/GDPR gizlilik, kullanım koşulları ve "Uyar-Kaldır" telif e-postası (`mailto:` konu/gövde
  `Uri.encodeComponent` ile — `queryParameters` boşluğu "+" yapar).
- Yalnızca RSS'ini herkese açık sunan kaynaklar; seed'deki RSS adreslerini yayından önce doğrulat.

## 6. Kullanıcı "Firebase'e girip sen yap" derse
- Konteynerde Google oturumu yok (`gcloud auth list` boş); `firebase login` tarayıcı onayı ister → ajan giriş yapamaz.
  Proje açma ve Blaze (kredi kartı) de kullanıcıya ait. Bunu dürüstçe söyle, kalan her şeyi otomatikleştir.
- Çözüm: `workflow_dispatch` iş akışı (`news-firebase-deploy.yml`): `FIREBASE_SERVICE_ACCOUNT` gizli değişkeni →
  `GOOGLE_APPLICATION_CREDENTIALS` dosyası → `firestore:databases:get` yoksa `firestore:databases:create "(default)" --location europe-west1`
  → `firebase deploy --only firestore,functions --non-interactive --force` → seed → ilk çekim → anahtarı sil.
- Hizmet hesabı rolleri: Editor + Service Account User + Firebase Admin. Kullanıcının adımları: proje aç, Blaze,
  hizmet hesabı JSON anahtarı, GitHub secret, "Run workflow".

## 7. Ücretsiz (Spark) test modu — kullanıcı ödeme yapmak istemezse
- Spark'ta Cloud Functions YAYINLANAMAZ; Firestore + Admin SDK erişimi serbest. Aynı ingest kodunu GitHub Actions
  `schedule` ile çalıştır (`news-fetch-free.yml`, cron `7,37 * * * *`); herkese açık depoda dakika ücretsiz.
- Anahtar: Firebase Console → Proje ayarları → Hizmet hesapları → "Yeni özel anahtar" (Cloud Console'a gerek yok).
  JSON'u `${{ runner.temp }}`'e yaz (workspace'e değil; checkout'tan önce yazılırsa karışır), sonunda sil.
  Secret yoksa iş akışı hata vermesin: `::notice::` + adımları atla (yoksa her 30 dk'da hata e-postası gider).
- Proje kimliğini SA JSON'undaki `project_id`'den oku (`scripts/init-admin.js`); Admin SDK `options.projectId`'yi boş bırakır.
- Kurallar ve birleşik dizin Console'dan elle (yapıştır/oluştur) — ücretsiz ve ek yetki gerektirmez.
- Zamanlanmış/elle tetiklenen iş akışları yalnızca varsayılan dalda tanımlıysa görünür → kullanıcıya merge gerektiğini söyle.
- Kota: 50k okuma / 20k yazma / gün; getAll ile var olan kimlik kontrolü her belge için okuma sayar → sıklığı 30 dk tut.
  Kota dolarsa ücret çıkmaz, istek reddedilir. GitHub 60 gün commit olmazsa cron'u durdurur.
