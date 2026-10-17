package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.video.ShortVideo
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
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
 * 沉浸式 mock 播放器 — Flutter `ShortVideoPlayPage` 业务对齐。
 * 真源：`features/video/lib/short_video/view/short_video_play_page.dart`
 * （`ShortVideoPlayerKit.single` + `PlaybackEvent`：play/pause/position）。
 *
 * 本工程无播放器 SDK，故用状态机模拟：Idle/Playing/Paused/Ended，
 * 播放时上报播放量（reportView），暂停/结束时落盘进度（saveProgress），并支持收藏。
 */
private enum class PlayState { Idle, Playing, Paused, Ended }

@Page("VideoPlay")
internal class VideoPlayPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        setContent {
            val result = remember(id) { VideoStore.repo.fetchById(id) }
            val video = result.getOrNull()
            var state by remember { mutableStateOf(PlayState.Idle) }
            var currentSec by remember(id) { mutableStateOf(VideoStore.repo.playedSecOf(id)) }
            var favorited by remember(id) { mutableStateOf(VideoStore.repo.isFavorited(id)) }
            var toastText by remember { mutableStateOf<String?>(null) }

            val total = video?.durationSec ?: 0
            val ratio = if (total > 0) currentSec.toDouble() / total.toDouble() else 0.0

            fun onPlayPause() {
                when (state) {
                    PlayState.Idle, PlayState.Paused -> {
                        if (state == PlayState.Idle) {
                            VideoStore.repo.reportView(id)
                            Utils.currentBridgeModule().toast("开始播放，已上报播放量")
                        }
                        state = PlayState.Playing
                    }
                    PlayState.Playing -> {
                        state = PlayState.Paused
                        VideoStore.repo.saveProgress(id, currentSec)
                    }
                    PlayState.Ended -> {
                        currentSec = 0
                        state = PlayState.Playing
                    }
                }
            }

            // 模拟播放帧：仅在 Playing 态推进进度（对应源 PlaybackEvent.position）。
            fun onStep(sec: Int) {
                if (state != PlayState.Playing) return
                val next = (currentSec + sec).coerceIn(0, total)
                currentSec = next
                if (total > 0 && next >= total) {
                    state = PlayState.Ended
                    VideoStore.repo.saveProgress(id, total)
                }
            }

            fun onSeek(sec: Int) {
                val next = sec.coerceIn(0, total)
                currentSec = next
                when {
                    total > 0 && next >= total -> state = PlayState.Ended
                    state == PlayState.Ended -> state = PlayState.Paused
                    else -> Unit
                }
                VideoStore.repo.saveProgress(id, next)
            }

            fun onFavorite() {
                VideoStore.repo.favorite(id)
                    .onSuccess { fav ->
                        favorited = fav
                        Utils.currentBridgeModule().toast(if (fav) "已收藏" else "已取消收藏")
                    }
            }

            Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                VideoPlayTopBar(onBack = { Utils.currentBridgeModule().closePage() })
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "▶",
                            fontSize = 56.sp,
                            color = if (state == PlayState.Playing) KuiklyTheme.primary else Color(0xCCFFFFFF),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            video?.title ?: "视频不存在",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "状态：${when (state) {
                                PlayState.Idle -> "未开始"
                                PlayState.Playing -> "播放中"
                                PlayState.Paused -> "已暂停"
                                PlayState.Ended -> "已播完"
                            }}",
                            fontSize = 13.sp,
                            color = Color(0xAAFFFFFF),
                        )
                    }
                }
                VideoPlayControls(
                    state = state,
                    ratio = ratio,
                    currentSec = currentSec,
                    total = total,
                    favorited = favorited,
                    onPlayPause = ::onPlayPause,
                    onStep = ::onStep,
                    onSeek = ::onSeek,
                    onFavorite = ::onFavorite,
                )
                if (toastText != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("提示：$toastText", fontSize = 12.sp, color = Color(0xAAFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                } else {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun VideoPlayTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹",
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier.clickable { onBack() }.padding(12.dp),
        )
        Spacer(Modifier.weight(1f))
        Text("沉浸式播放", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(46.dp))
    }
}

@Composable
private fun VideoPlayControls(
    state: PlayState,
    ratio: Double,
    currentSec: Int,
    total: Int,
    favorited: Boolean,
    onPlayPause: () -> Unit,
    onStep: (Int) -> Unit,
    onSeek: (Int) -> Unit,
    onFavorite: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        VideoProgressTrack(progress = ratio, trackWidth = 320)
        Spacer(Modifier.height(8.dp))
        Text(
            "${formatVideoSec(currentSec)} / ${formatVideoSec(total)}",
            fontSize = 12.sp,
            color = Color(0xAAFFFFFF),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VideoPlayButton(text = if (state == PlayState.Playing) "⏸ 暂停" else "▶ 播放") { onPlayPause() }
            Spacer(Modifier.width(8.dp))
            VideoPlayButton(text = "-15s") { onSeek(currentSec - 15) }
            Spacer(Modifier.width(8.dp))
            VideoPlayButton(text = "+15s") { onSeek(currentSec + 15) }
            Spacer(Modifier.width(8.dp))
            VideoPlayButton(text = "从头") { onSeek(0) }
            Spacer(Modifier.width(8.dp))
            VideoPlayButton(text = "看完") { onSeek(total) }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 仅在播放态可用的「模拟播放帧」，驱动状态机前进。
            val stepping = state == PlayState.Playing
            VideoPlayButton(text = if (stepping) "⏭ 模拟播放帧(+5s)" else "▶ 先播放再模拟", enabled = stepping) { onStep(5) }
            Spacer(Modifier.width(10.dp))
            Row(
                modifier = Modifier.clickable { onFavorite() }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("♥", fontSize = 20.sp, color = if (favorited) KuiklyTheme.error else Color(0xAAFFFFFF))
                Spacer(Modifier.width(4.dp))
                Text(if (favorited) "已收藏" else "收藏", fontSize = 12.sp, color = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun VideoPlayButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (enabled) KuiklyTheme.canvasSoft2 else KuiklyTheme.hairline,
                RoundedCornerShape(KuiklyTheme.radiusSm),
            )
            .clickable { if (enabled) onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = if (enabled) KuiklyTheme.ink else KuiklyTheme.muted,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun formatVideoSec(sec: Int): String = "%02d:%02d".format(sec / 60, sec % 60)
