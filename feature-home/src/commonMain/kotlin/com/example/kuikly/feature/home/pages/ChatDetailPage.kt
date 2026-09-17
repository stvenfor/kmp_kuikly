package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.chat.ChatMessage
import com.example.kuikly.data.chat.ChatStore
import com.example.kuikly.data.chat.Conversation
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * Mock chat detail — Flutter `ChatDetailPage` 复刻（Phase-2 / P2-W2b）。
 * 真源：`features/chat/lib/chat/view/chat_detail_page.dart` + `widgets/message_bubble.dart`
 * + `widgets/input_panel.dart`（收起态）。刻度同 `ChatPalette` KDoc：裸 dp/sp。
 * Errors / empty handled inline; composing new chat is out of slice scope（输入条为静态 + toast）。
 */
@Page(name = "ChatDetail", moduleId = "feature_home")
internal class ChatDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "1" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val result = remember(id) { ChatStore.repo.messages(id) }
            // 头部需要会话名 / 在线态：从 conversations 里取同 id 项（mock 同源）。
            val conversation = remember(id) {
                ChatStore.repo.conversations().getOrNull()?.find { it.id == id }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatPalette.background),
            ) {
                ChatDetailHeader(conversation = conversation, topInset = top)
                result
                    .onSuccess { messages ->
                        if (messages.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("暂无消息", fontSize = 13.sp, color = ChatPalette.labelSecondary)
                            }
                        } else {
                            // Flutter `MessageListView`：padding top/bottom 8，气泡自带 h12 v4。
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentPadding = PaddingValues(
                                    top = 8.dp,
                                    bottom = 8.dp,
                                ),
                            ) {
                                items(messages, key = { it.id }) { message ->
                                    MessageBubble(message = message)
                                }
                            }
                        }
                    }
                    .onFailure {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                it.message ?: "加载失败",
                                fontSize = 13.sp,
                                color = ChatPalette.labelSecondary,
                            )
                        }
                    }
                ChatInputPanel(bottomInset = bottom)
            }
        }
    }
}

/** Flutter `_ChatDetailHeader`：返回 ‹ + 36 头像 + 名字/在线态 + ⋯。 */
@Composable
private fun ChatDetailHeader(conversation: Conversation?, topInset: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatPalette.surface)
            // Flutter `EdgeInsets.only(top: AppSafeInsets.top, left: 4, right: 4, bottom: 8)`。
            .padding(start = 4.dp, end = 4.dp, top = topInset.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Flutter `CupertinoButton(CupertinoIcons.back, color: accent, size: 24)`。
        Text(
            "‹",
            fontSize = 24.sp,
            color = ChatPalette.accent,
            modifier = Modifier
                .clickable { Utils.currentBridgeModule().closePage() }
                .padding(12.dp),
        )
        // Flutter `CacheImageUtils.circle(peerAvatar, size: 36)` 未加载占位。
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(ChatPalette.fillSecondary, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                conversation?.title?.take(1) ?: "友",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChatPalette.labelPrimary,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Flutter `headline.copyWith(fontSize: 16)`（16 / w600）。
            Text(
                conversation?.title ?: "消息详情",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChatPalette.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Flutter `caption`（13）+ online/labelSecondary 着色。
            Text(
                if (conversation?.isOnline == true) "在线" else "离线",
                fontSize = 13.sp,
                color = if (conversation?.isOnline == true) ChatPalette.online else ChatPalette.labelSecondary,
            )
        }
        // Flutter `CupertinoButton(CupertinoIcons.ellipsis, color: accent)`。
        Text(
            "⋯",
            fontSize = 22.sp,
            color = ChatPalette.accent,
            modifier = Modifier
                .clickable { Utils.currentBridgeModule().toast("「会话操作」即将接入") }
                .padding(12.dp),
        )
    }
}

