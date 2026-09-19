# P8-E3 — dubbing-home polish unlock (MAP 36)

**Date:** 2026-09-18  
**Verdict:** **PASS** — hard MAP **35 → 36**

## Unlocked

| Pair | RMSE_rel | Note |
|------|----------|------|
| 39-dubbing-home | 0.214 | first-viewport structure polish (`最近在学` / `新手赛场` / banner AD + dots); icon PNGs still geometric (`ponytail:` ceiling) |

Official gate: `./scripts/golden-vs-flutter-diff.sh` → `all mapped Cross-Source diffs passed` (36 pairs, max 0.22).

## What changed

- `DubbingHomePage.kt` — Flutter first-viewport order; 热度榜 / 猜你喜欢 below the fold
- Recapture `goldens/android/{actual,baseline}/39-dubbing-home.png` (202018B)
- `scripts/golden-vs-flutter-diff.sh` MAP comment-pending removed

## Not in this gate

- `02b` android **baseline** (208KB) still RMSE≈0.250 vs Flutter; locked **actual** (245KB) is 0.192 and is what the Cross-Source script reads. Baseline refresh is optional, not a MAP blocker.

DONE_P8_E3_GATE_MAP36
