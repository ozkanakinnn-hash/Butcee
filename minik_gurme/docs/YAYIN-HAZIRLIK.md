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
2. **İletişim e-postası**: `util/LegalTexts.kt` → `CONTACT_EMAIL` = ozkan.akinnn@gmail.com (ayrı bir destek adresi açarsanız değiştirin).
3. **Gizlilik politikası adresi**: https://ozkanakinnn-hash.github.io/Butcee/minik-gurme/gizlilik.html (uygulamadaki metinden `tool/build_site.py` ile üretilir, GitHub Pages ile yayınlanır). Hesap silme: …/minik-gurme/hesap-silme.html. Mağaza metinleri ve görseller: `docs/store/`.
4. **Marka araması**: TÜRKPATENT'te "Minik Gurme" (sınıf 9, 41, 44).
5. **İçerik onayı (önerilir)**: Rehber ve tarifleri bir çocuk doktoru / diyetisyen gözden geçirsin.
6. **Abonelik ürünleri** (Play Console › Para kazanma › Abonelikler):
   - `minikgurme_plus_monthly` — 1 aylık temel plan, otomatik yenilenen
   - `minikgurme_plus_yearly` — 1 yıllık temel plan, otomatik yenilenen
   - Öneri: aylık ~59,99 TL, yıllık ~399,99 TL; isteğe bağlı 7 gün ücretsiz deneme (teklif olarak).
   - Fiyatlar uygulamaya Play'den gelir; kodda sabit fiyat yok.
7. **Vergi/ödeme profili** (Play Console › Ödemeler) ve gelir için vergi kaydı (şahıs şirketi / serbest meslek) — muhasebecinize danışın.

## 3. Play Console formları (taslak cevaplar)
**Veri güvenliği** (Ebeveyn Topluluğu eklendiği için güncellendi)
- Uygulama kullanıcı verisi topluyor mu? → **Evet** — yalnızca isteğe bağlı topluluk için:
  - *Kişisel bilgiler › Kullanıcı kimlikleri*: anonim Firebase kimliği — amaç: Uygulama işlevselliği, Hesap yönetimi
  - *Kişisel bilgiler › Ad*: takma ad (kullanıcı seçer) — amaç: Uygulama işlevselliği
  - *Mesajlar › Diğer uygulama içi mesajlar*: gönderi ve yorumlar — amaç: Uygulama işlevselliği
  - Hepsi **isteğe bağlı** (kullanıcı topluluğa katılmayı seçerse), **paylaşılmaz** (Google veri işleyen olarak sayılır).
