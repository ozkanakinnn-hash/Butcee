# Blok Bahçesi

Dikey (portrait) mobil blok bulmaca oyunu — **Seç → Döndür → Bırak**. Vite + TypeScript + saf HTML5 Canvas,
Android paketi Capacitor 8. Tüm grafikler ve sesler koddan üretilir (görsel/ses dosyası yok).

## Çalıştırma
```bash
cd blok-bahcesi
npm install
npm run dev        # tarayıcıda http://localhost:5173 (telefon görünümünde deneyin)
npm test           # Vitest birim testleri (src/game)
npm run build      # dist/
```

## APK
- **GitHub Actions:** `.github/workflows/build-apk.yml` — `blok-bahcesi/**` değişince ve elle (Run workflow) çalışır;
  testleri koşar, web'i derler, `cap sync` + `assembleDebug` yapar ve `BlokBahcesi-debug` artifact'ını yükler.
- **Yerelde (Android SDK + JDK 21 varsa):** `npm run build && npx cap sync android && cd android && ./gradlew assembleDebug`

## v0.2 yenilikleri (ayrıntı: `docs/RAPOR.md`)
- **Bahçıvan Dede**: ana menüde el sallayan, dokununca konuşan karakter.
- **Bahçıvanı Kurtar**: 6 ekstra bölüm; su yükselir, Dede'yi kurtarınca +30 elmas.
- **Elmas ekonomisi**: kritik eşikler, boss/kurtarma ödülleri, ödüllü reklam (AdMob, test kimlikleri). Harcama: geri alma, +5 hamle, suyu çekme, bahçe objeleri, ev geliştirme.
- **Bahçem**: 5 seviyeli ev, 12 obje, 8 yuva, mağaza.
- **Bahçe Ligi**: Google Play Games kimliği + sıralama; ilk üçe madalya, bahçelerini ziyaret (bahçe, skor etiketine 64 karakterle kodlanır).
- **60 bölüm** (21–60 üretilip bot ile kalibre edildi), prosedürel müzik, çift dokunuşla bırakma.

### Bölüm kalibrasyonu
```bash
npx vite-node scripts/calibrate.ts   # src/levels-extra.ts ve docs/difficulty.tsv dosyalarını yeniden üretir (~1,5 dk)
```

### İsteğe bağlı GitHub Secrets
`PGS_APP_ID`, `PGS_LEADERBOARD_ID` (Play Games), `ADMOB_APP_ID`, `ADMOB_REWARDED_ID` (yayın reklamı). Boşsa: sıralama kapalı, AdMob test kimliği.

## Kod düzeni
| Klasör | İçerik |
|---|---|
| `src/game/` | DOM'dan bağımsız kurallar: tahta, parçalar, temizleme/yerçekimi, elementler, puan, boss, bölüm üretici + `__tests__` |
| `src/render/` | Canvas çizimi: hücre sprite önbelleği, ahşap arka plan + mevsim tonu, parçacıklar, bahçe sahnesi |
| `src/ui/` | Ekranlar (menü, harita, oyun, ayarlar, Bahçem, Günlük/Klasik), yönlendirici + Android geri tuşu |
| `src/levels.ts`, `levels-base.ts`, `levels-extra.ts` | El yapımı 1–20 + kurtarma haritaları; kalibre edilmiş 60 bölüm |
| `src/economy.ts`, `ads.ts`, `playgames.ts` | Elmas/katalog kuralları, ödüllü reklam, Play Games köprüsü |
| `android/.../PlayGamesPlugin.java` | Yerel Capacitor eklentisi (giriş, skor gönder, ilk 25, kendi sıram) |
| `src/storage.ts` / `src/audio.ts` / `src/i18n.ts` | localStorage kaydı, Web Audio efektleri + Haptics, TR/EN metinler |
| `scripts/gen-icons.cjs` | Uygulama simgesi ve açılış ekranını oyunun çizim kodundan üretir |

## Tasarım kararları / varsayımlar
- **Zincir:** saf Tetris kaydırmasında satırlar bütün halinde indiği için zincir yalnızca kaya (sütunu bölen) ya da
  hayatta kalan hücreler (ot/buz/tohum) varken oluşur. Zincirin her ek adımı +%50 puan.
- **Kombo:** ilk temizlemede x1, ardışık her temizlemede x(1 + kombo·0,5). Mevsim bölüm boyunca geri gitmez.
- **Taş:** satırın puan payı, taş olmayan hücre oranıyla çarpılır (tamamı taş = 0 puan).
- **Tohum:** sulanmadan temizlenen satırdaysa yok olmaz, yerinde kalır. Su; temizlenen satırın kendisindeki tohumları da filizlendirir.
- **Buz:** tasarımda yalnız güneş etkisinde geçtiği için yabani otla aynı kurallı (2 temizleme ya da güneş) bir hücre olarak eklendi.
- **Mor/Kırmızı simgeleri:** renk körlüğü için elmas ve üçgen eklendi.
- **Yalnızca kaya/tohum içeren dolu satır** silinecek bir şey olmadığından temizlenmiş sayılmaz (sonsuz döngü koruması).
- **Klasik Hız:** el/sıra mekaniği korunur; sıradaki parça otomatik düşer, ele dokunarak (aynı konumda sığıyorsa) değiştirilebilir.
- **Günlük Tohum:** bölüm ve parça sırası tarihten üretilen tohumla herkes için aynıdır.
- Bahçe dekorları: Çit 3★, Bank 9★, Fener 18★, Kuş evi 30★, Çeşme 45★.
- Node 22 ve JDK 21 kullanılır: Capacitor 8 CLI Node ≥22, Android kütüphanesi Java 21 ister.
