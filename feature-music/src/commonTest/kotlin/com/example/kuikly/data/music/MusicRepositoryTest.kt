package com.example.kuikly.data.music

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MusicRepositoryTest {
    @Test
    fun `tracks success`() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(5, MusicRepository().page(1, 50).getOrThrow().total)
    }

    @Test
    fun `play then pause toggles state`() {
        val repo = MusicRepository()
        val t = repo.play("t1").getOrThrow()
        assertEquals("t1", t.id)
        assertEquals(PlaybackState.playing, repo.playbackState().first)
        assertEquals(PlaybackState.paused, repo.pause())
    }

    @Test
    fun `toggle switches between playing and paused`() {
        val repo = MusicRepository()
        repo.play("t2")
        // play() 后已是 playing，toggle 第一次应转为 paused，第二次再回到 playing。
        assertEquals(PlaybackState.paused, repo.toggle())
        assertEquals(PlaybackState.paused, repo.playbackState().first)
        assertEquals(PlaybackState.playing, repo.toggle())
        assertEquals("t2", repo.playbackState().second)
    }

    @Test
    fun `next loops back to first at the end`() {
        val repo = MusicRepository()
        repo.playAt(4).getOrThrow()
        assertEquals("t5", repo.playbackState().second)
        assertEquals("t1", repo.next().getOrThrow().id)
        assertEquals("t2", repo.next().getOrThrow().id)
    }

    @Test
    fun `previous loops to last at the beginning`() {
        val repo = MusicRepository()
        repo.playAt(0).getOrThrow()
        assertEquals("t5", repo.previous().getOrThrow().id)
        assertEquals("t4", repo.previous().getOrThrow().id)
    }

    @Test
    fun `next and previous on empty session never crash`() {
        val repo = MusicRepository()
        assertEquals("t1", repo.next().getOrThrow().id)
        assertEquals("t5", repo.previous().getOrThrow().id)
        assertTrue(repo.playAt(-1).isFailure)
        assertTrue(repo.playAt(99).isFailure)
    }

    @Test
    fun `seek clamps into range`() {
        val repo = MusicRepository()
        repo.play("t1")
        assertEquals(30, repo.seekTo(30).getOrThrow())
        assertEquals(0, repo.seekTo(-100).getOrThrow())
        assertEquals(269, repo.seekTo(99999).getOrThrow())
        assertTrue(repo.progress() > 0.99)
        assertTrue(repo.seekRatio(0.5).getOrThrow() in 1..268)
        assertTrue(repo.seekTo(10).isSuccess)
        assertTrue(MusicRepository().progress() == 0.0)
    }

    @Test
    fun `mute toggles`() {
        val repo = MusicRepository()
        assertTrue(!repo.isMuted())
        assertTrue(repo.toggleMute())
        assertTrue(!repo.toggleMute())
    }

    @Test
    fun `playlists expose grouped tracks`() {
        val repo = MusicRepository()
        val playlists = repo.playlists().getOrThrow()
        assertEquals(2, playlists.size)
        assertEquals(2, repo.tracksOf("pl1").getOrThrow().size)
        assertEquals(3, repo.tracksOf("pl2").getOrThrow().size)
        assertTrue(repo.tracksOf("pl-none").getOrThrow().isEmpty())
    }

    @Test
    fun `duration label format`() {
        // seed() 在基类中是 protected，改用公开的 byId() 取值。
        val track = MusicRepository().byId("t1").getOrThrow()
        assertEquals("4:29", track.durationLabel)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(MusicRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `resume continues paused session and stop clears it`() {
        val repo = MusicRepository()
        repo.play("t1")
        repo.pause()
        assertEquals(PlaybackState.playing, repo.resume().getOrThrow())
        assertTrue(repo.hasActiveSession())
        assertEquals(PlaybackState.idle, repo.stop())
        assertTrue(!repo.hasActiveSession())
        assertTrue(repo.resume().isFailure)
        // stop 后位置归零。
        repo.play("t2")
        repo.seekTo(30)
        repo.stop()
        assertEquals(0, repo.position())
    }

    @Test
    fun `shuffle plays a track from the library`() {
        val repo = MusicRepository()
        val ids = repo.all().getOrThrow().map { it.id }.toSet()
        repeat(8) {
            val t = repo.shuffle().getOrThrow()
            assertTrue(ids.contains(t.id))
            assertEquals(PlaybackState.playing, repo.playbackState().first)
        }
    }

    @Test
    fun `complete auto advances to next track`() {
        val repo = MusicRepository()
        repo.play("t1")
        assertEquals("t2", repo.onComplete().getOrThrow().id)
        repo.playAt(4)
        // 末尾播放完成循环回第一首。
        assertEquals("t1", repo.onComplete().getOrThrow().id)
    }

    @Test
    fun `favorites toggle and list`() {
        val repo = MusicRepository()
        assertTrue(repo.toggleFavorite("t3").getOrThrow())
        assertTrue(repo.isFavorite("t3"))
        assertEquals(1, repo.favoriteCount())
        assertEquals(listOf("t3"), repo.favoriteTracks().map { it.id })
        assertTrue(!repo.toggleFavorite("t3").getOrThrow())
        assertTrue(repo.favoriteTracks().isEmpty())
        assertTrue(repo.toggleFavorite("nope").isFailure)
    }

    @Test
    fun `download mock progresses to completed`() {
        val repo = MusicRepository()
        assertEquals(DownloadStatus.idle, repo.downloadStatus("t1"))
        assertEquals(0, repo.startDownload("t1").getOrThrow())
        assertEquals(DownloadStatus.downloading, repo.downloadStatus("t1"))
        repo.advanceDownload("t1", 100)
        assertEquals(DownloadStatus.downloaded, repo.downloadStatus("t1"))
        assertEquals(100, repo.downloadProgress("t1"))
        assertEquals(listOf("t1"), repo.downloadedTracks().map { it.id })
        assertEquals(0, repo.removeDownload("t1").getOrThrow())
        assertEquals(DownloadStatus.idle, repo.downloadStatus("t1"))
        assertTrue(repo.removeDownload("t1").isFailure)
        assertTrue(repo.startDownload("nope").isFailure)
    }

    @Test
    fun `advance all downloads caps at 100`() {
        val repo = MusicRepository()
        repo.startDownload("t1")
        repo.startDownload("t2")
        repeat(5) { repo.advanceDownloads(25) }
        assertEquals(100, repo.downloadProgress("t1"))
        assertEquals(100, repo.downloadProgress("t2"))
        assertEquals(2, repo.downloadSnapshot().size)
    }

    @Test
    fun `queue enqueue dequeue reorder reset`() {
        val repo = MusicRepository()
        assertEquals(listOf("t1", "t2", "t3", "t4", "t5"), repo.queueIds())
        // 已在队列中不重复入队。
        assertEquals(5, repo.enqueue("t1").getOrThrow())
        assertEquals(4, repo.dequeue("t3").getOrThrow())
        assertEquals(listOf("t1", "t2", "t4", "t5"), repo.queueIds())
        assertEquals(5, repo.enqueue("t3").getOrThrow())
        assertEquals(listOf("t1", "t2", "t4", "t5", "t3"), repo.queueIds())
        // 排序：队尾移到队首；越界下标裁剪到队尾。
        assertEquals(listOf("t3", "t1", "t2", "t4", "t5"), repo.moveInQueue(4, 0).getOrThrow().map { it.id })
        assertEquals(listOf("t1", "t2", "t4", "t5", "t3"), repo.moveInQueue(0, 99).getOrThrow().map { it.id })
        // 重置回曲库全序。
        assertEquals(listOf("t1", "t2", "t3", "t4", "t5"), repo.resetQueue().map { it.id })
        assertTrue(repo.dequeue("t1").isSuccess)
        assertTrue(repo.dequeue("t1").isFailure)
        assertTrue(repo.enqueue("nope").isFailure)
    }

    @Test
    fun `next and previous follow the queue after removal`() {
        val repo = MusicRepository()
        repo.play("t2")
        repo.dequeue("t3")
        // t2 的下一首跳过被移除的 t3。
        assertEquals("t4", repo.next().getOrThrow().id)
        assertEquals("t2", repo.previous().getOrThrow().id)
        // 播放不在队列中的曲目会自动入队。
        repo.play("t3")
        assertEquals(listOf("t1", "t2", "t4", "t5", "t3"), repo.queueIds())
        // 移除当前曲目后 next 落到队首。
        repo.dequeue("t3")
        repo.play("t5")
        assertEquals("t1", repo.next().getOrThrow().id)
    }

    @Test
    fun `search filters by title artist album`() {
        val repo = MusicRepository()
        assertEquals(listOf("t1"), repo.search("周杰伦").getOrThrow().map { it.id })
        // 忽略大小写。
        assertEquals(listOf("t2"), repo.search("beyond").getOrThrow().map { it.id })
        // 专辑命中。
        assertEquals(listOf("t5"), repo.search("世界").getOrThrow().map { it.id })
        assertTrue(repo.search("").getOrThrow().isEmpty())
        assertTrue(repo.search("不存在的歌").getOrThrow().isEmpty())
        assertTrue(repo.searchSuggestions().isNotEmpty())

        MockBackend.scenario = MockScenario.Error
        assertTrue(repo.search("晴天").isFailure)
        MockBackend.scenario = MockScenario.Empty
        assertTrue(repo.search("晴天").getOrThrow().isEmpty())
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `start download fails on error scenario`() {
        val repo = MusicRepository()
        MockBackend.scenario = MockScenario.Error
        assertTrue(repo.startDownload("t1").isFailure)
        MockBackend.scenario = MockScenario.Success
        assertTrue(repo.startDownload("t1").isSuccess)
    }
}
