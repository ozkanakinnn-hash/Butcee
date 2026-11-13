#!/usr/bin/env python3
"""Tanıtım görselleri (Play Console tanıtım içeriği + Google Ads/sosyal medya), TR + EN.
Önce: ./gradlew :app:testDebugUnitTest --tests '*StoreScreenshotsTest'   (app/build/store-shots)
Sonra: python3 tool/promo_graphics.py   → docs/store/promo/
"""
import os
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SHOTS = os.path.join(ROOT, "app/build/store-shots")
OUT = os.path.join(ROOT, "docs/store/promo")
FONT = os.path.join(ROOT, "shared/src/commonMain/composeResources/font")
LOGO = os.path.join(ROOT, "shared/src/commonMain/composeResources/drawable/logo_emblem.webp")
INDIGO, CORAL = (0x4F, 0x63, 0xD8), (0xF0, 0x8A, 0x6C)

TEXT = {
    "tr": {"title": "Minik Gurme", "head": "Ek gıdaya geçiş artık çok kolay",
           "bullets": ["1 yaşına kadar 100 gıda", "Alerjen planı ve 3 gün kuralı", "Yaşa uygun tarifler", "Büyüme takibi · Ebeveyn Topluluğu"],
           "cta": "Google Play'de ücretsiz"},
    "en": {"title": "Minik Gurme", "head": "Starting solids made easy",
           "bullets": ["100 foods before age 1", "Allergen plan & 3-day rule", "Age-appropriate recipes", "Growth tracking · Parent Community"],
           "cta": "Free on Google Play"},
}

def font(w, s): return ImageFont.truetype(os.path.join(FONT, f"plus_jakarta_sans_{w}.ttf"), s)

def gradient(w, h):
    top = Image.new("RGB", (1, 2)); top.putpixel((0, 0), INDIGO); top.putpixel((0, 1), CORAL)
    g = top.resize((w, h), Image.BICUBIC)
    diag = Image.linear_gradient("L").resize((w, h)).rotate(0)
    return Image.composite(Image.new("RGB", (w, h), INDIGO), g, diag.point(lambda v: int(v * 0.35)))

def phone(shot, width):
    """Ekran görüntüsünü yuvarlatılmış telefon çerçevesine yerleştirir."""
    s = Image.open(shot).convert("RGB")
    sh = int(s.height * width / s.width)
    s = s.resize((width, sh), Image.LANCZOS)
    bezel = max(10, width // 28)
    W, H = width + 2 * bezel, sh + 2 * bezel
    frame = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(frame).rounded_rectangle((0, 0, W - 1, H - 1), bezel * 3, fill=(22, 24, 38, 255))
    m = Image.new("L", s.size, 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, width - 1, sh - 1), bezel * 2, fill=255)
    frame.paste(s, (bezel, bezel), m)
    return frame

def drop(canvas, img, xy, blur=24, alpha=120):
    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    a = img.split()[-1].point(lambda v: alpha if v else 0)
    sh.paste((20, 20, 60, 255), (xy[0] + 10, xy[1] + 22), a)
    canvas.alpha_composite(sh.filter(ImageFilter.GaussianBlur(blur)))
    canvas.alpha_composite(img, xy)

def wrap(d, text, f, maxw):
    out, cur = [], ""
    for w in text.split():
        n = (cur + " " + w).strip()
        if d.textlength(n, font=f) > maxw and cur: out.append(cur); cur = w
        else: cur = n
    return out + [cur]

def text_block(c, x, y, maxw, t, scale, bullets=True):
    d = ImageDraw.Draw(c)
    logo = Image.open(LOGO).convert("RGBA").resize((int(84 * scale), int(84 * scale)), Image.LANCZOS)
    c.alpha_composite(logo, (x, y))
    d.text((x + int(100 * scale), y + int(18 * scale)), t["title"], font=font("extrabold", int(44 * scale)), fill="white")
    y += int(120 * scale)
    fh = font("extrabold", int(64 * scale))
    for line in wrap(d, t["head"], fh, maxw):
        d.text((x, y), line, font=fh, fill="white"); y += int(76 * scale)
    y += int(14 * scale)
    if bullets:
        fb = font("semibold", int(30 * scale))
        for b in t["bullets"]:
            r = int(9 * scale)
            d.ellipse((x, y + int(13 * scale), x + 2 * r, y + int(13 * scale) + 2 * r), fill=(255, 255, 255, 230))
            d.text((x + int(32 * scale), y), b, font=fb, fill=(255, 255, 255, 240)); y += int(46 * scale)
        y += int(18 * scale)
    else:
        y += int(22 * scale)
    fc = font("bold", int(30 * scale))
    tw = d.textlength(t["cta"], font=fc); pad = int(26 * scale)
    d.rounded_rectangle((x, y, x + tw + 2 * pad, y + int(64 * scale)), int(32 * scale), fill="white")
    d.text((x + pad, y + int(14 * scale)), t["cta"], font=fc, fill=INDIGO)

def landscape(lang, W, H, scale):
    c = gradient(W, H).convert("RGBA"); t = TEXT[lang]
    p1 = phone(os.path.join(SHOTS, f"{lang}_2_foods100.png"), int(W * 0.19))
    p2 = phone(os.path.join(SHOTS, f"{lang}_1_today.png"), int(W * 0.21))
    top = int(H * 0.07)
    drop(c, p1, (int(W * 0.56), top + int(H * 0.12)))
    drop(c, p2, (int(W * 0.74), top))
    text_block(c, int(W * 0.06), int(H * 0.12), int(W * 0.50), t, scale, bullets=H >= 900)
    return c.convert("RGB")

def square(lang, W, H, scale):
    c = gradient(W, H).convert("RGBA"); t = TEXT[lang]
    text_block(c, int(W * 0.07), int(H * 0.06), int(W * 0.86), t, scale, bullets=False)
    pw = int(W * 0.36)
    p1 = phone(os.path.join(SHOTS, f"{lang}_5_growth.png"), pw)
    p2 = phone(os.path.join(SHOTS, f"{lang}_1_today.png"), pw)
    base = int(H * (0.50 if H > W else 0.44))
    drop(c, p1, (int(W * 0.10), base + int(H * 0.05)))
    drop(c, p2, (int(W * 0.52), base))
    return c.convert("RGB")

SIZES = [("tanitim-1920x1080", 1920, 1080, landscape, 1.25),
         ("yatay-1200x628", 1200, 628, landscape, 0.82),
         ("kare-1200x1200", 1200, 1200, square, 1.0),
         ("dikey-1200x1500", 1200, 1500, square, 1.05)]

def main():
    for lang in ("tr", "en"):
        d = os.path.join(OUT, lang); os.makedirs(d, exist_ok=True)
        for name, W, H, fn, sc in SIZES:
            fn(lang, W, H, sc).save(os.path.join(d, f"{name}.png"), optimize=True)
    print("Tanıtım görselleri:", OUT)

if __name__ == "__main__":
    main()
