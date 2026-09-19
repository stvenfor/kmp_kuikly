package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.navigation.PageNames
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
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 配音首页 — Flutter `DubbingHomePage` 结构复刻（P8-E1b）。
 * 真源：`features/home/lib/home/view/dubbing_home_page.dart` + widgets/dubbing_home。
 *
 * ponytail：裸 `dp`/`sp`；PNG 图标/封面无加载器 → 色块 + 几何近似（同 W2c 口径）。
 */
@Page(name = "DubbingHome", moduleId = "feature_home")
internal class DubbingHomePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var selectedCategory by remember { mutableStateOf(1) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DubbingHomePalette.background),
            ) {
                Spacer(modifier = Modifier.height(top.dp))
                DubbingHeader()
                DubbingCategoryTabs(
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = (bottom + 24f).dp),
                ) {
                    item { DubbingBannerCarousel() }
                    item { DubbingFeatureRow() }
                    item {
                        DubbingSectionHeader(
                            title = "最近在学",
                            trailing = SectionTrailing.Chevron,
                        )
                    }
                    item { DubbingRecentLearningRow() }
                    item {
                        DubbingSectionHeader(
                            title = "新手赛场",
                            trailing = SectionTrailing.Refresh,
                        )
                    }
                    item { DubbingExpertShowcaseGrid() }
                    item { DubbingHotRankSection() }
                    item {
                        DubbingSectionHeader(
                            title = "猜你喜欢",
                            trailing = SectionTrailing.None,
                        )
                    }
                    item { DubbingGuessYouLikeList() }
                }
            }
        }
    }
}

// ── palette / mock（Flutter DubbingHomeTheme + dubbing_home_mock_data.dart） ──

private object DubbingHomePalette {
    val background = Color.White
    val primaryGreen = Color(0xFF45D1A1)
    val titleBlack = Color(0xFF1A1A1A)
    val textGray = Color(0xFF666666)
    val subtitleGray = Color(0xFF999999)
    val searchFieldBackground = Color(0xFFF5F5F5)
    val divider = Color(0xFFEEEEEE)
    val svipGold = Color(0xFFD4A017)
    val cardRadius = 12.dp
    val thumbRadius = 8.dp
}

private data class RecentItem(val title: String, val duration: String, val thumbColor: Color)
private data class ExpertItem(
    val title: String,
    val userName: String,
    val subtitle: String,
    val thumbColor: Color,
)
private data class GuessItem(val title: String, val playCount: String, val thumbColor: Color)

private val RECENT_ITEMS = listOf(
    RecentItem("穿梭在迷宫的勇士", "03:24", Color(0xFFB3E5FC)),
    RecentItem("萌宠部落", "02:18", Color(0xFFC8E6C9)),
    RecentItem("完美的世界", "04:05", Color(0xFFFFE0B2)),
)

private val EXPERT_ITEMS = listOf(
    ExpertItem("英语启蒙课堂", "蓝儿老师Joyue", "跟读练习 · 初级", Color(0xFFE1BEE7)),
    ExpertItem("趣味配音挑战", "配音达人", "动画配音 · 中级", Color(0xFFBBDEFB)),
)

private val GUESS_ITEMS = listOf(
    GuessItem("趣味英语配音", "6.8万", Color(0xFFFFF3E0)),
    GuessItem("经典电影片段", "9.2万", Color(0xFFE8F5E9)),
)

private val FEATURE_LABELS = listOf("每日打卡", "影视单词", "经典剧场", "排行榜", "全部视频")

private enum class SectionTrailing { None, Chevron, Refresh }

// ── header ──

@Composable
private fun DubbingHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹",
            fontSize = 22.sp,
            color = DubbingHomePalette.titleBlack,
            modifier = Modifier
                .clickable { Utils.currentBridgeModule().closePage() }
                .padding(12.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(DubbingHomePalette.searchFieldBackground, RoundedCornerShape(18.dp))
                .clickable { Utils.currentBridgeModule().openPage(PageNames.Search) }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(DubbingHomePalette.subtitleGray.copy(alpha = 0.35f), CircleShape),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("学英语", fontSize = 14.sp, color = DubbingHomePalette.subtitleGray)
            }
        }
        HeaderIconButton(glyph = "↺")
        HeaderIconButton(glyph = "◌", showBadge = true)
    }
}

@Composable
private fun HeaderIconButton(glyph: String, showBadge: Boolean = false) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable { },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 20.sp, color = DubbingHomePalette.titleBlack)
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(7.dp)
                    .background(Color(0xFFFF4D4F), CircleShape),
            )
        }
    }
}

// ── category tabs ──

@Composable
private fun DubbingCategoryTabs(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Row(modifier = Modifier.weight(1f)) {
            listOf("SVIP", "配音", "听力", "小剧场", "专题").forEachIndexed { index, label ->
                val active = index == selected
                Column(
                    modifier = Modifier
                        .clickable { onSelect(index) }
                        .padding(end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        label,
                        fontSize = 16.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            index == 0 -> DubbingHomePalette.svipGold
                            active -> DubbingHomePalette.primaryGreen
                            else -> DubbingHomePalette.textGray
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(if (active) 24.dp else 0.dp)
                            .height(3.dp)
                            .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable { },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(DubbingHomePalette.primaryGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 12.dp)
                        .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}

// ── banner ──

@Composable
private fun DubbingBannerCarousel(activeIndex: Int = 0) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFE082)),
                    ),
                    RoundedCornerShape(DubbingHomePalette.cardRadius),
                ),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp)
                    .background(Color(0xFFFFC107).copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 10.dp)
                                .background(Color(0xFFFFB300), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 10.dp)
                                .background(Color(0xFFFFB300), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 10.dp)
                                .background(Color(0xFFFFB300), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(22.dp)
                            .background(Color(0xFFFFB300), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text("AD", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (index == activeIndex) 14.dp else 6.dp)
                        .height(6.dp)
                        .background(
                            if (index == activeIndex) DubbingHomePalette.primaryGreen else DubbingHomePalette.divider,
                            RoundedCornerShape(3.dp),
                        ),
                )
            }
        }
    }
}

