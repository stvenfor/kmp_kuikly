package com.example.kuikly.data.friend

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FriendRepositoryTest {
    @Test
    fun `friends grouped`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        assertEquals(3, repo.groups().getOrThrow().size)
        assertEquals(4, repo.page(1, 50).getOrThrow().total)
        assertEquals(2, repo.all { it.groupId == "g2" }.getOrThrow().size)
    }

    @Test
    fun `search filters`() {
        val r = FriendRepository().search("李").getOrThrow()
        assertEquals(1, r.size)
        assertEquals("李雷", r.first().name)
    }

    @Test
    fun `error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(FriendRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `sections by group keep declaration order`() {
        MockBackend.scenario = MockScenario.Success
        val sections = FriendRepository().sectionsByGroup().getOrThrow()
        assertEquals(3, sections.size)
        assertEquals(listOf("我的好友", "同事", "家人"), sections.map { it.title })
        assertEquals(listOf(1, 2, 1), sections.map { it.count })
        assertEquals(4, sections.sumOf { it.count })
        // 组内按拼音排序：g2 应为 韩梅梅(h) 在 李雷(l) 之前
        assertEquals(listOf("韩梅梅", "李雷"), sections.first { it.key == "g2" }.friends.map { it.name })
        assertEquals(1, sections.first { it.key == "g2" }.onlineCount)
    }

    @Test
    fun `sections by initial are sorted a to z`() {
        MockBackend.scenario = MockScenario.Success
        val sections = FriendRepository().sectionsByInitial().getOrThrow()
        assertEquals(listOf("H", "L", "M", "Z"), sections.map { it.key })
        assertTrue(sections.all { it.friends.size == 1 })
        assertEquals(4, sections.sumOf { it.count })
        assertEquals("韩梅梅", sections.first().friends.first().name)
    }

    @Test
    fun `initial falls back to hash when pinyin missing`() {
        val anonymous = Friend("f9", "无名", "", "g1", false)
        assertEquals("#", anonymous.initialLabel)
        assertEquals("f9", anonymous.subtitle)
        assertEquals('Z', FriendRepository().byId("f1").getOrThrow().initial)
    }

    @Test
    fun `search all matches pinyin too`() {
        MockBackend.scenario = MockScenario.Success
        val repo = FriendRepository()
        assertEquals(1, repo.searchAll("lilei").getOrThrow().size)
        assertEquals("李雷", repo.searchAll("lilei").getOrThrow().first().name)
        assertEquals(1, repo.searchAll("张").getOrThrow().size)
        assertEquals(4, repo.searchAll("").getOrThrow().size)
        assertTrue(repo.searchAll("zzz").getOrThrow().isEmpty())
    }

    @Test
    fun `aggregation honours error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(FriendRepository().sectionsByGroup().isFailure)
        assertTrue(FriendRepository().sectionsByInitial().isFailure)
        assertTrue(FriendRepository().searchAll("李").isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `empty scenario yields no sections`() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(FriendRepository().sectionsByGroup().getOrThrow().isEmpty())
        MockBackend.scenario = MockScenario.Success
    }
}
