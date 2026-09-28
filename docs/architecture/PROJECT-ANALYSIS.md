# kmp_kuikly 项目分析文档

> 对照上游 [Tencent-TDS/KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI) 与官方文档 [kuikly.tds.qq.com](https://kuikly.tds.qq.com)，基于仓库**真实代码与证据**整理。  
> 术语见根目录 [`CONTEXT.md`](../../CONTEXT.md)；方案见 [`docs/SCHEME.md`](../SCHEME.md)；硬决策见 [`docs/adr/`](../adr/)。  
> 分析日期：2026-09-15。

---

## 1. 一句话定位

| | 说明 |
|---|---|
| **本仓库** | 基于 KuiklyUI 的 **Demo Skeleton**：可跑四端壳工程 + 准生产模块布局，教学与可 fork 模板，**不是**真实产品后端 |
| **KuiklyUI** | 腾讯 TDS 开源的 KMP 跨端 UI 框架：一套 Kotlin 业务代码 → Android / iOS / HarmonyOS / H5 / 小程序 / macOS 等端渲染 |
| **关系** | 本仓是 **KuiklyUI 的消费者与教学样板**；框架源码不在本仓，通过 Maven `com.tencent.kuikly-open:*` 引入；本地 skills 内嵌了 KuiklyUI 文档参考副本 |

---

## 2. 与 KuiklyUI 的映射

### 2.1 官方分层（KuiklyUI）

```text
业务 Page（ComposeContainer / Pager）
        ↓
Kuikly Core（声明式 UI 树、动画、手势、布局）+ Compose / 自研 DSL
        ↓
Bridge（Module / 适配器）
        ↓
KuiklyRender（Android View / UIKit / ArkUI / DOM …）
```

官方把接入拆成两段（见 QuickStart overview）：

1. **KMP 侧一次接入 KuiklyCore**（写页面、依赖 `core` / `compose` / KSP）
2. **各端分别接入 KuiklyRender**（容器挂载 + 适配器注册）

### 2.2 本仓对应关系

| KuiklyUI 概念 | 本仓落点 |
|---|---|
| KuiklyCore + Compose DSL | `app-shared` / `feature-*` / `core-pager` 依赖 `com.tencent.kuikly-open:core`、`compose`、`core-annotations`、`core-ksp` |
| 自研 DSL（Legacy） | `DslLabPage` → `BasePager`（教学对照，非主栈） |
| KuiklyRender | Shell：`androidApp`（`core-render-android`）、`iosApp`（CocoaPods OpenKuiklyIOSRender）、`ohosApp`、`h5App` |
| `@Page` + KSP 注册 | 主模块 `app-shared`（`isMainModule=true`）；子模块 `feature_auth` / `feature_feed` |
| Module 扩展 | `core-pager` 的 `BridgeModule`；Android `KRBridgeModule` / `KRShareModule` |
| 适配器 | `androidApp/.../adapter/`（Router / Image / Font / Thread / Log / Exception） |
| 动态化 | **v1 不做**；仅 ADR-0003 Dynamicization Seam（模块边界预留） |
| 官方平台广度 | KuiklyUI 含小程序 / macOS 等；本仓 **v1 明确四端**：Android / iOS / 鸿蒙 / H5 |

### 2.3 工程模式归属

对照官方 [跨端工程模式](https://kuikly.tds.qq.com/Introduction/paradigm.html)：

- **页面层**：Compose Track 为主 → 接近「标准模式」的 UI + 控制逻辑
- **数据 / 仓库**：`core-data` 全 Mock，在 common 内 → 带一点「进阶模式」的内置业务逻辑味道
- **平台能力**：`expect`/`actual` Platform Extension，而非直接塞进 Feature → 对齐官方「用 Module / 解耦平台能力」方向
- **交付**：AOT 随包（内置），非动态下发

---

## 3. 技术底座（已落地）

| 项 | 值 | 定义位置 |
|---|---|---|
| 框架 | Kuikly Open | Maven `com.tencent.kuikly-open` |
| Kuikly | `2.16.0` | `buildSrc/.../KotlinBuildVar.kt` |
| Kotlin | `2.1.21` | 同上 |
| 制品坐标 | `2.16.0-2.1.21`（Ohos 变体 `…-2.0.21-ohos`） | `Version.getKuiklyVersion()` |
| 脚手架 | `create-kuikly-app@0.2.7` Compose 模板 | README |
| 包名 | `com.example.kuikly` | 全仓占位 |
| Maven | 腾讯镜像 + 阿里云 + google/central | `settings.gradle.kts` |
| UI 主轨 | Compose Track | ADR-0001 |
| 数据 | 全 Mock | `core-data` |

版本锁定方式：当前用 **buildSrc `Version` 对象**，尚未迁到 SCHEME 提到的 `gradle/libs.versions.toml`（Phase 4 打磨项）。

---

## 4. 逻辑架构（现状）

```text
┌──────────────────────────────────────────────────────────────┐
│ Shell Apps（可替换宿主）                                       │
│  androidApp │ iosApp │ ohosApp │ h5App                        │
│  KuiklyRender 挂载 + 适配器 / Bridge 注册                       │
└────────────────────────────┬─────────────────────────────────┘
                             │ 依赖 Embeddable 入口
┌────────────────────────────▼─────────────────────────────────┐
│ app-shared（薄组装 · 唯一主模块 · KSP Page 总出口）              │
│  Home / Gallery / Perf / DSL / Compose* Demo / Permission/Share │
│  enableMultiModule；subModules = feature_auth & feature_feed   │
└───────┬──────────────────────────┬───────────────────────────┘
        │                          │
┌───────▼──────────┐    ┌──────────▼───────────────────────────┐
│ Feature Modules  │    │ Platform Extension（单模块 expect）     │
│ feature-auth     │    │ platform-permission（android/ios/js）  │
│ feature-feed     │    │ platform-share（android/ios/js）        │
└────────┬─────────┘    └──────────────────────────────────────┘
         │
┌────────▼─────────────────────────────────────────────────────┐
│ core-*                                                         │
│ core-pager      BaseComposePager / BasePager / BridgeModule    │
│ core-navigation PageNames                                      │
│ core-data       Auth/Feed Repository + MockBackend             │
└────────────────────────────────────────────────────────────────┘
         ▲
         │ Maven
┌────────┴────────┐
│ KuiklyUI 制品    │
│ core / compose / render-* / core-ksp / gradle-plugin
└─────────────────┘
```

交互式架构图：[`kmp-kuikly-architecture.html`](./kmp-kuikly-architecture.html)（源：[`kmp-kuikly-architecture.json`](./kmp-kuikly-architecture.json)，Archify showcase）。

### 4.1 依赖规则（已遵守的部分）

1. Shell → `app-shared` → feature / platform / core；Feature **不**依赖 Shell。
2. Feature 之间不互依；跳转用 `PageNames` + `BridgeModule.openPage`。
3. Feature 只碰 platform **API**（expect），不碰各端 Shell 实现细节。
4. Kuikly **主模块唯一**：`app-shared`（`isMainModule=true`）；子模块自带 KSP、`moduleId`。

---

## 5. Gradle 模块清单

`settings.gradle.kts` 当前 include：

| 模块 | 角色 | 备注 |
|---|---|---|
| `:androidApp` | Android Shell | `KuiklyRenderActivity` |
| `:h5App` | H5 Shell | webpack / `nativevue2.js` |
| `:app-shared` | 主业务入口 | CocoaPods framework 名仍为 `shared` |
| `:feature-auth` | 登录 Feature | `moduleId=feature_auth` |
| `:feature-feed` | Feed 列表/详情 | `moduleId=feature_feed` |
| `:core-pager` | 页面基类 | 无 `@Page` |
| `:core-navigation` | 路由名常量 | |
| `:core-data` | Mock + Repository | 含 commonTest |
| `:platform-permission` | 权限 API | expect/actual |
| `:platform-share` | 分享 API | expect/actual |

**不在 settings、但存在于仓库：**

- `iosApp/`、`ohosApp/` — Xcode / DevEco 工程，非 Gradle `include`（或走 `settings.ohos.gradle.kts`）
- `static_server/` — H5 静态调试辅助
- `scripts/run*.sh` — 四端一键启动

### 5.1 Kuikly 多模块配置要点

主模块（`app-shared/build.gradle.kts`）：

```kotlin
ksp {
    arg("moduleId", "app-shared")
    arg("isMainModule", "true")
    arg("subModules", "feature_auth&feature_feed")
    arg("enableMultiModule", "true")
}
```

子模块：`isMainModule=false`，各自 `moduleId`，页面上 `@Page(name=…, moduleId=…)`。  
证据摘要：[`docs/evidence/MODULES-DONE.md`](../evidence/MODULES-DONE.md)。

---

## 6. 页面地图（`@Page`）

| Page 名 | 源码位置 | Track | 说明 |
|---|---|---|---|
| `HelloWorld` | `app-shared/.../HelloWorldPage.kt` | Compose | 默认入口之一 |
| `Home` | `app-shared/.../pages/HomePage.kt` | Compose | Demo Map 导航 |
| `Login` | `feature-auth/.../LoginPage.kt` | Compose | Mock 登录 |
| `FeedList` | `feature-feed/.../FeedListPage.kt` | Compose | 列表 + 场景切换 |
| `FeedDetail` | `feature-feed/.../FeedDetailPage.kt` | Compose | `pageData.id` |
| `DslLab` | `app-shared/.../DslLabPage.kt` | **Legacy DSL** | 对照教学 |
| `PerfLab` | `app-shared/.../PerfLabPage.kt` | Compose | 长列表演示 |
| `Gallery` | `app-shared/.../GalleryPage.kt` | Compose | 色块网格 |
| `ComposeAnim` | `app-shared/.../ComposeAnimPage.kt` | Compose | 动画样例 |
| `ComposeList` | `app-shared/.../ComposeListPage.kt` | Compose | 列表样例 |
| `ComposePager` | `app-shared/.../ComposePagerPage.kt` | Compose | Pager 样例 |
| `PermissionDemo` | `app-shared/.../PermissionDemoPage.kt` | Compose | 调 `PermissionApi` |
| `ShareDemo` | `app-shared/.../ShareDemoPage.kt` | Compose | 调 `ShareApi` |

完整常量：`core-navigation/.../PageNames.kt`。  
人类可读表：[`docs/demo-map.md`](../demo-map.md)（略旧：未列 ComposeAnim/List/Pager，以 `PageNames` 为准）。

**启动示例（Android）：**

```bash
./scripts/run.sh android --page Home
# 或
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName Home
```

---

## 7. 核心子系统详解

### 7.1 `core-pager` — 页面基类

| 类型 | 作用 |
|---|---|
| `BaseComposePager` | 继承 Kuikly `ComposeContainer`，注册 `BridgeModule` |
| `BasePager` | Legacy DSL 基类 |
| `BridgeModule` | 打开页面 / toast 等桥接 |
| `Utils` | 取当前 BridgeModule 等 |

Compose 业务页统一继承 `BaseComposePager`，再 `setContent { … }`。

### 7.2 `core-data` — Mock Backend

| 组件 | 行为 |
|---|---|
| `MockScenario` | `Success` / `Empty` / `Error` / `Slow` |
| `MockBackend` | 全局场景 + `cycle()` |
| `AuthSession` / `FakeAuthRepository` | 内存登录态 |
| `FakeFeedRepository` | 按场景返回列表 / 空 / 失败 |

单测：`core-data/src/commonTest/.../MockAuthFeedTest.kt`（登录、Success/Empty/Error、cycle）。

### 7.3 Platform Extension

```kotlin
// common
expect object PermissionApi { fun checkCamera(): String }
```

| 端 | permission | share |
|---|---|---|
| androidMain | mock `"android:granted(mock)"` | 有 |
| iosMain | mock | 有 |
| jsMain | mock | 有 |
| ohos | **无独立 actual** | **无**（鸿蒙若编此 API 需补） |

与 SCHEME 设想的「每能力拆 `platform-*-android` 独立 Gradle 工程」不同：本仓用 **单模块多 sourceSet expect/actual**（更短路径，仍满足 ADR-0002「有 Platform Extension 边界」）。

### 7.4 Shell：Android 挂载路径（已验证主路径）

`KuiklyRenderActivity`：

1. 读 Intent `pageName`（默认 `HelloWorld`）
2. `KuiklyRenderViewBaseDelegator.onAttach(container, "", pageName, pageData)`
3. 导出侧注册 `KRBridgeModule` / `KRShareModule` 与各类 `KR*Adapter`

其它端职责见 `docs/runbooks/{android,ios,ohos,h5}.md`。

---

## 8. 相对 SCHEME 的完成度

| Phase | SCHEME 目标 | 现状 |
|---|---|---|
| 0 模板可跑 | 官方模板 + README | **完成**（CLI 生成 + 版本对齐） |
| 1 竖切 | Auth+Feed 四端 + common 测 + Android CI | **Android PASS**；shared 页码就绪；iOS/Ohos 证据 blocker；H5 smoke PASS |
| 2 丰满示例 | home 多 Tab、gallery、perf、dsl-lab、core-design | **页面已在 app-shared**；**未**拆 `feature-home` 等独立 Gradle；**无** `core-design`；Home 是 Demo Map 列表而非底部多 Tab |
| 3 Platform Extension | permission/share + 四端 impl | **API + 三端 actual + Demo 页**；非拆分子工程；Ohos actual 缺口 |
| 4 打磨 | embed 契约、版本目录、架构图、动态化文档 | embed + demo-map + 本分析文档；`libs.versions.toml` 未锁；动态化仅 seam |

### 与 SCHEME「终态目录」差异（有意/未做）

| SCHEME 设想 | 现状 |
|---|---|
| `feature-home` / `gallery` / `perf` / `dsl-lab` 物理模块 | 演示页落在 `app-shared`（包级缝，符合 Ponytail「可先不物理拆」） |
| `core-design` | 缺失 |
| `platform-permission-android` 等四分模块 | 合并为单模块 expect/actual |
| 微信小程序 | **明确不做 v1**（KuiklyUI 支持，本仓 scope 外） |

---

## 9. ADR 摘要

| ADR | 决策 |
|---|---|
| [0001](../adr/0001-compose-primary-with-legacy-dsl-track.md) | Compose 主轨；Legacy DSL 仅对照 |
| [0002](../adr/0002-hybrid-feature-and-platform-extension-modules.md) | Feature + Platform Extension 混合，拒绝巨型 shared |
| [0003](../adr/0003-aot-v1-with-dynamicization-seam.md) | v1 AOT；动态化只留边界 |
| [0004](../adr/0004-ponytail-with-architecture-overrides.md) | Ponytail 管代码量；不能 YAGNI 掉 ADR/SCHEME 范围 |

---

## 10. CI / 测试 / 证据

### CI（`.github/workflows/ci.yml`）

- 触发：PR + `main`/`master`
- JDK 17
- `./gradlew :core-data:testDebugUnitTest :app-shared:testDebugUnitTest`
- `./gradlew :androidApp:assembleDebug`
- **不含** iOS / Ohos / H5 云端构建（与 SCHEME 一致）

### Reality Check（[`docs/evidence/REALITY-CHECK.md`](../evidence/REALITY-CHECK.md)）

**总评：NEEDS WORK**（教学骨架，非生产认证）

| 项 | 状态 |
|---|---|
| Android Auth+Feed 竖切 | PASS（含截图证据目录） |
| common 单测 | PASS（Mock 5 用例量级） |
| CI 文件 | PASS |
| iOS 视觉证据 | BLOCKER（`pod install` 拉 KuiklyUI tag 网络失败） |
| Ohos 视觉证据 | BLOCKER（缺 `libshared.so` 链接管线） |
| H5 smoke | PASS |
| 多模块拆分 | 已落地（MODULES-DONE，晚于初版 Reality Check 中「NEEDS WORK」表述） |

---

## 11. 嵌入外部宿主契约

详见 [`docs/embed-contract.md`](../embed-contract.md)。摘要：

1. 依赖 `:app-shared`（或日后发布的 AAR / framework / JS bundle）
2. 按官方文档接对应端 **KuiklyRender**
3. 注册适配器与 Bridge Module
4. 用 `PageNames` 打开页面  
**不要**把业务 UI 写进 Shell，也**不要**让 Feature 依赖 `platform-*-android` 式实现模块。

---

## 12. 与 KuiklyUI 仓库本身的边界

| | KuiklyUI（上游） | kmp_kuikly（本仓） |
|---|---|---|
| 内容 | Core、Compose、各端 Render、demo、文档、小程序等 | 业务 Demo Skeleton + Shell |
| 消费方式 | 源码 / Maven 发布 | **只消费 Maven 制品** |
| 文档副本 | 官方站点 + 仓库 `docs/` | `.agents/skills/.../references/KuiklyUI/docs/`（给 Agent 用，非运行时依赖） |
| 目标读者 | 框架使用者与贡献者 | 想 fork Kuikly 工程骨架的业务/教学团队 |

升级 Kuikly：改 `KotlinBuildVar.kt` 中 `KUIKLY_VERSION` / `KOTLIN_VERSION`，对照官方 [版本配置](https://kuikly.tds.qq.com/DevGuide/version_skills.html)，并同步 Ohos 坐标与 iOS Pod tag。

---

## 13. 推荐阅读顺序

1. [`CONTEXT.md`](../../CONTEXT.md) — 术语  
2. 本文 — 现状总览  
3. [`docs/SCHEME.md`](../SCHEME.md) — 目标架构与阶段  
4. [`docs/demo-map.md`](../demo-map.md) + `PageNames` — 怎么点进各页  
5. [`docs/embed-contract.md`](../embed-contract.md) — 嵌宿主  
6. 官方：[架构](https://kuikly.tds.qq.com/Introduction/arch.html) → [KMP 接入](https://kuikly.tds.qq.com/QuickStart/common.html) → [Compose FAQ](https://kuikly.tds.qq.com/Compose/faq.html)  
7. 工单：`.scratch/demo-skeleton/issues/`（01–15）

---

## 14. 后续可选项（非承诺）

按优先级（教学价值 / 证据缺口）：

1. 补齐 **iOS Pod / Ohos so** 本地证据，刷新 Reality Check  
2. Home **底部多 Tab**（SCHEME Phase 2），或明确「Demo Map 列表即 v1 IA」并改 SCHEME  
3. `platform-*` **Ohos actual**  
4. 可选：把 Gallery/Perf/DSL 升格为独立 Feature 模块（仅当教学需要「物理模块」故事）  
5. `libs.versions.toml` 锁版本；架构 HTML 交付物（由 `kmp-kuikly-architecture.json` 生成）

---

## 附录 A — 关键路径速查

```text
buildSrc/src/main/java/KotlinBuildVar.kt          # 版本
settings.gradle.kts                               # 模块图
app-shared/build.gradle.kts                       # 主模块 + multi-module KSP
core-pager/.../BaseComposePager.kt
core-navigation/.../PageNames.kt
core-data/.../mock/MockBackend.kt
feature-auth/.../pages/LoginPage.kt
feature-feed/.../pages/FeedListPage.kt
androidApp/.../KuiklyRenderActivity.kt
.github/workflows/ci.yml
docs/SCHEME.md / docs/adr/* / docs/evidence/*
```

## 附录 B — 运行命令

```bash
./scripts/run.sh list
./scripts/run.sh android --page Home
./scripts/run.sh ios --page Home --sim "iPhone 16"
./scripts/run.sh ohos --open-deveco
./scripts/run.sh h5 --page Home
./gradlew :core-data:testDebugUnitTest :androidApp:assembleDebug
```
