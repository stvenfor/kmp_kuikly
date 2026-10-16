package com.example.kuikly.data.auth

/**
 * Authenticated user (port of source `UserProfile`/`UserStoreStats`).
 * `id`/`name` kept for backward compatibility with existing LoginPage/FeedListPage.
 */
data class User(
    val id: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val storeName: String = "",
    val role: Int = 0, // 0=销售顾问 1=销售经理 2=总经理
)

data class StoreItem(
    val storeId: Int,
    val storeName: String,
    val role: Int = 0,
    val roleLabel: String = "",
    val isCurrent: Boolean = false,
)

interface AuthRepository {
    fun currentUser(): User?

    /** 便捷判断：是否已登录（Main / 我的 Tab 的访客态判断用）。 */
    fun isLoggedIn(): Boolean = currentUser() != null

    fun login(username: String, password: String): Result<User>
    fun otpLogin(phone: String, code: String): Result<User>

    /** OTP 登录别名（demo-skeleton 的 Main 框架用名），与 [otpLogin] 等价。 */
    fun loginWithOtp(phone: String, code: String): Result<User> = otpLogin(phone, code)

    fun register(username: String, password: String): Result<User>
    fun logout()
    fun stores(): Result<List<StoreItem>>
    fun switchStore(storeId: Int): Result<User>
}

class FakeAuthRepository : AuthRepository {
    private var session: User? = null
    private val stores = listOf(
        StoreItem(1, "北京旗舰店", 2, "总经理", true),
        StoreItem(2, "上海中心店", 1, "销售经理", false),
        StoreItem(3, "广州天河店", 0, "销售顾问", false),
    )

    /** Mock OTP 登录常量（golden 捕获脚本与 Main 框架的 mockLogin 用）。 */
    companion object {
        const val MOCK_PHONE = "13400000000"
        const val MOCK_OTP = "123456"
        const val DISPLAY_ID_PREFIX = "dev-"
    }

    override fun currentUser(): User? = session

    override fun login(username: String, password: String): Result<User> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("username required"))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("password too short"))
        }
        val user = User(
            id = "u1", name = username.trim(),
            email = "$username@demo.com", phone = "138****0001",
            avatarUrl = "", storeName = "北京旗舰店", role = 2,
        )
        session = user
        return Result.success(user)
    }

    override fun otpLogin(phone: String, code: String): Result<User> {
        if (phone.length != 11 || code != "123456") {
            return Result.failure(IllegalArgumentException("invalid phone or code"))
        }
        val user = User(id = "u2", name = "OTP-$phone", phone = phone, storeName = "上海中心店", role = 1)
        session = user
        return Result.success(user)
    }

    override fun register(username: String, password: String): Result<User> {
        if (username.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("invalid username/password"))
        }
        val user = User(id = "u3", name = username.trim(), email = "$username@demo.com", storeName = "广州天河店", role = 0)
        session = user
        return Result.success(user)
    }

    override fun logout() {
        session = null
    }

    override fun stores(): Result<List<StoreItem>> = Result.success(stores)

    override fun switchStore(storeId: Int): Result<User> {
        val target = stores.find { it.storeId == storeId } ?: return Result.failure(NoSuchElementException("store=$storeId"))
        val cur = session ?: return Result.failure(IllegalStateException("not logged in"))
        session = cur.copy(storeName = target.storeName, role = target.role, avatarUrl = "")
        return Result.success(session!!)
    }
}

object AuthSession {
    val repo: AuthRepository = FakeAuthRepository()
}
