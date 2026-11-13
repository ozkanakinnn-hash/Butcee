# Ücretsiz Kurulum (Firebase Spark planı — kredi kartı gerekmez)

Firebase'in ücretsiz **Spark** planında Firestore veritabanı kullanılabilir, ancak **Cloud Functions yayınlanamaz**
(zamanlanmış fonksiyon Blaze planı ister). Bu yüzden test döneminde RSS botu, aynı kodla **GitHub Actions** üzerinde
30 dakikada bir çalışır ve haberleri Firestore'a yazar. Depo herkese açık olduğu için GitHub Actions ücretsizdir.

İleride Blaze'e geçerseniz README'deki "1b. Otomatik kurulum" ile Cloud Function'a taşınırsınız; kod aynıdır.

---

## Adım 1 — Firebase projesi (ücretsiz)
1. [console.firebase.google.com](https://console.firebase.google.com) → **Proje oluştur**.
2. Ad: ör. `haber-akisi` → Google Analytics: **kapalı** bırakabilirsiniz → **Proje oluştur**.
3. Plan sorulursa / sol altta **Spark** yazıyorsa dokunmayın. **Yükselt (Upgrade) düğmesine basmayın.**

## Adım 2 — Firestore veritabanı
1. Sol menü **Derleme (Build) → Firestore Database → Veritabanı oluştur**.
2. Sürüm sorulursa **Standard** → Konum: **europe-west1 (Belçika)** (sonradan değiştirilemez).
3. **Üretim modunda başlat (Production mode)** → **Oluştur**.

## Adım 3 — Güvenlik kuralları
Firestore → **Kurallar (Rules)** sekmesi → içindekileri silip aşağıdakini yapıştırın → **Yayınla (Publish)**:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /categories/{id} { allow read: if true;  allow write: if false; }
    match /sources/{id}    { allow read: if true;  allow write: if false; }
    match /articles/{id} {
      allow get: if true;
      allow list: if request.query.limit <= 50;
      allow write: if false;
    }
    match /notifications/{id} {
      allow get: if true;
      allow list: if request.query.limit <= 100;
      allow write: if false;
    }
    match /market/{id}         { allow read: if true;  allow write: if false; }
    match /football_teams/{id} { allow read: if true;  allow write: if false; }
    match /matches/{id}        { allow read: if true;  allow write: if false; }
    match /users/{uid}            { allow read, write: if request.auth != null && request.auth.uid == uid; }
    match /user_preferences/{uid} { allow read, write: if request.auth != null && request.auth.uid == uid; }
  }
}
```
(Uygulama yalnızca okur; haberleri bot, kurallardan etkilenmeyen yönetici anahtarıyla yazar.)

## Adım 4 — Kategori filtresi için dizin
Firestore → **Dizinler (Indexes)** → **Birleşik (Composite) → Dizin oluştur**:

| Alan | Değer |
|---|---|
| Koleksiyon kimliği | `articles` |
| Alan 1 | `category_id` → **Artan (Ascending)** |
| Alan 2 | `published_at` → **Azalan (Descending)** |
| Sorgu kapsamı | **Koleksiyon (Collection)** |

**Oluştur** → durumu birkaç dakika içinde "Etkin (Enabled)" olur.

**Arama için ikinci dizin** (aynı ekranda bir dizin daha):

| Alan | Değer |
|---|---|
| Koleksiyon kimliği | `articles` |
| Alan 1 | `search_keywords` → **Diziler (Arrays)** / *Array contains* |
| Alan 2 | `published_at` → **Azalan (Descending)** |
| Sorgu kapsamı | **Koleksiyon (Collection)** |

## Adım 5 — Yönetici anahtarı (hizmet hesabı)
1. Sol üstte ⚙️ **Proje ayarları → Hizmet hesapları (Service accounts)**.
2. **Firebase Admin SDK** seçili iken **Yeni özel anahtar oluştur → Anahtar oluştur**. Bir `.json` dosyası iner.
3. Bu dosya veritabanınızın anahtarıdır: **kimseyle paylaşmayın, sohbete yapıştırmayın, depoya eklemeyin.**

## Adım 6 — Anahtarı GitHub'a ekleyin
1. GitHub'da depo → **Settings → Secrets and variables → Actions → New repository secret**.
2. Name: `FIREBASE_SERVICE_ACCOUNT`
3. Secret: indirilen JSON dosyasını Not Defteri ile açın, **tüm içeriği** kopyalayıp yapıştırın → **Add secret**.

## Adım 7 — Kodu varsayılan dala alın
GitHub zamanlanmış iş akışlarını yalnızca deponun varsayılan dalından çalıştırır (bu depoda `claude/taslak-uygulamasi-vn0y2s`).
`ccr-e3d7c6b8-cccl41` dalı için bir Pull Request açın (veya Claude'dan açmasını isteyin) ve **Merge** edin.

## Adım 8 — İlk çalıştırma
1. Depo → **Actions → "Haber Akışı - Ücretsiz RSS Çekimi" → Run workflow**.
2. **"Önce örnek kategori ve kaynakları yükle"** kutusunu **işaretleyin** → **Run workflow**.
3. 1–2 dakika sonra iş yeşil olmalı. "RSS çekimi" adımında her kaynak için `- NTV: 25/30 yeni` gibi satırlar görünür.
4. Firebase → Firestore → `articles` koleksiyonunda haberler görünür.

Bundan sonra iş akışı **30 dakikada bir kendiliğinden** çalışır (GitHub yoğunlukta birkaç dakika geciktirebilir).
Sonraki elle çalıştırmalarda kutuyu işaretlemenize gerek yok.

## Adım 9 — Android uygulamasını bağlama
1. Firebase → Proje ayarları → **Genel → Uygulamalarınız → Android simgesi**.
2. Paket adı: `com.haberakisi.haber_akisi` → **Uygulamayı kaydet** → `google-services.json` dosyasını indirin.
3. GitHub → Settings → Secrets → **New repository secret** → Name: `NEWS_GOOGLE_SERVICES_JSON`,
   Secret: dosyanın tüm içeriği. (Bu dosya gizli değildir ama depoda tutmuyoruz.)
4. Actions → **News Aggregator → Run workflow** → bitince `haber-akisi-debug-apk` dosyasını indirip telefona kurun.

---

## Ücretsiz kotalar (Spark)
| Kaynak | Günlük ücretsiz | Bu kurulumda tahmini kullanım |
|---|---|---|
| Firestore okuma | 50.000 | Bot: ~10–17 bin/gün (7 kaynak, 30 dk'da bir) + uygulama açılışları |
| Firestore yazma | 20.000 | Bot: birkaç yüz/gün |
| Depolama | 1 GiB | Binlerce haber birkaç MB eder |

- Kota dolarsa **ücret çıkmaz**; o gün Firestore istekleri reddedilir, ertesi gün sıfırlanır.
- Kaynak eklemek kullanımı artırır. Çok kaynak eklerseniz `.github/workflows/news-fetch-free.yml` içindeki
  `cron: "7,37 * * * *"` satırını saatte bire (`"7 * * * *"`) çekebilirsiniz.
- Kullanımı izlemek: Firestore → **Kullanım (Usage)** sekmesi.

## Bilmeniz gerekenler
- GitHub, depoda **60 gün hiç commit olmazsa** zamanlanmış iş akışlarını durdurur; Actions sayfasından tek tıkla
  yeniden açılır.
- Test bittiğinde Blaze'e geçmeden önce GitHub iş akışını kapatın (Actions → iş akışı → ⋯ → Disable),
  aksi halde bot iki yerde birden çalışır (zararsız ama gereksiz okuma yapar).
- Firestore Console'da `sources` belgesinde `is_active` alanını `false` yaparak bir kaynağı kapatabilir,
  hatalı kaynağın nedenini `last_error` alanında görebilirsiniz.