- Bebek profili, kilo/boy, öğün ve alerji kayıtları **toplanmaz** (cihazda kalır).
- Satın alma geçmişi Google Play tarafından işlenir (Google'ın kendi beyanı kapsamında).
- Veriler aktarımda şifreleniyor mu? → **Evet** (Firebase, HTTPS/TLS).
- Kullanıcı veri silme talep edebilir mi? → **Evet**: uygulama içinde Ayarlar › Topluluk › Topluluk hesabımı sil. Play, hesap silme için ayrıca bir **web bağlantısı** da istiyor: gizlilik sayfanıza "Silme talebi için e-posta gönderin: …" bölümü ekleyip o adresi girin.

**Sağlık uygulamaları beyanı**: Kategori "Beslenme ve kilo yönetimi / ebeveynlik". Tıbbi cihaz değildir; teşhis/tedavi sunmaz.

**Hedef kitle ve içerik**: Hedef yaş **18+** (ebeveynler). "Çocuklara yönelik" **işaretlenmemeli**.

**İçerik derecelendirmesi (IARC)**: Şiddet/cinsellik/kumar yok, konum paylaşımı yok. **"Kullanıcılar etkileşime girebilir / içerik paylaşabilir: Evet"** (Ebeveyn Topluluğu). Moderasyon: şikâyet, engelleme, otomatik gizleme ve moderatör silmesi var.

**Kullanıcı tarafından oluşturulan içerik (UGC) politikası**: Uygulamada şunlar mevcut — kullanım koşullarının kabulü (katılırken onay kutusu), uygunsuz içerik/kullanıcı **şikâyet** etme, kullanıcı **engelleme**, 3 şikâyette otomatik gizleme, moderatör onay/silme, kendi içeriğini ve hesabını silme. Şikâyetleri düzenli olarak (en az haftada bir) Firebase konsolundaki `reports` koleksiyonundan kontrol edin.

**Reklam**: Uygulamada reklam **yok**.

**İzinler**: `POST_NOTIFICATIONS` (hatırlatıcılar), `RECEIVE_BOOT_COMPLETED` (yeniden başlatmada hatırlatıcıları kurmak), `BILLING` (Play aboneliği). `INTERNET` ve `ACCESS_NETWORK_STATE` izinlerini Google Play Billing ve Firebase kütüphaneleri ekler: abonelik işlemleri ve isteğe bağlı Ebeveyn Topluluğu için. Bebek verileri hiçbir sunucuya gönderilmez.

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
• Ebeveyn Topluluğu: ay ay konu başlıklarında soru sorun, deneyim paylaşın (takma adla, isteğe bağlı)
Gizlilik: Reklam yok, analiz yok — bebeğinizin tüm kayıtları telefonunuzda kalır; topluluğa yalnızca sizin yazdıklarınız gider.
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
• Parent Community: ask questions and share experiences in month-by-month topics (optional, with a nickname)
Privacy: no ads, no analytics — all of your baby's records stay on your phone; only what you write yourself goes to the community.
Minik Gurme Plus: weekly meal planner, doctor report (PDF), multiple babies, meal reminders.
The app provides general information and does not replace medical advice. Please consult your paediatrician.

**Görseller**: Simge 512×512 (`res/drawable-nodpi/logo_emblem.webp` kaynağından), öne çıkan görsel 1024×500, en az 2 telefon ekran görüntüsü (`minik_gurme/docs/screenshots`).

## 5. Uygulanan güvenlik/uyum değişiklikleri
- Debug anahtarıyla yayın imzası kaldırıldı; anahtar yoksa release imzasız kalır.
- Paket adı ve iç adlar AI Studio kalıntılarından temizlendi.
- WHO medyanları kız/erkek ayrı (0–24 ay); cinsiyet seçilmezse ortalama.
- "Dr. Tavsiyesi" → "İpucu"; "klinik/pediatrik onaylı" izlenimi veren ifadeler yumuşatıldı; WHO rozetleri kaldırıldı.
- İlk açılışta zorunlu "tıbbi tavsiye değildir" onayı; Ayarlar'da Gizlilik, Koşullar, Lisanslar.
- Lisansı belirsiz 4 fotoğraf kaldırıldı.
- Yedekleme kuralları açık; Gizlilik metninde belirtildi. Dışa aktar / geri yükle eklendi.
- Abonelik doğrulaması cihazda; gelir büyüdüğünde sunucu taraflı doğrulama önerilir.


## 6. Ebeveyn Topluluğu (Firebase) kurulumu
Proje: `minik-gurme-f0db0` · Uygulama yapılandırması: `minik_gurme/app/google-services.json`
1. **Authentication › Sign-in method › Anonymous → Etkinleştir.** (Google ile giriş kullanılmıyor; SHA-1 eklemek gerekmez.)
2. **Firestore Database › Veritabanı oluştur** → konum **europe-west3 (Frankfurt)** → **Üretim modu**.
3. **Firestore › Kurallar** sekmesine `minik_gurme/docs/firestore.rules` dosyasının tamamını yapıştırıp **Yayınla**.
4. **Moderatör olmak için**: uygulamada topluluğa katılın → Ayarlar › Topluluk › **Topluluk kimliğim**'e dokunun (kopyalanır) →
   Firestore › Veri › **Koleksiyon başlat**: koleksiyon kimliği `mods`, belge kimliği = kopyaladığınız kimlik, bir alan ekleyin (ör. `ad` = `Özkan`, string) → Kaydet.
   Uygulamayı yeniden açınca gizlenen içeriklerde **Onayla** ve her içerikte **Sil (moderatör)** görünür.
5. **Şikâyetler**: Firestore › `reports` koleksiyonunda kimin, hangi içeriği (`path` = başlık/gönderi[/yorum]), hangi nedenle şikâyet ettiği görülür.
6. **Bütçe**: Ücretsiz Spark planında kalındığı sürece ücret çıkmaz; kota aşılırsa topluluk o gün durur. Blaze'e geçerseniz bütçe uyarısı (ör. 5 $) kurun.
7. **Yazma Plus'a özel** (şu an açık): okumak herkese açık ve hesapsız; beğeni/şikâyet için takma adla katılmak yeterli. Kapatmak için `community/Community.kt` → `CommunityConfig.WRITE_PLUS_ONLY = false`. Not: Plus kontrolü uygulamada yapılır; değiştirilmiş bir uygulamayla aşılabilir. Tam koruma için sunucu taraflı satın alma doğrulaması (Cloud Functions, Blaze planı) gerekir.
8. Kurallar, Firestore emülatöründe 34 senaryoyla test edildi (başkası adına yazma, sahte beğeni, şikâyet temizleme, kendini moderatör yapma vb. engelleniyor).
