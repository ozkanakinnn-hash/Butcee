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

### iOS (20 Kasım 2026)
- **1.0.0 (yapı 6) App Store Connect'e yüklendi** — `.github/workflows/news-ios-release.yml` (Mac'siz, Codemagic'siz; macos-15-intel).
  Yeniden yüklemek: `flutter_app/ios/RELEASE_VERSION` değiştir ya da Actions'tan workflow_dispatch (ref: bu dal).
- Bundle `akinai.app1` (Push açık) ve Firebase iOS uygulaması otomatik oluşturuldu; APNs anahtarı Firebase'e yüklendi (kullanıcı).
- App Store kaydı oluşturuldu. Bekleyen: sürüm sayfası (metinler `docs/ios/HAZIRLIK.md`, görseller `docs/ios/ekran-goruntuleri/`),
  AdMob iOS kimlikleri (gelene kadar test reklamı), TestFlight denemesi, incelemeye gönderme.

### X otomasyonu (kod33)
- `social.js`: `x_enabled` + `x_webhook` → Make (Webhook → Google Tablolar → filtre deneme=false → Buffer → X). Belge: `docs/SOSYAL_PAYLASIM.md`.
- Kullanıcıya **kod33** sunucu paketi gönderildi; yüklenip yüklenmediğini ve test_ping sonucunu kontrol et.


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
