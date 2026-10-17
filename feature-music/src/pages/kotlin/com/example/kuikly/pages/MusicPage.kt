package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
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
import com.tencent.kuikly.compose.foundation.lazy.items
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

@Page("Music")
internal class MusicPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var list by remember { mutableStateOf<List<Track>>(emptyList()) }
            var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
            var activePlaylistId by remember { mutableStateOf<String?>(null) }
            var playingId by remember { mutableStateOf<String?>(null) }
            var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
            var pbState by remember { mutableStateOf(PlaybackState.idle) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var booted by remember { mutableStateOf(false) }

            fun syncPlayback() {
                val pb = MusicStore.repo.playbackState()
                pbState = pb.first
                playingId = pb.second
            }

            fun refreshFavorites() {
                favoriteIds = MusicStore.repo.favoriteTracks().map { it.id }.toSet()
            }

            fun applyResult(ok: Boolean, tracks: List<Track>, message: String?) {
                if (ok) {
                    list = tracks
                    empty = tracks.isEmpty()
                    error = null
                } else {
                    list = emptyList()
                    empty = false
                    error = message ?: "加载失败"
                }
            }

            fun reload() {
                scenario = MockBackend.scenario
                val pid = activePlaylistId
                if (pid == null) {
                    val result = MusicStore.repo.page(1, 50)
                    applyResult(result.isSuccess, result.getOrNull()?.items ?: emptyList(), result.exceptionOrNull()?.message)
                } else {
                    val result = MusicStore.repo.tracksOf(pid)
                    applyResult(result.isSuccess, result.getOrDefault(emptyList()), result.exceptionOrNull()?.message)
                }
            }

            if (!booted) {
                booted = true
                playlists = MusicStore.repo.playlists().getOrDefault(emptyList())
                syncPlayback()
                refreshFavorites()
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "音乐",
                        fontSize = 22.sp,
                        color = KuiklyTheme.foreground,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(12.dp))
                    MusicTextButton(text = "搜索", onClick = {
                        Utils.currentBridgeModule().openPage(url = "MusicSearch")
                    })
                    Spacer(Modifier.width(8.dp))
                    MusicTextButton(text = "收藏 · 下载", onClick = {
                        Utils.currentBridgeModule().openPage(url = "MusicCollection")
                    })
                    Spacer(Modifier.width(8.dp))
                    MusicTextButton(text = "随机播放", onClick = {
                        MusicStore.repo.shuffle()
                        syncPlayback()
                        Utils.currentBridgeModule().openPage(url = "NowPlaying")
                    })
                }
                MusicPlaylistRow(
                    playlists = playlists,
                    activeId = activePlaylistId,
                    onPick = { pid ->
                        activePlaylistId = if (activePlaylistId == pid) null else pid
                        reload()
                    },
                )
                Spacer(Modifier.height(8.dp))
                when {
                    error != null && list.isEmpty() ->
                        MusicErrorState(message = error ?: "加载失败", onRetry = ::reload)
                    empty -> MusicEmptyState(onRefresh = { activePlaylistId = null; reload() })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    ) {
                        items(list, key = { it.id }) { track ->
                            MusicTrackTile(
                                track = track,
                                playing = playingId == track.id && pbState == PlaybackState.playing,
                                favorite = favoriteIds.contains(track.id),
                                onTap = {
                                    MusicStore.repo.play(track.id)
                                    syncPlayback()
                                },
                                onToggleFavorite = {
                                    MusicStore.repo.toggleFavorite(track.id)
                                    refreshFavorites()
                                    Utils.currentBridgeModule()
                                        .toast(if (favoriteIds.contains(track.id)) "已取消收藏" else "已收藏 ${track.title}")
                                },
                                onOpenPlayer = {
                                    MusicStore.repo.play(track.id)
                                    syncPlayback()
                                    Utils.currentBridgeModule().openPage(
                                        url = "NowPlaying",
                                        userData = JSONObject().apply { put("id", track.id) },
                                    )
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            // 迷你播放条（对齐 Flutter MusicMiniPlayerBar）：仅在有播放会话时显示，
                            // 含进度条、位置/总时长与关闭（stop）入口。
                            val pb = MusicStore.repo.playbackState()
                            val activeTrack = pb.third
                            if (activeTrack != null && (pb.first == PlaybackState.playing || pb.first == PlaybackState.paused)) {
                                MusicPageMiniBar(
                                    track = activeTrack,
                                    pbState = pb.first,
                                    position = MusicStore.repo.position(),
                                    total = MusicStore.repo.durationSec(),
                                    onToggle = {
                                        pbState = MusicStore.repo.toggle()
                                        playingId = MusicStore.repo.playbackState().second
                                    },
                                    onOpen = {
                                        Utils.currentBridgeModule().openPage(url = "NowPlaying")
                                    },
                                    onClose = {
                                        MusicStore.repo.stop()
                                        syncPlayback()
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            MusicMockCycleRow(scenario = scenario) {
                                scenario = MockBackend.cycle()
                                reload()
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
private fun MusicPlaylistRow(playlists: List<Playlist>, activeId: String?, onPick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MusicFilterPill(text = "全部", selected = activeId == null) { onPick("") }
        Spacer(Modifier.width(8.dp))
        playlists.forEach { pl ->
            MusicFilterPill(text = "${pl.name} ${pl.countLabel}", selected = pl.id == activeId) { onPick(pl.id) }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun MusicFilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.primary else KuiklyTheme.canvas,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.ink,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MusicTrackTile(
    track: Track,
    playing: Boolean,
    favorite: Boolean,
    onTap: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onTap() }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicCoverBlock(title = track.title, size = 48)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${track.title}${if (playing) "  ▶" else ""}",
                    fontSize = 15.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${track.artist} · ${track.album} · ${track.durationLabel}",
                    fontSize = 12.sp,
                    color = KuiklyTheme.muted,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KMetricPill("时长", track.durationLabel)
            Spacer(Modifier.width(6.dp))
            KChip(track.playlistId, color = KuiklyTheme.violet)
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (favorite) KuiklyTheme.highlightMagenta else KuiklyTheme.canvasSoft2,
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .clickable { onToggleFavorite() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (favorite) "♥ 已收藏" else "♡ 收藏",
                    fontSize = 12.sp,
                    color = if (favorite) KuiklyTheme.onPrimary else KuiklyTheme.ink,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                "打开播放器 ›",
                fontSize = 12.sp,
                color = KuiklyTheme.link,
                modifier = Modifier.clickable { onOpenPlayer() },
            )
        }
    }
}

@Composable
internal fun MusicCoverBlock(title: String, size: Int) {
    Box(
        modifier = Modifier
            .width(size.dp)
            .height(size.dp)
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (title.isEmpty()) "♪" else title.take(1),
            fontSize = (size / 3).sp,
            color = KuiklyTheme.ink,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MusicPageMiniBar(
    track: Track,
    pbState: PlaybackState,
    position: Int,
    total: Int,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    onClose: () -> Unit,
) {
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicCoverBlock(title = track.title, size = 40)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.width(140.dp)) {
                Text(track.title, fontSize = 13.sp, color = KuiklyTheme.ink, maxLines = 1)
                Text(
                    when (pbState) {
                        PlaybackState.playing -> "播放中"
                        PlaybackState.paused -> "已暂停"
                        PlaybackState.idle -> "空闲"
                    },
                    fontSize = 11.sp,
                    color = KuiklyTheme.muted,
                )
            }
            Spacer(Modifier.width(8.dp))
            MusicTextButton(text = if (pbState == PlaybackState.playing) "暂停" else "播放", onClick = onToggle)
            Spacer(Modifier.width(8.dp))
            Text(
                "详情 ›",
                fontSize = 12.sp,
                color = KuiklyTheme.link,
                modifier = Modifier.clickable { onOpen() },
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicProgressTrack(
                progress = if (total > 0) position.toDouble() / total.toDouble() else 0.0,
                trackWidth = 200,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "${formatMusicSec(position)} / ${formatMusicSec(total)}",
                fontSize = 12.sp,
                color = KuiklyTheme.muted,
            )
            Spacer(Modifier.width(8.dp))
            MusicTextButton(text = "关闭", onClick = onClose)
        }
    }
}

private fun formatMusicSec(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

@Composable
internal fun MusicTextButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusSm))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 12.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun MusicEmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无曲目", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("切换 Mock 场景或清空歌单筛选", fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.clickable { onRefresh() }
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("显示全部", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun MusicErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, fontSize = 14.sp, color = KuiklyTheme.error)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.clickable { onRetry() }
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("重试", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun MusicMockCycleRow(scenario: MockScenario, onCycle: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clickable { onCycle() }.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("[CYCLE MOCK] $scenario", fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}
