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
import com.example.kuikly.data.pay.MemberProfile
import com.example.kuikly.data.pay.MemberSku
import com.example.kuikly.data.pay.PayChannel
import com.example.kuikly.data.pay.PayChannels
import com.example.kuikly.data.pay.PayFlowStore
import com.example.kuikly.data.pay.PayOrder
import com.example.kuikly.data.pay.PayQuote
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.RectangleShape
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 会员购买页（port of Flutter `membership/view/membership_renew_page.dart`）：
 * 用户信息 → 档位 Tab → 套餐选择 → 活动倒计时 → 剩余天数抵扣 → 支付渠道 → 自动续费 → 权益区
 * → 底部协议 + 渐变 CTA。
 *
 * **与 `MembershipRenew` 页的分工**：后者是既有 mock 页（`MembershipPlan` 月/季/年模型，
 * 由 `PayPage` 直达）；本页按源 `membership` 目录的 svip / ai_svip 双档位 + 1/6/12 月档期
 * 建模，承载抵扣、红包、协议勾选、渠道校验与下单状态机。
 *
 * **刻度**：同 `PayPalette` —— 源 pay 模块 view/theme 全裸逻辑 px，一律裸 `dp`/`sp`。
 */
@Page("PayVip")
internal class PayVipPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var tier by remember { mutableStateOf(PayTier.Svip) }
            var skuId by remember { mutableStateOf(MemberCatalog.skusFor(PayTier.Svip).first().id) }
            var channel by remember { mutableStateOf(PayChannels.available().first()) }
            var useDeduction by remember { mutableStateOf(false) }
            var useRedPacket by remember { mutableStateOf(true) }
            var autoRenew by remember { mutableStateOf(false) }
            var agreed by remember { mutableStateOf(false) }
            var countdown by remember { mutableStateOf(MemberCatalog.RED_PACKET_SECONDS) }
            var orders by remember { mutableStateOf<List<PayOrder>>(emptyList()) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                PayFlowStore.repo.orders()
                    .onSuccess { orders = it }
                    .onFailure { orders = emptyList() }
            }

            if (!booted) {
                booted = true
                reload()
            }

            fun selectTier(next: PayTier) {
                if (tier == next) return
                tier = next
                // 源 `selectTier`：切档位重置套餐 / 抵扣 / 协议勾选
                skuId = MemberCatalog.skusFor(next).first().id
                useDeduction = false
                agreed = false
            }

            val skus = MemberCatalog.skusFor(tier)
            val sku = skus.firstOrNull { it.id == skuId } ?: skus.first()
            val quote = PayQuote(sku = sku, useDeduction = useDeduction, useRedPacket = useRedPacket)
            val entitlement = MemberCatalog.entitlement(tier, orders)
            val accent = payAccent(tier)
            val wallet = MemberCatalog.wallet
            val channels = PayChannels.available()
            val showAgreement = tier == PayTier.AiSvip || channel.requiresAgreement

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PayPalette.pageBackground),
            ) {
                AppNavBarBar(
                    title = "会员开通",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    actions = {
                        Text(
                            "客服",
                            fontSize = 13.sp,
                            color = PayPalette.textGray,
                            modifier = Modifier
                                .clickable { Utils.currentBridgeModule().toast("客服帮助（开发中）") }
                                .padding(horizontal = 12.dp),
                        )
                    },
                )

                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    item {
                        PayProfileHeader(
                            profile = MemberCatalog.profile,
                            entitlement = entitlement,
                            accent = accent,
                        )
                    }
                    item {
                        PayTierTabs(selected = tier, onSelect = { selectTier(it) })
                    }
                    item {
                        PayPlanCards(
                            skus = skus,
                            selectedId = sku.id,
                            countdownSeconds = countdown,
                            onSelect = { skuId = it },
                        )
                    }
                    item {
                        PayPromoBanner(
                            promo = MemberCatalog.promoFor(tier),
                            // 无 Timer API：倒计时由点击刷新驱动（每点一次减 60 秒）
                            onTick = { countdown = (countdown - 60).coerceAtLeast(0) },
                        )
                    }
                    item {
                        PayDeductionRow(
                            amount = wallet.deductionAmount(tier),
                            selected = useDeduction,
                            accent = accent,
                            hint = "SVIP抵扣0.7元/天，VIP抵扣0.3元/天",
                            onToggle = { useDeduction = !useDeduction },
                        )
                    }
                    item {
                        PayChannelRows(
                            channels = channels,
                            selected = channel,
                            wallet = wallet,
                            onSelect = { channel = it },
                        )
                    }
                    item {
                        PayAutoRenewRow(
                            sku = sku,
                            enabled = autoRenew,
                            accent = accent,
                            expiresAt = entitlement.expiresAt,
                            onToggle = { autoRenew = it },
                            onManage = {
                                Utils.currentBridgeModule().openPage(
                                    url = "PayAutoRenew",
                                    userData = JSONObject(),
                                )
                            },
                        )
                    }
                    item {
                        PayFeatureSection(tier = tier)
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "[CYCLE MOCK] ${MockBackend.scenario}",
                            fontSize = 12.sp,
                            color = PayPalette.textGray,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    MockBackend.cycle()
                                    reload()
                                },
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                PayRenewBar(
                    tier = tier,
                    priceText = formatMoney(quote.payable),
                    cta = entitlement.cta,
                    savingText = if (quote.saved > 0.0) "已省 ¥${formatMoney(quote.saved)}" else "",
                    showAgreement = showAgreement,
                    agreed = agreed,
                    onAgreementToggle = { agreed = !agreed },
                    bottomInset = bottom,
                    onConfirm = {
                        val result = PayFlowStore.repo.renewNow(
                            quote = quote,
                            channel = channel,
                            autoRenew = autoRenew,
                            agreed = agreed,
                        )
                        val order = result.getOrNull()
                        if (order == null) {
                            val reason = result.exceptionOrNull()?.message ?: "支付失败"
                            Utils.currentBridgeModule().toast(reason)
                        } else {
                            val params = JSONObject()
                            params.put("orderNo", order.orderNo)
                            Utils.currentBridgeModule().openPage(url = "PayResult", userData = params)
                        }
                    },
                )
            }
        }
    }
}

