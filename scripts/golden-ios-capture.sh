#!/usr/bin/env bash
# Capture iOS Simulator screenshots for Per-Platform Golden checklist.
# Locked profile: iPhone 16 (override with SIM=...).
set -euo pipefail
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

UPDATE=0
SIM_NAME="${SIM:-iPhone 16}"
BUNDLE="${IOS_BUNDLE:-com.example.kuikly}"
OUT_DIR="$ROOT/goldens/ios/actual"
BASE_DIR="$ROOT/goldens/ios/baseline"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --update) UPDATE=1; shift ;;
    --help|-h) sed -n '2,8p' "$0"; exit 0 ;;
    *) die "unknown arg: $1" ;;
  esac
done

udid_for_sim() {
  xcrun simctl list devices available | awk -v name="$SIM_NAME" '
    $0 ~ name && /\([A-F0-9-]{36}\)/ {
      if (match($0, /\([A-F0-9-]{36}\)/)) {
        print substr($0, RSTART+1, RLENGTH-2)
        exit
      }
    }'
}

UDID="$(udid_for_sim)"
[[ -n "$UDID" ]] || die "simulator not found: $SIM_NAME"
STATE="$(xcrun simctl list devices | grep "$UDID" | grep -oE 'Booted|Shutdown' | head -1 || true)"
if [[ "$STATE" != "Booted" ]]; then
  log "boot $SIM_NAME"
  xcrun simctl boot "$UDID" || true
  open -a Simulator --args -CurrentDeviceUDID "$UDID"
  for _ in $(seq 1 60); do
    xcrun simctl list devices | grep "$UDID" | grep -q Booted && break
    sleep 1
  done
fi

mkdir -p "$OUT_DIR"

capture_page() {
  local page="$1"
  local name="$2"
  local wait_s="${3:-3}"
  local page_data="${4:-}"
  log "capture $page → $name.png"
  xcrun simctl terminate "$UDID" "$BUNDLE" >/dev/null 2>&1 || true
  sleep 3
  # AppDelegate parses KUIKLY_PAGE_DATA JSON into pageData params (e.g. {"tab":"Me"})
  if ! SIMCTL_CHILD_KUIKLY_PAGE="$page" \
     SIMCTL_CHILD_KUIKLY_PAGE_DATA="$page_data" \
     xcrun simctl launch "$UDID" "$BUNDLE" >/dev/null; then
    sleep 2
    SIMCTL_CHILD_KUIKLY_PAGE="$page" \
      SIMCTL_CHILD_KUIKLY_PAGE_DATA="$page_data" \
      xcrun simctl launch "$UDID" "$BUNDLE" >/dev/null
  fi
  sleep "$wait_s"
  xcrun simctl io "$UDID" screenshot "$OUT_DIR/$name.png"
}

# App must already be installed (./scripts/run-ios.sh once).
capture_page "Splash" "01-splash" 3
capture_page "Main" "02-main-home" 3
capture_page "Login" "03-login" 3
capture_page "UsedCarList" "04-usedcar-list" 3
capture_page "UsedCarDetail" "05-usedcar-detail" 3
# Me tab + Settings (Slice-02). Guest vs logged-in primed via pageData
# (MainPage reads tab=Me / mockLogin=1); each capture terminates first,
# so 06 is always guest (fresh process) and 07 always logged-in.
capture_page "Main" "06-main-me-guest" 3 '{"tab":"Me"}'
capture_page "Main" "07-main-me-logged-in" 4 '{"tab":"Me","mockLogin":"1"}'
capture_page "Settings" "08-settings" 3
# Password-mode Login (Slice-03). LoginPage reads pageData mode=password.
capture_page "Login" "09-login-password" 3 '{"mode":"password"}'
# SearchPage (Slice-04). Deep-linked by pageName; reads no pageData.
# 02-main-home now includes the search chrome (Home SearchBar) — re-lock on device gates.
capture_page "Search" "10-search" 3


# Slice-05/06 Chat + Community
capture_page "Main" "11-main-chat" 3 '{"tab":"Chat"}'
capture_page "ChatDetail" "12-chat-detail" 3 '{"id":"1"}'
capture_page "Main" "13-main-community" 3 '{"tab":"Community"}'
capture_page "PostDetail" "14-post-detail" 3 '{"id":"1"}'

# Slice-07 Music + Slice-08 Video
capture_page "MusicList" "15-music-list" 3
capture_page "Main" "16-main-home-music" 3 '{"mockPlay":"1"}'
capture_page "VideoList" "17-video-list" 3
capture_page "VideoDetail" "18-video-detail" 3 '{"id":"1"}'

# Slice-09 Friend + Slice-10 Classroom
capture_page "FriendList" "19-friend-list" 3
capture_page "FriendDetail" "20-friend-detail" 3 '{"id":"1"}'
capture_page "ClassroomList" "21-classroom-list" 3
capture_page "ClassroomDetail" "22-classroom-detail" 3 '{"id":"1"}'

# Slice-11 Live + Slice-12 Pay
capture_page "LiveList" "23-live-list" 3
capture_page "LiveDetail" "24-live-detail" 3 '{"id":"1"}'
capture_page "PayList" "25-pay-list" 3
capture_page "PayConfirm" "26-pay-confirm" 3 '{"id":"1"}'

if [[ "$UPDATE" -eq 1 ]]; then
  mkdir -p "$BASE_DIR"
  cp -f "$OUT_DIR"/*.png "$BASE_DIR/"
  log "baselines updated under goldens/ios/baseline/"
fi
log "captures in $OUT_DIR"
