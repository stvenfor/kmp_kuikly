# Kuikly 同步实施手册（并行 agent 必读）

本文件是所有并行实施 agent 的**唯一事实来源**。写 Kotlin 之前先通读，违反"红线"的代码一律会被打回重做。

工程根目录：`/Users/stvenfor/Desktop/MyWork/kmp_kuikly`
源项目（Flutter+GetX）：`/Users/stvenfor/Desktop/MyWork/my_ai_project`

---

## 0. 绝对红线（违反即编译失败）

1. **不要运行 Gradle / 任何构建命令**。多个 agent 并发跑 Gradle 会争抢工程锁导致全部失败。你只负责写代码，中心节点统一编译验收。
2. **不要修改以下公共文件**（由中心节点统一处理）：
   - `settings.gradle.kts`
   - `app-shared/build.gradle.kts`
   - `core-navigation/src/commonMain/kotlin/com/example/kuikly/navigation/PageNames.kt`
   - `app-shared/src/commonMain/kotlin/com/example/kuikly/pages/HomePage.kt`
   - `core-data/**`、`core-design/**`（如需新组件/基类，先在报告里提出，不要自己改）
3. **只动分配给你自己的模块目录**（`feature-xxx/**` 或 `platform-xxx/**`）。
4. **Kotlin 注释里不能出现 `/*`**（Kotlin 块注释可嵌套，写了就"Unclosed comment"）。例如禁止写 `components/*`，改写 `components 下各模块`。
5. **禁止使用的 API**（本项目 Kuikly Compose 未提供）：
   - `Modifier.fillMaxHeight()` → 用 `fillMaxSize()`
   - `Modifier.weight(...)` → 本项目无先例，改用纯 `Column` 堆叠或固定尺寸
   - `toStringAsFixed(...)`、`Spacer(Modifier.height = x)` 之类 Dart 写法 → 用 Kotlin `"%.1f".format(...)` / `Spacer(Modifier.height(x.dp))`

---

## 1. 包名与文件位置约定

feature 模块是**两层结构**：

| 目录 | 放什么 | 包名示例 |
|---|---|---|
| `feature-xxx/src/commonMain/kotlin/.../data/xxx/` | 模型 + Repository + Store | `com.example.kuikly.data.xxx` |
| `feature-xxx/src/pages/kotlin/com/example/kuikly/pages/` | `@Page` 页面（UI） | `com.example.kuikly.pages` |
| `feature-xxx/src/commonTest/kotlin/.../data/xxx/` | 单元测试 | `com.example.kuikly.data.xxx` |

platform 模块：`platform-xxx/src/commonMain/kotlin/com/example/kuikly/platform/xxx/`，包名 `com.example.kuikly.platform.xxx`。

> `src/pages` 会被 `app-shared` 通过 `kotlin.srcDir` 聚合，KSP 在那里扫描 `@Page`。**页面必须放在 `src/pages` 下**，放错位置不会被注册。

---

## 2. 数据层范式（照抄，别自创）

Mock 相关 API 的真实包名是 `com.example.kuikly.data.mock`（**不是** `com.example.kuikly.data`）：

```kotlin
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario   // Success / Empty / Error / Slow
```

标准结构（每个 feature 一个 `XxxData.kt`）：

```kotlin
package com.example.kuikly.data.xxx

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockScenario

data class Foo(
    val id: String,
    val title: String,
    val amount: Double = 0.0,        // Kotlin 字面量敏感：Double 必须写 0.0，不能写 0
    val count: Int = 0,
    val enabled: Boolean = false,
)

class XxxRepository : BaseMockRepository<Foo>() {
    override fun seed(): List<Foo> = listOf(/* ... */)
    override fun idOf(item: Foo): String = item.id

    fun detail(id: String): Result<Foo> = detailOf(id)
    fun submit(text: String): Result<Foo> =
        if (text.isBlank()) Result.failure(IllegalArgumentException("内容不能为空"))
        else Result.success(Foo(...))
}

object XxxStore {
    val repo = XxxRepository()
}
```

`BaseMockRepository<T>`（`com.example.kuikly.data.BaseMockRepository`）已提供的方法，**只有这 4 个**：

```kotlin
fun page(page: Int, pageSize: Int = 20, filter: (T) -> Boolean = { true }): Result<Paged<T>>
fun byId(id: String): Result<T>
fun all(filter: (T) -> Boolean = { true }): Result<List<T>>
// + 需要实现的抽象成员：
protected abstract fun seed(): List<T>
protected abstract fun idOf(item: T): String
```

- `Paged<T>` 字段：`items` / `total` / `page` / `pageSize`，另有 `hasMore`、`isEmpty` 两个 get 属性。
- 基类已自动响应四态：Error → `Result.failure`；Empty → 空列表；Success/Slow → `seed()` 全量。
- **不要臆造 `detailOf` / `list` / `total()` 等方法**，调用不存在的方法会直接编译失败。

