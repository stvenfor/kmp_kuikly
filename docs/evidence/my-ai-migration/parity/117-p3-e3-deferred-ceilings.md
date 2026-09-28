# P3-E3f — Phase-3 deferred ceilings register

**Date:** 2026-09-17
**Verdict:** **REGISTERED** (code-impact: none; effect: unlocks MAP expand beyond these pages)

This file freezes the four Phase-3 ceilings that will **not** block MAP expand of other pages. Each row is the canonical reason subsequent tickets may skip Cross-Source RMSE for that page until the ceiling is explicitly re-opened.

## Deferred ceilings

| Page | Ceiling | Rationale | Re-open trigger |
|------|---------|-----------|------------------|
| `14-post-detail` | photo density | Flutter photo grid vs Kuikly limited common assets; RMSE stayed >0.22 after E2a media block (cite 103) | Provide full photo asset set (≥ current Flutter grid) → recapture pair → RMSE ≤0.22 re-gates |
| `19-friend-list` | Flutter stub | Flutter `FriendPage` is `Center(Text stub)`; Cross-Source MAP is meaningless | Re-source real Flutter friend-list page → recapture pair |
| `08-settings` | in-flight E3a | flatten ListTile; re-gate after recapture (not a hard ceiling yet) | Complete E3a in-flight polish → recapture pair → RMSE gate |
| `15-music-list` | in-flight E3b2 | multi-scroll capture; re-gate after lock | Complete E3b2 Flutter-ref lock (cite 116) → recapture pair → RMSE gate |

## MAP non-blocking

Cross-Source MAP may expand to other pages while these four remain at their ceilings. MAP 13 (cite 112) already locked `25-pay-list`; this register keeps `08-settings` / `15-music-list` / `14-post-detail` / `19-friend-list` out of MAP contention rather than hard-failing them.

## Executor Report
### Status
- [x] Done — evidence file written at `docs/evidence/my-ai-migration/parity/117-p3-e3-deferred-ceilings.md`

### Evidence
- Cited 103 (`P3-E2a acceptance — PostDetail media assets`, RMSE 0.254 > 0.22 → photo density ceiling)
- Cited 112 (`P3-E2 gate — MAP 13`, deferred ceilings already enumerated; this file is the canonical registration)
- Cited 116 (`P3-E3e acceptance — MusicList structure polish`, `15-music-list` waits on E3b2 lock)
- No product code, no scripts, no golden edits — file-lock honored (single new file in the parallel-safe path)
