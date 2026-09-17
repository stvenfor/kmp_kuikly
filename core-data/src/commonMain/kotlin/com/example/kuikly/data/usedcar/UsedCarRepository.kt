package com.example.kuikly.data.usedcar

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/**
 * 二手车域 Mock 记录。Migration Source（`transaction_model.dart` +
 * `transaction_mock_data.dart`）里 homeUsedCar 路由实际承载的是交易记录：
 * type income/expense、category 车型名、amount 金额、date 日期、note 备注。
 */
data class UsedCarItem(
    val id: String,
    val type: String,
    val category: String,
    val amount: Double,
    val date: String,
    val note: String?,
)

/** `TransactionListItem.formatAmount` 镜像：≥1 万按「¥ x.xx 万」展示。 */
fun formatAmount(amount: Double): String =
    if (kotlin.math.abs(amount) >= 10000) {
        "¥ ${"%.2f".format(amount / 10000)} 万"
    } else {
        "¥ ${"%.2f".format(amount)}"
    }

data class UsedCarPage(
    val items: List<UsedCarItem>,
    val hasMore: Boolean,
)

interface UsedCarRepository {
    fun listPage(page: Int, pageSize: Int = 20): Result<UsedCarPage>
    fun detail(id: String): Result<UsedCarItem>
}

class FakeUsedCarRepository : UsedCarRepository {
    // 镜像 TransactionMockData._all
    private val seed = listOf(
        UsedCarItem("1", "income", "宝马 320Li", 228000.0, "2024-05-10", "一手车，全程4S保养"),
        UsedCarItem("2", "expense", "奥迪 A4L", 185000.0, "2024-05-08", "置换收购，待整备"),
        UsedCarItem("3", "income", "丰田 凯美瑞", 168000.0, "2024-05-05", "2021款 2.0G 豪华版"),
    )

    override fun listPage(page: Int, pageSize: Int): Result<UsedCarPage> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock used-car list error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            MockScenario.Empty -> return Result.success(UsedCarPage(emptyList(), hasMore = false))
            MockScenario.Success, MockScenario.Slow -> Unit
        }
        if (page < 0) return Result.failure(IllegalArgumentException("page must be >= 0"))
        val from = page * pageSize
        if (from >= seed.size) {
            return Result.success(UsedCarPage(emptyList(), hasMore = false))
        }
        val to = minOf(from + pageSize, seed.size)
        val slice = seed.subList(from, to)
        return Result.success(UsedCarPage(items = slice, hasMore = to < seed.size))
    }

    override fun detail(id: String): Result<UsedCarItem> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock used-car detail error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no used-car $id"))
    }
}

object UsedCarStore {
    val repo: UsedCarRepository = FakeUsedCarRepository()
}
