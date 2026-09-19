# P8-E2a acceptance — wave5 settle harden

**Date:** 2026-09-18  
**Verdict:** **PASS**

## Change

`ensure_main_then_deeplink` in `scripts/flutter-ref-capture.sh` now polls Main until screencap >120KB (same as `ui_cold_main`) and dismisses privacy before deeplink — fixes Wave5 splash mis-locks from fixed `sleep 5`.

35 demo→FAB fallback already present (P6).

DONE_P8_E2A
