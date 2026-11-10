# Minik Gurme besin çizimleri — elle tasarlanmış SVG'ler (256x256), yumuşak gölgeli yarı gerçekçi stil.
def lg(id,c1,c2,x1=0,y1=0,x2=0,y2=1): return f'<linearGradient id="{id}" x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}"><stop offset="0" stop-color="{c1}"/><stop offset="1" stop-color="{c2}"/></linearGradient>'
def rg(id,c1,c2,cx=.4,cy=.35,r=.75): return f'<radialGradient id="{id}" cx="{cx}" cy="{cy}" r="{r}"><stop offset="0" stop-color="{c1}"/><stop offset="1" stop-color="{c2}"/></radialGradient>'
SH='<ellipse cx="128" cy="226" rx="78" ry="12" fill="#000" opacity=".12"/>'
def svg(defs,body,shadow=True): return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256"><defs>{defs}</defs>{SH if shadow else ""}{body}</svg>'
def bowl(fill_defs,fill_body,bowl1="#F4F1EC",bowl2="#D9D2C7",rim="#FFFFFF"):
    d=lg('bw',bowl1,bowl2)+fill_defs
    b=f'<path d="M34 128 Q38 214 128 216 Q218 214 222 128 Z" fill="url(#bw)"/>'+fill_body+f'<ellipse cx="128" cy="128" rx="94" ry="20" fill="none" stroke="{rim}" stroke-width="6"/><path d="M58 150 Q66 190 104 202" stroke="#fff" stroke-width="7" fill="none" opacity=".55" stroke-linecap="round"/>'
    return d,b
A={}
# 🥑 avokado (yarım + bütün)
A['🥑']=svg(lg('a1','#5E8C2F','#2F5216')+rg('a2','#F3F7B5','#B9D46A',.45,.4,.7)+rg('a3','#A36B3C','#5B3418',.35,.3,.8)+lg('a4','#3E6B1E','#1F3A0C'),
 '<path d="M150 40 C196 36 226 92 214 152 C204 200 168 214 146 206 C120 196 112 160 116 120 C120 80 124 44 150 40Z" fill="url(#a4)"/>'
 '<path d="M96 70 C140 62 176 110 172 160 C168 204 132 222 102 214 C66 204 48 170 54 134 C60 96 70 74 96 70Z" fill="url(#a1)"/>'
 '<path d="M98 82 C134 78 162 116 158 158 C154 194 128 208 104 202 C76 196 62 168 66 136 C70 104 78 86 98 82Z" fill="url(#a2)"/>'
 '<circle cx="110" cy="152" r="28" fill="url(#a3)"/><ellipse cx="101" cy="142" rx="8" ry="5" fill="#fff" opacity=".35"/>')
# 🥒 sakız kabağı
A['🥒']=svg(lg('z1','#4C8A2E','#244A12',0,0,0,1)+rg('z2','#F2F6CF','#C9DB8C',.5,.5,.6),
 '<g transform="rotate(-28 128 128)"><rect x="24" y="96" width="196" height="66" rx="33" fill="url(#z1)"/>'
 '<path d="M44 108 H200 M40 128 H206 M44 148 H200" stroke="#7FB650" stroke-width="3" opacity=".55" stroke-linecap="round"/>'
 '<rect x="10" y="114" width="22" height="30" rx="6" fill="#6B5A2E"/>'
 '<ellipse cx="214" cy="129" rx="16" ry="33" fill="url(#z2)" stroke="#3D6E22" stroke-width="4"/><circle cx="214" cy="129" r="6" fill="#E3EBB0"/></g>')
# 🥕 havuç
A['🥕']=svg(lg('c1','#FFA23A','#E2610F',0,0,1,0),
 '<path d="M150 58 L196 104 L80 222 Q60 230 52 214 Q48 206 56 196 Z" fill="url(#c1)"/>'
 '<path d="M120 112 l18 16 M98 140 l16 14 M78 168 l14 12" stroke="#C4520C" stroke-width="5" stroke-linecap="round"/>'
 '<path d="M160 64 Q150 26 168 18 Q178 40 172 66Z M172 72 Q188 30 214 30 Q206 60 182 82Z M184 86 Q214 66 236 82 Q214 100 190 96Z" fill="#4E9A2C"/>')
