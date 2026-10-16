package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.Utils
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/** Shared page chrome for the analytics sub pages: back button + title. */
@Composable
fun AnalyticsTopBar(title: String, subtitle: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹ 返回",
            fontSize = 16.sp,
            color = KuiklyTheme.link,
            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = KuiklyTheme.muted)
            }
        }
    }
}

/** Centred loading block (uses fillMaxSize only). */
@Composable
fun AnalyticsLoadingBlock(text: String = "加载中…") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, fontSize = 14.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text("首次进入与重新加载都会先展示本状态", fontSize = 12.sp, color = KuiklyTheme.muted)
        }
    }
}

/** Empty state with a retry action. */
@Composable
fun AnalyticsEmptyBlock(title: String, hint: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(title, fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
            if (hint.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(hint, fontSize = 13.sp, color = KuiklyTheme.muted)
            }
            Spacer(Modifier.height(14.dp))
            AnalyticsActionButton("重新加载", onRetry)
        }
    }
}

/** Error state with the raw message and a retry action. */
@Composable
fun AnalyticsErrorBlock(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("加载失败", fontSize = 15.sp, color = KuiklyTheme.error, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(message, fontSize = 13.sp, color = KuiklyTheme.muted)
            Spacer(Modifier.height(14.dp))
            AnalyticsActionButton("重试", onRetry)
        }
    }
}

/** Filled primary action button. */
@Composable
fun AnalyticsActionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clickable { onClick() }
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** Segmented control row built from fixed-width cells (no flexible sizing). */
@Composable
fun AnalyticsSegments(
    options: List<String>,
    selectedIndex: Int,
    cellWidth: Int = 72,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selectedIndex
            Box(
                modifier = Modifier.width(cellWidth.dp)
                    .background(
                        if (active) KuiklyTheme.linkSoft else KuiklyTheme.canvas,
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    color = if (active) KuiklyTheme.linkDeep else KuiklyTheme.body,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                )
            }
            if (index < options.size - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

/** Thin progress track with an inner filled bar, ratio in 0.0 .. 1.0. */
@Composable
fun AnalyticsProgressBar(ratio: Double, label: String) {
    val clamped = if (ratio < 0.0) 0.0 else if (ratio > 1.0) 1.0 else ratio
    Box(
        modifier = Modifier.fillMaxWidth().height(24.dp)
            .background(KuiklyTheme.hairline, RoundedCornerShape(KuiklyTheme.radiusSm)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(clamped.toFloat()).height(24.dp)
                .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusSm)),
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 12.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        }
    }
}
