package com.example.kuikly.data.analytics

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyticsRepositoryTest {

    @Test
    fun successReturnsSeed() {
        MockBackend.scenario = MockScenario.Success
        val result = AnalyticsStore.repo.fetchPage(1)
        assertTrue(result.isSuccess)
        val page = result.getOrThrow()
        assertEquals(8, page.total)
        assertEquals(8, page.items.size)
        assertEquals(1, page.items.first().id)
        // 默认 pageSize = 10 > seed 总数 8，第一页即全部，故无下一页。
        assertFalse(page.hasMore)
    }

    @Test
    fun pagingWithSmallerPageSizeHasMore() {
        MockBackend.scenario = MockScenario.Success
        val first = AnalyticsStore.repo.fetchPage(1, 4).getOrThrow()
        assertEquals(8, first.total)
        assertEquals(4, first.items.size)
        assertTrue(first.hasMore)
        val second = AnalyticsStore.repo.fetchPage(2, 4).getOrThrow()
        assertEquals(4, second.items.size)
        assertFalse(second.hasMore)
    }

    @Test
    fun emptyReturnsEmpty() {
        MockBackend.scenario = MockScenario.Empty
        val page = AnalyticsStore.repo.fetchPage(1).getOrThrow()
        assertEquals(0, page.total)
        assertTrue(page.items.isEmpty())
        assertFalse(page.hasMore)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun errorFails() {
        MockBackend.scenario = MockScenario.Error
        val result = AnalyticsStore.repo.fetchPage(1)
        assertTrue(result.isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun fetchByIdFindsRecord() {
        MockBackend.scenario = MockScenario.Success
        val rec = AnalyticsStore.repo.fetchById(3).getOrThrow()
        assertEquals("ANA-1003", rec.code)
        assertTrue(rec.conversionRate != null)
        assertEquals(rec.stickiness, 100 - (rec.metricBounceRate * 100).toInt())
    }

    @Test
    fun conversionRateFormula() {
        MockBackend.scenario = MockScenario.Success
        val rec = AnalyticsStore.repo.fetchById(1).getOrThrow()
        assertEquals(
            rec.metricConvert.toDouble() / rec.metricClick.toDouble(),
            rec.conversionRate!!,
            0.0001,
        )
    }

    @Test
    fun clickThroughRateFormula() {
        MockBackend.scenario = MockScenario.Success
        val rec = AnalyticsStore.repo.fetchById(2).getOrThrow()
        assertEquals(
            rec.metricClick.toDouble() / rec.metricPv.toDouble(),
            rec.clickThroughRate!!,
            0.0001,
        )
    }

    @Test
    fun profitAndMarginFormula() {
        MockBackend.scenario = MockScenario.Success
        val rec = AnalyticsStore.repo.fetchById(3).getOrThrow()
        assertEquals(rec.metricRevenue - rec.metricCost, rec.profit, 0.0001)
        assertEquals((rec.metricRevenue - rec.metricCost) / rec.metricRevenue, rec.profitMargin!!, 0.0001)
    }

    @Test
    fun priorityLabelMapping() {
        MockBackend.scenario = MockScenario.Success
        // Seed marks anomalous records as priority 1, healthy ones as priority 2.
        assertEquals("P0", AnalyticsStore.repo.fetchById(2).getOrThrow().priorityLabel)
        assertEquals("P1", AnalyticsStore.repo.fetchById(1).getOrThrow().priorityLabel)
    }

    @Test
    fun slowScenarioBehavesLikeSuccess() {
        MockBackend.scenario = MockScenario.Slow
        val page = AnalyticsStore.repo.fetchPage(1).getOrThrow()
        assertEquals(8, page.total)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun unknownIdFails() {
        MockBackend.scenario = MockScenario.Success
        assertTrue(AnalyticsStore.repo.fetchById(9999).isFailure)
    }

    @Test
    fun errorScenarioPropagatesToDetail() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(AnalyticsStore.repo.fetchById(1).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun formatUnixSecondsHandlesZeroAndEpoch() {
        assertEquals("-", formatUnixSeconds(0L))
        assertEquals("2023-11-14 22:13:20", formatUnixSeconds(1_700_000_000L))
    }
}
