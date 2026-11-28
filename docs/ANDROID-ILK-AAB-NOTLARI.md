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

### Kesin çözüm: Diyanet'in resmi yıllık tablosu (iOS'ta yapıldı, Android'e aynısı)
- `https://namazvakitleri.diyanet.gov.tr/tr-TR/<IlceID>` (yönlendirmeyi izle) sayfasında **"Yıllık Namaz Vakti"**
  tablosu var: ilçenin bütün yılı, 365 gün. İlçe numarası emushaf'takiyle aynı (İstanbul 9541, Saruhanlı 9719).
- 28.11.2026 karşılaştırması: resmi yıllık tablo = emushaf 30 günlük tablo, **32/32 gün birebir aynı**.
- Okuma: `aria-describedby="table-caption-yearly"` tablosundaki satırlar `<td>09 Şubat 2026 Pazartesi</td><td>hicri</td>`
  + 6 saat. Ay adlarında HTML kodu olabilir (`Eyl&#252;l`); çöz. iOS karşılığı: `DiyanetSource.parseYearTable`.
- Sıra: resmi yıllık tablo (yılda bir, ayda bir tazele) → emushaf 30 gün (yıl geçişi + Kıble Saati) → yalnızca hiç
  veri yoksa Aladhan/yerel hesap, ekranda "Yaklaşık vakit" uyarısıyla.

## 2. Alarm hassasiyeti (kontrol edildi, sorun yok — bilgi)
- Ezan, sahur ve iftar alarmları `setAlarmClock` ile kuruluyor (dakikası dakikasına, Doze muaf). İzin yoksa
  `setAndAllowWhileIdle`'a düşüyor: **bu birkaç dakika gecikebilir**. Bildirim sihirbazı izni istiyor; izin
  verilmemişse ana ekranda kalıcı bir uyarı kartı göstermek düşünülebilir.
- `IFTAR_LIVE` (canlı sayaç) 3 dk, `FASTING_TIP` 15 dk pencereli. Bunlar ezan değil; bilerek böyle.

## 3. iOS ile ortak tutulacak yeni özellikler (App Store 4.3(a) reddi sonrası) — kullanıcı onayladı
iOS'ta yapıldı (dal `ccr-220041a8-fdjzbw`, commit 53ae629). Android'e AYNI tema ve davranışla eklenecek:
- Takip kaydı `prayer_log_v1` = {"YYYY-MM-DD": [0..4]} (0 Sabah … 4 Yatsı). iOS: `Core/PrayerLog.swift`.
- Ana sayfa: cami fotoğraflı söz kartı yerine "Bugün" halkası (5 yay, n/5 kılındı, seri); vakit kartında her farz
  vaktin altında işaret dairesi (yalnız vakti girmişler dokunulabilir); altında Kaza özeti kartı.
- Takip ekranı: Hafta/Ay; ay takvimi renkleri 5/5, 3–4, 1–2, işaret yok; Seri / Bu ay % / Kaza; son 30 gün vakit bazında
  oranlar; en çok kaçırılan vakit ipucu; ayarlar: "Kaçırılan vakitleri kazaya ekle" (varsayılan açık).
- Otomatik kaza: yalnız EN AZ BİR vakit işaretlenen günler; eksikler ertesi gün kazaya, aynı gün iki kez eklenmez, 30 gün geriye.
- Kilit ekranı sayacının Android karşılığı: sıradaki vakte geri sayan kalıcı bildirim (mevcut IFTAR_LIVE altyapısı).
- Widget'ta "Kıldım" düğmesi (Glance/AppWidget), ezan bildiriminde "Kıldım" / "20 dk sonra hatırlat" eylemleri.
- Android'de modül ÇIKARILMAZ (kullanıcı yalnız iOS'ta çıkarmayı onayladı).
Tasarım: `docs/yeni-tasarim/6..9-*.png`.

## 4. Sürüm
`gradle.properties` → `APP_VERSION_CODE` Play'deki en yüksek koddan büyük olmalı.
