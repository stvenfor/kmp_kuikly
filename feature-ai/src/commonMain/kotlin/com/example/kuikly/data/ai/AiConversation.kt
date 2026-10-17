package com.example.kuikly.data.ai

import com.example.kuikly.data.BaseMockRepository

/**
 * 停留会话（port of source ADR 0005 Visit Conversation）：
 * 首句由服务端下发 id，Redis 短时保存上下文；本工程落到内存，支持新建 / 切换 / 删除。
 */
data class AiConversation(
    val id: String,
    val title: String,
    val modelId: String = AiModels.DEFAULT_ID,
    val messages: List<AiMessage> = emptyList(),
    val updatedAt: Long = 0L,
) {
    val turnCount: Int get() = messages.count { it.role == AiRole.user }
    val preview: String get() = messages.lastOrNull()?.text ?: ""
}

class AiConversationRepository : BaseMockRepository<AiConversation>() {
    private val items = mutableListOf<AiConversation>()
    private var seq = 3

    override fun seed(): List<AiConversation> = items.sortedByDescending { it.updatedAt }
    override fun idOf(item: AiConversation): String = item.id

    /** 新建会话（会话历史「新建」）。 */
    fun create(title: String = "新对话", modelId: String = AiModels.DEFAULT_ID): AiConversation {
        val item = AiConversation(
            id = "conv_${++seq}",
            title = title,
            modelId = modelId,
            messages = emptyList(),
            updatedAt = System.currentTimeMillis(),
        )
        items.add(0, item)
        return item
    }

    /** meta 事件下发的 conversationId 首次出现时登记（port of source 首句无 id）。 */
    fun ensure(id: String, title: String, modelId: String): AiConversation {
        val exist = items.firstOrNull { it.id == id }
        if (exist != null) return exist
        val created = AiConversation(
            id = id,
            title = title,
            modelId = modelId,
            messages = emptyList(),
            updatedAt = System.currentTimeMillis(),
        )
        items.add(0, created)
        return created
    }

    /** 整包同步气泡（会话内消息以最新一版为准）。 */
    fun replaceMessages(id: String, messages: List<AiMessage>) {
        val old = items.firstOrNull { it.id == id } ?: return
        val next = old.copy(messages = messages, updatedAt = System.currentTimeMillis())
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) items[index] = next
    }

    /** 会话历史「删除」。 */
    fun remove(id: String): Boolean = items.removeAll { it.id == id }

    init {
        val now = System.currentTimeMillis()
        items.add(
            AiConversation(
                id = "conv_1",
                title = "二手车入口咨询",
                modelId = AiModels.DEFAULT_ID,
                messages = listOf(
                    AiMessage("c1_u1", AiRole.user, "二手车入口在哪里？", conversationId = "conv_1"),
                    AiMessage(
                        "c1_a1",
                        AiRole.assistant,
                        "二手车入口在「首页 → 全部服务 → 二手车」。",
                        conversationId = "conv_1",
                    ),
                ),
                updatedAt = now - 3_600_000L,
            ),
        )
        items.add(
            AiConversation(
                id = "conv_2",
                title = "账号登录问题",
                modelId = AiModels.DEFAULT_ID,
                messages = listOf(
                    AiMessage("c2_u1", AiRole.user, "怎么登录账号？", conversationId = "conv_2"),
                    AiMessage(
                        "c2_a1",
                        AiRole.assistant,
                        "登录用手机号 + 验证码，验证码 5 分钟内有效。",
                        conversationId = "conv_2",
                    ),
                ),
                updatedAt = now - 86_400_000L,
            ),
        )
    }
}
