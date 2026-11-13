# FlutterFlow Kurulum Rehberi — Veri Bağlama (Data Binding)

Bu rehber, `articles`, `categories` ve `sources` koleksiyonlarını FlutterFlow'a bağlamayı adım adım anlatır.
FlutterFlow görsel bir araç olduğu için bu adımlar FlutterFlow ekranında **tıklanarak** yapılır; aynı mantığın
çalışan kod karşılığı `flutter_app/` klasöründedir (FlutterFlow kullanmak istemezseniz doğrudan onu derleyebilirsiniz).

> FlutterFlow menü adları sürümden sürüme küçük farklılıklar gösterebilir. Bir seçeneği tam bu adla bulamazsanız
> aynı paneldeki benzer adlı seçeneğe bakın.

---

## 0. Ön hazırlık (bir kez)

1. **Firebase projesi** oluşturun → Firestore Database'i **Production mode** ve `eur3` / `europe-west1` konumuyla açın.
2. Firebase planını **Blaze** yapın (zamanlanmış Cloud Function için gerekli; bu proje ölçeğinde ücretsiz kotada kalır).
   Bütçe uyarısı ekleyin: Google Cloud Console → Faturalandırma → Bütçeler → ör. 5 USD.
3. FlutterFlow → **Settings → Project Setup → Firebase** → proje kimliğini girip **Connect** deyin →
   **Generate Config Files**.
