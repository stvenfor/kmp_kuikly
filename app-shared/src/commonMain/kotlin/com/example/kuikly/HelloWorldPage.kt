package com.example.kuikly

import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.background
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

@Page("HelloWorld")
internal class HelloWorldPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Hello, kmp_kuikly!", fontSize = 24.sp, color = Color.Black)
                Text("Powered by Kuikly Compose", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Open Demo Map (Home) →",
                    fontSize = 16.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().openPage("Home")
                    },
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Open Login vertical slice →",
                    fontSize = 16.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().openPage("Login")
                    },
                )
            }
        }
    }
}
