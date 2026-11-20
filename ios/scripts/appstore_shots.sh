#!/bin/bash
# App Store için 6,9" (1320×2868) ekran görüntüleri: Türkçe ve İngilizce.
# Kullanım: appstore_shots.sh <App.app yolu> <çıktı klasörü>
set -x
APP="$1"; OUT="$2"; BID=akin.ai2
DEV=$(xcrun simctl list devices available | grep -E "iPhone 1[6-9] Pro Max \(" | tail -1 | grep -oE "[0-9A-F-]{36}")
[ -n "$DEV" ] || { echo "Pro Max simülatörü yok"; xcrun simctl list devices available; exit 1; }
xcrun simctl boot "$DEV"; xcrun simctl bootstatus "$DEV" -b
xcrun simctl status_bar "$DEV" override --time "9:41" --batteryState charged --batteryLevel 100 --cellularBars 4 --wifiBars 3 || true
xcrun simctl install "$DEV" "$APP"
xcrun simctl privacy "$DEV" grant location "$BID" || true
TODAY=$(date +%Y-%m-%d)
# Isınma: ilk açılıştaki sistem bildirimleri (ör. Apple Intelligence) görüntülere düşmesin
xcrun simctl launch "$DEV" $BID -debug_no_ads YES -qiblaCalibrationIntro NO -key_onboarding_completed YES; sleep 40
xcrun simctl terminate "$DEV" $BID
shot () { # dil, ad, argümanlar...
  local lang=$1 name=$2; shift 2
  mkdir -p "$OUT/$lang"
  xcrun simctl terminate "$DEV" $BID 2>/dev/null
  xcrun simctl launch "$DEV" $BID -debug_no_ads YES -qiblaCalibrationIntro NO -key_auto_location NO -key_language "$lang" \
    -key_onboarding_completed YES -key_user_name "Özkan" -key_province Manisa -key_district Saruhanlı "$@"
  sleep 14
  xcrun simctl io "$DEV" screenshot "$OUT/$lang/$name.png"
}
for L in tr en; do
  shot $L 01_ana_sayfa -key_theme_mode DARK
  shot $L 02_oruc -key_theme_mode DARK -key_fasting_dates "(\"$TODAY\")"
  shot $L 03_kible -key_theme_mode DARK -debug_route qibla -debug_heading 145
  shot $L 04_kuran -key_theme_mode DARK -debug_route reader
  shot $L 05_takvim -key_theme_mode DARK -debug_start_tab 2
  shot $L 06_zikirmatik -key_theme_mode DARK -debug_route zikirmatik
  shot $L 07_aydinlik -key_theme_mode LIGHT -key_color_theme ZUMRUT
  shot $L 08_dahasi -key_theme_mode DARK -debug_start_tab 4
done
# Kıble: dönüş yönü gösteren hâl
shot tr 03b_kible_don -key_theme_mode LIGHT -debug_route qibla -debug_heading 105
# Kıble açılışındaki kalibrasyon anlatımı (önizleme için)
xcrun simctl terminate "$DEV" $BID; xcrun simctl launch "$DEV" $BID -debug_no_ads YES -qiblaCalibrationIntro YES -key_auto_location NO \
  -key_language tr -key_onboarding_completed YES -key_province Manisa -key_district Saruhanlı -key_theme_mode DARK -debug_route qibla -debug_heading 110
sleep 12; xcrun simctl io "$DEV" screenshot "$OUT/kible_kalibrasyon.png"
ls -laR "$OUT"
