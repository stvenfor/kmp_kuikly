# P3-E3 gate — MAP 14 (music-list)

**Date:** 2026-09-17  
**Verdict:** **PASS** (hard MAP **14**)

## Cross-Source

| Pair | RMSE_rel | Result |
|------|----------|--------|
| Prior MAP 13 | all ≤0.22 | PASS |
| `15-music-list` | **0.145** | **PASS** — Flutter-ref via AllServices→音频列表 multi-scroll (E3b2); Kuikly E3e 72dp row |
| `08-settings` | 0.237 | still deferred after E3a flatten (was ≈0.238) |
| `14-post-detail` | — | photo density ceiling ([117](117-p3-e3-deferred-ceilings.md)) |
| `19-friend-list` | — | Flutter stub ceiling |

## Evidence
- E3a/c/d/e/b2/f/g2/h acceptances 113–121
- Flutter `15-flutter-music-list` baseline locked 213944B (UI-tap scroll pass 3)
- Full gate: `golden-vs-flutter-diff.sh` → all mapped diffs passed
