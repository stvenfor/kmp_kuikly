package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.community.CommunityStore
import com.example.kuikly.data.community.Post
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

private val FEED_TABS = listOf("最新" to "latest", "热门" to "hot", "关注" to "following")
private const val PAGE_SIZE = 10

@Page("Community")
internal class CommunityPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var tab by remember { mutableStateOf("latest") }
            var items by remember { mutableStateOf<List<Post>>(emptyList()) }
            var page by remember { mutableStateOf(1) }
            var hasMore by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(false) }
            var isLoadingMore by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun load(reset: Boolean) {
                if (reset) {
                    page = 1
                    isLoading = true
                    error = null
                } else {
                    if (!hasMore || isLoadingMore) return
                    isLoadingMore = true
                }
                CommunityStore.repo.feed(tab, page, PAGE_SIZE)
                    .onSuccess { r ->
                        items = if (reset) r.items else items + r.items
                        hasMore = r.hasMore
                        page = if (reset) (if (r.items.isEmpty()) 1 else 2) else page + 1
                        error = null
                    }
                    .onFailure { e -> if (reset) error = e.message ?: "加载失败" }
                isLoading = false
                isLoadingMore = false
            }

            fun toggleLike(post: Post) {
                CommunityStore.repo.toggleLike(post.id, !post.isLiked)
                    .onSuccess { updated ->
                        items = items.map { if (it.id == post.id) updated else it }
                        Utils.currentBridgeModule().toast(if (updated.isLiked) "已赞" else "已取消赞")
                    }
                    .onFailure { Utils.currentBridgeModule().toast("操作失败") }
            }

            if (!booted) {
                booted = true
                load(true)
            }

            val scenario = MockBackend.scenario

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                // 头部：标题 + 发布/搜索入口（对齐源 _CommunityHeader）。
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("社区", fontSize = 22.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .background(KuiklyTheme.primary, RoundedCornerShape(18.dp))
                                .clickable { Utils.currentBridgeModule().openPage(url = "CommunityPublish") }
                                .padding(9.dp),
                        ) {
                            Text("+", fontSize = 18.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .border(0.5.dp, KuiklyTheme.hairline, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .clickable { Utils.currentBridgeModule().openPage(url = "CommunitySearch") }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                    ) {
                        Text("搜索动态、话题、用户", fontSize = 14.sp, color = KuiklyTheme.muted)
                    }
                    Spacer(Modifier.height(12.dp))
                    // 顶栏 Tab（对齐源 _FilterTabs：最新/热门/关注）。
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FEED_TABS.forEach { (label, key) ->
                            val active = tab == key
                            Column(
                                modifier = Modifier
                                    .clickable { if (!active) { tab = key; load(true) } }
                                    .padding(end = 20.dp),
                            ) {
                                Text(
                                    label,
                                    fontSize = 15.sp,
                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (active) KuiklyTheme.foreground else KuiklyTheme.muted,
                                )
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(if (active) 20.dp else 0.dp)
                                        .background(KuiklyTheme.primary, RoundedCornerShape(1.dp)),
                                )
                            }
                        }
                    }
                }

                // 慢速场景提示（对齐 Mock 四态之 Slow）。
                if (scenario == MockScenario.Slow) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            .background(KuiklyTheme.warningSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text("慢速模式：模拟弱网，数据加载较慢", fontSize = 12.sp, color = KuiklyTheme.warningDeep)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                when {
                    error != null && items.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("加载失败", fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(error ?: "", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                                    .clickable { load(true) }
                                    .padding(horizontal = 22.dp, vertical = 9.dp),
                            ) {
                                Text("重试", fontSize = 13.sp, color = KuiklyTheme.onPrimary)
                            }
                        }
                    }
                    items.isEmpty() && !isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("暂无动态", fontSize = 16.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (tab == "following") "关注作者后即可在这里看到 TA 的动态" else "发布第一条动态，开始互动吧",
                                fontSize = 13.sp,
                                color = KuiklyTheme.muted,
                            )
                            Spacer(Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                                    .clickable { load(true) }
                                    .padding(horizontal = 22.dp, vertical = 9.dp),
                            ) {
                                Text("刷新", fontSize = 13.sp, color = KuiklyTheme.onPrimary)
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                        ) {
                            items(items) { post -> FeedPostRow(post = post, onLike = { toggleLike(post) }) }
                            item {
                                if (isLoadingMore) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("加载中…", fontSize = 13.sp, color = KuiklyTheme.muted)
                                    }
                                } else if (hasMore) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                            .clickable { load(false) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("点击加载更多", fontSize = 13.sp, color = KuiklyTheme.link)
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("没有更多了", fontSize = 13.sp, color = KuiklyTheme.muted)
                                    }
                                }
                            }
                        }
                    }
                }

                // Mock 场景切换（验证 Empty / Error / Slow 三态）。
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clickable {
                            MockBackend.cycle()
                            load(true)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("[CYCLE MOCK] $scenario", fontSize = 12.sp, color = KuiklyTheme.muted)
                }
            }
        }
    }
}

@Composable
private fun FeedPostRow(post: Post, onLike: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable {
                val params = JSONObject()
                params.put("id", post.id)
                Utils.currentBridgeModule().openPage(url = "PostDetail", userData = params)
            }
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
                    .background(KuiklyTheme.linkSoft, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(post.nickname.take(1), fontSize = 14.sp, color = KuiklyTheme.linkDeep, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(post.nickname, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                Text(post.publishTime, fontSize = 11.sp, color = KuiklyTheme.muted)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(post.content, fontSize = 15.sp, color = KuiklyTheme.ink, maxLines = 4)
        post.topicName?.let { name ->
            Spacer(Modifier.height(6.dp))
            Text("#$name", fontSize = 13.sp, color = KuiklyTheme.link)
        }
        // 预览评论（对齐源 CommentPreviewWidget）。
        post.previewComments.firstOrNull()?.let { c ->
            Spacer(Modifier.height(8.dp))
            Text(
                if (c.replyToNickname != null) "${c.nickname} 回复 ${c.replyToNickname}：${c.content}" else "${c.nickname}：${c.content}",
                fontSize = 13.sp,
                color = KuiklyTheme.muted,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 点赞（对齐源 LikeBarWidget：翻转 + 计数）。
            Row(
                modifier = Modifier.clickable { onLike() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (post.isLiked) "♥" else "♡", fontSize = 18.sp, color = if (post.isLiked) KuiklyTheme.error else KuiklyTheme.muted)
                Spacer(Modifier.width(4.dp))
                Text(if (post.likeCount > 0) post.likeCount.toString() else "赞", fontSize = 13.sp, color = KuiklyTheme.muted)
            }
            Spacer(Modifier.width(24.dp))
            // 评论（打开评论页）。
            Row(
                modifier = Modifier.clickable {
                    val params = JSONObject()
                    params.put("id", post.id)
                    Utils.currentBridgeModule().openPage(url = "CommunityComments", userData = params)
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("💬", fontSize = 18.sp)
                Spacer(Modifier.width(4.dp))
                Text(if (post.commentCount > 0) post.commentCount.toString() else "评论", fontSize = 13.sp, color = KuiklyTheme.muted)
            }
        }
    }
}
