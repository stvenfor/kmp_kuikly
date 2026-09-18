package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 「策略」— Flutter `StrategyPage`（`features/home/lib/home/view/strategy_page.dart`）复刻
 * （Phase-2 / P2-W3）。
 *
 * **刻度**：`strategy_page.dart` **零** `.w/.h/.sp`（全裸逻辑 px），故一律裸 `dp`/`sp`，
 * 不加 `ProvideDesignScale`（判断法同 `TabBarTokens` / `ChatPalette` / W2c）。
 *
 * ponytail 天花板（不弹回）：
 * - 仪表盘 Flutter 用 `CustomPaint` + `SweepGradient(green→yellow→red)`；Kuikly `Brush` 无
 *   `sweepGradient` → 用**三段 60° 纯色弧**（#34C759 / #FFCC00 / #FF3B30）近似同一色序。
 * - 订阅按钮为 mock（Flutter `onPressed: () {}` 亦为空实现）。
 */
@Page(name = "Strategy", moduleId = "feature_home")
internal class StrategyPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var tab by remember { mutableStateOf(0) }
            var period by remember { mutableStateOf(4) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeDashboardPalette.background),
            ) {
                AppNavBarBar(
                    title = "策略",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = bottom.dp + 24.dp,
                    ),
                ) {
                    item { SubTabs(selected = tab, onSelect = { tab = it }) }
                    item { Spacer(Modifier.height(16.dp)) }
                    item { AssetGridCard(period = period, onPeriod = { period = it }) }
                    item { Spacer(Modifier.height(16.dp)) }
                    item { StrategyCard() }
                }
            }
        }
    }
}

private object HomeDashboardPalette {
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val fillSecondary = Color(0xFFE9E9EB)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val separator = Color(0xFFC6C6C8)
    val riseRed = Color(0xFFFF3B30)
    val fallGreen = Color(0xFF34C759)
}

private val STRATEGY_TABS = listOf("推荐", "逆向", "趋势")
private val STRATEGY_PERIODS = listOf("今年来", "近1周", "近1月", "近3月", "近1年")

private data class GridCell(val label: String, val value: String, val positive: Boolean)

private val STRATEGY_GRID = listOf(
    GridCell("A股", "+19.22%", true),
    GridCell("中债", "+3.15%", true),
    GridCell("黄金", "+8.76%", true),
    GridCell("港股", "+12.40%", true),
    GridCell("美股", "+15.88%", true),
    GridCell("原油", "-2.34%", false),
    GridCell("美元债", "-1.80%", false),
    GridCell("商品", "+4.56%", true),
    GridCell("现金", "+1.20%", true),
)

