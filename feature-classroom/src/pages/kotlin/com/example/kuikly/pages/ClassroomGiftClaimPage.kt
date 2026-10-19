@file:OptIn(InternalResourceApi::class)

package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.Utils
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
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
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.draw.rotate
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.annotations.Page

/**
 * 领取礼品卡 — Flutter `ClaimGiftCardPage`
 * （`features/classroom/lib/view/claim_gift_card_page.dart`）复刻（P2-W4a 接入路由
 * `classroomClaimGift`）。
 *
 * **真源**：白卡（r16 阴影 16 / offset 4）+ 蓝色 180×渐变礼品卡（28 白圆 logo + 英语趣配音
 * 13 + 居中「Way to go ✨」28·bold·italic + 时长 14 + 类型 chip 10 + 🧑‍🎓 48 emoji）
 * + 信纸卡（左上 -0.3rad 回形针图标 + 称呼 15·w500 + message 14·h1.6 + 落款右对齐）。
 *
 * **刻度**：Flutter classroom 模块 view/theme **全用裸逻辑 px**（designWidth = 设备宽 480），
 * KMP 裸 `.dp`/`.sp` 在 golden 比对空间会整体偏大 16.7%，故用
 * `ProvideDesignScale(pagerData.pageViewWidth, designWidth = 480f)` + `.su`/`.susp` 对齐。
 *
 * ponytail 天花板（不弹回）：礼品卡资源图 → 用真实蓝渐变 + Unicode 字形近似；信纸落款日期
 * 为静态文案；领取动作为本地 toast「领取成功，可在背包中查看」+ closePage。
 *
 * **P2-V9a 抛光**：外白卡补 `BoxShadow(black 8% / blur 16)`（offset 受 Kuikly `.shadow` 限制
 * 无法表达）；内白卡补 `BoxShadow(black 6% / blur 8)`；message 加 `lineHeight = 22.4.susp`
 * （14susp × 1.6）；回形针图标改 `Modifier.offset(24.su, 0.su).rotate(-17.188f)`（-0.3 rad）。
 */
