package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.settings.PersonalizedSetting
import com.example.kuikly.data.settings.SettingKind
import com.example.kuikly.data.settings.SettingsStore
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
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("PersonalizedSettings")
internal class PersonalizedSettingsPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var items by remember { mutableStateOf<List<PersonalizedSetting>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var message by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                SettingsStore.repo.personalized()
                    .onSuccess { items = it; error = null }
                    .onFailure { items = emptyList(); error = it.message ?: "加载设置失败" }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                SettingsNavBar(title = "个性化设置", subtitle = "开关与偏好会写入 Mock 存储")

                if (message != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                            .background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(message!!, fontSize = 13.sp, color = KuiklyTheme.linkDeep)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                when {
                    error != null -> SettingsErrorState(message = error!!, onRetry = ::reload)
                    items.isEmpty() -> SettingsEmptyState(text = "暂无可配置项", actionText = "重新加载", onAction = ::reload)
                    else -> Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        items.forEach { item ->
                            if (item.kind == SettingKind.Option) {
                                OptionSettingRow(
                                    item = item,
                                    onSelect = { value ->
                                        SettingsStore.repo.setPersonalizedValue(item.key, value)
                                            .onSuccess { message = "${item.label} 已设为 $value"; reload() }
                                            .onFailure { message = it.message ?: "设置失败" }
                                    },
                                )
                            } else {
                                SettingsSwitchRow(
                                    label = item.label,
                                    desc = item.desc,
                                    checked = item.enabled,
                                    onToggle = { next ->
                                        SettingsStore.repo.setPersonalized(item.key, next)
                                            .onSuccess { message = "${item.label} 已${if (next) "开启" else "关闭"}"; reload() }
                                            .onFailure { message = it.message ?: "设置失败" }
                                    },
                                )
                            }
                            SettingsDivider()
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                "装扮中心（开发中）",
                                fontSize = 13.sp,
                                color = KuiklyTheme.link,
                                modifier = Modifier.clickable { Utils.currentBridgeModule().toast("装扮中心（开发中）") },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionSettingRow(item: PersonalizedSetting, onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(240.dp)) {
                Text(item.label, fontSize = 15.sp, color = KuiklyTheme.foreground)
                if (item.desc.isNotEmpty()) {
                    Text(item.desc, fontSize = 12.sp, color = KuiklyTheme.muted)
                }
            }
            Text(item.value, fontSize = 13.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        SettingsOptionChips(options = item.options, selected = item.value, onSelect = onSelect)
    }
}
