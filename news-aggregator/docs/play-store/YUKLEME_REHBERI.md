# Play Console yükleme rehberi — Haber Akışı (akinai.app1)

Kişisel geliştirici hesabı → **üretime çıkmadan önce kapalı testte en az 12 test kullanıcısı, 14 gün kesintisiz** gerekir.
Sıra: uygulamayı oluştur → formları doldur → kapalı teste AAB yükle → 12 test kullanıcısı 14 gün → üretim erişimi başvurusu.

---

## 1. Uygulamayı oluştur
Play Console → **Uygulama oluştur**
| Alan | Değer |
|---|---|
| Uygulama adı | Haber Akışı |
| Varsayılan dil | Türkçe – tr-TR |
| Uygulama mı, oyun mu | Uygulama |
| Ücretsiz mi, ücretli mi | Ücretsiz |
| Beyanlar | Geliştirici Program Politikaları ✓, ABD ihracat yasaları ✓ |

> Paket adı (`akinai.app1`) ilk AAB yüklenince kesinleşir ve **bir daha değiştirilemez**.

## 2. Play Uygulama İmzalama
İlk yüklemede **"Google tarafından oluşturulan imzalama anahtarı"** seçeneğini (varsayılan) olduğu gibi bırakın.
AAB, sizdeki **yükleme anahtarıyla** (akinai-app1-yukleme-anahtari.zip) imzalıdır; parmak izi (SHA-256):
`B6:1C:F6:EC:EF:EB:D2:37:B3:ED:2B:44:7A:E1:27:50:2D:B0:DD:60:0F:5D:33:52:87:EB:43:9E:BE:3B:53:76`

**Yükledikten sonra (önemli):** Play Console → *Test ve yayınla → Uygulama bütünlüğü → Uygulama imzalama* sayfasındaki
**"Uygulama imzalama anahtarı sertifikası" SHA-256** değerini kopyalayın → Firebase Console → Proje ayarları →
`akinai.app1` uygulaması → **Parmak izi ekle**. (App Check / Play Integrity bunun için gerekli.) App Check'te
**"Zorunlu kıl" (Enforce) hâlâ AÇILMAZ** — uygulama üretimde, kullanıcılar Play sürümüne geçtikten sonra konuşacağız.

## 3. Uygulama içeriği (Politika → Uygulama içeriği)

### 3.1 Gizlilik politikası
`https://haber-68a3c.web.app/gizlilik`

### 3.2 Uygulama erişimi
**"Tüm işlevler özel erişim olmadan kullanılabilir"** (üyelik/giriş yok).

### 3.3 Reklamlar
**"Evet, uygulamam reklam içeriyor."**

### 3.4 İçerik derecelendirmesi (IARC anketi)
- E-posta: haberakisinfo@gmail.com · Kategori: **"Referans, Haber veya Eğitim"**
- Şiddet / cinsellik / küfür / uyuşturucu içeriği: **Hayır** (uygulama haber başlığı gösterir; haber metni yayıncının sitesinde açılır)
- Kullanıcılar arası iletişim / içerik paylaşımı: **Hayır** · Konum paylaşımı (başka kullanıcılarla): **Hayır**
- Dijital ürün satın alma: **Hayır** · Kumar: **Hayır**
- Beklenen sonuç: **3+ / PEGI 3** civarı (anket sonucunu olduğu gibi kabul edin)

### 3.5 Hedef kitle ve içerik
- Hedef yaş grubu: **18 ve üzeri** (önerilen; haber + reklam içeren uygulamada aile politikalarının ek
  yükümlülüklerinden kaçınır). "Uygulama çocukların ilgisini çekebilir mi?" → **Hayır**.

### 3.6 Haber uygulamaları
- "Uygulamanız haber uygulaması mı?" → **Evet**
- Yayıncı/iletişim bilgisi sayfası: `https://haber-68a3c.web.app/kunye`
- (Play mağaza girişinde e-posta + web sitesi dolu olmalı — magaza-girisi.md)

### 3.7 Reklam kimliği (Advertising ID)
- "Uygulamanız reklam kimliği kullanıyor mu?" → **Evet**
- Amaçlar: **Reklam veya pazarlama**, **Analiz**, **Dolandırıcılığı önleme, güvenlik ve uyumluluk**

### 3.8 Devlet uygulamaları → **Hayır** · Finansal özellikler → **Hiçbiri** (piyasa paneli yalnızca bilgi)
### 3.9 Sağlık → **Hiçbiri** · COVID-19 → **Hayır**
### 3.10 Ön plan hizmeti izinleri → istenirse: uygulama özel tür (konum, medya…) kullanmıyor.

## 4. Veri güvenliği formu
**Genel:** Veri topluyor veya paylaşıyor mu → **Evet** · Aktarımda şifreleniyor mu → **Evet** ·
Kullanıcı verilerin silinmesini isteyebilir mi → **Evet** (haberakisinfo@gmail.com; hesap yok, çoğu veri uygulama
silinince silinir) · Hesap oluşturma → **Uygulama hesap oluşturmayı desteklemiyor**.

