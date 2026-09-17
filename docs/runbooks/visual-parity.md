# Visual Parity runbook (Phase-2)

**ADR:** [0018](../adr/0018-cross-source-visual-parity.md)  
**Plan:** [VISUAL-PARITY-PHASE2.md](../architecture/VISUAL-PARITY-PHASE2.md)

## Devices

| Role | Profile |
|------|---------|
| Flutter ref + Kuikly Android | `emulator-5554` (Pixel_7_Pro class) |
| Kuikly iOS / Ohos | existing runbooks; regression self-lock only |

## Mapped pairs (Cross-Source MAP)

`scripts/golden-vs-flutter-diff.sh` diffs Kuikly Android `actual/` vs `goldens/flutter-ref/baseline/` (masked RMSE_rel ≤ `GOLDEN_RMSE_MAX`, default 0.22). MAP is defined in that script:

| Kuikly actual | Flutter ref | S1 RMSE_rel |
|---|---|---|
| `02-main-home` | `02-flutter-main-home` | 0.141 |
| `06-main-me-guest` | `03-flutter-main-me-guest` | 0.146 |
| `07-main-me-logged-in` | `08-flutter-main-me-logged-in` | 0.164 |
| `03-login` | `07-flutter-login` | 0.115 |
| `10-search` | `10-flutter-search` | 0.144 |
| `04-usedcar-list` | `04-flutter-usedcar-list` | 0.073 |
| `11-main-chat` | `11-flutter-main-chat` | 0.080 |
| `13-main-community` | `13-flutter-main-community` | 0.197 |
| `12-chat-detail` | `12-flutter-chat-detail` | 0.091 |

`02b-flutter-main-home-logged-in` is already captured on the Flutter side, but has **no Kuikly actual yet** — add the `02b-main-home-logged-in=02b-flutter-main-home-logged-in` pair to `MAP` when ticket S2 lands.

## Capture caveats (Phase-2)

- **Android cold start ~9s.** Main/Search settle is raised (8s) but a cold-start blank frame is ~30KB and can still be captured. **Size-poll the PNG** (settled Main/Search ≈ >120KB) and recapture rather than trusting the settle sleep — under-settled blanks caused interim RMSE fails on 02/06/10 in S1.
- **Flutter `12-chat-detail` is flaky, never lock it blindly.** `chat_detail_via_tap` cold-starts → Chat tab → taps row 1, gated by `CHAT_DETAIL_MIN_BYTES` (default `100000`; `120000` rejected a valid 119629-byte UI) **plus** a bottom-input-bar check. On failure it falls back to the `chat/detail` deeplink; if that is also under the byte gate or lacks the input bar the shot is **deleted**, and `--update` will not clobber a prior good `12` baseline.
- **Flutter logged-in runs** can hit VIP/SVIP interstitials (`确认开通`) or drop the session (auth gate) mid-capture. The tap path back-navigates / re-logs, but re-check every locked stem before accepting.
- Kuikly avatars stay letter placeholders vs Flutter photos (no image SDK) — accepted ceiling; RMSE stays under max.

## Commands

```bash
# 1) Capture Flutter references
./scripts/flutter-ref-capture.sh --update

# 2) Capture Kuikly Android
./scripts/golden-android-capture.sh

# 3) Cross-source gate (mapped pairs in script)
./scripts/golden-vs-flutter-diff.sh

# 4) iOS / Ohos regression
./scripts/golden-ios-diff.sh
./scripts/golden-ohos-diff.sh
```

## Soft Gate (ADR-0018)

iOS / Ohos are **self-lock only** — no cross-source pairs. Re-lock + verify after UI waves:

```bash
# iOS / Ohos self-lock (re-lock then diff → expect all AE=00)
./scripts/golden-ios-capture.sh  --update && ./scripts/golden-ios-diff.sh
./scripts/golden-ohos-capture.sh --update && ./scripts/golden-ohos-diff.sh

# Android self-baseline after any Phase-2 UI wave, then cross-source
./scripts/golden-android-capture.sh --update && ./scripts/golden-android-diff.sh
./scripts/golden-vs-flutter-diff.sh

# Flutter refs only when mock data / UI source changed
./scripts/flutter-ref-capture.sh --update
```

## Evidence

- [77 — RMSE cross-source hard gate PASS](../evidence/my-ai-migration/parity/77-rmse-gate-pass.md) (8 pairs, pre-`12`)
- [86 — P2 Soft Gate S1 PASS](../evidence/my-ai-migration/parity/86-p2-s1-soft-gate-pass.md) (MAP 9/9, `12-chat-detail` RMSE_rel 0.091, iOS + Ohos self-lock)

## Masks

- Top: `MASK_TOP_PX` default 80 (status bar)
- Bottom: `MASK_BOTTOM_PX` default 40 (home indicator)
- Threshold: `GOLDEN_PIXEL_THRESHOLD` default 50000 — calibrate after first Home pair

## Privacy / login on Flutter

Cold start may show privacy dialog. For stable refs: accept once on device, or document a debug skip when available. Re-lock refs after mock data changes.
