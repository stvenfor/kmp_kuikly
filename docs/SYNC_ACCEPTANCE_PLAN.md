# 全量同步验收计划（my_ai_project → kmp_kuikly）

> 目标：将源项目 `my_ai_project`（Flutter + GetX，约 **8.8 万行 Dart**）的「功能、业务逻辑、UI 效果」完整同步到当前 Kuikly（KMP Compose）项目，符合腾讯 Kuikly 规范、已落地的 Rules（`.codebuddy/rules/kuiklyDSL.mdc` / `kuiklyComposeDSL.mdc`）与已安装的 13 个官方 Skills。
> 策略：**逐模块垂直切片 + 四端竖切验收**，每个切片验收通过再进下一片；验收不合格则继续分解直到通过。
> 本文档为细粒度任务与验收的**唯一权威来源**；任务系统（TaskList）按本文档的切片粒度建立跟踪。

---

## 0. 源项目清单（事实基础）

### 0.1 Features（15 个，约 7.9 万行）

| # | Feature | 文件 | 行数 | 关键页面（源） |
|---|---------|------|------|----------------|
| F1 | home | 141 | 20996 | home_page, analytics_list_page, strategy_page, all_services_page, hot_rank_detail, ledger_list/detail, used_car_list/detail/create, learning_report, feature_content |
| F2 | settings | 68 | 8816 | settings_page, dialog_demo, purchase_calculator, personalized_settings, address_list/edit, mine_http_test, mine_profile, deal_invoice_demo/upload |
| F3 | bfui | 55 | 10261 | navigation_home, invite_friend, fitness_app_home, bottom_bar, training, my_diary, water_view, running_view, glass_view, title_view, mediterranean_diet_view |
| F4 | chat | 41 | 4623 | chat_page, chat_detail_page, image_preview_page, message_list_view |
| F5 | auth | 38 | 5218 | login_page, login_otp_page, login_password_page, register_page |
| F6 | community | 37 | 5187 | community_page, publish_page, search_page, convention_page, video_play_page, topic_select_page, image_preview_page |
| F7 | video | 25 | 4208 | video_page (short_video/dubbing/videos) |
| F8 | pay | 22 | 2981 | pay_page, membership_renew_page |
| F9 | mall | 21 | 2896 | mall_page, mall_detail_page, mall_orders_page, mall_order_detail_page |
| F10 | classroom | 16 | 3915 | claim_gift_card, homework_review, video_detail, dubbing_homework, my_class_list, homework_detail_teacher/student, class_homework_stats |
| F11 | music | 14 | 1122 | now_playing_page, music_list_page |
| F12 | ai | 11 | 832 | ai_stream_page |
| F13 | wallet | 6 | 593 | （钱包页，待细扫） |
| F14 | live | 4 | 206 | live_page, live_room_page |
| F15 | friend | 3 | 868 | friend_page |

### 0.2 Components（10 个能力库，约 2.0 万行）

| # | Component | 文件 | 行数 | 能力 |
|---|-----------|------|------|------|
| C1 | dokit | 55 | 11086 | 调试工具箱（性能/日志/布局检查） |
| C2 | realtime | 24 | 2363 | 实时通信/WebSocket 封装 |
| C3 | linking | 23 | 1525 | 深度链接 / scheme 路由 |
| C4 | rongcloud_im | 16 | 1545 | 融云 IM SDK 封装（对接 chat） |
| C5 | wys_push | 12 | 1259 | 推送 |
| C6 | wys_login_share_pay | 10 | 659 | 登录/分享/支付桥接（对接 auth/mall/pay） |
| C7 | wys_chart | 8 | 604 | 图表（转化环/迷你柱/漏斗/雷达）→ 已在 analytics 切片落地近似版 |
| C8 | wys_face_verify | 4 | 142 | 人脸核身 |
| C9 | bluetooth | 4 | 379 | 蓝牙 |
| C10 | dokit_bootstrap | 2 | 44 | dokit 启动引导 |

