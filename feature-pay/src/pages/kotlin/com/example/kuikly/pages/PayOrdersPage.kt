package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.pay.MemberCatalog
import com.example.kuikly.data.pay.MemberEntitlement
import com.example.kuikly.data.pay.PayFlowStore
import com.example.kuikly.data.pay.PayOrder
import com.example.kuikly.data.pay.PayOrderState
import com.example.kuikly.data.pay.PayTier
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 会员订单列表：汇总（累计消费 / 生效中权益）+ 全部订单（点击进入 `PayResult`）。
 *
 * 复用 `BaseMockRepository` 四态：Error → 失败态重试；Empty → 空态。
 */
@Page("PayOrders")
internal class PayOrdersPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        setContent {
            var orders by remember { mutableStateOf<List<PayOrder>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                PayFlowStore.repo.orders()
                    .onSuccess {
                        orders = it
                        error = null
                    }
                    .onFailure {
                        orders = emptyList()
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            fun open(order: PayOrder) {
                val params = JSONObject()
                params.put("orderNo", order.orderNo)
                Utils.currentBridgeModule().openPage(url = "PayResult", userData = params)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PayPalette.pageBackground),
            ) {
                AppNavBarBar(
                    title = "会员订单",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )

                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "加载失败",
                            message = error ?: "加载失败",
                            actionLabel = "重试",
                            onAction = { reload() },
                        )
                    }

                    orders.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "🧾",
                            title = "暂无订单",
                            message = "开通会员后可在此查看订单与权益",
                            actionLabel = "去开通",
                            onAction = {
                                Utils.currentBridgeModule().openPage(
                                    url = "PayVip",
                                    userData = JSONObject(),
                                )
                            },
                        )
                    }

                    else -> LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            PayOrdersSummary(orders = orders)
                            Spacer(Modifier.height(12.dp))
                        }
                        items(orders) { order ->
                            PayOrderRow(order = order, onClick = { open(order) })
                            Spacer(Modifier.height(10.dp))
                        }
                        item {
                            Text(
                                "[CYCLE MOCK] ${MockBackend.scenario}",
                                fontSize = 12.sp,
                                color = PayPalette.textGray,
                                modifier = Modifier.clickable {
                                    MockBackend.cycle()
                                    reload()
                                },
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/** 汇总卡：累计消费 + 两档权益的到期日（会员权益计算）。 */
@Composable
private fun PayOrdersSummary(orders: List<PayOrder>) {
    val paid = orders.filter { it.state == PayOrderState.Success }
    val total = paid.sumOf { it.amount }
    val svip = MemberCatalog.entitlement(PayTier.Svip, orders)
    val aiSvip = MemberCatalog.entitlement(PayTier.AiSvip, orders)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "累计消费",
                fontSize = 13.sp,
                color = PayPalette.textGray,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "¥${formatMoney(total)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.priceBlack,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "成功 ${paid.size} 笔 · 共 ${orders.size} 笔",
            fontSize = 12.sp,
            color = PayPalette.textGray,
            maxLines = 1,
        )
        Spacer(Modifier.height(10.dp))
        PayEntitlementLine(label = "SVIP", entitlement = svip)
        Spacer(Modifier.height(4.dp))
        PayEntitlementLine(label = "AI SVIP", entitlement = aiSvip)
    }
}

@Composable
private fun PayEntitlementLine(label: String, entitlement: MemberEntitlement) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = PayPalette.titleBlack, maxLines = 1)
        Spacer(Modifier.width(8.dp))
        Text(
            if (entitlement.active) "生效中 · ${entitlement.expiresAt} 到期" else "未开通",
            fontSize = 12.sp,
            color = if (entitlement.active) PayPalette.svipPromoAccent else PayPalette.textGrayLight,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (entitlement.active) {
            Text(entitlement.cta, fontSize = 12.sp, color = PayPalette.beanOrange, maxLines = 1)
        }
    }
}
