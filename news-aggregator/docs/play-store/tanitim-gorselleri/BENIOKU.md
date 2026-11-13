# Tanıtım görselleri (Play Store)

`0*.png` — Play Console → Ana mağaza girişi → **Telefon ekran görüntüleri** (1080×1920, 9:16).
`ekranlar/` — uygulamadan yakalanan ham ekranlar (720×1500); çerçevesiz hâlleri.

## Nasıl üretilir
Ekranlar, uygulamanın **gerçek widget'ları** sahte Firestore verisiyle çalıştırılarak yakalanır
(`flutter_app/tool/promo/capture.dart`), sonra telefon çerçevesine yerleştirilir.

```bash
cd flutter_app
PROMO_OUT=$PWD/../docs/play-store/tanitim-gorselleri/ekranlar \
PROMO_IMAGES=/yol/images.json tool/promo/yakala.sh      # 7 ekranı yakalar
cd ../docs/play-store/tanitim-gorselleri
node olustur.js ekranlar .                               # çerçeve + başlık
```

`PROMO_IMAGES`: haber görselleri için `{"https://gorsel.local/x.jpg": "/yerel/yol.png"}` eşlemesi.

## İçerik kuralı
Gerçek haber fotoğrafı, yayıncı adı, takım ve oyuncu adı **kullanılmaz** (telif ve kişilik hakkı):
örnek veriler temsilidir (Haber Ajansı, Yeşil FK, A. Yılmaz…).
