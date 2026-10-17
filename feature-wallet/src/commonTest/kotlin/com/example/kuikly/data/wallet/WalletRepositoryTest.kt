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
        val result = repo.recharge(10000).getOrThrow()
        assertEquals(before + 10000, result.balanceFen)
        assertEquals(10000, result.deltaFen)
        assertEquals(9, repo.page(1, 50).getOrThrow().total)
    }

    @Test
    fun `recharge rejects non positive`() {
        assertTrue(WalletRepository().recharge(0).isFailure)
    }

    @Test
    fun `recharge bank channel requires valid card`() {
        val repo = WalletRepository()
        assertTrue(repo.recharge(1000, WalletChannel.BANK_CARD, null).isFailure)
        assertTrue(repo.recharge(1000, WalletChannel.BANK_CARD, 999).isFailure)
        assertTrue(repo.recharge(1000, WalletChannel.BANK_CARD, 2).isSuccess)
        assertTrue(repo.recharge(1000, 9).isFailure)
    }

    @Test
    fun `bind card validates bank name and last4`() {
        val repo = WalletRepository()
        assertTrue(repo.bindCard("", "1234").isFailure)
        assertTrue(repo.bindCard("招商银行", "12").isFailure)
        assertTrue(repo.bindCard("招商银行", "12ab").isFailure)
        val card = repo.bindCard("建设银行", "6688").getOrThrow()
        assertEquals(3, card.cardId)
        assertEquals("建设银行(****6688)", card.display)
        assertEquals(3, repo.cards().getOrThrow().size)
    }

    @Test
    fun `set default card moves flag`() {
        val repo = WalletRepository()
        repo.setDefaultCard(2).getOrThrow()
        val list = repo.cards().getOrThrow()
        assertTrue(list.first { it.cardId == 2 }.isDefault)
        assertTrue(list.none { it.cardId == 1 && it.isDefault })
        assertTrue(repo.setDefaultCard(999).isFailure)
    }

    @Test
    fun `delete card removes it`() {
        val repo = WalletRepository()
        repo.deleteCard(1).getOrThrow()
        assertEquals(1, repo.cards().getOrThrow().size)
        assertTrue(repo.deleteCard(1).isFailure)
    }

    @Test
    fun `summary carries balance and cards`() {
        val summary = WalletRepository().summary().getOrThrow()
        assertEquals(123456, summary.balanceFen)
        assertEquals("¥1234.56", summary.balance)
        assertEquals(2, summary.cards.size)
    }

    @Test
    fun `ledger filter splits income and expense`() {
        MockBackend.scenario = MockScenario.Success
        val repo = WalletRepository()
        val income = repo.page(1, 50) { it.deltaFen >= 0 }.getOrThrow()
        val expense = repo.page(1, 50) { it.deltaFen < 0 }.getOrThrow()
        assertTrue(income.items.isNotEmpty())
        assertTrue(expense.items.isNotEmpty())
        assertEquals(8, income.total + expense.total)
    }

    @Test
    fun `reason label maps source codes`() {
        assertEquals("充值", LedgerEntry(1, 1, 0, "recharge").reasonLabel)
        assertEquals("商城支付", LedgerEntry(1, -1, 0, "mall_pay").reasonLabel)
        assertEquals("商城退款入账", LedgerEntry(1, 1, 0, "mall_refund").reasonLabel)
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
        val repo = WalletRepository()
        assertTrue(repo.cards().isFailure)
        assertTrue(repo.summary().isFailure)
        assertTrue(repo.recharge(100).isFailure)
        assertTrue(repo.bindCard("招商银行", "1234").isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
