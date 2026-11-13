# Botları Firebase'e taşıma (Cloud Shell ile, bilgisayar gerekmez)

Haber çekimi, bildirimler, piyasa verisi ve canlı skor artık **Firebase Cloud Functions**'ta çalışır.
GitHub'a ihtiyaç yoktur. Kurulum bir kez yapılır (~15 dk).

| Görev | Ne zaman |
|---|---|
| `fetchRssFeeds` | 20 dakikada bir: haberler + bildirimler + piyasa + bakım |
| `cleanupOldArticles` | Her gece 04.00: 30 günden eski haberleri siler |
| `footballTick` | Dakikada bir: maç yoksa API'ye gitmeden biter; maç varsa skor + gol bildirimi |

## 0. Ön koşul
- Firebase projesi **Blaze** planında (✔ yapıldı).
- Bütçe uyarısı: Google Cloud Console → **Faturalandırma → Bütçeler ve uyarılar → Bütçe oluştur** → 1 $ → uyarı %50/%90/%100.

## 1. Cloud Shell'i aç
1. Telefonda/tablette tarayıcıyı **masaüstü sitesi** moduna al.
2. https://shell.cloud.google.com adresini aç; Firebase'e girdiğin Google hesabıyla giriş yap.
3. Alttaki siyah terminal açılınca hazırsın.

## 2. Kodu yükle
Terminalin üstündeki **⋮ (Diğer) → Yükle / Upload** → `Haber-Akisi-kod.zip` dosyasını seç.

## 3. Komutları sırayla yapıştır
(Her satırı ayrı yapıştırıp Enter'a bas.)

```bash
unzip -o Haber-Akisi-kod.zip -d haber && cd haber/news-aggregator/firebase
(cd functions && npm ci --omit=dev)
npx -y firebase-tools@15 login --no-localhost
```
Çıkan bağlantıyı aç → Google hesabınla izin ver → verilen kodu terminale yapıştır.

```bash
npx -y firebase-tools@15 use --add
```
Listeden **Haber Akışı projeni** seç, takma ad sorulunca `default` yaz.

```bash
npx -y firebase-tools@15 functions:secrets:set FOOTBALL_API_KEY
```
API-Football anahtarını **buraya** yapıştır (ekranda görünmez, normal). Anahtarın yoksa `-` yaz; canlı skor kapalı kalır, diğer her şey çalışır.

```bash
npx -y firebase-tools@15 deploy --only functions,firestore
```
- "Container images cleanup policy" sorulursa **Y** ve gün olarak `1` → eski sürüm imajları birikip ücret çıkarmaz.
- Bazı Google API'lerini (Cloud Functions, Cloud Build, Cloud Scheduler, Secret Manager…) etkinleştirmeyi isterse **Y**.
- Sonunda `✔ Deploy complete!` görmelisin.

## 4. Kontrol
Firebase Console → **Functions** → üç fonksiyon listede olmalı. **Günlükler (Logs)** sekmesinde 20 dakika içinde
`RSS çekimi tamamlandı` satırı görünür.

## 5. Telif kaldırma (Uyar-Kaldır) — Cloud Shell'den
```bash
cd ~/haber/news-aggregator/firebase/functions
gcloud auth application-default login     # bir kez
PROJE=$(gcloud config get-value project)
GCLOUD_PROJECT=$PROJE node scripts/takedown.js article "https://haberin-adresi"    # tek haber
GCLOUD_PROJECT=$PROJE node scripts/takedown.js source  kaynak-kimligi            # kaynağın tamamı
GCLOUD_PROJECT=$PROJE node scripts/takedown.js images  kaynak-kimligi            # yalnızca görseller
```
`PROJE` boş çıkarsa `gcloud config set project PROJE_KIMLIGIN` ile ayarla (Firebase → Proje ayarları → Proje kimliği).

## 6. Güvenlik (önerilir)
GitHub hesabı erişilemez olduğu için orada saklanan hizmet hesabı anahtarını iptal et:
Firebase Console → ⚙️ **Proje ayarları → Hizmet hesapları → "Hizmet hesabı izinlerini yönet"** →
`firebase-adminsdk-…` hesabı → **Anahtarlar** → eski anahtarı **Sil**. (Cloud Functions bu anahtarı kullanmaz.)

## Güncelleme
Kod değişince yeni zip'i yükleyip yalnızca şunlar:
```bash
cd ~ && rm -rf haber && unzip -o Haber-Akisi-kod.zip -d haber && cd haber/news-aggregator/firebase
(cd functions && npm ci --omit=dev) && npx -y firebase-tools@15 deploy --only functions,firestore
```

## Ücretsiz canlı skor: Highlightly (günde 100 istek)
1. https://highlightly.net → ücretsiz (Basic) plana kaydol → API anahtarını kopyala.
2. Cloud Shell'de (güncel zip yüklenip açıldıktan sonra, `~/haber/news-aggregator/firebase` klasöründe):
   ```bash
   npx -y firebase-tools@15 functions:secrets:set HIGHLIGHTLY_API_KEY
   ```
   Anahtarı yapıştır (görünmez) → Enter.
3. Biçim kontrolü (Firestore'a yazmaz, ~4 istek kullanır):
   ```bash
   cd functions && HIGHLIGHTLY_API_KEY=$(gcloud secrets versions access latest --secret=HIGHLIGHTLY_API_KEY) node scripts/football-watch.js hl-raw; cd ..
   ```
   Çıktının fotoğrafını Claude'a gönder (anahtar çıktıda görünmez).
4. Yükle: `npx -y firebase-tools@15 deploy --only functions`
- Highlightly anahtarı tanımlıysa öncelikli kullanılır; API-Football (FOOTBALL_API_KEY) yalnızca Highlightly `-` ise devreye girer.
- 100 istek güne yayılır: hafta sonu ~3–5 dk'da bir skor kontrolü; gol bildirimi birkaç dakika gecikebilir.
