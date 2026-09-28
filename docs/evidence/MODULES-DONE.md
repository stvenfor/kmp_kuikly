# MODULES-DONE

**Date:** 2026-09-15  
**Tickets:** 07–09 + Kuikly `enableMultiModule`

## Confirmed names

| Role | Gradle / moduleId |
|---|---|
| Main | `app-shared` |
| Sub | `feature_auth`, `feature_feed` |
| Base for pages | `core-pager` (BridgeModule / BaseComposePager) |

## Layout

| Gradle project | Role |
|---|---|
| `:core-pager` | Shared pager base (no `@Page`) |
| `:core-navigation` / `:core-data` | Routing names + Mock repos |
| `:feature_auth` / `:feature_feed` | Kuikly submodules (`isMainModule=false`, own KSP) |
| `:platform-permission` / `:platform-share` | expect/actual APIs |
| `:app-shared` | Main module (`enableMultiModule`, `subModules=feature_auth&feature_feed`) |

## Kuikly multi-module

- Removed `kotlin.srcDir` page hanging.
- `@Page(name=…, moduleId=…)` lives in feature `commonMain`.
- Ohos: matching `build.ohos.gradle.kts` on app-shared / features / core-pager (core-data Android plugin under ohos settings still a known gap).

## Verify

```bash
./gradlew :core-data:testDebugUnitTest :androidApp:assembleDebug
```
