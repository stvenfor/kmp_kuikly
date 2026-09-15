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
            "Main (四 Tab)" to PageNames.Main,
            "Splash" to PageNames.Splash,
            "Auth + Feed" to PageNames.Login,
            "HelloWorld" to PageNames.HelloWorld,
            "Legacy DSL Lab" to PageNames.DslLab,
            "Perf Lab" to PageNames.PerfLab,
            "Gallery" to PageNames.Gallery,
            "Compose Anim" to PageNames.ComposeAnim,
            "Compose List" to PageNames.ComposeList,
            "Compose Pager" to PageNames.ComposePager,
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
