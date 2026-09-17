package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.FriendStore
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
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
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 好友资料（Phase-1 路由，P2-W2c 按 Flutter 共享 AppTheme/AppNavBar 令牌重定型）。
 *
 * ponytail 天花板：Flutter `features/friend` **无资料页真源**（模块整体是占位桩，见
 * [FriendListPage]），故保留 Kuikly Phase-1 的 mock 字段，仅按 `AppTheme` 分组卡形制加厚
 * （头像 72 + 名字 20·w700 + 简介卡行 15/15 + 0.5 hairline）。
 */
@Page(name = "FriendDetail", moduleId = "feature_home")
internal class FriendDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "1" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val result = remember(id) { FriendStore.repo.detail(id) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "好友资料",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                val friend = result.getOrNull()
                if (friend == null) {
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = (bottom + 24f).dp,
                        ),
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppChrome.surface, RoundedCornerShape(12.dp))
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(AppChrome.accent.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        friend.name.take(1),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppChrome.accent,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    friend.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppChrome.labelPrimary,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    friend.bio,
                                    fontSize = 13.sp,
                                    color = AppChrome.labelSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppChrome.surface, RoundedCornerShape(12.dp)),
                            ) {
                                InfoRow("用户名", friend.name)
                                Hairline()
                                InfoRow("个人简介", friend.bio)
                                Hairline()
                                InfoRow("编号", friend.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 15.sp, color = AppChrome.labelPrimary)
        Spacer(Modifier.width(16.dp))
        Text(
            value,
            fontSize = 15.sp,
            color = AppChrome.labelSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Flutter grouped 卡内的 0.5 hairline 分隔。 */
@Composable
private fun Hairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(AppChrome.HAIRLINE.dp)
            .background(AppChrome.separator.copy(alpha = 0.5f)),
    )
}
