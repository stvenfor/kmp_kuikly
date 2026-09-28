# P2 Soft Gate S1 — Destination close

**Date:** 2026-09-17  
**Verdict:** **PASS**

## Scope (ADR-0018 Soft Gate)

1. Lock Flutter-ref `12-flutter-chat-detail` + MAP pair vs Kuikly Android.  
2. iOS Per-Platform Golden self-lock regression.  
3. Ohos Per-Platform Golden self-lock regression.

## WorkBuddy tickets

| ID | Result | Evidence |
|----|--------|----------|
| S1a | PASS (script harden) | [83](83-p2-s1a-acceptance.md) |
| S1a2 | PASS (gate tune verify) | [85](85-p2-s1a2-acceptance.md) |
| S1b | PASS (chat seed + readStatus) | [79](79-p2-s1b-acceptance.md) |
| S1c | PASS (read-status caption UI) | [80](80-p2-s1c-acceptance.md) |
| S1d | PASS (iOS settle) | [81](81-p2-s1d-acceptance.md) |
| S1e | PASS (Ohos settle) | [82](82-p2-s1e-acceptance.md) |
| S1f | PASS (iOS/Ohos mockLogin priming) | [84](84-p2-s1f-acceptance.md) |

## Orchestrator device gates

| Gate | Result |
|------|--------|
| Flutter-ref `12-flutter-chat-detail` | **PASS** — UI-tap WON, baseline **120058** B (`86-flutter-chat-detail.png`) |
| Kuikly `12-chat-detail` | **PASS** — bubbles + `已读` + input (`86-k-12-chat-detail.png`) |
| Cross-Source MAP **9/9** | **PASS** — `12-chat-detail` RMSE_rel=**0.091** (`86-rmse-s1-gate.log`) |
| iOS self-lock `golden-ios-diff.sh` | **PASS** — all AE=00 after `--update` |
| Ohos self-lock `golden-ohos-diff.sh` | **PASS** — all AE=00 after `--update` (`hdc` `127.0.0.1:5557`) |

### MAP scores (final)

```
02-main-home                 0.141
06-main-me-guest             0.146
07-main-me-logged-in         0.164
03-login                     0.115
10-search                    0.144
04-usedcar-list              0.073
11-main-chat                 0.080
13-main-community            0.197
12-chat-detail               0.091
```

## Ceilings / notes

- Cold-start Kuikly Main can take ~9s before first frame; under-settled blanks (~30KB) caused interim RMSE fails on 02/06/10 — fixed by size-poll recapture; Android capture settle bumped for Main/Search.
- Flutter logged-in set can hit VIP/SVIP interstitial (S1a2 note); chat-detail UI-tap path succeeded this run.
- Avatar images still letter placeholders on Kuikly vs Flutter photo — accepted under no-image-SDK ceiling; RMSE still well under 0.22.

## Destination

**Phase-2 Android Cross-Source + Soft Gate (iOS/Ohos self-lock): PASS.** In-scope sync node closed.
