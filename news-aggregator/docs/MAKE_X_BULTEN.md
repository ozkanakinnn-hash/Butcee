# X Bülteni — Ücretsiz Kurulum (Make + Ücretsiz Yapay Zekâ)

Haber Akışı'nın topladığı haberler **hafta içi** günde 4 kez yapay zekâ ile **kendi cümlelerimizle** özetlenir ve
X'te zincir (thread) olarak paylaşılır. Daha önce paylaşılan bir haber, **yeni bir gelişme yoksa tekrar yazılmaz**.
**Cumartesi ve pazar gönderi yapılmaz.**

| Akış | Saat (TSİ, Pzt–Cum) | Kapsadığı aralık |
|---|---|---|
| 🌅 Sabah Akışı | 07:30 | önceki gece 23:30 → 07:30 |
| ☀️ Öğle Akışı | 12:30 | 07:30 → 12:30 |
| 🌆 Akşam Akışı | 18:30 | 12:30 → 18:30 |
| 🌙 Gece Akışı / **Günün Bülteni** | 23:30 | 18:30 → 23:30; gün **yoğunsa** (≥ 40 ayrı olay) "Günün Bülteni" olur ve sona *Günün öne çıkanları* eklenir |

Pazartesi sabah akışı yalnızca pazar 23:30'dan sonrasını kapsar; hafta sonu haberleri bültene girmez.
Tekrar hafızası 4 gün tutulur, böylece pazartesi cuma günü paylaşılanları bilir.

## Neden bu yapı ücretsiz?

| Parça | Ne yapar | Ücret |
|---|---|---|
| GitHub Actions | Hafta içi zamanlayıcı, haberleri toplar, aynı olayı gruplar | Ücretsiz (her çalışma ~1 dk; ayda ~88 dk) |
| Firestore (Spark) | Haberler + paylaşılan konular hafızası (`bulletin_topics`) | Ücretsiz kota içinde |
| **Google Gemini** (AI Studio ücretsiz katmanı) | Telifsiz özet zincirini yazar, tekrar kontrolü yapar | Ücretsiz (dakika/gün sınırlı; günde 4 istek çok az) |
| **GitHub Models** (yedek) | Gemini anahtarı yoksa ya da hata verirse aynı işi yapar | Ücretsiz, anahtar gerekmez (`GITHUB_TOKEN`) |
| Make (Free) | Yalnızca X'e gönderir | Çalışma başına ~7 işlem → ayda ~620 / 1.000 |
| X | Zincir gönderi (Premium gerekmez) | Aşağıdaki X notuna bak |

Önceki sürümde yapay zekâ ve hafıza Make içindeydi. Bu, ücretli Claude API'si ve ayda ~1.500 Make işlemi
demekti. Şimdi bu iki iş ücretsiz tarafta (GitHub + Firestore) yapılıyor, Make'te yalnızca paylaşım kaldı.

```
GitHub Actions (Pzt–Cum, günde 4)                         Make (Free)
  1 Firestore'dan aralıktaki haberler                      1 Webhook
  2 aynı olay → tek kayıt, önem sırası                     2 X: Create a Post   (part1)
  3 son 4 günde paylaşılan konular (bulletin_topics)       3 Router ─ dal 1: part2 → part3 → part4 (yanıt zinciri,
  4 Gemini (ücretsiz) → yedek GitHub Models                │          boş parçalar filtreyle atlanır)
  5 zincir ≤ 4 parça, her biri ≤ 280 karakter              └ dal 2: Webhook response "ok"
  6 ── POST part1..part4 ──────────────────────────────►
  7 Make "ok" derse konuları hafızaya yaz
```

---

## 1. Yapılacaklar listesi

