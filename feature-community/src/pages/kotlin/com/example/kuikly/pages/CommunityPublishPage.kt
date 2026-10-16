package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.community.CommunityStore
import com.example.kuikly.data.community.Topic
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
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

private val PUBLISH_CANDIDATES = listOf(
    "今天跑了 5 公里，状态不错～",
    "分享一个效率工具清单",
    "新出的降噪耳机真香",
    "周末去哪儿玩？求推荐",
)

private val PUBLISH_MEDIA = listOf("none", "image", "video")

private const val PUBLISH_MAX_LENGTH = 200

@Page("CommunityPublish")
internal class CommunityPublishPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var draft by remember { mutableStateOf("") }
            var mediaType by remember { mutableStateOf("none") }
            var topic by remember { mutableStateOf<String?>(null) }
            var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            if (!booted) {
                booted = true
                CommunityStore.repo.topics().onSuccess { topics = it }
            }

            fun doPublish() {
                if (draft.isBlank()) {
                    error = "内容不能为空"
                    Utils.currentBridgeModule().toast("内容不能为空")
                    return
                }
                CommunityStore.repo.publish(draft, topic)
                    .onSuccess {
                        error = null
                        Utils.currentBridgeModule().toast("发布成功")
                        Utils.currentBridgeModule().closePage()
                    }
                    .onFailure { error = it.message ?: "发布失败" }
            }

            Column(
                modifier = Modifier.fillMaxSize()
                    .background(KuiklyTheme.canvasSoft2)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 取消",
                        fontSize = 15.sp,
                        color = KuiklyTheme.muted,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("发布动态", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                        Text(
                            "${draft.length}/$PUBLISH_MAX_LENGTH",
                            fontSize = 11.sp,
                            color = if (draft.length > PUBLISH_MAX_LENGTH) KuiklyTheme.error else KuiklyTheme.muted,
                        )
                    }
                }

                if (error != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(error ?: "", fontSize = 13.sp, color = KuiklyTheme.errorDeep)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // 正文区（mock 无 TextField，用候选内容快捷切换代替输入）
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(12.dp),
                ) {
                    Text("正文", fontSize = 12.sp, color = KuiklyTheme.muted)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .height(90.dp)
                            .background(KuiklyTheme.canvasSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                            .padding(10.dp),
                    ) {
                        Text(
                            if (draft.isBlank()) "点击下面的候选内容，切换要发布的正文" else draft,
                            fontSize = 14.sp,
                            color = if (draft.isBlank()) KuiklyTheme.muted else KuiklyTheme.ink,
                            maxLines = 4,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    PUBLISH_CANDIDATES.forEach { candidate ->
                        val selected = draft == candidate
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                .background(
                                    if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                                    RoundedCornerShape(KuiklyTheme.radiusSm),
                                )
                                .clickable { draft = if (selected) "" else candidate }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Text(
                                candidate,
                                fontSize = 13.sp,
                                color = if (selected) KuiklyTheme.linkDeep else KuiklyTheme.body,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 媒体类型
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(12.dp),
                ) {
                    Text("媒体", fontSize = 12.sp, color = KuiklyTheme.muted)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PUBLISH_MEDIA.forEach { m ->
                            MediaChip(
                                label = mediaLabel(m),
                                selected = mediaType == m,
                                onClick = { mediaType = m },
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    if (mediaType != "none") {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(120.dp)
                                .background(KuiklyTheme.hairline, RoundedCornerShape(KuiklyTheme.radiusSm))
                                .clickable { Utils.currentBridgeModule().toast("预览${mediaLabel(mediaType)}（mock）") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${mediaLabel(mediaType)}占位预览 · 点击预览",
                                fontSize = 12.sp,
                                color = KuiklyTheme.body,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "发布后将使用默认示例媒体（不上传所选文件）",
                            fontSize = 11.sp,
                            color = KuiklyTheme.muted,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 话题
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                Utils.currentBridgeModule().openPage(url = "CommunityTopicSelect")
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                topic ?: "关联话题",
                                fontSize = 15.sp,
                                color = if (topic == null) KuiklyTheme.muted else KuiklyTheme.link,
                                fontWeight = FontWeight.Medium,
                            )
                            Text("在话题选择页挑选 ›", fontSize = 11.sp, color = KuiklyTheme.muted)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    topics.forEach { t ->
                        val selected = topic == t.name
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                .background(
                                    if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                                    RoundedCornerShape(KuiklyTheme.radiusSm),
                                )
                                .clickable { topic = if (selected) null else t.name }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t.displayName, fontSize = 13.sp, color = KuiklyTheme.ink)
                                Spacer(Modifier.width(6.dp))
                                KChip(t.heatLabel, color = KuiklyTheme.muted)
                                if (t.isAskEveryone) {
                                    Spacer(Modifier.width(6.dp))
                                    KChip("问大家", color = KuiklyTheme.warning)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            if (draft.isBlank()) KuiklyTheme.hairline else KuiklyTheme.primary,
                            RoundedCornerShape(KuiklyTheme.radiusMd),
                        )
                        .clickable { doPublish() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("发布", fontSize = 15.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clickable {
                            draft = ""
                            topic = null
                            mediaType = "none"
                            error = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("重置", fontSize = 13.sp, color = KuiklyTheme.muted)
                }
            }
        }
    }
}

private fun mediaLabel(type: String): String = when (type) {
    "image" -> "图片"
    "video" -> "视频"
    else -> "无媒体"
}

@Composable
private fun MediaChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.primary else KuiklyTheme.canvasSoft2,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
