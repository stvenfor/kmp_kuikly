package com.example.kuikly.platform.linking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkingServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(LinkingService.isAvailable(), "深度链接 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        LinkingService.clearLogs()
        val r = LinkingService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, LinkingService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(LinkingService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
