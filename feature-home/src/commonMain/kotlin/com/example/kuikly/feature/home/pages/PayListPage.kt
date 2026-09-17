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
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import kotlin.math.roundToInt

/**
 * `MembershipPalette` / `MembershipDimens` 令牌镜像
 * （`features/pay/lib/membership/theme/membership_theme.dart`）。
 *
 * **刻度**：Flutter pay 模块 view/theme/widgets **全用裸逻辑 px**（grep 无 `.w/.h/.sp`），
 * 唯一宽度相关量是 `tabBarHeightForWidth(width) = width * 84 / 564` 与卡宽自适应 —— 用
 * `pageViewWidth` 原样换算，**不要** `.su()`。
 */
internal object PayPalette {
    val titleBlack = Color(0xFF333333)
    val textGray = Color(0xFF999999)
    val textGrayLight = Color(0xFFBFBFBF)
    val pageBackground = Color(0xFFF7F7F7)
    val cardWhite = Color(0xFFFFFFFF)
    val priceBlack = Color(0xFF1A1A1A)
    val originalPriceGray = Color(0xFFBFBFBF)
    val beanOrange = Color(0xFFFF8A34)
    val planBadgePromoBg = Color(0xFFFFF8E6)
    val planFooterPeach = Color(0xFFFFF3E8)
    val planBorderUnselected = Color(0xFFEEEEEE)
    val planSelectedBg = Color(0xFFFFF8ED)
    val divider = Color(0xFFEEEEEE)
    val agreement = Color(0xFFBDBDBD)

    // MembershipPalette.of(svip)
    val svipHeaderGradient = listOf(Color(0xFFFFF3D4), Color(0xFFFFFFFF))
    val svipAccent = Color(0xFFFF8A34)
    val svipAccentLight = Color(0xFFFFB800)
    val svipPromoBackground = Color(0xFFEFF8E8)
    val svipPromoAccent = Color(0xFF52C41A)
    val svipCtaGradient = listOf(Color(0xFFFFD36A), Color(0xFFFF8A34))

    // MembershipPalette.of(aiSvip)
    val aiHeaderGradient = listOf(Color(0xFFE8E1FF), Color(0xFFFFFFFF))
    val aiAccent = Color(0xFF9D7CFF)
    val aiAccentLight = Color(0xFF7B5CFF)
    val aiPromoBackground = Color(0xFFF3EEFF)
    val aiPromoAccent = Color(0xFF9D7CFF)
    val aiCtaGradient = listOf(Color(0xFF9D7CFF), Color(0xFF5B7CFF))

    const val PLAN_CARD_WIDTH = 118f
    const val PLAN_CARD_HEIGHT = 148f
    const val PLAN_CARD_RADIUS = 12f
    const val PLAN_CARD_GAP = 10f
    const val PLAN_POINTER_HEIGHT = 6f
}

/** Flutter `MembershipMockData` 的个人信息 / 抵扣 / 活动文案。 */
private object PayMock {
    const val DISPLAY_NAME = "小趣友腻腻"
    const val LEVEL_BADGE = "V3"
    const val STATUS_TEXT = "您的会员身份已过期"
    const val DEDUCTION_AMOUNT = 188.0
    const val BEAN_BALANCE = 88.88
    const val PROMO_COUNTDOWN = "2天 22:59:59"
    const val RED_PACKET_COUNTDOWN = "02:32:59"
    const val PROFILE_GLYPH = "腻"
}

/** Flutter `MembershipFeatureItem`（AI SVIP 四宫格）。 */
private class PayFeature(
    val title: String,
    val subtitle: String,
    val gradient: List<Color>,
)

private val PAY_AI_FEATURES = listOf(
    PayFeature("背单词", "听音辨义 拼写无忧", listOf(Color(0xFF7ED957), Color(0xFF52C41A))),
    PayFeature("读课文", "智能打分 纠正发音", listOf(Color(0xFFFFB347), Color(0xFFFF8A34))),
    PayFeature("AI私教", "告别死记 活学活用", listOf(Color(0xFF6CB6FF), Color(0xFF3D8BFF))),
    PayFeature("刷真题", "考点精粹 高效提分", listOf(Color(0xFFFF7B7B), Color(0xFFFF4D4F))),
)

