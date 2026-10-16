package com.example.kuikly.design

import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * Vercel-inspired design tokens, ported from the source project DESIGN.md.
 * Colors use the near-white canvas + ink duet with cyan/blue/magenta accents.
 */
object KuiklyTheme {
    // --- Color ---
    val primary = Color(0xFF171717)
    val onPrimary = Color(0xFFFFFFFF)
    val foreground = Color(0xFF171717)
    val ink = Color(0xFF171717)
    val body = Color(0xFF4D4D4D)
    val muted = Color(0xFF888888)
    val canvas = Color(0xFFFFFFFF)
    val canvasSoft = Color(0xFFFAFAFA)
    val canvasSoft2 = Color(0xFFF5F5F5)
    val hairline = Color(0xFFEBEBEB)
    val hairlineStrong = Color(0xFFA1A1A1)

    val link = Color(0xFF0070F3)
    val linkDeep = Color(0xFF0761D1)
    val linkSoft = Color(0xFFD3E5FF)

    val success = Color(0xFF0070F3)
    val error = Color(0xFFEE0000)
    val errorSoft = Color(0xFFF7D4D6)
    val errorDeep = Color(0xFFC50000)
    val warning = Color(0xFFF5A623)
    val warningSoft = Color(0xFFFFEFCF)
    val warningDeep = Color(0xFFAB570A)

    val violet = Color(0xFF7928CA)
    val violetSoft = Color(0xFFD8CCF1)
    val cyan = Color(0xFF50E3C2)
    val cyanSoft = Color(0xFFAAFFEC)
    val highlightPink = Color(0xFFFF0080)
    val highlightMagenta = Color(0xFFEB367F)

    /** Chart accent palette (Vercel Token API colors). */
    val chartColors = listOf(
        Color(0xFF0070F3),
        Color(0xFF7928CA),
        Color(0xFF50E3C2),
        Color(0xFFFF0080),
        Color(0xFFF5A623),
    )

    /** Semantic cue colors for analytics list (anomaly / featured). */
    val anomaly = error
    val featured = warning

    // --- Radius ---
    val radiusNone = 0.dp
    val radiusXs = 4.dp
    val radiusSm = 6.dp
    val radiusMd = 8.dp
    val radiusLg = 12.dp
    val radiusXl = 16.dp
    val radiusPill = 100.dp

    // --- Spacing ---
    val spaceXxs = 4.dp
    val spaceXs = 8.dp
    val spaceSm = 12.dp
    val spaceMd = 16.dp
    val spaceLg = 24.dp
    val spaceXl = 32.dp

    // --- Font sizes (sp) ---
    val textCaption = 12.sp
    val textBodySm = 14.sp
    val textBodyMd = 16.sp
    val textTitle = 18.sp
    val textDisplaySm = 20.sp
    val textDisplayMd = 24.sp
}
