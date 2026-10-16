package com.example.kuikly.data.analytics

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Covers Strategy / Ledger / UsedCar repositories added on top of the analytics skeleton. */
class AnalyticsBusinessDataTest {

    // ------------------------------------------------------------------ Strategy

    @Test
    fun strategyCellsSuccess() {
        MockBackend.scenario = MockScenario.Success
        val cells = StrategyStore.repo.cells().getOrThrow()
        assertEquals(9, cells.size)
        assertEquals("A股", cells.first().label)
        assertEquals("+19.22%", cells.first().valueLabel)
        assertEquals("-2.34%", cells.first { it.label == "原油" }.valueLabel)
    }

    @Test
    fun strategyPlansPaged() {
        MockBackend.scenario = MockScenario.Success
        val page = StrategyStore.repo.page(1, 50).getOrThrow()
        assertEquals(5, page.total)
        assertEquals(5, page.items.size)
        assertFalse(page.hasMore)
    }

    @Test
    fun strategyPlanDerivedFields() {
        MockBackend.scenario = MockScenario.Success
        val plan = StrategyStore.repo.byId("ST-2001").getOrThrow()
        assertEquals("36 / 50", plan.progressLabel)
        assertEquals(36.0 / 50.0, plan.progress, 0.0001)
        assertEquals("-11.35%", plan.returnLabel)
        assertFalse(plan.positiveReturn)
        assertEquals("恐慌", plan.gaugeLabel)
    }

    @Test
    fun strategyPlansOfKindFilters() {
        MockBackend.scenario = MockScenario.Success
        val reversePlans = StrategyStore.repo.plansOfKind("逆向").getOrThrow()
        assertEquals(1, reversePlans.size)
        assertEquals(2, StrategyStore.repo.plansOfKind("推荐").getOrThrow().size)
        assertTrue(StrategyStore.repo.plansOfKind("不存在的分类").getOrThrow().isEmpty())
    }

    @Test
    fun strategyEmptyScenario() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(StrategyStore.repo.cells().getOrThrow().isEmpty())
        assertTrue(StrategyStore.repo.page(1, 50).getOrThrow().isEmpty)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun strategyErrorScenario() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(StrategyStore.repo.cells().isFailure)
        assertTrue(StrategyStore.repo.page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    // ------------------------------------------------------------------- Ledger

    @Test
    fun ledgerPageAndSummary() {
        MockBackend.scenario = MockScenario.Success
        val page = AnalyticsLedgerStore.repo.page(1, 50).getOrThrow()
        assertEquals(12, page.total)
        val summary = AnalyticsLedgerStore.repo.summary().getOrThrow()
        assertEquals(12, summary.count)
        assertEquals(21066.80, summary.income, 0.001)
        assertEquals(8927.80, summary.expense, 0.001)
        assertEquals(12139.00, summary.balance, 0.001)
        assertEquals("12139.00", summary.balanceLabel)
    }

    @Test
    fun ledgerEntryDerivedFields() {
        MockBackend.scenario = MockScenario.Success
        val entry = AnalyticsLedgerStore.repo.byId("1").getOrThrow()
        assertFalse(entry.isIncome)
        assertEquals("支出", entry.typeLabel)
        assertEquals("-68.50", entry.amountLabel)
        assertEquals(-68.50, entry.signedAmount, 0.001)

        val income = AnalyticsLedgerStore.repo.byId("2").getOrThrow()
        assertTrue(income.isIncome)
        assertEquals("+18500.00", income.amountLabel)
        assertEquals(18500.00, income.signedAmount, 0.001)
    }

    @Test
    fun ledgerEntriesOfType() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(4, AnalyticsLedgerStore.repo.entriesOfType("income").getOrThrow().size)
        assertEquals(8, AnalyticsLedgerStore.repo.entriesOfType("expense").getOrThrow().size)
    }

    @Test
    fun ledgerEmptyScenario() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(AnalyticsLedgerStore.repo.page(1, 50).getOrThrow().isEmpty)
        assertEquals(0, AnalyticsLedgerStore.repo.summary().getOrThrow().count)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun ledgerErrorScenario() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(AnalyticsLedgerStore.repo.page(1, 50).isFailure)
        assertTrue(AnalyticsLedgerStore.repo.summary().isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    // ------------------------------------------------------------------ UsedCar

    @Test
    fun usedCarStats() {
        MockBackend.scenario = MockScenario.Success
        val page = UsedCarOrderStore.repo.page(1, 50).getOrThrow()
        assertEquals(6, page.total)
        val stats = UsedCarOrderStore.repo.stats().getOrThrow()
        assertEquals(6, stats.submitted)
        assertEquals(2, stats.pendingReview)
        assertEquals(3, stats.approved)
        assertEquals(1, stats.rejected)
    }

    @Test
    fun usedCarOrderDerivedFields() {
        MockBackend.scenario = MockScenario.Success
        val order = UsedCarOrderStore.repo.byId("UC-3001").getOrThrow()
        assertEquals("待审核", order.statusLabel)
        assertEquals("48000 km", order.mileageLabel)
        assertEquals("2020款", order.modelYearLabel)
        assertEquals("118000.00", order.amountYuanLabel)
        assertFalse(order.isRejected)

        val rejected = UsedCarOrderStore.repo.byId("UC-3004").getOrThrow()
        assertEquals("未通过", rejected.statusLabel)
        assertTrue(rejected.isRejected)
        assertTrue(rejected.rejectReason.isNotEmpty())
    }

    @Test
    fun usedCarOrdersOfStatus() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(6, UsedCarOrderStore.repo.ordersOfStatus("all").getOrThrow().size)
        assertEquals(2, UsedCarOrderStore.repo.ordersOfStatus("pending_review").getOrThrow().size)
        assertEquals(3, UsedCarOrderStore.repo.ordersOfStatus("approved").getOrThrow().size)
        assertEquals(1, UsedCarOrderStore.repo.ordersOfStatus("rejected").getOrThrow().size)
    }

    @Test
    fun usedCarEmptyScenario() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(UsedCarOrderStore.repo.page(1, 50).getOrThrow().isEmpty)
        assertEquals(0, UsedCarOrderStore.repo.stats().getOrThrow().submitted)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun usedCarErrorScenario() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(UsedCarOrderStore.repo.page(1, 50).isFailure)
        assertTrue(UsedCarOrderStore.repo.stats().isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
