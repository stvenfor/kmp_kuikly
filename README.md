# kmp_kuikly

Kuikly / KMP **Demo Skeleton**（教学 + 可 fork 准生产骨架）。

## Stack

| Item | Value |
|---|---|
| Framework | [Kuikly](https://kuikly.tds.qq.com) Compose DSL |
| Package | `com.example.kuikly` |
| Kuikly SDK | `2.16.0`（制品坐标 `2.16.0-2.1.21`） |
| Kotlin | `2.1.21` |
| Maven | `maven-tencent` + `maven-public`（Tencent mirrors） |
| Scaffold | `create-kuikly-app@0.2.7`（Compose template） |

## Modules

| Module | Role |
|---|---|
| `app-shared` | Thin Kuikly entry（KSP `@Page` 总出口；framework pod name 仍为 `shared`） |
| `core-data` / `core-navigation` | Mock + repos；路由 Page 名 |
| `feature-auth` / `feature-feed` | Auth/Feed Feature（页面源在 `src/pages`，由 app-shared 编译） |
| `platform-permission` / `platform-share` | Platform Extension expect/actual |
| `androidApp` / `iosApp` / `ohosApp` / `h5App` | Shell Apps |

## Prerequisites

Follow official [环境搭建](https://kuikly.tds.qq.com/QuickStart/env-setup.html):

- Android Studio + Kotlin / KMP plugins
- Optional: [Kuikly AS plugin](https://kuikly.tds.qq.com/DevGuide/as-plugin.html)（本仓已用 CLI 生成，可不依赖插件新建）
- AS ≥ 2024.2.1 → Gradle JDK **17**
- iOS: Xcode + CocoaPods；Ohos: DevEco Studio + signing

## Quick commands

```bash
# Doctor: tools + simulators / devices
./scripts/run.sh list

# Android emulator (auto-boot Pixel_7_Pro if needed)
./scripts/run.sh android --page Home

# iOS Simulator
./scripts/run.sh ios --page Home --sim "iPhone 16"

# HarmonyOS (hdc + hvigor; falls back to DevEco if link fails)
./scripts/run.sh ohos --open-deveco

# H5 webpack-dev-server
./scripts/run.sh h5 --page Home
```

Low-level Gradle still works:

```bash
./gradlew projects
./gradlew :core-data:testDebugUnitTest :androidApp:assembleDebug
```

Platform details: `docs/runbooks/`.

## Project docs

- **现状分析（对照 KuiklyUI）**: [`docs/architecture/PROJECT-ANALYSIS.md`](docs/architecture/PROJECT-ANALYSIS.md) · [架构图](docs/architecture/kmp-kuikly-architecture.html)
- Glossary: [`CONTEXT.md`](CONTEXT.md)
- Plan: [`docs/SCHEME.md`](docs/SCHEME.md)
- Demo map: [`docs/demo-map.md`](docs/demo-map.md)
- Embed contract: [`docs/embed-contract.md`](docs/embed-contract.md)
- Reality check: [`docs/evidence/REALITY-CHECK.md`](docs/evidence/REALITY-CHECK.md)
- Tickets: [`.scratch/demo-skeleton/issues/`](.scratch/demo-skeleton/issues/)
- Agency roles: [`AGENTS.md`](AGENTS.md)

## Rename package later

Search-replace `com.example.kuikly` across Gradle / source / Pod / Ohos configs when forking.
