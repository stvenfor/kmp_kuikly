package com.example.kuikly.data.live

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

data class LiveRoom(
    val id: String,
    val title: String,
    val host: String,
    val viewerCount: Int,
)

interface LiveRepository {
    fun list(): Result<List<LiveRoom>>
    fun detail(id: String): Result<LiveRoom>
}

/**
 * Mock live room list + detail (no real streaming / player).
 * 直播间列表 → 直播间详情，纯 mock 数据。
 */
class FakeLiveRepository : LiveRepository {
    private val seed = listOf(
        LiveRoom("1", "城市夜景慢直播", "主播小夜", 1204),
        LiveRoom("2", "海边日落直播", "主播阿澜", 867),
        LiveRoom("3", "深夜电台陪伴直播", "主播老周", 2350),
        LiveRoom("4", "萌宠日常直播间", "主播喵喵", 512),
    )

    override fun list(): Result<List<LiveRoom>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock live list error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun detail(id: String): Result<LiveRoom> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock live detail error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no live room $id"))
    }
}

object LiveStore {
    val repo: LiveRepository = FakeLiveRepository()
}
