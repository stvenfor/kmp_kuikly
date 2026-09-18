# P7-E1 — Unlock `24-live-detail` (LiveRoom stub flatten)

**Date:** 2026-09-18  
**Verdict:** **PASS** — hard Cross-Source MAP **32 → 33**

## Proven

- Kuikly `LiveDetailPage` flattened to Flutter `LiveRoomPage` stub: NavBar `直播 $roomId` + WS 行 +「发送 join 信令」+ Divider + 信令列表；移除 Phase-1 视频区/房间信息卡。
- Default `id` aligned to Flutter `mock_room`.
- Flutter-ref `24-flutter-live-detail.png` locked via `xiaomao://app/live/room` (75 431 B; content: `直播 mock_room` / `发送 join 信令`).
- Kuikly re-capture `24-live-detail.png` (48 408 B) after rebuild.
- Masked RMSE_rel ≈ **0.097** (≤ 0.22).
- `scripts/golden-vs-flutter-diff.sh` MAP append: `24-live-detail=24-flutter-live-detail`.

## Splash disposition

- Kuikly `01-splash` = 隐私弹窗；Flutter `01-flutter-cold-start` = Home mis-lock — **not MAP-eligible**.
- Mark splash **PASS-out** (platform cold-start / privacy gate; no true Flutter splash golden).

DONE_P7_E1_LIVE_DETAIL
