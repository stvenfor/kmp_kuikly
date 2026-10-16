package com.example.kuikly.platform.chart

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

enum class ChartKind { conversionRing, miniBar, funnel, radar, compare }

/** 图表规格描述（供能力中心/预览使用；实际 Compose 组件位于 feature-analytics 图表库）。 */
data class ChartSpec(val kind: ChartKind, val name: String, val desc: String)

/** 图表能力目录（port of source wys_chart 封装）。 */
object ChartCatalog {
    val specs: List<ChartSpec> = listOf(
        ChartSpec(ChartKind.conversionRing, "转化环", "转化率环形图"),
        ChartSpec(ChartKind.miniBar, "迷你柱", "多指标对比小柱图"),
        ChartSpec(ChartKind.funnel, "漏斗", "转化漏斗"),
        ChartSpec(ChartKind.radar, "雷达", "质量/风险/粘性雷达"),
        ChartSpec(ChartKind.compare, "对比条", "收入/成本对比"),
    )

    fun load(): Result<List<ChartSpec>> =
        if (MockBackend.scenario == MockScenario.Error) Result.failure(IllegalStateException("chart catalog error"))
        else Result.success(specs)

    fun scenarioLabel(): String = when (MockBackend.scenario) {
        MockScenario.Success -> "可用"
        MockScenario.Empty -> "空数据"
        MockScenario.Error -> "不可用"
        MockScenario.Slow -> "慢速"
    }
}
