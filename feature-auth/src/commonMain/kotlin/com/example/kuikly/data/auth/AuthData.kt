package com.example.kuikly.data.auth

import com.example.kuikly.data.BaseMockRepository

/** 演示账号（port of source AuthService mock 账号）。 */
data class AuthAccount(
    val id: String,
    val email: String,
    val phone: String,
    val password: String,
    val nickname: String,
    val storeName: String = "",
) {
    /** 列表展示用的主标题。 */
    val displayName: String
        get() = if (nickname.isNotBlank()) nickname else email.ifBlank { phone }
}

/** 登录会话，登出后置空。 */
data class LoginSession(
    val token: String,
    val account: AuthAccount,
    val mode: String, // password | otp | register
)

/** 登录表单校验规则（port of source AuthController）。 */
object AuthRules {
    const val MIN_PASSWORD_LENGTH = 6
    const val OTP_LENGTH = 6
    const val PHONE_LENGTH = 11
    const val MOCK_OTP = "123456"

    const val MODE_PASSWORD = "password"
    const val MODE_OTP = "otp"
    const val MODE_REGISTER = "register"

    fun digitsOnly(raw: String): String = raw.filter { it in '0'..'9' }

    fun isValidEmail(raw: String): Boolean {
        val value = raw.trim()
        if (value.length < 5 || value.contains(' ')) return false
        val at = value.indexOf('@')
        if (at <= 0) return false
        val dot = value.lastIndexOf('.')
        return dot >= at + 2 && dot < value.length - 1
    }

    /**
     * 合法的中国大陆手机号：11 位、以 1 开头，且第二位为 3~9
     * （排除 10x/11x/12x 这类无效号段，例如 "12345678901" 应判为非法）。
     */
    fun isValidPhone(raw: String): Boolean {
        val digits = digitsOnly(raw)
        return digits.length == PHONE_LENGTH &&
            digits.startsWith("1") &&
            digits[1] in '3'..'9'
    }

    fun isValidPassword(raw: String): Boolean = raw.length >= MIN_PASSWORD_LENGTH

    fun isValidOtp(raw: String): Boolean {
        val value = raw.trim()
        return value.length == OTP_LENGTH && value.all { it in '0'..'9' }
    }

    /** 138****0000。 */
    fun maskPhone(raw: String): String {
        val digits = digitsOnly(raw)
        if (digits.length != PHONE_LENGTH) return raw
        return "${digits.substring(0, 3)}****${digits.substring(7)}"
    }
}

/**
 * 认证仓库：账号密码登录、短信验证码登录、邮箱注册、登出清态。
 * 全部走 Mock，会话保存在仓库实例内。
 */
class AuthCredentialRepository : BaseMockRepository<AuthAccount>() {
    private val demoAccounts = listOf(
        AuthAccount("u1", "demo@demo.com", "13800000000", "123456", "演示账号", "北京旗舰店"),
        AuthAccount("u2", "manager@demo.com", "13800000001", "a123456", "销售经理", "上海中心店"),
        AuthAccount("u3", "sales@demo.com", "13800000002", "demo123", "销售顾问", "广州天河店"),
    )

    private val registeredAccounts: MutableList<AuthAccount> = mutableListOf()

    private var session: LoginSession? = null
    private var pendingPhone: String = ""
    private var pendingCode: String = ""
    private var otpSentCount: Int = 0

    override fun seed(): List<AuthAccount> = demoAccounts + registeredAccounts
    override fun idOf(item: AuthAccount): String = item.id

    fun currentSession(): LoginSession? = session

    fun isLoggedIn(): Boolean = session != null

    /** 最近一次发送验证码的号码，空串表示未发码。 */
    fun pendingOtpPhone(): String = pendingPhone

    fun otpSentCount(): Int = otpSentCount

    /** 演示账号列表（登录页快捷填充用）。 */
    fun demoCredentials(): List<AuthAccount> = demoAccounts

