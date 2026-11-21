# Haber Akışı — Strateji ve Yol Haritası (22 Kasım 2026)

Bu belge dört soruya cevap verir: (1) "187 yeni haber" gibi yığın bildirimler neden geliyor ve nasıl düzelir,
(2) rakipler ne yapıyor, biz neyi ekleyebiliriz, (3) uygulamayı gereksiz dolduran ne, (4) ücretsiz büyüme ve X planı.

> Not: Türkiye haber uygulaması indirme sıralamalarına ait güncel, güvenilir bir 2026 verisi bulunamadı. Rakip
> değerlendirmesi uygulamaların bilinen özelliklerine ve sektör araştırmalarına dayanır; kesin pazar payı değildir.

---

## 1. Bildirim sorunu: "📰 187 yeni haber"

### Neden oluyor (kodda tespit edildi — `firebase/functions/src/notify.js`)
| Sorun | Ayrıntı |
|---|---|
| Sayı şişiyor | Kategori kuyruğundaki haber sayıları her turda **toplanıyor**; gönderilene kadar 187'ye çıkabiliyor. Kullanıcıya "çok fazla" hissi veriyor, bilgi taşımıyor. |
| Önemsiz başlıklar | Özet, her kategorinin **en yeni** haberini seçiyor, **en önemli** (çok kaynaklı) haberini değil. "Selçuk İnan: Oyuncularımı tebrik ediyorum" gibi sıradan başlıklar giriyor. |
| Gece gönderim | 00:00–08:00 arası özet yine gidiyor (sessiz kanalda ama bildirim yine düşüyor). Ekrandaki bildirim 00:19'da. |
| Çok tür | Son dakika + kategori özeti (saatlik) + günde 3 bülten + maç + panel + bayram/kandil → aynı kişiye günde 10+ bildirim. |

### Çözüm (önerilen — onayınla uygulanır)
1. **Sayıyı kaldır.** Başlık: "📰 Gündemin öne çıkanları" ya da kategori adı. Asla "187".
2. **Yalnızca önemli haber.** Özete yalnız **2+ farklı yayıncının verdiği** hikâyeler girsin (bildirimdeki önem ölçütüyle aynı). Tek kaynaklı rutin haberler bildirime girmez, akışta kalır.
3. **Gece özet yok.** 23:00–08:00 arası özet birikir, **sabah bülteninde** tek seferde gider. Gece yalnız deprem/afet gibi kritik son dakika.
4. **Daha seyrek.** "Dengeli": özet en az **3 saatte bir**, günde en fazla **4**; "Tümü": 90 dk'da bir, günde en fazla 10.
5. **Bülten = özet.** Günde 3 bülten zaten varsa, bültene yakın saatlerde (±90 dk) ayrı özet gitmesin.
6. **Ölç.** Bildirim açılma oranı ve "Daha az bildirim" tıklamaları haftalık izlensin (Firebase Analytics olayları).

Sektör verisi de bunu destekliyor: Reuters Digital News Report 2025'e göre bildirim almayanların en sık gerekçesi
"çok fazla bildirim" (%19).

---

## 2. Rakip analizi

### Rakip türleri
| Tür | Örnekler | Güçlü | Zayıf |
|---|---|---|---|
| Yayıncı uygulamaları | NTV, CNN Türk, Hürriyet, Sözcü, Habertürk | Marka güveni, video/canlı yayın, hız | Tek kaynak, yoğun reklam, **çok bildirim** |
| Büyük toplayıcı | Google Haberler | Ücretsiz, kişiselleştirme, "Tam kapsam" (aynı olayın farklı kaynakları) | Türkiye'ye özel servis yok (skor, döviz, deprem), kişiliksiz |
| "Son dakika" uygulamaları | Mağazadaki "Son Dakika", haber siteleri ağları | Hız | Tıklama tuzağı, reklam yükü, düşük güven |
| Kürasyon | Oksijen gibi seçilmiş içerik | Kalite, sakinlik | Dar kapsam |
| Yurt dışı yenilikçi | Ground News (farklı bakış açıları), Inshorts (kısa özet), SmartNews | Farklılaşmış tek bir güçlü fikir | Türkiye'de yok/zayıf |

