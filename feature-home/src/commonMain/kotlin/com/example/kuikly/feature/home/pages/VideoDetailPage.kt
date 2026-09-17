package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.video.VideoAlbumPart
import com.example.kuikly.data.video.VideoItem
import com.example.kuikly.data.video.VideoLeaderboard
import com.example.kuikly.data.video.VideoStore
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
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.itemsIndexed
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 配音视频详情 — Flutter `DubbingVideoDetailPage` 复刻（Phase-2 / P2-W2c，P2-V5d 视觉对齐）。
 *
 * 真源：`features/video/lib/dubbing/view/dubbing_video_detail_page.dart`
 * （`PlayableVideoHeader`(16:9 黑底 + 右上水印 + 底部居中双字幕) →
 *  `ListView(padding bottom 8)`：标题+赞踩 / tags / 可展开简介 / 1px 分隔 / 上传者+打赏 /
 *  1px 分隔 / 视频专辑(横滑 148×108·可选段) / 8px 粗分隔 / 点赞榜 → 底部收藏·分享·开启配音）。
 *
 * **刻度**：Flutter video **dubbing** 页全用**裸逻辑 px**（同 `VideoListPage` 结论）→ 全程裸 `dp`/`sp`，
 * **不要** `.su()`。
 *
 * ponytail 天花板（Kuikly 无对应能力，非本 ticket 可解）：
 * - 无视频播放器 SDK → 16:9 黑底恒为占位播放头（Flutter 为真实视频帧 + `AppVideoControlsBar`）。
 * - 无图片加载器 → 所有头像恒色块占位（Flutter `NetworkImage`）。
 * - 无图标字体 → `thumb_up` / `keyboard_arrow_*` / `card_giftcard` / `chevron_right` 以字形近似，
 *   与兄弟页 `VideoListPage` 同口径。
 * - 无 `Shadow` 文字阴影 → 字幕无 `Colors.black54` 描边。
 * - 无 `BoxShadow(offset)` → 底栏顶部阴影以 1px `#EEEEEE` 边替代。
 * - 模型无 `latestWorkAvatars`（`VideoRepository.kt`）→ 「最新作品」整段缺失，8px 粗分隔保留在点赞榜前。
 */
@Page(name = "VideoDetail", moduleId = "feature_home")
internal class VideoDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "1" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val result = remember(id) { VideoStore.repo.detail(id) }
            var descExpanded by remember { mutableStateOf(false) }
            var selectedPart by remember { mutableStateOf(0) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
            ) {
                val video = result.getOrNull()
                if (video == null) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "加载失败",
                            message = result.exceptionOrNull()?.message ?: "加载失败",
                            actionLabel = "返回",
                            onAction = { Utils.currentBridgeModule().closePage() },
                        )
                    }
                } else {
                    VideoHeader(
                        subtitleEn = video.subtitleEn,
                        subtitleZh = video.subtitleZh,
                        topInset = top,
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        // Flutter `ListView(padding: EdgeInsets.only(bottom: 8))` —— 各段落自带内距。
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        item { VideoTitleSection(item = video) }
                        item { VideoTagsSection(tags = video.tags) }
                        item {
                            VideoDescriptionSection(
                                desc = video.summary,
                                expanded = descExpanded,
                                onToggle = { descExpanded = !descExpanded },
                            )
                        }
                        item { HairlineDivider() }
                        item { VideoUploaderSection(author = video.author) }
                        item { HairlineDivider() }
                        item {
                            VideoAlbumSection(
                                item = video,
                                selectedIndex = selectedPart,
                                onSelect = { selectedPart = it },
                            )
                        }
                        item { ThickSectionGap() }
                        video.leaderboard?.let { entry ->
                            item { VideoLeaderboardSection(entry = entry) }
                        }
                    }
                    VideoBottomBar(
                        bottomInset = bottom,
                        onAction = { Utils.currentBridgeModule().toast("「$it」即将接入") },
                    )
                }
            }
        }
    }
}

/**
 * Flutter `PlayableVideoHeader`：`height = width * 9 / 16` 黑底播放区，
 * 右上水印胶囊（top+8 / right 12）、左上白返回（top+4 / left 4）、底部居中双字幕（bottom 48）。
 */
