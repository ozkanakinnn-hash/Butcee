#!/usr/bin/env python3
"""App Store imzası için geçici dağıtım sertifikası + App Store profilleri (cihaz gerekmez).

Kullanım (GitHub Actions, macOS):
  asc_signing.py setup   <çalışma klasörü>   → sertifika (.p12) ve profilleri oluşturur, kurar; GITHUB_ENV'e yazar
  asc_signing.py cleanup <çalışma klasörü>   → oluşturulan sertifika ve profilleri Apple'dan siler

Ortam: ASC_KEY_ID, ASC_ISSUER_ID, KEY_PATH (.p8), BUNDLE_IDS (virgülle, ilki ana uygulama: akin.aiN,akin.aiN.widget)
İsteğe bağlı: BUNDLE_CAPABILITIES (virgülle, ör. PUSH_NOTIFICATIONS) → kimlik yoksa kaydedilir, yetenekler açılır;
             REQUIRE_APP_RECORD=1 → App Store Connect'te uygulama kaydı yoksa anlaşılır bir hatayla durur.
Gerekli paket: PyJWT[crypto]
"""
import base64, json, os, subprocess, sys, time, urllib.error, urllib.request

import jwt  # PyJWT

API = "https://api.appstoreconnect.apple.com/v1"


def token():
    now = int(time.time())
    key = open(os.environ["KEY_PATH"]).read()
    return jwt.encode({"iss": os.environ["ASC_ISSUER_ID"], "iat": now, "exp": now + 1100, "aud": "appstoreconnect-v1"},
                      key, algorithm="ES256", headers={"kid": os.environ["ASC_KEY_ID"], "typ": "JWT"})


