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
    <string name="key_language">${3:-tr}</string>
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

launch () {
  adb logcat -c
  adb shell am start -n $PKG/com.example.MainActivity; sleep 15
  # Çökme olursa yığın izini kaydet
  adb logcat -d -b crash >> $OUT/crash_log.txt
  adb logcat -d | grep -B2 -A60 "FATAL EXCEPTION" >> $OUT/crash_log.txt || true
}

# 1) Koyu tema, normal gün
write_prefs "" "DARK"
launch
shot 01_ana_sayfa 2
adb shell input swipe 540 1700 540 700 500; shot 02_ana_sayfa_asagi
adb shell input swipe 540 1700 540 700 500; shot 03_ana_sayfa_radyo_menu
tap_text "Oruç Rehberi"; shot 04_oruc_rehberi
tap_text "Takvim"; shot 05_takvim
tap_text "Ayet & Dua"; shot 06_ayet_dua
tap_text "Ana Sayfa"; sleep 2
tap_text "Kıble Bulucu"; shot 07_kible 4
adb shell input keyevent KEYCODE_BACK; sleep 2
tap_text "Ayarlar"; shot 08_ayarlar
adb shell input swipe 540 1800 540 500 500; adb shell input swipe 540 1800 540 500 500; shot 09_ayarlar_tema

# 1b) Arka plan senaryosu: radyoyu çal, uygulamayı arka plana al, bekle
# Ayarlar penceresini kapat ve uygulamayı temiz başlat
adb shell am force-stop $PKG; launch
adb shell input swipe 540 1700 540 900 400; sleep 2
adb shell uiautomator dump /sdcard/ui.xml >/dev/null; adb pull /sdcard/ui.xml $OUT/ui_radyo.xml >/dev/null
# Oynat/Durdur düğmesinin koordinatını (animasyon başlamadan önce) bir kez bul
python3 - <<'PY' > /tmp/playxy
import re
s=open('screenshots/ui_radyo.xml',encoding='utf-8').read()
for m in re.finditer(r'<node [^>]*>', s):
    n=m.group(0)
    if 'content-desc="Dinle"' in n:
        b=list(map(int,re.findall(r'\d+',re.search(r'bounds="([^"]*)"',n).group(1))))
        print((b[0]+b[2])//2,(b[1]+b[3])//2); break
PY
echo "play button at: $(cat /tmp/playxy)"
adb shell input tap $(cat /tmp/playxy); sleep 20; shot 11_radyo_caliyor 1
adb shell dumpsys media_session | grep -E "state=PlaybackState" | head -3 >> $OUT/crash_log.txt
# Durdur senaryosu
adb logcat -c
adb shell input tap $(cat /tmp/playxy); sleep 8; shot 11b_radyo_durduruldu 1
echo "=== DURDUR SONRASI ===" >> $OUT/crash_log.txt
adb shell dumpsys media_session | grep -E "state=PlaybackState" | head -3 >> $OUT/crash_log.txt
adb logcat -d -b crash >> $OUT/crash_log.txt
adb logcat -d | grep -B2 -A60 "FATAL EXCEPTION" >> $OUT/crash_log.txt || true
# Hızlı oynat-durdur (bağlanırken durdurma)
adb logcat -c
adb shell input tap $(cat /tmp/playxy); sleep 1; adb shell input tap $(cat /tmp/playxy); sleep 10
shot 11c_hizli_durdur 1
echo "=== HIZLI DURDUR SONRASI ===" >> $OUT/crash_log.txt
adb logcat -d -b crash >> $OUT/crash_log.txt
adb logcat -d | grep -B2 -A60 "FATAL EXCEPTION" >> $OUT/crash_log.txt || true
# Arka plan için tekrar çal
adb shell input tap $(cat /tmp/playxy); sleep 20
adb shell input keyevent KEYCODE_HOME; sleep 30
echo "=== ARKA PLAN BİLDİRİMLERİ ===" >> $OUT/crash_log.txt
adb shell dumpsys notification --noredact | grep -A3 "pkg=com.abacusai" | head -12 >> $OUT/crash_log.txt
adb shell dumpsys activity services $PKG | grep -E "ServiceRecord|isForeground" | head -5 >> $OUT/crash_log.txt
shot 12_arka_plan_bildirim 1
adb shell cmd statusbar expand-notifications; shot 13_bildirim_paneli 3
adb shell cmd statusbar collapse
# Son kullanılanlardan uygulamayı kaldır (onTaskRemoved)
adb shell am kill $PKG; sleep 5
adb shell am start -n $PKG/com.example.MainActivity; sleep 10
adb logcat -d -b crash >> $OUT/crash_log.txt
adb logcat -d | grep -B2 -A60 "FATAL EXCEPTION" >> $OUT/crash_log.txt || true
adb logcat -d | grep -iE "AndroidRuntime|ForegroundService|ramazanrehberi" | tail -40 >> $OUT/crash_log.txt || true

# 2) İngilizce + aydınlık tema
write_prefs "" "LIGHT" "en"
launch
shot 10_english_home 2
adb shell input swipe 540 1700 540 700 500; shot 10b_english_home_asagi

adb logcat -d -b crash >> $OUT/crash_log.txt
ls -la $OUT; cat $OUT/crash_log.txt | head -80
