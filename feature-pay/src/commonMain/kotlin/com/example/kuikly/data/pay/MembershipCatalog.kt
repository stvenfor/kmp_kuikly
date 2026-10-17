package com.example.kuikly.data.pay

import kotlin.math.roundToInt

/**
 * Flutter `toStringAsFixed(2)` 的等价物：手动四舍五入到分（Kotlin 无该 API）。
 * data 层统一用它，UI 层沿用 `pages` 包的 `formatMoney`（同一口径）。
 */
internal fun round2(value: Double): Double = (value * 100).roundToInt() / 100.0

/** 金额文案（不带 ¥）：两位小数，data 层不依赖 Compose / String.format。 */
internal fun amountLabel(value: Double): String {
    val cents = (round2(value) * 100).roundToInt()
    val negative = cents < 0
    val abs = if (negative) -cents else cents
    val body = "${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
    return if (negative) "-$body" else body
}

/** Flutter `MembershipUserProfile`（`membership_models.dart`）。 */
data class MemberProfile(
    val displayName: String,
    val levelBadge: String,
    val statusText: String,
    val avatarUrl: String,
)

/** Flutter `MembershipPromoBanner`。 */
data class MemberPromo(
    val tier: PayTier,
    val title: String,
    val subtitle: String,
    val countdownLabel: String,
)

/**
 * Flutter `MembershipFeatureItem`：权益卡片（渐变以 ARGB Long 存，UI 侧 `Color(value)` 还原，
 * 避免 data 层依赖 Compose）。
 */
data class MemberFeature(
    val title: String,
    val subtitle: String,
    val gradient: List<Long>,
)

/** Flutter `MembershipEntitlementView`：会员权益计算结果（是否生效 / CTA / 到期日 / 来源渠道）。 */
data class MemberEntitlement(
    val tier: PayTier,
    val active: Boolean,
    val cta: String,
    val expiresAt: String,
    val sourceChannel: String,
)

/**
 * Flutter `MembershipMockData.deductionAmount` 的口径：
 * 剩余会员天数 × 每天可抵扣额（SVIP 0.7 元/天，VIP 0.3 元/天 —— 见
 * `membership_deduction_row.dart` 文案）。
 *
 * 字段映射难点：源 `MembershipTier` 是 `svip` / `aiSvip`，抵扣文案却按 **SVIP / VIP** 两档；
 * main 的 `PayTier` 只有 `Svip` / `AiSvip`，故把 `Svip` 对应 0.7、`AiSvip` 对应 0.3。
 */
data class MemberWallet(
    val balance: Double,
    val beanBalance: Double,
    val remainingMemberDays: Int,
) {
    fun deductionAmount(tier: PayTier): Double {
        val perDay = if (tier == PayTier.Svip) SVIP_PER_DAY else VIP_PER_DAY
        return round2(remainingMemberDays * perDay)
    }

    companion object {
        const val SVIP_PER_DAY = 0.7
        const val VIP_PER_DAY = 0.3
    }
}

/** 红包条（`membership_plan_carousel.dart` 的 `_RedPacketFooter`：8 元红包 + 倒计时）。 */
data class MemberRedPacket(
    val amount: Double,
    val countdownLabel: String,
)

/**
 * 会员套餐（port of Flutter `MembershipPlan`）。
 *
 * 与 `PayData.kt` 的 `MembershipPlan` / `Plan.kt` 的 `Plan` **三套并存、互不合并**：
 * 本类按源 `membership/mock` 的 svip + ai_svip 双档位目录建模，服务会员购买页。
 */
data class MemberSku(
    val id: String,
    val tier: PayTier,
    val title: String,
    val price: Double,
    val originalPrice: Double,
    val months: Int,
    val badge: String? = null,
    val dailyHint: String? = null,
    val showRedPacket: Boolean = false,
    val autoRenewEligible: Boolean = false,
    val huaweiProductId: String = "",
    val appleProductId: String = "",
) {
    /** 源按 30 天/月折算每日价（6 个月 150 元 → 每日仅需 0.83 元）。 */
    val days: Int get() = months * DAYS_PER_MONTH

    val perMonth: Double get() = round2(price / months.coerceAtLeast(1))

    val perDay: Double get() = round2(price / days.coerceAtLeast(1))

    /** 折扣率（0.38 = 省 38%）。 */
    val discount: Double get() = if (originalPrice > 0.0) round2(1.0 - price / originalPrice) else 0.0

    /** 相对原价已省金额。 */
    val saved: Double get() = round2((originalPrice - price).coerceAtLeast(0.0))

    val dailyLabel: String get() = "每日仅需${amountLabel(perDay)}元"

    /** 源 `dailyHint` 缺省时回落到计算的每日价。 */
    val dailyText: String get() = dailyHint ?: dailyLabel

    companion object {
        const val DAYS_PER_MONTH = 30
    }
}

/**
 * 下单报价（价格计算核心）：基础价 − 剩余天数抵扣 − 红包，下限 0，四舍五入到分。
 * port of `MembershipRenewController.finalPrice`，额外叠加源 `_RedPacketFooter` 的红包面额。
 */
