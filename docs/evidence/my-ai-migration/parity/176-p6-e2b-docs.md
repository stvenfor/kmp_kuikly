# P6-E2b acceptance — INVENTORY + runbook Phase-6 docs

**Date:** 2026-09-18  
**Verdict:** **PASS**

## Proven

- `.scratch/my-ai-migration/parity/INVENTORY.md` refreshed:
  - Twelve Phase-5 polished structure routes reclassified as `structure (P5 source-diff)` with golden stems **27–38**.
  - `loginOtp` row marked PASS-out (E1m — covered by `03-login`; no separate MAP pair).
  - Counts updated: MAP-green **20/20** unchanged; **12** P6 flutter-ref-pending stems; plain `structure` **0**.
  - Explicit note: stems 27–38 are **not MAP-green** until E2-GATE RMSE ≤0.22 after wave5 capture.
- `docs/runbooks/visual-parity.md` adds **Phase-6** section: stem table 27–38, `CAPTURE_SET=wave5` capture flow, RMSE ≤0.22 unlock gate.

## Not claimed

- No MAP pair promotion — hard Cross-Source MAP remains **20/20** ([172](172-p3-e9g-friend-live-unlock.md) / [175](175-p5-soft-close.md)).
- Flutter-ref wave5 capture and E2-GATE RMSE probe deferred to Orchestrator (E2a scripts + E2-CAP + E2-GATE).

## Follow-up

- E2a: `flutter-ref-capture.sh` `CAPTURE_SET=wave5` + android capture stems + commented MAP stubs in `golden-vs-flutter-diff.sh`.
- E2-CAP / E2-GATE: batch capture → per-pair RMSE probe → promote ≤0.22.

DONE_P6_E2B
