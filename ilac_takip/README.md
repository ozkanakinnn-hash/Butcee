# Vitamin Takip (Flutter)

Vitamin ve takviye takibi için Android + iOS uygulaması. Arayüz Türkçe ve İngilizce.
İlk konseptten korunanlar: güvenilir alarmlar, erteleme ve "Bekleyenler" paneli, karekod/barkod okuma, Basit Mod ve doktor raporu.

## Ekranlar

| Sekme | İçerik |
|---|---|
| **Bugün** | İlerleme halkası (4/6 alındı), 🔥 seri, etkileşim uyarısı, bekleyenler, zaman dilimine (Erken sabah 07:00 …) ve aç/tok durumuna göre dozlar, "Bir doz daha kaydet", ruh hâli |
| **Takviyeler** | Takviyelerim, hazır setler, program PDF'i, takviye ayrıntısı (RDA/AI/UL, günlük üst sınır uyarısı, uyumlu / ayrı al / dikkat listeleri, sonraki 14 gün) |
| **Panel** | Tüm araçlar: karekod/barkod tara, takviye ara, etkileşim kontrolü, hazır setler, program PDF, doktor raporu, yakındaki ve nöbetçi eczaneler, yan etki bildir, hatırlatıcılar, alarm güvenilirliği, Basit Mod |
| **İstatistik** | Denge puanı (uyum + öz bildirim), seri, bu hafta / bu ay, son 14 gün, takviye bazında 30 günlük uyum, ruh hâli |
| **Öğren** | Temel bilgiler (RDA/AI/UL, birimler), Semptomlar (teşhis değildir), Emilim (artırır/azaltır) |

Sıklık seçenekleri: her gün, gün aşırı, Pzt·Çar·Cum, yalnızca hafta sonları, N günde bir, belirli günler, kullanım/ara döngüsü (ör. 21/7), N saatte bir, gerektikçe.

## Kaynaklar ve sınırlar

- Referans değerler: NIH Office of Dietary Supplements, 19–50 yaş yetişkin (`lib/data/supplement_catalog.dart`). Uygulama doz **önermez**; etiketteki doz girilir, toplam günlük üst sınırı (UL) aşarsa uyarır.
- Etkileşim kuralları yaygın ve iyi bilinen takviye etkileşimlerini kapsar (kalsiyum–demir, çinko–demir, çinko–bakır, C vitamini–demir, D vitamini–kalsiyum…). İlaç etkileşimleri için eczacıya yönlendirilir.
- Eczane araçları Google Haritalar aramasını açar; nöbetçi eczaneler için ulusal açık bir API yok.
- `assets/catalog/sample_catalog.json` (ilaç barkodları) yalnızca **örnek** veridir.

## Mimari

```
lib/
├── domain/      Saf Dart: dozaj kuralları, durum geçişleri, uyum/seri/puan, GS1 ayrıştırıcı
├── data/        sqflite (v2 şema + geçiş), takviye kataloğu ve içerik, ilaç kataloğu
├── services/    Alarm zamanlayıcı/planlayıcı, doz işlemleri, takviye servisi, PDF, çökme raporlayıcı
├── state/       Riverpod sağlayıcıları
├── ui/          shell (alt gezinme), today, supplements, panel, stats, learn, settings, senior, alarm, scan
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

CI (`.github/workflows/ilac-takip-flutter.yml`):
- **build:** analiz, test ve debug APK (`ilac-takip-debug-apk`).
- **emulator:** Android 14 emülatöründe entegrasyon testi ve ekran görüntüleri (`ilac-takip-ekran-goruntuleri`).
