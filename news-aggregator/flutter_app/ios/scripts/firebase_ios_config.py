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


def load_service_account(raw):
    """Telefondan yapıştırılan JSON bozulmuş olabilir (gerçek satır sonları, fazla ters eğik çizgi).
    Önce normal okunur; olmazsa yalnızca gereken alanlar ayıklanır ve özel anahtar PEM olarak yeniden kurulur."""
    import re, textwrap

    def clean_pem(key):
        body = re.sub(r"-----(BEGIN|END)[A-Z ]*-----", "", key)
        body = re.sub(r"\\n|\\", "", body)          # kaçış kalıntıları
        body = re.sub(r"[^A-Za-z0-9+/=]", "", body)
        return "-----BEGIN PRIVATE KEY-----\n" + "\n".join(textwrap.wrap(body, 64)) + "\n-----END PRIVATE KEY-----\n"

    raw = raw.strip()
    try:
        sa = json.loads(raw, strict=False)
        sa["private_key"] = clean_pem(sa["private_key"])
        return sa
    except (ValueError, KeyError):
        pass

    def field(name):
        m = re.search(r'"%s"\s*:\s*"([^"]*)"' % name, raw)
        if not m:
            sys.exit(f"FIREBASE_SERVICE_ACCOUNT içinde '{name}' alanı bulunamadı; servis hesabı JSON'unu yeniden yapıştırın.")
        return m.group(1)
    pem = clean_pem(field("private_key"))
    print("Servis hesabı JSON'u bozuk biçimli; alanlar ayıklanarak okundu.")
    return {"client_email": field("client_email"), "project_id": field("project_id"), "private_key": pem}


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
