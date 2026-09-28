package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
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
 * 「发布动态」— Flutter `PublishPage`（`features/community/lib/community/view/publish_page.dart`）
 * 复刻（Phase-2 / P2-W3；P2-V8e 视觉打磨）。
 *
 * Flutter 侧本页**本身就是 stub**：`AppNavBar(title: '发布动态', leading: IconButton(Icons.close))` +
 * `Center(Column(min))`：`Icon(edit_note, 64, grey.shade400)` + 16 间隙 +
 * 「发布动态功能开发中」(16 · grey.shade600) + 8 间隙 + 「后续可接入发帖接口」(13 · grey.shade500)。
 * Kuikly 逐行镜像，**不做超出 Flutter 真源的富文本编辑器**（ponytail：不发明需求）。
 *
 * **刻度**：`publish_page.dart` 全部为**裸逻辑 px**（无 `.w/.h/.sp`）→ 一律裸 `dp`/`sp`。
 *
 * P2-V8e：Flutter 的 NavBar **显式覆盖了 leading** —— `AppNavBar._buildLeading` 里
 * `leading != null` 直接短路返回，故 `showBackButton: true` 实际不生效，屏上是
 * `IconButton(Icons.close)`（✕ · M3 默认 24 图标 / 40×40 盒）而非 `arrow_back_ios_new`。
 * 全仓仅此一页是非 `‹` 的 AppNavBar leading，共享 [AppNavBarBar] 不提供 leading 槽位，
 * 故在锁内自绘同形工具栏（topInset / 56 高 / 17·w600 居中标题 / 0.5 发丝线全部沿用
 * [AppChrome] 令牌，尺寸语义与 [AppNavBarBar] 逐字节一致，只换左字形）——
 * 与 `MusicListPage` 迷你播放条（同样源出 Flutter `Icons.close`）同款 `✕` 字形口径。
 *
 * ponytail 天花板：
 * - `Icons.edit_note`（带笔便签）无 icon font/矢量资源，用文本呈现的 `✎`(U+270E) 近似，
 *   可被 `grey.shade400` 染色（emoji 变体如 📝 会脱离染色体系）；字形非像素级一致。
 * - `Icon(size: 64)` 是盒高，`Text(fontSize = 64.sp)` 行盒更高、且随系统字体缩放，
 *   与其它 V 批同口径记为天花板。
 * - `Icons.close` 属 M3 IconButton，取色为 `colorScheme.onSurfaceVariant`；此处沿用
 *   [AppChrome.labelPrimary]（与标题同色），未引入无法在 Kuikly 侧复算的 M3 派生色。
 */
@Page(name = "CommunityPublish", moduleId = "feature_home")
internal class CommunityPublishPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppChrome.background),
            ) {
                PublishNavBar(
                    topInset = top,
                    onClose = { Utils.currentBridgeModule().closePage() },
                )
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("✎", fontSize = 64.sp, color = Color(0xFFBDBDBD))
                    Spacer(Modifier.height(16.dp))
                    Text("发布动态功能开发中", fontSize = 16.sp, color = Color(0xFF757575))
                    Spacer(Modifier.height(8.dp))
                    Text("后续可接入发帖接口", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                }
                Spacer(Modifier.height(bottomSafeInset().dp))
            }
        }
    }
}

/**
 * Flutter `AppNavBarStyle.solid` + `leading: IconButton(Icons.close)` 的本页专用工具栏。
 * 形制（占位/高度/标题/发丝线）与共享 [AppNavBarBar] 相同，仅左字形为关闭 ✕。
 */
@Composable
private fun PublishNavBar(topInset: Float, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppChrome.surface),
    ) {
        Spacer(Modifier.height(topInset.dp))
        Box(Modifier.fillMaxWidth().height(AppChrome.NAV_BAR_HEIGHT.dp)) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "✕",
                    fontSize = AppChrome.BACK_SIZE.sp,
                    color = AppChrome.labelPrimary,
                    modifier = Modifier
                        .clickable(onClick = onClose)
                        .padding(AppChrome.ICON_TAP_PADDING.dp),
                )
                Spacer(Modifier.weight(1f))
            }
            Box(Modifier.align(Alignment.Center)) {
                Text(
                    "发布动态",
                    fontSize = AppChrome.TITLE_SIZE.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppChrome.labelPrimary,
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppChrome.HAIRLINE.dp)
                .background(AppChrome.hairline),
        )
    }
}
