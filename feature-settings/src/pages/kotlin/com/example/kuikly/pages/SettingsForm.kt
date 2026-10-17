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

// 本文件是 settings 各子页共用的表单/状态组件。
// 只使用 Kuikly Compose 已验证的 Box / Row / Column / Text / clickable / background 组合，
// 不使用 TextField（工程内无先例），文本输入通过「候选词追加 + 退格 + 清空」实现。

/** 顶部导航条，左侧返回。 */
@Composable
fun SettingsNavBar(title: String, subtitle: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
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

/** 开关轨道里的占位块，用来把滑块推到左/右侧。 */
@Composable
private fun SettingsKnobSpace() {
    Box(modifier = Modifier.width(20.dp).height(20.dp))
}

/** 分组标题。 */
@Composable
fun SettingsSectionTitle(text: String, extra: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        if (extra.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Text(extra, fontSize = 12.sp, color = KuiklyTheme.muted)
        }
    }
}

/** 1px 分隔线。 */
@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier.fillMaxWidth().height(1.dp)
            .background(KuiklyTheme.hairline),
    )
}

/** 键值行。 */
@Composable
fun SettingsKeyValue(label: String, value: String, valueColor: Color = KuiklyTheme.body, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 13.sp, color = KuiklyTheme.muted, modifier = Modifier.width(110.dp))
        Text(
            value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/** 可点击的开关行。 */
@Composable
fun SettingsSwitchRow(
    label: String,
    desc: String = "",
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(240.dp)) {
            Text(label, fontSize = 15.sp, color = KuiklyTheme.foreground)
            if (desc.isNotEmpty()) {
                Text(desc, fontSize = 12.sp, color = KuiklyTheme.muted)
            }
        }
        Row(
            modifier = Modifier.width(46.dp).height(26.dp)
                .background(
                    if (checked) KuiklyTheme.success else KuiklyTheme.hairline,
                    RoundedCornerShape(KuiklyTheme.radiusPill),
                )
                .padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checked) SettingsKnobSpace()
            Box(
                modifier = Modifier.width(20.dp).height(20.dp)
                    .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusPill)),
            )
            if (!checked) SettingsKnobSpace()
        }
        Spacer(Modifier.width(8.dp))
        Text(if (checked) "开" else "关", fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

/** 表单字段行：点击后置为激活态，激活时高亮边框。 */
@Composable
fun SettingsFieldRow(
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
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp, color = KuiklyTheme.muted, modifier = Modifier.width(88.dp))
        val shown = if (value.isEmpty()) hint else if (mask) "•".repeat(value.length) else value
        Text(
            shown,
            fontSize = 15.sp,
            color = if (value.isEmpty()) KuiklyTheme.muted else KuiklyTheme.foreground,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** 单选项 chips 行。 */
@Composable
fun SettingsOptionChips(options: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        options.forEach { option ->
            SettingsChip(text = option, selected = option == selected, onClick = { onSelect(option) })
            Spacer(Modifier.width(8.dp))
        }
    }
}

/** 通用小 chip。 */
@Composable
fun SettingsChip(text: String, selected: Boolean = false, color: Color = KuiklyTheme.link, onClick: () -> Unit = {}) {
    val bg = if (selected) color.copy(alpha = 0.16f) else KuiklyTheme.canvasSoft2
    val fg = if (selected) color else KuiklyTheme.body
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusPill))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 13.sp, color = fg, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

/** 五星评分：点击第 n 颗星即评为 n 分（1..5）。 */
@Composable
fun SettingsStarRating(stars: Int, onRate: (Int) -> Unit = {}) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        for (i in 1..5) {
            val filled = i <= stars
            Text(
                if (filled) "★" else "☆",
                fontSize = 22.sp,
                color = if (filled) KuiklyTheme.warningDeep else KuiklyTheme.hairlineStrong,
                modifier = Modifier.clickable { onRate(i) }.padding(horizontal = 2.dp),
            )
        }
    }
}

/** 文本输入区：候选词追加 + 退格 + 清空（无 TextField 时的替代方案）。 */
@Composable
fun SettingsTextInput(
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
        Text("点选词条拼接内容，可用退格删除末尾", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        candidates.chunked(4).forEach { rowWords ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                rowWords.forEachIndexed { index, word ->
                    SettingsChip(text = word, color = KuiklyTheme.violet, onClick = { onAppend(word) })
                    if (index < rowWords.size - 1) Spacer(Modifier.width(8.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SettingsChip(text = "退格", color = KuiklyTheme.warning, onClick = onBackspace)
            Spacer(Modifier.width(8.dp))
            SettingsChip(text = "清空", color = KuiklyTheme.error, onClick = onClear)
        }
    }
}

/** 数字键盘：0-9 + 退格 + 清空。 */
@Composable
fun SettingsNumberKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(10.dp),
    ) {
        SettingsKeypadRow(listOf("1", "2", "3"), onDigit)
        Spacer(Modifier.height(8.dp))
        SettingsKeypadRow(listOf("4", "5", "6"), onDigit)
        Spacer(Modifier.height(8.dp))
        SettingsKeypadRow(listOf("7", "8", "9"), onDigit)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SettingsKeyButton(text = "清空", color = KuiklyTheme.error, onClick = onClear)
            Spacer(Modifier.width(8.dp))
            SettingsKeyButton(text = "0", color = KuiklyTheme.foreground, onClick = { onDigit("0") })
            Spacer(Modifier.width(8.dp))
            SettingsKeyButton(text = "退格", color = KuiklyTheme.warning, onClick = onBackspace)
        }
    }
}

@Composable
private fun SettingsKeypadRow(digits: List<String>, onDigit: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        digits.forEachIndexed { index, digit ->
            SettingsKeyButton(text = digit, color = KuiklyTheme.foreground, onClick = { onDigit(digit) })
            if (index < digits.size - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun SettingsKeyButton(text: String, color: Color = KuiklyTheme.foreground, onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(100.dp).height(44.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 17.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

/** 通栏主按钮。 */
@Composable
fun SettingsPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val bg = if (enabled) KuiklyTheme.primary else KuiklyTheme.hairline
    val fg = if (enabled) KuiklyTheme.onPrimary else KuiklyTheme.muted
    Box(
        modifier = Modifier.fillMaxWidth().height(46.dp)
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 16.sp, color = fg, fontWeight = FontWeight.Bold)
    }
}

/** 空状态。 */
@Composable
fun SettingsEmptyState(text: String, actionText: String = "", onAction: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("— ○ —", fontSize = 28.sp, color = KuiklyTheme.hairlineStrong)
        Spacer(Modifier.height(12.dp))
        Text(text, fontSize = 14.sp, color = KuiklyTheme.muted)
        if (actionText.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusMd))
                    .clickable { onAction() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(actionText, fontSize = 14.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/** 错误状态。 */
@Composable
fun SettingsErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("加载失败", fontSize = 16.sp, color = KuiklyTheme.error, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 13.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                .clickable { onRetry() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("重试", fontSize = 14.sp, color = KuiklyTheme.errorDeep, fontWeight = FontWeight.Medium)
        }
    }
}

/** Mock 场景切换条。 */
@Composable
fun SettingsScenarioBar(scenarioText: String, onCycle: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clickable { onCycle() }.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("[CYCLE MOCK] $scenarioText", color = KuiklyTheme.link, fontSize = 12.sp)
    }
}
