package com.example.kuikly.data.live

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiveRepositoryTest {
    @Test
    fun `rooms success`() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(3, LiveRepository().page(1, 50).getOrThrow().total)
    }

    @Test
    fun `only live rooms`() {
        MockBackend.scenario = MockScenario.Success
        val live = LiveRepository().liveRooms().getOrThrow()
        assertEquals(2, live.size)
        assertTrue(live.all { it.status == LiveStatus.live })
    }

    @Test
    fun `offline rooms filtered out`() {
        MockBackend.scenario = MockScenario.Success
        val offline = LiveRepository().all { it.status == LiveStatus.offline }.getOrThrow()
        assertEquals(1, offline.size)
        assertEquals("l3", offline.first().id)
    }

    @Test
    fun `refresh viewers increases online count`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        val before = repo.fetchById("l1").getOrThrow().viewerCount
        val after = repo.refreshViewers("l1").getOrThrow()
        assertTrue(after.viewerCount > before)
        assertEquals(after.viewerCount, repo.fetchById("l1").getOrThrow().viewerCount)
        assertTrue(repo.refreshViewers("l1").getOrThrow().viewerCount > after.viewerCount)
        assertTrue(repo.refreshViewers("missing").isFailure)
    }

    @Test
    fun `offline room can refresh to zero or more`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        val after = repo.refreshViewers("l3").getOrThrow()
        assertTrue(after.viewerCount >= 0)
    }

    @Test
    fun `danmaku seeded and send prepends`() {
        val repo = LiveRepository()
        assertEquals(3, repo.danmakuOf("l1").getOrThrow().size)
        assertEquals(2, repo.danmakuOf("l2").getOrThrow().size)
        val msg = repo.sendDanmaku("l1", "666").getOrThrow()
        val after = repo.danmakuOf("l1").getOrThrow()
        assertEquals(4, after.size)
        assertEquals(msg.id, after.first().id)
        assertEquals("我", after.first().user)
        assertTrue(repo.sendDanmaku("l1", "   ").isFailure)
        assertTrue(repo.danmakuOf("no-room").getOrThrow().isEmpty())
    }

    @Test
    fun `danmaku keeps at most thirty messages`() {
        val repo = LiveRepository()
        repeat(35) { i -> repo.sendDanmaku("l2", "msg$i").getOrThrow() }
        val list = repo.danmakuOf("l2").getOrThrow()
        assertEquals(30, list.size)
        assertEquals("msg34", list.first().text)
    }

    @Test
    fun `viewer label compacts big numbers`() {
        MockBackend.scenario = MockScenario.Success
        val room = LiveRepository().fetchById("l1").getOrThrow()
        assertEquals("1.2w", room.viewerLabel)
        assertEquals("860", LiveRepository().fetchById("l2").getOrThrow().viewerLabel)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(LiveRepository().page(1, 50).isFailure)
        assertTrue(LiveRepository().fetchById("l1").isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
