package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.settings.Invoice
import com.example.kuikly.data.settings.InvoiceStatus
import com.example.kuikly.data.settings.InvoiceStats
import com.example.kuikly.data.settings.SettingsStore
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KMetricPill
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
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
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

@Page("InvoiceList")
internal class InvoiceListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var items by remember { mutableStateOf<List<Invoice>>(emptyList()) }
            var stats by remember { mutableStateOf<InvoiceStats?>(null) }
            var tab by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }
            var scenarioText by remember { mutableStateOf(MockBackend.scenario.name) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenarioText = MockBackend.scenario.name
                SettingsStore.repo.invoices(tab)
                    .onSuccess { items = it; error = null }
                    .onFailure { items = emptyList(); error = it.message ?: "加载发票失败" }
                SettingsStore.repo.invoiceStats()
                    .onSuccess { stats = it }
                    .onFailure { stats = null }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                SettingsNavBar(title = "发票管理", subtitle = "成交发票审核状态")

                val current = stats
                if (current != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        KMetricPill("已上传", current.uploaded.toString())
                        Spacer(Modifier.width(8.dp))
                        KMetricPill("待审核", current.pending.toString())
                        Spacer(Modifier.width(8.dp))
                        KMetricPill("已通过", current.approved.toString(), accent = true)
                        Spacer(Modifier.width(8.dp))
                        KMetricPill("未通过", current.rejected.toString())
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .clickable { Utils.currentBridgeModule().openPage("InvoiceUpload") }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+ 上传发票", fontSize = 13.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SettingsChip(text = "全部", selected = tab.isEmpty(), onClick = { tab = ""; reload() })
                    Spacer(Modifier.width(8.dp))
                    SettingsChip(
                        text = InvoiceStatus.labelOf(InvoiceStatus.Pending),
                        selected = tab == InvoiceStatus.Pending,
                        onClick = { tab = InvoiceStatus.Pending; reload() },
                    )
                    Spacer(Modifier.width(8.dp))
                    SettingsChip(
                        text = InvoiceStatus.labelOf(InvoiceStatus.Approved),
                        selected = tab == InvoiceStatus.Approved,
                        onClick = { tab = InvoiceStatus.Approved; reload() },
                    )
                    Spacer(Modifier.width(8.dp))
                    SettingsChip(
                        text = InvoiceStatus.labelOf(InvoiceStatus.Rejected),
                        selected = tab == InvoiceStatus.Rejected,
                        onClick = { tab = InvoiceStatus.Rejected; reload() },
                    )
                }

                when {
                    error != null && items.isEmpty() -> SettingsErrorState(message = error!!, onRetry = ::reload)
                    items.isEmpty() -> SettingsEmptyState(text = "该分类下暂无发票", actionText = "查看全部", onAction = { tab = ""; reload() })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            InvoiceCard(
                                item = item,
                                onOpen = {
                                    val data = JSONObject()
                                    data.put("id", item.id)
                                    Utils.currentBridgeModule().openPage(url = "InvoiceUpload", userData = data)
                                },
                                onResubmit = {
                                    SettingsStore.repo.resubmitInvoice(item.id)
                                        .onSuccess { Utils.currentBridgeModule().toast("已重新提交"); reload() }
                                        .onFailure { Utils.currentBridgeModule().toast(it.message ?: "提交失败") }
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        item { SettingsScenarioBar(scenarioText = scenarioText, onCycle = { reload() }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceCard(item: Invoice, onOpen: () -> Unit, onResubmit: () -> Unit) {
    val statusColor: Color = when (item.status) {
        InvoiceStatus.Approved -> KuiklyTheme.success
        InvoiceStatus.Rejected -> KuiklyTheme.error
        else -> KuiklyTheme.warningDeep
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onOpen() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.title, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            KChip(if (item.type == "company") "企业" else "个人", color = KuiklyTheme.violet)
            Spacer(Modifier.width(8.dp))
            KChip(item.statusLabel, color = statusColor)
        }
        Spacer(Modifier.height(8.dp))
        SettingsKeyValue(label = "发票金额", value = "¥${"%.2f".format(item.amount)}", bold = true)
        SettingsKeyValue(label = "提交时间", value = item.submittedAt)
        if (item.customerPhone.isNotEmpty()) {
            SettingsKeyValue(label = "客户", value = item.customerPhone)
        }
        if (item.taxNo.isNotEmpty()) {
            SettingsKeyValue(label = "税号", value = item.taxNo)
        }
        if (item.status == InvoiceStatus.Approved && item.ratingStars > 0) {
            SettingsKeyValue(label = "客户评分", value = "★".repeat(item.ratingStars))
        }
        if (item.status == InvoiceStatus.Rejected) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text("驳回原因：${item.rejectReason}", fontSize = 12.sp, color = KuiklyTheme.errorDeep)
            }
            Spacer(Modifier.height(10.dp))
            SettingsPrimaryButton(text = "重新提交", onClick = onResubmit)
        }
    }
}