/**
 * 会员页 — Flutter `MembershipRenewPage` 复刻（Phase-2 / P2-W2c）。
 * 真源：`features/pay/lib/membership/view/membership_renew_page.dart` 及其 9 个 widget
 * （Header / TierTabs / PlanCarousel / PromoBanner / DeductionRow / PaymentMethods /
 * FeatureSection / RenewBar）。
 *
 * ponytail 天花板：Flutter 的头部背景 / tab 三切图 / 皇冠·钻石图标 / 单选项 / 插画全为
 * **PNG 资源**，Kuikly 无图片资源 → 用 `MembershipPalette` 里的**真实渐变色 + Unicode 字形**
 * 近似；滚动折叠导航条（`MembershipCollapsedNavBar`）与 1s 红包倒计时未复刻（静态文案）；
 * 支付 SDK / 客服为 toast。
 */
@Page(name = "PayList", moduleId = "feature_home")
internal class PayListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val pageWidth = pagerData.pageViewWidth
        setContent {
            var plans by remember { mutableStateOf<List<Plan>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            var tier by remember { mutableStateOf(PayTier.Svip) }
            var selectedPlanId by remember { mutableStateOf("1") }
            var useDeduction by remember { mutableStateOf(true) }
            var wechat by remember { mutableStateOf(true) }
            var agreed by remember { mutableStateOf(false) }

            fun load() {
                PayStore.repo.list()
                    .onSuccess {
                        plans = it
                        error = null
                    }
                    .onFailure {
                        plans = emptyList()
                        error = it.message
                    }
            }

            if (!booted) {
                booted = true
                load()
            }

            val tierPlans = plans.filter { it.tier == tier }
            val selectedPlan = tierPlans.firstOrNull { it.id == selectedPlanId } ?: tierPlans.firstOrNull()
            val accent = if (tier == PayTier.Svip) PayPalette.svipAccent else PayPalette.aiAccent

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PayPalette.pageBackground),
            ) {
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
                    tierPlans.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "💳",
                            title = "暂无套餐",
                            message = "点击刷新重新加载",
                            actionLabel = "刷新",
                            onAction = { load() },
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            item {
                                MembershipHeader(
                                    tier = tier,
                                    accent = accent,
                                    topInset = top,
                                    pageWidth = pageWidth,
                                    onBack = { Utils.currentBridgeModule().closePage() },
                                    onSelectTier = { next ->
                                        tier = next
                                        // Flutter `selectTier`：切换档位时重置抵扣 / 协议 / 选中套餐。
                                        useDeduction = next == PayTier.Svip
                                        agreed = false
                                        selectedPlanId = plans.firstOrNull { it.tier == next }?.id ?: selectedPlanId
                                    },
                                )
                            }
                            item {
                                Column(modifier = Modifier.fillMaxWidth().background(PayPalette.cardWhite)) {
                                    Spacer(Modifier.height(16.dp))
                                    PlanCarousel(
                                        plans = tierPlans,
                                        selectedPlanId = selectedPlan?.id,
                                        accent = accent,
                                        pageWidth = pageWidth,
                                        redPacketCountdown = PayMock.RED_PACKET_COUNTDOWN,
                                        onSelect = { selectedPlanId = it },
                                    )
                                    PromoBanner(tier = tier)
                                    DeductionRow(
                                        selected = useDeduction,
                                        accent = accent,
                                        onToggle = { useDeduction = !useDeduction },
                                    )
                                    PaymentMethods(
                                        wechat = wechat,
                                        onSelect = { wechat = it },
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                            item {
                                FeatureSection(tier = tier, pageWidth = pageWidth)
                            }
                        }
                        // Flutter `MembershipRenewBar`（固定底栏）
                        RenewBar(
                            tier = tier,
                            selectedPlan = selectedPlan,
                            useDeduction = useDeduction,
                            wechat = wechat,
                            agreed = agreed,
                            onToggleAgreement = { agreed = !agreed },
                            bottomInset = bottom,
                            onRenew = {
                                if (tier == PayTier.AiSvip && !agreed) {
                                    Utils.currentBridgeModule().toast("请先阅读并同意会员协议")
                                } else {
                                    Utils.currentBridgeModule().toast(
                                        "Mock 续费 ¥${formatMoney(finalPrice(selectedPlan, useDeduction, wechat))}" +
                                            "（${selectedPlan?.title ?: ""}）",
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Flutter `MembershipRenewController.finalPrice`（抵扣 188 + 微信趣豆 88.88，下限 0）。 */
private fun finalPrice(plan: Plan?, useDeduction: Boolean, wechat: Boolean): Double {
    var price = plan?.price ?: 0.0
    if (useDeduction) price -= PayMock.DEDUCTION_AMOUNT
    if (wechat) price -= PayMock.BEAN_BALANCE
    return if (price < 0) 0.0 else price
}

/** 当前档位的 accent（`MembershipPalette.of(tier).accent`）。 */
internal fun payAccent(tier: PayTier): Color =
    if (tier == PayTier.Svip) PayPalette.svipAccent else PayPalette.aiAccent

/** 当前档位的 CTA 渐变（`MembershipPalette.of(tier).ctaGradient`）。 */
internal fun payCtaGradient(tier: PayTier): List<Color> =
    if (tier == PayTier.Svip) PayPalette.svipCtaGradient else PayPalette.aiCtaGradient

/** Flutter `toStringAsFixed(2)`（Kotlin 无该 API，手动四舍五入到分）。 */
internal fun formatMoney(value: Double): String {
    val cents = (value * 100).roundToInt()
    val negative = cents < 0
    val abs = if (negative) -cents else cents
    val body = "${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
    return if (negative) "-$body" else body
}

/**
 * Flutter `MembershipHeader`：渐变头（`headerGradient`，PNG 背景的近似）+ 返回 / 客服 /
 * 头像 48 + 名字 18·w600 + 等级胶囊 + 状态 13 + `MembershipTierTabs`。
 */
@Composable
private fun MembershipHeader(
    tier: PayTier,
    accent: Color,
    topInset: Float,
    pageWidth: Float,
    onBack: () -> Unit,
    onSelectTier: (PayTier) -> Unit,
) {
    val gradient = if (tier == PayTier.Svip) PayPalette.svipHeaderGradient else PayPalette.aiHeaderGradient
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(gradient)),
    ) {
        // Flutter `headerTopPadding = 8` + `headerActionRowHeight = 38`
        Spacer(Modifier.height((topInset + 8f).dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(38.dp).padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Flutter `MembershipHeaderBackButton`：`arrow_back_ios_new` 18 + padding 8。
            Text(
                "‹",
                fontSize = 20.sp,
                color = PayPalette.titleBlack,
                modifier = Modifier.clickable(onClick = onBack).padding(8.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                "ⓘ",
                fontSize = 22.sp,
                color = accent,
                modifier = Modifier
                    .clickable { Utils.currentBridgeModule().toast("客服帮助（开发中）") }
                    .padding(8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(PayMock.PROFILE_GLYPH, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        PayMock.DISPLAY_NAME,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PayPalette.titleBlack,
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            PayMock.LEVEL_BADGE,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(PayMock.STATUS_TEXT, fontSize = 13.sp, color = PayPalette.textGray)
            }
        }
        Spacer(Modifier.height(10.dp))
        TierTabs(tier = tier, pageWidth = pageWidth, onSelectTier = onSelectTier)
    }
}

/** Flutter `MembershipTierTabs`：`tabHeight = width * 84 / 564`，左右各 1/2（原为 1/3 切图）。 */
@Composable
private fun TierTabs(tier: PayTier, pageWidth: Float, onSelectTier: (PayTier) -> Unit) {
    val tabHeight = pageWidth * 84f / 564f
    val selectedAccent = if (tier == PayTier.Svip) PayPalette.svipAccentLight else PayPalette.aiAccentLight
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(tabHeight.dp)
            .background(
                // Flutter 用三张切图拼接（`tab_svip_*` / `tab_ai_svip_*`）；无资源 → 用该档位
                // 真实 accent 色做低透明度横向渐变近似。
                Brush.horizontalGradient(
                    listOf(
                        selectedAccent.copy(alpha = 0.30f),
                        selectedAccent.copy(alpha = 0.08f),
                        selectedAccent.copy(alpha = 0.30f),
                    ),
                ),
            ),
    ) {
        TierTab(
            Modifier.weight(1f),
            glyph = "♛",
            title = "SVIP",
            subtitle = "6W内容 无限评分",
            selected = tier == PayTier.Svip,
            accent = PayPalette.svipAccent,
            onClick = { onSelectTier(PayTier.Svip) },
        )
        TierTab(
            Modifier.weight(1f),
            glyph = "◆",
            title = "Ai SVIP",
            subtitle = "SVIP+AI权益",
            italicPrefix = "Ai",
            selected = tier == PayTier.AiSvip,
            accent = PayPalette.aiAccent,
            onClick = { onSelectTier(PayTier.AiSvip) },
        )
    }
}

@Composable
private fun TierTab(
    modifier: Modifier,
    glyph: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    italicPrefix: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            glyph,
            fontSize = 14.sp,
            color = if (selected) accent else PayPalette.textGrayLight,
        )
        Spacer(Modifier.width(6.dp))
        Column {
            if (italicPrefix != null) {
                Row {
                    Text(
                        italicPrefix,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = if (selected) PayPalette.titleBlack else PayPalette.textGray,
                    )
                    Text(
                        " SVIP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) PayPalette.titleBlack else PayPalette.textGray,
                    )
                }
            } else {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) PayPalette.titleBlack else PayPalette.textGray,
                )
            }
            Text(
                subtitle,
                fontSize = 8.sp,
                color = if (selected) PayPalette.textGray else PayPalette.textGrayLight,
                maxLines = 1,
            )
        }
    }
}

/**
 * Flutter `MembershipPlanCarousel`：`inset = (width - 118×3) / 4`（不足时 inset 8 且卡宽自适应），
 * 高 148 + 6 指针；选中卡 `#FFF8ED` + accent 1.5 边 + 底部三角指针。
 */
@Composable
private fun PlanCarousel(
    plans: List<Plan>,
    selectedPlanId: String?,
    accent: Color,
    pageWidth: Float,
    redPacketCountdown: String,
    onSelect: (String) -> Unit,
) {
    val designTotal = PayPalette.PLAN_CARD_WIDTH * 3
    val spare = pageWidth - designTotal
    val inset: Float
    val cardWidth: Float
    if (spare >= 0) {
        inset = spare / 4f
        cardWidth = PayPalette.PLAN_CARD_WIDTH
    } else {
        inset = 8f
        cardWidth = (pageWidth - 32f) / 3f
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(
            (PayPalette.PLAN_CARD_HEIGHT + PayPalette.PLAN_POINTER_HEIGHT).dp,
        ),
        verticalAlignment = Alignment.Top,
    ) {
        Spacer(Modifier.width(inset.dp))
        plans.forEachIndexed { index, plan ->
            if (index > 0) Spacer(Modifier.width(inset.dp))
            PlanCard(
                modifier = Modifier.width(cardWidth.dp),
                plan = plan,
                selected = plan.id == selectedPlanId,
                accent = accent,
                redPacketCountdown = redPacketCountdown,
                onSelect = { onSelect(plan.id) },
            )
        }
        Spacer(Modifier.width(inset.dp))
    }
}

@Composable
private fun PlanCard(
    modifier: Modifier,
    plan: Plan,
    selected: Boolean,
    accent: Color,
    redPacketCountdown: String,
    onSelect: () -> Unit,
) {
    Column(modifier = modifier.clickable(onClick = onSelect)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PayPalette.PLAN_CARD_HEIGHT.dp)
                .background(
                    if (selected) PayPalette.planSelectedBg else PayPalette.cardWhite,
                    RoundedCornerShape(PayPalette.PLAN_CARD_RADIUS.dp),
                )
                .border(
                    if (selected) 1.5.dp else 1.dp,
                    if (selected) accent else PayPalette.planBorderUnselected,
                    RoundedCornerShape(PayPalette.PLAN_CARD_RADIUS.dp),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(if (plan.badge != null) 22.dp else 12.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(plan.title, fontSize = 14.sp, color = PayPalette.titleBlack, maxLines = 1)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "¥",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PayPalette.priceBlack,
                            )
                            Text(
                                "${plan.price.roundToInt()}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = PayPalette.priceBlack,
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "¥${plan.originalPrice.roundToInt()}",
                            fontSize = 11.sp,
                            color = PayPalette.originalPriceGray,
                        )
                    }
                }
                if (selected && plan.showRedPacket) {
                    // Flutter `_RedPacketFooter`（红包图标为资源 → 文字近似）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent)
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🧧", fontSize = 10.sp)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "8元红包 $redPacketCountdown",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                } else if (!selected && plan.dailyHint != null) {
                    // Flutter `_DailyHintFooter`（底部桃色条，底角 r11）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                PayPalette.planFooterPeach,
                                RoundedCornerShape(bottomStart = 11.dp, bottomEnd = 11.dp),
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            plan.dailyHint ?: "",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = accent,
                            maxLines = 1,
                        )
                    }
                } else {
                    Spacer(Modifier.height(if (selected) 8.dp else 10.dp))
                }
            }
            // Flutter `Positioned(top: 0, left: 0, child: _PlanBadge)`
            plan.badge?.let { badge ->
                val trial = badge == "开学尝鲜价"
                Box(modifier = Modifier.align(Alignment.TopStart)) {
                    if (trial && selected) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.65f))),
                                    RoundedCornerShape(
                                        topStart = PayPalette.PLAN_CARD_RADIUS.dp,
                                        bottomEnd = 8.dp,
                                    ),
                                )
                                .padding(start = 6.dp, top = 3.dp, end = 8.dp, bottom = 4.dp),
                        ) {
                            Text(
                                badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 8.dp)
                                .background(PayPalette.planBadgePromoBg, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accent,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
        // Flutter 选中态 `_PlanPointerPainter`（12×6 三角）
        if (selected) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(12.dp, PayPalette.PLAN_POINTER_HEIGHT.dp)) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    drawPath(path, accent)
                }
            }
        } else {
            Spacer(Modifier.height(PayPalette.PLAN_POINTER_HEIGHT.dp))
        }
    }
}