# 🍠 tatlı patates
A['🍠']=svg(lg('s1','#B5523A','#7A2E1E')+rg('s2','#FFB24D','#F07A1A',.5,.5,.6),
 '<path d="M30 150 C30 98 90 70 150 74 C206 78 232 112 226 146 C220 182 170 204 112 200 C60 196 30 182 30 150Z" fill="url(#s1)"/>'
 '<ellipse cx="180" cy="140" rx="40" ry="52" fill="url(#s2)" stroke="#9A3B24" stroke-width="5"/>'
 '<path d="M70 120 q10 6 20 0 M96 168 q10 6 20 0" stroke="#5E2014" stroke-width="4" fill="none" opacity=".5"/>')
# 🥦 brokoli
A['🥦']=svg(lg('b1','#A7C96B','#6E9A3A')+rg('b2','#5FA33A','#2E6417',.4,.35,.7),
 '<path d="M108 140 L96 218 Q128 228 160 218 L148 140Z" fill="url(#b1)"/>'
 '<g fill="url(#b2)"><circle cx="82" cy="118" r="38"/><circle cx="128" cy="86" r="44"/><circle cx="176" cy="118" r="38"/><circle cx="128" cy="134" r="36"/></g>'
 '<g fill="#7CC04A" opacity=".55"><circle cx="112" cy="70" r="8"/><circle cx="140" cy="76" r="6"/><circle cx="70" cy="104" r="7"/><circle cx="168" cy="102" r="7"/><circle cx="124" cy="120" r="6"/></g>')
# 🎃 bal kabağı (dilim)
A['🎃']=svg(rg('p1','#FFB44A','#E46F12',.4,.35,.8),
 '<path d="M128 70 C70 64 32 104 36 150 C40 196 86 218 128 214 C170 218 216 196 220 150 C224 104 186 64 128 70Z" fill="url(#p1)"/>'
 '<path d="M128 72 C108 110 108 176 128 214 M128 72 C148 110 148 176 128 214 M76 82 C62 120 66 176 92 206 M180 82 C194 120 190 176 164 206" stroke="#C85A0C" stroke-width="5" fill="none" opacity=".6"/>'
 '<path d="M122 72 Q118 46 136 36 Q142 50 134 74Z" fill="#5C7A2A"/>')
# 🟢 bezelye (kabuk içinde)
A['🟢']=svg(lg('g1','#8BC34A','#4E8A1E')+rg('g2','#B7E36A','#5D9E26',.35,.3,.8),
 '<path d="M26 150 Q60 80 150 70 Q220 66 232 96 Q200 168 110 184 Q48 192 26 150Z" fill="url(#g1)"/>'
 '<path d="M44 148 Q80 104 150 96 Q200 92 214 104 Q186 150 110 162 Q60 168 44 148Z" fill="#E6F2C6"/>'
 '<g fill="url(#g2)"><circle cx="72" cy="140" r="17"/><circle cx="106" cy="130" r="18"/><circle cx="142" cy="120" r="18"/><circle cx="178" cy="110" r="16"/></g>'
 '<path d="M232 96 Q246 84 238 70" stroke="#4E8A1E" stroke-width="6" fill="none" stroke-linecap="round"/>')
# 🥬 ıspanak
A['🥬']=svg(lg('l1','#5DAA3A','#24601A')+lg('l2','#6FBF46','#2E7A1E'),
 '<path d="M128 214 C70 200 40 150 56 96 C66 62 100 46 120 70 C128 80 128 120 128 214Z" fill="url(#l1)"/>'
 '<path d="M128 214 C186 200 216 150 200 96 C190 62 156 46 136 70 C128 80 128 120 128 214Z" fill="url(#l2)"/>'
 '<path d="M128 214 V76 M128 150 L92 112 M128 120 L100 92 M128 150 L164 112 M128 120 L156 92" stroke="#C9E6A8" stroke-width="4" fill="none" stroke-linecap="round"/>')
# 🍎 kırmızı elma, 🍏 yeşil elma
def apple(c1,c2,leaf="#4E9A2C"):
    return svg(rg('ap',c1,c2,.38,.32,.8),
     '<path d="M128 76 C96 54 40 66 38 128 C36 186 84 222 112 214 C122 211 134 211 144 214 C172 222 220 186 218 128 C216 66 160 54 128 76Z" fill="url(#ap)"/>'
     '<path d="M126 78 Q122 50 132 34" stroke="#6B4423" stroke-width="7" fill="none" stroke-linecap="round"/>'
     f'<path d="M134 54 Q160 30 188 40 Q170 66 134 58Z" fill="{leaf}"/><ellipse cx="82" cy="110" rx="12" ry="22" fill="#fff" opacity=".35" transform="rotate(20 82 110)"/>')
