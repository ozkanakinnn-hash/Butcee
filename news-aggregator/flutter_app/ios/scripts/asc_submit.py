#!/usr/bin/env python3
"""App Store'a gönderim: sürümü hazırlar, derlemeyi bağlar, "Yenilikler" metnini yazar ve incelemeye gönderir.

Ortam: ASC_KEY_ID, ASC_ISSUER_ID, KEY_PATH (.p8), BUNDLE_ID, VERSION (ör. 1.0.4), BUILD (ör. 12; boşsa en yeni),
       WHATS_NEW_FILE (isteğe bağlı, Türkçe metin), CANCEL_PENDING=1 (incelemede bekleyen eski gönderim iptal edilir)
Gerekli paket: PyJWT[crypto]

Akış:
 1. Derleme (VERSION + BUILD) bulunur; işlenmesi bitmemişse beklenir. Şifreleme sorusu "muaf" (HTTPS) olarak cevaplanır.
 2. Düzenlenebilir sürüm varsa (hazırlanıyor/reddedildi) numarası VERSION yapılır; yoksa VERSION sürümü oluşturulur.
    İncelemede bekleyen bir gönderim varsa (CANCEL_PENDING=1) önce iptal edilir — Apple aynı anda tek gönderime izin verir.
 3. Derleme sürüme bağlanır, Yenilikler metni yazılır (ilk sürümde Apple izin vermez → atlanır).
 4. İnceleme gönderimi oluşturulur ve gönderilir.
"""
import json, os, sys, time, urllib.error, urllib.request

import jwt

API = "https://api.appstoreconnect.apple.com/v1"
EDITABLE = ("PREPARE_FOR_SUBMISSION", "DEVELOPER_REJECTED", "REJECTED", "METADATA_REJECTED", "INVALID_BINARY")
WAITING = ("WAITING_FOR_REVIEW", "IN_REVIEW", "READY_FOR_REVIEW")


def token():
    now = int(time.time())
    return jwt.encode({"iss": os.environ["ASC_ISSUER_ID"], "iat": now, "exp": now + 1100, "aud": "appstoreconnect-v1"},
                      open(os.environ["KEY_PATH"]).read(), algorithm="ES256",
                      headers={"kid": os.environ["ASC_KEY_ID"], "typ": "JWT"})


class ApiError(Exception):
    def __init__(self, code, text):
        super().__init__(f"{code}: {text[:800]}")
        self.code = code


