package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthCredentialStore
import com.example.kuikly.data.auth.AuthRules
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("Register")
internal class RegisterPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirm by remember { mutableStateOf("") }
            var agreed by remember { mutableStateOf(true) }
            var focus by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }
            var info by remember { mutableStateOf<String?>(null) }

            fun submit() {
                if (!agreed) {
                    error = "请先阅读并同意隐私条款"
                    info = null
                    return
                }
                AuthCredentialStore.repo.register(email, password, confirm)
                    .onSuccess { session ->
                        error = null
                        info = null
                        Utils.currentBridgeModule().toast("注册成功：${session.account.displayName}")
                        Utils.currentBridgeModule().openPage(url = "LoginPassword", closeCurPage = true)
                    }
                    .onFailure {
                        info = null
                        error = it.message ?: "注册失败"
                    }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AuthNavBar(title = "注册账号", subtitle = "Mock 注册，写入内存账号表")
                Spacer(Modifier.height(12.dp))
                AuthHeadline(
                    title = "创建账号",
                    desc = "邮箱格式需合法，密码至少 ${AuthRules.MIN_PASSWORD_LENGTH} 位且两次一致",
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    AuthFieldRow(
                        label = "邮箱",
                        value = email,
                        hint = "name@demo.com",
                        active = focus == "email",
                        onClick = { focus = if (focus == "email") "" else "email" },
                    )
                    Spacer(Modifier.height(10.dp))
                    AuthFieldRow(
                        label = "密码",
                        value = password,
                        hint = "至少 ${AuthRules.MIN_PASSWORD_LENGTH} 位",
                        active = focus == "password",
                        mask = true,
                        onClick = { focus = if (focus == "password") "" else "password" },
                    )
                    Spacer(Modifier.height(10.dp))
                    AuthFieldRow(
                        label = "确认密码",
                        value = confirm,
                        hint = "再次输入密码",
                        active = focus == "confirm",
                        mask = true,
                        onClick = { focus = if (focus == "confirm") "" else "confirm" },
                    )
                    Spacer(Modifier.height(8.dp))
                    AuthAgreeRow(checked = agreed, onToggle = { agreed = it })
                }

                Spacer(Modifier.height(12.dp))

                when (focus) {
                    "email" -> AuthTextInput(
                        candidates = listOf("demo", "manager", "sales", "new", "@demo.com", ".com"),
                        onAppend = { email += it },
                        onBackspace = { if (email.isNotEmpty()) email = email.dropLast(1) },
                        onClear = { email = "" },
                    )
                    "password" -> AuthTextInput(
                        candidates = listOf("123456", "a123456", "demo123"),
                        onAppend = { password += it },
                        onBackspace = { if (password.isNotEmpty()) password = password.dropLast(1) },
                        onClear = { password = "" },
                    )
                    "confirm" -> AuthTextInput(
                        candidates = listOf("123456", "a123456", "demo123"),
                        onAppend = { confirm += it },
                        onBackspace = { if (confirm.isNotEmpty()) confirm = confirm.dropLast(1) },
                        onClear = { confirm = "" },
                    )
                    else -> Box(modifier = Modifier.fillMaxWidth().height(0.dp))
                }

                Spacer(Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    if (error != null) {
                        AuthErrorText(message = error!!)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (info != null) {
                        AuthInfoText(message = info!!)
                        Spacer(Modifier.height(10.dp))
                    }
                    AuthPrimaryButton(
                        text = "注册并登录",
                        enabled = email.isNotBlank() && password.isNotBlank() && confirm.isNotBlank(),
                        onClick = ::submit,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        AuthFooterLink(text = "已有账号，去登录", onClick = {
                            Utils.currentBridgeModule().openPage("LoginPassword")
                        })
                        Spacer(Modifier.width(20.dp))
                        AuthFooterLink(text = "验证码登录", onClick = {
                            Utils.currentBridgeModule().openPage("LoginOtp")
                        })
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "已占用的邮箱：demo@demo.com / manager@demo.com / sales@demo.com",
                        fontSize = 12.sp,
                        color = KuiklyTheme.muted,
                        fontWeight = FontWeight.Normal,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