A['🍎']=apple('#FF6B5E','#C0192B'); A['🍏']=apple('#C8E86A','#6FA52A')
# 🍐 armut
A['🍐']=svg(rg('pe','#E9EE7A','#A8B83A',.4,.4,.8),
 '<path d="M128 52 C108 52 102 80 102 100 C102 120 60 136 60 176 C60 212 96 224 128 222 C160 224 196 212 196 176 C196 136 154 120 154 100 C154 80 148 52 128 52Z" fill="url(#pe)"/>'
 '<path d="M128 54 Q126 34 138 22" stroke="#6B4423" stroke-width="6" fill="none" stroke-linecap="round"/><path d="M136 38 Q160 24 176 36 Q158 52 136 42Z" fill="#4E9A2C"/>'
 '<ellipse cx="94" cy="168" rx="10" ry="22" fill="#fff" opacity=".3"/>')
# 🍌 muz
A['🍌']=svg(lg('m1','#FFE36A','#F2B705',0,0,0,1),
 '<path d="M40 96 C60 180 150 214 214 160 C222 152 216 140 204 146 C150 174 92 160 66 92 C60 78 36 76 40 96Z" fill="url(#m1)"/>'
 '<path d="M56 90 C80 150 140 172 196 150" stroke="#E0A400" stroke-width="5" fill="none" opacity=".6"/>'
 '<path d="M36 86 L46 74 L60 82 L52 96Z" fill="#6B4E16"/><circle cx="212" cy="152" r="7" fill="#4A3510"/>')
# 🍑 şeftali
A['🍑']=svg(rg('sf','#FFC48A','#F06A4A',.38,.36,.8),
 '<path d="M128 70 C72 60 34 110 46 162 C58 210 108 224 128 210 C148 224 198 210 210 162 C222 110 184 60 128 70Z" fill="url(#sf)"/>'
 '<path d="M128 72 C114 120 116 176 128 210" stroke="#D9573A" stroke-width="5" fill="none" opacity=".5"/>'
 '<path d="M130 70 Q150 38 190 42 Q176 76 136 76Z" fill="#5E9E33"/><ellipse cx="86" cy="118" rx="12" ry="20" fill="#fff" opacity=".3"/>')
# 🫐 yaban mersini
def berry(x,y,r): return f'<circle cx="{x}" cy="{y}" r="{r}" fill="url(#bb)"/><path d="M{x-6} {y-r+8} l6 6 l6 -6" stroke="#2A2F5E" stroke-width="3" fill="none"/><circle cx="{x-r*.35}" cy="{y-r*.3}" r="{r*.2}" fill="#fff" opacity=".35"/>'
A['🫐']=svg(rg('bb','#7B8BE0','#2E347A',.35,.3,.8),berry(92,150,40)+berry(164,150,40)+berry(128,96,40))
# 🍓 çilek
A['🍓']=svg(rg('st','#FF6B6B','#C21830',.4,.3,.8),
 '<path d="M128 214 C70 180 44 120 62 92 C80 66 176 66 194 92 C212 120 186 180 128 214Z" fill="url(#st)"/>'
 +''.join(f'<ellipse cx="{x}" cy="{y}" rx="3" ry="5" fill="#FFE07A"/>' for x,y in [(96,110),(128,104),(160,110),(84,140),(116,138),(148,138),(176,140),(104,170),(136,168),(160,166),(124,194)])+
 '<path d="M128 78 L96 62 L112 84 L80 86 L112 96 L128 84 L144 96 L176 86 L144 84 L160 62Z" fill="#3E8E2A"/>')
# 🍊 mandalina
A['🍊']=svg(rg('or','#FFB547','#F07A0A',.38,.35,.8),
 '<circle cx="128" cy="136" r="84" fill="url(#or)"/>'+''.join(f'<circle cx="{x}" cy="{y}" r="2.5" fill="#E06C00" opacity=".5"/>' for x,y in [(90,110),(110,90),(160,100),(180,140),(150,180),(100,170),(80,140),(130,130)])+
 '<path d="M124 54 Q130 40 142 46 Q170 34 186 50 Q164 70 134 58Z" fill="#4E9A2C"/><ellipse cx="94" cy="104" rx="14" ry="22" fill="#fff" opacity=".3" transform="rotate(30 94 104)"/>')
