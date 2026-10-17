package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.data.pay.MemberCatalog
import com.example.kuikly.data.pay.MemberCountdown
import com.example.kuikly.data.pay.MemberFeature
import com.example.kuikly.data.pay.MemberPromo
import com.example.kuikly.data.pay.MemberSku
import com.example.kuikly.data.pay.MemberWallet
import com.example.kuikly.data.pay.PayChannel
import com.example.kuikly.data.pay.PayOrder
import com.example.kuikly.data.pay.PayOrderState
import com.example.kuikly.data.pay.PayTier
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.RowScope
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import kotlin.math.roundToInt

/** 渠道图标（Flutter 侧是 asset 切图，Kuikly 侧用字形近似）。 */
internal fun payChannelGlyph(channel: PayChannel): String = when (channel) {
    PayChannel.wechat -> "💬"
    PayChannel.alipay -> "🅐"
    PayChannel.balance -> "💰"
    PayChannel.huawei -> "🌸"
    PayChannel.apple -> "🍎"
}

/** 订单状态色：成功绿 / 进行中主色 / 终态灰红。 */
internal fun payStateColor(state: PayOrderState): Color = when (state) {
    PayOrderState.Success -> PayPalette.svipPromoAccent
    PayOrderState.Creating, PayOrderState.Created, PayOrderState.Paying, PayOrderState.Processing -> PayPalette.aiPromoAccent
    PayOrderState.Timeout -> PayPalette.beanOrange
    PayOrderState.Cancelled -> PayPalette.textGray
    PayOrderState.Failed -> PayPalette.beanOrange
}

/** Flutter `MembershipTierTabs`：SVIP / AI SVIP 二选一。 */
@Composable
internal fun PayTierTabs(selected: PayTier, onSelect: (PayTier) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        PayTierTab(
            tier = PayTier.Svip,
            title = "SVIP",
            subtitle = "6W内容 无限评分",
            selected = selected == PayTier.Svip,
            onSelect = onSelect,
        )
        Spacer(Modifier.width(10.dp))
        PayTierTab(
            tier = PayTier.AiSvip,
            title = "AI SVIP",
            subtitle = "SVIP+AI权益",
            selected = selected == PayTier.AiSvip,
            onSelect = onSelect,
        )
    }
}

@Composable
private fun RowScope.PayTierTab(
    tier: PayTier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: (PayTier) -> Unit,
) {
    val accent = payAccent(tier)
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .background(
                if (selected) accent.copy(alpha = 0.12f) else PayPalette.cardWhite,
                shape,
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) accent else PayPalette.planBorderUnselected,
                shape,
            )
            .clickable { onSelect(tier) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) PayPalette.titleBlack else PayPalette.textGray,
            maxLines = 1,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            fontSize = 11.sp,
            color = if (selected) PayPalette.textGray else PayPalette.textGrayLight,
            maxLines = 1,
        )
    }
}

