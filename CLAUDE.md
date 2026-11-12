# Çalışma kuralları (kullanıcı)

- **APK YALNIZCA İSTENİNCE (kullanıcı kuralı, tüm projeler):** Kullanıcı açıkça "APK derle" demeden APK/derleme çıktısı
  üretme ve gönderme (release derlemesi, SendUserFile ile .apk, CI artifact linki dahil). Değişiklikleri yap, kodu derlenebilir tut,
  gerekirse birim testleri çalıştır, commit et; sonunda "APK hazır değil, isterseniz derleyeyim" diye belirt ve komutu bekle.
- Her uygulamada Türkçe + İngilizce dil desteği ve ayarlarda dil seçici olmalı; veri içeriği de çevrilmeli.
- Öğrenilen kalıplar `.claude/skills/` altında saklanır (android-compose-app, flutter-firebase-health-app).
- **Her uygulama depoda kendi klasöründe** (ör. `minik_gurme/`, `ilac_takip/`): kod, belgeler (`<klasör>/docs`), ekran görüntüleri ve
  yardımcı betikler o klasörün içinde. Kökte yalnızca araçların zorunlu tuttuğu dosyalar kalır (`codemagic.yaml`, `.github/workflows/*`).
