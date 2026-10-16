package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.data.analytics.StrategyAssetCell
import com.example.kuikly.data.analytics.StrategyPlan
import com.example.kuikly.data.analytics.StrategyStore
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
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
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

private val STRATEGY_KINDS = listOf("推荐", "逆向", "趋势")
private val STRATEGY_PERIODS = listOf("今年来", "近1周", "近1月", "近3月", "近1年")

@Page("AnalyticsStrategy")
internal class AnalyticsStrategyPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var booted by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }
            var cells by remember { mutableStateOf<List<StrategyAssetCell>>(emptyList()) }
            var plans by remember { mutableStateOf<List<StrategyPlan>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var kindIndex by remember { mutableStateOf(0) }
            var periodIndex by remember { mutableStateOf(4) }

            fun load() {
                val cellResult = StrategyStore.repo.cells()
                val planResult = StrategyStore.repo.page(1, 50)
                val failure = cellResult.exceptionOrNull() ?: planResult.exceptionOrNull()
                if (failure != null) {
                    cells = emptyList()
                    plans = emptyList()
                    error = failure.message ?: "策略数据加载失败"
                } else {
                    cells = cellResult.getOrThrow()
                    plans = planResult.getOrThrow().items
                    error = null
                }
                isLoading = false
            }

            fun reload() {
                error = null
                cells = emptyList()
                plans = emptyList()
                isLoading = true
                booted = false
            }

            // First frame renders the loading state, the next one performs the fetch.
            if (!booted) {
                booted = true
            } else if (isLoading) {
                load()
            }

            val visiblePlans = plans.filter { it.kind == STRATEGY_KINDS[kindIndex] }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AnalyticsTopBar("策略分析", "大类资产九宫格 · 跟投计划")
                when {
                    isLoading -> AnalyticsLoadingBlock("策略数据加载中…")
                    error != null -> AnalyticsErrorBlock(error!!, onRetry = { reload() })
                    plans.isEmpty() && cells.isEmpty() ->
                        AnalyticsEmptyBlock("暂无策略数据", "切换 Mock 场景后重新加载", onRetry = { reload() })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        item {
                            AnalyticsSegments(
                                options = STRATEGY_KINDS,
                                selectedIndex = kindIndex,
                                cellWidth = 80,
                                onSelect = { kindIndex = it },
                            )
                        }
                        if (cells.isNotEmpty()) {
                            item {
                                AssetGridCard(cells = cells)
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                        item {
                            KCard {
                                Text(
                                    "区间收益",
                                    fontSize = 14.sp,
                                    color = KuiklyTheme.ink,
                                    fontWeight = FontWeight.Bold,
                                )
                                AnalyticsSegments(
                                    options = STRATEGY_PERIODS,
                                    selectedIndex = periodIndex,
                                    cellWidth = 64,
                                    onSelect = { periodIndex = it },
                                )
                                Text(
                                    "当前区间：${STRATEGY_PERIODS[periodIndex]} · 共 ${visiblePlans.size} 个跟投计划",
                                    fontSize = 12.sp,
                                    color = KuiklyTheme.muted,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                        if (visiblePlans.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${STRATEGY_KINDS[kindIndex]}暂无在投计划",
                                        fontSize = 13.sp,
                                        color = KuiklyTheme.muted,
                                    )
                                }
                            }
                        } else {
                            items(visiblePlans, key = { it.id }) { plan ->
                                StrategyPlanCard(plan = plan, periodLabel = STRATEGY_PERIODS[periodIndex])
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetGridCard(cells: List<StrategyAssetCell>) {
    KCard {
        Text(
            "「大类资产九宫格策略」通过分散配置降低波动，帮助你在不同市场环境下保持稳健收益。",
            fontSize = 13.sp,
            color = KuiklyTheme.body,
        )
        Spacer(Modifier.height(12.dp))
        cells.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { cell -> AssetGridCell(cell = cell) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AssetGridCell(cell: StrategyAssetCell) {
    // Source paints gains red and losses green; this design system has no green token.
    val color: Color = if (cell.positive) KuiklyTheme.error else KuiklyTheme.success
    Column(
        modifier = Modifier.width(96.dp)
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(KuiklyTheme.radiusSm))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(cell.label, fontSize = 12.sp, color = KuiklyTheme.body)
        Spacer(Modifier.height(4.dp))
        Text(cell.valueLabel, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StrategyPlanCard(plan: StrategyPlan, periodLabel: String) {
    val returnColor: Color = if (plan.positiveReturn) KuiklyTheme.error else KuiklyTheme.success
    KCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(plan.name, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                KChip(plan.kind, color = KuiklyTheme.violet)
            }
            Text("如何跟投", fontSize = 13.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column {
                Text(plan.returnLabel, fontSize = 28.sp, color = returnColor, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("本期收益率 · $periodLabel", fontSize = 12.sp, color = KuiklyTheme.muted)
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("恐贪指数", fontSize = 12.sp, color = KuiklyTheme.muted)
                Text(plan.gaugeScore.toString(), fontSize = 18.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
                Text(plan.gaugeLabel, fontSize = 11.sp, color = KuiklyTheme.violet)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("定投进度", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(6.dp))
        AnalyticsProgressBar(ratio = plan.progress, label = plan.progressLabel)
        Spacer(Modifier.height(10.dp))
        Text(
            "本周已投 ${plan.weeklyInvested} 份 · ${plan.summary}",
            fontSize = 12.sp,
            color = KuiklyTheme.body,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(plan.id, color = KuiklyTheme.muted)
            Spacer(Modifier.width(8.dp))
            KChip(plan.periodLabel, color = KuiklyTheme.link)
        }
    }
}
