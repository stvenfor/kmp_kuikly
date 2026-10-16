#!/usr/bin/env bash
# Pack JS bundle and run H5 webpack-dev-server.
# Usage: ./scripts/run-h5.sh [--page Home] [--port 8080]
set -euo pipefail
# shellcheck source=common.sh
source "$(cd "$(dirname "$0")" && pwd)/common.sh"

PAGE="$PAGE_DEFAULT"
PORT="${PORT:-8080}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --page|-p) PAGE="${2:?}"; shift 2 ;;
    --port) PORT="${2:?}"; shift 2 ;;
    --help|-h)
      cat <<EOF
Usage: $0 [--page PAGE] [--port 8080]

  Packs :app-shared JS bundle then starts :h5App:jsBrowserDevelopmentRun.
  Open: http://localhost:PORT/?page_name=PAGE
EOF
      exit 0
      ;;
    *) die "unknown arg: $1" ;;
  esac
done

log "packLocalJsBundleDebug"
./gradlew :app-shared:packLocalJsBundleDebug

log "dev server (page_name=$PAGE) → http://localhost:${PORT}/?page_name=${PAGE}"
# webpack-dev-server default is 8080 for this project
./gradlew :h5App:jsBrowserDevelopmentRun
