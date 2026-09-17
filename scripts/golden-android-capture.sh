#!/usr/bin/env bash
# Capture Android screenshots for Per-Platform Golden checklist.
# Locked profile: AVD Pixel_7_Pro (override with AVD=...).
#
# Usage:
#   ./scripts/golden-android-capture.sh           # capture → goldens/android/actual/
#   ./scripts/golden-android-capture.sh --update  # copy actual → goldens/android/baseline/
#   ./scripts/golden-android-diff.sh              # diff actual vs baseline
set -euo pipefail
# shellcheck source=common.sh
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_android_sdk
command -v adb >/dev/null || die "adb required"

UPDATE=0
PACKAGE="${ANDROID_PACKAGE:-com.example.kuikly}"
OUT_DIR="$ROOT/goldens/android/actual"
BASE_DIR="$ROOT/goldens/android/baseline"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --update) UPDATE=1; shift ;;
    --help|-h)
      sed -n '2,12p' "$0"
      exit 0
      ;;
    *) die "unknown arg: $1" ;;
  esac
done

adb devices | awk 'NR>1 && $2=="device"{ok=1} END{exit !ok}' \
  || die "no Android device/emulator online (start Pixel_7_Pro then retry)"

mkdir -p "$OUT_DIR"

capture_page() {
  local page="$1"
  local name="$2"
  local wait_s="${3:-2}"
  local page_data="${4:-}"
  log "capture $page → $name.png"
  adb shell am force-stop "$PACKAGE" >/dev/null 2>&1 || true
  if [[ -n "$page_data" ]]; then
    adb shell am start -n "$PACKAGE/.KuiklyRenderActivity" \
      --es pageName "$page" --es pageData "$page_data" >/dev/null
  else
    adb shell am start -n "$PACKAGE/.KuiklyRenderActivity" --es pageName "$page" >/dev/null
  fi
  sleep "$wait_s"
  adb exec-out screencap -p >"$OUT_DIR/$name.png"
}

# Closed checklist (product path pages that can be deep-linked by pageName).
capture_page "Splash" "01-splash" 2
capture_page "Main" "02-main-home" 2
capture_page "Login" "03-login" 2
capture_page "UsedCarList" "04-usedcar-list" 2
capture_page "UsedCarDetail" "05-usedcar-detail" 2 '{"id":"1"}'
# Me tab + Settings (Slice-02). Guest vs logged-in primed via pageData
# (MainPage reads tab=Me / mockLogin=1); each capture force-stops first,
# so 06 is always guest (fresh process) and 07 always logged-in.
capture_page "Main" "06-main-me-guest" 2 '{"tab":"Me"}'
capture_page "Main" "07-main-me-logged-in" 3 '{"tab":"Me","mockLogin":"1"}'
capture_page "Settings" "08-settings" 2
# Password-mode Login (Slice-03). LoginPage reads pageData mode=password.
capture_page "Login" "09-login-password" 2 '{"mode":"password"}'
# SearchPage (Slice-04). Deep-linked by pageName; reads no pageData.
# 02-main-home now includes the search chrome (Home SearchBar) — re-lock on device gates.
capture_page "Search" "10-search" 2


# Slice-05/06 Chat + Community
capture_page "Main" "11-main-chat" 2 '{"tab":"Chat"}'
capture_page "ChatDetail" "12-chat-detail" 2 '{"id":"1"}'
capture_page "Main" "13-main-community" 2 '{"tab":"Community"}'
capture_page "PostDetail" "14-post-detail" 2 '{"id":"1"}'

# Slice-07 Music + Slice-08 Video
capture_page "MusicList" "15-music-list" 2
capture_page "Main" "16-main-home-music" 2 '{"mockPlay":"1"}'
capture_page "VideoList" "17-video-list" 2
capture_page "VideoDetail" "18-video-detail" 2 '{"id":"1"}'

# Slice-09 Friend + Slice-10 Classroom
capture_page "FriendList" "19-friend-list" 2
capture_page "FriendDetail" "20-friend-detail" 2 '{"id":"1"}'
capture_page "ClassroomList" "21-classroom-list" 2
capture_page "ClassroomDetail" "22-classroom-detail" 2 '{"id":"1"}'

# Slice-11 Live + Slice-12 Pay
capture_page "LiveList" "23-live-list" 2
capture_page "LiveDetail" "24-live-detail" 2 '{"id":"1"}'
capture_page "PayList" "25-pay-list" 2
capture_page "PayConfirm" "26-pay-confirm" 2 '{"id":"1"}'

if [[ "$UPDATE" -eq 1 ]]; then
  mkdir -p "$BASE_DIR"
  cp -f "$OUT_DIR"/*.png "$BASE_DIR/"
  log "baselines updated under goldens/android/baseline/"
fi

log "captures in $OUT_DIR"
