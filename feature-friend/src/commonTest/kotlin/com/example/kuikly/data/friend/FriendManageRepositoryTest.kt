package com.example.kuikly.data.friend

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FriendManageRepositoryTest {
    @Test
    fun `request rejects self and duplicates`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRequestRepository(FriendRepository())
        assertTrue(repo.requestFriend(FriendRequestRepository.SELF_ID, "自己").isFailure)
        assertTrue(
            repo.requestFriend("u101", "周涛").isSuccess,
        )
        val again = repo.requestFriend("u101", "周涛")
        assertTrue(again.isFailure)
        assertEquals(
            "已发送过申请，等待对方同意",
            FriendRequestRepository.friendlyError(again.exceptionOrNull()!!),
        )
        val toFriend = repo.requestFriend("f2", "李雷")
        assertTrue(toFriend.isFailure)
        assertEquals(
            "你们已经是好友了",
            FriendRequestRepository.friendlyError(toFriend.exceptionOrNull()!!),
        )
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `respond accept adds friend and enables private chat`() {
        MockBackend.scenario = MockScenario.Success
        val friends = FriendRepository()
        val repo = FriendRequestRepository(friends)
        val incoming = repo.incoming().getOrThrow()
        assertEquals(2, incoming.size)
        assertFalse(friends.canPrivateChat("u201"))

        val accepted = repo.respond(incoming.first().id, true).getOrThrow()
        assertEquals(FriendRequestStatus.ACCEPTED, accepted.status)
        assertTrue(friends.isFriend("u201"))
        assertTrue(repo.canPrivateChat("u201"))
        assertEquals(1, repo.incoming().getOrThrow().size)
        assertEquals(1, repo.history().getOrThrow().size)

        val twice = repo.respond(incoming.first().id, true)
        assertTrue(twice.isFailure)
        assertEquals(
            "无权处理该申请",
            FriendRequestRepository.friendlyError(twice.exceptionOrNull()!!),
        )
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `respond reject keeps stranger`() {
        MockBackend.scenario = MockScenario.Success
        val friends = FriendRepository()
        val repo = FriendRequestRepository(friends)
        val first = repo.incoming().getOrThrow().first()
        val rejected = repo.respond(first.id, false).getOrThrow()
        assertEquals(FriendRequestStatus.REJECTED, rejected.status)
        assertFalse(friends.isFriend(first.fromUserId))
        assertTrue(repo.history().getOrThrow().any { it.id == first.id })
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `outgoing requests are tracked separately`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRequestRepository(FriendRepository())
        assertTrue(repo.outgoingRequests().getOrThrow().isEmpty())
        repo.requestFriend("u102", "吴敏", "135****0102")
        val sent = repo.outgoingRequests().getOrThrow()
        assertEquals(1, sent.size)
        assertEquals("u102", sent.first().fromUserId)
        assertTrue(sent.first().outgoing)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `search users merges friends and strangers`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        assertEquals(1, repo.searchUsers("李雷").getOrThrow().size)
        assertEquals(1, repo.searchUsers("lilei").getOrThrow().size)
        assertEquals(1, repo.searchUsers("138").getOrThrow().size)
        assertTrue(repo.searchUsers("u10").getOrThrow().size >= 4)
        assertTrue(repo.searchUsers("").getOrThrow().isEmpty())
        val hits = repo.searchUsers("周").getOrThrow()
        assertEquals("u101", hits.first().id)
        assertFalse(hits.first().isFriend)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `group crud and moving friends`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        val created = repo.createGroup("球友").getOrThrow()
        assertEquals(4, repo.groups().getOrThrow().size)
        assertTrue(repo.createGroup("球友").isFailure)
        assertTrue(repo.createGroup("  ").isFailure)

        val renamed = repo.renameGroup(created.id, "羽毛球").getOrThrow()
        assertEquals("羽毛球", renamed.name)
        assertEquals("羽毛球", repo.groups().getOrThrow().first { it.id == created.id }.name)

        val moved = repo.moveFriend("f1", created.id).getOrThrow()
        assertEquals(created.id, moved.groupId)
        assertEquals(1, repo.memberCount(created.id))
        assertTrue(repo.moveFriend("f1", "gx").isFailure)

        assertEquals(Pair(1, "我的好友"), repo.deleteGroupImpact(created.id))
        assertEquals(1, repo.deleteGroup(created.id).getOrThrow())
        assertEquals(3, repo.groups().getOrThrow().size)
        assertEquals("g1", repo.detail("f1").getOrThrow().groupId)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `last group cannot be deleted`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        val groups = repo.groups().getOrThrow()
        repo.deleteGroup(groups[1].id)
        repo.deleteGroup(groups[2].id)
        assertEquals(1, repo.groups().getOrThrow().size)
        assertTrue(repo.deleteGroup(groups[0].id).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `remark and tags update friend`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        val remarked = repo.setRemark("f1", "敏姐").getOrThrow()
        assertEquals("敏姐", remarked.remark)
        assertEquals("敏姐", remarked.displayName)
        assertTrue(repo.setRemark("f1", "012345678901234567890").isFailure)
        assertEquals("张敏", repo.setRemark("f1", "  ").getOrThrow().displayName)

        val tagged = repo.setTags("f1", listOf("同事", " 同事 ", "", "重要")).getOrThrow()
        assertEquals(listOf("同事", "重要"), tagged.tags)
        assertEquals("同事 · 重要", tagged.tagLabel)
        assertTrue(repo.setTags("f1", List(10) { "t$it" }).isFailure)
        assertTrue(repo.setRemark("nobody", "x").isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `blocked friends leave contact sections and cannot chat`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        assertTrue(repo.canPrivateChat("f1"))
        val blocked = repo.setBlocked("f1", true).getOrThrow()
        assertTrue(blocked.blocked)
        assertFalse(repo.canPrivateChat("f1"))
        assertEquals(1, repo.blockedFriends().getOrThrow().size)
        assertEquals(3, repo.sectionsByGroup().getOrThrow().sumOf { it.count })
        assertTrue(repo.sectionsByInitial().getOrThrow().none { it.friends.any { f -> f.id == "f1" } })
        assertEquals(4, repo.all { true }.getOrThrow().size)

        repo.setBlocked("f1", false)
        assertTrue(repo.canPrivateChat("f1"))
        assertTrue(repo.blockedFriends().getOrThrow().isEmpty())
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `remove friend`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        assertTrue(repo.removeFriend("f4").getOrThrow())
        assertEquals(3, repo.all { true }.getOrThrow().size)
        assertTrue(repo.detail("f4").isFailure)
        assertTrue(repo.removeFriend("f4").isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `add friend rejects duplicates`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        assertTrue(repo.addFriend("f2", "李雷").isFailure)
        val added = repo.addFriend("u301", "新朋友", "130****0301").getOrThrow()
        assertEquals("g1", added.groupId)
        assertEquals("新朋友", added.name)
        assertTrue(repo.isFriend("u301"))
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `error scenario propagates to new queries`() {
        MockBackend.scenario = MockScenario.Error
        val repo = FriendRepository()
        assertTrue(repo.searchUsers("李").isFailure)
        assertTrue(repo.blockedFriends().isFailure)
        assertTrue(repo.groups().isFailure)
        assertTrue(FriendRequestRepository(FriendRepository()).incoming().isFailure)
        MockBackend.scenario = MockScenario.Success
    }
}
