package com.example.kuikly.data.chat

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatRepositoryTest {
    private lateinit var chat: FakeChatRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        chat = FakeChatRepository()
    }

    @Test
    fun success_has_at_least_three_conversations() {
        val conversations = chat.conversations().getOrThrow()
        assertTrue(conversations.size >= 3)
        assertEquals("Mock好友1", conversations.first().title)
        assertTrue(conversations.all { it.lastMessage.isNotBlank() && it.timeLabel.isNotBlank() })
    }

    @Test
    fun empty_scenario_returns_empty_list() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(chat.conversations().getOrThrow().isEmpty())
    }

    @Test
    fun error_scenario_fails() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(chat.conversations().isFailure)
        assertTrue(chat.messages("1").isFailure)
    }

    @Test
    fun unauthorized_scenario_fails() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(chat.conversations().isFailure)
        assertTrue(chat.messages("1").isFailure)
    }

    @Test
    fun messages_by_conversation_id() {
        val first = chat.conversations().getOrThrow().first()
        val messages = chat.messages(first.id).getOrThrow()
        assertTrue(messages.isNotEmpty())
        assertTrue(messages.all { it.conversationId == first.id })
        assertTrue(messages.any { !it.isSelf } && messages.any { it.isSelf })
    }

    @Test
    fun conversation_one_matches_flutter_seed() {
        val messages = chat.messages("1").getOrThrow()
        assertEquals(2, messages.size)
        assertEquals("你好，在吗？", messages[0].content)
        assertEquals(false, messages[0].isSelf)
        assertEquals("在的，有什么事？", messages[1].content)
        assertEquals(true, messages[1].isSelf)
    }

    @Test
    fun messages_default_to_read_status() {
        val messages = chat.messages("1").getOrThrow()
        assertTrue(messages.all { it.readStatus == "已读" })
    }

    @Test
    fun unknown_conversation_id_returns_empty() {
        assertTrue(chat.messages("no_such_id").getOrThrow().isEmpty())
    }
}
