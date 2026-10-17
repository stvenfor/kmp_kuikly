package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

// friend 管理类页面（搜索/申请/分组/备注/黑名单）共用的组件。
// 工程无 TextField（全仓无先例），文本输入统一走「候选词追加 + 退格 + 清空」，
// 与 feature-auth 的 AuthTextInput、feature-community 发布页口径一致。

/** 分区小标题。 */
@Composable
internal fun FriendSectionTitle(text: String, extra: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, fontSize = 13.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Bold)
        if (extra.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Text(extra, fontSize = 12.sp, color = KuiklyTheme.muted)
        }
    }
}

/** 圆形头像位（无图片资源，取名字首字）。 */
@Composable
internal fun FriendAvatar(name: String, size: Int = 40, color: Color = KuiklyTheme.link) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.take(1),
            fontSize = (size / 2 - 4).sp,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 文本输入区：候选词追加 + 退格 + 清空。
 *
 * 对齐源端 `TextField`（`friend_page.dart` 的搜索框 / 建群弹窗的群名称输入框）。
 */
@Composable
internal fun FriendTextInput(
    label: String,
    value: String,
    hint: String,
    candidates: List<String>,
    onAppend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Text(label, fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusSm))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                if (value.isEmpty()) hint else value,
                fontSize = 15.sp,
                color = if (value.isEmpty()) KuiklyTheme.muted else KuiklyTheme.foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(8.dp))
        candidates.chunked(4).forEach { rowWords ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                rowWords.forEachIndexed { index, word ->
                    FriendWordChip(text = word, onClick = { onAppend(word) })
                    if (index < rowWords.size - 1) Spacer(Modifier.width(6.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            FriendWordChip(text = "退格", color = KuiklyTheme.warning, onClick = onBackspace)
            Spacer(Modifier.width(8.dp))
            FriendWordChip(text = "清空", color = KuiklyTheme.error, onClick = onClear)
        }
    }
}

@Composable
private fun FriendWordChip(text: String, color: Color = KuiklyTheme.link, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(KuiklyTheme.radiusPill))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

/** 胶囊按钮：filled 为主操作，outlined 为次级（对齐源端 _PillButton）。 */
@Composable
internal fun FriendPillButton(
    label: String,
    filled: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val bg = if (!enabled) KuiklyTheme.canvasSoft2 else if (filled) KuiklyTheme.primary else KuiklyTheme.canvas
    val fg = if (!enabled) KuiklyTheme.muted else if (filled) KuiklyTheme.onPrimary else KuiklyTheme.body
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusPill))
            .clickable { if (enabled) onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 13.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

/** 通栏按钮。 */
@Composable
internal fun FriendPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                if (enabled) KuiklyTheme.primary else KuiklyTheme.hairline,
                RoundedCornerShape(KuiklyTheme.radiusMd),
            )
            .clickable { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 15.sp,
            color = if (enabled) KuiklyTheme.onPrimary else KuiklyTheme.muted,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** 提示条：默认错误口径，可传 success 配色。 */
@Composable
internal fun FriendNoteBar(
    message: String,
    bg: Color = KuiklyTheme.errorSoft,
    fg: Color = KuiklyTheme.errorDeep,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(message, fontSize = 13.sp, color = fg)
    }
}

/** 二次确认条（删除好友 / 删除分组等破坏性操作）。 */
@Composable
internal fun FriendConfirmBar(
    message: String,
    confirmLabel: String = "确认",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.warningSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Text(message, fontSize = 13.sp, color = KuiklyTheme.warningDeep)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FriendPillButton(label = "取消", filled = false, onClick = onCancel)
            Spacer(Modifier.width(10.dp))
            FriendPillButton(label = confirmLabel, filled = true, onClick = onConfirm)
        }
    }
}

/** 居中空态。 */
@Composable
internal fun FriendEmptyBlock(
    title: String,
    message: String,
    actionLabel: String = "",
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, fontSize = 15.sp, color = KuiklyTheme.body, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            fontSize = 12.sp,
            color = KuiklyTheme.muted,
            textAlign = TextAlign.Center,
        )
        if (actionLabel.isNotEmpty() && onAction != null) {
            Spacer(Modifier.height(14.dp))
            FriendPillButton(label = actionLabel, filled = false, onClick = onAction)
        }
    }
}
