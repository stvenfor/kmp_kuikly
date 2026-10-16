package com.example.kuikly.navigation

/**
 * One-shot Main tab selection seed for login redirect and golden pageData priming.
 */
object MainTabLaunch {
    enum class Tab { Home, Chat, Community, Me }

    private var pending: Tab? = null

    /** True when a Me seed is pending (tests / callers). */
    val selectMe: Boolean
        get() = pending == Tab.Me

    fun requestMe() {
        pending = Tab.Me
    }

    fun requestChat() {
        pending = Tab.Chat
    }

    fun requestCommunity() {
        pending = Tab.Community
    }

    fun request(tab: Tab) {
        pending = tab
    }

    fun consumeMe(): Boolean {
        if (pending != Tab.Me) return false
        pending = null
        return true
    }

    fun consume(): Tab? {
        val value = pending
        pending = null
        return value
    }

    fun clear() {
        pending = null
    }
}
