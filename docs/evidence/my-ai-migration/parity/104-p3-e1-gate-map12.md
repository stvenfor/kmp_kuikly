# P3-E1 gate — MAP expand + 02b promote

**Date:** 2026-09-17  
**Verdict:** **PASS** (hard MAP **12**)

## Cross-Source RMSE

| Pair | RMSE_rel | Notes |
|------|----------|--------|
| Phase-2 original 9 | all ≤0.22 | still green; home guest rose to ~0.200 after assets |
| `05-usedcar-detail` | ~0.151 | Wave3 Flutter-ref after harden |
| `09-login-password` | ~0.148 | Wave3 |
| `02b-main-home-logged-in` | **0.192** | promoted after E1b home photo assets (was ~0.250) |
| `08-settings` | 0.243 | deferred — E2b polish in flight |
| `14-post-detail` | 0.345 | deferred — E2a assets insufficient vs Flutter photo density |
| `15/19/25` | — | Flutter-ref capture still unreliable (E1a3) |

## Evidence chain
- E1a/E1a2/E1b/E1c/E1d/E1e/E1f/E2a acceptances 95–103
- Flutter wave3 harden recapture locked 05/08/09/14 refs
