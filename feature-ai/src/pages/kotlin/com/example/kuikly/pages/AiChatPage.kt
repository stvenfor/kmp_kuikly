package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.base.setTimeout
import com.example.kuikly.data.ai.AiChatSession
import com.example.kuikly.data.ai.AiConversation
import com.example.kuikly.data.ai.AiMessage
import com.example.kuikly.data.ai.AiModel
import com.example.kuikly.data.ai.AiModels
import com.example.kuikly.data.ai.AiPrompts
import com.example.kuikly.data.ai.AiRole
import com.example.kuikly.data.ai.AiStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
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
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * AI 小石头对话页 —— port of source `ai/view/ai_stream_page.dart` + AiStreamController。
 *
 * 承载：多轮气泡、分片流式（打字机）、停止生成、错误重试、复制、快捷问（prompt 预设）、
 * 模型选择、会话历史（新建 / 切换 / 删除）。停止 / 重试后保留已生成的半截文本。
 * 导航条复用共享 [AppNavBarBar]，配色沿用 [KuiklyTheme]。
 */
@Page("AiChat")
internal class AiChatPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val self = this
        setContent {
            val session = remember { AiChatSession(AiStore.stream, AiStore.conversations) }
            var version by remember { mutableStateOf(0) }
            var draft by remember { mutableStateOf("") }
            var models by remember { mutableStateOf<List<AiModel>>(emptyList()) }
            var history by remember { mutableStateOf<List<AiConversation>>(emptyList()) }
            var showModels by remember { mutableStateOf(false) }
            var showHistory by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            session.onChanged = { version = version + 1 }

            /** 逐帧播放：每帧后按 chunkDelayMs 排下一帧，得到打字机效果。 */
            fun pumpNext() {
                self.setTimeout(session.chunkDelayMs) {
                    if (session.pump()) pumpNext()
                }
            }

            fun ask(text: String) {
                val prompt = text.trim()
                if (prompt.isEmpty()) {
                    Utils.currentBridgeModule().toast("请输入问题")
                    return
                }
                if (!session.canSend) {
                    Utils.currentBridgeModule().toast("生成中，请稍候")
                    return
                }
                draft = ""
                if (session.send(prompt)) pumpNext()
            }

            if (!booted) {
                booted = true
                session.modelId = AiStore.currentModelId
                session.start()
            }

            Box(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AppNavBarBar(
                        title = "AI 小石头",
                        topInset = top,
                        onBack = { Utils.currentBridgeModule().closePage() },
                        background = KuiklyTheme.canvas,
                        foreground = KuiklyTheme.foreground,
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (session.isStreaming) {
                                    Text(
                                        "停止",
                                        fontSize = 13.sp,
                                        color = KuiklyTheme.error,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable { session.stop() }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                    )
                                } else {
                                    Text(
                                        "模型",
                                        fontSize = 13.sp,
                                        color = KuiklyTheme.link,
                                        modifier = Modifier
                                            .clickable {
                                                models = AiStore.models.all().getOrNull() ?: emptyList()
                                                showModels = true
                                            }
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "会话",
                                        fontSize = 13.sp,
                                        color = KuiklyTheme.link,
                                        modifier = Modifier
                                            .clickable {
                                                history = AiStore.conversations.all().getOrNull() ?: emptyList()
                                                showHistory = true
                                            }
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        },
                    )

                    AiStatusBar(session = session, onCycle = { version = version + 1 })

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            top = 10.dp,
                            end = 12.dp,
                            bottom = 12.dp,
                        ),
                    ) {
                        items(session.messages) { message ->
                            AiChatBubble(
                                message = message,
                                canRetry = !session.isStreaming && message.role == AiRole.assistant,
                                onCopy = {
                                    AiStore.clipboard.copy(message.text)
                                    Utils.currentBridgeModule().toast("已复制回复")
                                },
                                onRetry = { if (session.retry()) pumpNext() },
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    AiComposer(
                        draft = draft,
                        streaming = session.isStreaming,
                        quickPrompts = if (session.showQuickPrompts) AiPrompts.QUICK else emptyList(),
                        onQuickPrompt = { ask(it) },
                        onAppend = { draft += it },
                        onBackspace = { if (draft.isNotEmpty()) draft = draft.dropLast(1) },
                        onClear = { draft = "" },
                        onSend = { ask(draft) },
                        onStop = { session.stop() },
                        bottomInset = bottom,
                    )
                }

                if (showModels) {
                    AiModelSheet(
                        items = models,
                        current = session.modelId,
                        onPick = { id ->
                            AiStore.currentModelId = id
                            session.modelId = id
                            showModels = false
                            Utils.currentBridgeModule().toast("已切换到 ${AiModels.nameOf(id)}")
                        },
                        onDismiss = { showModels = false },
                    )
                }

                if (showHistory) {
                    AiHistorySheet(
                        items = history,
                        currentId = session.conversationId,
                        onOpen = { id ->
                            session.openConversation(id)
                            showHistory = false
                        },
                        onDelete = { id ->
                            session.deleteConversation(id)
                            history = AiStore.conversations.all().getOrNull() ?: emptyList()
                        },
                        onCreate = {
                            session.newConversation()
                            history = AiStore.conversations.all().getOrNull() ?: emptyList()
                            showHistory = false
                        },
                        onDismiss = { showHistory = false },
                    )
                }
            }
        }
    }
}

