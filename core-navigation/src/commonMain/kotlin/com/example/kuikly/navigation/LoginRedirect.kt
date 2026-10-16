package com.example.kuikly.navigation

/**
 * Pending route after modal login (used-car gate). Consume once after success.
 */
object LoginRedirect {
    var pendingRoute: String? = null
        private set

    fun setPending(route: String) {
        pendingRoute = route
    }

    fun peek(): String? = pendingRoute

    fun consume(): String? {
        val value = pendingRoute
        pendingRoute = null
        return value
    }

    fun clear() {
        pendingRoute = null
    }
}