/** Flutter `_buildSubTabs`：16 文案 + 8 间隙 + 3 高 accent 条（active 24 宽）。 */
@Composable
private fun SubTabs(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        STRATEGY_TABS.forEachIndexed { index, label ->
            val active = index == selected
            Column(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .padding(end = if (index < STRATEGY_TABS.lastIndex) 32.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    label,
                    fontSize = 16.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) HomeDashboardPalette.labelPrimary else HomeDashboardPalette.labelSecondary,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(if (active) 24.dp else 0.dp)
                        .height(3.dp)
                        .background(HomeDashboardPalette.accent, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}

/** Flutter `_buildAssetGridCard`：说明 + 3 列九宫格 + 横滑周期。 */
@Composable
private fun AssetGridCard(period: Int, onPeriod: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeDashboardPalette.surface, RoundedCornerShape(12.dp))
            .border(0.5.dp, HomeDashboardPalette.separator, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text(
            "「大类资产九宫格策略」通过分散配置降低波动，帮助你在不同市场环境下保持稳健收益。",
            fontSize = 13.sp,
            // Flutter 真源 `sectionLabel.copyWith(color: labelPrimary, height: 1.5)`
            // → 13 × 1.5 = 19.5 sp。原 17.5 sp 漏抄 height。
            lineHeight = 19.5.sp,
            color = HomeDashboardPalette.labelPrimary,
        )
        Spacer(Modifier.height(16.dp))
        STRATEGY_GRID.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { cell ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            // Flutter `SliverGridDelegateWithFixedCrossAxisCount(childAspectRatio: 1.35)`
                            // → 高 = 宽 / 1.35，随卡片宽自适应；原固定 68.dp 只对 375 宽成立。
                            .aspectRatio(1.35f)
                            .background(
                                if (cell.positive) Color(0x14FF3B30) else Color(0x1434C759),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            cell.label,
                            fontSize = 12.sp,
                            color = HomeDashboardPalette.labelPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            cell.value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cell.positive) {
                                HomeDashboardPalette.riseRed
                            } else {
                                HomeDashboardPalette.fallGreen
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            STRATEGY_PERIODS.forEachIndexed { index, label ->
                val active = index == period
                Text(
                    label,
                    modifier = Modifier
                        .clickable { onPeriod(index) }
                        .padding(end = if (index < STRATEGY_PERIODS.lastIndex) 16.dp else 0.dp),
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) HomeDashboardPalette.accent else HomeDashboardPalette.labelSecondary,
                )
            }
        }
    }
}

/** Flutter `_buildStrategyCard`：标题/标签 + 收益率 + 仪表盘 + 定投进度 + 订阅 + 说明。 */
@Composable
private fun StrategyCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeDashboardPalette.surface, RoundedCornerShape(12.dp))
            .border(0.5.dp, HomeDashboardPalette.separator, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "黄金恐贪定投 · 第一期",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeDashboardPalette.labelPrimary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            HomeDashboardPalette.accent.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        "逆向",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeDashboardPalette.accent,
                    )
                }
            }
            Text(
                "如何跟投",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = HomeDashboardPalette.accent,
                modifier = Modifier.clickable {
                    Utils.currentBridgeModule().toast("「如何跟投」即将接入")
                },
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column {
                Text(
                    "-11.35%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeDashboardPalette.fallGreen,
                )
                Text("本期收益率", fontSize = 13.sp, color = HomeDashboardPalette.labelSecondary)
            }
            Spacer(Modifier.weight(1f))
            Gauge()
        }
        Spacer(Modifier.height(20.dp))
        Text("定投进度", fontSize = 13.sp, color = HomeDashboardPalette.labelSecondary)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(HomeDashboardPalette.fillSecondary, RoundedCornerShape(6.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(36f / 50f)
                        .height(24.dp)
                        .background(HomeDashboardPalette.accent, RoundedCornerShape(6.dp)),
                )
            }
            Text(
                "36 / 50",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDashboardPalette.labelPrimary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "本周已投 1 份",
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = HomeDashboardPalette.labelSecondary,
            )
            Box(
                modifier = Modifier
                    // Flutter 真源 `FilledButton.styleFrom(minimumSize: Size(72, 36), padding: EdgeInsets.symmetric(horizontal: 16))`
                    // → 最小宽度 72 dp。原实现 `height(36.dp) + 文字内 padding 16dp` 只 ~60 dp，
                    //   比 Flutter 视觉窄 12 dp，统一加 `widthIn(min = 72.dp)`。
                    .widthIn(min = 72.dp)
                    .height(36.dp)
                    .background(HomeDashboardPalette.accent, RoundedCornerShape(18.dp))
                    .clickable { Utils.currentBridgeModule().toast("已订阅「黄金恐贪定投」") }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("订阅", fontSize = 14.sp, color = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "在恐慌时买入、贪婪时卖出，通过定期定额降低择时压力，适合长期持有的投资者。",
            fontSize = 13.sp,
            // Flutter 真源 `sectionLabel.copyWith(height: 1.5)` → 13 × 1.5 = 19.5 sp。
            lineHeight = 19.5.sp,
            color = HomeDashboardPalette.labelSecondary,
        )
    }
}

/**
 * Flutter `_buildGauge`：88×56 位（88×44 弧 + 居中「63 中立」）。
 * 无 `Brush.sweepGradient` → 三段 60° 纯色弧（同色序）。
 */
@Composable
private fun Gauge() {
    Box(modifier = Modifier.size(88.dp, 56.dp)) {
        Canvas(
            modifier = Modifier
                .width(88.dp)
                .height(44.dp)
                // Flutter `_buildGauge` 用 `Stack(alignment: Alignment.bottomCenter)`
                // 把 88×44 弧放到 88×56 容器**底部**（顶部留 12dp 空隙，让出给居底文字）。
                // Kuikly `Box` 默认 `TopStart` → 弧会顶到 y=0，弧底比真源高 12dp。
                .align(Alignment.BottomCenter),
        ) {
            val stroke = Stroke(width = 8f, cap = StrokeCap.Round)
            val topLeft = Offset(size.width / 2f - 36f, size.height - 36f)
            val arcSize = Size(72f, 72f)
            drawArc(
                color = Color(0xFF34C759),
                startAngle = 180f,
                sweepAngle = 60.5f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = Color(0xFFFFCC00),
                startAngle = 240.5f,
                sweepAngle = 59f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = Color(0xFFFF3B30),
                startAngle = 299.5f,
                sweepAngle = 60.5f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        Text(
            "63 中立",
            modifier = Modifier.align(Alignment.BottomCenter),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = HomeDashboardPalette.labelPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
