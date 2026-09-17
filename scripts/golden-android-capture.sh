#!/usr/bin/env bash
# Capture Android screenshots for Per-Platform Golden checklist.
# Locked profile: AVD Pixel_7_Pro (override with AVD=...).
#
# Usage:
#   ./scripts/golden-android-capture.sh           # capture → goldens/android/actual/
#   ./scripts/golden-android-capture.sh --update  # copy actual → goldens/android/baseline/
#   ./scripts/golden-android-diff.sh              # diff actual vs baseline
set -euo pipefail
# JSON pageData contains `{a,b}` — bash braceexpand must stay off or commas split args.
set +o braceexpand 2>/dev/null || set +B
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

# Size-poll gate. A cold start can need ~9s, and a fixed sleep still yields
# ~30KB blank frames; poll until the shot looks settled. Override with:
#   ANDROID_CAPTURE_MIN_BYTES / ANDROID_CAPTURE_POLL_TIMEOUT / ANDROID_CAPTURE_POLL_INTERVAL
ANDROID_CAPTURE_MIN_BYTES="${ANDROID_CAPTURE_MIN_BYTES:-100000}"
ANDROID_CAPTURE_POLL_TIMEOUT="${ANDROID_CAPTURE_POLL_TIMEOUT:-24}"
ANDROID_CAPTURE_POLL_INTERVAL="${ANDROID_CAPTURE_POLL_INTERVAL:-2}"

adb devices | awk 'NR>1 && $2=="device"{ok=1} END{exit !ok}' \
  || die "no Android device/emulator online (start Pixel_7_Pro then retry)"

mkdir -p "$OUT_DIR"

shot_size() { wc -c < "$1" | tr -d ' '; }

capture_page() {
  local page="$1"
  local name="$2"
  local wait_s="${3:-2}"
  local page_data="${4:-}"
  log "capture $page → $name.png"
  local shot="$OUT_DIR/$name.png"
  local started=$SECONDS
  adb shell am force-stop "$PACKAGE" >/dev/null 2>&1 || true
  if [[ -n "$page_data" ]]; then
    # Entire am cmdline must be ONE remote shell string — otherwise commas in JSON
    # split args and fragments like mockLogin:1 become VIEW data URIs / crash JSONObject.
    adb shell "am start -n ${PACKAGE}/.KuiklyRenderActivity --es pageName ${page} --es pageData '${page_data}'" >/dev/null
  else
    adb shell "am start -n ${PACKAGE}/.KuiklyRenderActivity --es pageName ${page}" >/dev/null
  fi
  sleep "$wait_s"
  # Poll screencap until the frame is plausibly settled; the last shot stays on disk.
  local sz=0
  while :; do
    adb exec-out screencap -p >"$shot"
    sz="$(shot_size "$shot")"
    if [[ "$sz" -gt "$ANDROID_CAPTURE_MIN_BYTES" ]]; then
      break
    fi
    if (( SECONDS - started >= ANDROID_CAPTURE_POLL_TIMEOUT )); then
      warn "$name still ${sz}B (<= ${ANDROID_CAPTURE_MIN_BYTES}B) after ${ANDROID_CAPTURE_POLL_TIMEOUT}s — keeping last shot"
      break
    fi
    sleep "$ANDROID_CAPTURE_POLL_INTERVAL"
  done
  log "  $name ${sz}B (gate ${ANDROID_CAPTURE_MIN_BYTES}B, ${SECONDS}s elapsed)"
}

# Closed checklist (product path pages that can be deep-linked by pageName).
# Settle defaults raised for Phase-2 Cross-Source (under-settled ≈85KB blanks).
capture_page "Splash" "01-splash" 3
capture_page "Main" "02-main-home" 8
# Logged-in Home (greeting row). Parity twin of 02b-flutter-main-home-logged-in;
# each capture force-stops first, so 02 is always guest and 02b always logged-in.
capture_page "Main" "02b-main-home-logged-in" 8 '{"mockLogin":"1"}'
capture_page "Login" "03-login" 3
capture_page "UsedCarList" "04-usedcar-list" 4
capture_page "UsedCarDetail" "05-usedcar-detail" 3 '{"id":"1"}'
# Me tab + Settings (Slice-02). Guest vs logged-in primed via pageData
# (MainPage reads tab=Me / mockLogin=1); each capture force-stops first,
# so 06 is always guest (fresh process) and 07 always logged-in.
capture_page "Main" "06-main-me-guest" 6 '{"tab":"Me"}'
capture_page "Main" "07-main-me-logged-in" 5 '{"tab":"Me","mockLogin":"1"}'
capture_page "Settings" "08-settings" 3
# Password-mode Login (Slice-03). LoginPage reads pageData mode=password.
capture_page "Login" "09-login-password" 3 '{"mode":"password"}'
# SearchPage (Slice-04). Deep-linked by pageName; reads no pageData.
# 02-main-home now includes the search chrome (Home SearchBar) — re-lock on device gates.
capture_page "Search" "10-search" 8


# Slice-05/06 Chat + Community
capture_page "Main" "11-main-chat" 8 '{"tab":"Chat","mockLogin":"1"}'
capture_page "ChatDetail" "12-chat-detail" 5 '{"id":"1"}'
capture_page "Main" "13-main-community" 4 '{"tab":"Community","mockLogin":"1"}'
capture_page "PostDetail" "14-post-detail" 3 '{"id":"1"}'

# Slice-07 Music + Slice-08 Video
capture_page "MusicList" "15-music-list" 4
capture_page "Main" "16-main-home-music" 4 '{"mockPlay":"1"}'
capture_page "VideoList" "17-video-list" 4
capture_page "VideoDetail" "18-video-detail" 4 '{"id":"1"}'

# Slice-09 Friend + Slice-10 Classroom
capture_page "FriendList" "19-friend-list" 4
capture_page "FriendDetail" "20-friend-detail" 4 '{"id":"1"}'
capture_page "ClassroomList" "21-classroom-list" 4
capture_page "ClassroomDetail" "22-classroom-detail" 4 '{"id":"1"}'

# Slice-11 Live + Slice-12 Pay
capture_page "LiveList" "23-live-list" 4
capture_page "LiveDetail" "24-live-detail" 4 '{"id":"1"}'
capture_page "PayList" "25-pay-list" 4
capture_page "PayConfirm" "26-pay-confirm" 4 '{"id":"1"}'

if [[ "$UPDATE" -eq 1 ]]; then
  mkdir -p "$BASE_DIR"
  cp -f "$OUT_DIR"/*.png "$BASE_DIR/"
  log "baselines updated under goldens/android/baseline/"
fi

log "captures in $OUT_DIR"