| Veri türü | Toplanıyor | Paylaşılıyor | Geçici mi | Zorunlu mu | Amaç |
|---|---|---|---|---|---|
| **Konum → Yaklaşık konum** | Evet | Hayır* | Evet | İsteğe bağlı | Uygulama işlevi (hava durumu) |
| **Uygulama bilgisi ve performansı → Kilitlenme günlükleri** | Evet | Hayır | Hayır | Zorunlu | Analiz, uygulama işlevi (Crashlytics) |
| **Uygulama bilgisi ve performansı → Teşhis** | Evet | Hayır | Hayır | Zorunlu | Analiz (Crashlytics, App Check) |
| **Cihaz veya diğer kimlikler** | Evet | **Evet** | Hayır | Zorunlu | Uygulama işlevi (bildirim belirteci), Reklam veya pazarlama, Dolandırıcılığı önleme (AdMob reklam kimliği) |
| **Uygulama etkinliği → Uygulama etkileşimleri** | Evet | **Evet** | Hayır | Zorunlu | Reklam veya pazarlama, Analiz (AdMob) |

\* Yaklaşık konum, kullanıcı hava durumunu açtığında doğrudan cihazdan MET Norway'e gider, saklanmaz → Google
tanımına göre kullanıcının beklediği işlev için aktarım "paylaşım" sayılmaz. AdMob, IP üzerinden yaklaşık konum da
kullanabilir; Google'ın AdMob veri güvenliği rehberine göre "Konum → Yaklaşık konum: Paylaşılıyor (Reklam)" olarak da
işaretlemek daha temkinlidir — **avukat/temkinli tercih: işaretleyin.**
Toplanmayanlar: ad, e-posta, telefon, kişiler, fotoğraf, dosyalar, mesajlar, finansal bilgi, sağlık, web geçmişi.

## 5. Kapalı test (12 test kullanıcısı × 14 gün)
1. Google Gruplar'da grup açın: ör. **haber-akisi-test@googlegroups.com** (katılım: "Herkes katılabilir").
2. Play Console → *Test ve yayınla → Test → Kapalı test* → **Kanal oluştur** → Test kullanıcıları: **Google Grubu**
   olarak bu grubu ekleyin → Geri bildirim e-postası: haberakisinfo@gmail.com.
3. **Yeni sürüm oluştur** → `Haber-Akisi-1.0.0-play.aab` dosyasını yükleyin → sürüm notu
   (magaza-girisi.md) → **İncelemeye gönder**. İlk inceleme birkaç gün sürebilir.
4. Onaylanınca "Katılım bağlantısı"nı (opt-in URL) test kullanıcılarına gönderin. 14 gün sayacı, **12 kişi katılıp
   uygulamayı yükledikten** sonra işlemeye başlar; kimse ayrılmamalı.
5. 14 gün dolunca: *Kontrol paneli → Üretime erişim başvurusu* (test sürecine dair kısa sorular gelir: kaç kişi,
   ne geri bildirim aldınız, ne düzelttiniz — test sırasında not tutun).

**Test kullanıcılarına gönderilecek mesaj (WhatsApp/e-posta):**
```
Merhaba! Haber Akışı adında bir haber uygulaması geliştirdim ve Google Play'de yayınlamadan önce
14 günlük kapalı teste ihtiyacım var. Yardımcı olur musun? 🙏
1) Şu gruba katıl (Google hesabınla): https://groups.google.com/g/haber-akisi-test
2) Bu bağlantıdan test kullanıcısı ol ve uygulamayı yükle: <KATILIM BAĞLANTISI>
3) 14 gün boyunca uygulamayı silme, ara ara açıp haberlere göz at.
Hata ya da önerin olursa bana yazman yeter. Teşekkürler!
```
⚠️ Telefonunda daha önce yan yüklenmiş (APK) Haber Akışı olan kişi (siz dahil) önce onu kaldırmalı: Play sürümü farklı
imzalıdır, üzerine kurulmaz.

## 6. Üretimden önce yapılacaklar (kapalı test sürerken)
- [ ] **Gerçek AdMob kimlikleri** (şu an test reklamları): AdMob'da uygulama + 3 reklam birimi → kimlikleri gönderin →
      yeni AAB + `app-ads.txt` (siteye eklenecek; Play mağaza girişindeki web sitesiyle aynı alan adı).
- [ ] AdMob'da uygulamayı Play mağaza girişine bağlama (yayınlandıktan sonra)
- [ ] Highlightly ticari kullanım koşullarını teyit (ücretsiz plan) / gerekiyorsa ücretli plan
- [ ] Gizlilik/KVKK metinleri için avukat incelemesi, vergi için mali müşavir (20/B)
- [ ] Ekran görüntüleri (magaza-girisi.md listesi)