/**
 * Flutter `MessageBubble`（text 分支）：h12 v4 外边距 + 32 头像 + 8 间隙 +
 * 气泡（self `#007AFF` / peer `#E9E9EB`，r18 + 尾角 4）+ 17sp 正文。
 */
@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isSelf) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!message.isSelf) {
            BubbleAvatar(initial = "友", self = false)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            message.content,
            fontSize = 17.sp,
            color = if (message.isSelf) Color.White else ChatPalette.labelPrimary,
            modifier = Modifier
                // Flutter phone cap ≈ 75% 屏宽 (375dp → ~280dp)，K 留 285dp 余量避免过早换行。
                .widthIn(max = 285.dp)
                .background(
                    if (message.isSelf) ChatPalette.accent else ChatPalette.fillSecondary,
                    // Flutter `ChatTheme.bubbleRadiusFor`：顶角 18，尾角（对侧）4。
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (message.isSelf) 18.dp else 4.dp,
                        bottomEnd = if (message.isSelf) 4.dp else 18.dp,
                    ),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
        if (message.isSelf) {
            Spacer(Modifier.width(8.dp))
            BubbleAvatar(initial = "我", self = true)
        }
    }
}

/** Flutter `CacheImageUtils.circle(peerAvatar | ChatAvatarUrls.self, size: 32)` 未加载占位。 */
@Composable
private fun BubbleAvatar(initial: String, self: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                if (self) ChatPalette.accent else ChatPalette.fillSecondary,
                RoundedCornerShape(16.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initial,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (self) Color.White else ChatPalette.labelPrimary,
        )
    }
}

/**
 * Flutter `_PanelIconButton`：44×44 SizedBox + 24 icon + IconButton 默认内 padding。
 * Kuikly 没有 IconButton，故等价用 44×44 Box + 24sp emoji 居中（命中区与 Flutter 完全一致）。
 */
@Composable
private fun PanelIconButton(glyph: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 24.sp, color = ChatPalette.labelSecondary)
    }
}

/**
 * Flutter `InputPanel` 收起态的静态等价：白底 + mic 44 位 + r20 灰输入框（hint「信息」）
 * + smiley / plus 44 位。输入为 mock（点击 toast），不做真实键盘交互。
 */
@Composable
private fun ChatInputPanel(bottomInset: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatPalette.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Flutter `_PanelIconButton`：44×44 SizedBox + 24 icon 内嵌。这里同样显式锁定
            // 44×44 命中区，把 emoji 字号抬到 24.sp 与 Cupertino 视觉一致。
            PanelIconButton(
                glyph = "🎤",
                onClick = { Utils.currentBridgeModule().toast("「语音输入」即将接入") },
            )
            Spacer(Modifier.width(4.dp))
            // Flutter `_TextInput`：fillSecondary r20 / 0.5 separator 边 / h14 v10 / hint 17。
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(ChatPalette.fillSecondary, RoundedCornerShape(20.dp))
                    .border(0.5.dp, ChatPalette.separator, RoundedCornerShape(20.dp))
                    .clickable { Utils.currentBridgeModule().toast("「发送消息」即将接入") }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text("信息", fontSize = 17.sp, color = ChatPalette.labelTertiary)
            }
            Spacer(Modifier.width(4.dp))
            // Flutter `_PanelIconButton(CupertinoIcons.smiley | plus)`（无输入时的收起态）。
            PanelIconButton(
                glyph = "☺",
                onClick = { Utils.currentBridgeModule().toast("「表情」即将接入") },
            )
            // IconButton 默认 8dp 内 padding → 相邻两个 icon 间视觉间距 ≈ 16dp（smiley 右边 8 + plus 左边 8）。
            Spacer(Modifier.width(8.dp))
            PanelIconButton(
                glyph = "＋",
                onClick = { Utils.currentBridgeModule().toast("「更多」即将接入") },
            )
        }
        // Flutter `SizedBox(height: MediaQuery.paddingOf(context).bottom)`。
        Spacer(Modifier.height(bottomInset.dp))
    }
}
