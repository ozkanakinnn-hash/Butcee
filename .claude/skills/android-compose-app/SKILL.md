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
