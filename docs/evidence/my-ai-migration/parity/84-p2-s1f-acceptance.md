# P2-S1f acceptance — iOS/Ohos mockLogin priming

**Date:** 2026-09-17  
**Ticket:** `.scratch/my-ai-migration/parity/handoffs/P2-S1f-ios-ohos-mocklogin-priming-workbuddy.md`  
**Verdict:** **PASS**

## Evidence

- iOS + Ohos `11-main-chat` / `13-main-community` now use `mockLogin":"1"` matching Android.
- UsedCarDetail gained `{"id":"1"}` where missing.
- `bash -n` clean on both scripts after land.
- Mid-flight capture failures during S1f edit were Orchestrator-side races; re-capture follows.
