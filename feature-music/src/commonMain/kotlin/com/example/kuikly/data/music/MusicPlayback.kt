package com.example.kuikly.data.music

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
const val MUSIC_PLACEHOLDER_FALLBACK = 0xFF616161L

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