# 🥝 kivi
A['🥝']=svg(lg('kv','#8A6A3A','#5A4220')+rg('kf','#E8F5B0','#6FAE2A',.5,.5,.6),
 '<ellipse cx="128" cy="140" rx="92" ry="72" fill="url(#kv)"/><ellipse cx="128" cy="140" rx="78" ry="60" fill="url(#kf)"/>'
 '<ellipse cx="128" cy="140" rx="24" ry="16" fill="#F6FBE0"/>'+''.join(f'<ellipse cx="{128+40*c}" cy="{140+30*s}" rx="3" ry="6" fill="#2E2A10" transform="rotate({a} {128+40*c} {140+30*s})"/>' for a,c,s in [(0,1,0),(45,.7,.7),(90,0,1),(135,-.7,.7),(0,-1,0),(45,-.7,-.7),(90,0,-1),(135,.7,-.7)]))
# 🥩 kıyma
A['🥩']=svg(rg('km','#E2655E','#9C2A24',.4,.35,.8),
 '<path d="M44 170 C40 120 80 84 130 82 C182 80 220 116 214 166 C210 200 170 212 128 212 C86 212 48 202 44 170Z" fill="url(#km)"/>'
 +''.join(f'<path d="M{x} {y} q8 -6 16 0" stroke="#F4A7A0" stroke-width="4" fill="none" stroke-linecap="round"/>' for x,y in [(80,130),(110,112),(146,110),(176,132),(92,160),(126,148),(160,160),(110,186),(146,188),(70,182),(186,172)])+
 '<g stroke="#7A1E1A" stroke-width="3" fill="none" opacity=".45">'+''.join(f'<path d="M{x} {y} q6 4 12 0"/>' for x,y in [(96,140),(132,128),(166,146),(120,172),(150,176)])+'</g>')
# 🥚 haşlanmış yumurta (yarım)
A['🥚']=svg(rg('eg','#FFFFFF','#E9E2D6',.4,.35,.8)+rg('yk','#FFD34D','#F29A0A',.4,.35,.7),
 '<ellipse cx="128" cy="140" rx="84" ry="74" fill="url(#eg)" stroke="#E2D8C8" stroke-width="3"/><circle cx="128" cy="140" r="40" fill="url(#yk)"/><ellipse cx="114" cy="126" rx="10" ry="7" fill="#fff" opacity=".4"/>')
# 🐟 somon fileto
A['🐟']=svg(lg('sm','#FFA07A','#F06A4A',0,0,1,1),
 '<path d="M30 150 C40 100 120 76 200 90 C230 96 234 120 226 150 C218 186 140 206 80 196 C46 190 26 178 30 150Z" fill="url(#sm)"/>'
 +''.join(f'<path d="M{x} {y} q14 30 6 64" stroke="#FFE0D2" stroke-width="5" fill="none" opacity=".85"/>' for x,y in [(70,104),(104,92),(138,88),(172,92),(202,104)])+
 '<path d="M30 150 C60 172 140 184 226 150 C220 184 140 206 80 196 C46 190 26 178 30 150Z" fill="#B9B2AE" opacity=".6"/>')
# 🍗 tavuk but
A['🍗']=svg(rg('tv','#F2B66A','#B8692A',.38,.3,.8),
 '<path d="M64 112 C64 70 110 46 150 62 C196 80 200 134 168 156 C150 168 130 168 112 164 L80 196 C74 202 64 200 60 192 C56 186 50 186 46 190 C38 194 30 186 34 178 C38 170 34 164 28 162 C20 160 20 148 28 144 L58 128 C60 122 64 118 64 112Z" fill="url(#tv)"/>'
 '<circle cx="40" cy="178" r="13" fill="#F6EFE4"/><circle cx="30" cy="154" r="13" fill="#F6EFE4"/><path d="M96 82 q20 -14 44 -8" stroke="#fff" stroke-width="7" fill="none" opacity=".35" stroke-linecap="round"/>')
# 🌾 tahıl başakları
def ear(x,ang,c="#E8B85A"):
    g=''.join(f'<ellipse cx="0" cy="{-40-i*16}" rx="9" ry="14" fill="{c}" transform="rotate({s*28} 0 {-40-i*16})" />' for i in range(6) for s in (-1,1))
    return f'<g transform="translate({x} 214) rotate({ang})"><path d="M0 0 V-150" stroke="#B78A3A" stroke-width="5"/>{g}<ellipse cx="0" cy="-138" rx="8" ry="14" fill="{c}"/></g>'
