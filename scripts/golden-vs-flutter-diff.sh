#!/usr/bin/env bash
# Diff Kuikly Android actual/ against Flutter Reference Golden baseline/.
# Masks status bar + home indicator; compares with ImageMagick RMSE (relative).
#
# Usage: ./scripts/golden-vs-flutter-diff.sh
# Env:
#   GOLDEN_RMSE_MAX   max relative RMSE in (0..1), default 0.22 (Wave-0 calibrate)
#   MASK_TOP_PX       default 80
#   MASK_BOTTOM_PX    default 40
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KUIKLY="${KUIKLY_ACTUAL:-$ROOT/goldens/android/actual}"
REF="${FLUTTER_REF:-$ROOT/goldens/flutter-ref/baseline}"
DIFF="$ROOT/goldens/flutter-ref/diff"
RMSE_MAX="${GOLDEN_RMSE_MAX:-0.22}"
MASK_TOP="${MASK_TOP_PX:-80}"
MASK_BOTTOM="${MASK_BOTTOM_PX:-40}"

mkdir -p "$DIFF"
fail=0

# Kuikly actual stem = Flutter-ref baseline stem
MAP=(
  "02-main-home=02-flutter-main-home"
  "02b-main-home-logged-in=02b-flutter-main-home-logged-in"
  "06-main-me-guest=03-flutter-main-me-guest"
  "07-main-me-logged-in=08-flutter-main-me-logged-in"
  "03-login=07-flutter-login"
  "10-search=10-flutter-search"
  "04-usedcar-list=04-flutter-usedcar-list"
  "11-main-chat=11-flutter-main-chat"
  "13-main-community=13-flutter-main-community"
  "12-chat-detail=12-flutter-chat-detail"
)
# Phase-3 Wave3 expand: unlock pairs as Flutter-ref baselines pass size-gate (≥100KB).
MAP[${#MAP[@]}]="05-usedcar-detail=05-flutter-usedcar-detail"
MAP[${#MAP[@]}]="08-settings=08-flutter-settings"
MAP[${#MAP[@]}]="09-login-password=09-flutter-login-password"
# pending photo-density (RMSE≈0.34 after E2a): MAP[${#MAP[@]}]="14-post-detail=14-flutter-post-detail"
MAP[${#MAP[@]}]="15-music-list=15-flutter-music-list"
# pending harden+recapture: MAP[${#MAP[@]}]="19-friend-list=19-flutter-friend-list"
MAP[${#MAP[@]}]="25-pay-list=25-flutter-pay-list"
# pending wave4 video-list polish (RMSE≈0.259): MAP[${#MAP[@]}]="17-video-list=17-flutter-video-list"
MAP[${#MAP[@]}]="18-video-detail=18-flutter-video-detail"
MAP[${#MAP[@]}]="21-classroom-list=21-flutter-classroom-list"
# pending wave4: MAP[${#MAP[@]}]="23-live-list=23-flutter-live-list"
# pending wave4: MAP[${#MAP[@]}]="26-pay-confirm=26-flutter-pay-confirm"
# Wave4 unlock: E7a Flutter-ref + size-gate + RMSE_rel ≤0.22 (evidence 148).
# Wave4 unlock gated on E7a Flutter-ref capture + size-gate + RMSE_rel ≤0.22 (evidence stub: .scratch/my-ai-migration/parity/wb-logs/E7a-*.log).
# Soft Gate S1: chat-detail Flutter-ref via UI-tap harden (P2-S1a).
# Phase-3: 02b promoted after home assets (E1b) dropped RMSE_rel to ~0.192.


MAGICK_BIN=""
if command -v magick >/dev/null 2>&1; then
  MAGICK_BIN=magick
elif command -v convert >/dev/null 2>&1 && command -v compare >/dev/null 2>&1; then
  MAGICK_BIN=legacy
else
  echo "ImageMagick required (magick or convert+compare)" >&2
  exit 1
fi

run_magick() {
  if [[ "$MAGICK_BIN" == "magick" ]]; then
    magick "$@"
  else
    # legacy: first arg is subcommand for magick CLI compatibility helpers below
    local sub="$1"; shift
    case "$sub" in
      convert) convert "$@" ;;
      compare) compare "$@" ;;
      *) echo "bad legacy sub $sub"; return 1 ;;
    esac
  fi
}

mask() {
  local src="$1" dst="$2"
  local h w
  h="$(sips -g pixelHeight "$src" 2>/dev/null | awk '/pixelHeight/ {print $2}')"
  w="$(sips -g pixelWidth "$src" 2>/dev/null | awk '/pixelWidth/ {print $2}')"
  [[ -n "$h" && -n "$w" ]] || { cp "$src" "$dst"; return; }
  local y2=$((h - MASK_BOTTOM))
  if [[ "$MAGICK_BIN" == "magick" ]]; then
    magick "$src" -fill black \
      -draw "rectangle 0,0 ${w},${MASK_TOP}" \
      -draw "rectangle 0,${y2} ${w},${h}" \
      "$dst"
  else
    convert "$src" -fill black \
      -draw "rectangle 0,0 ${w},${MASK_TOP}" \
      -draw "rectangle 0,${y2} ${w},${h}" \
      "$dst"
  fi
}

for pair in "${MAP[@]}"; do
  k="${pair%%=*}"
  f="${pair##*=}"
  ka="$KUIKLY/${k}.png"
  fr="$REF/${f}.png"
  if [[ ! -f "$ka" ]]; then echo "!! missing Kuikly actual $k"; fail=1; continue; fi
  if [[ ! -f "$fr" ]]; then echo "!! missing Flutter ref $f"; fail=1; continue; fi
  mk="$DIFF/mask-k-$k.png"
  mf="$DIFF/mask-f-$f.png"
  mask "$ka" "$mk"
  mask "$fr" "$mf"
  fw=$(sips -g pixelWidth "$mf" | awk '/pixelWidth/ {print $2}')
  fh=$(sips -g pixelHeight "$mf" | awk '/pixelHeight/ {print $2}')
  rk="$DIFF/resized-k-$k.png"
  if [[ "$MAGICK_BIN" == "magick" ]]; then
    magick "$mk" -resize "${fw}x${fh}!" "$rk"
    # stderr: "1234.5 (0.0188)" — take relative in parens
    out="$(magick compare -metric RMSE "$mf" "$rk" "$DIFF/${k}-vs-${f}.png" 2>&1 || true)"
  else
    convert "$mk" -resize "${fw}x${fh}!" "$rk"
    out="$(compare -metric RMSE "$mf" "$rk" "$DIFF/${k}-vs-${f}.png" 2>&1 || true)"
  fi
  rel="$(echo "$out" | sed -n 's/.*(\([0-9.]*\)).*/\1/p' | head -1)"
  rel="${rel:-1}"
  # awk compare floats
  worse="$(awk -v r="$rel" -v m="$RMSE_MAX" 'BEGIN { print (r > m) ? 1 : 0 }')"
  if [[ "$worse" == "1" ]]; then
    echo "!! DIFF $k vs $f RMSE_rel=$rel (max $RMSE_MAX) raw=$out"
    fail=1
  else
    echo "==> OK $k vs $f RMSE_rel=$rel (max $RMSE_MAX)"
  fi
done

[[ "$fail" -eq 0 ]] || exit 1
echo "==> all mapped Cross-Source diffs passed"
