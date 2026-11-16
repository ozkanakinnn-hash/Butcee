# AdMob kimlikleri — Haber Akışı (akinai.app1)

Bu kimlikler gizli değildir (uygulamanın içinde herkese açık dururlar), ancak **koda yazılmaz**:
yayın derlemesinde `--dart-define` / `ADMOB_APP_ID` ile verilir (bkz. `lib/ads/ads_config.dart`).

| Alan | Değer |
|---|---|
| Yayıncı kimliği | `pub-2835938155487054` |
| Uygulama kimliği | `ca-app-pub-2835938155487054~3670282038` |
| Banner (ön sayfa) | `ca-app-pub-2835938155487054/6927000769` |
| Yerel gelişmiş (akış içi) | _bekleniyor_ |
| Geçiş (interstitial) | _bekleniyor_ |

`app-ads.txt` → `firebase/hosting/public/app-ads.txt` (yayında: https://haber-68a3c.web.app/app-ads.txt)

## Yayın derlemesi (gerçek reklamlarla)

```bash
cd flutter_app
ADMOB_APP_ID=ca-app-pub-2835938155487054~3670282038 \
flutter build appbundle --release --build-number=101 \
  --dart-define=ADMOB_BANNER_ID=ca-app-pub-2835938155487054/6927000769 \
  --dart-define=ADMOB_NATIVE_ID=<yerel-gelişmiş> \
  --dart-define=ADMOB_INTERSTITIAL_ID=<geçiş>
```

- Kimlik verilmezse o reklam alanı **hiç gösterilmez** (sürüm derlemesinde test reklamı gösterilmez).
- Test APK'larında Google'ın test kimlikleri kullanılır → kendi reklamınıza tıklama riski yok.
- ⚠️ Kendi reklamlarınıza tıklamayın; AdMob hesabı kalıcı kapatır.

## Play yayınından sonra
1. AdMob → uygulamayı **Play mağaza girişine bağla** (doluluk oranı artar).
2. AdMob `app-ads.txt` dosyasını mağaza girişindeki web sitesinden tarar; site adresi
   `https://haber-68a3c.web.app` olarak girilmiş olmalı.
