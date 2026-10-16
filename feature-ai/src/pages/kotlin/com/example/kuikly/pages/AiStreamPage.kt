package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.ai.AiMessage
import com.example.kuikly.data.ai.AiRepository
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

/**
 * AI 流式对话页：快捷提问 + 打字机回复 + 对话历史（port of source AiStreamPage）。
 *
 * 流式通过 [AiRepository.streamChunks] 逐段回调拼接实现；
 * 历史由 [AiStore] 之上的 [AiHistory] 承载（Mock 内存态）。
 */
@Page("AiStream")
internal class AiStreamPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            val prompts = listOf("讲讲转化率", "如何看漏斗", "帮我分析数据", "什么是 ROI")
            val history = AiStore.history
            var messages by remember { mutableStateOf<List<AiMessage>>(emptyList()) }
            var streamed by remember { mutableStateOf("") }
            var streaming by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                history.page(1, 50).onSuccess { messages = it.items }.onFailure { error = it.message }
            }

            fun ask(prompt: String) {
                val userMsg = AiMessage("u-${System.currentTimeMillis()}", AiRole.user, prompt)
                history.append(userMsg)
                messages = messages + userMsg
                streamed = ""
                streaming = true
                error = null
                val replyId = "a-${System.currentTimeMillis()}"
                AiStore.repo.streamChunks(prompt) { chunk -> streamed += chunk }
                    .onSuccess { reply ->
                        val replyMsg = AiMessage(replyId, AiRole.assistant, reply)
                        history.append(replyMsg)
                        messages = messages + replyMsg
                    }
                    .onFailure {
                        error = it.message
                        val failMsg = AiMessage(replyId, AiRole.assistant, "回复失败，请重试", errorMessage = it.message)
                        history.append(failMsg)
                        messages = messages + failMsg
                    }
                streaming = false
                reload()
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("AI 助手", fontSize = 20.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "重开一轮",
                        fontSize = 13.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable {
                            Utils.currentBridgeModule().openPage(
                                url = "AiStream",
                                userData = JSONObject(),
                            )
                        },
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    item {
                        Text("试试这样问", fontSize = 13.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            prompts.take(2).forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusPill))
                                        .clickable { ask(p) }
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                ) {
                                    Text(p, fontSize = 12.sp, color = KuiklyTheme.link)
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            prompts.drop(2).forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusPill))
                                        .clickable { ask(p) }
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                ) {
                                    Text(p, fontSize = 12.sp, color = KuiklyTheme.link)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    if (error != null && messages.isEmpty()) {
                        item {
                            Text("加载失败：$error", fontSize = 13.sp, color = KuiklyTheme.error)
                            Spacer(Modifier.height(8.dp))
                        }
                    } else if (messages.isEmpty()) {
                        item {
                            Text("还没有对话，点上面的快捷问题开始吧", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        items(messages) { m -> AiBubble(m) ; Spacer(Modifier.height(8.dp)) }
                    }

                    if (streaming && streamed.isNotEmpty()) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .background(KuiklyTheme.canvas, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                ) {
                                    Text("$streamed▍", fontSize = 14.sp, color = KuiklyTheme.foreground)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    item {
                        Text(
                            "[CYCLE MOCK] $MockBackend.scenario",
                            fontSize = 12.sp,
                            color = KuiklyTheme.muted,
                            modifier = Modifier.clickable { MockBackend.cycle(); reload() },
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AiBubble(message: AiMessage) {
    val isUser = message.role == AiRole.user
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier
                .background(
                    if (isUser) KuiklyTheme.link else KuiklyTheme.canvas,
                    RoundedCornerShape(10.dp),
                )
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Column {
                Text(
                    message.text,
                    fontSize = 14.sp,
                    color = if (isUser) KuiklyTheme.onPrimary else KuiklyTheme.foreground,
                )
                if (message.errorMessage != null) {
                    Spacer(Modifier.height(4.dp))
                    KChip("失败", color = KuiklyTheme.error)
                }
            }
        }
    }
}