/** Flutter `MembershipPlanCarousel`：三张等宽套餐卡（118×148 设计稿）。 */
@Composable
internal fun PayPlanCards(
    skus: List<MemberSku>,
    selectedId: String,
    countdownSeconds: Int,
    onSelect: (String) -> Unit,
) {
    val countdown = MemberCountdown.format(countdownSeconds)
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        PayPlanCard(
            sku = skus[0],
            selected = skus[0].id == selectedId,
            countdown = countdown,
            onSelect = onSelect,
        )
        if (skus.size > 1) {
            Spacer(Modifier.width(PayPalette.PLAN_CARD_GAP.dp))
            PayPlanCard(
                sku = skus[1],
                selected = skus[1].id == selectedId,
                countdown = countdown,
                onSelect = onSelect,
            )
        }
        if (skus.size > 2) {
            Spacer(Modifier.width(PayPalette.PLAN_CARD_GAP.dp))
            PayPlanCard(
                sku = skus[2],
                selected = skus[2].id == selectedId,
                countdown = countdown,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun RowScope.PayPlanCard(
    sku: MemberSku,
    selected: Boolean,
    countdown: String,
    onSelect: (String) -> Unit,
) {
    val accent = payAccent(sku.tier)
    val shape = RoundedCornerShape(PayPalette.PLAN_CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .height(PayPalette.PLAN_CARD_HEIGHT.dp)
            .background(
                if (selected) PayPalette.planSelectedBg else PayPalette.cardWhite,
                shape,
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) accent else PayPalette.planBorderUnselected,
                shape,
            )
            .clickable { onSelect(sku.id) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(22.dp), contentAlignment = Alignment.CenterStart) {
            val badge = sku.badge
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(
                            if (selected) accent else PayPalette.planBadgePromoBg,
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Color.White else accent,
                        maxLines = 1,
                    )
                }
            }
        }
        Text(sku.title, fontSize = 14.sp, color = PayPalette.titleBlack, maxLines = 1)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("¥", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PayPalette.priceBlack)
            Text(
                "${sku.price.roundToInt()}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.priceBlack,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            "¥${sku.originalPrice.roundToInt()}",
            fontSize = 11.sp,
            color = PayPalette.originalPriceGray,
            textDecoration = TextDecoration.LineThrough,
        )
        Spacer(Modifier.weight(1f))
        if (selected && sku.showRedPacket) {
            PayRedPacketFooter(countdown = countdown)
        } else if (!selected) {
            PayDailyHintFooter(hint = sku.dailyText, accent = accent)
        } else {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PayRedPacketFooter(countdown: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.beanOrange)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🧧", fontSize = 12.sp)
        Spacer(Modifier.width(3.dp))
        Text(
            "8元红包 $countdown",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Composable
private fun PayDailyHintFooter(hint: String, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.planFooterPeach)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(hint, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = accent, maxLines = 1)
    }
}

/** Flutter `MembershipPromoBanner`：活动标题 + 倒计时。 */
@Composable
internal fun PayPromoBanner(promo: MemberPromo, onTick: () -> Unit) {
    val accent = if (promo.tier == PayTier.Svip) PayPalette.svipPromoAccent else PayPalette.aiPromoAccent
    val background = if (promo.tier == PayTier.Svip) PayPalette.svipPromoBackground else PayPalette.aiPromoBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(background, RoundedCornerShape(12.dp))
            .clickable(onClick = onTick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                promo.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(promo.subtitle, fontSize = 12.sp, color = PayPalette.textGray, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("距结束还剩", fontSize = 11.sp, color = PayPalette.textGray, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                promo.countdownLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1,
            )
        }
    }
}

/**
 * Flutter `MembershipDeductionRow`：剩余会员天数抵扣开关（优惠券/红包区）。
 * 勾选后从应付金额里扣掉 `MemberWallet.deductionAmount`。
 */
@Composable
internal fun PayDeductionRow(
    amount: Double,
    selected: Boolean,
    accent: Color,
    hint: String,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "剩余会员天数可抵扣 ",
                    fontSize = 14.sp,
                    color = PayPalette.titleBlack,
                    maxLines = 1,
                )
                Text(
                    "${formatMoney(amount)} 元",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(hint, fontSize = 11.sp, color = PayPalette.textGray, maxLines = 1)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (selected) "●" else "○",
            fontSize = 20.sp,
            color = if (selected) accent else PayPalette.textGrayLight,
        )
    }
}

/** Flutter `MembershipPaymentMethods`：渠道列表（含 56 缩进分隔线）。 */
@Composable
internal fun PayChannelRows(
    channels: List<PayChannel>,
    selected: PayChannel,
    wallet: MemberWallet,
    onSelect: (PayChannel) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp)),
    ) {
        channels.forEachIndexed { index, channel ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 56.dp)
                        .height(1.dp)
                        .background(PayPalette.divider),
                )
            }
            PayChannelTile(
                channel = channel,
                subtitle = channel.subtitle(wallet.balance),
                selected = channel == selected,
                onTap = { onSelect(channel) },
            )
        }
    }
}

