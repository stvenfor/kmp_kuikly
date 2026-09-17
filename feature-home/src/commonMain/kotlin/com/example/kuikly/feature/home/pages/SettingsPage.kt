package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.navigation.MainTabLaunch
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.material3.ModalBottomSheet
import com.tencent.kuikly.compose.material3.Switch
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 「设置」— Flutter `SettingsPage` 复刻（Phase-2 / P2-W3）。
 *
 * 真源：`features/settings/lib/settings/view/settings_page.dart` +
 * `viewmodel/settings_viewmodel.dart` + `commons/core/lib/env/env_config.dart`（AppEnv 三档地址）。
 *
 * 行序与文案照 Flutter：运行环境 / 深色模式(switch) / 语言 / 蓝牙连接示例 / 新车成交示例
 * → `Divider(height: 24)` + 「开发调试」小节（弹框调度、链接与推送、Realtime+WebSocket、融云 IM）。
 *
 * **刻度**：Flutter `settings_page` 走 `flutter_screenutil`，但**该页全部尺寸是 Material 默认值**
 * （`ListTile`/`Divider`/`TextStyle(fontSize: 13)` 均**无** `.w/.h/.sp` 命中）；
 * 唯一显式数值 `EdgeInsets.fromLTRB(16, 8, 16, 4)` 也是裸 px → 故此处一律裸 `dp`/`sp`，
 * **不引入** `ProvideDesignScale`（与 P2-03 / W2b / W2c 判断法一致，避免 ×1.463 破坏 parity）。
 *
 * ponytail 天花板（不弹回，供验收登记）：
 * - Kuikly 无主题/环境/国际化运行时 → 深色模式与语言仅本地 state（Flutter 会真改 `ThemeMode`/`Locale`）；
 *   运行环境选择器与「设置」之外的路由（蓝牙/新车成交/四个调试页）为 **out of scope**，沿用 toast 口径。
 * - 「退出登录」是 Kuikly 侧既有入口（Flutter 把登录/登出放在「我的」顶栏），本页保留以免丢可达性。
 * - 底部选择器用 `ModalBottomSheet`（Kuikly material3 已有），形制对齐 Flutter `showModalBottomSheet`。
 */
@Page(name = "Settings", moduleId = "feature_home")
internal class SettingsPage : BaseComposePager() {

    private companion object {
        /** Flutter `AppEnv` 三档 label + `EnvConfig.backendBaseUrl`。 */
        val ENVS = listOf(
            "测试" to "http://127.0.0.1:8080",
            "预发" to "http://127.0.0.1:8080",
            "线上" to "https://api.xiaomaomain.com",
        )
        val LANGUAGES = listOf("简体中文", "English")
    }

    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val isLoggedIn = AuthSession.repo.isLoggedIn()
            var envIndex by remember { mutableStateOf(0) }
            var darkMode by remember { mutableStateOf(false) }
            var languageIndex by remember { mutableStateOf(0) }
            var sheet by remember { mutableStateOf(0) } // 0 = 无，1 = 运行环境，2 = 语言

            fun unsupported(label: String) {
                Utils.currentBridgeModule().toast("「$label」即将接入")
            }

            fun logout() {
                AuthSession.repo.logout()
                MainTabLaunch.requestMe()
                Utils.currentBridgeModule().openPage(PageNames.Main, closeCurPage = true)
            }

