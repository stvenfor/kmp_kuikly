package com.example.kuikly.platform.bluetooth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BluetoothServiceTest {

    @Test
    fun testAvailableByDefault() {
        assertTrue(BluetoothService.isAvailable(), "蓝牙能力 应默认可用")
    }

    @Test
    fun testNativeCallRecordsLogAndReturnsValue() {
        BluetoothService.clearLogs()
        val r = BluetoothService.nativeCall("ping") { 42 }
        assertTrue(r.isSuccess, "调用应成功")
        assertEquals(42, r.getOrNull())
        assertEquals(1, BluetoothService.logs().size, "应记录一条日志")
    }

    @Test
    fun testCapabilitiesNotEmpty() {
        assertTrue(BluetoothService.capabilities().isNotEmpty(), "能力清单不应为空")
    }
}
