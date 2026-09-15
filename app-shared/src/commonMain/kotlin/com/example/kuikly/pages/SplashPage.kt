package com.example.kuikly.pages

import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.base.setTimeout
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/** 启动页：已登录 → Main，否则 → Login（setTimeout：docs/DevGuide/set-timeout.md） */
@Page("Splash")
internal class SplashPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("kmp_kuikly", fontSize = 28.sp, color = Color.Black)
                Text("Demo Skeleton", fontSize = 16.sp, color = Color.Gray)
            }
        }
        setTimeout(600) {
            val next = if (AuthSession.repo.currentUser() != null) {
                PageNames.Main
            } else {
                PageNames.Login
            }
            Utils.currentBridgeModule().openPage(next, closeCurPage = true)
        }
    }
}
