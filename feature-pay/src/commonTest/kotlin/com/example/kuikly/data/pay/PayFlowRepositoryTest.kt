package com.example.kuikly.data.pay

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PayFlowRepositoryTest {

    private fun sku(id: String): MemberSku = MemberCatalog.skuOf(id)!!

    @Test
    fun `sku price calc`() {
        val sixMonth = sku("svip_6m")
        assertEquals(25.0, sixMonth.perMonth, 0.001)
        assertEquals(0.83, sixMonth.perDay, 0.001)
        assertEquals("每日仅需0.83元", sixMonth.dailyText)
        assertTrue(sixMonth.discount > 0.4)
        assertEquals(138.0, sixMonth.saved, 0.001)
    }

    @Test
    fun `quote applies deduction and red packet`() {
        val oneMonth = sku("svip_1m")
        val plain = PayQuote(sku = oneMonth)
        assertEquals(30.0, plain.payable, 0.001)

        val deducted = PayQuote(sku = oneMonth, useDeduction = true)
        assertEquals(8.4, deducted.deduction, 0.001)
        assertEquals(21.6, deducted.payable, 0.001)

        // 红包只对 showRedPacket 的套餐生效
        val year = sku("svip_12m")
        assertEquals(0.0, PayQuote(sku = oneMonth, useRedPacket = true).redPacket, 0.001)
        val full = PayQuote(sku = year, useDeduction = true, useRedPacket = true)
        assertEquals(8.0, full.redPacket, 0.001)
        assertEquals(263.6, full.payable, 0.001)
        assertEquals(224.4, full.saved, 0.001)
    }

    @Test
    fun `payable never goes negative`() {
        val cheap = MemberSku(
            id = "tiny", tier = PayTier.Svip, title = "体验", price = 2.0,
            originalPrice = 2.0, months = 1, showRedPacket = true,
        )
        val quote = PayQuote(sku = cheap, useDeduction = true, useRedPacket = true)
        assertEquals(0.0, quote.payable, 0.001)
    }

    @Test
    fun `channel availability by platform`() {
        assertEquals(3, PayChannels.available(PayPlatform.Android).size)
        assertTrue(PayChannels.available(PayPlatform.Ohos).contains(PayChannel.huawei))
        assertTrue(PayChannels.available(PayPlatform.Ios).contains(PayChannel.apple))
        assertTrue(PayChannel.huawei.requiresAgreement)
        assertFalse(PayChannel.wechat.requiresAgreement)
    }

    @Test
    fun `channel validation`() {
        val wallet = MemberCatalog.wallet
        assertNotNull(PayChannels.validate(PayChannel.huawei, 30.0, wallet))
        assertNull(PayChannels.validate(PayChannel.wechat, 30.0, wallet))
        assertNotNull(PayChannels.validate(PayChannel.balance, 999.0, wallet))
        assertNull(PayChannels.validate(PayChannel.balance, 30.0, wallet))
    }

    @Test
    fun `create order and pay success`() {
        MockBackend.scenario = MockScenario.Success
        val repo = PayOrderRepository()
        val quote = PayQuote(sku = sku("svip_12m"), useRedPacket = true)
        val created = repo.createOrder(quote, PayChannel.wechat, true).getOrThrow()
        assertEquals(PayOrderState.Created, created.state)
        assertEquals(272.0, created.amount, 0.001)

        val paid = repo.pay(created).getOrThrow()
        assertEquals(PayOrderState.Success, paid.state)
        assertTrue(paid.isSuccess)
        assertEquals("微信支付", paid.channel.label)
        // 新订单进入历史，可被订单列表读到
        assertTrue(repo.orders().getOrThrow().first().orderNo == paid.orderNo)
    }

    @Test
    fun `renewNow rejects when agreement missing`() {
        MockBackend.scenario = MockScenario.Success
        val repo = PayOrderRepository()
        val quote = PayQuote(sku = sku("ai_svip_12m"))
        val failure = repo.renewNow(
            quote = quote,
            channel = PayChannel.apple,
            autoRenew = true,
            agreed = false,
            platform = PayPlatform.Ios,
        )
        assertTrue(failure.isFailure)
        assertEquals("请先阅读并同意会员协议", failure.exceptionOrNull()?.message)

        val ok = repo.renewNow(
            quote = quote,
            channel = PayChannel.apple,
            autoRenew = true,
            agreed = true,
            platform = PayPlatform.Ios,
        )
        assertTrue(ok.getOrThrow().isSuccess)
    }

    @Test
    fun `slow scenario makes order timeout`() {
        MockBackend.scenario = MockScenario.Slow
        val repo = PayOrderRepository()
        val order = repo.createOrder(PayQuote(sku = sku("svip_1m")), PayChannel.alipay, false).getOrThrow()
        assertEquals(PayOrderState.Timeout, order.state)
        assertEquals("下单超时", order.state.label)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `slow payment is processing`() {
        MockBackend.scenario = MockScenario.Success
        val repo = PayOrderRepository()
        val order = repo.createOrder(PayQuote(sku = sku("svip_1m")), PayChannel.alipay, false).getOrThrow()
        MockBackend.scenario = MockScenario.Slow
        val paid = repo.pay(order).getOrThrow()
        assertEquals(PayOrderState.Processing, paid.state)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `error scenario fails payment`() {
        MockBackend.scenario = MockScenario.Success
        val repo = PayOrderRepository()
        val order = repo.createOrder(PayQuote(sku = sku("svip_1m")), PayChannel.alipay, false).getOrThrow()
        MockBackend.scenario = MockScenario.Error
        assertEquals(PayOrderState.Failed, repo.pay(order).getOrThrow().state)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `empty scenario cancels payment and empties list`() {
        MockBackend.scenario = MockScenario.Success
        val repo = PayOrderRepository()
        val order = repo.createOrder(PayQuote(sku = sku("svip_1m")), PayChannel.alipay, false).getOrThrow()
        MockBackend.scenario = MockScenario.Empty
        assertEquals(PayOrderState.Cancelled, repo.pay(order).getOrThrow().state)
        assertTrue(repo.orders().getOrThrow().isEmpty())
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `auto renew switch`() {
        MockBackend.scenario = MockScenario.Success
        val repo = PayOrderRepository()
        val order = repo.orders().getOrThrow().first { it.state == PayOrderState.Success }
        assertTrue(order.autoRenew)
        val off = repo.setAutoRenew(order.orderNo, false).getOrThrow()
        assertFalse(off.autoRenew)
        assertEquals("已关闭自动续费", off.message)
        assertFalse(repo.detail(order.orderNo).getOrThrow().autoRenew)
        assertTrue(repo.setAutoRenew("not-exist", true).isFailure)
    }

    @Test
    fun `entitlement from successful order`() {
        MockBackend.scenario = MockScenario.Success
        val repo = PayOrderRepository()
        val orders = repo.orders().getOrThrow()
        val svip = MemberCatalog.entitlement(PayTier.Svip, orders)
        assertTrue(svip.active)
        assertEquals("续费", svip.cta)
        assertEquals("2027-02-10", svip.expiresAt)
        assertEquals("微信支付", svip.sourceChannel)

        val none = MemberCatalog.entitlement(PayTier.Svip, emptyList())
        assertFalse(none.active)
        assertEquals("开通", none.cta)
    }

    @Test
    fun `expiry date rolls over year`() {
        assertEquals("2027-02-10", PayDates.addMonths("2026-02-10", 12))
        assertEquals("2026-04-18", PayDates.addMonths("2026-03-18", 1))
        assertEquals("2027-01-31", PayDates.addMonths("2026-01-31", 12))
        // 闰年 2 月：2028-02-29 + 12 个月 → 2029-02-28
        assertEquals("2029-02-28", PayDates.addMonths("2028-02-29", 12))
    }

    @Test
    fun `countdown formats hh mm ss`() {
        assertEquals("02:32:59", MemberCountdown.format(MemberCatalog.RED_PACKET_SECONDS))
        assertEquals("00:00:00", MemberCountdown.format(-5))
        assertEquals("01:02:03", MemberCountdown.format(3723))
    }
}