---

## 3. 页面范式（照抄）

```kotlin
package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.xxx.Foo
import com.example.kuikly.data.xxx.XxxStore
import com.example.kuikly.data.mock.MockBackend
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("Xxx")
internal class XxxPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var items by remember { mutableStateOf<List<Foo>>(emptyList()) }
            // ... UI
        }
    }
}
```

### 关键 API

- **`@Composable` 来自 `androidx.compose.runtime.Composable`**（不是 `com.tencent.kuikly.compose.runtime.*`）。
- **页面类必须 `internal class Xxx : BaseComposePager()`**，`@Page("名字")` 里的名字全局唯一。
- **列表**：`LazyColumn { items(list) { item -> ... } }`。
- **间距**：`Spacer(Modifier.height(12.dp))`（注意是函数调用，不是 `Modifier.height =`）。
- **背景+圆角**：`Modifier.background(Color(0xFFF7F7F8), RoundedCornerShape(10.dp))`。
- **点击**：`Modifier.clickable { ... }`。
- **对齐**：`Row(verticalAlignment = Alignment.CenterVertically)`、`Row(horizontalArrangement = Arrangement.SpaceBetween)`。
- **取值委托**：`var x by remember { mutableStateOf(0) }`。

### 导航

```kotlin
// 打开新页面并传参
val params = JSONObject()
params.put("id", item.id)
Utils.currentBridgeModule().openPage(url = "XxxDetail", userData = params)

// 关闭当前页（返回）
Utils.currentBridgeModule().closePage()

// 提示
Utils.currentBridgeModule().toast("操作成功")
```

### 详情页读参数

```kotlin
val id = pageData.params.optString("id")   // 或 optInt("id")
```

---

## 4. 设计规范（core-design）

签名已核实，**参数必须完全一致**：

```kotlin
@Composable fun KCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)
@Composable fun KButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit = {})
@Composable fun KChip(text: String, modifier: Modifier = Modifier, color: Color = KuiklyTheme.muted)
@Composable fun KMetricPill(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false)
```

注意 `KCard` 的尾参是带 receiver 的 lambda（`ColumnScope`），调用写成：

```kotlin
KCard { Text("hi") }
```

### KuiklyTheme 已有成员（全部核实过，可直接用）

- **颜色**：`primary` `onPrimary` `foreground` `ink` `body` `muted` `canvas` `canvasSoft` `canvasSoft2` `hairline` `hairlineStrong` `link` `linkDeep` `linkSoft` `success` `error` `errorSoft` `errorDeep` `warning` `warningSoft` `warningDeep` `violet` `violetSoft` `cyan` `cyanSoft` `highlightPink` `highlightMagenta` `anomaly` `featured`
- **圆角**：`radiusNone` `radiusXs` `radiusSm` `radiusMd` `radiusLg` `radiusXl` `radiusPill`
- **间距**：`spaceXxs` `spaceXs` `spaceSm` `spaceMd` `spaceLg` `spaceXl`
- **字号**：`textCaption` `textBodySm` `textBodyMd` `textTitle` `textDisplaySm` `textDisplayMd`
- 另外有 `chartColors: List<Color>`

设计基调：Vercel 极简风 —— 卡片白底 `canvas`、主文字 `foreground`/`ink`、次要文字 `muted`、链接色 `link`、分隔线 `hairline`。

---

## 5. 测试范式

放 `src/commonTest/kotlin/com/example/kuikly/data/xxx/XxxRepositoryTest.kt`：

```kotlin
package com.example.kuikly.data.xxx

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class XxxRepositoryTest {
    @Test
    fun `list success`() {
        MockBackend.scenario = MockScenario.Success
        val r = XxxRepository().page(1, 50).getOrThrow()
        assertTrue(r.items.isNotEmpty())
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(XxxRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success   // 务必复位，否则污染其它用例
    }
}
```

要求：**改了 `MockBackend.scenario` 的用例必须在结尾复位为 Success**（测试在同一 JVM 内串行共享全局状态）。

---

## 6. 参考样例（写之前先读）

- 完整列表页：`feature-analytics/src/pages/kotlin/com/example/kuikly/pages/AnalyticsListPage.kt`
- 完整详情页：`feature-analytics/src/pages/kotlin/com/example/kuikly/pages/AnalyticsDetailPage.kt`
- 图表组件（不用 Canvas）：`feature-analytics/src/pages/kotlin/com/example/kuikly/pages/AnalyticsCharts.kt`
- 简单列表页：`feature-chat/src/pages/kotlin/com/example/kuikly/pages/ChatListPage.kt`

---

## 7. 交付要求

完成后用 ≤150 字汇报：新增/修改了哪些文件、新增了哪些 `@Page` 名（中心节点要拿去注册）、有哪些你不确定能否编译的点。
