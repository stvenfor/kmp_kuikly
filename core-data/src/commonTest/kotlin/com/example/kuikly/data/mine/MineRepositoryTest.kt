package com.example.kuikly.data.mine

import com.example.kuikly.data.auth.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MineRepositoryTest {
    private lateinit var auth: FakeAuthRepository
    private lateinit var mine: FakeMineRepository

    private fun newRepo(): FakeMineRepository {
        auth = FakeAuthRepository()
        mine = FakeMineRepository(authRepository = auth)
        return mine
    }

    @Test
    fun guest_profile_when_not_logged_in() {
        val mine = newRepo()
        assertFalse(auth.isLoggedIn())

        val profile = mine.profile().getOrThrow()

        assertTrue(profile.isGuest)
        assertEquals(FakeMineRepository.GUEST_DISPLAY_NAME, profile.displayName)
        assertNull(profile.roleBadge)
        assertNull(profile.storeLine)
        assertNull(profile.maskedPhone)
        assertEquals(0, profile.stats.customerCount)
        assertEquals(0, profile.stats.orderCount)
        assertEquals(0, profile.stats.followUpCount)
    }

    @Test
    fun logged_in_profile_after_otp_login() {
        val mine = newRepo()
        val user = auth.loginWithOtp(
            phone = FakeAuthRepository.MOCK_PHONE,
            code = FakeAuthRepository.MOCK_OTP,
        ).getOrThrow()
        assertEquals(FakeAuthRepository.DISPLAY_ID_PREFIX + FakeAuthRepository.MOCK_PHONE, user.name)

        val profile = mine.profile().getOrThrow()

        assertFalse(profile.isGuest)
        assertEquals(FakeAuthRepository.DISPLAY_ID_PREFIX + FakeAuthRepository.MOCK_PHONE, profile.displayName)
        assertEquals(FakeMineRepository.ROLE_BADGE, profile.roleBadge)
        assertEquals(FakeMineRepository.STORE_LINE, profile.storeLine)
        assertNotNull(profile.maskedPhone)
        // maskedPhone must still strip the `dev-` prefix and mask the digits only.
        assertEquals("134****0000", profile.maskedPhone)
        assertFalse(profile.maskedPhone!!.contains(FakeAuthRepository.MOCK_PHONE))
        assertFalse(profile.maskedPhone!!.contains(FakeAuthRepository.DISPLAY_ID_PREFIX))
        assertEquals(FakeMineRepository.LOGGED_IN_CUSTOMER_COUNT, profile.stats.customerCount)
        assertEquals(FakeMineRepository.LOGGED_IN_ORDER_COUNT, profile.stats.orderCount)
        assertEquals(FakeMineRepository.LOGGED_IN_FOLLOW_UP_COUNT, profile.stats.followUpCount)
    }

    @Test
    fun logged_in_profile_after_password_login_has_no_phone() {
        val mine = newRepo()
        auth.login("alex", "x").getOrThrow()

        val profile = mine.profile().getOrThrow()

        assertFalse(profile.isGuest)
        assertEquals("alex", profile.displayName)
        assertEquals(FakeMineRepository.ROLE_BADGE, profile.roleBadge)
        assertEquals(FakeMineRepository.STORE_LINE, profile.storeLine)
        assertNull(profile.maskedPhone)
    }

    @Test
    fun logout_returns_to_guest_profile() {
        val mine = newRepo()
        auth.loginWithOtp(
            phone = FakeAuthRepository.MOCK_PHONE,
            code = FakeAuthRepository.MOCK_OTP,
        ).getOrThrow()
        assertFalse(mine.profile().getOrThrow().isGuest)

        auth.logout()
        assertFalse(auth.isLoggedIn())

        val profile = mine.profile().getOrThrow()
        assertTrue(profile.isGuest)
        assertEquals(FakeMineRepository.GUEST_DISPLAY_NAME, profile.displayName)
        assertNull(profile.maskedPhone)
    }

    @Test
    fun quick_services_match_source_labels() {
        val mine = newRepo()
        val labels = mine.quickServices().map { it.label }
        assertEquals(listOf("商城", "我的钱包", "我的课程", "我的订单"), labels)
        assertEquals(4, mine.quickServices().size)
    }

    @Test
    fun menu_matches_source_labels() {
        val mine = newRepo()
        val labels = mine.menu().map { it.label }
        assertEquals(
            listOf("商务合作", "提醒事项", "邀请好友", "粉丝群", "意见反馈", "设置"),
            labels,
        )
        assertEquals(6, mine.menu().size)
    }

    @Test
    fun functions_non_empty_with_unique_keys() {
        val mine = newRepo()
        val items = mine.functions()
        assertTrue(items.isNotEmpty())
        assertEquals(items.size, items.map { it.key }.toSet().size)
        assertTrue(items.all { it.label.isNotBlank() })
    }

    @Test
    fun catalogs_independent_of_auth_state() {
        val mine = newRepo()
        val guestQuick = mine.quickServices()
        val guestMenu = mine.menu()
        val guestFunctions = mine.functions()

        auth.loginWithOtp(
            phone = FakeAuthRepository.MOCK_PHONE,
            code = FakeAuthRepository.MOCK_OTP,
        ).getOrThrow()

        assertEquals(guestQuick, mine.quickServices())
        assertEquals(guestMenu, mine.menu())
        assertEquals(guestFunctions, mine.functions())
    }
}