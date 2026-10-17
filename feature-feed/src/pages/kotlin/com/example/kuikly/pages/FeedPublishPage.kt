package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.feed.FeedStore
import com.example.kuikly.data.feed.FeedTopic
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

private val PUB_BG = Color(0xFFF2F2F7)
private val PUB_SURFACE = Color(0xFFFFFFFF)
private val PUB_FG = Color(0xFF1C1C1E)
private val PUB_MUTED = Color(0x993C3C43)
private val PUB_HAIRLINE = Color(0xFFE5E5EA)
private val PUB_ACCENT = Color(0xFF1565C0)

private val PUBLISH_CANDIDATES = listOf(
    "今天去了推荐的咖啡店，环境不错。#好物推荐",
    "周末 hiking，天气太好了！#户外",
    "刚读完一本好书，推荐 @李四 也看看。",
    "分享一下最近在用的效率工具 #效率工具",
)
private val PUBLISH_MEDIA = listOf("none", "image", "video")
private const val PUBLISH_MAX_LENGTH = 200

@Page("FeedPublish")
internal class FeedPublishPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var draft by remember { mutableStateOf("") }
            var mediaType by remember { mutableStateOf("none") }
            var topic by remember { mutableStateOf<String?>(null) }
            var mention by remember { mutableStateOf<String?>(null) }
            var topics by remember { mutableStateOf<List<FeedTopic>>(emptyList()) }
            var mentions by remember { mutableStateOf<List<String>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            if (!booted) {
                booted = true
                FeedStore.repo.rankedTopics().onSuccess { topics = it }
                FeedStore.repo.mentionCandidates().onSuccess { mentions = it }
            }

            fun doPublish() {
                var content = draft
                if (mention != null && !content.contains("@$mention")) {
                    content = "$content @$mention"
                }
                FeedStore.repo.submitPost(content, topic, mediaType)
                    .onSuccess {
                        error = null
                        Utils.currentBridgeModule().toast("发布成功")
                        Utils.currentBridgeModule().closePage()
                    }
                    .onFailure {
                        error = it.message ?: "发布失败"
                        Utils.currentBridgeModule().toast(it.message ?: "发布失败")
                    }
            }

            Column(
                modifier = Modifier.fillMaxSize().background(PUB_BG)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 取消",
                        fontSize = 15.sp, color = PUB_MUTED,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("发布动态", fontSize = 18.sp, color = PUB_FG, fontWeight = FontWeight.Bold)
                        Text(
                            "${draft.length}/$PUBLISH_MAX_LENGTH",
                            fontSize = 11.sp,
                            color = if (draft.length > PUBLISH_MAX_LENGTH) Color(0xFFFA3E3E) else PUB_MUTED,
                        )
                    }
                }

                if (error != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0x14FA3E3E), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) { Text(error ?: "", fontSize = 13.sp, color = Color(0xFFC62828)) }
                    Spacer(Modifier.height(10.dp))
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(PUB_SURFACE, RoundedCornerShape(12.dp)).padding(12.dp),
                ) {
                    Text("正文", fontSize = 12.sp, color = PUB_MUTED)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                            .background(PUB_BG, RoundedCornerShape(8.dp)).padding(10.dp),
                    ) {
                        Text(
                            if (draft.isBlank()) "点击下面的候选内容，切换要发布的正文" else draft,
                            fontSize = 14.sp,
                            color = if (draft.isBlank()) PUB_MUTED else PUB_FG,
                            maxLines = 4,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    PUBLISH_CANDIDATES.forEach { candidate ->
                        val selected = draft == candidate
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                .background(if (selected) Color(0x1F1565C0) else PUB_BG, RoundedCornerShape(6.dp))
                                .clickable { draft = if (selected) "" else candidate }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Text(
                                candidate,
                                fontSize = 13.sp,
                                color = if (selected) PUB_ACCENT else PUB_FG,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(PUB_SURFACE, RoundedCornerShape(12.dp)).padding(12.dp),
                ) {
                    Text("媒体", fontSize = 12.sp, color = PUB_MUTED)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PUBLISH_MEDIA.forEach { m ->
                            MediaChip(mediaLabel(m), mediaType == m) { mediaType = m }
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    if (mediaType != "none") {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                                .background(PUB_HAIRLINE, RoundedCornerShape(8.dp))
                                .clickable { Utils.currentBridgeModule().toast("预览${mediaLabel(mediaType)}（mock）") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${mediaLabel(mediaType)}占位预览 · 点击预览", fontSize = 12.sp, color = PUB_MUTED)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(PUB_SURFACE, RoundedCornerShape(12.dp)).padding(12.dp),
                ) {
                    Text("话题", fontSize = 12.sp, color = PUB_MUTED)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        topics.forEach { t ->
                            val selected = topic == t.name
                            Box(
                                modifier = Modifier.padding(end = 8.dp)
                                    .background(if (selected) PUB_ACCENT else PUB_BG, RoundedCornerShape(14.dp))
                                    .clickable { topic = if (selected) null else t.name }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    t.displayName,
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else PUB_FG,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("提醒谁看（@用户）", fontSize = 12.sp, color = PUB_MUTED)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        mentions.forEach { u ->
                            val selected = mention == u
                            Box(
                                modifier = Modifier.padding(end = 8.dp)
                                    .background(if (selected) PUB_ACCENT else PUB_BG, RoundedCornerShape(14.dp))
                                    .clickable { mention = if (selected) null else u }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "@$u",
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else PUB_FG,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(if (draft.isBlank()) PUB_HAIRLINE else PUB_ACCENT, RoundedCornerShape(8.dp))
                        .clickable { doPublish() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("发布", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { draft = ""; topic = null; mention = null; mediaType = "none"; error = null }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("重置", fontSize = 13.sp, color = PUB_MUTED)
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
        modifier = Modifier.background(
            if (selected) PUB_ACCENT else PUB_BG,
            RoundedCornerShape(14.dp),
        ).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) Color.White else PUB_FG,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
