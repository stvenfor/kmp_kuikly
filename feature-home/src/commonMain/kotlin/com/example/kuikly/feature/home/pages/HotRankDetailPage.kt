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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 「热搜榜详情」— Flutter `HotRankDetailPage`（`features/home/lib/home/view/hot_rank_detail_page.dart`）
 * 复刻（Phase-2 / P2-W3）。
 *
 * 真源：`hot_rank_detail_page.dart` + `widgets/hot_rank/{hot_rank_sidebar,hot_rank_list_item,
 * hot_rank_age_filter}.dart` + `mock/hot_rank_detail_mock_data.dart` + `theme/dubbing_home_theme.dart`。
 *
 * **刻度**：Flutter 本页四文件命中 `.w/.h/.sp`（29 / 20 / 18 / 12 处）→ `ProvideDesignScale` +
 * `.su` / `.susp`。
 *
 * ponytail 天花板（不弹回）：
 * - `assets/dubbing_home` 下的 PNG（麦穗 ×4、TOP20 徽章、分享/封面/名次图）无图片加载器 →
 *   统一 Unicode 字形 + `DubbingHomeTheme` 真值色（同 W2c 口径）。
 * - 点条目 Flutter 跳 `dubbingVideoDetail`（Kuikly 未落地）→ toast，不新增模块/路由。
 */
@Page(name = "HotRankDetail", moduleId = "feature_home")
internal class HotRankDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        setContent {
            ProvideDesignScale(pagerData.pageViewWidth) {
                var category by remember { mutableStateOf(HOT_RANK_CATEGORIES.indexOf("热搜榜")) }
                var ageFilter by remember { mutableStateOf(2) }
                var menuOpen by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DubbingPalette.background)
                        // Flutter `GestureDetector(onTap: closeAgeFilterMenu,
                        // HitTestBehavior.translucent)` 让点空白处也能关掉 age 下拉；
                        // 原 Kuikly 只能再点 pill 来关，与 Flutter UX 不齐。
                        .clickable { if (menuOpen) menuOpen = false },
                ) {
                    HotRankHeader(topInset = top)
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        HotRankSidebar(
                            selected = category,
                            onSelect = {
                                category = it
                                menuOpen = false
                            },
                        )
                        // Flutter `DecoratedBox(bg white, only(topLeft: 12.r))`。
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(
                                    DubbingPalette.background,
                                    RoundedCornerShape(topStart = 12.su),
                                ),
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.su, top = 10.su, end = 12.su, bottom = 4.su),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    AgeFilterBar(
                                        label = AGE_FILTERS[ageFilter],
                                        open = menuOpen,
                                        onToggle = { menuOpen = !menuOpen },
                                        onSelect = {
                                            ageFilter = it
                                            menuOpen = false
                                        },
                                    )
                                }
                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentPadding = PaddingValues(bottom = 24.su),
                                ) {
                                    items(HOT_RANK_ITEM_COUNT) { index ->
                                        if (index > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.su)
                                                    .height(1.dp)
                                                    .background(DubbingPalette.divider),
                                            )
                                        }
                                        HotRankListItem(index = index)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── 令牌 / 数据 ─────────────────────────

private object DubbingPalette {
    val background = Color(0xFFFFFFFF)
    val primaryGreen = Color(0xFF45D1A1)
    val titleBlack = Color(0xFF1A1A1A)
    val textGray = Color(0xFF666666)
    val subtitleGray = Color(0xFF999999)
    val divider = Color(0xFFEEEEEE)
    val svipGold = Color(0xFFD4A017)
    val hotRankHeaderPink = Color(0xFFFFF0F5)
    val hotRankSidebarBg = Color(0xFFF7F8FA)
    val hotRankSidebarActive = Color(0xFFFFFFFF)
    val hotRankRankGold = Color(0xFFFFC107)
    val hotRankRankSilver = Color(0xFFCFD8DC)
    val hotRankRankBronze = Color(0xFFFFCCBC)
    val hotRankRankDefault = Color(0xFFBDBDBD)

    /** Flutter `dubbing_home_theme.dart` `hotRankDropdownShadow = Color(0x1A000000)`。 */
    val hotRankDropdownShadow = Color(0x1A000000)
}

private val HOT_RANK_CATEGORIES = listOf("热读榜", "新书榜", "童话榜", "热搜榜", "科普榜", "高分榜")
private val AGE_FILTERS = listOf("1-2岁", "3岁到大班", "1-3年级", "4年级以上")

