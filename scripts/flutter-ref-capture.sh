#!/usr/bin/env bash
# Capture Flutter Migration Source screenshots as Cross-Source Reference Goldens.
# Device: same Android emulator as Kuikly Android goldens (default emulator-5554).
#
# Usage:
#   ./scripts/flutter-ref-capture.sh           # write goldens/flutter-ref/actual/
#   ./scripts/flutter-ref-capture.sh --update  # also lock → baseline/
#   CAPTURE_SET=wave1|wave2|all ./scripts/flutter-ref-capture.sh
#
# Requires: flutter on PATH, adb, running emulator. First install may take minutes.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FLUTTER_ROOT="${FLUTTER_ROOT:-/Users/mac/Desktop/github/my_ai_project}"
BUNDLE="${FLUTTER_ANDROID_ID:-com.sample.module_sample}"
ACTIVITY="${FLUTTER_ANDROID_ACTIVITY:-.MainActivity}"
OUT="$ROOT/goldens/flutter-ref/actual"
BASE="$ROOT/goldens/flutter-ref/baseline"
SERIAL="${ANDROID_SERIAL:-emulator-5554}"
HOST="${FLUTTER_LINK_HOST:-xiaomaomain.com}"
SCHEME="${FLUTTER_LINK_SCHEME:-xiaomao}"
CAPTURE_SET="${CAPTURE_SET:-all}"
UPDATE=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --update) UPDATE=1; shift ;;
    *) echo "unknown: $1"; exit 1 ;;
  esac
done

mkdir -p "$OUT" "$BASE"
command -v flutter >/dev/null || { echo "flutter not found"; exit 1; }
adb -s "$SERIAL" get-state >/dev/null

log() { echo "==> $*"; }

# Ensure app installed (debug). Skip rebuild if already present unless FORCE_FLUTTER_BUILD=1.
if [[ "${FORCE_FLUTTER_BUILD:-0}" == "1" ]] || ! adb -s "$SERIAL" shell pm path "$BUNDLE" >/dev/null 2>&1; then
  log "flutter install on $SERIAL"
  (cd "$FLUTTER_ROOT" && flutter install -d "$SERIAL")
fi

shot() {
  local name="$1"
  adb -s "$SERIAL" exec-out screencap -p > "$OUT/${name}.png"
  log "wrote $OUT/${name}.png"
}

# Cold start / settle then screencap.
launch_and_shot() {
  local name="$1"
  local settle="${2:-4}"
  log "capture $name (settle ${settle}s)"
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.5
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  sleep "$settle"
  shot "$name"
}

# VIEW deeplink — MUST pin -n package/activity or Chrome steals https intents.
deeplink_and_shot() {
  local name="$1"
  local url="$2"
  local settle="${3:-5}"
  log "deeplink $name ← $url (settle ${settle}s)"
  adb -s "$SERIAL" shell am start \
    -n "$BUNDLE/$ACTIVITY" \
    -a android.intent.action.VIEW \
    -c android.intent.category.BROWSABLE \
    -d "$url" >/dev/null
  sleep "$settle"
  shot "$name"
}

# Cold-start app, settle to Main, then custom-scheme deeplink into feature.
ensure_main_then_deeplink() {
  local name="$1"
  local path="$2" # e.g. community or chat/detail?peerName=MockUser
  local settle="${3:-5}"
  local url="${SCHEME}://app/${path}"
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.4
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  sleep 5
  deeplink_and_shot "$name" "$url" "$settle"
}

want() {
  case "$CAPTURE_SET" in
    all) return 0 ;;
    wave1) [[ "$1" == wave1 ]] && return 0; return 1 ;;
    wave2) [[ "$1" == wave2 ]] && return 0; return 1 ;;
    *) return 0 ;;
  esac
}

if want wave1; then
  # Smoke set for Wave 0 / Wave 1
  launch_and_shot "01-flutter-cold-start" 5
  launch_and_shot "02-flutter-main-home" 6
  # Mine guest: tap bottom-right tab region (profile may vary; re-check visually)
  log "capture 03-flutter-main-me-guest (tap Me tab)"
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.4
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  sleep 6
  # denser taps in bottom-right (Pixel 7 Pro ~1440x3120 class; scale via wm size)
  for xy in 1180 2980 1260 3000 1340 3020 1100 2960; do
    :
  done
  # sequential pairs
  adb -s "$SERIAL" shell input tap 1180 2980 || true
  sleep 1
  adb -s "$SERIAL" shell input tap 1260 3000 || true
  sleep 1.5
  shot "03-flutter-main-me-guest"
fi

if want wave2; then
  # Guest Chat/Community tabs redirect to Login — best path for 07-flutter-login.
  # Main tab order (Flutter): Home, Chat, Community, Mine.
  # Community/Chat *content* refs require a logged-in session (manual / future).
  log "capture 07-flutter-login (Chat tab auth gate)"
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.4
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  # poll past splash (~60KB → home ~200KB+)
  for _i in 1 2 3 4 5 6 7 8 9 10 11 12; do
    sleep 2
    adb -s "$SERIAL" exec-out screencap -p > "$OUT/_probe-main.png"
    sz=$(wc -c < "$OUT/_probe-main.png" | tr -d " ")
    [[ "$sz" -gt 120000 ]] && break
  done
  SIZE_LINE=$(adb -s "$SERIAL" shell wm size | tr -d "\r")
  WH=${SIZE_LINE##* }
  W=${WH%x*}; H=${WH#*x}
  CHAT_X=$(( W * 3 / 8 ))
  TAB_Y=$(( H - 120 ))
  adb -s "$SERIAL" shell input tap "$CHAT_X" "$TAB_Y"
  sleep 3
  shot "07-flutter-login"
  # Optional deeplinks (require logged-in + linking listener after Main):
  # ensure_main_then_deeplink "04-flutter-community" "community" 5
fi

if [[ "$UPDATE" -eq 1 ]]; then
  cp -f "$OUT"/*.png "$BASE/"
  log "baselines locked under goldens/flutter-ref/baseline/"
fi
log "actuals in $OUT (CAPTURE_SET=$CAPTURE_SET)"
