package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.data.pay.PayTier
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
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
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import kotlin.math.roundToInt

/** 趣豆余额（mock），仅用于支付方式副文案。 */
private object PayMock {
    const val BEAN_BALANCE = 88.88
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
        // Flutter 图标 28×28 → 字形 28sp 近似
        Text(glyph, fontSize = 28.sp)
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
private fun Radio(selected: Boolean, accent: Color) {
    Text(
        if (selected) "●" else "○",
        fontSize = 20.sp,
        color = if (selected) accent else PayPalette.textGrayLight,
    )
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
