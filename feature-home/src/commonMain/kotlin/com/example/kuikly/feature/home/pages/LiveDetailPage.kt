package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 直播间 — Flutter `LiveRoomPage` stub 1:1（P7-E1 flatten for MAP）。
 *
 * 真源：`features/live/lib/live/view/live_room_page.dart`
 * （`AppNavBar('直播 $roomId')` + `Padding(12)` WS 行 + FilledButton「发送 join 信令」
 * + `Divider` + dense 信令 `ListTile` 列表）。
 *
 * Phase-1 封面/房间信息卡已移除（Flutter 桩无对应真源；同 E9g friend/live-list stub flatten）。
 * Kuikly 无 RealtimeClient → WS 恒「未连接」；点按本地追加信令行。
 */
@Page(name = "LiveDetail", moduleId = "feature_home")
internal class LiveDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        // Flutter Get.arguments default `'mock_room'`.
        val roomId = pageData.params.optString("id").ifBlank { "mock_room" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var signals by remember { mutableStateOf<List<String>>(emptyList()) }
            var seq by remember { mutableStateOf(0) }

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
                    "WS: 未连接 · paused 保持连接",
                    fontSize = 14.sp,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(12.dp),
                )
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    PrimaryAction(label = "发送 join 信令") {
                        seq += 1
                        val line = "[signal] live.join seq=$seq {\"roomId\":\"$roomId\"}"
                        signals = (listOf(line) + signals).take(30)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppChrome.hairline),
                )
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(signals) { line ->
                        Text(
                            line,
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
