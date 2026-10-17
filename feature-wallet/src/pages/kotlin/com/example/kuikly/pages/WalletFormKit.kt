package com.example.kuikly.pages

import androidx.compose.runtime.Composable
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

// wallet 表单共用组件：工程无 TextField，金额/卡号后四位用数字键盘，银行名用候选词条拼接。

/** 数字键盘（充值金额、卡号后四位共用）。 */
@Composable
fun WalletNumberKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(10.dp),
    ) {
        WalletKeypadRow(listOf("1", "2", "3"), onDigit)
        Spacer(Modifier.height(8.dp))
        WalletKeypadRow(listOf("4", "5", "6"), onDigit)
        Spacer(Modifier.height(8.dp))
        WalletKeypadRow(listOf("7", "8", "9"), onDigit)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WalletKeyButton(text = "清空", color = KuiklyTheme.error, onClick = onClear)
            Spacer(Modifier.width(8.dp))
            WalletKeyButton(text = "0", color = KuiklyTheme.foreground, onClick = { onDigit("0") })
            Spacer(Modifier.width(8.dp))
            WalletKeyButton(text = "退格", color = KuiklyTheme.warning, onClick = onBackspace)
        }
    }
}

@Composable
private fun WalletKeypadRow(digits: List<String>, onDigit: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        digits.forEachIndexed { index, digit ->
            WalletKeyButton(text = digit, color = KuiklyTheme.foreground, onClick = { onDigit(digit) })
            if (index < digits.size - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun WalletKeyButton(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(100.dp).height(44.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 17.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

/** 银行名称候选词条输入（点选拼接 + 退格/清空）。 */
@Composable
fun WalletBankNameInput(
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
        Text("点选银行名拼接，或用退格删除", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(8.dp))
        candidates.chunked(3).forEach { rowWords ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                rowWords.forEachIndexed { index, word ->
                    WalletTextChip(text = word, onClick = { onAppend(word) })
                    if (index < rowWords.size - 1) Spacer(Modifier.width(8.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WalletTextChip(text = "退格", color = KuiklyTheme.warning, onClick = onBackspace)
            Spacer(Modifier.width(8.dp))
            WalletTextChip(text = "清空", color = KuiklyTheme.error, onClick = onClear)
        }
    }
}

@Composable
private fun WalletTextChip(text: String, color: Color = KuiklyTheme.link, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(KuiklyTheme.radiusPill))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