data class PayQuote(
    val sku: MemberSku,
    val useDeduction: Boolean = false,
    val useRedPacket: Boolean = false,
) {
    val base: Double get() = sku.price

    val deduction: Double
        get() = if (useDeduction) MemberCatalog.wallet.deductionAmount(sku.tier) else 0.0

    val redPacket: Double
        get() = if (useRedPacket && sku.showRedPacket) MemberCatalog.redPacket.amount else 0.0

    /** 应付金额（源 `finalPrice`：`price < 0 ? 0 : toStringAsFixed(2)`）。 */
    val payable: Double get() = round2((base - deduction - redPacket).coerceAtLeast(0.0))

    /** 合计已省：原价差 + 抵扣 + 红包。 */
    val saved: Double get() = round2(sku.saved + deduction + redPacket)

    val perDay: Double get() = round2(payable / sku.days.coerceAtLeast(1))
}

/** 极简 `yyyy-MM-dd` 日期推算（会员到期 / 下次扣费日），无三方日期库依赖。 */
object PayDates {
    fun addMonths(ymd: String, months: Int): String {
        val parts = ymd.split("-")
        if (parts.size != 3) return ymd
        val year = parts[0].toIntOrNull() ?: return ymd
        val month = parts[1].toIntOrNull() ?: return ymd
        val day = parts[2].toIntOrNull() ?: return ymd
        val total = year * 12 + (month - 1) + months
        val nextYear = total / 12
        val nextMonth = total % 12 + 1
        val nextDay = day.coerceAtMost(daysInMonth(nextYear, nextMonth)).coerceAtLeast(1)
        return "${pad(nextYear, 4)}-${pad(nextMonth, 2)}-${pad(nextDay, 2)}"
    }

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        2 -> if (isLeap(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    private fun isLeap(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

    private fun pad(value: Int, width: Int): String {
        val raw = value.toString()
        return if (raw.length >= width) raw else raw.padStart(width, '0')
    }
}

/** 红包倒计时 `HH:mm:ss` 格式化（port of `_startCountdown`；秒数由页面持有，Kuikly 侧无 Timer）。 */
object MemberCountdown {
    fun format(seconds: Int): String {
        val left = seconds.coerceAtLeast(0)
        val hours = left / 3600
        val minutes = (left % 3600) / 60
        val secs = left % 60
        return "${pad(hours)}:${pad(minutes)}:${pad(secs)}"
    }

    private fun pad(value: Int): String {
        val raw = value.toString()
        return if (raw.length >= 2) raw else raw.padStart(2, '0')
    }
}

/**
 * 会员目录 mock（port of `membership/mock/membership_mock_data.dart`）。
 *
 * 与源一致的两档位 × 三档期；`wallet` / `redPacket` 为让「抵扣 + 红包」的价格计算可观察，
 * 取了非零 mock 值（源两处常量都是 0）。
 */
object MemberCatalog {
    /** mock「今天」，用于订单创建日与到期推算。 */
    const val TODAY = "2026-03-18"

    const val PROMO_COUNTDOWN = "2天 22:59:59"

    /** 源 `_redPacketCountdown` 初值 02:32:59 的秒数。 */
    const val RED_PACKET_SECONDS = 2 * 3600 + 32 * 60 + 59

    val profile = MemberProfile(
        displayName = "小趣友腻腻",
        levelBadge = "V3",
        statusText = "您的会员身份已过期",
        avatarUrl = "https://picsum.photos/seed/membership_child/120/120",
    )

    val wallet = MemberWallet(
        balance = 88.88,
        beanBalance = 88.88,
        remainingMemberDays = 12,
    )

    val redPacket = MemberRedPacket(amount = 8.0, countdownLabel = "02:32:59")

    val svipSkus = listOf(
        MemberSku(
            id = "svip_1m", tier = PayTier.Svip, title = "1个月",
            price = 30.0, originalPrice = 48.0, months = 1,
            badge = "开通尝鲜", huaweiProductId = "wys_svip_1m", appleProductId = "wys_svip_1m",
        ),
        MemberSku(
            id = "svip_6m", tier = PayTier.Svip, title = "6个月",
            price = 150.0, originalPrice = 288.0, months = 6,
            dailyHint = "每日仅需0.83元", huaweiProductId = "wys_svip_6m", appleProductId = "wys_svip_6m",
        ),
        MemberSku(
            id = "svip_12m", tier = PayTier.Svip, title = "1年",
            price = 280.0, originalPrice = 488.0, months = 12,
            badge = "最划算", showRedPacket = true, autoRenewEligible = true,
            huaweiProductId = "wys_svip_12m", appleProductId = "wys_svip_12m",
        ),
    )

    val aiSvipSkus = listOf(
        MemberSku(
            id = "ai_svip_1m", tier = PayTier.AiSvip, title = "1个月",
            price = 48.0, originalPrice = 68.0, months = 1,
            badge = "开通尝鲜", huaweiProductId = "wys_ai_svip_1m", appleProductId = "wys_ai_svip_1m",
        ),
        MemberSku(
            id = "ai_svip_6m", tier = PayTier.AiSvip, title = "6个月",
            price = 240.0, originalPrice = 408.0, months = 6,
            dailyHint = "每日仅需1.33元", huaweiProductId = "wys_ai_svip_6m", appleProductId = "wys_ai_svip_6m",
        ),
        MemberSku(
            id = "ai_svip_12m", tier = PayTier.AiSvip, title = "1年",
            price = 398.0, originalPrice = 688.0, months = 12,
            badge = "最划算", showRedPacket = true, autoRenewEligible = true,
            huaweiProductId = "wys_ai_svip_12m", appleProductId = "wys_ai_svip_12m",
        ),
    )

    fun skusFor(tier: PayTier): List<MemberSku> =
        if (tier == PayTier.Svip) svipSkus else aiSvipSkus

    fun skuOf(id: String): MemberSku? = (svipSkus + aiSvipSkus).firstOrNull { it.id == id }

    val svipPromo = MemberPromo(
        tier = PayTier.Svip,
        title = "春日踏青礼",
        subtitle = "加赠限定勋章、装扮套装",
        countdownLabel = PROMO_COUNTDOWN,
    )

    val aiSvipPromo = MemberPromo(
        tier = PayTier.AiSvip,
        title = "AI 创作季",
        subtitle = "加赠 AI 音色试用包",
        countdownLabel = PROMO_COUNTDOWN,
    )

    fun promoFor(tier: PayTier): MemberPromo =
        if (tier == PayTier.Svip) svipPromo else aiSvipPromo

    val svipFeatureTitle = "畅享6W+会员内容 系统进阶"
    val svipFeatureSubtitle = "精选全球IP 孩子主动要学"

    val aiFeatureTitle = "AI同步练 校内好提分"
    val aiFeatureBadge = "AI SVIP 专享"

    val aiFeatures = listOf(
        MemberFeature("背单词", "听音辨义 拼写无忧", listOf(0xFF50E3C2, 0xFF0070F3)),
        MemberFeature("读课文", "智能打分 纠正发音", listOf(0xFFF9CB28, 0xFFF5A623)),
        MemberFeature("AI私教", "告别死记 活学活用", listOf(0xFF6CB6FF, 0xFF3D8BFF)),
        MemberFeature("刷真题", "考点精粹 高效提分", listOf(0xFFFF7B7B, 0xFFFF4D4F)),
    )

    /**
     * 会员权益计算（port of `MembershipEntitlementView` + controller 的 `ctaLabel`）：
     * 该档位是否有生效中的成功订单 → 决定 CTA 是「开通」还是「续费」，并推算到期日与签约渠道。
     */
    fun entitlement(tier: PayTier, orders: List<PayOrder>): MemberEntitlement {
        val mine = orders.filter { it.tier == tier && it.state == PayOrderState.Success }
        val latest = mine.maxByOrNull { it.createdAtLabel }
        if (latest == null) {
            return MemberEntitlement(
                tier = tier,
                active = false,
                cta = "开通",
                expiresAt = "",
                sourceChannel = "",
            )
        }
        return MemberEntitlement(
            tier = tier,
            active = true,
            cta = "续费",
            expiresAt = PayDates.addMonths(latest.createdAtLabel, latest.months),
            sourceChannel = latest.channel.label,
        )
    }

    /** 历史订单种子（下单/支付闭环的初始数据，最新在前）。 */
    fun seedOrders(): List<PayOrder> = listOf(
        PayOrder(
            orderNo = "M20260318001", skuId = "ai_svip_12m", tier = PayTier.AiSvip,
            title = "1年", months = 12, amount = 398.0, channel = PayChannel.alipay,
            autoRenew = true, state = PayOrderState.Processing,
            createdAtLabel = TODAY, message = "支付处理中，请稍后确认",
        ),
        PayOrder(
            orderNo = "M20260210007", skuId = "svip_12m", tier = PayTier.Svip,
            title = "1年", months = 12, amount = 280.0, channel = PayChannel.wechat,
            autoRenew = true, state = PayOrderState.Success,
            createdAtLabel = "2026-02-10",
        ),
        PayOrder(
            orderNo = "M20260105003", skuId = "svip_1m", tier = PayTier.Svip,
            title = "1个月", months = 1, amount = 30.0, channel = PayChannel.balance,
            autoRenew = false, state = PayOrderState.Cancelled,
            createdAtLabel = "2026-01-05", message = "已取消支付",
        ),
        PayOrder(
            orderNo = "M20251220012", skuId = "ai_svip_1m", tier = PayTier.AiSvip,
            title = "1个月", months = 1, amount = 48.0, channel = PayChannel.wechat,
            autoRenew = false, state = PayOrderState.Failed,
            createdAtLabel = "2025-12-20", message = "支付失败（sdkError）",
        ),
    )
}
