# P3-E8c Docs — 17-video-list Flutter-ref Home mis-lock ceiling

**Date:** 2026-09-18
**Verdict:** **PASS** (docs) — deferred root cause corrected from “polish” to **Flutter-ref Home mis-lock**.

## Finding (recap from E7f [149](149-p3-e7f-acceptance.md))

`goldens/flutter-ref/baseline/17-flutter-video-list.png` is **not** a Flutter video-list page — it is the Flutter Home tab. Polishing Kuikly `17-video-list` against it would chase an unreachable RMSE target.

## Evidence — file-level

| Path | Size (B) | SHA-256 |
|---|---|---|
| `goldens/flutter-ref/baseline/17-flutter-video-list.png` | **1,004,572** | `d97749b8072cb16b527469f73b858b49eb9dd90fa30136893393145c02722c66` |
| `goldens/flutter-ref/baseline/02b-flutter-main-home-logged-in.png` | 972,686 | `9b8c0b0544a02d89225e99027cdde54af3c20631ba1c19837f4ea9640b7e7971` |
| `goldens/flutter-ref/baseline/_probe-main.png` | **1,005,203** | `29ab63ea923c6ed904397a76c5f94afea523bed5736b18e22895520535bc8637` |

- 17-flutter-video-list size 1,004,572 B is **within 631 B** of `_probe-main.png` (1,005,203 B) — strong home-proximity match by size.
- SHA differs (deeplink arrived at Home from a slightly different scroll/clock than the probe main capture), but the content class is the same Home tab.
- Wave4 capture log [`.scratch/my-ai-migration/parity/logs/orch-wave4-capture-090739.log:3`](../.scratch/my-ai-migration/parity/logs/orch-wave4-capture-090739.log) shows:
  > `Warning: Activity not started, intent has been delivered to currently running top-most instance.`
  — deeplink `xiaomao://app/video/dubbing/videos` was delivered to the already-running Home, so the screencap locked the Home tab.
- `scripts/flutter-ref-capture.sh:444-468` `wave4_capture()` accepts on `wc -c ≥ 100000` only; the Home image is ~1 MB so the size gate passes and the script’s richer `ui_capture_video_list()` fallback (`:758-809`) is skipped (`:1172-1181`).
- Reproduce RMSE 0.259 with `magick compare -metric RMSE goldens/flutter-ref/baseline/17-flutter-video-list.png android/actual/17-video-list.png` — that is Kuikly video-list vs Flutter **Home**, not vs the real video-list.
- Same-shape control: `18-flutter-video-detail.png` 232,106 B and `21-flutter-classroom-list.png` 150,135 B are well under 1 MB — real video / classroom surfaces are not full-tab captures.

## Why this matters for the parity program

- MAP entry `17-video-list=17-flutter-video-list` in `scripts/golden-vs-flutter-diff.sh:43` is correctly **commented out** (“pending wave4 video-list polish”) — 17 is *not* in the hard MAP and never should have been.
- The “RMSE≈0.259 > 0.22” framing is misleading: the threshold is unreachable while the baseline is the wrong page, so Kuikly-side polish work is wasted.
- Same shape as [P3-Gamebook project_flutter_ref_size_gate_blind](../.scratch/my-ai-migration/parity/wb-logs/README.md) — size gate alone cannot distinguish “did not navigate” from “navigated successfully”.

## Follow-ups (already dispatched)

| ID | Item | Status |
|---|---|---|
| **E8a** | Wave4 content gate — reject Home captures for 17 (uiautomator nav-title assertion or template / OCR) | **DISPATCHING** |
| **E8b** | Force UI-tap recapture of true Flutter video-list, lock baseline | **DISPATCHING** (after E8a) |
| **E8d** | Runbook `docs/runbooks/visual-parity.md` deferred row reason updated to Flutter-ref Home mis-lock | **DISPATCHING** ([153](153-p3-e8d-acceptance.md)) |

Hard MAP stays at **17**. `17-video-list` will only be considered for MAP-green **after** a real Flutter video-list baseline is locked and `RMSE_rel ≤ 0.22`.

## Files touched (lock)

- `.scratch/my-ai-migration/parity/INVENTORY.md` — row 69 deferred reason → Flutter-ref Home mis-lock; counts note rewritten.
- `.scratch/my-ai-migration/parity/DISPATCH.md` — E7f wording, E8b → DISPATCHING, hard MAP = 17 unchanged.
- `docs/evidence/my-ai-migration/parity/152-p3-e8-video-list-mislock.md` (this file).