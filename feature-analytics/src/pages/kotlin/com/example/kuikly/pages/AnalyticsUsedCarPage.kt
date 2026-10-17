package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.analytics.UsedCarAggregate
import com.example.kuikly.data.analytics.UsedCarOrder
import com.example.kuikly.data.analytics.UsedCarOrderStats
import com.example.kuikly.data.analytics.UsedCarOrderStore
import com.example.kuikly.data.analytics.aggregateUsedCars
import com.example.kuikly.data.analytics.depreciationRateLabel
import com.example.kuikly.data.analytics.estimatedValueLabel
import com.example.kuikly.data.analytics.formatUnixSeconds
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KMetricPill
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
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

private val USED_CAR_TABS = listOf("全部", "待审核", "已通过", "未通过")

@Page("AnalyticsUsedCar")
internal class AnalyticsUsedCarPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var booted by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }
            var orders by remember { mutableStateOf<List<UsedCarOrder>>(emptyList()) }
            var stats by remember { mutableStateOf<UsedCarOrderStats?>(null) }
            var carAnalysis by remember { mutableStateOf<UsedCarAggregate?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var tabIndex by remember { mutableStateOf(0) }

            fun load() {
                val rows = UsedCarOrderStore.repo.page(1, 50)
                val counters = UsedCarOrderStore.repo.stats()
                val failure = rows.exceptionOrNull() ?: counters.exceptionOrNull()
                if (failure != null) {
                    orders = emptyList()
                    stats = null
                    carAnalysis = null
                    error = failure.message ?: "业务单加载失败"
                } else {
                    val items = rows.getOrThrow().items
                    orders = items
                    stats = counters.getOrThrow()
                    carAnalysis = aggregateUsedCars(items)
                    error = null
                }
                isLoading = false
            }

            fun reload() {
                error = null
                orders = emptyList()
                stats = null
                carAnalysis = null
                isLoading = true
                booted = false
            }

            // First frame renders the loading state, the next one performs the fetch.
            if (!booted) {
                booted = true
            } else if (isLoading) {
                load()
            }

            val visibleOrders = when (tabIndex) {
                1 -> orders.filter { it.status == "pending_review" }
                2 -> orders.filter { it.status == "approved_pending_rating" || it.status == "rated" }
                3 -> orders.filter { it.isRejected }
                else -> orders
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AnalyticsTopBar("二手车业务单", "置换 / 专卖 / 收车")
                when {
                    isLoading -> AnalyticsLoadingBlock("业务单加载中…")
                    error != null -> AnalyticsErrorBlock(error!!, onRetry = { reload() })
                    orders.isEmpty() ->
                        AnalyticsEmptyBlock("暂无业务单", "切换 Mock 场景后重新加载", onRetry = { reload() })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        if (stats != null) {
                            item {
                                UsedCarStatsCard(stats = stats!!)
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                        if (carAnalysis != null) {
                            item {
                                UsedCarValuationCard(analysis = carAnalysis!!)
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                        item {
                            AnalyticsSegments(
                                options = USED_CAR_TABS,
                                selectedIndex = tabIndex,
                                cellWidth = 76,
                                onSelect = { tabIndex = it },
                            )
                        }
                        if (visibleOrders.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${USED_CAR_TABS[tabIndex]}暂无业务单",
                                        fontSize = 13.sp,
                                        color = KuiklyTheme.muted,
                                    )
                                }
                            }
                        } else {
                            items(visibleOrders, key = { it.orderId }) { order ->
                                UsedCarOrderCard(order = order, onTap = {
                                    Utils.currentBridgeModule().toast("${order.orderId} ${order.statusLabel}")
                                })
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsedCarStatsCard(stats: UsedCarOrderStats) {
    KCard {
        Text("工单概览", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatsCell("提交", stats.submitted.toString(), KuiklyTheme.foreground)
            StatsCell("待审核", stats.pendingReview.toString(), KuiklyTheme.warningDeep)
            StatsCell("已通过", stats.approved.toString(), KuiklyTheme.success)
            StatsCell("未通过", stats.rejected.toString(), KuiklyTheme.error)
        }
    }
}

@Composable
private fun StatsCell(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 18.sp, color = color, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

/** 估值总览：GMV、均价、按类型拆分。 */
@Composable
private fun UsedCarValuationCard(analysis: UsedCarAggregate) {
    KCard {
        Text("估值总览", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatsCell("总 GMV", analysis.gmvLabel, KuiklyTheme.foreground)
            StatsCell("均价", analysis.avgAmountLabel, KuiklyTheme.link)
            StatsCell("单数", analysis.count.toString(), KuiklyTheme.violet)
        }
        Spacer(Modifier.height(10.dp))
        analysis.byKind.forEach { kind ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(kind.kindLabel, fontSize = 13.sp, color = KuiklyTheme.body, modifier = Modifier.width(56.dp))
                Spacer(Modifier.width(8.dp))
                AnalyticsProgressBar(ratio = if (analysis.gmv <= 0.0) 0.0 else kind.totalAmount / analysis.gmv, label = "${kind.count} 单 · 均 ${kind.avgAmountLabel}")
            }
        }
    }
}

@Composable
private fun UsedCarOrderCard(order: UsedCarOrder, onTap: () -> Unit) {
    val statusColor: Color = when (order.status) {
        "pending_review" -> KuiklyTheme.warningDeep
        "rejected" -> KuiklyTheme.error
        "rated" -> KuiklyTheme.success
        else -> KuiklyTheme.link
    }
    KCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(order.vehicleModel, fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(order.plateNo, fontSize = 12.sp, color = KuiklyTheme.muted)
            }
            KChip(order.statusLabel, color = statusColor)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(order.kindLabel, color = KuiklyTheme.violet)
            Spacer(Modifier.width(8.dp))
            KMetricPill(order.amountLabel, order.amountYuanLabel, accent = true)
            Spacer(Modifier.width(8.dp))
            KMetricPill("里程", order.mileageLabel)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KMetricPill("当前估值", order.estimatedValueLabel, accent = true)
            Spacer(Modifier.width(8.dp))
            KChip("折旧率 ${order.depreciationRateLabel}", color = KuiklyTheme.warning)
        }
        Spacer(Modifier.height(10.dp))
        KVLine("车架号", order.vin)
        KVLine("年款", order.modelYearLabel)
        KVLine("客户", "${order.customerName} · ${order.phone}")
        KVLine("提交时间", formatUnixSeconds(order.submittedAt))
        if (order.ratingStars > 0) {
            KVLine("评分", "★".repeat(order.ratingStars))
        }
        if (order.rejectReason.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(order.rejectReason, fontSize = 12.sp, color = KuiklyTheme.errorDeep)
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(KuiklyTheme.canvasSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                .clickable { onTap() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("查看单号 ${order.orderId}", fontSize = 13.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun KVLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, fontSize = 12.sp, color = KuiklyTheme.muted, modifier = Modifier.width(64.dp))
        Text(value, fontSize = 12.sp, color = KuiklyTheme.body)
    }
}
