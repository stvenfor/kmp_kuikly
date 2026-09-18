package com.example.kuikly.feature.auth.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.FakeAuthRepository
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
import com.tencent.kuikly.compose.ui.draw.clip
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

internal enum class RegisterMode(val label: String) {
    Email("邮箱注册"),
    Phone("手机注册"),
}

/**
 * 注册页 — Flutter `RegisterPage`
 * （`features/auth/lib/user/view/register_page.dart`）复刻（P2-W4a 接入路由 `register`）。
 *
 * **真源**：`AuthBackButton` + 「创建账号」32·bold + 「选择注册方式并填写信息」15 +
 * `CupertinoSlidingSegmentedControl`（邮箱/手机）+ 邮箱形态（`AuthGroupedFormCard`：
 * 邮箱 / 昵称 / 密码 / 确认密码）或手机形态（`PhoneOtpFormSection` fromRegister=true）
 * + `AuthPrivacyRow` + 48·r14 「注册」CTA + 「已有账号·去登录」text button。
 *
 * **刻度**：Flutter `register_page` view **grep 无 `.w/.h/.sp`**（共享
 * `module_common_ui/layout/app_nav_bar.dart` 与 `module_auth/.../auth_form_widgets.dart`
 * 同样裸 px），故一律裸 `dp`/`sp`，**不**接 `ProvideDesignScale`（判断法与 `LoginPage` 同）。
 *
 * ponytail 天花板（不弹回）：
 * - Kuikly 无密码可见切换 / `CupertinoSlidingSegmentedControl` 原生 widget → 简化版（仅切
 *   mode 显示不同 form，无 toggle 密码可见态，无密码可见图标）。
 * - 「注册」按钮：Kuikly 侧 AuthRepository 无 `registerWithEmail` / `registerWithPhone`
 *   方法 → mock 本地 validate + toast「注册成功（开发中）」+ `closePage` 回登录。
 * - 隐私行 `controller.togglePrivacy()` → 本地 `mutableStateOf` 切换。
 */
@Page(name = "Register", moduleId = "feature_auth")
internal class RegisterPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var mode by remember { mutableStateOf(RegisterMode.Email) }
            var email by remember { mutableStateOf("") }
            var displayName by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var otp by remember { mutableStateOf("") }
            var otpSent by remember { mutableStateOf(false) }
            var agreed by remember { mutableStateOf(true) }
            var message by remember { mutableStateOf("") }

            val canSubmit = when (mode) {
                RegisterMode.Email -> agreed &&
                    email.isNotBlank() &&
                    password.length >= 6 &&
                    password == confirmPassword
                RegisterMode.Phone -> agreed &&
                    phone.filter { it.isDigit() } == FakeAuthRepository.MOCK_PHONE &&
                    otp.trim() == FakeAuthRepository.MOCK_OTP
            }

            fun submit() {
                if (!agreed) {
                    message = "请先同意隐私条款"
                    return
                }
                message = "注册成功（开发中 mock）"
                Utils.currentBridgeModule().toast("注册成功（开发中）")
                Utils.currentBridgeModule().openPage(PageNames.Login, closeCurPage = true)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RegisterPalette.background)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = (top + 8f).dp,
                        bottom = (bottom + 24f).dp,
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { Utils.currentBridgeModule().closePage() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "‹",
                            fontSize = 24.sp,
                            color = RegisterPalette.accent,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("创建账号", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = RegisterPalette.labelPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "选择注册方式并填写信息",
                    fontSize = 15.sp,
                    color = RegisterPalette.labelSecondary,
                )
                Spacer(Modifier.height(32.dp))
                RegisterModeSwitcher(mode = mode, onSelect = { mode = it })
                Spacer(Modifier.height(24.dp))
                if (mode == RegisterMode.Email) {
                    SectionLabel("账号信息")
                    Spacer(Modifier.height(8.dp))
                    EmailForm(
                        email = email, onEmailChange = { email = it },
                        displayName = displayName, onDisplayNameChange = { displayName = it },
                        password = password, onPasswordChange = { password = it },
                        confirm = confirmPassword, onConfirmChange = { confirmPassword = it },
                    )
                } else {
                    SectionLabel("手机验证")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "首次验证通过后将自动创建账号",
                        fontSize = 13.sp,
                        color = RegisterPalette.labelSecondary,
                    )
                    Spacer(Modifier.height(16.dp))
                    PhoneForm(
                        phone = phone, onPhoneChange = { phone = it },
                        otp = otp, onOtpChange = { otp = it },
                        otpSent = otpSent, onSendOtp = { otpSent = true },
                    )
                }
                Spacer(Modifier.height(24.dp))
                PrivacyRow(agreed = agreed, onToggle = { agreed = it })
                Spacer(Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RegisterPalette.fieldHeight)
                        .background(
                            if (canSubmit) RegisterPalette.accent else RegisterPalette.buttonDisabled,
                            RoundedCornerShape(RegisterPalette.buttonRadius),
                        )
                        .clickable { if (canSubmit) submit() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "注册",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(message, color = RegisterPalette.error, fontSize = 14.sp)
                }
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "已有账号？去登录",
                        fontSize = 13.sp,
                        color = RegisterPalette.accent,
                        modifier = Modifier
                            .clickable {
                                Utils.currentBridgeModule().openPage(
                                    PageNames.Login,
                                    closeCurPage = true,
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    // AuthTheme.sectionLabel：fontSize 13 / w400 / labelSecondary / height 1.35 / letterSpacing -0.08。
    Text(
        text,
        fontSize = 13.sp,
        color = RegisterPalette.labelSecondary,
    )
}

/** `CupertinoSlidingSegmentedControl` 简化版：fillSecondary 12 圆角 + 选中白底 8 圆角。 */
@Composable
private fun RegisterModeSwitcher(mode: RegisterMode, onSelect: (RegisterMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RegisterPalette.fillSecondary, RoundedCornerShape(12.dp))
            .padding(4.dp),
    ) {
        RegisterMode.entries.forEach { item ->
            val selected = item == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selected) RegisterPalette.surface else Color.Transparent,
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
                    color = RegisterPalette.labelPrimary,
                )
            }
        }
    }
}

