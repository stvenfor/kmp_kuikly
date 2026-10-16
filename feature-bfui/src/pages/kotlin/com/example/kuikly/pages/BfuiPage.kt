package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.bfui.BfuiShowcase
import com.example.kuikly.data.bfui.BfuiStore
import com.example.kuikly.data.mock.MockBackend
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
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 业务 UI 组件库展示页（port of source features/bfui）。
 *
 * 组件按分类分组，每项提供「预览卡 + 说明」；点击可看组件说明气泡。
 */
@Page("BfuiShowcase")
internal class BfuiPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var items by remember { mutableStateOf<List<BfuiShowcase>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                BfuiStore.repo.page(1, 50)
                    .onSuccess { items = it.items; error = null }
                    .onFailure { items = emptyList(); error = it.message ?: "加载失败" }
            }

            if (!booted) {
                booted = true
                reload()
            }

            val categoryLabels = mapOf(
                "navigation" to "导航",
                "card" to "卡片",
                "chart" to "图表",
                "action" to "操作",
                "animation" to "动画",
                "business" to "业务",
            )
            val grouped = items.groupBy { it.category }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("UI 组件库", fontSize = 20.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("共 ${items.size} 个", fontSize = 12.sp, color = KuiklyTheme.muted)
                }

                when {
                    error != null && items.isEmpty() -> BfuiEmpty("加载失败：${error!!}")
                    items.isEmpty() -> BfuiEmpty("暂无可展示组件")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        grouped.forEach { (category, list) ->
                            item {
                                Text(
                                    categoryLabels[category] ?: category,
                                    fontSize = 15.sp,
                                    color = KuiklyTheme.foreground,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            items(list) { item -> BfuiRow(item) }
                            item { Spacer(Modifier.height(12.dp)) }
                        }
                        item {
                            Text(
                                "[CYCLE MOCK] $MockBackend.scenario",
                                fontSize = 12.sp,
                                color = KuiklyTheme.muted,
                                modifier = Modifier.clickable { MockBackend.cycle(); reload() },
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BfuiRow(item: BfuiShowcase) {
    KCard(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 组件预览占位（用首字 + 色块表达，无真实位图资源）
            Box(
                modifier = Modifier.width(52.dp).height(52.dp)
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.name.take(1),
                    fontSize = 22.sp,
                    color = KuiklyTheme.hairlineStrong,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(item.name, fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(item.description, fontSize = 12.sp, color = KuiklyTheme.muted)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().toast("${item.name}：${item.description}")
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KChip(item.id, color = KuiklyTheme.violet)
                    Spacer(Modifier.width(6.dp))
                    Text("查看说明 ›", fontSize = 12.sp, color = KuiklyTheme.link)
                }
            }
        }
    }
}

@Composable
private fun BfuiEmpty(hint: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(hint, fontSize = 14.sp, color = KuiklyTheme.muted)
    }
}
