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

    @Test
    fun `cart select all computes selected subtotal`() {
        val repo = MallRepository()
        repo.addToCart("pd1", 1)   // 1299
        repo.addToCart("pd2", 2)   // 25 * 2 = 50
        repo.selectAll(true)
        // selectedCount 统计「件数」（数量之和），对齐 UI「已选 N 件」：1 + 2 = 3
        assertEquals(3, repo.selectedCount())
        assertEquals(2, repo.selectedLines().size)
        assertEquals(1349.0, repo.selectedSubtotal(), 0.001)
        repo.selectAll(false)
        assertEquals(0, repo.selectedCount())
    }

    @Test
    fun `place selected order keeps unselected in cart`() {
        val repo = MallRepository()
        repo.addToCart("pd1", 1)
        repo.addToCart("pd2", 1)
        repo.selectAll(false)
        repo.toggleSelect("pd1")
        val order = repo.placeSelectedOrder().getOrThrow()
        assertEquals(1299.0, order.amount, 0.001)
        assertTrue(order.isUnpaid)
        assertEquals(1, repo.cartCount())      // pd2 仍在购物车
        assertEquals(0, repo.selectedCount())
    }

    @Test
    fun `browse filters points products`() {
        MockBackend.scenario = MockScenario.Success
        val all = MallRepository().browse(2)
        assertEquals(3, all.size)
        val pointsOnly = MallRepository().browse(0)
        assertTrue(pointsOnly.all { (it.points ?: 0) > 0 })
        assertEquals(1, pointsOnly.size)
    }

    @Test
    fun `address book exposes a default address`() {
        val repo = MallRepository()
        val addr = repo.selectedAddress()
        assertTrue(addr != null)
        assertTrue(repo.addresses().size >= 3)
    }
}
