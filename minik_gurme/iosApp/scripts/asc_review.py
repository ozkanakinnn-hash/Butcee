#!/usr/bin/env python3
"""App Store Connect inceleme bilgisini günceller: not, ek dosya (video) ve isteğe bağlı yeniden gönderim.

Ortam: ASC_KEY_ID, ASC_ISSUER_ID, KEY_PATH (.p8), BUNDLE_ID, NOTES_FILE, ATTACHMENT (isteğe bağlı), RESUBMIT=1 (isteğe bağlı)
Gerekli paket: PyJWT[crypto]
"""
import hashlib, json, os, sys, time, urllib.error, urllib.request

import jwt

API = "https://api.appstoreconnect.apple.com/v1"


def token():
    now = int(time.time())
    return jwt.encode({"iss": os.environ["ASC_ISSUER_ID"], "iat": now, "exp": now + 1100, "aud": "appstoreconnect-v1"},
                      open(os.environ["KEY_PATH"]).read(), algorithm="ES256",
                      headers={"kid": os.environ["ASC_KEY_ID"], "typ": "JWT"})


def call(method, path, body=None):
    req = urllib.request.Request(path if path.startswith("http") else API + path, method=method,
                                 data=json.dumps(body).encode() if body is not None else None,
                                 headers={"Authorization": "Bearer " + token(), "Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as r:
            data = r.read()
            return json.loads(data) if data else {}
    except urllib.error.HTTPError as e:
        sys.exit(f"API hatası {e.code} ({method} {path}): {e.read().decode(errors='replace')[:1500]}")


def main():
    bundle = os.environ["BUNDLE_ID"]
    apps = call("GET", f"/apps?filter[bundleId]={bundle}")["data"]
    if not apps:
        sys.exit(f"{bundle} için uygulama yok")
    app_id = apps[0]["id"]
    versions = call("GET", f"/apps/{app_id}/appStoreVersions?filter[platform]=IOS&limit=10")["data"]
    for v in versions:
        print("Sürüm", v["attributes"]["versionString"], v["attributes"].get("appStoreState") or v["attributes"].get("appVersionState"))
    editable = [v for v in versions if (v["attributes"].get("appStoreState") or "") in
                ("REJECTED", "METADATA_REJECTED", "DEVELOPER_REJECTED", "PREPARE_FOR_SUBMISSION", "INVALID_BINARY")]
    if not editable:
        sys.exit("Düzenlenebilir (reddedilmiş/hazırlanan) sürüm yok")
    ver = editable[0]
    print("Seçilen sürüm:", ver["attributes"]["versionString"], ver["id"])

    # İnceleme ayrıntısı (yoksa oluştur)
    det = call("GET", f"/appStoreVersions/{ver['id']}/appStoreReviewDetail").get("data")
    notes = open(os.environ["NOTES_FILE"]).read().strip()
    if det:
        call("PATCH", f"/appStoreReviewDetails/{det['id']}", {"data": {"type": "appStoreReviewDetails", "id": det["id"],
             "attributes": {"notes": notes, "demoAccountRequired": False}}})
    else:
        det = call("POST", "/appStoreReviewDetails", {"data": {"type": "appStoreReviewDetails",
                   "attributes": {"notes": notes, "demoAccountRequired": False},
                   "relationships": {"appStoreVersion": {"data": {"type": "appStoreVersions", "id": ver["id"]}}}}})["data"]
    print("Not güncellendi (", len(notes), "karakter )")

    for att in [a.strip() for a in os.environ.get("ATTACHMENT", "").split(",") if a.strip()]:
        if not os.path.exists(att):
            sys.exit(f"Ek bulunamadı: {att}")
        # Apple yalnızca 1 ek kabul eder: mevcut ekleri sil
        for a in call("GET", f"/appStoreReviewDetails/{det['id']}/appStoreReviewAttachments").get("data", []):
            call("DELETE", f"/appStoreReviewAttachments/{a['id']}")
        data = open(att, "rb").read()
        res = call("POST", "/appStoreReviewAttachments", {"data": {"type": "appStoreReviewAttachments",
                   "attributes": {"fileName": os.path.basename(att), "fileSize": len(data)},
                   "relationships": {"appStoreReviewDetail": {"data": {"type": "appStoreReviewDetails", "id": det["id"]}}}}})["data"]
        for op in res["attributes"]["uploadOperations"]:
            chunk = data[op["offset"]:op["offset"] + op["length"]]
            req = urllib.request.Request(op["url"], method=op["method"], data=chunk,
                                         headers={h["name"]: h["value"] for h in op.get("requestHeaders", [])})
            urllib.request.urlopen(req).read()
        call("PATCH", f"/appStoreReviewAttachments/{res['id']}", {"data": {"type": "appStoreReviewAttachments", "id": res["id"],
             "attributes": {"uploaded": True, "sourceFileChecksum": hashlib.md5(data).hexdigest()}}})
        for _ in range(30):
            st = call("GET", f"/appStoreReviewAttachments/{res['id']}")["data"]["attributes"].get("assetDeliveryState", {})
            if st.get("state") in ("COMPLETE", "FAILED"):
                break
            time.sleep(5)
        print("Ek yüklendi:", os.path.basename(att), st)
        if st.get("state") == "FAILED":
            sys.exit(f"Ek işlenemedi: {st}")

    if os.environ.get("RESUBMIT") == "1":
        subs = call("GET", f"/reviewSubmissions?filter[app]={app_id}&filter[platform]=IOS&limit=20")["data"]
        for s in subs:
            print("İnceleme gönderimi", s["id"], s["attributes"].get("state"))
        open_sub = [s for s in subs if s["attributes"].get("state") in ("UNRESOLVED_ISSUES", "READY_FOR_REVIEW")]
        if not open_sub:
            sys.exit("Yeniden gönderilecek inceleme kaydı bulunamadı")
        call("PATCH", f"/reviewSubmissions/{open_sub[0]['id']}", {"data": {"type": "reviewSubmissions", "id": open_sub[0]["id"],
             "attributes": {"submitted": True}}})
        print("Yeniden incelemeye gönderildi.")


if __name__ == "__main__":
    main()