/** 当前模型 / 会话 id 信息条（含 Mock 场景切换，与 AiStream 页一致的调试入口）。 */
@Composable
private fun AiStatusBar(session: AiChatSession, onCycle: () -> Unit) {
    val cid = session.conversationId
    val model = if (session.serverModel.isEmpty()) session.modelId else session.serverModel
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvasSoft)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KChip("模型 ${AiModels.nameOf(model)}", color = KuiklyTheme.link)
        Spacer(Modifier.width(8.dp))
        KChip(if (cid == null) "新会话" else cid, color = KuiklyTheme.violet)
        Spacer(Modifier.weight(1f))
        Text(
            "[MOCK] ${MockBackend.scenario}",
            fontSize = 11.sp,
            color = KuiklyTheme.muted,
            modifier = Modifier.clickable { MockBackend.cycle(); onCycle() },
        )
    }
}

/**
 * 气泡：user 右侧蓝底、welcome 左侧浅蓝底带「AI 小石头」署名、assistant 左侧白底。
 * 流式未出字显示 …；已出字追加光标；错误态给出错误文案 + 重试。
 */
@Composable
private fun AiChatBubble(
    message: AiMessage,
    canRetry: Boolean,
    onCopy: () -> Unit,
    onRetry: () -> Unit,
) {
    val isUser = message.role == AiRole.user
    val isWelcome = message.role == AiRole.welcome
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (isUser) KuiklyTheme.link else if (isWelcome) KuiklyTheme.linkSoft else KuiklyTheme.canvas,
                    RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (isWelcome) {
                Text("AI 小石头", fontSize = 13.sp, color = KuiklyTheme.link, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
            }
            val display = when {
                message.text.isEmpty() && message.isStreaming -> "…"
                message.text.isEmpty() -> "（本次无回复）"
                message.isStreaming -> message.text + "▍"
                else -> message.text
            }
            Text(
                display,
                fontSize = 15.sp,
                color = if (isUser) KuiklyTheme.onPrimary else KuiklyTheme.foreground,
            )
            val err = message.errorMessage
            if (err != null) {
                Spacer(Modifier.height(6.dp))
                Text(err, fontSize = 13.sp, color = KuiklyTheme.error)
                if (canRetry) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "重试",
                        fontSize = 13.sp,
                        color = KuiklyTheme.link,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onRetry() }.padding(vertical = 2.dp),
                    )
                }
            } else if (!isUser && message.text.isNotEmpty() && !message.isStreaming) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "复制",
                    fontSize = 12.sp,
                    color = KuiklyTheme.muted,
                    modifier = Modifier.clickable { onCopy() }.padding(vertical = 2.dp),
                )
            }
        }
    }
}