@Composable
private fun VideoHeader(subtitleEn: String, subtitleZh: String, topInset: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
    ) {
        // 无播放器 SDK：Flutter 此处为 `AppVideoPlayer.view` + `AppVideoControlsBar`，Kuikly 恒占位。
        Box(modifier = Modifier.align(Alignment.Center)) {
            Text("▶", fontSize = 44.sp, color = Color(0x8AFFFFFF))
        }
        // Flutter `Positioned(top: AppSafeInsets.top + 8, right: 12)` 水印：黑 35% / r4 / h8·v4。
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = (topInset + 8f).dp, end = 12.dp)
                .background(Color(0x59000000), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text("英语趣配音", fontSize = 10.sp, color = Color.White)
            Text("FUN DUBBING", fontSize = 8.sp, color = Color(0xB3FFFFFF))
        }
        // Flutter `Positioned(top: AppSafeInsets.top + 4, left: 4, IconButton(arrow_back_ios, white))`。
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = (topInset + 4f).dp)
                .clickable { Utils.currentBridgeModule().closePage() }
                .padding(8.dp),
        ) {
            Text("‹", fontSize = 20.sp, color = Color.White)
        }
        // Flutter `Positioned(left: 16, right: 16, bottom: 48)`：en 13 white / 2 / zh 12 white70，居中。
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                subtitleEn,
                fontSize = 13.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitleZh,
                fontSize = 12.sp,
                color = Color(0xB3FFFFFF),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Flutter `_TitleSection`：`fromLTRB(16,14,16,0)` —— 标题 17·w600·height1.35 + 右侧赞/数/踩列。 */
@Composable
private fun VideoTitleSection(item: VideoItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 14.dp, end = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            item.title,
            modifier = Modifier.weight(1f),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = VideoPalette.titleBlack,
            // Flutter `height: 1.35` × 17 = 22.95 → 23sp
            lineHeight = 23.sp,
        )
        Spacer(Modifier.width(12.dp))
        // Flutter `Icons.thumb_up`(#E85D5D, 22) / 计数 12 灰 / 8 / `thumb_down_outlined`(灰, 20)。
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👍", fontSize = 22.sp, color = VideoPalette.likeRed)
            Text("${item.likeCount}", fontSize = 12.sp, color = VideoPalette.textGray)
            Spacer(Modifier.height(8.dp))
            Text("👎", fontSize = 20.sp, color = VideoPalette.textGray)
        }
    }
}

/** Flutter `_TagsSection`：`fromLTRB(16,10,16,0)` + `Wrap(spacing 6, runSpacing 6)`。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoTagsSection(tags: List<String>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 10.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag -> VideoTagLarge(tag) }
    }
}

/** Flutter `_DescriptionSection`：`fromLTRB(16,10,16,12)`，点整行切换 1 行 ⇄ 全展开 + 18 箭头。 */
@Composable
private fun VideoDescriptionSection(desc: String, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            desc,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = VideoPalette.textGray,
            maxLines = if (expanded) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            // Flutter `Icons.keyboard_arrow_up / keyboard_arrow_down`（18）。
            if (expanded) "⌃" else "⌄",
            fontSize = 18.sp,
            color = VideoPalette.textGray,
        )
    }
}

/**
 * Flutter `_UploaderSection`：`symmetric(h 16, v 12)` —— `CircleAvatar(radius 18, #E8F8E8, '趣' 14 绿)`
 * + 10 + '上传者 x'(14) + 打赏 pill（`#FFE4EC` / r18 / h14·v6 / 礼盒 14 pink + 4 + '打赏' 13 pink）。
 */
@Composable
private fun VideoUploaderSection(author: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFE8F8E8), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("趣", fontSize = 14.sp, color = VideoPalette.primaryGreen)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "上传者 $author",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = VideoPalette.titleBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier
                .background(VideoPalette.tipBackground, RoundedCornerShape(18.dp))
                .clickable { Utils.currentBridgeModule().toast("「打赏」即将接入") }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🎁", fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            // Flutter `Colors.pink` = #FFE91E63（非 hotpink）。
            Text("打赏", fontSize = 13.sp, color = Color(0xFFE91E63))
        }
    }
}

/**
 * Flutter `_AlbumSection`：`fromLTRB(16,12,16,16)` ——
 * 头部（'视频专辑 (n)' 15·w600 + `chevron_right` 18 灰 + Spacer + '+ 添加学习计划' 13 绿）
 * + 12 + `SizedBox(height: 108, ListView.builder(horizontal))`。
 */
