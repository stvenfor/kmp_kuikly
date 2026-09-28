package com.example.kuikly.data.pay

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PayRepositoryTest {
    private lateinit var pay: FakePayRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        pay = FakePayRepository()
    }

    @Test
    fun success_has_at_least_three_plans() {
        val plans = pay.list().getOrThrow()
        assertTrue(plans.size >= 3)
        assertTrue(plans.all { it.title.isNotBlank() && it.priceLabel.isNotBlank() && it.desc.isNotBlank() })
        assertEquals(plans.size, plans.map { it.id }.toSet().size)
    }

    @Test
    fun detail_returns_plan_by_id() {
        val first = pay.list().getOrThrow().first()
        val plan = pay.detail(first.id).getOrThrow()
        assertEquals(first, plan)
    }

    @Test
    fun detail_unknown_id_fails() {
        assertTrue(pay.detail("999").isFailure)
    }

    @Test
    fun empty_scenario_returns_empty_list() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(pay.list().getOrThrow().isEmpty())
    }

    @Test
    fun empty_scenario_detail_fails() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(pay.detail("1").isFailure)
    }

    @Test
    fun error_scenario_fails() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(pay.list().isFailure)
        assertTrue(pay.detail("1").isFailure)
    }
}
