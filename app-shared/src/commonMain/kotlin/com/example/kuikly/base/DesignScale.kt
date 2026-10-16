package com.example.kuikly.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.TextUnit
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * Flutter `flutter_screenutil`（designSize 375×812）的薄等价物。
 *
 * Kuikly 无内置 ScreenUtil；按官方惯例用 `pageViewWidth / designWidth` 缩放设计稿数值。
 * 与 Migration Source `ModuleUtilsConfig.designSize = Size(375, 812)` 对齐。
 *
 * 用法：
 * ```
 * ProvideDesignScale(pageViewWidth) {
 *   Box(Modifier.size(48.su()))
 *   Text("hi", fontSize = 16.susp())
 * }
 * ```
 *
 * 状态栏 / safeArea 等 [pagerData] 已是设备逻辑像素，**不要**再 `.su()`。
 */
class DesignScale(
    val pageWidth: Float,
    val designWidth: Float = DESIGN_WIDTH,
) {
    val factor: Float
        get() = if (designWidth <= 0f) 1f else pageWidth / designWidth

    fun su(v: Number): Dp = (v.toFloat() * factor).dp

    fun susp(v: Number): TextUnit = (v.toFloat() * factor).sp

    /** 缩放后的裸 Float（与未缩放的 statusBarHeight 等相加时用）。 */
    fun raw(v: Number): Float = v.toFloat() * factor

    companion object {
        const val DESIGN_WIDTH = 375f
    }
}

val LocalDesignScale = staticCompositionLocalOf {
    DesignScale(DesignScale.DESIGN_WIDTH)
}

@Composable
fun ProvideDesignScale(
    pageWidth: Float,
    designWidth: Float = DesignScale.DESIGN_WIDTH,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDesignScale provides DesignScale(pageWidth, designWidth),
        content = content,
    )
}

/** 设计稿宽度单位 → Dp（需在 [ProvideDesignScale] 内）。 */
val Number.su: Dp
    @Composable
    @ReadOnlyComposable
    get() = LocalDesignScale.current.su(this)

/** 设计稿字号单位 → sp。 */
val Number.susp: TextUnit
    @Composable
    @ReadOnlyComposable
    get() = LocalDesignScale.current.susp(this)
