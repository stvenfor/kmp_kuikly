package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.Utils
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Switch
import com.tencent.kuikly.compose.material3.SwitchDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.core.annotations.Page

/**
 * 「签到商城」— Flutter `CheckInMallPage`
 * （`features/home/lib/home/view/check_in_mall_page.dart`）复刻（Phase-2 / P2-W3）。
 *
 * 真源 + `theme/check_in_mall_theme.dart`（`CheckInMallTheme`：蓝 `#4A90E2` / 背景 `#F5F6F8` /
 * 金币金 `#FFB800` / 分隔 `#E8E8E8`）。
 *
 * **刻度**：Flutter 本页命中 `.w/.h/.sp`（78 处）→ `ProvideDesignScale` + `.su` / `.susp`。
 *
 * ponytail 天花板：Material icon（arrow_upward / chat_bubble / person_add / volume_up /
 * help_outline / card_giftcard）无矢量资源 → Unicode 字形近似（同 W2c 口径）。
 */
@Page(name = "CheckInMall", moduleId = "feature_home")
internal class CheckInMallPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            ProvideDesignScale(pagerData.pageViewWidth) {
                var reminderEnabled by remember { mutableStateOf(false) }

                // P2-V8c：Flutter 真源是**单个** `CustomScrollView`（slivers[0] = `_buildHeader`，
                // check_in_mall_page.dart:66-83），即「蓝头 + 公告 + 双指标」随内容一起滚出视口。
                // 原实现把 header 放在滚动容器外（钉死）→ 改为首个 `item`，口径同 P2-V1b `MineTab`。
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CheckInPalette.background),
                    contentPadding = PaddingValues(bottom = (bottom + 24f).su),
                ) {
                    item { CheckInHeader(topInset = top) }
                    item {
                        CheckInCard(
                            reminderEnabled = reminderEnabled,
                            onReminderChange = { reminderEnabled = it },
                        )
                    }
                    item { Spacer(Modifier.height(16.su)) }
                    item { TaskSection() }
                    item { Spacer(Modifier.height(16.su)) }
                    item { GiftSection() }
                }
            }
        }
    }
}

private object CheckInPalette {
    val primaryBlue = Color(0xFF4A90E2)
    val background = Color(0xFFF5F6F8)
    val cardWhite = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF666666)
    val textHint = Color(0xFF999999)
    val coinGold = Color(0xFFFFB800)
    val divider = Color(0xFFE8E8E8)
    val noticeBlue = Color(0xFF3A8EE6)
    val iconBlueBg = Color(0xFFE8F1FA)
    val pendingBg = Color(0xFFF5F6F8)
}

private enum class CheckInStatus { Signed, Today, Pending }

private data class CheckInDay(val day: Int, val reward: Int, val status: CheckInStatus)

private val CHECK_IN_DAYS = listOf(
    CheckInDay(1, 5, CheckInStatus.Signed),
    CheckInDay(2, 5, CheckInStatus.Today),
    CheckInDay(3, 10, CheckInStatus.Pending),
    CheckInDay(4, 5, CheckInStatus.Pending),
    CheckInDay(5, 5, CheckInStatus.Pending),
    CheckInDay(6, 5, CheckInStatus.Pending),
    CheckInDay(7, 20, CheckInStatus.Pending),
)

private data class TaskItem(
    val glyph: String,
    val title: String,
    val subtitle: String,
    val showHelp: Boolean = false,
    val actionText: String,
    val completed: Boolean = false,
)

private val TASKS = listOf(
    TaskItem("↑", "每日登录", "+10成长值 +5i车币", actionText = "去完成"),
    TaskItem("💬", "回复客户消息", "+50成长值 +20i车币", showHelp = true, actionText = "已完成", completed = true),
    TaskItem("👤＋", "邀请新销售顾问", "+200成长值 +50i车币", actionText = "去邀请"),
)

private const val CONSECUTIVE_DAYS = 26
private const val I_CAR_COINS = 320
private const val GROWTH_VALUE = 780

// ───────────────────────── header ─────────────────────────