A['🌾']=svg(lg('w','#000','#000'),ear(104,-14,"#E2B04E")+ear(152,14,"#F0C96E")+ear(128,0,"#EAC060"))
# 🥣 kâse lapa
d,b=bowl(rg('pr','#FFF3D6','#EBCB8E',.5,.4,.7),'<ellipse cx="128" cy="128" rx="92" ry="19" fill="url(#pr)"/><path d="M100 126 q14 -8 28 0 q14 8 28 0" stroke="#D9AE60" stroke-width="4" fill="none"/>',"#9FC9E8","#5E95C2")
A['🥣']=svg(d,b+'<path d="M150 120 L206 54" stroke="#C9CED6" stroke-width="10" stroke-linecap="round"/>')
# 🍲 çorba
d,b=bowl(rg('sp','#FFB66A','#E2701E',.5,.4,.7),'<ellipse cx="128" cy="128" rx="92" ry="19" fill="url(#sp)"/><g fill="#7CB342"><circle cx="100" cy="126" r="5"/><circle cx="150" cy="130" r="4"/></g><g fill="#FFE0A8"><circle cx="124" cy="122" r="5"/><circle cx="168" cy="124" r="4"/></g>',"#E8E2D8","#BDB3A4")
A['🍲']=svg(d,b+'<path d="M96 96 q-10 -16 0 -32 M128 92 q-10 -18 0 -36 M160 96 q-10 -16 0 -32" stroke="#C9CED6" stroke-width="5" fill="none" stroke-linecap="round" opacity=".8"/>')
# 🧀 lor / labne kâsede
d,b=bowl(rg('lr','#FFFFFF','#EDE8DC',.5,.4,.7),'<path d="M40 128 Q60 92 96 98 Q110 80 132 90 Q156 78 172 96 Q206 92 216 128 Z" fill="url(#lr)"/>'+''.join(f'<circle cx="{x}" cy="{y}" r="5" fill="#F3EEE2"/>' for x,y in [(80,112),(110,100),(150,96),(178,110),(126,114)]),"#7EB3A4","#3F7E6C")
A['🧀']=svg(d,b+'<path d="M130 90 q6 -8 14 -2" stroke="#6FAE4A" stroke-width="5" fill="none" stroke-linecap="round"/>')
# 🥛 yoğurt/süt bardak
A['🥛']=svg(lg('gl','#FFFFFF','#E6EEF2',0,0,1,0),
 '<path d="M72 50 H184 L170 212 Q168 220 160 220 H96 Q88 220 86 212 Z" fill="#E9F1F5" opacity=".6" stroke="#C9D6DE" stroke-width="4"/>'
 '<path d="M78 92 H178 L168 208 Q166 214 160 214 H96 Q90 214 88 208 Z" fill="url(#gl)"/><ellipse cx="128" cy="92" rx="50" ry="8" fill="#FFFFFF" stroke="#E2EAEE" stroke-width="2"/>'
 '<path d="M94 108 L102 196" stroke="#fff" stroke-width="7" opacity=".8" stroke-linecap="round"/>')
# 🌰 ceviz
A['🌰']=svg(rg('cz','#D9A066','#8A5426',.38,.32,.8),
 '<path d="M128 54 C80 54 48 96 52 140 C56 190 96 216 128 216 C160 216 200 190 204 140 C208 96 176 54 128 54Z" fill="url(#cz)"/>'
 '<path d="M128 56 V214" stroke="#6E3E18" stroke-width="5"/>'
 '<path d="M84 100 q20 14 0 30 q20 14 0 30 M172 100 q-20 14 0 30 q-20 14 0 30 M104 76 q10 30 0 60 M152 76 q-10 30 0 60" stroke="#7A4720" stroke-width="4" fill="none" opacity=".6"/>')
# 🥜 yer fıstığı
A['🥜']=svg(rg('fs','#E6C28A','#B98A4E',.4,.35,.8),
 '<g transform="rotate(-30 128 136)"><path d="M128 40 C96 40 84 70 92 96 C98 116 98 126 92 144 C82 176 98 218 128 218 C158 218 174 176 164 144 C158 126 158 116 164 96 C172 70 160 40 128 40Z" fill="url(#fs)"/>'
 +''.join(f'<circle cx="{x}" cy="{y}" r="3" fill="#A8773E" opacity=".6"/>' for x,y in [(112,64),(144,70),(118,100),(140,112),(110,160),(146,170),(128,196),(128,82)])+'</g>')
