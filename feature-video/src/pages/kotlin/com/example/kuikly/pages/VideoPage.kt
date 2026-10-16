package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import com.example.kuikly.data.video.ShortVideo
import com.example.kuikly.data.video.ShortVideoStatus
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KMetricPill
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

private const val VIDEO_PAGE_SIZE = 2

@Page("Video")
internal class VideoPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var items by remember { mutableStateOf<List<ShortVideo>>(emptyList()) }
            var page by remember { mutableStateOf(0) }
            var total by remember { mutableStateOf(0) }
            var hasMore by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenario = MockBackend.scenario
                VideoStore.repo.fetchPage(1, VIDEO_PAGE_SIZE)
                    .onSuccess { r ->
                        items = r.items
                        total = r.total
                        hasMore = r.hasMore
                        page = if (r.items.isEmpty()) 0 else 1
                        empty = r.items.isEmpty()
                        error = null
                    }
                    .onFailure {
                        items = emptyList()
                        page = 0
                        empty = false
                        error = it.message ?: "加载失败"
                    }
            }

            fun loadMore() {
                if (!hasMore) return
                val next = page + 1
                VideoStore.repo.fetchPage(next, VIDEO_PAGE_SIZE)
                    .onSuccess { r ->
                        if (r.items.isEmpty()) {
                            hasMore = false
                        } else {
                            items = items + r.items
                            page = next
                            total = r.total
                            hasMore = r.hasMore
                        }
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                VideoListHeader(count = items.size, total = total, page = page)
                when {
                    error != null && items.isEmpty() ->
                        VideoListErrorState(message = error ?: "加载失败", onRetry = ::reload)
                    empty -> VideoListEmptyState(onRefresh = ::reload)
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            VideoListTile(
                                item = item,
                                onTap = {
                                    val data = JSONObject().apply { put("id", item.id) }
                                    Utils.currentBridgeModule()
                                        .openPage(url = "VideoDetail", userData = data)
                                },
                                onDubbing = {
                                    val data = JSONObject().apply { put("id", item.id) }
                                    Utils.currentBridgeModule()
                                        .openPage(url = "VideoDubbing", userData = data)
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            VideoLoadMoreRow(
                                hasMore = hasMore,
                                page = page,
                                onLoadMore = ::loadMore,
                            )
                        }
                        item {
                            Spacer(Modifier.height(12.dp))
                            VideoMockCycleRow(scenario = scenario) {
                                scenario = MockBackend.cycle()
                                reload()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoListHeader(count: Int, total: Int, page: Int) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "已加载 $count / 共 $total · 第 $page 页",
            fontSize = 13.sp,
            color = KuiklyTheme.muted,
            fontWeight = FontWeight.Medium,
        )
        KChip("分页 $VIDEO_PAGE_SIZE 条/页", color = KuiklyTheme.link)
    }
}

@Composable
private fun VideoListTile(item: ShortVideo, onTap: () -> Unit, onDubbing: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onTap() }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VideoCoverBlock(title = item.title, duration = item.duration, size = 64)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    item.title,
                    fontSize = 15.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${item.uploaderLabel} · ${item.category} · ${item.duration}",
                    fontSize = 12.sp,
                    color = KuiklyTheme.muted,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KMetricPill("播放", item.viewLabel)
                    Spacer(Modifier.width(6.dp))
                    KMetricPill("点赞", item.likeLabel, accent = true)
                    Spacer(Modifier.width(6.dp))
                    KChip(
                        when (item.status) {
                            ShortVideoStatus.published -> "已发布"
                            ShortVideoStatus.reviewing -> "审核中"
                            ShortVideoStatus.draft -> "草稿"
                        },
                        color = if (item.status == ShortVideoStatus.published) KuiklyTheme.success else KuiklyTheme.warning,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            VideoProgressTrack(progress = VideoStore.repo.progressOf(item.id), trackWidth = 200)
            Spacer(Modifier.width(8.dp))
            Text(
                "${VideoStore.repo.playedSecOf(item.id)}s / ${item.durationSec}s",
                fontSize = 11.sp,
                color = KuiklyTheme.muted,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onDubbing() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("配音任务", fontSize = 13.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Text("›", fontSize = 16.sp, color = KuiklyTheme.muted)
        }
    }
}

@Composable
internal fun VideoCoverBlock(title: String, duration: String, size: Int) {
    Box(
        modifier = Modifier
            .width(size.dp)
            .height(size.dp)
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (title.isEmpty()) "▶" else title.take(1),
                fontSize = 18.sp,
                color = KuiklyTheme.ink,
                fontWeight = FontWeight.Bold,
            )
            Text(duration, fontSize = 10.sp, color = KuiklyTheme.muted)
        }
    }
}

@Composable
internal fun VideoProgressTrack(progress: Double, trackWidth: Int = 260) {
    val ratio = progress.coerceIn(0.0, 1.0)
    Box(
        modifier = Modifier
            .width(trackWidth.dp)
            .height(6.dp)
            .background(KuiklyTheme.hairline, RoundedCornerShape(KuiklyTheme.radiusPill)),
    ) {
        Box(
            modifier = Modifier
                .width((trackWidth.toDouble() * ratio).toFloat().dp)
                .height(6.dp)
                .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusPill)),
        )
    }
}

@Composable
private fun VideoLoadMoreRow(hasMore: Boolean, page: Int, onLoadMore: () -> Unit) {
    if (!hasMore) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("已经到底了 · 共 $page 页", fontSize = 12.sp, color = KuiklyTheme.muted)
        }
        return
    }
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onLoadMore() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("加载更多", fontSize = 13.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun VideoMockCycleRow(scenario: MockScenario, onCycle: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clickable { onCycle() }.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("[CYCLE MOCK] $scenario", fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

@Composable
internal fun VideoListEmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无视频", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("点击重试或切换 Mock 场景", fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.clickable { onRefresh() }
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("重试", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun VideoListErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, fontSize = 14.sp, color = KuiklyTheme.error)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.clickable { onRetry() }
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("重试", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
