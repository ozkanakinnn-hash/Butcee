---
name: capacitor-canvas-game
description: Vite + TypeScript + saf Canvas ile mobil oyun (veya web tabanlı uygulama) yapıp Capacitor ile Android APK'ya paketleme. Oyun mantığını test edilebilir tutma, kodla grafik/ses üretme, Playwright ile oynatarak test, GitHub Actions'ta APK. Yeni bir HTML5/Capacitor oyunu veya uygulaması başlatırken kullan.
---

# Capacitor + Canvas Oyun — Öğrenilenler (Blok Bahçesi projesinden)

## 0. Kullanıcı kuralları (her uygulamada)
- Arayüz Türkçe; **İngilizce dil desteği de ekle** (Ayarlar'da dil seçici). Web projesinde basit `i18n.ts` sözlüğü + `t('anahtar')` yeterli.
- Reklam / satın alma yalnızca istenirse.
- Depo başka bir uygulama içeriyorsa yeni projeyi alt klasöre koy (`blok-bahcesi/`), iş akışında `working-directory` ve `paths` filtresi kullan.

## 1. Sürüm uyumu (Kasım 2026)
- Capacitor 8: **CLI Node ≥ 22** ister, Android kütüphanesi **Java 21** ile derlenir (JDK 17 yetmez). compileSdk 36, AGP 8.13, Gradle 8.14.3.
- Kararlı seçim: `vite@^7`, `typescript@^5.9`, `vitest@^3` (en yeni ana sürümlere hemen atlama).
- `vite.config.ts`'te `base: './'` (WebView dosyadan yükler).
- Capacitor 8 `SystemBars.insetsHandling: 'css'` → WebView'a `--safe-area-inset-*` CSS değişkenleri enjekte eder.
  CSS'te `var(--safe-area-inset-top, env(safe-area-inset-top, 0px))` kullan; `viewport-fit=cover`.
- Dikey kilit: `AndroidManifest.xml` aktivitesine `android:screenOrientation="portrait"`.
- Geri tuşu: `@capacitor/app` → `App.addListener('backButton', …)`; kökte `App.exitApp()`. Tarayıcıda `Escape` ile aynı yolu test et.
- Titreşim: `@capacitor/haptics`, yalnız `Capacitor.isNativePlatform()` iken çağır, `.catch(()=>{})`.

## 2. Mimari
- `src/game/` saf TS (DOM yok): durum tek serileştirilebilir nesne → **geri al = JSON anlık görüntü yığını**.
  RNG durumu da tek sayı (mulberry32) olsun ki geri alma ve günlük tohum deterministik olsun.
- Hamle fonksiyonu animasyon için olay listesi döndürsün (yerleşmiş tahta, her temizleme adımının *öncesi*, kurtarılanlar,
  saldırı öncesi tahta, son tahta). Arayüz bunları **zaman çizelgesi kuyruğu** (`{dur,start,update,end}`) ile oynatır;
  mantık anında çözülür, görüntü `displayGrid` ile geriden gelir.
- Menü/HUD/diyaloglar DOM (net yazı, erişilebilir düğme), oyun alanı Canvas. Ekranlar için küçük yığın tabanlı Router
  (`push/replace/pop/back`, ekran `onBack()` true dönerse geri tuşunu kendisi işler).
- Canvas: `devicePixelRatio` (≤2.5) ile ölçekle; hücreleri **sprite önbelleğine** çiz (tür+boyut anahtarı) → 60 FPS rahat.
- Ahşap arka planı bir kez ekran dışı canvas'a çiz, her karede `drawImage` + ton katmanı (`soft-light`).

## 3. Kodla grafik ve ses
- Kabartmalı blok: koyu taban dikdörtgeni + üstte açık→asıl renk gradyanı + üst parlak şerit, yuvarlak köşe.
- Renk körlüğü: her renge simge (damla, yaprak, güneş, nokta, elmas, üçgen); ayarla aç/kapa → sprite önbelleğini temizle.
- Ses: Web Audio osilatör + üstel zarf; gürültü için rastgele buffer + lowpass. AudioContext'i ilk dokunuşta aç.
- Uygulama simgesi / splash: Vite dev sunucusunu aç, Playwright'ta `await import('/src/render/draw.ts')` ile oyunun kendi
  çizim fonksiyonlarını kullanıp `toDataURL` → `android/app/src/main/res/mipmap-*` ve `drawable-*` PNG'lerinin üzerine yaz
  (bkz. `blok-bahcesi/scripts/gen-icons.cjs`). Adaptive simge ön planında çizimi %66 güvenli alanda tut.

## 4. Test
- Vitest: satır temizleme/kaydırma, özel hücreler, puan/kombo, oyun sonu, boss; ayrıca **bölüm verisi doğrulama testi**
  (doğru boyut, başlangıçta dolu satır yok, hedef sayısı haritada mevcut, başlangıçta tıkalı değil).
- Kural tuzağı: Tetris tarzı "sabit kaydırma"da satırlar bütün iner → kayasız/engelsiz zincir oluşmaz; testi buna göre kur.
- "Sadece kaya/tohum içeren dolu satır" gibi hiçbir şeyin silinmediği durumlar sonsuz döngü yaratır → dışla + adım sınırı.
- Playwright (Pixel 7, `deviceScaleFactor: 1` kendi kontrollerin için): menüleri tıkla, oyunu otomatik oynat
  (el yuvasına dokun → tahtaya dokun → Bırak), `pageerror`/`console.error` topla, ekran görüntülerine bak.
  Nabız animasyonlu düğmelerde `click({ force: true })`; gizli ekranlardaki aynı sınıflı düğmeler yüzünden metinle seç.
- Geliştirmede `import.meta.env.DEV` iken `window.__bb()` gibi durum okuyucu bırak → Playwright'tan iç durumu sorgula.

## 5. APK (bulut konteynerinde SDK yok → GitHub Actions)
`checkout@v5 → setup-node@v5 (22, npm cache) → setup-java@v5 (temurin 21) →
android-actions/setup-android@v3 with packages: "platform-tools"` (varsayılan "tools" paketi artık yok, çöker) →
`npm ci → npm test → npm run build → npx cap sync android → gradle/actions/setup-gradle@v5 → ./gradlew assembleDebug →
upload-artifact@v5`. Capacitor'ın `android/.gitignore`'u `assets/public`'i dışlar; CI'da `cap sync` şart.
