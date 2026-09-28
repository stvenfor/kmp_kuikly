#!/usr/bin/env bash
# Capture HarmonyOS screenshots via hdc snapshot (device must be Connected).
# Usage: ./scripts/golden-ohos-capture.sh [--update]
set -euo pipefail
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

UPDATE=0
BUNDLE="${OHOS_BUNDLE:-com.example.kuikly}"
ABILITY="${OHOS_ABILITY:-EntryAbility}"
MODULE="${OHOS_MODULE:-entry}"
OUT_DIR="$ROOT/goldens/ohos/actual"
BASE_DIR="$ROOT/goldens/ohos/baseline"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --update) UPDATE=1; shift ;;
    --help|-h) sed -n '2,6p' "$0"; exit 0 ;;
    *) die "unknown arg: $1" ;;
  esac
done

ensure_hdc || die "hdc not found"
TARGET="$("$HDC_BIN" list targets | awk 'NF && $1 !~ /\[Empty\]/ {print $1; exit}')"
[[ -n "${TARGET:-}" ]] || die "no Connected hdc target"

mkdir -p "$OUT_DIR"

start_page() {
  local page="$1"
  local page_data="${2:-}"
  "$HDC_BIN" -t "$TARGET" shell aa force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 1
  # hdc shell eats unquoted JSON — wrap the whole aa command so pageData keeps quotes.
  # EntryAbility reads --ps pageName / pageData → AppStorage → Index → Kuikly pageData.
  local cmd
  if [[ -n "$page_data" ]]; then
    # Escape double-quotes for the inner JSON payload.
    local escaped="${page_data//\"/\\\"}"
    cmd="aa start -a $ABILITY -b $BUNDLE -m $MODULE --ps pageName $page --ps pageData \"$escaped\""
  else
    cmd="aa start -a $ABILITY -b $BUNDLE -m $MODULE --ps pageName $page"
  fi
  "$HDC_BIN" -t "$TARGET" shell "$cmd" >/dev/null
  # Ensure process is up before snapshot (avoids capturing launcher desktop).
  local i=0
  while [[ $i -lt 10 ]]; do
    if "$HDC_BIN" -t "$TARGET" shell "pidof $BUNDLE" 2>/dev/null | grep -q '[0-9]'; then
      return 0
    fi
    sleep 0.5
    i=$((i + 1))
  done
  die "aa start did not bring up $BUNDLE (page=$page)"
}

capture_page() {
  local page="$1"
  local name="$2"
  local wait_s="${3:-4}"
  local page_data="${4:-}"
  log "capture $page → $name.png"
  start_page "$page" "$page_data"
  sleep "$wait_s"
  local remote="/data/local/tmp/kuikly_${name}.jpeg"
  "$HDC_BIN" -t "$TARGET" shell snapshot_display -f "$remote" >/dev/null
  "$HDC_BIN" -t "$TARGET" file recv "$remote" "$OUT_DIR/${name}.jpeg" >/dev/null
  if command -v sips >/dev/null 2>&1; then
    sips -s format png "$OUT_DIR/${name}.jpeg" --out "$OUT_DIR/${name}.png" >/dev/null
  else
    die "sips required to convert jpeg→png on macOS"
  fi
}

# Settle waits aligned with golden-android-capture.sh Phase-2 (under-settled ≈85KB blanks
# were captured as launcher/splash frames). Never lower these below the Android values.
capture_page "Splash" "01-splash" 4
capture_page "Main" "02-main-home" 4
capture_page "Login" "03-login" 3
capture_page "UsedCarList" "04-usedcar-list" 4
capture_page "UsedCarDetail" "05-usedcar-detail" 3 '{"id":"1"}'
# Me tab + Settings (Slice-02). Guest vs logged-in primed via pageData
# (MainPage reads tab=Me / mockLogin=1); each capture force-stops first,
# so 06 is always guest (fresh process) and 07 always logged-in.
capture_page "Main" "06-main-me-guest" 4 '{"tab":"Me"}'
capture_page "Main" "07-main-me-logged-in" 5 '{"tab":"Me","mockLogin":"1"}'
capture_page "Settings" "08-settings" 3
# Password-mode Login (Slice-03). LoginPage reads pageData mode=password.
# Keep fixed quoting above (Slice-02): pageData JSON wrapped+escaped for hdc shell.
capture_page "Login" "09-login-password" 3 '{"mode":"password"}'
# SearchPage (Slice-04). Deep-linked by pageName; reads no pageData.
# 02-main-home now includes the search chrome (Home SearchBar) — re-lock on device gates.
capture_page "Search" "10-search" 3


# Slice-05/06 Chat + Community (settle raised to Android Phase-2; mockLogin=1 priming
# matches golden-android-capture.sh — guest state would self-lock the wrong baseline)
capture_page "Main" "11-main-chat" 4 '{"tab":"Chat","mockLogin":"1"}'
capture_page "ChatDetail" "12-chat-detail" 3 '{"id":"1"}'
capture_page "Main" "13-main-community" 4 '{"tab":"Community","mockLogin":"1"}'
capture_page "PostDetail" "14-post-detail" 3 '{"id":"1"}'

# Slice-07 Music + Slice-08 Video
capture_page "MusicList" "15-music-list" 3
capture_page "Main" "16-main-home-music" 4 '{"mockPlay":"1"}'
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
  log "baselines updated under goldens/ohos/baseline/"
fi
log "captures in $OUT_DIR"
