package com.example.kuikly.feature.auth.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page(name = "Login", moduleId = "feature_auth")
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
                Button(
                    onClick = {
                        AuthSession.repo.login("demo", "demo")
                            .onSuccess {
                                message = ""
                                Utils.currentBridgeModule().openPage(PageNames.Main, closeCurPage = true)
                            }
                            .onFailure { message = it.message ?: "fail" }
                    },
                ) {
                    Text("登录并进入主页")
                }
                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(message, color = Color.Red, fontSize = 14.sp)
                }
            }
        }
    }
}
