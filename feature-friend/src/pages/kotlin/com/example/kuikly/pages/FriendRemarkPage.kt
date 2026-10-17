package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.FRIEND_REMARK_MAX
import com.example.kuikly.data.friend.FRIEND_TAG_MAX
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

private val FRIEND_REMARK_WORDS = listOf("老大", "老王", "同事", "大学室友", "健身搭子", "客户", "邻居", "球友")
private val FRIEND_TAG_WORDS = listOf("同事", "同学", "家人", "球友", "书友", "客户", "邻居", "重要")

/**
 * 好友备注 / 标签 / 拉黑 / 删除（main 侧扩展，落在一个好友设置页里）。
 *
 * 备注为空时对外回落昵称（[Friend.displayName]），与源端"备注为空显示昵称"一致。
 */
@Page("FriendRemark")
internal class FriendRemarkPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "f1" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var friend by remember { mutableStateOf<Friend?>(null) }
            var remarkDraft by remember { mutableStateOf("") }
            var pickedTags by remember { mutableStateOf<List<String>>(emptyList()) }
            var confirmDelete by remember { mutableStateOf(false) }
            var note by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                FriendStore.repo.detail(id)
                    .onSuccess {
                        friend = it
                        pickedTags = it.tags
                        if (remarkDraft.isBlank()) remarkDraft = it.remark
                    }
                    .onFailure {
                        friend = null
                        note = it.message ?: "好友不存在"
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
                    title = "好友设置",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                val current = friend
                if (current == null) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "好友不存在",
                            message = note ?: "好友不存在",
                            actionLabel = "返回",
                            onAction = { Utils.currentBridgeModule().closePage() },
                        )
                    }
                } else {
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
                            FriendRemarkHeader(friend = current)
                            if (note != null) {
                                Spacer(Modifier.height(10.dp))
                                FriendNoteBar(note ?: "操作失败")
                            }
                        }

                        item {
                            Spacer(Modifier.height(14.dp))
                            FriendTextInput(
                                label = "备注（最多 $FRIEND_REMARK_MAX 字，留空则显示昵称）",
                                value = remarkDraft,
                                hint = "点下方词条拼接到备注",
                                candidates = FRIEND_REMARK_WORDS,
                                onAppend = { remarkDraft += it },
                                onBackspace = { if (remarkDraft.isNotEmpty()) remarkDraft = remarkDraft.dropLast(1) },
                                onClear = { remarkDraft = "" },
                            )
                            Spacer(Modifier.height(10.dp))
                            FriendPrimaryButton(
                                text = "保存备注",
                                onClick = {
                                    FriendStore.repo.setRemark(current.id, remarkDraft)
                                        .onSuccess {
                                            note = null
                                            Utils.currentBridgeModule().toast(
                                                if (it.remark.isBlank()) "已清空备注" else "备注已更新为「${it.remark}」",
                                            )
                                            reload()
                                        }
                                        .onFailure { e -> note = e.message ?: "保存失败" }
                                },
                            )
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            FriendSectionTitle("标签", "${pickedTags.size} / $FRIEND_TAG_MAX")
                            Spacer(Modifier.height(8.dp))
                            FriendTagPicker(
                                words = FRIEND_TAG_WORDS,
                                picked = pickedTags,
                                onToggle = { word ->
                                    pickedTags = if (pickedTags.contains(word)) {
                                        pickedTags.filter { it != word }
                                    } else {
                                        pickedTags + word
                                    }
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                            FriendPrimaryButton(
                                text = "保存标签",
                                onClick = {
                                    FriendStore.repo.setTags(current.id, pickedTags)
                                        .onSuccess {
                                            note = null
                                            Utils.currentBridgeModule().toast("标签已更新：${it.tagLabel}")
                                            reload()
                                        }
                                        .onFailure { e -> note = e.message ?: "保存失败" }
                                },
                            )
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            FriendSectionTitle("黑名单")
                            Spacer(Modifier.height(8.dp))
                            FriendBlockSwitchRow(
                                blocked = current.blocked,
                                onToggle = { blocked ->
                                    FriendStore.repo.setBlocked(current.id, blocked)
                                        .onSuccess {
                                            note = null
                                            Utils.currentBridgeModule().toast(
                                                if (blocked) "已把 ${it.displayName} 加入黑名单" else "已移出黑名单",
                                            )
                                            reload()
                                        }
                                        .onFailure { e -> note = e.message ?: "操作失败" }
                                },
                            )
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            FriendSectionTitle("危险操作")
                            Spacer(Modifier.height(8.dp))
                            if (confirmDelete) {
                                FriendConfirmBar(
                                    message = "删除后 ${current.displayName} 将从好友列表移除，且不可恢复",
                                    confirmLabel = "确认删除好友",
                                    onConfirm = {
                                        FriendStore.repo.removeFriend(current.id)
                                            .onSuccess {
                                                Utils.currentBridgeModule().toast("已删除好友 ${current.displayName}")
                                                Utils.currentBridgeModule().closePage()
                                            }
                                            .onFailure { e -> note = e.message ?: "删除失败" }
                                    },
                                    onCancel = { confirmDelete = false },
                                )
                            } else {
                                FriendPrimaryButton(
                                    text = "删除好友",
                                    onClick = { confirmDelete = true },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRemarkHeader(friend: Friend) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FriendAvatar(friend.displayName, size = 64)
        Spacer(Modifier.height(10.dp))
        Text(
            friend.displayName,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppChrome.labelPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (friend.remark.isBlank()) "昵称：${friend.name}" else "昵称：${friend.name} · ${friend.subtitle}",
            fontSize = 12.sp,
            color = AppChrome.labelSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FriendTagPicker(
    words: List<String>,
    picked: List<String>,
    onToggle: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        words.chunked(4).forEach { rowWords ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                rowWords.forEachIndexed { index, word ->
                    FriendPillButton(
                        label = word,
                        filled = picked.contains(word),
                        onClick = { onToggle(word) },
                    )
                    if (index < rowWords.size - 1) Spacer(Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun FriendBlockSwitchRow(blocked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .clickable { onToggle(!blocked) }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                if (blocked) "已加入黑名单" else "加入黑名单",
                fontSize = 15.sp,
                color = AppChrome.labelPrimary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (blocked) "对方不在联系人列表，且无法发起单聊" else "拉黑后对方不出现在联系人列表",
                fontSize = 11.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // 无 Switch 组件（工程无先例），沿用 feature-auth AuthAgreeRow 的方块勾选形制。
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(22.dp)
                .background(
                    if (blocked) AppChrome.accent else AppChrome.surface,
                    RoundedCornerShape(4.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (blocked) {
                Text("✓", fontSize = 12.sp, color = AppChrome.surface, fontWeight = FontWeight.Bold)
            }
        }
    }
}
