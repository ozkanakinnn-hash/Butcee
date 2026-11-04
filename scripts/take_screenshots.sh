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

shot () {
  sleep "${2:-3}"
  # "... isn't responding" uyarısı varsa kapat (yavaş emülatörde ilk açılışta çıkabiliyor)
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml /tmp/ui.xml >/dev/null 2>&1
  if grep -q "isn&apos;t responding\|isn't responding" /tmp/ui.xml 2>/dev/null; then echo "ANR uyarısı: $1" >> $OUT/crash_log.txt; tap_text "Wait"; sleep 2; fi
  adb exec-out screencap -p > "$OUT/$1.png"
}

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
  # Emülatördeki "Pixel Launcher isn't responding" uyarısını kapat
  tap_text "Wait" || true
  # Çökme olursa yığın izini kaydet
  adb logcat -d -b crash >> $OUT/crash_log.txt
  adb logcat -d | grep -B2 -A60 "FATAL EXCEPTION" >> $OUT/crash_log.txt || true
}

# 1) Koyu tema, normal gün
write_prefs "" "DARK"
launch
shot 01_ana_sayfa 2
tap_text "Zikirmatik"; shot 01b_zikirmatik 2
for i in 1 2 3 4 5 6 7; do adb shell input tap 540 1400; sleep 0.3; done; shot 01c_zikirmatik_7 1
adb shell input keyevent KEYCODE_BACK; sleep 2
adb shell input swipe 540 1700 540 700 500; shot 02_ana_sayfa_asagi
adb shell input swipe 540 1700 540 700 500; shot 03_ana_sayfa_radyo_menu
tap_text "Kur'an-ı Kerim"; shot 04_kuran_sekmesi
tap_text "Bakara"; shot 04b_okuyucu_bakara 3
adb shell input swipe 200 1300 950 1300 300; shot 04c_okuyucu_sonraki_sayfa 2
tap_text "Meal"; shot 04d_okuyucu_meal 2
tap_text "Okunuş"; shot 04e_okuyucu_okunus 2
adb shell input keyevent KEYCODE_BACK; sleep 2
adb shell input tap 810 396; shot 04f_hatim 2
adb shell input swipe 540 1800 540 700 500; shot 04g_hatim_asagi 2
adb shell input swipe 540 600 540 1800 400; adb shell input tap 270 396; sleep 1
tap_text "Takvim"; shot 05_takvim
tap_text "Ayet & Dua"; shot 06_ayet_dua
tap_text "Dahası"; shot 06a_dahasi 2
adb shell input swipe 540 1800 540 900 500; shot 06a2_dahasi_asagi 2
tap_text "Esmâü'l-Hüsnâ"; shot 06b_esma 2
adb shell input keyevent KEYCODE_BACK; sleep 2
for item in "Kaza Namazı:06b2_kaza" "Kandil Duaları:06b3_kandil" "Cuma Hutbesi:06b4_hutbe" "Bildirim Sihirbazı:06b5_sihirbaz"; do
  tap_text "${item%%:*}"; shot "${item##*:}" 4
  adb shell input keyevent KEYCODE_BACK; sleep 2
done
# Ramazan Rehberi (Dahası) + özel gün kartları + widget'lar
tap_text "Ramazan Rehberi"; shot 06c_ramazan_rehberi 3
adb shell input swipe 540 1800 540 600 500; shot 06d_rehber_faydalar 2
adb shell input swipe 540 1800 540 600 500; shot 06e_rehber_iftar_sahur 2
adb shell input keyevent KEYCODE_BACK; sleep 2
tap_text "Wait" || true
tap_text "Özel Gün Kartları"; shot 06f_ozel_gun_kartlari 5
tap_text "Wait" || true; shot 06f_ozel_gun_kartlari 2
tap_text "Kurban Bayramı"; shot 06g_kart_kurban 4
adb shell input keyevent KEYCODE_BACK; sleep 2
# Seçenek satırları (1080x2400): y = 1074 / 1201 / 1327
for y in 1074 1201 1327; do
  tap_text "Ana Ekran Widget'ı"; sleep 2; adb shell input tap 540 $y; sleep 4
  adb exec-out screencap -p > $OUT/06h_widget_pin_$y.png
  tap_text "Add to home screen" || tap_text "ADD TO HOME SCREEN" || tap_text "Add"
  sleep 3; adb shell am start -n $PKG/com.example.MainActivity; sleep 3
done
adb shell input keyevent KEYCODE_HOME; shot 06i_widgetlar 5
adb shell input swipe 900 1200 150 1200 300; shot 06j_widgetlar_2 3
adb shell input swipe 900 1200 150 1200 300; shot 06k_widgetlar_3 3
adb shell am start -n $PKG/com.example.MainActivity; sleep 3
tap_text "Ana Sayfa"; sleep 2
tap_text "Kıble Bulucu"; shot 07_kible 4
adb shell input keyevent KEYCODE_BACK; sleep 2
tap_text "Ayarlar"; shot 08_ayarlar
adb shell input swipe 540 1800 540 500 500; adb shell input swipe 540 1800 540 500 500; shot 09_ayarlar_tema

# 1b) Namaz vakitlerinin altındaki Günün Manevi İklimi kartı
adb shell am force-stop $PKG; launch
adb shell input swipe 540 1700 540 900 400; shot 11_manevi_iklim 2

# 2) İngilizce + aydınlık tema
write_prefs "" "LIGHT" "en"
launch
shot 10_english_home 2
adb shell input swipe 540 1700 540 700 500; shot 10b_english_home_asagi
adb shell input swipe 540 1800 540 300 600; adb shell input swipe 540 1800 540 300 600; shot 10c_english_menu
adb shell input swipe 540 1800 540 300 600; shot 10d_english_names
adb shell input swipe 540 1800 540 300 600; shot 10e_english_inspiration
tap_text "Holy Quran"; shot 10f_english_quran 3
tap_text "Fâtiha"; shot 10g_english_fatiha 3
adb shell input swipe 540 1800 540 400 600; shot 10h_english_fatiha_meaning

adb logcat -d -b crash >> $OUT/crash_log.txt
ls -la $OUT; cat $OUT/crash_log.txt | head -80

# Uygulama ikonu: ana ekrana çık, uygulama çekmecesini aç
adb shell input keyevent KEYCODE_HOME; sleep 2
adb shell input swipe 540 1900 540 500 400; sleep 3
adb exec-out screencap -p > $OUT/14_uygulama_ikonu.png
