package com.example.kuikly.platform.chart

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartCatalogTest {
    @Test
    fun `catalog has all chart kinds`() {
        assertEquals(5, ChartCatalog.load().getOrThrow().size)
        assertTrue(ChartCatalog.specs.any { it.kind == ChartKind.funnel })
    }

    @Test
    fun `error scenario fails`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(ChartCatalog.load().isFailure)
        assertEquals("不可用", ChartCatalog.scenarioLabel())
        MockBackend.scenario = MockScenario.Success
    }
}
