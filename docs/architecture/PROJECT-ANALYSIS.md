# kmp_kuikly 项目分析文档

> 对照上游 [Tencent-TDS/KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI) 与官方文档 [kuikly.tds.qq.com](https://kuikly.tds.qq.com)，基于仓库**真实代码与证据**整理。  
> 术语见根目录 [`CONTEXT.md`](../../CONTEXT.md)；方案见 [`docs/SCHEME.md`](../SCHEME.md)；硬决策见 [`docs/adr/`](../adr/)。  
> 分析日期：2026-09-15（原始结构分析）；2026-10-19（模块清单 / 设计 token 单源 / 页面编译缝 重构更新）。

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
| KuiklyCore + Compose DSL | `app-shared` / `feature-*` / `core-*` 依赖 `com.tencent.kuikly-open:core`、`compose`、`core-annotations`、`core-ksp` |
| 自研 DSL（Legacy） | `DslLabPage` → `BasePager`（`app-shared/base/`，教学对照，非主栈） |
| KuiklyRender | Shell：`androidApp`（`core-render-android`）、`iosApp`（CocoaPods OpenKuiklyIOSRender）、`ohosApp`、`h5App` |
| `@Page` + KSP 注册 | **主模块唯一**：`app-shared`（`isMainModule=true`）；16 个 `feature-*` 作为子模块，页面源经 `app-shared` 的 `kotlin.srcDir` 缝并入主模块编译（见 §4.3） |
| Module 扩展 | `app-shared/base/BridgeModule`；Android `KRBridgeModule` / `KRShareModule` |
| 适配器 | `androidApp/.../adapter/`（Router / Image / Font / Thread / Log / Exception） |
| 动态化 | **v1 不做**；仅 ADR-0003 Dynamicization Seam（模块边界预留） |
| 官方平台广度 | KuiklyUI 含小程序 / macOS 等；本仓 **v1 明确四端**：Android / iOS / 鸿蒙 / H5 |

### 2.3 工程模式归属

对照官方 [跨端工程模式](https://kuikly.tds.qq.com/Introduction/paradigm.html)：

- **页面层**：Compose Track 为主 → 接近「标准模式」的 UI + 控制逻辑
- **数据 / 仓库**：`core-data` 全 Mock，在 common 内 → 带一点「进阶模式」的内置业务逻辑味道
- **平台能力**：`expect`/`actual` Platform Extension，而非直接塞进 Feature → 对齐官方「用 Module / 解耦平台能力」方向
- **交付**：AOT 随包（内置），非动态下发
- **设计 token**：收敛到 `core-design` 单一出口（见 §4.2）

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
| 设计 token 单源 | `core-design`（`KuiklyTheme` + `KuiklySystemPalette`） | `core-design/src/commonMain` |

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
│  业务/演示 Tab：Home / Community / Mine / Chat（包级缝，见 §4.4） │
│  base/：BaseComposePager / BasePager / BridgeModule             │
│  enableMultiModule；subModules = 全部 16 个 feature-*           │
└───────┬──────────────────────────┬───────────────────────────┘
        │ (api)                    │ (api)
┌───────▼──────────────────────┐  ┌──────────▼─────────────────────────────┐
│ Feature Modules（16）         │  │ Platform Extension / 能力模块（12）        │
│ feature-auth / -feed /        │  │ platform-permission / -share / -chart /   │
│ -analytics / -settings /      │  │ -im / -push / -realtime / -linking /      │
│ -chat / -bfui / -community /  │  │ -face-verify / -bluetooth /               │
│ -video / -mall / -pay /       │  │ -login-share-pay / -dokit(+bootstrap)     │
│ -classroom / -music / -ai /   │  │ （permission/share 为 expect/actual 跨端   │
│ -wallet / -live / -friend     │  │  API；其余提供具体能力实现）               │
└────────┬─────────────────────┘  └──────────────────────────────────────────┘
         │ (仅依赖 core-*)
┌────────▼─────────────────────────────────────────────────────┐
│ core-*（3）                                                     │
│ core-navigation  PageNames（路由名常量）                         │
│ core-data        Auth/Feed/Mock Repository + MockBackend        │
│ core-design      KuiklyTheme（Vercel 26 token）+                │
│                 KuiklySystemPalette（iOS 语义色，单源）          │
└────────────────────────────────────────────────────────────────┘
         ▲
         │ Maven
┌────────┴────────┐
│ KuiklyUI 制品    │
│ core / compose / render-* / core-ksp / gradle-plugin
└─────────────────┘
```

交互式架构图：[`kmp-kuikly-architecture.html`](./kmp-kuikly-architecture.html)（源：[`kmp-kuikly-architecture.json`](./kmp-kuikly-architecture.json)，Archify showcase）。

### 4.1 依赖规则（已遵守）

1. Shell → `app-shared` → feature / platform / core；Feature **不**依赖 Shell。
2. Feature 之间不互依；跳转用 `PageNames` + `BridgeModule.openPage`。
3. Feature 只碰 platform **API**（expect），不碰各端 Shell 实现细节；feature 不反向依赖 platform 实现模块。
4. Kuikly **主模块唯一**：`app-shared`（`isMainModule=true`）；子模块（16 个 feature）页面经 §4.3 的源合并缝并入主模块编译。
5. **设计 token 单源**：所有语义色 / 主题来自 `core-design`，业务/feature 不得各自硬编码重复色板（见 §4.2）。

> 审计结论（2026-10-19）：依赖方向干净 —— 每个 feature 仅依赖 `core-data` / `core-navigation` / `core-design`，无 feature→feature、feature→shell、feature→platform-impl 违规。`app-shared` 的 `api(project(":feature-X"))` + `kotlin.srcDir(...)` 是 §4.3 的有意缝，**不是** feature 间代码依赖。

### 4.2 设计 token 单源模型（core-design）

`core-design` 是本仓**唯一**的设计 token 出口（SCHEME §3 / §6）。它目前承载两套互补的调色板：

| 调色板 | 定位 | 用途 | 来源 |
|---|---|---|---|
| `KuiklyTheme` | Vercel 风格 26-token 对象 | 组件壳 `KCard` / `KButton` / `KChip` / `KMetricPill` 及 analytics/bfui 体系主题 | Vercel 黑白灰 + 单一强调色 |
| `KuiklySystemPalette` | iOS System 语义色 | 社交/业务 Tab（Home/Community/Mine/Chat）的 iOS 风格配色 | iOS systemBlue/label/separator 等 |

**为什么有两套？** 二者服务不同视觉语言：组件体系走 Vercel 中性风，`KuiklyTheme` 是组件默认主题；而 4 个社交业务 Tab 需要对齐 Flutter 参考图的 iOS 系统调色板。`KuiklySystemPalette` 是后者的**唯一源**。

**重构要点（2026-10-19）**：此前 `CommunityPalette` / `HomePalette` / `MinePalette` / `ChatPalette` 在 `app-shared` 内**逐字重复**了同一组 iOS 系统色（4× 重复定义，core-design 被绕过）。现已把这组色值集中到 `core-design/KuiklySystemPalette.kt`，4 个业务 Palette 改为 `: KuiklySystemPalette()` 继承复用。色值**保持字节级不变**，因此 36/36 golden-diff 基准不受影响。

**feature 级品牌色保留在各自模块**：Pay 金、Video/Classroom 绿、HomeReport 深色等底色与 iOS 系统色不同，仍保留在对应 feature 模块内 —— 这是合法的 feature 级 token（ADR-0002 混合分层），不属于违规重复。

### 4.3 页面编译缝（kotlin.srcDir 源合并）

`app-shared/build.gradle.kts` 的 `commonMain` 对每个 feature 声明：

```kotlin
kotlin.srcDir(project(":feature-auth").file("src/pages/kotlin"))
kotlin.srcDir(project(":feature-feed").file("src/pages/kotlin"))
// …… 其余 14 个 feature 同理
```

作用：Kuikly 的 `@Page` 注册由 KSP 在**主模块**内扫描源文件完成。feature 的页面源放在各自 `feature-X/src/pages/kotlin`，通过 `srcDir` 并入 `app-shared` 主模块一起编译，使 KSP 能发现 `@Page` 并生成注册。

**这不是 feature 间代码依赖**：feature 源码仅在主模块编译期被“借”来参与 KSP 扫描，运行期仍通过 `api(project(":feature-X"))` 以正常模块依赖引用。该缝是「单主模块 + 多子模块」Kuikly 接入模式的必然产物，符合 §4.1 规则 4。

### 4.4 业务 Tab 落在 app-shared（包级缝）

`Home` / `Community` / `Mine` / `Chat` 等底部 Tab 与 `MainPage` 直接写在 `app-shared` 的 `pages/` 包内，而非拆成独立 `feature-home` 等 Gradle 模块。这是 **ADR-0004 / Ponytail** 允许的「包级缝」：Demo Skeleton 阶段用包边界替代物理模块拆分，避免过早 YAGNI 的物理模块开销（SCHEME 的「终态目录」是目标，非强制当前形态）。这些 Tab 复用 §4.2 的 `KuiklySystemPalette`，是本次 token 集中化的主要落点。

---

## 5. Gradle 模块清单

`settings.gradle.kts` 当前 `include` 共 **34 个** Gradle 模块：

| 模块 | 角色 | 备注 |
|---|---|---|
| `:androidApp` | Android Shell | `KuiklyRenderActivity` |
| `:h5App` | H5 Shell | webpack / `nativevue2.js` |
| `:app-shared` | 主业务入口（唯一主模块） | CocoaPods framework 名仍为 `shared` |
| `:core-navigation` | 路由名常量 `PageNames` | |
| `:core-data` | Mock + Repository | 含 commonTest |
| `:core-design` | 设计 token 单源 | `KuiklyTheme` + `KuiklySystemPalette` |
| `:feature-auth` | 登录/注册 Feature | `moduleId=feature_auth` |
| `:feature-feed` | Feed 列表/详情 | `moduleId=feature_feed` |
| `:feature-analytics` | 数据/图表 Feature | 复用 core-design |
| `:feature-settings` | 设置 Feature | |
| `:feature-chat` | 聊天 Feature | |
| `:feature-bfui` | 业务组件库 Feature | |
| `:feature-community` | 社区 Feature | 继承 KuiklySystemPalette |
| `:feature-video` | 视频 Feature | |
| `:feature-mall` | 商城 Feature | |
| `:feature-pay` | 支付 Feature（品牌金） | |
| `:feature-classroom` | 课堂 Feature（品牌绿） | |
| `:feature-music` | 音乐 Feature | |
| `:feature-ai` | AI Feature | |
| `:feature-wallet` | 钱包 Feature | |
| `:feature-live` | 直播 Feature | |
| `:feature-friend` | 好友 Feature | |
| `:platform-permission` | 权限 API | expect/actual |
| `:platform-share` | 分享 API | expect/actual |
| `:platform-chart` | 图表能力 | |
| `:platform-im` | IM 能力 | |
| `:platform-push` | 推送能力 | |
| `:platform-realtime` | 实时能力 | |
| `:platform-linking` | 拉起/联运能力 | |
| `:platform-face-verify` | 人脸核验能力 | |
| `:platform-bluetooth` | 蓝牙能力 | |
| `:platform-login-share-pay` | 登录/分享/支付聚合能力 | |
| `:platform-dokit` | DoKit 调试能力 | |
| `:platform-dokit-bootstrap` | DoKit 启动引导 | |

**不在 settings、但存在于仓库：**

- `iosApp/`、`ohosApp/` — Xcode / DevEco 工程，非 Gradle `include`（或走 `settings.ohos.gradle.kts`）
- `static_server/` — H5 静态调试辅助
- `scripts/run*.sh` — 四端一键启动

### 5.1 Kuikly 多模块配置要点

主模块（`app-shared/build.gradle.kts`）：单一 `isMainModule=true`，通过 `commonMain` 的 16 条 `kotlin.srcDir(feature-X/src/pages/kotlin)` 把子模块页面源并入主模块，配合 `api(project(":feature-X"))` 完成 KSP 注册与运行期引用（见 §4.3）。

子模块：各自 `isMainModule=false`、`moduleId`，页面上 `@Page(name=…, moduleId=…)`。  
证据摘要：[`docs/evidence/MODULES-DONE.md`](../evidence/MODULES-DONE.md)。

---

## 6. 页面地图（`@Page`）

页面现分布于 `app-shared/pages/`（业务 Tab）与 16 个 `feature-X/src/pages/kotlin/`（子模块页面），统一经 §4.3 缝在主模块注册 `@Page`。

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
**已验收界面集合**：golden 跨源对比覆盖 **41 屏**（KMP 截图 vs Flutter 参考，RMSE_rel ≤ 0.22），详见 `scripts/golden_vs_flutter_diff.py` 的 `MAP`。

**启动示例（Android）：**

```bash
./scripts/run.sh android --page Home
# 或
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName Home
```

---

## 7. 核心子系统详解

### 7.1 `app-shared/base/` — 页面基类（原 `core-pager` 已合并）

> 注意：`core-pager` 模块已不存在，基类已并入 `app-shared/src/commonMain/kotlin/com/example/kuikly/base/`。

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

### 7.3 Platform Extension / 能力模块（12 个）

```kotlin
// common（以 permission 为例）
expect object PermissionApi { fun checkCamera(): String }
```

| 端 | permission | share | 其余 platform-* |
|---|---|---|---|
| androidMain | mock `"android:granted(mock)"` | 有 | 各能力 Android 实现 |
| iosMain | mock | 有 | 各能力 iOS 实现 |
| jsMain | mock | 有 | 各能力 JS 实现 |
| ohos | **无独立 actual** | **无**（鸿蒙若编此 API 需补） | 视能力而定 |

与 SCHEME 设想的「每能力拆 `platform-*-android` 独立 Gradle 工程」不同：本仓用 **单模块多 sourceSet expect/actual**（更短路径，仍满足 ADR-0002「有 Platform Extension 边界」）。当前共 12 个 `platform-*` 模块（含 `platform-dokit` + `platform-dokit-bootstrap`），由 `app-shared` 以 `api` 统一引入。

### 7.4 Shell：Android 挂载路径（已验证主路径）

`KuiklyRenderActivity`：

1. 读 Intent `pageName`（默认 `HelloWorld`）
2. `KuiklyRenderViewBaseDelegator.onAttach(container, "", pageName, pageData)`
3. 导出侧注册 `KRBridgeModule` / `KRShareModule` 与各类 `KR*Adapter`

其它端职责见 `docs/runbooks/{android,ios,ohos,h5}.md`。

---

## 8. 相对 SCHEME 的完成度

| Phase | SCHEME 目标 | 现状（2026-10-19） |
|---|---|---|
| 0 模板可跑 | 官方模板 + README | **完成**（CLI 生成 + 版本对齐） |
| 1 竖切 | Auth+Feed 四端 + common 测 + Android CI | **Android PASS**；shared 页码就绪；iOS/Ohos 证据 blocker；H5 smoke PASS |
| 2 丰满示例 | home 多 Tab、gallery、perf、dsl-lab、core-design | **页面已在 app-shared**（包级缝，§4.4）；`core-design` **已建**（§4.2）；Home 是 Demo Map 列表而非底部多 Tab |
| 3 Platform Extension | permission/share + 四端 impl | **API + 三端 actual + Demo 页** + 扩展至 12 个 platform 能力模块；非拆分子工程；Ohos actual 缺口 |
| 4 打磨 | embed 契约、版本目录、架构图、动态化文档 | embed + demo-map + 本分析文档；`libs.versions.toml` 未锁；动态化仅 seam |

### 与 SCHEME「终态目录」差异（有意/未做）

| SCHEME 设想 | 现状 |
|---|---|
| `feature-home` / `gallery` / `perf` / `dsl-lab` 物理模块 | 演示页落在 `app-shared`（包级缝，符合 Ponytail「可先不物理拆」） |
| `core-design` | **已建**（KuiklyTheme + KuiklySystemPalette 双调色板） |
| `platform-permission-android` 等四分模块 | 合并为单模块 expect/actual + 12 个 platform 能力模块 |
| 微信小程序 | **明确不做 v1**（KuiklyUI 支持，本仓 scope 外） |

---

## 9. ADR 摘要

| ADR | 决策 |
|---|---|
| [0001](../adr/0001-compose-primary-with-legacy-dsl-track.md) | Compose 主轨；Legacy DSL 仅对照 |
| [0002](../adr/0002-hybrid-feature-and-platform-extension-modules.md) | Feature + Platform Extension 混合，拒绝巨型 shared |
| [0003](../adr/0003-aot-v1-with-dynamicization-seam.md) | v1 AOT；动态化只留边界 |
| [0004](../adr/0004-ponytail-with-architecture-overrides.md) | Ponytail 管代码量；不能 YAGNI 掉 ADR/SCHEME 范围；允许业务 Tab 以「包级缝」留在 app-shared |

---

## 10. CI / 测试 / 证据

### CI（`.github/workflows/ci.yml`）

- 触发：PR + `main`/`master`
- JDK 17
- `./gradlew :core-data:testDebugUnitTest :app-shared:testDebugUnitTest`
- `./gradlew :androidApp:assembleDebug`
- **不含** iOS / Ohos / H5 云端构建（与 SCHEME 一致）

### 视觉验收（Golden Cross-Source Diff）

- 脚本：`scripts/golden-android-capture.sh`（截图 41 屏 → `goldens/android/actual/`）+ `scripts/golden_vs_flutter_diff.py`（KMP vs `goldens/flutter-ref/baseline/`）。
- 方法：遮罩顶/底状态栏、resize 对齐、像素 RMSE_rel，阈值 `GOLDEN_RMSE_MAX=0.22`（38 对映射）。
- 当前状态：**36/36 通过**（2026-10-19 重构后仍稳定，token 集中化保持色值字节级不变）。

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
| 多模块拆分 | 已落地（34 模块；MODULES-DONE，晚于初版 Reality Check 中「NEEDS WORK」表述） |

---

## 11. 嵌入外部宿主契约

详见 [`docs/embed-contract.md`](../embed-contract.md)。摘要：

1. 依赖 `:app-shared`（或日后发布的 AAR / framework / JS bundle）
2. 按官方文档接对应端 **KuiklyRender**
3. 注册适配器与 Bridge Module
4. 用 `PageNames` 打开页面  
**不要**把业务 UI 写进 Shell，也**不要**让 Feature 依赖 `platform-*-android` 式实现模块。**不要**在 feature/app-shared 内重复声明 `core-design` 已提供的语义色（见 §4.2）。

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
6. 将 4 个业务 Tab 的「包级缝」按 SCHEME 终态物理拆为 `feature-home` 等（仅当需要真实多团队并行开发时）

---

## 附录 A — 关键路径速查

```text
buildSrc/src/main/java/KotlinBuildVar.kt          # 版本
settings.gradle.kts                               # 模块图（34 模块）
app-shared/build.gradle.kts                       # 主模块 + multi-module KSP + srcDir 缝
app-shared/src/commonMain/kotlin/com/example/kuikly/base/BaseComposePager.kt
core-navigation/.../PageNames.kt
core-data/.../mock/MockBackend.kt
core-design/.../design/KuiklyTheme.kt             # Vercel 风格 token
core-design/.../design/KuiklySystemPalette.kt     # iOS 语义色单源
feature-auth/.../pages/LoginPage.kt
androidApp/.../KuiklyRenderActivity.kt
.github/workflows/ci.yml
docs/SCHEME.md / docs/adr/* / docs/evidence/*
scripts/golden_vs_flutter_diff.py                 # 跨源 RMSE 验收
```

## 附录 B — 运行命令

```bash
./scripts/run.sh list
./scripts/run.sh android --page Home
./scripts/run.sh ios --page Home --sim "iPhone 16"
./scripts/run.sh ohos --open-deveco
./scripts/run.sh h5 --page Home
./gradlew :core-data:testDebugUnitTest :androidApp:assembleDebug
# 重建 + 重跑 golden 跨源验收（需 emulator-5554 在线）
bash /tmp/kmp-golden-verify.sh
```
