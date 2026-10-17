package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
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
 * 购物车页：复选 / 数量增删改 / 全选 / 选中合计 / 删除选中 / 去结算。
 * 选中态由 [com.example.kuikly.data.mall.MallStore] 的仓库维护，保证结算页能读取同一份数据。
 */
@Page("MallCart")
internal class MallCartPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var lines by remember { mutableStateOf<List<CartLine>>(emptyList()) }
            var selectedSet by remember { mutableStateOf<Set<String>>(emptySet()) }
            var subtotal by remember { mutableStateOf("¥0.00") }
            var count by remember { mutableStateOf(0) }
            var allSel by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                lines = MallStore.repo.cartLines()
                selectedSet = MallStore.repo.selectedKeys()
                subtotal = MallStore.repo.selectedSubtotalLabel()
                count = MallStore.repo.selectedCount()
                allSel = MallStore.repo.allSelected()
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
                    Text("购物车", fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("${MallStore.repo.cartCount()} 件", fontSize = 13.sp, color = KuiklyTheme.muted)
                }

                when {
                    lines.isEmpty() -> MallCartEmpty()
                    else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        MallStore.repo.selectAll(!allSel)
                                        reload()
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MallCheckbox(checked = allSel)
                                Spacer(Modifier.width(8.dp))
                                Text("全选", fontSize = 14.sp, color = KuiklyTheme.foreground)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        items(lines) { line ->
                            MallCartItem(
                                line = line,
                                checked = selectedSet.contains(line.productId),
                                onToggle = {
                                    MallStore.repo.toggleSelect(line.productId)
                                    reload()
                                },
                                onMinus = {
                                    if (line.qty > 1) {
                                        MallStore.repo.setCartQty(line.productId, line.qty - 1)
                                        reload()
                                    } else {
                                        MallStore.repo.removeFromCart(line.productId)
                                        reload()
                                    }
                                },
                                onPlus = {
                                    MallStore.repo.setCartQty(line.productId, line.qty + 1)
                                    reload()
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                if (lines.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvas)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "删除",
                            fontSize = 13.sp,
                            color = KuiklyTheme.error,
                            modifier = Modifier.clickable {
                                if (count > 0) {
                                    MallStore.repo.removeSelected()
                                    reload()
                                    Utils.currentBridgeModule().toast("已删除选中商品")
                                } else {
                                    Utils.currentBridgeModule().toast("请先选择商品")
                                }
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("已选 $count 件", fontSize = 12.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(2.dp))
                            Text("合计 $subtotal", fontSize = 16.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                        }
                        KButton(
                            text = "去结算",
                            enabled = count > 0,
                            onClick = {
                                val data = JSONObject()
                                Utils.currentBridgeModule().openPage(url = "MallCheckout", userData = data)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MallCartItem(
    line: CartLine,
    checked: Boolean,
    onToggle: () -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    KCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.clickable { onToggle() }) {
                MallCheckbox(checked = checked)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier.width(64.dp).height(64.dp)
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm)),
                contentAlignment = Alignment.Center,
            ) {
                Text(line.title.take(1), fontSize = 24.sp, color = KuiklyTheme.hairlineStrong, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(line.title, fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(line.unitPriceLabel, fontSize = 15.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.width(28.dp).height(28.dp)
                            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                            .clickable { onMinus() },
                        contentAlignment = Alignment.Center,
                    ) { Text("−", fontSize = 16.sp, color = KuiklyTheme.foreground) }
                    Text("${line.qty}", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), maxLines = 1)
                    Box(
                        modifier = Modifier.width(28.dp).height(28.dp)
                            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                            .clickable { onPlus() },
                        contentAlignment = Alignment.Center,
                    ) { Text("+", fontSize = 16.sp, color = KuiklyTheme.foreground) }
                }
            }
            Text(line.lineAmountLabel, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MallCheckbox(checked: Boolean) {
    Box(
        modifier = Modifier.width(20.dp).height(20.dp)
            .background(
                if (checked) KuiklyTheme.link else KuiklyTheme.canvas,
                RoundedCornerShape(4.dp),
            )
            .padding(3.dp)
            .background(
                if (checked) KuiklyTheme.link else KuiklyTheme.hairline,
                RoundedCornerShape(3.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Text("✓", fontSize = 11.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MallCartEmpty() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("购物车还是空的", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("去积分商城挑点好物吧", fontSize = 13.sp, color = KuiklyTheme.muted)
    }
}
