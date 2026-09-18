#!/usr/bin/env bash
# Capture Flutter Migration Source screenshots as Cross-Source Reference Goldens.
# Device: same Android emulator as Kuikly Android goldens (default emulator-5554).
#
# Usage:
#   ./scripts/flutter-ref-capture.sh           # write goldens/flutter-ref/actual/
#   ./scripts/flutter-ref-capture.sh --update  # also lock → baseline/
#   CAPTURE_SET=wave1|wave2|wave3|wave4|all ./scripts/flutter-ref-capture.sh
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
# P3-E1a2 wave3 size-gate harden: per-stem reliability flags mirror CHAT_DETAIL_OK
# so --update can skip locking any Wave3 stem whose capture is still on splash.
WAVE3_OK_09=0
WAVE3_OK_05=0
WAVE3_OK_08=0
WAVE3_OK_14=0
WAVE3_OK_15=0
WAVE3_OK_19=0
WAVE3_OK_25=0
# P3-E7a: Wave4 (Kuikly self-lock stems without Flutter-ref) mirrors Wave3
# reliability flags + size gate. Same WAVE4_MIN_BYTES default as WAVE3_MIN_BYTES
# so the gate behavior is identical for splash/blank frames.
WAVE4_OK_17=0
WAVE4_OK_18=0
WAVE4_OK_21=0
WAVE4_OK_23=0
WAVE4_OK_26=0
# Test OTP account (USAGE_GUIDE): works with Mock or Go dev bypass.
FLUTTER_TEST_PHONE="${FLUTTER_TEST_PHONE:-13400000000}"
FLUTTER_TEST_OTP="${FLUTTER_TEST_OTP:-123456}"
# P3-E1a2: default 100000 mirrors chat-detail's gate; splash/blank frames landed ~60KB.
WAVE3_MIN_BYTES="${WAVE3_MIN_BYTES:-100000}"
# P3-E9g: FriendPage / LivePage are chrome-only stubs (~55KB). The Wave3/4
# 100KB splash gate rejects them; accept when content_ok AND size ≥ STUB_MIN.
STUB_MIN_BYTES="${STUB_MIN_BYTES:-40000}"
# P3-E7a: Wave4 size gate. Same default as WAVE3 — parity with Wave3 chat-detail
# Soft Gate behavior; raise via env if a Wave4 page reliably lands smaller.
WAVE4_MIN_BYTES="${WAVE4_MIN_BYTES:-100000}"

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
    loggedin) [[ "$1" == loggedin ]] && return 0; return 1 ;;
    wave3) [[ "$1" == wave3 ]] && return 0; return 1 ;;
    wave4) [[ "$1" == wave4 ]] && return 0; return 1 ;;
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

# P3-E8a: content gate for 17-flutter-video-list. The Wave4 size gate alone
# locked a settled Home frame as the baseline (evidence 149) because Home is
# also ≥ WAVE4_MIN_BYTES. Dump the UI hierarchy (same uiautomator pull pattern
# as has_bottom_text_field) and require BOTH:
#   - a video-list signal: any text=/content-desc= containing 视频 (the
#     AppNavBar title is '视频列表' — dubbing_video_list_page.dart line 21)
#   - no strong Home needle: Home always renders the time-based greeting
#     早上好/下午好/晚上好 (home_controller.dart), so any hit means Home.
# Returns 0 when content OK; 1 on content reject or dump failure.
video_list_content_ok() {
  local dump="/tmp/flutter-video-list.xml"
  adb -s "$SERIAL" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || return 1
  adb -s "$SERIAL" pull /sdcard/ui.xml "$dump" >/dev/null 2>&1 || return 1
  if ! grep -q '视频' "$dump"; then
    log "17-video-list content gate: no 视频 signal in UI dump — reject"
    return 1
  fi
  if grep -q -e '早上好' -e '下午好' -e '晚上好' "$dump"; then
    log "17-video-list content gate: Home greeting needle present — reject"
    return 1
  fi
  log "17-video-list content gate: 视频 signal present, no Home needle — OK"
  return 0
}

# P3-E9d: content gate for 19-flutter-friend-list. The Wave3 size gate alone
# cannot tell the friend stub from a settled Home frame (Home is also ≥
# WAVE3_MIN_BYTES). FriendPage.dart renders AppNavBar(title: '好友') over
# Center(Text('Friend 模块')) — those are the only reliable in-app signals.
# Require one of them AND no Home greeting needle (home_controller.dart).
# Returns 0 when content OK; 1 on content reject or dump failure.
friend_list_content_ok() {
  local dump="/tmp/flutter-friend-list.xml"
  adb -s "$SERIAL" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || return 1
  adb -s "$SERIAL" pull /sdcard/ui.xml "$dump" >/dev/null 2>&1 || return 1
  if ! grep -q -e 'Friend 模块' -e '好友' "$dump"; then
    log "19-friend-list content gate: no Friend 模块/好友 signal in UI dump — reject"
    return 1
  fi
  if grep -q -e '早上好' -e '下午好' -e '晚上好' "$dump"; then
    log "19-friend-list content gate: Home greeting needle present — reject"
    return 1
  fi
  log "19-friend-list content gate: friend signal present, no Home needle — OK"
  return 0
}

