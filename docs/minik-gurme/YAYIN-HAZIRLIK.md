# Minik Gurme — Yayın Hazırlık Rehberi

> Genel bilgilendirmedir, hukuki görüş değildir. Ücretli sürüm ve gizlilik metinleri için bir avukata danışmanız önerilir.

## 1. Kimlik ve imza
| Öğe | Değer |
|---|---|
| Paket adı (kalıcı) | `akinai.minikgurme` |
| Yükleme anahtarı | `minikgurme-upload.jks` (alias `upload`, RSA 4096, 10.000 gün) — **depoda değil**, size ayrıca verildi |
| Şifreler | `keystore.properties` içinde — şifre yöneticisinde saklayın, kimseyle paylaşmayın |
| Play App Signing | Play Console'da **açık** olmalı (Google uygulama imzalama anahtarını saklar; siz yalnızca yükleme anahtarını kullanırsınız) |

Derleme: `minik_gurme/keystore.properties` ve `.jks` dosyasını proje köküne koyup `./gradlew :app:bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.
Anahtar kaybolursa Play Console'dan "yükleme anahtarı sıfırlama" talep edilebilir (Play App Signing açıksa).

## 2. Yayından önce sizin yapmanız gerekenler
1. **Google Play geliştirici hesabı** (25 $). Bireysel yeni hesaplarda: üretime çıkmadan önce **12 test kullanıcısıyla 14 gün kapalı test**.
2. **İletişim e-postası**: `util/LegalTexts.kt` → `CONTACT_EMAIL` doldurulmalı.
3. **Gizlilik politikası adresi**: Uygulamadaki metni (Ayarlar › Gizlilik Politikası) bir web sayfasında yayımlayın (ör. Google Sites) ve adresi Play Console'a girin.
4. **Marka araması**: TÜRKPATENT'te "Minik Gurme" (sınıf 9, 41, 44).
5. **İçerik onayı (önerilir)**: Rehber ve tarifleri bir çocuk doktoru / diyetisyen gözden geçirsin.
6. **Abonelik ürünleri** (Play Console › Para kazanma › Abonelikler):
   - `minikgurme_plus_monthly` — 1 aylık temel plan, otomatik yenilenen
   - `minikgurme_plus_yearly` — 1 yıllık temel plan, otomatik yenilenen
   - Öneri: aylık ~59,99 TL, yıllık ~399,99 TL; isteğe bağlı 7 gün ücretsiz deneme (teklif olarak).
   - Fiyatlar uygulamaya Play'den gelir; kodda sabit fiyat yok.
7. **Vergi/ödeme profili** (Play Console › Ödemeler) ve gelir için vergi kaydı (şahıs şirketi / serbest meslek) — muhasebecinize danışın.

## 3. Play Console formları (taslak cevaplar)
**Veri güvenliği**
- Uygulama kullanıcı verisi topluyor veya paylaşıyor mu? → **Hayır** (tüm veriler cihazda; geliştiriciye gönderilmez).
- Satın alma geçmişi Google Play tarafından işlenir (Google'ın kendi beyanı kapsamında).
- Veriler aktarımda şifreleniyor mu? → Uygulama veri aktarmaz.
- Kullanıcı veri silme talep edebilir mi? → Evet: Ayarlar'dan sıfırlama / uygulamayı kaldırma.

**Sağlık uygulamaları beyanı**: Kategori "Beslenme ve kilo yönetimi / ebeveynlik". Tıbbi cihaz değildir; teşhis/tedavi sunmaz.

**Hedef kitle ve içerik**: Hedef yaş **18+** (ebeveynler). "Çocuklara yönelik" **işaretlenmemeli**.

**İçerik derecelendirmesi (IARC)**: Şiddet/cinsellik/kumar yok, kullanıcı etkileşimi yok, konum paylaşımı yok → "Herkes / 3+".

**Reklam**: Uygulamada reklam **yok**.

**İzinler**: `POST_NOTIFICATIONS` (hatırlatıcılar), `RECEIVE_BOOT_COMPLETED` (yeniden başlatmada hatırlatıcıları kurmak), `BILLING` (Play aboneliği). İnternet izni yok.

## 4. Mağaza metinleri
**Uygulama adı**: Minik Gurme: Ek Gıda Takibi
**Kısa açıklama (TR, ≤80)**: Bebeğinizin ek gıda yolculuğu: 100 gıda, alerjen planı, tarifler ve takip.
**Short description (EN)**: Your baby's solid food journey: 100 foods, allergen plan, recipes & tracking.

**Uzun açıklama (TR)**
Minik Gurme, 6. aydan itibaren ek gıdaya geçişi kolaylaştıran sade bir takip uygulamasıdır.
• 1 yaşına kadar 100 gıda: denediklerinizi işaretleyin, tepkileri not edin
• Alerjen tanıtım planı: sık görülen 9 alerjeni 3 gün kuralıyla sırayla deneyin
• Yaşa uygun tarifler, porsiyon ayarı ve alışveriş listesi
• Ay ay beslenme rehberi, kıvam aşamaları ve güvenlik uyarıları
• Kilo ve boy takibi; WHO kız/erkek medyanlarıyla karşılaştırma
• Açık ve koyu tema, Türkçe ve İngilizce
Gizlilik: Hesap yok, reklam yok, internet yok — tüm veriler telefonunuzda kalır.
Minik Gurme Plus: haftalık menü planlayıcı, doktor raporu (PDF), birden fazla bebek, öğün hatırlatıcıları.
Uygulama genel bilgi sunar; tıbbi tavsiye yerine geçmez. Lütfen çocuk doktorunuza danışın.

**Long description (EN)**
Minik Gurme is a simple tracker that makes starting solids easier from 6 months.
• 100 foods before age 1: tick what you've tried and note reactions
• Allergen introduction plan: introduce the 9 common allergens one by one with the 3-day rule
• Age-appropriate recipes, portion scaling and a shopping list
• Month-by-month feeding guide, texture stages and safety tips
• Weight and height tracking compared with WHO girl/boy medians
• Light and dark theme, Turkish and English
Privacy: no account, no ads, no internet — all data stays on your phone.
Minik Gurme Plus: weekly meal planner, doctor report (PDF), multiple babies, meal reminders.
The app provides general information and does not replace medical advice. Please consult your paediatrician.

**Görseller**: Simge 512×512 (`res/drawable-nodpi/logo_emblem.webp` kaynağından), öne çıkan görsel 1024×500, en az 2 telefon ekran görüntüsü (`docs/minik-gurme/screenshots`).

## 5. Uygulanan güvenlik/uyum değişiklikleri
- Debug anahtarıyla yayın imzası kaldırıldı; anahtar yoksa release imzasız kalır.
- Paket adı ve iç adlar AI Studio kalıntılarından temizlendi.
- WHO medyanları kız/erkek ayrı (0–24 ay); cinsiyet seçilmezse ortalama.
- "Dr. Tavsiyesi" → "İpucu"; "klinik/pediatrik onaylı" izlenimi veren ifadeler yumuşatıldı; WHO rozetleri kaldırıldı.
- İlk açılışta zorunlu "tıbbi tavsiye değildir" onayı; Ayarlar'da Gizlilik, Koşullar, Lisanslar.
- Lisansı belirsiz 4 fotoğraf kaldırıldı.
- Yedekleme kuralları açık; Gizlilik metninde belirtildi. Dışa aktar / geri yükle eklendi.
- Abonelik doğrulaması cihazda; gelir büyüdüğünde sunucu taraflı doğrulama önerilir.
