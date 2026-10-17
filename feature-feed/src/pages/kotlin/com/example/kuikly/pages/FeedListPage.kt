package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.feed.FeedItem
import com.example.kuikly.data.feed.FeedStore
import com.example.kuikly.data.feed.FeedTabs
import com.example.kuikly.data.mock.MockBackend
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
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

private val FEED_BG = Color(0xFFF2F2F7)
private val FEED_SURFACE = Color(0xFFFFFFFF)
private val FEED_FG = Color(0xFF1C1C1E)
private val FEED_MUTED = Color(0x993C3C43)
private val FEED_HAIRLINE = Color(0xFFE5E5EA)
private val FEED_ACCENT = Color(0xFF1565C0)
private val FEED_LIKE = Color(0xFFFA3E3E)

@Page("FeedList")
internal class FeedListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var tab by remember { mutableStateOf("recommend") }
            var posts by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(false) }
            var isLoadingMore by remember { mutableStateOf(false) }
            var hasMore by remember { mutableStateOf(true) }
            var total by remember { mutableStateOf(0) }
            var page by remember { mutableStateOf(0) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenario = MockBackend.scenario
                isLoading = true
                FeedStore.repo.fetchPage(1, tab)
                    .onSuccess { r ->
                        posts = r.items
                        total = r.total
                        hasMore = r.hasMore
                        page = if (r.items.isEmpty()) 0 else 1
                        empty = r.items.isEmpty()
                        error = null
                    }
                    .onFailure {
                        posts = emptyList()
                        empty = false
                        error = it.message ?: "加载失败"
                    }
                isLoading = false
            }

            fun loadMore() {
                if (isLoadingMore || !hasMore || isLoading) return
                isLoadingMore = true
                val next = page + 1
                FeedStore.repo.fetchPage(next, tab)
                    .onSuccess { r ->
                        if (r.items.isEmpty()) {
                            hasMore = false
                        } else {
                            posts = posts + r.items
                            page = next
                            total = r.total
                            hasMore = r.hasMore
                        }
                    }
                isLoadingMore = false
            }

            fun applyLike(id: String) {
                FeedStore.repo.toggleLike(id).onSuccess { updated ->
                    posts = posts.map { if (it.id == id) updated else it }
                }
            }

            fun applyFavorite(id: String) {
                FeedStore.repo.toggleFavorite(id).onSuccess { updated ->
                    posts = posts.map { if (it.id == id) updated else it }
                }
            }

            fun openDetail(item: FeedItem) {
                val data = JSONObject().apply { put("id", item.id) }
                Utils.currentBridgeModule().openPage(url = "FeedDetail", userData = data)
            }

            if (!booted) {
                booted = true
                if (AuthSession.repo.currentUser() == null) {
                    error = "Not logged in — open Login page"
                } else {
                    reload()
                }
            }

            Column(modifier = Modifier.fillMaxSize().background(FEED_BG)) {
                // 顶栏：标题 + 发布入口 + 刷新
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("动态", fontSize = 22.sp, color = FEED_FG, fontWeight = FontWeight.Bold)
                        Text(
                            "Hi, ${AuthSession.repo.currentUser()?.name ?: "?"}",
                            fontSize = 12.sp, color = FEED_MUTED,
                        )
                    }
                    Box(
                        modifier = Modifier.clickable { reload() }
                            .background(FEED_SURFACE, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("刷新", fontSize = 14.sp, color = FEED_ACCENT, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.clickable {
                            Utils.currentBridgeModule().openPage(url = "FeedPublish")
                        }
                            .background(FEED_ACCENT, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Tab 切换：推荐 / 关注 / 附近
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FeedTabs.labels.forEachIndexed { i, label ->
                        val key = FeedTabs.keys[i]
                        val active = key == tab
                        Column(
                            modifier = Modifier.clickable { if (!active) { tab = key; reload() } }
                                .padding(end = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                label,
                                fontSize = 15.sp,
                                color = if (active) FEED_FG else FEED_MUTED,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier.height(2.dp).width(if (active) 20.dp else 0.dp)
                                    .background(FEED_ACCENT, RoundedCornerShape(1.dp)),
                            )
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(FEED_HAIRLINE))

                when {
                    isLoading && posts.isEmpty() ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("加载中…", color = FEED_MUTED, fontSize = 15.sp)
                        }
                    error != null && posts.isEmpty() ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(error ?: "加载失败", color = FEED_MUTED, fontSize = 15.sp)
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier.clickable { reload() }
                                        .background(FEED_ACCENT, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 24.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("重试", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    empty ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("暂无动态", fontSize = 16.sp, color = FEED_FG, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(6.dp))
                                Text("发布第一条动态，开始互动吧", fontSize = 13.sp, color = FEED_MUTED)
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier.clickable { Utils.currentBridgeModule().openPage(url = "FeedPublish") }
                                        .background(FEED_ACCENT, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 24.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("去发布", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        items(posts, key = { it.id }) { post ->
                            val index = posts.indexOf(post)
                            if (index >= posts.size - 1) loadMore()
                            FeedCard(
                                post = post,
                                onOpen = { openDetail(post) },
                                onLike = { applyLike(post.id) },
                                onFavorite = { applyFavorite(post.id) },
                                onShare = {
                                    FeedStore.repo.incrementShare(post.id)
                                    Utils.currentBridgeModule().toast("已分享")
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        if (isLoadingMore) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("加载更多…", color = FEED_MUTED, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // 场景切换（测试用），与现有 FeedList 风格一致
                Box(
                    modifier = Modifier.fillMaxWidth().clickable {
                        scenario = MockBackend.cycle()
                        if (AuthSession.repo.currentUser() != null) reload()
                    }.padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("[CYCLE SCENARIO] now=$scenario", fontSize = 11.sp, color = FEED_MUTED)
                }
            }
        }
    }
}

@Composable
private fun FeedCard(
    post: FeedItem,
    onOpen: () -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(FEED_SURFACE, RoundedCornerShape(12.dp))
            .clickable { onOpen() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.width(36.dp).height(36.dp)
                    .background(FEED_HAIRLINE, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    post.author.take(1),
                    fontSize = 16.sp, color = FEED_ACCENT, fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.author, fontSize = 15.sp, color = FEED_FG, fontWeight = FontWeight.Medium)
                Text("${post.timeLabel} · ${post.source}", fontSize = 11.sp, color = FEED_MUTED)
            }
            if (post.isMine) {
                Text("我的", fontSize = 11.sp, color = FEED_MUTED)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(post.content, fontSize = 15.sp, color = FEED_FG)
        if (post.topic != null || post.mentions.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.topic != null) {
                    Text("#${post.topic}", fontSize = 13.sp, color = FEED_ACCENT)
                }
                post.mentions.forEach { m ->
                    Spacer(Modifier.width(8.dp))
                    Text("@$m", fontSize = 13.sp, color = FEED_ACCENT)
                }
            }
        }
        if (post.hasVideo) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .background(FEED_HAIRLINE, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("▶ 视频", fontSize = 14.sp, color = FEED_MUTED)
            }
        } else if (post.hasImages) {
            Spacer(Modifier.height(10.dp))
            ImageGrid(images = post.images)
        }
        if (post.comments.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            post.comments.take(2).forEach { c ->
                val prefix = if (c.replyToNickname != null) "回复 @${c.replyToNickname}：" else ""
                Text(
                    "${c.nickname}：$prefix${c.content}",
                    fontSize = 12.sp, color = FEED_MUTED,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActionItem(
                label = if (post.likeCount > 0) "${post.likeCount}" else "赞",
                active = post.isLiked,
                activeColor = FEED_LIKE,
                onClick = onLike,
            )
            Spacer(Modifier.width(24.dp))
            ActionItem(
                label = if (post.commentCount > 0) "${post.commentCount}" else "评论",
                active = false,
                activeColor = FEED_MUTED,
                onClick = onOpen,
            )
            Spacer(Modifier.width(24.dp))
            ActionItem(
                label = if (post.shareCount > 0) "${post.shareCount}" else "分享",
                active = false,
                activeColor = FEED_MUTED,
                onClick = onShare,
            )
            Spacer(Modifier.weight(1f))
            ActionItem(
                label = if (post.favoriteCount > 0) "${post.favoriteCount}" else "收藏",
                active = post.isFavorited,
                activeColor = FEED_ACCENT,
                onClick = onFavorite,
            )
        }
    }
}

@Composable
private fun ActionItem(label: String, active: Boolean, activeColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = if (active) activeColor else FEED_MUTED)
    }
}

@Composable
private fun ImageGrid(images: List<String>) {
    val cols = 3
    val rows = (images.size + cols - 1) / cols
    Column {
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until cols) {
                    val idx = r * cols + c
                    if (idx < images.size) {
                        Box(
                            modifier = Modifier.weight(1f).height(100.dp)
                                .background(FEED_HAIRLINE, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("图", fontSize = 12.sp, color = FEED_MUTED)
                        }
                        if (c < cols - 1) Spacer(Modifier.width(6.dp))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            if (r < rows - 1) Spacer(Modifier.height(6.dp))
        }
    }
}
