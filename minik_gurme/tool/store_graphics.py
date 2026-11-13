#!/usr/bin/env python3
"""Google Play mağaza görselleri: simge 512, öne çıkan görsel 1024x500, başlıklı telefon ekran görüntüleri (TR + EN).
Önce: ./gradlew :app:testDebugUnitTest --tests '*StoreScreenshotsTest'   (app/build/store-shots)
Sonra: python3 tool/store_graphics.py   → docs/store/
"""
import os
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SHOTS = os.path.join(ROOT, "app/build/store-shots")
OUT = os.path.join(ROOT, "docs/store")
FONT = os.path.join(ROOT, "shared/src/commonMain/composeResources/font")
ICON = os.path.join(ROOT, "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png")
LOGO = os.path.join(ROOT, "shared/src/commonMain/composeResources/drawable/logo_emblem.webp")
INDIGO, CORAL = (0x4F, 0x63, 0xD8), (0xF0, 0x8A, 0x6C)

CAPTIONS = {
    "tr": ["Öğünleri tek dokunuşla kaydedin", "1 yaşına kadar 100 gıda", "Yaşa uygun tarifler ve günün menüsü",
           "Ay ay rehber ve alerjen planı", "WHO eğrileriyle büyüme takibi", "Ebeveyn Topluluğu"],
    "en": ["Log meals in one tap", "100 foods before age 1", "Age-appropriate recipes & daily menu",
           "Month-by-month guide & allergen plan", "Growth tracking with WHO curves", "Parent Community"],
}
NAMES = ["1_today", "2_foods100", "3_recipes", "4_guide", "5_growth", "6_community"]

def font(weight, size):
    return ImageFont.truetype(os.path.join(FONT, f"plus_jakarta_sans_{weight}.ttf"), size)

def gradient(w, h):
    g = Image.new("RGB", (w, h))
    px = g.load()
    for y in range(h):
        for x in range(w):
            t = (x / w * 0.35 + y / h * 0.65)
            px[x, y] = tuple(int(INDIGO[i] * (1 - t) + CORAL[i] * t) for i in range(3))
    return g

def wrap(draw, text, f, maxw):
    lines, cur = [], ""
    for word in text.split():
        nxt = (cur + " " + word).strip()
        if draw.textlength(nxt, font=f) > maxw and cur:
            lines.append(cur); cur = word
        else:
            cur = nxt
    return lines + [cur]