@Composable
private fun VideoAlbumSection(
    item: VideoItem,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "视频专辑 (${item.albumCount})",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = VideoPalette.titleBlack,
            )
            Text("›", fontSize = 18.sp, color = VideoPalette.textGray)
            Spacer(Modifier.weight(1f))
            Text(
                "+ 添加学习计划",
                fontSize = 13.sp,
                color = VideoPalette.primaryGreen,
                modifier = Modifier.clickable { Utils.currentBridgeModule().toast("「添加学习计划」即将接入") },
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            modifier = Modifier.height(108.dp),
            // Flutter `margin: EdgeInsets.only(right: 10)` —— 只在卡片之间。
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(item.albumParts) { index, part ->
                AlbumPartCard(
                    part = part,
                    index = index,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

/**
 * Flutter `_AlbumSection` 的 `Part` 卡：148 宽 / r8 / 1 边（`#EEEEEE`，选中 2 边绿）/ 10 内距 +
 * 首行 `Part N`(11 灰) + 角标 + `Spacer` + 标题 13·w500（2 行）。
 */
@Composable
private fun AlbumPartCard(
    part: VideoAlbumPart,
    index: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .width(148.dp)
            .fillMaxHeight()
            .background(Color.White, shape)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) VideoPalette.primaryGreen else VideoPalette.divider,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Part ${index + 1}", fontSize = 11.sp, color = VideoPalette.textGray)
            Spacer(Modifier.weight(1f))
            part.badge?.let { badge -> AlbumBadge(badge) }
        }
        Spacer(Modifier.weight(1f))
        Text(
            part.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = VideoPalette.titleBlack,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Flutter 角标：`试听` → 绿 12% + 绿字，其余 → `#FFF3E0` + `#FF8A34`；h6·v2·r4·10sp。 */
@Composable
private fun AlbumBadge(badge: String) {
    val trial = badge == "试听"
    Box(
        modifier = Modifier
            .background(
                if (trial) VideoPalette.primaryGreen.copy(alpha = 0.12f) else Color(0xFFFFF3E0),
                RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            badge,
            fontSize = 10.sp,
            color = if (trial) VideoPalette.primaryGreen else VideoPalette.orange,
        )
    }
}

/**
 * Flutter `_LeaderboardSection`：`fromLTRB(16,12,16,20)` —— '点赞榜' 15·w600 + 12 +
 * 🥇22 / 8 / 头像(44，右下 -4,-2 挂 `#FFD54F` 等级角标 9·w600) / 10 /
 * 名字 14·w500 + 'date · location' 12 灰 / ❤18 #E85D5D / 4 / 计数 13 灰。
 */
@Composable
private fun VideoLeaderboardSection(entry: VideoLeaderboard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 20.dp),
    ) {
        Text("点赞榜", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = VideoPalette.titleBlack)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🥇", fontSize = 22.sp)
            Spacer(Modifier.width(8.dp))
            // Flutter `Stack(clipBehavior: Clip.none)` + `Positioned(right: -4, bottom: -2)`。
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(entry.userName.take(1), fontSize = 16.sp, color = VideoPalette.textGray)
                }
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 2.dp)
                        .background(Color(0xFFFFD54F), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text(entry.level, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.userName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = VideoPalette.titleBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${entry.date} · ${entry.location}",
                    fontSize = 12.sp,
                    color = VideoPalette.textGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("❤", fontSize = 18.sp, color = VideoPalette.likeRed)
            Spacer(Modifier.width(4.dp))
            Text(formatLikeCount(entry.likeCount), fontSize = 13.sp, color = VideoPalette.textGray)
        }
    }
}

/** Flutter `Divider(height: 1, color: Color(0xFFEEEEEE))` —— 通栏 1px。 */
@Composable
private fun HairlineDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(VideoPalette.divider),
    )
}

/** Flutter `Divider(height: 12, thickness: 8, color: Color(0xFFF5F5F5))` —— 12 高内居中 8px 粗线。 */
@Composable
private fun ThickSectionGap() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(VideoPalette.background),
        )
    }
}

/**
 * Flutter `_DubbingBottomBar`：白底 + 顶部 1px `#EEEEEE` 边（`BoxShadow(0,-2,blur 8)` 不可复现 → 天花板）
 * + `fromLTRB(16,10,16,10+bottomInset)`；☆收藏 / ↗分享（22 + 2 + 11 灰）+ 44 高 r22 绿 CTA。
 */
@Composable
private fun VideoBottomBar(bottomInset: Float, onAction: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        HairlineDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = (bottomInset + 10f).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomAction(icon = "☆", label = "收藏", onClick = { onAction("收藏") })
            Spacer(Modifier.width(20.dp))
            BottomAction(icon = "↗", label = "分享", onClick = { onAction("分享") })
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(VideoPalette.primaryGreen, RoundedCornerShape(22.dp))
                    .clickable { onAction("开启配音") },
                contentAlignment = Alignment.Center,
            ) {
                Text("开启配音", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun BottomAction(icon: String, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 22.sp, color = VideoPalette.titleBlack)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = VideoPalette.textGray)
    }
}

/** Flutter `likeCount >= 10000 ? '${(n / 10000).toStringAsFixed(1)}万' : '$n'`。 */
private fun formatLikeCount(count: Int): String =
    if (count >= 10000) "${(count / 1000) / 10f}万" else "$count"
