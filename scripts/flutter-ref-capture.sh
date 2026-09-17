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
# Test OTP account (USAGE_GUIDE): works with Mock or Go dev bypass.
FLUTTER_TEST_PHONE="${FLUTTER_TEST_PHONE:-13400000000}"
FLUTTER_TEST_OTP="${FLUTTER_TEST_OTP:-123456}"

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
    loggedin|wave3) [[ "$1" == loggedin ]] && return 0; return 1 ;;
    *) return 0 ;;
  esac
}

wm_size() {
  local size_line wh
  size_line=$(adb -s "$SERIAL" shell wm size | tr -d "\r")
  wh=${size_line##* }
  echo "${wh%x*} ${wh#*x}"
}

# Dump UI hierarchy and return center of first node whose text/content-desc matches.
ui_center() {
  local needle="$1"
  local dump="/tmp/flutter-ui-dump.xml"
  adb -s "$SERIAL" shell uiautomator dump /sdcard/ui.xml >/dev/null
  adb -s "$SERIAL" pull /sdcard/ui.xml "$dump" >/dev/null
  python3 - "$dump" "$needle" <<'PY'
import re, sys
xml = open(sys.argv[1], encoding="utf-8", errors="ignore").read()
needle = sys.argv[2]
# Prefer exact text=, then content-desc=
pat = re.compile(
    r'text="%s"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
    % re.escape(needle)
)
m = pat.search(xml)
if not m:
    pat = re.compile(
        r'content-desc="%s"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
        % re.escape(needle)
    )
    m = pat.search(xml)
if not m:
    # text may precede other attrs in either order
    for node in re.finditer(r'<node[^>]+>', xml):
        s = node.group(0)
        if f'text="{needle}"' in s or f'content-desc="{needle}"' in s:
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', s)
            if b:
                m = b
                break
if not m:
    sys.exit(2)
x1, y1, x2, y2 = map(int, m.groups())
print((x1 + x2) // 2, (y1 + y2) // 2)
PY
}

tap_text() {
  local label="$1"
  local xy
  if ! xy=$(ui_center "$label"); then
    log "WARN: ui node not found for '$label'"
    return 1
  fi
  log "tap '$label' @ $xy"
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $xy
}

# Type digits via keyevents — `adb input text` drops a leading '1' on this emulator/IME.
type_digits() {
  local s="$1" i c
  for ((i = 0; i < ${#s}; i++)); do
    c=${s:i:1}
    adb -s "$SERIAL" shell input keyevent "KEYCODE_$c" >/dev/null
  done
}

# SMS login on current Login screen (must already be on Login).
# Flutter semantics expose labels as content-desc; fields are EditText with empty text.
flutter_sms_login() {
  log "SMS login phone=$FLUTTER_TEST_PHONE"
  tap_text "短信登录" || true
  sleep 1.0
  local dump="/tmp/flutter-login-edits.xml"
  adb -s "$SERIAL" shell uiautomator dump /sdcard/ui.xml >/dev/null
  adb -s "$SERIAL" pull /sdcard/ui.xml "$dump" >/dev/null
  # Tap EditTexts by index (0=phone, 1=otp) then keyevent digits.
  python3 - "$dump" <<'PY' || true
import re, subprocess, sys
xml = open(sys.argv[1], encoding="utf-8", errors="ignore").read()
edits = []
for m in re.finditer(r"<node\b[^>]*>", xml):
    s = m.group(0)
    if "EditText" in s:
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', s)
        if b:
            x1, y1, x2, y2 = map(int, b.groups())
            edits.append(((x1 + x2) // 2, (y1 + y2) // 2))
print("edits", edits)
open("/tmp/flutter-login-edit-centers.txt", "w").write(
    "\n".join(f"{x} {y}" for x, y in edits)
)
PY
  local phone_xy otp_xy
  phone_xy=$(sed -n '1p' /tmp/flutter-login-edit-centers.txt 2>/dev/null || true)
  otp_xy=$(sed -n '2p' /tmp/flutter-login-edit-centers.txt 2>/dev/null || true)
  if [[ -n "$phone_xy" ]]; then
    # shellcheck disable=SC2086
    adb -s "$SERIAL" shell input tap $phone_xy
    sleep 0.4
    for _ in $(seq 1 20); do adb -s "$SERIAL" shell input keyevent 67 >/dev/null; done
    type_digits "$FLUTTER_TEST_PHONE"
  fi
  sleep 0.3
  if [[ -n "$otp_xy" ]]; then
    # shellcheck disable=SC2086
    adb -s "$SERIAL" shell input tap $otp_xy
    sleep 0.4
    for _ in $(seq 1 10); do adb -s "$SERIAL" shell input keyevent 67 >/dev/null; done
    type_digits "$FLUTTER_TEST_OTP"
  fi
  sleep 0.4
  # Dismiss IME without backing out of Login.
  adb -s "$SERIAL" shell input tap 720 160 >/dev/null 2>&1 || true
  sleep 0.5
  tap_text "登录" || true
  sleep 5
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

  # Search / UsedCar — custom-scheme after Main settle (guest-safe in Flutter).
  ensure_main_then_deeplink "10-flutter-search" "home/search" 5
  ensure_main_then_deeplink "04-flutter-usedcar-list" "home/used_car" 8
fi

if want loggedin; then
  # Login via Chat auth gate, then capture logged-in Chat / Community / Mine / UsedCar.
  log "capture logged-in set (SMS OTP)"
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.4
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  for _i in 1 2 3 4 5 6 7 8 9 10 11 12; do
    sleep 2
    adb -s "$SERIAL" exec-out screencap -p > "$OUT/_probe-main.png"
    sz=$(wc -c < "$OUT/_probe-main.png" | tr -d " ")
    [[ "$sz" -gt 120000 ]] && break
  done
  read -r W H < <(wm_size)
  CHAT_X=$(( W * 3 / 8 ))
  TAB_Y=$(( H - 120 ))
  adb -s "$SERIAL" shell input tap "$CHAT_X" "$TAB_Y"
  sleep 3
  shot "07-flutter-login"
  flutter_sms_login
  # After login, Chat tab content should be visible.
  sleep 2
  shot "11-flutter-main-chat"
  # Community tab
  COMM_X=$(( W * 5 / 8 ))
  adb -s "$SERIAL" shell input tap "$COMM_X" "$TAB_Y"
  sleep 4
  shot "13-flutter-main-community"
  # Mine logged-in
  ME_X=$(( W * 7 / 8 ))
  adb -s "$SERIAL" shell input tap "$ME_X" "$TAB_Y"
  sleep 3
  shot "08-flutter-main-me-logged-in"
  # Home (logged-in greeting)
  HOME_X=$(( W / 8 ))
  adb -s "$SERIAL" shell input tap "$HOME_X" "$TAB_Y"
  sleep 3
  shot "02b-flutter-main-home-logged-in"
  # UsedCar after session (success/list more likely)
  ensure_main_then_deeplink "04-flutter-usedcar-list" "home/used_car" 8
  ensure_main_then_deeplink "10-flutter-search" "home/search" 5
  # Chat detail deep-link (session required)
  ensure_main_then_deeplink "12-flutter-chat-detail" "chat/detail?peerName=MockUser" 5
fi

if [[ "$UPDATE" -eq 1 ]]; then
  cp -f "$OUT"/*.png "$BASE/"
  log "baselines locked under goldens/flutter-ref/baseline/"
fi
log "actuals in $OUT (CAPTURE_SET=$CAPTURE_SET)"
