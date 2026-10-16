package com.example.kuikly.data.settings

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsRepositoryTest {

    private fun newRepo(): SettingsRepository {
        MockBackend.scenario = MockScenario.Success
        return SettingsRepository()
    }

    private fun sampleAddress(id: String = "", name: String = "赵六", phone: String = "13800000009"): Address =
        Address(
            id = id,
            name = name,
            phone = phone,
            province = "浙江省",
            city = "杭州市",
            detail = "文三路1号",
            isDefault = false,
            district = "西湖区",
            postalCode = "310012",
            label = "家",
        )

    // ---------- 原有用例 ----------

    @Test
    fun `quote formula applies discount and freight`() {
        val q = SettingsRepository().quote(qty = 10, unitPrice = 100.0, discount = 0.1, freight = 20.0)
        assertEquals(900.0, q.subtotal, 0.001)
        assertEquals(920.0, q.total, 0.001)
    }

    @Test
    fun `addresses success returns seed`() {
        MockBackend.scenario = MockScenario.Success
        val r = SettingsRepository().page(1, 50)
        assertTrue(r.isSuccess)
        assertEquals(3, r.getOrThrow().total)
    }

    @Test
    fun `empty scenario yields no addresses`() {
        MockBackend.scenario = MockScenario.Empty
        val r = SettingsRepository().page(1, 50).getOrThrow()
        assertTrue(r.isEmpty)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `error scenario fails`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(SettingsRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    // ---------- 地址 CRUD ----------

    @Test
    fun `create address then read back`() {
        val repo = newRepo()
        val created = repo.addAddress(sampleAddress()).getOrThrow()
        assertTrue(created.id.isNotBlank())
        assertEquals(4, repo.page(1, 50).getOrThrow().total)
        assertEquals("赵六", repo.addressDetail(created.id).getOrThrow().name)
        assertEquals("浙江省 杭州市 西湖区 文三路1号", repo.addressDetail(created.id).getOrThrow().fullAddress)
    }

    @Test
    fun `create address rejects invalid phone`() {
        val repo = newRepo()
        assertTrue(repo.addAddress(sampleAddress(phone = "23800000009")).isFailure)
        assertTrue(repo.addAddress(sampleAddress(phone = "1380000")).isFailure)
        assertEquals(3, repo.page(1, 50).getOrThrow().total)
    }

    @Test
    fun `create address rejects blank receiver and detail`() {
        val repo = newRepo()
        assertTrue(repo.addAddress(sampleAddress(name = "  ")).isFailure)
        assertTrue(repo.addAddress(sampleAddress().copy(detail = "")).isFailure)
    }

    @Test
    fun `update address modifies existing record`() {
        val repo = newRepo()
        val updated = repo.updateAddress(
            repo.addressDetail("a2").getOrThrow().copy(name = "李四改名", detail = "世纪大道200号"),
        ).getOrThrow()
        assertEquals("李四改名", updated.name)
        assertEquals("李四改名", repo.addressDetail("a2").getOrThrow().name)
        assertEquals(3, repo.page(1, 50).getOrThrow().total)
    }

    @Test
    fun `update unknown address fails`() {
        val repo = newRepo()
        assertTrue(repo.updateAddress(sampleAddress(id = "not-exist")).isFailure)
    }

    @Test
    fun `delete address removes record`() {
        val repo = newRepo()
        assertTrue(repo.deleteAddress("a3").getOrThrow())
        assertEquals(2, repo.page(1, 50).getOrThrow().total)
        assertTrue(repo.addressDetail("a3").isFailure)
    }

    @Test
    fun `delete missing address fails`() {
        val repo = newRepo()
        assertTrue(repo.deleteAddress("nope").isFailure)
        assertTrue(repo.deleteAddress("").isFailure)
    }

    @Test
    fun `set default address is exclusive`() {
        val repo = newRepo()
        val updated = repo.setDefaultAddress("a2").getOrThrow()
        assertTrue(updated.isDefault)
        assertFalse(repo.addressDetail("a1").getOrThrow().isDefault)
        assertTrue(repo.addressDetail("a2").getOrThrow().isDefault)
    }

    @Test
    fun `deleting default address promotes another one`() {
        val repo = newRepo()
        repo.deleteAddress("a1")
        val rest = repo.page(1, 50).getOrThrow().items
        assertTrue(rest.isNotEmpty())
        assertTrue(rest.first().isDefault)
    }

    // ---------- 计算器 ----------

    @Test
    fun `monthly payment with zero rate splits principal`() {
        assertEquals(1000.0, PurchaseCalculator.monthlyPayment(12000.0, 0.0, 12), 0.001)
    }

    @Test
    fun `monthly payment with zero term is zero`() {
        assertEquals(0.0, PurchaseCalculator.monthlyPayment(12000.0, 4.75, 0), 0.001)
        assertEquals(0.0, PurchaseCalculator.monthlyPayment(-100.0, 4.75, 12), 0.001)
    }

    @Test
    fun `total interest never negative`() {
        assertEquals(0.0, PurchaseCalculator.totalInterest(0.0, 0, 1000.0), 0.001)
        assertTrue(PurchaseCalculator.totalInterest(100.0, 12, 1200.0) >= 0.0)
    }

    @Test
    fun `purchase tax guards non positive`() {
        assertEquals(0.0, PurchaseCalculator.purchaseTax(0.0), 0.001)
        assertEquals(0.0, PurchaseCalculator.purchaseTax(-100.0), 0.001)
        assertTrue(PurchaseCalculator.purchaseTax(113000.0) > 0.0)
    }

    @Test
    fun `cash quote rejects non positive bare price`() {
        val repo = newRepo()
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_CASH, 0.0, 0.0).isFailure)
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_CASH, -1.0, 0.0).isFailure)
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_CASH, 100000.0, -1.0).isFailure)
    }

    @Test
    fun `cash quote sums totals`() {
        val repo = newRepo()
        val quote = repo.calcQuote(PurchaseCalculator.MODE_CASH, 100000.0, 0.0, productId = 1).getOrThrow()
        assertEquals(100000.0, quote.barePrice, 0.001)
        assertTrue(quote.lines.any { it.code == "tax" })
        assertTrue(quote.totalDue >= quote.barePrice)
        assertEquals(quote.initialPayment, quote.totalDue, 0.001)
    }

    @Test
    fun `loan quote computes monthly and rejects low down payment`() {
        val repo = newRepo()
        // 产品 3 要求最低首付 50%，3 万首付应被拒绝
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_LOAN, 100000.0, 0.0, 30000.0, 24, 3).isFailure)
        val quote = repo.calcQuote(PurchaseCalculator.MODE_LOAN, 100000.0, 0.0, 60000.0, 24, 1).getOrThrow()
        assertEquals(40000.0, quote.loanAmount, 0.001)
        assertTrue(quote.monthlyPayment > 0.0)
        assertTrue(quote.totalInterest >= 0.0)
        assertEquals(quote.monthlyPayment * 24, quote.totalRepayment, 0.001)
        assertTrue(quote.totalDue > quote.initialPayment)
    }

    @Test
    fun `loan quote rejects invalid term and down payment`() {
        val repo = newRepo()
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_LOAN, 100000.0, 0.0, 60000.0, 0, 1).isFailure)
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_LOAN, 100000.0, 0.0, -1.0, 24, 1).isFailure)
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_LOAN, 100000.0, 0.0, 100000.0, 24, 1).isFailure)
        assertTrue(repo.calcQuote(PurchaseCalculator.MODE_LOAN, 100000.0, 0.0, 60000.0, 24, 99).isFailure)
    }

    // ---------- 发票 ----------

    @Test
    fun `invoices filter by status`() {
        val repo = newRepo()
        assertEquals(4, repo.invoices().getOrThrow().size)
        assertEquals(1, repo.invoices(InvoiceStatus.Pending).getOrThrow().size)
        assertEquals(2, repo.invoices(InvoiceStatus.Approved).getOrThrow().size)
        assertEquals(1, repo.invoices(InvoiceStatus.Rejected).getOrThrow().size)
    }

    @Test
    fun `invoice stats count all buckets`() {
        val repo = newRepo()
        val stats = repo.invoiceStats().getOrThrow()
        assertEquals(4, stats.uploaded)
        assertEquals(1, stats.pending)
        assertEquals(2, stats.approved)
        assertEquals(1, stats.rejected)
    }

    @Test
    fun `resubmit rejected invoice moves to pending`() {
        val repo = newRepo()
        val updated = repo.resubmitInvoice("i3").getOrThrow()
        assertEquals(InvoiceStatus.Pending, updated.status)
        assertTrue(updated.rejectReason.isEmpty())
        assertTrue(repo.resubmitInvoice("nope").isFailure)
    }

    // ---------- 个性化设置 ----------

    @Test
    fun `personalized switch toggles value`() {
        val repo = newRepo()
        assertTrue(repo.personalized().getOrThrow().first { it.key == "push" }.enabled)
        assertFalse(repo.setPersonalized("push", false).getOrThrow().enabled)
        assertFalse(repo.personalized().getOrThrow().first { it.key == "push" }.enabled)
        assertTrue(repo.setPersonalized("not-exist", true).isFailure)
    }

    @Test
    fun `personalized option value updates`() {
        val repo = newRepo()
        val updated = repo.setPersonalizedValue("eye", "跟随系统").getOrThrow()
        assertEquals("跟随系统", updated.value)
        assertTrue(updated.enabled)
        assertFalse(repo.setPersonalizedValue("eye", "关闭").getOrThrow().enabled)
    }

    // ---------- 校验规则 ----------

    @Test
    fun `address rules validate phone and name`() {
        assertTrue(AddressRules.isValidPhone("13800000000"))
        assertFalse(AddressRules.isValidPhone("12800000000"))
        assertFalse(AddressRules.isValidPhone("1380000000"))
        assertTrue(AddressRules.isValidName("张三"))
        assertFalse(AddressRules.isValidName(""))
        assertNull(AddressRules.validate(sampleAddress()))
        assertNotNull(AddressRules.validate(sampleAddress().copy(detail = "")))
    }
}
