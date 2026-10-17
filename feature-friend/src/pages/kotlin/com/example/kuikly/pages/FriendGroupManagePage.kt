package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.Friend
import com.example.kuikly.data.friend.FriendGroup
import com.example.kuikly.data.friend.FriendStore
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
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

// 分组名候选词：源端建群/分组弹窗是自由输入，工程无 TextField，改用常用分组名词条。
private val FRIEND_GROUP_WORDS = listOf("家人", "同事", "同学", "球友", "书友", "邻居", "客户", "老乡")

/**
 * 好友分组管理（main 侧扩展：源端仅消费分组，未落增删改 UI）。
 *
 * 覆盖四件事：新建分组、重命名分组、删除分组（成员回落到首个剩余分组）、把好友移动到其它分组。
 */
@Page("FriendGroupManage")
internal class FriendGroupManagePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var groups by remember { mutableStateOf<List<FriendGroup>>(emptyList()) }
            var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
            var draft by remember { mutableStateOf("") }
            var editingId by remember { mutableStateOf<String?>(null) }
            var deletingId by remember { mutableStateOf<String?>(null) }
            var movingId by remember { mutableStateOf<String?>(null) }
            var note by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                FriendStore.repo.groups().onSuccess { groups = it }
                FriendStore.repo.all { true }.onSuccess { friends = it.filter { f -> !f.blocked } }
            }

            if (!booted) {
                booted = true
                reload()
            }

            fun submitName() {
                val currentEditing = editingId
                val result = if (currentEditing == null) {
                    FriendStore.repo.createGroup(draft)
                } else {
                    FriendStore.repo.renameGroup(currentEditing, draft)
                }
                result
                    .onSuccess {
                        note = null
                        draft = ""
                        editingId = null
                        Utils.currentBridgeModule().toast("已保存分组「${it.name}」")
                        reload()
                    }
                    .onFailure { note = it.message ?: "保存失败" }
            }

            val editingGroup = groups.firstOrNull { it.id == editingId }
            val deletingGroup = groups.firstOrNull { it.id == deletingId }
            val movingFriend = friends.firstOrNull { it.id == movingId }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                AppNavBarBar(
                    title = "分组管理",
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
                            label = if (editingGroup == null) "新建分组" else "重命名「${editingGroup.name}」",
                            value = draft,
                            hint = "点下方词条拼接到分组名",
                            candidates = FRIEND_GROUP_WORDS,
                            onAppend = { draft += it },
                            onBackspace = { if (draft.isNotEmpty()) draft = draft.dropLast(1) },
                            onClear = { draft = "" },
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            FriendPrimaryButton(
                                text = if (editingGroup == null) "创建分组" else "保存名称",
                                enabled = draft.isNotBlank(),
                                onClick = { submitName() },
                            )
                        }
                        if (editingGroup != null) {
                            Spacer(Modifier.height(8.dp))
                            FriendPillButton(
                                label = "取消重命名",
                                filled = false,
                                onClick = { editingId = null; draft = "" },
                            )
                        }
                        if (note != null) {
                            Spacer(Modifier.height(10.dp))
                            FriendNoteBar(note ?: "操作失败")
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        FriendSectionTitle("全部分组", "${groups.size} 个")
                    }

                    if (groups.isEmpty()) {
                        item {
                            FriendEmptyBlock(
                                title = "还没有分组",
                                message = "先创建一个分组，再把好友移动进去",
                            )
                        }
                    } else {
                        items(groups) { group ->
                            FriendGroupManageRow(
                                group = group,
                                memberCount = friends.count { it.groupId == group.id },
                                onRename = {
                                    editingId = group.id
                                    draft = ""
                                    deletingId = null
                                    note = null
                                },
                                onDelete = { deletingId = group.id },
                            )
                            if (deletingGroup != null && deletingGroup.id == group.id) {
                                Spacer(Modifier.height(8.dp))
                                val impact = FriendStore.repo.deleteGroupImpact(group.id)
                                FriendConfirmBar(
                                    message = "删除「${group.name}」后，${impact.first} 位好友将移至「${impact.second}」",
                                    confirmLabel = "确认删除",
                                    onConfirm = {
                                        FriendStore.repo.deleteGroup(group.id)
                                            .onSuccess { moved ->
                                                deletingId = null
                                                note = null
                                                Utils.currentBridgeModule().toast("已删除分组，迁移 $moved 位好友")
                                                reload()
                                            }
                                            .onFailure { note = it.message ?: "删除失败" }
                                    },
                                    onCancel = { deletingId = null },
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        FriendSectionTitle("移动好友", movingFriend?.name ?: "点好友后选目标分组")
                    }

                    if (friends.isEmpty()) {
                        item {
                            FriendEmptyBlock(
                                title = "还没有好友",
                                message = "先添加好友再来分组",
                            )
                        }
                    } else {
                        items(friends) { friend ->
                            FriendMoveRow(
                                friend = friend,
                                groupName = groups.firstOrNull { it.id == friend.groupId }?.name ?: "未分组",
                                selected = movingId == friend.id,
                                onSelect = {
                                    movingId = if (movingId == friend.id) null else friend.id
                                },
                            )
                            if (movingFriend != null && movingFriend.id == friend.id) {
                                Spacer(Modifier.height(8.dp))
                                FriendGroupPicker(
                                    groups = groups,
                                    currentId = friend.groupId,
                                    onPick = { target ->
                                        FriendStore.repo.moveFriend(friend.id, target.id)
                                            .onSuccess {
                                                movingId = null
                                                note = null
                                                Utils.currentBridgeModule().toast("已把 ${friend.name} 移到「${target.name}」")
                                                reload()
                                            }
                                            .onFailure { note = it.message ?: "移动失败" }
                                    },
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendGroupManageRow(
    group: FriendGroup,
    memberCount: Int,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                group.name,
                fontSize = 15.sp,
                color = AppChrome.labelPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text("$memberCount 位好友", fontSize = 11.sp, color = AppChrome.labelSecondary)
        }
        FriendPillButton(label = "改名", filled = false, onClick = onRename)
        Spacer(Modifier.width(8.dp))
        FriendPillButton(label = "删除", filled = false, onClick = onDelete)
    }
}

@Composable
private fun FriendMoveRow(
    friend: Friend,
    groupName: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) AppChrome.accent.copy(alpha = 0.08f) else AppChrome.surface,
                RoundedCornerShape(12.dp),
            )
            .clickable { onSelect() }
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
                "当前：$groupName",
                fontSize = 11.sp,
                color = AppChrome.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(if (selected) "选择目标" else "移动", fontSize = 12.sp, color = AppChrome.accent)
    }
}

@Composable
private fun FriendGroupPicker(
    groups: List<FriendGroup>,
    currentId: String,
    onPick: (FriendGroup) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text("移动到", fontSize = 12.sp, color = AppChrome.labelSecondary)
        Spacer(Modifier.height(8.dp))
        groups.chunked(3).forEach { rowGroups ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                rowGroups.forEachIndexed { index, group ->
                    if (group.id == currentId) {
                        FriendPillButton(label = "${group.name}（当前）", filled = false, enabled = false, onClick = { })
                    } else {
                        FriendPillButton(label = group.name, filled = false, onClick = { onPick(group) })
                    }
                    if (index < rowGroups.size - 1) Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
