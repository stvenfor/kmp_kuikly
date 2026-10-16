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
}