/** Flutter `_buildHeader`：蓝底（透明 navBar）→ 公告条 → 双指标 → 16 收尾。 */
@Composable
private fun CheckInHeader(topInset: Float) {
    Column(modifier = Modifier.fillMaxWidth().background(CheckInPalette.primaryBlue)) {
        AppNavBarBar(
            title = "签到商城",
            topInset = topInset,
            onBack = { Utils.currentBridgeModule().closePage() },
            background = CheckInPalette.primaryBlue,
            foreground = Color.White,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.su)
                .background(CheckInPalette.noticeBlue, RoundedCornerShape(4.su))
                .padding(horizontal = 12.su, vertical = 8.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔊", fontSize = 16.susp, color = Color.White)
            Spacer(Modifier.width(8.su))
            Text(
                "温馨提示：本页面只保留近3个月内的i车币记录",
                modifier = Modifier.weight(1f),
                fontSize = 12.susp,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(16.su))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.su)) {
            StatItem(
                label = "我的i车币",
                value = I_CAR_COINS.toString(),
                modifier = Modifier.weight(1f),
            )
            StatItem(
                label = "我的成长值",
                value = GROWTH_VALUE.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.su))
    }
}

/** Flutter `_buildStatItem`：13 白80 标签 + ›，32 加粗白数值 + ?。 */
@Composable
private fun StatItem(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier.clickable { Utils.currentBridgeModule().toast("「$label」明细即将接入") }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 13.susp, color = Color.White.copy(alpha = 0.8f))
            Text("›", fontSize = 16.susp, color = Color.White.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(4.su))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 32.susp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.width(4.su))
            Text("?", fontSize = 16.susp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

// ───────────────────────── 签到卡 ─────────────────────────

/** Flutter `_buildCheckInCard`：白卡 r12 + 标题/累计/立即签到 + 7 日 + 断签提示/提醒开关。 */
@Composable
private fun CheckInCard(reminderEnabled: Boolean, onReminderChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `_buildCheckInCard` 的 `margin` 只有 `symmetric(horizontal: 16.w)`：
            // 与 header 尾部的 `SizedBox(height: 16.h)` 合起来正好 16 间距（原多出 top 16 → 32）。
            .padding(horizontal = 16.su)
            .background(CheckInPalette.cardWhite, RoundedCornerShape(12.su))
            .padding(16.su),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "连签7日可得65成长值",
                    fontSize = 15.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = CheckInPalette.textPrimary,
                )
                Spacer(Modifier.height(4.su))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("已累计签到 ", fontSize = 12.susp, color = CheckInPalette.textSecondary)
                    Text(
                        CONSECUTIVE_DAYS.toString(),
                        fontSize = 12.susp,
                        fontWeight = FontWeight.SemiBold,
                        color = CheckInPalette.primaryBlue,
                    )
                    Text(" 天", fontSize = 12.susp, color = CheckInPalette.textSecondary)
                }
            }
            Box(
                modifier = Modifier
                    .background(CheckInPalette.primaryBlue, RoundedCornerShape(20.su))
                    .clickable { Utils.currentBridgeModule().toast("签到成功 · +5成长值") }
                    .padding(horizontal = 16.su, vertical = 8.su),
            ) {
                Text(
                    "立即签到",
                    fontSize = 14.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
        Spacer(Modifier.height(16.su))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CHECK_IN_DAYS.forEach { CheckInDayItem(it) }
        }
        Spacer(Modifier.height(12.su))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("断签或者签完需重新开始", fontSize = 12.susp, color = CheckInPalette.textHint)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("签到提醒", fontSize = 12.susp, color = CheckInPalette.textSecondary)
                Spacer(Modifier.width(8.su))
                // Flutter 真源 `SizedBox(44.w × 24.h)` + `Switch`：M3 track 固定 52×32，
                // 收缩约束下**居中溢出**（`_computeTrackPaintOffset` 取 (canvas - track)/2，
                // flutter/lib/src/material/switch.dart:1712-1718）→ 故对齐用 Center 而非 CenterEnd。
                Box(modifier = Modifier.size(44.su, 24.su), contentAlignment = Alignment.Center) {
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = onReminderChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CheckInPalette.primaryBlue,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = CheckInPalette.textHint.copy(alpha = 0.3f),
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }
        }
    }
}

