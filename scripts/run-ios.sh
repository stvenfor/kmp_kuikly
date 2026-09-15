#!/usr/bin/env bash
# Prepare pods, build, and launch iosApp on Simulator.
# Usage: ./scripts/run-ios.sh [--page Home] [--sim "iPhone 16"] [--skip-pods]
set -euo pipefail
# shellcheck source=common.sh
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

PAGE="$PAGE_DEFAULT"
SIM_NAME="${SIM:-iPhone 16}"
SKIP_PODS=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --page|-p) PAGE="${2:?}"; shift 2 ;;
    --sim) SIM_NAME="${2:?}"; shift 2 ;;
    --skip-pods) SKIP_PODS=1; shift ;;
    --help|-h)
      cat <<EOF
Usage: $0 [--page PAGE] [--sim "iPhone 16"] [--skip-pods]

  --page       Kuikly page (passed as SIMCTL_CHILD_KUIKLY_PAGE; default: Home)
  --sim        Simulator device name (default: iPhone 16)
  --skip-pods  Skip generateDummyFramework / xcodegen / pod install
EOF
      exit 0
      ;;
    *) die "unknown arg: $1" ;;
  esac
done

command -v xcrun >/dev/null || die "xcrun not found (need Xcode CLI)"
command -v xcodegen >/dev/null || die "xcodegen not found (brew install xcodegen)"
command -v pod >/dev/null || die "pod not found (sudo gem install cocoapods / brew install cocoapods)"

udid_for_sim() {
  xcrun simctl list devices available | awk -v name="$SIM_NAME" '
    $0 ~ name && /\([A-F0-9-]{36}\)/ {
      if (match($0, /\([A-F0-9-]{36}\)/)) {
        id=substr($0, RSTART+1, RLENGTH-2)
        print id
        exit
      }
    }'
}

UDID="$(udid_for_sim)"
[[ -n "$UDID" ]] || die "simulator not found: $SIM_NAME (try: xcrun simctl list devices available)"

if [[ "$SKIP_PODS" -eq 0 ]]; then
  log "generateDummyFramework"
  ./gradlew :app-shared:generateDummyFramework
  log "xcodegen + pod install"
  (
    cd "$ROOT/iosApp"
    xcodegen generate
    pod install
  )
fi

STATE="$(xcrun simctl list devices | grep "$UDID" | grep -oE 'Booted|Shutdown|Shutting Down' | head -1 || true)"
if [[ "$STATE" != "Booted" ]]; then
  log "boot simulator $SIM_NAME ($UDID)"
  xcrun simctl boot "$UDID" || true
  open -a Simulator --args -CurrentDeviceUDID "$UDID"
  # wait until booted
  for _ in $(seq 1 60); do
    xcrun simctl list devices | grep "$UDID" | grep -q Booted && break
    sleep 1
  done
fi

DERIVED="$ROOT/iosApp/build/DerivedData"
SCHEME="iosApp"
WORKSPACE="$ROOT/iosApp/iosApp.xcworkspace"
[[ -d "$WORKSPACE" ]] || die "missing $WORKSPACE — run without --skip-pods first"

log "xcodebuild ($SCHEME → $SIM_NAME)"
xcodebuild \
  -workspace "$WORKSPACE" \
  -scheme "$SCHEME" \
  -configuration Debug \
  -destination "platform=iOS Simulator,id=$UDID" \
  -derivedDataPath "$DERIVED" \
  -quiet \
  build

APP="$(find "$DERIVED/Build/Products" -name 'iosApp.app' -type d | head -1)"
[[ -n "$APP" && -d "$APP" ]] || die "iosApp.app not found under $DERIVED"

log "install + launch page=$PAGE"
xcrun simctl install "$UDID" "$APP"
# AppDelegate reads ProcessInfo environment KUIKLY_PAGE
SIMCTL_CHILD_KUIKLY_PAGE="$PAGE" xcrun simctl launch "$UDID" com.example.kuikly

log "done"
