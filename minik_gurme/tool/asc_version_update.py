#!/usr/bin/env python3
"""App Store Connect: düzenlenebilir iOS sürümüne (ör. inceleme sonrası REJECTED) yeni yapıyı bağlar,
ekran görüntülerini (her dil için) yenileriyle değiştirir ve inceleme notunu günceller. Yeniden gönderim YAPMAZ.

Ortam: ASC_KEY_ID, ASC_ISSUER_ID, KEY_PATH, BUNDLE_ID,
       SHOTS_DIR (…/appstore; içinde tr/ ve en/ PNG'ler), ATTACH_BUILD=1, NOTES_FILE (isteğe bağlı)
"""
import glob, hashlib, json, os, sys, time, urllib.error, urllib.request

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


def upload(create_path, rel_name, rel_type, rel_id, kind, path):
    data = open(path, "rb").read()
    res = call("POST", create_path, {"data": {"type": kind,
               "attributes": {"fileName": os.path.basename(path), "fileSize": len(data)},
               "relationships": {rel_name: {"data": {"type": rel_type, "id": rel_id}}}}})["data"]
    for op in res["attributes"]["uploadOperations"]:
        chunk = data[op["offset"]:op["offset"] + op["length"]]
        req = urllib.request.Request(op["url"], method=op["method"], data=chunk,
                                     headers={h["name"]: h["value"] for h in op.get("requestHeaders", [])})
        urllib.request.urlopen(req).read()
    call("PATCH", f"/{kind}/{res['id']}", {"data": {"type": kind, "id": res["id"],
         "attributes": {"uploaded": True, "sourceFileChecksum": hashlib.md5(data).hexdigest()}}})
    return res["id"]


def main():
    bundle = os.environ["BUNDLE_ID"]
    app_id = call("GET", f"/apps?filter[bundleId]={bundle}")["data"][0]["id"]
    versions = call("GET", f"/apps/{app_id}/appStoreVersions?filter[platform]=IOS&limit=10")["data"]
    for v in versions:
        print("Sürüm", v["attributes"]["versionString"], v["attributes"].get("appStoreState"))
    editable = [v for v in versions if (v["attributes"].get("appStoreState") or "") in
                ("REJECTED", "METADATA_REJECTED", "DEVELOPER_REJECTED", "PREPARE_FOR_SUBMISSION", "INVALID_BINARY")]
    if not editable:
        sys.exit("Düzenlenebilir sürüm yok (inceleme bekliyorsa önce App Store Connect'te reddedilmiş/düzenlenebilir olmalı)")
    ver = editable[0]; vs = ver["attributes"]["versionString"]
    print("Seçilen sürüm:", vs, ver["id"], ver["attributes"].get("appStoreState"))

    # 1) Yeni yapıyı bağla (işlenmesi bitene kadar bekle)
    if os.environ.get("ATTACH_BUILD") == "1":
        cur = call("GET", f"/appStoreVersions/{ver['id']}/build").get("data")
        cur_no = cur["attributes"]["version"] if cur else None
        print("Bağlı yapı:", cur_no)
        best = None
        for _ in range(60):
            builds = call("GET", f"/builds?filter[app]={app_id}&filter[preReleaseVersion.version]={vs}&sort=-uploadedDate&limit=5")["data"]
            if builds:
                b = builds[0]; st = b["attributes"].get("processingState")
                print("En yeni yapı:", b["attributes"]["version"], st)
                if st == "VALID":
                    best = b; break
                if st in ("FAILED", "INVALID"):
                    sys.exit(f"Yapı işlenemedi: {st}")
            time.sleep(60)
        if not best:
            sys.exit("Yapı 60 dakikada işlenmedi")
        if best["attributes"]["version"] != cur_no:
            call("PATCH", f"/appStoreVersions/{ver['id']}/relationships/build", {"data": {"type": "builds", "id": best["id"]}})
            print("Sürüme bağlanan yapı:", best["attributes"]["version"])

    # 2) Ekran görüntüleri: her yerelleştirmedeki mevcut setleri yenileriyle değiştir
    shots_dir = os.environ.get("SHOTS_DIR")
    if shots_dir:
        locs = call("GET", f"/appStoreVersions/{ver['id']}/appStoreVersionLocalizations")["data"]
        for loc in locs:
            locale = loc["attributes"]["locale"]
            lang = "tr" if locale.lower().startswith("tr") else "en"
            files = sorted(glob.glob(os.path.join(shots_dir, lang, "*.png")))
            if not files:
                print("Görsel yok:", locale); continue
            sets = call("GET", f"/appStoreVersionLocalizations/{loc['id']}/appScreenshotSets")["data"]
            if not sets:
                sys.exit(f"{locale}: ekran görüntüsü seti yok (önce App Store Connect'te bir set olmalı)")
            for s in sets:
                typ = s["attributes"]["screenshotDisplayType"]
                old = call("GET", f"/appScreenshotSets/{s['id']}/appScreenshots")["data"]
                if not old:
                    continue
                print(f"{locale} {typ}: {len(old)} eski görsel siliniyor, {len(files)} yeni yükleniyor")
                for o in old:
                    call("DELETE", f"/appScreenshots/{o['id']}")
                ids = [upload("/appScreenshots", "appScreenshotSet", "appScreenshotSets", s["id"], "appScreenshots", f) for f in files]
                for _ in range(40):
                    states = [call("GET", f"/appScreenshots/{i}")["data"]["attributes"].get("assetDeliveryState", {}).get("state") for i in ids]
                    if all(x in ("COMPLETE", "FAILED") for x in states):
                        break
                    time.sleep(5)
                print("  durum:", states)
                if "FAILED" in states:
                    sys.exit(f"{locale}: görsel işlenemedi")
                call("PATCH", f"/appScreenshotSets/{s['id']}/relationships/appScreenshots",
                     {"data": [{"type": "appScreenshots", "id": i} for i in ids]})

    # 3) İnceleme notu
    nf = os.environ.get("NOTES_FILE")
    if nf and os.path.exists(nf):
        det = call("GET", f"/appStoreVersions/{ver['id']}/appStoreReviewDetail").get("data")
        if det:
            call("PATCH", f"/appStoreReviewDetails/{det['id']}", {"data": {"type": "appStoreReviewDetails", "id": det["id"],
                 "attributes": {"notes": open(nf).read().strip()}}})
            print("İnceleme notu güncellendi")
    print("Bitti. Yeniden gönderim App Store Connect'ten elle yapılır.")


if __name__ == "__main__":
    main()
