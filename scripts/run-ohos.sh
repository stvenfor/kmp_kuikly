#!/usr/bin/env bash
# Best-effort HarmonyOS: link shared .so (if possible), assemble HAP, install via hdc.
# Usage: ./scripts/run-ohos.sh [--page Home] [--open-deveco]
set -euo pipefail
# shellcheck source=common.sh
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

PAGE="$PAGE_DEFAULT"
OPEN_DEVECO=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --page|-p) PAGE="${2:?}"; shift 2 ;;
    --open-deveco) OPEN_DEVECO=1; shift ;;
    --help|-h)
      cat <<EOF
Usage: $0 [--page PAGE] [--open-deveco]

  Tries CLI path: copy/link libshared.so → hvigor assemble → hdc install/start.
  Ohos KMP link often needs DevEco signing; --open-deveco opens the project.

  Bundle: com.example.kuikly / EntryAbility
  Page default in Index.ets is HelloWorld (router params); CLI start uses ability only.
EOF
      exit 0
      ;;
    *) die "unknown arg: $1" ;;
  esac
done

if [[ "$OPEN_DEVECO" -eq 1 ]]; then
  open -a "DevEco-Studio" "$ROOT/ohosApp" 2>/dev/null || open "$ROOT/ohosApp"
  log "opened ohosApp in DevEco Studio"
fi

ensure_hdc || die "hdc not found (install DevEco Studio SDK toolchains)"
log "hdc=$HDC_BIN"
"$HDC_BIN" list targets || true

LIBS_DIR="$ROOT/ohosApp/entry/libs/arm64-v8a"
mkdir -p "$LIBS_DIR"

# Prefer an already-built shared lib from Kotlin/Native ohos target
SO_CANDIDATES=(
  "$ROOT/app-shared/build/bin/ohosArm64/debugShared/libshared.so"
  "$ROOT/app-shared/build/bin/ohosArm64/releaseShared/libshared.so"
)
SO=""
for c in "${SO_CANDIDATES[@]}"; do
  [[ -f "$c" ]] && { SO="$c"; break; }
done

if [[ -z "$SO" ]]; then
  warn "libshared.so not found — attempting ohos Gradle link (may fail after module split)"
  if ./gradlew -c settings.ohos.gradle.kts :app-shared:linkDebugSharedOhosArm64; then
    SO="$ROOT/app-shared/build/bin/ohosArm64/debugShared/libshared.so"
  else
    warn "Gradle ohos link failed. Build/run from DevEco (Signing Configs → Run entry)."
    warn "See docs/runbooks/ohos.md"
    [[ "$OPEN_DEVECO" -eq 1 ]] || open -a "DevEco-Studio" "$ROOT/ohosApp" 2>/dev/null || true
    exit 2
  fi
fi

[[ -f "$SO" ]] || die "still no libshared.so"
log "copy $SO → $LIBS_DIR/"
cp -f "$SO" "$LIBS_DIR/libshared.so"

if ! ensure_hvigorw; then
  warn "hvigorw not found; open DevEco to assemble HAP"
  open -a "DevEco-Studio" "$ROOT/ohosApp" 2>/dev/null || true
  exit 2
fi

log "hvigor assemble ($HVIGORW)"
(
  cd "$ROOT/ohosApp"
  if [[ -x "$HVIGORW" ]]; then
    "$HVIGORW" --mode module -p module=entry@default -p product=default assembleHap
  else
    node "$HVIGORW" --mode module -p module=entry@default -p product=default assembleHap
  fi
)

HAP="$(find "$ROOT/ohosApp/entry/build" -name '*.hap' 2>/dev/null | head -1 || true)"
[[ -n "$HAP" ]] || die "no .hap produced under ohosApp/entry/build"

TARGET="$("$HDC_BIN" list targets | awk 'NF && $1 !~ /\[/ {print $1; exit}')"
[[ -n "$TARGET" ]] || die "no hdc target; start Harmony emulator or plug device"

log "install $HAP → $TARGET"
"$HDC_BIN" -t "$TARGET" install -r "$HAP"

log "start EntryAbility (page UI default HelloWorld; PAGE=$PAGE noted for Index router)"
"$HDC_BIN" -t "$TARGET" shell aa start -a EntryAbility -b com.example.kuikly || \
  "$HDC_BIN" -t "$TARGET" shell aa start -a EntryAbility -b com.example.kuikly -m entry

log "done"
