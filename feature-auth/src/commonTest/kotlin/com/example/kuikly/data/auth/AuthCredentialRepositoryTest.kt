package com.example.kuikly.data.auth

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthCredentialRepositoryTest {

    private fun newRepo(): AuthCredentialRepository {
        MockBackend.scenario = MockScenario.Success
        return AuthCredentialRepository()
    }

    // ---------- 账号密码登录 ----------

    @Test
    fun `login with email succeeds`() {
        val repo = newRepo()
        val session = repo.loginWithPassword("demo@demo.com", "123456").getOrThrow()
        assertEquals("demo@demo.com", session.account.email)
        assertEquals(AuthRules.MODE_PASSWORD, session.mode)
        assertTrue(repo.isLoggedIn())
        assertNotNull(repo.currentSession())
    }

    @Test
    fun `login with phone succeeds`() {
        val repo = newRepo()
        val session = repo.loginWithPassword("13800000001", "a123456").getOrThrow()
        assertEquals("manager@demo.com", session.account.email)
    }

    @Test
    fun `login fails on wrong password`() {
        val repo = newRepo()
        assertTrue(repo.loginWithPassword("demo@demo.com", "654321").isFailure)
        assertFalse(repo.isLoggedIn())
    }

    @Test
    fun `login fails on unknown account`() {
        val repo = newRepo()
        assertTrue(repo.loginWithPassword("nobody@demo.com", "123456").isFailure)
        assertTrue(repo.loginWithPassword("", "123456").isFailure)
    }

    @Test
    fun `login fails on short password`() {
        val repo = newRepo()
        assertTrue(repo.loginWithPassword("demo@demo.com", "123").isFailure)
    }

    // ---------- 验证码登录 ----------

    @Test
    fun `otp login succeeds after request`() {
        val repo = newRepo()
        val code = repo.requestOtp("13800000000").getOrThrow()
        assertEquals(AuthRules.MOCK_OTP, code)
        assertEquals("13800000000", repo.pendingOtpPhone())
        assertEquals(1, repo.otpSentCount())
        val session = repo.loginWithOtp("13800000000", code).getOrThrow()
        assertEquals(AuthRules.MODE_OTP, session.mode)
        assertTrue(repo.isLoggedIn())
    }

    @Test
    fun `otp login fails on wrong code`() {
        val repo = newRepo()
        repo.requestOtp("13800000000")
        assertTrue(repo.loginWithOtp("13800000000", "000000").isFailure)
        assertTrue(repo.loginWithOtp("13800000000", "12345").isFailure)
        assertFalse(repo.isLoggedIn())
    }

    @Test
    fun `otp login fails when phone changed or not requested`() {
        val repo = newRepo()
        assertTrue(repo.loginWithOtp("13800000000", AuthRules.MOCK_OTP).isFailure)
        repo.requestOtp("13800000000")
        assertTrue(repo.loginWithOtp("13800000001", AuthRules.MOCK_OTP).isFailure)
        assertTrue(repo.loginWithOtp("138000", AuthRules.MOCK_OTP).isFailure)
    }

    @Test
    fun `request otp rejects invalid phone`() {
        val repo = newRepo()
        assertTrue(repo.requestOtp("23800000000").isFailure)
        assertTrue(repo.requestOtp("138").isFailure)
    }

    // ---------- 注册 ----------

    @Test
    fun `register creates session`() {
        val repo = newRepo()
        val session = repo.register("new@demo.com", "abc123", "abc123").getOrThrow()
        assertEquals("new@demo.com", session.account.email)
        assertEquals(AuthRules.MODE_REGISTER, session.mode)
        assertTrue(repo.isLoggedIn())
        assertTrue(repo.loginWithPassword("new@demo.com", "abc123").isSuccess)
    }

    @Test
    fun `register rejects duplicate email`() {
        val repo = newRepo()
        assertTrue(repo.register("demo@demo.com", "abc123", "abc123").isFailure)
    }

    @Test
    fun `register rejects bad input`() {
        val repo = newRepo()
        assertTrue(repo.register("not-an-email", "abc123", "abc123").isFailure)
        assertTrue(repo.register("new@demo.com", "123", "123").isFailure)
        assertTrue(repo.register("new@demo.com", "abc123", "abc124").isFailure)
    }

    // ---------- 登出清态 ----------

    @Test
    fun `logout clears session and pending otp`() {
        val repo = newRepo()
        repo.loginWithPassword("demo@demo.com", "123456")
        repo.requestOtp("13800000000")
        assertNotNull(repo.currentSession())
        repo.logout()
        assertNull(repo.currentSession())
        assertFalse(repo.isLoggedIn())
        assertTrue(repo.pendingOtpPhone().isEmpty())
        assertEquals(0, repo.otpSentCount())
    }

    // ---------- Mock 场景 ----------

    @Test
    fun `accounts page honours mock scenario`() {
        MockBackend.scenario = MockScenario.Success
        assertTrue(AuthCredentialRepository().page(1, 50).getOrThrow().items.isNotEmpty())

        MockBackend.scenario = MockScenario.Empty
        assertTrue(AuthCredentialRepository().page(1, 50).getOrThrow().isEmpty)

        MockBackend.scenario = MockScenario.Error
        assertTrue(AuthCredentialRepository().page(1, 50).isFailure)

        MockBackend.scenario = MockScenario.Success
    }

    // ---------- 规则 ----------

    @Test
    fun `auth rules validate email phone password and otp`() {
        assertTrue(AuthRules.isValidEmail("demo@demo.com"))
        assertFalse(AuthRules.isValidEmail("demo@demo"))
        assertFalse(AuthRules.isValidEmail("demo @demo.com"))
        assertTrue(AuthRules.isValidPhone("13800000000"))
        assertFalse(AuthRules.isValidPhone("12345678901"))
        assertTrue(AuthRules.isValidPassword("123456"))
        assertFalse(AuthRules.isValidPassword("12345"))
        assertTrue(AuthRules.isValidOtp("123456"))
        assertFalse(AuthRules.isValidOtp("12345a"))
        assertEquals("138****0000", AuthRules.maskPhone("13800000000"))
        assertEquals("1380000000", AuthRules.digitsOnly("138-0000-000"))
    }
}
