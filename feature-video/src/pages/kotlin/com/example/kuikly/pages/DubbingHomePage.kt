package com.example.kuikly.pages

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
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.Image
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
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page

/**
 * 配音首页 — Flutter `DubbingHomePage` 结构复刻（P8-E1b）。
 * 真源：`features/home/lib/home/view/dubbing_home_page.dart` + widgets/dubbing_home。
 *
 * ponytail：裸 `dp`/`sp`；PNG 图标/封面无加载器 → 色块 + 几何近似（同 W2c 口径）。
 */
/** dubbing 模块公共资源（`assets/common/dubbing/`），同 `homeCommonAsset` 机制。 */
@OptIn(InternalResourceApi::class)
private fun dubbingCommonAsset(name: String): DrawableResource =
    DrawableResource(ImageUri.commonAssets("dubbing/$name").toUrl(""))

/** Flutter `dubbing_home_mock_data` 的 `banner_main.png`（已从源工程搬运）。 */
private val DUBBING_BANNER_RES by lazy(LazyThreadSafetyMode.NONE) { dubbingCommonAsset("banner_main.png") }

@Page("DubbingHome")
internal class DubbingHomePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var selectedCategory by remember { mutableStateOf(1) }
            // Flutter 源 dubbing_home 全量 screenutil（designSize 375×812）→ designWidth 375
            ProvideDesignScale(pagerData.pageViewWidth, designWidth = 375f) {
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
    val cardRadius = 12f
    val thumbRadius = 8f
}

private data class RecentItem(val title: String, val duration: String, val thumbColor: Color, val cover: String)
private data class ExpertItem(
    val title: String,
    val userName: String,
    val subtitle: String,
    val thumbColor: Color,
    val cover: String,
)
private data class GuessItem(val title: String, val playCount: String, val thumbColor: Color, val cover: String)

private val RECENT_ITEMS = listOf(
    RecentItem("穿梭在迷宫的勇士", "03:24", Color(0xFFB3E5FC), "cover_05"),
    RecentItem("萌宠部落", "02:18", Color(0xFFC8E6C9), "cover_06"),
    RecentItem("完美的世界", "04:05", Color(0xFFFFE0B2), "cover_07"),
)

private val EXPERT_ITEMS = listOf(
    ExpertItem("英语启蒙课堂", "蓝儿老师Joyue", "跟读练习 · 初级", Color(0xFFE1BEE7), "cover_09"),
    ExpertItem("趣味配音挑战", "配音达人", "动画配音 · 中级", Color(0xFFBBDEFB), "cover_10"),
)

private val GUESS_ITEMS = listOf(
    GuessItem("趣味英语配音", "6.8万", Color(0xFFFFF3E0), "cover_09"),
    GuessItem("经典电影片段", "9.2万", Color(0xFFE8F5E9), "cover_10"),
)

private val FEATURE_LABELS = listOf("每日打卡", "影视单词", "经典剧场", "排行榜", "全部视频")

private enum class SectionTrailing { None, Chevron, Refresh }

// ── header ──

