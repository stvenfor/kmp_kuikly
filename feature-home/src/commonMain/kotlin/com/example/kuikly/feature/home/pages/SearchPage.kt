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
import com.example.kuikly.data.search.FakeSearchRepository
import com.example.kuikly.data.search.SearchRankItem
import com.example.kuikly.data.search.SearchRankTab
import com.example.kuikly.data.search.SearchStore
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.ExperimentalLayoutApi
import com.tencent.kuikly.compose.foundation.layout.FlowRow
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
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.itemsIndexed
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.foundation.text.BasicTextField
import com.tencent.kuikly.compose.material3.HorizontalDivider
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/** `SearchPageTheme`（module_home iOS 极简令牌）镜像。 */
private object SearchPalette {
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val fillSecondary = Color(0xFFE9E9EB)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val labelTertiary = Color(0x4D3C3C43)
    val separator = Color(0xFFC6C6C8)
    val rankGold = Color(0xFFFF9500)
    val rankSilver = Color(0xFF8E8E93)
    val rankBronze = Color(0xFFCD7F32)

    /** accent.withValues(alpha: 0.1) */
    val accentSoft = Color(0x1A007AFF)
}

/**
 * SearchPage — Migration Source `search_page.dart` 复刻（Phase-2 / P2-W2a）。
 *
 * - 真源：`module_home/home/view/search_page.dart` + `view/widgets/search_*.dart`。
 * - Flutter 源多数尺寸走 `.w/.h/.sp`（screenutil 375），故页面主体接 `ProvideDesignScale`
 *   用 `su`/`susp`；`search_rank_tab_bar` / `search_page` 的列表卡片边距是裸字面量 → 裸 dp。
 * - 无图片加载器：榜单封面渲染为 Flutter 未加载 errorWidget 的近似
 *   （fillSecondary 圆角块 + tertiary ▶）。
 * - 轮播占位词（SearchRotatingKeyword）为动态行为，静态结构用输入框 hint 替代。
 */
