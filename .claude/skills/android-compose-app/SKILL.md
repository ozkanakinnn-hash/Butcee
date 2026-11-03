---
name: android-compose-app
description: Kotlin + Jetpack Compose Android uygulaması oluşturma/iskeletini kurma. Tek dosyalık "codebase context" dökümünden proje çıkarma, eksik kaynakları tamamlama, Gradle wrapper ve CI kurma. Yeni bir Android uygulaması başlatırken veya bir kod dökümünden proje üretirken kullan.
---

# Android Compose Uygulaması — Öğrenilenler

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
Çözüm: `.github/workflows/android-build.yml` ekle (setup-java 21 + android-actions/setup-android + `./gradlew assembleDebug`, APK'yı artifact olarak yükle) ve doğrulamayı CI'da yap.

## 5. Tasarım tercihleri (kullanıcının uygulamaları)
- Türkçe arayüz, Material 3, krem/bej/altın + zümrüt yeşili palet, karanlık mod desteği.
- MVVM + StateFlow, SharedPreferences tabanlı `AppPreferences`.
- AdMob test kimliğiyle başla; yayın öncesi gerçek kimliği hatırlat.
