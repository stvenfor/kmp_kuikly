#!/usr/bin/env bash
# Unified launcher for Demo Skeleton shells.
# Usage: ./scripts/run.sh <android|ios|ohos|h5|list> [args...]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CMD="${1:-}"
shift || true

usage() {
  cat <<EOF
kmp_kuikly demo runners

  ./scripts/run.sh android [--page Home] [--avd Pixel_7_Pro]
  ./scripts/run.sh ios     [--page Home] [--sim "iPhone 16"] [--skip-pods]
  ./scripts/run.sh ohos    [--page Home] [--open-deveco]
  ./scripts/run.sh h5      [--page Home] [--port 8080]
  ./scripts/run.sh list              # show tools / simulators / devices

Default page is Home (Android/iOS/H5). Ohos Index defaults to HelloWorld in UI.
EOF
}

list_env() {
  echo "--- tools ---"
  command -v adb || echo "adb: missing"
  command -v emulator || echo "emulator: missing"
  command -v xcrun || echo "xcrun: missing"
  command -v xcodegen || echo "xcodegen: missing"
  command -v pod || echo "pod: missing"
  command -v hdc || ls /Applications/DevEco-Studio.app/Contents/sdk/default/openharmony/toolchains/hdc 2>/dev/null || echo "hdc: missing"
  echo "--- android devices ---"
  if command -v adb >/dev/null; then adb devices; else echo "(no adb)"; fi
  echo "--- android AVDs ---"
  if [[ -x "${ANDROID_HOME:-$HOME/Library/Android/sdk}/emulator/emulator" ]]; then
    "${ANDROID_HOME:-$HOME/Library/Android/sdk}/emulator/emulator" -list-avds || true
  fi
  echo "--- ios sims (available) ---"
  xcrun simctl list devices available 2>/dev/null | head -40 || true
  echo "--- ohos hdc targets ---"
  HDC="${HDC:-/Applications/DevEco-Studio.app/Contents/sdk/default/openharmony/toolchains/hdc}"
  if [[ -x "$HDC" ]]; then "$HDC" list targets || true; else echo "(no hdc)"; fi
}

case "$CMD" in
  android) exec "$ROOT/scripts/run-android.sh" "$@" ;;
  ios)     exec "$ROOT/scripts/run-ios.sh" "$@" ;;
  ohos|harmony|harmonyos) exec "$ROOT/scripts/run-ohos.sh" "$@" ;;
  h5|web)  exec "$ROOT/scripts/run-h5.sh" "$@" ;;
  list|doctor) list_env ;;
  ""|-h|--help|help) usage ;;
  *) echo "unknown: $CMD"; usage; exit 1 ;;
esac
