package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.Friend
import com.example.kuikly.data.friend.FriendStore
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
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 好友列表（Phase-1 路由，P2-W2c 按 Flutter 共享 AppTheme/AppNavBar 令牌重定型）。
 *
 * ponytail 天花板：Flutter `features/friend/lib/friend/view/friend_page.dart` 是**占位桩**
 * （`AppPageScaffold(navBar: AppNavBar(title: '好友'), body: Center(Text('Friend 模块')))`），
 * **无列表/详情的视觉真源**。故本页保留 Kuikly Phase-1 mock 列表，仅对齐共享 chrome
 * （`AppNavBar` 几何 + `AppTheme` 的 `#F2F2F7` / 白卡 / `separator`）与文案「好友」。
 * 刻度：Flutter friend 模块 grep 无 `.w/.h/.sp` → 裸 `dp`/`sp`（不接 DesignScale）。
 */
@Page(name = "FriendList", moduleId = "feature_home")
internal class FriendListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var items by remember { mutableStateOf<List<Friend>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var loaded by remember { mutableStateOf(false) }

            fun load() {
                FriendStore.repo.list()
                    .onSuccess {
                        items = it
                        error = null
                    }
                    .onFailure {
                        items = emptyList()
                        error = it.message
                    }
            }

            if (!loaded) {
                loaded = true
                load()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                // Flutter `AppNavBar(title: '好友')`（`showBackButton: false`；Kuikly 该页由首页
                // 深链进入，保留返回箭头以维持 Phase-1 深链返回行为）。
                AppNavBarBar(
                    title = "好友",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
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
                    items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "👥",
                            title = "暂无好友",
                            message = "点击刷新重新加载",
                            actionLabel = "刷新",
                            onAction = { load() },
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // Flutter 列表卡间留白（`AppTheme.grouped` 形制下，单卡 r12 之间保留
                        // 视觉间距以露出每个卡片的圆角，而非堆叠成长方块）。首卡顶部 `top = 12.dp`。
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 12.dp,
                            end = 16.dp,
                            bottom = (bottom + 24f).dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            FriendRow(
                                friend = item,
                                onTap = {
                                    Utils.currentBridgeModule().openPage(
                                        PageNames.FriendDetail,
                                        userData = JSONObject().apply { put("id", item.id) },
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
 * 好友行 — Flutter grouped 卡片形制（`AppTheme`）：白卡 r12 + 44 头像 + 12 间隙 +
 * 名字 17·w600 + 2 + 简介 13·labelSecondary + › 分隔箭头（0.5 hairline 组内分隔）。
 */
@Composable
private fun FriendRow(friend: Friend, onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(AppChrome.accent.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                friend.name.take(1),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppChrome.accent,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                friend.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppChrome.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                friend.bio,
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
