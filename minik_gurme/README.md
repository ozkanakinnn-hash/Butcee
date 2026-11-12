# Minik Gurme (Android, Kotlin + Jetpack Compose)

Paket adı: `akinai.minikgurme` · Palet: "Yaban Mersini & Krem" · Yayın rehberi: `docs/YAYIN-HAZIRLIK.md`

**Ücretsiz:** Bugün (öğün kaydı), 1 yaşına kadar 100 gıda, tarifler (porsiyon ayarı), Rehber (Bu Ay + kütüphane),
alerjen tanıtım planı (9 alerjen, 3 gün kuralı), alerjen hatırlatıcısı, alışveriş listesi, yedekle/geri yükle, WHO kız/erkek karşılaştırması.
**Minik Gurme Plus (abonelik, Google Play Billing):** haftalık menü planlayıcı, doktor raporu (PDF), birden fazla bebek, öğün hatırlatıcıları.
Ürün kimlikleri: `minikgurme_plus_monthly`, `minikgurme_plus_yearly` (bkz. `billing/PlusManager.kt`). Test sürümünde Ayarlar › Geliştirici › "Plus'ı test et".

**İmza:** `keystore.properties` + `minikgurme-upload.jks` proje kökünde (depoya girmez). Yoksa release imzasız derlenir.

Bebeklerde ek gıdaya geçiş uygulaması: öğün planı, 100 gıda listesi, tarifler, öneriler, haftalık pediatrik rehber,
alerjen (3 gün kuralı) takibi, büyüme grafiği ve bebek profili. Kaynak: AI Studio dışa aktarımı (Kasım 2026).

## Dil desteği: Türkçe + İngilizce
- Profil ekranında **🌐 Dil / Language** kartı. Seçim kaydedilir; ekranlar anında İngilizceye geçer.
- Kaynak metinler Türkçe koda gömülüdür. Ekranlar `androidx.compose.material3.Text` yerine `akinai.minikgurme.i18n.Text`'i
  import eder; bu, metni `assets/i18n/en.json` sözlüğünden (2.070 birebir metin: arayüz + rehber, tarif ve gıda verileri)
  ve `assets/i18n/en_patterns.json` kalıplarından (değişken içeren metinler) çevirir. Bulunamayan metin Türkçe kalır.
- Kullanıcının girdiği veriler (bebek adı, notlar) çevrilmez.

## Dışa aktarımdan yapılan temizlik
- Kullanılmayan Firebase AI / App Check, Retrofit, OkHttp, Moshi bağımlılıkları ve Secrets/Google Services eklentileri kaldırıldı
  (kodda kullanılmıyordu; `google-services.json` olmadan derleme de başarısız oluyordu).
- Release: R8 küçültme + kaynak küçültme; depodaki `debug.keystore` ile imzalı (güncellemeler üzerine kurulur).
- Çökme raporlayıcı: yakalanmamış hata dosyaya yazılır, sonraki açılışta "Raporu kopyala" diyaloğu çıkar.

## Derleme
```bash
./gradlew :app:assembleRelease   # app/build/outputs/apk/release/app-release.apk
```
CI: `.github/workflows/minik-gurme-android.yml` — APK (`minik-gurme-apk` artifact), emülatörde duman testi
(`tool/smoke.sh`: tüm sekmeler TR + EN, dil değiştirme, çökme kontrolü), ekran görüntüleri `docs/screenshots`.

## Görseller
Besin, tarif ve kart ikonları Microsoft **Fluent Emoji (3D)** çizimleridir (MIT lisansı; npm `@lobehub/fluent-emoji-3d`),
`res/drawable-nodpi/emoji_<kod>.webp` (144 px). Eşleme `ui/components/FoodImage.kt` içindedir; yeni bir emoji
eklenirse ilgili `.webp` dosyası ve eşleme satırı da eklenmelidir (yoksa emojinin kendisi gösterilir).


## Klasör yapısı (bu uygulamaya ait her şey bu klasörde)
```
minik_gurme/
├── app/        Android uygulaması (Google Play, alarmlar, Firebase SDK, yedek/PDF)
├── shared/     Ortak Kotlin kodu: tüm ekranlar, veritabanı, çeviriler, topluluk (Android + iPhone)
├── iosApp/     iPhone kabuğu (XcodeGen project.yml, SwiftUI giriş, simge)
├── docs/       Yayın rehberi (YAYIN-HAZIRLIK.md), Firebase kuralları (firestore.rules)
└── tool/       Yardımcı betikler
```
Kök dizinde kalmak ZORUNDA olan iki dosya (araçlar yalnızca orada arar):
- `codemagic.yaml` — iPhone bulut derlemesi (Codemagic yalnızca depo kökünü okur)
- `.github/workflows/minik-gurme-android.yml` — Android CI (GitHub yalnızca bu klasörü çalıştırır)
