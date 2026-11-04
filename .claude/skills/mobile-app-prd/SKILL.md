---
name: mobile-app-prd
description: Mobil uygulama konsepti ve ürün gereksinim dokümanı (PRD) hazırlama. İsim önerileri, önceliklendirilmiş özellik listesi (Must/Nice + Free/Premium), kullanıcı akışları, ekran listesi, bilgi mimarisi, renk paleti/tasarım yönü, onboarding ve abonelik fiyatlandırması. Yeni bir uygulama fikri için spec/PRD istendiğinde kullan.
---

# Mobil Uygulama PRD — Öğrenilenler

Örnek çıktı: `docs/shift-worker-app-prd.md` (vardiya çalışanları için "Nöbet" uygulaması).

## Dosya ve teslim
- PRD'yi repoda `docs/<uygulama>-prd.md` olarak yaz, commit + push et.
- Başta meta tablo (isim, sürüm, tarih, platform, diller) + içindekiler.

## Zorunlu bölüm iskeleti
0. Problem, vizyon, ölçülebilir MVP hedefleri, non-goals, 3-4 persona (TR + EN kullanıcı karışık)
1. 3-5 isim önerisi (tablo: gerekçe, TR/EN uyumu) + tavsiye + mağaza kısa/uzun açıklama **TR ve EN**
2. Özellik listesi: ID'li tablolar (CAL-01…), Öncelik M/N, Tier F/P — freemium sınırlarını net yaz (ör. "3 şablon / sınırsız")
3. Kullanıcı akışları: ASCII oklarla (ilk açılış → ilk değer < 2 dk hedefi, en sık akış tek elle)
4. Ekran listesi (S-01…) + ana ekran ASCII wireframe
5. Bilgi mimarisi ağacı (🔒 = premium) + yerel veri modeli (entity/alan tablosu)
6. Tasarım: ilkeler, light/dark (+ isteğe bağlı özel tema) hex token tabloları, kategori renkleri **renk + ikon + harf**, tipografi, erişilebilirlik (WCAG AA, 48 dp)
7. Onboarding: adım tablosu, EN ve TR metinler yan yana, izinleri değeri gösterdikten sonra iste
8. Fiyatlandırma: Free vs Premium karşılaştırma, TRY/USD/EUR (TR için alım gücüne göre, kur çevirisi değil), yıllık + deneme varsayılan, lifetime, indirim kampanyaları
- Ekler: teknik mimari (paket seçimleri, performans bütçesi, offline-first, KVKK/GDPR), iş kuralları/hesaplamalar (formüller), yerelleştirme (ARB, ICU plural, Türkçe i/İ büyük harf, sözlük, örnek bildirim çiftleri), KPI, riskler, yol haritası, sprint planı

## Kullanıcı kuralları (her uygulamada)
- **Türkçe + İngilizce tam destek**, cihaz diline göre varsayılan, Ayarlar'dan anında değiştirme.
- Dark mode zorunlu, sakin/güvenilir ton, suçlayıcı olmayan dil.
- Flutter tercih: Riverpod, Drift (SQLite), flutter_local_notifications + timezone, intl/ARB, fl_chart, go_router, RevenueCat.
- iOS 64 bekleyen bildirim sınırı → kayan 14 günlük zamanlama + uygulama açılışında uzlaştırma.
