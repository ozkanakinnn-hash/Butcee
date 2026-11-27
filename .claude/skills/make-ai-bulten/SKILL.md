---
name: make-ai-bulten
description: Mevcut bir Make (make.com) senaryosuna (Webhook → Google Tablolar → Buffer → X) dokunmadan, ücretsiz Gemini ile telifsiz zamanlanmış haber özeti "akışları" ekleme — hafta içi günde 4 (sabah/öğle/akşam/gece), hafta sonu günde 1, "gelişme yoksa tekrar etme" hafızası (Firestore), aynı olayı gruplama, Make Free işlem kotası. Make, webhook, otomatik X/sosyal medya paylaşımı, AI özet bülteni isteklerinde kullan.
---

# Make + Gemini Akışları — Öğrenilenler (Haber Akışı, 27–28 Kasım 2026)

Referans: `news-aggregator/firebase/functions/src/akis.js`, `test/akis.test.js`, `docs/SOSYAL_PAYLASIM.md` (X akışları).

## Önce mevcut sistemi bul (kullanıcı "zaten var" diyorsa)
- Varsayılan dal eski olabilir. `git log --all -S <anahtar kelime>` ve `git branch -r` ile asıl dalı bul; `DEVAM.md` /
  `DEVIR_NOTU.md` gibi devir notlarını oku (kullanıcı kuralları orada: GitHub'da zamanlanmış iş YOK, deploy Cloud Shell'den).
- Make kayıtlarını ajan göremez (Make bağlantısı yok). Sunucu tarafı kayıtlar için yalnızca okuyan teşhis aracı:
  Actions → "Haber Akışı - Firebase'e Kur" `only_report: true`, ref = asıl dal (`scripts/durum-raporu.js`).
- Anahtarlar (Gemini vb.) depoda değil **Firebase Secret** olarak durur; asla sohbete/dosyaya yazdırma, mevcut
  `defineSecret('GEMINI_API_KEY')` olan fonksiyonun içinde çalıştır.

## Mevcut senaryoyu bozmamak
- Aynı webhook'a **aynı alan adlarıyla** yeni `tur` gönder (`metin` ≤280, `deneme:false`, `baslik`, `zaman`, `haber_id`).
  Senaryo değişmeden paylaşır; isteyen Router'da `tur` ile ayırır.
- Yeni özellik **varsayılan kapalı** (`send_akis`), ayrı deneme anahtarı (`akis_dry_run`) — ortak `dry_run` diğer
  paylaşımları da durdurur.
- Aynı içeriğin iki kez gitmemesi: akışlar açıkken eski bülten kaydı Make'e gitmez (`akis_bulten_birlikte` ile açılır).
- Ek zamanlayıcı açma: 20 dk'lık mevcut işe ekle, "saat geçti + henüz gönderilmedi (`notify_state/akis.last_id`) +
  ≤100 dk" kuralı. Ücretsiz kalır.

## Akış mantığı
- Saatler (TSİ): hafta içi 07:30/12:30/18:30/23:30; Cmt-Paz 20:30. Aralıklar boşluksuz: Cmt cuma 23:30'dan, Paz cmt
  20:30'dan, Pzt sabah paz 20:30'dan.
- Gruplama: projedeki `storyTokens` + `sameStory` (importance.js) — yeniden yazma.
- Gemini: mevcut `generate()`'e `json`, `maxOutputTokens` (8192; düşünen modeller jeton yer), `timeoutMs` (60 sn) eklendi.
  Hata → `last_id` ilerlemez, sonraki turda yeniden dener. Çıktı JSON çitlerden temizlenir.
- Hafıza: `bulletin_topics/{konu_anahtari}` (son_durum, son_paylasim, paylasim_sayisi), 96 saat AI'ya verilir,
  yalnızca webhook başarılıysa yazılır, 14 gün sonra silinir.
- X metni: başlık + sığan maddeler + uygulama bağlantısı (bağlantı 23 karakter sayılır); Buffer tek gönderi → zincir yok.
- Telif: kopya yok, ≤5 ardışık kelime, alıntı yok, uydurma yok; 6 kelimelik kopya `kopya_uyari` olarak kaydedilir.
  Hukuk: masumiyet karinesi, çocuk/mağdur adı yok.

## Maliyet
- Make Free 1.000 işlem/ay; kayıt başına modül sayısı × gönderi. Mevcut günlük sınırı hesapla ve aşacaksa
  kullanıcıya düşürmeyi öner (ör. `make_daily_cap` 12 → 7).
- X API ücretli; Buffer ücretsiz planla X'e gönderim yapılıyor.

## Kullanıcı tercihleri
- Ücretsiz seçenekler; hafta sonu günde tek "Hafta Sonu Akışı"; büyük değişiklikte önce ne yapılacağını anlat.
