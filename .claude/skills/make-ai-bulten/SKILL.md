---
name: make-ai-bulten
description: Make (make.com) + ÜCRETSİZ yapay zekâ (Gemini ücretsiz katmanı, yedek GitHub Models) ile zamanlanmış, telifsiz haber/özet bülteni üretip X'e (Twitter) zincir olarak gönderme — hafta içi günde N akış (sabah/öğle/akşam/gece), hafta sonu kapalı, "gelişme yoksa tekrar etme" hafızası (Firestore), aynı olayı gruplama, Make Free işlem kotasına sığdırma. Make, webhook, otomatik sosyal medya paylaşımı, AI özet bülteni isteklerinde kullan.
---

# Make + Ücretsiz Yapay Zekâ Bülteni — Öğrenilenler (Haber Akışı X bülteni)

Referans: `news-aggregator/docs/MAKE_X_BULTEN.md`, `firebase/functions/src/{bulletin,ai,bulletin-prompt}.js`,
`scripts/send-bulletin.js`, `.github/workflows/news-x-bulletin.yml`.

## Kullanıcı tercihleri
- **Ücretsiz seçenekler** öncelikli (kullanıcı isteği, 2026-11-27). Ücretli bir parça kaçınılmazsa açıkça söyle
  ve ücretsiz alternatif öner.
- Bülten **hafta sonu gönderilmez** (cron `* * 1-5` + kodda İstanbul saatine göre `isWeekend` koruması;
  elle deneme için `force`). Hafıza penceresi 96 saat, böylece pazartesi cumayı bilir.

## Mimari (ücretsiz)
- Ağır iş GitHub Actions'ta (ücretsiz): Firestore'dan haber, olay gruplama (başlık Jaccard ≥0.45, 6 harflik kök,
  `foldForSearch`), yapay zekâ çağrısı, Firestore hafıza (`bulletin_topics`).
- **Make yalnızca paylaşır**: Webhook → X Create a Post (part1) → Router [dal1: part2→part3→part4 yanıt zinciri,
  `Exists` filtreleri | dal2: Webhook response "ok"] ≈ 7 işlem/çalışma → hafta içi 4 akışta ~620/ay (Free 1.000).
  Yapay zekâ veya hafıza Make'te olursa ~1.500 işlem/ay olur ve ücretli plana çıkar.
- Hafıza yalnızca Make "ok" dönünce yazılır (X hatasında Break → "ok" yok → sonraki akışta yeniden dener).

## Ücretsiz yapay zekâ
- **Gemini** (aistudio.google.com, kart yok, faturalandırmayı AÇMA): REST
  `POST v1beta/models/{model}:generateContent`, başlık `x-goog-api-key`, `systemInstruction`,
  `generationConfig.responseMimeType: application/json`, maxOutputTokens yüksek (Flash "thinking" jetonu harcar).
  Model adı ortam değişkeniyle değiştirilebilir olsun (`GEMINI_MODEL`); modeller sık kaldırılıyor.
- **GitHub Models** yedek: `POST https://models.github.ai/inference/chat/completions`, `GITHUB_TOKEN`,
  iş akışında `permissions: models: read`. İstek başına giriş sınırı küçük → daha az olay (30) ve kısa özet gönder.
- Sağlayıcı zinciri: Gemini → hata/429 → GitHub Models. Çıktı JSON'u çitlerden temizle (`parseModelJson`).

## İçerik kuralları
- Telif: kopya yok, ≤5 ardışık kelime, alıntı yok, uydurma yok. Kod 6 kelimelik ortak dizileri arar ve uyarı yazar.
- Hukuk: masumiyet karinesi, çocuk/mağdur adı yok, hakaret aktarma yok.
- X Premium yok sayılır: zincir ≤4 parça, her parça numarasıyla ≤280 (`normalizeThread` zorlar).
- X API ücretsiz gönderi hakkı belirsiz/değişken → kullanıcıya Developer Portal'da kontrol ettir; yoksa ücretsiz kanal
  (Telegram) öner. Limit uydurma.

## Süreç
- `dry_run` varsayılan açık (elle çalıştırmada); 2–3 gün metin kontrolü sonra Make ON.
- Secret yoksa iş akışı `::notice::` ile atlar; bulut ortamından haber sitelerine erişim yok → deneme Actions'ta.
- Örnek özet üretirken başka özet hesaplarının metnini kopyalama; yeniden yaz, "biçim örneği" diye işaretle.
