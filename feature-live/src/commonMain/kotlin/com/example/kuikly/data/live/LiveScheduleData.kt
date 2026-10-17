package com.example.kuikly.data.live

import com.example.kuikly.data.BaseMockRepository

/**
 * 直播状态机：预告 → 直播中 → 回放。
 *
 * 源 `features/live` 只有 offline / live 两态，本文件的三态用于把
 * 「开播前的场次」与「结束后的回放」也纳入同一个列表（Room(I) 对齐用）。
 */
enum class LivePhase(val label: String) {
    upcoming("预告"),
    live("直播中"),
    replay("回放"),
    ;

    /** 只有预告场次允许预约 / 取消预约。 */
    val reservable: Boolean get() = this == upcoming
}

/** 一场直播（同一个房间可以有预告场次与历史回放场）。 */
data class LiveSession(
    val id: String,
    val roomId: String,
    val title: String,
    val hostName: String,
    val dayLabel: String,
    val startAt: String,
    val phase: LivePhase,
    val viewerCount: Int,
    val reserveCount: Int = 0,
    val reserved: Boolean = false,
    val peakLabel: String = "",
    val durationLabel: String = "",
) {
    val viewerLabel: String get() = compactCount(viewerCount)

    val scheduleLabel: String get() = "$dayLabel $startAt"

    val canReserve: Boolean get() = phase.reservable
}

class LiveSessionRepository : BaseMockRepository<LiveSession>() {
    private val sessions = mutableListOf(
        LiveSession(
            id = "s1",
            roomId = "l1",
            title = "带货专场 · 第 12 期",
            hostName = "小丽",
            dayLabel = "今晚",
            startAt = "20:00",
            phase = LivePhase.upcoming,
            viewerCount = 0,
            reserveCount = 128,
        ),
        LiveSession(
            id = "s2",
            roomId = "l3",
            title = "周末歌唱会 · 第 3 期",
            hostName = "阿杰",
            dayLabel = "周六",
            startAt = "21:30",
            phase = LivePhase.upcoming,
            viewerCount = 0,
            reserveCount = 46,
        ),
        LiveSession(
            id = "s3",
            roomId = "l1",
            title = "带货专场 · 第 11 期回放",
            hostName = "小丽",
            dayLabel = "10-15",
            startAt = "20:00",
            phase = LivePhase.replay,
            viewerCount = 23100,
            peakLabel = "峰值 3.1w",
            durationLabel = "时长 01:52:10",
        ),
        LiveSession(
            id = "s4",
            roomId = "l2",
            title = "KMP 跨端实践回放",
            hostName = "老王",
            dayLabel = "10-12",
            startAt = "19:30",
            phase = LivePhase.replay,
            viewerCount = 5240,
            peakLabel = "峰值 1.1k",
            durationLabel = "时长 00:48:35",
        ),
        LiveSession(
            id = "s5",
            roomId = "l2",
            title = "技术答疑 · 直播间答疑",
            hostName = "老王",
            dayLabel = "今晚",
            startAt = "19:30",
            phase = LivePhase.live,
            viewerCount = 860,
        ),
    )

    override fun seed(): List<LiveSession> = sessions
    override fun idOf(item: LiveSession): String = item.id

    fun upcomingSessions(): Result<List<LiveSession>> = all { it.phase == LivePhase.upcoming }

    fun replaySessions(): Result<List<LiveSession>> = all { it.phase == LivePhase.replay }

    fun reservedSessions(): Result<List<LiveSession>> = all { it.reserved }

    /** 某一房间的全部场次（预告 / 直播中 / 回放）。 */
    fun sessionsOf(roomId: String): Result<List<LiveSession>> = all { it.roomId == roomId }

    /**
     * 预约 / 取消预约：仅 [LivePhase.upcoming] 可操作，其它相位直接失败。
     * 成功后同步调整预约人数。
     */
    fun toggleReserve(sessionId: String): Result<LiveSession> {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return Result.failure(NoSuchElementException("session=$sessionId"))
        val old = sessions[index]
        if (!old.canReserve) {
            return Result.failure(IllegalStateException("${old.phase.label}场次不支持预约"))
        }
        val updated = old.copy(
            reserved = !old.reserved,
            reserveCount = if (old.reserved) (old.reserveCount - 1).coerceAtLeast(0) else old.reserveCount + 1,
        )
        sessions[index] = updated
        return Result.success(updated)
    }
}

object LiveSessionStore {
    val repo: LiveSessionRepository = LiveSessionRepository()
}
