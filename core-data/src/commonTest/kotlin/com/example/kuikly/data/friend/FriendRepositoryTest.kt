package com.example.kuikly.data.friend

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FriendRepositoryTest {
    private lateinit var repo: FakeFriendRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        repo = FakeFriendRepository()
    }

    @Test
    fun list_success_has_at_least_three_friends() {
        val friends = repo.list().getOrThrow()
        assertTrue(friends.size >= 3)
        assertTrue(friends.all { it.name.isNotBlank() && it.bio.isNotBlank() })
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
    fun detail_returns_seeded_friend() {
        val friend = repo.detail("1").getOrThrow()
        assertEquals("张伟", friend.name)
    }

    @Test
    fun detail_unknown_id_fails() {
        assertTrue(repo.detail("nope").isFailure)
    }

    @Test
    fun store_repo_is_fake() {
        assertTrue(FriendStore.repo is FakeFriendRepository)
    }
}
