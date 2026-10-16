package com.example.kuikly.platform.realtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RealtimeServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(RealtimeService.isAvailable(), "实时通信 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        RealtimeService.clearLogs()
        val r = RealtimeService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, RealtimeService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(RealtimeService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
