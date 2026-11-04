#!/usr/bin/env bash
# Küçültülmüş (R8) release APK'yı emülatöre kurar, açar ve çökmediğini doğrular.
set -u
PKG=com.ilactakip.ilac_takip
APK=build/app/outputs/flutter-apk/app-x86_64-release.apk
mkdir -p screenshots
adb uninstall "$PKG" >/dev/null 2>&1 || true
adb install -r "$APK" || { echo "RELEASE_SMOKE: kurulum BAŞARISIZ"; exit 1; }
adb logcat -c
adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null
sleep 20
adb exec-out screencap -p > screenshots/00_release_acilis.png
PID=$(adb shell pidof "$PKG" | tr -d '\r')
CRASH=$(adb logcat -b crash -d | grep -i "$PKG" || true)
FATAL=$(adb logcat -d | grep -E "FATAL EXCEPTION|E/flutter" | head -20 || true)
echo "RELEASE_SMOKE: pid=$PID"
[ -n "$FATAL" ] && echo "RELEASE_SMOKE: hatalar:" && echo "$FATAL"
if [ -z "$PID" ] || [ -n "$CRASH" ]; then
  echo "RELEASE_SMOKE: ÇÖKTÜ"; echo "$CRASH"; exit 1
fi
echo "RELEASE_SMOKE: tamam"
