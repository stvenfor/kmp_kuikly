package com.example.kuikly.pages

import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("Home")
internal class HomePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val entries = listOf(
            "★ 多 Tab 主框架 (Main)" to PageNames.Main,
            "全部分类" to PageNames.AllServices,
            "Splash 启动页" to PageNames.Splash,
            "Compose 动画" to PageNames.ComposeAnim,
            "Compose 列表" to PageNames.ComposeList,
            "Compose Pager" to PageNames.ComposePager,
            "Auth + Feed" to PageNames.Login,
            "Data Analytics" to PageNames.AnalyticsList,
            "Settings" to PageNames.Settings,
            "Chat" to PageNames.ChatList,
            "UI Components" to PageNames.BfuiShowcase,
            "Community" to PageNames.Community,
            "Video" to PageNames.Video,
            "Mall" to PageNames.Mall,
            "Pay / Membership" to PageNames.Pay,
            "Classroom" to PageNames.Classroom,
            "Music" to PageNames.Music,
            "AI Assistant" to PageNames.AiStream,
            "Wallet" to PageNames.Wallet,
            "Live" to PageNames.Live,
            "Friends" to PageNames.Friend,
            "Platform Capabilities" to PageNames.PlatformShowcase,
            "HelloWorld" to PageNames.HelloWorld,
            "Legacy DSL Lab" to PageNames.DslLab,
            "Perf Lab" to PageNames.PerfLab,
            "Gallery" to PageNames.Gallery,
            "Permission Demo" to PageNames.PermissionDemo,
            "Share Demo" to PageNames.ShareDemo,
        )
        setContent {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text("Demo Map", fontSize = 24.sp, color = Color.Black)
                Spacer(Modifier.height(16.dp))
                entries.forEach { (label, page) ->
                    Text(
                        text = "→ $label",
                        fontSize = 18.sp,
                        color = Color(0xFF1565C0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Utils.currentBridgeModule().openPage(page) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}
