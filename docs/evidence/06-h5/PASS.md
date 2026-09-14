# H5 smoke — PASS

**Date:** 2026-09-14

## Commands

```bash
./gradlew :app-shared:packLocalJsBundleDebug
./gradlew :h5App:jsBrowserDevelopmentRun
```

(`:shared` renamed to `:app-shared`; pack task alias `packLocalJsBundleDebug` → `packLocalJSBundleDebug`.)

## Evidence

| Artifact | Path |
|---|---|
| Pack log | `docs/evidence/06-h5/packLocalJsBundleDebug.log` |
| Dev-server log | `docs/evidence/06-h5/jsBrowserDevelopmentRun.log` |
| Home screenshot | `docs/evidence/06-h5/06-home.png` |

## Result

- Pack: **BUILD SUCCESSFUL** — `app-shared/build/outputs/kuikly/js/debug/local/nativevue2.zip`
- Server: webpack-dev-server at `http://localhost:8080/`
- Screenshot: `?page_name=Home` shows **Demo Map** with Auth+Feed and other demo entries

## Verdict: **PASS**
