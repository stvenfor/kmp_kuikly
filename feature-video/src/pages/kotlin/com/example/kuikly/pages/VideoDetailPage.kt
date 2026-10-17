package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.video.ShortVideo
import com.example.kuikly.data.video.ShortVideoStatus
import com.example.kuikly.data.video.VideoComment
import com.example.kuikly.data.video.VideoLeaderboard
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KMetricPill
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
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
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("VideoDetail")
internal class VideoDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        setContent {
            val result = remember(id) { VideoStore.repo.fetchById(id) }
            val video = result.getOrNull()
            var playedSec by remember(id) { mutableStateOf(VideoStore.repo.playedSecOf(id)) }
            var partIndex by remember { mutableStateOf(0) }
            var descExpanded by remember { mutableStateOf(false) }
            var toastText by remember { mutableStateOf<String?>(null) }
            var liked by remember(id) { mutableStateOf(video?.isLiked ?: false) }
            var likeCount by remember(id) { mutableStateOf(video?.likeCount ?: 0) }
            var favorited by remember(id) { mutableStateOf(false) }
            var comments by remember(id) { mutableStateOf(VideoStore.repo.comments(id).getOrDefault(emptyList())) }
            val leaderboard = remember(id) { VideoStore.repo.leaderboard(id).getOrDefault(emptyList()) }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                VideoDetailNavBar(title = "视频详情")
                when {
                    id.isBlank() || video == null ->
                        VideoListErrorState(
                            message = "视频不存在或加载失败",
                            onRetry = { Utils.currentBridgeModule().closePage() },
                        )
                    else -> VideoDetailBody(
                        video = video,
                        playedSec = playedSec,
                        partIndex = partIndex,
                        descExpanded = descExpanded,
                        toastText = toastText,
                        liked = liked,
                        likeCount = likeCount,
                        favorited = favorited,
                        comments = comments,
                        leaderboard = leaderboard,
                        onSeek = { sec ->
                            VideoStore.repo.saveProgress(video.id, sec)
                            playedSec = VideoStore.repo.playedSecOf(video.id)
                        },
                        onPartSelected = { partIndex = it },
                        onToggleDesc = { descExpanded = !descExpanded },
                        onToast = { text ->
                            Utils.currentBridgeModule().toast(text)
                            toastText = text
                        },
                        onPlay = {
                            val data = JSONObject().apply { put("id", video.id) }
                            Utils.currentBridgeModule().openPage(url = "VideoPlay", userData = data)
                        },
                        onDubbing = {
                            val data = JSONObject().apply { put("id", video.id) }
                            Utils.currentBridgeModule().openPage(url = "VideoDubbing", userData = data)
                        },
                        onToggleLike = {
                            VideoStore.repo.toggleLike(video.id, !liked)
                                .onSuccess { up ->
                                    liked = up.isLiked
                                    likeCount = up.likeCount
                                    Utils.currentBridgeModule().toast(if (up.isLiked) "已点赞" else "已取消赞")
                                }
                        },
                        onToggleFavorite = {
                            VideoStore.repo.favorite(video.id)
                                .onSuccess { fav ->
                                    favorited = fav
                                    Utils.currentBridgeModule().toast(if (fav) "已收藏" else "已取消收藏")
                                }
                        },
                        onAddComment = { text ->
                            VideoStore.repo.addComment(video.id, text)
                                .onSuccess { c -> comments = comments + c }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoDetailNavBar(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹ 返回",
            fontSize = 16.sp,
            color = KuiklyTheme.link,
            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
        )
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VideoDetailBody(
    video: ShortVideo,
    playedSec: Int,
    partIndex: Int,
    descExpanded: Boolean,
    toastText: String?,
    liked: Boolean,
    likeCount: Int,
    favorited: Boolean,
    comments: List<VideoComment>,
    leaderboard: List<VideoLeaderboard>,
    onSeek: (Int) -> Unit,
    onPartSelected: (Int) -> Unit,
    onToggleDesc: () -> Unit,
    onToast: (String) -> Unit,
    onPlay: () -> Unit,
    onDubbing: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddComment: (String) -> Unit,
) {
    val total = video.durationSec
    val ratio = if (total > 0) playedSec.toDouble() / total.toDouble() else 0.0
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        item {
            VideoDetailCover(video = video, ratio = ratio, onPlay = onPlay)
            Spacer(Modifier.height(10.dp))
        }
        item {
            VideoDetailProgressBar(
                playedSec = playedSec,
                totalSec = total,
                ratio = ratio,
                onSeek = onSeek,
            )
            Spacer(Modifier.height(10.dp))
        }
        item {
            VideoDetailTitleCard(
                video = video,
                liked = liked,
                likeCount = likeCount,
                onToggleLike = onToggleLike,
            )
            Spacer(Modifier.height(10.dp))
        }
        item {
            KCard {
                Text("简介", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (descExpanded || video.desc.length <= 30) video.desc else video.desc.take(30) + "…",
                    fontSize = 13.sp,
                    color = KuiklyTheme.muted,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (descExpanded) "收起" else "展开",
                    fontSize = 12.sp,
                    color = KuiklyTheme.link,
                    modifier = Modifier.clickable { onToggleDesc() },
                )
            }
            Spacer(Modifier.height(10.dp))
        }
        item {
            KCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VideoCoverBlock(title = video.uploaderLabel, duration = "", size = 40)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("上传者 ${video.uploaderLabel}", fontSize = 14.sp, color = KuiklyTheme.ink)
                        Text(
                            "难度 ${video.difficulty} · ${video.sentenceCount} 句 · 状态 ${statusLabelOf(video.status)}",
                            fontSize = 12.sp,
                            color = KuiklyTheme.muted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        item {
            VideoDetailAlbumCard(video = video, partIndex = partIndex, onPartSelected = onPartSelected)
            Spacer(Modifier.height(10.dp))
        }
        item {
            VideoDetailLeaderboardCard(leaderboard = leaderboard)
            Spacer(Modifier.height(10.dp))
        }
        item {
            KCard {
                Text("字幕预览", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(video.subtitleEn, fontSize = 13.sp, color = KuiklyTheme.ink)
                Spacer(Modifier.height(4.dp))
                Text(video.subtitleZh, fontSize = 13.sp, color = KuiklyTheme.muted)
            }
            Spacer(Modifier.height(10.dp))
        }
        item {
            VideoDetailCommentsCard(comments = comments, onAddComment = onAddComment)
            Spacer(Modifier.height(10.dp))
        }
        item {
            VideoDetailBottomBar(
                favorited = favorited,
                onToast = onToast,
                onDubbing = onDubbing,
                onToggleFavorite = onToggleFavorite,
            )
            Spacer(Modifier.height(10.dp))
        }
        item {
            if (toastText != null) {
                Text("提示：$toastText", fontSize = 12.sp, color = KuiklyTheme.muted)
                Spacer(Modifier.height(8.dp))
            }
            VideoMockCycleRow(scenario = MockBackend.scenario) { MockBackend.cycle() }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VideoDetailCover(video: ShortVideo, ratio: Double, onPlay: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(200.dp)
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusLg)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("▶", fontSize = 32.sp, color = KuiklyTheme.onPrimary)
            Spacer(Modifier.height(6.dp))
            Text(video.title, fontSize = 14.sp, color = KuiklyTheme.onPrimary, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(
                "静态封面占位 · 已播 ${(ratio * 100).toInt()}% · ${video.duration}",
                fontSize = 11.sp,
                color = KuiklyTheme.onPrimary,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .background(Color(0x33000000), RoundedCornerShape(KuiklyTheme.radiusPill))
                .clickable { onPlay() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("▦ 全屏播放", fontSize = 12.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun VideoDetailLeaderboardCard(leaderboard: List<VideoLeaderboard>) {
    KCard {
        Text("点赞榜", fontSize = 15.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (leaderboard.isEmpty()) {
            Text("暂无榜单数据", fontSize = 13.sp, color = KuiklyTheme.muted)
        } else {
            leaderboard.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val medal = when (index) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "  " }
                    Text(medal, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.userName, fontSize = 13.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(2.dp))
                        Text("${entry.date} · ${entry.location}", fontSize = 11.sp, color = KuiklyTheme.muted)
                    }
                    Spacer(Modifier.width(8.dp))
                    KChip(entry.level, color = KuiklyTheme.violet)
                    Spacer(Modifier.width(8.dp))
                    Text("♥ ${compactLike(entry.likeCount)}", fontSize = 12.sp, color = KuiklyTheme.error)
                }
            }
        }
    }
}

private fun compactLike(n: Int): String = when {
    n >= 10000 -> "${"%.1f".format(n / 10000.0)}w"
    n >= 1000 -> "${"%.1f".format(n / 1000.0)}k"
    else -> "$n"
}

@Composable
private fun VideoDetailProgressBar(
    playedSec: Int,
    totalSec: Int,
    ratio: Double,
    onSeek: (Int) -> Unit,
) {
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VideoProgressTrack(progress = ratio, trackWidth = 150)
            Spacer(Modifier.width(10.dp))
            Text(
                "${formatVideoSec(playedSec)} / ${formatVideoSec(totalSec)}",
                fontSize = 12.sp,
                color = KuiklyTheme.muted,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            VideoSeekButton(text = "-15s") { onSeek(playedSec - 15) }
            Spacer(Modifier.width(8.dp))
            VideoSeekButton(text = "+15s") { onSeek(playedSec + 15) }
            Spacer(Modifier.width(8.dp))
            VideoSeekButton(text = "从头播") { onSeek(0) }
            Spacer(Modifier.width(8.dp))
            VideoSeekButton(text = "看完") { onSeek(totalSec) }
        }
    }
}

@Composable
private fun VideoSeekButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 12.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun VideoDetailTitleCard(video: ShortVideo, liked: Boolean, likeCount: Int, onToggleLike: () -> Unit) {
    KCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.width(230.dp)) {
                Text(video.title, fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KMetricPill("播放", video.viewLabel)
                    Spacer(Modifier.width(6.dp))
                    KMetricPill("点赞", video.likeLabel, accent = true)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("♥", fontSize = 18.sp, color = if (liked) KuiklyTheme.error else KuiklyTheme.muted,
                    modifier = Modifier.clickable { onToggleLike() })
                Text("$likeCount", fontSize = 11.sp, color = KuiklyTheme.muted)
                Spacer(Modifier.height(6.dp))
                Text("✕", fontSize = 15.sp, color = KuiklyTheme.muted)
                Text("${video.dislikeCount}", fontSize = 11.sp, color = KuiklyTheme.muted)
            }
        }
        Spacer(Modifier.height(10.dp))
        VideoDetailTags(tags = video.tags)
    }
}

@Composable
private fun VideoDetailTags(tags: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        tags.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { tag ->
                    KChip(
                        tag,
                        color = if (tag.startsWith("难度")) KuiklyTheme.primary else KuiklyTheme.muted,
                    )
                    Spacer(Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun VideoDetailAlbumCard(video: ShortVideo, partIndex: Int, onPartSelected: (Int) -> Unit) {
    KCard {
        Text(
            "视频专辑 (${video.albumCount})",
            fontSize = 15.sp,
            color = KuiklyTheme.ink,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(10.dp))
        if (video.albumParts.isEmpty()) {
            Text("暂无分集", fontSize = 13.sp, color = KuiklyTheme.muted)
        } else {
            video.albumParts.forEachIndexed { index, part ->
                val selected = index == partIndex
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                            RoundedCornerShape(KuiklyTheme.radiusSm),
                        )
                        .clickable { onPartSelected(index) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Part ${index + 1}", fontSize = 11.sp, color = KuiklyTheme.muted)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        part.title,
                        fontSize = 13.sp,
                        color = if (selected) KuiklyTheme.linkDeep else KuiklyTheme.ink,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.width(6.dp))
                    val partBadge = part.badge
                    if (partBadge != null) {
                        KChip(partBadge, color = KuiklyTheme.warning)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun VideoDetailCommentsCard(comments: List<VideoComment>, onAddComment: (String) -> Unit) {
    KCard {
        Text("评论 ${comments.size}", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        var draft by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                if (draft.isEmpty()) "说点什么…（点击短语追加）" else draft,
                fontSize = 13.sp,
                color = if (draft.isEmpty()) KuiklyTheme.muted else KuiklyTheme.ink,
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            COMMENT_CANDIDATES.forEach { cand ->
                KChip(cand, color = KuiklyTheme.link) {
                    if (draft.length < 50) draft = (draft + cand).take(50)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "清空",
                fontSize = 12.sp,
                color = KuiklyTheme.link,
                modifier = Modifier.clickable { draft = "" }.padding(horizontal = 6.dp, vertical = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clickable { if (draft.isNotBlank()) { onAddComment(draft); draft = "" } }
                    .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusPill))
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("发送", color = KuiklyTheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        if (comments.isEmpty()) {
            Text("还没有评论，快来抢沙发", fontSize = 12.sp, color = KuiklyTheme.muted)
        } else {
            comments.forEach { c ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(28.dp)
                            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Text(c.authorName.take(1), fontSize = 12.sp, color = KuiklyTheme.ink) }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(c.authorName, fontSize = 12.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(2.dp))
                        Text(c.content, fontSize = 13.sp, color = KuiklyTheme.ink)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun VideoDetailBottomBar(
    favorited: Boolean,
    onToast: (String) -> Unit,
    onDubbing: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        VideoBottomAction(label = if (favorited) "已收藏" else "收藏", active = favorited) { onToggleFavorite() }
        VideoBottomAction(label = "分享") { onToast("分享（开发中）") }
        Box(
            modifier = Modifier
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusPill))
                .clickable { onDubbing() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("开启配音", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VideoBottomAction(label: String, active: Boolean = false, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("✩", fontSize = 18.sp, color = if (active) KuiklyTheme.error else KuiklyTheme.ink)
        Text(label, fontSize = 11.sp, color = if (active) KuiklyTheme.error else KuiklyTheme.muted)
    }
}

private fun statusLabelOf(status: ShortVideoStatus): String = when (status) {
    ShortVideoStatus.published -> "已发布"
    ShortVideoStatus.reviewing -> "审核中"
    ShortVideoStatus.draft -> "草稿"
}

private fun formatVideoSec(sec: Int): String = "%02d:%02d".format(sec / 60, sec % 60)

private val COMMENT_CANDIDATES = listOf("发音标准", "继续加油", "喜欢这个片段", "学到了", "棒棒哒")
