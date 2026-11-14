"""Play Store tanıtım videosu (1920x1080, 30 fps, ~33 sn) üretir.

Girdi : docs/store/screens/ss0*.png (mağaza görselleri; telefon çerçevesi kırpılır)
Çıktı : docs/store/ezan-vakit-tanitim.mp4
Kullanım: python3 scripts/store/make_promo_video.py   (Playwright + ffmpeg gerekir)

Sahneler HTML olarak çizilir; window.render(t) her kare için zamanı ayarlar, Playwright
kare kare ekran görüntüsü alır, ffmpeg H.264 MP4'e çevirir (deterministik, takılma yok).
"""
import base64, io, json, os, shutil, subprocess, sys
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
STORE = ROOT + '/docs/store'
FONT = ROOT + '/app/src/main/res/font'
WORK = os.environ.get('PROMO_WORK', '/tmp/claude-0/promo-video')
OUT = STORE + '/ezan-vakit-tanitim.mp4'
FPS = 30

SCENES = [
    ('ss01_ezan', "Ezanı Vaktinde<br>Duyun", "Ezan sesli vakit bildirimleri, canlı iftar sayacı, sahur ve Cuma hatırlatıcıları."),
    ('ss02_ramazan', "İftar ve Sahur<br>Sayacı", "Dakikası dakikasına vakitler, iftara kalan süre ve oruç ilerlemesi."),
    ('ss03_kible', "Kıbleyi<br>Hassas Bulun", "Titremeyen pusula, resmî kıble açısı ve güneşle kontrol için Kıble Saati."),
    ('ss04_imsakiye', "30 Günlük<br>İmsakiye", "Resmî ilçe vakitleri; tek dokunuşla PDF olarak paylaşın."),
    ('ss05_kuran', "Kur'an-ı Kerim<br>Sayfa Sayfa", "604 sayfa mushaf; Arapça, okunuş ve meal. Hatminizi takip edin."),
    ('ss06_widgetlar', "Ana Ekranda<br>Şık Widget'lar", "Sıradaki vakit, günün vakitleri, iftar sayacı ve günün ayeti."),
    ('ss07_fitre_zekat', "Fitre, Fidye ve<br>Zekât Hesabı", "Güncel tutarlar ve nisap kontrolüyle zekât hesaplayıcı."),
    ('ss08_hepsi', "Hepsi Tek<br>Uygulamada", "Zikirmatik, kaza namazı, Esmâü'l-Hüsnâ, kandiller ve daha fazlası."),
]
INTRO, SCENE, OUTRO = 3.5, 3.2, 4.0
TOTAL = INTRO + SCENE * len(SCENES) + OUTRO


def b64(im, fmt='PNG'):
    buf = io.BytesIO(); im.save(buf, fmt)
    return f'data:image/{fmt.lower()};base64,' + base64.b64encode(buf.getvalue()).decode()


