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
import com.example.kuikly.data.music.Track
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
import com.tencent.kuikly.compose.foundation.lazy.itemsIndexed
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

/**
 * 音频列表页（port of source MusicListPage）。
 *
 * 视觉对齐 Flutter 参考图 15-flutter-music-list：深色底 + 顶部「返回 / 音频列表」标题栏，
 * 列表行为「圆形封面（播放三角） + 曲目标题 + 歌手」两行结构，右下角随机播放 FAB。
 * 业务逻辑沿用 MusicStore：点行播放、迷你播放条（播放会话中出现）、Mock 场景切换。
 */
@Page("Music")
internal class MusicPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var list by remember { mutableStateOf<List<Track>>(emptyList()) }
            var playingId by remember { mutableStateOf<String?>(null) }
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

            fun reload() {
                scenario = MockBackend.scenario
                val result = MusicStore.repo.page(1, 50)
                if (result.isSuccess) {
                    list = result.getOrNull()?.items ?: emptyList()
                    empty = list.isEmpty()
                    error = null
                } else {
                    list = emptyList()
                    empty = false
                    error = result.exceptionOrNull()?.message ?: "加载失败"
                }
            }

            if (!booted) {
                booted = true
                syncPlayback()
                reload()
            }

            Box(modifier = Modifier.fillMaxSize().background(MusicListBg)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    MusicListTopBar(
                        onBack = { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.height(42.dp))
                    when {
                        error != null && list.isEmpty() ->
                            MusicErrorState(message = error ?: "加载失败", onRetry = ::reload)
                        empty -> MusicEmptyState(onRefresh = ::reload)
                        else -> LazyColumn(
                            modifier = Modifier.weight(1f),
                        ) {
                            itemsIndexed(list, key = { _, track -> track.id }) { index, track ->
                                MusicTrackRow(
                                    track = track,
                                    coverIndex = index,
                                    playing = playingId == track.id && pbState == PlaybackState.playing,
                                    onTap = {
                                        MusicStore.repo.play(track.id)
                                        syncPlayback()
                                    },
                                )
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
                // 随机播放 FAB（对齐 Flutter MusicListPage 的 shuffle FAB）。
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 60.dp),
                ) {
                    MusicShuffleFab(onClick = {
                        MusicStore.repo.shuffle()
                        syncPlayback()
                        Utils.currentBridgeModule().openPage(url = "NowPlaying")
                    })
                }
            }
        }
    }
}

/** 深色画布 / 文字 / FAB 常量（port of source MusicListPage 的暗色主题）。 */
private val MusicListBg = Color(0xFF000000)
private val MusicRowTitle = Color(0xFFFFFFFF)
private val MusicRowSubtitle = Color(0xFF999999)
private val MusicFabBg = Color(0xFF4DD0C8)
private val MusicMiniBg = Color(0xFF1C1C1E)
private val MusicMiniSub = Color(0xFF8E8E93)

/** 封面圆占位色板（对齐参考图中 Material 色系的纯色封面）。 */
private val MusicCoverPalette = listOf(
    Color(0xFF673AB7),
    Color(0xFF2196F3),
    Color(0xFF3F51B5),
    Color(0xFF009688),
    Color(0xFF424242),
)

@Composable
private fun MusicListTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹",
            fontSize = 26.sp,
            color = MusicRowTitle,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBack() }.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.weight(1f))
        Text(
            "音频列表",
            fontSize = 17.sp,
            color = MusicRowTitle,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun MusicTrackRow(
    track: Track,
    coverIndex: Int,
    playing: Boolean,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { onTap() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MusicCoverCircle(coverIndex = coverIndex)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${track.title}${if (playing) "  ▶" else ""}",
                fontSize = 15.sp,
                color = MusicRowTitle,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                "By ${track.artist}",
                fontSize = 13.sp,
                color = MusicRowSubtitle,
                maxLines = 1,
            )
        }
    }
}

/** 圆形封面占位：纯色圆底 + 居中白色播放三角（封面图加载失败时的参考图样式）。 */
@Composable
private fun MusicCoverCircle(coverIndex: Int) {
    val bg = MusicCoverPalette[coverIndex % MusicCoverPalette.size]
    Box(
        modifier = Modifier.size(32.dp).background(bg, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("▶", fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun MusicShuffleFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(52.dp)
            .background(MusicFabBg, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text("⇄", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MusicMiniBg, RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp)
                    .background(MusicCoverPalette[0], RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("▶", fontSize = 12.sp, color = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, fontSize = 13.sp, color = MusicRowTitle, maxLines = 1)
                Text(
                    when (pbState) {
                        PlaybackState.playing -> "播放中"
                        PlaybackState.paused -> "已暂停"
                        PlaybackState.idle -> "空闲"
                    },
                    fontSize = 11.sp,
                    color = MusicMiniSub,
                )
            }
            Text(
                if (pbState == PlaybackState.playing) "暂停" else "播放",
                fontSize = 12.sp,
                color = MusicFabBg,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onToggle() }.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "详情 ›",
                fontSize = 12.sp,
                color = MusicFabBg,
                modifier = Modifier.clickable { onOpen() }.padding(4.dp),
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
                color = MusicMiniSub,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "关闭",
                fontSize = 12.sp,
                color = MusicMiniSub,
                modifier = Modifier.clickable { onClose() }.padding(4.dp),
            )
        }
    }
}

private fun formatMusicSec(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

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