/** `AuthGroupedFormCard` 简化版：4 字段白底圆角卡堆叠（无 visible 切换 / 无 helper 校验）。 */
@Composable
private fun EmailForm(
    email: String, onEmailChange: (String) -> Unit,
    displayName: String, onDisplayNameChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirm: String, onConfirmChange: (String) -> Unit,
) {
    // `AuthGroupedFormCard` 用 `ClipRRect(radiusMd)` 包住 Column，让 1 高的
    // FieldDivider 不越过卡圆角。Kuikly 侧 `.background(RoundedCornerShape)`
    // 只裁背景，不裁子节点，需显式 `.clip()`。
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RegisterPalette.fieldRadius))
            .background(RegisterPalette.surface, RoundedCornerShape(RegisterPalette.fieldRadius)),
    ) {
        RegisterField(value = email, onValueChange = onEmailChange, hint = "邮箱")
        FieldDivider()
        RegisterField(value = displayName, onValueChange = onDisplayNameChange, hint = "昵称（可选）")
        FieldDivider()
        // Flutter `obscureText: !_passwordVisible` 默认即为掩码；Kuikly 侧
        // 无切换 toggle 但仍默认掩码，跟 LoginPage 的 `LoginField(obscure=true)` 同型。
        RegisterField(value = password, onValueChange = onPasswordChange, hint = "密码（至少 6 位）", obscure = true)
        FieldDivider()
        RegisterField(value = confirm, onValueChange = onConfirmChange, hint = "确认密码", obscure = true)
    }
}

@Composable
private fun FieldDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(1.dp)
            .background(RegisterPalette.separator),
    )
}

/** `PhoneOtpFormSection(fromRegister: true)` 简化版：+86 / 手机号 / 验证码 + 获取按钮。 */
@Composable
private fun PhoneForm(
    phone: String, onPhoneChange: (String) -> Unit,
    otp: String, onOtpChange: (String) -> Unit,
    otpSent: Boolean, onSendOtp: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(RegisterPalette.fieldHeight)
                .background(RegisterPalette.surface, RoundedCornerShape(RegisterPalette.fieldRadius))
                .border(0.5.dp, RegisterPalette.separator, RoundedCornerShape(RegisterPalette.fieldRadius))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "+86",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = RegisterPalette.labelPrimary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            RegisterField(value = phone, onValueChange = onPhoneChange, hint = "手机号")
        }
    }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            RegisterField(value = otp, onValueChange = onOtpChange, hint = "验证码")
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .height(RegisterPalette.fieldHeight)
                .width(96.dp)
                .background(RegisterPalette.surface, RoundedCornerShape(RegisterPalette.fieldRadius))
                .border(0.5.dp, RegisterPalette.separator, RoundedCornerShape(RegisterPalette.fieldRadius))
                .clickable(onClick = onSendOtp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "获取验证码",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = RegisterPalette.accent,
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
        color = if (otpSent) RegisterPalette.labelSecondary else RegisterPalette.accent,
    )
}

/** `AuthGroupedTextField` 简化版：52 高的白底单行输入 + hint + 占位提示色 + 可选掩码。 */
@Composable
private fun RegisterField(value: String, onValueChange: (String) -> Unit, hint: String, obscure: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RegisterPalette.fieldHeight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 17.sp, color = RegisterPalette.labelPrimary),
                visualTransformation = if (obscure) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (value.isEmpty()) {
                Text(hint, fontSize = 17.sp, color = RegisterPalette.labelTertiary)
            }
        }
    }
}

/** `AuthPrivacyRow` 简化版：22 圆形勾选 + 「我已阅读并同意《某个隐私条款》」。 */
@Composable
private fun PrivacyRow(agreed: Boolean, onToggle: (Boolean) -> Unit) {
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
                        if (agreed) RegisterPalette.accent else Color.Transparent,
                        CircleShape,
                    )
                    .border(
                        1.5.dp,
                        if (agreed) RegisterPalette.accent else RegisterPalette.separator,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (agreed) {
                    Text("✓", fontSize = 14.sp, color = Color.White)
                }
            }
        }
        Text(
            // Flutter AuthPrivacyRow RichText：说明 labelSecondary，书名号条款名 accent。
            buildAnnotatedString {
                withStyle(SpanStyle(color = RegisterPalette.labelSecondary)) {
                    append("我已阅读并同意")
                }
                withStyle(SpanStyle(color = RegisterPalette.accent)) {
                    append("《某个隐私条款》")
                }
            },
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp),
        )
    }
}

/** RegisterPage 私有调色板：与 LoginPage 同令牌（`AuthTheme` 镜像）。 */
internal object RegisterPalette {
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
    val fieldRadius = 12.dp
    val buttonRadius = 14.dp
    val fieldHeight = 52.dp
}