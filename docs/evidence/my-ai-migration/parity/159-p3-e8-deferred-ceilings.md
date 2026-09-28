# P3-E8h — Phase-3 deferred ceilings close

**Date:** 2026-09-18
**Verdict:** **CEILINGS REGISTERED** — Phase-3 hard MAP **18/18 PASS** via [158](158-p3-e8-gate-map18.md); four deferred ceilings below are non-blocking for soft-close.

This file closes the deferred-ceilings register opened in [117](117-p3-e3-deferred-ceilings.md). The two in-flight entries from E3f (`08-settings`, `15-music-list`) have since MAP-gated and drop out of the deferred table. The four remaining Phase-3 ceilings — `14-post-detail`, `19-friend-list`, `23-live-list`, `26-pay-confirm` — are frozen as non-blocking for Phase-3 expand destination soft-close.

## Deferred ceilings (Phase-3 frozen)

| Page | Ceiling | Rationale | Re-open trigger |
|------|---------|-----------|------------------|
| `14-post-detail` | photo density | Flutter photo grid vs Kuikly limited common assets; RMSE stayed >0.22 after E2a media block (cite [117](117-p3-e3-deferred-ceilings.md), also [103](103-p3-e2a-acceptance.md)) | Provide full photo asset set (≥ current Flutter grid) → recapture pair → RMSE ≤0.22 re-gates |
| `19-friend-list` | Flutter stub | Flutter `FriendPage` is `Center(Text stub)`; Cross-Source MAP is meaningless ([117](117-p3-e3-deferred-ceilings.md)) | Re-source real Flutter friend-list page → recapture pair |
| `23-live-list` | Flutter-ref UNRELIABLE | No Home 直播 tile in the Flutter-ref Wave4 capture set; the baseline cannot represent a livestream list surface (cite [148](148-p3-e7-gate-map17.md) deferred row; also [19](../my-ai-migration/19-slice-11-acceptance.md)) | Add a Home 直播 tile or recapture a real Flutter live-list page → re-source baseline → recapture pair |
| `26-pay-confirm` | Flutter surface ≠ Kuikly `PayConfirm` | Flutter-ref nearest surface is the membership page, not a pay-confirm flow; one-to-one RMSE is structurally unmatched (cite [148](148-p3-e7-gate-map17.md) deferred row; also [20](../my-ai-migration/20-slice-12-acceptance.md)) | Re-source a Flutter pay-confirm-equivalent surface (or accept membership-surface as the Cross-Source stand-in) → recapture pair |

## MAP 18/18 PASS

Hard MAP = 18. All 18 pairs landed at RMSE_rel ≤ 0.22 (gate `GOLDEN_RMSE_MAX=0.22`), per [158](158-p3-e8-gate-map18.md):

- `02-main-home` 0.200 / `02b-main-home-logged-in` 0.192 / `03-login` 0.115 / `04-usedcar-list` 0.073
- `05-usedcar-detail` 0.149 / `06-main-me-guest` 0.146 / `07-main-me-logged-in` 0.164 / `08-settings` 0.103
- `09-login-password` 0.148 / `10-search` 0.144 / `11-main-chat` 0.080 / `12-chat-detail` 0.091
- `13-main-community` 0.197 / `15-music-list` 0.145 / `17-video-list` 0.154 / `18-video-detail` 0.211
- `21-classroom-list` 0.141 / `25-pay-list` 0.159

Threshold `GOLDEN_RMSE_MAX=0.22` (`.scratch/my-ai-migration/parity/logs/orch-e8-gate-map18.log`). The four deferred ceilings above are carved out of the MAP claim; they neither gate nor block the 18/18 result.

## MAP non-blocking

Cross-Source MAP is locked at 18 with `17-video-list` finally inside (Home-mislock ceiling cleared by E8a/E8b/E8c/E8d; cite [152](152-p3-e8-video-list-mislock.md), [154](154-p3-e8a-acceptance.md), [156](156-p3-e8b-acceptance.md), [155](155-p3-e8c-acceptance.md), [153](153-p3-e8d-acceptance.md)). The four Phase-3 ceilings remain at their rows above and may be re-opened by future tickets independently of MAP 18.

## Executor Report
### Status
- [x] Done — evidence file written at `docs/evidence/my-ai-migration/parity/159-p3-e8-deferred-ceilings.md`

### Evidence
- Cited [158](158-p3-e8-gate-map18.md) — MAP 18/18 PASS, RMSE 0.154 on 17-video-list (gate ≤0.22)
- Cited [117](117-p3-e3-deferred-ceilings.md) — earlier register; superseded by this file for Phase-3, retains 14 / 19 rows verbatim
- Cited [148](148-p3-e7-gate-map17.md) — original source for the 23 / 26 deferred rows carried into MAP 18
- Cited [152](152-p3-e8-video-list-mislock.md) + E8a/E8b/E8c/E8d acceptances — closes the 17 mis-lock chapter; not a ceiling itself
- Cited [103](103-p3-e2a-acceptance.md), [19](../my-ai-migration/19-slice-11-acceptance.md), [20](../my-ai-migration/20-slice-12-acceptance.md) — surface-source context for 14 / 23 / 26
- File-lock honored: single new file under `docs/evidence/my-ai-migration/parity/`; no product code, no scripts, no golden edits, no runbook change

DONE_P3_E8H
