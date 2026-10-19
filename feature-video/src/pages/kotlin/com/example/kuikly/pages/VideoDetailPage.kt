package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.video.ShortVideo
import com.example.kuikly.data.video.VideoAlbumPart
import com.example.kuikly.data.video.VideoComment
import com.example.kuikly.data.video.VideoLeaderboard
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
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

/**
 * 视频详情页（port of source DubbingVideoDetailPage）。
 *
 * 视觉对齐 Flutter 参考图 18-flutter-video-detail：顶部黑色视频头（返回 / 品牌 / 双语字幕），
 * 其下为白底内容区——标题 + 点赞列、绿色标签 chips、简介、上传者行（打赏）、
 * 视频专辑（横向分集卡）、最新作品、点赞榜，底部固定操作栏（收藏 / 分享 / 开启配音）。
 * 播放进度、字幕预览、评论等功能区块保留在列表下方（参考图可视区之外）。
 */
@Page("VideoDetail")
internal class VideoDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        setContent {
            // 深链 id 归一：路由可能不带 id，或携带 Flutter 侧短 id（"1" 即恐龙预告，
            // 对应 KMP 数据 v4 —— 18-flutter-video-detail 参考图同款数据）。
            val video = remember(id) {
                val resolved = if (id.isBlank() || id == "1") "v4" else id
                VideoStore.repo.fetchById(resolved).getOrNull()
            }
            val videoId = video?.id ?: id
            var playedSec by remember(videoId) { mutableStateOf(VideoStore.repo.playedSecOf(videoId)) }
            var partIndex by remember { mutableStateOf(0) }
            var descExpanded by remember { mutableStateOf(false) }
            var toastText by remember { mutableStateOf<String?>(null) }
            var liked by remember(videoId) { mutableStateOf(video?.isLiked ?: false) }
            var likeCount by remember(videoId) { mutableStateOf(video?.likeCount ?: 0) }
            var favorited by remember { mutableStateOf(false) }
            var comments by remember(videoId) {
                mutableStateOf(VideoStore.repo.comments(videoId).getOrDefault(emptyList()))
            }
            val leaderboard = remember(videoId) {
                VideoStore.repo.leaderboard(videoId).getOrDefault(emptyList())
            }

