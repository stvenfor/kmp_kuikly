package com.example.kuikly.data.community

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommunityRepositoryTest {
    @Test
    fun `posts success`() {
        MockBackend.scenario = MockScenario.Success
        val r = CommunityRepository().page(1, 50).getOrThrow()
        assertEquals(3, r.total)
        assertTrue(r.items.any { it.topicName == "健身打卡" })
    }

    @Test
    fun `publish requires content`() {
        assertTrue(CommunityRepository().publish("  ", null).isFailure)
        assertTrue(CommunityRepository().publish("hello", "健身打卡").isSuccess)
    }

    @Test
    fun `topics returned`() {
        assertTrue(CommunityRepository().topics().getOrThrow().isNotEmpty())
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(CommunityRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `publish failure message is readable`() {
        val failure = CommunityRepository().publish("", "健身打卡")
        assertTrue(failure.isFailure)
        assertEquals("内容不能为空", failure.exceptionOrNull()?.message)
        // 发布成功时回传话题与作者
        val ok = CommunityRepository().publish("今天状态不错", "健身打卡").getOrThrow()
        assertEquals("健身打卡", ok.topicName)
        assertTrue(ok.isMine)
    }

    @Test
    fun `search posts matches content and nickname`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        assertEquals(1, repo.searchPosts("耳机").getOrThrow().size)
        assertEquals(1, repo.searchPosts("效率").getOrThrow().size)
        assertEquals(1, repo.searchPosts("小鹿").getOrThrow().size)
        assertEquals(3, repo.searchPosts("").getOrThrow().size)
        assertTrue(repo.searchPosts("不存在的关键词").getOrThrow().isEmpty())
        // 大小写不敏感
        assertTrue(repo.searchPosts("IPHONE").getOrThrow().isEmpty())
    }

    @Test
    fun `topic filter narrows posts`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        assertEquals(3, repo.postsByTopic(null).getOrThrow().size)
        assertEquals(3, repo.postsByTopic("  ").getOrThrow().size)
        assertEquals(1, repo.postsByTopic("健身打卡").getOrThrow().size)
        assertTrue(repo.postsByTopic("好物推荐").getOrThrow().isEmpty())
        assertTrue(repo.postsByTopic("健身打卡").getOrThrow().all { it.topicName == "健身打卡" })
    }

    @Test
    fun `search topics and ranked topics`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        assertEquals(1, repo.searchTopics("健身").getOrThrow().size)
        assertTrue(repo.searchTopics("不存在的").getOrThrow().isEmpty())
        val ranked = repo.rankedTopics().getOrThrow()
        // "问大家"话题置顶
        assertTrue(ranked.first().isAskEveryone)
        // 其余按热度降序
        val rest = ranked.filter { !it.isAskEveryone }
        assertEquals(rest.sortedByDescending { it.heat }.map { it.id }, rest.map { it.id })
    }

    @Test
    fun `composite search returns three sections`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        val r = repo.search("健身").getOrThrow()
        assertEquals("健身", r.query)
        assertTrue(r.posts.isNotEmpty())
        assertTrue(r.topics.isNotEmpty())
        assertTrue(r.users.isEmpty())
        assertEquals(r.posts.size + r.topics.size + r.users.size, r.hitCount)

        val empty = repo.search("绝对不存在的关键词").getOrThrow()
        assertTrue(empty.isEmpty)
        assertEquals(0, empty.hitCount)
    }

    @Test
    fun `toggle follow flips state`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        val before = repo.searchUsers("").getOrThrow().first { it.id == "u1" }
        assertTrue(!before.isFollowed)
        val after = repo.toggleFollow("u1").getOrThrow()
        assertTrue(after.isFollowed)
        assertTrue(repo.toggleFollow("不存在").isFailure)
    }

    @Test
    fun `search honours error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(CommunityRepository().search("健身").isFailure)
        assertTrue(CommunityRepository().searchPosts("健身").isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
