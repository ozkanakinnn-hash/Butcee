const { chromium } = require('playwright');
const fs = require('fs');
const logo = fs.readFileSync('/home/user/Butcee/news-aggregator/docs/brand/logo.svg', 'utf8').replace(/^<svg[^>]*>|<\/svg>$/g, '');
const css = `
*{box-sizing:border-box}body{margin:0;width:540px;height:960px;overflow:hidden;font-family:system-ui,Roboto,sans-serif}
.slide{width:540px;height:960px;position:relative;color:#fff;background:linear-gradient(160deg,#1b3d6e 0%,#0B1F3A 60%,#081629 100%);overflow:hidden}
.slide.dark{background:linear-gradient(160deg,#16181d 0%,#0b0c0f 100%)}
.head{position:absolute;top:46px;left:36px;right:36px;text-align:center}
.head h1{margin:0;font-size:38px;line-height:1.12;font-weight:800;letter-spacing:-.5px}
.head p{margin:12px 0 0;font-size:18.5px;color:#c9d4e5;line-height:1.35}
.red{color:#ff5a5f}
.phone{position:absolute;left:88px;top:206px;width:364px;height:736px;border-radius:46px;background:#0a0a0c;padding:11px;box-shadow:0 30px 70px rgba(0,0,0,.5)}
.screen{width:100%;height:100%;border-radius:36px;overflow:hidden;background:#fff;color:#121821;position:relative}
.screen.dk{background:#000;color:#f5f5f7}
.sb{height:30px;display:flex;justify-content:space-between;align-items:center;padding:0 22px;font-size:12px;font-weight:600}
.ab{display:flex;align-items:center;justify-content:space-between;padding:6px 16px 4px}
.ab b{font-size:21px;font-weight:800;letter-spacing:-.3px}
.ic{width:22px;height:22px;border-radius:6px;opacity:.75}
.tabs{display:flex;gap:6px;padding:8px 12px;overflow:hidden;white-space:nowrap}
.tab{padding:6px 12px;border-radius:16px;font-size:12.5px;font-weight:700;color:#6B7280}
.tab.on{background:#16345F;color:#fff}
.dk .tab.on{background:#1E3A66}
.card{margin:8px 12px 10px}
.img{height:150px;border-radius:14px;position:relative;overflow:hidden}
.ttl{font-size:15.5px;font-weight:800;line-height:1.28;margin-top:8px}
.meta{font-size:11.5px;color:#6B7280;margin-top:5px;display:flex;gap:6px;align-items:center}
.badge{background:#D62828;color:#fff;font-size:10px;font-weight:800;padding:2px 6px;border-radius:5px}
.row{display:flex;gap:10px;margin:10px 12px;align-items:flex-start}
.thumb{width:92px;height:68px;border-radius:10px;flex:none}
.rt{font-size:13.5px;font-weight:700;line-height:1.3}
.nav{position:absolute;bottom:0;left:0;right:0;height:64px;border-top:1px solid #e5e5ea;background:#fff;display:flex;justify-content:space-around;align-items:center;font-size:10.5px;color:#6B7280}
.dk .nav{background:#000;border-color:#1f1f22}
.nav div{text-align:center}.nav .on{color:#16345F;font-weight:800}
.dk .nav .on{color:#7FA6E3}
.nav i{display:block;font-style:normal;font-size:19px;margin-bottom:2px}
`;
const g = (a, b, deco = '') => `background:linear-gradient(135deg,${a},${b});${deco}`;
const shape = (kind) => ({
  city: `<svg viewBox="0 0 300 150" width="100%" height="100%" preserveAspectRatio="xMidYMid slice"><rect width="300" height="150" fill="#2c4a73"/><circle cx="235" cy="38" r="20" fill="#f6c76b" opacity=".85"/><path d="M0 150V95h20V70h18v25h14V55h24v40h12V80h20v70zM120 150V60h26v90zM155 150V85h18V65h22v85zM205 150V75h30v75zM245 150V92h18V70h22v80z" fill="#132a48"/></svg>`,
  chart: `<svg viewBox="0 0 300 150" width="100%" height="100%" preserveAspectRatio="xMidYMid slice"><rect width="300" height="150" fill="#0f3d3a"/><polyline points="0,120 40,105 80,112 120,80 160,90 200,55 240,62 300,30" fill="none" stroke="#5be3b0" stroke-width="5"/><polygon points="0,120 40,105 80,112 120,80 160,90 200,55 240,62 300,30 300,150 0,150" fill="#5be3b0" opacity=".15"/></svg>`,
  pitch: `<svg viewBox="0 0 300 150" width="100%" height="100%" preserveAspectRatio="xMidYMid slice"><rect width="300" height="150" fill="#2f7d3b"/><rect x="10" y="10" width="280" height="130" fill="none" stroke="#fff" stroke-opacity=".7" stroke-width="2"/><line x1="150" y1="10" x2="150" y2="140" stroke="#fff" stroke-opacity=".7" stroke-width="2"/><circle cx="150" cy="75" r="22" fill="none" stroke="#fff" stroke-opacity=".7" stroke-width="2"/><circle cx="185" cy="88" r="6" fill="#fff"/></svg>`,
  tech: `<svg viewBox="0 0 300 150" width="100%" height="100%" preserveAspectRatio="xMidYMid slice"><rect width="300" height="150" fill="#3a2a6e"/><rect x="95" y="30" width="110" height="80" rx="8" fill="#6c5ce7"/><rect x="105" y="40" width="90" height="56" rx="4" fill="#a29bfe"/><rect x="80" y="112" width="140" height="8" rx="4" fill="#6c5ce7"/></svg>`,
  sky: `<svg viewBox="0 0 300 150" width="100%" height="100%" preserveAspectRatio="xMidYMid slice"><rect width="300" height="150" fill="#5aa7e0"/><circle cx="80" cy="60" r="26" fill="#ffd166"/><ellipse cx="170" cy="80" rx="60" ry="24" fill="#fff"/><ellipse cx="140" cy="70" rx="34" ry="22" fill="#fff"/></svg>`,
}[kind]);
const sb = (dark) => `<div class="sb"><span>09:41</span><span>●●● ▮</span></div>`;
const nav = (on = 0) => `<div class="nav">${[['⌂', 'Ana Sayfa'], ['⌕', 'Ara'], ['🔔', 'Bildirimler'], ['▦', 'İlgi Alanların']].map(([i, t], k) => `<div class="${k === on ? 'on' : ''}"><i>${i}</i>${t}</div>`).join('')}</div>`;
const appbar = `<div class="ab"><b>Haber Akışı</b><span style="font-size:20px">☰</span></div>`;
const tabs = (on = 'Gündem') => `<div class="tabs">${['Son Dakika', 'Gündem', 'Ekonomi', 'Spor', 'Dünya', 'Teknoloji'].map((t) => `<span class="tab ${t === on ? 'on' : ''}">${t}</span>`).join('')}</div>`;
const feed = (dark) => `<div class="screen ${dark ? 'dk' : ''}">${sb()}${appbar}${tabs('Gündem')}
  <div class="card"><div class="img">${shape('city')}</div><div class="ttl">Büyükşehirlerde yeni toplu ulaşım düzenlemesi bugün başladı</div><div class="meta"><span class="badge">SON DAKİKA</span>Haber Ajansı · 5 dk önce</div></div>
  <div class="row"><div class="thumb">${shape('chart')}</div><div><div class="rt">Piyasalarda gün sonu: endeksler yükselişle kapandı</div><div class="meta">Ekonomi Gazetesi · 18 dk</div></div></div>
  <div class="row"><div class="thumb">${shape('pitch')}</div><div><div class="rt">Ligde haftanın maçı nefes kesti: 3 puan son dakikada geldi</div><div class="meta">Spor Servisi · 32 dk</div></div></div>
  <div class="row"><div class="thumb">${shape('tech')}</div><div><div class="rt">Yerli yazılım girişimi yeni yatırım turunu tamamladı</div><div class="meta">Teknoloji Masası · 1 sa</div></div></div>
  ${nav(0)}</div>`;

