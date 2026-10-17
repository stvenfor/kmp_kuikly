package com.example.kuikly.data.live

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 直播场次状态机（预告 / 直播中 / 回放）与预约。 */
class LiveSessionRepositoryTest {

    @Test
    fun `upcoming and replay split by phase`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveSessionRepository()
        assertEquals(2, repo.upcomingSessions().getOrThrow().size)
        assertEquals(2, repo.replaySessions().getOrThrow().size)
        assertTrue(repo.upcomingSessions().getOrThrow().all { it.phase == LivePhase.upcoming })
        assertTrue(repo.replaySessions().getOrThrow().all { it.phase == LivePhase.replay })
    }

    @Test
    fun `reserve toggles state and count`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveSessionRepository()
        val before = repo.upcomingSessions().getOrThrow().first { it.id == "s1" }
        val reserved = repo.toggleReserve("s1").getOrThrow()
        assertTrue(reserved.reserved)
        assertEquals(before.reserveCount + 1, reserved.reserveCount)
        assertEquals(1, repo.reservedSessions().getOrThrow().size)
        val canceled = repo.toggleReserve("s1").getOrThrow()
        assertFalse(canceled.reserved)
        assertEquals(before.reserveCount, canceled.reserveCount)
        assertTrue(repo.reservedSessions().getOrThrow().isEmpty())
    }

    @Test
    fun `only upcoming sessions are reservable`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveSessionRepository()
        assertTrue(repo.toggleReserve("s3").isFailure)
        assertTrue(repo.toggleReserve("s5").isFailure)
        assertTrue(repo.toggleReserve("missing").isFailure)
    }

    @Test
    fun `sessions grouped by room`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveSessionRepository()
        assertEquals(2, repo.sessionsOf("l1").getOrThrow().size)
        assertEquals(2, repo.sessionsOf("l2").getOrThrow().size)
        assertTrue(repo.sessionsOf("none").getOrThrow().isEmpty())
    }

    @Test
    fun `labels follow the compact formatter`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveSessionRepository()
        val replay = repo.replaySessions().getOrThrow().first { it.id == "s3" }
        assertEquals("2.3w", replay.viewerLabel)
        assertEquals("10-15 20:00", replay.scheduleLabel)
        assertEquals("回放", LivePhase.replay.label)
        assertEquals("直播中", LivePhase.live.label)
        assertEquals("预告", LivePhase.upcoming.label)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        val repo = LiveSessionRepository()
        assertTrue(repo.upcomingSessions().isFailure)
        assertTrue(repo.toggleReserve("s1").isSuccess)
        MockBackend.scenario = MockScenario.Success
    }
}
