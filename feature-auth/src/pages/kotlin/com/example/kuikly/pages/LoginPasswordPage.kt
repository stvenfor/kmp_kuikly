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
import com.tencent.kuikly.compose.foundation.clickable
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
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("LoginPassword")
internal class LoginPasswordPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var account by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
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
                AuthCredentialStore.repo.loginWithPassword(account, password)
                    .onSuccess { session ->
                        error = null
                        info = null
                        Utils.currentBridgeModule().toast("登录成功：${session.account.displayName}")
                        Utils.currentBridgeModule().openPage(url = "FeedList", closeCurPage = true)
                    }
                    .onFailure {
                        info = null
                        error = it.message ?: "登录失败"
                    }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AuthNavBar(title = "密码登录", subtitle = "演示账号见下方候选词条")
                Spacer(Modifier.height(12.dp))
                AuthHeadline(
                    title = "请输入账号密码",
                    desc = "密码至少 ${AuthRules.MIN_PASSWORD_LENGTH} 位，可用邮箱或手机号登录",
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    AuthFieldRow(
                        label = "账号",
                        value = account,
                        hint = "邮箱 / 手机号",
                        active = focus == "account",
                        onClick = { focus = if (focus == "account") "" else "account" },
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
                    Spacer(Modifier.height(8.dp))
                    AuthAgreeRow(checked = agreed, onToggle = { agreed = it })
                }

                Spacer(Modifier.height(12.dp))

                when (focus) {
                    "account" -> AuthTextInput(
                        candidates = listOf("demo", "manager", "sales", "@demo.com", ".com", "13800000000"),
                        onAppend = { account += it },
                        onBackspace = { if (account.isNotEmpty()) account = account.dropLast(1) },
                        onClear = { account = "" },
                    )
                    "password" -> AuthTextInput(
                        candidates = listOf("123456", "a123456", "demo123"),
                        onAppend = { password += it },
                        onBackspace = { if (password.isNotEmpty()) password = password.dropLast(1) },
                        onClear = { password = "" },
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
                        text = "登录",
                        enabled = account.isNotBlank() && password.isNotBlank(),
                        onClick = ::submit,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        AuthFooterLink(text = "短信验证码登录", onClick = {
                            Utils.currentBridgeModule().openPage("LoginOtp")
                        })
                        Spacer(Modifier.width(20.dp))
                        AuthFooterLink(text = "注册新账号", onClick = {
                            Utils.currentBridgeModule().openPage("Register")
                        })
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "演示账号：demo@demo.com / 123456",
                        fontSize = 12.sp,
                        color = KuiklyTheme.muted,
                        fontWeight = FontWeight.Normal,
                    )
                }

                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "一键填充演示账号",
                        fontSize = 13.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable {
                            account = "demo@demo.com"
                            password = "123456"
                            error = null
                            info = "已填充 demo@demo.com，点击登录即可"
                        },
                    )
                }
            }
        }
    }
}
