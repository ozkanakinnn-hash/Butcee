import base64, os, sys
from PIL import Image

ROOT = '/home/user/Butcee'
SHOTS = ROOT + '/docs/screenshots'
IOS = ROOT + '/docs/screenshots-ios'
OUT = ROOT + '/docs/store/screens'
FONT = ROOT + '/app/src/main/res/font'
os.makedirs(OUT, exist_ok=True)

def b64(path, crop=None, fmt='JPEG'):
    im = Image.open(path).convert('RGB')
    if crop:
        im = im.crop(crop)
    tmp = '/tmp/claude-0/promo/_tmp.jpg'
    im.save(tmp, quality=92)
    return 'data:image/jpeg;base64,' + base64.b64encode(open(tmp, 'rb').read()).decode()

def screen_img(name, ios=False, keep=1.0):
    """Durum çubuğunu keser; keep<1 ise alt kısmı (ör. test reklamı) kırpar."""
    p = (IOS if ios else SHOTS) + '/' + name + '.png'
    im = Image.open(p)
    w, h = im.size
    top = 162 if ios else 86
    bottom = int(top + (h - top) * keep)
    return b64(p, (0, top, w, bottom)), (bottom - top) / w

LOGO = 'data:image/png;base64,' + base64.b64encode(open(ROOT + '/docs/store/play_store_icon_512.png', 'rb').read()).decode()
WIDGET = 'data:image/png;base64,' + base64.b64encode(open(''+ROOT+'/scripts/store/widget_crop.png', 'rb').read()).decode()
MOSQUE = b64(ROOT + '/app/src/main/res/drawable-nodpi/mosque_sultanahmet.webp')

CSS = f"""
@font-face {{ font-family: InterX; src: url(file://{FONT}/inter_extrabold.ttf); }}
@font-face {{ font-family: InterR; src: url(file://{FONT}/inter_regular.ttf); }}
@font-face {{ font-family: Playfair; src: url(file://{FONT}/playfair_display_bold.ttf); }}
@font-face {{ font-family: Amiri; src: url(file://{FONT}/amiri_bold.ttf); }}
* {{ margin:0; padding:0; box-sizing:border-box; }}
body {{ width:1080px; height:1920px; overflow:hidden; background:#050505; position:relative; font-family:InterR; color:#EDEBE6; }}
.glow {{ position:absolute; inset:0;
  background:
    radial-gradient(900px 900px at 100% 100%, rgba(229,184,105,.55), rgba(229,184,105,0) 70%),
    radial-gradient(700px 600px at 0% 100%, rgba(158,107,32,.30), rgba(0,0,0,0) 70%),
    radial-gradient(600px 500px at 85% 8%, rgba(229,184,105,.10), rgba(0,0,0,0) 70%); }}
.pattern {{ position:absolute; inset:0; opacity:.07; }}
.brand {{ position:absolute; top:70px; left:84px; display:flex; align-items:center; gap:18px; }}
.brand img {{ width:64px; height:64px; border-radius:16px; }}
.brand span {{ font-family:Playfair; font-size:34px; color:#F5D98F; letter-spacing:.5px; }}
h1 {{ position:absolute; top:170px; left:84px; right:84px; font-family:InterX; font-size:104px; line-height:1.04; letter-spacing:-1.5px;
  background:linear-gradient(180deg,#FFE7A8 0%,#E5B869 55%,#C98A2C 100%); -webkit-background-clip:text; color:transparent; }}
p.sub {{ position:absolute; left:84px; right:110px; font-size:40px; line-height:1.32; color:#F2EEE6; }}
.phone {{ position:absolute; left:50%; transform:translateX(-50%); width:700px; border-radius:92px; padding:22px;
  background:linear-gradient(145deg,#4a4031,#1b1712 40%,#5b4d38 70%,#141210);
  box-shadow:0 0 0 3px #8c7650 inset, 0 40px 90px rgba(0,0,0,.75), 0 0 120px rgba(229,184,105,.18); }}
.screen {{ position:relative; width:656px; min-height:1480px; border-radius:72px; overflow:hidden; background:#050505; }}
.screen img.s {{ display:block; width:656px; }}
.sb {{ height:56px; display:flex; align-items:center; justify-content:space-between; padding:0 52px 0 58px; font-family:InterX; font-size:24px; color:#fff; background:#050505; }}
.island {{ position:absolute; top:14px; left:50%; transform:translateX(-50%); width:190px; height:42px; border-radius:22px; background:#000; }}
.sb .ic {{ display:flex; gap:10px; align-items:center; }}
.bar {{ width:6px; background:#fff; border-radius:2px; display:inline-block; }}
.side {{ position:absolute; width:6px; border-radius:3px; background:#6d5d42; }}
"""

PATTERN_SVG = """<svg class='pattern' width='1080' height='1920' xmlns='http://www.w3.org/2000/svg'>
<defs><pattern id='p' width='180' height='180' patternUnits='userSpaceOnUse'>
<path d='M90 20 L106 60 L150 46 L136 90 L176 106 L136 120 L150 164 L106 150 L90 190 L74 150 L30 164 L44 120 L4 106 L44 90 L30 46 L74 60 Z' fill='none' stroke='#E5B869' stroke-width='2'/>
<circle cx='90' cy='105' r='22' fill='none' stroke='#E5B869' stroke-width='1.5'/></pattern></defs>
<rect width='1080' height='1920' fill='url(#p)'/></svg>"""