/** Flutter `MembershipPromoBanner`：`promoBackground` r12 + 标题 16·w700 + 右侧倒计时。 */
@Composable
private fun PromoBanner(tier: PayTier) {
    val background = if (tier == PayTier.Svip) PayPalette.svipPromoBackground else PayPalette.aiPromoBackground
    val accent = if (tier == PayTier.Svip) PayPalette.svipPromoAccent else PayPalette.aiPromoAccent
    val title = if (tier == PayTier.Svip) "春日踏青礼" else "寒假趣超车"
    val subtitle = if (tier == PayTier.Svip) "加赠限定勋章、装扮套装" else "赠新春礼包"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .background(background, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accent)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = PayPalette.textGray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("距结束还剩", fontSize = 11.sp, color = PayPalette.textGray)
            Spacer(Modifier.height(2.dp))
            Text(
                PayMock.PROMO_COUNTDOWN,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
    }
}

/** Flutter `MembershipDeductionRow`：白卡 r12「剩余会员天数可抵扣 **188 元**」+ 单选。 */
@Composable
private fun DeductionRow(selected: Boolean, accent: Color, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PayPalette.planSelectedBg, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("折", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = accent)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("剩余会员天数可抵扣 ", fontSize = 14.sp, color = PayPalette.titleBlack)
                Text(
                    "${PayMock.DEDUCTION_AMOUNT.roundToInt()} 元",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayPalette.beanOrange,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "SVIP抵扣0.7元/天，VIP抵扣0.3元/天",
                fontSize = 11.sp,
                color = PayPalette.textGray,
            )
        }
        Radio(selected = selected, accent = accent)
    }
}

