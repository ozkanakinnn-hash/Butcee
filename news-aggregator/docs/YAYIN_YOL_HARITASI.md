# Haber Akışı — Play Store Yayın Yol Haritası, Eksik Analizi ve Hukuki Risk Değerlendirmesi

> 11 Kasım 2026. Bu belge hukuki danışmanlık değildir; "Yüksek" riskli maddeleri yayından önce bir bilişim/fikri mülkiyet
> avukatına ve vergi için bir mali müşavire teyit ettirin.

---

## 1. Play Store'da yayın süreci (sırasıyla)

| # | Adım | Not |
|---|---|---|
| 1 | **Geliştirici hesabı** (play.google.com/console) — 25 $ tek seferlik, kimlik doğrulama | **Kişisel** hesap: yeni uygulama için **kapalı test (en az 12 testçi, 14 gün kesintisiz)** zorunlu. **Kuruluş** hesabı (şirket + D-U-N-S numarası, ~30 gün) bu kuraldan muaf. |
| 2 | **Alan adı + web sitesi** (ör. haberakisi.app) | Gizlilik politikası, kullanım koşulları, iletişim/künye sayfası ve AdMob için `app-ads.txt` burada durur. |
| 3 | **Yayın imza anahtarı** + Play App Signing | Şu an test anahtarıyla imzalı. Play'e yükleme anahtarı oluşturulacak, yedeklenecek (kaybolursa güncelleme yapılamaz). |
| 4 | **AAB derlemesi** (`flutter build appbundle`) | Play APK değil AAB ister. Hedef API 36 (Android 16) — uygulama zaten 36. |
| 5 | **Gerçek AdMob hesabı ve kimlikleri** | Şu an Google test reklam kimlikleri var. AdMob'da uygulama + birimler + ödeme bilgisi. |
| 6 | **Play Console "Uygulama içeriği" beyanları** | Gizlilik politikası URL'si, reklam içerir, veri güvenliği formu, içerik derecelendirmesi anketi, hedef kitle (18+ önerilir), **haber uygulaması beyanı**, konum izni gerekçesi. |
| 7 | **Mağaza girişi (ASO)** | Ad, kısa/uzun açıklama (TR+EN), simge 512 px, öne çıkan görsel 1024×500, en az 2 ekran görüntüsü. |
| 8 | **Kapalı test** (12+ testçi, 14 gün) → **üretim erişimi başvurusu** | Testçiler gerçekten kullanmalı; geri bildirim formu tutun. |
| 9 | **Üretim yayını** — önce %10–20 aşamalı dağıtım | Çökme ve ANR oranlarını izleyip %100'e çıkın. |

---

## 2. Uygulama analizi

### 2.1 Güçlü yanlar (korunacak)
- Kaynak gösteren, metni kopyalamayan haber akışı (başlık + ≤200 karakter, okuma kaynağın sitesinde)
- Son dakika, kategori bildirimleri, gün içine yayılan özet bildirim, gece sessiz saatleri
- Canlı skor paneli (Android), gol/maç bildirimleri, Süper Lig takım seçimi
- Bayram/anma/kandil bildirimleri, açık/siyah koyu tema, TR/EN
- Uyar-Kaldır aracı, AA görsellerinin kapatılması, sunucusuz değil ama düşük maliyetli Firebase mimarisi