            Box(modifier = Modifier.fillMaxSize().background(AppChrome.background)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AppNavBarBar(
                        title = "设置",
                        topInset = top,
                        onBack = { Utils.currentBridgeModule().closePage() },
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = bottom.dp + 24.dp),
                    ) {
                        item {
                            SettingRow(
                                title = "运行环境",
                                subtitle = "${ENVS[envIndex].first} · ${ENVS[envIndex].second}",
                                onClick = { sheet = 1 },
                            )
                        }
                        item {
                            SwitchRow(
                                title = "深色模式",
                                subtitle = "切换浅色 / 深色主题",
                                checked = darkMode,
                                onCheckedChange = { darkMode = it },
                            )
                        }
                        item {
                            SettingRow(
                                title = "语言",
                                subtitle = LANGUAGES[languageIndex],
                                onClick = { sheet = 2 },
                            )
                        }
                        // P2-W4a：接入 inventory 行 29 / 37 的真实路由（PersonalizedSettings /
                        // DealInvoiceUpload），chrome 与文案不动，仅加两行入口。
                        item {
                            SettingRow(
                                title = "个性化设置",
                                subtitle = "护眼 / 教学模式 / 推荐开关",
                                onClick = {
                                    Utils.currentBridgeModule().openPage(PageNames.PersonalizedSettings)
                                },
                            )
                        }
                        item {
                            SettingRow(
                                title = "上传发票",
                                subtitle = "购车客户 · 发票图片 · 审核",
                                onClick = {
                                    Utils.currentBridgeModule().openPage(PageNames.DealInvoiceUpload)
                                },
                            )
                        }
                        item {
                            SettingRow(
                                title = "蓝牙连接示例",
                                subtitle = "BLE 扫描、连接、服务发现",
                                onClick = { unsupported("蓝牙连接示例") },
                            )
                        }
                        item {
                            SettingRow(
                                title = "新车成交示例",
                                subtitle = "悬浮 Tab、下拉刷新、上拉加载更多",
                                onClick = { unsupported("新车成交示例") },
                            )
                        }

                        // Flutter `if (kDebugMode) ... const Divider(height: 24)` + 「开发调试」小节。
                        item { Spacer(Modifier.height(12.dp)) }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppChrome.separator),
                            )
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                        item { DebugSectionHeader("开发调试") }
                        item {
                            SettingRow(
                                title = "弹框调度示例",
                                subtitle = "样式、优先级队列、清空/取消待展示",
                                onClick = { unsupported("弹框调度示例") },
                            )
                        }
                        item {
                            SettingRow(
                                title = "链接与推送调试",
                                subtitle = "Mock Deeplink / 前台 Push Banner",
                                onClick = { unsupported("链接与推送调试") },
                            )
                        }
                        item {
                            SettingRow(
                                title = "Realtime / WebSocket 调试",
                                subtitle = "连接状态、Mock 信令、离线队列",
                                onClick = { unsupported("Realtime / WebSocket 调试") },
                            )
                        }
                        item {
                            SettingRow(
                                title = "融云 IM 调试",
                                subtitle = "imUserId、连接态、备份队列",
                                onClick = { unsupported("融云 IM 调试") },
                            )
                        }

                        // Kuikly 侧既有入口（Flutter 把登出放在「我的」顶栏）——保留可达性。
                        if (isLoggedIn) {
                            item { Spacer(Modifier.height(12.dp)) }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(AppChrome.separator),
                                )
                            }
                            item {
                                SettingRow(
                                    title = "退出登录",
                                    subtitle = null,
                                    titleColor = Color(0xFFFF3B30),
                                    onClick = { logout() },
                                )
                            }
                        }
                        item {
                            Text(
                                "版本 1.0.0 (demo)",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                                modifier = Modifier.padding(start = 16.dp, top = 24.dp),
                            )
                        }
                    }
                }

                ModalBottomSheet(
                    visible = sheet != 0,
                    onDismissRequest = { sheet = 0 },
                    containerColor = AppChrome.surface,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (sheet == 1) {
                            ENVS.forEachIndexed { index, env ->
                                PickerRow(
                                    title = env.first,
                                    subtitle = env.second,
                                    selected = index == envIndex,
                                    onClick = {
                                        envIndex = index
                                        sheet = 0
                                    },
                                )
                            }
                        } else if (sheet == 2) {
                            LANGUAGES.forEachIndexed { index, label ->
                                PickerRow(
                                    title = label,
                                    subtitle = null,
                                    selected = index == languageIndex,
                                    onClick = {
                                        languageIndex = index
                                        sheet = 0
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(bottom.dp + 8.dp))
                    }
                }
            }
        }
    }
}

/** Flutter `ListTile(title, subtitle, trailing: chevron_right_rounded, onTap)`。 */
@Composable
private fun SettingRow(
    title: String,
    subtitle: String?,
    titleColor: Color = AppChrome.labelPrimary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = titleColor)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    fontSize = 14.sp,
                    color = AppChrome.labelSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        // Flutter `Icon(Icons.chevron_right_rounded, size: 24)` 的字形近似。
        Text("›", fontSize = 24.sp, color = Color(0xFF8E8E93))
    }
}

/** Flutter `SwitchListTile(title, subtitle, value, onChanged)`。 */
@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = AppChrome.labelPrimary)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 14.sp, color = AppChrome.labelSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Flutter `Padding(fromLTRB(16, 8, 16, 4))` + 13·w600·`grey.shade600` 小节标题。 */
@Composable
private fun DebugSectionHeader(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF757575),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}

/** Flutter `showModalBottomSheet` 里的 `ListTile`（选中项 trailing 蓝色 ✓，无 subtitle 时单行）。 */
@Composable
private fun PickerRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = AppChrome.labelPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 14.sp, color = AppChrome.labelSecondary)
            }
        }
        if (selected) {
            // Flutter `Icon(Icons.check_rounded, color: Colors.blue)`。
            Text("✓", fontSize = 20.sp, color = AppChrome.accent)
        }
    }
}