const lock = `<div class="screen" style="background:linear-gradient(180deg,#3a2347,#7a3f3a 55%,#2a2430);color:#fff">${sb()}
  <div style="text-align:center;margin-top:40px;font-size:76px;font-weight:700;line-height:.9">20<br>33</div>
  <div style="text-align:center;font-size:16px;font-weight:600;margin-top:10px">12 Kasım Perşembe</div>
  <div style="margin:26px 12px 0;background:rgba(28,28,32,.92);border-radius:22px;padding:13px 14px;font-size:12px">
    <div style="display:flex;justify-content:space-between;color:#cfcfd4"><span>UEFA Uluslar Ligi</span><b style="color:#ff453a">67'</b></div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-top:6px">
      <b style="font-size:19px">🇹🇷 TÜR</b><b style="font-size:28px">2 – 1</b><b style="font-size:19px">BEL 🇧🇪</b></div>
    <div style="display:flex;justify-content:space-between;color:#cfcfd4;margin-top:2px"><span>Türkiye</span><span>Belçika</span></div>
    <div style="height:1px;background:#3a3a40;margin:9px 0 5px"></div>
    ${[['⚽ A. Yılmaz', "20'", ''], ['', "27'", 'J. Peeters ⚽'], ['🟨 K. Demir', "32'", ''], ['sep', '', '— İLK YARI 1 – 1 —'], ['⚽ M. Arslan (pen.)', "58'", ''], ['', "63'", 'L. Maes 🟥']]
      .map(([l, m, r]) => l === 'sep' ? `<div style="text-align:center;color:#a9a9b0;font-size:11px;margin:4px 0">${r}</div>` : `<div style="display:grid;grid-template-columns:1fr 36px 1fr;padding:3px 0"><span>${l}</span><b style="text-align:center;color:#cfcfd4">${m}</b><span style="text-align:right">${r}</span></div>`).join('')}
  </div>
</div>`;

