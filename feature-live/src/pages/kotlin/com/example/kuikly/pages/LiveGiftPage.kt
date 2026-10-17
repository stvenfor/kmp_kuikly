package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.live.LiveGift
import com.example.kuikly.data.live.LiveGiftRecord
import com.example.kuikly.data.live.LiveRoom
import com.example.kuikly.data.live.LiveStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KMetricPill
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
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
 * 礼物面板：直播间互动的「送礼物」子流程（钻石余额扣减 + 房间礼物流水）。
 *
 * 源 `features/live` 暂无该能力（仅 Realtime 信令联调），此处按直播间通用业务补齐：
 * 余额不足 / 礼物不存在 / 房间不存在均为可测的失败路径。
 */
@Page("LiveGift")
internal class LiveGiftPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val roomId = pageData.params.optString("id")
        setContent {
            var room by remember { mutableStateOf<LiveRoom?>(null) }
            var gifts by remember { mutableStateOf<List<LiveGift>>(emptyList()) }
            var feed by remember { mutableStateOf<List<LiveGiftRecord>>(emptyList()) }
            var balance by remember { mutableStateOf(0) }
            var error by remember { mutableStateOf<String?>(null) }
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenario = MockBackend.scenario
                LiveStore.repo.fetchById(roomId)
                    .onSuccess { r ->
                        room = r
                        error = null
                        balance = LiveStore.repo.coinBalance()
                        feed = LiveStore.repo.giftFeedOf(r.id)
                        gifts = LiveStore.repo.giftCatalog().getOrDefault(emptyList())
                    }
                    .onFailure {
                        room = null
                        error = it.message ?: "直播间不存在"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                LiveGiftHeader(onBack = { Utils.currentBridgeModule().closePage() })
                val current = room
                when {
                    roomId.isBlank() || current == null ->
                        LiveErrorState(message = error ?: "直播间不存在", onRetry = ::reload)
                    else -> LiveGiftBody(
                        room = current,
                        gifts = gifts,
                        feed = feed,
                        balance = balance,
                        scenario = scenario,
                        onCycle = { scenario = MockBackend.cycle(); reload() },
                        onSend = { gift ->
                            LiveStore.repo.sendGift(current.id, gift.id)
                                .onSuccess { record ->
                                    Utils.currentBridgeModule().toast("已送出 ${record.giftName}")
                                    reload()
                                }
                                .onFailure { ex ->
                                    Utils.currentBridgeModule().toast(ex.message ?: "送礼失败")
                                }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveGiftHeader(onBack: () -> Unit) {
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
        Text("赠送礼物", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LiveGiftBody(
    room: LiveRoom,
    gifts: List<LiveGift>,
    feed: List<LiveGiftRecord>,
    balance: Int,
    scenario: MockScenario,
    onCycle: () -> Unit,
    onSend: (LiveGift) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        item {
            LiveGiftRoomCard(room = room, balance = balance)
            Spacer(Modifier.height(10.dp))
        }
        if (gifts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("暂无可用礼物", fontSize = 13.sp, color = KuiklyTheme.muted)
                }
                Spacer(Modifier.height(10.dp))
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(12.dp),
                ) {
                    Text("礼物面板", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
                    Text("点击赠送并扣除钻石", fontSize = 12.sp, color = KuiklyTheme.muted)
                    Spacer(Modifier.height(10.dp))
                    gifts.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            row.forEach { gift ->
                                LiveGiftTile(gift = gift, affordable = balance >= gift.coin) {
                                    onSend(gift)
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        item {
            Text("本场送礼 ${feed.size} 次", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        if (feed.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("本场还没有人送礼", fontSize = 13.sp, color = KuiklyTheme.muted)
                    Spacer(Modifier.height(4.dp))
                    Text("送一个小礼物给主播打个招呼", fontSize = 12.sp, color = KuiklyTheme.muted)
                }
                Spacer(Modifier.height(10.dp))
            }
        } else {
            items(feed, key = { it.id }) { record ->
                LiveGiftRecordRow(record = record)
                Spacer(Modifier.height(6.dp))
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            LiveMockCycleRow(scenario = scenario, onCycle = onCycle)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LiveGiftRoomCard(room: LiveRoom, balance: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Text(room.title, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("主播 ${room.hostName}", fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(room.liveLabel)
            Spacer(Modifier.width(6.dp))
            KMetricPill("礼物", "${room.giftCount}", accent = true)
            Spacer(Modifier.width(6.dp))
            KMetricPill("钻石", "$balance")
        }
    }
}

@Composable
private fun LiveGiftTile(gift: LiveGift, affordable: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .background(
                if (affordable) KuiklyTheme.canvasSoft2 else KuiklyTheme.hairline,
                RoundedCornerShape(KuiklyTheme.radiusSm),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(gift.glyph, fontSize = 20.sp, color = if (affordable) KuiklyTheme.highlightPink else KuiklyTheme.muted)
        Spacer(Modifier.height(4.dp))
        Text(gift.name, fontSize = 12.sp, color = if (affordable) KuiklyTheme.ink else KuiklyTheme.muted)
        Spacer(Modifier.height(2.dp))
        Text("${gift.coin} 钻", fontSize = 11.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun LiveGiftRecordRow(record: LiveGiftRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusSm))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("${record.user} 送出 ${record.giftName}", fontSize = 13.sp, color = KuiklyTheme.ink)
        Text("${record.coin} 钻", fontSize = 12.sp, color = KuiklyTheme.highlightPink, fontWeight = FontWeight.Bold)
    }
}
