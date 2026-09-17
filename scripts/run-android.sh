#!/usr/bin/env bash
# Build + install + launch androidApp on emulator/device.
# Usage: ./scripts/run-android.sh [--page Home] [--avd Pixel_7_Pro] [--no-boot]
set -euo pipefail
# shellcheck source=common.sh
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

EXTRA_ARGS=()
AVD_NAME="${AVD:-Pixel_7_Pro}"
NO_BOOT=0
PAGE="$PAGE_DEFAULT"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --page|-p) PAGE="${2:?}"; shift 2 ;;
    --avd) AVD_NAME="${2:?}"; shift 2 ;;
    --no-boot) NO_BOOT=1; shift ;;
    --help|-h)
      cat <<EOF
Usage: $0 [--page PAGE] [--avd AVD_NAME] [--no-boot]

  --page   Kuikly pageName (default: Splash)
  --avd    Android Virtual Device name (default: Pixel_7_Pro)
  --no-boot  Do not start an emulator if none is connected
EOF
      exit 0
      ;;
    *) die "unknown arg: $1" ;;
  esac
done

ensure_android_sdk

wait_for_adb_device() {
  local i
  for i in $(seq 1 60); do
    if adb devices | awk 'NR>1 && $2=="device"{ok=1} END{exit !ok}'; then
      return 0
    fi
    sleep 2
  done
  return 1
}

if ! adb devices | awk 'NR>1 && $2=="device"{ok=1} END{exit !ok}'; then
  if [[ "$NO_BOOT" -eq 1 ]]; then
    die "no Android device/emulator in 'device' state"
  fi
  command -v emulator >/dev/null || die "emulator not found under ANDROID_HOME"
  log "starting AVD: $AVD_NAME"
  # shellcheck disable=SC2086
  nohup emulator -avd "$AVD_NAME" -netdelay none -netspeed full >/tmp/kmp-kuikly-emulator.log 2>&1 &
  log "waiting for adb device..."
  wait_for_adb_device || die "emulator did not become ready (see /tmp/kmp-kuikly-emulator.log)"
fi

log "assembleDebug"
./gradlew :androidApp:assembleDebug

APK="$ROOT/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
[[ -f "$APK" ]] || die "APK missing: $APK"

log "install $APK"
adb install -r "$APK"

log "launch pageName=$PAGE"
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName "$PAGE"

log "done"
