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

    @Test
    fun `search matches title or last message`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val byTitle = repo.searchConversations("增长").getOrThrow()
        assertTrue(byTitle.any { it.id == "c2" })
        val byMessage = repo.searchConversations("原型").getOrThrow()
        assertTrue(byMessage.any { it.id == "c1" })
        assertEquals(3, repo.searchConversations("").getOrThrow().size)
    }

    @Test
    fun `send image appends to history`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val sent = repo.sendImage("c3", "file://x.png").getOrThrow()
        assertTrue(sent.type == MessageType.image)
        assertTrue(repo.images("c3").contains("file://x.png"))
    }

    @Test
    fun `send voice keeps duration`() {
        MockBackend.scenario = MockScenario.Success
        val sent = ChatRepository().sendVoice("c1", 7).getOrThrow()
        assertEquals(7, sent.voiceDurationSeconds)
        assertEquals(MessageType.voice, sent.type)
    }

    @Test
    fun `send custom carries payload`() {
        MockBackend.scenario = MockScenario.Success
        val sent = ChatRepository().sendCustom("c1", "card", "Demo 名片", "来自自定义消息").getOrThrow()
        assertEquals(MessageType.custom, sent.type)
        assertEquals("Demo 名片", sent.customPayload["title"])
    }

    @Test
    fun `delete removes outbox message`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val sent = repo.send("c1", "临时").getOrThrow()
        assertTrue(repo.deleteMessage("c1", sent.id).isSuccess)
        assertTrue(repo.messageById("c1", sent.id).isFailure)
    }

    @Test
    fun `recall marks message recalled`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val sent = repo.send("c2", "撤回我").getOrThrow()
        val recalled = repo.recallMessage("c2", sent.id).getOrThrow()
        assertTrue(recalled.isRecalled)
        assertEquals("你撤回了一条消息", recalled.content)
    }

    @Test
    fun `simulate peer read sets self message read`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val sent = repo.send("c1", "已读回执测试").getOrThrow()
        val read = repo.simulatePeerRead("c1", sent.id).getOrThrow()
        assertEquals(ReadStatus.read, read.readStatus)
    }

    @Test
    fun `status label reflects state machine`() {
        val selfSending = Message("a", "c", MessageType.text, "x", true, 1L, sendStatus = SendStatus.sending)
        val selfFailed = selfSending.copy(sendStatus = SendStatus.failed)
        val selfSentUnread = selfSending.copy(sendStatus = SendStatus.sent, readStatus = ReadStatus.unread)
        val selfSentRead = selfSending.copy(sendStatus = SendStatus.sent, readStatus = ReadStatus.read)
        val peer = Message("b", "c", MessageType.text, "y", false, 1L)
        assertEquals("发送中", statusLabel(selfSending))
        assertEquals("发送失败", statusLabel(selfFailed))
        assertEquals("送达", statusLabel(selfSentUnread))
        assertEquals("已读", statusLabel(selfSentRead))
        assertEquals("", statusLabel(peer))
    }

    @Test
    fun `send fails under error scenario`() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(ChatRepository().send("c1", "hi").isFailure)
        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `sending updates conversation preview`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        repo.send("c1", "新的预览内容").getOrThrow()
        assertEquals("新的预览内容", repo.byId("c1").getOrThrow().lastMessage)
    }

    @Test
    fun `sending image updates preview to image tag`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        repo.sendImage("c3", "file://x.png").getOrThrow()
        assertEquals("[图片]", repo.byId("c3").getOrThrow().lastMessage)
    }

    @Test
    fun `auto reply updates conversation preview`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        repo.autoReply("c1", "你好").getOrThrow()
        assertEquals("已读：你好", repo.byId("c1").getOrThrow().lastMessage)
    }

    @Test
    fun `seed sorts conversations by last message time desc`() {
        MockBackend.scenario = MockScenario.Success
        val items = ChatRepository().page(1, 50).getOrThrow().items
        for (i in 1 until items.size) {
            assertTrue(items[i - 1].lastMessageTime >= items[i].lastMessageTime)
        }
    }

    @Test
    fun `ensure private conversation creates once`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val a = repo.ensurePrivateConversation("u_new", "新朋友", "").getOrThrow()
        assertEquals("u_new", a.targetId)
        assertTrue(a.isGroup.not())
        val b = repo.ensurePrivateConversation("u_new", "新朋友", "").getOrThrow()
        assertEquals(a.id, b.id)
        assertEquals(4, repo.page(1, 50).getOrThrow().total)
    }

    @Test
    fun `ensure group conversation updates title`() {
        MockBackend.scenario = MockScenario.Success
        val repo = ChatRepository()
        val g = repo.ensureGroupConversation("g_x", "群X").getOrThrow()
        val g2 = repo.ensureGroupConversation("g_x", "群X改名").getOrThrow()
        assertEquals("群X改名", g2.title)
        assertEquals(g.id, g2.id)
        assertTrue(g2.isGroup)
    }
}
