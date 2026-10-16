package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/** "列表扫一眼图"：转化环（中心百分比徽标）。 */
@Composable
fun ConversionRing(rate: Double?, size: Int = 64, modifier: Modifier = Modifier) {
    val accent = KuiklyTheme.link
    Box(
        modifier = modifier
            .width(size.dp).height(size.dp)
            .background(accent.copy(alpha = 0.15f), RoundedCornerShape(KuiklyTheme.radiusPill))
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusPill)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (rate == null) "—" else "${(rate * 100).toInt()}%",
                color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            )
        }
    }
}

data class BarDatum(val label: String, val value: Double)

/** "列表扫一眼图"：以 PV 为 100% 归一的 PV/UV/点击/转化迷你柱。 */
@Composable
fun MiniBarChart(baseMax: Double, data: List<BarDatum>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        data.forEach { d ->
            val ratio = if (baseMax <= 0.0) 0.0 else (d.value / baseMax).coerceIn(0.0, 1.0)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
            ) {
                Box(
                    modifier = Modifier.height((ratio * 56).dp).fillMaxWidth()
                        .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusXs)),
                )
                Spacer(Modifier.height(4.dp))
                Text(d.label, fontSize = 11.sp, color = KuiklyTheme.muted)
            }
        }
    }
}

/** 详情图表面板：流量漏斗柱（PV→UV→点击→转化）。 */
@Composable
fun FunnelChart(stages: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val max = stages.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0
    Column(modifier = modifier.fillMaxWidth()) {
        stages.forEach { (label, value) ->
            val w = (value / max).coerceIn(0.0, 1.0)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            ) {
                Text(label, fontSize = 12.sp, color = KuiklyTheme.body, modifier = Modifier.width(48.dp))
                Box(
                    modifier = Modifier.weight(1f).height(22.dp)
                        .background(
                            KuiklyTheme.link.copy(alpha = (0.25f + w.toFloat() * 0.6f)),
                            RoundedCornerShape(KuiklyTheme.radiusXs),
                        ),
                )
                Text(
                    value.toInt().toString(),
                    fontSize = 12.sp, color = KuiklyTheme.muted, modifier = Modifier.width(56.dp),
                )
            }
        }
    }
}

/** 质量/风险/粘性雷达（Demo 用多轴条形雷达近似）。 */
@Composable
fun RadarBars(metrics: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        metrics.forEach { (label, score) ->
            val pct = (score.coerceIn(0.0, 100.0) / 100.0).toFloat()
            Text(label, fontSize = 12.sp, color = KuiklyTheme.body)
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .background(KuiklyTheme.hairline, RoundedCornerShape(KuiklyTheme.radiusPill)),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(pct).height(10.dp)
                        .background(KuiklyTheme.violet, RoundedCornerShape(KuiklyTheme.radiusPill)),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(score.toInt().toString(), fontSize = 11.sp, color = KuiklyTheme.muted)
            Spacer(Modifier.height(10.dp))
        }
    }
}

/** 收入/成本对比柱。 */
@Composable
fun CompareBars(metrics: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val max = metrics.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        metrics.forEach { (label, value) ->
            val ratio = (value / max).coerceIn(0.0, 1.0)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            ) {
                Box(
                    modifier = Modifier.height((ratio * 90).dp).fillMaxWidth()
                        .background(KuiklyTheme.cyan, RoundedCornerShape(KuiklyTheme.radiusXs)),
                )
                Spacer(Modifier.height(4.dp))
                Text(label, fontSize = 11.sp, color = KuiklyTheme.body)
                Text(value.toInt().toString(), fontSize = 11.sp, color = KuiklyTheme.muted)
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = KuiklyTheme.ink,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 8.dp),
    )
}
