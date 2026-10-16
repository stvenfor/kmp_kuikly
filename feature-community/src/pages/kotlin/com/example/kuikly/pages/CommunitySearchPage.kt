package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.community.CommunitySearchResult
import com.example.kuikly.data.community.CommunityStore
import com.example.kuikly.data.community.Post
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

private val SEARCH_TABS = listOf("全部", "动态", "话题", "用户")

private val SEARCH_KEYWORDS = listOf("健身", "耳机", "效率", "小鹿", "跑步")

@Page("CommunitySearch")
internal class CommunitySearchPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var query by remember { mutableStateOf("") }
            var tabIndex by remember { mutableStateOf(0) }
            var result by remember { mutableStateOf<CommunitySearchResult?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var hotTopics by remember { mutableStateOf<List<Topic>>(emptyList()) }
            var booted by remember { mutableStateOf(false) }

            fun runSearch(keyword: String) {
                query = keyword
                CommunityStore.repo.search(keyword)
                    .onSuccess {
                        result = it
                        error = null
                    }
                    .onFailure {
                        result = null
                        error = it.message ?: "搜索失败"
                    }
            }

            if (!booted) {
                booted = true
                CommunityStore.repo.rankedTopics().onSuccess { hotTopics = it }
            }

            val res = result
            val showEmpty = query.isNotBlank() && error == null && res != null && res.isEmpty

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
                            Text("搜索", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                            Text("搜索动态、话题、用户", fontSize = 11.sp, color = KuiklyTheme.muted)
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusLg))
                            .padding(12.dp),
                    ) {
                        Text(
                            if (query.isBlank()) "点击候选关键词开始搜索" else "当前关键词：$query",
                            fontSize = 14.sp,
                            color = if (query.isBlank()) KuiklyTheme.muted else KuiklyTheme.ink,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SEARCH_KEYWORDS.forEach { kw ->
                                KeywordChip(
                                    label = kw,
                                    selected = query == kw,
                                    onClick = { runSearch(if (query == kw) "" else kw) },
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SEARCH_TABS.forEachIndexed { index, label ->
                            SearchTab(
                                label = label,
                                selected = tabIndex == index,
                                onClick = { tabIndex = index },
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (error != null) {
                    item {
                        SearchStateBlock(
                            title = error ?: "搜索失败",
                            subtitle = "点击重试",
                            onClick = { runSearch(query) },
                        )
                    }
                } else if (query.isBlank()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                                .padding(12.dp),
                        ) {
                            Text("热门话题", fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            if (hotTopics.isEmpty()) {
                                Text("暂无热门话题", fontSize = 12.sp, color = KuiklyTheme.muted)
                            } else {
                                hotTopics.forEach { t ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable { runSearch(t.name) }
                                            .padding(vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(t.displayName, fontSize = 15.sp, color = KuiklyTheme.ink)
                                        }
                                        Text(t.heatLabel, fontSize = 12.sp, color = KuiklyTheme.muted)
                                    }
                                }
                            }
                        }
                    }
                } else if (showEmpty) {
                    item {
                        SearchStateBlock(
                            title = "暂无搜索结果",
                            subtitle = "换个关键词试试",
                            actionLabel = "清空关键词",
                            onClick = { query = ""; result = null },
                        )
                    }
                } else if (res != null) {
                    when (tabIndex) {
                        0 -> {
                            if (res.posts.isNotEmpty()) {
                                item { SearchSectionHeader("动态 · ${res.posts.size}") }
                                items(res.posts) { post -> SearchPostRow(post = post) }
                            }
                            if (res.topics.isNotEmpty()) {
                                item { SearchSectionHeader("话题 · ${res.topics.size}") }
                                items(res.topics) { topic -> SearchTopicRow(topic = topic, onClick = { runSearch(topic.name) }) }
                            }
                            if (res.users.isNotEmpty()) {
                                item { SearchSectionHeader("用户 · ${res.users.size}") }
                                items(res.users) { user ->
                                    SearchUserRow(
                                        name = user.nickname,
                                        followed = user.isFollowed,
                                        onClick = {
                                            CommunityStore.repo.toggleFollow(user.id)
                                                .onSuccess {
                                                    Utils.currentBridgeModule()
                                                        .toast(if (it.isFollowed) "已关注 ${it.nickname}" else "已取消关注")
                                                    runSearch(query)
                                                }
                                        },
                                    )
                                }
                            }
                        }
                        1 -> {
                            if (res.posts.isEmpty()) {
                                item { SearchStateBlock("暂无动态", "换个关键词试试", actionLabel = "清空关键词", onClick = { query = ""; result = null }) }
                            } else {
                                item { SearchSectionHeader("动态 · ${res.posts.size}") }
                                items(res.posts) { post -> SearchPostRow(post = post) }
                            }
                        }
                        2 -> {
                            if (res.topics.isEmpty()) {
                                item { SearchStateBlock("暂无话题", "换个关键词试试", actionLabel = "清空关键词", onClick = { query = ""; result = null }) }
                            } else {
                                item { SearchSectionHeader("话题 · ${res.topics.size}") }
                                items(res.topics) { topic -> SearchTopicRow(topic = topic, onClick = { runSearch(topic.name) }) }
                            }
                        }
                        else -> {
                            if (res.users.isEmpty()) {
                                item { SearchStateBlock("暂无用户", "换个关键词试试", actionLabel = "清空关键词", onClick = { query = ""; result = null }) }
                            } else {
                                item { SearchSectionHeader("用户 · ${res.users.size}") }
                                items(res.users) { user ->
                                    SearchUserRow(
                                        name = user.nickname,
                                        followed = user.isFollowed,
                                        onClick = {
                                            CommunityStore.repo.toggleFollow(user.id)
                                                .onSuccess {
                                                    Utils.currentBridgeModule()
                                                        .toast(if (it.isFollowed) "已关注 ${it.nickname}" else "已取消关注")
                                                    runSearch(query)
                                                }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun SearchTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.primary else KuiklyTheme.canvas,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun KeywordChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) KuiklyTheme.linkDeep else KuiklyTheme.body,
        )
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        color = KuiklyTheme.muted,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SearchPostRow(post: Post) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(post.nickname, fontSize = 13.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            Text(post.publishTime, fontSize = 11.sp, color = KuiklyTheme.muted)
            val topicName = post.topicName
            if (topicName != null) {
                Spacer(Modifier.width(8.dp))
                KChip("#$topicName", color = KuiklyTheme.link)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(post.content, fontSize = 15.sp, color = KuiklyTheme.ink, maxLines = 3)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip("❤ ${post.likeCount}", color = KuiklyTheme.highlightMagenta)
            Spacer(Modifier.width(8.dp))
            KChip("💬 ${post.commentCount}", color = KuiklyTheme.muted)
            Spacer(Modifier.width(8.dp))
            KChip(post.mediaLabel, color = KuiklyTheme.violet)
        }
    }
}

@Composable
private fun SearchTopicRow(topic: Topic, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(topic.displayName, fontSize = 15.sp, color = KuiklyTheme.ink)
            if (topic.isAskEveryone) {
                Text("问大家 · 关联后邀请盘友回答", fontSize = 11.sp, color = KuiklyTheme.muted)
            }
        }
        Text(topic.heatLabel, fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun SearchUserRow(name: String, followed: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(36.dp)
                .background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusPill)),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.take(1), fontSize = 14.sp, color = KuiklyTheme.linkDeep, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(name, fontSize = 15.sp, color = KuiklyTheme.ink)
            Text("社区用户", fontSize = 11.sp, color = KuiklyTheme.muted)
        }
        Box(
            modifier = Modifier
                .background(
                    if (followed) KuiklyTheme.canvasSoft2 else KuiklyTheme.link,
                    RoundedCornerShape(KuiklyTheme.radiusPill),
                )
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (followed) "已关注" else "关注",
                fontSize = 12.sp,
                color = if (followed) KuiklyTheme.body else KuiklyTheme.onPrimary,
            )
        }
    }
}

@Composable
private fun SearchStateBlock(
    title: String,
    subtitle: String,
    actionLabel: String = "重试",
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
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
