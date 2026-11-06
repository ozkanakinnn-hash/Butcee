// Uygulama simgesi ve açılış ekranını (yalnız dikey) oyunun kendi çizim kodundan üretir.
// Kullanım: önce `npx vite --port 5173`, sonra `node scripts/gen-icons.cjs`
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const RES = path.join(__dirname, '../android/app/src/main/res');

(async () => {
  const b = await chromium.launch();
  const p = await b.newPage();
  await p.goto('http://localhost:5173/');
  const out = await p.evaluate(async () => {
    const d = await import('/src/render/draw.ts');
    const res = {};
    const mk = (w, h) => { const c = document.createElement('canvas'); c.width = w; c.height = h; return c; };
    // simgedeki blok kompozisyonu: (0,0) merkezli, birim = blok
    function art(ctx, cx, cy, s) {
      const cells = [[-1, 0, 'leaf'], [0, -1, 'water'], [0, 0, 'leaf'], [0, 1, 'sun'], [1, -1, 'red'], [1, 0, 'purple'], [1, 1, 'water']];
      for (const [r, c, el] of cells) d.drawCell(ctx, { k: 'block', el }, cx + (c - 0.5) * s, cy + (r - 0.5) * s, s, true);
      d.drawFlower(ctx, cx + 1.05 * s, cy - 1.25 * s, s * 0.85, '#ff8fb1');
      d.drawAnimal(ctx, 'ladybug', cx - 1.15 * s, cy - 1.0 * s, s * 0.7);
    }
    function bg(ctx, w, h, round) {
      const g = ctx.createLinearGradient(0, 0, 0, h);
      g.addColorStop(0, '#fff0f6'); g.addColorStop(1, '#e3d2fb');
      ctx.fillStyle = g;
      if (round) { ctx.beginPath(); ctx.arc(w / 2, h / 2, w / 2, 0, Math.PI * 2); ctx.fill(); }
      else { d.roundRect(ctx, 0, 0, w, h, w * 0.2); ctx.fill(); }
    }
    for (const [name, px] of [['mdpi', 48], ['hdpi', 72], ['xhdpi', 96], ['xxhdpi', 144], ['xxxhdpi', 192]]) {
      for (const round of [false, true]) {
        const c = mk(px, px); const ctx = c.getContext('2d');
        d.clearSpriteCache(1);
        bg(ctx, px, px, round);
        art(ctx, px / 2, px * 0.55, px * 0.245);
        res[`mipmap-${name}/ic_launcher${round ? '_round' : ''}.png`] = c.toDataURL('image/png');
      }
      const fp = Math.round(px * 108 / 48);
      const c = mk(fp, fp); const ctx = c.getContext('2d');
      d.clearSpriteCache(1);
      art(ctx, fp / 2, fp * 0.54, fp * 0.16); // güvenli alan (%66) içinde
      res[`mipmap-${name}/ic_launcher_foreground.png`] = c.toDataURL('image/png');
    }
    const splash = (w, h) => {
      const c = mk(w, h); const ctx = c.getContext('2d');
      d.clearSpriteCache(1);
      // düz gradyan (ahşap dokusu PNG'de çok yer kaplıyor)
      const g = ctx.createLinearGradient(0, 0, 0, h);
      g.addColorStop(0, '#ffe6d6'); g.addColorStop(0.55, '#efe2fb'); g.addColorStop(1, '#d9f3ea');
      ctx.fillStyle = g; ctx.fillRect(0, 0, w, h);
      const s = Math.min(w, h) * 0.11;
      art(ctx, w / 2, h / 2 - s * 0.6, s);
      ctx.font = `800 ${s * 0.75}px system-ui, sans-serif`;
      ctx.textAlign = 'center';
      ctx.fillStyle = '#7a4fb0';
      ctx.shadowColor = 'rgba(255,255,255,0.9)'; ctx.shadowOffsetY = s * 0.06;
      ctx.fillText('Blok Bahçesi', w / 2, h / 2 + s * 2);
      return c.toDataURL('image/png');
    };
    const sizes = { mdpi: [320, 480], hdpi: [480, 800], xhdpi: [720, 1280], xxhdpi: [960, 1600], xxxhdpi: [1280, 1920] };
    for (const [k, [w, h]] of Object.entries(sizes)) {
      res[`drawable-port-${k}/splash.png`] = splash(w, h);
    }
    res['drawable/splash.png'] = splash(480, 320);
    return res;
  });
  for (const [f, url] of Object.entries(out)) {
    fs.writeFileSync(path.join(RES, f), Buffer.from(url.split(',')[1], 'base64'));
  }
  console.log('yazıldı:', Object.keys(out).length, 'dosya');
  await b.close();
})();
