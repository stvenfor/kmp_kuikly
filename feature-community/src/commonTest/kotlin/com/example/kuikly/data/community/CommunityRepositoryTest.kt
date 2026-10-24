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
        assertTrue(r.items.any { it.nickname == "张三" })
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
        assertEquals(1, repo.searchPosts("咖啡店").getOrThrow().size)
        assertEquals(1, repo.searchPosts("效率").getOrThrow().size)
        assertEquals(1, repo.searchPosts("张三").getOrThrow().size)
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
        assertEquals(1, repo.postsByTopic("效率").getOrThrow().size)
        assertTrue(repo.postsByTopic("好物推荐").getOrThrow().isEmpty())
        assertTrue(repo.postsByTopic("效率").getOrThrow().all { it.topicName == "效率" })
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
        val r = repo.search("效率").getOrThrow()
        assertEquals("效率", r.query)
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

    // ------------------------------------------------------------------
    // 真实交互（替代原 CommunityTab 的 index 取模伪状态）
    // ------------------------------------------------------------------

    @Test
    fun `feed honours tab ordering`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        // latest：seed 顺序
        val latest = repo.feed("latest", 1, 20).getOrThrow()
        assertEquals(listOf("p1", "p2", "p3"), latest.items.map { it.id })
        // hot：按 `likeCount * 2 + commentCount` 降序（p3=669 > p1=322 > p2=156）
        val hot = repo.feed("hot", 1, 20).getOrThrow()
        assertEquals(listOf("p3", "p1", "p2"), hot.items.map { it.id })
        // following：初始未关注任何人 → 空（演示空态）
        assertTrue(repo.feed("following", 1, 20).getOrThrow().items.isEmpty())
    }

    @Test
    fun `toggleLike flips flag and count`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        // seed 按源规则 `isLiked = i % 4 == 0` → 首条默认已赞
        val first = repo.feed("latest", 1, 20).getOrThrow().items.first()
        assertTrue(first.isLiked)
        val off = repo.toggleLike(first.id, false).getOrThrow()
        assertTrue(!off.isLiked)
        assertEquals(first.likeCount - 1, off.likeCount)
        val on = repo.toggleLike(first.id, true).getOrThrow()
        assertTrue(on.isLiked)
        assertEquals(first.likeCount, on.likeCount)
    }

    @Test
    fun `deletePost removes post from feed`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        assertTrue(repo.deletePost("p3").isSuccess)
        assertEquals(2, repo.feed("latest", 1, 20).getOrThrow().items.size)
        assertTrue(repo.deletePost("不存在").isFailure)
    }

    @Test
    fun `seed comments follow flutter nickname rule`() {
        MockBackend.scenario = MockScenario.Success
        val repo = CommunityRepository()
        val first = repo.feed("latest", 1, 20).getOrThrow().items.first()
        assertEquals(2, first.previewComments.size)
        // 源 `_seedComments`：`_nicknames[(index+1)%8]` / `[(index+3)%8]`，
        // 第 2 条 replyToNickname = `_nicknames[index%8]`
        assertEquals("李四", first.previewComments[0].nickname)
        assertEquals("说得对！", first.previewComments[0].content)
        assertEquals("赵六", first.previewComments[1].nickname)
        assertEquals("张三", first.previewComments[1].replyToNickname)
    }
}
