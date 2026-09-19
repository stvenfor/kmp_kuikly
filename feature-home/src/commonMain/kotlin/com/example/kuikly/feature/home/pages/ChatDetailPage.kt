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
 * Flutter chat detail 的几何令牌（真源同 [ChatDetailPage] KDoc）。
 *
 * chat 模块不用 `flutter_screenutil`，故这些值原样落到裸 dp/sp（与 [ChatPalette] 同尺度）。
 */
private object ChatDetailTokens {
    /** 头部 `CupertinoButton` 与输入条 `_PanelIconButton` 的命中区（Flutter 两者都是 44）。 */
    const val HIT = 44f

    /** 返回 / 输入条图标（Flutter 显式 `size: 24`）。 */
    const val GLYPH = 24f

    /** 头部 ⋯：`CupertinoIcons.ellipsis` 未给 size → `actionTextStyle 17 × 1.2 ≈ 20`。 */
    const val MORE_GLYPH = 20f

    /**
     * Flutter `MessageBubble` 用 `Flexible` 约束气泡宽（无固定 maxWidth）：
     * 上限 = 屏宽 −（Row 水平 padding 12×2 + 头像 32 + 间隙 8）= 屏宽 − 64。
     */
    const val BUBBLE_CHROME = 64f

    /** `ChatTheme.body` / `selfBubbleText` / `peerBubbleText` 的 `height: 1.35` × 17 = 22.95 ≈ 23。 */
    const val BUBBLE_LINE_HEIGHT = 23f

    /** Flutter `MessageBubble` 在气泡与 caption 之间的 `SizedBox(height: 4)`。 */
    const val READ_STATUS_GAP = 4f

    /** Flutter `ChatTheme.caption.copyWith(fontSize: 11)`（`height: 1.3` × 11 = 14.3 ≈ 14）。 */
    const val READ_STATUS_FONT = 11f

    const val READ_STATUS_LINE_HEIGHT = 14f
}

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
        // Flutter `Flexible` 上限随屏宽走；这里按页面宽度反推，避免再用 285dp 经验值过早换行。
        val bubbleMaxWidth = pagerData.pageViewWidth - ChatDetailTokens.BUBBLE_CHROME
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
                                    MessageBubble(message = message, maxWidth = bubbleMaxWidth)
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
        // Flutter `CupertinoButton(padding: zero, child: Icon(back, accent, size: 24))`：
        // 命中区被 `kMinInteractiveDimensionCupertino(44)` 撑满，故整条头部高 44 + bottom 8。
        GlyphButton(
            glyph = "‹",
            fontSize = ChatDetailTokens.GLYPH,
            color = ChatPalette.accent,
            onClick = { Utils.currentBridgeModule().closePage() },
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
        // Flutter `CupertinoButton(padding: zero, child: Icon(ellipsis, color: accent))`（同为 44 命中区）。
        GlyphButton(
            glyph = "⋯",
            fontSize = ChatDetailTokens.MORE_GLYPH,
            color = ChatPalette.accent,
            onClick = { Utils.currentBridgeModule().toast("「会话操作」即将接入") },
        )
    }
}

/**
 * Flutter `MessageBubble`（text 分支）：h12 v4 外边距 + 32 头像 + 8 间隙 +
 * 气泡（self `#007AFF` / peer `#E9E9EB`，r18 + 尾角 4）+ 17sp 正文 + 4 间隙 + 11sp 已读态。
 *
 * 宽度：Flutter 用 `Flexible` 兜底（内容撑到屏宽 − 64），[maxWidth] 由调用方按页面宽度算好传入；
 * 该约束套在 `Column`（气泡 + caption）上，与 Flutter `Flexible(child: Column(...))` 同构。
 *
 * caption 文案取 `ChatMessage.readStatus`（Flutter `controller.readStatusLabel(current)`）；
 * 着色固定 `labelSecondary`——Flutter 仅 `sendStatus == failed` 时改 `unreadBadge`，
 * 而 mock 无发送态，故不引入该分支。
 */
