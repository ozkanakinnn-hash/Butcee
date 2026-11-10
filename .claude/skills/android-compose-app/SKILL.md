---
name: android-compose-app
description: Kotlin + Jetpack Compose Android uygulaması oluşturma/iskeletini kurma. Tek dosyalık "codebase context" dökümünden proje çıkarma, eksik kaynakları tamamlama, Gradle wrapper ve CI kurma. Yeni bir Android uygulaması başlatırken veya bir kod dökümünden proje üretirken kullan.
---

# Android Compose Uygulaması — Öğrenilenler

## 0a. Kullanıcının kalıcı kuralları
- **APK YALNIZCA İSTENİNCE (kullanıcı kuralı, tüm projeler):** Kullanıcı açıkça "APK derle" demeden APK/derleme çıktısı
  üretme ve gönderme (release derlemesi, SendUserFile ile .apk, CI artifact linki dahil). Değişiklikleri yap, kodu derlenebilir tut,
  gerekirse birim testleri çalıştır, commit et; sonunda "APK hazır değil, isterseniz derleyeyim" diye belirt ve komutu bekle.

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
Önce `curl -s -o /dev/null -w '%{http_code}' https://dl.google.com/android/repository/repository2-3.xml` ile dene: erişilebiliyorsa
yerelde derle (cmdline-tools zip → `/opt/android-sdk`, `sdkmanager "platforms;android-36.1"`, `local.properties` sdk.dir).
Maven Central 429 (Too Many Requests) verirse: `--no-configuration-cache --max-workers=2` ile yeniden dene; büyük jar'ı
(ör. Robolectric `android-all-instrumented`) `https://maven-central.storage-download.googleapis.com/maven2/...` aynasından
`~/.m2/repository` altına indir. `/dev/kvm` yoksa emülatör çalışmaz → Robolectric + Compose UI test ile duman testi yap
(`createEmptyComposeRule` + `ActivityScenario.launch(intent)`, testTag ile sekmelere tıkla).
GitHub hesabı yoksa APK'yı SendUserFile ile doğrudan kullanıcıya gönder, kaynak kodu da zip olarak ver.

Eski not: bazı konteynerlerde `dl.google.com` engelli → Android SDK/Google Maven indirilemez, yerel derleme yapılamaz.
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
- Emülatörde test: uygulamadaki "widget ekle" düğmesi → sistem diyaloğunda "Add to home screen"'e dokun → ana ekran ekran görüntüsü.

## Paylaşılabilir görsel kart (Canvas → PNG → FileProvider)
- 1080x1920 Bitmap'e Canvas ile çiz; önizleme için aynı fonksiyonu `scale` (ör. 0.45) ile `Dispatchers.Default`'ta üret, `produceState` ile göster.
- Fotoğrafı `inSampleSize` ile küçültüp kemer yoluna `clipPath` ederek centerCrop çiz; metinleri `StaticLayout` ile ortala, sığmazsa yazı boyutunu küçülterek tekrar ölç.
- Paylaşım: cache/shared_images → `FileProvider` Uri → `ACTION_SEND` chooser; Instagram için `com.instagram.share.ADD_TO_STORY` (facebook_app_id gerekir), yoksa genel paylaşıma düş.
- Tasarım onayı için önce HTML/SVG ile örnek kart çiz, Playwright ile PNG al ve kullanıcıya göster. Onaydan sonra aynı sahneleri **metinsiz** 1080x1920 render edip `drawable-nodpi/*.webp` (quality ~86, kart başı 25–100 KB) olarak göm; metni uygulamada Canvas ile çiz (çok dilli ve çok mesajlı kalır). Yazı tiplerini `res/font` içine göm (Google Fonts TTF, OFL; `ResourcesCompat.getFont`). `StaticLayout` için `BREAK_STRATEGY_BALANCED` başlıkları dengeli böler.
- CI emülatöründe "Pixel Launcher isn't responding" uyarısı çıkabilir: betikte `tap_text "Wait"` ile kapat.

## Kodda gömülü binlerce Türkçe metne İngilizce ekleme (Minik Gurme'den)
- `strings.xml`'e taşımak yerine **import gölgeleme**: `com.example.i18n.Text(text: String, ...)` aynı parametrelerle
  `material3.Text(t(text))` çağırır; betikle tüm `import androidx.compose.material3.Text` satırlarını değiştir.
  Böylece veri sınıflarındaki/DB'deki içerik de ekranda otomatik çevrilir. `contentDescription` ve `showSnackbar` için `t(...)`.
