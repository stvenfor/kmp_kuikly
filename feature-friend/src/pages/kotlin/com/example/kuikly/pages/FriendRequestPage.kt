package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.FriendRequest
import com.example.kuikly.data.friend.FriendRequestRepository
import com.example.kuikly.data.friend.FriendRequestStore
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

/**
 * 新的朋友（port of source `friend_page.dart` 的「新的朋友」分区 + `_respond`）。
 *
 * 源端 `_IncomingTile` 给每条待处理申请配「拒绝 / 同意」两个胶囊，
 * 同意后 `respondFriend(accept: true)` 并刷新好友列表。
 */
@Page("FriendRequest")
internal class FriendRequestPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var incoming by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
            var history by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                FriendRequestStore.repo.incoming()
                    .onSuccess { incoming = it }
                    .onFailure {
                        incoming = emptyList()
                        error = FriendRequestRepository.friendlyError(it, "加载失败")
                    }
                FriendRequestStore.repo.history()
                    .onSuccess { history = it }
                    .onFailure { history = emptyList() }
            }

            if (!booted) {
                booted = true
                reload()
            }

            fun respond(request: FriendRequest, accept: Boolean) {
                FriendRequestStore.repo.respond(request.id, accept)
                    .onSuccess {
                        val toast = if (accept) "已添加 ${request.displayName}" else "已拒绝"
                        Utils.currentBridgeModule().toast(toast)
                        error = null
                        reload()
                    }
                    .onFailure { e ->
                        Utils.currentBridgeModule().toast(
                            FriendRequestRepository.friendlyError(e, "处理失败"),
                        )
                    }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "新的朋友",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    actions = {
                        Text(
                            "${incoming.size} 条待处理",
                            fontSize = 13.sp,
                            color = AppChrome.accent,
                            fontWeight = FontWeight.Medium,
                        )
                    },
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

                    item { FriendSectionTitle("待处理申请", "${incoming.size} 条") }

                    if (incoming.isEmpty()) {
                        item {
                            FriendEmptyBlock(
                                title = "没有新的好友请求",
                                message = "对方同意后会出现在这里",
                            )
                        }
                    } else {
                        items(incoming) { request ->
                            FriendIncomingRow(
                                request = request,
                                onAccept = { respond(request, true) },
                                onReject = { respond(request, false) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    if (history.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            FriendSectionTitle("申请记录", "${history.size} 条")
                        }
                        items(history) { request ->
                            FriendHistoryRow(request = request)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/** 待处理申请行：拒绝（次级）+ 同意（主操作）。 */
@Composable
private fun FriendIncomingRow(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FriendAvatar(request.displayName)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                request.displayName,
                fontSize = 15.sp,
                color = AppChrome.labelPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                request.subtitle,
                fontSize = 11.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FriendPillButton(label = "拒绝", filled = false, onClick = onReject)
        Spacer(Modifier.width(8.dp))
        FriendPillButton(label = "同意", filled = true, onClick = onAccept)
    }
}

@Composable
private fun FriendHistoryRow(request: FriendRequest) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FriendAvatar(request.displayName)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(request.displayName, fontSize = 15.sp, color = AppChrome.labelPrimary)
            Spacer(Modifier.height(2.dp))
            Text(
                "${request.createdAt} · ${request.statusLabel}",
                fontSize = 11.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(contentAlignment = Alignment.Center) {
            Text(
                request.statusLabel,
                fontSize = 12.sp,
                color = if (request.statusLabel == "已同意") AppChrome.accent else AppChrome.labelSecondary,
            )
        }
    }
}
