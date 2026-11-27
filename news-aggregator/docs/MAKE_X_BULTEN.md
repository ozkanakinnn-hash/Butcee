# X Bülteni — Make + Yapay Zekâ ile Günde 4 Telifsiz Haber Özeti

Haber Akışı'nın topladığı haberler günde 4 kez yapay zekâ ile **kendi cümlelerimizle** özetlenir ve X'te paylaşılır.
Daha önce paylaşılan bir haber, **yeni bir gelişme yoksa tekrar yazılmaz**.

| Akış | Saat (TSİ) | Kapsadığı aralık |
|---|---|---|
| 🌅 Sabah Akışı | 07:30 | dün 23:30 → 07:30 |
| ☀️ Öğle Akışı | 12:30 | 07:30 → 12:30 |
| 🌆 Akşam Akışı | 18:30 | 12:30 → 18:30 |
| 🌙 Gece Akışı / **Günün Bülteni** | 23:30 | 18:30 → 23:30; gün **yoğunsa** (≥ 40 ayrı olay) gece akışı "Günün Bülteni" olur ve sonuna *Günün öne çıkanları* bölümü eklenir |

```
GitHub Actions (günde 4 kez)            Make senaryosu
  scripts/send-bulletin.js  ──POST──►  1 Webhook (Custom webhook)
  • Firestore'dan aralıktaki haberler    2 Data store: son 48 saatte paylaşılan konular
  • aynı olay → tek kayıt (gruplama)     3 Text aggregator: geçmiş listesi
  • önem sırası, en fazla 60 olay        4 Anthropic Claude: özet + tekrar kontrolü (JSON)
  • gece: gün yoğun mu?                  5 Parse JSON
                                         6 Router: paylaşılacak bir şey yoksa dur
                                         7 X: Create a Post (uzun gönderi veya zincir)
                                         8 Data store: yeni/gelişen konuları kaydet
```

> Neden zamanlayıcı GitHub'da? Haberler zaten Firestore'da; GitHub ücretsiz, Make'te işlem (operation) harcamaz,
> aynı olayı anlatan 5 kaynağı tek kayda indirerek yapay zekâ maliyetini düşürür. İstersen Make'in kendi
> zamanlayıcısını + RSS modüllerini de kullanabilirsin (aşağıda "Alternatif").

---

## 1. Gerekenler (yapılacaklar listesi)

### Hesaplar ve anahtarlar
- [ ] **Make hesabı** — Ücretsiz plan ayda 1.000 işlem; bu senaryo günde ~4×10–15 işlem ≈ ayda 1.200–1.800 →
      **Core** plan önerilir (veya gruplamayla işlem sayısını düşür).
- [ ] **Yapay zekâ API anahtarı** — Anthropic Console → API Keys. Make'te *Anthropic Claude* uygulaması.
      Model: kalite için `claude-sonnet-5-5`, ucuz/hızlı için `claude-haiku-5-5`.
      Maliyet kabaca: akış başına ~6–10 bin giriş + ~1,5 bin çıkış jetonu → günde 4 akışta birkaç sent düzeyi.
- [ ] **X Developer hesabı** — developer.x.com → Project + App oluştur → *User authentication settings*:
      **Read and write**, OAuth 2.0, Callback URL olarak Make'in verdiği adres. Günde 4 gönderi düşük hacimdir ama
      **güncel API planını ve gönderi limitini Developer Portal'da kontrol et** (X sık değiştiriyor).
- [ ] **Uzun gönderi mi, zincir mi?** Ekran görüntüsündeki gibi tek uzun gönderi için hesapta **X Premium**
      gerekir (280 karakter üstü). Premium yoksa senaryo aynı metni **zincir (thread)** olarak atar.
