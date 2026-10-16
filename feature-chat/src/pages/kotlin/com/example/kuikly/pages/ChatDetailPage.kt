package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.chat.ChatStore
import com.example.kuikly.data.chat.Conversation
import com.example.kuikly.data.chat.Message
import com.example.kuikly.data.chat.MessageType
import com.example.kuikly.data.chat.groupMessagesByTime
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

private val CHAT_QUICK_PHRASES = listOf(
    "在吗？新版原型看了吗",
    "收到，我下午同步到群里",
    "周三复盘会几点？",
    "会议室已预定 A302",
    "稍后把结论贴出来",
)

private const val CHAT_PAGE_SIZE = 5

@Page("ChatDetail")
internal class ChatDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val conversationId = pageData.params.optString("id")
        setContent {
            var conversation by remember { mutableStateOf<Conversation?>(null) }
            var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var hasMore by remember { mutableStateOf(false) }
            var page by remember { mutableStateOf(0) }
            var draft by remember { mutableStateOf("") }
            var marked by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                ChatStore.repo.byId(conversationId)
                    .onSuccess { conversation = it }
                    .onFailure { error = it.message ?: "会话不存在" }
                ChatStore.repo.messagePage(conversationId, 1, CHAT_PAGE_SIZE)
                    .onSuccess {
                        messages = it.items
                        hasMore = it.hasMore
                        page = if (it.items.isEmpty()) 0 else 1
                        error = null
                    }
                    .onFailure {
                        messages = emptyList()
                        error = it.message ?: "消息加载失败"
                    }
            }

            fun loadMore() {
                if (!hasMore) return
                val next = page + 1
                ChatStore.repo.messagePage(conversationId, next, CHAT_PAGE_SIZE)
                    .onSuccess {
                        messages = messages + it.items
                        hasMore = it.hasMore
                        page = next
                    }
            }

            if (!booted) {
                booted = true
                reload()
                // 进入会话即清零未读（mock：写入未读覆盖表）。
                if (!marked) {
                    marked = true
                    ChatStore.repo.markRead(conversationId).onSuccess { conversation = it }
                }
            }

            val nowSeconds = messages.maxOfOrNull { it.createdAt } ?: 0L
            val sections = groupMessagesByTime(messages, nowSeconds)

            Box(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                when {
                    error != null && messages.isEmpty() ->
                        ChatDetailMessage(text = error ?: "加载失败", onRetry = ::reload)
                    messages.isEmpty() ->
                        ChatDetailMessage(text = "还没有消息，选一句候选内容发送试试", onRetry = ::reload)
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    ) {
                        item { ChatDetailHeader(conversation = conversation) }
                        item {
                            if (error != null) {
                                ChatErrorBanner(text = error ?: "加载失败", onRetry = ::reload)
                            }
                        }
                        if (hasMore) {
                            item {
                                ChatMoreButton(onClick = ::loadMore)
                            }
                        }
                        sections.forEach { section ->
                            item { ChatTimeDivider(label = section.label) }
                            items(section.messages) { msg ->
                                ChatBubble(
                                    message = msg,
                                    onImageClick = { url ->
                                        val data = JSONObject().apply { put("url", url) }
                                        Utils.currentBridgeModule()
                                            .openPage(url = "ChatImagePreview", userData = data)
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        item {
                            ChatNoMoreTip(hasMore = hasMore, total = messages.size)
                        }
                        item {
                            ChatInputPanel(
                                draft = draft,
                                onDraftChange = { draft = it },
                                onSend = {
                                    if (draft.isBlank()) {
                                        Utils.currentBridgeModule().toast("请先选择要发送的内容")
                                        return@ChatInputPanel
                                    }
                                    ChatStore.repo.send(conversationId, draft)
                                        .onSuccess { sent ->
                                            messages = messages + sent
                                            ChatStore.repo.autoReply(conversationId, draft)
                                                .onSuccess { reply -> messages = messages + reply }
                                            draft = ""
                                            Utils.currentBridgeModule().toast("已发送")
                                        }
                                        .onFailure { Utils.currentBridgeModule().toast("发送失败") }
                                },
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatDetailHeader(conversation: Conversation?) {
    Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 12.dp),
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
                conversation?.title ?: "会话",
                fontSize = 17.sp,
                color = KuiklyTheme.foreground,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                conversation?.subtitle ?: "加载中",
                fontSize = 12.sp,
                color = KuiklyTheme.muted,
                maxLines = 1,
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ChatTimeDivider(label: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 11.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun ChatBubble(message: Message, onImageClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSelf) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (message.isSelf) KuiklyTheme.linkSoft else KuiklyTheme.canvas,
                    RoundedCornerShape(KuiklyTheme.radiusLg),
                )
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            if (!message.isSelf && message.senderName.isNotEmpty()) {
                Text(message.senderName, fontSize = 11.sp, color = KuiklyTheme.muted)
                Spacer(Modifier.height(3.dp))
            }
            if (message.type == MessageType.image) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(110.dp)
                        .background(KuiklyTheme.hairline, RoundedCornerShape(KuiklyTheme.radiusSm))
                        .clickable { onImageClick(message.imageUrl ?: message.content) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("图片消息 · 点击预览", fontSize = 12.sp, color = KuiklyTheme.body)
                }
            } else {
                Text(message.content, fontSize = 15.sp, color = KuiklyTheme.ink)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (message.isSelf) "已读" else "",
                    fontSize = 10.sp,
                    color = KuiklyTheme.muted,
                )
                if (message.voiceDurationSeconds > 0) {
                    Text("${message.voiceDurationSeconds}″", fontSize = 10.sp, color = KuiklyTheme.muted)
                }
            }
        }
    }
}

@Composable
private fun ChatMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("加载更早的消息", fontSize = 13.sp, color = KuiklyTheme.link)
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun ChatNoMoreTip(hasMore: Boolean, total: Int) {
    if (hasMore) return
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text("已加载全部 $total 条消息", fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun ChatInputPanel(draft: String, onDraftChange: (String) -> Unit, onSend: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Text("快捷候选内容", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        CHAT_QUICK_PHRASES.forEach { phrase ->
            val selected = draft == phrase
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    .background(
                        if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .clickable { onDraftChange(if (selected) "" else phrase) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    phrase,
                    fontSize = 13.sp,
                    color = if (selected) KuiklyTheme.linkDeep else KuiklyTheme.body,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                .padding(horizontal = 10.dp, vertical = 10.dp),
        ) {
            Text(
                if (draft.isBlank()) "点击上方候选词切换要发送的内容" else draft,
                fontSize = 14.sp,
                color = if (draft.isBlank()) KuiklyTheme.muted else KuiklyTheme.ink,
                maxLines = 2,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KChip(if (draft.isBlank()) "未选择" else "已选 ${draft.length} 字", color = KuiklyTheme.muted)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .clickable { onDraftChange("") }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("清空", fontSize = 13.sp, color = KuiklyTheme.body)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (draft.isBlank()) KuiklyTheme.hairline else KuiklyTheme.primary,
                        RoundedCornerShape(KuiklyTheme.radiusMd),
                    )
                    .clickable { onSend() }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("发送", fontSize = 14.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ChatErrorBanner(text: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onRetry() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text("$text · 点击重试", fontSize = 13.sp, color = KuiklyTheme.errorDeep)
    }
}

@Composable
private fun ChatDetailMessage(text: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, fontSize = 14.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("重试", color = KuiklyTheme.onPrimary, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "‹ 返回",
            fontSize = 14.sp,
            color = KuiklyTheme.link,
            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
        )
    }
}
