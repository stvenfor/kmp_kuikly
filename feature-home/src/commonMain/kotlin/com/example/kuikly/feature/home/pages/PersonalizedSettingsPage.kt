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
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Switch
import com.tencent.kuikly.compose.material3.SwitchDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 个性化设置 — Flutter `PersonalizedSettingsPage`
 * （`features/settings/lib/mine/personalized_settings/view/personalized_settings_page.dart`）
 * 复刻（P2-W4a 接入路由 `personalizedSettings`）。
 *
 * **真源**：白卡 r12 × 3 + 小节标题（`EdgeInsets.fromLTRB(4, 20, 4, 8)` 13·灰） +
 * 装扮中心 nav / 护眼模式 nav + help / 教学模式 switch + help / 5 开关（个性化内容推荐 /
 * 广告 / 口语评分 / 流量播放提醒 / 上传监控）。每行 `minHeight: 52`，开关用
 * `CupertinoSwitch(activeTrackColor: AppTheme.seedColor)`。
 *
 * **刻度**：本 Flutter view **grep 无 `.w/.h/.sp`**（仅 0xFFF5F5F5 调色板 + 裸 px），
 * 故一律裸 `dp`/`sp`，不加 `ProvideDesignScale`。
 *
 * ponytail 天花板（不弹回）：
 * - `AppTheme.seedColor`（Flutter 侧注册色）→ 取 Material3 默认绿（`#34C759`）。
 * - 「护眼模式」picker / 「装扮中心」跳转 / `showHelp` 弹窗 → 全部 toast。
 * - `_HelpButton` / `_ChevronIcon` 是 PNG 资源 → `?` / `›` Unicode 字形近似。
 */
@Page(name = "PersonalizedSettings", moduleId = "feature_home")
internal class PersonalizedSettingsPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            // 护眼模式状态（picker 值；Kuikly 不接 picker，用本地 state 模拟）。
            var eyeMode by remember { mutableStateOf("跟随系统") }
            var teaching by remember { mutableStateOf(false) }
            var contentRec by remember { mutableStateOf(true) }
            var adRec by remember { mutableStateOf(false) }
            var oralScoring by remember { mutableStateOf(true) }
            var cellularVideo by remember { mutableStateOf(false) }
            var uploadMonitor by remember { mutableStateOf(true) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PersonalizedPalette.pageBackground),
            ) {
                AppNavBarBar(
                    title = "个性化设置",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = PersonalizedPalette.pageBackground,
                    foreground = AppChrome.labelPrimary,
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = (bottom + 24f).dp,
                    ),
                ) {
                    item {
                        SettingsCard {
                            NavTile(
                                title = "装扮中心",
                                onTap = { Utils.currentBridgeModule().toast("「装扮中心」即将接入") },
                            )
                        }
                    }
                    item { SectionHeader("模式选择") }
                    item {
                        SettingsCard {
                            NavTile(
                                title = "护眼模式",
                                showHelp = true,
                                onHelpTap = { Utils.currentBridgeModule().toast("护眼模式 帮助") },
                                trailingText = eyeMode,
                                onTap = {
                                    eyeMode = nextEyeMode(eyeMode)
                                    Utils.currentBridgeModule().toast("护眼模式已切换为 $eyeMode")
                                },
                            )
                            RowDivider()
                            SwitchTile(
                                title = "教学模式",
                                showHelp = true,
                                onHelpTap = { Utils.currentBridgeModule().toast("教学模式 帮助") },
                                value = teaching,
                                onChanged = { teaching = it },
                            )
                        }
                    }
                    item { SectionHeader("个性化设置") }
                    item {
                        SettingsCard {
                            SwitchTile(
                                title = "个性化内容推荐",
                                showHelp = true,
                                onHelpTap = { Utils.currentBridgeModule().toast("个性化内容推荐 帮助") },
                                value = contentRec,
                                onChanged = { contentRec = it },
                            )
                            RowDivider()
                            SwitchTile(
                                title = "个性化广告推荐",
                                showHelp = true,
                                onHelpTap = { Utils.currentBridgeModule().toast("个性化广告推荐 帮助") },
                                value = adRec,
                                onChanged = { adRec = it },
                            )
                            RowDivider()
                            SwitchTile(
                                title = "口语评分",
                                value = oralScoring,
                                onChanged = { oralScoring = it },
                            )
                            RowDivider()
                            SwitchTile(
                                title = "2/3/4/5G 流量播放视频时提醒我",
                                value = cellularVideo,
                                onChanged = { cellularVideo = it },
                            )
                            RowDivider()
                            SwitchTile(
                                title = "作品上传状态监控",
                                value = uploadMonitor,
                                onChanged = { uploadMonitor = it },
                            )
                        }
                    }
                }
            }
        }
    }
}

