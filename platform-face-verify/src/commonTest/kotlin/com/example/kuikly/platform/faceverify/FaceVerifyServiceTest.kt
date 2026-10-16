package com.example.kuikly.platform.faceverify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FaceVerifyServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(FaceVerifyService.isAvailable(), "人脸核身 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        FaceVerifyService.clearLogs()
        val r = FaceVerifyService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, FaceVerifyService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(FaceVerifyService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