### 0.3 设计系统（源）
- `DESIGN.md`：Vercel 风格（黑白灰 + 单一强调色 + 大圆角 + 阴影 + 系统字体栈）
- `features/home/lib/home/theme/analytics_theme.dart`：数据分析专属色板（含 anomaly 红、featured 金、转化环渐变）

---

## 1. 目标架构与 Kuikly 约定映射

| 维度 | 源（Flutter/GetX） | 目标（Kuikly KMP Compose） |
|------|--------------------|----------------------------|
| 页面 | `XxxPage` (Stateless/StatefulWidget) | `@Page("Name") class XxxPage : BaseComposePager()` + `setContent { ... }` |
| 状态 | GetX `Obx` / `Rx` | `mutableStateOf` + `remember`（来自 `kuikly-reactive-observer` skill） |
| 模型 | `class Xxx { final ... }` | `data class Xxx(...)`（全 Kotlin 不可变） |
| 仓库 | `XxxRepository` (gRPC/REST) | `interface XxxRepository` + `object XxxFake : XxxRepository` + `object XxxStore`（接入 `MockBackend` 四态） |
| 路由 | GetX `Get.toNamed` | `BridgeModule.openPage("Name", params)`；返回 `BridgeModule.closePage()` |
| 列表 | `ListView.builder` | `LazyColumn { items(...) }`（`kuikly-visibility-exposure` skill 示例） |
| 图表 | `CustomPainter` / `fl_chart` | 基础组件组合近似（Box/Column/Row/Modifier），零 Canvas 依赖（已验证 Kuikly Compose 文档未暴露 Canvas API） |
| 设计 token | `DESIGN.md` / `analytics_theme` | `core-design` 模块（`KuiklyTheme` + `KCard/KButton/KChip/KMetricPill`） |
| 模块注册 | `pubspec` + 手动 import | `settings.gradle.kts` 注册 `:feature-x`；`app-shared` 通过 `kotlin.srcDir(project(":feature-x").file("src/pages/kotlin"))` 聚合；`PageNames` 登记 |

> 工程约定（已确认）：`@Page` 注解页面只能放在 `feature-x/src/pages/kotlin`，由 `app-shared` 用 `srcDir` 聚合后由 KSP 生成路由表；数据/逻辑放 `feature-x/src/commonMain/kotlin`。

---

## 2. 基础层（先于所有 feature，必须最先验收）

### B1 · core-design 完整设计系统  【任务 B1】
- B1.1 移植 `DESIGN.md` 全部 token：颜色（bg/surface/fg/muted/border/accent + 状态色）、字号阶梯（12/14/16/20/24/32）、字重、圆角（sm/md/lg/xl）、间距（4/8/12/16/24/32）、阴影、层级 z。
- B1.2 移植 `analytics_theme`：anomaly（#E5484D 系）、featured（金/琥珀）、转化环渐变（cyan→blue）、success/warn。
- B1.3 提供暗色主题（`KuiklyTheme(dark)`）。
- B1.4 基础组件壳：`KCard`、`KButton`（primary/ghost/danger）、`KChip`、复用 `KMetricPill`；统一 Modifier 扩展（圆角/阴影/点击）。
- B1.5 单测：token 取值、暗色切换、组件可组合性（无崩溃实例化）。
- **验收**：`core-design` 编译通过；被 `feature-analytics` 引用且视觉一致。

### B2 · 数据层基础与 Mock 引擎  【任务 B2】
- B2.1 确认 `MockBackend` 四态（Success/Empty/Error/Slow）+ `Result<T>` + `Store` 模式（已存在 `core-data`）。
- B2.2 抽取通用 `interface CrudRepository<T>` + `abstract class FakeRepository` 模板（含分页/筛选/排序），供各 feature 复用。
- B2.3 提供 `kotlinx.serialization` JSON 编解码约定（对接 `kuikly-network-and-json` skill）。
- B2.4 统一错误模型 `AppError(code, message)`。
- **验收**：B2 模板被 analytics 复用；单测覆盖 Fake 四态。

