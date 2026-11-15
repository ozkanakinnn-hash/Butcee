# Minik Gurme — Hata Ayıklama, Rakip Analizi ve Sadeleştirme (15.11.2026)

## 1. Hata ayıklama — bulunan ve düzeltilenler
| # | Sorun | Etki | Durum |
|---|---|---|---|
| 1 | Rehber › "Sıradaki denenecek süper besinler" **zaten denenmiş** besinleri gösteriyordu (her zaman listenin ilk 4'ü) | Yanlış yönlendirme | ✅ Denenenler hariç; hepsi denendiyse tebrik mesajı |
| 2 | "Günlük Öğün Akışı" **her ay aynıydı** (6. ayda bile "kıymalı ezme + yoğurt") — koda gömülüydü | Yaşa uygun olmayan bilgi | ✅ Artık her ayın kendi akışı veriden geliyor (6–12. ay) |
| 3 | 100 Gıda › "Sonra bunu dene" bebeğin yaşını dikkate almıyordu | Yaşa uygun olmayan öneri | ✅ Önce yaşa uygun denenmemiş besin |
| 4 | Besin kartında uzun adlar kesiliyordu ("Tatlı Patates" → "Tatlı") | Görünüm | ✅ 2 satır |
| 5 | "Limon, demir emilimini **3 katına çıkarır!**" — kanıtlanmamış, abartılı sağlık iddiası | Hukuki risk | ✅ "artırır" + C vitamini kaynakları |
| 6 | Açılış alıcısı gelen olayı kontrol etmiyordu (lint güvenlik uyarısı) | Güvenlik | ✅ Yalnızca sistem olayları |
| 7 | Saat dilimi / saat değişince hatırlatıcılar kayabiliyordu | Bildirim yanlış saatte | ✅ Değişimde yeniden kurulur |
| 8 | Kullanılmayan kod/kaynaklar (eski renkler, kullanılmayan dil kartı) | Temizlik | ✅ Silindi |

Lint: **0 hata**; kalan uyarılar yalnızca "daha yeni kütüphane sürümü var" türünde. Testler: 56/56 geçti. iPhone derlemesi: geçti.
Yapılmadı: Android **release (R8 küçültmeli)** sürüm iOS hazırlığından beri telefonda denenmedi → Play'e yüklemeden önce "AAB derle" ile birlikte bir kez denenmeli.

## 2. Rakipler
| Uygulama | Dil | Öne çıkan | Fiyat modeli |
|---|---|---|---|
| **Solid Starts** (pazar lideri, 4.9★ / ~30 bin yorum) | EN | 300+ besinlik veritabanı: yaşa göre **nasıl kesilir/servis edilir fotoğraf + video**, boğulma/alerjen notları, 100 besin programı, tarif kütüphanesi, besin takibi | Ücretsiz veritabanı + abonelik (yıllık ~100 $) — "pahalı" şikâyeti çok |
| **Huckleberry** | EN | Uyku + emzirme + bez + katı gıda tek uygulamada; Plus'ta "en sevdiği besinler" ve "alerjen/hassasiyet" raporları | Ücretsiz + Plus |
| **BabyBites** | **TR** | 400+ besin, yaşa uygun tarifler, menü planlayıcı, büyüme, 3 gün kuralıyla alerji takibi, BLW rehberi | Freemium |
| **Minik Lokmalar** | **TR** | Tarifler, tariflerde alerjen etiketi, favoriler | — |
| BLW Meals, Avo, Grubsy, Tummi, Pippin | EN | 100+ tarif, ilk besin/alerjen takibi; Grubsy: hazır olma belirtileri, 5–24 ay kilometre taşları | Freemium |
| Mucize Bebek, İlkadımlarım, Momy | **TR** | Genel bebek takibi: emzirme/biberon, uyku, bez, aşı, diş, büyüme | Freemium |

### Sizin güçlü yanlarınız (rakiplerde az olan)
Türkçe + yerel mutfak tarifleri · 100 Gıda + 3 gün kuralı + alerjen planı **bir arada** · DSÖ büyüme eğrisi · **Doktor raporu PDF** · çoklu bebek · haftalık menü + alışveriş listesi · Ebeveyn Topluluğu · veriler telefonda (gizlilik) · tariflerde otomatik alerjen etiketi · TR/EN.

## 3. Sizde olmayanlar — eklenebilirler (öncelik sırasıyla)
| Öncelik | Özellik | Neden | Nasıl yapılır | Emek |
|---|---|---|---|---|
| ⭐⭐⭐ | **Yaşa göre "nasıl servis edilir" çizimleri** | Solid Starts'ın 1 numaralı özelliği; metin zaten var (6/9/12. ay) | Her besine fotoğraf yerine **8–10 ortak çizim** (parmak çubuk, ezme, rendelenmiş, küçük lokma, yassı köfte…) — SVG → webp; `FoodDirectoryItem`'a `serveShape6m/9m/12m` alanı; besin detayında çizim + metin | Orta |
| ⭐⭐⭐ | **Alerjen "sürdürme" takibi** | Güncel kılavuzlar: tanıtılan alerjen (yumurta, yer fıstığı…) **haftada 2–3 kez** verilmeye devam etmeli; rakiplerde yok | Alerjen "Güvenli" olunca sürdürme listesine eklenir; son verilme tarihi öğün kayıtlarından hesaplanır; 7 günü geçerse kartta turuncu uyarı + isteğe bağlı bildirim | Az |
| ⭐⭐⭐ | **İlk açılışta risk sorusu** ("şiddetli egzama / bilinen gıda alerjisi var mı?") | Kılavuzlara göre yüksek riskli bebekte alerjen tanıtımı doktorla planlanmalı; hukuki koruma | Onboarding'e evet/hayır; "evet" ise alerjen planı ve ilgili tariflerde kırmızı "Önce doktorunuza danışın" kutusu | Az |
| ⭐⭐ | **Ana ekran widget'ı** | Günlük kullanımı artırır | Android: Jetpack **Glance** (Compose benzeri) — "Bugün: 2 öğün · Yumurta 2. gün · Sonra: Bal kabağı" + "Öğün ekle" düğmesi; iOS: **WidgetKit** (Swift) + App Group ile paylaşılan küçük JSON | Orta |
| ⭐⭐ | **Beslenme raporu / istatistik** | Huckleberry Plus'ın satan özelliği | Mevcut kayıtlardan: en sevilenler, reddedilenler, besin grubu çeşitliliği ("gökkuşağı": yeşil/turuncu/kırmızı…), haftalık öğün sayısı grafiği. Yeni veri gerekmez; Plus'a eklenebilir | Az–Orta |
| ⭐⭐ | **Reddedilen besini yeniden sunma hatırlatması** | Bebek bir besini 8–15 denemede kabul edebilir | Tepki "Sevmedi" olan besinler için 3–4 gün sonra "Tekrar deneyin" kartı/bildirimi; deneme sayacı | Az |
| ⭐⭐ | **Fotoğraflı öğün günlüğü** | Ebeveynler anı saklamayı seviyor | Android Photo Picker / iOS PHPicker; uygulama içi klasöre küçültülmüş kopya; öğün kartında küçük resim; yedeğe dahil (zip) | Orta |
| ⭐⭐ | **Plus için 7 günlük ücretsiz deneme** | Rakiplerin hepsi kullanıyor, dönüşümü artırır | Play Console / App Store Connect'te abonelik "teklif"i (free trial); kod tarafında yalnızca paywall metni | Az |
| ⭐ | **Süt kaydı** (emzirme/biberon/mama, ml) | Genel takip uygulamalarında var; ek gıda döneminde süt hâlâ ana besin | Yeni Room tablosu `MilkFeed(saat, tür, ml/dk)`, Bugün ekranına hızlı düğme, günlük toplam; DB sürüm artışı + göç | Orta |
| ⭐ | **Eşle paylaşım** (iki telefonda aynı bebek) | Sık istenir | Firestore'da aile kodu ile eşitleme. ⚠ Veri artık telefondan çıkar → gizlilik politikası, KVKK açık rızası ve veri güvenliği formu değişir | Yüksek |
| ⭐ | **Genel arama** (besin + tarif + rehber tek kutuda) | Bilgi çok, bulmak zor | Üst bara arama simgesi; üç veri kaynağında ad/etiket araması, sonuçlar gruplu | Az–Orta |
| — | Uyku, bez, aşı, diş takibi | Rakip genel takip uygulamalarında var | **Önermiyorum**: odak dağılır; Minik Gurme'nin farkı "ek gıda uzmanı" olması | — |

## 4. Kaldırılması / sadeleştirilmesi önerilenler
| # | Ne | Sorun | Öneri |
|---|---|---|---|
| 1 | **Rehber sekmesinde 3 kat sekme** (Bu Ay/Rehber → Beslenme Önerileri/3 Gün Kuralı → ay seçici) | Kullanıcı nerede olduğunu kaybediyor | **3 Gün Kuralı**'nı (bir takip özelliği) 100 Gıda sekmesine taşı; Rehber yalnızca bilgi olsun |
| 2 | Rehber'deki büyük renkli **"Defne İçin Akıllı Öneriler"** başlık kartı | Yer kaplıyor, içerik yok; "akıllı öneri" ifadesi de "tavsiye değildir" çizgisiyle çelişiyor | Kaldır; yerine tek satır "8. Ay" başlığı |
| 3 | Rehber'deki **"Sıradaki denenecek süper besinler"** | 100 Gıda'daki "Sonra bunu dene" ile aynı iş | Birini bırak (100 Gıda'daki) |
| 4 | **Tarifler'de iki sıra filtre** (11 yaş çipi + 5 tür çipi) | Ekranın üçte biri filtre | Varsayılan "Bebeğime Uygun"; diğerleri tek "Filtrele" düğmesiyle açılan alt sayfada |
| 5 | Tarif kartında **"Bebeğinize Uygun"** rozeti | "Bebeğime Uygun" filtresindeyken her kartta aynı rozet | Yalnızca "Tüm Yaşlar"da göster |
| 6 | **"Günün Menüsü"** kartı + Plus **Haftalık planlayıcı** | İki ayrı menü sistemi | Günün menüsü = planlayıcının bugünü (Plus değilse rastgele) |
| 7 | **Üst bardaki alışveriş sepeti** simgesi | Üst bar kalabalık (logo, tarih, sepet, bebek, ayarlar); sepet nadiren kullanılır | Planlayıcı ve tarif detayından erişilsin, üst bardan kalksın |
| 8 | 100 Gıda kartındaki **ⓘ simgesi** | Kart zaten dokunulabilir; küçük hedef | Kaldır, tüm kart detay açsın |
| 9 | Ayarlar › **"Topluluk kimliğim — moderatör eklemek için kopyalayın"** | Yalnızca sizin (yönetici) işinize yarar; kullanıcıya anlamsız | Yalnızca test sürümünde veya moderatöre göster |
| 10 | Ayarlar › **"Alışveriş listesini temizle"** | Yanlış yerde | Alışveriş listesi ekranına taşı |
| 11 | Hoş geldiniz ekranı: "**Önerileri** bebeğinize göre hazırlayabilmemiz için…" | "Tavsiye değildir" diliyle çelişiyor | "Bilgileri bebeğinize göre gösterebilmemiz için…" |
| 12 | Metin içi emojiler ("Bebeğime Uygun 👶", "🍽 Karma") + 3D görseller karışık | Görsel tutarsızlık | Ya hep 3D görsel ya hiç; düğme metinlerinde emoji kullanma |

Kaynaklar: Solid Starts App Store sayfası (apps.apple.com/app/1564189151), appshunter.io, Huckleberry Plus yardım sayfası (huckleberry.zendesk.com), BabyBites ve Minik Lokmalar listeleri (mwm.ai), Grubsy App Store sayfası. Rakip bilgileri mağaza açıklamalarından alınmıştır; fiyat ve özellikler değişmiş olabilir.