@Page(name = "Search", moduleId = "feature_home")
internal class SearchPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            ProvideDesignScale(pagerData.pageViewWidth) {
                val repo = SearchStore.repo
                var selectedTab by remember { mutableStateOf(SearchRankTab.HOT_DUBBING) }
                val rankItems = repo.rankListForTab(selectedTab)
                var keyword by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SearchPalette.background)
                        .padding(top = top.dp, bottom = bottom.dp),
                ) {
                    // SearchHeaderBar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.su, top = 8.su, end = 16.su, bottom = 12.su),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.su)
                                .clickable { Utils.currentBridgeModule().closePage() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("‹", fontSize = 24.susp, color = SearchPalette.accent)
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.su)
                                .background(SearchPalette.surface, RoundedCornerShape(12.su))
                                .border(0.5.dp, SearchPalette.separator, RoundedCornerShape(12.su))
                                .padding(horizontal = 14.su),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("⌕", fontSize = 18.susp, color = SearchPalette.labelSecondary)
                            Spacer(Modifier.width(8.su))
                            Box(modifier = Modifier.weight(1f)) {
                                BasicTextField(
                                    value = keyword,
                                    onValueChange = { keyword = it },
                                    textStyle = TextStyle(fontSize = 16.susp, color = SearchPalette.labelPrimary),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                if (keyword.isEmpty()) {
                                    Text(
                                        FakeSearchRepository.SEARCH_PLACEHOLDER,
                                        fontSize = 15.susp,
                                        color = SearchPalette.labelTertiary,
                                    )
                                }
                            }
                            if (keyword.isNotEmpty()) {
                                Text(
                                    "⊗",
                                    fontSize = 18.susp,
                                    color = SearchPalette.labelTertiary,
                                    modifier = Modifier.clickable { keyword = "" },
                                )
                            } else {
                                Text(
                                    "◉",
                                    fontSize = 20.susp,
                                    color = SearchPalette.accent,
                                    modifier = Modifier.padding(4.su),
                                )
                            }
                        }
                        Spacer(Modifier.width(8.su))
                        Text(
                            "取消",
                            fontSize = 16.susp,
                            color = SearchPalette.accent,
                            modifier = Modifier
                                .clickable { Utils.currentBridgeModule().closePage() }
                                .padding(horizontal = 4.su),
                        )
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // SearchHistorySection
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.su, end = 16.su, bottom = 16.su),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "搜索历史",
                                        fontSize = 17.susp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SearchPalette.labelPrimary,
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Row(
                                        modifier = Modifier.clickable { },
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("⌫", fontSize = 16.susp, color = SearchPalette.labelSecondary)
                                        Spacer(Modifier.width(4.su))
                                        Text("清除", fontSize = 13.susp, color = SearchPalette.labelSecondary)
                                    }
                                }
                                Spacer(Modifier.height(12.su))
                                TagFlow(tags = repo.history(), highlightFirst = false, card = false)
                            }
                        }
                        // SearchDiscoverySection
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.su, end = 16.su, bottom = 16.su),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SearchPalette.surface, RoundedCornerShape(12.dp))
                                        .border(0.5.dp, SearchPalette.separator, RoundedCornerShape(12.dp))
                                        .padding(16.su),
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "搜索发现",
                                                fontSize = 17.susp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SearchPalette.labelPrimary,
                                            )
                                            Spacer(Modifier.width(6.su))
                                            Text(
                                                "↻",
                                                fontSize = 18.susp,
                                                color = SearchPalette.accent,
                                                modifier = Modifier.clickable { },
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Text("换一换", fontSize = 13.susp, color = SearchPalette.labelSecondary)
                                        }
                                        Spacer(Modifier.height(12.su))
                                        TagFlow(tags = repo.discovery(), highlightFirst = true, card = false)
                                    }
                                }
                            }
                        }
                        // SearchFilterSection
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.su, end = 16.su, bottom = 16.su),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "快捷筛选",
                                        fontSize = 17.susp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SearchPalette.labelPrimary,
                                    )
                                    Spacer(Modifier.width(6.su))
                                    Text("☰", fontSize = 18.susp, color = SearchPalette.accent)
                                }
                                Spacer(Modifier.height(12.su))
                                TagFlow(tags = repo.filterTags(), highlightFirst = false, card = false)
                            }
                        }
                        // SearchRankTabBar（Flutter 裸字面量 → 裸 dp）
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = 8.dp,
                                ),
                            ) {
                                itemsIndexed(repo.rankTabs()) { index, tab ->
                                    val active = tab == selectedTab
                                    Column(
                                        modifier = Modifier
                                            .clickable { selectedTab = tab }
                                            .padding(end = if (index < repo.rankTabs().size - 1) 20.dp else 0.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            tab.label,
                                            fontSize = 15.sp,
                                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (active) SearchPalette.accent else SearchPalette.labelSecondary,
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(if (active) 20.dp else 0.dp)
                                                .height(2.dp)
                                                .background(SearchPalette.accent),
                                        )
                                    }
                                }
                            }
                        }
                        // 榜单卡片（search_page.dart 裸 EdgeInsets(16,12,16,0) → 裸 dp）
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 12.dp, end = 16.dp)
                                    .background(SearchPalette.surface, RoundedCornerShape(12.dp))
                                    .border(0.5.dp, SearchPalette.separator, RoundedCornerShape(12.dp)),
                            ) {
                                rankItems.forEachIndexed { index, item ->
                                    RankRow(item)
                                    if (index < rankItems.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 52.su, end = 16.su),
                                            thickness = 0.5.dp,
                                            color = SearchPalette.separator,
                                        )
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

/** `SearchTagChip` 复刻：14sp 文案、20 圆角、highlight 时 accent 10% 底 + accent 文案。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagFlow(tags: List<String>, highlightFirst: Boolean, card: Boolean) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.su),
        verticalArrangement = Arrangement.spacedBy(8.su),
    ) {
        tags.forEachIndexed { index, tag ->
            val highlight = highlightFirst && index == 0
            Text(
                tag,
                fontSize = 14.susp,
                fontWeight = if (highlight) FontWeight.Medium else FontWeight.Normal,
                color = if (highlight) SearchPalette.accent else SearchPalette.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(
                        if (highlight) SearchPalette.accentSoft else SearchPalette.fillSecondary,
                        RoundedCornerShape(20.su),
                    )
                    .clickable { }
                    .padding(horizontal = 14.su, vertical = 8.su),
            )
        }
    }
}

/** `SearchRankListItem` 复刻：序号（金银铜）+ 封面占位 + 标题/副标题 + chevron。 */
@Composable
private fun RankRow(item: SearchRankItem) {
    val rankColor = when (item.rank) {
        1 -> SearchPalette.rankGold
        2 -> SearchPalette.rankSilver
        3 -> SearchPalette.rankBronze
        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.su, vertical = 14.su),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            item.rank.toString(),
            fontSize = if (rankColor != null) 16.susp else 15.susp,
            fontWeight = if (rankColor != null) FontWeight.Bold else FontWeight.Medium,
            color = rankColor ?: SearchPalette.labelSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.su),
        )
        Spacer(Modifier.width(12.su))
        // CacheImageUtils 未加载 errorWidget 近似
        Box(
            modifier = Modifier
                .size(72.su)
                .background(SearchPalette.fillSecondary, RoundedCornerShape(10.su)),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", fontSize = 28.susp, color = SearchPalette.labelTertiary)
        }
        Spacer(Modifier.width(12.su))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                fontSize = 16.susp,
                fontWeight = FontWeight.SemiBold,
                color = SearchPalette.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.su))
            Text(
                item.subtitle,
                fontSize = 13.susp,
                lineHeight = 18.2.susp,
                color = SearchPalette.labelSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text("›", fontSize = 20.susp, color = SearchPalette.labelTertiary)
    }
}
