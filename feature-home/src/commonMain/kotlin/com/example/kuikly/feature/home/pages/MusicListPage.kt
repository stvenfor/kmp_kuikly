package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.music.MUSIC_PLACEHOLDER_FALLBACK
import com.example.kuikly.data.music.MusicPlaybackStore
import com.example.kuikly.data.music.MusicStore
import com.example.kuikly.data.music.Song
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.core.annotations.Page

/**
 * `musicListDarkTheme` 令牌镜像（`features/music/lib/theme/music_theme.dart`）+
 * 迷你条常量（`widgets/music_mini_player_bar.dart`）。
 *
 * **刻度**：Flutter Music 模块 view/theme/widgets **全用裸逻辑 px**（grep 无 `.w/.h/.sp`），
 * 故一律裸 `dp`/`sp`，**不要** `.su()`（判断法同 P2-03 `TabBarTokens` / P2-W2b `ChatPalette`）。
 */
private object MusicPalette {
    val background = Color(0xFF000000)
    val miniBar = Color(0xFF1A1A1A)
    val accent = Color(0xFF4DD0C8)
    val trackInactive = Color(0x1FFFFFFF)
    val fallbackCover = Color(MUSIC_PLACEHOLDER_FALLBACK)
    /** Flutter `Material(elevation: 8)` 在深色底上的 ambient+spot 影调。 */
    val miniBarShadow = Color(0x66000000)
}

/** Flutter `musicMiniPlayerBarHeight`（列表/首页底部留白）。 */
internal const val MUSIC_MINI_BAR_INSET = 72f

/**
 * 音频列表 — Flutter `MusicListPage` 复刻（Phase-2 / P2-W2c）。
 * 真源：`features/music/lib/view/music_list_page.dart`（dark theme + AppNavBar +
 * ListView ListTile + MusicMiniPlayerBar + shuffle FAB）。
 *
 * Kuikly 无音频引擎/图片加载器（同 Phase-1 口径）：点行写入 `MusicPlaybackStore` 并 push
 * [MusicNowPlayingPage]（P2-W3 补齐），迷你条读 store 状态且标题区可点开播放页。
 */
@Page(name = "MusicList", moduleId = "feature_home")
internal class MusicListPage : BaseComposePager() {

    private var hasSession by mutableStateOf(MusicPlaybackStore.hasSession)
    private var playing by mutableStateOf(MusicPlaybackStore.playing)

    override fun pageDidAppear() {
        super.pageDidAppear()
        refreshSession()
    }

    private fun refreshSession() {
        hasSession = MusicPlaybackStore.hasSession
        playing = MusicPlaybackStore.playing
    }

    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            fun load() {
                MusicStore.repo.list()
                    .onSuccess {
                        songs = it
                        empty = it.isEmpty()
                        error = null
                    }
                    .onFailure {
                        songs = emptyList()
                        empty = false
                        error = it.message
                    }
            }

