# Devir Notu — Haber Akışı (2026-11-27, 2. oturum)

## Proje
- Flutter + Firebase haber uygulaması. Paket `akinai.app1`, Firebase projesi `haber-68a3c`, bölge `europe-west1`.
- Kod: `news-aggregator/` (sunucu: `firebase/functions/`, uygulama: `flutter_app/`).
- Çalışma dalı: `ccr-e3d7c6b8-cccl41` (push edildi; açık PR #16).
- DİKKAT: Kullanıcının telefondaki sürümü `claude/haber-akisi-2026-11` dalından derlenmiş (o dalda ayrı, kategori bölümlü
  bir `briefing_page.dart` var). Zamanlanmış GitHub işleri VARSAYILAN daldan (`claude/taslak-uygulamasi-vn0y2s`) çalışır.
- Yöntemler: `.claude/skills/flutter-firebase-app/SKILL.md` (bölüm 16 bu oturumda eklendi).

## Kullanıcının kuralları
- Yanıtlar Türkçe.
- Onay vermeden mevcut dosyaları değiştirme; büyük değişiklikten önce ne anladığını yaz, onay al.
- Tasarım değişikliğinde ÖNCE görsel taslak sun, beğenirse uygula.
- "Gönder" demeden APK gönderme.
- Anahtar/jeton sohbete yazdırılmaz (Firebase Secrets / GitHub Secrets / güvenli form).
- Deploy'u kullanıcı Cloud Shell'de yapar:
  `cd news-aggregator/firebase && firebase deploy --only functions,firestore:rules`

## Bu oturumda yapılanlar
### Sunucu (commit 35b4f2e) — 66/66 test (emülatör dahil)
- `gemini.js`: kapatılan `gemini-1.5-flash` yerine `app_meta/ai.model` (vars. `gemini-flash-latest`) + yedek modeller;
  `active_model` / `last_error` yazılır. Tek haber özeti (`summarizeStory`).
- `notify.js`: AI özeti bülten bildiriminin GÖVDESİNE eklenir; gönderilen bülten `briefings/*.notified = true`.
- `social.js`: Make'e üç tür (`tur`): `son_dakika`, `gundem` (≥5 farklı yayıncı), `bulten` (AI özet + maddeler).
  Ayarlar `app_meta/social` (aşağıda). Mükerrer engeli; `last_status`, `last_run_at` izleme alanları.
- `market.js`: kalemler `app_meta/market.items`; varsayılan USD, EUR, gram altın, ons (BIST 100 ve Brent kaldırıldı).
- Futbol: anahtar temizleme; anahtar hatasında / 5 ardışık hatada durur, `football_meta/state.last_error` yazar, iş kırmızı biter.

### Uygulama (commit 49f86bf) — 37/37 test, analiz temiz
- `app_config/main` uzaktan ayar (bkz. aşağı), duyuru + güncelleme şeridi (`RemoteBanner`).
- "5 dakikada gündem": `BriefingPage` + ana sayfada `BriefingCard`; bülten bildirimine dokununca ekran açılır.
- Bayat canlı skor gizlenir (`live_stale_minutes`). Piyasa paneli ve reklamlar uzaktan kapatılabilir.
- `firestore.rules`: `briefings`, `app_config` okunabilir. `analysis_options`: Flutter 3.47 deprecated uyarıları CI'yı kırmasın.

## Canlı skor arıza teşhisi
- 22 Kasım ~21:00'den beri api-football her isteğe "Missing application key" diyor → `FOOTBALL_API_KEY` reddediliyor.
- KULLANICI YAPACAK: dashboard.api-football.com'da anahtarı kontrol et → GitHub Secrets `FOOTBALL_API_KEY` boşluksuz
  yeniden kaydet → Actions "Haber Akışı - Canlı Skor" işini `check` ile çalıştır.
- GitHub cron 15 dk yerine ~saatte bir çalışıyor; gerekirse Cloud Functions'a taşıma önerildi (yapılmadı).

## Make
- `social.js` hiç deploy edilmemişti → Make'e hiç kayıt gitmemişti (senaryo bozuk değil).
- `app_meta/social` belgesi oluşturulacak:
  `enabled:true, x_enabled:true, x_webhook:"<Make URL>", dry_run:true, test_ping:true`
  → `test_result`'a bak; Make'te "Redetermine data structure"; sonra `dry_run:false`.
  İsteğe bağlı: `gundem_min_sources:5, daily_cap:12, min_gap_min:30, breaking_gap_min:10, send_breaking/send_gundem/send_bulletin`.

## Uzaktan ayar belgeleri (Firestore Console)
- `app_config/main`: `show_live_scores, show_market, show_briefing, ads_enabled` (bool), `announcement`, `announcement_url`,
  `announcement_level` ("info"|"warning"), `min_build` (sayı), `update_url`, `live_stale_minutes` (sayı).
- `app_meta/ai`: `enabled, model, story_summary`. `app_meta/market`: `items: [...]`.

## Bekleyenler
1. **Gazete tarzı gündem ekranı** — taslak gönderildi (A koyu / B gazete kâğıdı; görsel sağda, başlık+kaynak solda,
   manşet, AI başyazı kutusu, kategori bölümleri). KULLANICI ONAYI BEKLENİYOR. Taslak HTML:
   oturum scratchpad'inde `mockup/gazete.html` (silinmiş olabilir; tasarım SKILL/burada tarif edildi).
   Uygularken `claude/haber-akisi-2026-11` dalındaki kategori bölümlü sürümle birleştirmek gerekecek.
2. **DEPLOY** (functions + firestore rules) — kullanıcı yapacak.
3. Hangi dalın yayın dalı olacağı netleşmeli (3 farklı dal var); PR #16 varsayılan dala birleşmeli ki futbol düzeltmesi
   zamanlanmış işte etkili olsun.
4. Görev #6: yayın imzası, AAB, gerçek AdMob kimlikleri. Görev #8: depo temizliği, Codemagic.
5. Flutter material → material_ui geçişi (yayından sonra).

## Firestore haritası (özet)
| Yol | Yazan | Okuyan |
|---|---|---|
| `articles` | ingest.js, takedown.js, maintenance.js | Flutter (liste, arama) |
| `categories` | (elle) | ingest.js, Flutter |
| `notifications` | notification-log.js | Flutter (Bildirimler sekmesi) |
| `notify_state/news` | notify.js | notify.js, social.js |
| `notify_state/social` | social.js | social.js |
| `briefings/{id}`, `briefings/latest` | notify.js | Flutter (gündem ekranı/kartı), social.js |
| `market/latest` | market.js | Flutter (piyasa paneli) |
| `football_teams`, `matches`, `football_meta/state` | football-watch.js (GitHub Actions) | Flutter |
| `app_config/main` | elle | Flutter |
| `app_meta/social`, `app_meta/ai`, `app_meta/market` | elle + sunucu durum alanları | sunucu |
| `social_log`, `takedowns` | social.js, takedown.js | — (sunucu kaydı) |
