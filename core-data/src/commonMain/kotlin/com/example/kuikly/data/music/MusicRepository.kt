package com.example.kuikly.data.music

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    /**
     * Flutter `LocalSong.placeholderColor`（封面占位色）。Kuikly 无图片加载器，
     * 列表/迷你条一律渲染占位分支：`CircleAvatar(bg = placeholder ?: grey700)` + `play_arrow`。
     * null 表示 Flutter 侧走网络封面（`albumArtUrl`）——本仓降级为 `grey700` 占位。
     */
    val placeholder: Long? = null,
)

/** Flutter `Colors.grey.shade700` / `grey.shade800` 占位底色。 */
const val MUSIC_PLACEHOLDER_FALLBACK = 0xFF616161L

interface MusicRepository {
    fun list(): Result<List<Song>>
}

/**
 * Mock audio library, mirroring the Flutter mock_music_data seed
 * (title/artist + placeholderColor; no real audio engine in this slice).
 */
class FakeMusicRepository : MusicRepository {
    private val seed = listOf(
        Song("1", "Ya Ali - DJMaza.Com", "Zubeen", placeholder = null),
        Song("2", "Ek Do Teen - DJMaza.Info", "Parry G, Shreya Ghoshal", placeholder = 0xFF673AB7L),
        Song("3", "16 yeh dil diwana hai", "16 yeh dil diwana hai", placeholder = 0xFF3F51B5L),
        Song("4", "Shape of You", "Ed Sheeran", placeholder = null),
        Song("5", "Blinding Lights", "The Weeknd", placeholder = 0xFF2196F3L),
        Song("6", "Levitating", "Dua Lipa", placeholder = null),
        Song("7", "Stay", "The Kid LAROI & Justin Bieber", placeholder = 0xFF00BCD4L),
        Song("8", "Peaches", "Justin Bieber", placeholder = 0xFF009688L),
        Song("9", "Bad Habits", "Ed Sheeran", placeholder = null),
        Song("10", "Shivers", "Ed Sheeran", placeholder = 0xFF4CAF50L),
    )

    override fun list(): Result<List<Song>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock music error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }
}

object MusicStore {
    val repo: MusicRepository = FakeMusicRepository()
}

/**
 * Mock playback session shared across pages (MusicList 写入，Main / NowPlaying 读取).
 * UI + store state only — no real audio engine.
 *
 * 对齐 Flutter `MusicPlaybackController`（`features/music/lib/controller/music_playback_controller.dart`）
 * 的可观察状态子集：`currentSong` / `isPlaying` / `isMuted` / `playNext` / `playPrevious` /
 * `toggleMute` / `formatDuration`。**无** `position`/`duration`（无音频引擎 → 恒 0，
 * NowPlaying 的进度行与 Slider 按 Flutter 同款判据 `duration > 0` 隐藏）。
 */
object MusicPlaybackStore {
    var currentSong: Song? = null
        private set
    var playing: Boolean = false
        private set
    var muted: Boolean = false
        private set

    /** 播放队列（`MusicList` 点行时写入），供 `next()` / `previous()` 循环。 */
    private var queue: List<Song> = emptyList()

    val hasSession: Boolean get() = currentSong != null

    fun play(song: Song, songs: List<Song> = emptyList()) {
        if (songs.isNotEmpty()) queue = songs
        currentSong = song
        playing = true
    }

    /** Flutter `playNext`：末位回到 0。 */
    fun next() {
        val index = queue.indexOfFirst { it.id == currentSong?.id }
        if (index < 0) return
        play(queue[(index + 1) % queue.size])
    }

    /** Flutter `playPrevious`：首位回到末位。 */
    fun previous() {
        val index = queue.indexOfFirst { it.id == currentSong?.id }
        if (index < 0) return
        play(queue[(index - 1 + queue.size) % queue.size])
    }

    fun toggle() {
        if (currentSong != null) {
            playing = !playing
        }
    }

    fun toggleMute() {
        muted = !muted
    }

    fun clear() {
        currentSong = null
        playing = false
        muted = false
        queue = emptyList()
    }

    /** Flutter `MusicPlaybackController.formatDuration`（`m:ss`，超 1 小时 `h:mm:ss`）。 */
    fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        fun pad(value: Long) = if (value < 10) "0$value" else "$value"
        return if (hours > 0) "$hours:${pad(minutes)}:${pad(seconds)}" else "${pad(minutes)}:${pad(seconds)}"
    }
}
