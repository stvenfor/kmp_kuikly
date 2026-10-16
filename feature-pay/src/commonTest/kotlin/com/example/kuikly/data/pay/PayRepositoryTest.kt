package com.example.kuikly.data.pay

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PayRepositoryTest {
    @Test
    fun `plans success`() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(3, PayRepository().plans().getOrThrow().size)
    }

    @Test
    fun `yearly plan discount`() {
        val y = PayRepository().plans().getOrThrow().first { it.id == "m3" }
        assertEquals(12, y.months)
        assertTrue(y.discount > 0.3)
    }

    @Test
    fun `create order and pay success`() {
        MockBackend.scenario = MockScenario.Success
        val orderNo = PayRepository().createOrder("m2").getOrThrow()
        val result = PayRepository().pay(orderNo, 68.0, PayMethod.alipay).getOrThrow()
        assertTrue(result.success)
        assertEquals(68.0, result.amount, 0.001)
    }

    @Test
    fun `pay fails under error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(PayRepository().pay("PAY-x", 25.0, PayMethod.wechat).isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