/** 输入区：快捷问 + 候选词拼接（工程内无 TextField）+ 发送 / 停止。 */
@Composable
private fun AiComposer(
    draft: String,
    streaming: Boolean,
    quickPrompts: List<String>,
    onQuickPrompt: (String) -> Unit,
    onAppend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    bottomInset: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas)
            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = (bottomInset + 8f).dp),
    ) {
        if (quickPrompts.isNotEmpty()) {
            Text("试试这样问", fontSize = 12.sp, color = KuiklyTheme.muted)
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                quickPrompts.forEachIndexed { index, prompt ->
                    Box(
                        modifier = Modifier
                            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusPill))
                            .clickable { onQuickPrompt(prompt) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(prompt, fontSize = 12.sp, color = KuiklyTheme.link)
                    }
                    if (index < quickPrompts.size - 1) Spacer(Modifier.width(8.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (draft.isEmpty()) {
                    if (streaming) "生成中，请稍候…" else "点选词条拼接问题"
                } else {
                    draft
                },
                fontSize = 14.sp,
                color = if (draft.isEmpty()) KuiklyTheme.muted else KuiklyTheme.foreground,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (streaming) KuiklyTheme.error else KuiklyTheme.primary,
                        RoundedCornerShape(KuiklyTheme.radiusMd),
                    )
                    .clickable { if (streaming) onStop() else onSend() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (streaming) "停止" else "发送",
                    fontSize = 14.sp,
                    color = KuiklyTheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        AiPromptCandidates(
            onAppend = onAppend,
            onBackspace = onBackspace,
            onClear = onClear,
        )
    }
}

@Composable
private fun AiPromptCandidates(
    onAppend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(10.dp),
    ) {
        Text("点选词条拼接，或用退格 / 清空", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        AiPrompts.CANDIDATES.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                row.forEachIndexed { index, word ->
                    Box(
                        modifier = Modifier
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusSm))
                            .clickable { onAppend(word) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(word, fontSize = 13.sp, color = KuiklyTheme.body)
                    }
                    if (index < row.size - 1) Spacer(Modifier.width(8.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "退格",
                fontSize = 13.sp,
                color = KuiklyTheme.warningDeep,
                modifier = Modifier.clickable { onBackspace() }.padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "清空",
                fontSize = 13.sp,
                color = KuiklyTheme.error,
                modifier = Modifier.clickable { onClear() }.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

/** 模型选择底部弹层（port of source 请求体 options.model）。 */
@Composable
private fun AiModelSheet(
    items: List<AiModel>,
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KuiklyTheme.ink.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KuiklyTheme.canvas, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Text("选择模型", fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            if (items.isEmpty()) {
                Text("模型列表加载失败", fontSize = 13.sp, color = KuiklyTheme.error)
            } else {
                items.forEach { model ->
                    val selected = model.id == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(model.id) }
                            .background(
                                if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft,
                                RoundedCornerShape(KuiklyTheme.radiusMd),
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(model.name, fontSize = 15.sp, color = KuiklyTheme.foreground)
                            Spacer(Modifier.height(2.dp))
                            Text(model.desc, fontSize = 12.sp, color = KuiklyTheme.muted)
                        }
                        Spacer(Modifier.width(8.dp))
                        KChip(model.tag, color = if (selected) KuiklyTheme.link else KuiklyTheme.muted)
                        if (selected) {
                            Spacer(Modifier.width(6.dp))
                            Text("✓", fontSize = 15.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/** 会话历史底部弹层：新建 / 切换 / 删除。 */
@Composable
private fun AiHistorySheet(
    items: List<AiConversation>,
    currentId: String?,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KuiklyTheme.ink.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KuiklyTheme.canvas, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Text("会话历史", fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            if (items.isEmpty()) {
                Text("还没有会话，点下方新建", fontSize = 13.sp, color = KuiklyTheme.muted)
            } else {
                items.forEach { conversation ->
                    val selected = conversation.id == currentId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft,
                                RoundedCornerShape(KuiklyTheme.radiusMd),
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).clickable { onOpen(conversation.id) }) {
                            Text(conversation.title, fontSize = 14.sp, color = KuiklyTheme.foreground)
                            if (conversation.preview.isNotEmpty()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    conversation.preview,
                                    fontSize = 12.sp,
                                    color = KuiklyTheme.muted,
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${conversation.turnCount} 轮 · ${AiModels.nameOf(conversation.modelId)}",
                                fontSize = 11.sp,
                                color = KuiklyTheme.muted,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "删除",
                            fontSize = 13.sp,
                            color = KuiklyTheme.error,
                            modifier = Modifier.clickable { onDelete(conversation.id) }.padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                    .clickable { onCreate() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("新建对话", fontSize = 15.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
