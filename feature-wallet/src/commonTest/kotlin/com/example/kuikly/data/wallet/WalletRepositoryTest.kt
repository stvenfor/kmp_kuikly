package com.example.kuikly.data.wallet

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WalletRepositoryTest {
    @Test
    fun `recharge adds balance and ledger entry`() {
        MockBackend.scenario = MockScenario.Success
        val repo = WalletRepository()
        val before = repo.balanceFen()
        val after = repo.recharge(10000).getOrThrow()
        assertEquals(before + 10000, after)
        assertEquals(3, repo.page(1, 50).getOrThrow().total)
    }

    @Test
    fun `recharge rejects non positive`() {
        assertTrue(WalletRepository().recharge(0).isFailure)
    }

    @Test
    fun `delta yuan label`() {
        val e = LedgerEntry(1, -1234, 0, "x", "y")
        assertEquals("-¥12.34", e.deltaYuan)
        val p = LedgerEntry(1, 100, 0, "x", "y")
        assertEquals("+¥1.00", p.deltaYuan)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(WalletRepository().cards().isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
