#!/usr/bin/env python3
"""Minik Gurme web sayfalarını (gizlilik, koşullar, hesap silme) uygulamadaki LegalTexts.kt'den üretir.
Çıktı: minik_gurme/docs/site/  — GitHub Pages'te https://<kullanıcı>.github.io/<depo>/minik-gurme/ altında yayınlanır.
Kullanım: python3 tool/build_site.py
"""
import html, os, re, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "shared/src/commonMain/kotlin/akinai/minikgurme/util/LegalTexts.kt")
OUT = os.path.join(ROOT, "docs/site")

src = open(SRC, encoding="utf-8").read()
email = re.search(r'CONTACT_EMAIL = "([^"]*)"', src).group(1)
updated = re.search(r'UPDATED = "([^"]*)"', src).group(1)

def sections(fn, lang):
    body = src[src.index(f"fun {fn}(lang: String)"):]
    body = body[:body.index("\n    fun ", 10)] if "\n    fun " in body[10:] else body
    en_part, tr_part = body.split(") else listOf(", 1)
    part = en_part if lang == "en" else tr_part
    pairs = re.findall(r'^\s*"((?:[^"\\]|\\.)*)" to (?:"((?:[^"\\]|\\.)*)"|(contactEn|contact)),\s*$', part, re.M)
    out = []
    for h, b, c in pairs:
        text = email if c else b
        text = text.replace("$UPDATED", updated).replace('\\"', '"')
        out.append((h, text))
    return out

CSS = """
:root{--bg:#F7F6FB;--card:#fff;--text:#1F2238;--muted:#5A5F7D;--accent:#4F63D8;--border:#E4E3EE}
@media (prefers-color-scheme:dark){:root{--bg:#0D0E16;--card:#151726;--text:#ECEDF7;--muted:#A5A9C6;--accent:#8C9BFF;--border:#262a40}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:16px/1.6 -apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif}
main{max-width:760px;margin:0 auto;padding:24px 16px 64px}header{display:flex;align-items:center;gap:12px;margin:8px 0 24px}
header img{width:48px;height:48px;border-radius:50%}header b{font-size:20px;color:var(--accent)}
nav{margin-left:auto;font-size:14px}nav a{color:var(--muted);margin-left:12px}
h1{font-size:26px;margin:0 0 8px}section{background:var(--card);border:1px solid var(--border);border-radius:16px;padding:16px 20px;margin:12px 0}
h2{font-size:17px;margin:0 0 6px}p{margin:0;color:var(--muted)}a{color:var(--accent)}footer{margin-top:32px;font-size:13px;color:var(--muted);text-align:center}
.cards a{display:block;text-decoration:none}.cards section:hover{border-color:var(--accent)}
"""

def page(lang, title, other_href, items_html):
    other = "English" if lang == "tr" else "Türkçe"
    home = "index.html" if lang == "tr" else "en.html"
    return f"""<!doctype html><html lang="{lang}"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1"><title>{html.escape(title)} · Minik Gurme</title>
<style>{CSS}</style></head><body><main>
<header><img src="logo.png" alt=""><b>Minik Gurme</b><nav><a href="{home}">{"Ana sayfa" if lang=="tr" else "Home"}</a><a href="{other_href}">{other}</a></nav></header>
<h1>{html.escape(title)}</h1>
{items_html}
<footer>© Minik Gurme · <a href="mailto:{email}">{email}</a></footer>
</main></body></html>"""

def legal(lang, fn, fname, other):
    secs = sections(fn, lang)
    title, intro = secs[0]
    items = f"<p>{html.escape(intro)}</p>" + "".join(
        f"<section><h2>{html.escape(h)}</h2><p>{html.escape(b)}</p></section>" for h, b in secs[1:])
    open(os.path.join(OUT, fname), "w", encoding="utf-8").write(page(lang, title, other, items))

