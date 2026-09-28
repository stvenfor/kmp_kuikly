# P3-E9g — Unlock `19-friend-list` / `23-live-list` (stub flatten MAP)

**Verdict: PASS** — Cross-Source MAP **20/20** (was 18/18).

## What landed

| Stem | Flutter-ref | Kuikly actual/baseline | RMSE_rel |
|------|-------------|------------------------|----------|
| `19-friend-list` | `19-flutter-friend-list.png` **55 390 B** (title `好友` + `Friend 模块`) | stub flatten (E9b) **32 842 B** | **0.032** |
| `23-live-list` | `23-flutter-live-list.png` **56 946 B** (title `直播` + `进入 Mock 直播房`) | stub flatten (E9c) **34 270 B** | **0.032** |

Gate log: `.scratch/my-ai-migration/parity/logs/orch-e9g-gate-map20.log` — `all mapped Cross-Source diffs passed`.

## Capture notes (Orchestrator)

1. Flutter package missing → reinstall `app-debug.apk`; privacy dialog blocked UI → color-locate `同意并继续` @ (1108,1774) on 1440×3120.
2. Splash hang ~10–12s after cold start; settle poll until shot ≥120KB then deeplink.
3. Stub PNGs compress to ~32–57KB (uniform gray) — Wave3/4 `*_MIN_BYTES=100000` rejected true stubs → **`STUB_MIN_BYTES` (default 40000) + content gate** override in `flutter-ref-capture.sh`; Android capture lowers gate for FriendList/LiveList.
4. Collateral: working-tree `08-flutter-settings.png` had been re-locked as **1.7MB Home mis-lock**; restored HEAD true Settings (230426 B, RMSE 0.108) before MAP20.

## Script / MAP

- Uncommented MAP pairs in `scripts/golden-vs-flutter-diff.sh`.
- Deferred rows for 19/23 cleared (see runbook + INVENTORY).
- Prior: E9b/c flatten [166]/[167](167-p3-e9c-acceptance.md); E9d content gates [170](170-p3-e9d-acceptance.md).

DONE_P3_E9G
