package com.example.kuikly.platform.im

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/**
 * IM 即时通讯（对应源项目移动端能力模块）
 *
 * Mock 模式：全部状态本地模拟，不依赖真机原生能力。
 * 真实 SDK 接入时只需替换 [nativeCall] 的实现，接口保持不变。
 */
object ImService {

    /** 服务可用性（Mock 恒为 true；原生未接入时返回 false 便于灰度降级） */
    fun isAvailable(): Boolean = MockBackend.scenario != MockScenario.Error

    /** 最近一次操作日志 */
    private val logs = mutableListOf<String>()

    /** 获取操作日志（调试面板用） */
    fun logs(): List<String> = logs.toList()

    /** 清空日志 */
    fun clearLogs() { logs.clear() }

    /** 统一入口：记录日志 + 返回 Mock 结果 */
    fun <T> nativeCall(action: String, block: () -> T): Result<T> {
        logs.add(action)
        return runCatching { block() }
    }

    /** 模块能力清单，供 demo 页展示 */
    fun capabilities(): List<String> = listOf(
        "状态: ${if (isAvailable()) "可用" else "降级"}",
        "调用次数: ${logs.size}",
    )
}
