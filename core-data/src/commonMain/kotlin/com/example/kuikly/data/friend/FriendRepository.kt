package com.example.kuikly.data.friend

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

data class Friend(
    val id: String,
    val name: String,
    val bio: String,
)

interface FriendRepository {
    fun list(): Result<List<Friend>>
    fun detail(id: String): Result<Friend>
}

class FakeFriendRepository : FriendRepository {
    private val seed = listOf(
        Friend("1", "张伟", "爱跑步的程序员，周末常去滨江大道"),
        Friend("2", "李娜", "咖啡与爵士乐爱好者"),
        Friend("3", "王强", "二手车行老板，兼职摄影"),
        Friend("4", "赵敏", "幼儿园老师，喜欢手工"),
        Friend("5", "刘洋", "健身教练 · 三餐规律派"),
        Friend("6", "陈静", "小说编辑，养了一只橘猫"),
    )

    override fun list(): Result<List<Friend>> {
        return when (MockBackend.scenario) {
            MockScenario.Error -> Result.failure(IllegalStateException("mock friend list error"))
            MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
            MockScenario.Empty -> Result.success(emptyList())
            MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        }
    }

    override fun detail(id: String): Result<Friend> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock friend detail error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no friend $id"))
    }
}

object FriendStore {
    val repo: FriendRepository = FakeFriendRepository()
}