### B3 · 导航与模块注册规范  【任务 B3】
- B3.1 `PageNames` 集中登记所有页面常量（已含 Auth/Feed/Analytics，需扩展全部）。
- B3.2 `HomePage` 多 Tab 聚合壳（首页/动态/我的 + 各 feature 入口），遵循本项目约定。
- B3.3 深度链接方案：映射 `linking` component 的 scheme 到 `BridgeModule.openPage`。
- **验收**：从 Home 能导航到任一已同步 feature 页；参数透传正确。

---

## 3. Feature 细粒度切片（验收单元）

> 每个 feature 切片内子任务编号：M = 模型，R = 仓库+Mock，P = 页面（可多个），C = 组件抽取，T = 单测，I = 接入。
> **通用验收门禁**（每个切片必过）：见第 5 节 AC1–AC6。

### F1 · home（数据分析 + 聚合壳）— 已有 analytics  Pilot
- F1.M 模型：除 `AnalyticsRecordModel`（已建）外，迁移 `Strategy`/`HotRank`/`Ledger`/`UsedCar`/`LearningReport` 等模型（字段/枚举对齐源）。
- F1.R 仓库：`AnalyticsRepository`（已建 Fake+Store 四态）；补 `StrategyRepo`/`LedgerRepo`/`UsedCarRepo`/`LearningReportRepo`。
- F1.P1 `AnalyticsListPage`（**已建**：转化环+迷你柱+异常/精选色条+空/错/加载+分页）。
- F1.P2 `AnalyticsDetailPage`（**已建**：Banner+概况+大转化环+漏斗+收入成本+质量风险粘性雷达）。
- F1.P3 `HomePage` 多 Tab 壳（`feature-home`，聚合 analytics/动态/我的）。
- F1.P4 `StrategyPage` / `HotRankDetailPage` / `LedgerListPage`+`LedgerDetailPage` / `UsedCarList`+`Detail`+`Create` / `LearningReportPage` / `FeatureContentPage` / `AllServicesPage`。
- F1.C 复用 `core-design`，图表组件沉淀到 `wys_chart` 目标模块（见 C7）。
- F1.T 仓库四态 + 转化率/ROI/除零/排序算法断言。
- F1.I `PageNames` + `HomePage` 入口。
- **验收**：先修 analytics 单测路径（见 B 备注：用 `:feature-analytics:testDebugUnitTest`，因 `core-data/core-navigation` 无 jvm 变体），再逐页验收。

### F2 · settings（8816 行，最大 feature 之一）
- F2.M `SettingsModel`/`AddressModel`/`InvoiceModel`/`PurchaseCalcModel`/`PersonalizedSettings`。
- F2.R `SettingsRepo`/`AddressRepo`/`InvoiceRepo`/`PurchaseCalcRepo`（四态）。
- F2.P1 `SettingsPage`（分组列表 + 开关/跳转）。
- F2.P2 `DialogDemoPage`（各类弹窗 → 沉淀 `KDialog` 到 core-design）。
- F2.P3 `PurchaseCalculatorPage`（表单+计算+结果）。
- F2.P4 `PersonalizedSettingsPage` / `AddressListPage`+`AddressEditPage` / `MineProfilePage` / `MineHttpTestPage` / `DealInvoiceDemoPage`+`UploadPage`。
- F2.C `KDialog`/`KFormField`/`KSwitch` → core-design。
- F2.T 计算逻辑（购买计算器公式）、地址增删改查四态。
- F2.I 入口接 Home「我的」。

### F3 · bfui（UI 组件库，10261 行，多 feature 依赖）
- F3.M 无业务模型，纯展示组件。
- F3.C1 `NavigationHomeScreen` → `KBottomNav`（底部导航，多 feature 复用）。
- F3.C2 `BottomBarView` → 自定义底部栏动画。
- F3.C3 `TitleView`/`GlassView`/`WaterView`/`RunningView`/`MediterraneanDietView` → `core-design` 通用卡片/视图。
- F3.C4 `InviteFriendScreen`/`HelpScreen`/`FeedbackScreen` → 通用页面模板。
- F3.P `FitnessAppHomeScreen` + `TrainingScreen` + `MyDiaryScreen`（可独立成 demo 页或并入 home）。
- F3.T 组件可组合性 + 暗色适配。
- **验收**：bfui 组件被 ≥3 个其他 feature 复用才算完成。

