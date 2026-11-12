import numpy as np
from PIL import Image, ImageDraw
src=Image.open('logo_src.png').convert('RGB').crop((404,95,1004,695))   # 600x600, amblem merkezi (300,300)
a=np.asarray(src).astype(np.float32)/255
# 1) Yazıları sil: üst bant (y<200) ve önlükteki yazı -> zemin rengi
H,W=a.shape[:2]; yy,xx=np.mgrid[0:H,0:W]
a[(yy<206)&~((xx>228)&(xx<372)&(yy>196))]=1.0
# önlük yazısı: önlük kutusunda önlük renginden belirgin farklı pikselleri maskele, OpenCV ile doldur
import cv2
box=(xx>250)&(xx<350)&(yy>398)&(yy<452)
lum=a.max(-1); sat=lum-a.min(-1)
bibcol=np.median(a[box & (sat>0.3)],axis=0)
diff=np.abs(a-bibcol).sum(-1)
mask=(box & (diff>0.22)).astype(np.uint8)*255
mask=cv2.dilate(mask,np.ones((3,3),np.uint8),1)
img8=(a*255).astype(np.uint8)
img8=cv2.inpaint(img8[...,::-1].copy(),mask,6,cv2.INPAINT_TELEA)[...,::-1]
a=img8.astype(np.float32)/255
# 2) Renk: maviler -> indigo, turuncular -> mercan, yeşiller -> deniz yeşili
r,g,b=a[...,0],a[...,1],a[...,2]; mx=a.max(-1); mn=a.min(-1); d=mx-mn+1e-6
h=np.where(mx==r,((g-b)/d)%6,np.where(mx==g,(b-r)/d+2,(r-g)/d+4))*60; s=d/(mx+1e-6); v=mx
nh,ns=h.copy(),s.copy()
blue=(h>185)&(h<240)&(s>.2); orange=(h>14)&(h<42)&(s>.35)&(v>.6); green=(h>70)&(h<160)&(s>.22)
skin=(h>10)&(h<35)&(s<.45)&(v>.8)   # ten rengini koru
nh[blue]=231; ns[blue]=np.clip(s[blue]*1.05,0,1)
m=orange&~skin; nh[m]=12; ns[m]=np.clip(s[m]*.85,0,1)
nh[green]=166; ns[green]=s[green]*.9
def hsv2rgb(h,s,v):
    h=(h%360)/60; i=np.floor(h).astype(int)%6; f=h-np.floor(h); p=v*(1-s); q=v*(1-s*f); t=v*(1-s*(1-f))
    o=np.zeros(h.shape+(3,))
    for k,(R,G,B) in enumerate([(v,t,p),(q,v,p),(p,v,t),(p,q,v),(t,p,v),(v,p,q)]):
        mm=i==k; o[mm,0]=R[mm]; o[mm,1]=G[mm]; o[mm,2]=B[mm]
    return o
out=Image.fromarray((hsv2rgb(nh,ns,v)*255).clip(0,255).astype(np.uint8))
# 3) Yeni amblem: (300,350) merkezli daire; dışta indigo, içte deniz yeşili halka
S=4; cx,cy,R=300,352,234; size=2*R+40
canvas=Image.new('RGBA',(size*S,size*S),(0,0,0,0)); dr=ImageDraw.Draw(canvas)
dr.ellipse([20*S,20*S,(size-20)*S,(size-20)*S],fill=(255,255,255,255))
art=out.crop((cx-R,cy-R,cx+R,cy+R)).resize((2*R*S,2*R*S),Image.LANCZOS).convert('RGBA')
mk=Image.new('L',art.size,0); ImageDraw.Draw(mk).ellipse([6*S,6*S,(2*R-6)*S,(2*R-6)*S],fill=255); art.putalpha(mk)
canvas.alpha_composite(art,(20*S,20*S))
dr=ImageDraw.Draw(canvas)
o=10*S; dr.arc([o,o,size*S-o,size*S-o],start=0,end=360,fill=(79,99,216,255),width=9*S)
o2=24*S; dr.arc([o2,o2,size*S-o2,size*S-o2],start=20,end=160,fill=(46,156,132,255),width=6*S)
dr.arc([o2,o2,size*S-o2,size*S-o2],start=200,end=250,fill=(240,138,108,255),width=6*S)
em=canvas.resize((512,512),Image.LANCZOS); em.save('logo3_emblem.png')
# 4) Uygulama simgesi ön planı (432px, 108dp; görünür alan ~288px)
fg=Image.new('RGBA',(432,432),(0,0,0,0)); ic=em.resize((330,330),Image.LANCZOS); fg.alpha_composite(ic,(51,51)); fg.save('logo3_fg.png')
prev=Image.new('RGB',(1100,560),(247,246,251)); prev.paste(em,(20,24),em)
bgc=Image.new('RGB',(432,432),(236,238,252)); bgc.paste(fg,(0,0),fg)
c=Image.new('L',(432,432),0); ImageDraw.Draw(c).ellipse((72,72,360,360),fill=255)
dark=Image.new('RGB',(432,432),(13,14,22)); dark.paste(bgc,(0,0),c); prev.paste(dark.crop((60,60,372,372)),(560,40))
sq=Image.new('L',(432,432),0); ImageDraw.Draw(sq).rounded_rectangle((72,72,360,360),70,fill=255)
dark2=Image.new('RGB',(432,432),(247,246,251)); dark2.paste(bgc,(0,0),sq); prev.paste(dark2.crop((60,60,372,372)).resize((156,156)),(900,40))
prev.save('logo3_preview.png')
