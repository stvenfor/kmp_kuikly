# P6-E2-GATE — Wave5 MAP unlock 32/32

**Date:** 2026-09-18  
**Verdict:** **PASS** — hard Cross-Source MAP **20 → 32**

## Proven

- Kuikly Android baselines `27–38` locked (prior E2-CAP).
- Flutter-ref baselines `27–38` locked via settle+deeplink manual batch (script splash gate failed; Orchestrator path with ≥120KB settle).
- Stem **35** first lock was Home mis-lock (`settings/deal_invoice/upload` ignored). Relocked: `xiaomao://app/settings/deal_invoice_demo` → FAB「上传成交发票」; UI dump has `上传发票` / `购车客户` / `提交审核`, no Home greeting.
- `GOLDEN_RMSE_MAX=0.22 ./scripts/golden-vs-flutter-diff.sh` — **all mapped pairs OK**, including Wave5:

| Pair | RMSE_rel |
|------|----------|
| 27-all-services | 0.147 |
| 28-register | 0.068 |
| 29-music-now-playing | 0.039 |
| 30-strategy | 0.144 |
| 31-hot-rank-detail | 0.150 |
| 32-personalized-settings | 0.089 |
| 33-learning-report | 0.163 |
| 34-check-in-mall | 0.161 |
| 35-deal-invoice-upload | 0.043 |
| 36-classroom-gift-claim | 0.106 |
| 37-classroom-video-detail | 0.127 |
| 38-community-publish | 0.035 |

- Log: `.scratch/my-ai-migration/parity/logs/orch-p6-e2-gate-rmse.log`
- `scripts/golden-vs-flutter-diff.sh` Wave5 MAP entries uncommented (promoted).

## Not claimed

- bfui / short-video / homework missing / web / H5 still out of scope.
- Automated `CAPTURE_SET=wave5` still unreliable without settle+Home reject; 35 needs demo→FAB path in script follow-up.

DONE_P6_E2_GATE_MAP32
