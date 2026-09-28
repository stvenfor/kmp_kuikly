package com.example.kuikly.data

import com.example.kuikly.data.auth.FakeAuthRepository
import com.example.kuikly.data.feed.FakeFeedRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import com.example.kuikly.data.privacy.PrivacyConsentStore
import com.example.kuikly.data.usedcar.FakeUsedCarRepository
import com.example.kuikly.data.usedcar.formatAmount
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockAuthFeedTest {
    private lateinit var auth: FakeAuthRepository
    private lateinit var feed: FakeFeedRepository
    private lateinit var usedCar: FakeUsedCarRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        PrivacyConsentStore.revoke()
        auth = FakeAuthRepository()
        feed = FakeFeedRepository()
        usedCar = FakeUsedCarRepository()
    }

    @Test
    fun login_sets_session() {
        val user = auth.login("demo", "x").getOrThrow()
        assertEquals("demo", user.name)
        assertEquals("demo", auth.currentUser()?.name)
    }

    @Test
    fun password_login_success() {
        val user = auth.login("demo@example.com", "secret").getOrThrow()
        assertEquals("demo@example.com", user.name)
        assertTrue(auth.isLoggedIn())
    }

    @Test
    fun password_login_blank_username_fails() {
        assertTrue(auth.login("   ", "secret").isFailure)
        assertFalse(auth.isLoggedIn())
    }

    @Test
    fun password_login_unauthorized_fails() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(auth.login("demo@example.com", "secret").isFailure)
        assertFalse(auth.isLoggedIn())
    }

    @Test
    fun otp_login_success() {
        val user = auth.loginWithOtp(FakeAuthRepository.MOCK_PHONE, FakeAuthRepository.MOCK_OTP).getOrThrow()
        assertEquals(FakeAuthRepository.DISPLAY_ID_PREFIX + FakeAuthRepository.MOCK_PHONE, user.name)
        assertEquals("u_otp", user.id)
        assertTrue(auth.isLoggedIn())
    }

    @Test
    fun otp_login_wrong_code_fails() {
        assertTrue(auth.loginWithOtp(FakeAuthRepository.MOCK_PHONE, "000000").isFailure)
        assertFalse(auth.isLoggedIn())
    }

    @Test
    fun privacy_grant_and_revoke() {
        assertFalse(PrivacyConsentStore.isGranted())
        PrivacyConsentStore.grant()
        assertTrue(PrivacyConsentStore.isGranted())
        PrivacyConsentStore.revoke()
        assertFalse(PrivacyConsentStore.isGranted())
    }

    @Test
    fun used_car_success_has_items() {
        MockBackend.scenario = MockScenario.Success
        // 镜像 Flutter TransactionMockData：3 条交易记录
        val page = usedCar.listPage(0, pageSize = 2).getOrThrow()
        assertEquals(2, page.items.size)
        assertTrue(page.hasMore)
    }

    @Test
    fun used_car_empty() {
        MockBackend.scenario = MockScenario.Empty
        val page = usedCar.listPage(0).getOrThrow()
        assertTrue(page.items.isEmpty())
        assertFalse(page.hasMore)
    }

    @Test
    fun used_car_error() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(usedCar.listPage(0).isFailure)
    }

    @Test
    fun used_car_unauthorized() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(usedCar.listPage(0).isFailure)
        assertTrue(auth.loginWithOtp(FakeAuthRepository.MOCK_PHONE, FakeAuthRepository.MOCK_OTP).isFailure)
    }

    @Test
    fun used_car_detail() {
        MockBackend.scenario = MockScenario.Success
        val detail = usedCar.detail("1").getOrThrow()
        assertEquals("宝马 320Li", detail.category)
        assertEquals("¥ 22.80 万", formatAmount(detail.amount))
    }

    @Test
    fun feed_success_has_items() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(3, feed.list().getOrThrow().size)
    }

    @Test
    fun feed_empty() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(feed.list().getOrThrow().isEmpty())
    }

    @Test
    fun feed_error() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(feed.list().isFailure)
    }

    @Test
    fun cycle_scenario_includes_unauthorized() {
        MockBackend.scenario = MockScenario.Success
        assertEquals(MockScenario.Empty, MockBackend.cycle())
        assertEquals(MockScenario.Error, MockBackend.cycle())
        assertEquals(MockScenario.Unauthorized, MockBackend.cycle())
        assertEquals(MockScenario.Slow, MockBackend.cycle())
        assertEquals(MockScenario.Success, MockBackend.cycle())
    }
}