# 🫒 zeytinyağı şişesi
A['🫒']=svg(lg('oz','#D8C24A','#9E8A12',0,0,0,1)+lg('ob','#E6F0D0','#BFD39A',0,0,1,0),
 '<rect x="108" y="30" width="40" height="22" rx="6" fill="#3E5E22"/>'
 '<path d="M112 52 H144 V80 Q186 98 186 140 V206 Q186 220 172 220 H84 Q70 220 70 206 V140 Q70 98 112 80Z" fill="url(#ob)" opacity=".9"/>'
 '<path d="M76 130 Q128 120 180 130 V206 Q180 214 172 214 H84 Q76 214 76 206Z" fill="url(#oz)"/>'
 '<rect x="88" y="150" width="80" height="44" rx="8" fill="#FFFDF4"/><ellipse cx="128" cy="172" rx="10" ry="14" fill="#5E8A2A"/><path d="M128 158 q10 -4 14 4" stroke="#3E6E1A" stroke-width="3" fill="none"/>')
# 🥜🍯 tahin/bal: 🍯 kavanoz
A['🍯']=svg(lg('hj','#FFC84A','#E08A0A',0,0,0,1),
 '<rect x="70" y="46" width="116" height="26" rx="8" fill="#A0602A"/><path d="M74 72 H182 Q196 74 196 92 V196 Q196 216 176 216 H80 Q60 216 60 196 V92 Q60 74 74 72Z" fill="url(#hj)"/>'
 '<rect x="80" y="122" width="96" height="50" rx="10" fill="#FFF4D6"/><path d="M110 146 h36" stroke="#C47A12" stroke-width="6" stroke-linecap="round"/><path d="M86 90 V190" stroke="#fff" stroke-width="7" opacity=".4" stroke-linecap="round"/>')
# 🫘 kırmızı mercimek yığını
import random; random.seed(7)
lent=''.join(f'<ellipse cx="{x}" cy="{y}" rx="10" ry="7" fill="{random.choice(["#F07A3A","#E86A2A","#F5904E"])}" stroke="#C4501A" stroke-width="1.5" transform="rotate({random.randint(-40,40)} {x} {y})"/>' for x,y in sorted([(random.randint(60,196),random.randint(120,210)) for _ in range(70)],key=lambda p:p[1]) if (x-128)**2/70**2+(y-200)**2/90**2<1)
A['🫘']=svg('',lent)
# 🧆 köfte
A['🧆']=svg(rg('kf','#B5764A','#6E3E1E',.38,.32,.8),
 ''.join(f'<ellipse cx="{x}" cy="{y}" rx="{r}" ry="{r*.8}" fill="url(#kf)"/><ellipse cx="{x-r*.3}" cy="{y-r*.35}" rx="{r*.3}" ry="{r*.16}" fill="#fff" opacity=".25"/>' for x,y,r in [(88,170,40),(168,170,40),(128,126,42)])+
 '<g fill="#5E8A2A"><circle cx="110" cy="112" r="3"/><circle cx="146" cy="120" r="3"/><circle cx="76" cy="160" r="3"/><circle cx="180" cy="160" r="3"/></g>')
# 🍝 makarna (burgu)
A['🍝']=svg(lg('mk','#FFE08A','#F0B53A',0,0,1,1),
 ''.join(f'<g transform="translate({x} {y}) rotate({a})"><rect x="-14" y="-40" width="28" height="80" rx="12" fill="url(#mk)"/><path d="M-14 -24 L14 -12 M-14 -4 L14 8 M-14 16 L14 28" stroke="#D9952A" stroke-width="4"/></g>' for x,y,a in [(84,160,-30),(128,150,10),(172,164,40),(110,108,70),(156,110,-60)]))
# 🥞 pankek
A['🥞']=svg(lg('pk','#F6C27A','#D98A3A'),
 ''.join(f'<ellipse cx="128" cy="{y}" rx="88" ry="22" fill="url(#pk)" stroke="#B86A24" stroke-width="2"/>' for y in [196,176,156,136])+
 '<ellipse cx="128" cy="130" rx="84" ry="18" fill="#F9D79A"/><path d="M96 126 q32 18 64 0 q-8 30 -20 40 q-10 -20 -44 -40Z" fill="#E7A23A" opacity=".85"/><rect x="114" y="112" width="28" height="16" rx="4" fill="#FFF3B0"/>')
# 🥔 patates
A['🥔']=svg(rg('pt','#E8C27A','#B88A3E',.38,.32,.8),
 '<path d="M40 140 C36 92 96 66 146 72 C204 80 228 120 216 160 C204 198 150 214 102 206 C62 198 42 176 40 140Z" fill="url(#pt)"/>'
 +''.join(f'<ellipse cx="{x}" cy="{y}" rx="5" ry="3" fill="#8A6020" opacity=".55"/>' for x,y in [(90,110),(150,98),(184,140),(120,170),(76,156)]))
