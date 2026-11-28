# Android — kapalı test bitince ilk AAB'ye eklenecekler

Kullanıcı isteği (28 Kasım 2026): Android kapalı testi (12 kişi × 14 gün) bitene kadar Android koduna
dokunulmuyor. Aşağıdakiler test bitince yapılacak ilk AAB'ye (üretim) girecek. iOS'ta düzeltildi:
`ios/RamazanRehberi/Core/PrayerService.swift`, `PrayerCore.swift`, `DiyanetSource.swift`, testler `CoreTests.swift`.

## 1. Namaz vakitleri 1 dk sapıyor (ÖNEMLİ — dakikası dakikasına olmalı)

**Sebep:** `data/PrayerRepository.getPrayerTimes()` o günün vaktini önbellekte bulursa kaynağına bakmadan
döndürüyor. Diyanet'e o an ulaşılamadıysa (ağ yok, zaman aşımı, ilçe eşleşmedi) Aladhan ya da yerel hesap
değeri `saveCache()` ile kaydediliyor ve **o gün bir daha Diyanet'e sorulmuyor**. Aladhan method=13 ölçümü
(28 Kas 2026, İstanbul, 32 gün): günlerin ~%25'inde 1 dk fark (akşam 9 gün 1 dk ERKEN, yatsı ±1, ikindi ±1).

**Yapılacak:**
- `PrayerTimes`'a kaynak alanı ekle (`source = "diyanet" | "aladhan" | "local"`), `saveCache` bunu da yazsın.
  Eski önbellekte alan yoksa resmi sayılmasın.
- `getPrayerTimes`: önbellekteki kayıt `diyanet` değilse önce `DiyanetSource.today(...)` dene; başarılıysa
  üzerine yaz. Aynı ilçe için en çok 3 saatte bir dene (`diyanet_last_try|il|ilçe`).
- `ReminderScheduler.rescheduleAll()` zaten `DiyanetSource.cachedDay` kullanıyor; tablo 7 günden az ileriyi
  kapsıyorsa tabloyu yenilemeyi tetikle (iOS: `PrayerCache.needsOfficialRefresh`, 7 gün).
- Aladhan akşam düzeltmesi (`diyanetMaghrib`) yanıttaki `meta.latitude/longitude`'u kullanıyorsa kontrol et:
  adres modunda Aladhan sahte koordinat döndürebiliyor (**8.8888888, 7.7777777**). İl merkezinden 1,5°'den
  uzaksa GPS / il merkezi koordinatını kullan.
- Robolectric testi: Aladhan kaydı varken Diyanet gelince üzerine yazılıyor; eski önbellek resmi sayılmıyor.

## 2. Alarm hassasiyeti (kontrol edildi, sorun yok — bilgi)
- Ezan, sahur ve iftar alarmları `setAlarmClock` ile kuruluyor (dakikası dakikasına, Doze muaf). İzin yoksa
  `setAndAllowWhileIdle`'a düşüyor: **bu birkaç dakika gecikebilir**. Bildirim sihirbazı izni istiyor; izin
  verilmemişse ana ekranda kalıcı bir uyarı kartı göstermek düşünülebilir.
- `IFTAR_LIVE` (canlı sayaç) 3 dk, `FASTING_TIP` 15 dk pencereli. Bunlar ezan değil; bilerek böyle.

## 3. iOS ile ortak tutulacak yeni özellikler (App Store 4.3(a) reddi sonrası)
iOS'a eklenen takip / kaza / widget özellikleri onaylanırsa Android'e de aynı tasarımla eklenecek
(kullanıcı iki sürümün birbirinden ayrılmasını istemiyor). Ayrıntı: `docs/yeni-tasarim/`.

## 4. Sürüm
`gradle.properties` → `APP_VERSION_CODE` Play'deki en yüksek koddan büyük olmalı.