def status_bar(dark=True):
    c = '#fff' if dark else '#111'
    bg = '#050505' if dark else '#FDFBF7'
    return f"""<div class='sb' style='color:{c};background:{bg}'><span>21:45</span><div class='island'></div>
<div class='ic'><span><i class='bar' style='height:10px;background:{c}'></i> <i class='bar' style='height:14px;background:{c}'></i> <i class='bar' style='height:18px;background:{c}'></i> <i class='bar' style='height:22px;background:{c}'></i></span>
<span style='display:inline-block;width:44px;height:22px;border:2.5px solid {c};border-radius:7px;position:relative'><span style='position:absolute;left:3px;top:3px;bottom:3px;width:28px;background:{c};border-radius:3px'></span></span></div></div>"""

def phone(inner, top, screen_h=None):
    style = f"top:{top}px"
    sh = f"height:{screen_h}px;" if screen_h else ''
    return f"""<div class='phone' style='{style}'>
<div class='side' style='left:-6px;top:300px;height:110px'></div><div class='side' style='left:-6px;top:440px;height:110px'></div>
<div class='side' style='right:-6px;top:360px;height:170px'></div>
<div class='screen' style='{sh}'>{inner}</div></div>"""

def page(title, sub, body, sub_top):
    return f"""<html><head><meta charset='utf-8'><style>{CSS}</style></head><body>
<div class='glow'></div>{PATTERN_SVG}
<div class='brand'><img src='{LOGO}'><span>Ramazan Rehberi</span></div>
<h1>{title}</h1><p class='sub' style='top:{sub_top}px'>{sub}</p>
{body}</body></html>"""

def shot_slide(fn, title, sub, shot, ios=False, keep=1.0, lines=2, light=False):
    src, ratio = screen_img(shot, ios, keep)
    h1_h = 108 * lines
    sub_top = 170 + h1_h + 34
    phone_top = sub_top + 3 * 53 + 70
    inner = status_bar(not light) + f"<img class='s' src='{src}'>"
    return fn, page(title, sub, phone(inner, phone_top), sub_top)

def notif_slide():
    inner = f"""<div style='position:relative;height:1480px;background:url({MOSQUE}) center/cover'>
<div style='position:absolute;inset:0;background:linear-gradient(180deg,rgba(5,8,20,.35),rgba(5,8,20,.85))'></div>
{status_bar(True).replace("background:#050505","background:transparent")}
<div style='position:relative;text-align:center;color:#fff;margin-top:40px'>
<div style='font-family:InterR;font-size:30px;opacity:.9'>Cuma, 19 Şubat</div>
<div style='font-family:InterX;font-size:150px;line-height:1.05;letter-spacing:-3px'>18:57</div></div>
<div style='position:relative;margin:40px 30px 0;display:flex;flex-direction:column;gap:18px'>
""" + ''.join(f"""<div style='background:rgba(30,26,20,.78);backdrop-filter:blur(20px);border-radius:34px;padding:24px 28px;display:flex;gap:20px;align-items:flex-start;border:1px solid rgba(229,184,105,.25)'>
<img src='{LOGO}' style='width:60px;height:60px;border-radius:14px'>
<div style='flex:1'><div style='display:flex;justify-content:space-between;font-size:22px;color:#cfc6b4'><span>RAMAZAN REHBERİ</span><span>{t}</span></div>
<div style='font-family:InterX;font-size:28px;color:#fff;margin-top:6px'>{a}</div>
<div style='font-size:25px;color:#e9e3d6;margin-top:4px;line-height:1.3'>{b}</div></div></div>""" for t, a, b in [
        ("şimdi", "🕌 Akşam Ezanı • İftar Vakti", "Manisa / Saruhanlı için iftar vakti (18:57). Allah orucunuzu kabul etsin! 🤲"),
        ("15 dk önce", "🍽️ İftara 15 dakika kaldı", "Sofranızı hazırlarken dualarınızı da hazırlayın."),
        ("04:35", "🌙 Sahura 45 dakika kaldı", "Sahur yemeğinde bereket vardır. Kalkın, niyetinizi tazeleyin."),
    ]) + f"""</div>
<img src='{WIDGET}' style='position:relative;display:block;width:596px;margin:34px auto 0;border-radius:40px;box-shadow:0 20px 50px rgba(0,0,0,.6)'>
</div>"""
    title = "Ezanı Vaktinde<br>Duyun"
    sub_top = 170 + 216 + 34
    return 'ss03_bildirim.png', page(title, "Ezan sesli vakit bildirimleri, sahur ve iftar hatırlatıcıları, ana ekran widget'ları.", phone(inner, sub_top + 3 * 53 + 70), sub_top)

