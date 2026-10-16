package com.example.kuikly.platform.im

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(ImService.isAvailable(), "IM 即时通讯 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        ImService.clearLogs()
        val r = ImService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, ImService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(ImService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
