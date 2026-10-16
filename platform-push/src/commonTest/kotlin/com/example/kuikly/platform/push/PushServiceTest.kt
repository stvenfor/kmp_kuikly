package com.example.kuikly.platform.push

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PushServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(PushService.isAvailable(), "推送消息 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        PushService.clearLogs()
        val r = PushService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, PushService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(PushService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
