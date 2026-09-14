package com.example.kuikly.data.auth

data class User(val id: String, val name: String)

interface AuthRepository {
    fun currentUser(): User?
    fun login(username: String, password: String): Result<User>
    fun logout()
}

class FakeAuthRepository : AuthRepository {
    private var session: User? = null

    override fun currentUser(): User? = session

    override fun login(username: String, password: String): Result<User> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("username required"))
        }
        val user = User(id = "u1", name = username.trim())
        session = user
        return Result.success(user)
    }

    override fun logout() {
        session = null
    }
}

object AuthSession {
    val repo: AuthRepository = FakeAuthRepository()
}
