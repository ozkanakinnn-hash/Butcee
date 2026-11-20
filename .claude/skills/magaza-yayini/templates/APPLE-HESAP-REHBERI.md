# Ezan & Vakit'i App Store'a çıkarma — adım adım rehber

Mac gerekmez: uygulamayı GitHub'daki Mac sunucusu derleyip imzalar ve App Store Connect'e yükler.
Sizin yapacaklarınız aşağıda. Her adım telefondan yapılabilir. Masaüstü sitesi gereken yerlerde
tarayıcı menüsünden **"Masaüstü sitesi"**ni açın.

**Hazırlayın:**
- İki adımlı doğrulaması açık bir Apple Hesabı
- Kimlik kartı
- Banka bilgileri (IBAN ve SWIFT/BIC)
- Yıllık **99 ABD doları** üyelik ücreti

---

## 1. Apple Developer Program'a katılın

1. iPhone'a App Store'dan **Apple Developer** uygulamasını indirin.
2. Uygulamada **Hesap (Account) → Şimdi Kaydol (Enroll Now)** yolunu izleyin.
3. **Bireysel (Individual)** seçin. App Store'da satıcı adı olarak adınız soyadınız görünür.
   Şirketiniz varsa "Kuruluş" seçeneği de var, ama D-U-N-S numarası ister ve süreç uzar.
4. Kimlik doğrulamasını tamamlayın ve ücreti ödeyin.
5. Onay genellikle 1–2 gün sürer ve e-postayla gelir.

## 2. Ekip Kimliğinizi (Team ID) not alın

**developer.apple.com/account** adresinde **Üyelik ayrıntıları (Membership details)** bölümünde
10 karakterlik **Team ID** yazar (ör. `A1B2C3D4E5`). Bunu 7. adımda kullanacaksınız.

## 3. Uygulama kimliklerini oluşturun (Certificates, Identifiers & Profiles)

Bu adımı GitHub derlemesi kendisi de yapmayı dener. Elle yapmak hatayı önler.
**developer.apple.com/account/resources/identifiers** adresine girin:

1. **"+" → App Groups → Devam**
   - Açıklama: `Ezan Vakit Group`
   - Kimlik: `group.akin.ai2`
   - **Register**'a basın.
2. **"+" → App IDs → App → Devam**
   - Açıklama: `Ezan Vakit`
   - Bundle ID (Explicit): `akin.ai2`
   - Yetenekler (Capabilities) listesinde **App Groups** kutusunu işaretleyin.
   - **Configure**'a basıp `group.akin.ai2` grubunu seçin, ardından **Register**'a basın.
3. **"+" → App IDs → App → Devam**
   - Açıklama: `Ezan Vakit Widget`
   - Bundle ID: `akin.ai2.widget`
   - **App Groups** kutusunu işaretleyip `group.akin.ai2` grubunu seçin, **Register**'a basın.

## 4. App Store Connect: sözleşmeler, vergi, banka

**appstoreconnect.apple.com** adresinde **İş (Business)** bölümünü açın.
Eski adı "Anlaşmalar, Vergi ve Banka"dır.

1. **Ücretli Uygulamalar Sözleşmesi (Paid Apps Agreement)**: **Görüntüle ve Kabul Et**'e basın.
   Premium satışları ve abonelikler bunsuz çalışmaz.
2. **Banka hesabı**: **Ekle**'ye basın ve bilgileri girin.
   - Ülke: Türkiye
   - IBAN ve banka SWIFT kodu
   - Hesap sahibinin adı, Apple hesabındaki adınızla aynı olmalı.
   - Para birimi: hesabınızın birimi (TRY veya USD).
3. **Vergi formları**: **ABD Vergi Formu → W-8BEN** formunu doldurun (ABD dışındaki bireyler için).
   - Vergi kimlik numarası olarak T.C. kimlik numaranızı yazın (Foreign TIN).
   - "Vergi anlaşması avantajı" bölümünde **Türkiye**'yi seçin. Böylece ABD satışlarından kesinti %30 yerine anlaşma oranıyla yapılır.
4. **AB Dijital Hizmetler Yasası (DSA) — tacir durumu**
   - Uygulama reklam ve satın alma içerdiği için **"Tacirim (Trader)"** seçilmelidir.
   - Bu seçimde adres, telefon ve e-postanız AB ülkelerindeki App Store sayfasında herkese açık görünür.
   - Bunu istemiyorsanız 8. adımda AB ülkelerini dağıtım listesinden çıkarabilirsiniz.

## 5. Uygulamayı App Store Connect'te oluşturun

1. **Uygulamalar (Apps) → "+" → Yeni Uygulama (New App)** yolunu izleyin.
2. Formu doldurun:
   - Platform: **iOS**
   - Ad: **Ezan & Vakit: Ramazan Rehberi**. Bu ad alınmışsa: *Ezan & Vakit: Namaz Vakti*.
   - Birincil dil: **Türkçe**
   - Paket kimliği: listeden **akin.ai2** (3. adımda oluşturdunuz)
   - SKU: `ezanvakit-ios-001`
   - Kullanıcı erişimi: Tam erişim
3. **Oluştur**'a basın.

## 6. App Store Connect API anahtarı (GitHub'ın yükleme yapabilmesi için)

1. **Kullanıcılar ve Erişim (Users and Access) → Entegrasyonlar (Integrations) → App Store Connect API** yolunu izleyin.
2. İlk kez giriyorsanız **Erişim İste (Request Access)** düğmesine basıp onaylayın.
3. **Ekip Anahtarları (Team Keys) → "+"** ile yeni anahtar oluşturun:
   - Ad: `GitHub`
   - Erişim: **Admin**. İmza sertifikasını GitHub'ın oluşturabilmesi için Admin şart.
