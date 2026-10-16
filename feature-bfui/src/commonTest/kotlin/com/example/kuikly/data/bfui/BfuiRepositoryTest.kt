package com.example.kuikly.data.bfui

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BfuiRepositoryTest {
    @Test
    fun `showcase list success`() {
        MockBackend.scenario = MockScenario.Success
        val r = BfuiRepository().page(1, 50).getOrThrow()
        assertEquals(10, r.total)
        assertTrue(r.items.any { it.id == "bottom_nav" })
    }

    @Test
    fun `empty and error`() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(BfuiRepository().page(1, 50).getOrThrow().isEmpty)
        MockBackend.scenario = MockScenario.Error
        assertTrue(BfuiRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
