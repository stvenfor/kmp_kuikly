# P3-E1f acceptance — MAP Wave3 pairs

**Date:** 2026-09-17  
**Verdict:** **PASS** (script)

## Proven
- `golden-vs-flutter-diff.sh` MAP appends 7 Wave3 pairs (05/08/09/14/15/19/25). `02b` not added.
- `bash -n` OK.

## Orchestrator follow-up
- First `CAPTURE_SET=wave3 --update` locked several ~60KB blanks (05/08/15/19/25). Those baselines **removed**; re-capture + harden required before RMSE gate counts them.
- Keep 09 (~125KB) and 14 (~973KB) for now; verify visually before hard-gate.
