package com.example.kuikly.data.ai

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/**
 * port of source `ai/model/ai_stream_event.dart`：SSE 帧解包后的领域事件。
 *
 * - [AiStreamEvent.Meta]：流已接受，下发停留会话 id / 模型名（首句靠它拿到 conversationId）。
 * - [AiStreamEvent.Delta]：增量文本，逐段追加到气泡。
 * - [AiStreamEvent.Done]：正常结束，finishReason = stop / length / empty。
 * - [AiStreamEvent.Error]：流中失败，保留已生成文本并置错误态（可重试）。
 */
sealed class AiStreamEvent {
    data class Meta(
        val requestId: String? = null,
        val conversationId: String? = null,
        val model: String? = null,
        val clientRequestId: String? = null,
    ) : AiStreamEvent()

    data class Delta(val text: String) : AiStreamEvent()

    data class Done(val finishReason: String? = null) : AiStreamEvent()

    data class Error(val code: String, val message: String) : AiStreamEvent()
}

/** port of source `docs/sse-streaming-design.md` 6.4 的 AiStreamStatus。 */
enum class AiStreamStatus { Idle, Streaming, Done, Error }

/** port of source AiStreamRepository.streamCompletion 的入参。 */
data class AiCompletionRequest(
    val prompt: String,
    val conversationId: String? = null,
    val clientRequestId: String? = null,
    val model: String = AiModels.DEFAULT_ID,
    /** 多轮上下文（最近 N 轮 user + assistant）。 */
    val context: List<AiMessage> = emptyList(),
)

/**
 * port of source `ai/api/ai_sse_api.dart` + `ai/repository/ai_stream_repository.dart`。
 *
 * 源端是 Dart `Stream<AiStreamEvent>`；本工程无协程流式基建，改为**一次性产出帧序列**，
 * 由页面按 `chunkDelayMs` 逐帧播放（即打字机效果的帧源），语义与 SSE 帧序完全一致。
 */
class AiStreamRepository {

    /**
     * port of source controller._mapSseError：HTTP 流前错误码 → 用户文案。
     * 401/403 引导重新登录，429 限流，503 服务不可用。
     */
    fun mapSseError(statusCode: Int, fallback: String = "生成失败，请重试"): String = when (statusCode) {
        401, 403 -> "登录已失效，请重新登录"
        429 -> "请求过于频繁，请稍后再试"
        503 -> "服务暂不可用，请稍后再试"
        else -> fallback
    }

    /**
     * 生成一整条 SSE 帧序列。MockScenario：
     * Success / Slow 正常分片（Slow 分片更细）；Empty 只回 meta + done；
     * Error 先吐几片再发流中 error（保留半截文本，可重试）。
     */
    fun streamCompletion(request: AiCompletionRequest): Result<List<AiStreamEvent>> {
        if (request.prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("内容不能为空"))
        }
        val scenario = MockBackend.scenario
        val turns = request.context.count { it.role == AiRole.user }
        val reply = replyFor(request.prompt, turns)
        val frames = mutableListOf<AiStreamEvent>()
        frames.add(
            AiStreamEvent.Meta(
                requestId = nextId("sse"),
                conversationId = request.conversationId ?: nextId("conv"),
                model = request.model,
                clientRequestId = request.clientRequestId,
            ),
        )
        if (scenario == MockScenario.Empty) {
            frames.add(AiStreamEvent.Done("empty"))
            return Result.success(frames.toList())
        }
        if (scenario == MockScenario.Error) {
            reply.take(PARTIAL_ON_ERROR).chunked(ERROR_CHUNK).forEach { frames.add(AiStreamEvent.Delta(it)) }
            frames.add(
                AiStreamEvent.Error(
                    code = "upstream_timeout",
                    message = mapSseError(0, "模型上游超时，请稍后再试"),
                ),
            )
            return Result.success(frames.toList())
        }
        val size = if (scenario == MockScenario.Slow) SLOW_CHUNK else NORMAL_CHUNK
        reply.chunked(size).forEach { frames.add(AiStreamEvent.Delta(it)) }
        frames.add(AiStreamEvent.Done("stop"))
        return Result.success(frames.toList())
    }

    /** 业务向导的 canned 回复（port of source MockProvider 的按 prompt 路由）。 */
    private fun replyFor(prompt: String, turns: Int): String {
        val base = when {
            prompt.contains("二手车") ->
                "二手车入口在「首页 → 全部服务 → 二手车」，也可以在「我的」里点二手车卡片直接进入。"
            prompt.contains("登录") ->
                "登录用手机号 + 验证码：填手机号后获取验证码，5 分钟内回填即可；未登录时点 AI 入口会先弹登录再回跳。"
            prompt.contains("数据") || prompt.contains("分析") ->
                "看数据分析先读漏斗：曝光 → 点击 → 转化，先定位流失最大的一层，再下钻到单渠道看趋势。"
            else ->
                "我可以按业务向导的方式回答本 App 的问题，先从「二手车入口」「账号登录」「数据分析」这三块问起会更准。"
        }
        return if (turns > 0) "（已结合前 $turns 轮对话）$base" else base
    }

    private fun nextId(prefix: String): String = prefix + "_" + (++seq)

    private var seq = 0

    companion object {
        /** port of source 停留会话上下文：最近 10 轮。 */
        const val MAX_CONTEXT_TURNS = 10

        /** port of source AiSseApi.completionsPath。 */
        const val COMPLETIONS_PATH = "/api/v1/sse/completions"

        private const val NORMAL_CHUNK = 3
        private const val SLOW_CHUNK = 2
        private const val ERROR_CHUNK = 3

        /** Error 态保留的半截文本长度（port of source 测试「error keeps partial text」）。 */
        private const val PARTIAL_ON_ERROR = 6

        /** 取最近 maxTurns 轮上下文，welcome 气泡不入上下文。 */
        fun contextOf(messages: List<AiMessage>, maxTurns: Int = MAX_CONTEXT_TURNS): List<AiMessage> =
            messages.filter { it.role != AiRole.welcome }.takeLast(maxTurns * 2)
    }
}
