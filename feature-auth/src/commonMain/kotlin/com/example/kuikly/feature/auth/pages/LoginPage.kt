package com.example.kuikly.feature.auth.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.auth.FakeAuthRepository
import com.example.kuikly.navigation.LoginRedirect
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.foundation.text.BasicTextField
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.SpanStyle
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.input.PasswordVisualTransformation
import com.tencent.kuikly.compose.ui.text.input.VisualTransformation
import com.tencent.kuikly.compose.ui.text.withStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import java.util.Calendar

private enum class LoginMode(val label: String) {
    // Flutter `_buildCredentialSwitcher` 顺序：邮箱在前、短信在后。
    Email("邮箱登录"),
    Otp("短信登录"),
}

/** `AuthTheme`（module_auth iOS 极简令牌）镜像。 */
/** Shared with RegisterPage (P2-W4a); keep internal to feature-auth. */
internal object AuthPalette {
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val fillSecondary = Color(0xFFE9E9EB)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val labelTertiary = Color(0x4D3C3C43)
    val separator = Color(0xFFC6C6C8)
    val buttonDisabled = Color(0xFFC7C7CC)
    val error = Color(0xFFC62828)

    /** AuthTheme.radiusMd = 12 */
    val fieldRadius = 12.dp
    /** AuthTheme.radiusLg = 14 */
    val buttonRadius = 14.dp
    /** AuthTheme.fieldHeight / buttonHeight = 52 */
    val fieldHeight = 52.dp
}

/**
 * Mock login：邮箱登录 ↔ 短信登录（Migration Source `login_page.dart` 复刻，P2-W2a）。
 *
 * - 视觉令牌镜像 `module_auth/user/theme/auth_theme.dart`（accent `#007AFF` / 背景 `#F2F2F7`）。
 * - Flutter 源本页全部为裸逻辑 px（无 `.w/.sp`），故 Kuikly 侧同样用裸 dp/sp，不接 DesignScale。
 * - 默认仍开短信模式：保住 Slice-01 `03-login` per-platform golden 的语义
 *   （Flutter 控制器默认 email；差异仅 segment 选中态，结构/文案一致）。
 * - Golden priming：pageData {"mode":"email"} / {"mode":"password"} 打开邮箱模式。
 */