@Composable
private fun DubbingHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.su, vertical = 4.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹",
            fontSize = 22.susp,
            color = DubbingHomePalette.titleBlack,
            modifier = Modifier
                .clickable { Utils.currentBridgeModule().closePage() }
                .padding(12.su),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.su)
                .background(DubbingHomePalette.searchFieldBackground, RoundedCornerShape(18.su))
                .clickable { Utils.currentBridgeModule().openPage(PageNames.Search) }
                .padding(horizontal = 12.su),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.su)
                        .background(DubbingHomePalette.subtitleGray.copy(alpha = 0.35f), CircleShape),
                )
                Spacer(modifier = Modifier.width(8.su))
                Text("学英语", fontSize = 14.susp, color = DubbingHomePalette.subtitleGray)
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
            .size(44.su)
            .clickable { },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 20.susp, color = DubbingHomePalette.titleBlack)
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.su, end = 10.su)
                    .size(7.su)
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
            .padding(start = 16.su, end = 4.su, bottom = 4.su),
        verticalAlignment = Alignment.Top,
    ) {
        Row(modifier = Modifier.weight(1f)) {
            listOf("SVIP", "配音", "听力", "小剧场", "专题").forEachIndexed { index, label ->
                val active = index == selected
                Column(
                    modifier = Modifier
                        .clickable { onSelect(index) }
                        .padding(end = 24.su),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        label,
                        fontSize = 16.susp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            index == 0 -> DubbingHomePalette.svipGold
                            active -> DubbingHomePalette.primaryGreen
                            else -> DubbingHomePalette.textGray
                        },
                    )
                    Spacer(modifier = Modifier.height(8.su))
                    Box(
                        modifier = Modifier
                            .width(if (active) 24.su else 0.su)
                            .height(3.su)
                            .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(2.su)),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(44.su)
                .clickable { },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(28.su)
                    .background(DubbingHomePalette.primaryGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 8.su, height = 12.su)
                        .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(2.su)),
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
            .padding(horizontal = 16.su, vertical = 8.su),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.su)
                .clip(RoundedCornerShape(DubbingHomePalette.cardRadius.su)),
        ) {
            // Flutter `Image.asset(DubbingHomeAssets.path('banner_main.png'))` —— 搬运源资源
            Image(
                painter = painterResource(DUBBING_BANNER_RES),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.su)
                    .background(Color(0xFFFFC107).copy(alpha = 0.45f), RoundedCornerShape(8.su)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.su)) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.su, height = 10.su)
                                .background(Color(0xFFFFB300), RoundedCornerShape(topStart = 6.su, topEnd = 6.su)),
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 14.su, height = 10.su)
                                .background(Color(0xFFFFB300), RoundedCornerShape(topStart = 6.su, topEnd = 6.su)),
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 14.su, height = 10.su)
                                .background(Color(0xFFFFB300), RoundedCornerShape(topStart = 6.su, topEnd = 6.su)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(36.su)
                            .height(22.su)
                            .background(Color(0xFFFFB300), RoundedCornerShape(bottomStart = 4.su, bottomEnd = 4.su)),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.su)
                    .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(4.su))
                    .padding(horizontal = 6.su, vertical = 2.su),
            ) {
                Text("AD", fontSize = 10.susp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(10.su))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.su)
                        .width(if (index == activeIndex) 14.su else 6.su)
                        .height(6.su)
                        .background(
                            if (index == activeIndex) DubbingHomePalette.primaryGreen else DubbingHomePalette.divider,
                            RoundedCornerShape(3.su),
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
            .padding(start = 4.su, top = 4.su, end = 4.su, bottom = 12.su),
    ) {
        FEATURE_LABELS.forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FeatureIcon(index)
                Spacer(modifier = Modifier.height(8.su))
                Text(
                    label,
                    fontSize = 11.susp,
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
    Box(modifier = Modifier.size(52.su), contentAlignment = Alignment.Center) {
        when (index) {
            0 -> {
                Box(
                    modifier = Modifier
                        .size(44.su)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.su)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(22.su, 18.su)
                                .background(Color(0xFF66BB6A), RoundedCornerShape(3.su)),
                        )
                        Spacer(modifier = Modifier.height(2.su))
                        Box(
                            modifier = Modifier
                                .size(8.su)
                                .background(Color(0xFF43A047), CircleShape),
                        )
                    }
                }
            }
            1 -> {
                Box(
                    modifier = Modifier
                        .size(44.su)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(12.su)),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.su)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(width = 6.su, height = 18.su)
                                    .background(Color(0xFF42A5F5), RoundedCornerShape(2.su)),
                            )
                        }
                    }
                }
            }
            2 -> {
                Box(
                    modifier = Modifier
                        .size(44.su)
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.su)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.su)) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 8.su, height = 6.su)
                                        .background(Color(0xFFFFB300), RoundedCornerShape(2.su)),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(20.su)
                                .height(12.su)
                                .background(Color(0xFFFFA000), RoundedCornerShape(2.su)),
                        )
                    }
                }
            }
            3 -> {
                Box(
                    modifier = Modifier
                        .size(44.su)
                        .background(Color(0xFFF3E5F5), RoundedCornerShape(12.su)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.su)) {
                        listOf(
                            Triple(Color(0xFFFFC107), 22.su, 6.su),
                            Triple(Color(0xFFCFD8DC), 18.su, 5.su),
                            Triple(Color(0xFFFFCCBC), 14.su, 4.su),
                        ).forEach { (color, width, height) ->
                            Box(
                                modifier = Modifier
                                    .width(width)
                                    .height(height)
                                    .background(color, RoundedCornerShape(2.su)),
                            )
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(44.su)
                        .background(Color(0xFFECEFF1), RoundedCornerShape(12.su)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.su)
                            .background(Color(0xFF78909C), RoundedCornerShape(6.su)),
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.su, y = (-2).su)
                        .background(DubbingHomePalette.primaryGreen, RoundedCornerShape(4.su))
                        .padding(horizontal = 4.su, vertical = 1.su),
                ) {
                    Text("限免", fontSize = 8.susp, color = Color.White, fontWeight = FontWeight.SemiBold)
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
            .padding(start = 16.su, top = 24.su, end = 16.su, bottom = 12.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 18.susp,
            fontWeight = FontWeight.SemiBold,
            color = DubbingHomePalette.titleBlack,
            modifier = Modifier.weight(1f),
        )
        when (trailing) {
            SectionTrailing.Chevron -> {
                Text(
                    "›",
                    fontSize = 20.susp,
                    color = DubbingHomePalette.subtitleGray,
                    modifier = Modifier.clickable { }.padding(8.su),
                )
            }
            SectionTrailing.Refresh -> {
                Row(
                    modifier = Modifier.clickable { }.padding(horizontal = 4.su, vertical = 4.su),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("↻", fontSize = 14.susp, color = DubbingHomePalette.subtitleGray)
                    Spacer(modifier = Modifier.width(4.su))
                    Text("换一换", fontSize = 13.susp, color = DubbingHomePalette.subtitleGray)
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
    coverRes: DrawableResource? = null,
    duration: String? = null,
) {
    Box(
        modifier = width
            .height(height)
            .background(thumbColor, RoundedCornerShape(DubbingHomePalette.thumbRadius.su)),
    ) {
        if (coverRes != null) {
            Image(
                painter = painterResource(coverRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        if (duration != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.su, bottom = 6.su)
                    .background(Color(0x8C000000), RoundedCornerShape(4.su))
                    .padding(horizontal = 6.su, vertical = 2.su),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("×", fontSize = 10.susp, color = Color.White)
                Spacer(modifier = Modifier.width(3.su))
                Text(duration, fontSize = 10.susp, color = Color.White)
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
            .padding(horizontal = 16.su),
        horizontalArrangement = Arrangement.spacedBy(10.su),
    ) {
        RECENT_ITEMS.forEach { item ->
            Column(modifier = Modifier.width(120.su)) {
                DubbingCoverImage(
                    thumbColor = item.thumbColor,
                    width = Modifier.fillMaxWidth(),
                    height = 68.su,
                    coverRes = dubbingCommonAsset(item.cover),
                    duration = item.duration,
                )
                Spacer(modifier = Modifier.height(8.su))
                Text(
                    item.title,
                    fontSize = 12.susp,
                    color = DubbingHomePalette.titleBlack,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.susp,
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
            .padding(horizontal = 16.su),
        horizontalArrangement = Arrangement.spacedBy(12.su),
    ) {
        EXPERT_ITEMS.forEach { item ->
            Column(modifier = Modifier.weight(1f)) {
                DubbingCoverImage(
                    thumbColor = item.thumbColor,
                    width = Modifier.fillMaxWidth(),
                    height = 96.su,
                    coverRes = dubbingCommonAsset(item.cover),
                    duration = "02:30",
                )
                Spacer(modifier = Modifier.height(8.su))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.su)
                            .clip(CircleShape)
                            .background(Color(0xFFBDBDBD)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(dubbingCommonAsset("thumb_default")),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Spacer(modifier = Modifier.width(6.su))
                    Text(
                        item.userName,
                        fontSize = 11.susp,
                        color = DubbingHomePalette.subtitleGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(4.su))
                Text(
                    item.title,
                    fontSize = 13.susp,
                    fontWeight = FontWeight.Medium,
                    color = DubbingHomePalette.titleBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.su))
                Text(
                    item.subtitle,
                    fontSize = 11.susp,
                    color = DubbingHomePalette.subtitleGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.su))
}

// ── below-the-fold sections ──

@Composable
private fun DubbingHotRankSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.su, vertical = 8.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "热度榜",
            fontSize = 18.susp,
            fontWeight = FontWeight.SemiBold,
            color = DubbingHomePalette.titleBlack,
            modifier = Modifier.weight(1f),
        )
        Text(
            "查看全部 ›",
            fontSize = 12.susp,
            color = DubbingHomePalette.primaryGreen,
            modifier = Modifier.clickable {
                Utils.currentBridgeModule().openPage(PageNames.HotRankDetail)
            },
        )
    }
    Column(modifier = Modifier.padding(horizontal = 16.su)) {
        listOf("1  穿梭在迷宫的勇士", "2  萌宠部落", "3  完美的世界").forEach { line ->
            Text(
                line,
                fontSize = 14.susp,
                color = DubbingHomePalette.titleBlack,
                modifier = Modifier.padding(vertical = 8.su),
            )
        }
    }
}

@Composable
private fun DubbingGuessYouLikeList() {
    Column(modifier = Modifier.padding(horizontal = 16.su)) {
        GUESS_ITEMS.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.su),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.su)
                        .background(item.thumbColor, RoundedCornerShape(DubbingHomePalette.thumbRadius.su)),
                ) {
                    Image(
                        painter = painterResource(dubbingCommonAsset(item.cover)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Spacer(modifier = Modifier.width(12.su))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontSize = 14.susp, fontWeight = FontWeight.Medium)
                    Text("播放 ${item.playCount}", fontSize = 12.susp, color = DubbingHomePalette.subtitleGray)
                }
            }
        }
    }
}
