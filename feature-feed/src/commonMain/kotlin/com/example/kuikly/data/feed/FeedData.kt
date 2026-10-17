package com.example.kuikly.data.feed

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.Paged
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/**
 * 信息流帖子（port of source `PostModel`/`CommentModel`/`TopicModel`）。
 * 在原有 `FeedItem`（id/title/body/author/timeLabel）基础上对齐源工程字段，
 * 新增点赞/评论/转发/收藏、话题与 @用户、图文/视频、分页 Tab 等。
 * 旧字段保留默认值，原 FeedList/FeedDetail 仍可编译。
 */
data class FeedItem(
    val id: String,
    val title: String = "",
    val body: String = "",
    // —— 对齐 PostModel 的富字段 ——
    val userId: String = "",
    val author: String = "匿名",
    val avatar: String = "",
    val content: String = body,
    val timeLabel: String = "",
    val source: String = "来自 Kuikly",
    val images: List<String> = emptyList(),
    val videoUrl: String? = null,
    val videoCoverUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val favoriteCount: Int = 0,
    val isLiked: Boolean = false,
    val isFavorited: Boolean = false,
    val isMine: Boolean = false,
    val mediaType: String = "none",
    val topic: String? = null,
    val mentions: List<String> = emptyList(),
    val comments: List<FeedComment> = emptyList(),
    val nearby: Boolean = true,
) {
    val hasImages: Boolean get() = images.isNotEmpty()
    val hasVideo: Boolean get() = videoUrl != null && !videoUrl.isNullOrEmpty()
    val mediaLabel: String
        get() = when (mediaType) {
            "image" -> "图片"
            "video" -> "视频"
            else -> "无媒体"
        }
}

/** 评论（port of source `CommentModel`）。 */
data class FeedComment(
    val id: String,
    val postId: String,
    val nickname: String,
    val content: String,
    val replyToNickname: String? = null,
)

/** 话题（port of source `TopicModel`）。 */
data class FeedTopic(
    val id: String,
    val name: String,
    val heat: Int = 0,
    val isAskEveryone: Boolean = false,
) {
    val displayName: String get() = "#$name"
    val heatLabel: String
        get() = if (heat >= 10000) {
            val wan = heat / 10000.0
            if (wan == wan.toInt().toDouble()) "${wan.toInt()}万" else String.format("%.1f", wan) + "万"
        } else heat.toString()
}

/** 三个信息流 Tab：推荐 / 关注 / 附近（对齐源 latest/following + 新增 nearby）。 */
object FeedTabs {
    val keys = listOf("recommend", "following", "nearby")
    val labels = listOf("推荐", "关注", "附近")
    fun indexOf(key: String): Int = keys.indexOf(key).let { if (it < 0) 0 else it }
}

class FeedRepository : BaseMockRepository<FeedItem>() {
    private val posts = mutableListOf<FeedItem>()
    private var seq = 0
    private val followedUserIds = mutableSetOf<String>()
    private val topicList = listOf(
        FeedTopic("t1", "健身打卡", 120000, false),
        FeedTopic("t2", "好物推荐", 88000, true),
        FeedTopic("t3", "效率工具", 45000, false),
        FeedTopic("t4", "早起打卡", 12000, false),
        FeedTopic("t5", "读书笔记", 6800, false),
    )
    private val mentionUsers = listOf("张三", "李四", "王五", "赵六")

    private val sampleContents = listOf(
        "今天去了 @张三 推荐的咖啡店，环境不错。\n#健身打卡\n欢迎访问：https://flutter.dev",
        "周末 hiking，天气太好了！#户外",
        "刚读完一本好书，推荐 @李四 也看看。",
        "分享一张随手拍～",
        "项目上线啦，感谢团队！#效率工具 https://dart.dev",
        "午餐打卡 @王五",
        "学习 Compose 状态管理中…",
        "新出的降噪耳机真香 #好物推荐",
    )
    private val sampleNicknames = listOf("小鹿", "阿强", "产品汪", "数码菌", "小明", "momo", "开发者", "李雷")

    init {
        for (i in 0 until 18) {
            val id = "f$i"
            val isVideo = i % 7 == 0
            val hasImage = !isVideo && i % 3 != 0
            val imgs = if (hasImage) {
                List((i % 4) + 1) { j -> "https://picsum.photos/seed/${id}_$j/400" }
            } else emptyList()
            val content = sampleContents[i % sampleContents.size]
            val topic = when {
                content.contains("#健身打卡") -> "健身打卡"
                content.contains("#效率工具") -> "效率工具"
                content.contains("#好物推荐") -> "好物推荐"
                content.contains("#户外") -> "户外"
                else -> null
            }
            val mentions = "@([\\u4e00-\\u9fa5A-Za-z0-9_]+)".toRegex()
                .findAll(content).map { it.groupValues[1] }.toList()
            val nick = sampleNicknames[i % sampleNicknames.size]
            val userId = "u${i % 6}"
            val commentSeed = listOf(
                FeedComment("c_${id}_1", id, sampleNicknames[(i + 1) % sampleNicknames.size], "说得对！"),
                FeedComment("c_${id}_2", id, sampleNicknames[(i + 2) % sampleNicknames.size], "同感 +1", sampleNicknames[i % sampleNicknames.size]),
            )
            posts.add(
                FeedItem(
                    id = id,
                    title = content.lineSequence().firstOrNull()?.take(24) ?: content.take(24),
                    body = content,
                    content = content,
                    userId = userId,
                    author = nick,
                    avatar = "",
                    timeLabel = "${i + 1} 小时前",
                    source = if (i % 2 == 0) "来自 iPhone" else "来自 Android",
                    images = imgs,
                    videoUrl = if (isVideo) "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4" else null,
                    videoCoverUrl = if (isVideo) "https://picsum.photos/seed/video_$i/640" else null,
                    likeCount = (i * 37) % 520,
                    commentCount = 2 + (i % 9),
                    shareCount = (i * 5) % 60,
                    favoriteCount = (i * 11) % 200,
                    isLiked = i % 4 == 0,
                    isFavorited = i % 5 == 0,
                    isMine = i == 0,
                    mediaType = if (isVideo) "video" else if (hasImage) "image" else "none",
                    topic = topic,
                    mentions = mentions,
                    comments = commentSeed,
                    nearby = i % 2 == 0,
                ),
            )
        }
        // 预置少量关注，保证「关注」Tab 在 Success 下非空
        followedUserIds.add("u0")
        followedUserIds.add("u2")
    }

