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
    fun detail_returns_seeded_post() {
        val post = repo.detail("1").getOrThrow()
        assertEquals("张三", post.author)
        assertEquals("今天去了推荐的咖啡店", post.title)
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
