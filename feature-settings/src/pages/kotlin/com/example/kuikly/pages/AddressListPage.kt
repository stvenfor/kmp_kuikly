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
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("AddressList")
internal class AddressListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var items by remember { mutableStateOf<List<Address>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var scenarioText by remember { mutableStateOf(MockBackend.scenario.name) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenarioText = MockBackend.scenario.name
                SettingsStore.repo.page(1, 50)
                    .onSuccess { r ->
                        items = r.items
                        error = null
                    }
                    .onFailure {
                        items = emptyList()
                        error = it.message ?: "加载地址失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                SettingsNavBar(title = "收货地址", subtitle = "共 ${items.size} 个地址")

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("地址簿", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    KChip("Mock CRUD", color = KuiklyTheme.link)
                    Spacer(Modifier.width(48.dp))
                    Box(
                        modifier = Modifier
                            .background(KuiklyTheme.link, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .clickable { Utils.currentBridgeModule().openPage("AddressDetail") }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+ 新增地址", fontSize = 13.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                when {
                    error != null && items.isEmpty() -> SettingsErrorState(message = error!!, onRetry = ::reload)
                    items.isEmpty() -> SettingsEmptyState(
                        text = "还没有收货地址",
                        actionText = "+ 新增地址",
                        onAction = { Utils.currentBridgeModule().openPage("AddressDetail") },
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            AddressCard(
                                item = item,
                                onSetDefault = {
                                    SettingsStore.repo.setDefaultAddress(item.id)
                                        .onSuccess { Utils.currentBridgeModule().toast("已设为默认地址"); reload() }
                                        .onFailure { Utils.currentBridgeModule().toast("设置默认失败") }
                                },
                                onEdit = {
                                    val data = JSONObject()
                                    data.put("id", item.id)
                                    Utils.currentBridgeModule().openPage(url = "AddressDetail", userData = data)
                                },
                                onDelete = {
                                    SettingsStore.repo.deleteAddress(item.id)
                                        .onSuccess { Utils.currentBridgeModule().toast("已删除地址"); reload() }
                                        .onFailure { Utils.currentBridgeModule().toast("删除失败") }
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
private fun AddressCard(
    item: Address,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.name, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(item.phone, fontSize = 14.sp, color = KuiklyTheme.body)
            Spacer(Modifier.width(8.dp))
            if (item.label.isNotEmpty()) KChip(item.label, color = KuiklyTheme.violet)
            Spacer(Modifier.width(8.dp))
            if (item.isDefault) KChip("默认", color = KuiklyTheme.success)
        }
        Spacer(Modifier.height(8.dp))
        Text(item.fullAddress, fontSize = 13.sp, color = KuiklyTheme.muted)
        if (item.postalCode.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("邮编 ${item.postalCode}", fontSize = 12.sp, color = KuiklyTheme.muted)
        }
        Spacer(Modifier.height(10.dp))
        SettingsDivider()
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            SettingsLinkAction(text = if (item.isDefault) "已默认" else "设为默认", enabled = !item.isDefault, onClick = onSetDefault)
            Spacer(Modifier.width(20.dp))
            SettingsLinkAction(text = "编辑", onClick = onEdit)
            Spacer(Modifier.width(20.dp))
            SettingsLinkAction(text = "删除", color = KuiklyTheme.error, onClick = onDelete)
        }
    }
}

@Composable
private fun SettingsLinkAction(
    text: String,
    color: com.tencent.kuikly.compose.ui.graphics.Color = KuiklyTheme.link,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Text(
        text,
        fontSize = 13.sp,
        color = if (enabled) color else KuiklyTheme.muted,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.clickable { if (enabled) onClick() },
    )
}
