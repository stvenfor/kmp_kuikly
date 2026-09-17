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
CHAT_DETAIL_OK=1
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

# Screencap → OUT/<name>.png, echo the PNG byte size (used to gate flaky captures).
# Must stay log-free: callers capture stdout.
shot_size() {
  local name="$1"
  adb -s "$SERIAL" exec-out screencap -p > "$OUT/${name}.png"
  wc -c < "$OUT/${name}.png" | tr -d " "
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

# Harden 12-flutter-chat-detail: cold-start to Main, open the Chat tab, tap the
# first conversation row. The custom-scheme deeplink was flaky (R1c captured the
# used-car page / a splash instead of a chat).
# Returns 0 only when the shot is both plausibly settled (size gate) AND shows a
# bottom input bar; otherwise the caller falls back to the deeplink.
chat_detail_via_tap() {
  local name="$1"
  local settle="${2:-6}"
  # 120000 rejected a valid chat UI at 119629; 100000 still above splash/usedcar blanks (~60KB).
  local min_bytes="${CHAT_DETAIL_MIN_BYTES:-100000}"
  local i sz w h
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.4
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  # poll past splash (~60KB) before touching the tab bar
  for i in $(seq 1 12); do
    sleep 2
    sz=$(shot_size "_probe-main")
    [[ "$sz" -gt 120000 ]] && break
  done
  read -r w h < <(wm_size)
  # Chat tab (order: Home, Chat, Community, Mine) — same math as 11-flutter-main-chat.
  adb -s "$SERIAL" shell input tap $(( w * 3 / 8 )) $(( h - 120 ))
  sleep 3
  # A cold start can drop the session, and the Chat tab is auth-gated: log in
  # again if the login screen is what came up, then re-open the Chat tab.
  if ui_center "短信登录" >/dev/null 2>&1; then
    log "12-chat-detail: chat tab hit the auth gate — logging in"
    flutter_sms_login
    sleep 2
    adb -s "$SERIAL" shell input tap $(( w * 3 / 8 )) $(( h - 120 ))
    sleep 3
  fi
  # A VIP-purchase interstitial (确认开通 / 立即开通 / SVIP) hijacked a prior
  # logged-in run and got four tab stems locked to the same wrong page; dismiss
  # it if it is on top, then re-open Chat tab to recover (harden intact).
  if dismiss_paywall_if_present; then
    log "12-chat-detail: paywall dismissed — re-open Chat tab"
    adb -s "$SERIAL" shell input tap $(( w * 3 / 8 )) $(( h - 120 ))
    sleep 3
  fi
  # First conversation row: full-bleed list with 16dp side padding; the row band
  # sits ~14% down (status bar 144px + header 168px + list top 21px + half of the
  # 199px row ≈ 433px on 1440x3120), so center-x / 14% H lands inside row 1.
  log "12-chat-detail: tap first conversation row @ $(( w / 2 )),$(( h * 14 / 100 ))"
  adb -s "$SERIAL" shell input tap $(( w / 2 )) $(( h * 14 / 100 ))
  sleep "$settle"
  sz=$(shot_size "$name")
  if [[ "$sz" -le "$min_bytes" ]]; then
    sleep 4
    sz=$(shot_size "$name")
  fi
  log "12-chat-detail: shot size=$sz (gate $min_bytes)"
  if [[ "$sz" -le "$min_bytes" ]]; then
    log "12-chat-detail: size gate FAILED"
    return 1
  fi
  if has_bottom_text_field "$h"; then
    log "12-chat-detail: input bar present"
    return 0
  fi
  log "12-chat-detail: no bottom input bar — likely wrong page"
  return 1
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

# True if the UI dump has an EditText in the bottom third of the screen — i.e. the
# chat-detail input bar. The chat list has no TextField at all, so this tells the
# two screens apart and stops a wrongly-captured list page from passing the gate.
has_bottom_text_field() {
  local height="$1"
  local dump="/tmp/flutter-chat-detail.xml"
  adb -s "$SERIAL" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || return 1
  adb -s "$SERIAL" pull /sdcard/ui.xml "$dump" >/dev/null 2>&1 || return 1
  python3 - "$dump" "$height" <<'PY'
import re, sys
xml = open(sys.argv[1], encoding="utf-8", errors="ignore").read()
limit = int(sys.argv[2]) * 2 // 3
for node in re.finditer(r"<node\b[^>]*>", xml):
    s = node.group(0)
    if "EditText" not in s:
        continue
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', s)
    if b and int(b.group(2)) > limit:
        sys.exit(0)
sys.exit(1)
PY
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

# Dismiss VIP / paywall interstitial that surfaces after SMS auth (S1a2 + S2c).
# Detection needles are evidenced in Flutter source:
#   - "立即开通" : features/home/lib/home/view/home_learning_report_page.dart
#   - "SVIP"     : features/pay/lib/membership/widgets/membership_tier_tabs.dart
#                  + features/classroom/lib/data/classroom_mock_data.dart
#   - "确认开通" : prior capture (line 136 of this script before S2c refactor)
# Dismiss preference: in-page close / cancel / skip tap → BACK keyevent.
# Never purchase. Logs which action won.
# Returns 0 when paywall was detected and acted on; 1 when not present.
dismiss_paywall_if_present() {
  local needles=("确认开通" "立即开通" "SVIP")
  local close_words=("关闭" "取消" "跳过" "稍后" "再想想")
  local matched="" word dismiss_action="none" n
  for n in "${needles[@]}"; do
    if ui_center "$n" >/dev/null 2>&1; then
      matched="$n"
      break
    fi
  done
  if [[ -z "$matched" ]]; then
    log "paywall: not detected"
    return 1
  fi
  log "paywall: detected ($matched) — dismissing"
  for word in "${close_words[@]}"; do
    if tap_text "$word" >/dev/null 2>&1; then
      dismiss_action="tap:$word"
      break
    fi
  done
  if [[ "$dismiss_action" == "none" ]]; then
    adb -s "$SERIAL" shell input keyevent 4 >/dev/null
    dismiss_action="keyevent:BACK"
  fi
  log "paywall: dismissed via $dismiss_action"
  return 0
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
  # SVIP / 确认开通 / 立即开通 often hijacks the post-login landing; dismiss before
  # tab stems lock to the wrong page (S2c).
  dismiss_paywall_if_present || true
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
  # Chat detail: UI-tap path first; the custom-scheme deeplink is fallback only.
  # Never lock an unreliable 12-flutter-chat-detail into baseline (Soft Gate).
  CHAT_DETAIL_OK=0
  if chat_detail_via_tap "12-flutter-chat-detail" 8; then
    log "12-chat-detail: WON via UI tap"
    CHAT_DETAIL_OK=1
  else
    log "12-chat-detail: UI tap unusable → deeplink fallback"
    ensure_main_then_deeplink "12-flutter-chat-detail" "chat/detail?peerName=MockUser" 6
    read -r _w fh < <(wm_size)
    dsz=$(wc -c < "$OUT/12-flutter-chat-detail.png" | tr -d ' ')
    log "12-chat-detail: deeplink shot size=$dsz"
    if [[ "$dsz" -gt "${CHAT_DETAIL_MIN_BYTES:-100000}" ]] && has_bottom_text_field "$fh"; then
      log "12-chat-detail: deeplink input bar present"
      CHAT_DETAIL_OK=1
    else
      log "12-chat-detail: deeplink UNRELIABLE — skip baseline lock for this stem"
      rm -f "$OUT/12-flutter-chat-detail.png"
    fi
  fi
fi

if [[ "$UPDATE" -eq 1 ]]; then
  # Copy actual→baseline, but do not clobber a prior good 12 with a missing/unreliable shot.
  shopt -s nullglob
  for f in "$OUT"/*.png; do
    bn=$(basename "$f")
    if [[ "$bn" == "12-flutter-chat-detail.png" && ! -f "$f" ]]; then
      continue
    fi
    if [[ "$bn" == "12-flutter-chat-detail.png" && "${CHAT_DETAIL_OK:-0}" -ne 1 ]]; then
      log "skip locking unreliable $bn (keep prior baseline if any)"
      continue
    fi
    cp -f "$f" "$BASE/$bn"
  done
  log "baselines locked under goldens/flutter-ref/baseline/"
fi
log "actuals in $OUT (CAPTURE_SET=$CAPTURE_SET)"