const notifSettings = `<div class="screen">${sb()}
  <div class="ab"><b>Ayarlar</b><span></span></div>
  <div style="padding:6px 16px;font-size:13px;color:#6B7280;font-weight:700">BİLDİRİMLER</div>
  <div style="padding:6px 16px 2px;font-size:15px;font-weight:700">Bildirim yoğunluğu</div>
  ${[['Sadece önemli', 'Birçok kaynağın doğruladığı son dakikalar · günde ~4–6', 0], ['Dengeli (önerilen)', 'Önemli son dakika + saatte en fazla 1 özet · ~8–12', 1], ['Tümü', 'Her son dakika ve sık özetler · 20+', 0]]
    .map(([t, d, on]) => `<div style="display:flex;gap:12px;padding:9px 16px;align-items:flex-start"><span style="width:20px;height:20px;border-radius:50%;border:2px solid ${on ? '#16345F' : '#9aa0a6'};display:inline-flex;align-items:center;justify-content:center;flex:none;margin-top:2px">${on ? '<span style="width:10px;height:10px;border-radius:50%;background:#16345F"></span>' : ''}</span><div><div style="font-size:14.5px;font-weight:700">${t}</div><div style="font-size:12px;color:#6B7280;margin-top:2px">${d}</div></div></div>`).join('')}
  <div style="display:flex;justify-content:space-between;align-items:center;padding:10px 16px"><div><div style="font-size:14.5px;font-weight:700">Günlük bültenler</div><div style="font-size:12px;color:#6B7280">08:30 · 13:00 · 19:00</div></div><span style="width:42px;height:24px;border-radius:12px;background:#16345F;position:relative"><span style="position:absolute;right:3px;top:3px;width:18px;height:18px;border-radius:50%;background:#fff"></span></span></div>
  <div style="margin:14px 12px;background:#f2f2f7;border-radius:18px;padding:12px 14px">
    <div style="font-size:11px;color:#6B7280">Haber Akışı · şimdi</div>
    <div style="font-size:14px;font-weight:800;margin-top:3px">☀️ Sabah Bülteni</div>
    <div style="font-size:12.5px;margin-top:4px;line-height:1.4">• Büyükşehirlerde yeni toplu ulaşım düzenlemesi<br>• Piyasalarda gün sonu: endeksler yükselişte<br>• Ligde haftanın maçı nefes kesti</div>
    <div style="display:flex;gap:18px;margin-top:9px;font-size:12.5px;font-weight:800;color:#16345F"><span>Daha az bildirim</span></div>
  </div>${nav(2)}</div>`;

