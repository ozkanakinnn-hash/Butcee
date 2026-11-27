// App Store görselleri (iPhone 6,3" 1206×2622). Ham ekranlar: raw-ios/ — GERÇEK iPhone simülatörü, Apple durum çubuğuyla
// (.github/workflows/minik-gurme-ios-screenshots.yml). App Review 2.3.10: sahte/Android durum çubuğu ÇİZİLMEZ.
// raw/ (Robolectric) yalnızca yedek; onunla durum çubuğu alanı boş bırakılır.
// Kullanım: node olustur.js  → tr/*.png, en/*.png
const { chromium } = require('playwright');
const fs = require('fs'); const path = require('path');
const here = __dirname;
const iosRaw = fs.existsSync(path.join(__dirname, 'raw-ios', 'tr_1_today.png'));
const uri = (f) => 'data:image/png;base64,' + fs.readFileSync(f).toString('base64');
const icon = path.join(here, '../../../iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png'.replace('minik_gurme/', ''));
const css = `
*{box-sizing:border-box;margin:0}
body{width:402px;height:874px;overflow:hidden;font-family:-apple-system,"Helvetica Neue",Roboto,"Noto Sans",sans-serif}
.slide{width:402px;height:874px;position:relative;overflow:hidden;color:#3b2a24}
.blob{position:absolute;border-radius:50%;filter:blur(30px);opacity:.55}
.brand{position:absolute;top:32px;left:0;right:0;display:flex;justify-content:center;align-items:center;gap:8px;font-size:13px;font-weight:700;color:#7a4a3a}
.brand img{width:24px;height:24px;border-radius:7px}
.head{position:absolute;top:64px;left:22px;right:22px;text-align:center}
.head h1{font-size:30px;line-height:1.12;font-weight:800;letter-spacing:-.5px}
.head p{margin-top:9px;font-size:15px;line-height:1.35;color:#7a5a50}
.acc{color:#e8604c}
.phone{position:absolute;left:50%;transform:translateX(-50%);top:192px;width:300px;border-radius:50px;background:#1b1416;padding:10px;
       box-shadow:0 28px 60px rgba(120,60,40,.35)}
.screen{border-radius:40px;overflow:hidden;position:relative;background:#fff}
.sb{height:36px;display:flex;justify-content:space-between;align-items:center;padding:0 26px 0 30px;font-size:13px;font-weight:600;color:#111;background:#fff8f4}
.island{position:absolute;top:8px;left:50%;transform:translateX(-50%);width:88px;height:25px;border-radius:13px;background:#000}
.shot{display:block;width:100%}
`;
const themes = [['#fff1ea','#ffd9c9'],['#fef6e4','#ffe2a8'],['#f1f8ef','#cfe9c6'],['#eef4fb','#cfe0f5'],['#fbf0f6','#f3cfe2'],['#fff1ea','#ffd9c9']];
const slides = {
  tr: [['1_today','Bebeğinin ek gıda <span class="acc">günlüğü</span>','Öğünler, tepkiler ve alerjen uyarıları tek ekranda'],
       ['2_foods100','1 yaşına kadar <span class="acc">100 gıda</span>','Denediklerini işaretle, tepkileri not et'],
       ['3_recipes','Yaşa uygun <span class="acc">tarifler</span>','Günün menüsü, püre ve BLW seçenekleri'],
       ['4_guide','Ay ay <span class="acc">rehber</span>','Kıvam aşamaları, öğün akışı ve güvenlik'],
       ['5_growth','Büyümeyi <span class="acc">takip et</span>','Kilo ve boyu WHO eğrileriyle karşılaştır'],
       ['6_community','Ebeveyn <span class="acc">topluluğu</span>','Ay ay konularda soru sor, deneyim paylaş']],
  en: [['1_today','Your baby\'s <span class="acc">food diary</span>','Meals, reactions and allergen alerts in one place'],
       ['2_foods100','<span class="acc">100 foods</span> before one','Tick what you tried, note reactions'],
       ['3_recipes','Age-appropriate <span class="acc">recipes</span>','Daily menu, purée and BLW options'],
       ['4_guide','Month-by-month <span class="acc">guide</span>','Texture stages, meal flow and safety'],
       ['5_growth','Track <span class="acc">growth</span>','Compare weight and height with WHO curves'],
       ['6_community','Parent <span class="acc">community</span>','Ask questions and share experiences']],
};
(async () => {
  const b = await chromium.launch(); const p = await b.newPage({ viewport: { width: 402, height: 874 }, deviceScaleFactor: 3 });
  for (const lang of ['tr', 'en']) {
    fs.mkdirSync(path.join(here, lang), { recursive: true });
    for (const [i, [shot, title, sub]] of slides[lang].entries()) {
      const [c1, c2] = themes[i];
      await p.setContent(`<html><head><meta charset="utf-8"><style>${css}</style></head><body>
        <div class="slide" style="background:linear-gradient(170deg,${c1} 0%,${c2} 100%)">
          <div class="blob" style="width:240px;height:240px;left:-90px;top:300px;background:#ffffff"></div>
          <div class="blob" style="width:200px;height:200px;right:-70px;top:90px;background:${c2}"></div>
          <div class="brand">${fs.existsSync(icon) ? `<img src="${uri(icon)}">` : ''}Minik Gurme</div>
          <div class="head"><h1>${title}</h1><p>${sub}</p></div>
          <div class="phone"><div class="screen"><div class="island"></div>${iosRaw ? '' : '<div class="sb"></div>'}
            <img class="shot" src="${uri(path.join(here, iosRaw ? 'raw-ios' : 'raw', lang + '_' + shot + '.png'))}"></div></div>
        </div></body></html>`);
      await p.screenshot({ path: path.join(here, lang, String(i + 1).padStart(2, '0') + '-' + shot.slice(2) + '.png') });
    }
    console.log('✔', lang);
  }
  await b.close();
})();
