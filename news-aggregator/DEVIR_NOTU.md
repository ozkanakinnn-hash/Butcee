# Devir Notu — Haber Akışı (2026-11-27)

## Proje
- Flutter + Firebase haber uygulaması. Paket `akinai.app1`, Firebase projesi `haber-68a3c`, bölge `europe-west1`.
- Kod: `news-aggregator/` (sunucu: `firebase/functions/`, uygulama: `flutter_app/`).
- Çalışma dalı: `ccr-e3d7c6b8-cccl41` (son commit `7a27cb5`, push edildi).
- Yöntemler: `.claude/skills/flutter-firebase-app/SKILL.md`.

## Kullanıcının kuralları
- Yanıtlar Türkçe.
- Onay vermeden mevcut dosyaları değiştirme; büyük değişiklikten önce ne anladığını yaz, onay al.
- "Gönder" demeden APK gönderme.
- Anahtar/jeton sohbete yazdırılmaz (Firebase Secrets / güvenli form / Cloud Shell).
- Deploy'u kullanıcı Cloud Shell'de yapar: `cd news-aggregator/firebase && firebase deploy --only functions`

## Bu oturumda yapılanlar
1. **Bildirim sadeleştirme** (`src/notify.js` yeniden yazıldı): yalnızca sabah 08:30 / akşam 19:00 bülteni (`bulletin` topic). Breaking ve kategori özeti bildirimleri kapalı. Seviye cap'leri: low=5, balanced=9, all=15 (`PROFILES`).
2. **Hikâye birleştirme modülleri** eklendi: `importance.js`, `quiet-hours.js`, `scoring.js` (diğer daldan: `claude/haber-akisi-2026-11`).
3. **Gemini özeti**: `src/gemini.js` → `briefings/{id}.ai_summary` ve `briefings/latest`. Secret `GEMINI_API_KEY` Cloud Shell'de oluşturuldu; `fetchRssFeeds` içinde `secrets: ['GEMINI_API_KEY']`.
4. **X otomasyonu**: `src/social.js` eklendi (Make Custom Webhook). `fetchRssFeeds` her turda `runSocial(db)` çağırır. Telegram desteği bu sürümde YOK.
5. **Firestore temizliği**:
   - `updateMarket()` artık `fetchRssFeeds` içinde çağrılıyor (önceden hiç çağrılmıyordu → piyasa paneli bayattı).
   - `cleanupNotificationLog()` gece 04:00 `cleanupOldArticles` içinde çalışıyor.
   - `articles.also_by` / `source_count` yazmaları kaldırıldı (uygulama okumuyordu).
   - `flutter_app/lib/backend/schema/sources_record.dart` silindi (kullanılmıyordu).

## Bekleyenler
- **DEPLOY YAPILMADI** — yukarıdaki tüm sunucu değişiklikleri için kullanıcı deploy etmeli.
- **`app_meta/social` belgesi** Firestore Console'dan oluşturulmalı:
  `enabled:true, x_enabled:true, x_webhook:"<Make URL>", dry_run:true` (önce deneme; `social_log`'a bak, sonra `dry_run:false`). Bağlantı testi: `test_ping:true` → sonuç `test_result` alanında.
- **`briefings`** yazılıyor ama Flutter okumuyor — "5 dakikada gündem" ekranı uygulamaya bağlanmalı mı, kullanıcıya sorulmadı.
- Fonksiyon testleri (`firebase/functions/test`) bu değişikliklerden sonra çalıştırılmadı: `cd firebase/functions && npm test`.
- `market.js` hâlâ BIST 100 ve Brent üretiyor; görev #2 bunların kaldırılmasını istemişti — kontrol et.
- Görev #6: yayın imzası, AAB, gerçek AdMob kimlikleri (devam ediyor).
- Görev #8: depo temizliği ve Codemagic derleme hattı.

## Firestore haritası (özet)
| Yol | Yazan | Okuyan |
|---|---|---|
| `articles` | ingest.js, takedown.js, maintenance.js | Flutter (liste, arama) |
| `categories` | (elle) | ingest.js, Flutter |
| `sources` | ingest.js, takedown.js | ingest.js |
| `notifications` | notification-log.js | Flutter (Bildirimler sekmesi) |
| `notify_state/news` | notify.js | notify.js, social.js |
| `notify_state/social` | social.js | social.js |
| `briefings/{id}`, `briefings/latest` | notify.js | — (uygulama bağlı değil) |
| `market/latest` | market.js | Flutter (piyasa paneli) |
| `football_teams`, `matches` | football.js (GitHub Actions script) | Flutter |
| `app_meta/social` | elle / social.js (test sonucu) | social.js |
| `app_meta/search`, `breaking_category`, `topic_rescan` | maintenance.js (bir kerelik) | maintenance.js |
| `social_log`, `takedowns` | social.js, takedown.js | — (sunucu kaydı) |
