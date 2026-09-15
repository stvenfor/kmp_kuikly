#!/usr/bin/env bash
# Shared helpers for scripts/run-*.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

log()  { printf '==> %s\n' "$*"; }
warn() { printf '!!  %s\n' "$*" >&2; }
die()  { printf 'error: %s\n' "$*" >&2; exit 1; }

PAGE_DEFAULT="${PAGE:-Home}"

parse_page_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --page|-p)
        PAGE_DEFAULT="${2:?}"
        shift 2
        ;;
      --help|-h)
        return 2
        ;;
      *)
        EXTRA_ARGS+=("$1")
        shift
        ;;
    esac
  done
}

ensure_android_sdk() {
  if [[ -z "${ANDROID_HOME:-}${ANDROID_SDK_ROOT:-}" ]]; then
    if [[ -d "$HOME/Library/Android/sdk" ]]; then
      export ANDROID_HOME="$HOME/Library/Android/sdk"
      export ANDROID_SDK_ROOT="$ANDROID_HOME"
    fi
  fi
  export PATH="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}/platform-tools:${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}/emulator:${PATH}"
  command -v adb >/dev/null || die "adb not found; install Android SDK platform-tools"
}

ensure_hdc() {
  local candidates=(
    "${HDC:-}"
    "/Applications/DevEco-Studio.app/Contents/sdk/default/openharmony/toolchains/hdc"
    "/Applications/DevEco-Studio.app/Contents/sdk/default/hms/toolchains/hdc"
  )
  for c in "${candidates[@]}"; do
    [[ -n "$c" && -x "$c" ]] || continue
    HDC_BIN="$c"
    return 0
  done
  command -v hdc >/dev/null && { HDC_BIN="$(command -v hdc)"; return 0; }
  return 1
}

ensure_hvigorw() {
  local candidates=(
    "$ROOT/ohosApp/hvigorw"
    "/Applications/DevEco-Studio.app/Contents/tools/hvigor/bin/hvigorw"
  )
  for c in "${candidates[@]}"; do
    [[ -f "$c" ]] || continue
    HVIGORW="$c"
    return 0
  done
  return 1
}