# 🍆 patlıcan
A['🍆']=svg(rg('pl','#8A5ABE','#3E1E66',.35,.3,.8),
 '<path d="M150 80 C200 92 216 150 186 190 C156 228 92 226 66 196 C42 168 60 132 92 118 C116 108 128 76 150 80Z" fill="url(#pl)"/>'
 '<path d="M136 86 Q150 60 176 66 Q188 84 172 100 Q150 104 136 86Z" fill="#4E8A2A"/><path d="M168 70 Q176 46 196 40" stroke="#3E6E1A" stroke-width="7" fill="none" stroke-linecap="round"/>'
 '<ellipse cx="96" cy="150" rx="10" ry="22" fill="#fff" opacity=".25" transform="rotate(40 96 150)"/>')
# 🫑 kapya biber
A['🫑']=svg(rg('kb','#FF6A4A','#C0201A',.38,.32,.8),
 '<path d="M128 74 C92 60 52 82 54 126 C56 170 76 216 104 216 C116 216 120 206 128 206 C136 206 140 216 152 216 C180 216 200 170 202 126 C204 82 164 60 128 74Z" fill="url(#kb)"/>'
 '<path d="M128 78 C120 120 120 180 128 206" stroke="#A01A14" stroke-width="5" fill="none" opacity=".45"/>'
 '<path d="M116 76 Q118 54 132 54 Q144 54 140 76Z" fill="#4E8A2A"/><path d="M130 56 Q128 38 144 30" stroke="#3E6E1A" stroke-width="7" fill="none" stroke-linecap="round"/>')
# 🍳 omlet tava
A['🍳']=svg(lg('tv2','#4A4F57','#22262C'),
 '<rect x="176" y="118" width="68" height="18" rx="9" fill="#2E2A26" transform="rotate(-20 176 127)"/><circle cx="116" cy="140" r="84" fill="url(#tv2)"/><circle cx="116" cy="140" r="70" fill="#3A3F46"/>'
 '<path d="M64 138 C60 104 96 88 120 96 C150 84 176 110 168 140 C176 170 140 190 112 182 C80 190 62 168 64 138Z" fill="#FFFFFF"/><circle cx="116" cy="138" r="24" fill="#FFC42E"/><ellipse cx="108" cy="130" rx="7" ry="5" fill="#fff" opacity=".5"/>')
# 🌭 sosis
A['🌭']=svg(lg('ss','#D9644A','#9C2E1E',0,0,0,1),
 '<g transform="rotate(-20 128 140)"><rect x="34" y="112" width="188" height="52" rx="26" fill="url(#ss)"/><path d="M60 124 h136" stroke="#fff" stroke-width="6" opacity=".3" stroke-linecap="round"/>'
 '<path d="M80 112 v52 M128 112 v52 M176 112 v52" stroke="#7E2416" stroke-width="3" opacity=".4"/></g>')
# 🍬 şeker
A['🍬']=svg(rg('sk','#FF8AB0','#E0306A',.4,.35,.8),
 '<path d="M76 128 L30 96 L38 128 L30 160Z M180 128 L226 96 L218 128 L226 160Z" fill="#FFB0C8"/><circle cx="128" cy="128" r="56" fill="url(#sk)"/>'
 '<path d="M90 110 Q128 90 166 110 M84 132 Q128 112 172 132 M90 154 Q128 134 166 154" stroke="#fff" stroke-width="6" fill="none" opacity=".55"/>')
# 🫖 demlik
A['🫖']=svg(lg('dm','#F4F1EC','#C9C0B2'),
 '<path d="M188 120 Q230 110 224 150 Q218 172 188 168" stroke="#C9C0B2" stroke-width="12" fill="none"/>'
 '<path d="M62 130 L22 104 Q18 112 26 124 L60 160Z" fill="#D9D2C7"/>'
 '<path d="M60 120 Q60 214 128 214 Q196 214 196 120 Q196 92 128 92 Q60 92 60 120Z" fill="url(#dm)"/><ellipse cx="128" cy="94" rx="46" ry="12" fill="#E6E0D6"/><circle cx="128" cy="78" r="12" fill="#C47A3A"/>'
 '<path d="M84 150 q44 30 88 0" stroke="#5E9E7A" stroke-width="6" fill="none"/>')
