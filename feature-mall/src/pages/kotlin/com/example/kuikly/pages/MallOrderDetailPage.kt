package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mall.MallPayChannel
import com.example.kuikly.data.mall.MallStore
import com.example.kuikly.data.mall.Order
import com.example.kuikly.data.mall.OrderStatus
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KButton
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
 * 订单详情页：状态步骤条 + 商品行 + 收货信息 + 支付渠道 + 操作区。
 */
@Page("MallOrderDetail")
internal class MallOrderDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            val orderId = pageData.params.optInt("id")
            var order by remember { mutableStateOf<Order?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                MallStore.repo.orderDetail(orderId)
                    .onSuccess { order = it; error = null }
                    .onFailure { order = null; error = it.message ?: "加载失败" }
            }

            if (!booted) {
                booted = true
                reload()
            }

            val o = order
            if (o == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(error ?: "订单不存在", fontSize = 14.sp, color = KuiklyTheme.error)
                    Spacer(Modifier.height(16.dp))
                    KButton(text = "重试", onClick = ::reload)
                }
                return@setContent
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
                    Text("订单详情", fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    item { OrderStepBar(o); Spacer(Modifier.height(10.dp)) }

                    item {
                        KCard {
                            Text("商品信息", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            o.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text("· ${item.productTitle} ×${item.qty}", fontSize = 14.sp, color = KuiklyTheme.body)
                                    Spacer(Modifier.width(8.dp))
                                    Text(item.lineAmountLabel, fontSize = 14.sp, color = KuiklyTheme.foreground)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        KCard {
                            Text("收货信息", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (o.hasReceiver) "${o.receiverName}  ${o.receiverPhone}\n${o.receiverAddress}"
                                else "未填写收货信息（虚拟商品无需收货）",
                                fontSize = 13.sp,
                                color = KuiklyTheme.body,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        KCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("支付渠道", fontSize = 14.sp, color = KuiklyTheme.body)
                                Text(o.payChannelLabel, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("实付金额", fontSize = 14.sp, color = KuiklyTheme.body)
                                Text(o.amountLabel, fontSize = 18.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                            }
                            if (o.payDeadlineAt.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("付款截止 ${o.payDeadlineAt}", fontSize = 12.sp, color = KuiklyTheme.muted)
                            }
                            if (o.paidAt.isNotBlank()) {
                                Text("付款时间 ${o.paidAt}", fontSize = 12.sp, color = KuiklyTheme.muted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        Column {
                            if (o.canPay) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    MallPayChannelRow(
                                        label = "微信支付",
                                        modifier = Modifier.width(110.dp),
                                        onClick = {
                                            MallStore.repo.payOrder(o.orderId, MallPayChannel.wechat)
                                            Utils.currentBridgeModule().toast("支付成功")
                                            reload()
                                        },
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    MallPayChannelRow(
                                        label = "支付宝",
                                        modifier = Modifier.width(110.dp),
                                        onClick = {
                                            MallStore.repo.payOrder(o.orderId, MallPayChannel.alipay)
                                            Utils.currentBridgeModule().toast("支付成功")
                                            reload()
                                        },
                                    )
                                }
                            } else if (o.canConfirmReceipt) {
                                KButton(
                                    text = "确认收货",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        MallStore.repo.advanceOrder(o.orderId)
                                        Utils.currentBridgeModule().toast("已确认收货")
                                        reload()
                                    },
                                )
                            } else if (o.canCancel) {
                                KButton(
                                    text = "取消订单",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        MallStore.repo.cancelOrder(o.orderId)
                                        Utils.currentBridgeModule().toast("订单已取消")
                                        reload()
                                    },
                                )
                            }
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
private fun MallPayChannelRow(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** 状态步骤条：待付款 -> 待发货 -> 待收货 -> 已完成。 */
@Composable
private fun OrderStepBar(order: Order) {
    KCard {
        val step = order.stepIndex
        Text(order.statusLabel, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OrderStatus.flowLabels.forEachIndexed { index, label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
                    Box(
                        modifier = Modifier.width(20.dp).height(20.dp)
                            .background(
                                if (index <= step) KuiklyTheme.link else KuiklyTheme.hairline,
                                RoundedCornerShape(10.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            fontSize = 11.sp,
                            color = if (index <= step) KuiklyTheme.onPrimary else KuiklyTheme.muted,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        fontSize = 11.sp,
                        color = if (index <= step) KuiklyTheme.link else KuiklyTheme.muted,
                        maxLines = 1,
                    )
                }
                if (index < OrderStatus.flowLabels.size - 1) {
                    Box(
                        modifier = Modifier.width(12.dp).height(2.dp)
                            .background(if (index < step) KuiklyTheme.link else KuiklyTheme.hairline),
                    )
                }
            }
        }
    }
}