slides = [
    shot_slide('ss01_ramazan.png', "Ramazan'ı Dolu<br>Dolu Yaşayın", "İftar ve sahur sayacı, fener süslemeleri ve her gün değişen mahya ile Ramazan ruhu cebinizde.", '16_ramazan_anasayfa'),
    shot_slide('ss02_oruc_ilerleme.png', "Orucunuzun Ne<br>Kadarı Tamam?", "Yüzde ilerleme çubuğu, iftara kalan süre ve sizi motive eden ayet ve hadisler.", '16b_ramazan_oruc_ilerleme'),
    notif_slide(),
    shot_slide('ss04_kuran.png', "Kur'an-ı Kerim<br>Sayfa Sayfa", "604 sayfa Medine mushafı; Arapça, Türkçe okunuş ve meal tek dokunuşla.", '04b_okuyucu_bakara'),
    shot_slide('ss05_hatim.png', "Hatminizi<br>Takip Edin", "Okuduğunuz cüz ve sureleri işaretleyin, ilerlemenizi görün, hatimlerinizi sayın.", '04f_hatim'),
    shot_slide('ss06_kible.png', "Kıbleyi<br>Hassas Bulun", "Pusula ile kıble yönü, günün Kıble Saati ve yakındaki camilere yol tarifi.", '07_kible'),
    shot_slide('ss07_takvim.png', "Oruç Takvimi ve<br>Dini Günler", "Oruçlarınızı planlayın, kaza borcunuzu takip edin; kandil ve bayramları kaçırmayın.", '05_takvim'),
    shot_slide('ss08_kartlar.png', "Sevdiklerinize<br>Özel Gün Kartı", "Cuma, kandil ve bayramlar için ayet ve hadisli kartlar; WhatsApp ve Instagram'da paylaşın.", '15_kart_kadir'),
    shot_slide('ss09_menu.png', "Günün İftar ve<br>Sahur Menüsü", "Her güne özel menü önerileri, orucun faydaları ve sağlıklı beslenme ipuçları.", '06n_rehber_sahur_menu', keep=0.715),
    shot_slide('ss10_zikirmatik.png', "Dijital<br>Zikirmatik", "Serbest, 33, 99 ve tesbihat modları; titreşimli sayaç ve kaydedilen zikirleriniz.", '01c_zikirmatik_7'),
    shot_slide('ss11_manevi_iklim.png', "Günün Ayeti,<br>Hadisi ve Duası", "Her gün yeni bir ayet, hadis ve dua; tek dokunuşla sevdiklerinizle paylaşın.", '11_manevi_iklim'),
    shot_slide('ss12_dahasi.png', "Hepsi Tek<br>Uygulamada", "Kaza namazı, kandil duaları, Esmâü'l-Hüsnâ, bildirim sihirbazı ve daha fazlası.", '06a_dahasi'),
]

# Öne çıkan görsel (1024x500)
home_src, _ = screen_img('16_ramazan_anasayfa')
card_src, _ = screen_img('04b_okuyucu_bakara')
FEATURE = f"""<html><head><meta charset='utf-8'><style>{CSS}
body{{width:1024px;height:500px}}
.glow{{background:radial-gradient(600px 500px at 85% 100%, rgba(229,184,105,.55), rgba(0,0,0,0) 70%), radial-gradient(500px 400px at 0% 0%, rgba(158,107,32,.25), rgba(0,0,0,0) 70%)}}
.mini{{position:absolute;width:250px;border-radius:34px;padding:9px;background:linear-gradient(145deg,#4a4031,#1b1712 40%,#5b4d38 70%,#141210);box-shadow:0 0 0 2px #8c7650 inset,0 20px 50px rgba(0,0,0,.7)}}
.mini img{{display:block;width:232px;border-radius:26px}}
</style></head><body><div class='glow'></div>{PATTERN_SVG}
<img src='{LOGO}' style='position:absolute;left:64px;top:92px;width:110px;height:110px;border-radius:26px;box-shadow:0 10px 30px rgba(0,0,0,.6)'>
<div style='position:absolute;left:64px;top:222px;font-family:Playfair;font-size:62px;color:#F5D98F;line-height:1.05'>Ramazan<br>Rehberi</div>
<div style='position:absolute;left:66px;top:368px;width:470px;font-size:24px;line-height:1.35;color:#EDEBE6'>Namaz vakitleri • İftar & sahur sayacı • Kur'an-ı Kerim • Kıble</div>
<div class='mini' style='left:560px;top:60px;transform:rotate(-8deg)'><img src='{card_src}'></div>
<div class='mini' style='left:740px;top:40px;transform:rotate(6deg)'><img src='{home_src}'></div>
</body></html>"""

import json
jobs=[]
for fn, html in slides:
    f = '/tmp/claude-0/promo/' + fn + '.html'
    open(f, 'w').write(html)
    jobs.append([f, OUT + '/' + fn, 1080, 1920])
f = '/tmp/claude-0/promo/feature.html'; open(f, 'w').write(FEATURE)
jobs.append([f, ROOT + '/docs/store/feature_graphic_1024x500.png', 1024, 500])
json.dump(jobs, open('/tmp/claude-0/promo/jobs.json','w'))