- [ ] Hesap profiline **"Otomatik / yapay zekâ destekli özet"** etiketi ekle (X'in otomasyon kuralları).

### GitHub (bu depo)
- [ ] Secrets → `MAKE_WEBHOOK_URL` (Make'teki webhook adresi)
- [ ] Secrets → `FIREBASE_SERVICE_ACCOUNT` (zaten var, RSS çekimi kullanıyor)
- [ ] (İsteğe bağlı) Secrets → `APP_LINK` (uygulamanın Play Store bağlantısı; gönderinin sonuna eklenir)
- [ ] İş akışı: `.github/workflows/news-x-bulletin.yml` — zamanlanmış çalışma yalnızca **varsayılan dalda** tetiklenir.
- [ ] İlk deneme: Actions → *Haber Akışı - X Bülteni (Make)* → Run workflow → `dry_run` işaretli → paketi gör.

### Make
- [ ] Data store oluştur: `x_bulten_gecmis` (yapı aşağıda)
- [ ] Senaryoyu kur (modüller aşağıda), **Scheduling: Immediately** (webhook gelince çalışır)
- [ ] Önce X modülünü devre dışı bırakıp 2–3 gün yalnızca çıktıyı izle (Make → History), sonra aç.

---

## 2. Webhook'a gelen paket (örnek)

```json
{
  "slot": "gece",
  "slot_label": "Günün Bülteni",
  "emoji": "🌙",
  "date": "27 Kasım 2026",
  "window_start": "2026-11-27T15:30:00.000Z",
  "window_end": "2026-11-27T20:30:00.000Z",
  "daily_recap": true,
  "day_event_count": 63,
  "event_count": 18,
  "app_link": "https://play.google.com/store/apps/details?id=...",
  "events": [
    {
      "title": "Gürcistan'ın Ankara Büyükelçisi Dışişleri Bakanlığı'na çağrıldı",
      "summary": "RSS'teki kısa açıklama (en fazla 200 karakter)",
      "sources": ["Kaynak A", "Kaynak B"],
      "count": 2,
      "category": "gundem",
      "url": "https://...",
      "published_at": "2026-11-27T16:05:00.000Z",
      "breaking": true
    }
  ]
}
```

Make'te webhook'u oluşturduktan sonra **Redetermine data structure** → Actions'tan `dry_run` kapalı bir kez
çalıştır; Make alanları otomatik tanır.

---

## 3. Data store: `x_bulten_gecmis`

| Alan | Tür | Açıklama |
|---|---|---|
| `key` | (kayıt anahtarı) | `konu_anahtari` ile aynı, ör. `gurcistan-batum-taraftar-olaylari` |
| `konu_anahtari` | Text | Yapay zekânın verdiği kalıcı konu kimliği (küçük harf, tire) |
| `son_durum` | Text | Bu konuda en son paylaştığımız bilgi (tek cümle) |
| `ilk_paylasim` | Date | Konunun ilk paylaşıldığı an |
| `son_paylasim` | Date | En son paylaşıldığı an |
| `paylasim_sayisi` | Number | Kaç kez (gelişmeyle) paylaşıldı |

Eski kayıtlar: ayrı küçük bir senaryo haftada bir `son_paylasim < 7 gün önce` olanları siler
(Data store → Search records → Delete a record).

---

## 4. Make senaryosu — modül modül

1. **Webhooks → Custom webhook** — adı `haber-bulteni`. Adresi kopyala → GitHub secret `MAKE_WEBHOOK_URL`.
2. **Data store → Search records** — `x_bulten_gecmis`, filtre: `son_paylasim` *later than*
   `{{addHours(now; -48)}}`. Limit 200.
3. **Tools → Text aggregator** — kaynak modül: 2. Metin:
   `{{konu_anahtari}} | {{son_durum}} | {{formatDate(son_paylasim; "DD.MM HH:mm"; "Europe/Istanbul")}}`
   Satır ayırıcı: New row. *(Hiç kayıt yoksa boş döner; sorun değil.)*
4. **Anthropic Claude → Create a Prompt** (veya *Make an API Call* → `POST /v1/messages`)
   - Model: `claude-sonnet-5-5` · Max tokens: `4000` · Temperature: `0.3`
   - **System prompt:** aşağıdaki 5. bölümdeki metin (olduğu gibi yapıştır)
   - **User message:**
     ```
     AKIŞ: {{1.slot_label}} {{1.emoji}} — {{1.date}}
     GÜNÜN BÜLTENİ MODU: {{1.daily_recap}}
     UYGULAMA BAĞLANTISI: {{1.app_link}}

     DAHA ÖNCE PAYLAŞILAN KONULAR (son 48 saat):
     {{3.text}}

     YENİ HABERLER (JSON):
     {{toString(1.events)}}
     ```
     *(Make'te dizi metne çevrilmezse 1. modülden sonra **JSON → Create JSON** ile `events` dizisini metne çevir.)*
5. **JSON → Parse JSON** — Claude'un metin çıktısını ayrıştır (veri yapısı: 6. bölümdeki örnek JSON'dan
   *Generate* ile oluştur).
6. **Router / Filtre** — `paylas` = `true` ve `konular` dizisi boş değilse devam; değilse senaryo biter
   (gelişme yoksa hiç gönderi atılmaz).
7. **Gönderi** — iki seçenek (senaryoya bir değişken koy: `PREMIUM=true/false`):
   - **X Premium var:** *X → Create a Post*, Text = `{{5.uzun_metin}}`.
   - **Premium yok (zincir):** *Iterator* (`{{5.zincir}}`) → *X → Create a Post*; ilk gönderiden sonrakiler için
     *Reply to post ID* = önceki gönderinin ID'si. Make'te bunu **Tools → Set variable / Get variable** ile
     tutarsın (ilk turda boş, her turda yeni ID'yi yaz).
   - Make'te X uygulaması yoksa / bağlanamıyorsa: **HTTP → Make an OAuth 2.0 request**,
     `POST https://api.x.com/2/tweets`, gövde `{"text":"...","reply":{"in_reply_to_tweet_id":"..."}}`.
8. **Iterator** (`{{5.konular}}`) → **Data store → Add/replace a record** — key = `konu_anahtari`,
   `son_durum`, `son_paylasim = now`, `ilk_paylasim` yalnızca yeni kayıtta `now`,
   `paylasim_sayisi = (önceki değer) + 1`.
   **Önemli:** Bu adım X gönderisi başarılı olduktan sonra çalışmalı; aksi hâlde paylaşılmayan haber "paylaşıldı"
   sayılır. Hata yönetimi: X modülüne sağ tık → *Add error handler* → **Break** (3 deneme, 5 dk arayla).

---

## 5. Yapay zekâ sistem talimatı (System prompt)

```
Sen bir Türkçe haber bülteni editörüsün. Sana verilen haber listesinden X (Twitter) için kısa, tarafsız,
TELİFSİZ bir özet bülteni yazarsın.

TELİF KURALLARI (kesin):
- Haberlerin başlığını veya özet cümlesini KOPYALAMA. Her maddeyi kendi cümlelerinle, baştan yaz.
- Kaynaktan art arda 5 kelimeden uzun dizi alma. Özel adlar, unvanlar, kurum adları ve sayılar hariç.
- Sadece olguyu aktar (kim, ne, nerede, ne zaman). Yorum, köşe yazısı ifadesi, alıntı cümle, ünlem kullanma.
- Doğrudan alıntı (tırnak içinde söz) kullanma; gerekiyorsa dolaylı anlat: "... açıkladı", "... dedi" yerine
  "... yönünde açıklama yaptı".
- Listede olmayan hiçbir bilgiyi ekleme. Tarih, sayı, isim uydurma. Emin değilsen o maddeyi yazma.

HUKUK VE DİL:
- Suçlama içeren haberlerde masumiyet karinesine uy: "gözaltına alındı", "tutuklandı", "iddia edildi"
  ayrımını kaynaktaki gibi koru; "suçlu", "katil" gibi hüküm bildiren sözcük kullanma.
- Çocukların, mağdurların ve sıradan vatandaşların adını yazma. Kamuya mal olmuş kişilerin adı yazılabilir.
- Hakaret, küfür, nefret söylemi ve şiddet ayrıntısı içeren ifadeleri aktarma; olayı genel ifadeyle anlat.
- Kesinleşmemiş haberleri "iddia edildi" / "bildirildi" diye işaretle.

TEKRAR KONTROLÜ (çok önemli):
- "DAHA ÖNCE PAYLAŞILAN KONULAR" listesindeki her konu için: yeni haberlerde o konuda YENİ bir gelişme
  (yeni karar, sonuç, sayı değişimi, tutuklama/tahliye, açıklama, kabul/ret) VARSA maddeyi
  "🔄 GELİŞME:" ile başlatıp yalnızca yeni bilgiyi yaz. Gelişme YOKSA o konuyu HİÇ yazma.
- Aynı olayı anlatan birden fazla haberi tek maddede birleştir.
- Her konuya kalıcı bir "konu_anahtari" ver: küçük harf, Türkçe karakter yok, tire ile, 3–6 kelime
  (ör. "gurcistan-batum-turk-taraftar-olaylari"). Önceki listede aynı konu varsa AYNI anahtarı kullan.

SEÇİM VE SIRA:
- En önemli 8–12 maddeyi seç (son dakika ve çok kaynakta geçenler önce). Magazini en fazla 1–2 madde tut.
- Sıra: Gündem/Siyaset → Dünya → Ekonomi → Toplum/Yaşam → Spor → Bilim-Teknoloji → Kültür/Magazin.
- İlgili maddeleri arka arkaya, boş satır koymadan ver; konu değişince boş satır bırak.
- Güzel/insani bir haber varsa sona yakın bir tane koy.

BİÇİM:
- Başlık satırı: "{emoji} {AKIŞ} | {tarih}" ve altına "Bu akışta öne çıkanlar, sizin için derledik."
- Her madde "🔸 " ile başlar, en fazla 2 kısa cümle, ~180 karakter.
- GÜNÜN BÜLTENİ MODU true ise, maddelerden sonra "📌 Günün öne çıkanları:" başlığı altında, gün içinde
  paylaşılmış konulardan en önemli 3–5 tanesini TEK SATIRLIK hatırlatma olarak ekle (yeni bilgi ekleme,
  bu bölüm tekrar sayılmaz). false ise bu bölümü yazma.
- Son satır: uygulama bağlantısı verilmişse "📲 Tüm haberler: {bağlantı}"; ardından "#Gündem".
- Bağlantı, kaynak logosu veya fotoğraf koyma. En fazla 2 hashtag.

ZİNCİR:
- "zincir" alanında aynı metni X'in 280 karakter sınırına göre böl: her parça ≤ 270 karakter,
  maddeleri ortadan bölme, ilk parça başlık + ilk madde(ler), parçaların sonuna " (1/n)" ekle.

ÇIKTI: Yalnızca aşağıdaki JSON'u döndür, başka hiçbir şey yazma:
{
  "paylas": true,
  "uzun_metin": "...",
  "zincir": ["...", "..."],
  "konular": [
    {"konu_anahtari": "...", "durum": "yeni" | "gelisme", "son_durum": "tek cümlelik güncel durum"}
  ],
  "atlananlar": ["gelişme olmadığı için yazılmayan konu_anahtari", "..."]
}
Paylaşılacak yeni haber veya gelişme yoksa: {"paylas": false, "uzun_metin": "", "zincir": [], "konular": [], "atlananlar": [...]}
```

> Make'in Claude modülünde "Response format" seçeneği varsa JSON seç; yoksa talimat yeterli.
> Parse JSON hata verirse Claude çıktısını `replace(text; "/^```json|```$/g"; "")` ile temizle.

---

## 6. Örnek çıktı (27 Kasım 2026 — Gece Akışı, Günün Bülteni modu)

> Aşağıdaki maddeler, kullanıcının paylaştığı ekran görüntüsündeki 27 Kasım başlıklarından **yeniden yazılarak**
> biçim örneği olarak hazırlanmıştır; başlıklar kelimesi kelimesine alınmamıştır. Yayında yapay zekâ yalnızca
> Firestore'daki RSS haberlerini kullanır. Örnekteki "daha önce paylaşılan konular" varsayımsaldır.

**Girdi — daha önce paylaşılan konular (öğle ve akşam akışlarından):**
```
gurcistan-batum-turk-taraftar-olaylari | Batum'daki maç sonrası olaylarda 2 Türk taraftar hastaneye kaldırıldı | 27.11 13:10
butce-2027-plan-butce-komisyonu | 2027 bütçesinin komisyon görüşmeleri sürüyor | 27.11 12:20
ogretmen-atamasi-15-bin | 15 bin öğretmen ataması için takvim açıklandı | 26.11 18:40
```

**Uzun gönderi (X Premium):**
```
🌙 Günün Bülteni | 27 Kasım 2026
Bu akışta öne çıkanlar, sizin için derledik.

🔸 Batum'daki olayların ardından Türkiye'den bazı tur firmaları kente düzenledikleri gezileri iptal etmeye başladı.
🔸 Dışişleri Bakanlığı, Gürcistan'ın Ankara'daki büyükelçisini bakanlığa davet etti.
🔄 GELİŞME: Gürcistan'da tedavi altına alınan iki Türk taraftar hastaneden çıktı.

🔸 Karadağ vatandaşları için Türkiye'ye vizesiz giriş uygulaması askıya alındı.

🔸 Halk TV'ye yönelik soruşturma kapsamında Ece Üner ile Özgür Deniz Bayramoğlu gözaltına alındı.

🔄 GELİŞME: 2027 yılı bütçesi, TBMM Plan ve Bütçe Komisyonu'ndan geçti; teklif Genel Kurul'a gidecek.

🔸 Ünlülere yönelik uyuşturucu soruşturmasında Deniz Çakır, Özcan Deniz ve Burcu Özberk'in test sonuçları negatif geldi.

🔸 Hatay'da canlı yayında devlete, şehitlere ve polise yönelik hakaret içeren sözler sarf eden bir kişi gözaltına alındı.

🔸 15 bin öğretmen atamasında branşlara göre kontenjan dağılımı yayımlandı.

🔸 Hatay'da öğrenciler bir ay boyunca biriktirdikleri harçlıklarla mahalle bakkalındaki veresiye borçlarını kapattı. 💛

📌 Günün öne çıkanları:
• Gürcistan ile Batum'daki taraftar olayları nedeniyle diplomatik gerginlik
• 2027 bütçesi komisyon aşamasını tamamladı

📲 Tüm haberler: <uygulama bağlantısı>
#Gündem
```

**Tekrar kontrolünün sonucu:**
```json
{
  "konular": [
    {"konu_anahtari": "gurcistan-batum-turk-taraftar-olaylari", "durum": "gelisme", "son_durum": "Hastaneye kaldırılan iki Türk taraftar taburcu edildi; tur firmaları Batum gezilerini iptal ediyor"},
    {"konu_anahtari": "gurcistan-buyukelci-disisleri-cagri", "durum": "yeni", "son_durum": "Gürcistan'ın Ankara Büyükelçisi Dışişleri Bakanlığı'na çağrıldı"},
    {"konu_anahtari": "butce-2027-plan-butce-komisyonu", "durum": "gelisme", "son_durum": "2027 bütçesi komisyonda kabul edildi"},
    {"konu_anahtari": "ogretmen-atamasi-15-bin", "durum": "gelisme", "son_durum": "Branşlara göre kontenjan dağılımı açıklandı"}
  ],
  "atlananlar": []
}
```
Ertesi sabah aynı taraftar haberi yeni bilgi olmadan tekrar gelirse `atlananlar` listesine düşer ve **yazılmaz**.

**Zincir (Premium yoksa) — ilk iki parça:**
```
1) 🌙 Günün Bülteni | 27 Kasım 2026
Bu akışta öne çıkanlar, sizin için derledik.

🔸 Batum'daki olayların ardından Türkiye'den bazı tur firmaları kente düzenledikleri gezileri iptal etmeye başladı. (1/6)

2) 🔸 Dışişleri Bakanlığı, Gürcistan'ın Ankara'daki büyükelçisini bakanlığa davet etti.
🔄 GELİŞME: Gürcistan'da tedavi altına alınan iki Türk taraftar hastaneden çıktı. (2/6)
```

---

## 7. Telif ve güvenlik kontrol listesi
- Olgular telifle korunmaz; **anlatım** korunur → her maddeyi yeniden yaz, başlık kopyalama (talimatta var).
- Başka özet hesaplarının (ör. ekran görüntüsündeki gibi hesaplar) gönderilerini **kaynak olarak kullanma**;
  derleme ve anlatımları onlara aittir. Kaynak yalnızca RSS (izinli yayıncılar, bkz. `TELIF_PLANI.md`).
- Yayıncı fotoğrafı/logosu ekleme. Görsel gerekiyorsa kendi şablonun (ör. akış adına göre sabit kapak).
- AA gibi abonelikli ajans içeriklerini özetten çıkarmak için `send-bulletin.js` öncesinde kaynağı kapat
  veya Claude talimatına "AA kaynaklı haberleri kullanma" ekle.
- Haftada bir rastgele 2–3 bülteni kaynak haberlerle karşılaştır (yanlış bilgi / kopya kontrolü).
- Düzeltme gerekirse: aynı gönderiye "Düzeltme:" yanıtı at, Data store'daki `son_durum`u güncelle.

## 8. Alternatif: Her şey Make'te
GitHub kullanmak istemezsen: Make'in zamanlayıcısı tek senaryoda 4 farklı saat vermeyi kolay desteklemez;
en basit yol senaryonun 4 kopyası (her biri *Every day* + kendi saati: 07:30 / 12:30 / 18:30 / 23:30) ve her
kopyanın başında akış adını sabit yazan bir *Set variable*. Kaynak olarak *RSS → Retrieve RSS feed items*
(her kaynak için) + *Array aggregator*.
Dezavantaj: daha fazla işlem (her haber bir işlem), aynı olay gruplaması yok, Firestore'daki telif kaldırma
kararları uygulanmaz.

## 9. Sorun giderme
| Belirti | Çözüm |
|---|---|
| Actions "eksik; bülten atlandı" | `MAKE_WEBHOOK_URL` / `FIREBASE_SERVICE_ACCOUNT` secret'ını ekle |
| Make webhook `410 Gone` | Senaryo kapalı veya webhook silinmiş → senaryoyu aç, adresi güncelle |
| Firestore "requires an index" | Hata mesajındaki bağlantıya tıkla (published_at aralık sorgusu tek alan dizini; normalde gerekmez) |
| Parse JSON hatası | Claude çıktısındaki ```json çitlerini temizle; Max tokens'ı artır |
| Aynı haber tekrar geldi | Data store'a yazma adımı çalışmamış olabilir (History'ye bak) veya konu anahtarı değişmiş — talimattaki "AYNI anahtarı kullan" kuralını koru |
| X `403` | App izinleri *Read and write* değil veya bağlantı yenilenmeli |
| X `429` | Plan limiti doldu → zincir yerine tek gönderi / daha az madde |
