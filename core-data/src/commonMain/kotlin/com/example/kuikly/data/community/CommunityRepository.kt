package com.example.kuikly.data.community

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

data class Post(
    val id: String,
    val author: String,
    val title: String,
    val body: String,
    val likeCount: Int,
    val commentCount: Int,
)

interface CommunityRepository {
    fun list(): Result<List<Post>>
    fun detail(id: String): Result<Post>
}

class FakeCommunityRepository : CommunityRepository {
    // 镜像 Flutter `13-flutter-main-community` 真实截图（ADR-0018 P2-R1b）。
    // 富文本样式由 `feature-home/CommunityTab.PostContent` 解析 `@张三` / `#Flutter开发` /
    // `https://flutter.dev`；只动 seed 文案与计数，Post 字段保持最小集（不加 timeLabel /
    // sourceLabel / liked / mediaKind，避免与 P2-R1a UI 票撞字段）。
    private val seed = listOf(
        Post(
            id = "1",
            author = "张三",
            title = "Flutter 跨端开发",
            body = "新的跨端框架体验：@张三 强力推荐 #Flutter开发 官方文档 https://flutter.dev 写得很详细",
            likeCount = 158,
            commentCount = 6,
        ),
        Post(
            id = "2",
            author = "李四",
            title = "周末 hiking",
            body = "天气太好了！周末 hiking… #户外",
            likeCount = 77,
            commentCount = 2,
        ),
        Post(
            id = "3",
            author = "开发者",
            title = "项目上线啦",
            body = "感谢团队！跨端一套代码四端运行。",
            likeCount = 128,
            commentCount = 21,
        ),
    )

    override fun list(): Result<List<Post>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock community error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun detail(id: String): Result<Post> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock community error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no post $id"))
    }
}

object CommunityStore {
    val repo: CommunityRepository = FakeCommunityRepository()
}
