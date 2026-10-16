package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.analytics.AnalyticsStore
import com.example.kuikly.data.analytics.formatUnixSeconds
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KMetricPill
import com.example.kuikly.design.KuiklyTheme
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

@Page("AnalyticsDetail")
internal class AnalyticsDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optInt("id")
        setContent {
            var scenario by remember { mutableStateOf(com.example.kuikly.data.mock.MockBackend.scenario) }
            val result = remember(id) { AnalyticsStore.repo.fetchById(id) }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                // Nav bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp, color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("数据分析详情", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                when {
                    id <= 0 || result.isFailure ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("未找到记录", color = KuiklyTheme.muted, fontSize = KuiklyTheme.textBodyMd)
                        }
                    else -> {
                        val item = result.getOrNull()!!
                        // Anomaly / Featured banners
                        if (item.flagAnomaly) {
                            Banner(text = "异常记录：请重点关注转化与风险指标", color = KuiklyTheme.error, bg = KuiklyTheme.errorSoft)
                        } else if (item.flagFeatured) {
                            Banner(text = "精选记录：表现优异，建议复用策略", color = KuiklyTheme.warningDeep, bg = KuiklyTheme.warningSoft)
                        }
                        LazyDetail(item = item, scenario = scenario, onCycle = { scenario = com.example.kuikly.data.mock.MockBackend.cycle() })
                    }
                }
            }
        }
    }
}

@Composable
private fun Banner(text: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusMd)).padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LazyDetail(item: com.example.kuikly.data.analytics.AnalyticsRecord, scenario: com.example.kuikly.data.mock.MockScenario, onCycle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        // Overview card
        KCard {
            Text(item.title, fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(item.subtitle, fontSize = 13.sp, color = KuiklyTheme.muted)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                KChip(item.status, color = KuiklyTheme.link)
                Spacer(Modifier.width(8.dp))
                KChip(item.category, color = KuiklyTheme.violet)
                Spacer(Modifier.width(8.dp))
                KChip(item.code, color = KuiklyTheme.muted)
            }
            Spacer(Modifier.height(10.dp))
            KVRow("负责人", item.ownerName)
            KVRow("团队", item.ownerTeam)
            KVRow("观测时间", formatUnixSeconds(item.observedAt))
            KVRow("来源系统", item.sourceSystem)
        }

        Spacer(Modifier.height(12.dp))

        // Conversion + metrics
        KCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConversionRing(rate = item.conversionRate, size = 96)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("转化率 (转化/点击)", fontSize = 13.sp, color = KuiklyTheme.muted)
                    Text(
                        item.conversionRate?.let { "%.1f%%".format(it * 100) } ?: "—",
                        fontSize = 22.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        KMetricPill("UV", item.metricUv.toString())
                        Spacer(Modifier.width(8.dp))
                        KMetricPill("点击", item.metricClick.toString())
                        Spacer(Modifier.width(8.dp))
                        KMetricPill("ROI", "%.2f".format(item.metricRoi), accent = true)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Funnel
        KCard {
            SectionTitle("流量漏斗 (PV 归一)")
            FunnelChart(
                stages = listOf(
                    "PV" to item.metricPv.toDouble(),
                    "UV" to item.metricUv.toDouble(),
                    "点击" to item.metricClick.toDouble(),
                    "转化" to item.metricConvert.toDouble(),
                ),
            )
        }

        Spacer(Modifier.height(12.dp))

        // Revenue vs cost
        KCard {
            SectionTitle("收入 / 成本")
            CompareBars(
                metrics = listOf(
                    "收入" to item.metricRevenue,
                    "成本" to item.metricCost,
                ),
            )
        }

        Spacer(Modifier.height(12.dp))

        // Quality / Risk / Stickiness radar
        KCard {
            SectionTitle("质量 · 风险 · 粘性雷达")
            RadarBars(
                metrics = listOf(
                    "质量" to item.scoreQuality,
                    "风险" to item.scoreRisk,
                    "粘性" to item.stickiness.toDouble(),
                ),
            )
        }

        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clickable { onCycle() }.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("[CYCLE MOCK SCENARIO] now=$scenario", color = KuiklyTheme.link, fontSize = 13.sp)
        }
    }
}

@Composable
private fun KVRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 13.sp, color = KuiklyTheme.muted, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 13.sp, color = KuiklyTheme.body)
    }
}
