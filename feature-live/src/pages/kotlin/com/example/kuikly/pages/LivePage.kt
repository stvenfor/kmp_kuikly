package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.live.LiveRoom
import com.example.kuikly.data.live.LiveStatus
import com.example.kuikly.data.live.LiveStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
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

@Page("Live")
internal class LivePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var rooms by remember { mutableStateOf<List<LiveRoom>>(emptyList()) }
            var filter by remember { mutableStateOf("all") }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenario = MockBackend.scenario
                val result = when (filter) {
                    "live" -> LiveStore.repo.liveRooms()
                    "offline" -> LiveStore.repo.all { it.status == LiveStatus.offline }
                    else -> runCatching { LiveStore.repo.page(1, 50).getOrThrow().items }
                }
                result
                    .onSuccess { list ->
                        rooms = list
                        empty = list.isEmpty()
                        error = null
                    }
                    .onFailure {
                        rooms = emptyList()
                        empty = false
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Text(
                    "直播",
                    fontSize = 22.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                LiveFilterRow(filter = filter, onPick = { filter = it; reload() })
                Spacer(Modifier.height(8.dp))
                when {
                    error != null && rooms.isEmpty() ->
                        LiveErrorState(message = error ?: "加载失败", onRetry = ::reload)
                    empty -> LiveEmptyState(onRefresh = { filter = "all"; reload() })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    ) {
                        items(rooms, key = { it.id }) { room ->
                            LiveRoomTile(
                                room = room,
                                onTap = {
                                    val data = JSONObject().apply { put("id", room.id) }
                                    Utils.currentBridgeModule().openPage(url = "LiveRoom", userData = data)
                                },
                                onRefresh = {
                                    LiveStore.repo.refreshViewers(room.id)
                                    reload()
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            Spacer(Modifier.height(4.dp))
                            LiveMockCycleRow(scenario = scenario) {
                                scenario = MockBackend.cycle()
                                reload()
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveFilterRow(filter: String, onPick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiveFilterPill(text = "全部", selected = filter == "all") { onPick("all") }
        Spacer(Modifier.width(8.dp))
        LiveFilterPill(text = "直播中", selected = filter == "live") { onPick("live") }
        Spacer(Modifier.width(8.dp))
        LiveFilterPill(text = "未开播", selected = filter == "offline") { onPick("offline") }
    }
}

@Composable
internal fun LiveFilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.primary else KuiklyTheme.canvas,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.ink,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun LiveRoomTile(room: LiveRoom, onTap: () -> Unit, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onTap() }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveCoverBlock(title = room.title, size = 56)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    room.title,
                    fontSize = 15.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${room.hostName} · ${room.category} · ${room.liveLabel}",
                    fontSize = 12.sp,
                    color = KuiklyTheme.muted,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(
                room.liveLabel,
                color = if (room.status == LiveStatus.live) KuiklyTheme.error else KuiklyTheme.muted,
            )
            Spacer(Modifier.width(6.dp))
            KMetricPill("在线", room.viewerLabel, accent = room.status == LiveStatus.live)
            Spacer(Modifier.width(6.dp))
            KMetricPill("点赞", "${room.likeCount}")
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onRefresh() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("刷新在线人数", fontSize = 12.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Text("›", fontSize = 15.sp, color = KuiklyTheme.muted)
        }
    }
}

@Composable
internal fun LiveCoverBlock(title: String, size: Int) {
    Box(
        modifier = Modifier
            .width(size.dp)
            .height(size.dp)
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (title.isEmpty()) "LIVE" else title.take(1),
            fontSize = (size / 3).sp,
            color = KuiklyTheme.ink,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun LiveEmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无直播间", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("切换筛选条件或 Mock 场景", fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.clickable { onRefresh() }
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("查看全部", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun LiveErrorState(message: String, onRetry: () -> Unit) {
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

@Composable
internal fun LiveMockCycleRow(scenario: MockScenario, onCycle: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clickable { onCycle() }.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("[CYCLE MOCK] $scenario", fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}
