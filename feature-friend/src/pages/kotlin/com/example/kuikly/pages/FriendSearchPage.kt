package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.FriendCandidate
import com.example.kuikly.data.friend.FriendRequest
import com.example.kuikly.data.friend.FriendRequestRepository
import com.example.kuikly.data.friend.FriendRequestStore
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

// 搜索候选词：姓氏 / 拼音 / 手机号段 / 用户号段，覆盖源端"手机号或用户 ID"两种检索口径。
private val FRIEND_SEARCH_WORDS = listOf(
    "张", "李", "韩", "妈妈", "周", "吴", "郑", "孙",
    "zhang", "lilei", "han", "zhou", "138", "135", "133", "u10",
)

/**
 * 添加好友（port of source `friend_page.dart` 的搜索条 + 搜索结果 + 发送申请）。
 *
 * 源端 `_searchCtrl` 走 `ImFriendApi.search`，命中后用 `_PillButton('加好友')` 调
 * `requestFriend`；失败文案统一过 `ImFriendApi.friendlyError`。
 */
@Page("FriendSearch")
internal class FriendSearchPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var query by remember { mutableStateOf("") }
            var hits by remember { mutableStateOf<List<FriendCandidate>>(emptyList()) }
            var sent by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
            var friendIds by remember { mutableStateOf<Set<String>>(emptySet()) }
            var searched by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun refreshSent() {
                FriendRequestStore.repo.outgoingRequests()
                    .onSuccess { sent = it }
                    .onFailure { sent = emptyList() }
                friendIds = FriendStore.repo.friendIds()
            }

            fun runSearch() {
                searched = true
                error = null
                FriendStore.repo.searchUsers(query)
                    .onSuccess { hits = it }
                    .onFailure {
                        hits = emptyList()
                        error = FriendRequestRepository.friendlyError(it, "搜索失败")
                    }
                refreshSent()
            }

            if (!booted) {
                booted = true
                refreshSent()
            }

            val pendingIds = sent.map { it.fromUserId }.toSet()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "添加好友",
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
                    item {
                        FriendTextInput(
                            label = "手机号或用户 ID",
                            value = query,
                            hint = "点下方词条拼接关键词",
                            candidates = FRIEND_SEARCH_WORDS,
                            onAppend = { query += it },
                            onBackspace = { if (query.isNotEmpty()) query = query.dropLast(1) },
                            onClear = {
                                query = ""
                                hits = emptyList()
                                searched = false
                                error = null
                            },
                        )
                        Spacer(Modifier.height(10.dp))
                        FriendPrimaryButton(
                            text = "搜索",
                            enabled = query.isNotBlank(),
                            onClick = { runSearch() },
                        )
                    }

                    if (error != null) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            FriendNoteBar(error ?: "搜索失败")
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        FriendSectionTitle("搜索结果", if (searched) "${hits.size} 人" else "")
                    }

                    if (!searched) {
                        item {
                            FriendEmptyBlock(
                                title = "搜索并添加好友",
                                message = "输入对方手机号或用户 ID，发送申请后对方在「新的朋友」里同意即可聊天",
                            )
                        }
                    } else if (hits.isEmpty()) {
                        item {
                            FriendEmptyBlock(
                                title = "没有找到匹配的用户",
                                message = "换个手机号或用户 ID 试试",
                            )
                        }
                    } else {
                        items(hits) { candidate ->
                            FriendSearchHitRow(
                                candidate = candidate,
                                pending = pendingIds.contains(candidate.id),
                                isFriend = candidate.isFriend || friendIds.contains(candidate.id),
                                onAdd = {
                                    FriendRequestStore.repo
                                        .requestFriend(candidate.id, candidate.name, candidate.phoneMasked)
                                        .onSuccess {
                                            Utils.currentBridgeModule().toast("已向 ${candidate.name} 发送好友申请")
                                            refreshSent()
                                        }
                                        .onFailure { e ->
                                            Utils.currentBridgeModule().toast(
                                                FriendRequestRepository.friendlyError(e, "申请失败"),
                                            )
                                        }
                                },
                                onOpen = {
                                    Utils.currentBridgeModule().openPage(
                                        url = "FriendDetail",
                                        userData = JSONObject().apply { put("id", candidate.id) },
                                    )
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    if (sent.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            FriendSectionTitle("已发出的申请", "${sent.size} 条")
                        }
                        items(sent) { request ->
                            FriendSentRow(request = request)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/** 搜索命中行：右侧按钮在「加好友 / 已发送 / 已是好友」三态间切换。 */
@Composable
private fun FriendSearchHitRow(
    candidate: FriendCandidate,
    pending: Boolean,
    isFriend: Boolean,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .clickable { if (isFriend) onOpen() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FriendAvatar(candidate.name)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                candidate.name,
                fontSize = 15.sp,
                color = AppChrome.labelPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (candidate.desc.isEmpty()) candidate.subtitle else candidate.desc,
                fontSize = 11.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isFriend) {
            FriendPillButton(label = "已是好友", filled = false, enabled = false, onClick = { })
        } else if (pending) {
            FriendPillButton(label = "已发送", filled = false, enabled = false, onClick = { })
        } else {
            FriendPillButton(label = "加好友", filled = true, onClick = onAdd)
        }
    }
}

@Composable
private fun FriendSentRow(request: FriendRequest) {
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
                "${request.subtitle} · ${request.createdAt}",
                fontSize = 11.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(contentAlignment = Alignment.Center) {
            Text("等待同意", fontSize = 12.sp, color = AppChrome.accent)
        }
    }
}