def phone_crop(name):
    """Mağaza görselindeki telefonu (x 190–890, y 649–alt) yuvarlak köşeli saydam PNG olarak keser."""
    im = Image.open(f'{STORE}/screens/{name}.png').convert('RGBA').crop((184, 649, 896, 1920))
    mask = Image.new('L', im.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((6, 0, im.size[0] - 7, im.size[1] + 200), radius=92, fill=255)
    # yan tuşlar (çerçevenin dışına taşan ince çubuklar)
    d = ImageDraw.Draw(mask)
    d.rectangle((0, 300, 6, 410), fill=255); d.rectangle((0, 440, 6, 550), fill=255)
    d.rectangle((im.size[0] - 7, 360, im.size[0], 530), fill=255)
    im.putalpha(mask)
    return b64(im.resize((im.size[0] * 3 // 4, im.size[1] * 3 // 4), Image.LANCZOS))


def html():
    icon = b64(Image.open(f'{STORE}/play_store_icon_512.png').convert('RGBA'))
    scenes = ''.join(f"""
<div class='scene' id='s{i}'>
  <div class='txt'><h1>{t}</h1><p>{s}</p></div>
  <img class='ph' src='{phone_crop(n)}'>
</div>""" for i, (n, t, s) in enumerate(SCENES))
    return f"""<!doctype html><html><head><meta charset='utf-8'><style>
@font-face {{ font-family: InterX; src: url(file://{FONT}/inter_extrabold.ttf); }}
@font-face {{ font-family: InterR; src: url(file://{FONT}/inter_regular.ttf); }}
@font-face {{ font-family: Playfair; src: url(file://{FONT}/playfair_display_bold.ttf); }}
* {{ margin:0; padding:0; box-sizing:border-box; }}
body {{ width:1920px; height:1080px; overflow:hidden; background:#050505; position:relative; font-family:InterR; color:#EDEBE6; }}
#glow {{ position:absolute; inset:-200px; background:
  radial-gradient(1100px 900px at 100% 100%, rgba(229,184,105,.45), rgba(229,184,105,0) 70%),
  radial-gradient(900px 700px at 0% 100%, rgba(158,107,32,.25), rgba(0,0,0,0) 70%),
  radial-gradient(800px 600px at 30% 0%, rgba(229,184,105,.10), rgba(0,0,0,0) 70%); }}
#pat {{ position:absolute; left:0; top:0; width:2300px; height:1500px; opacity:.07; }}
.brand {{ position:absolute; top:64px; left:110px; display:flex; align-items:center; gap:18px; }}
.brand img {{ width:60px; height:60px; border-radius:15px; }}
.brand span {{ font-family:Playfair; font-size:32px; color:#F5D98F; }}
.scene {{ position:absolute; inset:0; opacity:0; }}
.txt {{ position:absolute; left:110px; top:250px; width:880px; }}
h1 {{ font-family:InterX; font-size:104px; line-height:1.05; letter-spacing:-1.5px;
  background:linear-gradient(180deg,#FFE7A8 0%,#E5B869 55%,#C98A2C 100%); -webkit-background-clip:text; color:transparent; }}
.txt p {{ margin-top:36px; font-size:40px; line-height:1.35; color:#F2EEE6; width:820px; }}
.ph {{ position:absolute; left:1150px; top:185px; width:534px;
  filter: drop-shadow(0 30px 60px rgba(0,0,0,.75)) drop-shadow(0 0 80px rgba(229,184,105,.18)); }}
#intro, #outro {{ position:absolute; inset:0; display:flex; flex-direction:column; align-items:center; justify-content:center; opacity:0; }}
#intro img, #outro img {{ width:220px; height:220px; border-radius:52px; box-shadow:0 30px 80px rgba(0,0,0,.7), 0 0 120px rgba(229,184,105,.25); }}
.big {{ font-family:Playfair; font-size:130px; color:#F5D98F; margin-top:40px; line-height:1; }}
.tag {{ font-size:42px; color:#F2EEE6; margin-top:28px; letter-spacing:.5px; }}
.cta {{ margin-top:46px; padding:22px 56px; border-radius:60px; font-family:InterX; font-size:38px; color:#1b1408;
  background:linear-gradient(180deg,#FFE7A8,#E5B869 60%,#C98A2C); box-shadow:0 14px 40px rgba(229,184,105,.35); }}
.bar {{ position:absolute; left:110px; bottom:70px; height:5px; width:240px; border-radius:3px; background:rgba(229,184,105,.18); }}
.bar i {{ display:block; height:100%; border-radius:3px; background:#E5B869; width:0; }}
</style></head><body>
<div id='glow'></div>
<svg id='pat' xmlns='http://www.w3.org/2000/svg'><defs><pattern id='p' width='180' height='180' patternUnits='userSpaceOnUse'>
<path d='M90 20 L106 60 L150 46 L136 90 L176 106 L136 120 L150 164 L106 150 L90 190 L74 150 L30 164 L44 120 L4 106 L44 90 L30 46 L74 60 Z' fill='none' stroke='#E5B869' stroke-width='2'/>
<circle cx='90' cy='105' r='22' fill='none' stroke='#E5B869' stroke-width='1.5'/></pattern></defs>
<rect width='2300' height='1500' fill='url(#p)'/></svg>
<div id='intro'><img src='{icon}'><div class='big'>Ezan &amp; Vakit</div><div class='tag'>Ramazan Rehberi • Vakitler • Kıble • Kur'an-ı Kerim</div></div>
<div class='brand' id='brand'><img src='{icon}'><span>Ezan &amp; Vakit</span></div>
{scenes}
<div class='bar' id='bar'><i id='bari'></i></div>
<div id='outro'><img src='{icon}'><div class='big'>Ezan &amp; Vakit</div>
<div class='tag'>Vakitler, oruç, kıble ve Kur'an — hepsi tek uygulamada.</div><div class='cta'>Hemen ücretsiz indirin</div></div>
<script>
const INTRO={INTRO}, SCENE={SCENE}, N={len(SCENES)}, TOTAL={TOTAL};
const cl=(x)=>Math.max(0,Math.min(1,x)), ease=(x)=>{{x=cl(x);return 1-Math.pow(1-x,3);}};
const io=(t,a,b,fi,fo)=>Math.min(ease((t-a)/fi), ease((b-t)/fo));
window.render=(t)=>{{
  document.getElementById('pat').style.transform=`translate(${{-t*6}}px,${{-t*4}}px)`;
  // giriş
  const it=document.getElementById('intro'); const a=io(t,0,INTRO,0.6,0.5);
  it.style.opacity=a; it.style.transform=`scale(${{0.92+0.08*ease(t/1.2)}})`;
  it.children[0].style.transform=`scale(${{0.6+0.4*ease(t/0.8)}})`;
  for(let k=1;k<3;k++){{const e=ease((t-0.3*k)/0.7); it.children[k].style.opacity=e; it.children[k].style.transform=`translateY(${{(1-e)*30}}px)`;}}
  // sahneler
  const s0=INTRO, s1=INTRO+SCENE*N;
  const br=io(t,s0,s1,0.5,0.4); document.getElementById('brand').style.opacity=br; document.getElementById('bar').style.opacity=br;
  document.getElementById('bari').style.width=(cl((t-s0)/(s1-s0))*100)+'%';
  for(let i=0;i<N;i++){{
    const st=s0+i*SCENE, en=st+SCENE, el=document.getElementById('s'+i);
    const v=io(t,st,en,0.45,0.35); el.style.opacity=v;
    const lt=t-st, h=el.querySelector('h1'), p=el.querySelector('p'), ph=el.querySelector('.ph');
    const eh=ease(lt/0.6), ep=ease((lt-0.2)/0.6), eph=ease((lt-0.05)/0.8);
    h.style.transform=`translateX(${{(1-eh)*-60}}px)`; h.style.opacity=eh;
    p.style.transform=`translateY(${{(1-ep)*24}}px)`; p.style.opacity=ep;
    ph.style.transform=`translateY(${{(1-eph)*260 - lt*14}}px)`;
  }}
  // kapanış
  const ot=t-s1, o=ease(ot/0.6); const out=document.getElementById('outro'); out.style.opacity=o;
  out.children[0].style.transform=`scale(${{0.7+0.3*ease(ot/0.7)}})`;
  for(let k=1;k<4;k++){{const e=ease((ot-0.25*k)/0.6); out.children[k].style.opacity=e; out.children[k].style.transform=`translateY(${{(1-e)*30}}px)`;}}
  const fade=cl((TOTAL-t)/0.5); document.body.style.filter=`brightness(${{fade}})`;
}};
</script></body></html>"""


def main():
    shutil.rmtree(WORK, ignore_errors=True); os.makedirs(WORK + '/frames')
    open(WORK + '/promo.html', 'w').write(html())
    frames = int(TOTAL * FPS)
    js = f"""const {{ chromium }} = require('playwright');
(async () => {{
  const b = await chromium.launch();
  const p = await b.newPage({{ viewport: {{ width: 1920, height: 1080 }} }});
  await p.goto('file://{WORK}/promo.html'); await p.waitForTimeout(800);
  for (let f = 0; f < {frames}; f++) {{
    await p.evaluate(t => window.render(t), f / {FPS});
    await p.screenshot({{ path: '{WORK}/frames/f' + String(f).padStart(5, '0') + '.jpg', type: 'jpeg', quality: 95 }});
  }}
  await b.close();
}})();"""
    open(WORK + '/rec.js', 'w').write(js)
    subprocess.run(['node', WORK + '/rec.js'], check=True)
    subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-framerate', str(FPS), '-i', WORK + '/frames/f%05d.jpg',
                    '-f', 'lavfi', '-i', 'anullsrc=r=48000:cl=stereo', '-shortest',
                    '-c:v', 'libx264', '-preset', 'slow', '-crf', '18', '-pix_fmt', 'yuv420p', '-profile:v', 'high',
                    '-c:a', 'aac', '-b:a', '128k', '-movflags', '+faststart', OUT], check=True)
    print(OUT, f'{os.path.getsize(OUT) / 1e6:.1f} MB', f'{TOTAL:.1f} sn')


if __name__ == '__main__':
    main()