    /** 账号密码登录：支持邮箱或手机号作为账号。 */
    fun loginWithPassword(account: String, password: String): Result<LoginSession> {
        if (account.isBlank()) return Result.failure(IllegalArgumentException("请输入邮箱或手机号"))
        if (!AuthRules.isValidPassword(password)) {
            return Result.failure(IllegalArgumentException("密码至少 ${AuthRules.MIN_PASSWORD_LENGTH} 位"))
        }
        val normalized = account.trim()
        val digits = AuthRules.digitsOnly(account)
        val hit = seed().firstOrNull {
            it.email.equals(normalized, ignoreCase = true) || (digits.isNotEmpty() && it.phone == digits)
        }
        if (hit == null) return Result.failure(IllegalArgumentException("账号不存在"))
        if (hit.password != password) return Result.failure(IllegalArgumentException("密码错误"))
        val created = LoginSession(token = "mock-token-${hit.id}", account = hit, mode = AuthRules.MODE_PASSWORD)
        session = created
        return Result.success(created)
    }

    /** 发送短信验证码，返回 Mock 验证码（成功时固定 123456）。 */
    fun requestOtp(phone: String): Result<String> {
        if (!AuthRules.isValidPhone(phone)) {
            return Result.failure(IllegalArgumentException("请输入有效的手机号"))
        }
        pendingPhone = AuthRules.digitsOnly(phone)
        pendingCode = AuthRules.MOCK_OTP
        otpSentCount = otpSentCount + 1
        return Result.success(pendingCode)
    }

    /** 验证码登录：必须先用同一号码发过码，改号后作废。 */
    fun loginWithOtp(phone: String, code: String): Result<LoginSession> {
        if (!AuthRules.isValidPhone(phone)) {
            return Result.failure(IllegalArgumentException("请输入有效的手机号"))
        }
        if (pendingPhone.isEmpty() || AuthRules.digitsOnly(phone) != pendingPhone) {
            return Result.failure(IllegalArgumentException("手机号已变更或未获取验证码"))
        }
        if (!AuthRules.isValidOtp(code)) {
            return Result.failure(IllegalArgumentException("请输入 6 位验证码"))
        }
        if (code != pendingCode) {
            return Result.failure(IllegalArgumentException("验证码错误"))
        }
        val hit = seed().firstOrNull { it.phone == pendingPhone }
            ?: AuthAccount("u-otp", "", pendingPhone, "", "短信用户", "上海中心店")
        val created = LoginSession(token = "mock-token-${hit.id}-otp", account = hit, mode = AuthRules.MODE_OTP)
        session = created
        pendingCode = ""
        return Result.success(created)
    }

    /** 邮箱注册：校验邮箱格式、密码长度、两次一致、邮箱未占用。 */
    fun register(email: String, password: String, confirm: String): Result<LoginSession> {
        if (!AuthRules.isValidEmail(email)) return Result.failure(IllegalArgumentException("请输入有效的邮箱"))
        if (!AuthRules.isValidPassword(password)) {
            return Result.failure(IllegalArgumentException("密码至少 ${AuthRules.MIN_PASSWORD_LENGTH} 位"))
        }
        if (password != confirm) return Result.failure(IllegalArgumentException("两次密码不一致"))
        val normalized = email.trim()
        if (seed().any { it.email.equals(normalized, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("邮箱已注册"))
        }
        val created = AuthAccount(
            id = "u-reg-${registeredAccounts.size + 1}",
            email = normalized,
            phone = "",
            password = password,
            nickname = normalized.substringBefore('@'),
            storeName = "广州天河店",
        )
        registeredAccounts.add(created)
        val loginSession = LoginSession(
            token = "mock-token-${created.id}",
            account = created,
            mode = AuthRules.MODE_REGISTER,
        )
        session = loginSession
        return Result.success(loginSession)
    }

    /** 登出：清掉会话、待校验手机号与验证码。 */
    fun logout() {
        session = null
        pendingPhone = ""
        pendingCode = ""
        otpSentCount = 0
    }
}

object AuthCredentialStore {
    val repo: AuthCredentialRepository = AuthCredentialRepository()
}
