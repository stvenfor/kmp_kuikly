# P3-E8 Gate — Cross-Source MAP 18/18

**Verdict: PASS**

Unlocked `17-video-list` after E8a content gate + E8b true Flutter-ref (Home mis-lock fixed).

| Pair | RMSE_rel |
|------|----------|
| 02-main-home | 0.200 |
| 02b-main-home-logged-in | 0.192 |
| 06-main-me-guest | 0.146 |
| 07-main-me-logged-in | 0.164 |
| 03-login | 0.115 |
| 10-search | 0.144 |
| 04-usedcar-list | 0.073 |
| 11-main-chat | 0.080 |
| 13-main-community | 0.197 |
| 12-chat-detail | 0.091 |
| 05-usedcar-detail | 0.149 |
| 08-settings | 0.103 |
| 09-login-password | 0.148 |
| 15-music-list | 0.145 |
| 25-pay-list | 0.159 |
| **17-video-list** | **0.154** |
| 18-video-detail | 0.211 |
| 21-classroom-list | 0.141 |

Log: `.scratch/my-ai-migration/parity/logs/orch-e8-gate-map18.log`  
Threshold: `GOLDEN_RMSE_MAX=0.22`. All 18 pairs OK.
