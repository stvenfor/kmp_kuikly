package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.Utils
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
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
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.core.annotations.Page

/**
 * 「学习报告」— Flutter `HomeLearningReportPage`
 * （`features/home/lib/home/view/home_learning_report_page.dart`）复刻（Phase-2 / P2-W3）。
 *
 * 暗色主题真源 `theme/home_report_theme.dart`（`HomeReportColors`：bg `#0B0C11` /
 * 高光卡 `#121A1F` / 记录卡 `#17161F` / 记录条 `#242630` / 文案 `#F5F6F8` · `#8B9099`）。
 *
 * **刻度**：Flutter 本页命中 `.w/.h/.sp`（72 处，含 `.r`）→ `ProvideDesignScale` + `.su` / `.susp`。
 *
 * ponytail 天花板：`_EmojiIconBox(gradient: true)` 的 **topLeft→bottomRight 对角渐变**用
 * `Brush.horizontalGradient` 近似（Kuikly `Brush` 无对角/`sweepGradient` 构造）；图标本身是 emoji，
 * 无矢量资源问题。底部「家长助手」胶囊按 Flutter `Positioned(bottom+88)` 叠在会员卡上方。
 */
@Page(name = "LearningReport", moduleId = "feature_home")
internal class LearningReportPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            ProvideDesignScale(pagerData.pageViewWidth) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HomeReportPalette.background),
                ) {
                    AppNavBarBar(
                        title = "学习报告",
                        topInset = top,
                        onBack = { Utils.currentBridgeModule().closePage() },
                        background = HomeReportPalette.background,
                        foreground = HomeReportPalette.titleWhite,
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.su,
                                top = 12.su,
                                end = 16.su,
                                bottom = (bottom + 120f).su,
                            ),
                        ) {
                            item { SectionHeader(HomeReportPalette.dotYellow, "今日高光") }
                            item { Spacer(Modifier.height(10.su)) }
                            item { HighlightCard() }
                            item { Spacer(Modifier.height(20.su)) }
                            item { SectionHeader(HomeReportPalette.dotBlue, "今日学习记录") }
                            item { Spacer(Modifier.height(10.su)) }
                            item { LearningRecordCard() }
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(start = 16.su, end = 16.su, bottom = (bottom + 88f).su),
                            horizontalAlignment = Alignment.End,
                        ) {
                            ParentAssistantChip()
                            Spacer(Modifier.height(10.su))
                            MembershipBanner()
                        }
                    }
                }
            }
        }
    }
}

private object HomeReportPalette {
    val background = Color(0xFF0B0C11)
    val highlightCard = Color(0xFF121A1F)
    val highlightBorder = Color(0xFF1E3330)
    val recordCard = Color(0xFF17161F)
    val recordItem = Color(0xFF242630)
    val iconTeal = Color(0xFF1A3B38)
    val iconTealLight = Color(0xFF2A5A54)
    val titleWhite = Color(0xFFF5F6F8)
    val subtitleGrey = Color(0xFF8B9099)
    val metaGrey = Color(0xFF6B7078)
    val orange = Color(0xFFFF8A34)
    val orangeDeep = Color(0xFFE8742A)
    val dotYellow = Color(0xFFFFD54F)
    val dotBlue = Color(0xFF5EB3FF)
    val divider = Color(0xFF2A2D35)
    val bannerStart = Color(0xFF2A1810)
    val bannerEnd = Color(0xFF1A1210)
    val parentChip = Color(0xCC252830)
    val playBox = Color(0xFF2E3340)
}

private data class HighlightItem(val emoji: String, val title: String, val subtitle: String, val trailing: String)
private data class RecordItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val status: String,
    val statusHighlight: Boolean = false,
)

private val HIGHLIGHTS = listOf(
    HighlightItem("🎬", "《哈利波特》第3章", "视频配音 · 刚刚发布", "score:100"),
    HighlightItem("🏆", "解锁「45天」打卡勋章", "里程碑达成 · 太棒了！", "emoji:🎉"),
    HighlightItem("🎵", "\"Wingardium Leviosa!\"", "满分句子 · 可播放原声", "play"),
)

private val RECORDS = listOf(
    RecordItem("🎬", "视频配音", "哈利波特 第3章", "18:32", "已发布", statusHighlight = true),
    RecordItem("⚔️", "配音闯关", "Level 8 · 3关", "17:10", "15 min"),
    RecordItem("📚", "同步练", "PEP 五年级上册 Unit 3", "16:45", "8 min"),
    RecordItem("🤖", "AI 外教", "自由对话 · Tom老师", "15:20", "12 min"),
    RecordItem("🎧", "听力练习", "英美绕口令 · 5题", "14:00", "5 min"),
)

/** Flutter `_SectionHeader`：8 圆点 + 8 间隙 + 18·w600 标题。 */
@Composable
private fun SectionHeader(dotColor: Color, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.su).background(dotColor, CircleShape))
        Spacer(Modifier.width(8.su))
        Text(
            title,
            fontSize = 18.susp,
            fontWeight = FontWeight.SemiBold,
            color = HomeReportPalette.titleWhite,
        )
    }
}

