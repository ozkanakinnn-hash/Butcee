# Logo B (Gülümseyen Kâse) — palet 3 varyasyonları. Her biri 200x200 viewBox, yuvarlatılmış kare simge.
IND="#4F63D8"; IND_D="#3446B0"; IND_L="#ECEEFC"; COR="#F08A6C"; COR_D="#D96A4B"; TEAL="#2E9C84"; TEAL_L="#5CCBB1"; CREAM="#F7F6FB"; NAVY="#0D0E16"; NAVY2="#151726"; WHITE="#FFFFFF"
def logo(uid,bg,bowl,bowl2,rim,face,cheek,leaf,leaf2,steam=None,spoon=None,outline=None,bgstroke=None,food=None):
    g=f'<linearGradient id="{uid}g" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{bowl}"/><stop offset="1" stop-color="{bowl2}"/></linearGradient>'
    s=f'<svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg"><defs>{g}</defs>'
    s+=f'<rect width="200" height="200" rx="46" fill="{bg}"' + (f' stroke="{bgstroke}" stroke-width="3"' if bgstroke else '') + '/>'
    if steam: s+=f'<path d="M78 70 q-8 -12 0 -24 M122 70 q-8 -12 0 -24" stroke="{steam}" stroke-width="6" fill="none" stroke-linecap="round" opacity=".85"/>'
    # yaprak-kalp (iki yaprak birleşip kalp oluşturur)
    s+=f'<path d="M100 92 C86 78 66 74 62 58 C60 46 76 40 88 48 C94 52 98 58 100 64 Z" fill="{leaf}"/>'
    s+=f'<path d="M100 92 C114 78 134 74 138 58 C140 46 124 40 112 48 C106 52 102 58 100 64 Z" fill="{leaf2}"/>'
    s+=f'<path d="M100 66 V90" stroke="{bg}" stroke-width="3" stroke-linecap="round" opacity=".7"/>'
    if food: s+=f'<ellipse cx="100" cy="104" rx="58" ry="10" fill="{food}"/>'
    # kâse
    if outline:
        s+=f'<path d="M38 104 H162 Q160 166 100 168 Q40 166 38 104Z" fill="none" stroke="{outline}" stroke-width="9" stroke-linejoin="round"/>'
    else:
        s+=f'<path d="M38 104 H162 Q160 166 100 168 Q40 166 38 104Z" fill="url(#{uid}g)"/>'
    s+=f'<path d="M30 104 H170" stroke="{rim}" stroke-width="9" stroke-linecap="round"/>'
    s+=f'<path d="M76 172 H124" stroke="{rim}" stroke-width="7" stroke-linecap="round"/>'
    # yüz: kapalı mutlu gözler, gülümseme, yanaklar
    s+=f'<path d="M72 124 q7 -7 14 0 M114 124 q7 -7 14 0" stroke="{face}" stroke-width="5" fill="none" stroke-linecap="round"/>'
    s+=f'<path d="M86 138 Q100 152 114 138" stroke="{face}" stroke-width="5.5" fill="none" stroke-linecap="round"/>'
    s+=f'<circle cx="66" cy="138" r="7" fill="{cheek}" opacity=".85"/><circle cx="134" cy="138" r="7" fill="{cheek}" opacity=".85"/>'
    if spoon: s+=f'<path d="M150 98 L172 52" stroke="{spoon}" stroke-width="8" stroke-linecap="round"/><ellipse cx="176" cy="44" rx="9" ry="13" fill="{spoon}" transform="rotate(25 176 44)"/>'
    return s+'</svg>'
V=[
 ("B1","İndigo Kâse","Krem zemin, indigo kâse, deniz yeşili yaprak-kalp, mercan yanaklar. Paletin en dengeli hâli.",
   logo("b1",CREAM,IND,IND_D,IND_D,WHITE,COR,TEAL,TEAL_L)),
 ("B2","Ters (İndigo Zemin)","İndigo zemin üzerinde beyaz kâse; mercan kalp. Mağazada en çok dikkat çeken, simge olarak en net.",
   logo("b2",IND,WHITE,"#E4E7FA",IND_L,IND,COR,COR,"#F5A58C")),
 ("B3","Mercan Kâse","Açık indigo zemin, sıcak mercan kâse, deniz yeşili yaprak. Daha sıcak ve iştah açıcı.",
   logo("b3",IND_L,COR,COR_D,COR_D,WHITE,"#FFD2C4",TEAL,TEAL_L,steam=IND)),
 ("B4","Geçişli Kâse","Uygulamadaki indigo→mercan geçişini taşır; beyaz zeminde modern ve canlı.",
   logo("b4",WHITE,"#6A5CD8",COR,IND_D,WHITE,"#FFD2C4",TEAL,TEAL_L,bgstroke="#E6E4F0")),
 ("B5","Kaşıklı Kâse","B1'e kaşık ve mama eklenmiş; 'beslenme' vurgusu daha belirgin.",
   logo("b5",CREAM,IND,IND_D,IND_D,WHITE,COR,TEAL,TEAL_L,spoon=COR,food="#F6C9A8")),
 ("B6","Çizgi (Minimal)","Tek renk indigo çizgi; en sade ve kurumsal. Bildirim ikonu ve küçük boyutlar için ideal.",
   logo("b6",WHITE,IND,IND,IND,IND,COR,TEAL,TEAL_L,outline=IND,bgstroke="#E6E4F0")),
]
def lock(svg,name_c,tag_c,bg="#FFFFFF",bd="#EDEBF5"):
    return f'<div class="lock" style="background:{bg};border-color:{bd}"><div class="ic">{svg}</div><div><div class="wm" style="color:{name_c}">minik<b>gurme</b></div><div class="tg" style="color:{tag_c}">ek gıda takibi</div></div></div>'
