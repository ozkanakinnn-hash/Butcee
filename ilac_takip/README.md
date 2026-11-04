# Vitamin Takip (Flutter)

Vitamin ve takviye takibi için Android + iOS uygulaması. Arayüz Türkçe ve İngilizce.
İlk konseptten korunanlar: güvenilir alarmlar, erteleme ve "Bekleyenler" paneli, uyum istatistikleri ve PDF dışa aktarma.
Kaldırılanlar (v4): karekod/barkod tarama, eczane bulma, yan etki bildirimi, Basit Mod.

## Ekranlar

| Sekme | İçerik |
|---|---|
| **Bugün** | İlerleme halkası (4/6 alındı), 🔥 seri, etkileşim uyarısı, bekleyenler, zaman dilimine (Erken sabah 07:00 …) ve aç/tok durumuna göre dozlar, "Bir doz daha kaydet", ruh hâli |
| **Takviyeler** | Takviyelerim, hazır setler, dışa aktar (PDF), takviye ayrıntısı (RDA/AI/UL, günlük üst sınır uyarısı, uyumlu / ayrı al / dikkat listeleri, sonraki 14 gün) |
| **Panel** | Takviye ara, etkileşim kontrolü, hazır setler, dışa aktar (PDF), hatırlatıcılar, alarm güvenilirliği, Öğren, ayarlar |
| **İstatistik** | Denge puanı (uyum + öz bildirim), seri, bu hafta / bu ay, son 14 gün, takviye bazında 30 günlük uyum, ruh hâli, dışa aktar (PDF) |
| **Öğren** | Temel bilgiler (RDA/AI/UL, birimler), Semptomlar (teşhis değildir), Emilim (artırır/azaltır) |

Sıklık seçenekleri: her gün, gün aşırı, Pzt·Çar·Cum, yalnızca hafta sonları, N günde bir, belirli günler, kullanım/ara döngüsü (ör. 21/7), N saatte bir, gerektikçe.

## Kaynaklar ve sınırlar

- Referans değerler: NIH Office of Dietary Supplements, 19–50 yaş yetişkin (`lib/data/supplement_catalog.dart`). Uygulama doz **önermez**; etiketteki doz girilir, toplam günlük üst sınırı (UL) aşarsa uyarır.
- Etkileşim kuralları yaygın ve iyi bilinen takviye etkileşimlerini kapsar (kalsiyum–demir, çinko–demir, çinko–bakır, C vitamini–demir, D vitamini–kalsiyum…). İlaç etkileşimleri için eczacıya yönlendirilir.

## Mimari

```
lib/
├── domain/      Saf Dart: dozaj kuralları, durum geçişleri, uyum/seri/puan, GS1 ayrıştırıcı
├── data/        sqflite (v2 şema + geçiş), takviye kataloğu ve içerik, ilaç kataloğu
├── services/    Alarm zamanlayıcı/planlayıcı, doz işlemleri, takviye servisi, PDF, çökme raporlayıcı
├── state/       Riverpod sağlayıcıları
├── ui/          shell (alt gezinme), today, supplements, panel, stats, learn, settings, alarm
└── l10n/        app_tr.arb (şablon), app_en.arb
```

## Çökme düzeltmeleri (v2)

1. **Tam zamanlı alarm izni:** Android 12+ cihazlarda (14'te yeni kurulumlarda varsayılan) bu izin yoksa alarm kurmak `exact_alarms_not_permitted` hatası fırlatıyordu. Bu hata açılışta yakalanmıyordu. Artık izin yoksa birkaç dakika sapabilen moda (`inexactAllowWhileIdle`) düşülüyor ve Bugün ekranında uyarı gösteriliyor.
2. **Türkçe tarih verisi:** `runApp` öncesinde ve bildirim aksiyonlarının arka plan izolatında `DateFormat('tr')` çağrısı `LocaleDataException` fırlatıyordu. Bunun için `initIntl()` eklendi.
3. **Açılış koruması:** Açılış artık asla beyaz ekranda kalmıyor; hata olursa okunabilir bir hata ekranı çıkıyor. Yakalanmamış hatalar dosyaya yazılıyor ve sonraki açılışta "Raporu kopyala" seçeneği sunuluyor.

## Geliştirme

```bash
flutter pub get
flutter analyze
flutter test        # alan, servis (sqflite_ffi), PDF ve 360×780 dp ekran duman testleri
flutter drive --driver=test_driver/integration_test.dart --target=integration_test/app_test.dart   # cihaz/emülatör
```

## Boyut

- Telefon için **release + R8 küçültme + mimari başına APK** (`app-arm64-v8a-release.apk`). 115 MB'lık dosya tüm mimarileri içeren *debug* derlemesiydi.
- PDF yazı tipleri yalnızca Latin + Türkçe karakterlerle alt kümelendi: 1,1 MB → 149 KB (`pyftsubset`, OFL lisansı korunur).
- Cihazda: senkron kuyruğu 30 günden, doz geçmişi ve ruh hâli 400 günden eski kayıtlar açılışta silinir (`pruneOldData`); çok silinirse `VACUUM`.
- APK'lar depodaki sabit debug anahtarıyla imzalanır → güncellemeler eskisinin üzerine kurulur.

CI (`.github/workflows/ilac-takip-flutter.yml`):
- **build:** analiz, test, release APK'lar (`vitamin-takip-arm64-apk`, `vitamin-takip-eski-telefon-armv7-apk`) ve boyut raporu.
- **emulator:** küçültülmüş release APK'nın açılış/çökme kontrolü (`tool/release_smoke.sh`), ardından entegrasyon testi ve ekran görüntüleri (`docs/vitamin-takip/screenshots`).