# P3-E9d: content gate for 23-flutter-live-list. Same rationale as 17/19 — Home
# is also ≥ WAVE4_MIN_BYTES, so a size-OK shot can still be the Home frame.
# LivePage.dart renders AppNavBar(title: '直播') plus a FilledButton
# '进入 Mock 直播房'; Home carries a '直播' tile too, so require BOTH 直播 AND a
# Mock needle to separate the page from Home, and reject Home greetings.
# Returns 0 when content OK; 1 on content reject or dump failure.
live_list_content_ok() {
  local dump="/tmp/flutter-live-list.xml"
  adb -s "$SERIAL" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || return 1
  adb -s "$SERIAL" pull /sdcard/ui.xml "$dump" >/dev/null 2>&1 || return 1
  if ! grep -q '直播' "$dump"; then
    log "23-live-list content gate: no 直播 signal in UI dump — reject"
    return 1
  fi
  if ! grep -q -e '进入 Mock' -e 'Mock 直播' "$dump"; then
    log "23-live-list content gate: no 进入 Mock/Mock 直播 signal in UI dump — reject"
    return 1
  fi
  if grep -q -e '早上好' -e '下午好' -e '晚上好' "$dump"; then
    log "23-live-list content gate: Home greeting needle present — reject"
    return 1
  fi
  log "23-live-list content gate: 直播 + Mock signal present, no Home needle — OK"
  return 0
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

# P3-E6e: Flutter first-launch shows the 隐私政策 dialog with a 同意并继续
# affordance (content-desc on the dialog button). Recent reinstalls land on the
# dialog instead of Home, so size gates and tab math would otherwise capture
# the modal. Tap 同意并继续 via ui_center (text=, then content-desc=); fall back
# to BACK only if the dialog is detected but no button is found.
# Returns 0 when privacy dialog was detected and acted on; 1 when not present.
dismiss_privacy_if_present() {
  local dismiss_action="none"
  if ui_center "同意并继续" >/dev/null 2>&1; then
    log "privacy dialog: detected — tap 同意并继续"
    if tap_text "同意并继续" >/dev/null 2>&1; then
      dismiss_action="tap:同意并继续"
    else
      adb -s "$SERIAL" shell input keyevent 4 >/dev/null
      dismiss_action="keyevent:BACK"
    fi
    sleep 1.5
    log "privacy dialog: dismissed via $dismiss_action"
    return 0
  fi
  log "privacy dialog: not detected"
  return 1
}

# P3-E1a2 wave3 capture harden. Each Wave3 deeplink stem gets a size gate: if
# the shot is < WAVE3_MIN_BYTES (default 100000), it is treated as still on the
# splash/blank frame and retried once with force-stop + cold
# ensure_main_then_deeplink + settle ≥8s. Mirrors the chat-detail Soft Gate
# pattern. The corresponding WAVE3_OK_<stem> flag stays 0 when the stem is
# unreliable after retry, so --update skips locking the bad baseline (and the
# prior baseline, if any, is preserved on disk).
# Args: <ok_var> <name> <deeplink_path> <initial_settle>
wave3_capture() {
  local ok_var="$1"
  local name="$2"
  local path="$3"
  local settle="${4:-6}"
  local sz
  ensure_main_then_deeplink "$name" "$path" "$settle"
  sz=$(wc -c < "$OUT/${name}.png" | tr -d " ")
  if [[ "$sz" -ge "$WAVE3_MIN_BYTES" ]]; then
    log "wave3 $name: size=$sz ≥ gate $WAVE3_MIN_BYTES (OK)"
    eval "$ok_var=1"
    return 0
  fi
  log "wave3 $name: size=$sz < gate $WAVE3_MIN_BYTES — retry once (force-stop + cold + settle 8s)"
  ensure_main_then_deeplink "$name" "$path" 8
  sz=$(wc -c < "$OUT/${name}.png" | tr -d " ")
  if [[ "$sz" -ge "$WAVE3_MIN_BYTES" ]]; then
    log "wave3 $name: retry size=$sz ≥ gate (OK)"
    eval "$ok_var=1"
    return 0
  fi
  log "wave3 $name: retry size=$sz < gate — UNRELIABLE (skip baseline lock)"
  eval "$ok_var=0"
  return 1
}

# P3-E7a: Wave4 size-gated deeplink capture. Identical structure to
# wave3_capture but keyed on WAVE4_MIN_BYTES / WAVE4_OK_<stem>. Wave4 stems
# without a UI fallback (18 video-detail, 26 pay-confirm) treat the second
# failure as terminal — the caller does NOT retry on UI (those routes have no
# in-app surface), and the corresponding WAVE4_OK_<stem> stays 0 so --update
# skips locking a bad baseline.
# Args: <ok_var> <name> <deeplink_path> <initial_settle>
wave4_capture() {
  local ok_var="$1"
  local name="$2"
  local path="$3"
  local settle="${4:-6}"
  local sz
  ensure_main_then_deeplink "$name" "$path" "$settle"
  sz=$(wc -c < "$OUT/${name}.png" | tr -d " ")
  if [[ "$sz" -ge "$WAVE4_MIN_BYTES" ]]; then
    log "wave4 $name: size=$sz ≥ gate $WAVE4_MIN_BYTES (OK)"
    eval "$ok_var=1"
    return 0
  fi
  log "wave4 $name: size=$sz < gate $WAVE4_MIN_BYTES — retry once (force-stop + cold + settle 8s)"
  ensure_main_then_deeplink "$name" "$path" 8
  sz=$(wc -c < "$OUT/${name}.png" | tr -d " ")
  if [[ "$sz" -ge "$WAVE4_MIN_BYTES" ]]; then
    log "wave4 $name: retry size=$sz ≥ gate (OK)"
    eval "$ok_var=1"
    return 0
  fi
  log "wave4 $name: retry size=$sz < gate — UNRELIABLE (skip baseline lock)"
  eval "$ok_var=0"
  return 1
}

# P3-E1a3: UI-tap fallback for Wave3 stems whose deeplink stays on splash/blank
# (15 music/list, 25 pay/list). Mirrors the chat-detail Soft Gate pattern:
# cold + force-stop + splash poll + ui_center (uiautomator dump) driven tap,
# size-gated against WAVE3_MIN_BYTES. Force-stop between attempts.
#
# Reachability / scope (evidenced under my_ai_project, search RoutePath / 全部服务):
#   - 15 music/list : AllServices '音频列表' → /music/list (MusicListPage). ✓
#   - 25 pay/list   : AllServices '会员续费' → /pay/membership (MembershipRenewPage).
#                     The `/pay` deeplink stub is `PayPage (Text('Pay 模块'))` —
#                     no parity match — but MembershipRenew is the closest in-app
#                     surface (Kuikly PayListPage shows membership tiers). Marked
#                     "best-effort" — UNRELIABLE if the gate still fails.
#   - 19 friend/list: NO UI entry exists in Home / AllServices / Mine menus.
#                     FriendPage.dart is a `Center(Text('Friend 模块'))` stub; the
#                     route `/friend` is only reachable via deeplink. No tap path
#                     to try — kept UNRELIABLE without attempt (no fake baseline).
# All taps use ui_center so tap math does not depend on screen geometry.

# Cold start + settle to Main, polling past splash (≈60KB frame).
# P3-E6e: a fresh install lands on the 隐私政策 dialog before Home; size-gate
# alone can pass while the dialog is still on top, so probe + dismiss here so
# every caller starts on real Main. Idempotent — no-op when dialog absent.
ui_cold_main() {
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.4
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  local i sz
  for i in 1 2 3 4 5 6 7 8 9 10 11 12; do
    sleep 2
    adb -s "$SERIAL" exec-out screencap -p > "$OUT/_probe-main.png"
    sz=$(wc -c < "$OUT/_probe-main.png" | tr -d " ")
    [[ "$sz" -gt 120000 ]] && break
  done
  dismiss_privacy_if_present || true
}

# P3-E2e: ensure a logged-in Home before opening AllServices. The '更多'
# tile is only reliably present on a logged-in Home — a guest Home (or one
# that just lost its session via a deeplink force-stop) may not surface it,
# which is what made wave3 ui_open_all_services return "'更多' not found on
# Home". We trigger the auth gate via the Chat tab (same trick as
# chat_detail_via_tap), run SMS login if it surfaces, dismiss the post-login
# paywall, and finally land on the Home tab. Idempotent — if the session is
# already valid, the '短信登录' probe returns non-zero and we skip the login.
prime_logged_in_home() {
  read -r w h < <(wm_size)
  # Trigger the auth gate via Chat tab; logged-in users land on the chat list.
  adb -s "$SERIAL" shell input tap $(( w * 3 / 8 )) $(( h - 120 ))
  sleep 2
  if ui_center "短信登录" >/dev/null 2>&1; then
    log "prime: auth gate hit — SMS login"
    flutter_sms_login
  else
    log "prime: already logged in (no auth gate on Chat tab)"
  fi
  dismiss_paywall_if_present || true
  # Land on Home tab so the AllServices '更多' tile is visible.
  adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
  sleep 3
}

# Open AllServices from Home. The entry tile is normally labelled '更多' (the
# 10th item in the 5×2 Home feature grid routing to /home/all_services), but
# the AllServices page title is '全部服务', and on some build flavors the tile
# itself may carry '更多服务' instead. P3-E2e: try each of the three labels
# (order: '更多' → '更多服务' → '全部服务') so guest / flavor variants don't
# hard-fail before we've proven the nav opens. ui_center finds each on any
# screen size. Returns 0 only when the title '全部服务' is on screen
# (sanity-check the nav landed on the right page).
ui_open_all_services() {
  local label more_xy=""
  for label in "更多" "更多服务" "全部服务"; do
    if more_xy=$(ui_center "$label" 2>/dev/null); then
      log "ui_open_all_services: tap '$label' @ $more_xy"
      break
    fi
    log "ui_open_all_services: '$label' not on Home, trying next"
  done
  if [[ -z "$more_xy" ]]; then
    log "ui_open_all_services: none of 更多/更多服务/全部服务 found on Home"
    return 1
  fi
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $more_xy
  sleep 4
  if ui_center "全部服务" >/dev/null 2>&1; then
    log "ui_open_all_services: '全部服务' title visible — open"
    return 0
  fi
  log "ui_open_all_services: '全部服务' title missing after tap"
  return 1
}

# P3-E3b: failure-diagnostics snippet for 15-flutter-music-list. Dumps the UI
# hierarchy to /tmp (never into the repo) and logs the short text/content-desc
# lines matching 音频|音乐|其他服务 so the script log shows what WAS on screen
# when the label probe failed. Best-effort: never fails the caller, and every
# grep is `|| true` so `set -e` safety is preserved.
music_list_dump_snippet() {
  local dump="/tmp/flutter-music-list.xml"
  adb -s "$SERIAL" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || return 0
  adb -s "$SERIAL" pull /sdcard/ui.xml "$dump" >/dev/null 2>&1 || return 0
  log "music-list failure dump snippet (音频|音乐|其他服务):"
  { grep -o 'text="[^"]*\(音频\|音乐\|其他服务\)[^"]*"' "$dump" \
      | sort -u | head -5; \
    grep -o 'content-desc="[^"]*\(音频\|音乐\|其他服务\)[^"]*"' "$dump" \
      | sort -u | head -5; } 2>/dev/null || true
  return 0
}

# UI-tap fallback for 15-flutter-music-list:
#   cold → Home → '更多' (AllServices) → swipe to expose '其他服务' → tap '音频列表'.
ui_capture_music_list() {
  local name="$1"
  local min_bytes="${WAVE3_MIN_BYTES:-100000}"
  local entry_xy sz w h label _try
  ui_cold_main || { log "$name: ui_cold_main failed"; return 1; }
  # P3-E2e: prime to logged-in Home before opening AllServices. The deeplink
  # attempt before this often force-stopped the app and dropped the session;
  # a guest Home then lacks the '更多' tile and ui_open_all_services fails.
  prime_logged_in_home || { log "$name: login prime failed"; return 1; }
  # P3-E2f: prime can land on the wrong tab (paywall dismissal / login flow
  # may leave Chat or Mine on top). Explicitly re-tap Home so the '更多' tile
  # is on screen, and retry AllServices once if the first open fails.
  read -r w h < <(wm_size)
  adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
  sleep 2
  if ! ui_open_all_services; then
    log "$name: AllServices open failed — retry once from Home tab"
    adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
    sleep 2
    ui_open_all_services || { log "$name: AllServices open failed after retry"; return 1; }
  fi
  # Favorites sit first; the music entry lives in catalog section '其他服务' and
  # is the LAST tile there (after 会员续费/帮助中心/意见反馈/侧滑导航), so one
  # swipe may not expose it — mirror ui_capture_pay_list's multi-pass scroll
  # (P3-E3b): up to 6 swipes, re-poll ui_center each pass. Exact '音频列表' is
  # probed FIRST every pass so the alternate labels (音频/音乐/Music) only win
  # when the exact tile is genuinely off-screen (build-flavor rename, etc.).
  entry_xy=""
  for _try in 1 2 3 4 5 6; do
    for label in "音频列表" "音频" "音乐" "Music"; do
      if entry_xy=$(ui_center "$label" 2>/dev/null); then
        log "$name: found '$label' @ $entry_xy (scroll pass $_try)"
        break 2
      fi
    done
    adb -s "$SERIAL" shell input swipe 720 1800 720 700 600 >/dev/null 2>&1 || true
    sleep 1.5
  done
  if [[ -z "$entry_xy" ]]; then
    log "$name: none of 音频列表/音频/音乐/Music visible after scroll"
    music_list_dump_snippet
    return 1
  fi
  log "$name: tap '$label' @ $entry_xy"
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $entry_xy
  sleep 5
  # Music page does not surface 立即开通 / SVIP, but call the dismisser anyway
  # — it is a no-op when no needles match (dismiss_paywall_if_present returns 1).
  dismiss_paywall_if_present || true
  sz=$(shot_size "$name")
  if [[ "$sz" -lt "$min_bytes" ]]; then
    sleep 4
    sz=$(shot_size "$name")
  fi
  log "$name: UI tap shot size=$sz (gate $min_bytes)"
  [[ "$sz" -ge "$min_bytes" ]] && return 0
  return 1
}

# UI-tap fallback for 25-flutter-pay-list (best-effort):
#   cold → Home → '更多' (AllServices) → swipe → tap '会员续费'.
#   Lands on MembershipRenewPage (NOT the `/pay` deeplink target — PayPage is a
#   stub). The Kuikly PayListPage is membership-tier shaped, so this is the
#   closest in-app surface and worth attempting; UNRELIABLE if the gate fails.
ui_capture_pay_list() {
  local name="$1"
  local min_bytes="${WAVE3_MIN_BYTES:-100000}"
  local entry_xy sz label _try
  ui_cold_main || { log "$name: ui_cold_main failed"; return 1; }
  # P3-E2e: prime to logged-in Home before opening AllServices — same
  # rationale as ui_capture_music_list above.
  prime_logged_in_home || { log "$name: login prime failed"; return 1; }
  ui_open_all_services || { log "$name: AllServices open failed"; return 1; }
  # P3-E2f: '会员续费' sits deep in the AllServices catalog and one swipe may
  # not expose it. Swipe up to 4 times and re-check alternate labels after
  # each pass — build flavors / tier copy rename the tile, but 会员 / 续费 /
  # SVIP / 开通会员 all route to the same membership surface.
  entry_xy=""
  for _try in 1 2 3 4; do
    for label in "会员续费" "会员" "续费" "SVIP" "开通会员"; do
      if entry_xy=$(ui_center "$label" 2>/dev/null); then
        log "$name: found '$label' @ $entry_xy (scroll pass $_try)"
        break 2
      fi
    done
    adb -s "$SERIAL" shell input swipe 720 1800 720 700 600 >/dev/null 2>&1 || true
    sleep 1.5
  done
  if [[ -z "$entry_xy" ]]; then
    log "$name: none of 会员续费/会员/续费/SVIP/开通会员 visible after scroll"
    return 1
  fi
  log "$name: tap '$label' @ $entry_xy (note: lands on MembershipRenewPage, not PayPage stub)"
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $entry_xy
  sleep 5
  # MembershipRenew exposes 立即开通 / SVIP — pre-dismiss so the shot is the page,
  # not the tier sheet.
  dismiss_paywall_if_present || true
  sz=$(shot_size "$name")
  if [[ "$sz" -lt "$min_bytes" ]]; then
    sleep 4
    sz=$(shot_size "$name")
  fi
  log "$name: UI tap shot size=$sz (gate $min_bytes)"
  [[ "$sz" -ge "$min_bytes" ]] && return 0
  return 1
}

# UI-tap fallback for 08-flutter-settings (mine_menu_data.dart line 95):
#   cold → Home → Mine tab → tap '设置' row → SettingsPage (title '设置').
# AllServices has NO settings entry (verified all_services_data.dart), so the
# fallback in the handoff has nothing to probe on that side — Mine is the only
# in-app path. ui_center finds the row regardless of its position in the menu.
# P3-E6e: dismiss the 隐私政策 dialog (if it re-surfaced after ui_cold_main's
# dismiss) before tapping Mine, and allow up to 6 scroll passes because a
# guest Mine menu can grow tall and bury '设置' below the fold on the first
# probe.
ui_capture_settings() {
  local name="$1"
  local min_bytes="${WAVE3_MIN_BYTES:-100000}"
  local entry_xy sz w h _try
  ui_cold_main || { log "$name: ui_cold_main failed"; return 1; }
  # P3-E2e rationale — same as music/pay: prime to a logged-in Home so the
  # session survives the cold-start force-stop, otherwise the Mine tab may
  # bounce off the auth gate.
  prime_logged_in_home || { log "$name: login prime failed"; return 1; }
  # P3-E6e: the privacy dialog can re-surface after ui_cold_main's dismiss
  # (timing race or post-login gate) — re-dismiss right before tapping Mine
  # so the tab tap doesn't land on the dialog button by accident.
  dismiss_privacy_if_present || true
  read -r w h < <(wm_size)
  # Mine tab is the 4th of Home/Chat/Community/Mine (math matches
  # chat_detail_via_tap / loggedin block — w*7/8, h-120).
  adb -s "$SERIAL" shell input tap $(( w * 7 / 8 )) $(( h - 120 ))
  sleep 3
  # '设置' is the last item in MineMenuData.items; on most builds the menu
  # fits without scroll, but a logged-in profile can grow taller — up to 6
  # swipe passes cover any layout variant (P3-E6e bump: guest Mine reliably
  # needs more passes than the logged-in variant). ui_center matches the row
  # text first, then content-desc, so no screen geometry is baked in.
  entry_xy=""
  for _try in 1 2 3 4 5 6; do
    if entry_xy=$(ui_center "设置" 2>/dev/null); then
      log "$name: found '设置' @ $entry_xy (pass $_try)"
      break
    fi
    adb -s "$SERIAL" shell input swipe 720 1800 720 700 600 >/dev/null 2>&1 || true
    sleep 1.5
  done
  if [[ -z "$entry_xy" ]]; then
    log "$name: '设置' row not visible on Mine after scroll — no AllServices fallback (entry absent)"
    return 1
  fi
  log "$name: tap '设置' @ $entry_xy"
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $entry_xy
  sleep 5
  # Settings page does not surface 立即开通 / SVIP needles; dismisser is a
  # no-op when no needles match (dismiss_paywall_if_present returns 1).
  dismiss_paywall_if_present || true
  sz=$(shot_size "$name")
  if [[ "$sz" -lt "$min_bytes" ]]; then
    sleep 4
    sz=$(shot_size "$name")
  fi
  log "$name: UI tap shot size=$sz (gate $min_bytes)"
  [[ "$sz" -ge "$min_bytes" ]] && return 0
  return 1
}

# P3-E7a: UI-tap fallback for 17-flutter-video-list.
# AllServices `视频列表` → RoutePath.dubbingVideoList ('/video/dubbing/videos').
# The tile sits in catalog section '教学服务' (after 班级教学 / 配音首页 / 作品列表),
# so 2–3 scroll passes are typically needed to expose it.
ui_capture_video_list() {
  local name="$1"
  local min_bytes="${WAVE4_MIN_BYTES:-100000}"
  local entry_xy sz label _try
  ui_cold_main || { log "$name: ui_cold_main failed"; return 1; }
  # P3-E7a: prime to logged-in Home (same rationale as Wave3 music/pay) so the
  # AllServices '更多' tile is reliably present after force-stop drops the session.
  prime_logged_in_home || { log "$name: login prime failed"; return 1; }
  # Explicitly re-tap Home before opening AllServices — paywall dismissal or
  # login flow can leave Chat/Mine on top.
  read -r w h < <(wm_size)
  adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
  sleep 2
  if ! ui_open_all_services; then
    log "$name: AllServices open failed — retry once from Home tab"
    adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
    sleep 2
    ui_open_all_services || { log "$name: AllServices open failed after retry"; return 1; }
  fi
  # Multi-pass scroll — `视频列表` lives in 教学服务 (section index 2 of 4) and
  # can sit below the fold on small screens. up to 6 passes; alternate labels
  # are flavor fallbacks (视频 / dubbing / Dubbing).
  entry_xy=""
  for _try in 1 2 3 4 5 6; do
    for label in "视频列表" "视频" "dubbing" "Dubbing"; do
      if entry_xy=$(ui_center "$label" 2>/dev/null); then
        log "$name: found '$label' @ $entry_xy (scroll pass $_try)"
        break 2
      fi
    done
    adb -s "$SERIAL" shell input swipe 720 1800 720 700 600 >/dev/null 2>&1 || true
    sleep 1.5
  done
  if [[ -z "$entry_xy" ]]; then
    log "$name: none of 视频列表/视频/dubbing/Dubbing visible after scroll"
    return 1
  fi
  log "$name: tap '$label' @ $entry_xy"
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $entry_xy
  sleep 5
  # VideoList page may surface VIP / paywall needles on some builds; pre-dismiss.
  dismiss_paywall_if_present || true
  sz=$(shot_size "$name")
  if [[ "$sz" -lt "$min_bytes" ]]; then
    sleep 4
    sz=$(shot_size "$name")
  fi
  log "$name: UI tap shot size=$sz (gate $min_bytes)"
  if [[ "$sz" -lt "$min_bytes" ]]; then
    return 1
  fi
  # P3-E8a: same content reject as the deeplink path — size alone cannot tell
  # a settled Home frame from the video list.
  if ! video_list_content_ok; then
    log "$name: content reject — shot is not the video list"
    return 1
  fi
  return 0
}

# P3-E7a: UI-tap fallback for 21-flutter-classroom-list.
# AllServices `班级教学` → RoutePath.classroomMyClass ('/classroom/my_class').
# `班级教学` is the FIRST item in catalog section '教学服务', so usually 1–2
# scrolls reach it.
ui_capture_classroom_list() {
  local name="$1"
  local min_bytes="${WAVE4_MIN_BYTES:-100000}"
  local entry_xy sz label _try
  ui_cold_main || { log "$name: ui_cold_main failed"; return 1; }
  prime_logged_in_home || { log "$name: login prime failed"; return 1; }
  read -r w h < <(wm_size)
  adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
  sleep 2
  if ! ui_open_all_services; then
    log "$name: AllServices open failed — retry once from Home tab"
    adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
    sleep 2
    ui_open_all_services || { log "$name: AllServices open failed after retry"; return 1; }
  fi
  entry_xy=""
  for _try in 1 2 3 4 5 6; do
    for label in "班级教学" "班级" "我的班级" "Classroom"; do
      if entry_xy=$(ui_center "$label" 2>/dev/null); then
        log "$name: found '$label' @ $entry_xy (scroll pass $_try)"
        break 2
      fi
    done
    adb -s "$SERIAL" shell input swipe 720 1800 720 700 600 >/dev/null 2>&1 || true
    sleep 1.5
  done
  if [[ -z "$entry_xy" ]]; then
    log "$name: none of 班级教学/班级/我的班级/Classroom visible after scroll"
    return 1
  fi
  log "$name: tap '$label' @ $entry_xy"
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $entry_xy
  sleep 5
  # ClassroomList typically shows tier copy (会员 / SVIP) — pre-dismiss.
  dismiss_paywall_if_present || true
  sz=$(shot_size "$name")
  if [[ "$sz" -lt "$min_bytes" ]]; then
    sleep 4
    sz=$(shot_size "$name")
  fi
  log "$name: UI tap shot size=$sz (gate $min_bytes)"
  [[ "$sz" -ge "$min_bytes" ]] && return 0
  return 1
}

# P3-E7a: best-effort UI fallback for 23-flutter-live-list.
# The `/live` route is exposed as a Home tile ('直播') on some build flavors and
# reachable via deeplink on all flavors. The deeplink path is tried first by
# the wave4 block via wave4_capture; this helper is the fallback when the
# deeplink stays on splash — search Home for the '直播' tile (NOT in
# AllServices; 'AllServicesData.catalogSections' has no live entry).
ui_capture_live_list() {
  local name="$1"
  local min_bytes="${WAVE4_MIN_BYTES:-100000}"
  local entry_xy sz label _try
  ui_cold_main || { log "$name: ui_cold_main failed"; return 1; }
  prime_logged_in_home || { log "$name: login prime failed"; return 1; }
  # Make sure Home tab is on top so the '直播' tile is visible.
  read -r w h < <(wm_size)
  adb -s "$SERIAL" shell input tap $(( w / 8 )) $(( h - 120 ))
  sleep 2
  # '直播' tile sits in the Home feature grid (5×2). Try center-x at multiple
  # Y bands (12% / 22% / 32% / 42%) before scrolling, then up to 3 scroll
  # passes if the tile is not yet exposed. Alternate labels cover flavor
  # renames (Live / 直播间).
  entry_xy=""
  for _try in 1 2 3 4 5 6 7; do
    for label in "直播" "Live" "直播间"; do
      if entry_xy=$(ui_center "$label" 2>/dev/null); then
        log "$name: found '$label' @ $entry_xy (scroll pass $_try)"
        break 2
      fi
    done
    adb -s "$SERIAL" shell input swipe 720 1800 720 700 600 >/dev/null 2>&1 || true
    sleep 1.5
  done
  if [[ -z "$entry_xy" ]]; then
    log "$name: none of 直播/Live/直播间 visible on Home — Home tile absent on this build"
    return 1
  fi
  log "$name: tap '$label' @ $entry_xy"
  # shellcheck disable=SC2086
  adb -s "$SERIAL" shell input tap $entry_xy
  sleep 5
  # Live page may surface VIP / membership copy — pre-dismiss.
  dismiss_paywall_if_present || true
  sz=$(shot_size "$name")
  if [[ "$sz" -lt "$min_bytes" ]]; then
    sleep 4
    sz=$(shot_size "$name")
  fi
  log "$name: UI tap shot size=$sz (gate $min_bytes)"
  [[ "$sz" -ge "$min_bytes" ]] && return 0
  return 1
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

if want wave3; then
  # Cross-Source coverage: auth-gated routes the Kuikly side already captures but
  # the Flutter-ref side was missing. Reuse flutter_sms_login / dismiss_paywall_if_present
  # to set up a session. P3-E1a2 harden: deeplink stems go through wave3_capture
  # (size gate + retry); post-detail (14) keeps its tap path with only a size
  # check (no retry per handoff); --update then skips locking any unreliable
  # stem via WAVE3_OK_<stem>.
  log "capture wave3 set (usedcar-detail, settings, login-password, post-detail, music-list, friend-list, pay-list)"
  # Cold start + splash poll (same pattern as loggedin / wave2) — gives SMS login a settled Main.
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
  COMM_X=$(( W * 5 / 8 ))

  # 09: login-password — P3-E1a2: prefer force-stop before each Wave3 deeplink
  # to avoid "intent delivered to top-most instance" no-op; wave3_capture wraps
  # ensure_main_then_deeplink (which already does force-stop + cold + deeplink)
  # and gates the shot on WAVE3_MIN_BYTES with a longer-settle retry.
  wave3_capture WAVE3_OK_09 "09-flutter-login-password" "login/password" 4 || true

  # SMS login for the auth-gated stems below. Skip if the loggedin block already
  # produced 11-flutter-main-chat.png (CAPTURE_SET=all path), so we don't
  # re-trigger the auth gate over an existing session.
  if [[ ! -f "$OUT/11-flutter-main-chat.png" ]]; then
    flutter_sms_login
    dismiss_paywall_if_present || true
    sleep 2
  fi

  # 05: used-car-detail — deeplink primary; Orchestrator may swap to list→detail tap.
  wave3_capture WAVE3_OK_05 "05-flutter-usedcar-detail" "home/used_car/detail" 6 || true
  # 08: settings — deeplink primary; P3-E4d UI-tap fallback via Mine '设置'
  # row (mine_menu_data.dart line 95 → SettingsPage '设置'). Mirrors the
  # 15/25 UI-tap fallback pattern: deeplink first, fall back to ui_center
  # tap when wave3_capture fails the WAVE3_MIN_BYTES gate. UNRELIABLE if
  # both paths fail — skip baseline lock (preserve any prior 459KB baseline).
  WAVE3_OK_08=0
  wave3_capture WAVE3_OK_08 "08-flutter-settings" "settings" 5 || true
  if [[ "${WAVE3_OK_08:-0}" -ne 1 ]]; then
    log "wave3 08: deeplink UNRELIABLE — trying Mine '设置' UI tap"
    if ui_capture_settings "08-flutter-settings"; then
      log "wave3 08: UI tap fallback OK — gate won"
      WAVE3_OK_08=1
    else
      log "wave3 08: UI tap fallback UNRELIABLE — skip baseline lock"
      WAVE3_OK_08=0
    fi
  fi
  # 14: post-detail — community tab → first post (tap primary; deeplink is
  # best-effort fallback). Per handoff: keep tap path, add size check only — no
  # retry, because the tap math is the only reliable way to land on the post.
  adb -s "$SERIAL" shell input tap "$COMM_X" "$TAB_Y"
  sleep 4
  adb -s "$SERIAL" shell input tap "$(( W / 2 ))" "$(( H * 18 / 100 ))"
  sleep 5
  shot "14-flutter-post-detail"
  sz=$(wc -c < "$OUT/14-flutter-post-detail.png" | tr -d " ")
  if [[ "$sz" -ge "$WAVE3_MIN_BYTES" ]]; then
    log "wave3 14-flutter-post-detail: size=$sz ≥ gate $WAVE3_MIN_BYTES (OK)"
    WAVE3_OK_14=1
  else
    log "wave3 14-flutter-post-detail: size=$sz < gate — UNRELIABLE (skip baseline lock)"
    WAVE3_OK_14=0
  fi
  # 15: music-list — deeplink primary; UI-tap fallback via AllServices '音频列表'.
  # P3-E1a3: ui_capture_music_list mirrors the chat-detail Soft Gate pattern
  # (force-stop, splash poll, ui_center tap, size-gated). If the deeplink size
  # gate fails twice, the UI fallback is tried once before declaring UNRELIABLE.
  WAVE3_OK_15=0
  wave3_capture WAVE3_OK_15 "15-flutter-music-list" "music/list" 5 || true
  if [[ "${WAVE3_OK_15:-0}" -ne 1 ]]; then
    log "wave3 15: deeplink UNRELIABLE — trying AllServices '音频列表' UI tap"
    if ui_capture_music_list "15-flutter-music-list"; then
      log "wave3 15: UI tap fallback OK — gate won"
      WAVE3_OK_15=1
    else
      log "wave3 15: UI tap fallback UNRELIABLE — skip baseline lock"
      WAVE3_OK_15=0
    fi
  fi
  # 19: friend-list — deeplink primary. P3-E1a3: NO UI entry exists in Home /
  # AllServices / Mine menus (FriendPage.dart is a Center(Text('Friend 模块'))
  # stub; route /friend has no caller outside friend_module.dart). Try the
  # deeplink via wave3_capture; if it fails, leave UNRELIABLE without inventing
  # a fake UI path.
  WAVE3_OK_19=0
  wave3_capture WAVE3_OK_19 "19-flutter-friend-list" "friend" 5 || true
  # P3-E9g: true Friend stub is ~55KB — WAVE3_MIN_BYTES rejects it. Accept via
  # STUB_MIN_BYTES + friend_list_content_ok (rejects splash/Home).
  if [[ "${WAVE3_OK_19:-0}" -ne 1 ]]; then
    sz=$(wc -c < "$OUT/19-flutter-friend-list.png" 2>/dev/null | tr -d " " || echo 0)
    if [[ "${sz:-0}" -ge "$STUB_MIN_BYTES" ]] && friend_list_content_ok; then
      log "wave3 19: stub size override size=$sz ≥ STUB_MIN=$STUB_MIN_BYTES + content OK"
      WAVE3_OK_19=1
    fi
  fi
  # P3-E9d: size ≥ gate is NOT enough — a settled Home frame also passes it.
  # Content-reject; on reject retry the deeplink once with a longer settle
  # (the stub can mount a beat later after the cold Main) before zeroing.
  if [[ "${WAVE3_OK_19:-0}" -eq 1 ]] && ! friend_list_content_ok; then
    log "wave3 19: size gate passed but content reject — retry deeplink with longer settle"
    WAVE3_OK_19=0
    wave3_capture WAVE3_OK_19 "19-flutter-friend-list" "friend" 10 || true
    if [[ "${WAVE3_OK_19:-0}" -ne 1 ]]; then
      sz=$(wc -c < "$OUT/19-flutter-friend-list.png" 2>/dev/null | tr -d " " || echo 0)
      if [[ "${sz:-0}" -ge "$STUB_MIN_BYTES" ]] && friend_list_content_ok; then
        log "wave3 19: stub size override (retry) size=$sz + content OK"
        WAVE3_OK_19=1
      fi
    fi
    if [[ "${WAVE3_OK_19:-0}" -eq 1 ]] && ! friend_list_content_ok; then
      log "wave3 19: retry still content reject — UNRELIABLE (skip baseline lock)"
      WAVE3_OK_19=0
    fi
  fi
  if [[ "${WAVE3_OK_19:-0}" -ne 1 ]]; then
    log "wave3 19: deeplink UNRELIABLE — NO UI entry exists (friend_page.dart is a stub), keeping UNRELIABLE"
    WAVE3_OK_19=0
  fi
  # 25: pay-list — deeplink primary. P3-E1a3: UI fallback via AllServices
  # '会员续费' (only in-app Pay surface reachable from Main; '/pay' deeplink goes
  # to PayPage stub — no parity match). Best-effort; if the gate still fails
  # after the UI tap, leave UNRELIABLE.
  WAVE3_OK_25=0
  wave3_capture WAVE3_OK_25 "25-flutter-pay-list" "pay" 5 || true
  if [[ "${WAVE3_OK_25:-0}" -ne 1 ]]; then
    log "wave3 25: deeplink UNRELIABLE — trying AllServices '会员续费' UI tap (lands on MembershipRenewPage)"
    if ui_capture_pay_list "25-flutter-pay-list"; then
      log "wave3 25: UI tap fallback OK — gate won"
      WAVE3_OK_25=1
    else
      log "wave3 25: UI tap fallback UNRELIABLE — skip baseline lock"
      WAVE3_OK_25=0
    fi
  fi
fi

if want wave4; then
  # P3-E7a: Wave4 cross-source expansion. Targets the 5 Kuikly self-lock stems
  # that currently have NO Flutter-ref baseline (17 video-list, 18 video-detail,
  # 21 classroom-list, 23 live-list, 26 pay-confirm). Pattern mirrors Wave3:
  #   - deeplink-first via wave4_capture (size gate + retry)
  #   - UI-tap fallback when AllServices / Home exposes the page (17 / 21 / 23)
  #   - deeplink-only (18 / 26) — RoutePath has no other in-app surface, so the
  #     WAVE4_OK_<stem> stays 0 on a failed gate and --update skips locking.
  log "capture wave4 set (video-list, video-detail, classroom-list, live-list, pay-confirm)"

  # Cold start + splash poll (matches wave3 / loggedin pattern) — gives every
  # Wave4 deeplink a settled Main even if the previous block force-stopped.
  adb -s "$SERIAL" shell am force-stop "$BUNDLE" >/dev/null 2>&1 || true
  sleep 0.4
  adb -s "$SERIAL" shell am start -n "$BUNDLE/$ACTIVITY" >/dev/null
  for _i in 1 2 3 4 5 6 7 8 9 10 11 12; do
    sleep 2
    adb -s "$SERIAL" exec-out screencap -p > "$OUT/_probe-main.png"
    sz=$(wc -c < "$OUT/_probe-main.png" | tr -d " ")
    [[ "$sz" -gt 120000 ]] && break
  done

  # 17: video-list — deeplink primary; AllServices `视频列表` UI fallback.
  # RoutePath.dubbingVideoList = '/video/dubbing/videos' → VideoListPage.
  WAVE4_OK_17=0
  wave4_capture WAVE4_OK_17 "17-flutter-video-list" "video/dubbing/videos" 6 || true
  # P3-E8a: size ≥ gate is NOT enough — a settled Home also passes it (that is
  # how Home got locked as 17 in evidence 149). Content-reject the deeplink
  # shot; on reject zero the flag so the UI-tap fallback below runs.
  if [[ "${WAVE4_OK_17:-0}" -eq 1 ]] && ! video_list_content_ok; then
    log "wave4 17: size gate passed but content reject — falling through to UI tap fallback"
    WAVE4_OK_17=0
  fi
  if [[ "${WAVE4_OK_17:-0}" -ne 1 ]]; then
    log "wave4 17: deeplink UNRELIABLE — trying AllServices '视频列表' UI tap"
    if ui_capture_video_list "17-flutter-video-list"; then
      log "wave4 17: UI tap fallback OK — gate won"
      WAVE4_OK_17=1
    else
      log "wave4 17: UI tap fallback UNRELIABLE — skip baseline lock"
      WAVE4_OK_17=0
    fi
  fi

  # 18: video-detail — deeplink only. RoutePath.dubbingVideoDetail =
  # '/video/dubbing/videos/detail' → VideoDetailPage. No AllServices entry
  # for this route (no UI surface to fall back to); UNRELIABLE if the gate
  # fails twice, and the file is deleted so a missing file is preserved as
  # missing instead of getting a locked splash.
  WAVE4_OK_18=0
  wave4_capture WAVE4_OK_18 "18-flutter-video-detail" "video/dubbing/videos/detail" 6 || true
  if [[ "${WAVE4_OK_18:-0}" -ne 1 ]]; then
    log "wave4 18: deeplink UNRELIABLE — no UI fallback (RoutePath has no other in-app surface)"
    rm -f "$OUT/18-flutter-video-detail.png"
  fi

  # 21: classroom-list — deeplink primary; AllServices `班级教学` UI fallback.
  # RoutePath.classroomMyClass = '/classroom/my_class' → MyClassPage.
  WAVE4_OK_21=0
  wave4_capture WAVE4_OK_21 "21-flutter-classroom-list" "classroom/my_class" 6 || true
  if [[ "${WAVE4_OK_21:-0}" -ne 1 ]]; then
    log "wave4 21: deeplink UNRELIABLE — trying AllServices '班级教学' UI tap"
    if ui_capture_classroom_list "21-flutter-classroom-list"; then
      log "wave4 21: UI tap fallback OK — gate won"
      WAVE4_OK_21=1
    else
      log "wave4 21: UI tap fallback UNRELIABLE — skip baseline lock"
      WAVE4_OK_21=0
    fi
  fi

  # 23: live-list — deeplink primary (RoutePath.live = '/live'); best-effort
  # Home '直播' tile UI fallback (NOT in AllServices — see all_services_data.dart).
  WAVE4_OK_23=0
  wave4_capture WAVE4_OK_23 "23-flutter-live-list" "live" 6 || true
  # P3-E9g: true Live stub is ~57KB — WAVE4_MIN_BYTES rejects it.
  if [[ "${WAVE4_OK_23:-0}" -ne 1 ]]; then
    sz=$(wc -c < "$OUT/23-flutter-live-list.png" 2>/dev/null | tr -d " " || echo 0)
    if [[ "${sz:-0}" -ge "$STUB_MIN_BYTES" ]] && live_list_content_ok; then
      log "wave4 23: stub size override size=$sz ≥ STUB_MIN=$STUB_MIN_BYTES + content OK"
      WAVE4_OK_23=1
    fi
  fi
  # P3-E9d: size ≥ gate is not enough (Home passes it too, and Home carries a
  # '直播' tile) — content-reject the deeplink shot; on reject zero the flag so
  # the UI-tap fallback below runs.
  if [[ "${WAVE4_OK_23:-0}" -eq 1 ]] && ! live_list_content_ok; then
    log "wave4 23: size gate passed but content reject — falling through to UI tap fallback"
    WAVE4_OK_23=0
  fi
  if [[ "${WAVE4_OK_23:-0}" -ne 1 ]]; then
    log "wave4 23: deeplink UNRELIABLE — trying Home '直播' tile UI tap (best-effort)"
    if ui_capture_live_list "23-flutter-live-list"; then
      # P3-E9d: the tap can miss and leave Home up (still ≥ gate) — gate the
      # UI-tap shot on content too before declaring the stem reliable.
      if live_list_content_ok; then
        log "wave4 23: UI tap fallback OK — gate won"
        WAVE4_OK_23=1
      else
        log "wave4 23: UI tap content reject — skip baseline lock"
        WAVE4_OK_23=0
      fi
    else
      # P3-E9g: UI tap may also fail size gate on the stub — last-chance stub override
      sz=$(wc -c < "$OUT/23-flutter-live-list.png" 2>/dev/null | tr -d " " || echo 0)
      if [[ "${sz:-0}" -ge "$STUB_MIN_BYTES" ]] && live_list_content_ok; then
        log "wave4 23: stub size override after UI-tap fail size=$sz + content OK"
        WAVE4_OK_23=1
      else
        log "wave4 23: UI tap fallback UNRELIABLE — skip baseline lock"
        WAVE4_OK_23=0
      fi
    fi
  fi

  # 26: pay-confirm — deeplink only. RoutePath has no '/pay/confirm' surface;
  # the closest real page reachable via deeplink is RoutePath.payMembership =
  # '/pay/membership' → MembershipRenewPage (the same surface used for the 25
  # pay-list stem). Kuikly PayConfirmPage and Flutter MembershipRenewPage are
  # NOT parity-matched by shape — the stem is captured best-effort so the
  # Kuikly side has at least a non-empty baseline to compare against.
  WAVE4_OK_26=0
  wave4_capture WAVE4_OK_26 "26-flutter-pay-confirm" "pay/membership" 6 || true
  if [[ "${WAVE4_OK_26:-0}" -ne 1 ]]; then
    log "wave4 26: deeplink UNRELIABLE — no UI fallback (PayConfirmPage not implemented in Flutter; closest surface is /pay/membership)"
    rm -f "$OUT/26-flutter-pay-confirm.png"
  fi
fi

if [[ "$UPDATE" -eq 1 ]]; then
  # Copy actual→baseline, but do not clobber a prior good 12 with a missing/unreliable shot.
  # P3-E1a2: same skip-lock pattern for Wave3 stems whose size gate failed —
  # WAVE3_OK_<stem> stays 0 (defaulted at top when wave3 didn't run), so any
  # prior baseline is preserved instead of being overwritten by a splash frame.
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
    case "$bn" in
      09-flutter-login-password.png)
        [[ "${WAVE3_OK_09:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave3 gate)"; continue; } ;;
      05-flutter-usedcar-detail.png)
        [[ "${WAVE3_OK_05:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave3 gate)"; continue; } ;;
      08-flutter-settings.png)
        [[ "${WAVE3_OK_08:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave3 gate)"; continue; } ;;
      14-flutter-post-detail.png)
        [[ "${WAVE3_OK_14:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave3 gate)"; continue; } ;;
      15-flutter-music-list.png)
        [[ "${WAVE3_OK_15:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave3 gate)"; continue; } ;;
      19-flutter-friend-list.png)
        [[ "${WAVE3_OK_19:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave3 gate)"; continue; } ;;
      25-flutter-pay-list.png)
        [[ "${WAVE3_OK_25:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave3 gate)"; continue; } ;;
      17-flutter-video-list.png)
        [[ "${WAVE4_OK_17:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave4 gate)"; continue; } ;;
      18-flutter-video-detail.png)
        [[ "${WAVE4_OK_18:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave4 gate)"; continue; } ;;
      21-flutter-classroom-list.png)
        [[ "${WAVE4_OK_21:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave4 gate)"; continue; } ;;
      23-flutter-live-list.png)
        [[ "${WAVE4_OK_23:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave4 gate)"; continue; } ;;
      26-flutter-pay-confirm.png)
        [[ "${WAVE4_OK_26:-0}" -ne 1 ]] && { log "skip locking unreliable $bn (wave4 gate)"; continue; } ;;
    esac
    cp -f "$f" "$BASE/$bn"
  done
  log "baselines locked under goldens/flutter-ref/baseline/"
fi
log "actuals in $OUT (CAPTURE_SET=$CAPTURE_SET)"
