package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.settings.DealInvoiceCustomer
import com.example.kuikly.data.settings.Invoice
import com.example.kuikly.data.settings.InvoiceStatus
import com.example.kuikly.data.settings.SettingsStore
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
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
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("InvoiceUpload")
internal class InvoiceUploadPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        val isDetail = id.isNotBlank()
        setContent {
            var customers by remember { mutableStateOf<List<DealInvoiceCustomer>>(emptyList()) }
            var selected by remember { mutableStateOf<DealInvoiceCustomer?>(null) }
            var invoice by remember { mutableStateOf<Invoice?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var message by remember { mutableStateOf<String?>(null) }
            var scenarioText by remember { mutableStateOf(MockBackend.scenario.name) }
            var booted by remember { mutableStateOf(false) }

            fun reloadCustomers() {
                scenarioText = MockBackend.scenario.name
                SettingsStore.repo.customers()
                    .onSuccess { customers = it; error = null }
                    .onFailure { customers = emptyList(); error = it.message ?: "加载客户失败" }
            }

            fun loadInvoice() {
                scenarioText = MockBackend.scenario.name
                SettingsStore.repo.findInvoice(id)
                    .onSuccess { invoice = it; error = null }
                    .onFailure { invoice = null; error = it.message ?: "发票不存在" }
            }

            if (!booted) {
                booted = true
                if (isDetail) loadInvoice() else reloadCustomers()
            }

            fun submit() {
                val cust = selected
                if (cust == null) {
                    message = "请选择购车客户"
                    return
                }
                SettingsStore.repo.uploadInvoice(cust.id)
                    .onSuccess {
                        Utils.currentBridgeModule().toast("已提交审核")
                        Utils.currentBridgeModule().closePage()
                    }
                    .onFailure { message = it.message ?: "提交失败" }
            }

            fun resubmit() {
                val inv = invoice
                if (inv == null) return
                SettingsStore.repo.resubmitInvoice(inv.id)
                    .onSuccess { Utils.currentBridgeModule().toast("已重新提交"); loadInvoice() }
                    .onFailure { message = it.message ?: "提交失败" }
            }

            fun rate(stars: Int) {
                val inv = invoice
                if (inv == null) return
                SettingsStore.repo.rateInvoice(inv.id, stars)
                    .onSuccess { Utils.currentBridgeModule().toast("已评分 $stars 星"); loadInvoice() }
                    .onFailure { message = it.message ?: "评分失败" }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                SettingsNavBar(
                    title = if (isDetail) "发票详情" else "上传成交发票",
                    subtitle = if (isDetail) "id=$id" else "选择购车客户后提交审核",
                )

                if (isDetail) {
                    val inv = invoice
                    when {
                        error != null && inv == null -> SettingsErrorState(message = error!!, onRetry = ::loadInvoice)
                        inv != null -> InvoiceDetailContent(invoice = inv, onRate = ::rate, onResubmit = ::resubmit)
                        else -> SettingsEmptyState(text = "发票不存在或已删除", actionText = "返回", onAction = { Utils.currentBridgeModule().closePage() })
                    }
                } else {
                    val sel = selected
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                        SettingsSectionTitle("选择购车客户")
                        Spacer(Modifier.width(8.dp))
                        if (sel != null) KChip(sel.display, color = KuiklyTheme.link)
                    }

                    when {
                        error != null && customers.isEmpty() -> SettingsErrorState(message = error!!, onRetry = ::reloadCustomers)
                        customers.isEmpty() -> SettingsEmptyState(text = "暂无购车客户", actionText = "重试", onAction = ::reloadCustomers)
                        else -> Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                            customers.forEach { cust ->
                                CustomerPickCard(cust = cust, selected = sel?.id == cust.id, onPick = { selected = cust })
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }

                    if (message != null) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                                .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(message!!, fontSize = 13.sp, color = KuiklyTheme.errorDeep)
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(4.dp))
                    SettingsPrimaryButton(
                        text = if (sel == null) "请选择客户" else "提交审核",
                        enabled = sel != null,
                        onClick = ::submit,
                    )
                }

                Spacer(Modifier.height(12.dp))
                SettingsScenarioBar(scenarioText = scenarioText, onCycle = { if (isDetail) loadInvoice() else reloadCustomers() })
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CustomerPickCard(cust: DealInvoiceCustomer, selected: Boolean, onPick: () -> Unit) {
    val bg = if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvas
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(bg, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onPick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(cust.name, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            if (selected) KChip("已选", color = KuiklyTheme.link)
        }
        Spacer(Modifier.height(6.dp))
        Text(cust.phone, fontSize = 13.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun InvoiceDetailContent(invoice: Invoice, onRate: (Int) -> Unit, onResubmit: () -> Unit) {
    val statusColor: Color = when (invoice.status) {
        InvoiceStatus.Approved -> KuiklyTheme.success
        InvoiceStatus.Rejected -> KuiklyTheme.error
        else -> KuiklyTheme.warningDeep
    }
    val customerText = if (invoice.customerName.isNotEmpty()) {
        "${invoice.customerName} ${invoice.customerPhone}"
    } else {
        invoice.customerPhone
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(invoice.title, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            KChip(if (invoice.type == "company") "企业" else "个人", color = KuiklyTheme.violet)
            Spacer(Modifier.width(8.dp))
            KChip(invoice.statusLabel, color = statusColor)
        }
        Spacer(Modifier.height(8.dp))
        SettingsKeyValue(label = "发票金额", value = "¥${"%.2f".format(invoice.amount)}", bold = true)
        if (customerText.isNotBlank()) SettingsKeyValue(label = "客户", value = customerText)
        if (invoice.submittedAt.isNotEmpty()) SettingsKeyValue(label = "提交时间", value = invoice.submittedAt)
        if (invoice.taxNo.isNotEmpty()) SettingsKeyValue(label = "税号", value = invoice.taxNo)

        if (invoice.status == InvoiceStatus.Rejected) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text("驳回原因：${invoice.rejectReason}", fontSize = 12.sp, color = KuiklyTheme.errorDeep)
            }
        }

        if (invoice.status == InvoiceStatus.Approved) {
            Spacer(Modifier.height(10.dp))
            SettingsSectionTitle("客户评分")
            SettingsStarRating(stars = invoice.ratingStars, onRate = onRate)
        }

        if (invoice.status == InvoiceStatus.Rejected) {
            Spacer(Modifier.height(10.dp))
            SettingsPrimaryButton(text = "重新提交", onClick = onResubmit)
        }
    }
}
