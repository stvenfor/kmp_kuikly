package com.example.kuikly.base

import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.core.module.Module
import kotlin.math.max

/** Compose pages that need BridgeModule (openPage / toast). */
abstract class BaseComposePager : ComposeContainer() {
    override fun createExternalModules(): Map<String, Module>? {
        val modules = hashMapOf<String, Module>()
        modules[BridgeModule.MODULE_NAME] = BridgeModule()
        return modules
    }

    /**
     * 状态栏高度：给**文字/控件**内边距用，不是把整页往下推空一块。
     *
     * 沉浸式正确写法：根布局 `fillMaxSize().background(...)` 从 y=0 盖住状态栏；
     * 再对标题等可交互内容 `padding(top = statusBarInset().dp)`。
     * 宿主需透明状态栏（androidApp KuiklyRenderActivity.setupImmersiveMode）。
     */
    protected fun statusBarInset(): Float = pagerData.statusBarHeight

    /** 底栏避让：safeArea 与 Android 导航栏取较大值（垫在底栏/内容内侧）。 */
    protected fun bottomSafeInset(): Float =
        max(pagerData.safeAreaInsets.bottom, pagerData.androidBottomBavBarHeight)
}
