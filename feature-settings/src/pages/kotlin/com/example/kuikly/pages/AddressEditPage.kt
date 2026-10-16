package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.settings.Address
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

@Page("AddressDetail")
internal class AddressEditPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        val isEdit = id.isNotBlank()
        setContent {
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var province by remember { mutableStateOf("") }
            var city by remember { mutableStateOf("") }
            var district by remember { mutableStateOf("") }
            var detail by remember { mutableStateOf("") }
            var label by remember { mutableStateOf("") }
            var postalCode by remember { mutableStateOf("") }
            var isDefault by remember { mutableStateOf(false) }
            var focus by remember { mutableStateOf("") }
            var message by remember { mutableStateOf<String?>(null) }
            var missing by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            if (!booted) {
                booted = true
                if (isEdit) {
                    SettingsStore.repo.addressDetail(id)
                        .onSuccess { a ->
                            name = a.name
                            phone = a.phone
                            province = a.province
                            city = a.city
                            district = a.district
                            detail = a.detail
                            label = a.label
                            postalCode = a.postalCode
                            isDefault = a.isDefault
                        }
                        .onFailure { missing = true }
                }
            }

            fun save() {
                val target = Address(
                    id = id,
                    name = name.trim(),
                    phone = phone.trim(),
                    province = province,
                    city = city,
                    detail = detail.trim(),
                    isDefault = isDefault,
                    district = district,
                    postalCode = postalCode,
                    label = label,
                )
                val result = if (isEdit) {
                    SettingsStore.repo.updateAddress(target)
                } else {
                    SettingsStore.repo.addAddress(target)
                }
                result
                    .onSuccess {
                        Utils.currentBridgeModule().toast(if (isEdit) "地址已更新" else "地址已保存")
                        Utils.currentBridgeModule().closePage()
                    }
                    .onFailure { message = it.message ?: "保存失败" }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                SettingsNavBar(
                    title = if (isEdit) "编辑地址" else "新增地址",
                    subtitle = if (isEdit) "id=$id" else "保存后写入 Mock 地址簿",
                )

                if (missing) {
                    SettingsErrorState(message = "地址不存在或已删除", onRetry = { Utils.currentBridgeModule().closePage() })
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        SettingsSectionTitle("联系人")
                        SettingsFieldRow(
                            label = "收货人",
                            value = name,
                            hint = "点击输入",
                            active = focus == "name",
                            onClick = { focus = if (focus == "name") "" else "name" },
                        )
                        Spacer(Modifier.height(8.dp))
                        SettingsFieldRow(
                            label = "手机号",
                            value = phone,
                            hint = "11 位手机号",
                            active = focus == "phone",
                            onClick = { focus = if (focus == "phone") "" else "phone" },
                        )

                        Spacer(Modifier.height(12.dp))
                        SettingsSectionTitle("所在地区")
                        SettingsOptionChips(
                            options = listOf("北京市", "上海市", "广东省", "浙江省"),
                            selected = province,
                            onSelect = { province = it },
                        )
                        SettingsOptionChips(
                            options = listOf("北京市", "上海市", "广州市", "杭州市"),
                            selected = city,
                            onSelect = { city = it },
                        )
                        SettingsOptionChips(
                            options = listOf("朝阳区", "浦东新区", "天河区", "西湖区"),
                            selected = district,
                            onSelect = { district = it },
                        )

                        Spacer(Modifier.height(12.dp))
                        SettingsSectionTitle("详细地址")
                        SettingsFieldRow(
                            label = "门牌号",
                            value = detail,
                            hint = "例如 建国路88号",
                            active = focus == "detail",
                            onClick = { focus = if (focus == "detail") "" else "detail" },
                        )
                        Spacer(Modifier.height(8.dp))
                        SettingsOptionChips(
                            options = listOf("100022", "200120", "510620", "310012"),
                            selected = postalCode,
                            onSelect = { postalCode = it },
                        )

                        Spacer(Modifier.height(12.dp))
                        SettingsSectionTitle("标签")
                        SettingsOptionChips(
                            options = listOf("家", "公司", "学校", "其他"),
                            selected = label,
                            onSelect = { label = it },
                        )

                        Spacer(Modifier.height(8.dp))
                        SettingsDivider()
                        SettingsSwitchRow(label = "设为默认地址", desc = "下单时优先使用", checked = isDefault, onToggle = { isDefault = it })
                    }

                    Spacer(Modifier.height(12.dp))

                    when (focus) {
                        "name" -> SettingsTextInput(
                            candidates = listOf("张", "李", "王", "刘", "陈", "先生", "女士", "小明"),
                            onAppend = { name += it },
                            onBackspace = { if (name.isNotEmpty()) name = name.dropLast(1) },
                            onClear = { name = "" },
                        )
                        "phone" -> SettingsNumberKeypad(
                            onDigit = { if (phone.length < 11) phone += it },
                            onBackspace = { if (phone.isNotEmpty()) phone = phone.dropLast(1) },
                            onClear = { phone = "" },
                        )
                        "detail" -> SettingsTextInput(
                            candidates = listOf("建国路", "世纪大道", "天河路", "88号", "100号", "3号楼", "502室"),
                            onAppend = { detail += it },
                            onBackspace = { if (detail.isNotEmpty()) detail = detail.dropLast(1) },
                            onClear = { detail = "" },
                        )
                        else -> {
                            Box(modifier = Modifier.fillMaxWidth().height(0.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (message != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(message!!, fontSize = 13.sp, color = KuiklyTheme.errorDeep)
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    SettingsPrimaryButton(text = if (isEdit) "保存修改" else "保存地址", onClick = ::save)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("预览：", fontSize = 12.sp, color = KuiklyTheme.muted)
                        Text(
                            "$name ${if (phone.isEmpty()) "" else phone} " +
                                "${listOf(province, city, district, detail).filter { it.isNotBlank() }.joinToString(" ")}",
                            fontSize = 12.sp,
                            color = KuiklyTheme.body,
                        )
                    }

                    if (isEdit) {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(KuiklyTheme.errorSoft, RoundedCornerShape(KuiklyTheme.radiusMd))
                                .clickable {
                                    SettingsStore.repo.deleteAddress(id)
                                        .onSuccess {
                                            Utils.currentBridgeModule().toast("已删除地址")
                                            Utils.currentBridgeModule().closePage()
                                        }
                                        .onFailure { message = it.message ?: "删除失败" }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("删除该地址", fontSize = 14.sp, color = KuiklyTheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
