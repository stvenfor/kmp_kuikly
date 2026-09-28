# 08 — Migrate Auth + Feed into Feature Modules

**Status:** done

**Blocked by:** 07

- [x] Physical `:feature-auth` / `:feature-feed` Gradle modules
- [x] Official Kuikly multi-module KSP (`enableMultiModule`, `moduleId=feature_auth|feature_feed`)
- [x] `@Page(name=…, moduleId=…)` in feature `commonMain` (no srcDir hang)
- [x] `core-pager` base for BridgeModule / BaseComposePager
- [x] `:androidApp:assembleDebug` green after migrate

## Comments
Gradle path keeps hyphens (`:feature-auth`); KSP `moduleId` uses underscores (`feature_auth`) — hyphens break generated Kotlin identifiers.
