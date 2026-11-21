// App Store ekran görüntüleri — iPhone 6,3" (1206×2622). Ham ekranlar: docs/ios/ekranlar-ham (tool/promo/capture.dart,
// PROMO_W=402 PROMO_H=874 PROMO_DPR=3; GitHub: "News tanıtım ekranları" iş akışı). Görsellerdeki tüm veriler temsilidir.
// Kullanım: node olustur.js [ham-klasör] [çıktı-klasörü]
// Not: iOS'ta olmayan özellik (bildirimdeki canlı skor paneli) metinlerde geçmez (Apple 2.3.1).
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const shots = process.argv[2] || '../ekranlar-ham';
const out = process.argv[3] || '.';
const icon = path.join(__dirname, '../../../flutter_app/ios/Runner/Assets.xcassets/AppIcon.appiconset');
const uri = (f) => 'data:image/png;base64,' + fs.readFileSync(f).toString('base64');
const iconFile = fs.existsSync(icon) ? fs.readdirSync(icon).filter((f) => f.endsWith('.png'))
  .map((f) => path.join(icon, f)).sort((a, b) => fs.statSync(b).size - fs.statSync(a).size)[0] : null;

const themes = {
  navy: ['#0B1F3A', '#1b3d6e', '#081629'],
  red: ['#3a0b14', '#7a1626', '#1f060b'],
  green: ['#062a1e', '#0f5a3f', '#04170f'],
  violet: ['#1c1240', '#3b2a86', '#0f0a24'],
  dark: ['#0b0c0f', '#1a1c22', '#000000'],
};

const css = `
*{box-sizing:border-box;margin:0}
body{width:402px;height:874px;overflow:hidden;font-family:-apple-system,"Helvetica Neue",Roboto,"Noto Sans",sans-serif}
.slide{width:402px;height:874px;position:relative;color:#fff;overflow:hidden}
.glow{position:absolute;border-radius:50%;filter:blur(40px);opacity:.55}
.brand{position:absolute;top:34px;left:0;right:0;display:flex;justify-content:center;align-items:center;gap:8px;
       font-size:13px;font-weight:700;letter-spacing:.3px;color:rgba(255,255,255,.85)}
.brand img{width:22px;height:22px;border-radius:6px}
.head{position:absolute;top:66px;left:22px;right:22px;text-align:center}
.head h1{font-size:31px;line-height:1.1;font-weight:800;letter-spacing:-.6px}
.head p{margin-top:10px;font-size:15px;line-height:1.35;color:rgba(255,255,255,.78)}
.acc{color:#ff6b70}
.phone{position:absolute;left:50%;transform:translateX(-50%);top:196px;width:304px;border-radius:50px;background:#0a0a0c;
       padding:10px;box-shadow:0 30px 70px rgba(0,0,0,.55),0 0 0 1.5px rgba(255,255,255,.18)}
.screen{border-radius:40px;overflow:hidden;position:relative;background:#fff}
.screen.dk{background:#000}
.sb{height:38px;display:flex;justify-content:space-between;align-items:center;padding:0 26px 0 30px;font-size:13px;font-weight:600;color:#111}
.dk .sb{color:#f5f5f7}
.island{position:absolute;top:9px;left:50%;transform:translateX(-50%);width:90px;height:26px;border-radius:13px;background:#000}
.shot{display:block;width:100%}
.bars{display:flex;gap:4px;align-items:center}.bars i{display:block;width:3px;border-radius:1px;background:currentColor}
.bat{width:22px;height:11px;border:1.5px solid currentColor;border-radius:3px;padding:1px;opacity:.9}.bat b{display:block;height:100%;width:80%;background:currentColor;border-radius:1px}
`;

const slides = [
  ['01-gundem-tek-akista', '01-akis', 'navy', 'Gündem <span class="acc">tek akışta</span>', 'Türkiye\'nin haber kaynakları tek, sade ve hızlı bir akışta'],
  ['02-son-dakika', '09-son-dakika', 'red', 'Son dakikayı <span class="acc">kaçırma</span>', 'Birden çok kaynağın doğruladığı önemli gelişmeler'],
  ['03-takimini-takip-et', '02-canli-skor', 'green', 'Takımını <span class="acc">takip et</span>', 'Gol, kırmızı kart, devre arası ve maç sonu bildirimleri'],
  ['04-az-ama-onemli', '03-bildirim-ayarlari', 'navy', 'Az ama <span class="acc">önemli</span> bildirim', 'Yoğunluğu sen seç · sabah, öğle ve akşam bülteni'],
  ['05-piyasa-hava', '04-piyasa-hava', 'violet', 'Piyasa ve <span class="acc">hava durumu</span>', 'Dolar, euro, altın ve İstanbul\'un havası tek dokunuşta'],
  ['06-arama', '08-arama', 'navy', 'Aradığını <span class="acc">hemen bul</span>', 'Başlıkta ve kaynakta hızlı arama'],
  ['07-ilgi-alanlari', '05-ilgi-alanlari', 'green', 'Sana özel <span class="acc">ilgi alanları</span>', 'Konularını ve takımlarını seç, akış sana uysun'],
  ['08-bildirimler', '06-bildirimler', 'violet', 'Kaçırdığın <span class="acc">her şey burada</span>', 'Gönderilen tüm bildirimler tek sekmede, 7 gün boyunca'],
  ['09-gece-modu', '07-akis-koyu', 'dark', 'Gece moduna <span class="acc">hazır</span>', 'Üyelik yok · ücretsiz · gizliliğine saygılı'],
];

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 402, height: 874 }, deviceScaleFactor: 3 });
  for (const [name, shot, theme, title, sub] of slides) {
    const [a, b, c] = themes[theme];
    const dk = theme === 'dark' ? 'dk' : '';
    const html = `<html><head><meta charset="utf-8"><style>${css}</style></head><body>
      <div class="slide" style="background:linear-gradient(165deg,${b} 0%,${a} 55%,${c} 100%)">
        <div class="glow" style="width:260px;height:260px;left:-80px;top:120px;background:${b}"></div>
        <div class="glow" style="width:220px;height:220px;right:-70px;top:420px;background:#ff5a5f;opacity:.18"></div>
        <div class="brand">${iconFile ? `<img src="${uri(iconFile)}">` : ''}Haber Akışı</div>
        <div class="head"><h1>${title}</h1><p>${sub}</p></div>
        <div class="phone"><div class="screen ${dk}"><div class="island"></div>
          <div class="sb"><span>9:41</span><span class="bars"><i style="height:4px"></i><i style="height:6px"></i><i style="height:8px"></i><i style="height:10px"></i>
            <span style="width:6px"></span><span class="bat"><b></b></span></span></div>
          <img class="shot" src="${uri(path.join(shots, shot + '.png'))}"></div></div>
      </div></body></html>`;
    await page.setContent(html);
    await page.screenshot({ path: path.join(out, name + '.png') });
    console.log('✔', name);
  }
  await browser.close();
})();
