---
name: make-ai-bulten
description: Make (make.com) senaryosu + yapay zekâ (Claude) ile zamanlanmış, telifsiz haber/özet bülteni üretip X'e (Twitter) veya başka kanala webhook ile gönderme — günde N akış (sabah/öğle/akşam/gece), "gelişme yoksa tekrar etme" hafızası (Make Data store), aynı olayı gruplama, zincir/uzun gönderi. Make, webhook, otomatik sosyal medya paylaşımı, AI özet bülteni isteklerinde kullan.
---

# Make + Yapay Zekâ Bülteni — Öğrenilenler (Haber Akışı X bülteni)

Referans uygulama: `news-aggregator/docs/MAKE_X_BULTEN.md`, `src/bulletin.js`, `scripts/send-bulletin.js`,
`.github/workflows/news-x-bulletin.yml`.

## Mimari (önerilen)
- **Zamanlayıcı + veri hazırlığı kodda (GitHub Actions cron, ücretsiz)**, **yazım + paylaşım Make'te**.
  Veri zaten Firestore'daysa Make'e RSS çektirme: her öğe Make'te işlem (operation) harcar.
- Gönderici betik aynı olayı anlatan haberleri **başlık kelime Jaccard'ı (≥0.45, 6 harflik kaba kök,
  `foldForSearch` ile Türkçe karakter katlama)** ile tek "olay"da toplar, önem sırasına koyar
  (son dakika > kaynak sayısı > yenilik), en fazla ~60 olay gönderir → AI maliyeti düşer.
- Akış aralıkları İstanbul saatiyle (sabit UTC+3) hesaplanır; cron birkaç dk geç kalabileceği için akış
  `slotForTime` ile **en yakın hedef saate** göre seçilir. Cron dakikasını :00/:30 yerine :33 gibi ver.
- "Günün bülteni / yoğun gün" kararı kodda sayıyla (gün içi olay sayısı ≥ eşik) verilir, AI'ya bayrak gider.

## Tekrar etmeme (hafıza)
- Make **Data store**: key = AI'nın verdiği kalıcı `konu_anahtari` (küçük harf, tire, Türkçe karaktersiz).
  Alanlar: `son_durum`, `ilk_paylasim`, `son_paylasim`, `paylasim_sayisi`.
- Her çalışmada son 48 saatin kayıtları Text aggregator ile AI'ya verilir. Kural: gelişme varsa
  "🔄 GELİŞME:" + yalnızca yeni bilgi, yoksa hiç yazma; önceki konuda **aynı anahtarı** kullan.
- Data store'a yazma **gönderi başarılı olduktan sonra**; X modülüne Break hata işleyicisi.
- Haftalık temizlik senaryosu (7 günden eski kayıtları sil).

## AI talimatı (özü)
- Telif: başlık/özet kopyalama yok, ≤5 ardışık kelime, doğrudan alıntı yok, listede olmayan bilgi yok.
- Hukuk: masumiyet karinesi (gözaltı ≠ tutuklama ≠ hüküm), çocuk/mağdur adı yok, hakaret aktarma yok.
- Çıktı **yalnızca JSON**: `paylas`, `uzun_metin`, `zincir[]` (≤270 karakter parçalar, "(i/n)"), `konular[]`,
  `atlananlar[]`. Make'te Parse JSON; ```json çitlerini temizle.
- Model: kalite `claude-sonnet-5-5`, ucuz `claude-haiku-5-5`; temperature 0.3.

## X (Twitter) gerçekleri
- 280 karakter üstü tek gönderi **X Premium** ister; yoksa zincir: Iterator + Set/Get variable ile önceki
  gönderi ID'sine yanıt. Make'te X uygulaması yoksa HTTP OAuth 2.0 → `POST https://api.x.com/2/tweets`.
- X Developer App: *Read and write*, OAuth 2.0, callback = Make'in adresi. API planı/limitleri sık değişir →
  kullanıcıya Developer Portal'da kontrol ettir; uydurma limit yazma.
- Profilde "otomatik / AI destekli" etiketi önerilir.

## Süreç kuralları
- İlk 2–3 gün X modülü kapalı, yalnızca Make History'de çıktıyı izle; Actions'ta `dry_run` girdisi bulunsun.
- Secret yoksa iş akışı `::notice::` ile atlasın (hata vermesin) — diğer news-* iş akışlarıyla aynı kalıp.
- Örnek özet üretirken başka özet hesaplarının metnini kopyalama; yeniden yaz ve "biçim örneği" olarak işaretle.
- Bulut ortamından haber sitelerine erişim yok → gerçek veriyle deneme GitHub Actions'ta yapılır.
