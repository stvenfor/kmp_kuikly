package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mall.MallStore
import com.example.kuikly.data.mall.Product
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
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

@Page("Mall")
internal class MallPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var items by remember { mutableStateOf<List<Product>>(emptyList()) }
            var cartCount by remember { mutableStateOf(0) }
            var subtotal by remember { mutableStateOf("¥0.00") }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }
            var filter by remember { mutableStateOf(2) } // 0积分 1热兑 2上新 3筛选

            fun reload() {
                val list = if (MockBackend.scenario == MockScenario.Error) {
                    emptyList()
                } else {
                    MallStore.repo.browse(filter)
                }
                items = list
                empty = list.isEmpty()
                error = if (MockBackend.scenario == MockScenario.Error) "加载失败" else null
                cartCount = MallStore.repo.cartCount()
                subtotal = MallStore.repo.cartSubtotalLabel()
            }

            fun openCart() {
                Utils.currentBridgeModule().openPage(url = "MallCart", userData = JSONObject())
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("积分商城", fontSize = 20.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                    Text(
                        "我的订单 ›",
                        fontSize = 14.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable {
                            Utils.currentBridgeModule().openPage(url = "MallOrders", userData = JSONObject())
                        },
                    )
                }

                // 商品筛选 / 排序（port of source 积分 / 热兑 / 上新 / 筛选）
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    val tabs = listOf(0 to "积分", 1 to "热兑", 2 to "上新", 3 to "筛选")
                    tabs.forEach { (idx, label) ->
                        val selectedTab = filter == idx
                        Box(
                            modifier = Modifier.padding(end = 8.dp)
                                .background(
                                    if (selectedTab) KuiklyTheme.primary else KuiklyTheme.canvasSoft2,
                                    RoundedCornerShape(KuiklyTheme.radiusPill),
                                )
                                .clickable { filter = idx; reload() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                color = if (selectedTab) KuiklyTheme.onPrimary else KuiklyTheme.body,
                                fontWeight = if (selectedTab) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                when {
                    error != null && items.isEmpty() -> MallErrorState(message = error!!, onRetry = ::reload)
                    empty -> MallEmptyState(onRefresh = ::reload)
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        item {
                            MallCartBar(
                                count = cartCount,
                                subtotal = subtotal,
                                onOpen = ::openCart,
                                onClear = {
                                    MallStore.repo.clearCart()
                                    reload()
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        items(items) { p ->
                            MallProductTile(
                                product = p,
                                onOpen = {
                                    val data = JSONObject()
                                    data.put("id", p.id)
                                    Utils.currentBridgeModule().openPage(url = "MallDetail", userData = data)
                                },
                                onAdd = {
                                    MallStore.repo.addToCart(p.id, 1)
                                    Utils.currentBridgeModule().toast("已加入购物车")
                                    reload()
                                },
                            )
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
private fun MallCartBar(count: Int, subtotal: String, onOpen: () -> Unit, onClear: () -> Unit) {
    KCard(modifier = Modifier.clickable { onOpen() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("购物车 $count 件", fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text("合计 $subtotal", fontSize = 16.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                KButton(text = "去结算", enabled = count > 0, onClick = onOpen)
                Spacer(Modifier.height(6.dp))
                Text(
                    "清空",
                    fontSize = 12.sp,
                    color = KuiklyTheme.muted,
                    modifier = Modifier.clickable { onClear() },
                )
            }
        }
    }
}

@Composable
private fun MallProductTile(product: Product, onOpen: () -> Unit, onAdd: () -> Unit) {
    KCard(modifier = Modifier.clickable { onOpen() }) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.width(88.dp).height(88.dp)
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    product.title.take(1),
                    fontSize = 30.sp,
                    color = KuiklyTheme.hairlineStrong,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        product.title,
                        fontSize = 16.sp,
                        color = KuiklyTheme.foreground,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(product.priceLabel, fontSize = 16.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(product.subtitle ?: "", fontSize = 12.sp, color = KuiklyTheme.muted, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    product.badges.forEach { badge ->
                        KChip(badge, color = KuiklyTheme.violet)
                        Spacer(Modifier.width(6.dp))
                    }
                    if (product.soldLabel != null) {
                        Text(product.soldLabel!!, fontSize = 12.sp, color = KuiklyTheme.muted)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (product.isVirtual) "虚拟商品" else "库存 ${product.stockQty}",
                        fontSize = 12.sp,
                        color = KuiklyTheme.muted,
                    )
                    Box(
                        modifier = Modifier.background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusSm))
                            .clickable { onAdd() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("加入购物车", color = KuiklyTheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MallEmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无在售商品", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("点击重试或切换 Mock 场景", fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(12.dp))
        MallRetryButton(onRefresh)
    }
}

@Composable
private fun MallErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, fontSize = 14.sp, color = KuiklyTheme.error)
        Spacer(Modifier.height(16.dp))
        MallRetryButton(onRetry)
    }
}

@Composable
private fun MallRetryButton(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.clickable { onRetry() }
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("重试", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
