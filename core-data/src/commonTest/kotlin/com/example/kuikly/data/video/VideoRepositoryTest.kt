package com.example.kuikly.data.video

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoRepositoryTest {
    private lateinit var video: FakeVideoRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        video = FakeVideoRepository()
    }

    @AfterTest
    fun tearDown() {
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun success_has_at_least_three_videos() {
        val items = video.list().getOrThrow()
        assertTrue(items.size >= 3)
        assertTrue(items.all { it.title.isNotBlank() && it.author.isNotBlank() && it.summary.isNotBlank() })
        assertEquals("1", items.first().id)
    }

    @Test
    fun empty_scenario_returns_empty_list() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(video.list().getOrThrow().isEmpty())
    }

    @Test
    fun error_scenario_fails() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(video.list().isFailure)
        assertTrue(video.detail("1").isFailure)
    }

    @Test
    fun unauthorized_scenario_fails() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(video.list().isFailure)
        assertTrue(video.detail("1").isFailure)
    }

    @Test
    fun detail_returns_item_matching_id() {
        val first = video.list().getOrThrow().first()
        val detail = video.detail(first.id).getOrThrow()
        assertEquals(first.id, detail.id)
        assertEquals(first.title, detail.title)
        assertEquals(first.author, detail.author)
        assertEquals(first.summary, detail.summary)
    }

    @Test
    fun unknown_id_fails() {
        assertTrue(video.detail("no_such_id").isFailure)
    }
}
