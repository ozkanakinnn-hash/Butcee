#!/bin/bash
# iOS simülatöründe uygulamayı farklı ekranlarla açıp ekran görüntüsü alır.
set -x
APP="$1"; OUT="$2"; BID=com.ozkanakin.ramazanrehberi
mkdir -p "$OUT"
DEV=$(xcrun simctl list devices available | grep -E "iPhone 16 \(|iPhone 16 Pro \(|iPhone 15 \(" | head -1 | grep -oE "[0-9A-F-]{36}")
xcrun simctl boot "$DEV"; xcrun simctl bootstatus "$DEV" -b
xcrun simctl status_bar "$DEV" override --time "14:00" --batteryState charged --batteryLevel 100 --cellularBars 4 || true
xcrun simctl install "$DEV" "$APP"
xcrun simctl privacy "$DEV" grant location "$BID" || true
TODAY=$(date +%Y-%m-%d)
shot () { # ad, argümanlar...
  local name=$1; shift
  xcrun simctl terminate "$DEV" $BID 2>/dev/null
  xcrun simctl launch "$DEV" $BID -debug_no_ads YES -key_language "${SHOT_LANG:-tr}" -key_onboarding_completed YES -key_user_name "Özkan" -key_province Manisa -key_district Saruhanlı "$@"
  sleep 14
  xcrun simctl io "$DEV" screenshot "$OUT/$name.png"
  # Çökme kontrolü
  if ! xcrun simctl spawn "$DEV" launchctl list | grep -q "$BID"; then echo "CRASH: $name" >> "$OUT/crash_log.txt"; fi
}
shot 01_ana_sayfa -key_theme_mode DARK
shot 02_ana_sayfa_oruc -key_theme_mode DARK -key_fasting_dates "(\"$TODAY\")"
shot 03_ana_sayfa_aydinlik -key_theme_mode LIGHT -key_color_theme ZUMRUT
shot 04_kuran -key_theme_mode DARK -debug_start_tab 1
shot 05_okuyucu -key_theme_mode DARK -debug_route reader
shot 06_takvim -key_theme_mode DARK -debug_start_tab 2
shot 07_ayet_dua -key_theme_mode DARK -debug_start_tab 3
shot 08_dahasi -key_theme_mode DARK -debug_start_tab 4
shot 09_kible -key_theme_mode DARK -debug_route qibla
shot 10_zikirmatik -key_theme_mode DARK -debug_route zikirmatik
shot 11_kartlar -key_theme_mode DARK -debug_route cards
shot 12_rehber -key_theme_mode DARK -debug_route guide -key_fasting_dates "(\"$TODAY\")"
shot 13_hatim -key_theme_mode DARK -debug_route hatim
shot 14_kaza -key_theme_mode LIGHT -debug_route kaza
shot 15_ayarlar -key_theme_mode DARK -debug_route settings
SHOT_LANG=en shot 16_english -key_theme_mode DARK
# İlk açılış ekranı
xcrun simctl terminate "$DEV" $BID; xcrun simctl launch "$DEV" $BID -debug_no_ads YES -key_language tr -key_theme_mode DARK; sleep 12
xcrun simctl io "$DEV" screenshot "$OUT/00_ilk_acilis.png"
xcrun simctl spawn "$DEV" log show --last 10m --predicate 'process == "RamazanRehberi" AND messageType == fault' --style compact 2>/dev/null | tail -40 >> "$OUT/crash_log.txt" || true
ls -la "$OUT"