private const val HOT_RANK_ITEM_COUNT = 20
private val RANKS = listOf(1, 2, 3, 88, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
private val TITLES = listOf(
    "穿条纹睡衣的...", "蛮荒故事", "爱冒险的朵拉", "道奇小狗",
    "你好，小朋友", "完美的世界", "萌宠部落", "穿梭在迷宫的勇士",
)
private val SUBTITLES = listOf(
    "某日布鲁诺决定...", "一种近似父子的不寻常感情", "开启你的奇幻冒险之旅",
    "跟佩奇一起快乐学英语", "经典动画配音练习", "趣味英语启蒙课堂",
)
private val HEATS = listOf(39274, 28390, 22007, 19874, 18560, 16230, 14890, 13540)

// ───────────────────────── header ─────────────────────────

/** Flutter `_HotRankHeader`：粉→白渐变 + 返回/分享 + 麦穗标题 + 副标题。 */
@Composable
private fun HotRankHeader(topInset: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                // Flutter 真源 `LinearGradient(begin: topCenter, end: bottomCenter,
                // colors: [pink, white], stops: [0.0, 1.0])`：渐变占满 header 整高，
                // 底部 = 100% white。原 `endY = 1200f` 是绝对像素端点，对 ~144.su 的
                // 短 header 渐变只跑到 12% (`y/H = 144/1200`)，底部色 = 88% 粉 + 12%
                // 白 ≈ #FFF2F6（Flutter 端是 #FFFFFF）。改用默认 `endY = Float
                // .POSITIVE_INFINITY`（= bottom of drawing area），让 `Brush.vertical
                // Gradient` 与 Flutter `LinearGradient(topCenter→bottomCenter)`
                // 形制对齐。其他页面（DealInvoice / PayList / MusicNowPlaying）
                // 全部走默认，无 `endY` 显式值 — 本页系漏改。
                Brush.verticalGradient(
                    listOf(DubbingPalette.hotRankHeaderPink, Color.White),
                ),
            )
            // `statusBarInset()` 已是设备逻辑像素（见 `BaseComposePager.statusBarInset`
            // + `DesignScale.kt:26` 「状态栏...不要 `.su()`」）；原 `(topInset + 4f).su` 让状态
            // 栏区域被设计稿系数再乘一次（×~1.46 on Pixel_7_Pro），title 整体被多推 ~24dp。
            .padding(start = 8.su, top = (topInset + 4f).dp, end = 8.su, bottom = 16.su),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.su)
                    .clickable { Utils.currentBridgeModule().closePage() },
                contentAlignment = Alignment.Center,
            ) {
                Text("‹", fontSize = 20.susp, color = DubbingPalette.titleBlack)
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(44.su)
                    .clickable { Utils.currentBridgeModule().toast("「分享」即将接入") },
                contentAlignment = Alignment.Center,
            ) {
                Text("↗", fontSize = 22.susp, color = DubbingPalette.titleBlack)
            }
        }
        Spacer(Modifier.height(4.su))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Flutter `Image.asset(wheat_large_left/right.png, 28×36)`。
            Text("🌾", fontSize = 26.susp)
            Spacer(Modifier.width(8.su))
            Text(
                "热搜榜",
                fontSize = 22.susp,
                fontWeight = FontWeight.Bold,
                color = DubbingPalette.titleBlack,
            )
            Spacer(Modifier.width(8.su))
            Text("🌾", fontSize = 26.susp)
        }
        Spacer(Modifier.height(6.su))
        Text(
            "趣配音用户近期热搜内容",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.susp,
            color = DubbingPalette.subtitleGray,
            textAlign = TextAlign.Center,
        )
    }
}

// ───────────────────────── sidebar ─────────────────────────

/** Flutter `HotRankSidebar`：88 宽 #F7F8FA 列表，选中项白底 + 金麦穗。 */
@Composable
private fun HotRankSidebar(selected: Int, onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .width(88.su)
            .fillMaxSize()
            .background(DubbingPalette.hotRankSidebarBg),
    ) {
        Spacer(Modifier.height(8.su))
        HOT_RANK_CATEGORIES.forEachIndexed { index, label ->
            val active = index == selected
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.su, vertical = 2.su)
                    .background(
                        if (active) DubbingPalette.hotRankSidebarActive else Color.Transparent,
                        if (active) {
                            RoundedCornerShape(topStart = 8.su, bottomStart = 8.su)
                        } else {
                            RoundedCornerShape(8.su)
                        },
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 4.su, vertical = 14.su),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (active && label == "热搜榜") {
                    // Flutter `Image.asset(badge_top20.png, 36×16)`：原版用 `Stack +
                    // Positioned(top: -18.h)` 让徽章悬浮在 item 之上（天花板：PNG 无图片加载器，
                    // 徽章落在 item 内顶部，统一 Unicode 字形）。
                    Box(
                        modifier = Modifier
                            .background(DubbingPalette.svipGold, RoundedCornerShape(3.su))
                            .padding(horizontal = 4.su, vertical = 1.su),
                    ) {
                        Text(
                            "TOP20",
                            fontSize = 8.susp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.height(2.su))
                }
                if (active) {
                    Row {
                        Text("❮", fontSize = 10.susp, color = DubbingPalette.svipGold)
                        Spacer(Modifier.width(2.su))
                        Text("❯", fontSize = 10.susp, color = DubbingPalette.svipGold)
                    }
                    Spacer(Modifier.height(4.su))
                }
                Text(
                    label,
                    fontSize = 13.susp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) DubbingPalette.titleBlack else DubbingPalette.textGray,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
        // Flutter `ListView.builder(padding: EdgeInsets.only(top: 8.h, bottom: 24.h))`：补足
        // 底距让最末一项（"高分榜"）不贴底边；loudest 之一（与下拉阴影、右对齐并列为 V8d 三件套）。
        Spacer(Modifier.height(24.su))
    }
}

