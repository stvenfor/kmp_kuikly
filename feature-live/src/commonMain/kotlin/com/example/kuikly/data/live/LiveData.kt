package com.example.kuikly.data.live

import com.example.kuikly.data.BaseMockRepository

enum class LiveStatus { offline, live }

/** 直播间（port of source LiveRoom）。 */
data class LiveRoom(
    val id: String,
    val title: String,
    val hostName: String,
    val viewerCount: Int,
    val coverUrl: String,
    val status: LiveStatus,
    val category: String = "聊天",
    val announcement: String = "",
    val likeCount: Int = 0,
    val tags: List<String> = emptyList(),
) {
    val viewerLabel: String get() = compactCount(viewerCount)

    val liveLabel: String get() = if (status == LiveStatus.live) "直播中" else "未开播"
}

/** 弹幕消息（port of source LiveRoomController.signals）。 */
data class DanmakuMessage(
    val id: String,
    val user: String,
    val text: String,
    val seq: Int,
)

private fun compactCount(n: Int): String = when {
    n >= 10000 -> "${"%.1f".format(n / 10000.0)}w"
    n >= 1000 -> "${"%.1f".format(n / 1000.0)}k"
    else -> "$n"
}

class LiveRepository : BaseMockRepository<LiveRoom>() {
    private val rooms = mutableListOf(
        LiveRoom(
            id = "l1",
            title = "晚间带货 · 好物专场",
            hostName = "小丽",
            viewerCount = 12300,
            coverUrl = "https://picsum.photos/seed/l1/400",
            status = LiveStatus.live,
            category = "带货",
            announcement = "每晚 20:00 开播，全场包邮，点击左下角购物车下单。",
            likeCount = 45200,
            tags = listOf("带货", "包邮", "限时折扣"),
        ),
        LiveRoom(
            id = "l2",
            title = "技术分享直播",
            hostName = "老王",
            viewerCount = 860,
            coverUrl = "https://picsum.photos/seed/l2/400",
            status = LiveStatus.live,
            category = "技术",
            announcement = "本场主题：KMP 跨端工程实践，欢迎在弹幕里提问。",
            likeCount = 3200,
            tags = listOf("技术", "KMP", "问答"),
        ),
        LiveRoom(
            id = "l3",
            title = "周末歌唱会",
            hostName = "阿杰",
            viewerCount = 0,
            coverUrl = "https://picsum.photos/seed/l3/400",
            status = LiveStatus.offline,
            category = "音乐",
            announcement = "主播暂未开播，开播后会第一时间提醒你。",
            likeCount = 0,
            tags = listOf("音乐", "点歌"),
        ),
    )

    private val seedDanmaku: Map<String, List<DanmakuMessage>> = mapOf(
        "l1" to listOf(
            DanmakuMessage("dm_l1_1", "观众A", "主播讲得很清楚", 1),
            DanmakuMessage("dm_l1_2", "观众B", "这个价格真香", 2),
            DanmakuMessage("dm_l1_3", "观众C", "已下单，坐等发货", 3),
        ),
        "l2" to listOf(
            DanmakuMessage("dm_l2_1", "观众D", "前排学习", 1),
            DanmakuMessage("dm_l2_2", "观众E", "KMP 在 iOS 上性能如何？", 2),
        ),
        "l3" to emptyList(),
    )

    private val danmakuStore: MutableMap<String, MutableList<DanmakuMessage>> = mutableMapOf()
    private var seq: Int = 100

    init {
        for ((roomId, list) in seedDanmaku) {
            danmakuStore[roomId] = list.toMutableList()
        }
    }

    override fun seed(): List<LiveRoom> = rooms
    override fun idOf(item: LiveRoom): String = item.id

    fun fetchById(id: String): Result<LiveRoom> = byId(id)

    fun liveRooms(): Result<List<LiveRoom>> = all { it.status == LiveStatus.live }

    /** 刷新在线人数：确定性递增 1..23，返回更新后的房间。 */
    fun refreshViewers(roomId: String): Result<LiveRoom> {
        val index = rooms.indexOfFirst { it.id == roomId }
        if (index < 0) return Result.failure(NoSuchElementException("room=$roomId"))
        val old = rooms[index]
        val delta = ((old.viewerCount + 7) % 23) + 1
        val updated = old.copy(viewerCount = old.viewerCount + delta)
        rooms[index] = updated
        return Result.success(updated)
    }

    fun danmakuOf(roomId: String): Result<List<DanmakuMessage>> =
        Result.success(danmakuStore[roomId]?.toList() ?: emptyList())

    /** 发送弹幕；空白内容失败，最新弹幕插在最前，最多保留 30 条。 */
    fun sendDanmaku(roomId: String, text: String): Result<DanmakuMessage> {
        if (text.isBlank()) return Result.failure(IllegalArgumentException("弹幕不能为空"))
        seq += 1
        val msg = DanmakuMessage("dm_$seq", "我", text, seq)
        val list = danmakuStore.getOrPut(roomId) { mutableListOf() }
        list.add(0, msg)
        if (list.size > 30) list.removeAt(list.size - 1)
        return Result.success(msg)
    }
}

object LiveStore {
    val repo: LiveRepository = LiveRepository()
}
