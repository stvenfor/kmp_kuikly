package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.pay.MemberCatalog
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 自动续费管理：签约详情 + 开关 + 关闭二次确认。
 *
 * port of `MembershipRenewBar` 的「《iHome会员协议》《iHome自动续费协议》」签约语义：
 * 源把协议勾选放在下单条上，这里把「已签约的连续包月/包年」单独成页，
 * 便于查看下次扣费日与关闭自动续费（关闭需二次确认）。
 */
@Page("PayAutoRenew")
internal class PayAutoRenewPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val orderNo = pageData.params.optString("orderNo")
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

            /** 写入签约开关：成功则同步列表状态并 toast。 */
            fun applyAutoRenew(orderNo: String, enabled: Boolean, onApplied: (PayOrder) -> Unit) {
                PayFlowStore.repo.setAutoRenew(orderNo, enabled)
                    .onSuccess { updated ->
                        onApplied(updated)
                        Utils.currentBridgeModule().toast(updated.message)
                    }
                    .onFailure { Utils.currentBridgeModule().toast("操作失败：${it.message}") }
            }

            // 未传订单号时取最近一笔成功订单作为签约单
            val signed = if (orderNo.isBlank()) {
                orders.firstOrNull { it.state == PayOrderState.Success }
            } else {
                orders.firstOrNull { it.orderNo == orderNo }
            }
            var pendingOff by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PayPalette.pageBackground),
            ) {
                AppNavBarBar(
                    title = "自动续费管理",
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

                    signed == null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "🔁",
                            title = "暂无自动续费签约",
                            message = "开通连续包月/包年后可在此管理",
                            actionLabel = "去开通",
                            onAction = {
                                Utils.currentBridgeModule().openPage(
                                    url = "PayVip",
                                    userData = JSONObject(),
                                )
                            },
                        )
                    }

                    else -> {
                        val o = signed!!
                        val accent = payAccent(o.tier)
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Spacer(Modifier.height(12.dp))
                            PaySignCard(order = o)
                            Spacer(Modifier.height(12.dp))
                            PayAutoRenewToggleCard(
                                order = o,
                                accent = accent,
                                pendingOff = pendingOff,
                                onRequestToggle = {
                                    if (o.autoRenew) {
                                        // 关闭需二次确认
                                        pendingOff = true
                                    } else {
                                        applyAutoRenew(o.orderNo, true) { updated ->
                                            orders = orders.map { item ->
                                                if (item.orderNo == updated.orderNo) updated else item
                                            }
                                        }
                                    }
                                },
                                onConfirmOff = {
                                    pendingOff = false
                                    applyAutoRenew(o.orderNo, false) { updated ->
                                        orders = orders.map { item ->
                                            if (item.orderNo == updated.orderNo) updated else item
                                        }
                                    }
                                },
                                onCancelOff = { pendingOff = false },
                            )
                            Spacer(Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = (bottom + 12f).dp,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "《趣配音会员协议》《自动续费协议》",
                                    fontSize = 11.sp,
                                    color = PayPalette.agreement,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

/** 签约信息卡。 */
@Composable
private fun PaySignCard(order: PayOrder) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (order.tier == PayTier.Svip) "SVIP" else "AI SVIP",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.titleBlack,
                maxLines = 1,
            )
            Spacer(Modifier.width(8.dp))
            Text(order.title, fontSize = 13.sp, color = PayPalette.textGray, maxLines = 1)
            Spacer(Modifier.weight(1f))
            PayStateBadge(state = order.state)
        }
        Spacer(Modifier.height(12.dp))
        PaySignRow(label = "签约渠道", value = order.channel.label)
        Spacer(Modifier.height(8.dp))
        PaySignRow(label = "续费金额", value = "¥${formatMoney(order.amount)}/${order.months}个月")
        Spacer(Modifier.height(8.dp))
        PaySignRow(label = "会员到期", value = order.expiresAtLabel)
        Spacer(Modifier.height(8.dp))
        PaySignRow(label = "下次扣费", value = order.nextChargeLabel)
        Spacer(Modifier.height(8.dp))
        PaySignRow(
            label = "每日均价",
            value = "¥${formatMoney(MemberCatalog.skuOf(order.skuId)?.perDay ?: 0.0)}",
        )
    }
}

@Composable
private fun PaySignRow(label: String, value: String) {
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

/** 开关卡：开启立即生效，关闭需二次确认。 */
@Composable
private fun PayAutoRenewToggleCard(
    order: PayOrder,
    accent: Color,
    pendingOff: Boolean,
    onRequestToggle: () -> Unit,
    onConfirmOff: () -> Unit,
    onCancelOff: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("到期自动续费", fontSize = 14.sp, color = PayPalette.titleBlack, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (order.autoRenew) "将于 ${order.nextChargeLabel} 自动扣款" else "已关闭，到期后不再扣款",
                    fontSize = 11.sp,
                    color = PayPalette.textGray,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(12.dp))
            PayAutoRenewSwitch(
                enabled = order.autoRenew,
                accent = accent,
                onToggle = onRequestToggle,
            )
        }
        if (pendingOff) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PayPalette.planFooterPeach, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                Column {
                    Text(
                        "关闭后 ${order.expiresAtLabel} 到期即失效，确认关闭？",
                        fontSize = 12.sp,
                        color = PayPalette.beanOrange,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "确认关闭",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PayPalette.beanOrange,
                            modifier = Modifier.clickable(onClick = onConfirmOff),
                        )
                        Spacer(Modifier.width(24.dp))
                        Text(
                            "再想想",
                            fontSize = 13.sp,
                            color = PayPalette.textGray,
                            modifier = Modifier.clickable(onClick = onCancelOff),
                        )
                    }
                }
            }
        }
    }
}
