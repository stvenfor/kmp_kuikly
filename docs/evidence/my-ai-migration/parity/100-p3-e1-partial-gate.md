# P3-E1 partial gate — MAP 10 + Wave3 notes

**Date:** 2026-09-17  
**Verdict:** **PARTIAL PASS**

## RMSE (active MAP)

| Pair | RMSE_rel | Result |
|------|----------|--------|
| Phase-2 9 pairs | all ≤0.22 | PASS |
| `09-login-password` | **0.118** | **PASS** → promote hard MAP |
| `14-post-detail` | **0.254** | FAIL — photo-dense Flutter-ref (~973KB) vs Kuikly ~98KB → **defer** (same ceiling class as `02b`) |

## Flutter Wave3 capture
- First capture locked blanks (~60KB) for 05/08/15/19/25 — baselines removed.
- Kept: `09-flutter-login-password`, `14-flutter-post-detail`.
- Harden ticket E1a2 in flight (force-stop + size-gate + skip-lock).

## Next
- Redispatch E1a2 / E1b if hung.
- Soft-comment `14` from MAP until assets/polish.
- Re-capture Wave3 after harden → unlock remaining pairs.
