package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.live.LiveStore
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 直播间私有色板（与 V4b `LiveListPage.LivePalette`、V2b UsedCarListPage、V3a UsedCarDetailPage、
 * V3d VideoListPage 同款命名约定，避免与共享 `AppChrome` 混淆）。
 *
 * ponytail 天花板：Flutter `LiveRoomPage` 无房间信息卡真源，本令牌仅在 Phase-1 mock 房间信息块上
 * 套用「白卡 + black 4% / blur 8 / offset 0,2 阴影」的 AppTheme grouped 卡语义，让直播详情页与
 * 同仓 V4b 直播列表页在 chrome 层次上对齐。
 */
private object LiveDetailPalette {
    val cardShadow = Color(0x0A000000)
}

/**
 * 直播间 — 按 Flutter `LiveRoomPage` 结构 + 共享 AppTheme 令牌加厚（P2-W2c / P2-V5b）。
 *
 * 真源：`features/live/lib/live/view/live_room_page.dart`（`AppNavBar('直播 $roomId')` +
 * `Padding(12)` WS 状态行 `'WS: ${label} · paused 保持连接'` + `Padding(horizontal 12)` FilledButton
 * `'发送 join 信令'` + `Divider` + dense `ListTile(fontSize 12)` 信令列表）。
 *
 * ponytail 天花板：Kuikly 无 RealtimeClient → 状态恒为「未连接」、信令由本地点按「发送 join 信令」
 * 追加（`[signal] live.join seq=N {...}`）；Phase-1 的封面/房间信息占位保留（Flutter 桩无视频区）。
 */
@Page(name = "LiveDetail", moduleId = "feature_home")
internal class LiveDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "1" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val result = remember(id) { LiveStore.repo.detail(id) }
            // Flutter `controller.signals`（最多保留 30 条，新的插在最前）。
            var signals by remember { mutableStateOf<List<String>>(emptyList()) }
            var seq by remember { mutableStateOf(0) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "直播 $id",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                val room = result.getOrNull()
                if (room == null) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "加载失败",
                            message = result.exceptionOrNull()?.message ?: "加载失败",
                            actionLabel = "返回",
                            onAction = { Utils.currentBridgeModule().closePage() },
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        item {
                            // Phase-1 mock 播放占位（Flutter LiveRoomPage 无视频区，保留以示房间身份）。
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(Color(0xFF1E1E1E)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("▶", fontSize = 40.sp, color = Color(0xFF8C8C8C))
                            }
                            // 房间信息卡（白卡 r12 + black 4% / blur 8 / offset(0,2) 阴影 + LIVE 字形徽章，
                            // 与 V4b `LiveListPage.LiveRoomRow` 同款令牌，让直播详情与直播列表在 chrome
                            // 层次上一致；Flutter 桩无对应真源——保留以承载房间身份）。
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            8.dp,
                                            RoundedCornerShape(12.dp),
                                            ambientColor = LiveDetailPalette.cardShadow,
                                            spotColor = LiveDetailPalette.cardShadow,
                                        )
                                        .background(AppChrome.surface, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp, 20.dp)
                                                .background(
                                                    AppChrome.accent.copy(alpha = 0.12f),
                                                    RoundedCornerShape(4.dp),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                "LIVE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppChrome.accent,
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "房间 ${room.id}",
                                            fontSize = 13.sp,
                                            color = AppChrome.labelSecondary,
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        room.title,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppChrome.labelPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "主播 ${room.host} · ${room.viewerCount} 人正在观看",
                                        fontSize = 13.sp,
                                        color = AppChrome.labelSecondary,
                                    )
                                }
                            }
                        }
                        item {
                            // Flutter `Padding(12)` 的 WS 状态行（label 取自
                            // `RealtimeConnectionState.label`，无客户端时 = '未连接'）。
                            Text(
                                "WS: 未连接 · paused 保持连接",
                                fontSize = 14.sp,
                                color = Color(0xFF616161),
                                modifier = Modifier.padding(12.dp),
                            )
                            // Flutter `Padding(horizontal: 12)` 的 FilledButton('发送 join 信令')
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                                PrimaryAction(label = "发送 join 信令") {
                                    seq += 1
                                    val line = "[signal] live.join seq=$seq {\"roomId\":\"${room.id}\"}"
                                    signals = (listOf(line) + signals).take(30)
                                }
                            }
                            // Flutter `Divider()` — 主题 `dividerColor` 8% alpha 灰（与 AppNavBar 底
                            // hairline 同语义，原 `AppChrome.separator` 实心灰过深，修复 V5b loudest 之一）。
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppChrome.hairline),
                            )
                        }
                        if (signals.isEmpty()) {
                            item {
                                Text(
                                    "暂无信令",
                                    fontSize = 12.sp,
                                    color = AppChrome.labelSecondary,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        } else {
                            items(signals) { line ->
                                // Flutter `ListTile(dense: true, title: Text(fontSize: 12))`.
                                Text(
                                    line,
                                    fontSize = 12.sp,
                                    color = AppChrome.labelPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
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
