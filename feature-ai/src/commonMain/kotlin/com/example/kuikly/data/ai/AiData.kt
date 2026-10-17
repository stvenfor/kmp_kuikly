package com.example.kuikly.data.ai

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** port of source AiChatRole：user / assistant / welcome（首屏欢迎气泡）。 */
enum class AiRole { user, assistant, welcome }

/** AI 对话消息（port of source AiChatMessage：可变的 text / errorMessage / isStreaming）。 */
data class AiMessage(
    val id: String,
    val role: AiRole,
    val text: String,
    val isStreaming: Boolean = false,
    val errorMessage: String? = null,
    /** port of source 停留会话 id（首句为空，由 meta 事件下发）。 */
    val conversationId: String? = null,
)

class AiRepository {
    private val canned = listOf(
        "你好！我是 AI 助手，有什么可以帮你？",
        "可以把「转化率」理解为 转化数 / 点击数，用来衡量流量质量。",
        "建议把漏斗拆成：曝光 → 点击 → 转化，逐层定位流失。",
    )

    /** 流式对话：把回复切成若干 chunk，调用方逐段拼接实现打字机效果。 */
    fun streamChunks(prompt: String, onChunk: (String) -> Unit): Result<String> {
        if (prompt.isBlank()) return Result.failure(IllegalArgumentException("prompt empty"))
        if (MockBackend.scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("ai service error"))
        }
        val reply = canned[prompt.length % canned.size]
        reply.chunked(4).forEach { onChunk(it) }
        return Result.success(reply)
    }
}

/** 复制：BridgeModule 未暴露 native pasteboard，先落到内存，由页面 toast 反馈。 */
class AiClipboard {
    var lastCopied: String = ""
        private set

    fun copy(text: String): String {
        lastCopied = text
        return text
    }
}

object AiStore {
    val repo: AiRepository = AiRepository()

    /** 对话历史（Mock 内存态，重启即清空）。 */
    val history: AiHistory = AiHistory()

    /** SSE 流式仓储（port of source AiStreamRepository）。 */
    val stream: AiStreamRepository = AiStreamRepository()

    /** 可选模型（port of source 请求体 options.model）。 */
    val models: AiModelRepository = AiModelRepository()

    /** 会话历史（新建 / 切换 / 删除）。 */
    val conversations: AiConversationRepository = AiConversationRepository()

    val clipboard: AiClipboard = AiClipboard()

    /** 全局默认模型，新建会话时继承。 */
    var currentModelId: String = AiModels.DEFAULT_ID
}

/** 简单的对话历史（port of source chat history）。 */
class AiHistory : BaseMockRepository<AiMessage>() {
    private val items = mutableListOf<AiMessage>()

    override fun seed(): List<AiMessage> = items.toList()
    override fun idOf(item: AiMessage): String = item.id

    fun append(m: AiMessage) { items.add(m) }
}
