package com.example.kuikly.data.live

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 直播间互动：分类筛选、点赞、关注、礼物（钻石余额）。 */
class LiveEngagementRepositoryTest {

    @Test
    fun `categories are distinct and filterable`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        val categories = repo.categories().getOrThrow()
        assertEquals(3, categories.size)
        assertTrue(categories.contains("带货"))
        assertEquals(1, repo.byCategory("技术").getOrThrow().size)
        assertTrue(repo.byCategory("不存在").getOrThrow().isEmpty())
    }

    @Test
    fun `like accumulates per room`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        val before = repo.fetchById("l1").getOrThrow().likeCount
        val after = repo.likeRoom("l1").getOrThrow()
        assertEquals(before + 1, after.likeCount)
        assertEquals(before + 1, repo.fetchById("l1").getOrThrow().likeCount)
        assertTrue(repo.likeRoom("missing").isFailure)
    }

    @Test
    fun `follow toggles both ways`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        assertFalse(repo.fetchById("l1").getOrThrow().followed)
        assertTrue(repo.toggleFollow("l1").getOrThrow().followed)
        assertFalse(repo.toggleFollow("l1").getOrThrow().followed)
        assertTrue(repo.toggleFollow("missing").isFailure)
    }

    @Test
    fun `gift deducts coin and grows counter`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        val coinBefore = repo.coinBalance()
        val giftBefore = repo.fetchById("l1").getOrThrow().giftCount
        val record = repo.sendGift("l1", "gf1").getOrThrow()
        assertEquals("小心心", record.giftName)
        assertEquals(10, record.coin)
        assertEquals(coinBefore - 10, repo.coinBalance())
        assertEquals(giftBefore + 1, repo.fetchById("l1").getOrThrow().giftCount)
        assertEquals(record, repo.giftFeedOf("l1").first())
        assertTrue(repo.giftFeedOf("l3").isEmpty())
    }

    @Test
    fun `gift failures cover missing gift room and coin`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        assertTrue(repo.sendGift("l1", "missing").isFailure)
        assertTrue(repo.sendGift("missing", "gf1").isFailure)
        assertTrue(repo.sendGift("l1", "gf4").isSuccess)
        assertTrue(repo.sendGift("l1", "gf4").isSuccess)
        assertTrue(repo.sendGift("l1", "gf3").isSuccess)
        assertEquals(500, repo.coinBalance())
        assertTrue(repo.sendGift("l1", "gf4").isFailure)
        assertTrue(repo.sendGift("l1", "gf3").isSuccess)
        assertTrue(repo.sendGift("l1", "gf4").isFailure)
    }

    @Test
    fun `gift catalog follows mock scenario`() {
        MockBackend.scenario = MockScenario.Success
        val repo = LiveRepository()
        assertEquals(4, repo.giftCatalog().getOrThrow().size)
        MockBackend.scenario = MockScenario.Empty
        assertTrue(repo.giftCatalog().getOrThrow().isEmpty())
        MockBackend.scenario = MockScenario.Error
        assertTrue(repo.giftCatalog().isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `categories follow mock scenario`() {
        MockBackend.scenario = MockScenario.Empty
        val repo = LiveRepository()
        assertTrue(repo.categories().getOrThrow().isEmpty())
        MockBackend.scenario = MockScenario.Error
        assertTrue(repo.categories().isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
