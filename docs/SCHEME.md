# kmp_kuikly 成熟 Demo Skeleton 方案

> 依据 [tds-Kuikly 文档](https://kuikly.tds.qq.com) 与 grilling 定案。术语见根目录 `CONTEXT.md`，硬决策见 `docs/adr/`。

## 1. 目标与非目标

### 目标

- 可 fork 的 **Demo Skeleton**：教学清晰，工程接近准生产。
- 四端可运行：**Android / iOS / 鸿蒙 / H5**（微信小程序明确不做 v1）。
- **Embeddable Shared**：业务在 KMP 模块中，Shell App 可替换为外部宿主。
- **丰满示例**：登录态、多 Tab、列表/详情、性能页、平台能力对照、Compose + 自研 DSL 对照。
- **全 Mock** 数据；**common 单测**；PR 上 **shared + Android** CI。

### 非目标（v1）

- 真实后端 / 鉴权供应商
- 动态下发 / 热更新流水线（仅留 Dynamicization Seam）
- 微信小程序
- 全端 E2E / 截图 CI
- 鸿蒙 / iOS / H5 的强制云端 CI（本地文档门禁）

## 2. 技术底座（对齐官方）

| 项 | 选择 |
|---|---|
| 框架 | [Kuikly / KuiklyUI](https://kuikly.tds.qq.com)（KMP + Core + Bridge + 各端 Render） |
| 起步 | Android Studio → **Kuikly Project Template**，再重构到终态模块 |
| 包名 | `com.example.kuikly`（占位） |
| Maven | `https://mirrors.tencent.com/nexus/repository/maven-tencent/`（≥2.5.0） |
| 版本 | 各端 Kuikly **同主版本**；KMP 制品为 `Kuikly-Kotlin` 组合号，见 [版本配置](https://kuikly.tds.qq.com/DevGuide/version_skills.html) |
| UI | Compose Track 为主；Legacy DSL Track 对照 |
| 交付 | AOT / 随包；动态化仅预留缝 |

官方必读路径：

1. [KMP 跨端工程接入](https://kuikly.tds.qq.com/QuickStart/common.html)
2. 各端 KuiklyRender：[Android](https://kuikly.tds.qq.com/QuickStart/android.html) / iOS / 鸿蒙 / H5
3. [HelloWorld](https://kuikly.tds.qq.com/QuickStart/hello-world.html) + Compose 概念文档
4. [扩展库创建指引](https://kuikly.tds.qq.com/Community/kuikly_extension_lib_guide.html)（Platform Extension 范式）

## 3. 逻辑架构

```text
┌─────────────────────────────────────────────────────────────┐
│ Shell Apps (可替换宿主)                                       │
│  androidApp │ iosApp │ ohosApp │ h5App                       │
│  · 启动页 / 调试路由入口                                        │
│  · KuiklyRender 挂载 + 平台适配器注册                            │
└───────────────────────────┬─────────────────────────────────┘
                            │ 依赖 Embeddable 入口
┌───────────────────────────▼─────────────────────────────────┐
│ app-shared (薄组装入口，唯一 Kuikly Page 注册总出口)              │
│  · 依赖各 feature-*                                          │
│  · 注册路由表 / 默认 Module 实现绑定                              │
└───────┬─────────────────────────┬───────────────────────────┘
        │                         │
┌───────▼──────────┐    ┌─────────▼────────────────────────────┐
│ Feature Modules  │    │ Platform Extension Modules           │
│ feature-auth     │    │ platform-permission (api)            │
│ feature-home     │    │ platform-permission-android/ios/...  │
│ feature-feed     │    │ platform-share (api + *-android/…)   │
│ feature-gallery  │    │ platform-analytics (桩)              │
│ feature-perf     │    └──────────────────────────────────────┘
│ feature-dsl-lab  │  ← Legacy DSL 对照页
└─────────┬────────┘
          │
┌─────────▼────────────────────────────────────────────────────┐
│ core-*                                                       │
│ core-design   主题 / 间距 / 基础组件包装                          │
│ core-navigation  路由契约、Page 名常量                           │
│ core-data     Repository 接口、Model、Mock 引擎                  │
│ core-common   Result、协程工具、日志门面                          │
└──────────────────────────────────────────────────────────────┘
```

运行时（概念，对齐官方分层）：

```text
Page (ComposeContainer 或 Pager)
  → Kuikly Core view tree
  → Bridge
  → 各端 KuiklyRender (Android View / UIKit / ArkUI / DOM)
```

## 4. 仓库目录（终态目标）

```text
kmp_kuikly/
├── CONTEXT.md
├── docs/
│   ├── SCHEME.md
│   ├── adr/
│   ├── runbooks/                 # 各端本地验收命令
│   └── architecture/             # 可选：模块依赖图
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml     # 统一 Kuikly / Kotlin / AGP
├── androidApp/
├── iosApp/
├── ohosApp/
├── h5App/
├── app-shared/                   # 组装入口（原模板 shared 演化）
├── core-common/
├── core-design/
├── core-navigation/
├── core-data/
├── feature-auth/
├── feature-home/
├── feature-feed/
├── feature-gallery/
├── feature-perf/
├── feature-dsl-lab/
├── platform-permission/          # common API
├── platform-permission-android/
├── platform-permission-ios/
├── platform-permission-ohos/
├── platform-permission-h5/
├── platform-share/               # 同上拆分
└── .github/workflows/ci.yml      # shared + Android + unit tests
```

说明：

- 第一天**不要**手工建齐所有目录；模板生成后按阶段拆出。
- 平台扩展模块命名对齐官方扩展库思路（common + 分端实现）。
- `app-shared` 保持薄：只做依赖组装与 `@Page` 发现所需的入口，避免再变巨型 shared。

## 5. 示例地图（丰满集）

| 区域 | Feature | 主要内容 | UI Track |
|---|---|---|---|
| 登录态 | `feature-auth` | 登录/登出、Token 假存储、未登录拦截 | Compose |
| 多 Tab | `feature-home` | 底部 Tab：首页 / 动态 / 我的 | Compose |
| 列表详情 | `feature-feed` | 分页列表、详情、空/错/加载态（Mock 可切换） | Compose |
| 媒体 | `feature-gallery` | 图片网格、大图 | Compose |
| 性能 | `feature-perf` | 长列表、频繁重组对照说明 | Compose |
| DSL 对照 | `feature-dsl-lab` | 同构「卡片列表」自研 DSL 版 | Legacy DSL |
| 平台能力 | `platform-*` + 演示页 | 权限申请、分享、埋点桩 | Compose 调 API |

Mock 引擎（`core-data`）：

- 场景开关：`Success` / `Empty` / `Error` / `Slow`
- 登录用户、Feed 条目等内存数据
- Feature 只依赖 Repository 接口，不直接 new Fake（便于以后换真实现，即使 v1 不做）

## 6. 分层与依赖规则

1. **Shell → app-shared → feature-* → core-***；禁止 feature 依赖 Shell。
2. **feature 之间默认不互依**；跳转只依赖 `core-navigation` 的 Page 名/路由契约。
3. **feature 可依赖 platform-*-api**；不可直接依赖 `platform-*-android` 等实现（由 Shell 或 `app-shared` 的平台源集绑定）。
4. **唯一 Kuikly 入口类**落在 `app-shared`（官方约束：平台产物入口唯一）；其它模块以 KLib 被编译进入口。
5. Dynamicization Seam：Feature 不假设「类一定在包内」——页面注册与 Module 获取走可替换的 Registry，便于日后动态包。

## 7. 各端 Shell 职责清单

对齐官方「工程接入 → Render 接入」两段式：

| 端 | 工程 | 关键工作 |
|---|---|---|
| Android | `androidApp` | 依赖 `core-render-android`；挂载 Render 容器；注册适配器与 platform impl |
| iOS | `iosApp` | `pod install`；OpenKuiklyIOSRender；framework 链 `app-shared` |
| 鸿蒙 | `ohosApp` | DevEco 签名；`kuikly-open/render`；oh-package 版本对齐 |
| H5 | `h5App` | `packLocalJsBundle*` + `jsBrowserDevelopmentRun`；URL `page_name` 调试 |

每个 Shell 提供统一调试入口页：输入 Page 名跳转（保留官方 Router 体验），并提供「示例地图」按钮进正式信息架构。

## 8. CI / 测试 / 门禁

### CI（托管）

- 触发：PR / main
- 任务：
  - `./gradlew :core-data:testDebugUnitTest`（及各 common 测试任务，以实际 sourceSet 为准）
  - `./gradlew :app-shared:compileKotlinMetadata` 或等价 compile
  - `./gradlew :androidApp:assembleDebug`
- 缓存 Gradle；固定 JDK 版本

### 本地门禁（文档化，见 `docs/runbooks/`）

- iOS：模拟器跑通登录竖切
- 鸿蒙：DevEco 签名后跑通同一竖切
- H5：browser 打开 `?page_name=...` 跑通同一竖切

### 测试策略

- **必测**：Mock 状态机、AuthRepository、FeedRepository、路由名常量不漂移（简易契约测）
- **不测（v1）**：Compose UI 像素、多端 E2E

## 9. 交付阶段（竖切）

### Phase 0 — 能跑的官方模板（约 0.5–1 天）

- AS 创建 Kuikly 工程；对齐最新版本与 maven 源
- Android / iOS / 鸿蒙 / H5 按官方文档各跑通一次空模板或 HelloWorld
- 根目录 README：环境要求 + 链接到官方文档

### Phase 1 — 模块骨架 + 第一条竖切（核心里程碑）

- 从 `shared` 拆出：`app-shared`、`core-*`、`feature-auth`、`feature-feed`（可先包名隔离，再物理模块）
- 实现：**登录 + 一列表**（含空/错态开关）
- Compose 页面；四端同一竖切验收
- common 单测绿；Android CI 绿

### Phase 2 — 信息架构与丰满示例

- `feature-home` 多 Tab
- `feature-gallery`、`feature-perf`
- `feature-dsl-lab` 对照页
- `core-design` 基础主题

### Phase 3 — Platform Extension

- `platform-permission` / `platform-share`（api + 四端 impl）
- 演示页 + 无权限/取消路径
- 文档：如何新增一个 Platform Extension（照着官方扩展库指引写短 runbook）

### Phase 4 — 准生产打磨

- 适配器边界说明（给外部宿主嵌入用）
- 版本目录 `libs.versions.toml` 锁齐
- Demo 地图页、架构图、贡献/fork 指南
- （可选）预留动态化 Registry 文档，不实现下发

## 10. 嵌入外部宿主时的契约

外部宿主需要：

1. 依赖 `app-shared`（或你们日后抽出的 `sdk` 发布物）
2. 按官方文档接入对应端 **KuiklyRender**
3. 注册本仓库定义的 **Module / 适配器**（网络可仍为 Mock，或宿主提供实现）
4. 使用 `core-navigation` 的 Page 名打开页面

不需要：带走 `androidApp` 等 Shell 工程。

## 11. 风险与缓解

| 风险 | 缓解 |
|---|---|
| 四端环境脆（尤其鸿蒙签名、iOS Pod） | Phase 0 先全通；竖切验收清单；CI 不阻塞非 Android |
| 过早重度拆模块导致无法编译 | 先包名分层，竖切通后再物理拆；一次只拆一个 feature |
| Compose / 自研 DSL 双轨漂移 | DSL 对照仅 1–2 页；禁止新功能只写在 Legacy Track |
| 丰满示例变假业务泥球 | Feature 边界 + 依赖规则 Code Review 对照 ADR-0002 |
| Kuikly 版本与 Kotlin 矩阵踩坑 | 单一 `Version`/`libs.versions.toml`；升级对照官方 changelog |

## 12. 执行入口（已就绪）

- 工单：`.scratch/demo-skeleton/issues/`（01–15，含 Agency 角色与证据验收）
- Spec 指针：`.scratch/demo-skeleton/spec.md`
- Agency：`AGENTS.md` + `.cursor/rules/`（engineering + testing）
- Ponytail：`.cursor/rules/ponytail.mdc`（always on；ADR-0004）

**当前前沿：** 工单 **01 — Bootstrap official Kuikly template**（无 blockers）。  
通常需你在 Android Studio 用 **Kuikly Project Template** 生成工程到本仓库；生成后把会话交给 **Mobile App Builder** 做版本对齐与 README。运行向导：待确认后见 `scripts/wizard-kuikly-bootstrap.sh`（若已生成）。
