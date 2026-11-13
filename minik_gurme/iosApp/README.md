# Minik Gurme — iPhone

Uygulamanın tamamı Kotlin ortak modülündedir (`../shared`): ekranlar (Compose Multiplatform), veritabanı (Room), çeviriler,
topluluk (Firebase REST). Bu klasör yalnızca iPhone kabuğudur (SwiftUI giriş noktası, simge, Info.plist).

## Derleme (Mac gerekmez — Codemagic)
Not: GitHub Actions macOS makineleri bu hesapta atanmadı (iş 15 dk sırada bekleyip iptal oldu), bu yüzden iPhone derlemesi yalnızca Codemagic'te yapılır.

Depodaki `codemagic.yaml` iki iş akışı içerir:
1. **iOS derleme kontrolü (imzasız)** — Apple hesabı gerekmez; uygulamanın iPhone simülatörü için derlendiğini doğrular.
2. **iOS → TestFlight** — Apple Developer üyeliği + App Store Connect API anahtarı gerekir.

## Mac ile (isteğe bağlı)
```
brew install xcodegen
cd iosApp && xcodegen generate && open MinikGurme.xcodeproj
```
Xcode "Kotlin ortak modülünü derle" adımında `./gradlew :shared:embedAndSignAppleFrameworkForXcode` çalıştırır
(JDK 17+ ve Android SDK yolu `local.properties` içinde gerekir).

## Platforma özel parçalar (shared/src/iosMain)
- `MainViewController.kt` — giriş noktası, servis kurulumu
- `IosPlatformServices.kt` / `IosFiles.kt` — pano, bildirim izni, Dosyalar'a yedek, PDF rapor, paylaşım
- `IosReminders.kt` — yerel bildirimler (19:00 alerjen, Plus öğün hatırlatıcıları)
- Topluluk: ortak `RestCommunityBackend` (iOS API anahtarı + paket kimliği başlığı)
- **Yapılacak:** Plus aboneliği (StoreKit 2) — App Store Connect'te `minikgurme_plus_monthly` ve `minikgurme_plus_yearly` ürünleri açılınca.
