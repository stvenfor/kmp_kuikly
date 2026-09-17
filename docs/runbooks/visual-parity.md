# Visual Parity runbook (Phase-2)

**ADR:** [0018](../adr/0018-cross-source-visual-parity.md)  
**Plan:** [VISUAL-PARITY-PHASE2.md](../architecture/VISUAL-PARITY-PHASE2.md)

## Devices

| Role | Profile |
|------|---------|
| Flutter ref + Kuikly Android | `emulator-5554` (Pixel_7_Pro class) |
| Kuikly iOS / Ohos | existing runbooks; regression self-lock only |

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

## Masks

- Top: `MASK_TOP_PX` default 80 (status bar)
- Bottom: `MASK_BOTTOM_PX` default 40 (home indicator)
- Threshold: `GOLDEN_PIXEL_THRESHOLD` default 50000 — calibrate after first Home pair

## Privacy / login on Flutter

Cold start may show privacy dialog. For stable refs: accept once on device, or document a debug skip when available. Re-lock refs after mock data changes.
