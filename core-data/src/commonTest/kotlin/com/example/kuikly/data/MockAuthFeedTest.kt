package com.example.kuikly.data

import com.example.kuikly.data.auth.FakeAuthRepository
import com.example.kuikly.data.feed.FakeFeedRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockAuthFeedTest {
    private lateinit var auth: FakeAuthRepository
    private lateinit var feed: FakeFeedRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        auth = FakeAuthRepository()
        feed = FakeFeedRepository()
    }

    @Test
    fun login_sets_session() {
        val user = auth.login("demo", "x").getOrThrow()
        assertEquals("demo", user.name)
        assertEquals("demo", auth.currentUser()?.name)
    }

    @Test
    fun feed_success_has_items() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(3, feed.list().getOrThrow().size)
    }

    @Test
    fun feed_empty() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(feed.list().getOrThrow().isEmpty())
    }

    @Test
    fun feed_error() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(feed.list().isFailure)
    }

    @Test
    fun cycle_scenario() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(MockScenario.Empty, MockBackend.cycle())
        assertEquals(MockScenario.Error, MockBackend.cycle())
    }
}
