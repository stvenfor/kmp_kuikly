package com.example.kuikly.data.dubbing

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** Flutter `DubbingWorkItem`（`dubbing_media_mock_data.dart`）。 */
data class DubbingWorkItem(
    val id: String,
    val title: String,
    val authorName: String,
    val likeCount: Int,
    val commentCount: Int,
    val publishedAt: String,
    val location: String,
    val badge: String? = null,
    val duration: String? = null,
)

interface DubbingWorkRepository {
    fun list(): Result<List<DubbingWorkItem>>
    fun detail(id: String): Result<DubbingWorkItem>
}

/**
 * Mock dubbing-work list + detail, mirroring Flutter
 * `features/video/lib/dubbing/mock/dubbing_media_mock_data.dart` `works` seed。
 */
class FakeDubbingWorkRepository : DubbingWorkRepository {
    private val seed = listOf(
        DubbingWorkItem(
            id = "work_1",
            title = "你好世界，这里是中国！",
            authorName = "小趣友宁Sir",
            likeCount = 11000,
            commentCount = 22,
            publishedAt = "2023-11-23",
            location = "北京",
            badge = "精选",
            duration = "1:10",
        ),
        DubbingWorkItem(
            id = "work_2",
            title = "【RP女音】赫敏：It is LeviOsa, not LevioSA!",
            authorName = "唯有爱与美不可辜...",
            likeCount = 9000,
            commentCount = 27,
            publishedAt = "2023-11-22",
            location = "北京",
            badge = "高秀",
            duration = "2:15",
        ),
        DubbingWorkItem(
            id = "work_3",
            title = "美诺明年夏天见 · 配音作品",
            authorName = "美诺明年夏天见",
            likeCount = 7000,
            commentCount = 32,
            publishedAt = "2023-11-21",
            location = "杭州市",
            duration = "3:20",
        ),
    )

    override fun list(): Result<List<DubbingWorkItem>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock dubbing work list error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun detail(id: String): Result<DubbingWorkItem> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock dubbing work detail error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: seed.firstOrNull()?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no dubbing work $id"))
    }
}

object DubbingWorkStore {
    val repo: DubbingWorkRepository = FakeDubbingWorkRepository()
}
