package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.settings.Address
import com.example.kuikly.data.settings.SettingsStore
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("Settings")
internal class SettingsPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var scenarioText by remember { mutableStateOf(MockBackend.scenario.name) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenarioText = MockBackend.scenario.name
                SettingsStore.repo.page(1, 50)
                    .onSuccess { addresses = it.items; error = null }
                    .onFailure { addresses = emptyList(); error = it.message ?: "加载失败" }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Text(
                    "设置",
                    fontSize = 22.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    SettingsEntryRow(title = "收货地址", subtitle = "管理地址簿，支持增删改查", page = "AddressList")
                    SettingsDivider()
                    SettingsEntryRow(title = "个性化设置", subtitle = "推送、推荐、护眼模式", page = "PersonalizedSettings")
                    SettingsDivider()
                    SettingsEntryRow(title = "发票管理", subtitle = "成交发票审核与重新提交", page = "InvoiceList")
                    SettingsDivider()
                    SettingsEntryRow(title = "购车计算器", subtitle = "全款 / 贷款 报价试算", page = "PurchaseCalc")
                }

                Spacer(Modifier.height(12.dp))

                // 采购计算器演示（沿用旧逻辑）
                val quote = SettingsStore.repo.quote(qty = 10, unitPrice = 99.0, discount = 0.1, freight = 12.0)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                        .background(KuiklyTheme.canvasSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(12.dp),
                ) {
                    Text("采购计算器", fontSize = 15.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
                    Text(
                        "数量 10 × 单价 ¥99 × (1-0.1折扣) + 运费 ¥12 = ¥${"%.2f".format(quote.total)}",
                        fontSize = 13.sp,
                        color = KuiklyTheme.body,
                    )
                }

                Spacer(Modifier.height(12.dp))

                SettingsSectionTitle("默认地址", "共 ${addresses.size} 个")

                if (error != null) {
                    SettingsErrorState(message = error!!, onRetry = ::reload)
                } else if (addresses.isEmpty()) {
                    SettingsEmptyState(
                        text = "暂无地址",
                        actionText = "+ 新增地址",
                        onAction = { Utils.currentBridgeModule().openPage("AddressDetail") },
                    )
                } else {
                    addresses.forEach { addr ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)
                                .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                                .clickable {
                                    val data = JSONObject()
                                    data.put("id", addr.id)
                                    Utils.currentBridgeModule().openPage(url = "AddressDetail", userData = data)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.width(240.dp)) {
                                Text("${addr.name}  ${addr.phone}", fontSize = 15.sp, color = KuiklyTheme.foreground)
                                Text(addr.fullAddress, fontSize = 12.sp, color = KuiklyTheme.muted)
                            }
                            if (addr.isDefault) KChip("默认", color = KuiklyTheme.success)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                SettingsScenarioBar(scenarioText = scenarioText, onCycle = { reload() })
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsEntryRow(title: String, subtitle: String, page: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { Utils.currentBridgeModule().openPage(page) }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(260.dp)) {
            Text(title, fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = KuiklyTheme.muted)
        }
        Text("›", fontSize = 20.sp, color = KuiklyTheme.muted)
    }
}
