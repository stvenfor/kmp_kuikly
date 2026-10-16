package com.example.kuikly.data.ai

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiRepositoryTest {
    @Test
    fun `stream chunks reassemble to full reply`() {
        MockBackend.scenario = MockScenario.Success
        val sb = StringBuilder()
        val result = AiRepository().streamChunks("讲讲转化率") { sb.append(it) }.getOrThrow()
        assertEquals(result, sb.toString())
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `empty prompt fails`() {
        assertTrue(AiRepository().streamChunks("  ") {}.isFailure)
    }

    @Test
    fun `error scenario fails`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(AiRepository().streamChunks("hi") {}.isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
