#!/usr/bin/env bash
# Emülatörde uygulamayı açıp ekran görüntüleri alır (CI içinde çalışır).
set -x
PKG=com.abacusai.ramazanrehberi.t1793533738
OUT=screenshots
mkdir -p $OUT

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS || true

write_prefs () { # $1 = festival preview ("" / RAMAZAN), $2 = theme mode
  cat > /tmp/p.xml <<XML
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="key_onboarding_completed" value="true" />
    <string name="key_user_name">Özkan</string>
    <string name="key_theme_mode">$2</string>
    <string name="key_festival_preview">$1</string>
</map>
XML
  adb shell am force-stop $PKG
  adb push /tmp/p.xml /data/local/tmp/p.xml
  adb shell "run-as $PKG mkdir -p shared_prefs"
  adb shell "run-as $PKG sh -c 'cat /data/local/tmp/p.xml > shared_prefs/ramazan_rehberi_prefs.xml'"
}

shot () { sleep "${2:-3}"; adb exec-out screencap -p > "$OUT/$1.png"; }

tap_text () { # ekrandaki metne dokun
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null; adb pull /sdcard/ui.xml /tmp/ui.xml >/dev/null
  python3 - "$1" <<'PY' > /tmp/xy
import re,sys
s=open('/tmp/ui.xml',encoding='utf-8').read()
for m in re.finditer(r'<node [^>]*>', s):
    n=m.group(0)
    t=re.search(r'text="([^"]*)"',n); d=re.search(r'content-desc="([^"]*)"',n)
    if (t and t.group(1)==sys.argv[1]) or (d and d.group(1)==sys.argv[1]):
        b=list(map(int,re.findall(r'\d+',re.search(r'bounds="([^"]*)"',n).group(1))))
        print((b[0]+b[2])//2,(b[1]+b[3])//2); break
PY
  [ -s /tmp/xy ] && adb shell input tap $(cat /tmp/xy)
}

launch () { adb shell am start -n $PKG/com.example.MainActivity; sleep 15; }

# 1) Koyu tema, normal gün
write_prefs "" "DARK"
launch
shot 01_ana_sayfa 2
adb shell input swipe 540 1700 540 700 500; shot 02_ana_sayfa_asagi
adb shell input swipe 540 1700 540 700 500; shot 03_ana_sayfa_radyo_menu
tap_text "Ramazan Rehberi"; shot 04_ramazan_rehberi
tap_text "Takvim"; shot 05_takvim
tap_text "Ayet & Dua"; shot 06_ayet_dua
tap_text "Ana Sayfa"; sleep 2
tap_text "Kıble Bulucu"; shot 07_kible 4
adb shell input keyevent KEYCODE_BACK; sleep 2
tap_text "Ayarlar"; shot 08_ayarlar
adb shell input swipe 540 1800 540 500 500; adb shell input swipe 540 1800 540 500 500; shot 09_ayarlar_tema

# 2) Bayram süslemesi önizlemesi (aydınlık tema)
write_prefs "RAMAZAN" "LIGHT"
launch
shot 10_bayram_suslemesi 2

ls -la $OUT
