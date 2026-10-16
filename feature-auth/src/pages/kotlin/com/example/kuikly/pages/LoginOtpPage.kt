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

@Page("LoginOtp")
internal class LoginOtpPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var phone by remember { mutableStateOf("") }
            var code by remember { mutableStateOf("") }
            var focus by remember { mutableStateOf("") }
            var sentTo by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }
            var info by remember { mutableStateOf<String?>(null) }

            fun sendOtp() {
                AuthCredentialStore.repo.requestOtp(phone)
                    .onSuccess { mockCode ->
                        sentTo = AuthRules.maskPhone(phone)
                        error = null
                        info = "验证码已发送至 $sentTo（Mock 验证码 $mockCode）"
                        focus = "code"
                    }
                    .onFailure {
                        info = null
                        error = it.message ?: "发送失败"
                    }
            }

            fun submit() {
                AuthCredentialStore.repo.loginWithOtp(phone, code)
                    .onSuccess { session ->
                        error = null
                        Utils.currentBridgeModule().toast("登录成功：${session.account.displayName}")
                        Utils.currentBridgeModule().openPage(url = "FeedList", closeCurPage = true)
                    }
                    .onFailure {
                        info = null
                        error = it.message ?: "登录失败"
                    }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AuthNavBar(title = "验证码登录", subtitle = "测试号验证码 ${AuthRules.MOCK_OTP}")
                Spacer(Modifier.height(12.dp))
                AuthHeadline(title = "输入验证码", desc = "先用手机号获取验证码，再输入 6 位验证码登录")
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    AuthFieldRow(
                        label = "手机号",
                        value = phone,
                        hint = "11 位手机号",
                        active = focus == "phone",
                        onClick = { focus = if (focus == "phone") "" else "phone" },
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.width(120.dp).height(40.dp)
                                .background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                                .clickable { sendOtp() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(if (sentTo.isEmpty()) "获取验证码" else "重新发送", fontSize = 13.sp, color = KuiklyTheme.linkDeep, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (sentTo.isEmpty()) "未发送" else "已发送至 $sentTo",
                            fontSize = 12.sp,
                            color = KuiklyTheme.muted,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    AuthFieldRow(
                        label = "验证码",
                        value = code,
                        hint = "6 位数字",
                        active = focus == "code",
                        onClick = { focus = if (focus == "code") "" else "code" },
                    )
                }

                Spacer(Modifier.height(12.dp))

                when (focus) {
                    "phone" -> AuthNumberKeypad(
                        onDigit = { if (phone.length < AuthRules.PHONE_LENGTH) phone += it },
                        onBackspace = { if (phone.isNotEmpty()) phone = phone.dropLast(1) },
                        onClear = { phone = "" },
                    )
                    "code" -> AuthNumberKeypad(
                        onDigit = { if (code.length < AuthRules.OTP_LENGTH) code += it },
                        onBackspace = { if (code.isNotEmpty()) code = code.dropLast(1) },
                        onClear = { code = "" },
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
                        enabled = phone.length == AuthRules.PHONE_LENGTH && code.length == AuthRules.OTP_LENGTH,
                        onClick = ::submit,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        AuthFooterLink(text = "账号密码登录", onClick = {
                            Utils.currentBridgeModule().openPage("LoginPassword")
                        })
                        Spacer(Modifier.width(20.dp))
                        AuthFooterLink(text = "注册新账号", onClick = {
                            Utils.currentBridgeModule().openPage("Register")
                        })
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "测试号 13800000000，验证码 ${AuthRules.MOCK_OTP}",
                        fontSize = 12.sp,
                        color = KuiklyTheme.muted,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
