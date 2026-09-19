package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.BaseComposePager
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 好友列表 — P3-E9b 按 Flutter `features/friend/lib/friend/view/friend_page.dart` 占位桩压平。
 *
 * Flutter 真源：`AppPageScaffold(navBar: AppNavBar(title: '好友'),
 * body: Center(child: Text('Friend 模块')))`。
 * 故 Kuikly 不再渲染 mock 列表，仅保留共享 chrome（`AppNavBar` 几何 + `AppTheme` 的
 * `#F2F2F7` 背景 / `#FFFFFF` 导航栏 / 0.5 发丝线）与居中的 `Friend 模块` 文案。
 *
 * **刻度**：Flutter friend 模块 grep 无 `.w/.h/.sp` → 裸 `dp`/`sp`（不接 DesignScale）。
 */
@Page(name = "FriendList", moduleId = "feature_home")
internal class FriendListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                // Flutter `AppNavBar(title: '好友')`（showBackButton: false，无 leading）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppChrome.surface),
                ) {
                    Spacer(Modifier.height(top.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppChrome.NAV_BAR_HEIGHT.dp),
                    ) {
                        Text(
                            text = "好友",
                            fontSize = AppChrome.TITLE_SIZE.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppChrome.labelPrimary,
                            maxLines = 1,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppChrome.HAIRLINE.dp)
                            .background(AppChrome.hairline),
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Friend 模块",
                        fontSize = 14.sp,
                        color = AppChrome.labelPrimary,
                    )
                }
            }
        }
    }
}
