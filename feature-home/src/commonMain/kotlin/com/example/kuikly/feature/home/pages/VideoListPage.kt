package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.video.VideoItem
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
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
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * `DubbingVideoListPage` / `DubbingVideoDetailPage` 令牌镜像
 * （`features/video/lib/dubbing/`）。
 *
 * **刻度**：Flutter video 模块的 **dubbing** 页（本轮真源）全用**裸逻辑 px**；
 * 仅 `video/lib/short_video/` 使用 `.w/.h`（不在本 ticket 范围）。
 * 故一律裸 `dp`/`sp`，**不要** `.su()`。
 */
internal object VideoPalette {
    val primaryGreen = Color(0xFF52C41A)
    val background = Color(0xFFF5F5F5)
    val textGray = Color(0xFF8C8C8C)
    val textGrayLight = Color(0xFFBFBFBF)
    val titleBlack = Color(0xFF1A1A1A)
    val divider = Color(0xFFEEEEEE)
    val coverPlaceholder = Color(0xFFEEEEEE)
    val likeRed = Color(0xFFE85D5D)
    val orange = Color(0xFFFF8A34)
    val tipBackground = Color(0xFFFFE4EC)

    /** Flutter `_VideoCard` `BoxShadow(color: black.withValues(alpha: 0.04))`。 */
    val cardShadow = Color(0x0A000000)
}

/**
 * 配音视频列表 — Flutter `DubbingVideoListPage` 复刻（Phase-2 / P2-W2c）。
 * 真源：`features/video/lib/dubbing/view/dubbing_video_list_page.dart`
 * （`AppNavBar('视频列表')` + `ListView.separated(padding 16, sep 12)` + `_VideoCard`）。
 */
@Page(name = "VideoList", moduleId = "feature_home")
internal class VideoListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var items by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var loaded by remember { mutableStateOf(false) }

            fun load() {
                VideoStore.repo.list()
                    .onSuccess {
                        items = it
                        error = null
                    }
                    .onFailure {
                        items = emptyList()
                        error = it.message
                    }
            }

            if (!loaded) {
                loaded = true
                load()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VideoPalette.background),
            ) {
                AppNavBarBar(
                    title = "视频列表",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = VideoPalette.background,
                    foreground = VideoPalette.titleBlack,
                )
                Spacer(Modifier.height(8.dp))
                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "加载失败",
                            message = error ?: "加载失败",
                            actionLabel = "重试",
                            onAction = { load() },
                        )
                    }
                    items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "🎬",
                            title = "暂无视频",
                            message = "点击刷新重新加载",
                            actionLabel = "刷新",
                            onAction = { load() },
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = (bottom + 16f).dp,
                        ),
                        // Flutter `separatorBuilder: SizedBox(height: 12)` — 只在条目之间，末条后无间隔。
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            VideoCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Flutter `_VideoCard`：白卡 r12（+ black 4% / blur 8 / offset(0,2) 阴影）+ 120×90 封面（左圆角）+ 12 内距列
 * （标题 14·w600 / 4 间隙 chip 组（h6·v2·r4，绿 12% 或 `#F5F5F5`，10sp）/ 8 / 点赞行 14+4+12 + › 18）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoCard(item: VideoItem) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `_VideoCard` BoxShadow: black 4% / blur 8 / offset(0, 2)
            .shadow(8.dp, shape, ambientColor = VideoPalette.cardShadow, spotColor = VideoPalette.cardShadow)
            .background(Color.White, shape)
            .clickable {
                Utils.currentBridgeModule().openPage(
                    PageNames.VideoDetail,
                    userData = JSONObject().apply { put("id", item.id) },
                )
            },
    ) {
        // Flutter `ClipRRect(horizontal-left 12)` + Image errorBuilder 占位（无图片加载器 → 恒占位）。
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(90.dp)
                .background(
                    VideoPalette.coverPlaceholder,
                    RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomEnd = 0.dp, bottomStart = 12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Flutter `Icon(Icons.movie, color: textGray)`（size 默认 24）。
            Text("🎬", fontSize = 24.sp, color = VideoPalette.textGray)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
        ) {
            Text(
                item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = VideoPalette.titleBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            // Flutter `Wrap(spacing: 4, runSpacing: 4, children: item.tags.take(3))`
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item.tags.take(3).forEach { tag -> VideoTag(tag) }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👍", fontSize = 14.sp, color = VideoPalette.textGray)
                Spacer(Modifier.width(4.dp))
                Text("${item.likeCount}", fontSize = 12.sp, color = VideoPalette.textGray)
                Spacer(Modifier.weight(1f))
                Text("›", fontSize = 18.sp, color = VideoPalette.textGrayLight)
            }
        }
    }
}

/** Flutter `_VideoCard` 的 tag chip：`难度*` / `合作` → 绿 12% 底 + 绿字，其余 `#F5F5F5` + 灰字。 */
@Composable
internal fun VideoTag(tag: String) {
    val highlight = tag.startsWith("难度") || tag == "合作"
    Box(
        modifier = Modifier
            .background(
                if (highlight) VideoPalette.primaryGreen.copy(alpha = 0.12f) else VideoPalette.background,
                RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            tag,
            fontSize = 10.sp,
            color = if (highlight) VideoPalette.primaryGreen else VideoPalette.textGray,
            maxLines = 1,
        )
    }
}

/** 详情页 tag chip：Flutter `_TagsSection`（h8·v4·r4，11sp，`合作/难度/漫威/经典大片/超级英雄` 绿）。 */
@Composable
internal fun VideoTagLarge(tag: String) {
    val highlight = tag == "合作" || tag.startsWith("难度") || tag == "漫威" ||
        tag == "经典大片" || tag == "超级英雄"
    Box(
        modifier = Modifier
            .background(
                if (highlight) VideoPalette.primaryGreen.copy(alpha = 0.12f) else VideoPalette.background,
                RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            tag,
            fontSize = 11.sp,
            color = if (highlight) VideoPalette.primaryGreen else VideoPalette.textGray,
            maxLines = 1,
        )
    }
}
