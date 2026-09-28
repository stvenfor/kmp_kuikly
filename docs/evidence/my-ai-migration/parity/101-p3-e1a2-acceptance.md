# P3-E1a2 acceptance — Flutter Wave3 harden

**Date:** 2026-09-17  
**Verdict:** **PASS**

## Proven
- `wave3_capture` + `WAVE3_MIN_BYTES=100000` + per-stem `WAVE3_OK_*` skip-lock on `--update`.
- Force-stop path via `ensure_main_then_deeplink` retry settle 8s.
- `bash -n` OK. Orchestrator will re-run `CAPTURE_SET=wave3 --update`.
