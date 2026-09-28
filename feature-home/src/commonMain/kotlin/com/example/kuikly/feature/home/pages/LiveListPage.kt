package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 直播列表 — Flutter `LivePage` 是**调试桩**：仅 `AppNavBar('直播')` + 居中
 * `FilledButton('进入 Mock 直播房')`，**无房间列表视觉真源**（P3-E9c 收敛）。
 *
 * 故本页与 Flutter stub 严格对齐为 nav + 居中 CTA；按钮 tap 仍走 `PageNames.LiveDetail`
 * （`LiveDetailPage` 缺省 id 回退 "1"），保持 P3-E4b 以来「CTA→房间详情」的连通路径。
 * chrome 与按钮样式沿用共享 `AppChrome` / `PrimaryAction` 令牌（`AppNavBarBar` 56+top inset、
 * `#F2F2F7` 页底、accent 底/白字/14·w500/r12/44 高/内容内距 24）。刻度：Flutter live 模块
 * grep 无 `.w/.h/.sp` → 裸 `dp`/`sp`。
 */
@Page(name = "LiveList", moduleId = "feature_home")
internal class LiveListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "直播",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                // Flutter `body: Center(child: FilledButton('进入 Mock 直播房'))`
                // （AppTheme：accent 底 / 白字 / 最小 64×44 / r12 / 14·w500）。
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    PrimaryAction(label = "进入 Mock 直播房") {
                        Utils.currentBridgeModule().openPage(
                            PageNames.LiveDetail,
                            userData = JSONObject().apply { put("id", "1") },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Flutter `FilledButton`（AppTheme accent / 白字 / r12 / 44 高）等价物。
 * 内容内距对齐 M3 默认 `padding1x = 24`（flutter/.../filled_button.dart:485，
 * P3-E4b 结构对齐；LiveDetailPage 复用本组件同步受益）。
 */
@Composable
internal fun PrimaryAction(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .background(AppChrome.accent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}