// ── feature row ──

@Composable
private fun DubbingFeatureRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 12.dp),
    ) {
        FEATURE_LABELS.forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FeatureIcon(index)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    color = DubbingHomePalette.titleBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FeatureIcon(index: Int) {
    Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
        when (index) {
            0 -> {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(22.dp, 18.dp)
                                .background(Color(0xFF66BB6A), RoundedCornerShape(3.dp)),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF43A047), CircleShape),
                        )
                    }
                }
            }
            1 -> {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(width = 6.dp, height = 18.dp)
                                    .background(Color(0xFF42A5F5), RoundedCornerShape(2.dp)),
                            )
                        }
                    }
                }
            }
            2 -> {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 8.dp, height = 6.dp)
                                        .background(Color(0xFFFFB300), RoundedCornerShape(2.dp)),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(12.dp)
                                .background(Color(0xFFFFA000), RoundedCornerShape(2.dp)),
                        )
                    }
                }
            }
            3 -> {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFF3E5F5), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf(
                            Triple(Color(0xFFFFC107), 22.dp, 6.dp),
                            Triple(Color(0xFFCFD8DC), 18.dp, 5.dp),
                            Triple(Color(0xFFFFCCBC), 14.dp, 4.dp),
                        ).forEach { (color, width, height) ->
                            Box(
                                modifier = Modifier
                                    .width(width)
                                    .height(height)
                                    .background(color, RoundedCornerShape(2.dp)),
                            )
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFECEFF1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF78909C), RoundedCornerShape(6.dp)),
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text("限免", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── section headers ──

@Composable
private fun DubbingSectionHeader(title: String, trailing: SectionTrailing) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = DubbingHomePalette.titleBlack,
            modifier = Modifier.weight(1f),
        )
        when (trailing) {
            SectionTrailing.Chevron -> {
                Text(
                    "›",
                    fontSize = 20.sp,
                    color = DubbingHomePalette.subtitleGray,
                    modifier = Modifier.clickable { }.padding(8.dp),
                )
            }
            SectionTrailing.Refresh -> {
                Row(
                    modifier = Modifier.clickable { }.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("↻", fontSize = 14.sp, color = DubbingHomePalette.subtitleGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("换一换", fontSize = 13.sp, color = DubbingHomePalette.subtitleGray)
                }
            }
            SectionTrailing.None -> Unit
        }
    }
}

// ── cover thumbnail + duration chip ──

@Composable
private fun DubbingCoverImage(
    thumbColor: Color,
    width: Modifier,
    height: Dp,
    duration: String? = null,
) {
    Box(
        modifier = width
            .height(height)
            .background(thumbColor, RoundedCornerShape(DubbingHomePalette.thumbRadius)),
    ) {
        if (duration != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 6.dp)
                    .background(Color(0x8C000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("×", fontSize = 10.sp, color = Color.White)
                Spacer(modifier = Modifier.width(3.dp))
                Text(duration, fontSize = 10.sp, color = Color.White)
            }
        }
    }
}

// ── recent learning ──

@Composable
private fun DubbingRecentLearningRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RECENT_ITEMS.forEach { item ->
            Column(modifier = Modifier.width(120.dp)) {
                DubbingCoverImage(
                    thumbColor = item.thumbColor,
                    width = Modifier.fillMaxWidth(),
                    height = 68.dp,
                    duration = item.duration,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    item.title,
                    fontSize = 12.sp,
                    color = DubbingHomePalette.titleBlack,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

// ── expert showcase ──

@Composable
private fun DubbingExpertShowcaseGrid() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EXPERT_ITEMS.forEach { item ->
            Column(modifier = Modifier.weight(1f)) {
                DubbingCoverImage(
                    thumbColor = item.thumbColor,
                    width = Modifier.fillMaxWidth(),
                    height = 96.dp,
                    duration = "02:30",
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0xFFBDBDBD), CircleShape),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        item.userName,
                        fontSize = 11.sp,
                        color = DubbingHomePalette.subtitleGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DubbingHomePalette.titleBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    item.subtitle,
                    fontSize = 11.sp,
                    color = DubbingHomePalette.subtitleGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

// ── below-the-fold sections ──

@Composable
private fun DubbingHotRankSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "热度榜",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = DubbingHomePalette.titleBlack,
            modifier = Modifier.weight(1f),
        )
        Text(
            "查看全部 ›",
            fontSize = 12.sp,
            color = DubbingHomePalette.primaryGreen,
            modifier = Modifier.clickable {
                Utils.currentBridgeModule().openPage(PageNames.HotRankDetail)
            },
        )
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        listOf("1  穿梭在迷宫的勇士", "2  萌宠部落", "3  完美的世界").forEach { line ->
            Text(
                line,
                fontSize = 14.sp,
                color = DubbingHomePalette.titleBlack,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun DubbingGuessYouLikeList() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        GUESS_ITEMS.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(item.thumbColor, RoundedCornerShape(DubbingHomePalette.thumbRadius)),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("播放 ${item.playCount}", fontSize = 12.sp, color = DubbingHomePalette.subtitleGray)
                }
            }
        }
    }
}
