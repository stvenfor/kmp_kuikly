package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.analytics.AnalyticsRecord
import com.example.kuikly.data.analytics.AnalyticsStore
import com.example.kuikly.data.mock.MockBackend
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
import com.tencent.kuikly.compose.foundation.layout.IntrinsicSize
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("AnalyticsList")
internal class AnalyticsListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var items by remember { mutableStateOf<List<AnalyticsRecord>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(false) }
            var isLoadingMore by remember { mutableStateOf(false) }
            var hasMore by remember { mutableStateOf(true) }
            var total by remember { mutableStateOf(0) }
            var page by remember { mutableStateOf(0) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenario = MockBackend.scenario
                isLoading = true
                AnalyticsStore.repo.fetchPage(1)
                    .onSuccess { r ->
                        items = r.items
                        total = r.total
                        hasMore = r.hasMore
                        page = if (r.items.isEmpty()) 0 else 1
                        empty = r.items.isEmpty()
                        error = null
                    }
                    .onFailure {
                        items = emptyList()
                        empty = false
                        error = it.message ?: "加载失败"
                    }
                isLoading = false
            }

            fun loadMore() {
                if (isLoadingMore || !hasMore) return
                isLoadingMore = true
                val next = page + 1
                AnalyticsStore.repo.fetchPage(next)
                    .onSuccess { r ->
                        if (r.items.isEmpty()) {
                            hasMore = false
                        } else {
                            items = items + r.items
                            page = next
                            total = r.total
                            hasMore = r.hasMore
                        }
                    }
                isLoadingMore = false
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                if (items.isNotEmpty()) {
                    SummaryBar(page = page, total = total, count = items.size)
                }
                when {
                    isLoading && items.isEmpty() ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("加载中…", color = KuiklyTheme.muted, fontSize = KuiklyTheme.textBodyMd)
                        }
                    error != null && items.isEmpty() -> ErrorState(message = error!!, onRetry = ::reload)
                    empty -> EmptyState(onRefresh = ::reload)
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            AnalyticsTile(item = item, onTap = {
                                val data = JSONObject().apply { put("id", item.id) }
                                Utils.currentBridgeModule()
                                    .openPage(url = "AnalyticsDetail", userData = data)
                            })
                            Spacer(Modifier.height(8.dp))
                        }
                        if (isLoadingMore) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("加载更多…", color = KuiklyTheme.muted, fontSize = KuiklyTheme.textBodySm)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBar(page: Int, total: Int, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("已加载 $count / 共 $total · 第 $page 页", fontSize = 13.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        KChip("gRPC", color = KuiklyTheme.link)
    }
}

@Composable
private fun AnalyticsTile(item: AnalyticsRecord, onTap: () -> Unit) {
    val cue: Color? = when {
        item.flagAnomaly -> KuiklyTheme.error
        item.flagFeatured -> KuiklyTheme.warning
        else -> null
    }
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))) {
        Box(modifier = Modifier.width(4.dp).fillMaxSize().background(cue ?: KuiklyTheme.canvas))
        Column(
            modifier = Modifier.fillMaxWidth().clickable { onTap() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                StatusChip(status = item.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(item.subtitle, fontSize = 13.sp, color = KuiklyTheme.muted, maxLines = 1)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConversionRing(rate = item.conversionRate, size = 64)
                Spacer(Modifier.width(12.dp))
                MiniBarChart(
                    baseMax = if (item.metricPv > 0) item.metricPv.toDouble() else 1.0,
                    data = listOf(
                        BarDatum("PV", item.metricPv.toDouble()),
                        BarDatum("UV", item.metricUv.toDouble()),
                        BarDatum("点", item.metricClick.toDouble()),
                        BarDatum("转", item.metricConvert.toDouble()),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                KMetricPill("UV", compact(item.metricUv))
                Spacer(Modifier.width(8.dp))
                KMetricPill("ROI", "%.2f".format(item.metricRoi), accent = true)
                Spacer(Modifier.weight(1f))
                Text(item.code, fontSize = 12.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Bold)
                Text("›", fontSize = 20.sp, color = KuiklyTheme.muted)
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color: Color = when (status) {
        "active" -> KuiklyTheme.success
        "paused" -> KuiklyTheme.warning
        "archived" -> KuiklyTheme.muted
        else -> KuiklyTheme.link
    }
    KChip(status, color = color)
}

@Composable
private fun EmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无分析数据", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("点击重试或切换 Mock 场景", fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(12.dp))
        RetryButton(onRefresh)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, fontSize = 14.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(16.dp))
        RetryButton(onRetry)
    }
}

@Composable
private fun RetryButton(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.clickable { onRetry() }
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("重试", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private fun compact(n: Int): String = when {
    n >= 10000 -> "${String.format("%.1f", n / 10000.0)}w"
    n >= 1000 -> "${String.format("%.1f", n / 1000.0)}k"
    else -> "$n"
}
