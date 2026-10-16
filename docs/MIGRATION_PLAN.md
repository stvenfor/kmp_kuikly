# 迁移计划：my_ai_project (Flutter) → kmp_kuikly (Kuikly)

> 源：`/Users/stvenfor/Desktop/MyWork/my_ai_project`（Flutter + GetX，约 7.4 万行 Dart）
> 目标：`/Users/stvenfor/Desktop/MyWork/kmp_kuikly`（Kuikly / KMP + Compose DSL，Demo Skeleton）
> 依据：`CONTEXT.md`、`docs/SCHEME.md`、`docs/adr/*`、官方 Kuikly Rules（` .codebuddy/rules/`）

## 0. 原则

- **UI Track**：Compose DSL 为主（对齐 SCHEME ADR-0001）。Legacy DSL 仅做对照页，不承载业务。
- **全 Mock**：源项目依赖 Go BFF / gRPC（:9090）、Supabase、第三方 IM 等真实后端。迁移**不接真实后端**，全部下沉到 `core-data` 的 `MockBackend` 场景机（`Success/Empty/Error/Slow`）。
- **数据层模式（强制统一）**：

  ```kotlin
  data class XxxItem(...)                                   // 模型
  interface XxxRepository { fun list(): Result<List<XxxItem>> ... }
  class FakeXxxRepository : XxxRepository { /* 读 MockBackend.scenario */ }
  object XxxStore { val repo: XxxRepository = FakeXxxRepository() }
  ```

- **页面约定**：`@Page("Name") internal class XxxPage : BaseComposePager()`，`willInit()` 里 `setContent { }`，状态用 `mutableStateOf + remember`，导航用 `Utils.currentBridgeModule().openPage(...)`。
- **模块归属**：业务在 `feature-*`；平台能力在 `platform-*`（api + 分端 impl）；公共在 `core-*`。禁止 feature 互依、禁止 feature 依赖 Shell。
- **按需调用 Skills**：UI 用 `kuikly-ui-framework`、响应式用 `kuikly-reactive-observer`、网络用 `kuikly-network-and-json`、动画用 `kuikly-animation`、扩展能力用 `kuikly-expand-api`/`kuikly-expand-view`、Compose↔DSL 互操作用 `kuikly-compose-interop-dsl`、性能用 `kuikly-recomposition-analyzer`。

## 1. 源项目画像（规模）

| 模块 | 文件 | 行数 | 备注 |
|---|---|---|---|
| home | 141 | 20996 | 核心「数据分析」列表/详情 + 图表 |
| settings | 68 | 8816 | 设置（最大 feature 之一） |
| bfui | 55 | 10261 | 业务 UI 组件库 |
| chat | 41 | 4623 | 即时通讯 |
| auth | 38 | 5218 | 登录/会话 |
| community | 37 | 5187 | 社区 |
| video | 25 | 4208 | 视频 |
| pay | 22 | 2981 | 支付 |
| mall | 21 | 2896 | 商城 |
| classroom | 16 | 3915 | 课堂 |
| music | 14 | 1122 | 音乐 |
| ai | 11 | 832 | AI 助手 |
| wallet | 6 | 593 | 钱包 |
| live | 4 | 206 | 直播 |
| friend | 3 | 868 | 好友 |
| components/* | 209 | ~22k | dokit/realtime/linking/rongcloud_im/wys_push/wys_login_share_pay/wys_chart/wys_face_verify/bluetooth |

## 2. 架构映射表

| Flutter 概念 | Kuikly 落点 |
|---|---|
| `features/<name>/lib/**` | `feature-<name>/src/commonMain/kotlin/com/example/kuikly/{pages,data}` |
| `commons/*`（network/storage/core） | `core-data` / `core-common`（仅保留 Mock 需要的部分） |
| `components/wys_chart` | `platform-analytics-chart` 或 `core-design` 图表组件 |
| `components/wys_login_share_pay` | 复用 `feature-auth` + `platform-share` + `platform-pay` |
| `components/rongcloud_im` / `realtime` | `feature-chat` + `platform-im` 桩 |
| `lib/route` (GetX routes) | `core-navigation` `PageNames` + `BridgeModule.openPage` |
| `GetX GetxController + .obs` | Compose `mutableStateOf` + `remember` / `derivedStateOf` |
| `Get.find<T>()` 依赖注入 | `object XxxStore { val repo }`（编译期单例） |
| DESIGN.md token | `core-design` `KuiklyTheme` |
| gRPC/真实后端 | `core-data` `FakeXxxRepository` + `MockBackend` |

## 3. 设计 Token 映射（Vercel → KuiklyTheme）

源 `DESIGN.md` 为黑/白双调 + 青蓝紫品红渐变。迁移为 `core-design` 中的：
- `color`：primary `#171717`、onPrimary `#fff`、canvas `#fff`、canvasSoft `#fafafa`、muted `#888`、hairline `#ebebeb`、link `#0070f3`、success/error/warning、violet/cyan/highlight* 渐变起止色。
- `radius`：none/xs(4)/sm(6)/md(8)/lg(12)/xl(16)/pill(100)。
- `spacing`：xxs4 xs8 sm12 md16 lg24 xl32 …
- `typography`：display-xl(48)…caption(12)/caption-mono（等宽）映射为 Compose `TextStyle` 工厂。
- 基础组件壳：`KCard`、`KButton`、`KChip`、`KNavBar`、`KSummaryBar`、`KMetricPill`（对应源 `AnalyticsTheme` 用法）。

## 4. feature WBS 与验收标准

| # | feature | 页面（抽样） | 验收标准 |
|---|---|---|---|
| 1 | **analytics**（首批） | 列表(转化环+迷你柱+异常/精选色条)、详情(漏斗+雷达+指标) | 编译通过；Mock 四态齐全；列表→详情导航通；图表视觉对齐源稿；单测覆盖仓库四态 |
| 2 | auth | 登录/登出/拦截 | 未登录拦截、Token 假存、Mock 登录 |
| 3 | home | 多 Tab 壳 | 首页聚合 analytics 等入口 |
| 4 | chat | 会话列表/聊天 | 消息 Mock、发送态 |
| 5 | community | 广场/帖子 | 列表/详情 Mock |
| 6 | video | 播放/列表 | 列表 Mock、播放占位 |
| 7 | mall | 商品列表/详情 | 列表/详情 Mock |
| 8 | pay | 收银台 | 下单 Mock、结果态 |
| 9 | wallet | 余额/账单 | 列表 Mock |
| 10 | ai | AI 对话 | 对话流 Mock |
| 11 | music | 歌单/播放 | 列表 Mock |
| 12 | classroom | 课程 | 列表 Mock |
| 13 | live | 直播间 | 占位页 |
| 14 | friend | 好友 | 列表 Mock |
| 15 | bfui | 组件库 | 抽为 `core-design` 复用组件 |
| 16 | settings | 设置 | 分组列表 Mock |
| P | platform | 权限/分享/IM/图表/推送 桥接 | 对齐 `platform-*` 范式 |

## 5. 四端验收门禁

- Android：`./gradlew :androidApp:assembleDebug` + 跑通登录→analytics 竖切。
- iOS / 鸿蒙 / H5：本地 runbook 跑通同一竖切（见 `docs/runbooks/`）。
- 单测：各 `FakeXxxRepository` 四态 + 路由名常量为必测项。

## 6. 执行顺序

core-design（token 底座）→ **feature-analytics（首批验收切片）** → auth → home → chat/community/video → mall/pay/wallet/ai/music → classroom/live/friend/bfui/settings → platform 扩展 → 四端验收。每片验收不合格则继续分解直到通过。
