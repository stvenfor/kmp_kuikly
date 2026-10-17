package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mall.Address
import com.example.kuikly.data.mall.CartLine
import com.example.kuikly.data.mall.MallStore
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 结算页：地址选择（内联，port of source 地址选择桥）+ 订单摘要 + 提交订单（port of source createOrder）。
 * 实体商品需收货地址；虚拟商品无需地址直接结算。
 */
@Page("MallCheckout")
internal class MallCheckoutPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var lines by remember { mutableStateOf<List<CartLine>>(emptyList()) }
            var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
            var addrId by remember { mutableStateOf("") }
            var subtotal by remember { mutableStateOf("¥0.00") }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                lines = MallStore.repo.selectedLines()
                addresses = MallStore.repo.addresses()
                val current = MallStore.repo.selectedAddress()
                addrId = current?.id ?: addresses.firstOrNull()?.id ?: ""
                subtotal = MallStore.repo.selectedSubtotalLabel()
            }

            if (!booted) {
                booted = true
                reload()
            }

            val physical = lines.any { it.kind != 1 }
            val address = addresses.firstOrNull { it.id == addrId }

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
                    Text("确认订单", fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                if (lines.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("没有可结算的商品", fontSize = 14.sp, color = KuiklyTheme.muted)
                    }
                    return@setContent
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    item {
                        // 收货地址（实体商品必填）
                        KCard {
                            Text("收货信息", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(10.dp))
                            if (physical) {
                                addresses.forEach { addr ->
                                    val active = addr.id == addrId
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable {
                                                MallStore.repo.selectAddress(addr.id)
                                                addrId = addr.id
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier.width(18.dp).height(18.dp)
                                                .background(
                                                    if (active) KuiklyTheme.link else KuiklyTheme.canvas,
                                                    RoundedCornerShape(9.dp),
                                                )
                                                .padding(4.dp)
                                                .background(
                                                    if (active) KuiklyTheme.link else KuiklyTheme.hairline,
                                                    RoundedCornerShape(9.dp),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (active) {
                                                Text("✓", fontSize = 10.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("${addr.name}", fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                                                Spacer(Modifier.width(8.dp))
                                                Text(addr.phone, fontSize = 13.sp, color = KuiklyTheme.body)
                                                if (addr.tag.isNotBlank()) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(addr.tag, fontSize = 11.sp, color = KuiklyTheme.link)
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(addr.detail, fontSize = 13.sp, color = KuiklyTheme.muted, maxLines = 2)
                                        }
                                    }
                                }
                            } else {
                                Text("虚拟商品 · 支付后自动发放，无需收货地址", fontSize = 13.sp, color = KuiklyTheme.muted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        KCard {
                            Text("商品清单", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            lines.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("· ${line.title}", fontSize = 14.sp, color = KuiklyTheme.body, modifier = Modifier.weight(1f), maxLines = 1)
                                    Spacer(Modifier.width(8.dp))
                                    Text("x${line.qty}", fontSize = 13.sp, color = KuiklyTheme.muted)
                                    Spacer(Modifier.width(8.dp))
                                    Text(line.lineAmountLabel, fontSize = 14.sp, color = KuiklyTheme.foreground)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        KCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("实付金额", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                                Text(subtotal, fontSize = 18.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("应付", fontSize = 12.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(2.dp))
                        Text(subtotal, fontSize = 18.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                    }
                    KButton(
                        text = "提交订单",
                        modifier = Modifier.width(150.dp),
                        onClick = {
                            if (physical && address == null) {
                                Utils.currentBridgeModule().toast("请选择收货地址")
                                return@KButton
                            }
                            val result = if (address != null) {
                                MallStore.repo.placeSelectedOrder(
                                    receiverName = address.name,
                                    receiverPhone = address.phone,
                                    receiverAddress = address.detail,
                                )
                            } else {
                                MallStore.repo.placeSelectedOrder()
                            }
                            result
                                .onSuccess {
                                    val data = JSONObject()
                                    data.put("id", it.orderId)
                                    Utils.currentBridgeModule().toast("订单已创建，请在 15 分钟内支付")
                                    Utils.currentBridgeModule().openPage(url = "MallOrderDetail", userData = data)
                                    Utils.currentBridgeModule().closePage()
                                }
                                .onFailure { Utils.currentBridgeModule().toast(it.message ?: "提交失败") }
                        },
                    )
                }
            }
        }
    }
}
