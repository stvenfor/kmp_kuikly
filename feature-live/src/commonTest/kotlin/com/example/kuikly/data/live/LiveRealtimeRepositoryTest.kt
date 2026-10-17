package com.example.kuikly.data.live

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 源 `LiveRoomController`（features/live/lib/live/view/live_room_page.dart）的等价仓库：
 * 订阅 topic、连接态、后台保活、join 信令与 30 条上限。
 */
class LiveRealtimeRepositoryTest {

    @Test
    fun `enter room subscribes both topics`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRealtimeRepository()
        repo.enterRoom("mock_room_001")
        assertEquals(LiveConnectionState.connected, repo.connection)
        assertTrue(repo.keepAliveInBackground)
        assertEquals(
            listOf("live.room.mock_room_001.state", "live.signal.mock_room_001"),
            repo.subscribedTopics,
        )
        assertTrue(repo.subscribedTopics.contains(LiveTopics.signal("mock_room_001")))
        assertTrue(repo.signalsOf("mock_room_001").isEmpty())
    }

    @Test
    fun `room state arrives on the state topic`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRealtimeRepository()
        repo.enterRoom("r1")
        val state = repo.receiveRoomState("r1").getOrThrow()
        assertEquals(LiveSignalKind.ROOM_STATE, state.kind)
        assertEquals(LiveTopics.roomState("r1"), state.topic)
        assertEquals("[state] room.state seq=${state.seq}", state.line)
    }

    @Test
    fun `join signal follows source line format`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRealtimeRepository()
        repo.enterRoom("r1")
        val signal = repo.sendJoinSignal("r1").getOrThrow()
        assertEquals("live.join", signal.eventName)
        assertEquals(LiveSignalKind.SIGNAL, signal.kind)
        assertEquals(LiveTopics.signal("r1"), signal.topic)
        assertEquals("[signal] live.join seq=${signal.seq} {\"roomId\":\"r1\"}", signal.line)
        assertEquals(signal, repo.signalsOf("r1").first())
    }

    @Test
    fun `leave room unsubscribes then blocks sending`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRealtimeRepository()
        repo.enterRoom("r1")
        repo.leaveRoom("r1")
        assertTrue(repo.subscribedTopics.isEmpty())
        assertFalse(repo.keepAliveInBackground)
        assertEquals(LiveConnectionState.disconnected, repo.connection)
        assertTrue(repo.sendJoinSignal("r1").isFailure)
    }

    @Test
    fun `keep alive can be toggled`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRealtimeRepository()
        repo.enterRoom("r1")
        repo.setKeepAliveInBackground(false)
        assertFalse(repo.keepAliveInBackground)
        repo.setKeepAliveInBackground(true)
        assertTrue(repo.keepAliveInBackground)
    }

    @Test
    fun `signals keep at most thirty entries`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRealtimeRepository()
        repo.enterRoom("r1")
        repeat(40) { repo.sendJoinSignal("r1").getOrThrow() }
        assertEquals(30, repo.signalsOf("r1").size)
    }

    @Test
    fun `error scenario fails the connection`() {
        MockBackend.scenario = MockScenario.Error
        val repo = LiveRealtimeRepository()
        repo.enterRoom("r1")
        assertEquals(LiveConnectionState.failed, repo.connection)
        assertTrue(repo.sendJoinSignal("r1").isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
