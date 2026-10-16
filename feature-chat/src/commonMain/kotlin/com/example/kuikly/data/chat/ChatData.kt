package com.example.kuikly.data.chat

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.Paged

enum class ConversationType { private, group }

enum class MessageType { text, image, voice, video, system }

enum class SendStatus { sending, sent, failed }

enum class ReadStatus { unread, read }

// 时间分组阈值：与源工程一致，相邻消息间隔 >= 5 分钟插入新分组。
const val CHAT_TIME_GAP_SECONDS: Long = 300L

/** 会话（port of source ConversationModel）。 */
data class Conversation(
    val id: String,
    val type: ConversationType,
    val targetId: String,
    val title: String,
    val portraitUrl: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val isOnline: Boolean,
    val unreadCount: Int,
    val memberCount: Int,
) {
    val isGroup: Boolean get() = type == ConversationType.group

    val subtitle: String
        get() = when {
            isGroup -> "群聊 · $memberCount 人"
            isOnline -> "在线"
            else -> "离线"
        }
}

/** 消息（port of source MessageModel）。 */
data class Message(
    val id: String,
    val conversationId: String,
    val type: MessageType,
    val content: String,
    val isSelf: Boolean,
    val createdAt: Long,
    val sendStatus: SendStatus = SendStatus.sent,
    val readStatus: ReadStatus = ReadStatus.read,
    val senderName: String = "",
    val imageUrl: String? = null,
    val voiceDurationSeconds: Int = 0,
)

/** 按时间切分后的消息分组（供会话详情页渲染时间分隔条）。 */
data class MessageSection(
    val label: String,
    val messages: List<Message>,
)

class ChatRepository : BaseMockRepository<Conversation>() {
    private val conversations = listOf(
        Conversation("c1", ConversationType.private, "u_a", "产品群-张敏", "", "新版原型已发群里", 1_700_000_000L, true, 2, 0),
        Conversation("c2", ConversationType.group, "g_1", "增长增长增长", "", "周三复盘会 20:00", 1_700_000_100L, false, 5, 128),
        Conversation("c3", ConversationType.private, "u_b", "客户-李工", "", "[图片]", 1_700_000_200L, false, 0, 0),
    )
    private val messages = listOf(
        Message("m1", "c1", MessageType.text, "在吗？新版原型已发群里", false, 1_700_000_000L, senderName = "张敏"),
        Message("m2", "c1", MessageType.text, "收到，我看看", true, 1_700_000_010L, senderName = "我"),
        Message("m3", "c1", MessageType.text, "重点看转化漏斗那页", false, 1_700_000_020L, senderName = "张敏"),

        Message("m4", "c2", MessageType.text, "本周增长数据看板已更新", false, 1_700_000_100L, senderName = "增长小助手"),
        Message("m5", "c2", MessageType.text, "收到，我下午同步到群里", true, 1_700_000_160L, senderName = "我"),
        Message("m6", "c2", MessageType.text, "周三复盘会 20:00，别忘了", false, 1_700_000_700L, senderName = "增长小助手"),
        Message("m7", "c2", MessageType.text, "会议室已预定 A302", false, 1_700_000_720L, senderName = "李工"),
        Message("m8", "c2", MessageType.text, "好的，我会准备转化漏斗的数据", true, 1_700_000_900L, senderName = "我"),
        Message("m9", "c2", MessageType.text, "补充一下：这次要对齐 ROI 口径", false, 1_700_001_500L, senderName = "增长小助手"),
        Message("m10", "c2", MessageType.text, "口径文档我发群里了", false, 1_700_001_520L, senderName = "李工"),
        Message("m11", "c2", MessageType.text, "收到，我先过一遍", true, 1_700_001_540L, senderName = "我"),
        Message("m12", "c2", MessageType.text, "明天上午前把结论贴出来", false, 1_700_002_200L, senderName = "增长小助手"),

        Message("m13", "c3", MessageType.image, "[图片]", false, 1_700_000_200L,
            senderName = "李工", imageUrl = "https://picsum.photos/seed/chat_c3_1/800"),
        Message("m14", "c3", MessageType.text, "图我看了，规格没问题", true, 1_700_000_260L, senderName = "我"),
        Message("m15", "c3", MessageType.text, "那就按这版报价走流程", false, 1_700_000_800L, senderName = "李工"),
    )

    // 已发送但未回落到 seed 的消息（模拟发送 / 自动回复）。
    private val outbox = mutableListOf<Message>()
    // 未读清零覆盖表：key = 会话 id，value = 覆盖后的未读数。
    private val unreadOverride = mutableMapOf<String, Int>()

    override fun seed(): List<Conversation> =
        conversations.map { c ->
            val override = unreadOverride[c.id]
            if (override == null) c else c.copy(unreadCount = override)
        }

    override fun idOf(item: Conversation): String = item.id

    private fun messagesOf(conversationId: String): List<Message> =
        (messages + outbox)
            .filter { it.conversationId == conversationId }
            .sortedBy { it.createdAt }

    fun messages(conversationId: String): Result<List<Message>> =
        Result.success(messagesOf(conversationId))