            Column(modifier = Modifier.fillMaxSize().background(VideoPageBg)) {
                if (video == null) {
                    VideoListErrorState(
                        message = "视频不存在或加载失败",
                        onRetry = { Utils.currentBridgeModule().closePage() },
                    )
                } else {
                    VideoDetailHeader(video = video, onBack = {
                        Utils.currentBridgeModule().closePage()
                    }, onPlay = {
                        val data = JSONObject().apply { put("id", video.id) }
                        Utils.currentBridgeModule().openPage(url = "VideoPlay", userData = data)
                    })
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        item {
                            VideoDetailTitleRow(
                                video = video,
                                likeCount = likeCount,
                                onToggleLike = {
                                    VideoStore.repo.toggleLike(video.id, !liked)
                                        .onSuccess { up ->
                                            liked = up.isLiked
                                            likeCount = up.likeCount
                                            Utils.currentBridgeModule()
                                                .toast(if (up.isLiked) "已点赞" else "已取消赞")
                                        }
                                },
                            )
                        }
                        item {
                            VideoDetailTagsRow(
                                tags = video.tags,
                                expanded = descExpanded,
                                onToggleDesc = { descExpanded = !descExpanded },
                            )
                        }
                        item {
                            VideoDetailDescRow(
                                video = video,
                                expanded = descExpanded,
                                onToggleDesc = { descExpanded = !descExpanded },
                            )
                        }
                        item {
                            VideoDetailUploaderRow(
                                video = video,
                                onToast = { text ->
                                    Utils.currentBridgeModule().toast(text)
                                    toastText = text
                                },
                            )
                        }
                        item {
                            VideoDetailAlbumSection(
                                video = video,
                                partIndex = partIndex,
                                onPartSelected = { partIndex = it },
                                onToast = { text ->
                                    Utils.currentBridgeModule().toast(text)
                                    toastText = text
                                },
                            )
                        }
                        item {
                            VideoDetailLatestSection()
                        }
                        item {
                            VideoDetailLeaderboardCard(leaderboard = leaderboard)
                        }
                        item {
                            // 以下为功能区块（参考图可视区之外，滚动可见）。
                            VideoDetailProgressBar(
                                playedSec = playedSec,
                                totalSec = video.durationSec,
                                ratio = if (video.durationSec > 0) {
                                    playedSec.toDouble() / video.durationSec.toDouble()
                                } else {
                                    0.0
                                },
                                onSeek = { sec ->
                                    VideoStore.repo.saveProgress(video.id, sec)
                                    playedSec = VideoStore.repo.playedSecOf(video.id)
                                },
                            )
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
                            VideoDetailCommentsCard(comments = comments, onAddComment = { text ->
                                VideoStore.repo.addComment(video.id, text)
                                    .onSuccess { c -> comments = comments + c }
                            })
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
                    VideoDetailBottomBar(
                        favorited = favorited,
                        onToast = { text ->
                            Utils.currentBridgeModule().toast(text)
                            toastText = text
                        },
                        onDubbing = {
                            val data = JSONObject().apply { put("id", video.id) }
                            Utils.currentBridgeModule().openPage(url = "VideoDubbing", userData = data)
                        },
                        onToggleFavorite = {
                            VideoStore.repo.favorite(video.id)
                                .onSuccess { fav ->
                                    favorited = fav
                                    Utils.currentBridgeModule().toast(if (fav) "已收藏" else "已取消收藏")
                                }
                        },
                    )
                }
            }
        }
    }
}

// ── 配色（port of source 的 Ant 绿 #52C41A 主色） ──
private val VideoPageBg = Color(0xFFFFFFFF)
private val VideoInk = Color(0xFF1A1A1A)
private val VideoMuted = Color(0xFF999999)
private val VideoAccent = Color(0xFF52C41A)
private val VideoAccentSoft = Color(0xFFF0F9EC)
private val VideoChipGrayBg = Color(0xFFF5F5F5)
private val VideoChipGrayText = Color(0xFF999999)
private val VideoCardBorder = Color(0xFFEEEEEE)
private val VideoChevron = Color(0xFFCCCCCC)
private val VideoAvatarBg = Color(0xFFEAF8E4)
private val VideoAvatarBlue = Color(0xFFD8E2FF)
private val VideoLevelBadge = Color(0xFFFAAD14)
private val VideoHeart = Color(0xFFFF4D4F)
private val VideoBadgePaidBg = Color(0xFFFFF7E6)
private val VideoBadgePaidText = Color(0xFFFA8C16)

/** 顶部黑色视频头：返回箭头、品牌名、播放态指示与双语字幕。 */
@Composable
private fun VideoDetailHeader(video: ShortVideo, onBack: () -> Unit, onPlay: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            // 参考图实测黑头部 953px/3.5dpr ≈ 272dp（非 16:9）
            .height(272.dp)
            .background(Color.Black)
            .clickable { onPlay() },
    ) {
        Text(
            "‹",
            fontSize = 28.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
                .clickable { onBack() }
                .padding(start = 10.dp, top = 36.dp, end = 12.dp, bottom = 12.dp),
        )
        Column(
            modifier = Modifier.align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 14.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text("英语趣配音", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("FUN DUBBING", fontSize = 10.sp, color = VideoMuted)
        }
        Box(
            modifier = Modifier.align(Alignment.Center)
                .width(5.dp)
                .height(26.dp)
                .background(Color.White, RoundedCornerShape(2.5.dp)),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(video.subtitleEn, fontSize = 14.sp, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(video.subtitleZh, fontSize = 12.sp, color = Color(0xFFAAAAAA))
        }
    }
}

/** 标题行：左侧标题（最多两行），右侧点赞 / 点踩列。 */
@Composable
private fun VideoDetailTitleRow(
    video: ShortVideo,
    likeCount: Int,
    onToggleLike: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            video.title,
            fontSize = 16.sp,
            color = VideoInk,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "👍",
                fontSize = 20.sp,
                modifier = Modifier.clickable { onToggleLike() },
            )
            Text("$likeCount", fontSize = 11.sp, color = VideoMuted)
            Spacer(Modifier.height(6.dp))
            Text("👎", fontSize = 15.sp, color = VideoMuted)
        }
    }
}