### 2.2 Yayın ÖNCESİ zorunlu eksikler (bloklayıcı)
1. **Gizlilik politikası / kullanım koşulları / telif e-postası** — `lib/config.dart` hâlâ `uygulamaadiniz.com` yer tutucuları.
2. **Künye / iletişim sayfası** (5651 m.3 ve Play haber politikası: e-posta + telefon ya da adres).
3. **Yayın imzası + AAB**, sürüm numarası düzeni (1.0.0+1 → otomatik artış).
4. **Gerçek AdMob kimlikleri + app-ads.txt**, UMP/ATT izin metinleri (UMP zaten var).
5. **Veri güvenliği formu** için toplanan veriler: FCM token (cihaz kimliği), yaklaşık konum (hava durumu, cihazda), reklam kimliği (AdMob), çökme verisi (Crashlytics eklenirse), isimsiz takım sayacı.
6. **MET Norway hava durumu atfı** (CC BY 4.0) uygulamada görünür olmalı — kontrol edin/ekleyin.
7. **Sorunlu veri kaynaklarının temizlenmesi** (bkz. 3. bölüm): Investing.com, BIST 100 / Truncgil piyasa verisi.
8. **Firebase App Check** — `team_follows` yazma kuralı herkese açık; kötüye kullanım ve Firestore kotası için gerekli.
9. **Çökme raporlama** (Firebase Crashlytics) — CI/emülatör testi artık yok; sahada çökmeyi görmenin tek yolu.
10. **Derleme hattı** — GitHub Actions kapandı; APK/AAB şimdilik bulut oturumunda elle. Kalıcı çözüm: Codemagic (Flutter'a özel, ücretsiz kota) ya da Bitbucket Pipelines.

### 2.3 Geliştirilebilir
- **Kişiye özel bildirim ayarları**: kategori başına aç/kapa, sessiz saatleri kullanıcının seçmesi, özet sıklığı.
- **Kaydedilenler / sonra oku**, paylaşımda kaynak bağlantısı.
- **Arama**: ön ek aramasına ek olarak tarih/kategori süzgeci.
- **Erişilebilirlik**: büyük yazı, ekran okuyucu etiketleri, kontrast.
- **Tablet/yatay düzen**, ana ekran **widget'ı** (son dakika / skor).
- **Çevrimdışı**: son 50 haberi önbellekten okuma (kısmen var).
- **Performans**: ilk açılış süresi, görsel boyutları (sunucu tarafı küçük görsel yok).
- **Canlı skor**: iOS yok; Android'de "Live Updates" (Android 16 terfi edilmiş bildirim) desteği.
- **İzleme**: Firebase Analytics (açık rıza ile), bildirim açılma oranları.

### 2.4 Eklenebilir (büyüme)
- **Günün Özeti** (yapay zekâ, yalnızca başlık+kısa özetten, kaynaklı, "YZ ile üretildi" etiketi)
- Yerel haberler (il seçimi), namaz vakitleri/ramazan modülü (ayrı uygulamaya da dönüşebilir)
- Puan durumu, fikstür, maç detayı sayfası (lisanslı veriyle)
- Abonelikle reklamsız sürüm (Google Play Billing)

### 2.5 Çıkarılabilir / sadeleştirilecek
- **Investing.com kaynakları** (kullanım koşulları ticari yeniden yayına kapalı) → kaldırın.
- **BIST 100 değeri** (lisans gerektirebilir) ve **Brent** (veri yok) → paneldan çıkarın ya da lisanslı kaynağa geçin.
- **FlutterFlow klasörü, eski GitHub Actions iş akışları, `ios/` taslağı, eski Yahoo/market denemeleri** → depodan temizleyin.
- **Test maçı kodu** → kalabilir ama yalnızca yöneticinin tetikleyebildiğinden emin olun (şu an yalnız Admin SDK ile).
- **API-Football yedeği** → kullanılmıyorsa kaldırılabilir (kod sadeleşir).

---

## 3. Hukuki risk analizi

| Risk | Seviye | Neden | Ne yapmalı |
|---|---|---|---|
| **Gizlilik politikası/aydınlatma metni yok** | **Yüksek** | KVKK m.10 aydınlatma yükümlülüğü; Play zorunlu kılar. | KVKK uyumlu aydınlatma metni + gizlilik politikası (TR/EN) yayımlayın, uygulamada bağlantı. |
| **Yurt dışına veri aktarımı** (Firebase/Google, AdMob) | **Yüksek** | KVKK m.9 (2024 değişikliği): 1 Eylül 2024'ten sonra açık rıza tek başına yetmiyor; standart sözleşme imzalanıp **5 iş günü** içinde Kurum'a bildirilmeli. | Google'ın sunduğu sözleşme/taahhütleri avukatla değerlendirin; standart sözleşme gerekiyorsa bildirin. Toplanan veriyi asgaride tutun (zaten hesap yok). |
| **Kişiselleştirilmiş reklam** | Orta | Açık rıza / UMP; KVKK açısından reklam kimliği kişisel veri. | UMP Türkiye'de de gösterilsin, kişiselleştirilmemiş reklam seçeneği. |
| **Haber içeriği telifi** (FSEK) | Orta | Günlük haber/olaylar serbest (FSEK m.36), ama **fotoğraflar ve özgün metin** korunur. | Başlık + kısa özet + kaynağa bağlantı (var). Görselleri yayıncı izni olmayanlarda kapatın (AA yapıldı). Yayıncılardan e-posta ile izin isteyin, Uyar-Kaldır 48 saat. |
| **Investing.com içeriği** | **Yüksek** | Ticari yeniden yayın yasak. | Yayından önce kaldırın. |
| **Anadolu Ajansı** | Orta | Abonelikli ajans içeriği. | Yalnızca başlık+bağlantı; mümkünse yazılı izin ya da kaldırın. |
| **BIST 100 / piyasa verisi** | **Yüksek** | Borsa İstanbul verisi lisanslı dağıtıcılar üzerinden; Truncgil'in ticari kullanım izni belirsiz. | BIST'i çıkarın ya da lisanslı dağıtıcıdan alın; döviz/altın için TCMB (resmî) kullanın. |
| **Canlı skor verisi** | Düşük-Orta | Skor bir olgu; risk sağlayıcının lisans koşulunda. | Highlightly koşullarında ticari kullanım iznini yazılı teyit edin; kulüp armaları kullanılmıyor (doğru). |
| **Marka**: "Süper Lig", kulüp adları/renkleri, "Haber Akışı" adı | Düşük-Orta | Tanımlayıcı kullanım serbest; logo/arma ve "resmî" izlenimi riskli. | Mağaza görsellerinde arma/logo yok; "resmî değildir" ibaresi; TÜRKPATENT'te ad araştırması. |
| **5651 İnternet Kanunu** | Orta | İçerik sağlayıcı sıfatı; iletişim bilgisi bulundurma; erişim engeli kararlarına uyum. | Künye/iletişim sayfası; kaldırma taleplerine hızlı yanıt (araç var). |
| **5187 Basın Kanunu (internet haber sitesi)** | Düşük | Toplayıcı uygulama, özgün haber üretmiyor. "Günün Özeti" eklenirse içerik üretimine yaklaşır. | YZ özeti eklemeden önce avukata danışın; düzeltme/cevap hakkı iletişimi. |
| **Reklam mevzuatı** (Ticari Reklam Yönetmeliği) | Düşük | Reklam ayırt edilebilir olmalı. | "Reklam" etiketi var; haberle karışmasın. |
| **Bildirim politikası** (Play) | Düşük | Aşırı/yanıltıcı bildirim yasak. | Gün içine yayma, sessiz saat, ayarlardan kapatma — mevcut. |
| **Konum izni** (Play) | Düşük | Gerekçe beyanı gerekir. | Yalnız yaklaşık konum, yalnız hava durumu; reddedilse de uygulama çalışıyor (doğru). |
| **Vergi** | Orta | AdMob geliri vergiye tabi. | GVK mük. 20/B "mobil uygulama geliştiriciliği istisnası": istisna belgesi + özel banka hesabı, %15 stopaj (2026 sınırı ~5,3 milyon TL — mali müşavirle teyit). Sınır aşılırsa şahıs şirketi. |
| **VERBİS** | Düşük | Çalışan <50 ve bilanço <100 milyon TL ise genelde muaf. | Mali müşavir/avukatla teyit. |

---

## 4. Yol haritası

```
Hafta 1  ── HUKUK & HESAP ──────────────────────────────────────────────
           • Play geliştirici hesabı (kişisel: 12 testçi planı) • alan adı + site
           • Gizlilik politikası, KVKK aydınlatma, kullanım koşulları, künye (avukat)
           • Mali müşavir: 20/B istisna belgesi + banka hesabı
Hafta 2  ── YAYIN ÖNCESİ TEKNİK ────────────────────────────────────────
           • Investing kaldır, BIST/Truncgil → TCMB, MET Norway atfı
           • config.dart gerçek adresler, App Check, Crashlytics
           • Yükleme anahtarı + AAB + gerçek AdMob + app-ads.txt
           • Codemagic derleme hattı
Hafta 3  ── PLAY CONSOLE ───────────────────────────────────────────────
           • Veri güvenliği, içerik derecelendirmesi, haber beyanı, reklam, hedef kitle
           • Mağaza girişi (TR/EN), ekran görüntüleri, öne çıkan görsel
           • Kapalı test başlat (12+ testçi)
Hafta 3–5 ─ KAPALI TEST (14 gün kesintisiz) ────────────────────────────
           • Çökme/geri bildirim düzeltmeleri, haftalık test sürümü
Hafta 5–6 ─ ÜRETİM ─────────────────────────────────────────────────────
           • Üretim erişimi başvurusu → %20 aşamalı → %100
Ay 2–3   ── BÜYÜME ─────────────────────────────────────────────────────
           • Kişisel bildirim ayarları, kaydedilenler, widget
           • Highlightly Pro (anında gol), yerel haber, Günün Özeti (hukuki onaydan sonra)
           • Reklamsız abonelik
```
