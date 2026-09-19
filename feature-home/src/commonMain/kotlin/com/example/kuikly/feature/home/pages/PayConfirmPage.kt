package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.pay.PayStore
import com.example.kuikly.data.pay.PayTier
import com.example.kuikly.data.pay.Plan
import com.tencent.kuikly.compose.foundation.background
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
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.RectangleShape
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import kotlin.math.roundToInt

/**
 * 会员套餐确认页（mock）。展示所选套餐 + 支付方式 + 「确认开通」→ toast 成功（无真实支付 SDK）。
 *
 * **真源说明**：Flutter `features/pay/lib/pay/view/pay_page.dart` 只是占位 stub
 * （`AppNavBar(title: '支付')` + `Center(Text('Pay 模块'))`），**没有**对应确认页 —— 本页是
 * Kuikly 侧既有 mock 页，故按 Pay 模块真源（`MembershipPalette` / `MembershipRenewBar`
 * 的 48·r24 渐变 CTA / `MembershipPaymentMethods`）对齐 chrome 与配色，功能保持不变。
 *
 * **刻度**：同 `PayPalette` —— Flutter pay 模块 view/theme **全裸逻辑 px**，一律裸 `dp`/`sp`。
 */
@Page(name = "PayConfirm", moduleId = "feature_home")
internal class PayConfirmPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val planId = pageData.params.optString("id").ifBlank { "1" }
        setContent {
            var plan by remember { mutableStateOf<Plan?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }
            var wechat by remember { mutableStateOf(true) }

            fun load() {
                PayStore.repo.detail(planId)
                    .onSuccess {
                        plan = it
                        error = null
                    }
                    .onFailure {
                        plan = null
                        error = it.message
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
                    title = "确认开通",
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
                            onAction = { load() },
                        )
                    }
                    plan != null -> {
                        val p = plan!!
                        val accent = payAccent(p.tier)
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Spacer(Modifier.height(16.dp))
                            OrderCard(plan = p, accent = accent)
                            // Flutter `MembershipPaymentMethods`（自带上/左右 padding 与白卡）
                            PaymentMethods(wechat = wechat, onSelect = { wechat = it })
                            Spacer(Modifier.weight(1f))
                            ConfirmBar(
                                tier = p.tier,
                                priceText = if (p.price > 0.0) "¥${formatMoney(p.price)}" else p.priceLabel,
                                wechat = wechat,
                                bottomInset = bottom,
                                onConfirm = {
                                    Utils.currentBridgeModule().toast("开通成功：${p.title}（mock）")
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 订单摘要白卡（套餐角标 + 名称 + 描述 + 价格/原价）—— 沿用 `MembershipPlan` 的真源字段。
 */
@Composable
private fun OrderCard(plan: Plan, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    if (plan.tier == PayTier.Svip) "SVIP" else "AI SVIP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
            plan.badge?.let { badge ->
                Spacer(Modifier.width(6.dp))
                Text(badge, fontSize = 11.sp, color = PayPalette.textGray, maxLines = 1)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            plan.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PayPalette.titleBlack,
            maxLines = 1,
        )
        Spacer(Modifier.height(4.dp))
        Text(plan.desc, fontSize = 13.sp, color = PayPalette.textGray)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PayPalette.divider),
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("¥", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PayPalette.priceBlack)
            Text(
                "${plan.price.roundToInt()}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.priceBlack,
            )
            if (plan.originalPrice > 0.0) {
                Spacer(Modifier.width(8.dp))
                // Flutter `PlanCard` 原价：`TextDecoration.lineThrough`，无「原价」前缀。
                Text(
                    "¥${plan.originalPrice.roundToInt()}",
                    fontSize = 11.sp,
                    color = PayPalette.originalPriceGray,
                    textDecoration = TextDecoration.LineThrough,
                )
            }
        }
    }
}

/**
 * 底部固定条：`MembershipRenewBar` 同款 48·r24 渐变 CTA + 协议说明。
 */
@Composable
private fun ConfirmBar(
    tier: PayTier,
    priceText: String,
    wechat: Boolean,
    bottomInset: Float,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `MembershipRenewBar`：`BoxShadow(Color(0x14000000), blurRadius: 12, offset(0, -2))`
            // 的向上投影近似（Kuikly `.shadow` 四向投影，底侧出屏不可见）。
            .shadow(
                4.dp,
                RectangleShape,
                ambientColor = Color(0x14000000),
                spotColor = Color(0x14000000),
            )
            .background(PayPalette.cardWhite)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = (bottomInset + 12f).dp),
    ) {
        Text(
            "支付方式：${if (wechat) "微信支付" else "支付宝支付"}",
            fontSize = 11.sp,
            color = PayPalette.textGray,
            maxLines = 1,
        )
        Spacer(Modifier.height(8.dp))
        PayGradientCta(
            label = "$priceText 确认开通",
            gradient = payCtaGradient(tier),
            onClick = onConfirm,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "开通即视为同意《趣配音会员协议》",
            fontSize = 11.sp,
            color = PayPalette.textGrayLight,
        )
    }
}
