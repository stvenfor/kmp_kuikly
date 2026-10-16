package com.example.kuikly.data.mine

import com.example.kuikly.data.auth.AuthRepository
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.auth.User

data class MineStats(
    val customerCount: Int,
    val orderCount: Int,
    val followUpCount: Int,
)

data class MineProfile(
    val isGuest: Boolean,
    val displayName: String,
    val roleBadge: String?,
    val storeLine: String?,
    val maskedPhone: String?,
    val stats: MineStats,
)

data class MineQuickService(
    val key: String,
    val label: String,
)

data class MineMenuItem(
    val key: String,
    val label: String,
)

data class MineFunctionItem(
    val key: String,
    val label: String,
)

interface MineRepository {
    fun profile(): Result<MineProfile>
    fun quickServices(): List<MineQuickService>
    fun menu(): List<MineMenuItem>
    fun functions(): List<MineFunctionItem>
}

class FakeMineRepository(
    private val authRepository: AuthRepository = AuthSession.repo,
) : MineRepository {
    override fun profile(): Result<MineProfile> {
        val user = authRepository.currentUser()
        val profile = if (user == null) guestProfile() else loggedInProfile(user)
        return Result.success(profile)
    }

    override fun quickServices(): List<MineQuickService> = listOf(
        MineQuickService(key = "mall", label = "商城"),
        MineQuickService(key = "wallet", label = "我的钱包"),
        MineQuickService(key = "courses", label = "我的课程"),
        MineQuickService(key = "orders", label = "我的订单"),
    )

    override fun menu(): List<MineMenuItem> = listOf(
        MineMenuItem(key = "business", label = "商务合作"),
        MineMenuItem(key = "reminders", label = "提醒事项"),
        MineMenuItem(key = "invite", label = "邀请好友"),
        MineMenuItem(key = "fan_group", label = "粉丝群"),
        MineMenuItem(key = "feedback", label = "意见反馈"),
        MineMenuItem(key = "settings", label = "设置"),
    )

    override fun functions(): List<MineFunctionItem> = listOf(
        MineFunctionItem(key = "sales_report", label = "销售报表"),
        MineFunctionItem(key = "customers", label = "客户管理"),
        MineFunctionItem(key = "inventory", label = "我的车源"),
        MineFunctionItem(key = "training", label = "培训中心"),
        MineFunctionItem(key = "ranking", label = "业绩排行"),
    )

    private fun guestProfile(): MineProfile = MineProfile(
        isGuest = true,
        displayName = GUEST_DISPLAY_NAME,
        roleBadge = null,
        storeLine = null,
        maskedPhone = null,
        stats = MineStats(customerCount = 0, orderCount = 0, followUpCount = 0),
    )

    private fun loggedInProfile(user: User): MineProfile {
        val phone = user.name.takeIf { it.isNotBlank() }?.let(::digitsOnly)?.takeIf { it.length >= 7 }
        return MineProfile(
            isGuest = false,
            displayName = user.name.ifBlank { GUEST_DISPLAY_NAME },
            roleBadge = ROLE_BADGE,
            storeLine = STORE_LINE,
            maskedPhone = phone?.let(::maskPhone),
            stats = MineStats(
                customerCount = LOGGED_IN_CUSTOMER_COUNT,
                orderCount = LOGGED_IN_ORDER_COUNT,
                followUpCount = LOGGED_IN_FOLLOW_UP_COUNT,
            ),
        )
    }

    private fun digitsOnly(input: String): String = buildString {
        for (c in input) if (c.isDigit()) append(c)
    }

    private fun maskPhone(phone: String): String {
        // 13400000000 -> 134****0000 (3 visible + 4 mask + 4 visible)
        if (phone.length < 7) return phone
        return phone.substring(0, 3) + "****" + phone.substring(phone.length - 4)
    }

    companion object {
        const val GUEST_DISPLAY_NAME = "未登录"
        const val ROLE_BADGE = "销售经理"
        const val STORE_LINE = "上海浦东门店"
        const val LOGGED_IN_CUSTOMER_COUNT = 128
        const val LOGGED_IN_ORDER_COUNT = 56
        const val LOGGED_IN_FOLLOW_UP_COUNT = 12
    }
}

object MineStore {
    val repo: MineRepository = FakeMineRepository()
}