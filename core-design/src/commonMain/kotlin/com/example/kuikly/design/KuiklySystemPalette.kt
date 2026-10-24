package com.example.kuikly.design

import com.tencent.kuikly.compose.ui.graphics.Color

/**
 * iOS System 语义色板 —— 全仓唯一源（core-design 是设计 token 的单一出口，见 SCHEME §3 / §6）。
 *
 * 这些色值来自 iOS 系统调色板（systemBlue / systemBackground / label 等），与 Flutter 参考图
 * 对齐，是 36/36 golden-diff 稳定的基准之一。**新增或修改此处色值会直接改变渲染像素，须重跑
 * golden 验收**，故固定不动。
 *
 * 此前 Community / Home / Mine / Chat 四个业务 Tab 各自重复声明了同一组色值（core-design 被绕过）；
 * 现统一收敛到此对象，业务 Palette 通过 `: KuiklySystemPalette()` 继承复用，消除重复定义。
 * 各 feature 自有品牌色板（Pay 金、Video/Classroom 绿、HomeReport 深色等）底色不同，仍保留在
 * 各自 feature 模块内，属合法的 feature 级 token（见 ADR-0002 混合分层）。
 */
open class KuiklySystemPalette {
    // ── 基础语义色（与 iOS system 对应）──
    val accent = Color(0xFF007AFF)          // systemBlue
    val background = Color(0xFFF2F2F7)       // systemBackground（组）
    val surface = Color(0xFFFFFFFF)          // systemSurface
    val fillSecondary = Color(0xFFE9E9EB)    // systemFill 二级
    val labelPrimary = Color(0xFF000000)     // label
    val labelSecondary = Color(0x993C3C43)   // label 60%
    val labelTertiary = Color(0x4D3C3C43)    // label 30%
    val separator = Color(0xFFC6C6C8)        // separator / opaqueSeparator

    // ── 通用强调 / 状态色 ──
    val likeRed = Color(0xFFFF3B30)          // systemRed（点赞 / 销毁 / 未读角标共用）
    val destructive = Color(0xFFFF3B30)      // = likeRed（Mine 销毁按钮）
    val badgeRed = Color(0xFFFF3B30)         // = likeRed（Mine 角标）
    val unreadBadge = Color(0xFFFF3B30)      // = likeRed（Chat 未读）
    val badgeOrange = Color(0xFFFF9500)      // systemOrange
    val iconOrange = Color(0xFFFF9500)       // = badgeOrange（Mine 图标）
    val iconPurple = Color(0xFF5856D6)       // systemPurple
    val iconBlue = Color(0xFF007AFF)         // = accent（Mine 图标）
    val iconGreen = Color(0xFF34C759)        // systemGreen
    val online = Color(0xFF34C759)           // = iconGreen（Chat 在线绿）

    // ── 文本 / 链接 ──
    val textDarkGray = Color(0xFF3C3C43)     // 深灰文字（Home）
    val richLink = Color(0xFF576B95)         // Flutter 富文本 #话题 / @用户 / 链接
    val commentFill = Color(0xFFF1F1F4)      // Flutter surfaceContainerHighest @50%
    val onSurface = Color(0xFF1A1B20)        // Flutter onSurface（评论正文）

    // ── 浅色蒙层（实色 alpha，IOS 风格 tint）──
    val softBlue = Color(0x14007AFF)
    val softPurple = Color(0x145856D6)
    val softOrange = Color(0x14FF9500)

    // ── 圆角（设计稿裸 px；组级容器用）──
    val radiusMdDesign = 12
    val radiusLgDesign = 14
}
