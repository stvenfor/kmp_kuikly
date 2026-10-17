package com.example.kuikly.data.live

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/**
 * 源 `components/realtime/lib/config/realtime_config.dart` 的 `RealtimeTopics`：
 * topic 字符串由后端约定，Flutter 端原样拼接房间号。
 */
object LiveTopics {
    fun signal(roomId: String): String = "live.signal.$roomId"
    fun roomState(roomId: String): String = "live.room.$roomId.state"
}

/**
 * 源 `commons/core/lib/model/realtime/realtime_connection_state.dart`
 * 的枚举 + `label` 扩展（7 态）。
 */
enum class LiveConnectionState(val label: String) {
    disconnected("未连接"),
    connecting("连接中"),
    authenticating("鉴权中"),
    connected("已连接"),
    reconnecting("重连中"),
    degraded("降级"),
    failed("失败"),
    ;

    /** 源 `RealtimeConnectionStateX.isActive`：connected / degraded 视为可用。 */
    val active: Boolean get() = this == connected || this == degraded
}

/**
 * 源 `LiveRoomController` 订阅两类 topic，渲染前缀分别是 `[signal]` 与 `[state]`。
 */
enum class LiveSignalKind(val tag: String) {
    SIGNAL("signal"),
    ROOM_STATE("state"),
}

/** 一条 WS 下行事件（源 `watchTopic` 回调里的 eventName / seq / payload）。 */
data class LiveSignal(
    val id: String,
    val topic: String,
    val kind: LiveSignalKind,
    val eventName: String,
    val seq: Int,
    val payload: String = "",
) {
    /** 源 `LiveRoomPage` 一行的拼接口径：`[signal] live.join seq=3 {payload}`。 */
    val line: String
        get() = when (kind) {
            LiveSignalKind.SIGNAL -> "[${kind.tag}] $eventName seq=$seq $payload".trimEnd()
            LiveSignalKind.ROOM_STATE -> "[${kind.tag}] $eventName seq=$seq"
        }
}

/** 源 `signals.length > 30` 的截断阈值。 */
private const val LIVE_SIGNAL_LIMIT = 30

/**
 * 源 `LiveRoomController` 的等价物（Kuikly 无真 WS，进程内模拟连接与 topic 订阅）。
 *
 * 生命周期对齐源实现：`enterRoom` = `onInit`（保活 + 订阅两个 topic），
 * `leaveRoom` = `onClose`（退订 + 关保活 + 断开），`sendJoinSignal` = `sendMockSignal`。
 */
class LiveRealtimeRepository {
    private val feeds: MutableMap<String, MutableList<LiveSignal>> = mutableMapOf()
    private val subscribed: MutableSet<String> = mutableSetOf()
    private var seq: Int = 0

    var connection: LiveConnectionState = LiveConnectionState.disconnected
        private set

    /** 源 `setKeepAliveInBackground(true/false)`：切后台是否保持 WS 连接。 */
    var keepAliveInBackground: Boolean = false
        private set

    val subscribedTopics: List<String> get() = subscribed.sorted()

    /** 进入房间：开启后台保活 → 订阅 `liveSignal` + `liveRoomState` → 建立连接。 */
    fun enterRoom(roomId: String): LiveConnectionState {
        setKeepAliveInBackground(true)
        subscribeTopics(roomId)
        connection = if (MockBackend.scenario == MockScenario.Error) {
            LiveConnectionState.failed
        } else {
            LiveConnectionState.connected
        }
        return connection
    }

    /** 离开房间：退订两个 topic → 关闭后台保活 → 置为未连接（源 `onClose`）。 */
    fun leaveRoom(roomId: String) {
        unsubscribeTopics(roomId)
        setKeepAliveInBackground(false)
        connection = LiveConnectionState.disconnected
    }

    fun subscribeTopics(roomId: String) {
        topicsOf(roomId).forEach { subscribed.add(it) }
    }

    fun unsubscribeTopics(roomId: String) {
        topicsOf(roomId).forEach { subscribed.remove(it) }
    }

    fun setKeepAliveInBackground(enabled: Boolean) {
        keepAliveInBackground = enabled
    }

    /** 源 `sendMockSignal`：`topic = liveSignal(roomId)`、`eventName = live.join`、`payload = {roomId}`。 */
    fun sendJoinSignal(roomId: String): Result<LiveSignal> {
        if (!connection.active) {
            return Result.failure(IllegalStateException("WS 未连接，信令未发出"))
        }
        val topic = LiveTopics.signal(roomId)
        subscribed.add(topic)
        return Result.success(
            append(
                roomId = roomId,
                topic = topic,
                kind = LiveSignalKind.SIGNAL,
                eventName = "live.join",
                payload = "{\"roomId\":\"$roomId\"}",
            ),
        )
    }

    /** 模拟 `liveRoomState` 的下行事件（源 `watchTopic(liveRoomState(roomId))` 回调）。 */
    fun receiveRoomState(roomId: String): Result<LiveSignal> {
        if (!connection.active) {
            return Result.failure(IllegalStateException("WS 未连接，收不到房间状态"))
        }
        return Result.success(
            append(
                roomId = roomId,
                topic = LiveTopics.roomState(roomId),
                kind = LiveSignalKind.ROOM_STATE,
                eventName = "room.state",
                payload = "{\"roomId\":\"$roomId\"}",
            ),
        )
    }

    fun signalsOf(roomId: String): List<LiveSignal> = feeds[roomId]?.toList() ?: emptyList()

    fun clear(roomId: String) {
        feeds.remove(roomId)
    }

    private fun topicsOf(roomId: String): List<String> = listOf(
        LiveTopics.signal(roomId),
        LiveTopics.roomState(roomId),
    )

    private fun append(
        roomId: String,
        topic: String,
        kind: LiveSignalKind,
        eventName: String,
        payload: String,
    ): LiveSignal {
        seq += 1
        val signal = LiveSignal(
            id = "sig_${roomId}_$seq",
            topic = topic,
            kind = kind,
            eventName = eventName,
            seq = seq,
            payload = payload,
        )
        val feed = feeds.getOrPut(roomId) { mutableListOf() }
        feed.add(0, signal)
        while (feed.size > LIVE_SIGNAL_LIMIT) {
            feed.removeAt(feed.size - 1)
        }
        return signal
    }
}

object LiveRealtimeStore {
    val repo: LiveRealtimeRepository = LiveRealtimeRepository()
}