@Composable
private fun PayChannelTile(
    channel: PayChannel,
    subtitle: String,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val accent = payAccent(PayTier.Svip)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(payChannelGlyph(channel), fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(channel.label, fontSize = 15.sp, color = PayPalette.titleBlack, maxLines = 1)
            Text(subtitle, fontSize = 12.sp, color = PayPalette.beanOrange, maxLines = 1)
        }
        Text(
            if (selected) "●" else "○",
            fontSize = 20.sp,
            color = if (selected) accent else PayPalette.textGrayLight,
        )
    }
}

/** 协议勾选行（Flutter `MembershipRenewBar` 的 checkbox）。 */
@Composable
internal fun PayAgreementRow(agreed: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (agreed) "☑" else "☐",
            fontSize = 18.sp,
            color = if (agreed) PayPalette.svipAccent else PayPalette.textGrayLight,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "已阅读并同意《趣配音会员协议》《自动续费协议》",
            fontSize = 11.sp,
            color = PayPalette.textGray,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
    }
}

/** 自动续费开关（track + knob）：开启时 knob 靠右，关闭时靠左。 */
@Composable
internal fun PayAutoRenewSwitch(enabled: Boolean, accent: Color, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .width(44.dp)
            .height(24.dp)
            .background(
                if (enabled) accent else PayPalette.planBorderUnselected,
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (enabled) {
            Spacer(Modifier.weight(1f))
            PaySwitchKnob()
        } else {
            PaySwitchKnob()
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PaySwitchKnob() {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(20.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(10.dp)),
    )
}

/** 会员权益区：SVIP 文案版 / AI SVIP 四宫格。 */
@Composable
internal fun PayFeatureSection(tier: PayTier) {
    if (tier == PayTier.Svip) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                MemberCatalog.svipFeatureTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.titleBlack,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                MemberCatalog.svipFeatureSubtitle,
                fontSize = 14.sp,
                color = PayPalette.textGray,
            )
        }
        return
    }
    val accent = payAccent(PayTier.AiSvip)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                MemberCatalog.aiFeatureTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.titleBlack,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    MemberCatalog.aiFeatureBadge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        val rows = MemberCatalog.aiFeatures.chunked(2)
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                PayFeatureCard(item = row[0])
                if (row.size > 1) {
                    Spacer(Modifier.width(10.dp))
                    PayFeatureCard(item = row[1])
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RowScope.PayFeatureCard(item: MemberFeature) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(84.dp)
            .background(
                Brush.horizontalGradient(item.gradient.map { Color(it) }),
                RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
    ) {
        Text(
            item.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
        )
        Spacer(Modifier.height(6.dp))
        Text(item.subtitle, fontSize = 11.sp, color = Color.White, maxLines = 1)
    }
}

/** 订单状态徽标。 */
@Composable
internal fun PayStateBadge(state: PayOrderState) {
    Box(
        modifier = Modifier
            .background(payStateColor(state).copy(alpha = 0.14f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            state.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = payStateColor(state),
            maxLines = 1,
        )
    }
}

/** 订单列表行。 */
@Composable
internal fun PayOrderRow(order: PayOrder, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                order.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.titleBlack,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            PayStateBadge(state = order.state)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¥${formatMoney(order.amount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PayPalette.priceBlack)
            Spacer(Modifier.width(8.dp))
            Text(
                "${order.channel.label} · ${order.createdAtLabel}",
                fontSize = 12.sp,
                color = PayPalette.textGray,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "订单号 ${order.orderNo}" + if (order.autoRenew) " · 自动续费" else "",
            fontSize = 11.sp,
            color = PayPalette.textGrayLight,
            maxLines = 1,
        )
        if (order.message.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(order.message, fontSize = 11.sp, color = PayPalette.textGray, maxLines = 1)
        }
    }
}
