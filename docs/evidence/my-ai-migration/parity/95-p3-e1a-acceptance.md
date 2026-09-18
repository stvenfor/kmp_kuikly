# P3-E1a acceptance — Flutter-ref Wave3 capture expand

**Date:** 2026-09-17  
**Verdict:** **PASS**

## Proven
- `scripts/flutter-ref-capture.sh`: `CAPTURE_SET=wave3` selects new block; `all` still includes wave1+wave2+loggedin+wave3.
- New stems: `05/08/09/14/15/19/25-flutter-*` (usedcar-detail, settings, login-password, post-detail, music, friend, pay).
- `bash -n` OK. No baseline lock (Orchestrator owns device capture).

## Notes
- 09/05/14 may need visual verify before lock (deeplink / tap geometry risks documented in handoff).
- Runbook temporarily misstated `wave3≈loggedin`; Orchestrator correcting to match E1a.
