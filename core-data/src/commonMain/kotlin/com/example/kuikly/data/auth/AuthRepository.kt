package com.example.kuikly.data.auth

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

data class User(val id: String, val name: String)

interface AuthRepository {
    fun currentUser(): User?
    fun isLoggedIn(): Boolean = currentUser() != null
    fun login(username: String, password: String): Result<User>
    fun loginWithOtp(phone: String, code: String): Result<User>
    fun logout()
}

class FakeAuthRepository : AuthRepository {
    private var session: User? = null

    override fun currentUser(): User? = session

    override fun login(username: String, password: String): Result<User> {
        if (MockBackend.scenario == MockScenario.Unauthorized) {
            return Result.failure(IllegalStateException("unauthorized"))
        }
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("username required"))
        }
        val user = User(id = "u1", name = username.trim())
        session = user
        return Result.success(user)
    }

    override fun loginWithOtp(phone: String, code: String): Result<User> {
        if (MockBackend.scenario == MockScenario.Unauthorized) {
            return Result.failure(IllegalStateException("unauthorized"))
        }
        val digits = phone.filter { it.isDigit() }
        if (digits != MOCK_PHONE) {
            return Result.failure(IllegalArgumentException("测试环境请使用 $MOCK_PHONE"))
        }
        if (code.trim() != MOCK_OTP) {
            return Result.failure(IllegalArgumentException("验证码错误"))
        }
        // Flutter mock uses `dev-` + test phone as logged-in display id; UI now
        // binds MineProfile.displayName from this name verbatim.
        val user = User(id = "u_otp", name = DISPLAY_ID_PREFIX + digits)
        session = user
        return Result.success(user)
    }

    override fun logout() {
        session = null
    }

    companion object {
        const val MOCK_PHONE = "13400000000"
        const val MOCK_OTP = "123456"
        /** Prefix for the mock logged-in display id (e.g. `dev-13400000000`). */
        const val DISPLAY_ID_PREFIX = "dev-"
    }
}

object AuthSession {
    val repo: AuthRepository = FakeAuthRepository()
}
