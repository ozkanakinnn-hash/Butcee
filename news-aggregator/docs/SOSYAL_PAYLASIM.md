# Sosyal paylaşım — Telegram (otomatik) + X (hazır metin)

`firebase/functions/src/social.js` — her RSS turunda (20 dk) çalışır. **Önemli** haberleri (3+ yayıncı, tıklama
tuzağı/magazin yok, son 3 saatte) seçer; yayıncının KENDİ haber bağlantısıyla paylaşır (metin/görsel kopyalanmaz).

| Hedef | Nasıl | Maliyet |
|---|---|---|
| Telegram kanalı | Bot API ile otomatik | Ücretsiz |
| X (Twitter) | Özel sohbete **kopyala-yapıştır metin** (≤280, bağlantı 23 sayılır) | Ücretsiz (elle) |
| X API ile otomatik | Yapılmadı — X API'si ücretli (kullandıkça öde, Şubat 2026) | Ücretli |
| **X otomatik (Make + Buffer)** | Sunucu → Make webhook → Google Tablolar kaydı → Buffer → X | Ücretsiz planlar |

## X otomatik: Make + Google Tablolar + Buffer
Telegram botu aradan çıkar (bot kendi mesajlarını göremediği için Make'in Telegram tetikleyicisi bu iş için uygun değil).
Seçimi yine sunucu yapar; aynı haber hem Telegram'a hem X'e gider (biri kapalıysa yalnızca diğerine).

1. **Buffer:** buffer.com → Connect a channel → **X** → hesabı bağla.
2. **Google Tablolar:** `Haber Akışı X` adlı tablo, ilk satır: `zaman | baslik | kaynak | kaynak_sayisi | link | metin | deneme`
3. **Make:** Create a new scenario →
   - **Webhooks → Custom webhook** → Add → ad `haber-x` → adresi kopyala.
   - **Google Sheets → Add a Row** → tabloyu seç → sütunları webhook alanlarıyla eşle.
   - **Buffer → Create a Status Update** (profil: X) → Text = `metin`. Aradaki bağlantıya **filtre**: `deneme` = `false`
     (bağlantı testi örneği X'e gitmesin).
   - Zamanlama **Immediately**, senaryo **ON**.
4. **Firestore → app_meta/social** belgesine ekle: `x_webhook` (string, Make adresi) · `x_enabled` (boolean) = true.
5. **Örnek veri:** Make'te webhook modülünde **Redetermine data structure** → `app_meta/social` belgesinde `test_ping` = true.
   En geç 20 dk içinde örnek kayıt (deneme: true) gelir, Make alanları öğrenir; tabloda bir test satırı görünür.
6. Gerçek paylaşım `dry_run: false` ve `enabled: true` iken başlar. Günlük sınır/aralık/gece sessizliği X için de geçerli.

Webhook alanları: `zaman, baslik, kaynak, kaynak_sayisi, link, metin (≤280, bağlantı dahil), haber_id, deneme`.
Kapatmak: `x_enabled: false`. Make ücretsiz planı ayda 1.000 işlem: günde 8 paylaşım × 3 modül ≈ 720 işlem.

## Kurulum
1. **Bot:** Telegram → @BotFather → `/newbot` → ad "Haber Akışı", kullanıcı adı `...bot` ile bitmeli → **jeton** (GİZLİ).
2. **Kanal:** Yeni kanal (herkese açık) → bağlantı adı ör. `haberakisi` → Yöneticiler → botu ekle, yetki: **Mesaj gönder**.
3. **X taslakları için sohbet kimliği:** Telegram'da @userinfobot'a yaz → `Id` değerini al; botunuza da bir kez `/start` yazın.
4. **Jetonu sunucuya tanıt (Cloud Shell; jeton sohbete/yere yazılmaz):**
   `npx -y firebase-tools@15 functions:secrets:set TELEGRAM_BOT_TOKEN --project haber-68a3c`
   (kullanmayacaksan değer olarak `-` gir; paylaşım sessiz kalır)
5. **Dağıt:** `deploy --only functions --project haber-68a3c`
6. **Ayarlar:** Firestore → `app_meta` → belge kimliği `social`:
   `enabled: true` · `dry_run: true` · `channel: "@haberakisi"` · `owner_chat_id: "123456789"` · `daily_cap: 8` · `min_gap_min: 45`
   İsteğe bağlı (telif için): `link_preview: false` (varsayılan; yayıncının görseli gösterilmez) ·
   `allowed_sources: ["Kaynak Adı", ...]` (doluysa yalnızca bu yayıncılar paylaşılır; boşsa hepsi). Kaynak adı,
   `social_log` kayıtlarındaki `source` alanında yazar.
7. **Bağlantı testi (haber beklemeden):** `app_meta/social` belgesine `test_ping` (boolean) = **true** ekleyin. En geç 20 dk
   içinde kanala ve özel sohbetinize "✅ bağlantı testi başarılı" mesajı gelir; belgede `test_result` alanı
   "başarılı" ya da hata metnini gösterir ve `test_ping` kendiliğinden false olur.
8. **Deneme:** `dry_run: true` iken hiçbir şey gönderilmez; `social_log` koleksiyonunda ne paylaşılacağı görünür.
   1–2 gün bakıp beğenirsen `dry_run: false` yap.

## Davranış
- Günlük üst sınır (`daily_cap`), iki paylaşım arası en az `min_gap_min`, gece 00:00–08:00 paylaşım yok
  (yalnızca son dakika kaynağından deprem/afet gibi kritik haber istisna).
- Aynı haber iki kez paylaşılmaz; haber telif talebiyle silindiyse paylaşılmaz.
- Kanal gönderimi hata verirse sayaç ilerlemez, bir sonraki turda yeniden denenir.
- Kapatmak: `enabled: false` (dağıtım gerekmez).

## Hukuki not
Başlık + kaynak adı + yayıncının bağlantısı paylaşılır. Kanal açıklamasına şunu ekleyin: "Haberler ilgili yayın
kuruluşlarına aittir; kaldırma talebi: haberakisinfo@gmail.com". Bir yayıncı çıkarılmak isterse kaynağı kapatmak
(takedown) paylaşımı da durdurur.