/** Flutter `MembershipPaymentMethods`：微信（含趣豆余额）/ 支付宝 两行 + 56 缩进分隔线。 */
@Composable
internal fun PaymentMethods(wechat: Boolean, onSelect: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .background(PayPalette.cardWhite, RoundedCornerShape(12.dp)),
    ) {
        PaymentTile(
            glyph = "💬",
            title = "微信支付",
            subtitle = "（趣豆余额抵扣 ${formatMoney(PayMock.BEAN_BALANCE)} 元）",
            selected = wechat,
            accent = PayPalette.svipAccent,
            onTap = { onSelect(true) },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp)
                .height(1.dp)
                .background(PayPalette.divider),
        )
        PaymentTile(
            glyph = "🅐",
            title = "支付宝支付",
            subtitle = null,
            selected = !wechat,
            accent = PayPalette.svipAccent,
            onTap = { onSelect(false) },
        )
    }
}

@Composable
private fun PaymentTile(
    glyph: String,
    title: String,
    subtitle: String?,
    selected: Boolean,
    accent: Color,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(glyph, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 15.sp, color = PayPalette.titleBlack)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = PayPalette.beanOrange, maxLines = 1)
            }
        }
        Radio(selected = selected, accent = accent)
    }
}

/** Flutter 单选图标（asset）→ `●` / `○` 字形近似。 */
@Composable
internal fun Radio(selected: Boolean, accent: Color) {
    Text(
        if (selected) "●" else "○",
        fontSize = 20.sp,
        color = if (selected) accent else PayPalette.textGrayLight,
    )
}

