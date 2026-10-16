package com.example.kuikly.design

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.ColumnScope
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/** Card surface on the near-white canvas, Vercel-style. */
@Composable
fun KCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(KuiklyTheme.spaceMd),
    ) { content() }
}

/** Primary filled button. */
@Composable
fun KButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val bg = if (enabled) KuiklyTheme.primary else KuiklyTheme.hairline
    val fg = if (enabled) KuiklyTheme.onPrimary else KuiklyTheme.muted
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { if (enabled) onClick() }
            .padding(horizontal = KuiklyTheme.spaceMd, vertical = KuiklyTheme.spaceSm),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = fg, fontSize = KuiklyTheme.textBodySm, fontWeight = FontWeight.Medium)
    }
}

/** Pill chip with soft tinted background derived from [color]. */
@Composable
fun KChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = KuiklyTheme.muted,
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(KuiklyTheme.radiusPill))
            .padding(horizontal = KuiklyTheme.spaceXs, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, fontSize = KuiklyTheme.textCaption, fontWeight = FontWeight.Bold)
    }
}

/** Compact metric pill: "UV 1.2w" style label. */
@Composable
fun KMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val color = if (accent) KuiklyTheme.link else KuiklyTheme.primary
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(KuiklyTheme.radiusSm))
            .padding(horizontal = KuiklyTheme.spaceXs, vertical = KuiklyTheme.spaceXs),
        contentAlignment = Alignment.Center,
    ) {
        Text("$label $value", color = color, fontSize = KuiklyTheme.textCaption, fontWeight = FontWeight.Bold)
    }
}
