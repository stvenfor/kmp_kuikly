package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.base.setTimeout
import com.example.kuikly.data.privacy.PrivacyConsentStore
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
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
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.ButtonDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextButton
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * Product Splash: privacy consent then Main.
 * Copy describes only first-slice capabilities (no unbuilt SDK claims).
 */
@Page("Splash")
internal class SplashPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        if (PrivacyConsentStore.isGranted()) {
            setTimeout(400) {
                Utils.currentBridgeModule().openPage(PageNames.Main, closeCurPage = true)
            }
        }
        setContent {
            var denied by remember { mutableStateOf(false) }
            var showPrivacy by remember {
                mutableStateOf(!PrivacyConsentStore.isGranted())
            }

            fun goMain() {
                Utils.currentBridgeModule().openPage(PageNames.Main, closeCurPage = true)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "kmp_kuikly",
                        fontSize = 28.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            denied -> "需同意隐私政策后才能继续使用"
                            showPrivacy -> "请阅读并同意隐私政策"
                            else -> "正在启动…"
                        },
                        fontSize = 16.sp,
                        color = Color.Gray,
                    )
                    if (denied) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            denied = false
                            showPrivacy = true
                        }) {
                            Text("重新选择")
                        }
                    }
                }

                if (showPrivacy) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                        ) {
                            Text(
                                "隐私政策与用户协议",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "为提供登录态与二手车列表等基础功能，我们需要在您同意后使用本地会话与 Mock 数据服务。" +
                                    "本版本不初始化推送或其他未接入的第三方 SDK。拒绝将无法继续使用本应用。",
                                fontSize = 14.sp,
                                color = Color(0xFF4B5563),
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = {
                                        showPrivacy = false
                                        denied = true
                                    },
                                ) {
                                    Text("不同意")
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        PrivacyConsentStore.grant()
                                        showPrivacy = false
                                        denied = false
                                        goMain()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1565C0),
                                    ),
                                ) {
                                    Text("同意并继续")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