- Sözlük `assets/i18n/en.json` (TR→EN, JVM 64KB metot sınırına takılmaz) + `en_patterns.json` (`[regex, "...$1..."]`,
  yakalanan grup da sözlükten çevrilir). Kalıpları yerelde `java T.java` ile `Pattern.compile` ederek doğrula.
- `AppLanguage.current` bir `mutableStateOf` → değişince her `Text` yeniden çizilir; `Locale("tr")` tarih biçimlerini
  `Locale(AppLanguage.current)` yap. Metinleri çıkarıp `idx\tmetin` dosyalarında partiler hâlinde çevir.
- Duman testi için `MainActivity` `--es lang en` ekstrasını okusun; kökte `Modifier.semantics { testTagsAsResourceId = true }`
  ile uiautomator `resource-id` = testTag üzerinden dokunsun.

## AI Studio dışa aktarımı temizliği
- README "debugConfig imzasını kaldır" der; Firebase AI/App Check, Retrofit/Moshi/OkHttp, secrets ve google-services
  eklentileri kodda kullanılmıyorsa kaldır (aksi hâlde `google-services.json` yokken derleme düşer).
- `applicationId`'yi koru (kullanıcının mevcut kurulumu üzerine gelsin), release'i depodaki debug.keystore ile imzala.

## Okunurluk, tema ve logo (Minik Gurme v1.1'den)
- **Koyu modda kaybolan yazılar**: ekranlar `TextPrimary` gibi sabit açık-tema renkleri kullanıyorsa her token'ı
  `val TextPrimary get() = if (ThemeState.dark) koyu else açık` getter'ına çevir (`ThemeState.dark` bir `mutableStateOf`,
  temada `isSystemInDarkTheme()` ile ayarlanır). Yüzlerce kullanım yeri değişmeden açık/koyu uyumlu olur; koda gömülü
  `Color(0xFF...)` yazı/kenarlık renklerini de token'lara eşle. Koyu zemin saf siyah değil #14171C, yazı kırık beyaz.
- **"Yazılar silik" şikâyeti**: `FontFamily` tek bir TTF'yi tüm ağırlıklara bağlıyorsa kalın yazı hiç kalın çizilmez.
  Gerçek ağırlıkları Google Fonts CSS'inden (`User-Agent: Mozilla/4.0` → .ttf linkleri) indir, `pyftsubset` ile
  Latin + Türkçe alt kümesine indir (~32 KB/ağırlık).
- **İlk açılış**: örnek veri (seed) koyma; `onboarding_done` tercihi yoksa ad/ay/kilo/boy (+ püre/BLW) soran ekran göster.
  DB sürümünü artır (`fallbackToDestructiveMigration`) ki eski örnek veriler silinsin. Sıfırlama butonları onay diyaloğuyla.
- **Kullanıcının logo görseli**: PIL+numpy ile HSV ton kaydırarak tema renklerine boya (bölgeye göre: başlık yazısı,
  halka, illüstrasyon), daire maske ile amblem (`drawable-nodpi/logo_emblem.webp`), yazısız orta kırpıntıyla adaptive
  icon ön planı (432px, beyaz arka plan) + mipmap PNG'leri. Fotoğraf gibi görsellerde `<monochrome>` katmanını kaldır.
- **Türkçe iyelik eki**: `trGenitive("Defne")` → "Defne'nin" (son ünlüye göre ın/in/un/ün, ünlüyle bitiyorsa n).
- **Robolectric ekran görüntüsü**: `captureToImage` takılabiliyor; `scenario.onActivity { decorView.draw(Canvas(bmp)) }`
  + `@GraphicsMode(NATIVE)`. Gece modu niteleyicisi sırası: `w411dp-h891dp-night-xxhdpi`.

## Uygulama içi görseller (Minik Gurme'den)
- Kullanıcı "görselleri kendin üret" derse: her öğe için elle SVG çiz (256 viewBox, gradyanlı yarı gerçekçi stil, altta yumuşak
  gölge), Playwright ile şeffaf PNG'ye çevir (`omitBackground: true`), `drawable-nodpi/*.webp` olarak göm. Aynı HTML sayfasında
  önizlerken gradyan `id`'lerini her çizime özgü yap (yoksa renkler karışır). Önizleme sayfasını kullanıcıya göster.
- Emoji yazı tipi cihaza göre değişir; `FoodImage(emoji, size)` gibi tek bir composable ile emoji → görsel eşlemesi yap, eşleşme yoksa emojiye düş.
- Wikimedia Commons otomatik indirmede iletişim adresli User-Agent ister; kullanıcının e-postasını izinsiz kullanma.