### Bizim farkımız (korunmalı ve öne çıkarılmalı)
- **Çok kaynak doğrulaması:** "3 yayıncı verdiyse önemli" → sakin ama güvenilir bildirim.
- **Tekrar ayıklama:** aynı haber 10 kez görünmüyor.
- **Canlı skor paneli** (Android'de rakiplerde yok denecek kadar az).
- **Üyeliksiz, sade.**

### Eklenebilecekler (öncelik sırasıyla)
| # | Özellik | Neden | Zorluk |
|---|---|---|---|
| 1 | **"Bu haberi kimler verdi?"** — haber kartında kaynak listesi (Ground News'ün Türkiye sade versiyonu, yalnız kaynak adları) | Kutuplaşmış gündemde güçlü fark; zaten elimizde kümeleme var | Düşük |
| 2 | **Deprem uyarısı** (AFAD/Kandilli verisi, büyüklük ve konum eşiğiyle) | Türkiye'de en çok aranan anlık bilgi; uygulama indirtme sebebi | Orta |
| 3 | **Ana ekran widget'ı** (son dakika + döviz, ya da canlı skor) | Günlük açılmadan görünürlük | Orta |
| 4 | **Sabah "5 dakikada gündem" ekranı** (bültenin uygulama içi hali, 5–7 hikâye) | Alışkanlık oluşturur | Düşük |
| 5 | **Şehrini seç → yerel haber** (önce 81 il yerine büyük şehirler + kullanıcının ili) | Yerel kitle, rakiplerde zayıf | Orta |
| 6 | **Akaryakıt fiyatları / nöbetçi eczane** (lisanslı/kamuya açık kaynak bulunursa) | Sık aranan, geri getiren hizmet | Orta–Yüksek (veri lisansı) |
| 7 | **Puanlama isteği** (5. okumadan sonra, sistem penceresi) | Mağaza sıralaması için kritik | Düşük |

⚠️ **Yapay zekâ ile haber özetleme önerilmez**: yayıncı metnini yeniden yazmak telif (FSEK) riski taşır.
Başlık + kaynak + bağlantı modelinde kalın.

---

## 3. Fazlalıklar — sadeleştirilmeli

| Fazlalık | Öneri |
|---|---|
| **14 kategori** (magazin, otomobil, eğitim, yaşam, kültür-sanat, bilim, sağlık…) | İlk açılışta 6–8 ana kategori göster; diğerleri "Daha fazla" altında. **Spor + Futbol** ayrı olması kafa karıştırıyor → tek "Spor", altında futbol. |
| **6 bildirim türü** | Varsayılan: Son dakika + günde 2 bülten + takım maçları. Diğerleri isteğe bağlı. |
| **Bayram/kandil/anma bildirimleri** | Haber uygulamasında beklenmiyor → **varsayılan kapalı** (açmak isteyen açsın). |
| **Ana sayfa üst kısmı** (piyasa paneli + hava kartı + canlı maçlar + bayram afişi + reklam) | İlk haber ekranın altına itiliyor. Tek ince **"bilgi şeridi"**: döviz · hava · canlı skor yan yana; dokununca açılsın. |
| **Birden çok akış görünümü** | 1–2 görünüm yeter (standart + kompakt). |
| **Geçiş (tam ekran) reklamı her 4 okumada** | Okuma deneyimini bozar, mağaza puanını düşürür → her 8 okumada bir ya da yalnız uygulamadan çıkarken. Banner + akış içi reklam gelirin çoğunu sağlar. |

---

## 4. Ücretsiz büyüme yol haritası (90 gün)

### Hafta 1–2: Temel
- [ ] Bildirim düzeltmesi (Bölüm 1) — **önce bu**: kötü bildirim = kaldırma.
- [ ] Mağaza başlığı/anahtar kelimeler (ASO): ad "Haber Akışı: Son Dakika, Skor" (≤30), kısa açıklamada "son dakika, canlı skor, döviz, deprem".
- [ ] Puanlama isteği (uygulama içi, Google/Apple'ın kendi penceresi).
- [ ] Play kapalı test: 12 testçi × 14 gün (aile/arkadaş) → üretime çıkış.

### Hafta 3–6: Dağıtım kanalları (hepsi ücretsiz)
- [ ] **WhatsApp Kanalı** "Haber Akışı Son Dakika": Türkiye'de en güçlü ücretsiz dağıtım; günde 3–5 önemli haber + uygulama linki.
- [ ] **Telegram kanalı** (kurulu): aynı içerik otomatik.
- [ ] Haberi uygulamadan paylaşınca metin sonuna "Haber Akışı ile paylaşıldı + indirme linki".
- [ ] Forumlar: DonanımHaber, Technopat "Uygulamamı yaptım" konusu (geliştirici hikâyesi olarak, reklam gibi değil).
- [ ] Yerel: Manisa/Saruhanlı Facebook ve WhatsApp grupları — yerel haber özelliği gelince.

### Hafta 7–12: Büyütme
- [ ] Deprem uyarısı + widget (Bölüm 2 #2–3) → "neden indireyim" sorusunun cevabı.
- [ ] Mağaza ekran görüntülerini yeni özelliklerle yenile.
- [ ] Haftalık ölçüm: indirme, 7 gün sonra kalan kullanıcı (D7), bildirim açılma oranı, puan.

---

## 5. X (Twitter) büyüme planı — adım adım

### Gün 1: Profil
1. Ad: **Haber Akışı | Son Dakika** — kullanıcı adı kısa ve akılda kalıcı.
2. Profil resmi: uygulama logosu. Kapak (1500×500): logo + "Gündem tek akışta" + uygulama ekranı.
3. Bio (≤160): "Birden çok kaynağın doğruladığı önemli haberler. Son dakika · canlı skor · döviz. 📲 Uygulama: [link]"
4. **Sabitlenmiş gönderi:** animasyonlu GIF (hazır: `docs/brand/haber-akisi-animasyon.gif`) + 3 cümlelik tanıtım + Play/App Store linki.

### Her gün: içerik ritmi
| Saat | İçerik |
|---|---|
| 08:30 | **Sabah özeti**: "Bugün gündemde 5 başlık" (kısa liste, tek gönderi ya da flood) |
| Gün içinde | **Son dakika** (otomatik, Buffer): yalnız 3+ kaynaklı haberler, günde en fazla 6–8 |
| Maç saatleri | Büyük maçlarda gol/sonuç kartı ("⚽ GOL! GS 1–0 SAM 27'"), maç sonu skor görseli |
| 18:30 | **Akşam piyasa**: dolar/euro/altın tek satır (görsel kart haftada 2–3) |
| Deprem | Kandilli/AFAD verisiyle anında, 4+ büyüklükte |

### Etkileşim kuralları
- Büyük haber hesaplarının son dakika gönderilerine **hızlı ve değer katan** yanıt: "Aynı haberi 4 kaynak doğruladı: …" (spam değil, bilgi).
- Gündem başlığına (TT) uygun 1–2 etiket; etiket yığını yok.
- Haftada 1 **anket** ("Bu hafta en çok hangi haber ilgini çekti?") ve 1 "Haftanın 5 haberi" flood.
- Futbol, teknoloji ve ekonomi **X Toplulukları**'na katıl, haftada birkaç kez katkı ver.
- Gece 00–08 arası paylaşım yok (uygulamadaki sessiz saatle tutarlı).

### Yasaklar
- Yayıncının metnini/fotoğrafını kopyalama: kendi cümlenle başlık + kaynak adı + bağlantı.
- Otomatik beğeni/yanıt/takip botu kullanma (X kuralları, hesap kapanır).
- Doğrulanmamış haberi "son dakika" diye verme: tek güven kaybı her şeyi siler.

### Ölçüm ve hedef (gerçekçi, garanti değil)
| Süre | Hedef |
|---|---|
| 30 gün | 300 takipçi, günlük 1–2 bin gösterim |
| 90 gün | 1.000–1.500 takipçi, sabitlenmiş gönderiden haftada 30+ link tıklaması |

Her hafta bak: takipçi artışı, en çok gösterim alan 3 gönderi (türünü çoğalt), link tıklaması. Link sonuna
`?utm_source=x` eklenirse mağazaya gelenlerin X'ten geldiği ölçülür.

---

## Uygulama sırası (önerim)
1. **Bildirim düzeltmesi** (sunucu, uygulama güncellemesi gerektirmez) — 1 gün
2. Fazlalık sadeleştirme + puanlama isteği (uygulama güncellemesi) — 2–3 gün
3. "Bu haberi kimler verdi?" — 1–2 gün
4. Deprem uyarısı — 3–5 gün
5. Widget — 3–5 gün

Kaynaklar: Reuters Institute Digital News Report 2025 (Press Gazette özeti), Zapier/daily.dev haber uygulaması karşılaştırmaları.