    // 消息按时间升序分页：第 1 页为最早的一批历史消息。
    fun messagePage(conversationId: String, page: Int, pageSize: Int = 20): Result<Paged<Message>> {
        if (pageSize <= 0) return Result.failure(IllegalArgumentException("pageSize 必须大于 0"))
        val all = messagesOf(conversationId)
        val from = (page - 1).coerceAtLeast(0) * pageSize
        val to = (from + pageSize).coerceAtMost(all.size)
        val slice = if (from <= all.size) all.subList(from, to) else emptyList()
        return Result.success(Paged(slice, all.size, page, pageSize))
    }

    fun send(conversationId: String, text: String): Result<Message> {
        if (text.isBlank()) return Result.failure(IllegalArgumentException("empty message"))
        val m = Message(
            id = "s${outbox.size + 1}",
            conversationId = conversationId,
            type = MessageType.text,
            content = text,
            isSelf = true,
            createdAt = nextTimestamp(),
            sendStatus = SendStatus.sent,
            senderName = "我",
        )
        outbox.add(m)
        return Result.success(m)
    }

    // 模拟对方自动回复，便于在纯 mock 环境下演示收发闭环。
    fun autoReply(conversationId: String, received: String): Result<Message> {
        val peerName = conversations.firstOrNull { it.id == conversationId }?.title ?: "对方"
        val text = when {
            received.contains("?") || received.contains("？") -> "我确认一下，稍后回复你"
            received.contains("会议") || received.contains("复盘") -> "好的，会议纪要我会整理"
            received.contains("收到") -> "那就这么定了"
            else -> "已读：$received"
        }
        val m = Message(
            id = "r${outbox.size + 1}",
            conversationId = conversationId,
            type = MessageType.text,
            content = text,
            isSelf = false,
            createdAt = nextTimestamp(),
            sendStatus = SendStatus.sent,
            readStatus = ReadStatus.read,
            senderName = peerName,
        )
        outbox.add(m)
        return Result.success(m)
    }

    // 未读清零：写入覆盖表后 seed() 即对 page / byId / all 生效。
    fun markRead(conversationId: String): Result<Conversation> {
        unreadOverride[conversationId] = 0
        return byId(conversationId)
    }

    fun unreadTotal(): Int = seed().sumOf { it.unreadCount }

    fun images(conversationId: String): List<String> =
        messagesOf(conversationId).mapNotNull { it.imageUrl }

    private fun nextTimestamp(): Long {
        val latest = (messages + outbox).maxOfOrNull { it.createdAt } ?: 1_700_000_000L
        return latest + 30L
    }
}

object ChatStore {
    val repo: ChatRepository = ChatRepository()
}

// --- 时间格式化 / 分组工具（纯函数，便于单测） ---

private data class CivilDate(val year: Long, val month: Long, val day: Long)

private fun pad2(value: Long): String = if (value < 10L) "0$value" else "$value"

private fun civilFromDays(z: Long): CivilDate {
    val shifted = z + 719468L
    val era = if (shifted >= 0) shifted else shifted - 146096L
    val eraIndex = era / 146097L
    val doe = era - eraIndex * 146097L
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
    val y = yoe + eraIndex * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val d = doy - (153L * mp + 2L) / 5L + 1L
    val m = if (mp < 10L) mp + 3L else mp - 9L
    val year = if (m <= 2L) y + 1L else y
    return CivilDate(year, m, d)
}

fun chatClock(epochSeconds: Long): String {
    val minutesOfDay = (((epochSeconds % 86400L) + 86400L) % 86400L) / 60L
    return "${pad2(minutesOfDay / 60L)}:${pad2(minutesOfDay % 60L)}"
}

fun chatTimeLabel(epochSeconds: Long, nowSeconds: Long): String {
    val day = epochSeconds / 86400L
    val today = nowSeconds / 86400L
    return when (day) {
        today -> chatClock(epochSeconds)
        today - 1L -> "昨天 ${chatClock(epochSeconds)}"
        else -> {
            val date = civilFromDays(day)
            "${date.month}/${date.day} ${chatClock(epochSeconds)}"
        }
    }
}

fun groupMessagesByTime(
    messages: List<Message>,
    nowSeconds: Long = 0L,
    gapSeconds: Long = CHAT_TIME_GAP_SECONDS,
): List<MessageSection> {
    if (messages.isEmpty()) return emptyList()
    val sorted = messages.sortedBy { it.createdAt }
    val sections = mutableListOf<MessageSection>()
    var sectionStart = sorted.first().createdAt
    var current = mutableListOf(sorted.first())
    for (i in 1 until sorted.size) {
        val m = sorted[i]
        val prev = sorted[i - 1]
        if (m.createdAt - prev.createdAt >= gapSeconds) {
            sections.add(MessageSection(chatTimeLabel(sectionStart, nowSeconds), current.toList()))
            current = mutableListOf()
            sectionStart = m.createdAt
        }
        current.add(m)
    }
    sections.add(MessageSection(chatTimeLabel(sectionStart, nowSeconds), current.toList()))
    return sections
}
