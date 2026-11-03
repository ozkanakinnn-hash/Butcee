# Ramazan Rehberi 2026

Kotlin + Jetpack Compose (Material 3) ile yazılmış Android Ramazan uygulaması.

## Özellikler
- **Ana Sayfa:** İftar/Sahur geri sayımı, günlük vakitler, 81 il + ilçe seçimi, Günün Manevi İklimi, hikâye paylaşımı
- **Kıble Bulucu:** Manyetometre + rotasyon sensörü, büyük çember formülü, titreşimli kilitlenme
- **Ramazan Rehberi:** Günlük sahur/iftar menüleri, oruç süresi ve canlı ilerleme çubuğu
- **Kur'an:** Arapça metin, Latin okunuş ve Diyanet meali
- **Oruç Takibi & Takvim:** Kaza/nafile sayaçları, dini günler
- **Yakındaki Camiler**, **Manevi Notlar**, bildirim ayarları, Ramazan topu / ney sesleri

## Proje yapısı
```
app/src/main/java/com/example/
├── MainActivity.kt          # Navigasyon (5 sekme) ve ViewModel bağlantısı
├── data/                    # Vakitler, iller, Kur'an, menüler, tercihler
├── service/                 # Firebase Cloud Messaging
├── ui/components|dialogs|screens|theme
└── util/                    # Bildirim, ses, hikâye görseli üretici
```

## Derleme
Gereksinimler: Android Studio (AGP 9.1 destekli), JDK 21, Android SDK 36.1.

```bash
./gradlew assembleDebug
```
APK: `app/build/outputs/apk/debug/`. Her push'ta GitHub Actions (`.github/workflows/android-build.yml`) debug APK üretir ve artifact olarak yükler.

### Yapılandırma
| Dosya | Açıklama |
|---|---|
| `app/google-services.json` | Firebase (FCM) için gerekli. Yoksa derleme uyarı verir, uygulama Firebase'siz çalışır. |
| `.env` | Gizli anahtarlar (Secrets Gradle Plugin). Şablon: `.env.example` |
| `debug.keystore` | Standart Android debug anahtarı (şifre: `android`). |
| Release imzası | `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` ortam değişkenleri |

> AdMob şu an Google'ın **test** uygulama kimliğini kullanıyor (`AndroidManifest.xml`). Yayın öncesi kendi kimliğinizle değiştirin.

## Kaynak dosyaları hakkında
Kod dökümünde bulunmayan kaynaklar yeniden oluşturuldu: tema (`themes.xml`), uygulama ikonu (hilal-yıldız, adaptive), `ic_quran_mushaf` / `ic_quran_rahle` vektörleri, `file_paths.xml`, yedekleme kuralları ve sentezlenmiş `sound_ramadan_cannon.wav` / `sound_sufi_ney.wav` sesleri. İsterseniz bunları kendi görsel/ses dosyalarınızla aynı adla değiştirebilirsiniz.