DELETE = {
 "tr": ("Hesap ve veri silme", [
   ("Bebek kayıtları", "Bebeğinizin profil, öğün, büyüme ve alerji kayıtları yalnızca telefonunuzda saklanır; bize hiç gönderilmez. Silmek için uygulamada Ayarlar › Veriler › Beslenme istatistiklerini sıfırla seçeneğini kullanın ya da uygulamayı kaldırın."),
   ("Ebeveyn Topluluğu hesabı", "Topluluğa katıldıysanız uygulamada Ayarlar › Topluluk › Topluluk hesabımı sil seçeneğiyle anonim hesabınızı ve yazdığınız tüm gönderi ve yorumları anında silebilirsiniz. Tek tek gönderi ve yorumlarınızı da ⋮ menüsünden silebilirsiniz."),
   ("Uygulamaya erişemiyorsanız", f"Uygulamayı kaldırdıysanız ya da telefonunuza erişemiyorsanız {email} adresine \"Topluluk hesabımı silin\" konulu e-posta gönderin; takma adınızı ve yaklaşık paylaşım tarihlerini yazın. Talebiniz en geç 30 gün içinde işlenir ve topluluk içerikleriniz silinir."),
   ("Saklanan veriler", "Silme sonrasında topluluk içerikleriniz ve anonim hesabınız kalıcı olarak kaldırılır. Başkalarının sizin içeriğiniz hakkında yaptığı şikâyet kayıtları yalnızca moderasyon incelemesi tamamlanana kadar saklanır ve ardından silinir."),
 ]),
 "en": ("Account and data deletion", [
   ("Baby records", "Your baby's profile, meals, growth and allergy records are stored only on your phone and are never sent to us. To delete them, use Settings › Data › Reset feeding statistics in the app, or uninstall the app."),
   ("Parent Community account", "If you joined the community, Settings › Community › Delete my community account in the app immediately deletes your anonymous account and all posts and comments you wrote. You can also delete individual posts and comments from the ⋮ menu."),
   ("If you cannot access the app", f"If you uninstalled the app or cannot access your phone, e-mail {email} with the subject \"Delete my community account\" and include your nickname and approximate posting dates. Your request is processed within 30 days and your community content is deleted."),
   ("Retained data", "After deletion your community content and anonymous account are permanently removed. Reports other users made about your content are kept only until the moderation review is complete and are then deleted."),
 ]),
}

def build():
    os.makedirs(OUT, exist_ok=True)
    legal("tr", "privacy", "gizlilik.html", "privacy.html")
    legal("en", "privacy", "privacy.html", "gizlilik.html")
    legal("tr", "terms", "kosullar.html", "terms.html")
    legal("en", "terms", "terms.html", "kosullar.html")
    for lang, fname, other in [("tr", "hesap-silme.html", "delete-account.html"), ("en", "delete-account.html", "hesap-silme.html")]:
        title, items = DELETE[lang]
        body = "".join(f"<section><h2>{html.escape(h)}</h2><p>{html.escape(b)}</p></section>" for h, b in items)
        open(os.path.join(OUT, fname), "w", encoding="utf-8").write(page(lang, title, other, body))
    for lang, fname, other in [("tr", "index.html", "en.html"), ("en", "en.html", "index.html")]:
        tr = lang == "tr"
        intro = ("Ek gıdaya geçişte bebeğinizin beslenme yolculuğu: 1 yaşına kadar 100 gıda, alerjen tanıtım planı, yaşa uygun tarifler, büyüme takibi ve Ebeveyn Topluluğu."
                 if tr else "Your baby's solid food journey: 100 foods before age 1, allergen introduction plan, age-appropriate recipes, growth tracking and a Parent Community.")
        links = [("gizlilik.html" if tr else "privacy.html", "Gizlilik Politikası" if tr else "Privacy Policy"),
                 ("kosullar.html" if tr else "terms.html", "Kullanım Koşulları" if tr else "Terms of Use"),
                 ("hesap-silme.html" if tr else "delete-account.html", "Hesap ve veri silme" if tr else "Account and data deletion")]
        body = f"<p>{html.escape(intro)}</p><div class='cards'>" + "".join(
            f"<a href='{h}'><section><h2>{html.escape(t)}</h2></section></a>" for h, t in links) + \
            f"<section><h2>{'İletişim' if tr else 'Contact'}</h2><p><a href='mailto:{email}'>{email}</a></p></section></div>"
        open(os.path.join(OUT, fname), "w", encoding="utf-8").write(page(lang, "Minik Gurme", other, body))
    print("Site üretildi:", OUT)

if __name__ == "__main__":
    build()