### A) Ücretsiz yapay zekâ anahtarı (Gemini)
1. [aistudio.google.com](https://aistudio.google.com) → Google hesabıyla gir → **Get API key** → **Create API key**.
   Kart bilgisi istemez. Projede faturalandırma **açma**, yoksa ücretsiz katmandan çıkarsın.
2. GitHub → depo → Settings → Secrets and variables → Actions → **New repository secret**: `GEMINI_API_KEY`.
3. Model varsayılanı `gemini-2.5-flash`. Google modeli kaldırırsa `GEMINI_MODEL` adında bir **variable/secret**
   ile güncel "Flash" modelini ver (AI Studio'daki model listesine bak).
4. Not: Ücretsiz katmanda Google gönderilen içeriği ürün geliştirmede kullanabilir. Buraya yalnızca herkese açık
   haber başlıkları ve kısa özetler gidiyor, kişisel veri gitmiyor.

> Gemini anahtarı hiç eklenmezse sistem **GitHub Models**'i kullanır (`openai/gpt-4.1-mini`, ücretsiz,
> günlük istek sınırlı). Bu yolun istek başına giriş sınırı küçük olduğu için yapay zekâya en önemli 30 olay gider.

### B) X (Twitter)
1. [developer.x.com](https://developer.x.com) → hesabı aç → **Project + App** oluştur.
2. App → *User authentication settings*: **Read and write**, tür **Web App**, OAuth 2.0,
   Callback URL = Make'in X bağlantısı sırasında gösterdiği adres.
3. **Ücret uyarısı:** X API planları sık değişiyor. Developer Portal'da şu an ücretsiz gönderi hakkı olup
   olmadığını ve aylık limiti kontrol et. Bu kurulum ayda en fazla ~88 çalışma × 4 parça ≈ **350 gönderi** yapar.
   Ücretsiz hak bunun altındaysa `src/ai.js` → `MAX_PARTS` değerini 2'ye düşür (~176 gönderi).
   Ücretsiz gönderi hakkı yoksa bu adım ücretlidir. O durumda aynı metni ücretsiz bir kanala
   (ör. Telegram kanalı, Make Free ile) gönderebiliriz.
4. Premium **gerekmez**: her parça 280 karakter altında zincir olarak atılır.
5. Profil biyografisine "Otomatik, yapay zekâ destekli haber özeti" yaz (X otomasyon kuralları).

### C) Make (Free plan)
1. **Webhooks → Custom webhook** → adı `haber-bulteni` → adresi kopyala.
2. **X → Create a Post** — Text: `part1`.
3. **Flow Control → Router** (2 dal; Make dalları sırayla çalıştırır):
   - **1. dal (zincirin devamı):**
     - *X → Create a Post* — Text: `part2`, *Reply to post ID*: 2. adımdaki gönderinin `ID`'si.
       Dal bağlantısındaki filtre: `part2` *Exists*.
     - *X → Create a Post* — Text: `part3`, yanıt: bir önceki gönderinin `ID`'si. Filtre: `part3` *Exists*.
     - *X → Create a Post* — Text: `part4`, yanıt: bir önceki gönderinin `ID`'si. Filtre: `part4` *Exists*.
   - **2. dal:** *Webhooks → Webhook response* — Status `200`, Body `ok`.
4. Her X modülüne sağ tık → **Add error handler → Break** (2 deneme). Hata olursa senaryo durur, "ok" dönmez ve
   GitHub konuları hafızaya yazmaz. Böylece bir sonraki akışta yeniden denenir.
5. Senaryo ayarı: **Scheduling → Immediately** (webhook gelince). Senaryoyu **ON** yap.
6. Webhook adresini GitHub secret olarak ekle: `MAKE_WEBHOOK_URL`.

İlk kurulumda webhook'un alanlarını tanıtmak için Actions'ta `dry_run` kapalı bir kez çalıştır ya da
webhook modülünde **Redetermine data structure** de ve 2. bölümdeki örnek JSON'u gönder.

Make işlem hesabı: webhook 1 + router 1 + en fazla 4 gönderi + yanıt 1 ≈ **7 işlem/çalışma**. Hafta içi 22 gün × 4 akış
yaklaşık **620 işlem/ay** eder; ücretsiz 1.000'in altında kalır.

### D) GitHub
- Secrets: `FIREBASE_SERVICE_ACCOUNT` (zaten var), `MAKE_WEBHOOK_URL`, `GEMINI_API_KEY`, isteğe bağlı `APP_LINK`
  (Play Store bağlantısı; son gönderiye eklenir).
- İş akışı: `.github/workflows/news-x-bulletin.yml` (cron `33 4,9,15,20 * * 1-5` = hafta içi). Zamanlanmış
  çalışma yalnızca **varsayılan dalda** tetiklenir, yani bu dal birleştirilince başlar.
- İlk deneme: Actions → *Haber Akışı - X Bülteni (Make)* → Run workflow → `dry_run` işaretli (varsayılan).
  Kayıtta yazılan zinciri okursun; Make'e gitmez, hafızaya yazılmaz. Hafta sonu denemek için `force` işaretle.
- 2–3 gün `dry_run` ile metinleri kontrol et, sonra Make senaryosunu aç.

---

## 2. Make'e gelen paket

```json
{
  "slot": "gece",
  "slot_label": "Günün Bülteni",
  "date": "27 Kasım 2026",
  "part_count": 3,
  "part1": "🌙 Günün Bülteni | 27 Kasım 2026\n🔸 ... (1/3)",
  "part2": "🔸 ... (2/3)",
  "part3": "🔸 ... (3/3)",
  "part4": ""
}
```

## 3. Tekrar etmeme nasıl çalışıyor?
- Yapay zekâ her maddeye kalıcı bir `konu_anahtari` verir (ör. `gurcistan-batum-turk-taraftar-olaylari`).
- Gönderi başarılı olunca (Make `ok` dönünce) Firestore `bulletin_topics/<anahtar>` belgesine
  `son_durum`, `son_paylasim`, `paylasim_sayisi` yazılır.
- Sonraki akışta son 4 günün konuları yapay zekâya verilir. Kural şu: konuda yeni gelişme yoksa madde **yazılmaz**,
  varsa "🔄 GELİŞME:" ile yalnızca yeni bilgi yazılır.
- 14 günden eski konu kayıtları otomatik silinir. Bu koleksiyon uygulamaya kapalıdır (Firestore kuralları
  varsayılan olarak reddeder; yalnızca bot yazar).

## 4. Yapay zekâ talimatı
Talimatın tamamı `firebase/functions/src/bulletin-prompt.js` dosyasında. Özeti:
- **Telif:** başlık veya özet kopyalanmaz, art arda 5 kelimeden uzun alıntı yapılmaz, doğrudan alıntı yoktur,
  listede olmayan bilgi eklenmez. Kod ayrıca başlıklarla 6 kelimelik ortak dizileri arar ve bulursa
  Actions kaydına **uyarı** yazar.
- **Hukuk:** masumiyet karinesi korunur, çocuk ve mağdur adı yazılmaz, hakaret aktarılmaz.
- **Biçim:** en fazla 4 parça, her biri ≤ 280 karakter (numara dahil), 6–8 madde. Model sınırı aşarsa
  kod satırları yeniden paketler ve fazlasını atar.

---

## 5. Örnek çıktı (27 Kasım 2026, Cuma — Gece Akışı, Günün Bülteni modu)

> Maddeler, paylaşılan ekran görüntüsündeki 27 Kasım başlıklarından **yeniden yazılarak** biçim örneği
> olarak hazırlandı. Yayında yalnızca Firestore'daki RSS haberleri kullanılır. "Önceki konular" varsayımsaldır.

**Daha önce paylaşılan konular (öğle ve akşam akışlarından):**
```
gurcistan-batum-turk-taraftar-olaylari | Batum'daki maç sonrası olaylarda 2 Türk taraftar hastaneye kaldırıldı
butce-2027-plan-butce-komisyonu | 2027 bütçesinin komisyon görüşmeleri sürüyor
ogretmen-atamasi-15-bin | 15 bin öğretmen ataması için takvim açıklandı
```

**Zincir (X'e atılacak 4 gönderi):**
```
1/4
🌙 Günün Bülteni | 27 Kasım 2026
🔸 Batum'daki olayların ardından bazı tur firmaları kente düzenlenen gezileri iptal etmeye başladı.
🔸 Dışişleri Bakanlığı, Gürcistan'ın Ankara Büyükelçisi'ni bakanlığa davet etti. (1/4)

2/4
🔄 GELİŞME: Gürcistan'da tedavi gören iki Türk taraftar hastaneden çıktı.
🔸 Karadağ vatandaşları için Türkiye'ye vizesiz giriş uygulaması askıya alındı. (2/4)

3/4
🔸 Halk TV'ye yönelik soruşturmada Ece Üner ile Özgür Deniz Bayramoğlu gözaltına alındı.
🔄 GELİŞME: 2027 bütçesi TBMM Plan ve Bütçe Komisyonu'ndan geçti; sırada Genel Kurul var. (3/4)

4/4
🔄 GELİŞME: 15 bin öğretmen atamasında branşlara göre kontenjanlar yayımlandı.
🔸 Hatay'da öğrenciler biriktirdikleri harçlıkla mahalle bakkalının veresiye defterini kapattı. 💛
📲 Tüm haberler: <uygulama bağlantısı>
#Gündem (4/4)
```

**Hafızaya yazılanlar:**
```json
[
  {"konu_anahtari": "gurcistan-batum-turk-taraftar-olaylari", "durum": "gelisme", "son_durum": "Hastaneye kaldırılan iki taraftar taburcu edildi; tur firmaları Batum gezilerini iptal ediyor"},
  {"konu_anahtari": "gurcistan-buyukelci-disisleri-cagri", "durum": "yeni", "son_durum": "Gürcistan'ın Ankara Büyükelçisi Dışişleri Bakanlığı'na çağrıldı"},
  {"konu_anahtari": "butce-2027-plan-butce-komisyonu", "durum": "gelisme", "son_durum": "2027 bütçesi komisyonda kabul edildi"}
]
```
Cumartesi ve pazar hiçbir şey gönderilmez. Pazartesi sabahı aynı taraftar haberi yeni bilgi olmadan gelirse
`atlananlar` listesine düşer ve **yazılmaz**.

---

## 6. Telif ve güvenlik kontrol listesi
- Olgular telifle korunmaz, **anlatım** korunur. Bu yüzden her madde yeniden yazılır (talimat + kopya uyarısı).
- Başka özet hesaplarının gönderilerini kaynak olarak **kullanma**. Kaynak yalnızca RSS (bkz. `TELIF_PLANI.md`).
- Yayıncı fotoğrafı veya logosu eklenmez.
- Haftada bir 2–3 zinciri kaynak haberlerle karşılaştır. Yanlış varsa aynı zincire "Düzeltme:" yanıtı at.

## 7. Sorun giderme
| Belirti | Çözüm |
|---|---|
| "Hafta sonu: bülten gönderilmez." | Beklenen davranış; denemek için `force` |
| `Gemini HTTP 429` | Ücretsiz dakika/gün sınırı doldu; sistem otomatik GitHub Models'e düşer |
| `Gemini HTTP 404` model | Model kaldırılmış → `GEMINI_MODEL` ile güncel Flash modelini ver |
| `GitHub Models HTTP 403` | İş akışında `permissions: models: read` olmalı (dosyada var) |
| "Make gönderiyi doğrulamadı" | Senaryo kapalı, X hatası veya Webhook response modülü eksik → Make History |
| Make `410 Gone` | Senaryo kapalı / webhook silinmiş → aç, adresi güncelle |
| Firestore "requires an index" | Hata mesajındaki bağlantıya tıkla (tek alan dizini; normalde gerekmez) |
| Aynı haber tekrar geldi | Hafızaya yazma çalışmamış ("ok" dönmedi) → Make History'ye bak |
| X `403` | App izni *Read and write* değil veya Make'te X bağlantısını yenile |
| X `429` / limit | Aylık limit doldu → `MAX_PARTS` değerini düşür |
