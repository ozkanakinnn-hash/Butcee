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

## Kod düzeni
| Klasör | İçerik |
|---|---|
| `src/game/` | DOM'dan bağımsız kurallar: tahta, parçalar, temizleme/yerçekimi, elementler, puan, boss, bölüm üretici + `__tests__` |
| `src/render/` | Canvas çizimi: hücre sprite önbelleği, ahşap arka plan + mevsim tonu, parçacıklar, bahçe sahnesi |
| `src/ui/` | Ekranlar (menü, harita, oyun, ayarlar, Bahçem, Günlük/Klasik), yönlendirici + Android geri tuşu |
| `src/levels.ts` | 20 el yapımı bölüm (metin haritası) |
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