@Composable
private fun MessageBubble(message: ChatMessage, maxWidth: Float) {
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
        Column(
            modifier = Modifier
                // Flutter `Flexible` 上限 = 屏宽 −（h12×2 + 头像 32 + 间隙 8）。
                .widthIn(max = maxWidth.dp),
            // Flutter `Column(crossAxisAlignment: isSelf ? end : start)`：caption 贴气泡同侧。
            horizontalAlignment = if (message.isSelf) Alignment.End else Alignment.Start,
        ) {
            Text(
                message.content,
                fontSize = 17.sp,
                // Flutter `ChatTheme.body/selfBubbleText/peerBubbleText` 显式 `height: 1.35`。
                lineHeight = ChatDetailTokens.BUBBLE_LINE_HEIGHT.sp,
                color = if (message.isSelf) Color.White else ChatPalette.labelPrimary,
                modifier = Modifier
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
            Spacer(Modifier.height(ChatDetailTokens.READ_STATUS_GAP.dp))
            // Flutter `ChatTheme.caption.copyWith(fontSize: 11, color: labelSecondary)`。
            Text(
                message.readStatus,
                fontSize = ChatDetailTokens.READ_STATUS_FONT.sp,
                lineHeight = ChatDetailTokens.READ_STATUS_LINE_HEIGHT.sp,
                color = ChatPalette.labelSecondary,
            )
        }
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
 * Flutter `_PanelIconButton`（输入条）/ `CupertinoButton`（头部）的等价：44×44 命中区 + 居中 glyph。
 *
 * `IconButton` / `CupertinoButton` 的最小命中区都是 44，Kuikly 没有内建图标矢量集，
 * 故用同尺寸 Box + emoji/字形占位，命中区与 Flutter 完全一致。
 */
@Composable
private fun GlyphButton(
    glyph: String,
    fontSize: Float,
    color: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(ChatDetailTokens.HIT.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = fontSize.sp, color = color)
    }
}

/**
 * Flutter `InputPanel` 收起态的静态等价：白底 + mic 44 位 + r20 灰输入框（hint「信息」）
 * + smiley / plus 44 位。输入为 mock（点击 toast），不做真实键盘交互。
 *
 * 排布照抄 Flutter：`Row(crossAxisAlignment: end, padding: 8)`，四枚 44 位控件**相邻无额外间距**
 * （空文本时 Flutter 的 `Expanded(_TextInput)` 直接夹在 mic 与 smiley 之间，
 * 有文本才用 `_SendButton` 顶替 smiley + plus）。
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
            verticalAlignment = Alignment.Bottom,
        ) {
            // Flutter `_PanelIconButton(CupertinoIcons.mic)`：44×44 SizedBox + 24 icon。
            GlyphButton(
                glyph = "🎤",
                fontSize = ChatDetailTokens.GLYPH,
                color = ChatPalette.labelSecondary,
                onClick = { Utils.currentBridgeModule().toast("「语音输入」即将接入") },
            )
            // Flutter `_TextInput`：fillSecondary r20 / 0.5 separator 边 / h14 v10 / hint 17。
            // 注意没有 Spacer——Flutter 的 Expanded 紧贴左侧 mic。
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
            // Flutter `_PanelIconButton(CupertinoIcons.smiley | plus)`：两者相邻，间距就是各自 44 宽。
            GlyphButton(
                glyph = "☺",
                fontSize = ChatDetailTokens.GLYPH,
                color = ChatPalette.labelSecondary,
                onClick = { Utils.currentBridgeModule().toast("「表情」即将接入") },
            )
            GlyphButton(
                glyph = "＋",
                fontSize = ChatDetailTokens.GLYPH,
                color = ChatPalette.labelSecondary,
                onClick = { Utils.currentBridgeModule().toast("「更多」即将接入") },
            )
        }
        // Flutter `SizedBox(height: MediaQuery.paddingOf(context).bottom)`。
        Spacer(Modifier.height(bottomInset.dp))
    }
}