def call(method, path, body=None, ok=()):
    req = urllib.request.Request(API + path, method=method, data=json.dumps(body).encode() if body else None,
                                 headers={"Authorization": "Bearer " + token(), "Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as r:
            data = r.read()
            return json.loads(data) if data else {}
    except urllib.error.HTTPError as e:
        msg = e.read().decode(errors="replace")
        if e.code in ok:
            return None
        raise SystemExit(f"App Store Connect API hatası {e.code} ({method} {path}): {msg}")


def sh(*args, **kw):
    subprocess.run(args, check=True, **kw)


def setup(work):
    os.makedirs(work, exist_ok=True)
    state = {"profiles": []}

    # 0) Uygulama kaydını sertifikadan önce denetle (boşuna sertifika oluşturmamak için)
    if os.environ.get("REQUIRE_APP_RECORD") == "1":
        bid0 = os.environ["BUNDLE_IDS"].split(",")[0]
        if not [b for b in call("GET", f"/bundleIds?filter[identifier]={bid0}&limit=200")["data"]
                if b["attributes"]["identifier"] == bid0]:
            pass  # kimlik 3. adımda kaydedilecek; kayıt denetimi orada
        elif not call("GET", f"/apps?filter[bundleId]={bid0}")["data"]:
            raise SystemExit(f"UYGULAMA_KAYDI_YOK: App Store Connect'te '{bid0}' için uygulama kaydı yok. "
                             "Apps → + → New App ile oluşturup yüklemeyi yeniden başlatın.")

    # 1) CSR + dağıtım sertifikası
    keyf, csrf = f"{work}/ci.key", f"{work}/ci.csr"
    sh("/usr/bin/openssl", "req", "-new", "-newkey", "rsa:2048", "-nodes", "-keyout", keyf, "-out", csrf,
       "-subj", "/CN=App Store CI/O=akin.ai/C=TR")
    csr = open(csrf).read()
    try:
        cert = call("POST", "/certificates", {"data": {"type": "certificates", "attributes": {
            "certificateType": "DISTRIBUTION", "csrContent": csr}}})["data"]
    except SystemExit as e:
        if "maximum" in str(e).lower() or "limit" in str(e).lower():
            raise SystemExit("Dağıtım sertifikası sınırına ulaşıldı. developer.apple.com → Certificates bölümünden "
                             "kullanılmayan 'Apple Distribution' sertifikalarını iptal edip (Revoke) tekrar deneyin.\n" + str(e))
        raise
    state["certificate"] = cert["id"]
    json.dump(state, open(f"{work}/state.json", "w"))
    der = f"{work}/ci.cer"
    open(der, "wb").write(base64.b64decode(cert["attributes"]["certificateContent"]))
    pem = f"{work}/ci.pem"
    sh("/usr/bin/openssl", "x509", "-inform", "DER", "-in", der, "-out", pem)
    p12 = f"{work}/ci.p12"
    sh("/usr/bin/openssl", "pkcs12", "-export", "-inkey", keyf, "-in", pem, "-out", p12, "-passout", "pass:ci")

    # 2) Geçici anahtarlık
    kc, kcpw = f"{work}/ci.keychain-db", "ci-keychain"
    sh("security", "create-keychain", "-p", kcpw, kc)
    sh("security", "set-keychain-settings", "-lut", "21600", kc)
    sh("security", "unlock-keychain", "-p", kcpw, kc)
    existing = subprocess.run(["security", "list-keychains", "-d", "user"], capture_output=True, text=True).stdout
    sh("security", "list-keychains", "-d", "user", "-s", kc, *[k.strip().strip('"') for k in existing.split() if k.strip()])
    wwdr = f"{work}/AppleWWDRCAG3.cer"
    sh("curl", "-sSfL", "-o", wwdr, "https://www.apple.com/certificateauthority/AppleWWDRCAG3.cer")
    subprocess.run(["security", "import", wwdr, "-k", kc, "-T", "/usr/bin/codesign"], check=False)
    sh("security", "import", p12, "-k", kc, "-P", "ci", "-T", "/usr/bin/codesign", "-T", "/usr/bin/security")
    sh("security", "set-key-partition-list", "-S", "apple-tool:,apple:,codesign:", "-s", "-k", kcpw, kc,
       stdout=subprocess.DEVNULL)

    # 3) App Store profilleri
    run = os.environ.get("GITHUB_RUN_ID", str(int(time.time()))) + "-" + os.environ.get("GITHUB_RUN_ATTEMPT", "1")
    profile_names = {}
    dirs = [os.path.expanduser("~/Library/MobileDevice/Provisioning Profiles"),
            os.path.expanduser("~/Library/Developer/Xcode/UserData/Provisioning Profiles")]
    for d in dirs:
        os.makedirs(d, exist_ok=True)
    for bid in os.environ["BUNDLE_IDS"].split(","):
        found = [b for b in call("GET", f"/bundleIds?filter[identifier]={bid}&limit=200")["data"]
                 if b["attributes"]["identifier"] == bid]
        if not found:
            print(f"'{bid}' kimliği yok, kaydediliyor…")
            found = [call("POST", "/bundleIds", {"data": {"type": "bundleIds", "attributes": {
                "identifier": bid, "name": os.environ.get("BUNDLE_NAME", bid).replace(".", " "), "platform": "IOS"}}})["data"]]
        for capability in filter(None, os.environ.get("BUNDLE_CAPABILITIES", "").split(",")):
            have = [c["attributes"]["capabilityType"] for c in
                    call("GET", f"/bundleIds/{found[0]['id']}/bundleIdCapabilities")["data"]]
            if capability not in have:
                call("POST", "/bundleIdCapabilities", {"data": {"type": "bundleIdCapabilities",
                     "attributes": {"capabilityType": capability},
                     "relationships": {"bundleId": {"data": {"type": "bundleIds", "id": found[0]["id"]}}}}}, ok=(409,))
                print(f"{bid}: {capability} açıldı")
        if os.environ.get("REQUIRE_APP_RECORD") == "1" and bid == os.environ["BUNDLE_IDS"].split(",")[0]:
            if not call("GET", f"/apps?filter[bundleId]={bid}")["data"]:
                raise SystemExit(f"UYGULAMA_KAYDI_YOK: '{bid}' kimliği hazır, ama App Store Connect'te uygulama kaydı yok. "
                                 "App Store Connect → Apps → + → New App ile bu Bundle ID'yi seçerek uygulamayı oluşturun, "
                                 "sonra yüklemeyi yeniden başlatın.")
        name = f"CI {bid} {run}"
        prof = call("POST", "/profiles", {"data": {"type": "profiles", "attributes": {"name": name, "profileType": "IOS_APP_STORE"},
                    "relationships": {"bundleId": {"data": {"type": "bundleIds", "id": found[0]["id"]}},
                                      "certificates": {"data": [{"type": "certificates", "id": cert["id"]}]}}}})["data"]
        state["profiles"].append(prof["id"])
        json.dump(state, open(f"{work}/state.json", "w"))
        content = base64.b64decode(prof["attributes"]["profileContent"])
        for d in dirs:
            open(f"{d}/{prof['attributes']['uuid']}.mobileprovision", "wb").write(content)
        profile_names[bid] = name
        print(f"Profil hazır: {name}")

    json.dump(profile_names, open(f"{work}/profiles.json", "w"))
    with open(os.environ["GITHUB_ENV"], "a") as env:
        env.write(f"SIGN_KEYCHAIN={kc}\n")
        # İlk kimlik ana uygulama → APP_PROFILE; sonrakiler PROFILE_2, PROFILE_3 … (widget vb. uzantılar).
        # Bu betiğin ilk sürümündeki adla uyum için ikinci kimlik ayrıca WIDGET_PROFILE olarak da yazılır.
        bids = os.environ["BUNDLE_IDS"].split(",")
        env.write(f"APP_PROFILE={profile_names[bids[0]]}\n")
        for i, b in enumerate(bids[1:], start=2):
            env.write(f"PROFILE_{i}={profile_names[b]}\n")
        if len(bids) > 1:
            env.write(f"WIDGET_PROFILE={profile_names[bids[1]]}\n")
    print("İmza hazırlığı tamam.")


def cleanup(work):
    try:
        state = json.load(open(f"{work}/state.json"))
    except FileNotFoundError:
        print("Silinecek bir şey yok.")
        return
    for pid in state.get("profiles", []):
        try:
            call("DELETE", f"/profiles/{pid}")
        except SystemExit as e:
            print(e)
    if state.get("certificate"):
        try:
            call("DELETE", f"/certificates/{state['certificate']}")
            print("Geçici sertifika iptal edildi.")
        except SystemExit as e:
            print(e)
    subprocess.run(["security", "delete-keychain", f"{work}/ci.keychain-db"], check=False)


if __name__ == "__main__":
    {"setup": setup, "cleanup": cleanup}[sys.argv[1]](sys.argv[2])
