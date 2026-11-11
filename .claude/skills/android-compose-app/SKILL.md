---
name: android-compose-app
description: Kotlin + Jetpack Compose Android uygulaması oluşturma/iskeletini kurma. Tek dosyalık "codebase context" dökümünden proje çıkarma, eksik kaynakları tamamlama, Gradle wrapper ve CI kurma. Yeni bir Android uygulaması başlatırken veya bir kod dökümünden proje üretirken kullan.
---

# Android Compose Uygulaması — Öğrenilenler

## 0. HER UYGULAMADA ZORUNLU: İngilizce dil desteği (kullanıcı kuralı)
Kullanıcı hangi uygulamayı yaparsa yapsın **Türkçe + İngilizce** dil desteği istenir; Ayarlar'da dil seçici olmalı.
- **Yeni uygulamada (önerilen):** Metinleri baştan `res/values/strings.xml` (Türkçe, varsayılan) ve
  `res/values-en/strings.xml` içine koy, Compose'ta `stringResource(R.string.x)` kullan. Dil seçimi için
  `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))` (appcompat + `AppCompatActivity`)
  veya Android 13+ `LocaleManager`; `res/xml/locales_config.xml` + manifest `android:localeConfig` ekle.
- **Var olan, metinleri koda gömülü uygulamada:** `i18n/I18n.kt` kalıbı — `AppLanguage.current` + `t("Türkçe metin")`
  sözlüğü (bulamazsa Türkçeyi döndürür), dinamik metinler için regex kalıpları; dil değişince kök composable'ı
  `key(language) { ... }` ile yeniden kur. Metinleri otomatik sarmak için `text = "..."`, `Text("...")`,
  `title = / subtitle = / contentDescription =` desenlerini betikle `t(...)` içine al.
