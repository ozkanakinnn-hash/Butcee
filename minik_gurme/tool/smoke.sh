#!/usr/bin/env bash
# Release APK'yı emülatöre kurar; her sekmeyi Türkçe ve İngilizce açıp ekran görüntüsü alır,
# çökme olmadığını doğrular. Başarılıysa "SMOKE: tamam" yazar.
set -u
PKG=com.aistudio.minikgurme.kqxvpt
APK=app/build/outputs/apk/release/app-release.apk
OUT=screenshots
mkdir -p "$OUT"

shot() { adb exec-out screencap -p > "$OUT/$1.png"; echo "SMOKE: görüntü $1"; }

# resource-id (testTag) ile dokun
tap_id() {
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  local xml b
  xml=$(adb shell cat /sdcard/ui.xml)
  b=$(echo "$xml" | tr '>' '\n' | grep "resource-id=\"$1\"" | head -1 | grep -o 'bounds="[^"]*"' | grep -oE '[0-9]+' | tr '\n' ' ')
  if [ -z "$b" ]; then echo "SMOKE: bulunamadı $1"; return 1; fi
  set -- $b
  adb shell input tap $(( ($1 + $3) / 2 )) $(( ($2 + $4) / 2 ))
  sleep 3
}

# Görünen metne göre dokun (sistem diyalogları için)
tap_text() {
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  local b
  b=$(adb shell cat /sdcard/ui.xml | tr '>' '\n' | grep "text=\"$1\"" | head -1 | grep -o 'bounds="[^"]*"' | grep -oE '[0-9]+' | tr '\n' ' ')
  [ -z "$b" ] && return 1
  set -- $b
  adb shell input tap $(( ($1 + $3) / 2 )) $(( ($2 + $4) / 2 ))
  sleep 2
}

check_alive() {
  local pid crash
  pid=$(adb shell pidof "$PKG" | tr -d '\r')
  crash=$(adb logcat -b crash -d | grep -A30 "$PKG" || true)
  if [ -z "$pid" ] || [ -n "$crash" ]; then
    echo "SMOKE: ÇÖKTÜ ($1)"; echo "$crash"
    adb logcat -d | grep -E "FATAL EXCEPTION|AndroidRuntime" -A30 | head -80
    exit 1
  fi
}

adb uninstall "$PKG" >/dev/null 2>&1 || true
adb install -r "$APK" || { echo "SMOKE: kurulum BAŞARISIZ"; exit 1; }
adb logcat -c

run_tabs() {
  local lang=$1
  adb shell am force-stop "$PKG"
  adb shell am start -n "$PKG/com.example.MainActivity" --es lang "$lang" >/dev/null
  sleep 12
  tap_text "Wait" || true
  check_alive "açılış $lang"
  shot "${lang}_01_ogunler"
  local i=2
  for tab in nav_foods100 nav_recipes nav_recommendations nav_guide; do
    tap_id "$tab" || true
    check_alive "$tab $lang"
    shot "${lang}_0${i}_${tab#nav_}"
    i=$((i + 1))
  done
  tap_id top_profile_pill || true
  check_alive "profil $lang"
  shot "${lang}_06_profil"
}

run_tabs tr
# Dil seçiciyi gerçekten dene: profilde English'e dokun
tap_id lang_en || true
sleep 2
check_alive "dil değişimi"
shot "en_00_profil_dil_secildi"
run_tabs en

echo "SMOKE: tamam"