4. Oluşan satırda **İndir (Download)**'a basın ve `AuthKey_XXXXXXXXXX.p8` dosyasını saklayın.
   ⚠️ Bu dosya **yalnızca bir kez** indirilebilir.
5. Sayfanın üstündeki **Issuer ID**'yi ve anahtar satırındaki **Key ID**'yi not alın.

## 7. Bilgileri GitHub'a gizli olarak girin

1. **github.com/ozkanakinnn-hash/Butcee** adresinde **Settings → Secrets and variables → Actions** yolunu izleyin.
2. **New repository secret** ile şu dört sırrı tek tek ekleyin:

| Name | Secret (değer) |
|---|---|
| `APPLE_TEAM_ID` | 2. adımdaki Team ID |
| `ASC_KEY_ID` | 6. adımdaki Key ID |
| `ASC_ISSUER_ID` | 6. adımdaki Issuer ID |
| `ASC_PRIVATE_KEY` | `.p8` dosyasının **tüm metni** (`-----BEGIN PRIVATE KEY-----` satırından `-----END PRIVATE KEY-----` satırına kadar) |

`.p8` dosyasını telefonda bir metin düzenleyiciyle açıp tamamını kopyalayabilirsiniz.
Bu değerleri sohbete yazmayın; yalnızca GitHub'a girin.

## 8. AdMob'da iOS uygulaması (reklam geliri için)

1. **admob.google.com** adresinde **Uygulamalar → Uygulama ekle → iOS** yolunu izleyin.
   - "App Store'da yayında mı?" sorusuna **Hayır** deyin.
   - Ad: Ezan & Vakit
2. Uygulama kimliğini (`ca-app-pub-…~…`) kopyalayın.
3. **Reklam birimleri → Banner** ile bir reklam birimi oluşturup kimliğini (`ca-app-pub-…/…`) kopyalayın.
4. Bu **iki kimliği bana gönderin** (gizli değiller). Ben uygulamaya ekleyeceğim; o zamana kadar uygulamada test reklamı görünür.
5. **Gizlilik ve mesajlaşma** bölümünde iOS için iki mesaj oluşturup yayınlayın:
   - **GDPR** onay mesajı
   - **IDFA açıklama mesajı** (Apple'ın izin sorusundan önce gösterilir)

## 9. Sürüm sayfasını doldurun

Uygulamanın sayfasında **1.0 Gönderime Hazırlanıyor** sürümünü açın. Metinlerin hepsi
`docs/appstore/app-store-metni.md` dosyasında hazır:

- **Uygulama Bilgileri**
  - Kategori, telif, içerik hakları
  - Yaş derecelendirmesi
  - İngilizce yerelleştirme: sağ üstteki dil menüsünden ekleyin
- **Fiyatlandırma ve Kullanılabilirlik**
  - Fiyat: Ücretsiz
  - Ülkeler: tümü (ya da 4. adıma göre AB hariç)
- **Uygulama Gizliliği**
  - Gizlilik politikası adresini girin.
  - Veri türlerini dosyadaki tabloya göre işaretleyin.
  - Ardından **Yayımla (Publish)**'a basın.
- **Uygulama İçi Satın Almalar ve Abonelikler**
  - Üç Premium ürününü oluşturun.
  - Abonelik grubu: `Premium`
- **Sürüm sayfası**
  - Ekran görüntüleri: `docs/appstore/raw/tr` ve `docs/appstore/raw/en` klasörleri
  - Alt başlık, tanıtım metni, açıklama, anahtar kelimeler
  - Destek adresi
  - Uygulama İnceleme bilgileri

## 10. Yükleme ve TestFlight

1. 7. adımı bitirince bana **"sırlar hazır"** yazın. Yüklemeyi ben başlatırım.
   İsterseniz siz de başlatabilirsiniz: GitHub'da **Actions → iOS App Store yükleme → Run workflow**.
2. Yükleme yaklaşık 20–40 dakika sürer. Apple'ın işlemesi 10–30 dakika daha sürer.
3. App Store Connect'te **TestFlight** sekmesinde yapı görünür.
   - Kendi iPhone'unuza **TestFlight** uygulamasını kurun.
   - **Dahili Test** grubuna kendinizi ekleyip uygulamayı deneyin. Dahili test için Apple incelemesi gerekmez.
4. Sorun yoksa sürüm sayfasında şunları yapın:
   - **Yapı (Build)** bölümünden yüklenen yapıyı seçin.
   - Üç Premium ürününü sürüme ekleyin.
   - **İncelemeye Gönder**'e basın.
5. İnceleme genellikle 1–3 gün sürer.
   - "Elle yayınla" seçerseniz onaydan sonra **Yayınla** düğmesine siz basarsınız.
   - Red gelirse mesajı bana iletin, birlikte düzeltelim.

---

### Sık karşılaşılan sorunlar
- **"No profiles for 'akin.ai2' were found"**: 3. adımdaki kimlikleri kontrol edin. API anahtarının **Admin** olduğundan emin olun.
- **"Bundle ID not available"**: Kimlik başka bir hesapta kayıtlı demektir. Bu durumda yeni bir kimlik gerekir; bana söyleyin.
- **Yapı TestFlight'ta "Eksik Uyumluluk" diyor**: Info.plist'te şifreleme beyanı var, normalde sorulmaz. Sorulursa **"Hiçbiri / Muaf"** deyin.
