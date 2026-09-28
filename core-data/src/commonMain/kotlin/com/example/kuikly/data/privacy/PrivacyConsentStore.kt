package com.example.kuikly.data.privacy

/**
 * In-process privacy consent for Mock / first Product Vertical Slice.
 * Not durable across process death — enough for tests and demo sessions.
 */
object PrivacyConsentStore {
    private var granted: Boolean = false

    fun isGranted(): Boolean = granted

    fun grant() {
        granted = true
    }

    fun revoke() {
        granted = false
    }
}
