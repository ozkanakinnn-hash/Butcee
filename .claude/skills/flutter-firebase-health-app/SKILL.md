---
name: flutter-firebase-health-app
description: Flutter + Firebase ile hatırlatıcı/alarm, paylaşım ve sağlık verisi içeren mobil uygulama (ör. ilaç takip) tasarlarken ve kodlarken kullan. Güvenilir arka plan alarmları, erteleme durum makinesi, Firestore paylaşım kuralları, FCM tetikleyicileri, karekod (GS1) okuma, PDF rapor, eczane/konum, yaşlı dostu "Basit Mod" kalıplarını içerir.
---

# Flutter + Firebase Sağlık/Hatırlatıcı Uygulaması — Öğrenilenler

Ayrıntılı referans tasarım: `docs/ilac-takip/01-ileri-ozellikler.md` ve `02-rakip-analizi-ve-mimari.md`.

## 0. Kullanıcının kalıcı kuralları
- Her uygulamada **Türkçe + İngilizce** (Flutter: `flutter_localizations` + `intl` + `app_tr.arb`/`app_en.arb`; sunucu bildirimleri alıcının `locale` alanına göre).
- Türkçe arayüz/doküman; kod tanımlayıcıları İngilizce.

## 1. Alarmlar (en çok 1 yıldız sebebi)
- **Offline-first**: alarm kararı yerel DB'den (`drift`) verilir; Firestore yalnızca senkron.
- Android: `flutter_local_notifications` `zonedSchedule` + `AndroidScheduleMode.alarmClock` (Doze'dan muaf) kritik dozlarda; diğerlerinde `exactAllowWhileIdle`.
- `flutter_background_service` alarm için **yanlış araç** (pil, Android 14 FGS türü). Önermeyin.
- Israrcı alarm = `additionalFlags: [4]` (FLAG_INSISTENT) + her N dk'lık önceden kurulmuş tekrar zinciri; "Aldım" tüm zinciri iptal eder. Kimlik şeması: `doseIntId*16 + k`.
- Kanal `audioAttributesUsage: AudioAttributesUsage.alarm`; kanal sesi sonradan değişmez → yeni kanal ID (`_v2`).
- İzinler: POST_NOTIFICATIONS (13+), SCHEDULE_EXACT_ALARM (14'te varsayılan kapalı → `requestExactAlarmsPermission()`), USE_FULL_SCREEN_INTENT (14+ Play beyanı), RECEIVE_BOOT_COMPLETED + eklentinin `ScheduledNotificationBootReceiver`'ı.
- Play politikası: `USE_EXACT_ALARM` yalnız çalar saat/takvim uygulamaları için — ilaç uygulamasında kullanma.
- Pil: `isIgnoringBatteryOptimizations` + `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`; OEM otomatik başlatma intent'leri `try/catch` zinciri (Xiaomi, Huawei, Oppo, Vivo) → son çare uygulama detay sayfası. Durum ekranı gerçek durumu okusun, "Test alarmı (1 dk)" sunsun.
- iOS: en fazla **64 bekleyen yerel bildirim** → kayan pencere + bütçe (~60). Time Sensitive yeteneği; Critical Alerts özel Apple yetkisi ister. Ses ≤30 sn.
- Bildirim aksiyonları kapalı uygulamada ayrı izolatta: top-level `@pragma('vm:entry-point')` + `DartPluginRegistrant.ensureInitialized()`; Firebase yerine yerel DB'ye yaz, outbox ile senkronla.
- `reconcile()` tetikleyicileri: açılış, plan değişimi, saat dilimi değişimi, günlük `workmanager`.
- `flutter_timezone` ^4 → `getLocalTimezone()` `TimezoneInfo` döndürür (`.identifier`).

## 2. Veri modeli kalıpları
- Doz örnekleri deterministik kimlik: `scheduleId_yyyyMMddTHHmm` (idempotent, çevrimdışı çakışma yok).
- Erteleme birinci sınıf durum: `pending | snoozed | taken | skipped | missed` + `snoozedUntil`, `snoozeCount`, `graceUntil`. "Bekleyenler" paneli bildirimlerden değil DB sorgusundan çizilir.
- Esnek dozaj: `sealed class DoseRule` → FixedTimes (+weekdays, everyNDays), Interval (N saatte bir), Cyclic (21/7), AsNeeded. Gün artışı `DateTime(y,m,d+1)` (DST güvenli).
- Aç/tok: öğün saatleri bir kez girilir, ofsetle çözülür. Rozet renklerinde kırmızı/yeşil çiftinden kaçın (renk körlüğü); ikon + kelime + renk.
- "Kaçırıldı" tespiti **sunucuda** (zamanlanmış CF, collectionGroup `status in [pending,snoozed]` & `graceUntil <= now`, bileşik indeks) — hasta cihazı kapalı olabilir. Cihaz 7 günlük `pending` örnekleri önceden yazar.

## 3. Paylaşım / güvenlik
- Bağlantı belgesi deterministik ID: `careLinks/{patientId}_{caregiverId}` → kurallarda tek `get()` ile izin kontrolü (`permissions.canViewLogs` vb.).
- Davet/oluşturma/silme yalnız Cloud Functions; istemci `create/delete: if false`.
- Doktor = hesapsız, süreli 128-bit token linki (`reportShares/{token}`) → Hosting rewrite → HTTPS CF; Firestore'u public açma.
- FCM token'ları `users/{uid}/devices/{deviceId}`; `registration-token-not-registered` hatasında sil.
- Sağlık verisi KVKK md.6 özel nitelikli → açık rıza ekranı, Firestore bölgesi `europe-west*`, kaskad silme.

## 4. Türkiye'ye özgü
- İlaç kutusu: EAN-13 (`869…`) + **karekod = GS1 DataMatrix** (QR değil): (01) GTIN-14, (21) seri, (17) SKT YYAAGG (GG=00 → ay sonu), (10) lot; ayırıcı `\u001d`; `]d2` önekini sil; GTIN-14 baştaki 0 atılarak 13 haneye normalize.
- `mobile_scanner` formatları: `dataMatrix, ean13, qrCode`.
- Prospektüs için açık API yok → TİTCK listelerinden haftalık CF içe aktarma + uygulamaya gömülü SQLite katalog + Firestore yedek + kullanıcı önerisi (moderasyon). KT/KÜB belgelerini kopyalama, bağlantı ver.
- Nöbetçi eczane: ulusal resmi API yok → `DutyPharmacySource` arayüzü (ticari API / oda sayfaları izinli), önbellek + `validUntil`, "gitmeden arayın" uyarısı.
- E-reçete (Medula/e-Nabız) üçüncü tarafa kapalı → kodu kullanıcı girer; uygulama yalnız hatırlatır. Reçete türleri: normal/kırmızı/yeşil/mor/turuncu.
- Places API (New): `places:searchNearby`, `includedTypes:["pharmacy"]`, `X-Goog-FieldMask` zorunlu, anahtar CF proxy'de (Secret Manager).

## 5. PDF / rapor
- `pdf` + `printing` + `share_plus`; Türkçe karakter için TTF gömülü (`NotoSans`); QR için `pw.BarcodeWidget(barcode: pw.Barcode.qrCode())`.
- Uyum = (zamanında+geç)/(planlanan − gelecek − gerektiğinde − grace'i dolmamış). Zamanında penceresi varsayılan ±60 dk.

## 6. Basit Mod (yaşlı kullanıcı)
- Tek ekran tek görev, sekme yok; birincil düğme ≥72 dp (alarmda 96 dp), metin ≥22 sp, ilaç adı ≥32 sp; AAA kontrast (7:1).
- `MediaQuery.textScaler` üzerine kullanıcı çarpanı (clamp 1.0–2.2); onay diyaloğu yerine 10 sn "Geri Al".
- `flutter_tts` ile alarm sesli okunur; `wakelock_plus`; ilaç fotoğrafı + takma ad; "Yakınımı Ara" her ekranda.
- Ayarlar PIN/uzun basma ile kilitli; bakım veren uzaktan açıp kilitleyebilir.

## 7. Test kontrol listesi
`adb shell dumpsys deviceidle force-idle`, yeniden başlatma, saat dilimi değişimi, zorla durdurma, ertele ×max, uçak modu, iOS ≤64 bekleyen, OEM cihaz matrisi, kurallar için `@firebase/rules-unit-testing`, alarm gecikmesi telemetrisi.
