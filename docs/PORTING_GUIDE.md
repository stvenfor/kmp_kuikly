# demo-skeleton → main 移植实施指南（并行 agent 必读）

本文件是「把 demo-skeleton 内容按 main 架构改造并入」任务的**唯一事实来源**。

- 工程根目录：`/Users/stvenfor/Desktop/MyWork/kmp_kuikly`
- demo-skeleton 源码已解包到：`/tmp/ds/`（`feature-home/src/...`、`core-data/src/...`、`core-pager/src/...`）
- 先读 `docs/AGENT_PLAYBOOK.md`（基础范式），再读本文件（本次移植专项规则）。

---

## 0. 绝对红线

1. **不要运行任何 Gradle / 构建命令**（并发会抢工程锁，全部失败）。只写代码。
2. **不要修改公共文件**：`settings.gradle.kts`、`app-shared/build.gradle.kts`、
   `core-navigation/.../PageNames.kt`、`app-shared/.../HomePage.kt`、`core-data/**`、`core-design/**`、
   `app-shared/src/commonMain/kotlin/com/example/kuikly/base/**`。这些由中心节点统一处理。
3. **只写分配给你的模块目录**。
4. Kotlin 注释内**不能出现 `/*`**（块注释可嵌套 → Unclosed comment）。
5. **Double 字面量必须写 `0.0`**，传 `1500` 会类型不匹配。

---

## 1. ⚠️ 重要更正：这些 API 在 Kuikly 2.16.0 确实可用

此前 `AGENT_PLAYBOOK.md` 基于不完整文档判定 `Canvas`/`weight`/`fillMaxHeight` 不可用 —— **这是错的**。
demo-skeleton 的代码证明了它们可用，**移植时请原样保留，不要改写**：

- `com.tencent.kuikly.compose.foundation.Canvas` + `DrawScope` 的 `drawPath` / `drawCircle` / `drawArc`
- `com.tencent.kuikly.compose.ui.graphics.Path` / `StrokeCap` / `StrokeJoin` / `drawscope.Fill` / `drawscope.Stroke`
- `Modifier.weight(1f)`、`Modifier.fillMaxHeight()`
- `Text(lineHeight = ...)`、`TextOverflow.Ellipsis`、`overflow` 参数
- `LazyColumn(contentPadding = PaddingValues(...))`
- `pagerData.pageViewWidth`、`pagerData.statusBarHeight`

基类已补齐 `statusBarInset()` / `bottomSafeInset()`（protected，`BaseComposePager` 子类可用）。

---

## 2. 页面移植三步法

### 步骤 1：改包名与位置

| 项 | demo-skeleton | main（目标） |
|---|---|---|
| 包名 | `com.example.kuikly.feature.home.pages` | `com.example.kuikly.pages` |
| 目录 | `feature-home/src/commonMain/kotlin/com/example/kuikly/feature/home/pages/` | `feature-<x>/src/pages/kotlin/com/example/kuikly/pages/` |
| 注解 | `@Page(name = "X", moduleId = "feature_home")` | `@Page("X")` |

页面类保持 `internal class XxxPage : BaseComposePager()`，`import com.example.kuikly.base.BaseComposePager`。

### 步骤 2：改数据层引用

demo-skeleton 的数据层集中在 `core-data`；main 按 feature 模块分布。**已由中心节点补齐的新数据层**（可直接用）：

| 包 | 提供 | 所在模块 |
|---|---|---|
| `com.example.kuikly.data.mine` | `MineStore` / `MineRepository` / `MineProfile` / `MineStats` / `MineQuickService` / `MineMenuItem` / `MineFunctionItem` | feature-settings |
| `com.example.kuikly.data.search` | `SearchStore` / `SearchRepository` / `SearchRankItem` / `SearchRankTab` | feature-community |
| `com.example.kuikly.data.usedcar` | `UsedCarStore` / `UsedCarRepository` / `UsedCarItem` / `UsedCarPage` | feature-analytics |
| `com.example.kuikly.data.dubbing` | `DubbingWorkStore` / `DubbingWorkRepository` / `DubbingWorkItem` | feature-video |
| `com.example.kuikly.data.music` | `Song` / `MusicPlaybackStore`（**新增**，与既有 `Track`/`Playlist`/`MusicStore` 并存） | feature-music |
| `com.example.kuikly.data.video` | `VideoLeaderboard`（**新增**，与既有 `ShortVideo`/`VideoStore` 并存） | feature-video |
| `com.example.kuikly.data.classroom` | `Course`（**新增**，与既有 `ClassInfo` 并存） | feature-classroom |
| `com.example.kuikly.data.pay` | `Plan` / `PayTier`（**新增**，与既有 `MembershipPlan` 并存） | feature-pay |
| `com.example.kuikly.data.chat` | `ChatMessage`（**新增**，与既有 `Message` 并存） | feature-chat |
| `com.example.kuikly.data.auth` | `AuthSession.repo.isLoggedIn()` / `loginWithOtp()` / `FakeAuthRepository.MOCK_PHONE` / `MOCK_OTP`（**已新增**） | core-data |
| `com.example.kuikly.navigation` | `MainTabLaunch` / `LoginRedirect`（**已新增**） | core-navigation |

