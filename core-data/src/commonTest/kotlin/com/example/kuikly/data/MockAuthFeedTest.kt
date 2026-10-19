package com.example.kuikly.data

import com.example.kuikly.data.auth.FakeAuthRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockAuthFeedTest {
    private lateinit var auth: FakeAuthRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        auth = FakeAuthRepository()
    }

    @Test
    fun login_sets_session() {
        val user = auth.login("demo", "demo1234").getOrThrow()
        assertEquals("demo", user.name)
        assertEquals("demo", auth.currentUser()?.name)
    }

    @Test
    fun login_rejects_short_password() {
        assertTrue(auth.login("demo", "x").isFailure)
    }

    @Test
    fun cycle_scenario() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(MockScenario.Empty, MockBackend.cycle())
        assertEquals(MockScenario.Error, MockBackend.cycle())
    }
}
