package com.example.kuikly.data.feed

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

data class FeedItem(val id: String, val title: String, val body: String)

interface FeedRepository {
    fun list(): Result<List<FeedItem>>
    fun detail(id: String): Result<FeedItem>
}

class FakeFeedRepository : FeedRepository {
    private val seed = listOf(
        FeedItem("1", "Welcome", "First mock feed item"),
        FeedItem("2", "Kuikly Compose", "Vertical slice demo"),
        FeedItem("3", "Mock Backend", "Toggle Success / Empty / Error"),
    )

    override fun list(): Result<List<FeedItem>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock feed error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun detail(id: String): Result<FeedItem> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock feed error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no item $id"))
    }
}

object FeedStore {
    val repo: FeedRepository = FakeFeedRepository()
}