/** Flutter `_HighlightCard`：r16 卡 + 3 行（行间 1 高内缩分隔线）。 */
@Composable
private fun HighlightCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeReportPalette.highlightCard, RoundedCornerShape(16.su))
            .border(1.su, HomeReportPalette.highlightBorder, RoundedCornerShape(16.su)),
    ) {
        HIGHLIGHTS.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.su, vertical = 14.su),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmojiIconBox(emoji = item.emoji, gradient = true)
                Spacer(Modifier.width(12.su))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        fontSize = 15.susp,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeReportPalette.titleWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.su))
                    Text(
                        item.subtitle,
                        fontSize = 12.susp,
                        color = HomeReportPalette.subtitleGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(8.su))
                HighlightTrailing(item.trailing)
            }
            if (index < HIGHLIGHTS.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.su)
                        .height(1.su)
                        .background(HomeReportPalette.divider),
                )
            }
        }
    }
}

/** Flutter `_HighlightTrailing`：`score:100` / `emoji:🎉` / `play` 三种形制。 */
@Composable
private fun HighlightTrailing(spec: String) {
    when {
        spec.startsWith("score:") -> Box(
            modifier = Modifier
                .background(HomeReportPalette.orangeDeep, RoundedCornerShape(12.su))
                .padding(horizontal = 10.su, vertical = 4.su),
        ) {
            Text(
                spec.removePrefix("score:"),
                fontSize = 13.susp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        spec.startsWith("emoji:") -> Text(spec.removePrefix("emoji:"), fontSize = 24.susp)
        else -> Box(
            modifier = Modifier
                .size(32.su)
                .background(HomeReportPalette.playBox, RoundedCornerShape(8.su)),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", fontSize = 20.susp, color = HomeReportPalette.dotBlue)
        }
    }
}

/** Flutter `_LearningRecordCard`：r16 卡（padding 12）内 5 条 r12 记录条 + 10 间隙。 */
@Composable
private fun LearningRecordCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeReportPalette.recordCard, RoundedCornerShape(16.su))
            .padding(12.su),
    ) {
        RECORDS.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HomeReportPalette.recordItem, RoundedCornerShape(12.su))
                    .padding(horizontal = 12.su, vertical = 12.su),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmojiIconBox(emoji = item.emoji, gradient = false)
                Spacer(Modifier.width(12.su))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        fontSize = 15.susp,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeReportPalette.titleWhite,
                    )
                    Spacer(Modifier.height(4.su))
                    Text(
                        item.subtitle,
                        fontSize = 12.susp,
                        color = HomeReportPalette.subtitleGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(item.time, fontSize = 12.susp, color = HomeReportPalette.metaGrey)
                    Spacer(Modifier.height(4.su))
                    Text(
                        item.status,
                        fontSize = 12.susp,
                        fontWeight = if (item.statusHighlight) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (item.statusHighlight) {
                            HomeReportPalette.orange
                        } else {
                            HomeReportPalette.metaGrey
                        },
                    )
                }
            }
            Spacer(Modifier.height(10.su))
        }
    }
}

/** Flutter `_EmojiIconBox`：44×44 r12；`gradient` 走 teal 渐变，否则记录条底 + 1 边框。 */
@Composable
private fun EmojiIconBox(emoji: String, gradient: Boolean) {
    Box(
        modifier = Modifier
            .size(44.su)
            .background(
                brush = if (gradient) {
                    Brush.horizontalGradient(
                        listOf(HomeReportPalette.iconTealLight, HomeReportPalette.iconTeal),
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(HomeReportPalette.recordItem, HomeReportPalette.recordItem),
                    )
                },
                shape = RoundedCornerShape(12.su),
            )
            .then(
                if (gradient) {
                    Modifier
                } else {
                    Modifier.border(1.su, HomeReportPalette.divider, RoundedCornerShape(12.su))
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 22.susp)
    }
}

/** Flutter `_MembershipBanner`：暖橙渐变 r16 卡 + 皇冠 + 文案 + 「立即开通」胶囊。 */
@Composable
private fun MembershipBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(HomeReportPalette.bannerStart, HomeReportPalette.bannerEnd),
                ),
                RoundedCornerShape(16.su),
            )
            .border(1.su, Color(0xFF3D2A20), RoundedCornerShape(16.su))
            .padding(horizontal = 14.su, vertical = 14.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("👑", fontSize = 28.susp)
        Spacer(Modifier.width(10.su))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "开通会员，解锁全部内容",
                fontSize = 14.susp,
                fontWeight = FontWeight.SemiBold,
                color = HomeReportPalette.titleWhite,
            )
            Spacer(Modifier.height(4.su))
            Text(
                "全量剧集 · AI外教不限时 · 专属勋章",
                fontSize = 11.susp,
                color = HomeReportPalette.subtitleGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.su))
        Box(
            modifier = Modifier
                .background(HomeReportPalette.orangeDeep, RoundedCornerShape(20.su))
                .clickable { Utils.currentBridgeModule().toast("「立即开通」即将接入") }
                .padding(horizontal = 14.su, vertical = 8.su),
        ) {
            Text(
                "立即开通",
                fontSize = 13.susp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

/** Flutter `_ParentAssistantChip`：半透底 20 圆角胶囊 + 👪 + 12 文案。 */
@Composable
private fun ParentAssistantChip() {
    Row(
        modifier = Modifier
            .background(HomeReportPalette.parentChip, RoundedCornerShape(20.su))
            .border(1.su, HomeReportPalette.divider, RoundedCornerShape(20.su))
            .clickable { Utils.currentBridgeModule().toast("「家长助手」即将接入") }
            .padding(horizontal = 12.su, vertical = 8.su),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("👪", fontSize = 16.susp, color = HomeReportPalette.subtitleGrey)
        Spacer(Modifier.width(6.su))
        Text("家长助手", fontSize = 12.susp, color = HomeReportPalette.subtitleGrey)
    }
}
