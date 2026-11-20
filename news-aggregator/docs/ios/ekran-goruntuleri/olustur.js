// App Store ekran görüntüleri (iPhone 6,9": 1320×2868) — Play için yakalanan GERÇEK uygulama ekranlarını
// (docs/play-store/tanitim-gorselleri/ekranlar, tool/promo/capture.dart) iPhone çerçevesine yerleştirir.
// Kullanım: node olustur.js <ekranlar-klasörü> <çıktı-klasörü>
// Not: iOS'ta olmayan özellik (bildirimdeki canlı skor paneli) metinlerde geçmez (Apple 2.3.1).
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const shots = process.argv[2] || '../../play-store/tanitim-gorselleri/ekranlar';
const out = process.argv[3] || '.';
const dataUri = (n) => 'data:image/png;base64,' + fs.readFileSync(path.join(shots, n + '.png')).toString('base64');

// 440×956 mantıksal × 3 = 1320×2868
const css = `
*{box-sizing:border-box}
body{margin:0;width:440px;height:956px;overflow:hidden;font-family:-apple-system,"Helvetica Neue",Roboto,"Noto Sans",sans-serif}
.slide{width:440px;height:956px;position:relative;color:#fff;overflow:hidden;
       background:linear-gradient(160deg,#1b3d6e 0%,#0B1F3A 60%,#081629 100%)}
.slide.dark{background:linear-gradient(160deg,#16181d 0%,#0b0c0f 100%)}
.head{position:absolute;top:52px;left:26px;right:26px;text-align:center}
.head h1{margin:0;font-size:33px;line-height:1.14;font-weight:800;letter-spacing:-.4px}
.head p{margin:10px 0 0;font-size:16px;color:#c9d4e5;line-height:1.35}
.red{color:#ff5a5f}
.phone{position:absolute;left:58px;top:206px;width:324px;height:716px;border-radius:56px;
       background:#0a0a0c;padding:10px;box-shadow:0 26px 60px rgba(0,0,0,.5)}
.screen{width:100%;height:100%;border-radius:46px;overflow:hidden;position:relative;background:#fff}
.screen.dk{background:#000}
.island{position:absolute;top:10px;left:50%;transform:translateX(-50%);width:96px;height:28px;border-radius:14px;background:#000;z-index:3}
.sb{position:absolute;top:0;left:0;right:0;height:46px;display:flex;justify-content:space-between;
    align-items:center;padding:0 30px;font-size:13px;font-weight:600;color:#121821;z-index:2;background:inherit}
.screen.dk .sb{color:#f5f5f7}
.shot{position:absolute;top:46px;left:0;width:100%;height:calc(100% - 46px);object-fit:cover;object-position:top;display:block}
`;

const slides = [
  ['01-gundem-tek-akista', '01-akis', '', 'Gündem <span class="red">tek akışta</span>',
    'Türkiye\'nin haber kaynakları tek, sade ve hızlı bir akışta'],
  ['02-mac-takibi', '02-canli-skor', '', 'Takımını <span class="red">takip et</span>',
    'Gol, kırmızı kart, devre arası ve maç sonu bildirimleri'],
  ['03-az-ama-onemli', '03-bildirim-ayarlari', '', 'Az ama <span class="red">önemli</span> bildirim',
    'Yoğunluğu siz seçin · sabah, öğle ve akşam bülteni'],
  ['04-piyasa-hava', '04-piyasa-hava', '', 'Piyasa ve <span class="red">hava durumu</span>',
    'Dolar, euro, altın ve bulunduğunuz yerin havası tek dokunuşta'],
  ['05-ilgi-alanlari', '05-ilgi-alanlari', '', 'Size özel <span class="red">ilgi alanları</span>',
    'Konularınızı ve takımlarınızı seçin, akış size uysun'],
  ['06-bildirimler', '06-bildirimler', '', 'Kaçırdığınız <span class="red">her şey burada</span>',
    'Gönderilen tüm bildirimler tek sekmede, 7 gün boyunca'],
  ['07-gece-modu', '07-akis-koyu', 'dark', 'Gece moduna <span class="red">hazır</span>',
    'Üyelik yok · ücretsiz · gizliliğinize saygılı'],
];

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 440, height: 956 }, deviceScaleFactor: 3 });
  for (const [name, shot, cls, title, sub] of slides) {
    const dk = cls === 'dark' ? 'dk' : '';
    await page.setContent(`<html><head><meta charset="utf-8"><style>${css}</style></head><body>
      <div class="slide ${cls}"><div class="head"><h1>${title}</h1><p>${sub}</p></div>
      <div class="phone"><div class="screen ${dk}"><div class="island"></div>
        <div class="sb"><span>9:41</span><span>▮▮▮ ◔ ▬</span></div>
        <img class="shot" src="${dataUri(shot)}"></div></div></div></body></html>`);
    await page.screenshot({ path: path.join(out, name + '.png') });
    console.log('✔', name);
  }
  await browser.close();
})();
