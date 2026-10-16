package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.live.DanmakuMessage
import com.example.kuikly.data.live.LiveRoom
import com.example.kuikly.data.live.LiveStatus
import com.example.kuikly.data.live.LiveStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KMetricPill
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

private val LIVE_QUICK_DANMAKU = listOf("666", "主播加油", "已下单", "前排围观", "讲得真好")

@Page("LiveRoom")
internal class LiveRoomPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        setContent {
            var room by remember { mutableStateOf<LiveRoom?>(null) }
            var danmaku by remember { mutableStateOf<List<DanmakuMessage>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var toastText by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                LiveStore.repo.fetchById(id)
                    .onSuccess { r ->
                        room = r
                        error = null
                        danmaku = LiveStore.repo.danmakuOf(r.id).getOrDefault(emptyList())
                    }
                    .onFailure {
                        room = null
                        error = it.message ?: "直播间加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("直播间 $id", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                val current = room
                when {
                    id.isBlank() || current == null ->
                        LiveErrorState(message = error ?: "直播间不存在", onRetry = ::reload)
                    else -> LiveRoomBody(
                        room = current,
                        danmaku = danmaku,
                        toastText = toastText,
                        onRefreshViewers = {
                            LiveStore.repo.refreshViewers(current.id).onSuccess { updated -> room = updated }
                        },
                        onSend = { text ->
                            LiveStore.repo.sendDanmaku(current.id, text)
                                .onSuccess {
                                    danmaku = LiveStore.repo.danmakuOf(current.id).getOrDefault(emptyList())
                                    Utils.currentBridgeModule().toast("弹幕已发送")
                                    toastText = "弹幕已发送：$text"
                                }
                                .onFailure { ex ->
                                    Utils.currentBridgeModule().toast("发送失败：${ex.message}")
                                    toastText = "发送失败：${ex.message}"
                                }
                        },
                        onFollow = { Utils.currentBridgeModule().toast("已关注主播") },
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveRoomBody(
    room: LiveRoom,
    danmaku: List<DanmakuMessage>,
    toastText: String?,
    onRefreshViewers: () -> Unit,
    onSend: (String) -> Unit,
    onFollow: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        item {
            LivePlayerShell(room = room)
            Spacer(Modifier.height(10.dp))
        }
        item {
            LiveRoomInfoCard(room = room, onRefreshViewers = onRefreshViewers, onFollow = onFollow)
            Spacer(Modifier.height(10.dp))
        }
        item {
            LiveDanmakuSendCard(onSend = onSend)
            Spacer(Modifier.height(10.dp))
        }
        if (danmaku.isEmpty()) {
            item {
                KCard {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("暂无弹幕，发送一条试试", fontSize = 13.sp, color = KuiklyTheme.muted)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        } else {
            item {
                KCard {
                    Text("弹幕 ${danmaku.size} 条", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }
            items(danmaku, key = { it.id }) { msg ->
                LiveDanmakuRow(msg = msg)
                Spacer(Modifier.height(6.dp))
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            if (toastText != null) {
                Text("提示：$toastText", fontSize = 12.sp, color = KuiklyTheme.muted)
                Spacer(Modifier.height(8.dp))
            }
            LiveMockCycleRow(scenario = MockBackend.scenario) { MockBackend.cycle() }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LivePlayerShell(room: LiveRoom) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(210.dp)
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusLg)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("▶", fontSize = 32.sp, color = KuiklyTheme.onPrimary)
            Spacer(Modifier.height(8.dp))
            Text(room.title, fontSize = 14.sp, color = KuiklyTheme.onPrimary, maxLines = 1)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (room.status == LiveStatus.live) "● LIVE" else "○ 未开播",
                    fontSize = 12.sp,
                    color = if (room.status == LiveStatus.live) KuiklyTheme.error else KuiklyTheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(10.dp))
                Text("在线 ${room.viewerLabel}", fontSize = 12.sp, color = KuiklyTheme.onPrimary)
            }
            Spacer(Modifier.height(6.dp))
            Text("播放器壳占位 · 未接入真实播放能力", fontSize = 10.sp, color = KuiklyTheme.onPrimary)
        }
    }
}

@Composable
private fun LiveRoomInfoCard(room: LiveRoom, onRefreshViewers: () -> Unit, onFollow: () -> Unit) {
    KCard {
        Text(room.title, fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("主播 ${room.hostName} · ${room.category}", fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(room.liveLabel, color = if (room.status == LiveStatus.live) KuiklyTheme.error else KuiklyTheme.muted)
            Spacer(Modifier.width(6.dp))
            room.tags.forEach { tag ->
                KChip(tag, color = KuiklyTheme.violet)
                Spacer(Modifier.width(6.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KMetricPill("在线", room.viewerLabel, accent = true)
            Spacer(Modifier.width(6.dp))
            KMetricPill("点赞", "${room.likeCount}")
        }
        Spacer(Modifier.height(10.dp))
        Text(room.announcement, fontSize = 13.sp, color = KuiklyTheme.body)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveActionButton(text = "刷新在线人数", onClick = onRefreshViewers)
            Spacer(Modifier.width(8.dp))
            LiveActionButton(text = "关注主播", onClick = onFollow)
        }
    }
}

@Composable
private fun LiveActionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 12.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LiveDanmakuSendCard(onSend: (String) -> Unit) {
    KCard {
        Text("快捷弹幕（工程内无 TextField，使用候选短语）", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            LIVE_QUICK_DANMAKU.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEach { phrase ->
                        Box(
                            modifier = Modifier
                                .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusPill))
                                .clickable { onSend(phrase) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(phrase, fontSize = 12.sp, color = KuiklyTheme.link)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveDanmakuRow(msg: DanmakuMessage) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusSm))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("#${msg.seq}", fontSize = 11.sp, color = KuiklyTheme.muted, modifier = Modifier.width(40.dp))
        Text(msg.user, fontSize = 12.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(8.dp))
        Text(msg.text, fontSize = 13.sp, color = KuiklyTheme.ink)
    }
}
