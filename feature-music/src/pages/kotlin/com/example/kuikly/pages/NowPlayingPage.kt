package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.music.MusicStore
import com.example.kuikly.data.music.PlaybackState
import com.example.kuikly.data.music.Playlist
import com.example.kuikly.data.music.Track
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

@Page("NowPlaying")
internal class NowPlayingPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        setContent {
            var currentId by remember { mutableStateOf<String?>(null) }
            var pbState by remember { mutableStateOf(PlaybackState.idle) }
            var position by remember { mutableStateOf(0) }
            var muted by remember { mutableStateOf(false) }
            var favorite by remember { mutableStateOf(false) }
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var booted by remember { mutableStateOf(false) }

            fun sync() {
                val pb = MusicStore.repo.playbackState()
                pbState = pb.first
                currentId = pb.second
                position = MusicStore.repo.position()
                muted = MusicStore.repo.isMuted()
                val idNow = pb.second
                favorite = idNow != null && MusicStore.repo.isFavorite(idNow)
            }

            if (!booted) {
                booted = true
                if (id.isNotBlank()) MusicStore.repo.play(id)
                sync()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
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
                    Text("Now Playing", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                val track = MusicStore.repo.playbackState().third
                val total = MusicStore.repo.durationSec()
                val ratio = MusicStore.repo.progress()

                if (track == null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("暂无播放歌曲", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        Text("点击下面按钮从第一首开始播放", fontSize = 13.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(12.dp))
                        MusicTextButton(text = "播放第一首") {
                            MusicStore.repo.playAt(0)
                            sync()
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                        item {
                            NowPlayingCover(track = track, playing = pbState == PlaybackState.playing)
                            Spacer(Modifier.height(10.dp))
                        }
                        item {
                            NowPlayingProgressCard(
                                position = position,
                                total = total,
                                ratio = ratio,
                                onSeek = { sec -> MusicStore.repo.seekTo(sec); sync() },
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        item {
                            NowPlayingControlCard(
                                playing = pbState == PlaybackState.playing,
                                muted = muted,
                                favorite = favorite,
                                onPrev = { MusicStore.repo.previous(); sync() },
                                onToggle = { pbState = MusicStore.repo.toggle(); sync() },
                                onNext = { MusicStore.repo.next(); sync() },
                                onMute = { MusicStore.repo.toggleMute(); sync() },
                                onToggleFavorite = {
                                    val idNow = currentId
                                    if (idNow != null) {
                                        MusicStore.repo.toggleFavorite(idNow)
                                            .onSuccess { fav ->
                                                favorite = fav
                                                Utils.currentBridgeModule()
                                                    .toast(if (fav) "已收藏" else "已取消收藏")
                                            }
                                            .onFailure {
                                                Utils.currentBridgeModule().toast("操作失败")
                                            }
                                    }
                                },
                                onComplete = {
                                    // 源 onComplete：播放完成自动进入下一首（连播）。
                                    MusicStore.repo.onComplete()
                                    sync()
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        item {
                            NowPlayingPlaylistCard(
                                track = track,
                                onPlay = { tid -> MusicStore.repo.play(tid); sync() },
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        item {
                            MusicMockCycleRow(scenario = scenario) {
                                scenario = MockBackend.cycle()
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NowPlayingCover(track: Track, playing: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(220.dp)
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusLg)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (playing) "♫" else "♪", fontSize = 40.sp, color = KuiklyTheme.onPrimary)
                Spacer(Modifier.height(10.dp))
                Text(track.album, fontSize = 14.sp, color = KuiklyTheme.onPrimary, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text("静态封面占位", fontSize = 10.sp, color = KuiklyTheme.onPrimary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(track.title, fontSize = 20.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text("${track.artist} · ${track.album}", fontSize = 13.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun NowPlayingProgressCard(position: Int, total: Int, ratio: Double, onSeek: (Int) -> Unit) {
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicProgressTrack(progress = ratio, trackWidth = 200)
            Spacer(Modifier.width(10.dp))
            Text("${formatMusicSec(position)} / ${formatMusicSec(total)}", fontSize = 12.sp, color = KuiklyTheme.muted)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicSeekChip(text = "-15s") { onSeek(position - 15) }
            Spacer(Modifier.width(6.dp))
            MusicSeekChip(text = "+15s") { onSeek(position + 15) }
            Spacer(Modifier.width(6.dp))
            MusicSeekChip(text = "25%") { onSeek((total * 0.25).toInt()) }
            Spacer(Modifier.width(6.dp))
            MusicSeekChip(text = "75%") { onSeek((total * 0.75).toInt()) }
        }
    }
}

@Composable
internal fun MusicProgressTrack(progress: Double, trackWidth: Int = 260) {
    val ratio = progress.coerceIn(0.0, 1.0)
    Box(
        modifier = Modifier
            .width(trackWidth.dp)
            .height(6.dp)
            .background(KuiklyTheme.hairline, RoundedCornerShape(KuiklyTheme.radiusPill)),
    ) {
        Box(
            modifier = Modifier
                .width((trackWidth.toDouble() * ratio).toFloat().dp)
                .height(6.dp)
                .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusPill)),
        )
    }
}

@Composable
private fun MusicSeekChip(text: String, onClick: () -> Unit) {
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
private fun NowPlayingControlCard(
    playing: Boolean,
    muted: Boolean,
    favorite: Boolean,
    onPrev: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onMute: () -> Unit,
    onToggleFavorite: () -> Unit,
    onComplete: () -> Unit,
) {
    KCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            MusicControlButton(text = "⏮", onClick = onPrev)
            Spacer(Modifier.width(16.dp))
            MusicControlButton(text = if (playing) "⏸" else "▶", primary = true, onClick = onToggle)
            Spacer(Modifier.width(16.dp))
            MusicControlButton(text = "⏭", onClick = onNext)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(if (playing) "播放中" else "已暂停", color = if (playing) KuiklyTheme.success else KuiklyTheme.muted)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (muted) KuiklyTheme.warningSoft else KuiklyTheme.canvasSoft2,
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .clickable { onMute() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (muted) "已静音" else "静音", fontSize = 12.sp, color = KuiklyTheme.ink)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(
                        if (favorite) KuiklyTheme.highlightMagenta else KuiklyTheme.canvasSoft2,
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .clickable { onToggleFavorite() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (favorite) "♥ 已收藏" else "♡ 收藏",
                    fontSize = 12.sp,
                    color = if (favorite) KuiklyTheme.onPrimary else KuiklyTheme.ink,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .clickable { onComplete() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("⏭ 模拟播放完成", fontSize = 12.sp, color = KuiklyTheme.ink)
            }
        }
    }
}

@Composable
private fun MusicControlButton(text: String, primary: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(if (primary) 64.dp else 52.dp)
            .height(if (primary) 64.dp else 52.dp)
            .background(
                if (primary) KuiklyTheme.primary else KuiklyTheme.canvasSoft2,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = if (primary) 22.sp else 18.sp,
            color = if (primary) KuiklyTheme.onPrimary else KuiklyTheme.ink,
        )
    }
}

@Composable
private fun NowPlayingPlaylistCard(track: Track, onPlay: (String) -> Unit) {
    val playlists: List<Playlist> = MusicStore.repo.playlists().getOrDefault(emptyList())
    val current = playlists.find { it.id == track.playlistId }
    val tracks: List<Track> = MusicStore.repo.tracksOf(track.playlistId).getOrDefault(emptyList())
    KCard {
        Text(
            "所属歌单 ${current?.name ?: track.playlistId}",
            fontSize = 14.sp,
            color = KuiklyTheme.ink,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text("共 ${tracks.size} 首 · ${current?.countLabel ?: ""}", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        tracks.forEach { t ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        if (t.id == track.id) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .clickable { onPlay(t.id) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    t.title,
                    fontSize = 13.sp,
                    color = if (t.id == track.id) KuiklyTheme.linkDeep else KuiklyTheme.ink,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(120.dp),
                    maxLines = 1,
                )
                Spacer(Modifier.width(8.dp))
                Text(t.artist, fontSize = 12.sp, color = KuiklyTheme.muted)
                Spacer(Modifier.width(8.dp))
                KMetricPill("时长", t.durationLabel)
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

private fun formatMusicSec(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)
