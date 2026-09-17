package com.example.kuikly.feature.home.pages

import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 「发布动态」— Flutter `PublishPage`（`features/community/lib/community/view/publish_page.dart`）
 * 复刻（Phase-2 / P2-W3）。
 *
 * Flutter 侧本页**本身就是 stub**：`AppNavBar(title: '发布动态', leading: close)` +
 * `Center(Column(min))`：`Icon(edit_note, 64, grey.shade400)` + 16 间隙 +
 * 「发布动态功能开发中」(16 · grey.shade600) + 8 间隙 + 「后续可接入发帖接口」(13 · grey.shade500)。
 * Kuikly 逐行镜像，**不做超出 Flutter 真源的富文本编辑器**（ponytail：不发明需求）。
 *
 * **刻度**：`publish_page.dart` 全部为**裸逻辑 px**（无 `.w/.h/.sp`）→ 一律裸 `dp`/`sp`。
 *
 * ponytail 天花板：AppNavBar 左侧 Flutter 显式用 `Icons.close`，共享 [AppNavBarBar] 只提供
 * `‹` 返回字形（无 icon font/矢量资源，同 W2c 口径）——语义（关闭本页）一致，字形非像素级一致。
 */
@Page(name = "CommunityPublish", moduleId = "feature_home")
internal class CommunityPublishPage : BaseComposePager() {
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
                    title = "发布动态",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("✎", fontSize = 64.sp, color = Color(0xFFBDBDBD))
                    Spacer(Modifier.height(16.dp))
                    Text("发布动态功能开发中", fontSize = 16.sp, color = Color(0xFF757575))
                    Spacer(Modifier.height(8.dp))
                    Text("后续可接入发帖接口", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                }
                Spacer(Modifier.height(bottomSafeInset().dp))
            }
        }
    }
}