            if (!booted) {
                booted = true
                load()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MusicPalette.background),
            ) {
                // Flutter `AppNavBar(title: '音频列表', style: dark, actions: [Now Playing])`
                AppNavBarBar(
                    title = "音频列表",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = MusicPalette.background,
                    foreground = Color.White,
                    actions = {
                        if (hasSession) {
                            // Flutter `TextButton`（`primary` = white in musicListDarkTheme）。
                            Text(
                                "Now Playing",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier
                                    .clickable {
                                        Utils.currentBridgeModule().openPage(PageNames.MusicNowPlaying)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    },
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        error != null -> DomainEmptyStateDark(
                            glyph = "⚠",
                            title = "加载失败",
                            message = error ?: "加载失败",
                            actionLabel = "重试",
                            onAction = { load() },
                        )
                        empty -> DomainEmptyStateDark(
                            glyph = "♪",
                            title = "暂无音频",
                            message = "点击刷新重新加载",
                            actionLabel = "刷新",
                            onAction = { load() },
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            // Flutter `Padding(bottom: miniBarInset)` 包住 ListView。
                            contentPadding = PaddingValues(
                                bottom = if (hasSession) MUSIC_MINI_BAR_INSET.dp else 24.dp,
                            ),
                        ) {
                            items(songs, key = { it.id }) { song ->
                                SongRow(
                                    song = song,
                                    onTap = {
                                        // Flutter：点行 selectSong → 立即 push NowPlaying。
                                        MusicPlaybackStore.play(song, songs)
                                        refreshSession()
                                        Utils.currentBridgeModule().openPage(PageNames.MusicNowPlaying)
                                    },
                                )
                            }
                        }
                    }
                    if (hasSession) {
                        Box(Modifier.align(Alignment.BottomCenter)) {
                            MusicMiniPlayerBar(
                                song = MusicPlaybackStore.currentSong!!,
                                playing = playing,
                                onToggle = {
                                    MusicPlaybackStore.toggle()
                                    refreshSession()
                                },
                                onClose = {
                                    MusicPlaybackStore.clear()
                                    refreshSession()
                                },
                            )
                        }
                    }
                    // Flutter `FloatingActionButton(backgroundColor: #4DD0C8, child: Icons.shuffle)`
                    // （默认 56、右下 16，叠在迷你条上方：padding.bottom = miniBarInset）。
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 16.dp,
                                bottom = (if (hasSession) MUSIC_MINI_BAR_INSET + 16f else 16f).dp,
                            )
                            .size(56.dp)
                            .background(MusicPalette.accent, CircleShape)
                            .clickable {
                                // Flutter `controller.shuffleAndPlay()` → push NowPlaying。
                                if (songs.isNotEmpty()) {
                                    MusicPlaybackStore.play(songs.random(), songs)
                                    refreshSession()
                                    Utils.currentBridgeModule().openPage(PageNames.MusicNowPlaying)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("⇄", fontSize = 22.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Flutter `_SongListTile` = `ListTile(leading: CircleAvatar(radius 24), title: 16·w500,
 * subtitle: 14·white60 'By artist')`。
 *
 * 源核验（P3-E3e，SDK list_tile.dart）：两行 ListTile `_targetTileHeight = 72` 且内容垂直居中
 * （`max(72, content + 2×minVerticalPadding4)`）→ `heightIn(min = 72.dp)`。**无** trailing/duration
 * （duration 只在 NowPlaying 渲染）、**无** 分隔线（`ListView.builder` 裸 ListTile）。
 */
@Composable
private fun SongRow(song: Song, onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    song.placeholder?.let { Color(it) } ?: MusicPalette.fallbackCover,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", fontSize = 22.sp, color = Color.White.copy(alpha = 0.9f))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "By ${song.artist}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Mock 迷你播放条 — Flutter `MusicMiniPlayerBar`（`widgets/music_mini_player_bar.dart`）：
 * 2px 进度条（`#4DD0C8` on `white12`）+ 44 封面叠播放/暂停 + 歌名 14·w600 / 14→12 `By artist`
 * + ✕ 关闭。
 *
 * ponytail 天花板：无真实音频引擎 → `position`/`duration` 恒为 0，进度条停在 0、
 * 时间行（`if (total.inMilliseconds > 0)`）不渲染。
 */
@Composable
internal fun MusicMiniPlayerBar(
    song: Song,
    playing: Boolean,
    onToggle: () -> Unit,
    onClose: () -> Unit,
) {    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `Material(elevation: 8)` 投影，ambient+spot 同色（黑色 40% @ 深色底）。
            .shadow(
                8.dp,
                ambientColor = MusicPalette.miniBarShadow,
                spotColor = MusicPalette.miniBarShadow,
            )
            .background(MusicPalette.miniBar),
    ) {
        // Flutter `LinearProgressIndicator(value: progress, minHeight: 2)`
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MusicPalette.trackInactive),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            song.placeholder?.let { Color(it) } ?: MusicPalette.fallbackCover,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("▶", fontSize = 22.sp, color = Color.White.copy(alpha = 0.9f))
                }
                // Flutter `Container(44×44, black@0.35, circle) + Icon(pause/play_arrow, 22)`
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (playing) "⏸" else "▶", fontSize = 22.sp, color = Color.White)
                }
            }
            Spacer(Modifier.width(12.dp))
            // Flutter `InkWell(onTap: () => Get.toNamed(musicNowPlaying))` 包住歌名/歌手列。
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { Utils.currentBridgeModule().openPage(PageNames.MusicNowPlaying) },
            ) {
                Text(
                    song.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "By ${song.artist}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "✕",
                fontSize = 22.sp,
                color = Color(0xB3FFFFFF),
                modifier = Modifier
                    .clickable(onClick = onClose)
                    .padding(AppChrome.ICON_TAP_PADDING.dp),
            )
        }
    }
}

/** 深色底（Music/Live 旧暗色页）用的空态/错误态，配色对齐白字体系。 */
@Composable
private fun DomainEmptyStateDark(
    glyph: String,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(glyph, fontSize = 44.sp, color = Color(0x66FFFFFF))
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(message, fontSize = 13.sp, color = Color(0xB3FFFFFF), maxLines = 2)
        Spacer(Modifier.height(16.dp))
        Text(
            actionLabel,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MusicPalette.accent,
            modifier = Modifier.clickable(onClick = onAction).padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
