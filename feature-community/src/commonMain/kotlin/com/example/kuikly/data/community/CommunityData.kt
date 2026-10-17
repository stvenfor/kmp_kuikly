package com.example.kuikly.data.community

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.Paged
import com.example.kuikly.data.mock.MockScenario
import kotlin.math.max

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
    val isAskEveryone: Boolean = false,
    val topicId: String? = null,
    val previewComments: List<Comment> = emptyList(),
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

/** 评论（port of source CommentModel）。 */
data class Comment(
    val id: String,
    val postId: String,
    val nickname: String,
    val avatar: String,
    val content: String,
    val createTime: String,
    val replyToNickname: String? = null,
)

/** 话题（port of source TopicModel）。 */
data class Topic(
    val id: String,
    val name: String,
    val heat: Int,
    val isAskEveryone: Boolean,
) {
    val displayName: String get() = "#$name"
    // 源 TopicModel.heatLabel：≥1万 用「x.x万」，整数万不显示小数。
    val heatLabel: String
        get() {
            if (heat < 10000) return heat.toString()
            val wan = heat / 10000
            val rem = heat % 10000
            return if (rem == 0) "${wan}万" else "${wan}.${rem / 1000}万"
        }
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
    // 可变：点赞 / 删除需在原地改写（对齐源 MockPostRepository 的 _allPosts 可变列表）。
    private val posts = mutableListOf(
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

    // 评论存储（对齐源 MockPostRepository._commentsByPost：帖子 id -> 评论列表，原地增删）。
    private val commentsByPost = mutableMapOf<String, MutableList<Comment>>()
    private var commentSeq = 0

    // 关注集合（对齐源 MockPostRepository._followedUserIds；"关注" Tab 数据源）。
    // 初始为空，与源一致：需用户关注后"关注"流才有内容（演示空态）。
    private val followedUserIds = mutableSetOf<String>()

    // 每条帖子种子 2 条预览评论（对齐源 _seedComments），并回填 Post.previewComments。
    init {
        posts.forEach { p ->
            val cs = listOf(
                Comment("c_${p.id}_1", p.id, "读者A", "", "说得对！", "2分钟前", null),
                Comment("c_${p.id}_2", p.id, "读者B", "", "同感 +1", "1分钟前", p.nickname),
            )
            commentsByPost[p.id] = cs.toMutableList()
        }
        for (i in posts.indices) {
            val p = posts[i]
            posts[i] = p.copy(previewComments = commentsByPost[p.id]?.take(2) ?: emptyList())
        }
    }

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
    /** 帖子详情（自 demo-skeleton 并入的 PostDetail 页用）。 */
    fun postDetail(id: String): Result<Post> = byId(id)

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

    // 对齐源 createPost：选中话题且正文未含该话题时，把 "#话题" 追加到正文；
    // isAskEveryone 由话题或显式参数决定，并回写 topicId。
    fun publish(content: String, topic: String? = null, isAskEveryone: Boolean = false): Result<Post> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("内容不能为空"))
        var body = content.trim()
        val t = topic?.trim().orEmpty()
        val topicObj = if (t.isNotEmpty()) topicList.firstOrNull { it.name == t } else null
        if (topicObj != null && !body.contains("#${topicObj.name}")) {
            body = if (body.isEmpty()) "#${topicObj.name}" else "$body\n#${topicObj.name}"
        }
        val ask = isAskEveryone || (topicObj?.isAskEveryone ?: false)
        return Result.success(
            Post(
                "p${posts.size + 1}", "u1", "我", "", body, "刚刚", "来自 Kuikly",
                emptyList(), null, null, 0, 0, false, true,
                if (topicObj == null) null else topicObj.name,
                previewComments = emptyList(),
                isAskEveryone = ask,
                topicId = topicObj?.id,
            ),
        )
    }

    // 点赞翻转（对齐源 toggleLike(postId, liked)：原地改写 likeCount / isLiked）。
    fun toggleLike(postId: String, liked: Boolean): Result<Post> {
        val idx = posts.indexOfFirst { it.id == postId }
        if (idx < 0) return Result.failure(NoSuchElementException("post=$postId"))
        val old = posts[idx]
        val updated = old.copy(
            isLiked = liked,
            likeCount = max(0, old.likeCount + if (liked) 1 else -1),
        )
        posts[idx] = updated
        return Result.success(updated)
    }

    // 删帖（对齐源 deletePost）。
    fun deletePost(postId: String): Result<Unit> {
        val removed = posts.removeIf { it.id == postId }
        return if (removed) Result.success(Unit) else Result.failure(NoSuchElementException("post=$postId"))
    }

    // 评论数回写（供旧调用同步，对齐源 addComment 增量）。
    fun incrementCommentCount(postId: String) {
        val idx = posts.indexOfFirst { it.id == postId }
        if (idx >= 0) posts[idx] = posts[idx].copy(commentCount = posts[idx].commentCount + 1)
    }

    // 拉取某帖评论（对齐源 fetchComments）。Error 场景直接失败；其余返回已存评论（含种子预览）。
    fun comments(postId: String): Result<List<Comment>> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        return Result.success(commentsByPost[postId]?.toList() ?: emptyList())
    }

    // 发评论（对齐源 addComment）：插入到帖子评论列表头部，回写评论数与预览评论（取前 2 条）。
    fun addComment(postId: String, content: String, replyToNickname: String? = null): Result<List<Comment>> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("评论内容不能为空"))
        commentSeq++
        val comment = Comment(
            id = "c_new_$commentSeq",
            postId = postId,
            nickname = "我",
            avatar = "",
            content = content.trim(),
            createTime = "刚刚",
            replyToNickname = replyToNickname,
        )
        val list = commentsByPost.getOrPut(postId) { mutableListOf() }
        list.add(0, comment)
        val idx = posts.indexOfFirst { it.id == postId }
        if (idx >= 0) {
            val p = posts[idx]
            posts[idx] = p.copy(
                commentCount = p.commentCount + 1,
                previewComments = list.take(2),
            )
        }
        return Result.success(list.toList())
    }

    // 信息流分页（对齐源 fetchPosts：tab=latest/hot/following + 页码分页 + 排序/筛选）。
    // 手动处理四态：Error -> 失败；Empty -> 空列表；Success/Slow -> 排序后切片。
    fun feed(tab: String, page: Int, pageSize: Int = 10): Result<Paged<Post>> {
        if (scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val base = if (scenario == MockScenario.Empty) emptyList() else sortForTab(tab)
        val from = (page - 1).coerceAtLeast(0) * pageSize
        val to = (from + pageSize).coerceAtMost(base.size)
        val slice = if (from <= base.size) base.subList(from, to) else emptyList()
        return Result.success(Paged(slice, base.size, page, pageSize))
    }

    private fun sortForTab(tab: String): List<Post> = when (tab) {
        "hot" -> posts.sortedWith(
            compareByDescending<Post> { it.likeCount * 2 + it.commentCount }
                .thenByDescending { it.publishTime },
        )
        "following" -> posts.filter { followedUserIds.contains(it.userId) }
        else -> posts.toList() // latest：seed 顺序（较新在前）
    }

    fun followUser(userId: String): Result<Unit> {
        followedUserIds.add(userId)
        return Result.success(Unit)
    }

    fun unfollowUser(userId: String): Result<Unit> {
        followedUserIds.remove(userId)
        return Result.success(Unit)
    }

    fun isFollowing(userId: String): Boolean = followedUserIds.contains(userId)

    // 翻转关注（对齐源 followUser/unfollowUser 的持久集合语义；供搜索页复用）。
    fun toggleFollow(userId: String): Result<CommunityUser> {
        val target = users.firstOrNull { it.id == userId }
            ?: return Result.failure(NoSuchElementException("user=$userId"))
        val nowFollowed = !followedUserIds.contains(userId)
        if (nowFollowed) followedUserIds.add(userId) else followedUserIds.remove(userId)
        return Result.success(target.copy(isFollowed = nowFollowed))
    }
}

object CommunityStore {
    val repo: CommunityRepository = CommunityRepository()
}
