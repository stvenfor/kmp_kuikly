package com.example.kuikly.platform.dokitbootstrap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DokitBootstrapServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(DokitBootstrapService.isAvailable(), "Dokit 启动引导 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        DokitBootstrapService.clearLogs()
        val r = DokitBootstrapService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, DokitBootstrapService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(DokitBootstrapService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
