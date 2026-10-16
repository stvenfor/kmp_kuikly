package com.example.kuikly.pages

import com.tencent.kuikly.compose.ui.graphics.Color
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