@Page(name = "Login", moduleId = "feature_auth")
internal class LoginPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val initialMode = when (pageData.params.optString("mode")) {
            "email", "password" -> LoginMode.Email
            else -> LoginMode.Otp
        }
        val greetingOverride = pageData.params.optString("greeting").ifBlank { null }
        setContent {
            var mode by remember { mutableStateOf(initialMode) }
            var phone by remember { mutableStateOf(FakeAuthRepository.MOCK_PHONE) }
            var otp by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var agreedPrivacy by remember { mutableStateOf(true) }
            var message by remember { mutableStateOf("") }
            var otpSent by remember { mutableStateOf(false) }

            // AuthController.greeting（按小时）
            val greeting = greetingOverride ?: remember {
                when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    in 0..11 -> "早上好，欢迎使用iHome"
                    in 12..17 -> "下午好，欢迎使用iHome"
                    else -> "晚上好，欢迎使用iHome"
                }
            }

            fun finishAfterLogin() {
                val next = LoginRedirect.consume() ?: PageNames.Main
                Utils.currentBridgeModule().openPage(next, closeCurPage = true)
            }

            fun doLogin() {
                if (!agreedPrivacy) {
                    message = "请先同意隐私条款"
                    return
                }
                val result = when (mode) {
                    LoginMode.Email -> AuthSession.repo.login(email, password)
                    LoginMode.Otp -> AuthSession.repo.loginWithOtp(phone, otp)
                }
                result
                    .onSuccess {
                        message = ""
                        finishAfterLogin()
                    }
                    .onFailure { message = it.message ?: "登录失败" }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AuthPalette.background)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = (top + 32f).dp,
                        bottom = (bottom + 24f).dp,
                    ),
            ) {
                // AuthTheme.largeTitle（fontSize 32 / w700）
                Text(greeting, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AuthPalette.labelPrimary)
                Spacer(Modifier.height(8.dp))
                // AuthTheme.subtitle（fontSize 15）
                Text("登录以继续使用", fontSize = 15.sp, color = AuthPalette.labelSecondary)
                Spacer(Modifier.height(40.dp))
                LoginModeSwitcher(mode = mode, onSelect = { mode = it })
                Spacer(Modifier.height(24.dp))
                if (mode == LoginMode.Email) {
                    LoginField(
                        glyph = "✉",
                        value = email,
                        onValueChange = { email = it },
                        hint = "邮箱",
                    )
                    Spacer(Modifier.height(16.dp))
                    LoginField(
                        glyph = "✱",
                        value = password,
                        onValueChange = { password = it },
                        hint = "密码",
                        obscure = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "测试账号 demo / 任意密码（非空即可）",
                        fontSize = 13.sp,
                        color = AuthPalette.labelSecondary,
                    )
                } else {
                    // PhoneOtpFormSection：+86 段 + 手机号
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .height(AuthPalette.fieldHeight)
                                .background(AuthPalette.surface, RoundedCornerShape(AuthPalette.fieldRadius))
                                .border(0.5.dp, AuthPalette.separator, RoundedCornerShape(AuthPalette.fieldRadius))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("+86", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = AuthPalette.labelPrimary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            LoginField(
                                glyph = "✆",
                                value = phone,
                                onValueChange = { phone = it },
                                hint = "手机号",
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            LoginField(
                                glyph = "✉",
                                value = otp,
                                onValueChange = { otp = it },
                                hint = "验证码",
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        // _SendOtpButton（minimumSize 96×52 / 白底描边 / accent 文案）
                        Box(
                            modifier = Modifier
                                .height(AuthPalette.fieldHeight)
                                .width(96.dp)
                                .background(AuthPalette.surface, RoundedCornerShape(AuthPalette.fieldRadius))
                                .border(0.5.dp, AuthPalette.separator, RoundedCornerShape(AuthPalette.fieldRadius))
                                .clickable { otpSent = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "获取验证码",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = AuthPalette.accent,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (otpSent) {
                            "验证码已发送至 ${phone.take(3)}****${phone.takeLast(4)}"
                        } else {
                            "测试号 ${FakeAuthRepository.MOCK_PHONE}，验证码 ${FakeAuthRepository.MOCK_OTP}"
                        },
                        fontSize = 13.sp,
                        color = if (otpSent) AuthPalette.labelSecondary else AuthPalette.accent,
                    )
                }
                Spacer(Modifier.height(24.dp))
                PrivacyRow(agreed = agreedPrivacy, onToggle = { agreedPrivacy = it })
                Spacer(Modifier.height(32.dp))
                val enabled = if (mode == LoginMode.Email) {
                    email.isNotBlank() && password.length >= 6
                } else {
                    phone.isNotBlank() && otp.length == 6
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AuthPalette.fieldHeight)
                        .background(
                            if (enabled) AuthPalette.accent else AuthPalette.buttonDisabled,
                            RoundedCornerShape(AuthPalette.buttonRadius),
                        )
                        .clickable { if (enabled) doLogin() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("登录", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(message, color = AuthPalette.error, fontSize = 14.sp)
                }
                Spacer(Modifier.height(24.dp))
                // LoginFooterLinks：我要注册 | 忘记密码
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "我要注册",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuthPalette.accent,
                        // P2-W4d：压栈打开注册页（不关登录页，Register「‹」返回即回到本页）。
                        modifier = Modifier
                            .clickable {
                                Utils.currentBridgeModule().openPage(PageNames.Register)
                            }
                            .padding(horizontal = 8.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(AuthPalette.separator),
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "忘记密码",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuthPalette.accent,
                        modifier = Modifier.clickable { }.padding(horizontal = 8.dp),
                    )
                }
            }
        }
    }
}

/** `AuthTheme.filledFieldDecoration` 复刻：白底 12 圆角 + 0.5 separator 描边 + 前缀图标位。 */
@Composable
private fun LoginField(
    glyph: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    obscure: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AuthPalette.fieldHeight)
            .background(AuthPalette.surface, RoundedCornerShape(AuthPalette.fieldRadius))
            .border(0.5.dp, AuthPalette.separator, RoundedCornerShape(AuthPalette.fieldRadius))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(glyph, fontSize = 16.sp, color = AuthPalette.labelSecondary)
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 17.sp, color = AuthPalette.labelPrimary),
                visualTransformation = if (obscure) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (value.isEmpty()) {
                Text(hint, fontSize = 17.sp, color = AuthPalette.labelTertiary)
            }
        }
    }
}

/**
 * 隐私行复刻（login_page._buildPrivacyRow）：44×44 触达区内的 22 圆形勾选 +
 * 顶对齐说明文案。
 */
@Composable
private fun PrivacyRow(agreed: Boolean, onToggle: (Boolean) -> Unit) =
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!agreed) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (agreed) AuthPalette.accent else Color.Transparent,
                        CircleShape,
                    )
                    .border(1.5.dp, if (agreed) AuthPalette.accent else AuthPalette.separator, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (agreed) {
                    Text("✓", fontSize = 14.sp, color = Color.White)
                }
            }
        }
        Text(
            // Flutter RichText：说明文字 labelSecondary，书名号条款名 accent。
            buildAnnotatedString {
                withStyle(SpanStyle(color = AuthPalette.labelSecondary)) {
                    append("我已阅读并同意")
                }
                withStyle(SpanStyle(color = AuthPalette.accent)) {
                    append("《某个隐私条款》")
                }
            },
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp),
        )
    }

/**
 * `CupertinoSlidingSegmentedControl` 复刻：
 * 内容自适应宽度（非撑满）、fillSecondary 12 圆角 + 白色 thumb + 15/w500 文案。
 */
@Composable
private fun LoginModeSwitcher(mode: LoginMode, onSelect: (LoginMode) -> Unit) =
    Row(
        modifier = Modifier
            .background(AuthPalette.fillSecondary, RoundedCornerShape(12.dp))
            .padding(4.dp),
    ) {
        LoginMode.entries.forEach { item ->
            val selected = item == mode
            Box(
                modifier = Modifier
                    .background(
                        if (selected) AuthPalette.surface else Color.Transparent,
                        RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelect(item) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AuthPalette.labelPrimary,
                )
            }
        }
    }
