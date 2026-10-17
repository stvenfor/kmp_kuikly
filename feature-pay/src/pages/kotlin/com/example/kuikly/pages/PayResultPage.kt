package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.pay.PayFlowStore
import com.example.kuikly.data.pay.PayOrder
import com.example.kuikly.data.pay.PayOrderState
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
 * 支付结果页（下单状态机的终态展示层）。
 *
 * port of `MembershipRenewController.renewNow` 的分支 toast：
 * 成功 / 处理中 / 已取消 / 失败 / 超时 —— 这里把一次性 toast 固化成可回溯的结果页，
 * 并提供「重新支付 / 刷新状态 / 查看订单」的续作入口。
 */
@Page("PayResult")
internal class PayResultPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val orderNo = pageData.params.optString("orderNo")
        setContent {
            var order by remember { mutableStateOf<PayOrder?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun load() {
                PayFlowStore.repo.detail(orderNo)
                    .onSuccess {
                        order = it
                        error = null
                    }
                    .onFailure {
                        order = null
                        error = it.message ?: "订单不存在"
                    }
            }

            if (!booted) {
                booted = true
                load()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PayPalette.pageBackground),
            ) {
                AppNavBarBar(
                    title = "支付结果",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )

                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "未找到订单",
                            message = error ?: "订单不存在",
                            actionLabel = "返回",
                            onAction = { Utils.currentBridgeModule().closePage() },
                        )
                    }

                    order != null -> {
                        val o = order!!
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            PayResultHeader(order = o)
                            Spacer(Modifier.height(12.dp))
                            PayResultCard(order = o)
                            Spacer(Modifier.weight(1f))
                            PayResultActions(
                                order = o,
                                bottomInset = bottom,
                                onReload = {
                                    // 处理中 / 失败 / 超时 都可重新发起支付（状态机回到受理分支）
                                    val settled = PayFlowStore.repo.pay(o).getOrNull()
                                    if (settled != null) {
                                        order = settled
                                        Utils.currentBridgeModule().toast(settled.state.label)
                                    } else {
                                        Utils.currentBridgeModule().toast("支付失败，请稍后重试")
                                    }
                                },
                                onOrders = {
                                    Utils.currentBridgeModule().openPage(
                                        url = "PayOrders",
                                        userData = JSONObject(),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 结果头部：状态字形 + 状态文案 + 渠道返回信息。 */
@Composable
private fun PayResultHeader(order: PayOrder) {
    val state = order.state
    val accent = payStateColor(state)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.cardWhite)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            when (state) {
                PayOrderState.Success -> "✅"
                PayOrderState.Processing, PayOrderState.Paying, PayOrderState.Creating -> "⏳"
                PayOrderState.Cancelled -> "🚫"
                else -> "⚠️"
            },
            fontSize = 44.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (state == PayOrderState.Success) "开通成功" else state.label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
        )
        if (order.message.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(order.message, fontSize = 13.sp, color = PayPalette.textGray, maxLines = 1)
        }
        if (state == PayOrderState.Success) {
            Spacer(Modifier.height(6.dp))
            Text(
                "会员有效期至 ${order.expiresAtLabel}",
                fontSize = 13.sp,
                color = PayPalette.textGray,
                maxLines = 1,
            )
        }
    }
}

/** 订单明细白卡。 */
@Composable
private fun PayResultCard(order: PayOrder) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        PayResultRow(label = "订单号", value = order.orderNo)
        Spacer(Modifier.height(8.dp))
        PayResultRow(label = "套餐", value = "${order.title}（${order.months}个月）")
        Spacer(Modifier.height(8.dp))
        PayResultRow(label = "实付", value = "¥${formatMoney(order.amount)}")
        Spacer(Modifier.height(8.dp))
        PayResultRow(label = "支付方式", value = order.channel.label)
        Spacer(Modifier.height(8.dp))
        PayResultRow(
            label = "自动续费",
            value = if (order.autoRenew) "已开启（${order.nextChargeLabel} 扣款）" else "未开启",
        )
        Spacer(Modifier.height(8.dp))
        PayResultRow(label = "下单时间", value = order.createdAtLabel)
    }
}

@Composable
private fun PayResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = PayPalette.textGray, maxLines = 1)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            fontSize = 13.sp,
            color = PayPalette.titleBlack,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

/** 底部操作：终态不同，主按钮语义不同。 */
@Composable
private fun PayResultActions(
    order: PayOrder,
    bottomInset: Float,
    onReload: () -> Unit,
    onOrders: () -> Unit,
) {
    val state = order.state
    val primary = when (state) {
        PayOrderState.Success -> "查看订单"
        PayOrderState.Processing, PayOrderState.Paying -> "刷新支付状态"
        PayOrderState.Created -> "继续支付"
        else -> "重新支付"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.cardWhite)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = (bottomInset + 12f).dp),
    ) {
        PayGradientCta(
            label = primary,
            gradient = payCtaGradient(order.tier),
            onClick = if (state == PayOrderState.Success) onOrders else onReload,
        )
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "返回会员页",
                fontSize = 13.sp,
                color = PayPalette.textGray,
                modifier = Modifier
                    .clickable { Utils.currentBridgeModule().closePage() }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}
