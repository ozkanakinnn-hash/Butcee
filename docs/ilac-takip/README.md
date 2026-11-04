# Mobil İlaç Takip Uygulaması — Tasarım Dokümanları

Flutter + Firebase tabanlı ilaç takip uygulamasının 2. aşama teknik ve fonksiyonel spesifikasyonları.

| Doküman | İçerik |
|---|---|
| [01-ileri-ozellikler.md](01-ileri-ozellikler.md) | Karekod/barkod okutma (GS1 DataMatrix, yerel katalog), Doktor & Bakım Veren paylaşımı (roller, güvenlik kuralları, uyum raporu PDF/QR, FCM), Eczane & konum servisleri (Places/OSM, nöbetçi eczane, reçete yenileme) |
| [02-rakip-analizi-ve-mimari.md](02-rakip-analizi-ve-mimari.md) | Rakip şikâyetleri → kök neden → çözüm, esnek dozaj kuralları, güvenilir alarm servisi, erteleme durum makinesi, Bekleyenler paneli, pil/OEM rehberi, Basit Mod (Senior Mode) UI/UX akışı |

Her iki doküman da Türkçe + İngilizce dil desteğini (ARB/`flutter_localizations`) ve KVKK özel nitelikli veri kurallarını temel kabul eder.