// ───────────────────────── age filter ─────────────────────────

/** Flutter `HotRankAgeFilterBar`：14 圆角 pill（12 文案 + ⌄）+ 36 下方 120 宽下拉。 */
@Composable
private fun AgeFilterBar(
    label: String,
    open: Boolean,
    onToggle: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    Box {
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(14.su))
                .border(1.su, DubbingPalette.divider, RoundedCornerShape(14.su))
                .clickable(onClick = onToggle)
                .padding(horizontal = 10.su, vertical = 6.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 12.susp, color = DubbingPalette.titleBlack)
            Spacer(Modifier.width(4.su))
            Text(
                if (open) "⌃" else "⌄",
                fontSize = 16.susp,
                color = DubbingPalette.subtitleGray,
            )
        }
        if (open) {
            // Flutter `Material(elevation: 8, r10, width: 120)` 定位在 pill 下方 36、右对齐
            // （`Positioned(top: 36.h, right: 0, ...)`）。Kuikly 用 `.align(TopEnd)` + 同色
            // 阴影还原：原版未加 `align` 会让下拉从 pill 左缘向右铺出，超出页面右沿 (loudest)。
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 36.su)
                    .width(120.su)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(10.su),
                        ambientColor = DubbingPalette.hotRankDropdownShadow,
                        spotColor = DubbingPalette.hotRankDropdownShadow,
                    )
                    .background(Color.White, RoundedCornerShape(10.su)),
            ) {
                AGE_FILTERS.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DubbingPalette.divider),
                        )
                    }
                    Text(
                        item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(horizontal = 12.su, vertical = 12.su),
                        fontSize = 13.susp,
                        color = DubbingPalette.titleBlack,
                        fontWeight = if (item == label) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ───────────────────────── list item ─────────────────────────

/** Flutter `HotRankListItem`：56 封面 r8 + 18 名次块 r4 + 标题/副标题/热度。 */
@Composable
private fun HotRankListItem(index: Int) {
    val rank = RANKS[index]
    val rankBg = when (rank) {
        1 -> DubbingPalette.hotRankRankGold
        2 -> DubbingPalette.hotRankRankSilver
        3 -> DubbingPalette.hotRankRankBronze
        else -> DubbingPalette.hotRankRankDefault
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Utils.currentBridgeModule().toast("「${TITLES[index % TITLES.size]}」详情即将接入") }
            .padding(start = 12.su, top = 10.su, end = 12.su, bottom = 10.su),
        verticalAlignment = Alignment.Top,
    ) {
        // Flutter `Image.asset(cover, 56×56, r8)` 的占位块。
        Box(
            modifier = Modifier
                .size(56.su)
                .background(DubbingPalette.hotRankSidebarBg, RoundedCornerShape(8.su)),
            contentAlignment = Alignment.Center,
        ) {
            Text("♪", fontSize = 22.susp, color = DubbingPalette.subtitleGray)
        }
        Spacer(Modifier.width(8.su))
        Box(
            modifier = Modifier
                .size(18.su)
                .background(rankBg, RoundedCornerShape(4.su)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                rank.toString(),
                fontSize = 11.susp,
                fontWeight = FontWeight.Bold,
                // Flutter 真源 `TextStyle(height: 1)` 把 lineHeight 收紧到
                // fontSize（=11.susp），让数字在 18×18 box 中竖直居中（line
                // 高度 ≈11sp → glyph 距 box 上下各 ≈3.5sp）。原 Kuikly 不设
                // `lineHeight`，Compose 默认 lineHeight ≈ 1.2–1.4× fontSize
                // (13–14sp)，导致 glyph 在 18.su box 内上下偏移 ~1sp（Pixel_7
                // _Pro dpr 2.625 上 ≈ 2.6dp 视觉差）。
                lineHeight = 11.susp,
                color = if (rank <= 3) Color.White else DubbingPalette.textGray,
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(8.su))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                TITLES[index % TITLES.size],
                fontSize = 14.susp,
                fontWeight = FontWeight.SemiBold,
                color = DubbingPalette.titleBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.su))
            Text(
                SUBTITLES[index % SUBTITLES.size],
                fontSize = 12.susp,
                color = DubbingPalette.subtitleGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.su))
            Text(
                "热度${HEATS[index % HEATS.size]}",
                fontSize = 11.susp,
                color = DubbingPalette.subtitleGray,
            )
        }
    }
}
