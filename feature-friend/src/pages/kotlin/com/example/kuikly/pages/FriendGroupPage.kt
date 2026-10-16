package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.friend.Friend
import com.example.kuikly.data.friend.FriendSection
import com.example.kuikly.data.friend.FriendStore
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
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
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

private const val FRIEND_MODE_GROUP = "group"
private const val FRIEND_MODE_INITIAL = "initial"
private const val FRIEND_FILTER_ALL = "全部"
private const val FRIEND_FILTER_ONLINE = "在线"

@Page("FriendGroup")
internal class FriendGroupPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var mode by remember { mutableStateOf(FRIEND_MODE_GROUP) }
            var sections by remember { mutableStateOf<List<FriendSection>>(emptyList()) }
            var letters by remember { mutableStateOf<List<String>>(emptyList()) }
            var filter by remember { mutableStateOf(FRIEND_FILTER_ALL) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                val result = if (mode == FRIEND_MODE_GROUP) {
                    FriendStore.repo.sectionsByGroup()
                } else {
                    FriendStore.repo.sectionsByInitial()
                }
                result
                    .onSuccess {
                        sections = it
                        error = null
                    }
                    .onFailure {
                        sections = emptyList()
                        error = it.message ?: "加载失败"
                    }
                FriendStore.repo.sectionsByInitial().onSuccess { letters = it.map { s -> s.key } }
            }

            if (!booted) { booted = true; reload() }

            fun keep(friend: Friend): Boolean = when (filter) {
                FRIEND_FILTER_ALL -> true
                FRIEND_FILTER_ONLINE -> friend.isOnline
                else -> friend.initialLabel == filter
            }

            val visible = sections
                .map { s -> FriendSection(s.key, s.title, s.friends.filter { keep(it) }) }
                .filter { it.friends.isNotEmpty() }
            val totalVisible = visible.sumOf { it.count }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .background(KuiklyTheme.canvasSoft2)
                    .padding(horizontal = 14.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "‹ 返回",
                            fontSize = 15.sp,
                            color = KuiklyTheme.link,
                            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "分组联系人",
                                fontSize = 18.sp,
                                color = KuiklyTheme.foreground,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "共 ${FriendStore.repo.totalCount()} 位联系人 · 当前展示 $totalVisible",
                                fontSize = 11.sp,
                                color = KuiklyTheme.muted,
                            )
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        FriendModeTab(
                            label = "按分组",
                            selected = mode == FRIEND_MODE_GROUP,
                            onClick = { mode = FRIEND_MODE_GROUP; filter = FRIEND_FILTER_ALL; reload() },
                        )
                        Spacer(Modifier.width(10.dp))
                        FriendModeTab(
                            label = "按首字母",
                            selected = mode == FRIEND_MODE_INITIAL,
                            onClick = { mode = FRIEND_MODE_INITIAL; filter = FRIEND_FILTER_ALL; reload() },
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(12.dp),
                    ) {
                        Text("快捷筛选", fontSize = 12.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FriendFilterChip(
                                label = FRIEND_FILTER_ALL,
                                selected = filter == FRIEND_FILTER_ALL,
                                onClick = { filter = FRIEND_FILTER_ALL },
                            )
                            Spacer(Modifier.width(8.dp))
                            FriendFilterChip(
                                label = FRIEND_FILTER_ONLINE,
                                selected = filter == FRIEND_FILTER_ONLINE,
                                onClick = { filter = FRIEND_FILTER_ONLINE },
                            )
                            letters.forEach { letter ->
                                Spacer(Modifier.width(8.dp))
                                FriendFilterChip(
                                    label = letter,
                                    selected = filter == letter,
                                    onClick = { filter = letter },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (error != null) {
                    item {
                        FriendStateBlock(
                            title = error ?: "加载失败",
                            subtitle = "点击重试",
                            onClick = { reload() },
                        )
                    }
                } else if (visible.isEmpty()) {
                    item {
                        FriendStateBlock(
                            title = "没有符合条件的联系人",
                            subtitle = "换个筛选条件试试",
                            actionLabel = "重置筛选",
                            onClick = { filter = FRIEND_FILTER_ALL },
                        )
                    }
                } else {
                    visible.forEach { section ->
                        item {
                            FriendSectionHeader(section = section)
                        }
                        items(section.friends) { friend ->
                            FriendRow(
                                friend = friend,
                                onClick = {
                                    Utils.currentBridgeModule().toast("打开与 ${friend.name} 的会话（mock）")
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun FriendModeTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.primary else KuiklyTheme.canvas,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun FriendFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) KuiklyTheme.linkDeep else KuiklyTheme.body,
        )
    }
}

@Composable
private fun FriendSectionHeader(section: FriendSection) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            section.title,
            fontSize = 13.sp,
            color = KuiklyTheme.muted,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(8.dp))
        KChip("${section.count} 人", color = KuiklyTheme.muted)
        if (section.onlineCount > 0) {
            Spacer(Modifier.width(6.dp))
            KChip("${section.onlineCount} 人在线", color = KuiklyTheme.success)
        }
    }
}

@Composable
private fun FriendRow(friend: Friend, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusPill)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                friend.name.take(1),
                fontSize = 16.sp,
                color = KuiklyTheme.linkDeep,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(friend.name, fontSize = 15.sp, color = KuiklyTheme.ink)
            Spacer(Modifier.height(2.dp))
            Text(friend.subtitle, fontSize = 11.sp, color = KuiklyTheme.muted, maxLines = 1)
        }
        if (friend.isOnline) {
            Box(
                modifier = Modifier
                    .background(KuiklyTheme.success, RoundedCornerShape(KuiklyTheme.radiusPill))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("在线", fontSize = 10.sp, color = KuiklyTheme.onPrimary)
            }
        } else {
            Text("离线", fontSize = 11.sp, color = KuiklyTheme.muted)
        }
    }
}

@Composable
private fun FriendStateBlock(
    title: String,
    subtitle: String,
    actionLabel: String = "重试",
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .clickable { onClick() }
                .padding(horizontal = 22.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(actionLabel, fontSize = 13.sp, color = KuiklyTheme.onPrimary)
        }
    }
}
