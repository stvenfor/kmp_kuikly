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
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

private val TOPIC_FILTERS = listOf("全部", "健身", "好物", "效率", "打卡")

@Page("CommunityTopicSelect")
internal class CommunityTopicSelectPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
            var filter by remember { mutableStateOf("全部") }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                val result = if (filter == "全部") {
                    CommunityStore.repo.rankedTopics()
                } else {
                    CommunityStore.repo.searchTopics(filter).map {
                        it.sortedWith(
                            compareByDescending<Topic> { t -> t.isAskEveryone }.thenByDescending { t -> t.heat },
                        )
                    }
                }
                result
                    .onSuccess {
                        topics = it
                        error = null
                    }
                    .onFailure {
                        topics = emptyList()
                        error = it.message ?: "加载话题失败"
                    }
            }

            if (!booted) { booted = true; reload() }

            val ask = topics.firstOrNull { it.isAskEveryone }
            val normal = topics.filter { !it.isAskEveryone }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .background(KuiklyTheme.canvasSoft2)
                    .padding(horizontal = 14.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "‹ 返回",
                            fontSize = 15.sp,
                            color = KuiklyTheme.link,
                            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("关联话题", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                            Text("共 ${topics.size} 个话题", fontSize = 11.sp, color = KuiklyTheme.muted)
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(12.dp),
                    ) {
                        Text("按关键词筛选", fontSize = 12.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TOPIC_FILTERS.forEach { f ->
                                TopicFilterChip(
                                    label = f,
                                    selected = filter == f,
                                    onClick = { filter = f; reload() },
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (error != null) {
                    item {
                        TopicStateBlock(title = error ?: "加载失败", actionLabel = "重试", onClick = { reload() })
                    }
                } else if (topics.isEmpty()) {
                    item {
                        TopicStateBlock(
                            title = "没有匹配的话题",
                            subtitle = "换个关键词或点重试",
                            actionLabel = "重置筛选",
                            onClick = { filter = "全部"; reload() },
                        )
                    }
                } else {
                    if (ask != null) {
                        item {
                            AskEveryoneCard(topic = ask)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    item {
                        Text("热门话题", fontSize = 13.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                    }
                    items(normal) { topic ->
                        TopicRow(topic = topic)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun AskEveryoneCard(topic: Topic) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusLg))
            .clickable {
                Utils.currentBridgeModule().toast("已选择 ${topic.displayName}")
                Utils.currentBridgeModule().closePage()
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(36.dp)
                .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusPill)),
            contentAlignment = Alignment.Center,
        ) {
            Text("#", fontSize = 18.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(topic.displayName, fontSize = 16.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "问大家 · 关联后将主动邀请相关盘友回答",
                fontSize = 12.sp,
                color = KuiklyTheme.body,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun TopicRow(topic: Topic) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable {
                Utils.currentBridgeModule().toast("已选择 ${topic.displayName}")
                Utils.currentBridgeModule().closePage()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(topic.displayName, fontSize = 16.sp, color = KuiklyTheme.ink)
            Spacer(Modifier.height(3.dp))
            KChip("热度 ${topic.heatLabel}", color = KuiklyTheme.muted)
        }
        Text("选择 ›", fontSize = 13.sp, color = KuiklyTheme.link)
    }
}

@Composable
private fun TopicFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.primary else KuiklyTheme.canvasSoft2,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body,
        )
    }
}

@Composable
private fun TopicStateBlock(
    title: String,
    subtitle: String = "点击按钮重试",
    actionLabel: String = "重试",
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .clickable { onClick() }
                .padding(horizontal = 22.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(actionLabel, fontSize = 13.sp, color = KuiklyTheme.onPrimary)
        }
    }
}
