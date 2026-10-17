package com.example.kuikly.data.live

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

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
    val followed: Boolean = false,
    val giftCount: Int = 0,
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

/** 礼物面板目录项：`coin` 为钻石单价。 */
data class LiveGift(
    val id: String,
    val name: String,
    val coin: Int,
    val glyph: String,
)

/** 一次送礼的流水记录。 */
data class LiveGiftRecord(
    val id: String,
    val giftName: String,
    val coin: Int,
    val user: String,
)

internal fun compactCount(n: Int): String = when {
    n >= 10000 -> "${"%.1f".format(n / 10000.0)}w"
    n >= 1000 -> "${"%.1f".format(n / 1000.0)}k"
    else -> "$n"
}

/** 送礼初始钻石余额（Mock 钱包）。 */
private const val LIVE_COIN_BALANCE = 5000

/** 礼物流水最多保留条数。 */
private const val LIVE_GIFT_FEED_LIMIT = 20

private val LIVE_GIFT_CATALOG = listOf(
    LiveGift("gf1", "小心心", 10, "♥"),
    LiveGift("gf2", "加油棒", 50, "✦"),
    LiveGift("gf3", "皇冠", 500, "♛"),
    LiveGift("gf4", "火箭", 2000, "⇡"),
)

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

    private val giftFeedStore: MutableMap<String, MutableList<LiveGiftRecord>> = mutableMapOf()
    private var coinBalance: Int = LIVE_COIN_BALANCE
    private var giftSeq: Int = 0

    override fun seed(): List<LiveRoom> = rooms
    override fun idOf(item: LiveRoom): String = item.id

    fun fetchById(id: String): Result<LiveRoom> = byId(id)

    fun liveRooms(): Result<List<LiveRoom>> = all { it.status == LiveStatus.live }

    /** 列表页分类Tab：所有在播/预告房间去重后的分类。 */
    fun categories(): Result<List<String>> = all().map { list -> list.map { it.category }.distinct() }

    /** 按分类筛选（与 [liveRooms] 同为列表页筛选入口）。 */
    fun byCategory(category: String): Result<List<LiveRoom>> = all { it.category == category }

    /** 点赞：累加房间赞数。 */
    fun likeRoom(roomId: String): Result<LiveRoom> =
        mutate(roomId) { it.copy(likeCount = it.likeCount + 1) }

    /** 关注 / 取关主播，返回最新房间态。 */
    fun toggleFollow(roomId: String): Result<LiveRoom> =
        mutate(roomId) { it.copy(followed = !it.followed) }

    fun giftCatalog(): Result<List<LiveGift>> = when (MockBackend.scenario) {
        MockScenario.Error -> Result.failure(IllegalStateException("mock backend error"))
        MockScenario.Empty -> Result.success(emptyList())
        else -> Result.success(LIVE_GIFT_CATALOG)
    }

    fun coinBalance(): Int = coinBalance

    /** 本房间的送礼流水（最新在前）。 */
    fun giftFeedOf(roomId: String): List<LiveGiftRecord> =
        giftFeedStore[roomId]?.toList() ?: emptyList()

    /**
     * 送礼：扣钻石 → 写房间流水 → 礼物数 +1。
     * 失败路径：礼物不存在 / 房间不存在 / 钻石不足。
     */
    fun sendGift(roomId: String, giftId: String): Result<LiveGiftRecord> {
        val gift = LIVE_GIFT_CATALOG.firstOrNull { it.id == giftId }
            ?: return Result.failure(NoSuchElementException("giftId=$giftId"))
        val room = rooms.firstOrNull { it.id == roomId }
            ?: return Result.failure(NoSuchElementException("room=$roomId"))
        if (coinBalance < gift.coin) {
            return Result.failure(IllegalStateException("钻石不足，请充值"))
        }
        coinBalance -= gift.coin
        giftSeq += 1
        val record = LiveGiftRecord("gr_$giftSeq", gift.name, gift.coin, "我")
        val feed = giftFeedStore.getOrPut(roomId) { mutableListOf() }
        feed.add(0, record)
        if (feed.size > LIVE_GIFT_FEED_LIMIT) feed.removeAt(feed.size - 1)
        val index = rooms.indexOfFirst { it.id == roomId }
        rooms[index] = room.copy(giftCount = room.giftCount + 1)
        return Result.success(record)
    }

    private fun mutate(roomId: String, transform: (LiveRoom) -> LiveRoom): Result<LiveRoom> {
        val index = rooms.indexOfFirst { it.id == roomId }
        if (index < 0) return Result.failure(NoSuchElementException("room=$roomId"))
        val updated = transform(rooms[index])
        rooms[index] = updated
        return Result.success(updated)
    }

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
