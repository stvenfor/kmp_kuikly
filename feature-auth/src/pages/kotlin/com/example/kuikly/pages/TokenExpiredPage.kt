package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.MockSessionGuard
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * Token 过期 / 会话失效提示与重新登录（源 `SessionGuardHook._handleForcedLogout`
 * + `AuthNavigation.resetToLogin`）。
 * 两条路径：刷新会话（源静默 refresh 成功）或重新登录（清本地会话回登录页）。
 */
@Page("TokenExpired")
internal class TokenExpiredPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var info by remember { mutableStateOf(MockSessionGuard.lastMessage.ifEmpty { "登录已失效，请重新登录" }) }

            fun refresh() {
                val ok = MockSessionGuard.refresh()
                info = MockSessionGuard.lastMessage.ifEmpty { "登录已失效，请重新登录" }
                if (ok) {
                    Utils.currentBridgeModule().toast("会话已刷新")
                    Utils.currentBridgeModule().closePage()
                }
            }

            fun reLogin() {
                MockSessionGuard.forceLogout()
                Utils.currentBridgeModule().openPage(url = "Login", closeCurPage = true)
            }

            Column(
                modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(64.dp))
                Box(
                    modifier = Modifier.width(64.dp).height(64.dp)
                        .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusLg)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = KuiklyTheme.errorDeep)
                }
                Spacer(Modifier.height(20.dp))
                Text("登录失效", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = KuiklyTheme.foreground)
                Spacer(Modifier.height(10.dp))
                Text(info, fontSize = 14.sp, color = KuiklyTheme.muted)
                Spacer(Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    AuthPrimaryButton(text = "刷新会话", onClick = ::refresh)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .clickable { reLogin() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("重新登录", fontSize = 16.sp, color = KuiklyTheme.errorDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
