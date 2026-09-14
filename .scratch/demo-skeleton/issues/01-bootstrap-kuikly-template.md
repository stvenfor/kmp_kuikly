# 01 — Bootstrap official Kuikly template

**What to build:** A syncable Kuikly Project Template in this repo with aligned Kuikly/Kotlin versions and a README that points to env setup and official docs — so later tickets have a real codebase to change.

**Blocked by:** None — can start immediately.

**Agency role:** Mobile App Builder (+ Evidence Collector / Reality Checker)

**Status:** done

- [x] Official Kuikly template (or equivalent scaffold) exists in repo root with Shell Apps for Android / iOS / Ohos / H5 present or clearly stubbed per template defaults
- [x] Kuikly artifact versions aligned across shared + shells; maven-tencent source present for ≥2.5.0
- [x] Package / application id uses `com.example.kuikly` (or documented one-step rename path)
- [x] Root README lists toolchain requirements and links to https://kuikly.tds.qq.com QuickStart
- [x] Evidence: `./gradlew` projects list or sync log attached in Comments

## Comments

### 2026-09-14 — Mobile App Builder + Evidence

- Scaffold: `npx create-kuikly-app@0.2.7 create` Compose DSL, package `com.example.kuikly`, Kuikly `2.16.0` / Kotlin `2.1.21`
- Modules: `shared`, `androidApp`, `iosApp`, `ohosApp`; **`h5App` stubbed** (`h5App/README.md`) — CLI did not emit full H5 sources
- Maven: `maven-tencent` + Aliyun mirrors in `settings.gradle.kts`
- Gradle wrapper mirror: Tencent `gradle-8.5-bin.zip`

**Evidence — `./gradlew projects` (BUILD SUCCESSFUL):**

```
Root project 'kmp_kuikly'
+--- Project ':androidApp'
\--- Project ':shared'
[KuiklyGradle] Using KSP dependency: com.tencent.kuikly-open:core-ksp:2.16.0-2.1.21
```

**Evidence — `./gradlew :androidApp:assembleDebug` (BUILD SUCCESSFUL):** APK under `androidApp/build/outputs/apk/debug/`.

### Reality Checker

- Ticket 01 acceptance: **PASS**
- H5 full module deferred to ticket 02 (explicit stub OK)
- Unblocks: **02 — Four-platform smoke**