- Dile özel içerik kuralları olabilir (ör. İngilizcede yalnızca Kur'an Radyo) — sor ve uygula.
- Veri içeriği (meal, menü, sözler) ayrıca çevrilmelidir; çevrilmediyse kullanıcıya açıkça söyle.

## 1. Kod dökümünden proje çıkarma
Dökümler genelde `### Dosya: \`yol\`` başlığı + kod bloğu biçimindedir.
- Önce `grep -n '^### Dosya'` ile dosya sayısını, `grep '^```'` ile açılış/kapanış dengesini doğrula.
- Küçük bir Python betiğiyle her bloğu kendi yoluna yaz (betiği proje dışında, scratchpad'de tut).
- Çıkardıktan sonra `package` satırlarının klasör yoluyla eşleştiğini kontrol et.

## 2. Dökümlerde genelde EKSİK olanlar (mutlaka kontrol et)
`grep -rhoE 'R\.[a-z]+\.\w+' app/src` ve `grep -rhoE '@(xml|mipmap|style|drawable|raw)/\w+' app/src`
ile referans verilen ama olmayan kaynakları bul:
- `values/themes.xml` (+ `values-night`) — Compose kullanıyorsa `android:Theme.Material.*.NoActionBar` yeterli, ekstra bağımlılık gerekmez.
- `mipmap/ic_launcher(_round)` — `mipmap-anydpi-v26` adaptive icon + minSdk<26 için `mipmap/` altında `<inset>`/layer-list yedeği.
- `xml/file_paths.xml` (FileProvider; koddaki `cacheDir`/`filesDir` alt klasörüne göre), `backup_rules.xml`, `data_extraction_rules.xml`.
- `res/raw/*.wav` sesler — Python `wave` modülüyle sentezlenebilir (22.05 kHz mono 16-bit, küçük boyut).
- `app/proguard-rules.pro` (release'te `isMinifyEnabled = true` ise).
- Gradle wrapper (`gradlew`, `gradle/wrapper/*`), `gradle.properties`, `.gitignore`.
- `debug.keystore` — build dosyası `${rootDir}/debug.keystore` istiyorsa:
  `keytool -genkeypair -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"`
- Secrets Gradle Plugin kullanılıyorsa `.env.example`.

## 3. Sürüm eşleşmeleri
- AGP 9.x yerleşik Kotlin içerir (`kotlin-android` eklentisi yok); AGP 9.1 → Gradle 9.3.1, JDK 17+ (21 önerilir).
- Wrapper'ı, proje ayarlarını değerlendirmemek için boş bir klasörde üret:
  `gradle wrapper --gradle-version 9.3.1 --distribution-type bin`, sonra dosyaları kopyala.

## 4. Bulut ortamı kısıtı
Bu konteynerde `dl.google.com` engelli → Android SDK/Google Maven indirilemez, yerel derleme yapılamaz.
Çözüm: `.github/workflows/android-build.yml` ekle ve doğrulamayı CI'da yap:
checkout@v5 → setup-java@v5 (21) → runner'daki hazır SDK ile
`yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses` + `"platforms;android-36.1"` →
gradle/actions/setup-gradle@v5 → `./gradlew assembleDebug` → upload-artifact@v5.
- **`android-actions/setup-android@v3` KULLANMA**: artık var olmayan `tools` paketini kurmaya çalışıp çöküyor.
- Depo herkese açıksa CI sonucunu `curl https://api.github.com/repos/OWNER/REPO/actions/runs?head_sha=SHA` ile arka planda bekle.
- APK linki: `https://github.com/OWNER/REPO/actions/runs/RUN_ID/artifacts/ARTIFACT_ID` (indirmek için GitHub girişi gerekir, zip içinde gelir).

## 5. Tasarım tercihleri (kullanıcının uygulamaları)
- Türkçe arayüz, Material 3, krem/bej/altın + zümrüt yeşili palet, karanlık mod desteği.
- MVVM + StateFlow, SharedPreferences tabanlı `AppPreferences`.
- AdMob test kimliğiyle başla; yayın öncesi gerçek kimliği hatırlat.

## 6. Sık hatalar ve kalıplar (bu projeden)
- **StaticLayout + `Paint.Align.CENTER` birlikte kullanılmaz**: metin sola kayar. StaticLayout'ta yalnızca `Layout.Alignment.ALIGN_CENTER` kullan.
- **Bildirim kanalı sesi sonradan değişmez**: sesi değiştirmek için yeni kanal ID'si aç, eskisini `deleteNotificationChannel` ile sil. Sistem sesi için `setSound` çağırma.
- **"Hatırlatıcı" ayarı ≠ hatırlatıcı**: AlarmManager (`setExactAndAllowWhileIdle`, izin yoksa `setAndAllowWhileIdle`) + BroadcastReceiver + BOOT_COMPLETED/MY_PACKAGE_REPLACED receiver kur; alarm çalınca bir sonrakini kur. Android 13+ `POST_NOTIFICATIONS` iznini iste.
- **Koyu tema kontrolü**: ekranlarda `isSystemInDarkTheme()` değil, uygulamanın seçtiği temaya bak (`MaterialTheme.colorScheme.background.luminance() < 0.5f`).
- **Çoklu tema**: `enum AppColorTheme` + her tema için açık/koyu `ColorScheme`; koyu mod için neredeyse siyah (#050505) zemin + temanın hafif tonlu yüzeyleri.
- **Alt menü hizası**: tüm ikonlar aynı boyut (24dp), etiketler `maxLines = 1, softWrap = false`.
- **Instagram Hikâyesi**: `com.instagram.share.ADD_TO_STORY` Meta App ID ister; yoksa `ACTION_SEND` + `setPackage("com.instagram.android")`. Manifest'e `<queries>` ekle.
- **Ramazan tarihleri** yıla göre değişir; sabit tarih yerine yıl tablosu (`RamadanCalendar`) kullan.
- **Kotlin tuzağı — yerel fonksiyon gölgelemesi**: Composable içinde `fun stop()` tanımlayıp `controller?.run { stop() }` yazmak, `run` bloğunda bile YEREL `stop()`'u çağırır → sonsuz özyineleme, `StackOverflowError`. Yerel fonksiyonlara alıcı üyeleriyle aynı adı verme (`stopPlayback`), alıcıyı açık yaz (`c.stop()`).
- **Gerçek cihaz testi olmadan doğrulama**: GitHub Actions'ta `reactivecircus/android-emulator-runner` ile emülatör aç, `adb` ile tıkla (uiautomator dump + metne göre dokunma), `screencap` ile ekran görüntüsü ve `logcat -b crash` ile çökme izi al; sonuçları `[skip ci]` commit'iyle dala yaz (scripts/take_screenshots.sh).
- **Kullanıcıdaki çökmeler için**: `Thread.setDefaultUncaughtExceptionHandler` ile yığın izini dosyaya yaz, sonraki açılışta "Raporu Kopyala" diyaloğu göster (util/CrashReporter.kt).
- **Canlı yayın adresi doğrulama**: sandbox çoğu alana erişemez; CI'da curl ile playlist → alt playlist → segment zincirini test et, resmi sitenin HTML'indeki `live_stream_url` değerini oku.

## Ana ekran widget'ları (RemoteViews — ek bağımlılık gerektirmez)
- `AppWidgetProvider` + `res/layout` (yalnızca LinearLayout/FrameLayout/TextView/ImageView/Chronometer) + `res/xml/widget_info_*.xml`.
- Canlı geri sayım: `Chronometer` + `setChronometer(id, elapsedRealtime()+kalanMs, null, true)` + `setChronometerCountDown(id, true)` (API 24). Her saniye güncellemeye gerek kalmaz.
- Vakit değişiminde yenileme: AlarmManager ile bir sonraki vakte (veya gece yarısına) `WidgetRefreshReceiver` alarmı; ayrıca `updatePeriodMillis=1800000` yedek.
- Önizleme: `android:previewLayout` aynı layout'u gösterir → layout'taki TextView'lara örnek `android:text` değerleri koy, yoksa önizleme boş görünür.
- Uygulama içinden ekleme: `AppWidgetManager.requestPinAppWidget` (API 26+), desteklenmezse "Ana ekranda basılı tut → Widget'lar" yönlendirmesi.
- Widget/Receiver kodu tüm `try/catch` içinde; dil için `AppLanguage.current = prefs.language` ayarla (Activity açılmadan çalışır).
- **Estetik widget (tam bitmap tasarım):** Tüm görseli Canvas ile bitmap'e çiz (gradyan, ışıltı, desen, halka, ikonlar, özel fontlar `ResourcesCompat.getFont`) → `setImageViewBitmap`. Canlı geri sayım için üstüne `Chronometer` (`setChronometer` + `setChronometerCountDown`). Boyut `getAppWidgetOptions` (MIN_WIDTH×MAX_HEIGHT dp), ölçek = min(density, 2.5) (Binder 1 MB sınırı); `onAppWidgetOptionsChanged`'de yeniden çiz.
- Metin sığdırma: başlıkları `measureText` döngüsüyle küçült; çok satırlı metinde StaticLayout'u her satır tek satıra sığana ve yükseklik alana girene kadar küçült (setMaxLines yüksekliği sınırlamaz). Dekoratif öğelerle (fener vb.) çakışmasın diye metin genişliğini öğenin soluyla sınırla.
- Widget görsel testi: Robolectric `@GraphicsMode(NATIVE)` ile bitmap'i PNG'ye yaz (`build/widget-renders`), her tema/durum için gözle kontrol et. Robolectric `android-all-instrumented` jar'ı Maven 429 verirse `maven-central.storage-download.googleapis.com` aynasından ~/.m2'ye indir.
- Önizleme: `previewImage` için `drawable-nodpi/widget_preview_*.png`; `initialLayout` basit bir yükleniyor düzeni.
- Emülatörde test: uygulamadaki "widget ekle" düğmesi → sistem diyaloğunda "Add to home screen"'e dokun → ana ekran ekran görüntüsü.

## Paylaşılabilir görsel kart (Canvas → PNG → FileProvider)
- 1080x1920 Bitmap'e Canvas ile çiz; önizleme için aynı fonksiyonu `scale` (ör. 0.45) ile `Dispatchers.Default`'ta üret, `produceState` ile göster.
- Fotoğrafı `inSampleSize` ile küçültüp kemer yoluna `clipPath` ederek centerCrop çiz; metinleri `StaticLayout` ile ortala, sığmazsa yazı boyutunu küçülterek tekrar ölç.
- Paylaşım: cache/shared_images → `FileProvider` Uri → `ACTION_SEND` chooser; Instagram için `com.instagram.share.ADD_TO_STORY` (facebook_app_id gerekir), yoksa genel paylaşıma düş.
- Tasarım onayı için önce HTML/SVG ile örnek kart çiz, Playwright ile PNG al ve kullanıcıya göster. Onaydan sonra aynı sahneleri **metinsiz** 1080x1920 render edip `drawable-nodpi/*.webp` (quality ~86, kart başı 25–100 KB) olarak göm; metni uygulamada Canvas ile çiz (çok dilli ve çok mesajlı kalır). Yazı tiplerini `res/font` içine göm (Google Fonts TTF, OFL; `ResourcesCompat.getFont`). `StaticLayout` için `BREAK_STRATEGY_BALANCED` başlıkları dengeli böler.
- CI emülatöründe "Pixel Launcher isn't responding" uyarısı çıkabilir: betikte `tap_text "Wait"` ile kapat.

## Yayın öncesi reklam/güvenlik kontrol listesi (her uygulamada)
- AdMob: Google UMP onay formu (`com.google.android.ump:user-messaging-platform`), izin yoksa reklam yükleme; Ayarlar'a "Reklam gizlilik seçenekleri".
- `MobileAds.initialize` arka plan iş parçacığında; dini/aile içerikli uygulamada `MAX_AD_CONTENT_RATING_PG` + AdMob panelinde hassas kategorileri (kumar, alkol, flört, faiz) engelle.
- Reklam kimlikleri koda gömülmez: debug'da test kimlikleri, release'de `manifestPlaceholders` + `buildConfigField` ortam değişkeninden.
- `AndroidView` reklamında `onRelease = { it.destroy() }`.
- Kullanılmayan SDK'ları (ör. firebase-ai, App Check debug sağlayıcısı) kaldır; debug sağlayıcısı asla release'e girmesin.
- Logo görselinden simge: kare içini kırp → kenarları yumuşat → 1024 ön plan PNG (sanat ~%60) + gradyan arka plan XML; `drawable-*dpi/ic_launcher_foreground.png`, `mipmap-*dpi/ic_launcher(_round).png`, Play Store 512px.

## Dakikası dakikasına bildirim (ezan, alarm, hatırlatıcı)
- `setExactAndAllowWhileIdle` Doze'da **uygulama başına ~9 dakikada bir** çalışır; aynı anlarda başka "allow while idle" alarm (widget yenileme vb.) varsa bildirim 9–15 dk gecikir.
- Kritik alarmlar için `AlarmManager.setAlarmClock(AlarmClockInfo(time, showPi), pi)` kullan: Doze ve kotadan muaf, cihazı uyandırır (durum çubuğunda alarm simgesi görünür).
- İzin: `USE_EXACT_ALARM` (API 33+, otomatik verilir; Play Console'da "alarm" beyanı gerekir) + `SCHEDULE_EXACT_ALARM` `maxSdkVersion="32"`.
- Kritik olmayanlar (günlük bilgi, widget yenileme): `setWindow` veya uyandırmayan `set(RTC, …)` — kritik alarmların kotasını yemesin.
- Boot, paket güncellemesi, `TIME_SET`, `TIMEZONE_CHANGED`, `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` olaylarında alarmları yeniden kur.
- FCM push gecikiyorsa sunucudan `priority: high` gönder (normal öncelik Doze'da bekletilir).

## Play Store yayın hazırlığı (her uygulamada kontrol et)
- Paket adı (applicationId) ilk yüklemeden sonra değişmez: geçici/şablon adları yayından önce değiştir.
- **Kullanıcının paket adı kuralı:** `com.ozkanakin.<uygulamaadi>` (Android applicationId = iOS bundle id). Uygulamalar: 1) Haber Akışı (`com.haberakisi.haber_akisi`, eski kural), 2) Ramazan Rehberi (`com.ozkanakin.ramazanrehberi`). Yeni uygulamalarda bu öneki kullan; Wear sürümü aynı applicationId'yi kullanır. Firebase `google-services.json` bu ada göre indirilmeli.
- Play politikası: `USE_EXACT_ALARM` yalnızca alarm saati/takvim uygulamalarına; namaz/hatırlatıcı uygulamalarında `SCHEDULE_EXACT_ALARM` kullan + izin yoksa Ayarlar'da uyarı ve `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` düğmesi.
- "Diyanet" adı 633 sayılı Kanun gereği izinsiz isim/marka olarak kullanılamaz: arayüzde "resmî takvim/ilçe vakitleri" de, bağlantısızlık notu ekle; mağaza adında/ikonda kullanma. Diyanet meali telifli (izin ya da telifsiz meal).
- Gizlilik politikası (docs/ + GitHub Pages), uygulama içi bağlantı; "veriler yalnızca cihazda" gibi iddialar reklam/analitik SDK varken yanıltıcıdır.
- Test/hata ayıklama düğmeleri `BuildConfig.DEBUG` arkasında.
- Wear + telefon aynı ilanda: sürüm kodları farklı olmalı (wear = 1_000_000 + kod), kod tek yerden (gradle.properties).
- CI'da `:app:minifyReleaseWithR8` (imzasız) ile R8 kontrolü; elle çalışan imzalı AAB iş akışı (KEYSTORE_BASE64 vb. gizli değişkenler).
- Premium: Play Billing 7 (`billing-ktx`), abonelik + tek seferlik ürün, açılışta/onResume'da queryPurchasesAsync ile doğrulama, 3 gün içinde acknowledge, otomatik yenileme ve iptal bilgilendirmesi, "geri yükle" ve "aboneliği yönet" bağlantısı; temel özellikleri (widget, ezan) ücretli yapma.
- Yeni kişisel geliştirici hesaplarında üretimden önce 12 testçi / 14 gün kapalı test zorunlu.

## Kotlin başlatma sırası tuzağı (enum + dosya düzeyi val)
- Enum girdileri aynı dosyadaki `private val` (ör. renk paletleri) değerlerine erişiyorsa ve aynı dosyada
  `mapOf(Enum.X to ...)` gibi enum'u kullanan bir üst düzey val varsa, sınıf başlatma döngüsünde enum
  sabitleri `null` görünür → `NoSuchElementException: Key X is missing in the map`. Çözüm: haritayı
  `by lazy { ... }` yap veya `when` kullan.
- Emülatör betiğinde Dahası gibi ızgaralarda hedef kutu ekran dışında kalabilir: `for i in 1..5; tap_text X && break; swipe` kalıbı.
- Kart üstü süsleme (filigran) çizerken opak "kesme" dairesi kullanma; `Path.op(..., PathOperation.Difference)` ile gerçek şekil çiz, düşük alfa ver.

## iOS sürümü (SwiftUI) — Android uygulamasından port
- Proje `ios/` altında XcodeGen (`project.yml`) ile; `.xcodeproj` depoya girmez, CI'da `xcodegen generate`.
- CI: `macos-15` runner, `xcodebuild ... -sdk iphoneos CODE_SIGNING_ALLOWED=NO build` → `.app`'i `Payload/` içine koyup zip'le = imzasız `.ipa`
  (AltStore/Sideloadly ile kurulur, 7 günde bir yenilenir). Apple Developer hesabı varsa TestFlight.
- İçerik aktarımı: saf Kotlin veri dosyalarını kotlinc ile (Android sınıflarına küçük stub'larla) derle, yansıma (reflection) ile
  JSON'a dök; `JAVA_OPTS=-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8` şart, yoksa Türkçe karakterler "?" olur.
- Android vector drawable → SVG: pathData'yı `<path d>` yap; asset catalog'da `preserves-vector-representation` + `template`.
- Sesler: iOS bildirim sesi ≤30 sn ve caf/wav/aiff olmalı → `ffmpeg -t 29.5 -c:a pcm_s16le -f caf`.
- iOS arka planda kod çalıştıramaz: bildirimleri günler öncesinden planla (en fazla 64 bekleyen); vakitleri aylık API'den
  (Aladhan `calendarByAddress`, method=13 Diyanet) önbelleğe al; çevrimdışı için astronomik hesap yedeği.
- Tuzaklar: StoreKit'in `AppStore` tipiyle çakışmaması için kendi sınıfına başka ad ver; iOS 16'da `Text + Text` için
  `.foregroundColor` kullan (`foregroundStyle` Text döndürmez); `.topBarTrailing` iOS 17 → `.navigationBarTrailing`;
  Swift sözlük sabitinde tekrarlanan anahtar çalışma anında çöker; AdMob SPM'de UMP ürün adı `GoogleUserMessagingPlatform`.

## Güvenlik ve test kontrol listesi (yayın öncesi)
- Android: `network_security_config` ile HTTP kapalı (minSdk<28'de varsayılan açık!), `allowBackup=false`, exported bileşenleri
  gözden geçir, PendingIntent'ler `FLAG_IMMUTABLE`, FileProvider yalnız gereken klasör, loglarda kimlik/jeton yok.
- iOS: `PrivacyInfo.xcprivacy` (UserDefaults: CA92.1, App Group: 1C8F.1), ATS açık, test başlatma argümanları `#if DEBUG`,
  UMP "Reklam gizlilik seçenekleri" ayarlarda.
- CI: iş akışlarında `permissions: contents: read`; yalnız ekran görüntüsü kaydeden işe `write`. Otomatik commit'lerde
  `git pull --rebase -X theirs` ile çakışmayı çöz, başarısızlıkta `exit 1`.
- Testler: Android JUnit + Robolectric (`@Config(sdk=[34])`, Robolectric yerel repo yolu koşullu olmalı), iOS XcodeGen
  `bundle.unit-test` hedefi + `xcodebuild test`. Tarih/takvim mantığını sabit tarihlerle test et: sabit kodlanmış yıl
  tarihleri ve platformlar arası hicri takvim farkları (ICU vs Apple Ümmü'l-Kura) testlerle yakalandı → dini günleri
  resmi (Diyanet) Ramazan tablosuna sabitle. Çeviri içeriğinde boş alan testi ekle.

## Depo/CI GitHub dışına taşınırsa (Bitbucket)
- Claude Code bulut oturumları yalnız GitHub depolarını otomatik bağlar; Bitbucket'a gönderim kullanıcının bilgisayarından
  (veya oturumda verilen erişim anahtarıyla) yapılır. Kodun yedeğini (zip) kullanıcıya her zaman ver.
- Derleme: Bitbucket Pipelines ücretsiz planda ayda 50 dk ve macOS yok → iOS için Codemagic (`codemagic.yaml`,
  ücretsiz 500 dk macOS, Bitbucket'a bağlanır). Android yedeği `bitbucket-pipelines.yml` (`cimg/android` imajı).
- Gizlilik politikası: GitHub Pages yerine `<workspace>.bitbucket.io` adlı depo → `https://<workspace>.bitbucket.io/...`.
- Yayın imza anahtarını ve gizli değişkenleri yalnız CI'da tutma; kullanıcının bilgisayarında da yedeği olmalı.

## Kıble pusulası (doğruluk notları)
- Açı: büyük çember formülü, Kâbe 21.422487, 39.826206 → gerçek kuzeye göre (Manisa ≈145°, İstanbul ≈151.6°, Ankara ≈160.2°).
- Diyanet sitesindeki "Kıble açısı" manyetik kuzeye göredir ve eski bir manyetik sapma ile hesaplanmıştır (İstanbul 147°, Ankara 155°, Manisa 141°) → bugünkü manyetik açıdan 1–2° farklıdır; önemsiz. Uygulamada "gerçek kuzey" ve "manyetik pusula" değerlerini ayrı göster.
- Hesaplar `util/QiblaMath.kt` içinde, `QiblaMathTest` ile test edilir (WMM sapması 2026: Saruhanlı +5.9°, İstanbul +6.4°, Ankara +6.5°). Sensör dinleyicisi DisposableEffect(Unit) içinde kurulduğundan sapmayı `rememberUpdatedState` ile oku (konum sonradan gelince güncellensin).
- Android: TYPE_ROTATION_VECTOR manyetik kuzey verir → `GeomagneticField.declination` ekle. getOrientation + remapCoordinateSystem eşik değiştirmesi (yatay↔dik) sıçrama/ters yön üretebilir; yerine R'den eğimden bağımsız yön: `east = |R8|·U.e − max(0,U.up)·R2`, `north = |R8|·U.n − max(0,U.up)·R5` (U = ekran üst kenarı ekseni, ekran dönüşüne göre ±X/±Y), `atan2(east, north)`.
- iOS: `trueHeading` (konum gerekir, yoksa magneticHeading → sapma kadar hata, kullanıcıyı uyar); `headingOrientation = .portrait`; kadranı animasyonla döndürürken 359→0 geçişinde ters tam tur atmaması için kesintisiz (unwrap) açı kullan.
- Pusuladan bağımsız doğrulama: Kıble Saati (güneşin azimutu = kıble açısı) ve ekranda güneş işareti.

## Namaz vakitleri: Diyanet ile dakikası dakikasına eşleşme
- Astronomik hesap (Aladhan method=13 veya yerel NOAA) Diyanet'le her gün tutmaz: Diyanet'in modeli saniye düzeyinde farklı (Saruhanlı'da −40…+66 sn); 32 günde 192 vakitten 44'ü ±1 dk sapıyor. Hiçbir koordinat + yuvarlama (ceil/round/floor) kombinasyonu tümünü tutturmuyor.
- Çözüm: Türkiye ve Diyanet'in listelediği yurt dışı şehirlerde vakitleri doğrudan Diyanet tablosundan al: `ezanvakti.emushaf.net` (`/ulkeler` → `/sehirler/{UlkeID}` → `/ilceler/{SehirID}` → `/vakitler/{IlceID}`; ~30 gün, `KibleSaati` dahil). İsimleri Türkçe karakterden arındırarak eşle; merkez ilçeler (Yunusemre, Şehzadeler, Eyyübiye…) Diyanet'te il adıyla geçer. Servis anlık reddedebilir → 1 tekrar.
- Aylık tabloyu sakla: çevrimdışı ~30 gün doğru; alarm kurarken vakit geçmişse YARININ vaktini tablodan al (bugünün saatini yarına kopyalama).
- Öncelik: Diyanet tablosu → Aladhan (Akşam ceil düzeltmesi) → yerel hesap. Önbellek anahtarını sürümle (v3) ki eski hatalı vakitler silinsin. Gizlilik politikasına yeni sunucuyu ekle.
- Diyanet temkinleri (resmi): İmsak 0, Güneş −7, Öğle +5, İkindi +4, Akşam +7, Yatsı 0; açılar 18°/17°.
- Diyanet "Kıble Açısı" = manyetik, ~2010 sapmasıyla (Saruhanlı 141, İstanbul 147, Ankara 155 birebir). iOS'ta WMM yok: bugünkü sapma − 0,1°×(yıl−2010,5).
- Kıble ekranı testleri: Robolectric'te `captureToImage` zaman aşımına uğrar → `createAndroidComposeRule<ComponentActivity>()` + `decorView.draw(Canvas)`; sonsuz `LaunchedEffect` döngüsü varsa `mainClock.autoAdvance = false`.
- Pusula kararlılığı: uyarlamalı süzgeç (birim vektör, sakin τ=0,45 sn / dönüşte τ=0,08 sn, histerezis 15°→2°, 0,6° ölü bölge) → 4° ham gürültü ekranda 0,5°, 90° dönüş 0,32 sn'de; manyetik parazit = ölçülen alan WMM beklenen alandan %30+ farklı.
