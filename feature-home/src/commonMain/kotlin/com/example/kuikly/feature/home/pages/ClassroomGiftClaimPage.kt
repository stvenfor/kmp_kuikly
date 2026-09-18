package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
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
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
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
 * **刻度**：Flutter classroom 模块 view/theme **全用裸逻辑 px**，故一律裸 `dp`/`sp`，
 * 不加 `ProvideDesignScale`（判断法与 `ClassroomPalette` 同）。
 *
 * ponytail 天花板（不弹回）：礼品卡资源图 → 用真实蓝渐变 + Unicode 字形近似；信纸落款日期
 * 为静态文案；领取动作为本地 toast「领取成功，可在背包中查看」+ closePage。
 *
 * **P2-V9a 抛光**：外白卡补 `BoxShadow(black 8% / blur 16)`（offset 受 Kuikly `.shadow` 限制
 * 无法表达）；内白卡补 `BoxShadow(black 6% / blur 8)`；message 加 `lineHeight = 22.4.sp`
 * （14sp × 1.6）；回形针图标改 `Modifier.offset(24.dp, -8.dp).rotate(-17.188f)`（-0.3 rad）。
 */
@Page(name = "ClassroomGiftClaim", moduleId = "feature_home")
internal class ClassroomGiftClaimPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
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
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = (bottom + 16f).dp,
                    ),
                ) {
                    item { GiftCardVisual() }
                    item { Spacer(Modifier.height(24.dp)) }
                    item { NotePaper() }
                    item { Spacer(Modifier.height(32.dp)) }
                    item {
                        // Flutter `ElevatedButton(h48, r24, primaryGreen)` + 关闭当前页。
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    ClassroomPalette.primaryGreen,
                                    RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.dp),
                                )
                                .clickable {
                                    Utils.currentBridgeModule().toast("领取成功，可在背包中查看")
                                    Utils.currentBridgeModule().closePage()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "立即领取",
                                fontSize = 16.sp,
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
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `_GiftCardVisual` 外白卡：black 8% / blur 16 / offset(0, 4)。
            // Kuikly `.shadow` 无 offset → 仅可近似的四向投影（天花板见 `PayListPage`）。
            .shadow(16.dp, cardShape, ambientColor = GiftPalette.giftCardShadow, spotColor = GiftPalette.giftCardShadow)
            .background(Color.White, cardShape)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    // Flutter `LinearGradient(begin: topLeft, end: bottomRight)`。
                    Brush.linearGradient(
                        listOf(GiftPalette.giftCardStart, GiftPalette.giftCardEnd),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    ),
                    RoundedCornerShape(12.dp),
                )
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🦜", fontSize = 16.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "英语趣配音",
                    fontSize = 13.sp,
                    color = Color.White,
                )
            }
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Way to go ✨",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("1天 AI SVIP", fontSize = 14.sp, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("班级会员卡", fontSize = 10.sp, color = Color.White)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("🧑‍🎓", fontSize = 48.sp)
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
    val noteShape = RoundedCornerShape(4.dp)
    // Flutter `Stack(clipBehavior: Clip.none)` 允许回形针 top:-8 溢出；
    // LazyColumn 会裁切负 offset → 用 top padding 8 等效占位，回形针 y 改 0。
    Box(modifier = Modifier.padding(top = 8.dp)) {
        // Flutter `Positioned(top: -8, left: 24, rotate(-0.3 rad))` 的回形针图标位：
        // -0.3 rad ≈ -17.188°；Kuikly `.offset` 不参与布局，仅位移渲染位置。
        Text(
            "📎",
            fontSize = 28.sp,
            color = GiftPalette.noteAccent,
            modifier = Modifier
                .offset(x = 24.dp, y = 0.dp)
                .rotate(-17.188f),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Flutter `_NotePaper` 内白卡：black 6% / blur 8 / offset(0, 2)。
                .shadow(8.dp, noteShape, ambientColor = GiftPalette.noteShadow, spotColor = GiftPalette.noteShadow)
                .background(Color.White, noteShape)
                .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Text(
                "乌克丽丽 同学：",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ClassroomPalette.titleBlack,
            )
            Spacer(Modifier.height(12.dp))
            // Flutter `TextStyle(height: 1.6)` → 14sp × 1.6 = 22.4sp lineHeight。
            Text(
                "本次作业完成的很棒！老师送你一张体验卡，以资鼓励",
                fontSize = 14.sp,
                lineHeight = 22.4.sp,
                color = ClassroomPalette.titleBlack,
            )
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                    Text(
                        "老坛酸菜",
                        fontSize = 14.sp,
                        color = ClassroomPalette.titleBlack,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "2026-05-20",
                        fontSize = 13.sp,
                        color = ClassroomPalette.textGray,
                    )
                }
            }
        }
    }
}