### F4 · chat（4623 行，依赖 C4 rongcloud_im）
- F4.M `Conversation`/`Message`(text/image/voice/system)/`ChatUser`。
- F4.R `ChatRepository`（消息列表/收发/分页；真实对接走 C4，Mock 走 Fake）。
- F4.P1 `ChatPage`（会话列表 + 未读）。
- F4.P2 `ChatDetailPage`（消息流 + 输入栏 + 发送态）。
- F4.P3 `ImagePreviewPage`。
- F4.C `MessageListView`/`MessageBubble` → 本地或 core-design。
- F4.T 消息分页/时间戳分组/未读清零。
- F4.I 入口接 Home「消息」。

### F5 · auth（5218 行，基础，多 feature 依赖）
- F5.M `User`/`Session`/`OtpTicket`/`LoginResult`。
- F5.R `AuthRepository`（login/password/otp/register/logout；Token 假存储；未登录拦截）。
- F5.P1 `LoginPage`（账号密码）。
- F5.P2 `LoginPasswordPage` / `LoginOtpPage` / `RegisterPage`。
- F5.T 登录成功/失败/Token 过期/未登录拦截。
- F5.I 对接 `wys_login_share_pay`（C6）与全局路由守卫。

### F6 · community（5187 行）
- F6.M `Post`/`Topic`/`Comment`/`UserFeed`。
- F6.R `CommunityRepository`（feed/发布/搜索/话题；图片上传 Mock）。
- F6.P1 `CommunityPage`（信息流 + 下拉刷新 + 分页）。
- F6.P2 `PublishPage` / `SearchPage` / `ConventionPage` / `TopicSelectPage` / `VideoPlayPage` / `ImagePreviewPage`。
- F6.T feed 分页/发布成功/搜索过滤。
- F6.I 入口接 Home「动态」。

### F7 · video（4208 行）
- F7.M `Video`/`ShortVideo`/`DubbingTask`。
- F7.R `VideoRepository`（列表/播放/配音任务）。
- F7.P1 `VideoPage`（短列表/播放器壳）。
- F7.P2 `Dubbing` 子流程页。
- F7.T 列表分页/播放进度 Mock。

### F8 · pay（2981 行，依赖 C6）
- F8.M `Order`/`PayMethod`/`Membership`/`PayResult`。
- F8.R `PayRepository`（下单/支付/续费；Mock 成功/失败/取消）。
- F8.P1 `PayPage`（收银台 + 支付方式选择）。
- F8.P2 `MembershipRenewPage`。
- F8.T 支付四态 + 续费计算。
- F8.I 对接 C6 支付桥。

### F9 · mall（2896 行，依赖 C6）
- F9.M `Product`/`Cart`/`Order`/`OrderDetail`。
- F9.R `MallRepository`（商品/购物车/订单四态）。
- F9.P1 `MallPage`（商品网格）。
- F9.P2 `MallDetailPage` / `MallOrdersPage` / `MallOrderDetailPage`。
- F9.T 加购/下单/订单状态机。
- F9.I 对接 C6 分享/支付。

### F10 · classroom（3915 行）
- F10.M `Class`/`Homework`/`GiftCard`/`HomeworkStats`。
- F10.R `ClassroomRepository`（课程/作业/礼卡四态）。
- F10.P1 `MyClassListPage`。
- F10.P2 `HomeworkReviewPage` / `VideoDetailPage` / `DubbingHomeworkPage` / `HomeworkDetailTeacher` / `HomeworkDetailStudent` / `ClassHomeworkStatsPage` / `ClaimGiftCardPage`。
- F10.T 作业状态机/统计聚合。

### F11 · music（1122 行）
- F11.M `Track`/`Playlist`/`PlaybackState`。
- F11.R `MusicRepository`（列表/播放态 Mock）。
- F11.P1 `MusicListPage`。
- F11.P2 `NowPlayingPage`（播放控制条）。
- F11.T 播放态切换/列表排序。

