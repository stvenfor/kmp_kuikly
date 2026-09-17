package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
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
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * Flutter `AppNavBar`（`commons/ui/lib/layout/app_nav_bar.dart`）+ `AppTheme`
 * （`commons/ui/lib/theme/app_theme.dart`）的跨页共享令牌镜像（P2-W2c 六个域共用）。
 *
 * **刻度**：Flutter `AppNavBar` 全用**裸逻辑 px**（`AppSafeInsets.toolbarHeight` = `kToolbarHeight`
 * = 56、标题 17 / w600、返回图标 20、`dividerColor.withValues(alpha: 0.08)`），
 * 且本批六个 Flutter 模块（music / video-dubbing / friend / classroom / live / pay）
 * 的 view+theme 目录 **grep 无 `.w/.h/.sp` screenutil 扩展**（唯一例外 `video/lib/short_video/`
 * 不在本 ticket 范围）。故一律裸 `dp`/`sp`，**不要** `.su()`——判断法与 P2-03 `TabBarTokens`、
 * P2-W2b `ChatPalette` 相同（加 su 会 ×1.463 破坏 parity）。
 */
internal object AppChrome {
    /** Flutter `kToolbarHeight`。 */
    const val NAV_BAR_HEIGHT = 56f
    const val TITLE_SIZE = 17f

    /** Flutter `Icon(Icons.arrow_back_ios_new, size: 20)` 的 Unicode 字形近似（无矢量资源）。 */
    const val BACK_SIZE = 22f

    /** IconButton 48×48 触达区（22 字形 + 13×2 padding）。 */
    const val ICON_TAP_PADDING = 13f

    const val HAIRLINE = 0.5f

    /** Flutter `AppTheme`：`accent` / `background` / `surface` / 标签色 / `separator`。 */
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val separator = Color(0xFFC6C6C8)

    /** Flutter `AppNavBar` 底边 `dividerColor.withValues(alpha: 0.08)`。 */
    val hairline = Color(0x14C6C6C8)
}

/**
 * Flutter `AppNavBar`（`AppNavBarStyle.solid` / `centerTitle: true`）的等价物：
 * 状态栏占位 + 56 高工具栏（左返回 / 居中标题 17·w600 / 右侧 actions）+ 0.5 底边。
 *
 * ponytail: 无 icon font/矢量资源，返回箭头用 `‹` 字形（同 P2-W2b 天花板口径）。
 */
@Composable
internal fun AppNavBarBar(
    title: String,
    topInset: Float,
    onBack: () -> Unit,
    background: Color = AppChrome.surface,
    foreground: Color = AppChrome.labelPrimary,
    actions: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background),
    ) {
        Spacer(Modifier.height(topInset.dp))
        Box(Modifier.fillMaxWidth().height(AppChrome.NAV_BAR_HEIGHT.dp)) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "‹",
                    fontSize = AppChrome.BACK_SIZE.sp,
                    color = foreground,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(AppChrome.ICON_TAP_PADDING.dp),
                )
                Spacer(Modifier.weight(1f))
                actions?.invoke()
            }
            Box(Modifier.align(Alignment.Center)) {
                Text(
                    title,
                    fontSize = AppChrome.TITLE_SIZE.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = foreground,
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

/**
 * Flutter `EmptyStateWidget` / `_EmptyState` 形制的居中空态（图标字形 + 两行文案）；
 * P2-W2b Chat/Community 已用同款形制，本 ticket 六页复用。
 */
@Composable
internal fun DomainEmptyState(
    glyph: String,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(glyph, fontSize = 44.sp, color = AppChrome.separator)
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppChrome.labelPrimary)
        Spacer(Modifier.height(6.dp))
        Text(message, fontSize = 13.sp, color = AppChrome.labelSecondary)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                actionLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppChrome.accent,
                modifier = Modifier.clickable(onClick = onAction).padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
