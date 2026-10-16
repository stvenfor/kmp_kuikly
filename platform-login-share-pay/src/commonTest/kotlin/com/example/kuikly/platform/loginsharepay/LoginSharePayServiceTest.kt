package com.example.kuikly.platform.loginsharepay

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginSharePayServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(LoginSharePayService.isAvailable(), "登录/分享/支付桥接 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        LoginSharePayService.clearLogs()
        val r = LoginSharePayService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, LoginSharePayService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(LoginSharePayService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
