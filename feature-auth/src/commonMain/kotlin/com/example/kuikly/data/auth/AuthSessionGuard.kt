package com.example.kuikly.data.auth

/**
 * Mock 会话守卫：镜像源 `SessionGuardHook` + `AuthTokenRefreshInterceptor`。
 * 源区分两类失效：401 可静默 refresh（[refresh]），互踢/会话失效则强制重新登录（[forceLogout]）。
 * 本 mock 只保留「过期标志」与「刷新成功/失败」两个态，供 `TokenExpired` 页演示，
 * 不触碰 core-data 的 `FakeAuthRepository`（其无 token 过期概念）。
 */
object MockSessionGuard {
    /** 是否处于「已过期需重新登录」态（源 `SessionGuardHook.shouldForceLogout`）。 */
    var expired: Boolean = false
        private set

    /** 最近一次刷新结果提示，UI 展示用。 */
    var lastMessage: String = ""
        private set

    /** 模拟 Token 过期（源 `SessionGuardHook.shouldForceLogout` 命中）。 */
    fun simulateExpiry() {
        expired = true
        lastMessage = "登录已失效，请重新登录"
    }

    /**
     * 静默刷新会话（源 `AuthTokenRefreshInterceptor._refreshTokens`）。
     * 仅当本地仍有登录态时成功，否则视为 refresh_token 已废。
     */
    fun refresh(): Boolean {
        val user = AuthSession.repo.currentUser()
        return if (user != null) {
            expired = false
            lastMessage = "会话已刷新"
            true
        } else {
            lastMessage = "登录已失效，请重新登录"
            false
        }
    }

    /** 强制登出并清态（源 `SessionGuardHook._clearLocalSession`）。 */
    fun forceLogout() {
        AuthSession.repo.logout()
        expired = false
        lastMessage = ""
    }

    /** 派生展示用 token（源 `LoginSession.token`）。 */
    fun tokenOf(user: User?): String = if (user != null) "mock-token-${user.id}" else ""
}
