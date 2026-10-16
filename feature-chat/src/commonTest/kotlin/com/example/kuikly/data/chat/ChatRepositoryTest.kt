package com.example.kuikly.data.chat

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatRepositoryTest {
    @Test
    fun `conversations success`() {
        MockBackend.scenario = MockScenario.Success
        val r = ChatRepository().page(1, 50).getOrThrow()
        assertEquals(3, r.total)
        assertEquals(2, r.items.first { it.id == "c1" }.unreadCount)
        assertEquals(5, r.items.first { it.id == "c2" }.unreadCount)
    }

    @Test
    fun `messages filtered by conversation`() {
        val msgs = ChatRepository().messages("c1").getOrThrow()
        assertEquals(3, msgs.size)
        assertTrue(msgs.all { it.conversationId == "c1" })
    }

    @Test
    fun `send requires non blank text`() {
        assertTrue(ChatRepository().send("c1", "  ").isFailure)
        assertTrue(ChatRepository().send("c1", "hi").isSuccess)
    }

    @Test
    fun `error scenario fails`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(ChatRepository().page(1, 50).isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `message page paginates oldest first`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val first = repo.messagePage("c2", 1, 5).getOrThrow()
        assertEquals(5, first.items.size)
        assertEquals(9, first.total)
        assertTrue(first.hasMore)

        val second = repo.messagePage("c2", 2, 5).getOrThrow()
        assertEquals(4, second.items.size)
        assertTrue(!second.hasMore)

        val third = repo.messagePage("c2", 3, 5).getOrThrow()
        assertTrue(third.items.isEmpty())

        // 升序：后一页的首条一定晚于前一页的末条
        assertTrue(second.items.first().createdAt > first.items.last().createdAt)
        // 两页合起来正好是全量，且无重复
        val merged = first.items + second.items
        assertEquals(9, merged.size)
        assertEquals(9, merged.distinctBy { it.id }.size)
    }

    @Test
    fun `message page rejects invalid page size`() {
        assertTrue(ChatRepository().messagePage("c2", 1, 0).isFailure)
    }

    @Test
    fun `sent messages join the message stream`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val before = repo.messages("c1").getOrThrow().size
        val sent = repo.send("c1", "我补充一句").getOrThrow()
        assertTrue(sent.isSelf)
        val reply = repo.autoReply("c1", "我补充一句").getOrThrow()
        assertTrue(!reply.isSelf)
        assertEquals(before + 2, repo.messages("c1").getOrThrow().size)
        assertTrue(repo.messages("c1").getOrThrow().last().isSelf.not())
    }

    @Test
    fun `mark read clears unread`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        assertEquals(7, repo.unreadTotal())
        val cleared = repo.markRead("c2").getOrThrow()
        assertEquals(0, cleared.unreadCount)
        assertEquals(0, repo.byId("c2").getOrThrow().unreadCount)
        assertEquals(2, repo.unreadTotal())
        // 列表分页同样读到清零后的值
        assertEquals(0, repo.page(1, 50).getOrThrow().items.first { it.id == "c2" }.unreadCount)
    }

    @Test
    fun `time grouping splits on five minute gap`() {
        MockBackend.scenario = MockScenario.Success
        val msgs = ChatRepository().messages("c2").getOrThrow()
        val now = msgs.maxOf { it.createdAt }
        val sections = groupMessagesByTime(msgs, now)
        assertEquals(4, sections.size)
        assertEquals(2, sections[0].messages.size)
        assertTrue(sections.all { it.label.isNotBlank() })
        // 最后一组落在"当天"，只显示 HH:mm
        assertTrue(!sections.last().label.contains("/"))
        // 全部分组的消息总数守恒
        assertEquals(msgs.size, sections.sumOf { it.messages.size })
    }

    @Test
    fun `time grouping of single message`() {
        val one = listOf(Message("x1", "c9", MessageType.text, "hi", false, 1_700_000_000L))
        val sections = groupMessagesByTime(one, 1_700_000_000L)
        assertEquals(1, sections.size)
        assertEquals(1, sections[0].messages.size)
    }

    @Test
    fun `chat clock formats hour and minute`() {
        assertEquals("22:13", chatClock(1_700_000_000L))
        assertEquals("00:00", chatClock(1_699_920_000L))
    }
}
