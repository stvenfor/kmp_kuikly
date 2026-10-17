package com.example.kuikly.data.music

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockScenario
import kotlin.random.Random

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

/**
 * 下载态（mock 三态）。源 Flutter music 模块没有下载能力，
 * 这里按任务要求补 mock：idle -> downloading(0..99) -> downloaded(100)。
 */
enum class DownloadStatus {
    idle,
    downloading,
    downloaded;

    val label: String
        get() = when (this) {
            idle -> "未下载"
            downloading -> "下载中"
            downloaded -> "已下载"
        }
}

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

    /** 播放队列：默认等于曲库全序（对应 Flutter `songs` 即队列），支持增删/排序。 */
    private val queue: MutableList<String> = tracks.map { it.id }.toMutableList()

    /** 收藏 trackId 集合（mock）。 */
    private val favorites = mutableSetOf<String>()

    /** 下载进度 trackId -> 0..100（mock，无定时器，由 UI 手动推进）。 */
    private val downloads = mutableMapOf<String, Int>()

    private var currentId: String? = null
    private var state: PlaybackState = PlaybackState.idle
    private var positionSec: Int = 0
    private var muted: Boolean = false

    override fun seed(): List<Track> = tracks
    override fun idOf(item: Track): String = item.id

    private fun currentTrack(): Track? = currentId?.let { id -> tracks.find { it.id == id } }

    fun play(id: String): Result<Track> {
        val t = tracks.find { it.id == id } ?: return Result.failure(NoSuchElementException("track=$id"))
        // 播放不在队列中的曲目时自动入队（保证 next/previous 有队列可走）。
        if (!queue.contains(id)) queue.add(id)
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

    /** Flutter `resume()`：暂停后继续播放；无会话时失败。 */
    fun resume(): Result<PlaybackState> {
        if (currentId == null) return Result.failure(IllegalStateException("暂无播放曲目"))
        state = PlaybackState.playing
        return Result.success(state)
    }

    /** Flutter `stop()`：停止播放并清空会话（stopped 对应 idle），位置归零。 */
    fun stop(): PlaybackState {
        state = PlaybackState.idle
        currentId = null
        positionSec = 0
        return state
    }

    /** Flutter `hasActiveSession`：有当前曲目且处于 playing/paused。 */
    fun hasActiveSession(): Boolean =
        currentId != null && (state == PlaybackState.playing || state == PlaybackState.paused)

    /** 下一首：按播放队列循环；无会话时从队列头开始（末位回到队首）。 */
    fun next(): Result<Track> {
        val ids = queue.toList()
        if (ids.isEmpty()) return Result.failure(IllegalStateException("播放队列为空"))
        val idx = ids.indexOf(currentId)
        val target = if (idx < 0) 0 else (idx + 1) % ids.size
        return play(ids[target])
    }

    /** 上一首：按播放队列循环；无会话/队首时回到队尾。 */
    fun previous(): Result<Track> {
        val ids = queue.toList()
        if (ids.isEmpty()) return Result.failure(IllegalStateException("播放队列为空"))
        val idx = ids.indexOf(currentId)
        val target = if (idx <= 0) ids.size - 1 else idx - 1
        return play(ids[target])
    }

    /** Flutter `shuffleAndPlay()`：随机一首播放（列表页 FAB shuffle）。 */
    fun shuffle(): Result<Track> {
        if (tracks.isEmpty()) return Result.failure(IllegalStateException("曲库为空"))
        return play(tracks[Random.nextInt(tracks.size)].id)
    }

    /** Flutter `_onComplete`：播放完成自动进入下一首（连播）。 */
    fun onComplete(): Result<Track> = next()

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

    // --- 播放队列管理（增删/排序；源 Flutter 无此能力，mock 化） ---

    fun queueIds(): List<String> = queue.toList()

    fun queueTracks(): List<Track> = queue.mapNotNull { id -> tracks.find { it.id == id } }

    fun queueSize(): Int = queue.size

    /** 追加到队尾；已在队列中则保持原位。 */
    fun enqueue(id: String): Result<Int> {
        if (tracks.none { it.id == id }) return Result.failure(NoSuchElementException("track=$id"))
        if (!queue.contains(id)) queue.add(id)
        return Result.success(queue.size)
    }

    /** 从队列移除；当前曲目被移除后仍继续播放，next/previous 按新队列走。 */
    fun dequeue(id: String): Result<Int> {
        if (!queue.remove(id)) return Result.failure(NoSuchElementException("track=$id"))
        return Result.success(queue.size)
    }

    /** 队列内排序（上移/下移），下标越界时裁剪到有效范围。 */
    fun moveInQueue(from: Int, to: Int): Result<List<Track>> {
        if (queue.isEmpty()) return Result.failure(IllegalStateException("播放队列为空"))
        val safeFrom = from.coerceIn(0, queue.size - 1)
        val safeTo = to.coerceIn(0, queue.size - 1)
        val moved = queue.removeAt(safeFrom)
        queue.add(safeTo, moved)
        return Result.success(queueTracks())
    }

    /** 重置为曲库全序。 */
    fun resetQueue(): List<Track> {
        queue.clear()
        queue.addAll(tracks.map { it.id })
        return queueTracks()
    }

    // --- 收藏（源 Flutter 无此能力，mock 化） ---

    /** 切换收藏态，返回切换后是否已收藏。 */
    fun toggleFavorite(id: String): Result<Boolean> {
        if (tracks.none { it.id == id }) return Result.failure(NoSuchElementException("track=$id"))
        if (!favorites.add(id)) {
            favorites.remove(id)
            return Result.success(false)
        }
        return Result.success(true)
    }

    fun isFavorite(id: String): Boolean = favorites.contains(id)

    fun favoriteTracks(): List<Track> = tracks.filter { favorites.contains(it.id) }

    fun favoriteCount(): Int = favorites.size

    // --- 下载态（源 Flutter 无此能力，mock 化） ---

    /** 开始下载（进度 0）；已下载完成的保持 100。 */
    fun startDownload(id: String): Result<Int> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        if (tracks.none { it.id == id }) return Result.failure(NoSuchElementException("track=$id"))
        if (downloadProgress(id) >= 100) return Result.success(100)
        downloads[id] = 0
        return Result.success(0)
    }

    /** 推进单个下载任务进度（封顶 100），返回推进后进度。 */
    fun advanceDownload(id: String, step: Int = 25): Int {
        val current = downloads[id] ?: return 0
        val next = (current + step.coerceAtLeast(0)).coerceAtMost(100)
        downloads[id] = next
        return next
    }

    /** 推进全部下载任务，返回进度快照。 */
    fun advanceDownloads(step: Int = 25): Map<String, Int> {
        downloads.keys.toList().forEach { id -> advanceDownload(id, step) }
        return downloads.toMap()
    }

    fun downloadSnapshot(): Map<String, Int> = downloads.toMap()

    fun downloadProgress(id: String): Int = downloads[id] ?: 0

    fun downloadStatus(id: String): DownloadStatus = when {
        !downloads.containsKey(id) -> DownloadStatus.idle
        (downloads[id] ?: 0) >= 100 -> DownloadStatus.downloaded
        else -> DownloadStatus.downloading
    }

    fun downloadedTracks(): List<Track> =
        tracks.filter { downloadStatus(it.id) == DownloadStatus.downloaded }

    /** 删除已下载内容，返回剩余下载任务数。 */
    fun removeDownload(id: String): Result<Int> {
        if (downloads.remove(id) == null) return Result.failure(NoSuchElementException("track=$id"))
        return Result.success(downloads.size)
    }

    // --- 搜索过滤（源 Flutter 无此能力，mock 化） ---

    /** 标题/歌手/专辑模糊匹配（忽略大小写）；空关键词返回空列表。 */
    fun search(query: String): Result<List<Track>> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        if (scenario == MockScenario.Empty) {
            return Result.success(emptyList())
        }
        val q = query.trim()
        if (q.isEmpty()) return Result.success(emptyList())
        return Result.success(
            tracks.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.artist.contains(q, ignoreCase = true) ||
                    it.album.contains(q, ignoreCase = true)
            },
        )
    }

    /** 搜索候选关键词（歌手/专辑优先，标题兜底），供无输入框的 chips UI 使用。 */
    fun searchSuggestions(limit: Int = 8): List<String> {
        val keywords = LinkedHashSet<String>()
        tracks.forEach { t ->
            keywords.add(t.artist)
            keywords.add(t.album)
        }
        tracks.forEach { t -> keywords.add(t.title) }
        return keywords.take(limit)
    }

    fun playlists(): Result<List<Playlist>> = Result.success(playlistSeed)

    fun tracksOf(playlistId: String): Result<List<Track>> =
        Result.success(tracks.filter { it.playlistId == playlistId })
}

object MusicStore {
    val repo: MusicRepository = MusicRepository()
}