/** 标签行：绿色 chips（句数类为灰色），末行行尾展开箭头。 */
@Composable
private fun VideoDetailTagsRow(tags: List<String>, expanded: Boolean, onToggleDesc: () -> Unit) {
    val rows = tags.chunked(3)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Spacer(Modifier.height(16.dp))
        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { tag ->
                    VideoTagChip(tag)
                    Spacer(Modifier.width(6.dp))
                }
                if (rowIndex == rows.lastIndex) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (expanded) "⌃" else "⌄",
                        fontSize = 14.sp,
                        color = VideoMuted,
                        modifier = Modifier.clickable { onToggleDesc() },
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoTagChip(tag: String) {
    val gray = tag.endsWith("句")
    Box(
        modifier = Modifier.background(
            if (gray) VideoChipGrayBg else VideoAccentSoft,
            RoundedCornerShape(6.dp),
        )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tag,
            fontSize = 12.sp,
            color = if (gray) VideoChipGrayText else VideoAccent,
        )
    }
}

/** 简介行：摘要 + 展开/收起箭头。 */
@Composable
private fun VideoDetailDescRow(video: ShortVideo, expanded: Boolean, onToggleDesc: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (expanded || video.desc.length <= 30) video.desc else video.desc.take(30) + "…",
            fontSize = 13.sp,
            color = VideoMuted,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (expanded) "⌃" else "⌄",
            fontSize = 14.sp,
            color = VideoMuted,
            modifier = Modifier.clickable { onToggleDesc() },
        )
    }
}

