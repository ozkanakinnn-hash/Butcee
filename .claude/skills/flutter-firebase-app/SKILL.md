---
name: flutter-firebase-app
description: Flutter/FlutterFlow + Firebase (Firestore, Cloud Functions) uygulaması kurma — RSS/haber toplayıcı, WebView okuma ekranı, zamanlanmış Node.js fonksiyonları, mükerrer kontrolü, emülatör testleri ve CI. Flutter veya FlutterFlow ile yeni uygulama başlatırken ya da Firebase arka ucu yazarken kullan.
---

# Flutter + Firebase Uygulaması — Öğrenilenler (news-aggregator projesinden)

## 0. Kullanıcı kuralları (her uygulamada)
- **APK'yı kullanıcı "gönder" demeden GÖNDERME (2026-11-08).** Değişiklikten sonra derleyip hazır tut, "APK hazır,
  istersen gönderirim" de; dosyayı yalnızca açıkça istendiğinde paylaş.
- **Ses/görsel gibi zevke bağlı öğelerde** tek seçenek yerine 2–3 aday üretip dinlet/göster, seçimi kullanıcıya bırak.
  Tescilli sesleri/markaları (ör. Netflix "ta-dum") kopyalama; yalnızca tarzını üret ve bunu söyle.
- **PR'ları Claude birleştirir (kullanıcı izni, 2026-11-06):** CI (testler + APK derlemesi; release değiştiyse emülatör
  açılış kontrolü) geçtikten sonra PR'ı aç ve `merge_pull_request` ile birleştir; kullanıcıya hangi PR'ı neyle
  birleştirdiğini yaz. Varsayılan dal `main` değil olabilir (`git remote show origin`). Birleştirmeden sonra yeni iş için
  dalı varsayılan daldan yeniden başlat (merge edilmiş geçmişe ekleme yapma).
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
- **GitHub Actions tuzağı:** `runner` bağlamı iş (job) düzeyindeki `env:` içinde KULLANILAMAZ → dosya geçersiz sayılır,
  Actions listesinde ad yerine dosya yolu görünür ve her push'ta ❌ "failure" üretir. Geçici yolu adım içinde
  `$RUNNER_TEMP` ile kur, `echo "VAR=..." >> "$GITHUB_ENV"` ile sonraki adımlara aktar. Job `env:`'de yalnızca
  github/needs/strategy/matrix/vars/secrets/inputs. Push'tan sonra yeni iş akışının adla listelendiğini kontrol et.
