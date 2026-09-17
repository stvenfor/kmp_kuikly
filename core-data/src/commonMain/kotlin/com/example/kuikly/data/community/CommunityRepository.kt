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
    private val seed = listOf(
        Post(
            id = "1",
            author = "张三",
            title = "今天去了推荐的咖啡店",
            body = "环境不错，适合写代码。\n#Kuikly开发",
            likeCount = 42,
            commentCount = 6,
        ),
        Post(
            id = "2",
            author = "李四",
            title = "周末 hiking",
            body = "天气太好了！#户外",
            likeCount = 18,
            commentCount = 3,
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
