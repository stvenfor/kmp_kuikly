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
import com.example.kuikly.data.chat.ReadStatus
import com.example.kuikly.data.chat.SendStatus
import com.example.kuikly.data.chat.canRecall
import com.example.kuikly.data.chat.groupMessagesByTime
import com.example.kuikly.data.chat.statusLabel
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

// 源 ChatDetailViewModel.emojiList（16 个）。
private val CHAT_EMOJIS = listOf(
    "😀", "😂", "🥰", "😎", "🤔", "👍", "🙏", "🎉",
    "❤️", "🔥", "👋", "😭", "🤣", "😊", "🥳", "💪",
)

// more 面板动作（源 MorePanel：相册 / 拍摄 / 语音自检 + 语音发送）。
private val CHAT_MORE_ACTIONS = listOf("相册", "拍摄", "语音", "名片")

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
            var emojiOpen by remember { mutableStateOf(false) }
            var moreOpen by remember { mutableStateOf(false) }
            var sendSeq by remember { mutableStateOf(0) }

            fun nowSeconds(): Long = messages.maxOfOrNull { it.createdAt } ?: 1_700_000_000L

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

            // 乐观发送：先插入 sending 态消息，再根据 repo 结果落到 sent/failed。
            fun doSend(text: String) {
                if (text.isBlank()) {
                    Utils.currentBridgeModule().toast("请先选择要发送的内容")
                    return
                }
                val tempId = "tmp_${++sendSeq}"
                val optimistic = Message(
                    id = tempId,
                    conversationId = conversationId,
                    type = MessageType.text,
                    content = text,
                    isSelf = true,
                    createdAt = nowSeconds() + 1,
                    sendStatus = SendStatus.sending,
                    readStatus = ReadStatus.unread,
                    senderName = "我",
                )
                messages = messages + optimistic
                ChatStore.repo.send(conversationId, text)
                    .onSuccess { sent ->
                        messages = messages.map { if (it.id == tempId) sent.copy(readStatus = ReadStatus.unread) else it }
                        ChatStore.repo.autoReply(conversationId, text)
                            .onSuccess { reply -> messages = messages + reply }
                        // 模拟对方已读回执：把刚发出的 self 消息标为已读。
                        messages = messages.map { if (it.id == sent.id) it.copy(readStatus = ReadStatus.read) else it }
                        draft = ""
                        emojiOpen = false
                        moreOpen = false
                        Utils.currentBridgeModule().toast("已发送")
                    }
                    .onFailure {
                        messages = messages.map { if (it.id == tempId) it.copy(sendStatus = SendStatus.failed) else it }
                        Utils.currentBridgeModule().toast("发送失败，点击下方消息重发")
                    }
            }

            fun doSendImage() {
                val url = "https://picsum.photos/seed/album_${++sendSeq}/800"
                ChatStore.repo.sendImage(conversationId, url)
                    .onSuccess { messages = messages + it; moreOpen = false; Utils.currentBridgeModule().toast("已发送图片") }
                    .onFailure { Utils.currentBridgeModule().toast("图片发送失败") }
            }

            fun doSendVoice() {
                ChatStore.repo.sendVoice(conversationId, 3)
                    .onSuccess { messages = messages + it; moreOpen = false; Utils.currentBridgeModule().toast("已发送语音") }
                    .onFailure { Utils.currentBridgeModule().toast("语音发送失败") }
            }

            fun doSendCustom() {
                ChatStore.repo.sendCustom(conversationId, "card", "Demo 名片", "来自自定义消息")
                    .onSuccess { messages = messages + it; moreOpen = false; Utils.currentBridgeModule().toast("已发送名片") }
                    .onFailure { Utils.currentBridgeModule().toast("名片发送失败") }
            }

            fun doRecall(messageId: String) {
                ChatStore.repo.recallMessage(conversationId, messageId)
                    .onSuccess { recalled -> messages = messages.map { if (it.id == messageId) recalled else it } }
                    .onFailure { Utils.currentBridgeModule().toast("撤回失败") }
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

            val now = nowSeconds()
            val sections = groupMessagesByTime(messages, now)

            Box(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                when {
                    error != null && messages.isEmpty() ->
                        ChatDetailMessage(text = error ?: "加载失败", onRetry = ::reload)
                    messages.isEmpty() ->
                        ChatDetailMessage(text = "还没有消息，选一句候选内容发送试试", onRetry = ::reload)
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    ) {
                        item { ChatDetailHeader(conversation = conversation, onRefresh = ::reload) }
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
                                    nowSeconds = now,
                                    onImageClick = { url ->
                                        val data = JSONObject().apply { put("url", url) }
                                        Utils.currentBridgeModule()
                                            .openPage(url = "ChatImagePreview", userData = data)
                                    },
                                    onRetry = { doSend(it) },
                                    onRecall = { doRecall(it) },
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
                                onSend = { doSend(draft) },
                                emojiOpen = emojiOpen,
                                moreOpen = moreOpen,
                                onToggleEmoji = { emojiOpen = !emojiOpen; if (emojiOpen) moreOpen = false },
                                onToggleMore = { moreOpen = !moreOpen; if (moreOpen) emojiOpen = false },
                                onAppendEmoji = { draft = draft + it },
                                onSendImage = ::doSendImage,
                                onSendVoice = ::doSendVoice,
                                onSendCustom = ::doSendCustom,
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
private fun ChatDetailHeader(conversation: Conversation?, onRefresh: () -> Unit) {
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
        Column(modifier = Modifier.weight(1f)) {
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
        Text(
            "刷新",
            fontSize = 13.sp,
            color = KuiklyTheme.link,
            modifier = Modifier.clickable { onRefresh() }.padding(start = 8.dp),
        )
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
private fun ChatBubble(
    message: Message,
    nowSeconds: Long,
    onImageClick: (String) -> Unit,
    onRetry: (String) -> Unit,
    onRecall: (String) -> Unit,
) {
    // 撤回消息：居中灰条，不再渲染气泡。
    if (message.isRecalled) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text("你撤回了一条消息", fontSize = 12.sp, color = KuiklyTheme.muted)
        }
        return
    }

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
            when (message.type) {
                MessageType.image -> {
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
                }
                MessageType.voice -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔊", fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("${message.voiceDurationSeconds}″", fontSize = 14.sp, color = KuiklyTheme.ink)
                    }
                }
                MessageType.custom -> {
                    Column {
                        Text(
                            message.customPayload["title"] ?: message.customType,
                            fontSize = 14.sp,
                            color = KuiklyTheme.foreground,
                            fontWeight = FontWeight.Bold,
                        )
                        val sub = message.customPayload["subtitle"]
                        if (!sub.isNullOrEmpty()) {
                            Spacer(Modifier.height(3.dp))
                            Text(sub, fontSize = 12.sp, color = KuiklyTheme.muted)
                        }
                    }
                }
                else -> {
                    Text(message.content, fontSize = 15.sp, color = KuiklyTheme.ink)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val label = statusLabel(message)
                if (label.isNotEmpty()) {
                    val failed = message.sendStatus == SendStatus.failed
                    Text(
                        label,
                        fontSize = 10.sp,
                        color = if (failed) KuiklyTheme.error else KuiklyTheme.muted,
                        modifier = if (failed) Modifier.clickable { onRetry(message.content) } else Modifier,
                    )
                }
                if (message.isSelf && canRecall(message, nowSeconds)) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "撤回",
                        fontSize = 10.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { onRecall(message.id) },
                    )
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
private fun ChatInputPanel(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    emojiOpen: Boolean,
    moreOpen: Boolean,
    onToggleEmoji: () -> Unit,
    onToggleMore: () -> Unit,
    onAppendEmoji: (String) -> Unit,
    onSendImage: () -> Unit,
    onSendVoice: () -> Unit,
    onSendCustom: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        if (emojiOpen) {
            ChatEmojiPanel(onAppendEmoji = onAppendEmoji)
            Spacer(Modifier.height(10.dp))
        }
        if (moreOpen) {
            ChatMorePanel(
                onSendImage = onSendImage,
                onSendVoice = onSendVoice,
                onSendCustom = onSendCustom,
            )
            Spacer(Modifier.height(10.dp))
        }

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
            // 表情 / 更多 切换（源 InputPanel 的 smiley + plus 按钮）
            Box(
                modifier = Modifier
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .clickable { onToggleEmoji() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (emojiOpen) "⌨" else "😊", fontSize = 14.sp, color = KuiklyTheme.body)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .clickable { onToggleMore() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("⊕", fontSize = 14.sp, color = KuiklyTheme.body)
            }
            Spacer(Modifier.width(8.dp))
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
private fun ChatEmojiPanel(onAppendEmoji: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CHAT_EMOJIS.chunked(8).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { emoji ->
                    Box(
                        modifier = Modifier.weight(1f).clickable { onAppendEmoji(emoji) }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMorePanel(
    onSendImage: () -> Unit,
    onSendVoice: () -> Unit,
    onSendCustom: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CHAT_MORE_ACTIONS.forEach { action ->
            Column(
                modifier = Modifier.clickable {
                    when (action) {
                        "相册", "拍摄" -> onSendImage()
                        "语音" -> onSendVoice()
                        "名片" -> onSendCustom()
                    }
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.width(52.dp).height(52.dp)
                        .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        when (action) {
                            "相册" -> "🖼"
                            "拍摄" -> "📷"
                            "语音" -> "🎤"
                            else -> "📇"
                        },
                        fontSize = 22.sp,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(action, fontSize = 12.sp, color = KuiklyTheme.body)
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
