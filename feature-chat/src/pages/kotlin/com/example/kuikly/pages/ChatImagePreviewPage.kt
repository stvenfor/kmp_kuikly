package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.design.KChip
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
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

private val PREVIEW_ZOOMS = listOf("适应屏幕", "1:1", "2x")

@Page("ChatImagePreview")
internal class ChatImagePreviewPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val url = pageData.params.optString("url")
        val title = pageData.params.optString("title")
        setContent {
            var zoomIndex by remember { mutableStateOf(0) }

            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF111111))) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // 顶部操作条
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "‹ 关闭",
                            fontSize = 15.sp,
                            color = Color(0xFFFFFFFF),
                            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                if (title.isBlank()) "图片预览" else title,
                                fontSize = 16.sp,
                                color = Color(0xFFFFFFFF),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                            Text(
                                "共 1 张 · ${PREVIEW_ZOOMS[zoomIndex]}",
                                fontSize = 12.sp,
                                color = Color(0xFFAAAAAA),
                            )
                        }
                    }

                    // 图片占位区（mock 无网络图片加载，用色块 + 地址文本表达）
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .height(260.dp)
                            .background(Color(0xFF1F1F1F), RoundedCornerShape(KuiklyTheme.radiusLg))
                            .clickable {
                                zoomIndex = (zoomIndex + 1) % PREVIEW_ZOOMS.size
                                Utils.currentBridgeModule().toast("缩放：${PREVIEW_ZOOMS[zoomIndex]}")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🖼", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (url.isBlank()) "无图片地址" else url,
                                fontSize = 12.sp,
                                color = Color(0xFFCCCCCC),
                                maxLines = 2,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("点击切换缩放", fontSize = 11.sp, color = Color(0xFF888888))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KChip("适应屏幕", color = KuiklyTheme.link)
                        Spacer(Modifier.width(8.dp))
                        KChip("原图 1024×768", color = KuiklyTheme.muted)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        PreviewAction(text = "保存", onClick = {
                            Utils.currentBridgeModule().toast("已保存到相册（mock）")
                        })
                        Spacer(Modifier.width(10.dp))
                        PreviewAction(text = "转发", onClick = {
                            Utils.currentBridgeModule().toast("已转发（mock）")
                        })
                        Spacer(Modifier.width(10.dp))
                        PreviewAction(text = "收藏", onClick = {
                            Utils.currentBridgeModule().toast("已收藏（mock）")
                        })
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "mock 环境不加载远端图片，仅演示预览页结构与交互",
                        fontSize = 12.sp,
                        color = Color(0xFF888888),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewAction(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFF2A2A2A), RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 14.sp, color = Color(0xFFFFFFFF))
    }
}
