package com.example.kuikly.data

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** Generic paged result returned by every mock repository. */
data class Paged<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
) {
    val hasMore: Boolean get() = page * pageSize < total
    val isEmpty: Boolean get() = items.isEmpty()
}

/**
 * Reusable synchronous mock repository base. Mirrors the hand-rolled
 * [com.example.kuikly.data.analytics.FakeAnalyticsRepository] pattern but removes
 * the per-feature boilerplate. Honours [MockBackend.scenario]:
 * Success/Empty -> list (Empty yields zero rows), Error -> failure, Slow -> same as Success.
 */
abstract class BaseMockRepository<T : Any> {
    protected open val scenario: MockScenario
        get() = MockBackend.scenario

    protected abstract fun seed(): List<T>
    protected abstract fun idOf(item: T): String

    fun page(
        page: Int,
        pageSize: Int = 20,
        filter: (T) -> Boolean = { true },
    ): Result<Paged<T>> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val all = if (scenario == MockScenario.Empty) emptyList() else seed().filter(filter)
        val from = (page - 1).coerceAtLeast(0) * pageSize
        val to = (from + pageSize).coerceAtMost(all.size)
        val slice = if (from <= all.size) all.subList(from, to) else emptyList()
        return Result.success(Paged(slice, all.size, page, pageSize))
    }

    fun byId(id: String): Result<T> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        return seed().firstOrNull { idOf(it) == id }
            ?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("id=$id"))
    }

    fun all(filter: (T) -> Boolean = { true }): Result<List<T>> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val list = if (scenario == MockScenario.Empty) emptyList() else seed().filter(filter)
        return Result.success(list)
    }
}