def framed(shot_path, caption):
    W, H = 1080, 1920
    bg = gradient(W, H)
    d = ImageDraw.Draw(bg)
    f = font("extrabold", 74)
    lines = wrap(d, caption, f, W - 140)
    y = 120
    for line in lines:
        d.text(((W - d.textlength(line, font=f)) / 2, y), line, font=f, fill="white"); y += 92
    shot = Image.open(shot_path).convert("RGB")
    sw = 860; sh = int(shot.height * sw / shot.width)
    shot = shot.resize((sw, sh), Image.LANCZOS)
    top = y + 60
    shot = shot.crop((0, 0, sw, min(sh, H - top + 60)))
    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, shot.width - 1, shot.height + 60), 48, fill=255)
    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(((W - sw) // 2 - 6, top + 14, (W + sw) // 2 + 6, H + 40), 52, fill=(20, 20, 60, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(22))
    out = bg.convert("RGBA"); out.alpha_composite(shadow)
    out.paste(shot, ((W - sw) // 2, top), mask)
    return out.convert("RGB")

DRAW = os.path.join(ROOT, "shared/src/commonMain/composeResources/drawable")

def food(name, size):
    return Image.open(os.path.join(DRAW, name + ".webp")).convert("RGBA").resize((size, size), Image.LANCZOS)

def phone_frame(shot_path, width):
    s = Image.open(shot_path).convert("RGB")
    sh = int(s.height * width / s.width); s = s.resize((width, sh), Image.LANCZOS)
    b = max(8, width // 26); W, H = width + 2 * b, sh + 2 * b
    f = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(f).rounded_rectangle((0, 0, W - 1, H - 1), b * 3, fill=(22, 24, 38, 255))
    m = Image.new("L", s.size, 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, width - 1, sh - 1), b * 2, fill=255)
    f.paste(s, (b, b), m)
    return f

def with_shadow(canvas, img, xy, blur=16, alpha=110):
    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    sh.paste((20, 20, 60, 255), (xy[0] + 6, xy[1] + 14), img.split()[-1].point(lambda v: alpha if v else 0))
    canvas.alpha_composite(sh.filter(ImageFilter.GaussianBlur(blur))); canvas.alpha_composite(img, xy)

def feature(lang):
    """Öne çıkan görsel: logo + başlık, telefon çerçevesinde uygulama ekranı ve uygulamadaki 3D besin çizimleri."""
    W, H = 1024, 500
    bg = gradient(W, H).convert("RGBA")
    d = ImageDraw.Draw(bg)
    logo = Image.open(LOGO).convert("RGBA").resize((86, 86), Image.LANCZOS)
    bg.alpha_composite(logo, (56, 92))
    d.text((154, 108), "Minik Gurme", font=font("extrabold", 50), fill="white")
    sub = "Ek gıdaya geçiş rehberiniz" if lang == "tr" else "Your starting-solids companion"
    d.text((58, 206), sub, font=font("extrabold", 34), fill="white")
    tags = ["1 yaşına kadar 100 gıda", "Alerjen planı · Tarifler", "Büyüme · Topluluk"] if lang == "tr" else \
           ["100 foods before age 1", "Allergen plan · Recipes", "Growth · Community"]
    y = 266
    for t in tags:
        d.ellipse((60, y + 11, 72, y + 23), fill="white"); d.text((84, y), t, font=font("semibold", 25), fill=(255, 255, 255, 240)); y += 40
    # Besin çizimleri (uygulamadaki 3D görseller)
    for name, x, yy, sz in [("emoji_1f951", 560, 40, 92), ("emoji_1f966", 600, 300, 104), ("emoji_1f955", 905, 330, 96),
                            ("emoji_1f353", 920, 70, 84), ("emoji_1f34c", 540, 170, 78)]:
        with_shadow(bg, food(name, sz), (x, yy), blur=10, alpha=80)
    p = phone_frame(os.path.join(SHOTS, f"{lang}_1_today.png"), 200)
    with_shadow(bg, p, (690, 34))
    return bg.convert("RGB")

def foods_showcase(lang):
    """7. ekran görüntüsü: uygulamadaki 100 besin çiziminden pano."""
    import re
    W, H = 1080, 1920
    bg = gradient(W, H)
    d = ImageDraw.Draw(bg)
    cap = "100 besin, 100 renkli çizim" if lang == "tr" else "100 foods, 100 colourful illustrations"
    f = font("extrabold", 74); yy = 120
    for line in wrap(d, cap, f, W - 140):
        d.text(((W - d.textlength(line, font=f)) / 2, yy), line, font=f, fill="white"); yy += 92
    src = open(os.path.join(ROOT, "shared/src/commonMain/kotlin/akinai/minikgurme/ui/components/FoodImage.kt"), encoding="utf-8").read()
    names = list(dict.fromkeys(re.findall(r"Res\.drawable\.((?:emoji|food)_\w+)", src)))
    skip = {"emoji_1f32d", "emoji_2600", "emoji_26a0", "emoji_1f308", "emoji_1f319", "emoji_1f324", "emoji_1f3af", "emoji_1f4da", "emoji_1f50d", "emoji_1fa7a", "emoji_1f4cf", "emoji_1f9ea", "emoji_1f37d"}
    names = [n for n in names if n not in skip and os.path.exists(os.path.join(DRAW, n + ".webp"))]
    # Düz çizimleri (emoji) ve renklendirilmiş besinleri (food_) karıştır
    em = [n for n in names if n.startswith("emoji_")]; fd = [n for n in names if n.startswith("food_")]
    names = [x for pair in zip(em, fd) for x in pair] + em[len(fd):] + fd[len(em):]
    card = Image.new("RGBA", (W - 120, H - yy - 120), (255, 255, 255, 235))
    m = Image.new("L", card.size, 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, card.width - 1, card.height - 1), 48, fill=255)
    out = bg.convert("RGBA"); out.paste(card, (60, yy + 50), m)
    cols, icon = 6, 128
    cell = (W - 120 - 40) // cols
    rows = (card.height - 40) // (icon + 14)
    gap_y = (card.height - 40 - rows * icon) / max(1, rows - 1)
    for i, n in enumerate(names[: cols * rows]):
        r, c = divmod(i, cols)
        x = 60 + 20 + c * cell + (cell - icon) // 2; y = int(yy + 50 + 20 + r * (icon + gap_y))
        out.alpha_composite(food(n, icon), (x, y))
    return out.convert("RGB")

def main():
    os.makedirs(OUT, exist_ok=True)
    Image.open(ICON).convert("RGB").resize((512, 512), Image.LANCZOS).save(os.path.join(OUT, "icon-512.png"))
    for lang in ("tr", "en"):
        d = os.path.join(OUT, lang); os.makedirs(d, exist_ok=True)
        feature(lang).save(os.path.join(d, "feature-1024x500.png"))
        for i, (n, cap) in enumerate(zip(NAMES, CAPTIONS[lang])):
            framed(os.path.join(SHOTS, f"{lang}_{n}.png"), cap).save(os.path.join(d, f"phone-{i+1}.png"), optimize=True)
        foods_showcase(lang).save(os.path.join(d, "phone-7.png"), optimize=True)
    print("Mağaza görselleri:", OUT)

if __name__ == "__main__":
    main()
