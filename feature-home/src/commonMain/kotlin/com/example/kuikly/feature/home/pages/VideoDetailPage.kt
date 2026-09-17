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
import com.example.kuikly.data.video.VideoStore
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
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
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 配音视频详情 — Flutter `DubbingVideoDetailPage` 复刻（Phase-2 / P2-W2c）。
 * 真源：`features/video/lib/dubbing/view/dubbing_video_detail_page.dart`
 * （220 播放头 + 字幕叠层 / 简介·评论 TabBar / 点赞行 / tags / 上传者+打赏 / 视频专辑横滑 /
 * 点赞榜 / 底部收藏·分享·开启配音）。
 *
 * ponytail 天花板：Kuikly 无视频播放器、无 `Shadow`、无图片加载器 → 播放头/头像恒占位；
 * 弹层（配音设置 sheet）未落 → toast。
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
            var introTab by remember { mutableStateOf(true) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
            ) {
                val item = result.getOrNull()
                if (item == null) {
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
                    VideoHeader(item = item, topInset = top)
                    VideoTabs(
                        introSelected = introTab,
                        onSelect = { introTab = it },
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (introTab) {
                            IntroTab(item = item, bottomInset = bottom)
                        } else {
                            CommentsTab(bottomInset = bottom)
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

/** Flutter `_VideoHeader`：220 黑底（`Colors.black87`）+ 居中 `play_circle_fill`(64, white54) + 白返回 + 底部字幕三行。 */
@Composable
private fun VideoHeader(item: VideoItem, topInset: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xDD000000)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", fontSize = 48.sp, color = Color(0x8AFFFFFF))
        }
        Column(modifier = Modifier.fillMaxSize()) {
            // Flutter `Positioned(top: padding.top + 8, left: 8, IconButton(arrow_back_ios, white))`
            Spacer(Modifier.height((topInset + 8f).dp))
            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    "‹",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clickable { Utils.currentBridgeModule().closePage() }
                        .padding(8.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            // Flutter `Positioned(bottom: 40, left/right: 16)`
            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 40.dp)) {
                Text(
                    item.title,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.subtitleEn,
                    fontSize = 12.sp,
                    color = Color(0xB3FFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    item.subtitleZh,
                    fontSize = 11.sp,
                    color = Color(0x8AFFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Flutter `TabBar(labelColor: primaryGreen, indicatorColor: primaryGreen)`：简介 / 评论 22。 */
@Composable
private fun VideoTabs(
    introSelected: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().height(46.dp)) {
        VideoTab(Modifier.weight(1f), "简介", introSelected) { onSelect(true) }
        VideoTab(Modifier.weight(1f), "评论 22", !introSelected) { onSelect(false) }
    }
}

@Composable
private fun VideoTab(modifier: Modifier, label: String, selected: Boolean, onTap: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onTap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) VideoPalette.primaryGreen else VideoPalette.textGray,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(if (selected) 24.dp else 0.dp)
                .height(2.dp)
                .background(VideoPalette.primaryGreen),
        )
    }
}

/** Flutter `_IntroTab`（`ListView(padding: EdgeInsets.all(16))` 的等价物）。 */
@Composable
private fun IntroTab(item: VideoItem, bottomInset: Float) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = (bottomInset + 16f).dp,
        ),
    ) {
        item {
            // 点赞/点踩行（Flutter `Icon(thumb_up_outlined, 18)` + '3983' + 16 + `thumb_down_outlined`）
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👍", fontSize = 18.sp, color = VideoPalette.titleBlack)
                Spacer(Modifier.width(4.dp))
                Text("${item.likeCount}", fontSize = 14.sp, color = VideoPalette.titleBlack)
                Spacer(Modifier.width(16.dp))
                Text("👎", fontSize = 18.sp, color = VideoPalette.titleBlack)
            }
            Spacer(Modifier.height(8.dp))
            Row {
                item.tags.forEach { tag ->
                    VideoTagLarge(tag)
                    Spacer(Modifier.width(6.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            // 上传者行 + 打赏 pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFE8F8E8), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("趣", fontSize = 14.sp, color = VideoPalette.primaryGreen)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "上传者 ${item.author}",
                    fontSize = 13.sp,
                    color = VideoPalette.titleBlack,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .background(VideoPalette.tipBackground, RoundedCornerShape(16.dp))
                        .clickable { Utils.currentBridgeModule().toast("「打赏」即将接入") }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text("打赏", fontSize = 12.sp, color = Color(0xFFFF69B4))
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("视频专辑 (${item.albumCount})", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(
                    "+ 添加学习计划",
                    fontSize = 13.sp,
                    color = VideoPalette.primaryGreen,
                    modifier = Modifier.clickable { Utils.currentBridgeModule().toast("「添加学习计划」即将接入") },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            // Flutter `SizedBox(height: 100, child: ListView.builder(horizontal))`
            LazyRow(modifier = Modifier.height(100.dp)) {
                items(item.albumParts) { part ->
                    AlbumPartCard(part = part)
                    Spacer(Modifier.width(10.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("点赞榜", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            item.leaderboard?.let { leaderboard ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🥇", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE0E0E0), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(leaderboard.userName.take(1), fontSize = 14.sp, color = VideoPalette.textGray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            leaderboard.userName,
                            fontSize = 13.sp,
                            color = VideoPalette.titleBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${leaderboard.date} · ${leaderboard.location}",
                            fontSize = 11.sp,
                            color = VideoPalette.textGray,
                        )
                    }
                    Text("❤", fontSize = 16.sp, color = VideoPalette.likeRed)
                    Spacer(Modifier.width(4.dp))
                    Text(formatLikeCount(leaderboard.likeCount), fontSize = 12.sp, color = VideoPalette.textGray)
                }
            }
        }
    }
}

/** Flutter `_AlbumSection` 的 `Part` 卡：140 宽 / r8 / 1 边（#EEEEEE，选中 2 边绿）/ 10 内距 + 角标 + 标题 12。 */
@Composable
private fun AlbumPartCard(part: VideoAlbumPart) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .fillMaxHeight()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, VideoPalette.divider, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        part.badge?.let { badge ->
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

/** Flutter `_CommentsTab`（`ListView(padding: 16)` + 两条 `ListTile`）。 */
@Composable
private fun CommentsTab(bottomInset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = (bottomInset + 16f).dp),
    ) {
        CommentRow(initial = "A", name = "评论用户A", body = "配音很棒！")
        Spacer(Modifier.height(12.dp))
        CommentRow(initial = "B", name = "评论用户B", body = "发音标准，继续加油")
    }
}

@Composable
private fun CommentRow(initial: String, name: String, body: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, fontSize = 16.sp, color = VideoPalette.textGray)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(name, fontSize = 16.sp, color = VideoPalette.titleBlack)
            Spacer(Modifier.height(2.dp))
            Text(body, fontSize = 14.sp, color = VideoPalette.textGray)
        }
    }
}

/** Flutter `_DubbingBottomBar`：☆收藏 / ↗分享（图标 22 + 2 + 11 灰）+ 44 高 r22 绿 CTA。 */
@Composable
private fun VideoBottomBar(bottomInset: Float, onAction: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
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
