package com.example.kuikly.data.classroom

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassroomRepositoryTest {
    @Test
    fun `tasks and stats`() {
        MockBackend.scenario = MockScenario.Success
        val tasks = ClassroomRepository().tasks().getOrThrow()
        assertEquals(3, tasks.size)
        val stats = ClassroomRepository().stats().getOrThrow()
        assertEquals(3, stats.totalCount)
        assertTrue(stats.classCompletionRate > 0.0 && stats.classCompletionRate < 1.0)
    }

    @Test
    fun `complete task marks done`() {
        val t = ClassroomRepository().completeTask("h2").getOrThrow()
        assertTrue(t.isCompleted)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(ClassroomRepository().tasks().isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