/** Flutter `MembershipFeatureSection`：SVIP 单图 → 占位块；AI SVIP → 2×2 渐变四宫格。 */
@Composable
private fun FeatureSection(tier: PayTier, pageWidth: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
        if (tier == PayTier.Svip) {
            Text(
                "畅享6W+会员内容 系统进阶",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PayPalette.titleBlack,
            )
            Spacer(Modifier.height(8.dp))
            Text("精选全球IP 孩子主动要学", fontSize = 14.sp, color = PayPalette.textGray)
            Spacer(Modifier.height(16.dp))
            // Flutter `Image.asset(illustrationSvip)`（r12）→ 无资源 → 占位块。
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(PayPalette.planSelectedBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("📚", fontSize = 40.sp, color = PayPalette.textGrayLight)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "AI同步练 校内好提分",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayPalette.titleBlack,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .background(PayPalette.aiAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        "AI SVIP 专享",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PayPalette.aiAccent,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            val cellWidth = (pageWidth - 42f) / 2f
            val cellHeight = cellWidth / 1.45f
            PAY_AI_FEATURES.chunked(2).forEach { pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { feature ->
                        Column(
                            modifier = Modifier
                                .width(cellWidth.dp)
                                .height(cellHeight.dp)
                                .background(
                                    Brush.horizontalGradient(feature.gradient),
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(12.dp),
                        ) {
                            Text(
                                feature.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(feature.subtitle, fontSize = 11.sp, color = Color(0xB3FFFFFF), maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

/** Flutter `MembershipRenewBar`：固定白底 + （AI 档协议勾选）+ 48·r24 渐变 CTA。 */
@Composable
private fun RenewBar(
    tier: PayTier,
    selectedPlan: Plan?,
    useDeduction: Boolean,
    wechat: Boolean,
    agreed: Boolean,
    onToggleAgreement: () -> Unit,
    bottomInset: Float,
    onRenew: () -> Unit,
) {
    val ctaGradient = payCtaGradient(tier)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PayPalette.cardWhite)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = (bottomInset + 12f).dp),
    ) {
        if (tier == PayTier.AiSvip) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleAgreement),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (agreed) "☑" else "☐",
                    fontSize = 18.sp,
                    color = if (agreed) PayPalette.aiAccent else PayPalette.textGrayLight,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "已阅读并同意《趣配音会员协议》《趣配音自动续费协议》",
                    fontSize = 11.sp,
                    color = PayPalette.textGray,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
        }
        PayGradientCta(
            label = "¥${formatMoney(finalPrice(selectedPlan, useDeduction, wechat))} 立即续费",
            gradient = ctaGradient,
            onClick = onRenew,
        )
        if (tier == PayTier.Svip) {
            Spacer(Modifier.height(8.dp))
            Text("趣配音会员协议", fontSize = 11.sp, color = PayPalette.textGrayLight)
        }
    }
}

/** Flutter `FilledButton` 形制的 48·r24 渐变 CTA（`MembershipRenewBar` 与确认页共用）。 */
@Composable
internal fun PayGradientCta(label: String, gradient: List<Color>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Brush.horizontalGradient(gradient), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