### F12 · ai（832 行）
- F12.M `AiMessage`/`AiStreamChunk`。
- F12.R `AiRepository`（流式对话 Mock：逐字返回）。
- F12.P1 `AiStreamPage`（打字机流式 UI）。
- F12.T 流式分块/中断/重试。

### F13 · wallet（593 行）
- F13.M `Wallet`/`Balance`/`Transaction`。
- F13.R `WalletRepository`（余额/流水四态）。
- F13.P1 `WalletPage`（余额卡 + 交易列表）。
- F13.T 余额计算/流水分页。

### F14 · live（206 行）
- F14.M `LiveRoom`/`LiveUser`。
- F14.R `LiveRepository`（房间列表/在线人数 Mock）。
- F14.P1 `LivePage`（房间网格）。
- F14.P2 `LiveRoomPage`（播放器壳 + 弹幕占位）。
- F14.T 房间列表分页/人数更新。

### F15 · friend（868 行）
- F15.M `Friend`/`FriendGroup`。
- F15.R `FriendRepository`（列表/分组四态）。
- F15.P1 `FriendPage`（分组联系人列表 + 搜索）。
- F15.T 分组聚合/搜索过滤。

---

## 4. Component 能力映射（平台/基础库）

> 优先实现被 feature 依赖的能力；dokit 调试类优先级最低。

- **C7 wys_chart（604）**【任务 C7】：将 analytics 切片内的 `ConversionRing`/`MiniBarChart`/`FunnelChart`/`RadarBars`/`CompareBars` 沉淀为独立 `core-chart` 或 `platform-chart` 模块，供全项目复用；补充源 `wys_chart` 的其余图表类型。
- **C6 wys_login_share_pay（659）**【任务 C6】：桥接 `auth`/`mall`/`pay`，封装登录态共享、分享面板、支付拉起 → `platform-auth-share-pay` 模块（复用现有 `platform-share`）。
- **C4 rongcloud_im（1545）**【任务 C4】：IM SDK 封装，对接 `chat`（F4）；Mock 模式下用 Fake 消息。
- **C5 wys_push（1259）**【任务 C5】：推送 → `platform-push`（注册/接收/点击跳转）。
- **C8 wys_face_verify（142）**【任务 C8】：人脸核身 → `platform-face-verify`（Mock 通过/失败）。
- **C9 bluetooth（379）**【任务 C9】：蓝牙 → `platform-bluetooth`（扫描/连接 Mock）。
- **C3 linking（1525）**【任务 C3】：深度链接/scheme → 接入 B3 导航规范。
- **C2 realtime（2363）**【任务 C2】：WebSocket 实时封装 → `platform-realtime`（聊天/直播在线数复用）。
- **C1 dokit（11086）**【任务 C1】+ **C10 dokit_bootstrap（44）**【任务 C10】：调试工具箱，最后做；仅 Android/iOS 调试构建包含。

---

## 5. 统一验收门禁（每个切片必过）

| 编号 | 名称 | 判定标准 |
|------|------|----------|
| AC1 | 编译通过 | `./gradlew :app-shared:compileDebugKotlinAndroid` 绿（UI 页）；对应 `:feature-x:testDebugUnitTest` 绿（数据层/单测）。注：`compileKotlinMetadata` 在本工程对所有模块均为 SKIPPED，**不作为编译判定依据**。 |
| AC2 | 单测绿 | 仓库四态（Success/Empty/Error/Slow）全绿；关键算法（转化率/ROI/除零/排序/统计聚合）有断言。 |
| AC3 | UI 还原 | 列表/详情/空/错/加载 五态齐全；视觉与源项目截图**像素级近似**（用 core-design token，无硬编码色值）。 |
| AC4 | 四端竖切 | Android 真机/模拟器跑通；iOS 模拟器、鸿蒙（ohosApp）、H5（js browser）至少**编译通过且能导航到该页**。 |
| AC5 | 规范合规 | 无 Dart/Flutter 残留；遵循 `.codebuddy/rules/kuiklyDSL`+`kuiklyComposeDSL`；`@Page` 注册；经 `BridgeModule` 导航；数据走 `Store`/`Result`。 |
| AC6 | 文档闭环 | 在 `docs/SYNC_TRACKER.md` 标记完成 + 记录与源的差异/取舍。 |

