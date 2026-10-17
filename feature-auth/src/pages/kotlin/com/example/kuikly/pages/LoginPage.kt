package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("Login")
internal class LoginPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var message by remember { mutableStateOf("") }
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Login (Mock)", fontSize = 22.sp, color = Color.Black)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Tap to sign in as demo",
                    fontSize = 16.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable {
                        AuthSession.repo.login("demo", "demo")
                            .onSuccess {
                                message = ""
                                Utils.currentBridgeModule().openPage("FeedList", closeCurPage = true)
                            }
                            .onFailure { message = it.message ?: "fail" }
                    },
                )
                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(message, color = Color.Red, fontSize = 14.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "账号密码登录",
                    fontSize = 15.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable { Utils.currentBridgeModule().openPage("LoginPassword") },
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "短信验证码登录",
                    fontSize = 15.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable { Utils.currentBridgeModule().openPage("LoginOtp") },
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "注册新账号",
                    fontSize = 15.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable { Utils.currentBridgeModule().openPage("Register") },
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "账号中心（门店 / 登录态 / 登出）",
                    fontSize = 15.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable { Utils.currentBridgeModule().openPage("AuthAccount") },
                )
            }
        }
    }
}
