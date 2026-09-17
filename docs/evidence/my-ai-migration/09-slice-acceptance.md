# First Product Vertical Slice — acceptance evidence

**Date:** 2026-09-16  
**Spec:** `.scratch/my-ai-migration/spec.md`  
**ADR:** `docs/adr/0005-my-ai-project-migration-vertical-slices.md`

## Verdict: PASS

Android + iOS + HarmonyOS Per-Platform Golden gates are green for the closed checklist.

## Proven

| Item | Evidence |
|------|----------|
| Default launch → Splash | All three platforms |
| Mock + OTP + used-car | `:core-data:testDebugUnitTest` SUCCESS |
| Compose product path | Splash privacy, Main shell, Login, UsedCar list/detail |
| Android goldens | `Pixel_7_Pro` → `goldens/android/baseline/` + diff OK |
| iOS goldens | `iPhone 16` → `goldens/ios/baseline/` + diff OK |
| Ohos goldens | `127.0.0.1:5557` Connected → `goldens/ohos/baseline/` + diff OK |
| Backend Seam | Fake only; no live `my_go_study` |

## Ohos notes

- Linked `libshared.so` via `-c settings.ohos.gradle.kts :app-shared:linkDebugSharedOhosArm64`
- HAP: `entry-default-unsigned.hap` (emulator accepted unsigned install)
- `Want --ps pageName` wired through EntryAbility AppStorage → Index

## Out of scope (affirm)

H5 gate, live backend, Music, Chat/Community/Me business, full Auth, full Home, Flutter pixel parity, three-platform CI screenshot farm.
