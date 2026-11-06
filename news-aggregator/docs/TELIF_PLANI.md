# Telif Uyumluluk Planı (Haber Akışı)

> Bu belge hukuki danışmanlık değildir. Yayına almadan önce bir bilişim/fikri mülkiyet avukatına göstermeniz önerilir.

## 1. Uygulama ne yapıyor? (mevcut durum)

| Konu | Durum | Risk |
|---|---|---|
| Haber metni | Yalnızca **başlık + en fazla 200 karakterlik kısa alıntı** (RSS'te yayıncının kendi verdiği özet). Haberin tamamı saklanmaz. | Düşük |
| Haberin okunması | Haber **kaynağın kendi sitesinde** (WebView) açılır; yayıncının reklamları ve sayfası olduğu gibi görünür. Üstte kaynak adı + kilitli adres gösterilir. | Düşük |
| Kaynak belirtme | Kartta kaynak adı + logo, bildirimde **"Kaynak: …"** satırı, okuma ekranında alan adı. | Düşük |
| Görseller | RSS'teki küçük görsel yayıncının sunucusundan gösterilir (kopyalanmaz). | **Orta** — haber fotoğrafları ajanslara/fotoğrafçılara ait olabilir |
| Anadolu Ajansı | AA içerikleri abonelikle satılır. | **Yüksek** → AA kaynaklarında görsel kapatıldı (`allow_images: false`) |
| Uyar-Kaldır | Ayarlar'da e-posta ile talep; 48 saat içinde işlem; GitHub'dan tek tıkla kaldırma (aşağıda). | — |

## 2. Yapılan değişiklikler (bu sürüm)
- Özet sınırı 300 → **200 karakter** (kısa alıntı).
- Bildirimlerin altına **kaynak adı** eklendi.
- Kaynak bazında **görsel kapatma**: `sources/<id>.allow_images = false` → yayıncı görseli yerine kategori görseli.
- AA kaynaklarında görsel varsayılan kapalı (yeniden "seed" çalıştırıldığında uygulanır; hemen uygulamak için aşağıdaki `images` işlemi).
- **Telif Kaldırma iş akışı** (GitHub Actions → *Haber Akışı - Telif Kaldırma* → Run workflow):
  - `article` + haber adresi → haberi ve kopyalarını siler
  - `source` + kaynak kimliği → kaynağı kapatır, tüm haberlerini siler
  - `images` + kaynak kimliği → kaynağın görsellerini kaldırır
  - Her işlem Firestore `takedowns` koleksiyonuna tarih ve notla kaydedilir.

## 3. Talep gelince ne yapılır? (48 saat kuralı)
1. E-postayı yanıtla: "Talebiniz alındı, 48 saat içinde işleme alınacaktır."
2. Hak sahibi olduğunu makul ölçüde doğrula (kurumsal e-posta adresi vb.).
3. GitHub → Actions → **Telif Kaldırma** → işlem türünü seç → hedefi yaz → not alanına talep sahibini ve tarihi yaz → Run.
4. Hak sahibine "kaldırıldı" yanıtı gönder. Kaynağın tamamı istenmişse `source` kullan.

## 4. Yayın öncesi yapılacaklar (senin tarafında)
- [ ] **Telif e-posta adresi**: `AppConfig.copyrightEmail` gerçek ve okunan bir adres olmalı.
- [ ] **Gizlilik politikası + Kullanım koşulları** sayfası (Play Console zorunlu); içinde "içerikler kaynaklarına aittir, yalnızca başlık/kısa özet ve bağlantı gösterilir" maddesi.
- [ ] Mümkünse büyük yayıncılardan (NTV, TRT, Hürriyet, Habertürk, CNN Türk) **e-posta ile RSS kullanım izni** iste; olumlu cevapları sakla. İzin vermeyeni `source` ile kaldır.
- [ ] AA'yı tamamen çıkarmayı ya da AA ile abonelik görüşmeyi değerlendir (en riskli kaynak).
- [ ] Play Store açıklamasında yayıncı adları/logoları "resmi uygulama" izlenimi vermeyecek şekilde kullanılmalı (ör. "NTV haberleri" değil "Türkiye'deki haber kaynaklarından başlıklar").
- [ ] Uygulama simgesi ve mağaza görsellerinde yayıncı logosu **kullanma**.

## 5. Kaçınılacaklar
- Haberin tam metnini kopyalamak / uygulama içinde yeniden yayınlamak.
- Yayıncı sitesindeki reklamları WebView'da engellemek.
- Görselleri kendi sunucuna kopyalamak (şu an yapılmıyor; görseller yalnızca cihazda geçici önbellekte, 3 gün).
- Paywall'lu (ücretli) içeriği atlatmak.
