package com.example.kuikly.data.chat

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

data class Conversation(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timeLabel: String,
    val unreadCount: Int = 0,
    // Phase-2 / P2-W2b: mirrors Flutter mock_im_chat_store seed (`isOnline: i.isEven`),
    // used by ChatDetail header's 在线/离线 status line.
    val isOnline: Boolean = false,
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val content: String,
    val isSelf: Boolean,
)

interface ChatRepository {
    fun conversations(): Result<List<Conversation>>
    fun messages(conversationId: String): Result<List<ChatMessage>>
}

/**
 * Mock conversation list + per-conversation messages, mirroring the Flutter
 * mock_im_chat_store seed (3 mock peers, text-only messages).
 */
class FakeChatRepository : ChatRepository {
    private val seed = listOf(
        Conversation("1", "Mock好友1", "晚上一起吃饭吗？", "刚刚", unreadCount = 2, isOnline = true),
        Conversation("2", "Mock好友2", "你好", "10:24", isOnline = false),
        Conversation("3", "Mock好友3", "周末看车方便吗？", "昨天", isOnline = true),
    )

    private val messagesByConversation = mapOf(
        "1" to listOf(
            ChatMessage("m_1_1", "1", "你好，在吗？", isSelf = false),
            ChatMessage("m_1_2", "1", "在的，有什么事？", isSelf = true),
            ChatMessage("m_1_3", "1", "晚上一起吃饭吗？", isSelf = false),
        ),
        "2" to listOf(
            ChatMessage("m_2_1", "2", "你好", isSelf = false),
        ),
        "3" to listOf(
            ChatMessage("m_3_1", "3", "周末看车方便吗？", isSelf = false),
            ChatMessage("m_3_2", "3", "方便，欢迎到店", isSelf = true),
        ),
    )

    override fun conversations(): Result<List<Conversation>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock chat error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun messages(conversationId: String): Result<List<ChatMessage>> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock chat error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return Result.success(messagesByConversation[conversationId] ?: emptyList())
    }
}

object ChatStore {
    val repo: ChatRepository = FakeChatRepository()
}
