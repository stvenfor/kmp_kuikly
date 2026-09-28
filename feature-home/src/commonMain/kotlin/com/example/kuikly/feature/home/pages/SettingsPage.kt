package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
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
 * 「设置」— Flutter `SettingsPage` 复刻（P3-E3a 拍平）。
 *
 * 真源：`features/settings/lib/settings/view/settings_page.dart`
 *
 * 与 E2b2/E2b3 不同：本页采用 Flutter 的 **扁平 ListTile 形制**，而非 iOS Settings 分组卡片形制。
 * - Scaffold 灰底 `#F2F2F7` 全屏铺
 * - 行**全幅白底**（无圆角、无左右 16dp 间隙、无卡间 8dp 间距）
 * - 行内 `padding(horizontal = 16.dp, vertical = 12.dp)` —— Material ListTile 默认近似
 * - **无任何 RowDivider** —— Flutter 真源 `ListView` 行间不画分隔线（仅调试小节前一个
 *   `Divider(height: 24)` 占位 24dp 间距）；全幅白块合在一起视觉上糊一团是 Flutter 同样
 *   的取舍，不是我们改的余地
 * - 调试小节前纯 24dp 间距（Flutter `Divider(height: 24)` 的 0 高版，不画发丝线）
 * - 行序与文案照 Flutter：5 主行 + 开发调试标题 + 4 调试行
 * - **移除**「退出登录」与「版本 1.0.0」页脚（Flutter 没有；logout 留在「我的」顶栏，本页只做
 *   视觉 MAP，不必保可达性）
 * - env / 语言 picker 仍是 ModalBottomSheet
 *
 * ponytail 天花板（不弹回，供验收登记）：
 * - Kuikly 无主题/环境/国际化运行时 → 深色模式与语言仅本地 state（Flutter 会真改 `ThemeMode`/`Locale`）；
 * - 「蓝牙连接示例」「新车成交示例」+ 四个调试入口为 out of scope，沿用 toast 口径；
 * - 行间不放 `RoundedCornerShape(12.dp)` 卡壳、不放 16dp 侧沟、不放 `Arrangement.spacedBy` —— 这些
 *   是 E2b2/E2b3 iOS 分组形制产物，与 Flutter 平铺 `ListView` 不符，是 RMSE≈0.238 的主因。
 */
@Page(name = "Settings", moduleId = "feature_home")
internal class SettingsPage : BaseComposePager() {

    private companion object {
        /** Flutter `AppEnv` 三档 label + `EnvConfig.backendBaseUrl`。 */
        val ENVS = listOf(
            "测试" to "http://${LanHost.fallback}:8080",
            "预发" to "http://${LanHost.fallback}:8080",
            "线上" to "https://api.xiaomaomain.com",
        )
        val LANGUAGES = listOf("简体中文", "English")
    }

    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var envIndex by remember { mutableStateOf(0) }
            var darkMode by remember { mutableStateOf(false) }
            var languageIndex by remember { mutableStateOf(0) }
            var sheet by remember { mutableStateOf(0) } // 0 = 无，1 = 运行环境，2 = 语言

            fun unsupported(label: String) {
                Utils.currentBridgeModule().toast("「$label」即将接入")
            }

            Box(modifier = Modifier.fillMaxSize().background(AppChrome.background)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AppNavBarBar(
                        title = "设置",
                        topInset = top,
                        onBack = { Utils.currentBridgeModule().closePage() },
                    )
                    // Flutter `ListView(children: [...])`：行**全幅**铺到屏幕边，无左右 16dp 侧沟，
                    // 无 `Arrangement.spacedBy` 卡间隙，**且行间不画分隔线**（仅底部安全区 + 24dp 额外呼吸）。
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = bottom.dp + 24.dp),
                    ) {
                        // 主设置组：5 行全幅白底；行间不画发丝线，对齐 Flutter 真源（Flutter 自己
                        // 行间也没分隔，全幅白块合在一起视觉糊一团是与 Flutter 共享的取舍）。
                        item {
                            SettingRow(
                                title = "运行环境",
                                subtitle = "${ENVS[envIndex].first} · ${ENVS[envIndex].second}",
                                onClick = { sheet = 1 },
                            )
                            SwitchRow(
                                title = "深色模式",
                                subtitle = "切换浅色 / 深色主题",
                                checked = darkMode,
                                onCheckedChange = { darkMode = it },
                            )
                            SettingRow(
                                title = "语言",
                                subtitle = LANGUAGES[languageIndex],
                                onClick = { sheet = 2 },
                            )
                            SettingRow(
                                title = "蓝牙连接示例",
                                subtitle = "BLE 扫描、连接、服务发现",
                                onClick = { unsupported("蓝牙连接示例") },
                            )
                            SettingRow(
                                title = "新车成交示例",
                                subtitle = "悬浮 Tab、下拉刷新、上拉加载更多",
                                onClick = { unsupported("新车成交示例") },
                            )
                        }

                        // Flutter `Divider(height: 24)` —— 0 高、24dp 占位，不画发丝线。
                        item { Spacer(Modifier.height(24.dp)) }

                        // Flutter `Padding(fromLTRB(16, 8, 16, 4))` + 13·w600·`grey.shade600` 小节标题。
                        item {
                            Text(
                                "开发调试",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF757575),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }

                        // 调试小节：4 行全幅白底，行间同样不画发丝线。
                        item {
                            SettingRow(
                                title = "弹框调度示例",
                                subtitle = "样式、优先级队列、清空/取消待展示",
                                onClick = { unsupported("弹框调度示例") },
                            )
                            SettingRow(
                                title = "链接与推送调试",
                                subtitle = "Mock Deeplink / 前台 Push Banner",
                                onClick = { unsupported("链接与推送调试") },
                            )
                            SettingRow(
                                title = "Realtime / WebSocket 调试",
                                subtitle = "连接状态、Mock 信令、离线队列",
                                onClick = { unsupported("Realtime / WebSocket 调试") },
                            )
                            SettingRow(
                                title = "融云 IM 调试",
                                subtitle = "imUserId、连接态、备份队列",
                                onClick = { unsupported("融云 IM 调试") },
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

/** Flutter `ListTile(title, subtitle, trailing: chevron_right_rounded, onTap)` —— 全幅白底。 */
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

/** Flutter `SwitchListTile(title, subtitle, value, onChanged)` —— 全幅白底，无 chevron。 */
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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