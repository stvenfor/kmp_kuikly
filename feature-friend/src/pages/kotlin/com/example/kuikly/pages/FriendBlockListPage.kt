package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.Friend
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
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 黑名单（main 侧扩展）。
 *
 * 拉黑的人从联系人分组/首字母聚合里剔除（[Friend.blocked]），只在本页可见，
 * 移出后回到默认分组继续参与聚合。
 */
@Page("FriendBlockList")
internal class FriendBlockListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var blocked by remember { mutableStateOf<List<Friend>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                FriendStore.repo.blockedFriends()
                    .onSuccess {
                        blocked = it
                        error = null
                    }
                    .onFailure {
                        blocked = emptyList()
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "黑名单",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        top = 14.dp,
                        end = 14.dp,
                        bottom = (bottom + 24f).dp,
                    ),
                ) {
                    if (error != null) {
                        item {
                            FriendNoteBar(error ?: "加载失败")
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    item { FriendSectionTitle("已拉黑", "${blocked.size} 人") }

                    if (blocked.isEmpty()) {
                        item {
                            FriendEmptyBlock(
                                title = "黑名单是空的",
                                message = "在好友设置里可以把对方加入黑名单",
                            )
                        }
                    } else {
                        items(blocked) { friend ->
                            FriendBlockedRow(
                                friend = friend,
                                onUnblock = {
                                    FriendStore.repo.setBlocked(friend.id, false)
                                        .onSuccess {
                                            Utils.currentBridgeModule().toast("已把 ${it.displayName} 移出黑名单")
                                            reload()
                                        }
                                        .onFailure { e -> error = e.message ?: "操作失败" }
                                },
                                onOpen = {
                                    Utils.currentBridgeModule().openPage(
                                        url = "FriendRemark",
                                        userData = JSONObject().apply { put("id", friend.id) },
                                    )
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendBlockedRow(
    friend: Friend,
    onUnblock: () -> Unit,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FriendAvatar(friend.displayName)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                friend.displayName,
                fontSize = 15.sp,
                color = AppChrome.labelPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${friend.subtitle} · 不可发起单聊",
                fontSize = 11.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FriendPillButton(label = "移出", filled = false, onClick = onUnblock)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.clickable { onOpen() },
            contentAlignment = Alignment.Center,
        ) {
            Text("详情", fontSize = 12.sp, color = AppChrome.accent)
        }
    }
}
