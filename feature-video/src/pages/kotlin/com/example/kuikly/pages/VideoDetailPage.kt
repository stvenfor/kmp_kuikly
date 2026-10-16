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
import com.example.kuikly.data.video.ShortVideoStatus
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KMetricPill
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
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
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
            var playedSec by remember(id) { mutableStateOf(VideoStore.repo.playedSecOf(id)) }
            var partIndex by remember { mutableStateOf(0) }
            var descExpanded by remember { mutableStateOf(false) }
            var toastText by remember { mutableStateOf<String?>(null) }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                VideoDetailNavBar(title = "视频详情")
                val video = result.getOrNull()
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
                        onDubbing = {
                            val data = JSONObject().apply { put("id", video.id) }
                            Utils.currentBridgeModule().openPage(url = "VideoDubbing", userData = data)
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
    onSeek: (Int) -> Unit,
    onPartSelected: (Int) -> Unit,
    onToggleDesc: () -> Unit,
    onToast: (String) -> Unit,
    onDubbing: () -> Unit,
) {
    val total = video.durationSec
    val ratio = if (total > 0) playedSec.toDouble() / total.toDouble() else 0.0
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        item {
            VideoDetailCover(video = video, ratio = ratio)
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
            VideoDetailTitleCard(video = video)
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
            VideoDetailBottomBar(onToast = onToast, onDubbing = onDubbing)
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
private fun VideoDetailCover(video: ShortVideo, ratio: Double) {
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
    }
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
private fun VideoDetailTitleCard(video: ShortVideo) {
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
                Text("♥", fontSize = 18.sp, color = KuiklyTheme.error)
                Text(video.likeLabel, fontSize = 11.sp, color = KuiklyTheme.muted)
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
private fun VideoDetailBottomBar(onToast: (String) -> Unit, onDubbing: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        VideoBottomAction(label = "收藏") { onToast("已收藏") }
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
private fun VideoBottomAction(label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("✩", fontSize = 18.sp, color = KuiklyTheme.ink)
        Text(label, fontSize = 11.sp, color = KuiklyTheme.muted)
    }
}

private fun statusLabelOf(status: ShortVideoStatus): String = when (status) {
    ShortVideoStatus.published -> "已发布"
    ShortVideoStatus.reviewing -> "审核中"
    ShortVideoStatus.draft -> "草稿"
}

private fun formatVideoSec(sec: Int): String = "%02d:%02d".format(sec / 60, sec % 60)
