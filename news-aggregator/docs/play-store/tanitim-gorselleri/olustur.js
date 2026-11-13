// Play Store tanıtım görsellerini üretir: uygulamadan yakalanan GERÇEK ekran görüntülerini (tool/promo/capture.dart)
// telefon çerçevesine yerleştirir ve başlık ekler. Çıktı: 1080×1920 (Play'in istediği 9:16).
//
// Kullanım (flutter_app dizininde ekranlar yakalandıktan sonra):
//   node olustur.js <ekranlar-klasörü> <çıktı-klasörü>

const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const shots = process.argv[2] || 'shots';
const out = process.argv[3] || '.';

/** Yakalanan ekran → data URI (setContent içinde file:// yüklenmez). */
const dataUri = (name) =>
  'data:image/png;base64,' + fs.readFileSync(path.join(shots, name + '.png')).toString('base64');

const css = `
*{box-sizing:border-box}
body{margin:0;width:540px;height:960px;overflow:hidden;
     font-family:system-ui,Roboto,"Noto Sans",sans-serif}
.slide{width:540px;height:960px;position:relative;color:#fff;overflow:hidden;
       background:linear-gradient(160deg,#1b3d6e 0%,#0B1F3A 60%,#081629 100%)}
.slide.dark{background:linear-gradient(160deg,#16181d 0%,#0b0c0f 100%)}
.head{position:absolute;top:44px;left:34px;right:34px;text-align:center}
.head h1{margin:0;font-size:37px;line-height:1.14;font-weight:800;letter-spacing:-.5px}
.head p{margin:11px 0 0;font-size:18px;color:#c9d4e5;line-height:1.35}
.red{color:#ff5a5f}
/* Telefon: 364×736, ekran 342×700 (yakalanan 360×750 oranına yakın) */
.phone{position:absolute;left:88px;top:200px;width:364px;height:740px;border-radius:44px;
       background:#0a0a0c;padding:11px;box-shadow:0 26px 60px rgba(0,0,0,.5)}
.screen{width:100%;height:100%;border-radius:34px;overflow:hidden;position:relative;background:#fff}
.screen.dk{background:#000}
.sb{position:absolute;top:0;left:0;right:0;height:26px;display:flex;justify-content:space-between;
    align-items:center;padding:0 18px;font-size:11.5px;font-weight:600;color:#121821;z-index:2}
.screen.dk .sb{color:#f5f5f7}
.shot{position:absolute;top:0;left:0;width:100%;display:block}
`;

const slides = [
  ['01-gundem-tek-akista', '01-akis', '', 'Gündem <span class="red">tek akışta</span>',
    'Türkiye\'nin haber kaynakları tek, sade ve hızlı bir akışta'],
  ['02-canli-skor', '02-canli-skor', '', 'Maçı <span class="red">canlı</span> takip et',
    'Skor, dakika ve goller anında · bildirimde canlı skor paneli'],
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
  const page = await browser.newPage({ viewport: { width: 540, height: 960 }, deviceScaleFactor: 2 });
  for (const [name, shot, cls, title, sub] of slides) {
    const dark = cls === 'dark';
    const html = `<html><head><meta charset="utf-8"><style>${css}</style></head><body>
      <div class="slide ${cls}">
        <div class="head"><h1>${title}</h1><p>${sub}</p></div>
        <div class="phone"><div class="screen ${dark ? 'dk' : ''}">
          <div class="sb"><span>09:41</span><span>●●● ▮</span></div>
          <img class="shot" src="${dataUri(shot)}">
        </div></div>
      </div></body></html>`;
    await page.setContent(html);
    await page.screenshot({ path: path.join(out, name + '.png') });
    console.log('✔', name);
  }
  await browser.close();
})();
