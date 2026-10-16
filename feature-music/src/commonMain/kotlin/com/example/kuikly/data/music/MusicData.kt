package com.example.kuikly.data.music

import com.example.kuikly.data.BaseMockRepository

/** 曲目（port of source LocalSong）。 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val audioUrl: String,
    val durationSec: Int,
    val coverUrl: String,
    val playlistId: String = "pl1",
) {
    val durationLabel: String get() = "%d:%02d".format(durationSec / 60, durationSec % 60)
}

/** 歌单（port of source MusicMockData.songs 分组）。 */
data class Playlist(
    val id: String,
    val name: String,
    val trackIds: List<String>,
    val coverUrl: String = "",
) {
    val countLabel: String get() = "${trackIds.size} 首"
}

enum class PlaybackState { idle, playing, paused }

class MusicRepository : BaseMockRepository<Track>() {
    private val tracks = listOf(
        Track("t1", "晴天", "周杰伦", "叶惠美", "https://a/qingtian.mp3", 269, "https://picsum.photos/seed/t1/200", "pl1"),
        Track("t2", "海阔天空", "Beyond", "乐与怒", "https://a/haikuo.mp3", 326, "https://picsum.photos/seed/t2/200", "pl1"),
        Track("t3", "平凡之路", "朴树", "猎户星座", "https://a/pingfan.mp3", 302, "https://picsum.photos/seed/t3/200", "pl2"),
        Track("t4", "起风了", "买辣椒也用券", "起风了", "https://a/qifengle.mp3", 325, "https://picsum.photos/seed/t4/200", "pl2"),
        Track("t5", "夜空中最亮的星", "逃跑计划", "世界", "https://a/yekong.mp3", 252, "https://picsum.photos/seed/t5/200", "pl2"),
    )

    private val playlistSeed = listOf(
        Playlist("pl1", "华语精选", listOf("t1", "t2"), "https://picsum.photos/seed/pl1/200"),
        Playlist("pl2", "深夜民谣", listOf("t3", "t4", "t5"), "https://picsum.photos/seed/pl2/200"),
    )

    private var currentId: String? = null
    private var state: PlaybackState = PlaybackState.idle
    private var positionSec: Int = 0
    private var muted: Boolean = false

    override fun seed(): List<Track> = tracks
    override fun idOf(item: Track): String = item.id

    private fun indexOf(id: String?): Int = tracks.indexOfFirst { it.id == id }

    private fun currentTrack(): Track? = currentId?.let { id -> tracks.find { it.id == id } }

    fun play(id: String): Result<Track> {
        val t = tracks.find { it.id == id } ?: return Result.failure(NoSuchElementException("track=$id"))
        currentId = id
        state = PlaybackState.playing
        positionSec = 0
        return Result.success(t)
    }

    fun playAt(index: Int): Result<Track> {
        if (index < 0 || index >= tracks.size) {
            return Result.failure(IndexOutOfBoundsException("index=$index"))
        }
        return play(tracks[index].id)
    }

    fun pause(): PlaybackState {
        state = if (state == PlaybackState.playing) PlaybackState.paused else PlaybackState.idle
        return state
    }

    /** 播放/暂停切换：无当前曲目时自动从第一首开始。 */
    fun toggle(): PlaybackState {
        state = when (state) {
            PlaybackState.playing -> PlaybackState.paused
            PlaybackState.paused -> PlaybackState.playing
            PlaybackState.idle -> {
                if (currentId == null) currentId = tracks.firstOrNull()?.id
                PlaybackState.playing
            }
        }
        return state
    }

    /** 下一首：末尾循环回第一首。 */
    fun next(): Result<Track> {
        val idx = indexOf(currentId)
        val target = if (idx < 0) 0 else (idx + 1) % tracks.size
        return playAt(target)
    }

    /** 上一首：首曲循环到最后一首。 */
    fun previous(): Result<Track> {
        val idx = indexOf(currentId)
        val target = if (idx <= 0) tracks.size - 1 else idx - 1
        return playAt(target)
    }

    /** 跳转（秒），越界裁剪到 0..durationSec。 */
    fun seekTo(sec: Int): Result<Int> {
        val t = currentTrack() ?: return Result.failure(IllegalStateException("暂无播放曲目"))
        positionSec = sec.coerceIn(0, t.durationSec)
        return Result.success(positionSec)
    }

    fun seekRatio(ratio: Double): Result<Int> {
        val t = currentTrack() ?: return Result.failure(IllegalStateException("暂无播放曲目"))
        val target = (ratio.coerceIn(0.0, 1.0) * t.durationSec.toDouble()).toInt()
        return seekTo(target)
    }

    fun position(): Int = positionSec

    /** 当前播放进度 0..1，无曲目时为 0.0。 */
    fun progress(): Double {
        val t = currentTrack() ?: return 0.0
        if (t.durationSec <= 0) return 0.0
        return positionSec.toDouble() / t.durationSec.toDouble()
    }

    fun durationSec(): Int = currentTrack()?.durationSec ?: 0

    fun toggleMute(): Boolean {
        muted = !muted
        return muted
    }

    fun isMuted(): Boolean = muted

    fun playbackState(): Triple<PlaybackState, String?, Track?> {
        val t = currentId?.let { id -> tracks.find { it.id == id } }
        return Triple(state, currentId, t)
    }

    fun playlists(): Result<List<Playlist>> = Result.success(playlistSeed)

    fun tracksOf(playlistId: String): Result<List<Track>> =
        Result.success(tracks.filter { it.playlistId == playlistId })
}

object MusicStore {
    val repo: MusicRepository = MusicRepository()
}
