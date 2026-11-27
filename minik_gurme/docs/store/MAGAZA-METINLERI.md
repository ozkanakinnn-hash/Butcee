# Minik Gurme — Google Play mağaza metinleri (kopyala-yapıştır)

Web: https://ozkanakinnn-hash.github.io/Butcee/minik-gurme/
Gizlilik politikası: https://ozkanakinnn-hash.github.io/Butcee/minik-gurme/gizlilik.html
Hesap/veri silme: https://ozkanakinnn-hash.github.io/Butcee/minik-gurme/hesap-silme.html
İletişim e-postası: ozkan.akinnn@gmail.com
Kategori: Ebeveynlik · Etiketler: bebek, beslenme, ek gıda, tarif

## Türkçe (varsayılan)
**Uygulama adı (≤30):** Minik Gurme: Ek Gıda Takibi

**Kısa açıklama (≤80):** Ek gıda yolculuğu: 100 gıda, alerjen planı, tarifler ve büyüme takibi.

**Tam açıklama (≤4000):**
Minik Gurme, 6. aydan itibaren ek gıdaya geçişi kolaylaştıran sade ve şık bir takip uygulamasıdır.

• 1 yaşına kadar 100 gıda: denediklerinizi işaretleyin, tepkileri ve belirtileri not edin
• Alerjen tanıtım planı: sık görülen 9 alerjeni 3 gün kuralıyla sırayla deneyin; tanıtılan alerjenleri düzenli vermeniz için hatırlatma
• Yaşa göre servis şekilleri: her gıdanın nasıl kesilip sunulacağını çizimlerle görün
• 35 yaşa uygun tarif (6–12+ ay): günün menüsü, püre ve BLW seçenekleri, alerjen uyarısı, porsiyon ayarı, alışveriş listesi
• Ay ay rehber: kıvam aşamaları, günlük öğün akışı ve güvenlik uyarıları
• Büyüme takibi: kilo ve boy ölçümlerinizi WHO kız/erkek medyanlarıyla karşılaştırın
• Ebeveyn Topluluğu: ay ay konu başlıklarında soru sorun, deneyim paylaşın (takma adla, isteğe bağlı)
• Hatırlatıcılar, yedekleme/geri yükleme, açık ve koyu tema, Türkçe ve İngilizce
• Sağlık konularında kaynak bağlantıları (DSÖ, ESPGHAN, T.C. Sağlık Bakanlığı, AAP)

Gizlilik: Reklam yok, analiz yok. Bebeğinizin tüm kayıtları telefonunuzda kalır; topluluğa yalnızca sizin yazdıklarınız gider.

Minik Gurme Plus: haftalık menü planlayıcı, doktor raporu (PDF), birden fazla bebek, öğün hatırlatıcıları ve toplulukta gönderi yazma.

Uygulama genel bilgi sunar; tıbbi tavsiye yerine geçmez. Lütfen çocuk doktorunuza danışın.

## English
**App name (≤30):** Minik Gurme: Baby Food Tracker

**Short description (≤80):** Starting solids: 100 foods, allergen plan, recipes and growth tracking.

**Full description (≤4000):**
Minik Gurme is a simple, beautiful tracker that makes starting solids easier from 6 months.

• 100 foods before age 1: tick what you've tried and note reactions and symptoms
• Allergen introduction plan: introduce the 9 common allergens one by one with the 3-day rule, with reminders to keep introduced allergens in the diet
• Serving shapes by age: see how to cut and serve each food with simple drawings
• 35 age-appropriate recipes (6–12+ months): daily menu, purée and BLW options, allergen notes, portion scaling, shopping list
• Month-by-month guide: texture stages, daily meal flow and safety tips
• Growth tracking: compare weight and height with WHO girl/boy medians
• Parent Community: ask questions and share experiences in month-by-month topics (optional, with a nickname)
• Reminders, backup and restore, light and dark theme, Turkish and English
• Source links on health topics (WHO, ESPGHAN, Turkish Ministry of Health, AAP)

Privacy: no ads, no analytics. All of your baby's records stay on your phone; only what you write yourself goes to the community.

Minik Gurme Plus: weekly meal planner, doctor report (PDF), multiple babies, meal reminders and posting in the community.

The app provides general information and does not replace medical advice. Please consult your paediatrician.

## Görseller (docs/store)
- Simge: `icon-512.png`
- Öne çıkan görsel: `tr/feature-1024x500.png`, `en/feature-1024x500.png`
- Telefon ekran görüntüleri (1080×1920): `tr/phone-1..7.png`, `en/phone-1..7.png` (7: Haftalık menü planlayıcı)
- Öne çıkan görsel uygulama ekranı ve besin çizimleri içerir; 7. ekran görüntüsü Plus özelliğini (haftalık menü) gösterir
- Yeniden üretmek: `./gradlew :app:testDebugUnitTest --tests '*StoreScreenshotsTest'` → `python3 tool/store_graphics.py`

## Tanıtım görselleri (docs/store/promo/tr, docs/store/promo/en)
| Dosya | Boyut | Nerede kullanılır |
|---|---|---|
| `tanitim-1920x1080.png` | 16:9 | Play Console › Tanıtım içeriği / Etkinlik görseli, YouTube kapak |
| `yatay-1200x628.png` | 1.91:1 | Google Ads uygulama kampanyası (yatay), Facebook/LinkedIn bağlantı görseli |
| `kare-1200x1200.png` | 1:1 | Google Ads (kare), Instagram gönderisi |
| `dikey-1200x1500.png` | 4:5 | Google Ads (dikey), Instagram/Facebook akışı |
Yeniden üretmek: `python3 tool/promo_graphics.py` (önce StoreScreenshotsTest).
