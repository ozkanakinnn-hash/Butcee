# Minik Gurme — App Store (iOS) yayın rehberi

İlk iOS sürümü **Plus'sız** (`IosStore.PURCHASES_ENABLED = false`): Plus kartı, kilitli Plus satırları ve "Plus'a geç"
düğmeleri iOS'ta gizli; toplulukta yazmak herkese açık. Abonelik ürünleri oluşturulunca bayrak `true` yapılıp 1.0.1 çıkar.

| Alan | Değer |
|---|---|
| Bundle ID | `akinai.minikgurme` (ilk yükleme kendisi kaydeder) |
| SKU | `akinai-minikgurme` |
| Ad (≤30) | **Minik Gurme: Ek Gıda Takibi** |
| Birincil dil | Türkçe (İngilizce yerelleştirme isteğe bağlı) |
| Kategori | Birincil **Health & Fitness** (Sağlık ve Fitness) · İkincil **Food & Drink** (Yemek ve İçecek) |
| Gizlilik URL | https://ozkanakinnn-hash.github.io/Butcee/minik-gurme/gizlilik.html |
| Destek URL | https://ozkanakinnn-hash.github.io/Butcee/minik-gurme/ |
| Fiyat | Ücretsiz · Çin hariç tüm ülkeler |
| Telif | 2026 Özkan Akın |
| Yükleme | `.github/workflows/minik-gurme-ios-release.yml` (Mac'siz; `minik_gurme/iosApp/RELEASE_VERSION` değişince ya da Run workflow) |

## Türkçe metinler

**Alt başlık (≤30):**
```
100 gıda, alerjen ve tarifler
```

**Tanıtım metni (≤170):**
```
Ek gıdaya geçişi kolaylaştıran sade takip: 100 gıda listesi, 3 gün kuralıyla alerjen planı, yaşa uygun tarifler ve büyüme takibi. Reklam yok.
```

**Anahtar kelimeler (≤100):**
```
ek gıda,bebek,blw,püre,alerjen,tarif,mama,büyüme,beslenme,100 gıda,bebek menüsü,emzirme
```

**Açıklama (emoji ve ok işareti yok):**
```
Minik Gurme, 6. aydan itibaren ek gıdaya geçişi kolaylaştıran sade ve şık bir takip uygulamasıdır.

100 GIDA
- 1 yaşına kadar 100 gıda: denediklerinizi işaretleyin
- Tepkileri ve belirtileri not edin
- Yaşa göre servis şekli çizimleri

ALERJEN PLANI
- Sık görülen 9 alerjeni 3 gün kuralıyla sırayla deneyin
- Devam eden denemeler için hatırlatma ve uyarılar

TARİFLER
- Yaşa uygun tarifler, günün menüsü
- Püre ve BLW seçenekleri, porsiyon ayarı, alışveriş listesi
- Tariflerde alerjen bilgisi

REHBER VE BÜYÜME
- Ay ay rehber: kıvam aşamaları, günlük öğün akışı, güvenlik uyarıları
- Kilo ve boy ölçümlerini WHO kız/erkek medyanlarıyla karşılaştırın

EBEVEYN TOPLULUĞU
- Ay ay konu başlıklarında soru sorun, deneyim paylaşın
- Takma adla, isteğe bağlı; şikâyet ve engelleme seçenekleri

Ayrıca: hatırlatıcılar, açık ve koyu tema, Türkçe ve İngilizce, yedekleme.

Gizlilik: Reklam yok, analiz yok. Bebeğinizin tüm kayıtları telefonunuzda kalır; topluluğa yalnızca sizin yazdıklarınız gider.

Uygulama genel bilgi sunar; tıbbi tavsiye yerine geçmez. Lütfen çocuk doktorunuza danışın.
```

## Uygulama gizliliği (App Privacy)
"Veri topluyor musunuz?" → **Evet** (yalnızca isteğe bağlı Ebeveyn Topluluğu için; reklam ve analiz yok).

| Veri türü | Amaç | Kimliğe bağlı | İzleme |
|---|---|---|---|
| **User Content → Other User Content** (gönderi, yorum) | App Functionality | Evet | Hayır |
| **Identifiers → User ID** (anonim Firebase hesabı) | App Functionality | Evet | Hayır |
| **Contact Info → Name** (takma ad) | App Functionality | Evet | Hayır |

Bebek bilgileri (ad, yaş, kilo, boy, tepkiler) **cihazdan çıkmaz** → beyan edilmez.

## Yaş derecelendirmesi
- İçerik soruları: hepsi **None**; **Medical or Treatment Information** → **Infrequent** (genel beslenme bilgisi).
- **User-Generated Content** → **Yes** (topluluk) · Messaging → No · Advertising → No · Unrestricted Web → No.
- Sonuç ne çıkarsa kabul.

## App Review notları (İngilizce)
```
No login is required. All baby records stay on the device; the app has no ads and no analytics.
The optional Parent Community uses an anonymous Firebase account with a nickname. Users can report posts and comments and block users (Settings > Community > Blocked users); reported content is hidden until reviewed by a moderator, and users can delete their posts and their community account in the app.
The app gives general feeding information with sources (Settings > Sources) and states that it does not replace medical advice.
There are no in-app purchases in this version.
```

## Ekran görüntüleri
App Store "medium display" yuvası: **1206×2622** (iPhone 6,3"). Plus ekranı (haftalık menü) iOS görsellerinde kullanılmaz.