const market = `<div class="screen">${sb()}
  <div style="padding:10px 18px 4px;font-size:22px;font-weight:800">Piyasalar</div>
  <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;padding:8px 14px">
  ${[['Dolar', '₺', '42,1850', '+0,21'], ['Euro', '₺', '48,7320', '−0,12'], ['Gram Altın', '₺', '5.612,40', '+0,84'], ['Ons Altın', '$', '4.138,25', '+0,56']]
    .map(([n, u, v, c]) => `<div style="background:#f2f2f7;border-radius:14px;padding:12px"><div style="font-size:12.5px;color:#6B7280;font-weight:700">${n}</div><div style="font-size:18px;font-weight:800;margin-top:6px">${u} ${v}</div><div style="font-size:12px;font-weight:800;margin-top:4px;color:${c.startsWith('+') ? '#1a8f3c' : '#D62828'}">${c.startsWith('+') ? '▲' : '▼'} %${c.slice(1)}</div></div>`).join('')}
  </div>
  <div style="padding:2px 16px;font-size:10.5px;color:#6B7280">Güncelleme: 15:30 · Döviz: TCMB gösterge kurları · Altın<br>Bilgi amaçlıdır, yatırım tavsiyesi değildir.</div>
  <div style="padding:18px 18px 4px;font-size:19px;font-weight:800">Hava Durumu</div>
  <div style="margin:6px 14px;border-radius:16px;padding:14px;background:linear-gradient(135deg,#4f9de0,#2f6fb7);color:#fff;display:flex;align-items:center;gap:14px">
    <span style="font-size:44px">⛅</span><div><div style="font-size:30px;font-weight:800">19°</div><div style="font-size:13px">Parçalı bulutlu · Saruhanlı</div></div></div>
  <div style="padding:4px 16px;font-size:10.5px;color:#6B7280">Veri: MET Norway (CC BY 4.0)</div>
</div>`;

const interests = `<div class="screen">${sb()}
  <div class="ab"><b>İlgi Alanların</b><span></span></div>
  <div style="padding:2px 16px 8px;font-size:13px;color:#6B7280">Akışında ve bildirimlerinde görmek istediklerini seç.</div>
  <div style="display:flex;flex-wrap:wrap;gap:8px;padding:4px 14px">
  ${[['🔴 Son Dakika', 1], ['📰 Gündem', 1], ['💹 Ekonomi', 1], ['⚽ Futbol', 1], ['🌍 Dünya', 0], ['💻 Teknoloji', 1], ['🩺 Sağlık', 0], ['🎭 Kültür-Sanat', 0], ['🚗 Otomobil', 0], ['🏀 Basketbol', 0], ['🔬 Bilim', 1], ['✈️ Seyahat', 0]]
    .map(([t, on]) => `<span style="padding:9px 13px;border-radius:20px;font-size:13.5px;font-weight:700;${on ? 'background:#16345F;color:#fff' : 'background:#f2f2f7;color:#121821'}">${t}</span>`).join('')}
  </div>
  <div style="padding:18px 16px 6px;font-size:15px;font-weight:800">Takımların</div>
  <div style="display:flex;gap:8px;padding:0 14px;flex-wrap:wrap">${['Takım A', 'Takım B', '+ Takım ekle'].map((t, i) => `<span style="padding:8px 13px;border-radius:20px;font-size:13px;font-weight:700;${i < 2 ? 'background:#e8eef7;color:#16345F' : 'border:1.5px dashed #9aa0a6;color:#6B7280'}">${t}</span>`).join('')}</div>
  ${nav(3)}</div>`;

const slides = [
  ['01-gundem-tek-akista', '', 'Gündem <span class="red">tek akışta</span>', 'Türkiye\'nin haber kaynakları tek, sade ve hızlı bir akışta', feed(false)],
  ['02-canli-skor', '', 'Kilit ekranında <span class="red">canlı skor</span>', 'Gol, kart ve dakika anında · aşağı kaydır, ayrıntıları gör', lock],
  ['03-az-ama-onemli', '', 'Az ama <span class="red">önemli</span> bildirim', 'Bildirim yoğunluğunu siz seçin · sabah, öğle, akşam bülteni', notifSettings],
  ['04-piyasa-hava', '', 'Piyasa ve <span class="red">hava durumu</span>', 'Dolar, euro, altın ve bulunduğunuz yerin havası tek dokunuşta', market],
  ['05-ilgi-alanlari', '', 'Size özel <span class="red">ilgi alanları</span>', 'Takımlarınızı ve konularınızı seçin, akış size uysun', interests],
  ['06-gece-modu', 'dark', 'Gece moduna <span class="red">hazır</span>', 'Üyelik yok · ücretsiz · gizliliğinize saygılı', feed(true)],
];
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 540, height: 960 }, deviceScaleFactor: 2 });
  for (const [name, cls, h1, sub, screen] of slides) {
    await p.setContent(`<html><head><meta charset="utf-8"><style>${css}</style></head><body><div class="slide ${cls}"><div class="head"><h1>${h1}</h1><p>${sub}</p></div><div class="phone">${screen}</div></div></body></html>`);
    await p.screenshot({ path: `${name}.png` });
  }
  await b.close();
})();
