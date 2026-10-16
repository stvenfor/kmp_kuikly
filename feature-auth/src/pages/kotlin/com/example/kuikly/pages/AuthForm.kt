package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.Utils
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

// auth 三个子页共用的表单组件。
// 工程未提供可验证的 TextField API，文本输入统一用「候选词追加 + 退格 + 清空」。

/** 顶部导航条。 */
@Composable
fun AuthNavBar(title: String, subtitle: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "‹ 返回",
            fontSize = 16.sp,
            color = KuiklyTheme.link,
            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 12.sp, color = KuiklyTheme.muted)
            }
        }
    }
}

/** 大标题 + 说明。 */
@Composable
fun AuthHeadline(title: String, desc: String = "") {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(title, fontSize = 24.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        if (desc.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(desc, fontSize = 13.sp, color = KuiklyTheme.muted)
        }
    }
}

/** 输入行，点击切换激活态。 */
@Composable
fun AuthFieldRow(
    label: String,
    value: String,
    hint: String,
    active: Boolean,
    mask: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .background(
                if (active) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft,
                RoundedCornerShape(KuiklyTheme.radiusMd),
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp, color = KuiklyTheme.muted, modifier = Modifier.width(76.dp))
        val shown = if (value.isEmpty()) hint else if (mask) "•".repeat(value.length) else value
        Text(
            shown,
            fontSize = 16.sp,
            color = if (value.isEmpty()) KuiklyTheme.muted else KuiklyTheme.foreground,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** 数字键盘，用于手机号 / 验证码。 */
@Composable
fun AuthNumberKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(10.dp),
    ) {
        AuthKeypadRow(listOf("1", "2", "3"), onDigit)
        Spacer(Modifier.height(8.dp))
        AuthKeypadRow(listOf("4", "5", "6"), onDigit)
        Spacer(Modifier.height(8.dp))
        AuthKeypadRow(listOf("7", "8", "9"), onDigit)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            AuthKeyButton(text = "清空", color = KuiklyTheme.error, onClick = onClear)
            Spacer(Modifier.width(8.dp))
            AuthKeyButton(text = "0", color = KuiklyTheme.foreground, onClick = { onDigit("0") })
            Spacer(Modifier.width(8.dp))
            AuthKeyButton(text = "退格", color = KuiklyTheme.warning, onClick = onBackspace)
        }
    }
}

@Composable
private fun AuthKeypadRow(digits: List<String>, onDigit: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        digits.forEachIndexed { index, digit ->
            AuthKeyButton(text = digit, color = KuiklyTheme.foreground, onClick = { onDigit(digit) })
            if (index < digits.size - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun AuthKeyButton(text: String, color: Color = KuiklyTheme.foreground, onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(100.dp).height(44.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 17.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

/** 词条输入区，用于邮箱 / 密码等文本。 */
@Composable
fun AuthTextInput(
    candidates: List<String>,
    onAppend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(10.dp),
    ) {
        Text("点选词条拼接，或用退格删除", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        candidates.chunked(3).forEach { rowWords ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                rowWords.forEachIndexed { index, word ->
                    AuthChip(text = word, color = KuiklyTheme.violet, onClick = { onAppend(word) })
                    if (index < rowWords.size - 1) Spacer(Modifier.width(8.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            AuthChip(text = "退格", color = KuiklyTheme.warning, onClick = onBackspace)
            Spacer(Modifier.width(8.dp))
            AuthChip(text = "清空", color = KuiklyTheme.error, onClick = onClear)
        }
    }
}

@Composable
fun AuthChip(text: String, selected: Boolean = false, color: Color = KuiklyTheme.link, onClick: () -> Unit = {}) {
    val bg = if (selected) color.copy(alpha = 0.16f) else KuiklyTheme.canvasSoft2
    val fg = if (selected) color else KuiklyTheme.body
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusPill))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 13.sp, color = fg, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

/** 同意条款开关行。 */
@Composable
fun AuthAgreeRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(20.dp).height(20.dp)
                .background(
                    if (checked) KuiklyTheme.success else KuiklyTheme.canvas,
                    RoundedCornerShape(KuiklyTheme.radiusXs),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Text("✓", fontSize = 12.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text("我已阅读并同意《用户协议》与《隐私政策》", fontSize = 13.sp, color = KuiklyTheme.body)
    }
}

/** 通栏主按钮。 */
@Composable
fun AuthPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val bg = if (enabled) KuiklyTheme.primary else KuiklyTheme.hairline
    val fg = if (enabled) KuiklyTheme.onPrimary else KuiklyTheme.muted
    Box(
        modifier = Modifier.fillMaxWidth().height(48.dp)
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 16.sp, color = fg, fontWeight = FontWeight.Bold)
    }
}

/** 错误提示条。 */
@Composable
fun AuthErrorText(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(message, fontSize = 13.sp, color = KuiklyTheme.errorDeep)
    }
}

/** 成功提示条。 */
@Composable
fun AuthInfoText(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(message, fontSize = 13.sp, color = KuiklyTheme.linkDeep)
    }
}

/** 底部跳转链接。 */
@Composable
fun AuthFooterLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        fontSize = 13.sp,
        color = KuiklyTheme.link,
        modifier = Modifier.clickable { onClick() }.padding(vertical = 6.dp),
    )
}
