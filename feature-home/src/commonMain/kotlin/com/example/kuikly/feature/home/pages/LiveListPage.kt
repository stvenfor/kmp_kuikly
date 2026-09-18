package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.live.LiveRoom
import com.example.kuikly.data.live.LiveStore
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * Live 域私有色板（与 V2b UsedCarListPage、V3a UsedCarDetailPage、V3d VideoListPage 同款
 * 命名约定，避免与共享 `AppChrome` 混淆）。Flutter `LivePage` 是调试桩无卡形真源，本令牌
 * 与 `AppTheme` 的分组卡同语义（black 4% / blur 8 / offset 0,2）。
 */
private object LivePalette {
    val cardShadow = Color(0x0A000000)
}

/**
 * 直播列表 — 按 Flutter `LivePage` chrome + 共享 AppTheme 令牌加厚（P2-W2c）。
 *
 * ponytail 天花板：Flutter `features/live` 是**调试桩**（`LivePage` = `AppNavBar('直播')` +
 * 居中 `FilledButton('进入 Mock 直播房')`，`LiveRoomPage` = WS 状态行 + 信令按钮 + 信令列表），
 * **无房间列表视觉真源**。故对齐其 chrome/文案（`AppNavBar` 56 / `#F2F2F7` / accent FilledButton
 * 44·r12 / '直播' / '进入 Mock 直播房'），并保留 Kuikly Phase-1 mock 房间列表以便进入房间详情。
 * 刻度：Flutter live 模块 grep 无 `.w/.h/.sp` → 裸 `dp`/`sp`。
 */
@Page(name = "LiveList", moduleId = "feature_home")
internal class LiveListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var rooms by remember { mutableStateOf<List<LiveRoom>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun load() {
                LiveStore.repo.list()
                    .onSuccess {
                        rooms = it
                        error = null
                    }
                    .onFailure {
                        rooms = emptyList()
                        error = it.message
                    }
            }

            if (!booted) {
                booted = true
                load()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "直播",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                // Flutter `body: Center(child: FilledButton('进入 Mock 直播房'))`
                // （AppTheme：accent 底 / 白字 / 最小 64×44 / r12 / 14·w500）。
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PrimaryAction(label = "进入 Mock 直播房") {
                        val target = rooms.firstOrNull()?.id ?: "1"
                        Utils.currentBridgeModule().openPage(
                            PageNames.LiveDetail,
                            userData = JSONObject().apply { put("id", target) },
                        )
                    }
                }
                Text(
                    "Mock 房间",
                    fontSize = 13.sp,
                    color = AppChrome.labelSecondary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                )
                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "加载失败",
                            message = error ?: "加载失败",
                            actionLabel = "重试",
                            onAction = { load() },
                        )
                    }
                    rooms.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "📡",
                            title = "暂无直播",
                            message = "点击刷新重新加载",
                            actionLabel = "刷新",
                            onAction = { load() },
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 0.dp,
                            end = 16.dp,
                            bottom = (bottom + 24f).dp,
                        ),
                        // Flutter `separatorBuilder: SizedBox(height: 10)` — 仅条目之间，末条后无间隔。
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(rooms, key = { it.id }) { room ->
                            LiveRoomRow(
                                room = room,
                                onTap = {
                                    Utils.currentBridgeModule().openPage(
                                        PageNames.LiveDetail,
                                        userData = JSONObject().apply { put("id", room.id) },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Flutter `FilledButton`（AppTheme accent / 白字 / r12 / 44 高）等价物。
 * 内容内距对齐 M3 默认 `padding1x = 24`（flutter/.../filled_button.dart:485，
 * P3-E4b 结构对齐；LiveDetailPage 复用本组件同步受益）。
 */
@Composable
internal fun PrimaryAction(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .background(AppChrome.accent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

/** 房间卡（Phase-1 mock 内容按 AppTheme grouped 卡形制重排）。 */
@Composable
private fun LiveRoomRow(room: LiveRoom, onTap: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 白卡 r12 + black 4% / blur 8 / offset(0,2) 阴影（与 V2b UsedCarListPage.kt、V3a
            // UsedCarDetailPage、V3d VideoListPage 同款令牌，让分组卡在 #F2F2F7 页底「浮」起来）。
            .shadow(8.dp, shape, ambientColor = LivePalette.cardShadow, spotColor = LivePalette.cardShadow)
            .background(AppChrome.surface, shape)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(AppChrome.accent.copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = AppChrome.accent)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                room.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppChrome.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "主播 ${room.host} · ${room.viewerCount} 人观看",
                fontSize = 13.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("›", fontSize = 18.sp, color = AppChrome.separator)
    }
}
