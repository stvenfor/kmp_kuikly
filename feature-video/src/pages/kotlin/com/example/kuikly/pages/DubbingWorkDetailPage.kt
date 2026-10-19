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
import com.example.kuikly.data.dubbing.DubbingWorkItem
import com.example.kuikly.data.dubbing.DubbingWorkStore
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

import com.tencent.kuikly.core.annotations.Page

/**
 * 配音作品详情 — Flutter `DubbingWorkDetailPage` 复刻。
 * 真源：`features/video/lib/dubbing/view/dubbing_work_detail_page.dart`
 * （`PlayableVideoHeader` + Tab(简介/评论) + 作者/标题/更多作品 + 底部赞/分享/开启配音）。
 *
 * **刻度**：Flutter video dubbing 页裸逻辑 px → 裸 `dp`/`sp`。
 *
 * ponytail 天花板：无播放器 SDK / 无图片加载器 → 16:9 黑底占位 + 色块头像。
 */
@Page("DubbingWorkDetail")
internal class DubbingWorkDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "work_1" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val result = remember(id) { DubbingWorkStore.repo.detail(id) }
            var tab by remember { mutableStateOf(0) }
            val allWorks = remember { DubbingWorkStore.repo.list().getOrDefault(emptyList()) }

            ProvideDesignScale(pagerData.pageViewWidth, designWidth = 480f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VideoPalette.background),
            ) {
                val work = result.getOrNull()
                if (work == null) {
                    AppNavBarBar(
                        title = "作品详情",
                        topInset = top,
                        onBack = { Utils.currentBridgeModule().closePage() },
                        background = VideoPalette.background,
                        foreground = VideoPalette.titleBlack,
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "作品不存在",
                            message = result.exceptionOrNull()?.message ?: "加载失败",
                            actionLabel = "返回",
                            onAction = { Utils.currentBridgeModule().closePage() },
                        )
                    }
                } else {
                    WorkVideoHeader(topInset = top, onBack = { Utils.currentBridgeModule().closePage() })
                    WorkDetailTabs(
                        selected = tab,
                        commentCount = work.commentCount,
                        onSelect = { tab = it },
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (tab) {
                            0 -> WorkIntroTab(
                                item = work,
                                moreWorks = allWorks.filter { it.id != work.id }.take(2),
                            )
                            else -> WorkCommentsTab()
                        }
                    }
                    WorkDetailBottomBar(
                        likeCount = work.likeCount,
                        bottomInset = bottom,
                        onStartDubbing = { Utils.currentBridgeModule().toast("开启配音（开发中）") },
                    )
                }
            }
            }
        }
    }
}

/** Flutter `PlayableVideoHeader`：16:9 黑底 + 左上返回。 */
@Composable
private fun WorkVideoHeader(topInset: Float, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 参考图实测黑头部 953/1440 = 0.662 × 屏宽（非 16:9）
            .aspectRatio(1440f / 953f)
            .background(Color.Black),
    ) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            Text("▶", fontSize = 44.susp, color = Color(0x8AFFFFFF))
        }
        Text(
            "‹",
            fontSize = 22.susp,
            color = Color.White,
            modifier = Modifier
                .padding(start = 4.su, top = (topInset + 4f).dp)
                .clickable(onClick = onBack)
                .padding(8.su),
        )
    }
}

@Composable
private fun WorkDetailTabs(selected: Int, commentCount: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        WorkDetailTab(
            modifier = Modifier.weight(1f),
            label = "简介",
            selected = selected == 0,
            onClick = { onSelect(0) },
        )
        WorkDetailTab(
            modifier = Modifier.weight(1f),
            label = "评论 $commentCount",
            selected = selected == 1,
            onClick = { onSelect(1) },
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.su)
            .background(VideoPalette.divider),
    )
}

@Composable
private fun WorkDetailTab(modifier: Modifier, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.su),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            fontSize = 15.susp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) VideoPalette.primaryGreen else VideoPalette.textGray,
        )
        Spacer(Modifier.height(8.su))
        Box(
            modifier = Modifier
                .width(if (selected) 24.su else 0.su)
                .height(3.su)
                .background(VideoPalette.primaryGreen, RoundedCornerShape(2.su)),
        )
    }
}

