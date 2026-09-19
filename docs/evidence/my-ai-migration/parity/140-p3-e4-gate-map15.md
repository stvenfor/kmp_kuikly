# P3-E4/E6 gate — MAP 15 (settings unlocked)

**Date:** 2026-09-17  
**Verdict:** **PASS** (hard MAP **15**)

## Root cause (settings)
Prior Flutter baseline `08-flutter-settings.png` (459KB) was a **Home** frame, not Settings — Cross-Source RMSE stuck ≈0.237 against Kuikly Settings. Reinstall wiped the Flutter package; after privacy dismiss + guest Mine scroll →「设置」, real Settings ref locked (230426B).

## Cross-Source

| Pair | RMSE_rel | Result |
|------|----------|--------|
| Prior MAP 14 | all ≤0.22 | PASS |
| `08-settings` | **0.103** | **PASS** — true Flutter Settings ref + E3a/E4e2 flatten |
| `14-post-detail` | — | photo density ceiling |
| `19-friend-list` | — | Flutter stub ceiling |

## Evidence
- Kuikly actual `08-settings` 200340B (post E4e2)
- Flutter baseline re-locked 230426B (Mine UI-tap)
- Full `golden-vs-flutter-diff.sh` → all mapped diffs passed