    override fun seed(): List<FeedItem> = posts
    override fun idOf(item: FeedItem): String = item.id

    /** 列表（兼容旧 FeedListPage）；全量。 */
    fun list(): Result<List<FeedItem>> = all { true }

    /** 帖子详情（兼容旧 FeedDetailPage）。 */
    fun detail(id: String): Result<FeedItem> = byId(id)

    /**
     * 分页拉取：page 从 1 开始，对齐 `BaseMockRepository.page`。
     * tab = recommend(全部) / following(已关注用户) / nearby(附近)。
     */
    fun fetchPage(page: Int, tab: String = "recommend", pageSize: Int = 8): Result<Paged<FeedItem>> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val filter: (FeedItem) -> Boolean = when (tab) {
            "following" -> { it -> followedUserIds.contains(it.userId) }
            "nearby" -> { it -> it.nearby }
            else -> { _ -> true }
        }
        val paged = page(page, pageSize, filter).getOrElse { return Result.failure(it) }
        return Result.success(paged)
    }

    fun toggleLike(id: String): Result<FeedItem> = mutate(id) { old ->
        old.copy(isLiked = !old.isLiked, likeCount = old.likeCount + if (old.isLiked) -1 else 1)
    }

    fun toggleFavorite(id: String): Result<FeedItem> = mutate(id) { old ->
        old.copy(isFavorited = !old.isFavorited, favoriteCount = old.favoriteCount + if (old.isFavorited) -1 else 1)
    }

    fun incrementShare(id: String): Result<FeedItem> = mutate(id) { old ->
        old.copy(shareCount = old.shareCount + 1)
    }

    fun fetchComments(postId: String): Result<List<FeedComment>> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val post = posts.firstOrNull { it.id == postId } ?: return Result.failure(NoSuchElementException("id=$postId"))
        return Result.success(post.comments)
    }

    fun addComment(postId: String, content: String, replyToNickname: String? = null): Result<FeedItem> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val c = content.trim()
        if (c.isEmpty()) return Result.failure(IllegalArgumentException("评论内容不能为空"))
        val idx = posts.indexOfFirst { it.id == postId }
        if (idx < 0) return Result.failure(NoSuchElementException("id=$postId"))
        val old = posts[idx]
        val comment = FeedComment("c_new_${old.comments.size + 1}_${seq++}", postId, "我", c, replyToNickname)
        val updated = old.copy(comments = listOf(comment) + old.comments, commentCount = old.commentCount + 1)
        posts[idx] = updated
        return Result.success(updated)
    }

    /** 发布（mock）：在列表头部插入一条我的动态。 */
    fun submitPost(content: String, topic: String? = null, mediaType: String = "none"): Result<FeedItem> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val c = content.trim()
        if (c.isEmpty()) return Result.failure(IllegalArgumentException("内容不能为空"))
        var body = c
        if (topic != null && !body.contains("#$topic")) {
            body = if (body.isEmpty()) "#$topic" else "$body\n#$topic"
        }
        val isImage = mediaType == "image"
        val isVideo = mediaType == "video"
        val id = "f_new_${seq++}"
        val item = FeedItem(
            id = id,
            title = body.lineSequence().firstOrNull()?.take(24) ?: body.take(24),
            body = body,
            content = body,
            userId = "me",
            author = "我",
            timeLabel = "刚刚",
            source = "来自 Kuikly",
            images = if (isImage) List(3) { j -> "https://picsum.photos/seed/wys_$j/400" } else emptyList(),
            videoUrl = if (isVideo) "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4" else null,
            videoCoverUrl = if (isVideo) "https://picsum.photos/seed/wys_video/640" else null,
            isMine = true,
            mediaType = mediaType,
            topic = topic,
            nearby = true,
        )
        posts.add(0, item)
        return Result.success(item)
    }

    fun topics(): Result<List<FeedTopic>> = Result.success(topicList)

    fun rankedTopics(): Result<List<FeedTopic>> =
        Result.success(topicList.sortedWith(compareByDescending<FeedTopic> { it.isAskEveryone }.thenByDescending { it.heat }))

    fun mentionCandidates(): Result<List<String>> = Result.success(mentionUsers)

    fun toggleFollow(userId: String): Result<Boolean> {
        if (followedUserIds.contains(userId)) followedUserIds.remove(userId)
        else followedUserIds.add(userId)
        return Result.success(followedUserIds.contains(userId))
    }

    private fun mutate(id: String, transform: (FeedItem) -> FeedItem): Result<FeedItem> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val idx = posts.indexOfFirst { it.id == id }
        if (idx < 0) return Result.failure(NoSuchElementException("id=$id"))
        val updated = transform(posts[idx])
        posts[idx] = updated
        return Result.success(updated)
    }
}

object FeedStore {
    val repo: FeedRepository = FeedRepository()
}
