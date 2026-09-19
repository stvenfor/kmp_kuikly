# P2 RMSE gate — interim (pre-R1)

**Date:** 2026-09-17  
**Verdict:** **7/8 PASS** — Community remaining

## Orchestrator work
- Flutter logged-in SMS capture fixed (`adb input text` drops leading `1` → keyevents).
- Flutter refs locked: Chat / Community / Mine logged-in / UsedCar empty / Search / Login.
- Kuikly `pageData` JSON via `adb shell am start` fixed (comma split → VIEW `mockLogin:` crash).
- Capture settle raised; Search needs ≥4s.

## Gate3 (`golden-vs-flutter-diff.sh`)

| Pair | RMSE_rel | Result |
|------|----------|--------|
| 02-main-home | 0.141 | OK |
| 06-main-me-guest | 0.146 | OK |
| 07-main-me-logged-in | 0.164 | OK |
| 03-login | 0.089 | OK |
| 10-search | 0.181 | OK |
| 04-usedcar-list | 0.155 | OK |
| 11-main-chat | 0.080 | OK |
| 13-main-community | 0.297 | FAIL |

## Follow-up
WorkBuddy R1a/R1b/R1c dispatched for Community RMSE + seed + ChatDetail.