/** 用户信息行（头像 + 昵称 + 等级 + 会员状态 / 到期日）。 */
@Composable
private fun PayProfileHeader(
    profile: MemberProfile,
    entitlement: MemberEntitlement,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.cardWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .background(accent.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                profile.displayName.take(1),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    profile.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PayPalette.titleBlack,
                    maxLines = 1,
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        profile.levelBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            val status = if (entitlement.active) {
                "会员有效期至 ${entitlement.expiresAt}（${entitlement.sourceChannel}）"
            } else {
                profile.statusText
            }
            Text(status, fontSize = 13.sp, color = PayPalette.textGray, maxLines = 1)
        }
    }
}

/** 自动续费开关行：不支持自动续费的套餐只展示说明。 */
@Composable
private fun PayAutoRenewRow(
    sku: MemberSku,
    enabled: Boolean,
    accent: Color,
    expiresAt: String,
    onToggle: (Boolean) -> Unit,
    onManage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("到期自动续费", fontSize = 14.sp, color = PayPalette.titleBlack, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                val hint = if (sku.autoRenewEligible) {
                    if (expiresAt.isEmpty()) "到期前 24 小时自动扣款，可随时关闭" else "$expiresAt 到期后自动扣款"
                } else {
                    "该套餐不支持自动续费"
                }
                Text(hint, fontSize = 11.sp, color = PayPalette.textGray, maxLines = 1)
            }
            Spacer(Modifier.width(12.dp))
            if (sku.autoRenewEligible) {
                PayAutoRenewSwitch(
                    enabled = enabled,
                    accent = accent,
                    onToggle = { onToggle(!enabled) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "自动续费管理 ›",
            fontSize = 11.sp,
            color = PayPalette.beanOrange,
            modifier = Modifier.clickable(onClick = onManage),
        )
    }
}

/**
 * 底部固定条（port of `MembershipRenewBar`）：协议勾选（AI SVIP / 内购渠道）+ 48·r24 渐变 CTA。
 */
@Composable
private fun PayRenewBar(
    tier: PayTier,
    priceText: String,
    cta: String,
    savingText: String,
    showAgreement: Boolean,
    agreed: Boolean,
    onAgreementToggle: () -> Unit,
    bottomInset: Float,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                4.dp,
                RectangleShape,
                ambientColor = Color(0x14000000),
                spotColor = Color(0x14000000),
            )
            .background(PayPalette.cardWhite)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = (bottomInset + 12f).dp),
    ) {
        if (showAgreement) {
            PayAgreementRow(agreed = agreed, onToggle = onAgreementToggle)
            Spacer(Modifier.height(10.dp))
        }
        if (savingText.isNotEmpty()) {
            Text(savingText, fontSize = 11.sp, color = PayPalette.beanOrange, maxLines = 1)
            Spacer(Modifier.height(6.dp))
        }
        PayGradientCta(
            label = "¥$priceText 立即$cta",
            gradient = payCtaGradient(tier),
            onClick = onConfirm,
        )
        if (!showAgreement) {
            Spacer(Modifier.height(8.dp))
            Text(
                "开通即视为同意《趣配音会员协议》",
                fontSize = 11.sp,
                color = PayPalette.textGrayLight,
                maxLines = 1,
            )
        }
    }
}
