package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
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
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 发布小视频（mock 上传）— Flutter `ShortVideoPublishPage` 业务对齐。
 * 真源：`features/video/lib/short_video/view/short_video_publish_page.dart`
 * （标题 + 关联话题 + `repo.create`，本地预览不上传，使用默认片源）。
 *
 * 文本输入沿用本工程 AuthForm 的「候选词追加 + 退格 + 清空」口径（无可靠 TextField API）。
 */
@Page("VideoPublish")
internal class VideoPublishPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var title by remember { mutableStateOf("") }
            var topic by remember { mutableStateOf<String?>(null) }
            var busy by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }

            fun publish() {
                if (busy) return
                error = null
                busy = true
                VideoStore.repo.publish(title, topic)
                    .onSuccess {
                        Utils.currentBridgeModule().toast("发布成功（默认片源，审核中）")
                        Utils.currentBridgeModule().closePage()
                    }
                    .onFailure {
                        busy = false
                        error = it.message ?: "发布失败"
                        Utils.currentBridgeModule().toast(error ?: "发布失败")
                    }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AppNavBarBar(
                    title = "发布小视频",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = KuiklyTheme.canvas,
                    foreground = KuiklyTheme.foreground,
                    actions = {
                        Box(
                            modifier = Modifier
                                .clickable { publish() }
                                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusPill))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("发布", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                )
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    KCard {
                        Text("标题", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(
                                if (title.isEmpty()) "写个标题吧" else title,
                                fontSize = 14.sp,
                                color = if (title.isEmpty()) KuiklyTheme.muted else KuiklyTheme.ink,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("字数 ${title.length}/50", fontSize = 11.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.weight(1f))
                            Text(
                                "退格",
                                fontSize = 12.sp,
                                color = KuiklyTheme.link,
                                modifier = Modifier.clickable {
                                    title = if (title.isEmpty()) "" else title.dropLast(1)
                                }.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                            Text(
                                "清空",
                                fontSize = 12.sp,
                                color = KuiklyTheme.link,
                                modifier = Modifier.clickable { title = "" }.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("常用标题（点击追加）", fontSize = 11.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PUBLISH_TITLE_CANDIDATES.forEach { cand ->
                                KChip(cand, color = KuiklyTheme.link) {
                                    if (title.length < 50) title = (title + cand).take(50)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    KCard {
                        Text("关联话题（可选）", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KChip("不关联", color = if (topic == null) KuiklyTheme.primary else KuiklyTheme.muted) {
                                topic = null
                            }
                            PUBLISH_TOPICS.forEach { t ->
                                KChip(t, color = if (topic == t) KuiklyTheme.primary else KuiklyTheme.muted) {
                                    topic = t
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    KCard {
                        Text(
                            "本地视频与封面仅供预览确认，不会上传；发布后使用服务端默认片源与封面。",
                            fontSize = 12.sp,
                            color = KuiklyTheme.muted,
                        )
                    }
                    if (error != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(error ?: "", fontSize = 12.sp, color = KuiklyTheme.error)
                    }
                    Spacer(Modifier.height(16.dp))
                    KButton(
                        text = if (busy) "发布中…" else "确认发布",
                        enabled = !busy,
                        onClick = { publish() },
                    )
                    Spacer(Modifier.height((bottom + 12f).dp))
                }
            }
        }
    }
}

private val PUBLISH_TITLE_CANDIDATES = listOf("我的日常Vlog", "周末探店", "宠物日常", "今日穿搭", "城市夜骑")
private val PUBLISH_TOPICS = listOf("生活", "美食", "运动", "影视", "旅行")
