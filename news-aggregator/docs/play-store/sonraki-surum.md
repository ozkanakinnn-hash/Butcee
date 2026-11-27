# Sonraki sürüm (Android 1.0.5 / kod 201 · iOS 1.0.5) — birikmiş değişiklikler

Bu dosya, son yayınlanan sürümden (Android 1.0.4 kod 200, iOS 1.0.4 yapı 12 — 27 Kasım 2026) sonra yapılan ve
**bir sonraki derlemeye girecek** değişiklikleri toplar. Yeni sürüm derlenip mağazaya gönderilince bu liste
"Yayınlananlar" bölümüne taşınır ve boşaltılır.

## Sonraki sürüme girecekler
- **Uçtan uca ekran (Play uyarısı, SDK 35+):** `MainActivity` → `WindowCompat.setDecorFitsSystemWindows(false)`,
  `main.dart` → `SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge)`. Kullanımdan kaldırılmış çubuk rengi
  API'leri kullanılmıyor. Gündem ve maç ayrıntısı listelerine alt sistem çubuğu boşluğu eklendi.
  → Play Console'daki "Uçtan uca ekran tüm kullanıcılara gösterilmeyebilir" uyarısı bu sürümle kapanmalı.

## Mağaza sürüm notu taslağı (tr-TR)
```
• Ekran artık telefonun tamamını kullanıyor (Android 15 uyumlu)
• Küçük düzeltmeler ve iyileştirmeler
```

## Derleme hatırlatmaları
- Android sürüm kodu **201**'den devam (125 Play'de reddedildi; 200 yüklendi). pubspec: `version: 1.0.5+201`.
- iOS: `ios/RELEASE_VERSION` → `1.0.5` (yükleme), sonra `ios/SUBMIT_VERSION` → `1.0.5 <yapı>` (incelemeye gönderim).
- iOS "Yenilikler" metni: `docs/ios/yenilikler.txt` (1.0.4 ilk yayındı; 1.0.5'ten itibaren bu alan zorunlu).

## Yayınlananlar
- **1.0.4** (Android 200 · iOS 12, 27 Kas 2026): gazete düzeninde gündem, bülten güvencesi, uzaktan ayar
  (`app_config/main`), bayat canlı skoru gizleme, birikmiş haber bildirimlerini tek bildirimde toplama, Türkçe büyük harf.
