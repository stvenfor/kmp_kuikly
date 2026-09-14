# MODULES-DONE

**Date:** 2026-09-14  
**Tickets:** 07–09 physical expand → migrate → contract

## Layout

| Gradle project | Role |
|---|---|
| `:core-navigation` | `PageNames` |
| `:core-data` | MockBackend + Auth/Feed repos + unit tests |
| `:platform-permission` | PermissionApi expect/actual |
| `:platform-share` | ShareApi expect/actual |
| `:feature-auth` | Feature seam + `src/pages` Login |
| `:feature-feed` | Feature seam + `src/pages` FeedList/Detail |
| `:app-shared` | Thin Kuikly entry (KSP / Compose / cocoapods) |

## Kuikly note

`@Page` sources under `feature-*/src/pages/kotlin` are **source-linked** into `:app-shared` so a single `KuiklyCoreEntry` is generated. Feature modules still exist as physical Gradle projects with `api` deps on cores.

## Verify

```bash
./gradlew :core-data:testDebugUnitTest :androidApp:assembleDebug
# BUILD SUCCESSFUL (2026-09-14)
```

CocoaPods pod path: `iosApp/Podfile` → `../app-shared` (framework baseName still `shared`).
