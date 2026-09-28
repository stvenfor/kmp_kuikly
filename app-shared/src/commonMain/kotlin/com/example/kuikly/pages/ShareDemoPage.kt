package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.platform.share.ShareApi
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("ShareDemo")
internal class ShareDemoPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var result by remember { mutableStateOf("(tap share)") }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = (top + 20f).dp,
                        bottom = (bottom + 20f).dp,
                    ),
            ) {
                Text("Share Demo", fontSize = 22.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Share hello (mock)",
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable {
                        result = ShareApi.shareText("hello from kmp_kuikly")
                    },
                )
                Spacer(Modifier.height(12.dp))
                Text(result, fontSize = 16.sp, color = Color.Gray)
            }
        }
    }
}
