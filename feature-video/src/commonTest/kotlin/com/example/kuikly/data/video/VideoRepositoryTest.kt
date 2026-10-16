package com.example.kuikly.data.video

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoRepositoryTest {
    @Test
    fun `videos success`() {
        MockBackend.scenario = MockScenario.Success
        val r = VideoRepository().page(1, 50).getOrThrow()
        assertEquals(5, r.total)
        assertTrue(r.items.any { it.status == ShortVideoStatus.published })
    }

    @Test
    fun `pagination splits seed into pages`() {
        MockBackend.scenario = MockScenario.Success
        val repo = VideoRepository()
        val p1 = repo.fetchPage(1, 2).getOrThrow()
        assertEquals(2, p1.items.size)
        assertEquals(5, p1.total)
        assertTrue(p1.hasMore)
        val p2 = repo.fetchPage(2, 2).getOrThrow()
        assertEquals(2, p2.items.size)
        assertTrue(p2.hasMore)
        val p3 = repo.fetchPage(3, 2).getOrThrow()
        assertEquals(1, p3.items.size)
        assertTrue(!p3.hasMore)
    }

    @Test
    fun `empty scenario yields no rows`() {
        MockBackend.scenario = MockScenario.Empty
        val r = VideoRepository().fetchPage(1, 2).getOrThrow()
        assertTrue(r.items.isEmpty())
        assertTrue(r.isEmpty)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `playback progress saved and clamped`() {
        MockBackend.scenario = MockScenario.Success
        val repo = VideoRepository()
        assertTrue(repo.progressOf("v1") == 0.0)
        repo.saveProgress("v1", 19).getOrThrow()
        assertEquals(19, repo.playedSecOf("v1"))
        val ratio = repo.progressOf("v1")
        assertTrue(ratio > 0.49 && ratio < 0.51)
        repo.saveProgress("v1", 9999).getOrThrow()
        assertEquals(repo.fetchById("v1").getOrThrow().durationSec, repo.playedSecOf("v1"))
        assertTrue(repo.progressOf("v1") == 1.0)
        repo.saveProgress("v1", -100).getOrThrow()
        assertEquals(0, repo.playedSecOf("v1"))
        assertTrue(repo.saveProgress("missing", 10).isFailure)
    }

    @Test
    fun `dubbing tasks returned`() {
        assertTrue(VideoRepository().dubbingTasks().getOrThrow().size == 2)
    }

    @Test
    fun `start dubbing then advance to done`() {
        val repo = VideoRepository()
        val before = repo.dubbingTasks().getOrThrow().size
        val task = repo.startDubbing("v1", "en").getOrThrow()
        assertEquals("pending", task.status)
        assertTrue(task.progress == 0.0)
        assertEquals(before + 1, repo.dubbingTasks().getOrThrow().size)
        assertTrue(repo.dubbingTasks("v1").getOrThrow().all { it.videoId == "v1" })
        val done = repo.advanceDubbing(task.id, 1.0).getOrThrow()
        assertEquals("done", done.status)
        assertTrue(repo.advanceDubbing("missing").isFailure)
        assertTrue(repo.startDubbing("missing", "en").isFailure)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(VideoRepository().page(1, 50).isFailure)
        assertTrue(VideoRepository().fetchById("v1").isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
