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

MAP=(
  "02-main-home=02-flutter-main-home"
  "06-main-me-guest=03-flutter-main-me-guest"
)

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