@Composable
private fun WorkIntroTab(item: DubbingWorkItem, moreWorks: List<DubbingWorkItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.su),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.su)
                        .background(VideoPalette.divider, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👤", fontSize = 18.susp)
                }
                Spacer(Modifier.width(10.su))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.authorName,
                        fontSize = 15.susp,
                        fontWeight = FontWeight.SemiBold,
                        color = VideoPalette.titleBlack,
                    )
                    Text(
                        "${item.location} · 粉丝1.5K",
                        fontSize = 12.susp,
                        color = VideoPalette.textGray,
                    )
                }
                Box(
                    modifier = Modifier
                        .border(1.su, VideoPalette.primaryGreen, RoundedCornerShape(4.su))
                        .padding(horizontal = 12.su, vertical = 6.su),
                ) {
                    Text("已关注", fontSize = 12.susp, color = VideoPalette.primaryGreen)
                }
            }
        }
        item { Spacer(Modifier.height(16.su)) }
        item {
            Row {
                item.badge?.let { badge ->
                    Box(
                        modifier = Modifier
                            .padding(end = 6.su, top = 2.su)
                            .background(Color(0x1FE91E63), RoundedCornerShape(4.su))
                            .padding(horizontal = 6.su, vertical = 2.su),
                    ) {
                        Text(badge, fontSize = 10.susp, color = Color(0xFFE91E63))
                    }
                }
                Text(
                    item.title,
                    fontSize = 15.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = VideoPalette.titleBlack,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item { Spacer(Modifier.height(8.su)) }
        item {
            Text(
                "${item.publishedAt} 12:09",
                fontSize = 12.susp,
                color = VideoPalette.textGray,
            )
        }
        item { Spacer(Modifier.height(16.su)) }
        item {
            Text(
                "99+ 人也配了这个视频",
                fontSize = 13.susp,
                color = VideoPalette.textGray,
            )
        }
        if (moreWorks.isNotEmpty()) {
            item { Spacer(Modifier.height(20.su)) }
            item {
                Text(
                    "Ta的更多作品",
                    fontSize = 15.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = VideoPalette.titleBlack,
                )
            }
            item { Spacer(Modifier.height(12.su)) }
            items(moreWorks, key = { it.id }) { work ->
                MoreWorkRow(work = work)
                Spacer(Modifier.height(12.su))
            }
        }
    }
}

@Composable
private fun MoreWorkRow(work: DubbingWorkItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.su))
            .padding(8.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            work.badge?.let { badge ->
                Text(badge, fontSize = 10.susp, color = VideoPalette.orange)
            }
            Text(
                work.title,
                fontSize = 13.susp,
                color = VideoPalette.titleBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.su))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👍", fontSize = 12.susp, color = VideoPalette.textGray)
                Text(" ${work.likeCount}", fontSize = 11.susp, color = VideoPalette.textGray)
            }
        }
    }
}

@Composable
private fun WorkCommentsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.su),
    ) {
        item {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.su)
                        .background(VideoPalette.divider, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("B", fontSize = 14.susp, color = VideoPalette.titleBlack)
                }
                Spacer(Modifier.width(12.su))
                Column {
                    Text("乌克丽丽", fontSize = 14.susp, fontWeight = FontWeight.SemiBold)
                    Text("发音标准，继续加油", fontSize = 13.susp, color = VideoPalette.textGray)
                }
            }
        }
    }
}

/** Flutter `_BottomBar`：赞 + 分享 + 绿 CTA「开启配音」。 */
@Composable
private fun WorkDetailBottomBar(likeCount: Int, bottomInset: Float, onStartDubbing: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                start = 16.su,
                top = 10.su,
                end = 16.su,
                bottom = (bottomInset + 10f).dp,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.su)
                .background(VideoPalette.divider),
        )
        Spacer(Modifier.height(10.su))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("👍", fontSize = 22.susp, color = VideoPalette.titleBlack)
            Spacer(Modifier.width(4.su))
            Text("$likeCount", fontSize = 12.susp, color = VideoPalette.titleBlack)
            Spacer(Modifier.width(16.su))
            Text("⇪", fontSize = 22.susp, color = VideoPalette.titleBlack)
            Spacer(Modifier.width(4.su))
            Text("分享", fontSize = 12.susp, color = VideoPalette.titleBlack)
            Spacer(Modifier.width(16.su))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.su)
                    .background(VideoPalette.primaryGreen, RoundedCornerShape(24.su))
                    .clickable(onClick = onStartDubbing),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "开启配音",
                    fontSize = 15.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}

private fun <T> Result<T>.getOrDefault(default: T): T = getOrNull() ?: default
