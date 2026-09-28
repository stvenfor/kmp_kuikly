# P3-E2 gate — MAP 13 (pay-list)

**Date:** 2026-09-17  
**Verdict:** **PASS** (hard MAP **13**)

## Cross-Source

| Pair | RMSE_rel | Result |
|------|----------|--------|
| Prior MAP 12 | all ≤0.22 | PASS |
| `25-pay-list` | **0.159** | **PASS** — Flutter-ref via AllServices→会员续费 UI tap (E2e/E2f) |
| `08-settings` | 0.238 | still deferred after E2b2/E2b3 chrome polish |
| `15-music-list` | — | AllServices open OK but `音频列表` label not found |
| `19-friend-list` | — | Flutter stub ceiling |
| `14-post-detail` | — | photo density deferred |

## Evidence
- E2b2/E2b3/E2e/E2f acceptances 108–111
- Flutter `25-flutter-pay-list` baseline ~889KB locked
