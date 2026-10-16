package com.example.kuikly.data.community

import com.example.kuikly.data.BaseMockRepository

/** 社区帖子（port of source PostModel）。 */
data class Post(
    val id: String,
    val userId: String,
    val nickname: String,
    val avatar: String,
    val content: String,
    val publishTime: String,
    val source: String,
    val images: List<String>,
    val videoUrl: String?,
    val videoCoverUrl: String?,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val isMine: Boolean,
    val topicName: String?,
    val mediaType: String = "none",
) {
    val hasImages: Boolean get() = images.isNotEmpty()
    val hasVideo: Boolean get() = videoUrl != null && videoUrl.isNotEmpty()
    val mediaLabel: String
        get() = when (mediaType) {
            "image" -> "图片"
            "video" -> "视频"
            else -> "无媒体"
        }
}

/** 话题（port of source TopicModel）。 */
data class Topic(
    val id: String,
    val name: String,
    val heat: Int,
    val isAskEveryone: Boolean,
) {
    val displayName: String get() = "#$name"
    val heatLabel: String get() = if (heat >= 10000) "${heat / 10000}万" else heat.toString()
}

/** 搜索命中的用户（port of source CommunityUserHit）。 */
data class CommunityUser(
    val id: String,
    val nickname: String,
    val avatar: String,
    val isFollowed: Boolean,
)

/** 一次搜索的结果聚合：动态 / 话题 / 用户三段。 */
data class CommunitySearchResult(
    val query: String,
    val posts: List<Post>,
    val topics: List<Topic>,
    val users: List<CommunityUser>,
) {
    val isEmpty: Boolean get() = posts.isEmpty() && topics.isEmpty() && users.isEmpty()
    val hitCount: Int get() = posts.size + topics.size + users.size
}

class CommunityRepository : BaseMockRepository<Post>() {
    private val posts = listOf(
        Post("p1", "u1", "小鹿", "", "今天跑了 5 公里，状态不错～", "2024-05-01 08:12", "来自 iPhone",
            listOf("https://picsum.photos/seed/run/400"), null, null, 128, 12, false, false, "健身打卡"),
        Post("p2", "u2", "阿强", "", "新出的降噪耳机真香", "2024-05-02 19:30", "来自 Android",
            emptyList(), "https://v.example.com/clip.mp4", "https://picsum.photos/seed/ear/400", 56, 3, true, false, null),
        Post("p3", "u3", "产品汪", "", "分享一个效率工具清单", "2024-05-03 10:05", "来自 Web",
            listOf("https://picsum.photos/seed/tool/400", "https://picsum.photos/seed/tool2/400"), null, null, 312, 45, false, true, "效率"),
    )
    private val topicList = listOf(
        Topic("t1", "健身打卡", 120000, false),
        Topic("t2", "好物推荐", 88000, true),
        Topic("t3", "效率工具", 45000, false),
        Topic("t4", "早起打卡", 12000, false),
        Topic("t5", "读书笔记", 6800, false),
    )
    private val users = listOf(
        CommunityUser("u1", "小鹿", "", false),
        CommunityUser("u2", "阿强", "", true),
        CommunityUser("u3", "产品汪", "", false),
        CommunityUser("u6", "数码菌", "", true),
    )

    override fun seed(): List<Post> = posts
    override fun idOf(item: Post): String = item.id

    fun topics(): Result<List<Topic>> = Result.success(topicList)

    // 话题按"问大家"置顶 + 热度降序，与源工程 TopicSelectPage 排序一致。
    fun rankedTopics(): Result<List<Topic>> =
        Result.success(topicList.sortedWith(compareByDescending<Topic> { it.isAskEveryone }.thenByDescending { it.heat }))

    fun searchTopics(query: String): Result<List<Topic>> =
        Result.success(topicList.filter { it.name.contains(query.trim(), ignoreCase = true) })

    // 话题过滤：空/空白表示不过滤，返回全部。
    fun postsByTopic(topic: String?): Result<List<Post>> {
        val t = topic?.trim().orEmpty()
        return all { p -> t.isEmpty() || p.topicName == t }
    }

    // 帖子搜索：正文 / 昵称 / 话题命中（忽略大小写）。
    fun searchPosts(query: String): Result<List<Post>> = all { p -> matchesPost(p, query) }

    private fun matchesPost(post: Post, query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return post.content.contains(q, ignoreCase = true) ||
            post.nickname.contains(q, ignoreCase = true) ||
            post.topicName?.contains(q, ignoreCase = true) == true
    }

    fun searchUsers(query: String): Result<List<CommunityUser>> {
        val q = query.trim()
        return all { true }.map { users.filter { u -> q.isEmpty() || u.nickname.contains(q, ignoreCase = true) } }
    }

    // 综合搜索：一次返回动态 / 话题 / 用户三段结果。
    fun search(query: String): Result<CommunitySearchResult> {
        val q = query.trim()
        return all { true }.map {
            CommunitySearchResult(
                query = q,
                posts = posts.filter { p -> matchesPost(p, q) },
                topics = topicList.filter { t -> q.isNotEmpty() && t.name.contains(q, ignoreCase = true) },
                users = users.filter { u -> q.isNotEmpty() && u.nickname.contains(q, ignoreCase = true) },
            )
        }
    }

    fun publish(content: String, topic: String?): Result<Post> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("内容不能为空"))
        return Result.success(Post("p${posts.size + 1}", "u1", "我", "", content, "just now", "来自 Kuikly",
            emptyList(), null, null, 0, 0, false, true, topic))
    }

    fun toggleFollow(userId: String): Result<CommunityUser> {
        val target = users.firstOrNull { it.id == userId }
            ?: return Result.failure(NoSuchElementException("user=$userId"))
        return Result.success(target.copy(isFollowed = !target.isFollowed))
    }
}

object CommunityStore {
    val repo: CommunityRepository = CommunityRepository()
}
