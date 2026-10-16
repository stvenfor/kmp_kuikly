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
import com.example.kuikly.data.mall.ProductDetail
import com.example.kuikly.data.mall.SkuOffer
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
 * 商品详情页：封面 + 规格选择 + 图文详情 + 底部操作栏。
 */
@Page("MallDetail")
internal class MallDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            val productId = pageData.params.optString("id")
            var detail by remember { mutableStateOf<ProductDetail?>(null) }
            var sku by remember { mutableStateOf<SkuOffer?>(null) }
            var qty by remember { mutableStateOf(1) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                MallStore.repo.productDetail(productId)
                    .onSuccess {
                        detail = it
                        sku = it.defaultSku()
                        error = null
                    }
                    .onFailure {
                        detail = null
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            val d = detail
            if (d == null) {
                MallFallback(message = error ?: "商品不存在", onRetry = ::reload)
                return@setContent
            }

            val selected = sku ?: d.defaultSku()

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
                    Text(d.title, fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    item {
                        // 封面区（无真实图床，用色块 + 首字表达）
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(160.dp)
                                .background(KuiklyTheme.canvasSoft, RoundedCornerShape(KuiklyTheme.radiusLg)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                d.title.take(1),
                                fontSize = 56.sp,
                                color = KuiklyTheme.hairlineStrong,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    item {
                        KCard {
                            Text(d.title, fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    selected?.priceLabel ?: "-",
                                    fontSize = 22.sp,
                                    color = KuiklyTheme.link,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.width(8.dp))
                                KChip(d.kindLabel, color = KuiklyTheme.violet)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(d.subtitle, fontSize = 13.sp, color = KuiklyTheme.muted)
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    if (d.skus.size > 1) {
                        item {
                            KCard {
                                Text("选择规格", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(8.dp))
                                d.skus.forEach { offer ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable { sku = offer; qty = 1 }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier.width(18.dp).height(18.dp)
                                                .background(
                                                    if (offer.skuId == selected?.skuId) KuiklyTheme.link else KuiklyTheme.canvas,
                                                    RoundedCornerShape(9.dp),
                                                )
                                                .padding(4.dp)
                                                .background(
                                                    if (offer.skuId == selected?.skuId) KuiklyTheme.link else KuiklyTheme.hairline,
                                                    RoundedCornerShape(9.dp),
                                                ),
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            "${offer.title} · ${offer.priceLabel}",
                                            fontSize = 14.sp,
                                            color = KuiklyTheme.foreground,
                                            fontWeight = if (offer.skuId == selected?.skuId) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.width(200.dp),
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    item {
                        KCard {
                            Text("购买数量", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.width(32.dp).height(32.dp)
                                        .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                                        .clickable { if (qty > 1) qty-- },
                                    contentAlignment = Alignment.Center,
                                ) { Text("−", fontSize = 18.sp, color = KuiklyTheme.foreground) }
                                Text(
                                    "$qty",
                                    fontSize = 16.sp,
                                    color = KuiklyTheme.foreground,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(48.dp),
                                    maxLines = 1,
                                )
                                Box(
                                    modifier = Modifier.width(32.dp).height(32.dp)
                                        .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                                        .clickable { if (qty < 99) qty++ },
                                    contentAlignment = Alignment.Center,
                                ) { Text("+", fontSize = 18.sp, color = KuiklyTheme.foreground) }
                                Spacer(Modifier.width(12.dp))
                                Text("库存 ${selected?.stockQty ?: 0}", fontSize = 12.sp, color = KuiklyTheme.muted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        // 图文详情段落（LazyColumn 内逐段渲染，避免长列表卡顿）
                        d.richText.forEachIndexed { index, para ->
                            if (index > 0) Spacer(Modifier.height(8.dp))
                            KCard {
                                Text("图文详情 ${index + 1}/${d.richText.size}", fontSize = 12.sp, color = KuiklyTheme.muted)
                                Spacer(Modifier.height(6.dp))
                                Text(para, fontSize = 14.sp, color = KuiklyTheme.body)
                            }
                        }
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

                // 底部操作栏
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KButton(
                        text = "加入购物车",
                        modifier = Modifier.width(140.dp),
                        onClick = {
                            MallStore.repo.addToCart(d.productId, qty)
                            Utils.currentBridgeModule().toast("已加入购物车")
                        },
                    )
                    KButton(
                        text = "立即购买",
                        modifier = Modifier.width(140.dp),
                        onClick = {
                            MallStore.repo.placeOrder(selected?.skuId ?: 0, qty)
                                .onSuccess {
                                    val data = com.tencent.kuikly.core.nvi.serialization.json.JSONObject()
                                    data.put("id", it.orderId)
                                    Utils.currentBridgeModule().openPage(url = "MallOrderDetail", userData = data)
                                }
                                .onFailure { Utils.currentBridgeModule().toast(it.message ?: "下单失败") }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MallFallback(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, fontSize = 14.sp, color = KuiklyTheme.error)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.clickable { onRetry() }
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) { Text("重试", color = KuiklyTheme.onPrimary, fontSize = 14.sp) }
    }
}