- Deponun varsayılan dalı `main` olmayabilir: `git remote show origin | grep HEAD` ile bak; PR'ı ve "main'e al" talimatını ona göre ver.
- **Tabletten/telefondan kopyalanan JSON secret'ı sık bozulur** (eksik kopya, akıllı tırnak). Okuyan betik anahtarı
  ASLA yazdırmadan teşhis vermeli: uzunluk, `{`/`}` ile başlayıp bitme, `"private_key"`/`"client_email"` var mı,
  akıllı tırnak var mı, hata konumu (JSON.parse mesajı metinden parça içerebilir — yalnızca position'ı al).
  BOM ve baştaki/sondaki boşlukları tolere et; `cert(json)` ile başlat. Kullanıcıya en güvenilir kopyalama yolu:
  dosyayı Chrome'da `file://` ile değil, bir metin düzenleyicide açıp "Tümünü seç".
- Kopyalamada en sık bozulma: `private_key` içindeki `\n` kaçışlarının gerçek satır sonuna (CRLF) dönmesi →
  "Bad control character in string". Yalnızca dizge İÇİNDEKİ ham satır sonlarını `\n`'e çeviren küçük bir tarayıcı ile
  onar (`escapeNewlinesInStrings`), sonra `crypto.createPrivateKey` ile PEM'in geçerli olduğunu test et.
  Hata türünü (kontrol karakteri / kesik / beklenmeyen karakter) metinden parça göstermeden raporla.
- İlk onarım denemesi tutmadı: tablet metin düzenleyicisi uzun satırları SARIP rastgele yerlerden bölüyor
  (dizge ortası, `\` ile `n` arası). Sağlam çözüm: (1) olduğu gibi, (2) dizge içi satır sonlarını kaçışla, (3) dizge içi
  satır sonlarını sil — sırayla dene; sonra private_key'i BEGIN/END arasındaki base64'ten 64'lük satırlarla yeniden kur ve
  `crypto.createPrivateKey` ile doğrula; diğer alanlardaki satır sonlarını sil. Senaryoları (sarma 40/64/76 CRLF/100,
  kaçışlar açılmış, BOM, kesik) birim testiyle sına; hata mesajında anahtar parçası olmadığını da test et.

## 8. Kaynak doğrulama, Son Dakika, kota (news-aggregator 2. tur)
- **RSS adresi tahmin etme, doğrula:** konteyner haber sitelerine erişemez → aday listesi + `check-feeds.js` +
  push ile tetiklenen iş akışı; kayıttan HTTP durumu, yönlendirme hedefi, güncel haber ve görselli haber sayısını oku.
  Bulgular (2026-11): NTV `/spor.rss`, `/sanat.rss`, `/seyahat.rss` 301 (Location yok) → `/sporskor.rss` çalışır;
  Hürriyet `/rss/saglik|bilim|otomobil` 301 → son dakika sayfası (geçersiz); Habertürk `/rss/kategori/<ad>.xml`,
  CNN Türk `/feed/rss/<kategori>/news`, TRT Haber `/<kategori>_articles.rss`, AA `/tr/rss/default?cat=<kategori>` çalışır.
- **"Son Dakika" düz kategori olmaz:** haberler zaten başka akıştan kaydedildiği için mükerrer kontrolü sekmeyi
  boşaltır. Kaynakta `is_breaking`, haberde `breaking_at` alanı; var olan haberi de işaretle; uygulamada
  `orderBy('breaking_at')` (alanı olmayan belgeler dışlanır → birleşik dizin gerekmez).
- **Spark okuma kotası:** kaynak sayısı artınca `getAll` her çalışmada her haberi okur (30 kaynak × 35 × 48 ≈ 50k/gün).
  Kaynakta `last_seen_published_at` sakla, ondan (pay ile) eski haberleri sorgulamadan atla.

## 9. Flutter arayüz kalıpları (kullanıcı tercihleri)
- **Koyu mod tam siyah** (#000000), vurgu #0A84FF; `ColorScheme.fromSeed(...).copyWith(surface, surfaceContainer*,
  onSurfaceVariant, outlineVariant)` ile M3 ton renklerini ez; AppBar `surfaceTintColor: transparent`.
  Ayarlar'da Sistem/Açık/Koyu (`SegmentedButton<ThemeMode>`), SharedPreferences'ta sakla.
- **İlgi alanı ekranı (kullanıcı referansı):** satır = gri çizgi ikon + ad + sağda mavi dolu "+" daire; takip edilenler
  üstte `SliverReorderableList` (sürükleme tutamağı + "−"). Flutter 3.4x: `onReorder` kullanımdan kalktı →
  `onReorderItem` (newIndex zaten düzeltilmiş gelir). İlk açılışta onboarding + "Devam et (n)"; sayıyı ve kaydı
  sunucuda VAR OLAN kategorilerle sınırla (test bu hatayı yakaladı).
- **Testte Firestore:** repository'ye `FirebaseFirestore? firestore` parametresi ver, `fake_cloud_firestore` (dev) ile
  widget testi yaz; `SharedPreferences.setMockInitialValues`.
- **Uygulama ikonu:** PIL ile süper örnekleme (4x) → mipmap ic_launcher/round/foreground/background/monochrome +
  `mipmap-anydpi-v26/ic_launcher.xml` (monochrome = Android 13 tema ikonu) + Play 512px. Açık/koyu ana ekran ve
  daire/squircle/kare maskeli önizleme üretip kullanıcıya göster. Siyah zeminli ikon koyu ana ekranda kaybolur →
  renkli zemin seç.

## 10. Bildirimler, izinler, canlı skor (news-aggregator 3. tur)
- **Spark'ta push:** FCM ücretsiz; GitHub Actions'taki Admin SDK (`firebase-adminsdk` hizmet hesabı) `getMessaging().sendEach`
  ile konuya gönderir. Uygulama konu aboneliği: `news_<kategori>`, `team_<id>`; ayarlar değişince yalnızca FARKI
  abone et/çık (son durumu prefs'te tut). İki takımı birden izleyen tek bildirim alsın: `condition: "'team_a' in topics || 'team_b' in topics"`.
- **Bildirim yağmurunu önle:** kategori başına bekleme (2 sa) + özet ("ve N haber daha"); Son Dakika hemen ama aynı
  haber iki kez gitmesin (`last_breaking_id`), eski haber "son dakika" diye gitmesin. Durum tek belgede (1 okuma+1 yazma).
- **Android kanalları** (sunucu `android.notification.channelId` ile eşleşmeli) uygulama açılışında oluşturulur; özel ses
  kanal oluştururken verilir (`RawResourceAndroidNotificationSound`) ve sonradan değişmez. `res/raw/keep.xml` ile
  sesin küçültmede silinmesini engelle. Ön planda FCM bildirimi görünmez → `onMessage` + flutter_local_notifications.
  flutter_local_notifications 22: `initialize(settings:)`, `show(id:, title:, ...)` adlandırılmış parametre;
  `isCoreLibraryDesugaringEnabled` + `desugar_jdk_libs` zorunlu. Bildirim simgesi beyaz+şeffaf `ic_stat_*`.
- **İzin ekranı:** permission_handler (bildirim) + geolocator (konum); `WidgetsBindingObserver` ile uygulamaya dönünce
  durumu yenile; kalıcı redde `openAppSettings`. Konum iznini MUTLAKA bir özelliğe bağla (Play reddeder) → yalnızca
  `ACCESS_COARSE_LOCATION`. Hava durumu: Open-Meteo ücretsiz sürümü ticari kullanıma kapalı → **MET Norway**
  (api.met.no; ticari serbest, User-Agent zorunlu, CC BY atıf, en fazla 4 ondalık koordinat).
- **Canlı skor ücretsiz mimari:** cron (15 dk) → maç yoksa API isteği harcamadan çık; maç varsa tek iş içinde döngü
  (≤5,5 sa). API-Football `/fixtures?ids=a-b-c` tek istekte tüm maçlar + olaylar. Aralık = kalan istek / kalan maç
  süresi (60 sn–10 dk). Sezon erişimini anahtar gelince `check` moduyla doğrula (ücretsiz plan sezon sınırlı olabilir;
  konteyner api-football.com'a erişemez). Testte `sleep` enjekte et — `global.setTimeout` ezmek gRPC zaman aşımlarını bozar.
- Kullanıcıya sor: veri kaynağı (resmi/anahtarlı vs resmi olmayan), konum amacı, bildirim sıklığı, kategori yapısı.

## 11. Arama, bildirim geçmişi, piyasa paneli (news-aggregator 4. tur)
- **Firestore'da tam metin arama yok:** yazarken kelimelerin 3–10 harflik ön eklerini `search_keywords` dizisine yaz;
  sorguda en uzun kelimenin ön ekiyle `array-contains` + `orderBy(published_at)` (birleşik dizin), diğer kelimeleri
  istemcide süz. Sadeleştirme (İ/I→i, ç ğ ı ö ş ü katlama) JS ve Dart'ta BİREBİR aynı olmalı → test beklentilerini
  JS çıktısından üret. Eski belgelere tek seferlik doldurma (`app_meta` imleci, sayfa sayfa).
- **Birleşik dizinsiz "en yeni önce" liste:** belge kimliği ters zaman damgası (`9999999999999-ms`); sıralama
  belirtilmeyen sorgu kimliğe göre sıralanır → `arrayContainsAny` + limit yeterli. AMA istemcide de `sentAt`'e göre
  sırala (fake_cloud_firestore ve olası değişikliklere karşı).
- **Piyasa verisi:** Yahoo GitHub Actions IP'lerini HTTP 429 ile reddediyor; çerez/crumb ile aşmaya ÇALIŞMA
  (bot korumasını atlatmak; izin reddedildi). Herkese açık kaynakları dürüst User-Agent ile dene (probe betiği + iş akışı).
  Çalışan: **Truncgil** `finans.truncgil.com/v4/today.json` (USD, EUR, GRA gram altın, XU100 + Change; ONS ve BRENT
  fiyatı 0 gelir, yalnızca değişim) — yanıt bazen bozuk JSON → kalemleri regex ile tek tek ayıkla. TCMB `kurlar/today.xml`
  resmi ama günlük. Stooq CSV 404. Ons = gram × 31,1035 ÷ USD/TL. Fiyatı olmayan kalemi gösterme, kullanıcıya söyle.
  Kullanıcının seçtiği kaynak çalışmazsa açıkça bildir, alternatifi doğrulayıp öner. Türkçe sayı: binlik nokta, ondalık virgül.
- **Kart ızgarası taşması:** `GridView` + `childAspectRatio` büyük yazıda taşar → satırları `IntrinsicHeight` + `Row` +
  `Expanded` ile kur, sütunu `mainAxisSize.min`. Widget testleri (Ahem yazı tipi) bunu yakalar.
- **Alt menü:** `NavigationBar` + `IndexedStack` (sekme durumu korunur); sağ üst ☰ → `endDrawer`
  (`Builder` içinden `Scaffold.of(context).openEndDrawer()`).
- **compileSdk tuzağı:** `permission_handler` 13 (permission_handler_android) compileSdk 37 istiyor; AGP 9.1 en fazla 36 →
  `checkDebugAarMetadata` hatası. Yerel analiz/test bunu YAKALAMAZ, yalnızca APK derlemesi yakalar → her yeni eklentiden
  sonra CI'da `flutter build apk` sonucuna bak. Çözüm: eklentiyi kaldır; bildirim izni `FirebaseMessaging.requestPermission`
  + `getNotificationSettings`, ayarları açmak `Geolocator.openAppSettings()`. Android'de iki retten sonra sistem penceresi
  çıkmaz → sayacı prefs'te tut, "engellendi" say ve ayarları aç.
- **Dış API kimliğine bağlanma:** takım listesi API anahtarına bağlıydı → anahtar yokken seçim ekranı boş kaldı.
  Varlıkları KENDİ kimliğinle (`galatasaray`) sabit listeyle tohumla; API adlarını takma ad tablosu + Türkçe katlama ile
  bu kimliğe eşle (bilinmeyen ad → addan kimlik). Bildirim konusu ve kullanıcı seçimi API değişse de bozulmaz.
  Kullanıcı rozet istemedi: logo/rozet yerine düz kısaltma metni (GS, FB, BJK; yoksa adın ilk 3 harfi). Ligden düşenler `active:false`.
- **API-Football ücretsiz plan (2026-11 doğrulandı):** güncel sezona ERİŞİM YOK — "Free plans do not have access to this
  season, try from 2022 to 2024". /status ve /leagues çalışır, /fixtures ve /teams güncel sezonda reddedilir. Canlı skor
  için ücretli plan şart. Kullanıcıya üyelikten ÖNCE bunu söyle. Bot: plan hatasında `plan_blocked_until` (24 sa) yaz, cron her
  çalıştığında istek harcamadan çık (yoksa 15 dk'da bir hata e-postası ve boşa istek).
- **Aynı haber iki sekmede:** Son Dakika kaynaklarını Gündem kategorisine yazmak + `breaking_at` işareti → haber hem
  Gündem hem Son Dakika'da. Çözüm: Son Dakika kaynaklarının haberi `son-dakika` kategorisine; eski kayıtları bir kerelik
  taşı; kategori sekmelerinde işaretli olanları istemcide ele (kısa kalan sayfayı birkaç turla doldur).
  fake_cloud_firestore `orderBy(alan)` alanı olmayan belgeleri de döndürür (gerçek Firestore dışlar) → istemcide de süz.

## 12. AdMob (Flutter, google_mobile_ads 9.x — compileSdk 36 ile uyumlu)
- Önce paketin `android/build.gradle` compileSdk'sine bak (proje 36). UMP `ConsentInformation.requestConsentInfoUpdate` →
  `ConsentForm.loadAndShowConsentFormIfRequired` → `canRequestAds` true ise `MobileAds.initialize`; Ayarlar'da
  `getPrivacyOptionsRequirementStatus()==required` ise `ConsentForm.showPrivacyOptionsForm`.
- Manifest `com.google.android.gms.ads.APPLICATION_ID` ZORUNLU (yoksa açılışta çöker) → `manifestPlaceholders` +
  `System.getenv("ADMOB_APP_ID") ?: test`. Birim kimlikleri `--dart-define`; release'te kimlik yoksa alan hiç gösterilmez.
- Doğal reklam: `NativeAd(nativeTemplateStyle: NativeTemplateStyle(templateType: TemplateType.medium, ...))` — platform
  kodu gerekmez; görünür "Reklam" etiketi; yüklenemezse `SizedBox.shrink`. Banner:
  `AdSize.getLargeAnchoredAdaptiveBannerAdSize(width)` ekranın ~1/5ini kaplar → kullanıcı ince istedi: `AdSize.banner` (320x50).
- Geçiş reklamı yalnızca doğal geçişte (WebView kapanınca), N okumada bir + minimum aralık; açılış/çıkışta asla.
- Testlerde reklam servisi başlatılmaz (`ready=false`) → reklam widget'ları boş; eklenti çağrısı yapılmaz.
- Test sürümünde çıkan 'AdMob native ad validator' penceresi: manifest'e `com.google.android.gms.ads.flag.NATIVE_AD_DEBUGGER_ENABLED=false`. Kullanıcıya bunun SDK'nın geliştirici aracı olduğunu açıkla.

## 13. Uygulama boyutu / bellek (kullanıcı "hafıza çok yüksek" dedi)
- Debug APK ~99 MB (tüm ABI'ler + JIT); kullanıcıya test için `flutter build apk --release --split-per-abi
  --target-platform android-arm64` ver (debug anahtarıyla imzalı, test reklam kimlikleri `--dart-define`).
- Firestore çevrimdışı önbelleği varsayılan 100 MB → `Settings(cacheSizeBytes: 20 MB)` (Firebase init'ten hemen sonra,
  ilk sorgudan önce). cached_network_image varsayılan 200 dosya/30 gün → özel `CacheManager(Config(..., 3 gün, 150))`,
  tüm `CachedNetworkImage`'lara `cacheManager:`; `imageCache.maximumSizeBytes` 50 MB; `memCacheWidth` ile küçük çöz.
- Kullanılmayan paketleri sil (cupertino_icons); Ayarlar'da önbellek boyutu + "Önbelleği temizle"
  (görsel önbelleği, eski DefaultCacheManager, imageCache, WebView clearCache/clearLocalStorage).
- **Release APK açılışta çöktü (2026-11):** `Unable to get provider androidx.startup.InitializationProvider … Failed to create
  an instance of androidx.work.impl.WorkDatabase` → R8, Room'un yansıma ile yüklediği `WorkDatabase_Impl`'i sildi (AdMob/
  Firebase WorkManager kullanır). Çözüm: `proguard-rules.pro` (`-keep class * extends androidx.room.RoomDatabase { <init>(); }`,
  `-keep class androidx.work.** { *; }`, flutter_local_notifications için `com.dexterous.**` + Gson TypeToken/Signature) ve
  release `proguardFiles(...)`. Debug testleri bunu YAKALAMAZ → release APK'yı emülatörde aç
  (`news-crash-check.yml`: reactivecircus/android-emulator-runner, x86_64 release, `adb logcat -b crash`) ve kullanıcıya
  vermeden önce "UYGULAMA ÇALIŞIYOR" gör.

## 14. Bayram / anma bildirimleri (sunucusuz, cihazda zamanlanır)
- Türk kullanıcılı uygulamalarda resmi bayram + anma günlerini (10 Kasım, 18 Mart, 15 Temmuz…) varsayılan açık ayarla ekle; TR+EN metin.
- `flutter_local_notifications.zonedSchedule` + `timezone` (Europe/Istanbul), `AndroidScheduleMode.inexactAllowWhileIdle` → SCHEDULE_EXACT_ALARM izni gerekmez (Play yalnızca alarm/takvim uygulamalarına verir). Birkaç dk sapar: 10 Kasım 09.00'da gönderilir, metin 09.05 saygı duruşunu hatırlatır.
- Manifest: RECEIVE_BOOT_COMPLETED + `ScheduledNotificationReceiver` ve `ScheduledNotificationBootReceiver` (yeniden başlatmada kurulur). Kanal (`observances`) `channels` listesine eklenmeyi unutma.
- Sabit id aralığı (5000–5999) → yeniden zamanlamadan önce yalnızca o aralığı iptal et. Dini bayramlar yıl tablosuyla (Diyanet); tablo biterse güncelle.
- Kandiller de (Regaip, Miraç, Berat, Kadir, Mevlid) tabloyla; bir yılda iki kez olabilir (2027 Miraç) → `dates` listesi (yıl, ay, gün), id = yıl*100 + sıra*3 + n.
- Anma = siyah sade şerit, bayram = kırmızı, kandil = koyu yeşil; ana sayfa üstünde o gün gösterilir. `{n}` = yıl − başlangıç yılı (10 Kasım 2026 → 88. yıl).

## 15. Test APK dağıtımı, açılış hızı, telif (kullanıcı: "APK kurulmadı", "görseller geç geliyor")
- **APK kurulmuyor ("Uygulama yüklenmedi")**: CI her çalışmada rastgele debug anahtarı üretir → imza değişir → güncelleme reddedilir. Çözüm: depoya gizli olmayan sabit `android/app/test-signing.keystore` (.gitignore'da `!` istisnası) + `signingConfigs.test` (debug & release); Play için `UPLOAD_KEYSTORE_*` ortam değişkenleri. `--build-number=${{ github.run_number }}` ile sürüm hep artar. Kullanıcıya: eski sürümü BİR KEZ kaldır; split-per-abi release APK'sının versionCode'u (+2000) debug'dan büyüktür → hep aynı tür APK'yı kur.
- **Görseller geç geliyor**: CachedNetworkImage varsayılan fadeIn 500 ms + fadeOut 1000 ms → 120 ms / 0 yap; ilk sayfa önce `GetOptions(source: Source.cache)` ile anında göster, sonra sunucu; yeni sayfanın ilk ~10 görselini `cacheManager.getSingleFile` ile önceden indir; logolara `memCacheWidth`.
- **Telif**: başlık + ≤200 karakter alıntı, okuma kaynağın sitesinde (WebView), kartta/bildirimde "Kaynak: X", kaynak bazında `allow_images:false`, ajans (AA) görselleri kapalı, `takedowns` kaydı tutan Uyar-Kaldır iş akışı (article/source/images), 48 saat taahhüdü, docs/TELIF_PLANI.md kontrol listesi.
- **Konu sekmesi boş (ör. Ekonomi)**: yayıncılar aynı haberi genel (Gündem/Son Dakika) ve konu akışında yayınlar; ilk gören kaynak kategoriyi sahiplenir → konu sekmesi boş kalır. Çözüm: konu kaynağı var olan haberi görünce genel kategoriden konu kategorisine taşı (`reassignToTopicCategory`), Son Dakika sekmesi `breaking_at`'e bakar; "son dakika işaretlileri gizle" kuralını YALNIZCA Gündem'e uygula; bir kerelik konu kaynaklarının `last_seen` işaretini sıfırla. Görseli kapalı haberlerde boş kutu yerine kaynak logosu.

## 16. ⚠️ GitHub Actions'ı zamanlanmış bot sunucusu olarak KULLANMA (hesap askıya alındı)
- 30 dk'da bir RSS + 15 dk'da bir saatlerce süren canlı skor döngüsü GitHub Actions'ta çalışırken kullanıcının GitHub hesabı "Terms of Service ihlali" ile askıya alındı. GitHub Actions koşulları, projeyi derleme/test/yayın dışındaki sürekli işleri kısıtlar.
- Kural: Actions YALNIZCA derleme/test/APK için. Zamanlanmış işler Firebase Cloud Functions (Blaze + bütçe uyarısı), Cloud Run Jobs vb.'de çalışır. Kullanıcı ücretsiz plan isterse bu riski baştan açıkça söyle.
- Uzun döngü yerine dakikalık kısa görev: `footballTick` (maç yoksa 1 belge + 1 sorgu, API yok; `next_poll_at` ile aralık), `maxInstances: 1`, gizli anahtar `defineSecret` (Secret Manager).
- Bilgisayarı olmayan kullanıcıya dağıtım: Google Cloud Shell + zip yükleme + `npx firebase-tools login --no-localhost` (docs/FIREBASE_TASIMA.md). Deploy'da imaj temizleme politikası 1 gün.
- GitHub erişimi kesilince: hemen `git archive` ile kod zip'ini kullanıcıya gönder (oturum kapanırsa tek yedek).
- Kullanıcı (Ozkan) git barındırma hesabı açamıyorsa yedek yöntemi: her değişiklikten sonra tarihli zip (`git archive HEAD news-aggregator .github .claude`) gönder → kullanıcı Google Drive'a kaydeder; Firebase güncellemesi Cloud Shell'e zip yükleyip `deploy`.
- Bitbucket (GitHub alternatifi): Atlassian "API token with scopes" → Bitbucket, read:repository + write:repository. Git HTTPS kullanıcı adı **`x-bitbucket-api-token-auth`** (e-posta ya da Bitbucket kullanıcı adı ÇALIŞMADI), şifre = anahtar. Sığ (shallow) klonu itme "shallow update not allowed" verir → `git commit-tree HEAD^{tree}` ile tek anlık görüntü gönder.
- Bulut oturumunda APK derleme: dl.google.com ortam izni + Android SDK (cmdline-tools, platforms;android-36, build-tools;36.0.0, ndk;28.2.13676358). Maven Central proxy'de 429 verir → `news-aggregator/tool/gradle-maven-mirror.init.gradle` dosyasını `~/.gradle/init.d/` altına koy (önce Google'ın Central aynası, sonra Google deposu, en son eklenti portalı). APK'yı SendUserFile ile doğrudan gönder; imzanın test anahtarıyla eşleştiğini `apksigner verify --print-certs` ile doğrula.
- Ücretsiz canlı skor (Süper Lig): API-Football/football-data/TheSportsDB/Sportmonks ücretsizleri Süper Lig'in güncel sezonunu veya canlı skoru VERMİYOR. Highlightly Basic (0 $, 100 istek/gün) uyarlayıcısı: `src/football-highlightly.js` API-Football `get()` arayüzünü taklit eder; günlük bütçe `football_meta/state.req_used`'da sayılır ve istekler günün son maçına kadar yayılır. Skor = olgu; telif riski yalnızca izinsiz site kazıma ve görüntü/yayın paylaşımında.
- Git anahtarı yoksa/çalışmıyorsa: oturumda Atlassian bağlayıcısı varsa `discover` → `createBitbucketRepoCommit` (executeWrite; files = {yol: içerik}) ile değişen metin dosyaları anahtarsız Bitbucket'a kaydedilir; `listBitbucketRepoCommits` ile uzak durum doğrulanır. Toplu yeniden düzenleme (dosya silme/taşıma) için yine git anahtarı gerekir. Atlassian "API token with scopes" sihirbazı telefonda "Next" göstermeyebilir → bilgisayardan oluşturulmalı.

## 17. Bildirim üslubu, son dakika sesi, gece sessiz saatleri (kullanıcı isteği 2026-11-08)
- Son dakika başlığı profesyonel: `🔴 SON DAKİKA` (⚡ değil). SONUÇ (13 tur sonra): kullanıcı "şimdilik SİSTEM bildirim sesi" dedi → kanal `breaking_news` sesi belirtilmeden (varsayılan), sunucu `sound` göndermez; kullanıcı telefon ayarından istediği sesi seçer. Önceki deneme: ses kullanıcıyla turlarda arandı (A–O, N2/N3 adayları `design/make_breaking_sound_v*.py`): **N2 "tok"** = dolgun bas vuruşu + Sol minör bakır akor + orta salon yankısı (2,4 sn) → `res/raw/breaking_news.wav`, kanal `breaking_news`. Kullanıcının zevki: baslı, yankılı, tok, sinematik; tiz bip/marimba/teleks/cam BEĞENİLMEDİ. Telefon hoparlörü için bas vuruşlara 2–4. harmonik ekle.
- Android kanal sesi sonradan DEĞİŞMEZ → yeni kanal kimliği (`breaking_alert`) + eskisini `deleteNotificationChannel` ile sil; sunucu `channelId` ve `sound` aynı adı göndermeli. Eski APK'da kanal yoksa FCM varsayılan kanala düşer → yeni APK ver.
- Gece 00:00–08:00 (İstanbul; kullanıcı 23'ten 24'e çekti) tüm bildirimler `quiet` kanalından (Importance.low, ses/titreşim yok); istisna: deprem (büyüklük ≥4,5 ya da belirtilmemiş), afet/AFAD, sel, tsunami, savaş, seferberlik, OHAL, füze/hava saldırısı, terör saldırısı içeren SON DAKİKA haberleri → sesli. Anahtar kelimeler Türkçe katlanmış metinde, kelime sınırıyla (`savaşım` eşleşmesin). `src/quiet-hours.js`. Testler sabit `now` vermeli (gerçek saate bağlı test gece kırılır).
- Kullanıcı belirli bir telefon/marka sesini (ör. Samsung "Dudum") isterse: KOPYALAMA (telifli). Yol: kanal sesini
  kullanıcı Android ayarlarından seçebilir (Ayarlar → Bildirimler → Uygulama → kanal → Ses) — bunu anlat; uygulamaya
  yalnızca o tarzda özgün varsayılan ses koy. Ses turları uzarsa (13 tur yaşandı) erken sor: "uzunluk / sertlik / referans?"

## 18. Canlı skor paneli (bildirimde sabit, yerinde güncellenen skor — Android)
- flutter_local_notifications özel düzen (RemoteViews) desteklemez → yerli Kotlin: `LiveScoreMessagingService` (eklentinin `FlutterFirebaseMessagingService`'ini genişletir; `type=match_live` veri mesajını yakalar, gerisini `super`'e verir) + manifest'te eklenti servisi `tools:node="remove"` + kendi servisin MESSAGING_EVENT ile. app/build.gradle.kts'e firebase-bom (firebase_core ile aynı sürüm, gradle.properties `FirebaseSDKVersion`) + firebase-messaging + androidx core-ktx.
- `LiveScoreNotifier`: `RemoteViews` (yalnız LinearLayout/TextView/ImageView/Chronometer), `DecoratedCustomViewStyle`, sabit tag+id ile `notify` (yeni bildirim açmaz, günceller), `setOnlyAlertOnce`+`setSilent`, `setOngoing(!bitti)`, bitti → `setTimeoutAfter(2 sa)`. Dakika `Chronometer` (base = elapsedRealtime − (dakika·60 sn + gecikme)) → sunucu her dakika göndermez. Takım armaları YOK: kısaltma + renk noktası (`setColorFilter`), siyaha yakın renk yerine ikinci renk.
- Ayar Flutter SharedPreferences'tan okunur: dosya `FlutterSharedPreferences`, anahtar `flutter.<ad>`. Sunucu: her skor turunda canlı/devre/biten maçlar için veri mesajı (tüm değerler string), `logSentMessages`'a GİRMEZ; testlerde sahte `sendEach` uyarı ve panel mesajlarını ayırmalı.
- **"Gol anında" skor (hızlı mod):** Cloud Scheduler en az 1 dk → görev içinde ~50 sn pencere, 15 sn aralıkla kontrol (`tick(..., {fast:true})`); döngü duvar saatine değil planlanan beklemelere göre sınırlı (test edilebilir, sonsuz döngü yok). Aralık bütçeden hesaplanır → ücretsiz planda (100/gün) kendiliğinden yavaş, ücretli planda (Highlightly Pro ~19 $/ay, 7.500/gün) gol ~15–30 sn. Sağlayıcının başlıkla bildirdiği kalan hak (`last_remaining_header`) varsa ona güven, yoksa kendi sayacın; günlük sınırı da başlıktan öğren (`x-ratelimit-*-limit`), sabit 100'e takılma.
- **Ücretsiz bütçeyi verimli kullanma (kullanıcı onayladı):** (1) Devre arasında istek yok: `ht_since` kaydet, hepsi devre arasındaysa `ht_since + 13 dk`'ya (ya da sıradaki başlamaya) kadar bekle. (2) Yalnız takip edilen takımlar: uygulama `team_follows/{takım}.count` isimsiz sayacını ±1 yazar (`TeamFollowSync`, yalnız fark; kurallar: yalnız `count`, oluşturmada 1, güncellemede ±1, ≥0, okuma kapalı); sunucu 15 dk önbellekle okur; sayaç hiç yoksa (eski sürümler) hepsini izle. Kurallar değişince deploy `--only functions,firestore`.
- Hesap (Süper Lig, 1 maç ≈ 120 dk, tüm eşzamanlı maçlar tek istek): 15 sn aralık ≈ 480 istek/maç, 1 dk ≈ 120, 100 istek ≈ 72 sn aralık. Kullanıcıya böyle tablo ver.
- **Canlı özelliği gerçek maç beklemeden test:** `football_meta/test` belgesi → dakikalık görev sahte maçı oynatır (başladı + panel, N. dk gol, M. dk sonuç, belge silinir; başlıkta `[TEST] `; gece sessizine takılmaz; API isteği harcamaz). Kurulum `scripts/test-live.js ev deplasman SS:DD` ya da konsoldan tek alan `start_at: "YYYY-MM-DDTHH:MM"` (İstanbul).
- Kullanıcı "bildirim sesleri telefonda seçili olana" dedi → gol dahil TÜM kanallar varsayılan ses; özel sesli eski kanallar (`goals`, `breaking*`) `deleteNotificationChannel`; yeni kimlikler `goal_alert`, `breaking_news`.
- **Bitbucket "erişim yok" ama anahtar sağlam olabilir:** `curl -D -` ile yanıtı oku (`X-Proxy-Error`, gövde metni); Atlassian bağlayıcısıyla `listBitbucketRepositories` → depo YENİDEN ADLANDIRILMIŞ olabilir (2026-11-09: `haber-akisi1` → "Ramazan rehperi"). Bitbucket REST'te API anahtarı e-posta kullanıcı adı ister ("API token must be used with an atlassian registered email"), git ise `x-bitbucket-api-token-auth`.
- Haber Akışı deposu artık `ozkan45/haber-akisi`, uygulama `haberler/` altında; gönderim `news-aggregator/tool/push-haberler.sh "mesaj"` (uzak son kaydın üzerine alt-ağaç kaydı, zorla itme yok).
- **Bildirimler aynı anda yağmasın (kullanıcı isteği, 1+2 birlikte):** kategori haberleri `notify_state/news.pending` kuyruğuna; son kategori bildiriminden ≥18 dk geçtiyse (bot 20 dk'da bir) kuyruktan en yeni ≤3 kategori: 1 → normal bildirim ("ve N haber daha"), 2–3 → TEK özet "📰 N yeni haber" (satır başına "Kategori: başlık", son satır "Kaynak: …", `condition` ile kategori konuları). Son dakika bekletilmez. Durum belgesini `set` ile ÜZERİNE yaz (merge, silinen kuyruk anahtarlarını bırakır) ve hiç bildirim yokken de kuyruğu kaydet.

## 19. Yayın öncesi yasal ve lisans temizliği (Play Store, Türkiye)
- **Kaynak lisansı:** Ticari yeniden yayını yasaklayan kaynakları (ör. Investing.com) seed'den çıkar + mevcut kurulumda bir kerelik `retireSources` (app_meta/retired_sources sürümlü) ile kapat ve haberlerini sil.
- **Piyasa verisi:** Döviz için TCMB `kurlar/today.xml` (resmî, kaynak göster; günlük değişimi önceki bültenle kıyasla, önceki değeri belgede sakla). BIST verisi yalnızca lisanslı dağıtıcıyla → gösterme. Panelde "yatırım tavsiyesi değildir" notu. Bir kaynak çökerse son bilinen değeri koru.
- **Atıf:** MET Norway → "Veri: MET Norway (CC BY 4.0)" görünür olmalı; User-Agent'ta uygulama sitesi.
- **Konum:** Hava durumu için 2 ondalık (≈1 km) yeter; gizlilik metninde "sunucumuza gönderilmez" yazılabilsin diye doğrudan cihazdan çağır.
- **Yasal sayfalar:** Firebase Hosting (`firebase/hosting/public`, `cleanUrls`) → `<proje>.web.app/gizlilik|privacy|kvkk|kullanim-kosullari|kunye`. Kişisel bilgiler `[[AD_SOYAD]]` gibi yer tutucu + `tool/fill-legal.sh` ile tek komutta doldurulur. Ayarlar sayfasına KVKK ve künye bağlantısı. Haber uygulamasında künyede e-posta + telefon + adres (Play haber politikası).
- **Gizlilik metni gerçeği yansıtsın:** hesap yok, tercihler cihazda, FCM token/konu, anonim sayaç, AdMob+UMP, Crashlytics, App Check; yurt dışı aktarım KVKK m.9 (standart sözleşme / arızi hâlde açık rıza). Avukat incelemesi öner.
- **Crashlytics + App Check:** `firebase_crashlytics`, `firebase_app_check`; `setCrashlyticsCollectionEnabled(kReleaseMode)`, FlutterError + PlatformDispatcher.onError. App Check `AndroidPlayIntegrityProvider` (release) / `AndroidDebugProvider` (debug). ⚠️ Zorunlu kılmayı (enforcement) ancak uygulama Play'den dağıtılınca aç — yan yüklenen APK'lar doğrulanamaz ve Firestore'a erişemez. Gradle: `com.google.firebase.crashlytics` 3.0.x eklentisi (google-services ile birlikte koşullu uygula).
- **Hesap türü:** Kişisel Play hesabı → 12 testçi × 14 gün kapalı test şartı; kurumsal hesap D-U-N-S ister. Vergi (GVK 20/B vs ticari kazanç) için mali müşavire yönlendir.

## 20. Paket adı düzeni (kullanıcı kararı 2026-11-11) ⭐ TÜM UYGULAMALARDA
- Kullanıcının uygulamaları **`akinai.appN`** düzeniyle adlandırılır: Haber Akışı = `akinai.app1`, sonraki uygulama `akinai.app2`, sonra `akinai.app3` … Yeni uygulamaya başlarken sıradaki numarayı sor/kontrol et (kullanılanlar: app1 = Haber Akışı).
- Tamamı küçük harf (Apple paket kimlikleri büyük/küçük harf duyarsız). Android `applicationId` + `namespace` aynı; Kotlin dosyaları `kotlin/akinai/appN/`. iOS bundle ID de aynı değer.
- Yayından sonra paket adı DEĞİŞTİRİLEMEZ. Değiştirirken: Firebase'de yeni Android uygulaması ekle → yeni google-services.json (package_name eşleşmezse derleme düşer); telefondaki eski paket ayrı uygulama olarak kalır, kaldırılmalı.
- **Yükleme anahtarı (JKS):** `keytool -genkeypair -storetype PKCS12 -alias upload -keyalg RSA -keysize 4096 -validity 10000`, şifre `openssl rand` ile üretilip doğrudan `key.properties`'e yazılır (sohbete asla yazdırılmaz). Kullanıcıya zip (jks + key.properties + OKUBENI) olarak verilir; depoya girmez (.gitignore: *.jks, key.properties). build.gradle.kts: `import java.util.Properties` + `val keyProps` **android{} bloğunun DIŞINDA** (içeride `java` adı Android uzantısına çözülür → "Unresolved reference util"). Öncelik key.properties, sonra UPLOAD_* ortam değişkenleri. Doğrulama: `keytool -printcert -jarfile app-release.aab`. Play App Signing'de "Google tarafından oluşturulan anahtar" seçilir; yükleme anahtarı kaybolursa Play Console'dan sıfırlanabilir.

## 21. Bildirim yoğunluğu: "az ama önemli" (kullanıcı isteği 2026-11-12) ⭐ haber uygulamalarında varsayılan
- 3 seviye (uygulama konu aboneliğiyle): Sadece önemli `lvl_important+calm`, **Dengeli (varsayılan)** `lvl_balanced+calm`, Tümü `lvl_all`; + `bulletin`. Sunucu kategori konusunu seviye konusuyla KOŞULDA birleştirir: `('news_a' in topics || 'news_b' in topics) && 'lvl_balanced' in topics` (en fazla 5 konu).
- Önem = aynı olayı veren FARKLI yayıncı sayısı: başlık → sadeleştirilmiş 5 harflik kökler (+ kısaltma eşleme TCMB→merkez banka), ortak kök ≥2 ve küçük kümenin ≥%50'si → aynı hikâye. 3+ yayıncı → "önemli son dakika" (spor/magazin hariç); son dakika kaynağından kritik (deprem ≥4,5/afet/savaş) → tek kaynakla hemen, sınırsız.
- Tıklama tuzağı/magazin regex'i bildirime ve bültene girmez (akışta kalır). Dengeli: saatte ≤1 özet, günde ≤6; önemli son dakika 30 dk aralık, günde ≤6. Bülten 08:30/13:00/19:00 (İstanbul), en çok kaynaklı 4 hikâye.
- Hikâyeler `notify_state/news.stories` (budanır: 14 sa, tek kaynaklılar 3 sa, en fazla 600).
- "Daha az bildirim" düğmesi: FCM'in gösterdiği bildirime düğme EKLENEMEZ → Android'e VERİ mesajı (`kind=news_alert`, n_title/n_body/n_image/n_channel) gönder, Kotlin `NewsNotifier` bildirimi kurar; düğme `LessNotificationsReceiver` → FlutterSharedPreferences `flutter.notif_level=important` + konuları yerelde değiştirir; Flutter `AppLifecycleListener(onResume: prefs.reload)`. iOS için `apns.payload.aps.alert` ekle. Dokunma verisi: launch intent extra → MainActivity MethodChannel `akinai/launch` (takePayload / payload).
- Bildirimler sekmesi kaydında seviye konuları (lvl_*, calm) çıkarılır (yoksa herkes her şeyi görür).

## 22. Marka/logo (Haber Akışı, 2026-11-12)
- Logo A: lacivert degrade (#16345F→#0B1F3A) + beyaz akış satırları + kırmızı (#E63946) canlı nokta. Palet: lacivert #0B1F3A (marka), vurgu açık #16345F / koyu #7FA6E3, haber kırmızısı #D62828 YALNIZCA son dakika/canlı, metin #121821, gri #6B7280.
- İkon üretimi: SVG → Playwright ile her yoğunlukta foreground/background/monochrome (108dp tuval, içerik 66dp güvenli daireye sığmalı: ölçek ~0.92) + eski ic_launcher/round (48dp) + Play 512×512 RGBA. Taslakları ana ekran önizlemesiyle (koyu/açık duvar kâğıdı, küçük boy) yan yana göster. Kaynak: docs/brand/logo.svg.
- SVG'leri tek sayfada gösterirken degrade kimlikleri benzersiz olmalı (aynı id → ilk tanım kazanır, renkler karışır). setContent içinde file:// görsel yüklenmez → base64 göm. `dart format` projede satır genişliği ayarı yoksa dosyaları gereksiz yeniden sarar → kullanma, yalnızca analyze.
