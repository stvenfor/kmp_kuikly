package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.chat.ChatStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

// 搜索预设关键词（mock 无输入框，用关键词 chips 触发 repo.searchConversations）。
private val CHAT_SEARCH_KEYWORDS = listOf("增长", "张敏", "李工", "原型", "复盘")

@Page("ChatList")
internal class ChatListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var list by remember { mutableStateOf<List<com.example.kuikly.data.chat.Conversation>>(emptyList()) }
            var displayList by remember { mutableStateOf<List<com.example.kuikly.data.chat.Conversation>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }
            var searchOpen by remember { mutableStateOf(false) }
            var query by remember { mutableStateOf("") }

            fun reload() {
                ChatStore.repo.page(1, 50).onSuccess { r ->
                    list = r.items
                    if (!searchOpen) displayList = r.items
                }.onFailure { error = it.message }
            }
            fun applySearch() {
                if (!searchOpen || query.isBlank()) { displayList = list; return }
                ChatStore.repo.searchConversations(query)
                    .onSuccess { displayList = it }
                    .onFailure { displayList = emptyList() }
            }
            if (!booted) { booted = true; reload() }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("消息", fontSize = 22.sp, color = KuiklyTheme.foreground, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (searchOpen) "✕" else "🔍",
                        fontSize = 18.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable {
                            searchOpen = !searchOpen
                            if (!searchOpen) { query = ""; displayList = list }
                            else applySearch()
                        }.padding(6.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (searchOpen) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CHAT_SEARCH_KEYWORDS.forEach { kw ->
                            val selected = query == kw
                            Box(
                                modifier = Modifier.padding(end = 8.dp)
                                    .background(
                                        if (selected) KuiklyTheme.link else KuiklyTheme.canvasSoft2,
                                        RoundedCornerShape(KuiklyTheme.radiusPill),
                                    )
                                    .clickable {
                                        query = if (selected) "" else kw
                                        applySearch()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(kw, fontSize = 12.sp, color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                if (error != null) Text("Error: $error", color = Color.Red, fontSize = 14.sp)
                displayList.forEach { c ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { Utils.currentBridgeModule().openPage("ChatDetail", userData = JSONObject().apply { put("id", c.id) }) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.width(40.dp).height(40.dp).background(KuiklyTheme.linkSoft, RoundedCornerShape(8.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(c.title, fontSize = 15.sp, color = KuiklyTheme.foreground)
                            Text(c.lastMessage, fontSize = 12.sp, color = KuiklyTheme.muted, maxLines = 1)
                        }
                        if (c.unreadCount > 0) {
                            Box(modifier = Modifier.background(Color(0xFFFF4D4F), RoundedCornerShape(10.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(c.unreadCount.toString(), fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
                if (displayList.isEmpty()) {
                    // 源 ConversationListEmpty / "没有匹配的会话"：列表空 与 搜索无结果 两种空态。
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            if (searchOpen) "没有匹配的会话" else "暂无会话",
                            fontSize = 14.sp,
                            color = KuiklyTheme.muted,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("[CYCLE MOCK] $MockBackend.scenario", fontSize = 12.sp, color = KuiklyTheme.muted,
                    modifier = Modifier.clickable { MockBackend.cycle(); reload() })
            }
        }
    }
}
