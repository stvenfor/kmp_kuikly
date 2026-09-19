package com.example.kuikly.data.live

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiveRepositoryTest {
    private lateinit var live: FakeLiveRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        live = FakeLiveRepository()
    }

    @Test
    fun success_has_at_least_three_rooms() {
        val rooms = live.list().getOrThrow()
        assertTrue(rooms.size >= 3)
        assertTrue(rooms.all { it.title.isNotBlank() && it.host.isNotBlank() })
        assertTrue(rooms.all { it.viewerCount >= 0 })
        assertEquals(rooms.size, rooms.map { it.id }.toSet().size)
    }

    @Test
    fun detail_returns_room_by_id() {
        val room = live.detail("1").getOrThrow()
        assertEquals("1", room.id)
        assertEquals("城市夜景慢直播", room.title)
        assertEquals("主播小夜", room.host)
    }

    @Test
    fun detail_unknown_id_fails() {
        assertTrue(live.detail("999").isFailure)
    }

    @Test
    fun empty_scenario_returns_empty_list() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(live.list().getOrThrow().isEmpty())
    }

    @Test
    fun error_scenario_fails() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(live.list().isFailure)
        assertTrue(live.detail("1").isFailure)
    }

    @Test
    fun unauthorized_scenario_fails() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(live.list().isFailure)
        assertTrue(live.detail("1").isFailure)
    }
}
