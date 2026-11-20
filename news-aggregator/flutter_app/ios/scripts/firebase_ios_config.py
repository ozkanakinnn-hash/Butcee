#!/usr/bin/env python3
"""Firebase iOS uygulamasını (yoksa) oluşturur ve yapılandırmasını GITHUB_ENV'e yazar.

Böylece GoogleService-Info.plist değerlerini kimsenin elle kopyalaması gerekmez.
Ortam: FIREBASE_SA (servis hesabı JSON'u), BUNDLE_ID, FIREBASE_PROJECT (boşsa servis hesabındaki proje)
Çıktı (GITHUB_ENV, maskeli): FIREBASE_IOS_API_KEY, FIREBASE_IOS_APP_ID, FIREBASE_IOS_SENDER_ID,
                             FIREBASE_IOS_STORAGE_BUCKET, FIREBASE_IOS_PROJECT_ID
Gerekli paket: PyJWT[crypto]
"""
import base64, json, os, plistlib, sys, time, urllib.error, urllib.parse, urllib.request

import jwt

API = "https://firebase.googleapis.com/v1beta1"


def _fix_newlines_in_strings(text, mode):
    """JSON dizgeleri içindeki ham satır sonlarını onarır (sunucudaki scripts/init-admin.js ile aynı mantık).
    'escape': satır sonu → \\n (kaçışlar gerçek satır sonuna dönmüş); 'remove': satır sonu silinir (düzenleyici satırı sarmış)."""
    out, in_str, esc = [], False, False
    for ch in text:
        brk = ch in "\r\n"
        if not in_str:
            if ch == '"':
                in_str = True
            out.append(ch)
            continue
        if brk:
            if mode == "escape" and ch == "\n" and not esc:
                out.append("\\n")
            continue
        if esc:
            esc = False
            out.append(ch)
            continue
        if ch == "\\":
            esc = True
        elif ch == '"':
            in_str = False
        elif ch == "\t":
            out.append("\\t")
            continue
        out.append(ch)
    return "".join(out)


def _normalize_key(raw):
    import re, textwrap
    from cryptography.hazmat.primitives import serialization
    m = re.search(r"-----BEGIN ([A-Z ]+)-----([\s\S]*?)-----END \1-----", raw.replace("\\n", "\n"))
    if not m:
        return None
    body = re.sub(r"[^A-Za-z0-9+/=]", "", m.group(2))
    pem = f"-----BEGIN {m.group(1)}-----\n" + "\n".join(textwrap.wrap(body, 64)) + f"\n-----END {m.group(1)}-----\n"
    try:
        serialization.load_pem_private_key(pem.encode(), None)
        return pem
    except ValueError:
        return None


def load_service_account(raw):
    """Telefondan kopyalanan servis hesabı JSON'u sık bozulur; sunucunun okuyucusuyla aynı onarımlar denenir."""
    text = raw.lstrip("\ufeff").strip()
    failures = []
    for label, cand in (("olduğu gibi", text), ("satır sonları kaçışlandı", _fix_newlines_in_strings(text, "escape")),
                        ("sarma satır sonları silindi", _fix_newlines_in_strings(text, "remove"))):
        try:
            sa = json.loads(cand, strict=False)
        except ValueError as e:
            failures.append(f"{label}: {type(e).__name__} (konum {getattr(e, 'pos', '?')})")
            continue
        for k, v in list(sa.items()):
            if k != "private_key" and isinstance(v, str):
                sa[k] = "".join(v.split("\\\n")).replace("\r", "").replace("\n", "").strip()
        pem = _normalize_key(sa.get("private_key", ""))
        if not (pem and sa.get("client_email") and sa.get("project_id")):
            failures.append(f"{label}: JSON okundu ama anahtar/e-posta/proje geçersiz")
            continue
        if label != "olduğu gibi":
            print(f"Uyarı: servis hesabı metni onarıldı ({label}).")
        sa["private_key"] = pem
        return sa
    sys.exit("FIREBASE_SERVICE_ACCOUNT okunamadı:\n  " + "\n  ".join(failures))


def access_token(sa):
    now = int(time.time())
    assertion = jwt.encode({"iss": sa["client_email"], "scope": "https://www.googleapis.com/auth/cloud-platform",
                            "aud": "https://oauth2.googleapis.com/token", "iat": now, "exp": now + 3000},
                           sa["private_key"], algorithm="RS256")
    data = urllib.parse.urlencode({"grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
                                   "assertion": assertion}).encode()
    with urllib.request.urlopen("https://oauth2.googleapis.com/token", data) as r:
        return json.load(r)["access_token"]


def main():
    sa = load_service_account(os.environ["FIREBASE_SA"])
    project = os.environ.get("FIREBASE_PROJECT") or sa["project_id"]
    bundle = os.environ["BUNDLE_ID"]
    tok = access_token(sa)

    def call(method, url, body=None):
        req = urllib.request.Request(url if url.startswith("http") else API + url, method=method,
                                     data=json.dumps(body).encode() if body is not None else None,
                                     headers={"Authorization": "Bearer " + tok, "Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req) as r:
                return json.load(r)
        except urllib.error.HTTPError as e:
            msg = e.read().decode(errors="replace")[:600]
            if e.code == 403:
                sys.exit("Firebase servis hesabının iOS uygulaması oluşturma/okuma yetkisi yok (403). Firebase Console → "
                         "Proje ayarları → Uygulamalarım → iOS uygulaması ekle (bundle " + bundle + ") ile elle ekleyin "
                         "ya da hesaba 'Firebase Admin' rolü verin.\n" + msg)
            sys.exit(f"Firebase API hatası {e.code} ({method} {url}): {msg}")

    apps = call("GET", f"/projects/{project}/iosApps?pageSize=100").get("apps", [])
    app = next((a for a in apps if a.get("bundleId") == bundle), None)
    if not app:
        print(f"Firebase'de {bundle} iOS uygulaması yok, oluşturuluyor…")
        op = call("POST", f"/projects/{project}/iosApps", {"bundleId": bundle, "displayName": "Haber Akışı iOS"})
        for _ in range(30):
            if op.get("done"):
                break
            time.sleep(3)
            op = call("GET", "https://firebase.googleapis.com/v1beta1/" + op["name"])
        if "error" in op:
            sys.exit(f"Firebase iOS uygulaması oluşturulamadı: {op['error']}")
        app = op["response"]
    cfg = call("GET", f"/projects/{project}/iosApps/{app['appId']}/config")
    plist = plistlib.loads(base64.b64decode(cfg["configFileContents"]))
    values = {
        "FIREBASE_IOS_API_KEY": plist["API_KEY"],
        "FIREBASE_IOS_APP_ID": plist["GOOGLE_APP_ID"],
        "FIREBASE_IOS_SENDER_ID": plist["GCM_SENDER_ID"],
        "FIREBASE_IOS_STORAGE_BUCKET": plist.get("STORAGE_BUCKET", ""),
        "FIREBASE_IOS_PROJECT_ID": plist.get("PROJECT_ID", project),
    }
    with open(os.environ["GITHUB_ENV"], "a") as env:
        for k, v in values.items():
            print(f"::add-mask::{v}")
            env.write(f"{k}={v}\n")
    print(f"Firebase iOS yapılandırması hazır (uygulama {app['appId'].split(':')[0]}:…, proje {project}).")


if __name__ == "__main__":
    main()