4. FlutterFlow → **Settings → Languages**: Birincil dil **Türkçe (tr)**, ek dil **İngilizce (en)**.
5. Güvenlik kuralları ve dizinler **bu depodan** yüklenir (FlutterFlow'un otomatik kurallarıyla üzerine yazmayın):
   ```bash
   cd news-aggregator/firebase
   cp .firebaserc.example .firebaserc     # içine proje kimliğinizi yazın
   npx firebase-tools login
   npx firebase-tools deploy --only firestore
   ```
   FlutterFlow → Settings → Firestore → **Firestore Rules** bölümünde "Deploy" düğmesine **basmayın**;
   basarsanız bu depodaki `firestore.rules` dosyasını tekrar yükleyin.

---

## 1. Koleksiyon şemalarını tanımlama

FlutterFlow → sol menü **Firestore** → **+ (Create Collection)**. Alan adlarını **birebir** aynı yazın
(Cloud Function bu adlarla yazar).

### `categories`
| Alan | Tür |
|---|---|
| `name` | String |
| `name_en` | String (İngilizce ad — isteğe bağlı) |
| `icon_name` | String |
| `order_index` | Integer |
| `placeholder_image_url` | Image Path (String) — resimsiz haberlerin kapak görseli |

### `sources`
| Alan | Tür |
|---|---|
| `source_name` | String |
| `rss_url` | String |
| `category_id` | Doc Reference → `categories` |
| `logo_url` | Image Path |
| `is_active` | Boolean |
| `last_fetched_at`, `last_error` | DateTime, String (Cloud Function doldurur — isteğe bağlı) |

### `articles`
| Alan | Tür |
|---|---|
| `title` | String |
| `summary` | String |
| `image_url` | Image Path |
| `original_url` | String |
| `source_name` | String |
| `source_logo_url` | Image Path (isteğe bağlı) |
| `category_id` | Doc Reference → `categories` |
| `published_at` | DateTime |
| `content_hash` | String |

> İpucu: Koleksiyonları önce `npm run seed` ile doldurursanız (README → Adım 3) FlutterFlow'daki
> **"Infer from data"/şema önerisi** alanları otomatik doldurabilir.

---

## 2. Ana Ekran (HomePage) — Haber Akışı

### 2.1 Sayfa durumu (Page State)
HomePage seçiliyken sağ panel → **State Management → Local Page State → + Add Field**:
- `selectedCategory` — Tür: **Doc Reference (categories)**, Nullable ✔, başlangıç değeri boş.

### 2.2 Kategori sekmeleri (yatay kaydırma)
1. AppBar'ın altına bir **Row** koyun, `Row` içine:
   - **ChoiceChip benzeri "Tümü" butonu** (Container + Text "Tümü").
     - Actions → On Tap → **Update Page State** → `selectedCategory` → **Reset Value / Clear**.
     - Renk: Conditional → `selectedCategory` **is not set** ise vurgu rengi.
   - **ListView** (Axis: Horizontal, Shrink Wrap ✔) → **Backend Query**:
     - Query Type: **Query Collection** → Collection: `categories`
     - Ordering: `order_index` → **Increasing (Ascending)**
     - Single Time Query: ✔ (kategoriler nadiren değişir, okuma maliyeti düşer)
2. ListView'in alt öğesi bir Container + Text:
   - Text → **Set from Variable** → `categories Document` → `name`
     (İngilizce için: Conditional Value → *Current Language* == `en` ise `name_en`).
   - On Tap → **Update Page State** → `selectedCategory` = `categories Document` → **Reference**.
   - Seçili rengi: Conditional → `selectedCategory` == `categories Document.reference`.

### 2.3 Haber listesi (sonsuz kaydırma + yenileme)
1. Sayfa gövdesine **ListView** (Vertical) ekleyin → **Backend Query**:
   - Query Type: **Query Collection** → Collection: `articles`
   - **Filters** → `category_id` **Equal To** → Page State `selectedCategory`
     → **"Ignore filter if value is empty / null"** ✔ (Seçim yoksa "Tümü" = filtresiz)
   - **Ordering** → `published_at` → **Decreasing (Descending)**
   - **Enable Infinite Scroll** ✔ → **Page Size: 20**
     (Firestore kuralı tek sorguda en fazla 50 habere izin verir; Limit/Page Size'ı 50'nin üstüne çıkarmayın.)
2. ListView özellikleri → **Pull to Refresh / Enable Refresh** ✔ (aşağı çekince sorgu yenilenir).
3. Kategori filtresi için gereken birleşik dizin (`category_id` + `published_at`) `firestore.indexes.json`
   içinde tanımlı; 0. adımda `deploy --only firestore` ile yüklendi.

### 2.4 Haber kartı bağlamaları
ListView'in alt öğesi: **Row** → [Image] + [Column(Text başlık, Text meta)]

| Bileşen | Bağlama |
|---|---|
| Image (112×80, BoxFit.cover, köşe 10) | Path → `articles Document` → `image_url`. **Error/Placeholder image**: yerel `assets/images/placeholder.png` |
| Text (başlık) | `articles Document` → `title`, Max Lines 3, **Bold** |
| Text (meta, küçük gri) | **Combine Text**: `source_name` + `" · "` + `published_at` (Date Format: **Relative**, ör. "5 dakika önce") |
| Row → On Tap | **Navigate To** → `ArticleDetail` sayfası. Parametreler: `url` = `original_url`, `sourceName` = `source_name`, `articleTitle` = `title` |

---

## 3. Haber Okuma Ekranı (ArticleDetail) — WebView

Önce sayfayı oluşturun: **+ Add Page → Blank** → adı `ArticleDetail` → **Page Parameters**:
`url` (String, zorunlu), `sourceName` (String), `articleTitle` (String).

### Seçenek A — Kodsuz (en hızlı)
1. AppBar'ı açın:
   - **Leading**: IconButton `close` (X) → On Tap → **Navigate Back**.
   - **Title**: Column → Text (`sourceName` parametresi) + küçük Row [Icon `lock` (yeşil) + Text "Güvenli bağlantı"].
   - **Actions**: IconButton `share` → On Tap → **Share** aksiyonu → Text: Combine Text (`articleTitle` + `\n` + `url`).
2. Gövdeye **WebView** bileşeni → URL: Page Parameter `url` → Width/Height: **infinity**.

### Seçenek B — Özel bileşen (önerilen: ilerleme çubuğu, geri tuşuyla sayfa içinde geri, hata ekranı, "Tarayıcıda aç")
1. **Custom Code → Widgets → + Add → Widget** → adı: `ArticleWebView`.
2. `flutterflow/custom_widgets/article_web_view.dart` dosyasının **tamamını** yapıştırın.
3. Sağ panel **Widget Settings → Parameters**:
   `url` (String, zorunlu), `sourceName` (String, nullable), `articleTitle` (String, nullable).
4. **Dependencies** bölümüne ekleyin (sürümleri FlutterFlow'un önerdiğiyle değiştirebilirsiniz):
   ```
   webview_flutter: ^4.14.1
   share_plus: ^13.3.1
   url_launcher: ^6.3.3
   ```
5. **Save** → **Compile** (hata yoksa yeşil onay).
6. `ArticleDetail` sayfasında **AppBar'ı kapatın** (bileşen kendi üst barını çizer), gövdeye
   `ArticleWebView` ekleyin → Width/Height **infinity** → parametreleri sayfa parametrelerine bağlayın.

---

## 4. Ayarlar & Yasal (SettingsPage)

| Öğe | Aksiyon |
|---|---|
| Dil seçimi (Türkçe / English) | **Set App Language** aksiyonu |
| Gizlilik Politikası (KVKK/GDPR) | **Launch URL** → gizlilik sayfanızın adresi |
| Kullanım Koşulları | **Launch URL** |
| Telif Hakkı Bildirimi (Uyar-Kaldır) | **Launch URL** → `mailto:telif@uygulamaadiniz.com?subject=Telif%20hakk%C4%B1%20-%20i%C3%A7erik%20kald%C4%B1rma%20talebi` |
| Hakkında metni | "Haberler, kaynakların herkese açık RSS yayınlarından alınır ve orijinal sitelerinde açılır. Tüm içerik hakları ilgili yayıncılara aittir." |

---

## 5. Kontrol listesi

- [ ] `npm run seed` ve `npm run fetch:once` sonrası `articles` koleksiyonunda haber var.
- [ ] FlutterFlow **Test Mode / Run** → "Tümü" sekmesinde haberler en yeniden eskiye sıralı.
- [ ] Kategoriye tıklayınca liste filtreleniyor (ilk denemede hata alırsanız dizin henüz oluşuyordur; birkaç dakika bekleyin).
- [ ] Listenin sonuna inince yeni 20 haber geliyor; aşağı çekince yenileniyor.
- [ ] Habere tıklayınca WebView açılıyor; X kapatıyor, Paylaş sistem menüsünü açıyor.
- [ ] Dil İngilizceye çevrilince sekme/menü metinleri İngilizce.
