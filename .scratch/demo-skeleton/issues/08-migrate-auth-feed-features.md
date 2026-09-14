# 08 — Migrate Auth + Feed into Feature Modules

**Status:** done

**Blocked by:** 07

- [x] Physical `:feature-auth` / `:feature-feed` Gradle modules
- [x] Auth/Feed `@Page` sources under `feature-*/src/pages` (compiled by `:app-shared` for Kuikly KSP)
- [x] Feature marker objects `FeatureAuth` / `FeatureFeed`

## Comments
Kuikly KSP requires a single entry (`KuiklyCoreEntry`); pages stay owned by feature dirs but compiled into app-shared.
