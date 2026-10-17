package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.auth.MockSessionGuard
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
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
 * 登录态展示 + 登出（源 `UserService.currentUser` + `AuthSession.logout`）。
 * 已登录：展示账号、门店、角色、Token 与操作入口；未登录：引导去登录。
 */
@Page("AuthAccount")
internal class AccountPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var user by remember { mutableStateOf(AuthSession.repo.currentUser()) }
            var expired by remember { mutableStateOf(MockSessionGuard.expired) }

            fun logout() {
                MockSessionGuard.forceLogout()
                user = AuthSession.repo.currentUser()
                expired = false
                Utils.currentBridgeModule().openPage(url = "Login", closeCurPage = true)
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AuthNavBar(title = "账号中心", subtitle = "登录态与门店管理")
                Spacer(Modifier.height(16.dp))

                if (user == null) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        AuthInfoText(message = "当前未登录")
                        Spacer(Modifier.height(12.dp))
                        AuthPrimaryButton(text = "去登录", onClick = {
                            Utils.currentBridgeModule().openPage(url = "Login")
                        })
                    }
                } else {
                    val localUser = user!!
                    val roleLabel = when (localUser.role) {
                        2 -> "总经理"
                        1 -> "销售经理"
                        else -> "销售顾问"
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                    ) {
                        Text(localUser.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = KuiklyTheme.foreground)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (localUser.email.isNotEmpty()) localUser.email else "手机号 ${localUser.phone}",
                            fontSize = 13.sp,
                            color = KuiklyTheme.muted,
                        )
                        Spacer(Modifier.height(16.dp))
                        InfoRow(label = "门店", value = localUser.storeName.ifEmpty { "未分配" })
                        Spacer(Modifier.height(8.dp))
                        InfoRow(label = "角色", value = roleLabel)
                        Spacer(Modifier.height(8.dp))
                        InfoRow(label = "登录态", value = if (expired) "已过期" else "已登录")
                        Spacer(Modifier.height(8.dp))
                        InfoRow(label = "Token", value = MockSessionGuard.tokenOf(localUser))
                    }

                    Spacer(Modifier.height(16.dp))

                    if (expired) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            AuthErrorText(message = "登录已失效，请前往处理或重新登录")
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        AccountAction(text = "切换门店", onClick = {
                            Utils.currentBridgeModule().openPage(url = "StoreSwitch")
                        })
                        Spacer(Modifier.height(12.dp))
                        AccountAction(text = if (expired) "前往处理过期" else "模拟 Token 过期", onClick = {
                            if (expired) {
                                Utils.currentBridgeModule().openPage(url = "TokenExpired")
                            } else {
                                MockSessionGuard.simulateExpiry()
                                expired = true
                                Utils.currentBridgeModule().openPage(url = "TokenExpired")
                            }
                        })
                        Spacer(Modifier.height(12.dp))
                        AccountAction(text = "退出登录", danger = true, onClick = ::logout)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** 信息行：标签 + 值。 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp, color = KuiklyTheme.muted, modifier = Modifier.width(64.dp))
        Text(value, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
    }
}

/** 通栏操作行（源账号页按钮）。 */
@Composable
private fun AccountAction(text: String, danger: Boolean = false, onClick: () -> Unit) {
    val fg = if (danger) KuiklyTheme.errorDeep else KuiklyTheme.linkDeep
    val bg = if (danger) KuiklyTheme.errorSoft else KuiklyTheme.linkSoft
    Box(
        modifier = Modifier.fillMaxWidth().height(48.dp)
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 16.sp, color = fg, fontWeight = FontWeight.Bold)
    }
}
