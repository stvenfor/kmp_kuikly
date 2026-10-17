package com.example.kuikly.data.ai

import com.example.kuikly.data.BaseMockRepository

/**
 * port of source `docs/sse-streaming-design.md` 4.2 请求体 `options.model`
 * 与 config.yaml（`provider: mock` / `openai_compatible` + `model: gpt-4o-mini`）。
 */
data class AiModel(
    val id: String,
    val name: String,
    val desc: String,
    val tag: String,
)

object AiModels {
    const val DEFAULT_ID = "mock-v1"

    val all = listOf(
        AiModel(
            id = "mock-v1",
            name = "Mock v1",
            desc = "内置 Mock Provider，按块切分推送，用于联调与验收",
            tag = "默认",
        ),
        AiModel(
            id = "gpt-4o-mini",
            name = "GPT-4o mini",
            desc = "OpenAI 兼容 Provider，需服务端配置密钥后启用",
            tag = "均衡",
        ),
    )

    fun nameOf(id: String): String = all.firstOrNull { it.id == id }?.name ?: id
}

class AiModelRepository : BaseMockRepository<AiModel>() {
    override fun seed(): List<AiModel> = AiModels.all
    override fun idOf(item: AiModel): String = item.id
}
