package com.example.kuikly.data.ai

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** port of source AiStreamController.welcomeText / quickPrompts（三条业务向导快捷问）。 */
object AiPrompts {
    const val WELCOME = "你好，我是 AI 小石头——本 App / 4S 店的业务向导。你可以问「二手车入口在哪」「如何登录」或点下方快捷问。"

    val QUICK = listOf(
        "二手车入口在哪里？",
        "怎么登录账号？",
        "数据分析怎么看？",
    )

    /** 工程内无 TextField，输入框沿用「候选词追加」口径的词条。 */
    val CANDIDATES = listOf(
        "二手车", "入口", "在哪里", "怎么", "登录", "账号",
        "数据", "分析", "怎么看", "转化率", "漏斗", "？",
    )
}

/**
 * port of source `ai/controller/ai_stream_controller.dart`：
 * 多轮气泡、单轮在途（streaming 时拒绝新发送）、停止生成、停留会话 id、错误重试。
 *
 * 与源端的差别：源端靠 `await for` 推流，本工程无协程流式基建，改为
 * [send] 先落帧、[pump] 逐帧消费（页面用定时器驱动，得到分片/打字机效果）。
 */
class AiChatSession(
    private val repository: AiStreamRepository = AiStreamRepository(),
    private val conversations: AiConversationRepository = AiConversationRepository(),
) {
    /** 状态变更回调：页面借此触发重组（Compose 无法直接观察普通类字段）。 */
    var onChanged: (() -> Unit)? = null

    var messages: List<AiMessage> = emptyList()
        private set

    var isStreaming: Boolean = false
        private set

    var status: AiStreamStatus = AiStreamStatus.Idle
        private set

    /** 停留会话 id：首句为空，由 meta 事件下发。 */
    var conversationId: String? = null
        private set

    /** meta 事件回显的服务端模型名（未下发时显示本地选择）。 */
    var serverModel: String = ""
        private set

    var modelId: String = AiModels.DEFAULT_ID

    var lastError: String? = null
        private set

    var lastUserPrompt: String? = null
        private set

    private var pending: List<AiStreamEvent> = emptyList()
    private var pendingIndex = 0
    private var streamingId: String? = null
    private var seq = 0

    /** port of source `canSend => !isStreaming.value`：单轮在途。 */
    val canSend: Boolean get() = !isStreaming

    /** 源端页「messages.length <= 1 && !streaming」才展示快捷问。 */
    val showQuickPrompts: Boolean get() = messages.size <= 1 && !isStreaming

    /** 打字机分片间隔：Slow 场景放慢。 */
    val chunkDelayMs: Int get() = if (MockBackend.scenario == MockScenario.Slow) 90 else 28

    /** port of source onInit：落欢迎气泡。 */
    fun start() {
        reset()
    }

    /** port of source onClose：清空气泡与停留会话 id。 */
    fun reset() {
        pending = emptyList()
        pendingIndex = 0
        streamingId = null
        isStreaming = false
        status = AiStreamStatus.Idle
        lastError = null
        lastUserPrompt = null
        conversationId = null
        serverModel = ""
        messages = listOf(welcomeMessage())
        emit()
    }

    /** 会话历史「切换」：载入该会话的气泡。 */
    fun openConversation(id: String): Boolean {
        if (isStreaming) return false
        val target = conversations.byId(id).getOrNull() ?: return false
        pending = emptyList()
        pendingIndex = 0
        streamingId = null
        isStreaming = false
        status = AiStreamStatus.Idle
        lastError = null
        lastUserPrompt = null
        conversationId = id
        modelId = target.modelId
        serverModel = ""
        messages = listOf(welcomeMessage()) + target.messages
        emit()
        return true
    }

    /** 会话历史「新建」。 */
    fun newConversation(): Boolean {
        if (isStreaming) return false
        val created = conversations.create(modelId = modelId)
        return openConversation(created.id)
    }

    /** 会话历史「删除」：删的是当前会话则回到欢迎态。 */
    fun deleteConversation(id: String): Boolean {
        val removed = conversations.remove(id)
        if (removed && conversationId == id) reset()
        return removed
    }

    /** port of source `send`：trim + 空/在途拦截，落 user 与空 assistant 气泡后置帧。 */
    fun send(rawPrompt: String): Boolean {
        val prompt = rawPrompt.trim()
        if (prompt.isEmpty() || isStreaming) return false

        lastUserPrompt = prompt
        lastError = null
        val context = AiStreamRepository.contextOf(messages)
        val userMsg = AiMessage(nextId(), AiRole.user, prompt, conversationId = conversationId)
        val assistantMsg = AiMessage(
            id = nextId(),
            role = AiRole.assistant,
            text = "",
            isStreaming = true,
            conversationId = conversationId,
        )
        messages = messages + userMsg + assistantMsg
        streamingId = assistantMsg.id
        isStreaming = true
        status = AiStreamStatus.Streaming

        val request = AiCompletionRequest(
            prompt = prompt,
            conversationId = conversationId,
            clientRequestId = "req_" + System.currentTimeMillis(),
            model = modelId,
            context = context,
        )
        val result = repository.streamCompletion(request)
        val frames = result.getOrNull()
        if (frames == null) {
            finish(result.exceptionOrNull()?.message ?: "生成失败，请重试")
            emit()
            return true
        }
        pending = frames
        pendingIndex = 0
        syncConversation()
        emit()
        return true
    }

    /**
     * 消费下一帧：meta 登记会话 id、delta 追加文本、done/error 收尾。
     * 返回是否仍在流式（页面据此决定是否继续排下一帧）。
     */
    fun pump(): Boolean {
        if (!isStreaming) return false
        if (pendingIndex >= pending.size) {
            pending = emptyList()
            isStreaming = false
            return false
        }
        val event = pending[pendingIndex]
        pendingIndex += 1
        when (event) {
            is AiStreamEvent.Meta -> {
                val cid = event.conversationId
                if (cid != null && cid.isNotEmpty()) {
                    conversationId = cid
                    conversations.ensure(cid, titleOf(), modelId)
                    syncConversation()
                }
                val model = event.model
                if (model != null && model.isNotEmpty()) serverModel = model
            }

            is AiStreamEvent.Delta -> patch(streamingId) { it.copy(text = it.text + event.text) }

            is AiStreamEvent.Done -> finish(null)

            is AiStreamEvent.Error -> finish(event.message)
        }
        emit()
        return isStreaming
    }

    /** port of source `stop`：取消在途请求，保留已生成的半截文本且静默收尾。 */
    fun stop(): Boolean {
        if (!isStreaming) return false
        finish(null)
        syncConversation()
        emit()
        return true
    }

    /** 错误重试：回退上一条失败回复与该轮提问后重发（port of design doc Phase 3 重试/重新生成）。 */
    fun retry(): Boolean {
        if (isStreaming) return false
        val prompt = lastUserPrompt
        if (prompt == null || prompt.isEmpty()) return false
        val trimmed = messages.toMutableList()
        while (trimmed.isNotEmpty() && trimmed.last().role != AiRole.user) {
            trimmed.removeAt(trimmed.size - 1)
        }
        if (trimmed.isNotEmpty()) trimmed.removeAt(trimmed.size - 1)
        messages = trimmed
        status = AiStreamStatus.Idle
        lastError = null
        syncConversation()
        return send(prompt)
    }

    private fun finish(errorMessage: String?) {
        pending = emptyList()
        pendingIndex = 0
        isStreaming = false
        status = if (errorMessage == null) AiStreamStatus.Done else AiStreamStatus.Error
        lastError = errorMessage
        patch(streamingId) { it.copy(isStreaming = false, errorMessage = errorMessage) }
        streamingId = null
        syncConversation()
    }

    private fun patch(id: String?, block: (AiMessage) -> AiMessage) {
        val target = id ?: return
        val index = messages.indexOfLast { it.id == target }
        if (index < 0) return
        val next = messages.toMutableList()
        next[index] = block(next[index])
        messages = next.toList()
    }

    private fun syncConversation() {
        val cid = conversationId
        if (cid == null) return
        conversations.replaceMessages(cid, messages.filter { it.role != AiRole.welcome })
    }

    private fun titleOf(): String {
        val first = messages.firstOrNull { it.role == AiRole.user }?.text ?: "新对话"
        return first.take(TITLE_MAX)
    }

    private fun welcomeMessage(): AiMessage = AiMessage(nextId(), AiRole.welcome, AiPrompts.WELCOME)

    private fun nextId(): String = "m_${++seq}"

    private fun emit() {
        onChanged?.invoke()
    }

    companion object {
        private const val TITLE_MAX = 12
    }
}
