# P2-S1d acceptance — iOS capture settle parity

**Date:** 2026-09-17  
**Ticket:** `.scratch/my-ai-migration/parity/handoffs/P2-S1d-ios-capture-settle-workbuddy.md`  
**Verdict:** **PASS** (script only)

## Evidence

- `scripts/golden-ios-capture.sh` settle bumps for Main/Login/UsedCar/Me/Search/Chat/Community/PostDetail.
- `bash -n` clean per executor; pageData literals unchanged.
- Device re-capture + self-lock owned by Orchestrator S1-GATE.

## Flagged (follow-up, not blocking S1d)

iOS Chat/Community still omit `mockLogin:"1"` vs Android — content priming divergence for later ticket if soft-diff noisy.
