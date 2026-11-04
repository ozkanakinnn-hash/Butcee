# İlaç Takip (Flutter)

`docs/ilac-takip/` altındaki tasarım dokümanlarının çalışan iskeleti. Android + iOS, Türkçe + İngilizce.

## Neler çalışıyor

| Alan | Durum |
|---|---|
| Esnek dozaj: belirli saatler, gün aşırı / N günde bir, haftanın günleri, N saatte bir, döngüsel (21/7), gerektiğinde | ✅ `lib/domain/dose_rule.dart` |
| Aç/tok karnına rozetleri (renk + ikon + kelime) | ✅ |
| Güvenilir alarm: kritik ilaçta `alarmClock` modu, `FLAG_INSISTENT`, ısrarcı tekrar zinciri, yeniden başlatmada geri yükleme | ✅ `lib/services/local_notification_scheduler.dart` |
| Bildirimden Aldım / 10 dk Ertele / Atla — uygulama kapalıyken de (arka plan izolatı) | ✅ `lib/bootstrap.dart` |
| Erteleme durum makinesi + "Bekleyenler" paneli (veritabanından çizilir, kaybolmaz) | ✅ |
| Kaçırılan doz tespiti (grace süresi), 10 sn "Geri al" | ✅ |
| Kayan 48 saatlik alarm penceresi, iOS 64 bildirim bütçesi | ✅ `lib/services/alarm_planner.dart` |
| Stok otomatik düşer, düşük stok bildirimi | ✅ |
| Karekod (GS1 DataMatrix) + EAN-13 okuma, SKT/seri/lot ayrıştırma, aynı kutu iki kez eklenmez | ✅ `lib/domain/gs1_parser.dart`, `lib/ui/scan/` |
| Basit Mod: büyük yazı, AAA kontrast, tek ekran, sesli okuma, "Yakınımı ara", PIN kilidi | ✅ `lib/ui/senior/` |
| Alarm güvenilirliği ekranı: izinler, pil kısıtlaması, üretici (Xiaomi, Huawei…) ayarları, test alarmı | ✅ `lib/ui/settings/reliability_screen.dart` + `MainActivity.kt` |
| Firestore senkronu, bakım veren paylaşımı, FCM, PDF rapor, eczane | ⏳ Sonraki adım — değişiklikler şimdiden `outbox` tablosuna yazılıyor; kurallar `firebase/` altında |

## Mimari

```
lib/
├── domain/      Saf Dart: dozaj kuralları, durum geçişleri, uyum hesabı, GS1 ayrıştırıcı
├── data/        sqflite yerel veritabanı (doğruluk kaynağı), ilaç kataloğu
├── services/    Alarm zamanlayıcı, planlayıcı, doz işlemleri, güvenilirlik, ayarlar
├── state/       Riverpod sağlayıcıları
├── ui/          Ekranlar (normal + Basit Mod)
├── l10n/        app_tr.arb (şablon), app_en.arb + üretilen sınıflar
├── bootstrap.dart  Servis kurulumu + arka plan bildirim girişi
└── main.dart
```

Altın kural: **alarm kararı yalnızca yerel veritabanından verilir**; ağ olmasa da çalar. Her işlem (aldım/ertele/atla) `DoseActionService` üzerinden geçer: DB güncellenir → alarmlar iptal edilir/yeniden kurulur → `outbox`'a senkron kaydı düşülür.

## Geliştirme

```bash
flutter pub get        # l10n sınıfları otomatik üretilir (flutter gen-l10n)
flutter analyze
flutter test           # 47 test: alan, servis (sqflite_ffi) ve widget duman testleri
flutter run
```

CI: `.github/workflows/ilac-takip-flutter.yml` her push'ta analiz + test + debug APK üretir (artifact: `ilac-takip-debug-apk`).

## Yayın öncesi yapılacaklar

- [ ] Paket adı `com.ilactakip.ilac_takip` → kendi alan adınız.
- [ ] Gerçek cihazda alarm matrisi (Doze, yeniden başlatma, Xiaomi/Samsung/Huawei) — `docs/ilac-takip/02-…md` §6.
- [ ] Play Console: `USE_FULL_SCREEN_INTENT` ve pil optimizasyonu beyanları.
- [ ] iOS: Xcode'da "Time Sensitive Notifications" yeteneği; bildirim aksiyonlarını cihazda test.
- [ ] `assets/catalog/sample_catalog.json` yalnızca **örnek** veridir; gerçek katalog TİTCK listelerinden içe aktarılmalı.
- [ ] Firebase: `flutterfire configure`, `firebase/firestore.rules` dağıtımı, outbox → Firestore senkronu.
