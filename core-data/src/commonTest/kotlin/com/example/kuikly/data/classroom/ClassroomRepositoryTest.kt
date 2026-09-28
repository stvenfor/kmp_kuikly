package com.example.kuikly.data.classroom

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassroomRepositoryTest {
    private lateinit var classroom: FakeClassroomRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        classroom = FakeClassroomRepository()
    }

    @Test
    fun success_has_at_least_three_courses() {
        val courses = classroom.list().getOrThrow()
        assertTrue(courses.size >= 3)
        assertTrue(courses.all { it.title.isNotBlank() && it.teacher.isNotBlank() && it.summary.isNotBlank() })
        assertEquals(courses.size, courses.map { it.id }.toSet().size)
    }

    @Test
    fun success_detail_returns_matching_course() {
        val course = classroom.list().getOrThrow().first()
        val detail = classroom.detail(course.id).getOrThrow()
        assertEquals(course, detail)
    }

    @Test
    fun detail_unknown_id_fails() {
        assertTrue(classroom.detail("course_none").isFailure)
    }

    @Test
    fun empty_scenario_returns_empty_list() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(classroom.list().getOrThrow().isEmpty())
    }

    @Test
    fun error_scenario_fails() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(classroom.list().isFailure)
    }

    @Test
    fun unauthorized_scenario_fails() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(classroom.list().isFailure)
    }
}
