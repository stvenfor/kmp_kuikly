package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mall.MallStore
import com.example.kuikly.data.mall.Order
import com.example.kuikly.data.mall.OrderStatus
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KButton
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
 * 订单列表页：状态筛选 + 订单卡片 + 主操作按钮。
 */
@Page("MallOrders")
internal class MallOrdersPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var filter by remember { mutableStateOf(-1) } // -1 = 全部
            var items by remember { mutableStateOf<List<Order>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                val result = if (filter < 0) MallStore.repo.orders() else MallStore.repo.orders(filter)
                result
                    .onSuccess {
                        items = it
                        error = null
                    }
                    .onFailure {
                        items = emptyList()
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("我的订单", fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                // 状态筛选（横向 chip）
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    val tabs = listOf(-1 to "全部") + OrderStatus.flow.map { it to OrderStatus.label(it) } + listOf(OrderStatus.cancelled to "已取消")
                    tabs.forEach { (status, label) ->
                        val selected = filter == status
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    if (selected) KuiklyTheme.primary else KuiklyTheme.canvasSoft2,
                                    RoundedCornerShape(KuiklyTheme.radiusPill),
                                )
                                .clickable { filter = status; reload() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                when {
                    error != null && items.isEmpty() -> MallOrdersEmpty("加载失败：${error!!}")
                    items.isEmpty() -> MallOrdersEmpty("暂无相关订单")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        items(items) { order ->
                            MallOrderRow(order = order, onOpen = {
                                val data = com.tencent.kuikly.core.nvi.serialization.json.JSONObject()
                                data.put("id", order.orderId)
                                Utils.currentBridgeModule().openPage(url = "MallOrderDetail", userData = data)
                            })
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "[CYCLE MOCK] $MockBackend.scenario",
                                fontSize = 12.sp,
                                color = KuiklyTheme.muted,
                                modifier = Modifier.clickable { MockBackend.cycle(); reload() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MallOrderRow(order: Order, onOpen: () -> Unit) {
    com.example.kuikly.design.KCard(modifier = Modifier.clickable { onOpen() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("订单 ${order.orderNo}", fontSize = 12.sp, color = KuiklyTheme.muted, maxLines = 1)
            KChip(order.statusLabel, color = if (order.isUnpaid) KuiklyTheme.warning else KuiklyTheme.link)
        }
        Spacer(Modifier.height(8.dp))
        Text(order.primaryTitle, fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium, maxLines = 1)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("共 ${order.totalQty} 件 · 实付 ${order.amountLabel}", fontSize = 13.sp, color = KuiklyTheme.body)
            Text(
                "${order.primaryActionLabel} ›",
                fontSize = 13.sp,
                color = KuiklyTheme.link,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MallOrdersEmpty(hint: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(hint, fontSize = 14.sp, color = KuiklyTheme.muted)
    }
}