# 🧂 tuz
A['🧂']=svg(lg('tz','#FFFFFF','#DCE3E8',0,0,1,0),
 '<path d="M92 50 Q128 30 164 50 L168 76 H88Z" fill="#B9C2CA"/>'+''.join(f'<circle cx="{x}" cy="{y}" r="3" fill="#7E8892"/>' for x,y in [(116,52),(128,48),(140,52)])+
 '<path d="M88 76 H168 L178 206 Q178 218 166 218 H90 Q78 218 78 206Z" fill="url(#tz)" stroke="#C9D2D9" stroke-width="3"/><rect x="96" y="128" width="64" height="40" rx="8" fill="#5E95C2"/><path d="M118 148 h20" stroke="#fff" stroke-width="5" stroke-linecap="round"/>')
# 🥯 simit benzeri
A['🥯']=svg(rg('sm2','#E8A85E','#A8622A',.4,.35,.8),
 '<circle cx="128" cy="140" r="82" fill="url(#sm2)"/><circle cx="128" cy="140" r="26" fill="#F5EFE6"/>'+''.join(f'<ellipse cx="{x}" cy="{y}" rx="3" ry="2" fill="#FFF3D6"/>' for x,y in [(90,100),(110,84),(150,88),(176,112),(186,150),(160,190),(110,196),(80,170),(72,130)]))
import json; json.dump(A,open('art.json','w'),ensure_ascii=False)
print(len(A))

# --- düzeltmeler ---
# 🌾 tahıl: çuval + başak
A['🌾']=svg(lg('sk1','#E9D3A8','#BF9E66')+rg('gr','#F6D27A','#D9A23A',.5,.3,.8),
 '<path d="M62 120 Q58 96 82 92 L174 92 Q198 96 194 120 L204 196 Q206 218 184 218 H72 Q50 218 52 196Z" fill="url(#sk1)"/>'
 '<path d="M70 100 Q128 82 186 100 Q170 70 128 64 Q86 70 70 100Z" fill="url(#gr)"/>'
 +''.join(f'<ellipse cx="{x}" cy="{y}" rx="5" ry="3.5" fill="#E2AE4E" transform="rotate({r} {x} {y})"/>' for x,y,r in [(96,84,20),(112,76,-10),(130,72,30),(148,78,-20),(162,88,10),(120,90,0),(140,92,40),(104,96,-30)])+
 '<path d="M64 112 Q128 124 192 112" stroke="#9E7A40" stroke-width="5" fill="none"/>'
 '<g transform="translate(176 150) rotate(20)"><path d="M0 60 V-50" stroke="#B78A3A" stroke-width="4"/>'
 +''.join(f'<ellipse cx="{s*7}" cy="{-10-i*12}" rx="6" ry="10" fill="#E8B85A" transform="rotate({s*25} {s*7} {-10-i*12})"/>' for i in range(4) for s in (-1,1))+'<ellipse cx="0" cy="-58" rx="5" ry="9" fill="#E8B85A"/></g>')
# 🧀 lor/labne: kâsede krem beyazı peynir tepeciği
d,b=bowl(rg('lr','#FFFFFF','#E8DFCC',.45,.35,.75),'',"#7EB3A4","#3F7E6C")
A['🧀']=svg(d,'<path d="M50 132 Q56 70 112 66 Q128 54 146 66 Q200 70 206 132 Z" fill="url(#lr)" stroke="#E0D6C2" stroke-width="3"/>'
 +''.join(f'<circle cx="{x}" cy="{y}" r="{r}" fill="#FFFFFF" stroke="#E6DDCB" stroke-width="2"/>' for x,y,r in [(84,108,10),(112,88,11),(146,86,10),(174,104,9),(128,112,9),(100,122,7),(158,122,8)])
 +b+'<path d="M128 62 q8 -12 20 -4 M128 62 q-8 -12 -20 -4" stroke="#6FAE4A" stroke-width="5" fill="none" stroke-linecap="round"/>')
# Her çizimin gradyan kimliklerini benzersiz yap (aynı sayfada karışmasın)
import re
for i,(k,v) in enumerate(list(A.items())):
    ids=set(re.findall(r'id="([^"]+)"',v))
    for d_ in ids:
        v=v.replace(f'id="{d_}"',f'id="f{i}_{d_}"').replace(f'url(#{d_})',f'url(#f{i}_{d_})')
    A[k]=v
json.dump(A,open('art.json','w'),ensure_ascii=False)
print('fixed',len(A))
