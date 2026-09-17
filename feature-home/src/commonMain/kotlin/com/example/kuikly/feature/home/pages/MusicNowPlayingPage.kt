package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.music.MUSIC_PLACEHOLDER_FALLBACK
import com.example.kuikly.data.music.MusicPlaybackStore
import com.example.kuikly.data.music.Song
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 「Now Playing」— Flutter `NowPlayingPage` 复刻（Phase-2 / P2-W3）。
 *
 * 真源：`features/music/lib/view/now_playing_page.dart` + `widgets/music_album_art.dart` +
 * `widgets/music_blur_background.dart` + `widgets/music_control_button.dart`。
 *
 * **刻度**：Flutter Music 模块 view/widgets/theme **全用裸逻辑 px**（grep 无 `.w/.h/.sp`），
 * 故一律裸 `dp`/`sp`，**不要** `.su()`（判断法同 `MusicPalette` / `TabBarTokens` / `ChatPalette`）。
 *
 * ponytail 天花板（同 W2c 音乐口径，不弹回）：
 * - 无音频引擎 → `position` / `duration` 恒 0。Flutter `_PlayerControls` 自带判据
 *   `if (duration.inMilliseconds <= 0) return SizedBox.shrink()`，故 Slider + 时间行**按真源逻辑不渲染**；
 *   专辑图上叠的迷你进度条同理停在「全白」。
 * - 无图片/模糊滤镜加载器 → `MusicBlurBackground`（封面 cover + black@0.54 + blur10）
 *   用**占位色 → 黑**竖向渐变近似；250 方封面用占位色块 + ♪ 字形（同 W2c 列表口径）。
 */
@Page(name = "MusicNowPlaying", moduleId = "feature_home")
internal class MusicNowPlayingPage : BaseComposePager() {

    private var song by mutableStateOf(MusicPlaybackStore.currentSong)
    private var playing by mutableStateOf(MusicPlaybackStore.playing)
    private var muted by mutableStateOf(MusicPlaybackStore.muted)

    override fun pageDidAppear() {
        super.pageDidAppear()
        refresh()
    }

    private fun refresh() {
        song = MusicPlaybackStore.currentSong
        playing = MusicPlaybackStore.playing
        muted = MusicPlaybackStore.muted
    }

    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        refresh()
        setContent {
            val current = song
            // Flutter 空态分支**不裹** `musicDarkTheme`（`if (song == null) return Scaffold(...)`），
            // 故走宿主 `AppTheme.light`：白 AppBar / F2F2F7 背景 / 黑 bodyMedium 文本。
            val empty = current == null
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (empty) AppChrome.background else Color.Black),
            ) {
                AppNavBarBar(
                    title = "Now Playing",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = if (empty) AppChrome.surface else Color.Black,
                    foreground = if (empty) AppChrome.labelPrimary else Color.White,
                )
                if (empty) {
                    // Flutter `Scaffold(appBar: 'Now Playing', body: Center(Text('暂无播放歌曲')))`。
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "暂无播放歌曲",
                            fontSize = 14.sp,
                            color = AppChrome.labelPrimary,
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        MusicBlurBackdrop(current)
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            MusicAlbumArt(current)
                            // Flutter：`Padding(all: 20)` 直接裹 `_PlayerControls`（其自身无 padding），
                            // 专辑图→标题间隙即 20；不再叠 `Spacer(20)`（否则 40 双倍）。
                            PlayerControls(
                                song = current,
                                playing = playing,
                                muted = muted,
                                onToggle = {
                                    MusicPlaybackStore.toggle()
                                    refresh()
                                },
                                onPrevious = {
                                    MusicPlaybackStore.previous()
                                    refresh()
                                },
                                onNext = {
                                    MusicPlaybackStore.next()
                                    refresh()
                                },
                                onToggleMute = {
                                    MusicPlaybackStore.toggleMute()
                                    refresh()
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(bottom.dp))
            }
        }
    }
}

/** Flutter `MusicBlurBackground` 的无图近似：占位色 → 黑竖向渐变（模拟封面压暗 + blur）。 */
@Composable
private fun MusicBlurBackdrop(song: Song) {
    val tint = song.placeholder?.let { Color(it) } ?: Color(MUSIC_PLACEHOLDER_FALLBACK)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        tint.copy(alpha = 0.45f),
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black,
                    ),
                ),
            ),
    )
}

/**
 * Flutter `MusicAlbumArt`：250×250 方封面（r5）+ 底边叠 `LinearProgressIndicator`。
 *
 * ponytail: 省掉 `elasticOut` 弹性入场动画与 Hero（跨页共享元素）——Kuikly 无同名 primitive，
 * 静态 250 尺寸即最终帧。
 */
@Composable
private fun MusicAlbumArt(song: Song) {
    val cover = song.placeholder?.let { Color(it) } ?: Color(MUSIC_PLACEHOLDER_FALLBACK)
    Box(modifier = Modifier.size(250.dp)) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(cover, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("♪", fontSize = 64.sp, color = Color.White.copy(alpha = 0.9f))
        }
        // Flutter：满值 primary(white) 轨道 + onPrimary(black) 进度叠层。progress 恒 0 → 全白。
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White, RoundedCornerShape(5.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0f)
                    .height(4.dp)
                    .background(Color.Black),
            )
        }
    }
}

/**
 * Flutter `_PlayerControls`：标题（headlineSmall 24）+ 歌手（bodySmall 12 · onSurface@0.7）
 * + 20 间隙 + 上一首/播放暂停/下一首（`MusicControlButton`：IconButton iconSize 50）
 * + 静音钮（headset / headset_off，onSurface@0.7）；`Padding(all: 20)` 由外层 Material 施加。
 */
@Composable
private fun PlayerControls(
    song: Song,
    playing: Boolean,
    muted: Boolean,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleMute: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            song.title,
            fontSize = 24.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            song.artist,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicControlButton(glyph = "⏮", onClick = onPrevious)
            MusicControlButton(glyph = if (playing) "⏸" else "▶", onClick = onToggle)
            MusicControlButton(glyph = "⏭", onClick = onNext)
        }
        Spacer(Modifier.height(20.dp))
        // Flutter `IconButton(icon: headset / headset_off, color: onSurface@0.7)`。
        Box(
            modifier = Modifier
                .clickable(onClick = onToggleMute)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (muted) "🔇" else "🎧",
                fontSize = 24.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

/** Flutter `MusicControlButton`（`IconButton(iconSize: 50)` → 50 图标 + 8 内距）。 */
@Composable
private fun MusicControlButton(glyph: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 50.sp, color = Color.White)
    }
}
