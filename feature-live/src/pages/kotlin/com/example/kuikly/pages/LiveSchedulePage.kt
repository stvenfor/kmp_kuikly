package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.live.LivePhase
import com.example.kuikly.data.live.LiveSession
import com.example.kuikly.data.live.LiveSessionStore
import com.example.kuikly.data.mock.MockBackend
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
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 直播日程：把源 `LivePage` 只有一个「进入 Mock 直播房」入口的能力补齐为
 * 可直接触达的场次列表（预告预约 / 回放 / 直播中），与 [LivePage] 同风格。
 *
 * 状态机见 [LivePhase]；预约只允许预告场次，回放场次直接回到房间。
 */
@Page("LiveSchedule")
internal class LiveSchedulePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val startTab = pageData.params.optString("tab").ifBlank { "upcoming" }
        setContent {
            var tab by remember { mutableStateOf(startTab) }
            var sessions by remember { mutableStateOf<List<LiveSession>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenario = MockBackend.scenario
                val result = when (tab) {
                    "replay" -> LiveSessionStore.repo.replaySessions()
                    "reserved" -> LiveSessionStore.repo.reservedSessions()
                    else -> LiveSessionStore.repo.upcomingSessions()
                }
                result
                    .onSuccess { list ->
                        sessions = list
                        empty = list.isEmpty()
                        error = null
                    }
                    .onFailure {
                        sessions = emptyList()
                        empty = false
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                LiveScheduleHeader(onBack = { Utils.currentBridgeModule().closePage() })
                LiveScheduleTabRow(tab = tab) { tab = it; reload() }
                when {
                    error != null && sessions.isEmpty() ->
                        LiveErrorState(message = error ?: "加载失败", onRetry = ::reload)
                    empty -> LiveEmptyState(onRefresh = { tab = "upcoming"; reload() })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            LiveSessionTile(
                                session = session,
                                onReserve = {
                                    LiveSessionStore.repo.toggleReserve(session.id)
                                        .onSuccess { updated ->
                                            Utils.currentBridgeModule().toast(
                                                if (updated.reserved) "已预约开播提醒" else "已取消预约",
                                            )
                                        }
                                        .onFailure { ex ->
                                            Utils.currentBridgeModule().toast(ex.message ?: "预约失败")
                                        }
                                    reload()
                                },
                                onEnter = {
                                    val data = JSONObject().apply { put("id", session.roomId) }
                                    Utils.currentBridgeModule().openPage(url = "LiveRoom", userData = data)
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
private fun LiveScheduleHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹ 返回",
            fontSize = 16.sp,
            color = KuiklyTheme.link,
            modifier = Modifier.clickable { onBack() },
        )
        Spacer(Modifier.width(12.dp))
        Text("直播日程", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LiveScheduleTabRow(tab: String, onPick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiveFilterPill(text = "预告", selected = tab == "upcoming") { onPick("upcoming") }
        Spacer(Modifier.width(8.dp))
        LiveFilterPill(text = "回放", selected = tab == "replay") { onPick("replay") }
        Spacer(Modifier.width(8.dp))
        LiveFilterPill(text = "我的预约", selected = tab == "reserved") { onPick("reserved") }
    }
}

@Composable
private fun LiveSessionTile(session: LiveSession, onReserve: () -> Unit, onEnter: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveCoverBlock(title = session.title, size = 44)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    session.title,
                    fontSize = 15.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${session.hostName} · ${session.scheduleLabel}",
                    fontSize = 12.sp,
                    color = KuiklyTheme.muted,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(session.phase.label, color = LivePhaseChipColor(session.phase))
            Spacer(Modifier.width(6.dp))
            when (session.phase) {
                LivePhase.upcoming -> KMetricPill("预约", "${session.reserveCount}", accent = session.reserved)
                LivePhase.replay -> KMetricPill("观看", session.viewerLabel)
                LivePhase.live -> KMetricPill("在线", session.viewerLabel, accent = true)
            }
        }
        if (session.peakLabel.isNotEmpty() || session.durationLabel.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                listOf(session.durationLabel, session.peakLabel).filter { it.isNotEmpty() }.joinToString(" · "),
                fontSize = 12.sp,
                color = KuiklyTheme.muted,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val actionLabel = when {
                session.phase == LivePhase.upcoming -> if (session.reserved) "已预约 · 取消" else "预约"
                session.phase == LivePhase.replay -> "看回放"
                else -> "进入直播间"
            }
            LiveScheduleAction(
                text = actionLabel,
                accent = session.phase == LivePhase.upcoming && !session.reserved,
                onClick = if (session.phase == LivePhase.upcoming) onReserve else onEnter,
            )
        }
    }
}

@Composable
private fun LiveScheduleAction(text: String, accent: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (accent) KuiklyTheme.primary else KuiklyTheme.canvasSoft2,
                RoundedCornerShape(KuiklyTheme.radiusSm),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = if (accent) KuiklyTheme.onPrimary else KuiklyTheme.ink,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun LivePhaseChipColor(phase: LivePhase): Color =
    when (phase) {
        LivePhase.live -> KuiklyTheme.error
        LivePhase.upcoming -> KuiklyTheme.link
        LivePhase.replay -> KuiklyTheme.muted
    }
