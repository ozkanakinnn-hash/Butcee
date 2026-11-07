"""Haber Akışı uygulama ikonu üreticisi.

Tasarım: mavi degrade zemin (#2D9BFF → #0A60E0) üzerinde beyaz "haber akışı" simgesi — üç satır,
her satırda bir görsel kutusu, kalın başlık çizgisi ve soluk alt satır; satırlar aşağı doğru kısalır.
Hem açık hem koyu ana ekranda belirgin kalır. Android 13+ tema ikonu için tek renkli katman da üretilir.

Çıktılar (Android):
  mipmap-*/ic_launcher.png              eski cihazlar için yuvarlatılmış kare tam ikon
  mipmap-*/ic_launcher_round.png        yuvarlak ikon
  mipmap-*/ic_launcher_foreground.png   uyarlanabilir ikon ön planı (108dp, güvenli alan içinde)
  mipmap-*/ic_launcher_monochrome.png   Android 13+ tema ikonu (tek renk)
  drawable-*/ic_stat_news.png           bildirim çubuğu simgesi (beyaz)
  design/play_store_icon_512.png        Play Store ikonu
Kullanım: python3 design/make_icon.py   (news-aggregator klasöründen)
"""
from pathlib import Path
from PIL import Image, ImageDraw

TOP = (45, 155, 255)
BOTTOM = (10, 96, 224)
WHITE = (255, 255, 255, 255)
BLACK = (0, 0, 0, 255)
SS = 4  # süper örnekleme (kenar yumuşatma)

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "flutter_app/android/app/src/main/res"
DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}


def gradient(size: int) -> Image.Image:
    """Sol üstten sağ alta mavi degrade."""
    g = Image.new("RGBA", (size, size))
    px = g.load()
    for y in range(size):
        for x in range(size):
            t = (x + y) / (2 * (size - 1))
            px[x, y] = tuple(round(TOP[i] + (BOTTOM[i] - TOP[i]) * t) for i in range(3)) + (255,)
    return g


def draw_glyph(d: ImageDraw.ImageDraw, size: int, box: float, mono: bool = False) -> None:
    """Akış simgesini, `size` boyutlu tuvalin ortasında `box` oranında bir kareye çizer."""
    side = size * box
    x0 = (size - side) / 2
    y0 = (size - side) / 2
    rows = 3
    gap = side * 0.13
    row_h = (side - gap * (rows - 1)) / rows
    thumb = row_h
    radius = row_h * 0.28
    bar_h = row_h * 0.36
    bar_x = x0 + thumb + side * 0.09
    max_bar = x0 + side - bar_x
    widths = [1.0, 0.78, 0.56]
    for i in range(rows):
        top = y0 + i * (row_h + gap)
        color = WHITE
        d.rounded_rectangle([x0, top, x0 + thumb, top + row_h], radius=radius, fill=color)
        # Başlık çizgisi (kalın) + alt satır (ince, soluk)
        by = top + row_h * 0.06
        d.rounded_rectangle([bar_x, by, bar_x + max_bar * widths[i], by + bar_h], radius=bar_h / 2, fill=WHITE)
        sub_y = by + bar_h + row_h * 0.17
        sub_h = bar_h * 0.66
        faded = WHITE if mono else (255, 255, 255, 150)
        d.rounded_rectangle([bar_x, sub_y, bar_x + max_bar * widths[i] * 0.62, sub_y + sub_h],
                            radius=sub_h / 2, fill=faded)


def render(size: int, kind: str) -> Image.Image:
    big = size * SS
    img = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    if kind in ("full", "round", "store", "background"):
        bg = gradient(size).resize((big, big), Image.BICUBIC)
        mask = Image.new("L", (big, big), 0)
        md = ImageDraw.Draw(mask)
        if kind == "full":  # eski cihazlar: yuvarlatılmış kare
            md.rounded_rectangle([0, 0, big - 1, big - 1], radius=big * 0.23, fill=255)
        elif kind == "round":
            md.ellipse([0, 0, big - 1, big - 1], fill=255)
        else:  # Play Store ve uyarlanabilir arka plan tam kare (köşeleri sistem keser)
            md.rectangle([0, 0, big, big], fill=255)
        img.paste(bg, (0, 0), mask)
    d = ImageDraw.Draw(img)
    if kind in ("full", "store"):
        draw_glyph(d, big, 0.58)
    elif kind == "round":
        draw_glyph(d, big, 0.52)
    elif kind == "foreground":  # 108dp tuval; güvenli alan 66dp çaplı daire
        draw_glyph(d, big, 0.50)
    elif kind == "monochrome":
        draw_glyph(d, big, 0.50, mono=True)
    return img.resize((size, size), Image.LANCZOS)


def main() -> None:
    for name, scale in DENSITIES.items():
        folder = RES / f"mipmap-{name}"
        folder.mkdir(parents=True, exist_ok=True)
        render(round(48 * scale), "full").save(folder / "ic_launcher.png")
        render(round(48 * scale), "round").save(folder / "ic_launcher_round.png")
        render(round(108 * scale), "foreground").save(folder / "ic_launcher_foreground.png")
        render(round(108 * scale), "background").save(folder / "ic_launcher_background.png")
        render(round(108 * scale), "monochrome").save(folder / "ic_launcher_monochrome.png")
    # Bildirim çubuğu simgesi: 24dp, beyaz + şeffaf (Android rengi kendisi verir)
    for name, scale in DENSITIES.items():
        folder = RES / f"drawable-{name}"
        folder.mkdir(parents=True, exist_ok=True)
        size = round(24 * scale)
        big = size * SS
        img = Image.new("RGBA", (big, big), (0, 0, 0, 0))
        draw_glyph(ImageDraw.Draw(img), big, 0.84, mono=True)
        img.resize((size, size), Image.LANCZOS).save(folder / "ic_stat_news.png")
    render(512, "store").save(ROOT / "design/play_store_icon_512.png")

    anydpi = RES / "mipmap-anydpi-v26"
    anydpi.mkdir(exist_ok=True)
    adaptive = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
    <monochrome android:drawable="@mipmap/ic_launcher_monochrome" />
</adaptive-icon>
"""
    (anydpi / "ic_launcher.xml").write_text(adaptive)
    (anydpi / "ic_launcher_round.xml").write_text(adaptive)
    old_color = RES / "values" / "ic_launcher_background.xml"
    if old_color.exists():
        old_color.unlink()
    print("ikonlar üretildi:", RES)


if __name__ == "__main__":
    main()
