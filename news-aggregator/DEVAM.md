# Haber Akışı — devir notu (yeni sohbet buradan devam etsin)

Yeni sohbete şunu yaz: **"news-aggregator/DEVAM.md dosyasını oku ve oradan devam edelim."**

Son güncelleme: 13 Kasım 2026.

---

## 1. Proje nedir, nerede

| | |
|---|---|
| Uygulama | Flutter — `news-aggregator/flutter_app` |
| Paket adı | **`akinai.app1`** (değiştirilemez; sonraki uygulamalar `akinai.app2`, `akinai.app3`…) |
| Uygulama adı | Haber Akışı |
| Firebase projesi | `haber-68a3c` (Blaze), bölge `europe-west1` |
| Sunucu kodu | `news-aggregator/firebase/functions` (Node.js 22) |
| Kod deposu | Bitbucket **`ozkan45/haber-akisi`** (dal `main`, uygulama `haberler/` klasöründe) |
| Yedek depo | GitHub `ozkanakinnn-hash/Butcee`, dal **`claude/haber-akisi-2026-11`** (16 Kas 2026'da açıldı). ⚠️ Oradaki eski `ccr-*` dallarının geçmişi buradan **bağımsız**; o dallara zorla gönderme yapma. |
| Yasal site | https://haber-68a3c.web.app (Firebase Hosting, `firebase/hosting/public`) |
| Beceri notları | `.claude/skills/flutter-firebase-app/SKILL.md` — öğrenilen dersler, kullanıcı tercihleri |

**Zamanlanmış işler (Cloud Functions):** `fetchRssFeeds` (20 dk), `cleanupOldArticles` (04:00),
`footballTick` (1 dk). Gizli anahtarlar: `HIGHLIGHTLY_API_KEY`, `FOOTBALL_API_KEY`.
⚠️ GitHub Actions'ta **zamanlanmış görev çalıştırma** (hesap bir kez askıya alındı).

---

## 2. Bu kapta olan, depoda OLMAYAN dosyalar
Yeni bir oturumda bunlar yok; kullanıcıdan istenmeli:

| Dosya | Ne için | Nereden |
|---|---|---|
| `flutter_app/android/app/google-services.json` | Firebase bağlantısı (derleme) | Firebase Console → Proje ayarları → `akinai.app1` |
| `flutter_app/android/akinai-app1-upload.jks` + `android/key.properties` | Play yükleme imzası | Kullanıcıda: **akinai-app1-yukleme-anahtari.zip** |

Yükleme anahtarı parmak izi (SHA-256):
`B6:1C:F6:EC:EF:EB:D2:37:B3:ED:2B:44:7A:E1:27:50:2D:B0:DD:60:0F:5D:33:52:87:EB:43:9E:BE:3B:53:76`

---

## 3. Nasıl çalışılır (bu ortamda denenmiş)

```bash
export PATH=<scratchpad>/flutter/flutter/bin:$PATH     # Flutter 3.47.9

# Uygulama
cd news-aggregator/flutter_app
flutter analyze && flutter test                        # 31 test
flutter build apk --release --split-per-abi --target-platform android-arm64 --build-number=NN
flutter build appbundle --release                      # Play için (sürüm kodu 100'den devam)

# Sunucu
cd news-aggregator/firebase/functions
npx -y firebase-tools@15 emulators:exec --only firestore --project demo-haber "npm test"   # 83 test

# Depoya gönderme (GitHub yerine Bitbucket)
news-aggregator/tool/push-haberler.sh "mesaj"
```

**Kullanıcı sunucuyu Cloud Shell'den yüklüyor.** Paket: `git archive --format=zip -o Haber-Akisi-kodNN.zip HEAD news-aggregator`
```
cd ~ && rm -rf haber && unzip -o "Haber-Akisi-kodNN.zip" -d haber && cd haber/news-aggregator/firebase \
  && (cd functions && npm ci --omit=dev) \
  && npx -y firebase-tools@15 deploy --only functions,firestore,hosting --project haber-68a3c
```

---

## 4. Durum: ne bitti

**Yayın öncesi temizlik**
- Investing.com kaynakları kaldırıldı (ticari yeniden yayın yasak); mevcut kurulumda bir kerelik kapanıyor.
- Piyasa paneli: döviz **TCMB** gösterge kurlarından; BIST 100 ve Brent kaldırıldı (lisans); "yatırım tavsiyesi değildir" notu.
- MET Norway atfı (CC BY 4.0) görünür; hava durumu konumu ≈1 km'ye yuvarlanıyor.
- Yasal sayfalar yayında: `/gizlilik`, `/privacy`, `/kvkk`, `/kullanim-kosullari`, `/kunye` (Özkan Akın, haberakisinfo@gmail.com, +90 540 511 55 70, Saruhanlı / Manisa).
- Crashlytics + App Check (Play Integrity) eklendi. ⚠️ App Check "Zorunlu kıl" **henüz açılmayacak** (Play'den dağıtım başlayınca).

**Marka**
- Logo: lacivert + kırmızı canlı nokta (`docs/brand/logo.svg`, `play-ikon-512.png`); uygulama paleti lacivert
  (#0B1F3A / vurgu #16345F, koyu temada #7FA6E3), kırmızı (#D62828) yalnızca son dakika/canlı.

**Bildirimler — "az ama önemli"**
- Önem = aynı olayı veren farklı yayıncı sayısı (3+); kritik haberler (deprem/afet/savaş) tek kaynakla gider.
- Tıklama tuzağı/magazin başlıkları bildirime girmez.
- 3 seviye: Sadece önemli / **Dengeli (varsayılan)** / Tümü + günde 3 bülten (08:30, 13:00, 19:00).
- Bildirimde "Daha az bildirim" düğmesi; gece 00:00–08:00 sessiz.

**Canlı skor paneli (kullanıcı onaylı tasarım)**
- Kilit ekranında görünür; aşağı kaydırınca zaman çizelgesi: goller, sarı/kırmızı kartlar, "İLK YARI" ayırıcısı.
- Dakika futbol biçiminde ("62'", "45+2'"), telefonda her dakika ilerler.
- Sesler: gol → telefonun sesi; **başlama, devre arası, maç sonu, kırmızı kart → düdük** (5g, `res/raw/whistle_kickoff.ogg`);
  sarı kart sessiz. Ayrı bildirim kartı YOK — panelin kendisi sesli uyarır (panel kapalıysa normal bildirim).
- Türkiye milli maçları: futbol ilgisi seçili + panel açık herkese (`national_tr`); bayraklar 🇹🇷 🇧🇪; penaltılar yalnızca panelde.
- Maçtan 10 dk önce panel başlama saatiyle gelir.

**Play Store hazırlığı** — `docs/play-store/`
- `YUKLEME_REHBERI.md` (tüm form cevapları, veri güvenliği tablosu, 12 testçi × 14 gün süreci)
- `magaza-girisi.md` (TR/EN metinler, karakter sınırları içinde)
- `one-cikan-gorsel-1024x500.png`, `brand/play-ikon-512.png`
- `tanitim-gorselleri/` — 7 adet 1080×1920, **uygulamanın gerçek ekranlarından** üretildi
  (`flutter_app/tool/promo/capture.dart` + `yakala.sh` + `olustur.js`; yöntem `BENIOKU.md`'de)
- **AAB hazır:** sürüm 1.0.0+100, yükleme anahtarıyla imzalı (kullanıcıda `Haber-Akisi-1.0.0-play.aab`).

---

## 5. Durum: ne bekliyor

### Devir notu — oturum session_01Q1LBDnWcxQXWdkeUuDQJoB (27 Kasım 2026)
⚠️ Bu dalda **iki Claude oturumu paralel çalıştı**. Göndermeden önce her zaman `git pull` yap; çakışmada iki tarafı da koru.

**Bu oturumun yaptıkları (dalda, testli):**
- **Kitaplığım (1.0.1):** `lib/services/library_store.dart` (kaydedilenler ≤300, okuma geçmişi ≤200, gizlenen kaynaklar — yalnızca
  cihazda, SharedPreferences `library_*`), `lib/pages/library_page.dart` (Kaydedilenler / Okuma geçmişi / Gizlenen kaynaklar),
  `lib/widgets/article_actions.dart` (openArticle → geçmişe ekler; kart ⋮ menüsü: Kaydet, Paylaş, "… haberlerini gösterme"),
  `lib/widgets/offline_banner.dart` (ağ hatası + önbellekten haber varsa "Çevrimdışısınız" şeridi). Okunan haber akışta soluk,
  kayıtlı haberde küçük yer imi işareti; okurken üst barda Kaydet. Menü ve Ayarlar → **Kitaplığım** girişleri.
  `ArticlesRecord.reference` artık nullable + `id` alanı + `ArticlesRecord.local(...)`.
- **Canlı maç kartı** (`lib/widgets/live_matches.dart`): tam genişlik, alt alta; ortada üstte durum (CANLI 67'), altında skor;
  goller (dakika + oyuncu, (P)/(KK)) golü atan takımın altında; **takım adı yerine yalnızca kısaltma** + takım rengi noktası
  (tam ad ekran okuyucuya). Kullanıcı önizlemeyle onayladı.
- Testler: `test/library_test.dart`, `features_test.dart` (gol yerleşimi). Önizleme ekranları: `tool/promo/capture.dart`
  10-kaydedilenler … 16-menu (örnek kitaplık verisi `_libraryPrefs`, `_OfflineRepo`).
- `prefetchArticleImages` hatası artık akışı "çevrimdışı" göstermez.
- App Review: Haber ve Minik Gurme için **gerçek iPhone (TestFlight) kaydı** ile 2.1 cevabı verildi (Haber 21 Kasım ~15:00
  Waiting for Review, yapı 7 — sonra diğer oturum yapı 11'i gönderdi). İş akışları: `news-ios-review-reply.yml`
  (push'ta not+ek günceller; yeniden gönderim API'den 409 veriyor → kullanıcı UI'da Reply + Resubmit).

**Yarım kalan iş: Android AAB**
- Kullanıcı "Android AAB üret" dedi. Gerekenler: `android/app/google-services.json` (kullanıcı yükledi; bu kapta
  `/root/.claude/uploads/…/881a7509-google-services.json`, gitignore'da) + **akinai-app1-yukleme-anahtari.zip** (HENÜZ GELMEDİ —
  içinden `android/akinai-app1-upload.jks` + `android/key.properties`; şifre sohbete yazdırılmaz).
- Sürüm: pubspec artık **1.0.2+123** (diğer oturum: "110 Play'de kullanılmış", 120 ve 123'e çıktı). Yeni AAB kodu Play'deki en
  yüksek koddan BÜYÜK olmalı — kullanıcıya Play'de son yüklenen kodu sor; 123 yüklendiyse 124'e çıkar.
- Derleme (denendi, test anahtarıyla başarılı, 67 MB): Flutter 3.50.1 (scratchpad) + `--android-skip-build-dependency-validation`
  (proje AGP 9.1.0, Flutter 3.50 9.1.1 istiyor), `ANDROID_HOME=/root/android-sdk`, `android/local.properties` sdk.dir.
  Maven Central ara sıra 429 veriyor → 60 sn arayla tekrar dene.
- Gerçek reklam kimlikleri: `docs/play-store/admob.md` (ADMOB_APP_ID ortam değişkeni + 3 `--dart-define`).
- İmza doğrulama: yükleme anahtarı SHA-256 `B6:1C:F6:EC:…:53:76` (§2) ile `keytool -printcert -jarfile` çıktısını karşılaştır.

### iOS bildirimleri ÇÖZÜLDÜ (21 Kasım 2026 akşam)
- **1.0.0 yapı 11 incelemeye gönderildi** (yapı 6 geri çekildi: imzada push yetkisi yoktu → iPhone'a hiç bildirim gitmiyordu).
- Kök nedenler (3): imzada `aps-environment` yok (iş akışı artık yeniden imzalıyor + doğruluyor), APNs anahtarı yalnızca Sandbox'tı
  (yeni anahtar "Haber Akisi Push 2" = Sandbox & Production), Firebase'de anahtar yalnızca Development yuvasındaydı (Production'a da yüklendi).
- Uygulamada Ayarlar → **Bildirim tanılama** (izin, APNs/FCM jetonu, konular, son hata, yeniden abone ol).
- Sunucu teşhis aracı: `scripts/mac-teshis.js` (`--duzelt`, `--gonder`, `--deneme <konu>`, `--jeton <fcm>`); Cloud Shell'de `~/haber-kod` (sparse clone).
- iPhone canlı skor: Live Activities YOK; tek bildirim `apns-collapse-id` ile yerinde güncellenir (kod33/kod34 — benim paketlerim;
  diğer oturumun "kod33"ü X otomasyonuydu, isim çakıştı). **kod34 yüklendi mi teyit et.**
- Apple'daki eski "Haber Akisi Push" (Sandbox) anahtarı iptal edilebilir.
- Küçük iş: tanılamadaki "Son hata" başarılı abonelikten sonra temizlenmiyor.

### iOS (20 Kasım 2026)
- **1.0.0 (yapı 6) App Store Connect'e yüklendi** — `.github/workflows/news-ios-release.yml` (Mac'siz, Codemagic'siz; macos-15-intel).
  Yeniden yüklemek: `flutter_app/ios/RELEASE_VERSION` değiştir ya da Actions'tan workflow_dispatch (ref: bu dal).
- Bundle `akinai.app1` (Push açık) ve Firebase iOS uygulaması otomatik oluşturuldu; APNs anahtarı Firebase'e yüklendi (kullanıcı).
- App Store kaydı oluşturuldu (Apple ID 6837488791). 21 Kasım: App Information, yaş (4+), Content Rights, fiyat (Free), App Privacy, sürüm 1.0.0 metinleri, inceleme notu, yapı 6 tamam; 9 görsel (1206×2622) gönderildi. Çin listeden çıkarılıyor (güvenlik değerlendirmesi şartı).
  **21 Kasım 04:15 incelemeye gönderildi (1.0.0 yapı 6), Çin hariç.** Sonucu bekleniyor; red gelirse mesajı al, 4.2 (minimum işlevsellik) riskine karşı inceleme notu hazır.
- Eski bekleyenler: sürüm sayfası (metinler `docs/ios/HAZIRLIK.md`, görseller `docs/ios/ekran-goruntuleri/`),
  AdMob iOS kimlikleri (gelene kadar test reklamı), TestFlight denemesi, incelemeye gönderme.

### X otomasyonu (kod33)
- `social.js`: `x_enabled` + `x_webhook` → Make (Webhook → Google Tablolar → filtre deneme=false → Buffer → X). Belge: `docs/SOSYAL_PAYLASIM.md`.
- **kod33 yüklendi** (21 Kasım 04:03, Cloud Shell, Deploy complete).
- `notifyBreakingNews` fonksiyonu projede var ama depoda YOK (kaynağı bilinmiyor, günde ~600 çalışma); silme sorusuna N denildi. Çift bildirim olursa incele.
- Make: tek webhook `haber-x` (eski webhook'lar silindi).


### ⚠️ Önce bakılacaklar
1. **Son sunucu paketi yüklendi mi?** Kullanıcıya gönderilen son paket **kod27** idi
   (goller/kartlar doğru takımın altında). 12 Kasım akşamı kod17 civarına kadar yüklendiği doğrulandı;
   kod26/kod27'nin yüklenip yüklenmediği **belirsiz** → kullanıcıya sor, gerekirse yeni paket üret.
2. **Bitbucket API jetonu 16 Kasım 2026.da DOLDU** → `push-haberler.sh` çalışmıyor; yenilenmeli (güvenli form ile).
   (Atlassian → Security → API tokens, "API token with scopes", read/write:repository:bitbucket.)
3. **v33'ten sonra uygulamada değişiklikler var** (görsel önbelleği ve hava servisi testte değiştirilebilir,
   bildirim izni Firebase'siz çökmüyor) → bir sonraki APK **v34** olmalı.

### Kullanıcının yapacakları
- **Play Console hesabı onayı bekleniyor** → onaylanınca `YUKLEME_REHBERI.md` adım adım uygulanacak.
- 12 test kullanıcısı toplanması (kapalı test, 14 gün).
- **AdMob gerçek kimlikleri**: uygulama kimliği (`~` içeren), 3 reklam birimi (banner / yerel / geçiş), yayıncı kimliği
  → sonra yeni AAB + siteye `app-ads.txt`.
- Play'de ilk yüklemeden sonra **uygulama imzalama anahtarı SHA-256**'yı Firebase'e ekleme (App Check için).
- Avukat: gizlilik/KVKK/kullanım koşulları incelemesi. Mali müşavir: GVK mük. 20/B vs ticari kazanç.
- Highlightly: gol anında bildirim için Pro plan (~19 $/ay) kararı — ücretsiz planda skor ~1,5 dk gecikmeli.

### Yapılacak geliştirme işleri
- Gerçek AdMob kimlikleriyle derleme yapılandırması + `app-ads.txt`.
- Depo temizliği (FlutterFlow kalıntıları, eski GitHub iş akışları, ios taslağı) + Codemagic derleme hattı.
- İsteğe bağlı: gol sesi (g1a/g1b/g1c hazır, şimdilik sistem sesi kullanılıyor), mağaza görsellerinin İngilizcesi.

---

## 6. Kullanıcının kuralları (unutma)
- **"Gönder" demeden APK gönderme.**
- Büyük değişiklikten önce **ne anladığını yaz ve onay al** (gerekirse görsel taslak).
- Telifli marka/ses taklidi yok (ör. "Maçkolik gol sesi" reddedildi); sesler sıfırdan üretilir.
- Gerçek yayıncı/oyuncu adları tanıtım görsellerinde kullanılmaz.
- Haber Akışı yalnızca `ozkan45/haber-akisi` deposuna gönderilir ("Ramazan rehperi" deposu başka proje).
- Anahtar/jeton sohbete yazdırılmaz; kullanıcıya güvenli form ya da Cloud Shell üzerinden alınır.
- Yanıtlar Türkçe.
