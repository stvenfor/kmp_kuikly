# Android goldens

- `baseline/` — locked Per-Platform Goldens (commit after `--update` on Pixel_7_Pro)
- `actual/` — last capture (usually gitignored or overwritten)
- `diff/` — failure artifacts

No baselines committed yet: run `./scripts/golden-android-capture.sh --update` when an emulator is online.
