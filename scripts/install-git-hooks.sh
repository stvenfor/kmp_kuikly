#!/usr/bin/env bash
# Install repo git hooks without changing git config (copies into .git/hooks).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "$ROOT/.git/hooks"
cp -f "$ROOT/.githooks/commit-msg" "$ROOT/.git/hooks/commit-msg"
chmod +x "$ROOT/.git/hooks/commit-msg" "$ROOT/.githooks/commit-msg"
echo "Installed commit-msg hook -> .git/hooks/commit-msg"