cards=''
for k,t,d,svg in V:
    circ=svg.replace('rx="46"','rx="100"')
    cards+=f'''<div class="card"><div class="hd"><span class="no">{k}</span><div><h3>{t}</h3><p>{d}</p></div></div>
<div class="row"><div class="big">{svg}</div><div class="sizes"><div class="circ">{circ}</div><div class="s48">{svg}</div><div class="s28">{svg}</div></div></div>
{lock(svg,IND_D,TEAL)}{lock(svg,"#ECEDF7",TEAL_L,bg=NAVY2,bd="#262942")}
<div class="phone"><div class="home">{''.join(f'<div class="app"><div class="ai">{x}</div><span>{n}</span></div>' for x,n in [(svg,"Minik Gurme"),('<div class="ph1"></div>',"Takvim"),('<div class="ph2"></div>',"Fotoğraflar"),('<div class="ph3"></div>',"Saat")])}</div></div></div>'''
html=f'''<!doctype html><html lang="tr"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Logo B Seçenekleri</title>
<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;800&display=swap" rel="stylesheet">
<style>
:root{{--bg:#F7F6FB;--card:#fff;--tx:#1F2238;--mu:#555A75;--bd:#E6E4F0}}
@media (prefers-color-scheme:dark){{:root:not([data-theme=light]){{--bg:#0D0E16;--card:#151726;--tx:#ECEDF7;--mu:#B7BAD0;--bd:#262942}}}}
:root[data-theme=dark]{{--bg:#0D0E16;--card:#151726;--tx:#ECEDF7;--mu:#B7BAD0;--bd:#262942}}
body{{margin:0;background:var(--bg);color:var(--tx);font:15px/1.5 'Plus Jakarta Sans',system-ui,sans-serif}}
main{{max-width:1120px;margin:0 auto;padding:24px 16px 48px}} h1{{margin:0 0 4px;font-size:26px}} .lead{{color:var(--mu);margin:0 0 18px}}
.grid{{display:grid;grid-template-columns:repeat(auto-fill,minmax(340px,1fr));gap:16px}}
.card{{background:var(--card);border:1px solid var(--bd);border-radius:20px;padding:16px}}
.hd{{display:flex;gap:10px;margin-bottom:12px}} .hd h3{{margin:0;font-size:16px}} .hd p{{margin:2px 0 0;color:var(--mu);font-size:13px}}
.no{{flex:none;min-width:34px;height:28px;border-radius:14px;background:#4F63D8;color:#fff;display:grid;place-items:center;font-weight:800;font-size:13px}}
.row{{display:flex;gap:14px;align-items:center}} .big svg{{width:130px;height:130px;display:block}}
.sizes{{display:flex;gap:10px;align-items:flex-end}} .circ svg{{width:72px;height:72px}} .s48 svg{{width:48px;height:48px}} .s28 svg{{width:28px;height:28px}}
.lock{{display:flex;gap:10px;align-items:center;margin-top:10px;border:1px solid;border-radius:14px;padding:10px}} .lock .ic svg{{width:44px;height:44px;display:block}}
.wm{{font-size:23px;font-weight:600;letter-spacing:-.4px;line-height:1.1}} .wm b{{font-weight:800}} .tg{{font-size:12px;font-weight:600}}
.phone{{margin-top:10px;border-radius:16px;padding:12px;background:linear-gradient(160deg,#3B4A9E,#7A62D6 60%,#F08A6C)}}
.home{{display:flex;justify-content:space-around}} .app{{display:flex;flex-direction:column;align-items:center;gap:4px;color:#fff;font-size:10px;font-weight:600}}
.ai,.ai svg{{width:46px;height:46px;border-radius:50%;overflow:hidden;display:block}} .ph1{{width:46px;height:46px;background:#fff;border-radius:50%}} .ph2{{width:46px;height:46px;background:conic-gradient(#F2C14E,#F08A6C,#7A5CD6,#2E9C84,#F2C14E);border-radius:50%}} .ph3{{width:46px;height:46px;background:#1F2238;border-radius:50%}}
</style></head><body><main><h1>Logo B · Gülümseyen Kâse — palet varyasyonları</h1>
<p class="lead">Her kartta: simge, telefonun yuvarlak maskesi, küçük boyutlar, açık ve koyu zeminde yazıyla kullanım, ana ekran görünümü.</p>
<div class="grid">{cards}</div></main></body></html>'''
open('MinikGurme-logo-B-secenekleri.html','w').write(html); print(len(html))
