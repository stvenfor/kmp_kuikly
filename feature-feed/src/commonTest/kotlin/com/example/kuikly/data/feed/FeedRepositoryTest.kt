package com.example.kuikly.data.feed

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedRepositoryTest {
    @Test
    fun `recommend page 1 returns items`() {
        MockBackend.scenario = MockScenario.Success
        val r = FeedStore.repo.fetchPage(1, "recommend").getOrThrow()
        assertTrue(r.items.isNotEmpty())
        assertEquals(8, r.pageSize)
    }

    @Test
    fun `following tab is non-empty after seed follow`() {
        MockBackend.scenario = MockScenario.Success
        val r = FeedStore.repo.fetchPage(1, "following").getOrThrow()
        assertTrue(r.items.isNotEmpty())
        assertTrue(r.items.all { it.userId == "u0" || it.userId == "u2" })
    }

    @Test
    fun `nearby tab filters by nearby flag`() {
        MockBackend.scenario = MockScenario.Success
        val r = FeedStore.repo.fetchPage(1, "nearby").getOrThrow()
        assertTrue(r.items.all { it.nearby })
    }

    @Test
    fun `toggleLike flips flag and count`() {
        MockBackend.scenario = MockScenario.Success
        val first = FeedStore.repo.fetchPage(1, "recommend").getOrThrow().items.first()
        val before = first.isLiked
        val updated = FeedStore.repo.toggleLike(first.id).getOrThrow()
        assertEquals(!before, updated.isLiked)
        assertEquals(first.likeCount + if (before) -1 else 1, updated.likeCount)
    }

    @Test
    fun `addComment increments count and prepends`() {
        MockBackend.scenario = MockScenario.Success
        val first = FeedStore.repo.fetchPage(1, "recommend").getOrThrow().items.first()
        val updated = FeedStore.repo.addComment(first.id, "测试评论").getOrThrow()
        assertEquals(first.commentCount + 1, updated.commentCount)
        assertEquals("测试评论", updated.comments.first().content)
    }

    @Test
    fun `submitPost returns mine item`() {
        MockBackend.scenario = MockScenario.Success
        val created = FeedStore.repo.submitPost("我的第一条动态 #效率工具", "效率工具").getOrThrow()
        assertTrue(created.isMine)
        assertEquals("效率工具", created.topic)
    }

    @Test
    fun `error scenario fetchPage fails`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(FeedStore.repo.fetchPage(1).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `empty scenario fetchPage empty`() {
        MockBackend.scenario = MockScenario.Empty
        val r = FeedStore.repo.fetchPage(1).getOrThrow()
        assertTrue(r.items.isEmpty())
        MockBackend.scenario = MockScenario.Success
    }
}