/** 上传者行：绿色头像 + 名称 + 打赏按钮。 */
@Composable
private fun VideoDetailUploaderRow(video: ShortVideo, onToast: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(VideoAvatarBg, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("趣", fontSize = 16.sp, color = VideoAccent, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "上传者 ${video.uploaderLabel}",
            fontSize = 14.sp,
            color = VideoInk,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.border(1.dp, VideoHeart, RoundedCornerShape(100.dp))
                .clickable { onToast("打赏（开发中）") }
                .padding(horizontal = 12.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("打赏", fontSize = 12.sp, color = VideoHeart, fontWeight = FontWeight.Medium)
        }
    }
}

/** 视频专辑：标题行 + 横向分集卡（选中项绿色描边）。 */
@Composable
private fun VideoDetailAlbumSection(
    video: ShortVideo,
    partIndex: Int,
    onPartSelected: (Int) -> Unit,
    onToast: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "视频专辑 (${video.albumCount})",
                fontSize = 15.sp,
                color = VideoInk,
                fontWeight = FontWeight.Bold,
            )
            Text(" ›", fontSize = 15.sp, color = VideoChevron)
            Spacer(Modifier.weight(1f))
            Text(
                "+ 添加学习计划",
                fontSize = 13.sp,
                color = VideoAccent,
                modifier = Modifier.clickable { onToast("添加学习计划（开发中）") },
            )
        }
        if (video.albumParts.isEmpty()) {
            Text(
                "暂无分集",
                fontSize = 13.sp,
                color = VideoMuted,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                video.albumParts.forEachIndexed { index, part ->
                    VideoAlbumPartCard(
                        part = part,
                        index = index,
                        selected = index == partIndex,
                        onClick = { onPartSelected(index) },
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun VideoAlbumPartCard(
    part: VideoAlbumPart,
    index: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(100.dp)
            .height(74.dp)
            .background(VideoPageBg, RoundedCornerShape(10.dp))
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) VideoAccent else VideoCardBorder,
                RoundedCornerShape(10.dp),
            )
            .clickable { onClick() }
            .padding(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Part ${index + 1}",
                fontSize = 11.sp,
                color = if (selected) VideoAccent else VideoMuted,
            )
            Spacer(Modifier.weight(1f))
            val badge = part.badge
            if (badge != null) {
                val trial = badge == "试听"
                Box(
                    modifier = Modifier.background(
                        if (trial) VideoAccentSoft else VideoBadgePaidBg,
                        RoundedCornerShape(4.dp),
                    )
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        badge,
                        fontSize = 10.sp,
                        color = if (trial) VideoAccent else VideoBadgePaidText,
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            part.title,
            fontSize = 12.sp,
            color = if (selected) VideoInk else VideoMuted,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
        )
    }
}

/** 最新作品：标题行 + 一排圆形占位。 */
@Composable
private fun VideoDetailLatestSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        VideoSectionHeader(title = "最新作品")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(6) {
                Box(
                    modifier = Modifier.size(32.dp)
                        .background(VideoAvatarBlue, RoundedCornerShape(16.dp)),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

/** 点赞榜：奖牌 + 头像（V 等级徽章）+ 昵称/日期 + 红心计数。 */
@Composable
private fun VideoDetailLeaderboardCard(leaderboard: List<VideoLeaderboard>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        VideoSectionHeader(title = "点赞榜")
        if (leaderboard.isEmpty()) {
            Text(
                "暂无榜单数据",
                fontSize = 13.sp,
                color = VideoMuted,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        } else {
            leaderboard.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val medal = when (index) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "  " }
                    Text(medal, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(32.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp)
                                .background(VideoAvatarBlue, RoundedCornerShape(16.dp)),
                        )
                        Box(
                            modifier = Modifier.align(Alignment.BottomStart)
                                .background(VideoLevelBadge, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        ) {
                            Text(entry.level, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.userName,
                            fontSize = 13.sp,
                            color = VideoInk,
                            fontWeight = FontWeight.Medium,
                        )
                        Text("${entry.date} · ${entry.location}", fontSize = 11.sp, color = VideoMuted)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("♥", fontSize = 14.sp, color = VideoHeart)
                    Spacer(Modifier.width(4.dp))
                    Text(compactLike(entry.likeCount), fontSize = 12.sp, color = VideoHeart)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun VideoSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 15.sp, color = VideoInk, fontWeight = FontWeight.Bold)
        Text(" ›", fontSize = 15.sp, color = VideoChevron)
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

/** 底部固定操作栏：收藏 / 分享 / 开启配音。 */
@Composable
private fun VideoDetailBottomBar(
    favorited: Boolean,
    onToast: (String) -> Unit,
    onDubbing: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(1.dp)
                .background(VideoCardBorder),
        )
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(VideoPageBg)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VideoBottomAction(
                icon = if (favorited) "★" else "☆",
                label = if (favorited) "已收藏" else "收藏",
                tint = VideoInk,
                onClick = onToggleFavorite,
            )
            Spacer(Modifier.width(12.dp))
            VideoBottomAction(
                icon = "↗",
                label = "分享",
                tint = VideoAccent,
                onClick = { onToast("分享（开发中）") },
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier.weight(1f)
                    .height(34.dp)
                    .background(VideoAccent, RoundedCornerShape(17.dp))
                    .clickable { onDubbing() },
                contentAlignment = Alignment.Center,
            ) {
                Text("开启配音", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VideoBottomAction(
    icon: String,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 17.sp, color = tint)
        Text(label, fontSize = 10.sp, color = VideoMuted)
    }
}

private fun formatVideoSec(sec: Int): String = "%02d:%02d".format(sec / 60, sec % 60)

private val COMMENT_CANDIDATES = listOf("发音标准", "继续加油", "喜欢这个片段", "学到了", "棒棒哒")
