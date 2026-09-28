#!/usr/bin/env bash
# Diff Android golden actual/ vs baseline/. Exit 1 on mismatch.
set -euo pipefail
# shellcheck source=common.sh
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ACTUAL="$ROOT/goldens/android/actual"
BASE="$ROOT/goldens/android/baseline"
DIFF_DIR="$ROOT/goldens/android/diff"
THRESHOLD="${GOLDEN_PIXEL_THRESHOLD:-0}"

[[ -d "$BASE" ]] || die "missing baselines: $BASE (run golden-android-capture.sh --update once)"
[[ -d "$ACTUAL" ]] || die "missing actuals: $ACTUAL (run golden-android-capture.sh)"

mkdir -p "$DIFF_DIR"
fail=0

shopt -s nullglob
for base in "$BASE"/*.png; do
  name="$(basename "$base")"
  actual="$ACTUAL/$name"
  if [[ ! -f "$actual" ]]; then
    warn "missing actual: $name"
    fail=1
    continue
  fi
  if command -v compare >/dev/null 2>&1; then
    # ImageMagick compare; AE = absolute error count
    ae="$(compare -metric AE "$base" "$actual" "$DIFF_DIR/$name" 2>&1 || true)"
    ae_num="$(echo "$ae" | tr -dc '0-9' | head -c 12)"
    ae_num="${ae_num:-999999}"
    if [[ "$ae_num" -gt "$THRESHOLD" ]]; then
      warn "DIFF $name AE=$ae_num (threshold $THRESHOLD)"
      fail=1
    else
      log "OK $name AE=$ae_num"
    fi
  else
    # Fallback: byte-identical via cmp
    if cmp -s "$base" "$actual"; then
      log "OK $name (byte-identical)"
    else
      warn "DIFF $name (cmp; install ImageMagick compare for AE metric)"
      cp -f "$actual" "$DIFF_DIR/$name"
      fail=1
    fi
  fi
done

if [[ "$fail" -ne 0 ]]; then
  die "golden diff failed — see $DIFF_DIR"
fi
log "all Android goldens passed"
