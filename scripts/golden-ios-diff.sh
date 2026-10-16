#!/usr/bin/env bash
# Diff iOS golden actual/ vs baseline/. Exit 1 on mismatch.
set -euo pipefail
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ACTUAL="$ROOT/goldens/ios/actual"
BASE="$ROOT/goldens/ios/baseline"
DIFF_DIR="$ROOT/goldens/ios/diff"
THRESHOLD="${GOLDEN_PIXEL_THRESHOLD:-0}"

[[ -d "$BASE" ]] || die "missing baselines: $BASE"
[[ -d "$ACTUAL" ]] || die "missing actuals: $ACTUAL"
mkdir -p "$DIFF_DIR"
fail=0
shopt -s nullglob
for base in "$BASE"/*.png; do
  name="$(basename "$base")"
  actual="$ACTUAL/$name"
  if [[ ! -f "$actual" ]]; then warn "missing actual: $name"; fail=1; continue; fi
  if command -v compare >/dev/null 2>&1; then
    ae="$(compare -metric AE "$base" "$actual" "$DIFF_DIR/$name" 2>&1 || true)"
    ae_num="$(echo "$ae" | tr -dc '0-9' | head -c 12)"; ae_num="${ae_num:-999999}"
    if [[ "$ae_num" -gt "$THRESHOLD" ]]; then warn "DIFF $name AE=$ae_num"; fail=1; else log "OK $name AE=$ae_num"; fi
  else
    if cmp -s "$base" "$actual"; then log "OK $name (byte-identical)"
    else warn "DIFF $name"; cp -f "$actual" "$DIFF_DIR/$name"; fail=1; fi
  fi
done
[[ "$fail" -eq 0 ]] || die "golden diff failed — see $DIFF_DIR"
log "all iOS goldens passed"
