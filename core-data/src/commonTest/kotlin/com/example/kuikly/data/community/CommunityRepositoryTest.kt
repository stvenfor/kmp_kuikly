package com.example.kuikly.data.community

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommunityRepositoryTest {
    private lateinit var repo: FakeCommunityRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        repo = FakeCommunityRepository()
    }

    @Test
    fun list_success_has_at_least_three_posts() {
        val posts = repo.list().getOrThrow()
        assertTrue(posts.size >= 3)
        assertTrue(posts.all { it.title.isNotBlank() && it.author.isNotBlank() && it.body.isNotBlank() })
    }

    @Test
    fun list_empty() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(repo.list().getOrThrow().isEmpty())
    }

    @Test
    fun list_error() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(repo.list().isFailure)
    }

    @Test
    fun list_unauthorized() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(repo.list().isFailure)
    }

    @Test
    fun detail_post1_flutter_seed() {
        // Flutter `13-flutter-main-community` 截图：@张三 / #Flutter开发 / https://flutter.dev,
        // likes=158, comments=6。
        val post = repo.detail("1").getOrThrow()
        assertEquals("张三", post.author)
        assertEquals("Flutter 跨端开发", post.title)
        assertEquals(158, post.likeCount)
        assertEquals(6, post.commentCount)
        assertTrue(post.body.contains("@张三"), "missing @张三 mention: ${post.body}")
        assertTrue(post.body.contains("#Flutter开发"), "missing #Flutter开发 tag: ${post.body}")
        assertTrue(post.body.contains("https://flutter.dev"), "missing flutter.dev URL: ${post.body}")
    }

    @Test
    fun detail_post2_hiking_seed() {
        // Flutter `13-flutter-main-community` 截图：#户外 outdoor tag, likes=77, comments=2。
        val post = repo.detail("2").getOrThrow()
        assertEquals("李四", post.author)
        assertEquals("周末 hiking", post.title)
        assertEquals(77, post.likeCount)
        assertEquals(2, post.commentCount)
        assertTrue(post.body.contains("周末 hiking"), "missing 周六 hiking body: ${post.body}")
        assertTrue(post.body.contains("#户外"), "missing #户外 tag: ${post.body}")
    }

    @Test
    fun detail_unknown_id_fails() {
        assertTrue(repo.detail("nope").isFailure)
    }

    @Test
    fun store_repo_is_fake() {
        assertTrue(CommunityStore.repo is FakeCommunityRepository)
    }
}
