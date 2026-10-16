package com.example.kuikly.platform.dokit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DokitServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(DokitService.isAvailable(), "Dokit 调试工具箱 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        DokitService.clearLogs()
        val r = DokitService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, DokitService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(DokitService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