def call(method, path, body=None):
    req = urllib.request.Request(path if path.startswith("http") else API + path, method=method,
                                 data=json.dumps(body).encode() if body is not None else None,
                                 headers={"Authorization": "Bearer " + token(), "Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as r:
            data = r.read()
            return json.loads(data) if data else {}
    except urllib.error.HTTPError as e:
        raise ApiError(e.code, e.read().decode(errors="replace"))


def state(v):
    a = v["attributes"]
    return a.get("appStoreState") or a.get("appVersionState") or "?"


def step(msg):
    print(f"\n▶ {msg}", flush=True)


def main():
    bundle, version = os.environ["BUNDLE_ID"], os.environ["VERSION"].strip()
    want_build = (os.environ.get("BUILD") or "").strip()

    apps = call("GET", f"/apps?filter[bundleId]={bundle}")["data"]
    if not apps:
        sys.exit(f"✘ {bundle} için App Store Connect'te uygulama yok")
    app_id = apps[0]["id"]

    # 1) Derleme
    step(f"Derleme aranıyor: {version} ({want_build or 'en yeni'})")
    build = None
    for _ in range(40):  # en fazla ~20 dk işlenmeyi bekle
        q = f"/builds?filter[app]={app_id}&filter[preReleaseVersion.version]={version}&sort=-uploadedDate&limit=20"
        if want_build:
            q += f"&filter[version]={want_build}"
        builds = call("GET", q)["data"]
        if builds:
            build = builds[0]
            ps = build["attributes"].get("processingState")
            print(f"  derleme {build['attributes']['version']} durum: {ps}")
            if ps == "VALID":
                break
            if ps in ("FAILED", "INVALID"):
                sys.exit("✘ Apple derlemeyi işleyemedi (FAILED/INVALID). Yeni derleme yüklenmeli.")
        else:
            print("  derleme henüz görünmüyor, bekleniyor…")
        time.sleep(30)
    else:
        sys.exit("✘ Derleme 20 dakikada hazır olmadı; biraz sonra iş akışını yeniden çalıştırın.")
    if build["attributes"].get("usesNonExemptEncryption") is None:
        call("PATCH", f"/builds/{build['id']}", {"data": {"type": "builds", "id": build["id"],
             "attributes": {"usesNonExemptEncryption": False}}})
        print("  şifreleme sorusu cevaplandı: yalnızca muaf (HTTPS)")

    # 2) Sürüm
    step("Mağaza sürümleri")
    versions = call("GET", f"/apps/{app_id}/appStoreVersions?filter[platform]=IOS&limit=20")["data"]
    for v in versions:
        print(f"  {v['attributes']['versionString']:8} {state(v)}")

    # İncelemede bekleyen gönderim (eski sürüm) → iptal (aynı anda tek gönderim olabilir)
    pending = [v for v in versions if state(v) in WAITING and v["attributes"]["versionString"] != version]
    if pending:
        if os.environ.get("CANCEL_PENDING") != "1":
            sys.exit(f"✘ {pending[0]['attributes']['versionString']} incelemede ({state(pending[0])}). "
                     "Önce iptal edilmeli (CANCEL_PENDING=1).")
        step(f"İncelemedeki {pending[0]['attributes']['versionString']} gönderimi iptal ediliyor")
        subs = call("GET", f"/reviewSubmissions?filter[app]={app_id}&filter[platform]=IOS&limit=20")["data"]
        for s in subs:
            st = s["attributes"].get("state")
            if st in ("WAITING_FOR_REVIEW", "IN_REVIEW", "UNRESOLVED_ISSUES"):
                call("PATCH", f"/reviewSubmissions/{s['id']}", {"data": {"type": "reviewSubmissions", "id": s["id"],
                     "attributes": {"canceled": True}}})
                print(f"  gönderim {s['id']} iptal edildi ({st})")
        for _ in range(20):  # iptalin işlenmesini bekle
            time.sleep(15)
            versions = call("GET", f"/apps/{app_id}/appStoreVersions?filter[platform]=IOS&limit=20")["data"]
            if not any(state(v) in WAITING for v in versions):
                break
        for v in versions:
            print(f"  {v['attributes']['versionString']:8} {state(v)}")

    same = [v for v in versions if v["attributes"]["versionString"] == version]
    editable = [v for v in versions if state(v) in EDITABLE]
    if same and state(same[0]) in WAITING:
        print(f"✔ {version} zaten incelemede ({state(same[0])}); yapılacak bir şey yok.")
        return
    if same and state(same[0]) in EDITABLE:
        ver = same[0]
    elif editable:
        ver = editable[0]
        step(f"Düzenlenebilir sürüm {ver['attributes']['versionString']} → {version}")
        call("PATCH", f"/appStoreVersions/{ver['id']}", {"data": {"type": "appStoreVersions", "id": ver["id"],
             "attributes": {"versionString": version}}})
    else:
        step(f"Yeni sürüm oluşturuluyor: {version}")
        ver = call("POST", "/appStoreVersions", {"data": {"type": "appStoreVersions",
                   "attributes": {"platform": "IOS", "versionString": version},
                   "relationships": {"app": {"data": {"type": "apps", "id": app_id}}}}})["data"]
    print(f"  sürüm kimliği {ver['id']}")

    # 3) Derlemeyi bağla
    step(f"Derleme bağlanıyor: {version} ({build['attributes']['version']})")
    call("PATCH", f"/appStoreVersions/{ver['id']}/relationships/build", {"data": {"type": "builds", "id": build["id"]}})

    wn_file = os.environ.get("WHATS_NEW_FILE")
    if wn_file and os.path.exists(wn_file):
        text = open(wn_file, encoding="utf-8").read().strip()
        step("'Bu sürümdeki yenilikler' yazılıyor")
        locs = call("GET", f"/appStoreVersions/{ver['id']}/appStoreVersionLocalizations")["data"]
        for loc in locs:
            try:
                call("PATCH", f"/appStoreVersionLocalizations/{loc['id']}", {"data": {"type": "appStoreVersionLocalizations",
                     "id": loc["id"], "attributes": {"whatsNew": text}}})
                print(f"  {loc['attributes']['locale']}: yazıldı")
            except ApiError as e:
                print(f"  {loc['attributes']['locale']}: atlandı ({e.code}; ilk sürümde Apple izin vermez)")

    # 4) İncelemeye gönder
    step("İncelemeye gönderiliyor")
    subs = call("GET", f"/reviewSubmissions?filter[app]={app_id}&filter[platform]=IOS&filter[state]=READY_FOR_REVIEW&limit=5")["data"]
    sub = subs[0] if subs else call("POST", "/reviewSubmissions", {"data": {"type": "reviewSubmissions",
          "attributes": {"platform": "IOS"}, "relationships": {"app": {"data": {"type": "apps", "id": app_id}}}}})["data"]
    try:
        call("POST", "/reviewSubmissionItems", {"data": {"type": "reviewSubmissionItems",
             "relationships": {"reviewSubmission": {"data": {"type": "reviewSubmissions", "id": sub["id"]}},
                               "appStoreVersion": {"data": {"type": "appStoreVersions", "id": ver["id"]}}}}})
    except ApiError as e:
        if e.code != 409:  # zaten ekliyse sorun değil
            raise
        print(f"  sürüm gönderime zaten ekli ya da eksik bilgi var: {e}")
    call("PATCH", f"/reviewSubmissions/{sub['id']}", {"data": {"type": "reviewSubmissions", "id": sub["id"],
         "attributes": {"submitted": True}}})
    time.sleep(5)
    v = call("GET", f"/appStoreVersions/{ver['id']}")["data"]
    print(f"\n✔ {version} ({build['attributes']['version']}) incelemeye gönderildi. Sürüm durumu: {state(v)}")


if __name__ == "__main__":
    try:
        main()
    except ApiError as e:
        sys.exit(f"✘ App Store Connect hatası {e}")