/** Flutter `_buildDayItem`：40×40 r8 奖励块 + 4 + 11 状态标签。 */
@Composable
private fun CheckInDayItem(day: CheckInDay) {
    val (bg, fg, label) = when (day.status) {
        CheckInStatus.Signed -> Triple(CheckInPalette.primaryBlue, Color.White, "已签")
        CheckInStatus.Today -> Triple(CheckInPalette.coinGold, Color.White, "${day.day}天")
        CheckInStatus.Pending -> Triple(CheckInPalette.pendingBg, CheckInPalette.textSecondary, "${day.day}天")
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier
                .size(40.su)
                .background(bg, RoundedCornerShape(8.su)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "+${day.reward}",
                fontSize = 12.susp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
            Text("→", fontSize = 10.susp, color = fg.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(4.su))
        Text(
            label,
            fontSize = 11.susp,
            color = if (day.status == CheckInStatus.Signed) {
                CheckInPalette.primaryBlue
            } else {
                CheckInPalette.textSecondary
            },
        )
    }
}

// ───────────────────────── 任务 / 礼品 ─────────────────────────

/** Flutter `_buildTaskSection`：16·w600 标题 + 白卡 r12 内 3 条（分隔线 indent 56）。 */
@Composable
private fun TaskSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.su)) {
        Text(
            "成长值任务",
            fontSize = 16.susp,
            fontWeight = FontWeight.SemiBold,
            color = CheckInPalette.textPrimary,
        )
        Spacer(Modifier.height(12.su))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CheckInPalette.cardWhite, RoundedCornerShape(12.su)),
        ) {
            TASKS.forEachIndexed { index, task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.su, vertical = 12.su),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.su)
                            .background(CheckInPalette.iconBlueBg, RoundedCornerShape(8.su)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(task.glyph, fontSize = 20.susp, color = CheckInPalette.primaryBlue)
                    }
                    Spacer(Modifier.width(12.su))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                task.title,
                                fontSize = 14.susp,
                                fontWeight = FontWeight.Medium,
                                color = CheckInPalette.textPrimary,
                            )
                            if (task.showHelp) {
                                Spacer(Modifier.width(4.su))
                                Text("?", fontSize = 14.susp, color = CheckInPalette.textHint)
                            }
                        }
                        Spacer(Modifier.height(2.su))
                        Text(task.subtitle, fontSize = 12.susp, color = CheckInPalette.textSecondary)
                    }
                    TaskActionButton(task)
                }
                if (index < TASKS.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 56.su, end = 16.su)
                            .height(1.su)
                            .background(CheckInPalette.divider),
                    )
                }
            }
        }
    }
}

/** Flutter `_buildTaskButton`：primary（蓝底白字 r4）/ completed（灰底灰字 r4）。 */
@Composable
private fun TaskActionButton(task: TaskItem) {
    Box(
        modifier = Modifier
            .background(
                if (task.completed) CheckInPalette.pendingBg else CheckInPalette.primaryBlue,
                RoundedCornerShape(4.su),
            )
            .clickable(enabled = !task.completed) {
                Utils.currentBridgeModule().toast("「${task.title}」即将接入")
            }
            .padding(horizontal = 12.su, vertical = 6.su),
    ) {
        Text(
            task.actionText,
            fontSize = 12.susp,
            fontWeight = if (task.completed) FontWeight.Normal else FontWeight.Medium,
            color = if (task.completed) CheckInPalette.textHint else Color.White,
        )
    }
}

/** Flutter `_buildGiftSection`：16·w600 标题 + 40 + 居中 80 礼品占位 + 14 文案 + 40。 */
@Composable
private fun GiftSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.su)) {
        Text(
            "i车币换礼",
            fontSize = 16.susp,
            fontWeight = FontWeight.SemiBold,
            color = CheckInPalette.textPrimary,
        )
        Spacer(Modifier.height(40.su))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Flutter `_buildGiftSection` 只有一个 80.sp 的 `card_giftcard_outlined`
            // （color: textHint@30%，无任何底色/圆形底板）→ 去掉原自造的 80 圆底。
            Box(modifier = Modifier.size(80.su), contentAlignment = Alignment.Center) {
                Text("🎁", fontSize = 40.susp)
            }
            Spacer(Modifier.height(16.su))
            Text("商城筹备中，礼品马上就到", fontSize = 14.susp, color = CheckInPalette.textHint)
        }
        Spacer(Modifier.height(40.su))
    }
}
