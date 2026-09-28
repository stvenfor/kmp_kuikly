# P8 Soft Close — in-scope inventory drain

**Date:** 2026-09-18  
**Verdict:** **PASS** (soft)

## Destination

- Hard Cross-Source MAP **35/35** at soft-close ([182](182-p8-e2-gate-map35.md)); stem 39 later unlocked → **36/36** ([184](184-p8-e3-gate-map36.md)).
- Inventory **missing: 0**; **self-lock: 0**.
- Structure queue empty after [184](184-p8-e3-gate-map36.md) (`39-dubbing-home` RMSE_rel **0.214**).
- Out of scope unchanged: bfui / short-video / H5 / live HTTP.

## Evidence chain

- [180](180-p8-e1a-passout-missing.md) PASS-out
- [181](181-p8-e2a-wave5-settle.md) capture harden
- [182](182-p8-e2-gate-map35.md) MAP 35

## Reality Checker note

Teaching-skeleton Cross-Source gate met with reproducible RMSE logs + goldens. Not a production-readiness cert — default NEEDS WORK for “ship to users” claims. For **parity program soft-close**: **PASS**.

DONE_P8_SOFT_CLOSE
