package com.example.kuikly.data.mall

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MallRepositoryTest {
    @Test
    fun `products success`() {
        MockBackend.scenario = MockScenario.Success
        val r = MallRepository().page(1, 50).getOrThrow()
        assertEquals(3, r.total)
        assertTrue(r.items.any { it.points != null })
    }

    @Test
    fun `cart add accumulates`() {
        val repo = MallRepository()
        repo.addToCart("pd1", 2)
        repo.addToCart("pd1", 1)
        assertEquals(3, repo.cartCount())
    }

    @Test
    fun `place order computes total and clears cart`() {
        val repo = MallRepository()
        repo.addToCart("pd1", 1)   // 1299
        repo.addToCart("pd2", 1)   // 25
        val order = repo.placeOrder().getOrThrow()
        assertEquals(1324.0, order.amount, 0.001)
        assertTrue(order.isUnpaid)
        assertEquals(0, repo.cartCount())
    }

    @Test
    fun `place order fails on empty cart`() {
        assertTrue(MallRepository().placeOrder().isFailure)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(MallRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