**验收不合格处理**：回到该切片继续分解（通常拆成更细的子页或子组件）直到 AC1–AC6 全过；不得带病进入下一 feature。

---

## 6. 执行顺序（依赖驱动）

1. **基础层**：B1 core-design → B2 数据层 → B3 导航。（已部分：B1 初版、B3 初版、analytics Pilot）
2. **强依赖 feature**：F5 auth（其余依赖登录）→ F3 bfui（组件被多方复用）→ F1 home（聚合壳 + analytics）。
3. **业务 feature**（按规模与依赖）：F4 chat(C4) → F6 community → F9 mall(C6) → F8 pay(C6) → F7 video → F10 classroom → F11 music → F12 ai → F13 wallet → F14 live → F15 friend → F2 settings（最大，放后）。
4. **组件层**：C7 chart → C6 login_share_pay → C4 im → C5 push → C8 face → C9 bt → C3 linking → C2 realtime → C1/C10 dokit。
5. **四端验收**：P 阶段统一跑 Android/iOS/鸿蒙/H5 竖切 + 产出 runbooks。

> 说明：组件与 feature 可并行（如 C7 图表在 analytics 阶段已先行；C6 在 mall/pay 前完成）。

---

## 7. 追踪总表（与任务系统 ID 对应）

| 任务 ID | 切片 | 子任务数 | 状态 | 验收 |
|---------|------|----------|------|------|
| B1 | core-design 设计系统 | 5 | 进行中 | AC1-AC6 |
| B2 | 数据层基础 | 4 | 待办 | AC1-AC6 |
| B3 | 导航与注册 | 3 | 待办 | AC1-AC6 |
| F1 | home+analytics | 9 | 进行中(Pilot) | AC1-AC6 |
| F2 | settings | 9 | 待办 | AC1-AC6 |
| F3 | bfui | 4 | 待办 | AC1-AC6 |
| F4 | chat | 7 | 待办 | AC1-AC6 |
| F5 | auth | 6 | 待办 | AC1-AC6 |
| F6 | community | 7 | 待办 | AC1-AC6 |
| F7 | video | 4 | 待办 | AC1-AC6 |
| F8 | pay | 4 | 待办 | AC1-AC6 |
| F9 | mall | 5 | 待办 | AC1-AC6 |
| F10 | classroom | 8 | 待办 | AC1-AC6 |
| F11 | music | 4 | 待办 | AC1-AC6 |
| F12 | ai | 4 | 待办 | AC1-AC6 |
| F13 | wallet | 4 | 待办 | AC1-AC6 |
| F14 | live | 4 | 待办 | AC1-AC6 |
| F15 | friend | 4 | 待办 | AC1-AC6 |
| C7 | wys_chart | 1 | 进行中 | AC1-AC6 |
| C6 | login_share_pay | 1 | 待办 | AC1-AC6 |
| C4 | rongcloud_im | 1 | 待办 | AC1-AC6 |
| C5 | wys_push | 1 | 待办 | AC1-AC6 |
| C8 | wys_face_verify | 1 | 待办 | AC1-AC6 |
| C9 | bluetooth | 1 | 待办 | AC1-AC6 |
| C3 | linking | 1 | 待办 | AC1-AC6 |
| C2 | realtime | 1 | 待办 | AC1-AC6 |
| C1 | dokit | 1 | 待办 | AC1-AC6 |
| C10 | dokit_bootstrap | 1 | 待办 | AC1-AC6 |
| P | 四端竖切验收 | 4 | 待办 | AC1-AC6 |

> 子任务总数 ≈ **110+**。每个切片内子任务编号见第 3、4 节。详细进度维护在任务系统与本文档同步更新。