**字段名差异（需适配）**：

| demo-skeleton | main | 说明 |
|---|---|---|
| `Conversation.timeLabel` | `Conversation.lastMessageTime: Long` | main 用 Long 时间戳，页面需自行格式化（或简化展示） |
| `Song` | `Track` | 两者并存；若页面只用 `Song` 的 id/title/artist，可继续用 `Song` |
| `MineStore.repo.profile()` | 同名可用 | — |

**无法对应时**：若页面引用了 main 不存在且上面表格没有的 API，**不要臆造**。
改为用 main 已有的等价能力实现，并在汇报里明确指出。

### 步骤 3：跳过重复页面

main 已有等价页面的，**不要移植**（会重复注册）。main 已有的等价物：

| demo-skeleton | main 已有 | 处置 |
|---|---|---|
| `SettingsPage` / `PersonalizedSettingsPage` | feature-settings 的 `Settings` / `PersonalizedSettings` | **跳过** |
| `ChatDetailPage` | feature-chat 的 `ChatDetail` | **跳过** |
| `CommunityPublishPage` | feature-community 的 `CommunityPublish` | **跳过** |
| `SearchPage` | feature-community 的 `CommunitySearch` | **跳过** |
| `VideoDetailPage` / `VideoListPage` | feature-video 的 `Video` / `VideoDetail` | **跳过** |
| `MusicListPage` / `MusicNowPlayingPage` | feature-music 的 `Music` / `NowPlaying` | **跳过** |
| `ClassroomListPage` | feature-classroom 的 `Classroom` | **跳过** |
| `FriendListPage` | feature-friend 的 `Friend` / `FriendGroup` | **跳过** |
| `LiveListPage` | feature-live 的 `Live` | **跳过** |
| `StrategyPage` / `UsedCarListPage` / `UsedCarDetailPage` | feature-analytics 的 `AnalyticsStrategy` / `AnalyticsUsedCar` | **跳过**（除非新增子页） |

---

## 3. 本次要移植的页面（按模块分组）

| 模块 | 要移植的页面（源文件在 /tmp/ds/feature-home/.../pages/） |
|---|---|
| **feature-analytics** | `LearningReportPage`、`HotRankDetailPage` |
| **feature-video** | `DubbingHomePage`、`DubbingWorkListPage`、`DubbingWorkDetailPage` |
| **feature-classroom** | `ClassroomDetailPage`、`ClassroomMyClassPage`、`ClassroomGiftClaimPage`、`ClassroomVideoDetailPage` |
| **feature-pay** | `PayConfirmPage` |
| **feature-mall** | `PayListPage`、`CheckInMallPage` |
| **feature-community** | `PostDetailPage` |
| **feature-friend** | `FriendDetailPage` |
| **feature-live** | `LiveDetailPage` |
| **app-shared** | `AllServicesPage` |
| **Main 框架** | `MainPage`、`AppChrome`、`HomeDashboardTab`、`CommunityTab`、`MineTab` → 放 `feature-home` 新模块或 app-shared（见下方专项说明） |

---

## 4. Main 多 Tab 框架专项（最重要）

`MainPage.kt` + `AppChrome.kt` + `HomeDashboardTab.kt` + `CommunityTab.kt` + `MineTab.kt` 是 main **完全没有**的能力（对应 WBS B3）。

要求：
1. 新建 `feature-home` 模块（`build.gradle.kts` 抄 `feature-feed/build.gradle.kts` 的格式，namespace 用 `com.example.kuikly.feature.home`，依赖 `core-data`/`core-navigation`/`core-design`）。
2. 页面放 `feature-home/src/commonMain/kotlin/com/example/kuikly/feature/home/pages/`（**注意：这个模块用 commonMain，不用 src/pages**，因为它自带 KSP）。
3. **需要 KSP**：抄 demo-skeleton 的 `feature-home/build.gradle.kts`（含 `id("com.google.devtools.ksp")` + `ksp {}` + `compileOnly core-ksp`）。
4. `@Page(name = "Main", moduleId = "feature_home")` **保持原样**（该模块用带 moduleId 的注解形式）。
5. `MainPage` 里 `HomeTab` / `ChatTab` / `MineTab` / `CommunityTab` 是 private fun，随文件一起搬。
6. `LanHost.kt`（6 行）一起搬。

> 若 KSP 方案验证不通，退路：把 Main 框架页面改为 main 的 `@Page("Main")` 形式放进 `app-shared/src/commonMain/kotlin/com/example/kuikly/pages/`，并汇报。

---

## 5. 交付要求

完成后用 ≤180 字汇报：移植了哪些页面（列出 `@Page` 名）、放到哪个模块、改了哪些数据层引用、有哪些不确定能否编译的点。
