# Ohos progress — 2026-09-16

## Done
- `settings.ohos.gradle.kts` now points all modules at `build.ohos.gradle.kts` (including core-data / platform-*)
- Ohos `actual` for PermissionApi / ShareApi
- `:app-shared:linkDebugSharedOhosArm64` → `app-shared/build/bin/ohosArm64/sharedDebugShared/libshared.so` (+ `libshared_api.h`)
- Copied into `ohosApp/entry/libs/arm64-v8a/` and `cpp/thirdparty/biz_entry/`
- `hvigor assembleHap` **SUCCESS** → `entry/build/default/outputs/default/entry-default-unsigned.hap`

## Blocked
- `hdc list targets` → `[Empty]` (no Connected device on this Mac CLI)
- HAP is **unsigned** (`SignHap` skipped) — install may need DevEco Signing Configs

## Next (human)
1. Phone: enable USB debugging + Security settings; unlock; tap Allow for this computer
2. Confirm: `hdc list targets -v` shows `USB Connected` (not Offline/Empty)
3. In DevEco: configure Signing Configs, Run `entry` once (produces signed HAP) **or** sign then:
   `./scripts/run-ohos.sh`
4. `./scripts/golden-ohos-capture.sh --update && ./scripts/golden-ohos-diff.sh`