private object PersonalizedPalette {
    val pageBackground = Color(0xFFF5F5F5)
    val titleColor = Color(0xFF333333)
    val sectionColor = Color(0xFF999999)
    val valueColor = Color(0xFF666666)
    val dividerColor = Color(0xFFEEEEEE)
    val switchTrack = Color(0xFF34C759) // AppTheme.seedColor 近似
}

/** Flutter `_SectionHeader`：`Padding(fromLTRB(4, 20, 4, 8))` + 13 灰。 */
@Composable
private fun SectionHeader(label: String) {
    Text(
        label,
        fontSize = 13.sp,
        color = PersonalizedPalette.sectionColor,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, end = 4.dp, bottom = 8.dp),
    )
}

/** Flutter `_SettingsCard`：白 r12。 */
@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp)),
    ) {
        content()
    }
}

/** Flutter `_RowDivider`：1 高缩进 16 divider（`Modifier.padding(start=16).background(c)` 让
 *  背景仅在 inner area 上色，对应 Flutter `Divider(indent: 16)`）。 */
@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 16.dp)
            .background(PersonalizedPalette.dividerColor),
    )
}

/**
 * Flutter `_NavTile`：`Material + InkWell + ConstrainedBox(minHeight: 52)` +
 * `Padding(symmetric horizontal: 16)` + 行（标题 + 可选 help + trailingText + chevron）。
 */
@Composable
private fun NavTile(
    title: String,
    onTap: () -> Unit,
    showHelp: Boolean = false,
    onHelpTap: (() -> Unit)? = null,
    trailingText: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TitleRow(title = title, showHelp = showHelp, onHelpTap = onHelpTap)
        if (trailingText != null) {
            Spacer(Modifier.weight(1f))
            Text(
                trailingText,
                fontSize = 15.sp,
                color = PersonalizedPalette.valueColor,
            )
            Spacer(Modifier.width(4.dp))
        }
        Text("›", fontSize = 16.sp, color = PersonalizedPalette.valueColor)
    }
}

/** Flutter `_SwitchTile`：`ConstrainedBox(minHeight: 52)` + `Padding(fromLTRB(16, 8, 12, 8))` + 标题 + help + Switch。 */
@Composable
private fun SwitchTile(
    title: String,
    value: Boolean,
    onChanged: (Boolean) -> Unit,
    showHelp: Boolean = false,
    onHelpTap: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TitleRow(title = title, showHelp = showHelp, onHelpTap = onHelpTap)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = value,
            onCheckedChange = onChanged,
            // Flutter `CupertinoSwitch(activeTrackColor: AppTheme.seedColor)`。
            colors = SwitchDefaults.colors(
                checkedTrackColor = PersonalizedPalette.switchTrack,
            ),
        )
    }
}

@Composable
private fun TitleRow(title: String, showHelp: Boolean, onHelpTap: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            fontSize = 16.sp,
            color = PersonalizedPalette.titleColor,
            maxLines = 2,
        )
        if (showHelp) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onHelpTap?.invoke() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PersonalizedPalette.valueColor,
                )
            }
        }
    }
}

/**
 * 护眼模式 picker 模拟：跟随系统 → 关闭 → 开启 → 跟随系统。Kuikly 侧不接 picker widget，
 * 直接循环三档触发 toast。
 */
private fun nextEyeMode(current: String): String = when (current) {
    "跟随系统" -> "关闭"
    "关闭" -> "开启"
    else -> "跟随系统"
}