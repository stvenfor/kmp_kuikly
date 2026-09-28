# P3-E1a3 acceptance — music/friend/pay UI-tap fallback

**Date:** 2026-09-17  
**Verdict:** **PASS** (script)

## Proven
- `ui_capture_music_list` / `ui_capture_pay_list` via AllServices after deeplink size-gate fail.
- `19-friend-list`: documented UNRELIABLE — Flutter `FriendPage` is a stub with no UI entry.
- `bash -n` OK. Orchestrator will re-run `CAPTURE_SET=wave3 --update` for 15/25.

## Ceiling
- Friend list stays out of Cross-Source MAP until Flutter source grows a real list (or explicit exception).
