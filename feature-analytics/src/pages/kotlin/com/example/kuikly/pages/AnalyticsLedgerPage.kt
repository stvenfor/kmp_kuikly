package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.analytics.AnalyticsLedgerEntry
import com.example.kuikly.data.analytics.AnalyticsLedgerStore
import com.example.kuikly.data.analytics.LedgerSummary
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
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

private val LEDGER_TABS = listOf("全部", "收入", "支出")

@Page("AnalyticsLedger")
internal class AnalyticsLedgerPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var booted by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }
            var entries by remember { mutableStateOf<List<AnalyticsLedgerEntry>>(emptyList()) }
            var summary by remember { mutableStateOf<LedgerSummary?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var tabIndex by remember { mutableStateOf(0) }

            fun load() {
                val rows = AnalyticsLedgerStore.repo.page(1, 50)
                val totals = AnalyticsLedgerStore.repo.summary()
                val failure = rows.exceptionOrNull() ?: totals.exceptionOrNull()
                if (failure != null) {
                    entries = emptyList()
                    summary = null
                    error = failure.message ?: "账单加载失败"
                } else {
                    entries = rows.getOrThrow().items
                    summary = totals.getOrThrow()
                    error = null
                }
                isLoading = false
            }

            fun reload() {
                error = null
                entries = emptyList()
                summary = null
                isLoading = true
                booted = false
            }

            // First frame renders the loading state, the next one performs the fetch.
            if (!booted) {
                booted = true
            } else if (isLoading) {
                load()
            }

            val visibleEntries = when (tabIndex) {
                1 -> entries.filter { it.isIncome }
                2 -> entries.filter { !it.isIncome }
                else -> entries
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AnalyticsTopBar("账单流水", "收入 / 支出 / 结余")
                when {
                    isLoading -> AnalyticsLoadingBlock("账单加载中…")
                    error != null -> AnalyticsErrorBlock(error!!, onRetry = { reload() })
                    entries.isEmpty() ->
                        AnalyticsEmptyBlock("暂无账单记录", "切换 Mock 场景后重新加载", onRetry = { reload() })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        if (summary != null) {
                            item {
                                LedgerSummaryCard(summary = summary!!)
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                        item {
                            AnalyticsSegments(
                                options = LEDGER_TABS,
                                selectedIndex = tabIndex,
                                cellWidth = 84,
                                onSelect = { tabIndex = it },
                            )
                        }
                        if (visibleEntries.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${LEDGER_TABS[tabIndex]}暂无记录",
                                        fontSize = 13.sp,
                                        color = KuiklyTheme.muted,
                                    )
                                }
                            }
                        } else {
                            items(visibleEntries, key = { it.id }) { entry ->
                                LedgerRow(entry = entry, onTap = {
                                    Utils.currentBridgeModule().toast("${entry.category} ${entry.amountLabel}")
                                })
                                Spacer(Modifier.height(8.dp))
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
private fun LedgerSummaryCard(summary: LedgerSummary) {
    KCard {
        Text("本期汇总", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryCell("收入", summary.incomeLabel, KuiklyTheme.success)
            SummaryCell("支出", summary.expenseLabel, KuiklyTheme.error)
            SummaryCell("结余", summary.balanceLabel, KuiklyTheme.link)
        }
        Spacer(Modifier.height(10.dp))
        Text("共 ${summary.count} 笔记录", fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun SummaryCell(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LedgerRow(entry: AnalyticsLedgerEntry, onTap: () -> Unit) {
    val amountColor: Color = if (entry.isIncome) KuiklyTheme.success else KuiklyTheme.error
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onTap() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.category, fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                KChip(entry.typeLabel, color = amountColor)
            }
            Spacer(Modifier.height(4.dp))
            Text(entry.date, fontSize = 12.sp, color = KuiklyTheme.muted)
            if (entry.note.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(entry.note, fontSize = 12.sp, color = KuiklyTheme.body)
            }
        }
        Text(entry.amountLabel, fontSize = 16.sp, color = amountColor, fontWeight = FontWeight.Bold)
    }
}
