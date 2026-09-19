#!/usr/bin/env bash
# Wrapper so hooks.json can call either preToolUse or beforeShellExecution.
exec python3 "$(cd "$(dirname "$0")" && pwd)/git-commit-policy.py"
