package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.settings.FinanceProduct
import com.example.kuikly.data.settings.PurchaseCalcQuote
import com.example.kuikly.data.settings.PurchaseCalculator
import com.example.kuikly.data.settings.SettingsStore
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("PurchaseCalc")
internal class PurchaseCalculatorPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            val products = remember { SettingsStore.repo.financeProducts() }
            var mode by remember { mutableStateOf(PurchaseCalculator.MODE_CASH) }
            var bareText by remember { mutableStateOf("100000") }
            var taxableText by remember { mutableStateOf("") }
            var downText by remember { mutableStateOf("30000") }
            var productId by remember { mutableStateOf(products.first().id) }
            var termMonths by remember { mutableStateOf(36) }
            var disableCommercial by remember { mutableStateOf(false) }
            var focus by remember { mutableStateOf("") }
            var quote by remember { mutableStateOf<PurchaseCalcQuote?>(null) }
            var error by remember { mutableStateOf<String?>(null) }

            val product: FinanceProduct? = SettingsStore.repo.findProduct(productId)

            fun calc() {
                val bare = bareText.toDoubleOrNull() ?: 0.0
                val taxable = taxableText.toDoubleOrNull() ?: 0.0
                val down = downText.toDoubleOrNull() ?: 0.0
                val result = SettingsStore.repo.calcQuote(
                    mode = mode,
                    barePrice = bare,
                    taxablePrice = taxable,
                    downPayment = down,
                    termMonths = termMonths,
                    productId = productId,
                    disableCommercial = disableCommercial,
                )
                result
                    .onSuccess { quote = it; error = null }
                    .onFailure { quote = null; error = it.message ?: "报价失败" }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                SettingsNavBar(title = "购车计算器", subtitle = "全款 / 贷款 报价试算")

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    SettingsSectionTitle("购车方式")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SettingsChip(
                            text = "全款",
                            selected = mode == PurchaseCalculator.MODE_CASH,
                            onClick = { mode = PurchaseCalculator.MODE_CASH; quote = null; error = null },
                        )
                        Spacer(Modifier.width(8.dp))
                        SettingsChip(
                            text = "贷款",
                            selected = mode == PurchaseCalculator.MODE_LOAN,
                            onClick = { mode = PurchaseCalculator.MODE_LOAN; quote = null; error = null },
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    SettingsSectionTitle("价格", "单位：元")
                    SettingsFieldRow(
                        label = "裸车价",
                        value = bareText,
                        hint = "必填",
                        active = focus == "bare",
                        onClick = { focus = if (focus == "bare") "" else "bare" },
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingsFieldRow(
                        label = "计税价",
                        value = taxableText,
                        hint = "选填，默认等于裸车价",
                        active = focus == "taxable",
                        onClick = { focus = if (focus == "taxable") "" else "taxable" },
                    )
                    if (mode == PurchaseCalculator.MODE_LOAN) {
                        Spacer(Modifier.height(8.dp))
                        SettingsFieldRow(
                            label = "首付款",
                            value = downText,
                            hint = "必填",
                            active = focus == "down",
                            onClick = { focus = if (focus == "down") "" else "down" },
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    SettingsSectionTitle("金融产品")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        products.forEach { p ->
                            SettingsChip(text = p.name, selected = p.id == productId, onClick = { productId = p.id; quote = null })
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    if (product != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "年化 ${"%.2f".format(product.annualRatePercent)}% · 最低首付 ${"%.0f".format(product.minDownPaymentPercent)}% · 手续费 ${"%.0f".format(product.oneTimeFee)}",
                            fontSize = 12.sp,
                            color = KuiklyTheme.muted,
                        )
                    }

                    if (mode == PurchaseCalculator.MODE_LOAN && product != null) {
                        Spacer(Modifier.height(12.dp))
                        SettingsSectionTitle("贷款期数")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            product.allowedTermsMonths.forEach { months ->
                                SettingsChip(text = "$months 期", selected = months == termMonths, onClick = { termMonths = months; quote = null })
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    SettingsSwitchRow(
                        label = "不计商业险",
                        desc = "仅保留交强险",
                        checked = disableCommercial,
                        onToggle = { disableCommercial = it; quote = null },
                    )
                }

                Spacer(Modifier.height(12.dp))

                when (focus) {
                    "bare" -> SettingsNumberKeypad(
                        onDigit = { if (bareText.length < 9) bareText += it },
                        onBackspace = { if (bareText.isNotEmpty()) bareText = bareText.dropLast(1) },
                        onClear = { bareText = "" },
                    )
                    "taxable" -> SettingsNumberKeypad(
                        onDigit = { if (taxableText.length < 9) taxableText += it },
                        onBackspace = { if (taxableText.isNotEmpty()) taxableText = taxableText.dropLast(1) },
                        onClear = { taxableText = "" },
                    )
                    "down" -> SettingsNumberKeypad(
                        onDigit = { if (downText.length < 9) downText += it },
                        onBackspace = { if (downText.isNotEmpty()) downText = downText.dropLast(1) },
                        onClear = { downText = "" },
                    )
                    else -> Box(modifier = Modifier.fillMaxWidth().height(0.dp))
                }

                Spacer(Modifier.height(12.dp))
                SettingsPrimaryButton(text = "计算报价", onClick = ::calc)
                Spacer(Modifier.height(12.dp))

                if (error != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(error!!, fontSize = 13.sp, color = KuiklyTheme.errorDeep)
                    }
                } else if (quote == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvasSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 12.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("填写价格后点击「计算报价」", fontSize = 13.sp, color = KuiklyTheme.muted)
                    }
                } else {
                    QuoteResultCard(quote = quote!!, onCopy = {
                        Utils.currentBridgeModule().toast("报价已生成（Mock）")
                    })
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuoteResultCard(quote: PurchaseCalcQuote, onCopy: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (quote.mode == PurchaseCalculator.MODE_LOAN) "贷款方案" else "全款方案",
                fontSize = 16.sp,
                color = KuiklyTheme.foreground,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            KChip(quote.productName, color = KuiklyTheme.link)
        }
        Spacer(Modifier.height(10.dp))
        quote.lines.forEach { line ->
            SettingsKeyValue(label = line.label, value = "¥${"%.2f".format(line.amount)}")
        }
        Spacer(Modifier.height(10.dp))
        SettingsDivider()
        Spacer(Modifier.height(10.dp))
        SettingsKeyValue(label = "首付合计", value = "¥${"%.2f".format(quote.initialPayment)}", bold = true)
        if (quote.mode == PurchaseCalculator.MODE_LOAN) {
            if (quote.effectiveAnnualRate != quote.annualRatePercent) {
                SettingsKeyValue(
                    label = "贴息后年化",
                    value = "%.2f%%（原 %.2f%%）".format(quote.effectiveAnnualRate, quote.annualRatePercent),
                    valueColor = KuiklyTheme.violet,
                    bold = true,
                )
            }
            SettingsKeyValue(label = "月供", value = "¥${"%.2f".format(quote.monthlyPayment)} × ${quote.termMonths} 期", bold = true)
            SettingsKeyValue(label = "总利息", value = "¥${"%.2f".format(quote.totalInterest)}")
            SettingsKeyValue(label = "还款总额", value = "¥${"%.2f".format(quote.totalRepayment)}")
        }
        SettingsKeyValue(
            label = "总支出",
            value = "¥${"%.2f".format(quote.totalDue)}",
            valueColor = KuiklyTheme.linkDeep,
            bold = true,
        )
        Spacer(Modifier.height(12.dp))
        SettingsPrimaryButton(text = "复制报价", onClick = onCopy)
    }
}
