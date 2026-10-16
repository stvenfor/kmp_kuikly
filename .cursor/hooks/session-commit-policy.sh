#!/usr/bin/env bash
# sessionStart: inject commit policy for this repo.
set -euo pipefail
cat >/dev/null
python3 - <<'PY'
import json
ctx = (
  "Git commit policy for this repo: "
  "1) Commit messages MUST be written in Chinese (include Han characters). "
  "2) NEVER add or keep the trailer `Co-authored-by: Cursor <cursoragent@cursor.com>` "
  "(or any cursoragent@cursor.com co-author). "
  "3) Use `git commit -m` or a HEREDOC; do not rely on Cursor auto co-author metadata."
)
print(json.dumps({"additional_context": ctx}, ensure_ascii=False))
PY