@Page("ClassroomGiftClaim")
internal class ClassroomGiftClaimPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            ProvideDesignScale(pagerData.pageViewWidth, designWidth = 480f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GiftPalette.noteBackground),
                ) {
                    AppNavBarBar(
                        title = "领取礼品卡",
                        topInset = top,
                        onBack = { Utils.currentBridgeModule().closePage() },
                        background = GiftPalette.noteBackground,
                        foreground = ClassroomPalette.titleBlack,
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 16.su,
                            top = 16.su,
                            end = 16.su,
                            bottom = (bottom + 16f).su,
                        ),
                    ) {
                        item { GiftCardVisual() }
                        item { Spacer(Modifier.height(10.su)) }
                        item { NotePaper() }
                        item { Spacer(Modifier.height(28.su)) }
                        item {
                            // Flutter `ElevatedButton(h48, r24, primaryGreen)` + 关闭当前页。
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.su)
                                    .background(
                                        ClassroomPalette.primaryGreen,
                                        RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.su),
                                    )
                                    .clickable {
                                        Utils.currentBridgeModule().toast("领取成功，可在背包中查看")
                                        Utils.currentBridgeModule().closePage()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "立即领取",
                                    fontSize = 16.susp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * `GiftClaim` 私有调色板：复用 `ClassroomPalette` 通用项（primaryGreen / titleBlack /
 * textGray / BUTTON_RADIUS），礼品卡专属色另起一份以免污染主 `ClassroomPalette`。
 *
 * `giftCardShadow` / `noteShadow` 对齐 Flutter 真源：`_GiftCardVisual` 外白卡
 * `BoxShadow(black 8% / blur 16 / offset(0,4))` 与 `_NotePaper` 内白卡
 * `BoxShadow(black 6% / blur 8 / offset(0,2))`。Kuikly `.shadow` 无 offset 参数，
 * elevation 16 / 8 区分两卡（PayListPage / ClassroomListPage 已知同款约束）。
 * 文案对齐 `ClassroomMockData.giftCard`（乌克丽丽 / 老坛酸菜 / 2026-05-20 /
 * 班级会员卡 / 1天 AI SVIP）。
 */
private object GiftPalette {
    val noteBackground = Color(0xFFF0F7FF)
    val noteAccent = Color(0xFF999999)
    val giftCardStart = Color(0xFF1677FF)
    val giftCardEnd = Color(0xFF0958D9)
    // Flutter `_GiftCardVisual` 外白卡 black 8%（0.08）。
    val giftCardShadow = Color(0x14000000)
    // Flutter `_NotePaper` 内白卡 black 6%（0.06）。
    val noteShadow = Color(0x0F000000)
}

/**
 * Flutter `_GiftCardVisual`：白卡 r16（black 8% / blur 16 / offset(0,4) 阴影）+ 16 padding +
 * 内部蓝渐变卡 r12 + 28 白圆 logo + 「英语趣配音」13 + 「Way to go ✨」28·bold·italic +
 * 时长 14 + chip 10 + 🧑‍🎓 48 emoji。
 */
@Composable
private fun GiftCardVisual() {
    val cardShape = RoundedCornerShape(16.su)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `_GiftCardVisual` 外白卡：black 8% / blur 16 / offset(0, 4)。
            // Kuikly `.shadow` 无 offset → 仅可近似的四向投影（天花板见 `PayListPage`）。
            .shadow(16.su, cardShape, ambientColor = GiftPalette.giftCardShadow, spotColor = GiftPalette.giftCardShadow)
            .background(Color.White, cardShape)
            .padding(16.su),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.su)
                .background(
                    // Flutter `LinearGradient(begin: topLeft, end: bottomRight)`。
                    Brush.linearGradient(
                        listOf(GiftPalette.giftCardStart, GiftPalette.giftCardEnd),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    ),
                    RoundedCornerShape(12.su),
                )
                .padding(16.su),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.su)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🦜", fontSize = 16.susp)
                }
                Spacer(Modifier.width(8.su))
                Text(
                    "英语趣配音",
                    fontSize = 13.susp,
                    color = Color.White,
                )
            }
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Way to go ✨",
                    fontSize = 28.susp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("1天 AI SVIP", fontSize = 14.susp, color = Color.White)
                    Spacer(Modifier.height(4.su))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.su))
                            .padding(horizontal = 8.su, vertical = 2.su),
                    ) {
                        Text("班级会员卡", fontSize = 10.susp, color = Color.White)
                    }
                }
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(
                        DrawableResource(ImageUri.commonAssets("classroom/emoji_grad_student.png").toUrl("")),
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(48.su),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

/**
 * Flutter `_NotePaper`：白卡 r4（black 6% / blur 8 / offset(0,2) 阴影）+ 左上 `Positioned(
 * top: -8, left: 24, rotate(-0.3 rad))` 的回形针图标 + 称呼 15·w500 + message 14·h1.6 +
 * 落款（teacher + date 右对齐）。回形针图标为资源 → 留位「📎」字形近似。
 */
@Composable
private fun NotePaper() {
    val noteShape = RoundedCornerShape(4.su)
    // Flutter `Stack(clipBehavior: Clip.none)` 允许回形针 top:-8 溢出；
    // LazyColumn 会裁切负 offset → 用 top padding 8 等效占位，回形针 y 改 0。
    Box(modifier = Modifier.padding(top = 8.su)) {
        // Flutter `Positioned(top: -8, left: 24, rotate(-0.3 rad))` 的回形针图标位：
        // -0.3 rad ≈ -17.188°；Kuikly `.offset` 不参与布局，仅位移渲染位置。
        Text(
            "📎",
            fontSize = 28.susp,
            color = GiftPalette.noteAccent,
            modifier = Modifier
                .offset(x = 24.su, y = 0.su)
                .rotate(-17.188f),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Flutter `_NotePaper` 内白卡：black 6% / blur 8 / offset(0, 2)。
                .shadow(8.su, noteShape, ambientColor = GiftPalette.noteShadow, spotColor = GiftPalette.noteShadow)
                .background(Color.White, noteShape)
                .padding(start = 20.su, top = 18.su, end = 20.su, bottom = 18.su),
        ) {
            Text(
                "乌克丽丽 同学：",
                fontSize = 15.susp,
                fontWeight = FontWeight.Medium,
                color = ClassroomPalette.titleBlack,
            )
            Spacer(Modifier.height(12.su))
            // Flutter `TextStyle(height: 1.6)` → 14sp × 1.6 = 22.4sp lineHeight。
            Text(
                "本次作业完成的很棒！老师送你一张体验卡，以资鼓励",
                fontSize = 14.susp,
                lineHeight = 22.4.susp,
                color = ClassroomPalette.titleBlack,
            )
            Spacer(Modifier.height(14.su))
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                    Text(
                        "老坛酸菜",
                        fontSize = 14.susp,
                        color = ClassroomPalette.titleBlack,
                    )
                    Spacer(Modifier.height(4.su))
                    Text(
                        "2026-05-20",
                        fontSize = 13.susp,
                        color = ClassroomPalette.textGray,
                    )
                }
            }
        }
    }
}
