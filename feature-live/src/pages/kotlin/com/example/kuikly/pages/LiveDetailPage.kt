package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.live.LiveConnectionState
import com.example.kuikly.data.live.LiveRealtimeStore
import com.example.kuikly.data.live.LiveSignal
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

/**
 * 直播间 — Flutter `LiveRoomPage` stub 1:1（P7-E1 flatten for MAP）。
 *
 * 真源：`features/live/lib/live/view/live_room_page.dart`
 * （`AppNavBar('直播 $roomId')` + `Padding(12)` WS 行 + FilledButton「发送 join 信令」
 * + `Divider` + dense 信令 `ListTile` 列表，空态 `暂无信令`）。
 *
 * 本轮把源 `LiveRoomController` 的 Realtime 语义落到 [LiveRealtimeStore.repo]：
 * 连接态 label（源 `RealtimeConnectionStateX.label`）、`liveSignal` / `liveRoomState`
 * 两个 topic 的订阅与退订、`setKeepAliveInBackground` 后台保活、信令上限 30 条。
 * 页面无 WS 客户端，连接态由仓库在本地按 Connect / SaveAlive / Leave 动作推进。
 */
@Page("LiveDetail")
internal class LiveDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        // Flutter `LivePage` 以 `Get.toNamed(RoutePath.liveRoom, arguments: 'mock_room_001')` 进入。
        val roomId = pageData.params.optString("id").ifBlank { "mock_room_001" }
        val realtime = LiveRealtimeStore.repo
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var signals by remember { mutableStateOf<List<LiveSignal>>(emptyList()) }
            var topics by remember { mutableStateOf<List<String>>(emptyList()) }
            var connection by remember { mutableStateOf(LiveConnectionState.disconnected) }
            var keepAlive by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            fun sync() {
                connection = realtime.connection
                keepAlive = realtime.keepAliveInBackground
                topics = realtime.subscribedTopics
                signals = realtime.signalsOf(roomId)
            }

            // 源 `LiveRoomController.onInit`：保活 + 订阅 + 建连。
            if (!booted) {
                booted = true
                realtime.enterRoom(roomId)
                sync()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "直播 $roomId",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                // Flutter body Column — not LazyColumn wrapper for chrome; list is Expanded.
                Text(
                    "WS: ${connection.label} · paused 保持连接",
                    fontSize = 14.sp,
                    color = AppChrome.labelSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("主题", fontSize = 11.sp, color = AppChrome.labelSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (topics.isEmpty()) "未订阅" else topics.joinToString(" / "),
                        fontSize = 11.sp,
                        color = AppChrome.accent,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)) {
                    PrimaryAction(label = "发送 join 信令") {
                        realtime.sendJoinSignal(roomId)
                            .onSuccess { Utils.currentBridgeModule().toast("live.join 已送达") }
                            .onFailure { ex ->
                                Utils.currentBridgeModule().toast(ex.message ?: "信令发送失败")
                            }
                        sync()
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    LiveSignalAction(label = "重新连接") {
                        realtime.enterRoom(roomId)
                        sync()
                    }
                    Spacer(Modifier.width(8.dp))
                    LiveSignalAction(label = "拉取房间状态") {
                        realtime.receiveRoomState(roomId)
                            .onFailure { ex -> Utils.currentBridgeModule().toast(ex.message ?: "拉取失败") }
                        sync()
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    LiveSignalAction(label = if (keepAlive) "保活 开" else "保活 关") {
                        realtime.setKeepAliveInBackground(!keepAlive)
                        sync()
                    }
                    Spacer(Modifier.width(8.dp))
                    LiveSignalAction(label = "断开退订") {
                        realtime.leaveRoom(roomId)
                        sync()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppChrome.hairline),
                )
                if (signals.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "暂无信令",
                            fontSize = 14.sp,
                            color = AppChrome.labelSecondary,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(signals, key = { it.id }) { signal ->
                            Text(
                                signal.line,
                                fontSize = 12.sp,
                                color = AppChrome.labelPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        item {
                            Spacer(Modifier.height((bottom + 24f).dp))
                        }
                    }
                }
            }
        }
    }
}

/** 次级操作按钮（蓝字白底，对应源 `TextButton` 口径）。 */
@Composable
private fun LiveSignalAction(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 12.sp, color = AppChrome.accent, fontWeight = FontWeight.Medium)
    }
}
