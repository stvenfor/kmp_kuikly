# P2 RMSE Cross-Source hard gate — PASS

**Date:** 2026-09-17  
**Verdict:** **PASS** — all mapped Android ↔ Flutter-ref pairs ≤ `GOLDEN_RMSE_MAX` (0.22)

## Gate4 results

| Kuikly actual | Flutter ref | RMSE_rel | Result |
|---------------|-------------|----------|--------|
| 02-main-home | 02-flutter-main-home | 0.141 | OK |
| 06-main-me-guest | 03-flutter-main-me-guest | 0.146 | OK |
| 07-main-me-logged-in | 08-flutter-main-me-logged-in | 0.164 | OK |
| 03-login | 07-flutter-login | 0.089 | OK |
| 10-search | 10-flutter-search | 0.181 | OK |
| 04-usedcar-list | 04-flutter-usedcar-list | 0.155 | OK |
| 11-main-chat | 11-flutter-main-chat | 0.080 | OK |
| 13-main-community | 13-flutter-main-community | 0.197 | OK |

Community improved 0.297 → 0.197 after R1a/R1b.

## Orchestrator infra fixed this wave
- Flutter SMS login automation (keyevent digits; `adb input text` drops leading `1`).
- Kuikly `pageData` JSON via remote-quoted `am start` (comma split → VIEW/`JSONException`).
- Capture settle / Search ≥4s.

## Deferred / ceilings (not blocking this gate)
- `12-chat-detail` Flutter-ref still flaky (deeplink) — R1c source polish landed; MAP pair deferred.
- Community/Home media = color-block placeholders (no image/video SDK).
- UsedCar Flutter ref = empty state「暂无交易记录」under live backend.
- iOS / Ohos = soft regression self-lock only (ADR-0018).
- bfui / short-video SDK / dubbing_feed / live HTTP = out of scope.

## Evidence
- Diffs: `goldens/flutter-ref/diff/`
- Log: `.scratch/my-ai-migration/parity/logs/orch-rmse-gate4.out`
- R1 accepts: [74](74-p2-r1b-acceptance.md) / [75](75-p2-r1c-acceptance.md) / [76](76-p2-r1a-acceptance.md)
