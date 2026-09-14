package com.example.kuikly.base

import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.core.module.Module

/** Compose pages that need BridgeModule (openPage / toast). */
internal abstract class BaseComposePager : ComposeContainer() {
    override fun createExternalModules(): Map<String, Module>? {
        val modules = hashMapOf<String, Module>()
        modules[BridgeModule.MODULE_NAME] = BridgeModule()
        return modules
    }
}
