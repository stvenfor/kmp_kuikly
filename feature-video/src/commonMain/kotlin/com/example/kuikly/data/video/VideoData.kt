package com.example.kuikly.data.video

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.Paged

enum class ShortVideoStatus { draft, published, reviewing }

/** 视频作者主页资料（port of source `ShortVideoProfileModel`）。 */
data class VideoProfile(
    val displayName: String,
    val avatarUrl: String,
    val roleBadge: String,
    val storeName: String,
    val isMe: Boolean,
    val stats: VideoProfileStats,
)

/** 视频资料统计（port of source `ShortVideoStatsModel`）。 */
data class VideoProfileStats(
    val videoCount: Int,
    val viewCount: Int,
    val likeCount: Int,
)

/** 视频评论（port of source 评论列表）。 */
data class VideoComment(
    val id: String,
    val authorName: String,
    val content: String,
    val likeCount: Int,
    val createdAt: String,
)

/** 视频专辑分集（port of source DubbingAlbumPart）。 */
data class VideoAlbumPart(
    val id: String,
    val title: String,
    val badge: String? = null,
)

/** 短视频（port of source ShortVideoItemModel + DubbingVideoItem）。 */
data class ShortVideo(
    val id: String,
    val title: String,
    val coverUrl: String,
    val videoUrl: String,
    val viewCount: Int,
    val likeCount: Int,
    val duration: String,
    val aspectRatio: Double,
    val status: ShortVideoStatus,
    val isLiked: Boolean,
    val isMine: Boolean,
    val nickname: String,
    val topicName: String?,
    val desc: String = "",
    val category: String = "综合",
    val dislikeCount: Int = 0,
    val sentenceCount: Int = 0,
    val difficulty: String = "PreA1",
    val uploaderName: String = "",
    val subtitleEn: String = "",
    val subtitleZh: String = "",
    val albumCount: Int = 0,
    val tags: List<String> = emptyList(),
    val albumParts: List<VideoAlbumPart> = emptyList(),
    val commentCount: Int = 0,
) {
    val uploaderLabel: String get() = if (uploaderName.isBlank()) nickname else uploaderName

    val viewLabel: String get() = compactCount(viewCount)

    val likeLabel: String get() = compactCount(likeCount)

    val durationSec: Int
        get() {
            val parts = duration.split(":")
            if (parts.size < 2) return 0
            val m = parts[0].toIntOrNull() ?: 0
            val s = parts[1].toIntOrNull() ?: 0
            return m * 60 + s
        }
}

/** 配音任务（port of source DubbingTask）。 */
data class DubbingTask(
    val id: String,
    val videoId: String,
    val lang: String,
    val progress: Double,
    val status: String,
    val videoTitle: String = "",
) {
    val progressLabel: String get() = "${(progress.coerceIn(0.0, 1.0) * 100).toInt()}%"

    val statusLabel: String
        get() = when (status) {
            "pending" -> "排队中"
            "running" -> "合成中"
            "done" -> "已完成"
            "failed" -> "失败"
            else -> status
        }

    val langLabel: String
        get() = when (lang) {
            "en" -> "英语"
            "ja" -> "日语"
            "ko" -> "韩语"
            "zh" -> "中文"
            else -> lang
        }
}

private fun compactCount(n: Int): String = when {
    n >= 10000 -> "${"%.1f".format(n / 10000.0)}w"
    n >= 1000 -> "${"%.1f".format(n / 1000.0)}k"
    else -> "$n"
}

class VideoRepository : BaseMockRepository<ShortVideo>() {
    private val videos = mutableListOf(
        ShortVideo(
            id = "v1",
            title = "露营的一天",
            coverUrl = "https://picsum.photos/seed/v1/400",
            videoUrl = "https://v/x1.mp4",
            viewCount = 12000,
            likeCount = 320,
            duration = "00:38",
            aspectRatio = 0.5625,
            status = ShortVideoStatus.published,
            isLiked = true,
            isMine = true,
            nickname = "户外小张",
            topicName = "露营",
            desc = "周末带娃去郊区露营，从搭帐篷到生火做饭的完整记录，适合练习生活类旁白。",
            category = "生活",
            dislikeCount = 12,
            sentenceCount = 10,
            difficulty = "PreA1",
            uploaderName = "户外小张",
            subtitleEn = "We set up the tent before it got dark.",
            subtitleZh = "我们在天黑前把帐篷搭好了",
            albumCount = 3,
            tags = listOf("生活", "10句", "难度 PreA1", "户外"),
            albumParts = listOf(
                VideoAlbumPart("p11", "搭帐篷", "试听"),
                VideoAlbumPart("p12", "生火做饭", "付费"),
                VideoAlbumPart("p13", "星空夜话"),
            ),
        ),
        ShortVideo(
            id = "v2",
            title = "十分钟搞定早餐",
            coverUrl = "https://picsum.photos/seed/v2/400",
            videoUrl = "https://v/x2.mp4",
            viewCount = 45000,
            likeCount = 980,
            duration = "02:10",
            aspectRatio = 0.5625,
            status = ShortVideoStatus.published,
            isLiked = false,
            isMine = false,
            nickname = "厨房日记",
            topicName = "美食",
            desc = "三款快手早餐，十分钟上桌，句子短、语速快，适合入门跟读。",
            category = "美食",
            dislikeCount = 5,
            sentenceCount = 8,
            difficulty = "PreA2",
            uploaderName = "厨房日记",
            subtitleEn = "Ten minutes is all you need.",
            subtitleZh = "十分钟就够了",
            albumCount = 2,
            tags = listOf("美食", "8句", "难度 PreA2", "跟读"),
            albumParts = listOf(
                VideoAlbumPart("p21", "鸡蛋三明治", "试听"),
                VideoAlbumPart("p22", "燕麦杯"),
            ),
        ),
        ShortVideo(
            id = "v3",
            title = "城市夜骑",
            coverUrl = "https://picsum.photos/seed/v3/400",
            videoUrl = "https://v/x3.mp4",
            viewCount = 8900,
            likeCount = 210,
            duration = "01:05",
            aspectRatio = 0.5625,
            status = ShortVideoStatus.reviewing,
            isLiked = false,
            isMine = true,
            nickname = "我",
            topicName = null,
            desc = "沿着江边骑了 20 公里，记录城市夜景与骑行节奏，配音尚未过审。",
            category = "运动",
            dislikeCount = 3,
            sentenceCount = 6,
            difficulty = "PreA3",
            uploaderName = "我",
            subtitleEn = "The city looks different at night.",
            subtitleZh = "这座城市在夜里是另一种样子",
            albumCount = 1,
            tags = listOf("运动", "6句", "难度 PreA3", "Vlog"),
            albumParts = listOf(
                VideoAlbumPart("p31", "江边路线", "试听"),
            ),
        ),
        ShortVideo(
            id = "v4",
            title = "恐龙科幻电影回归预告",
            coverUrl = "https://picsum.photos/seed/v4/400",
            videoUrl = "https://v/x4.mp4",
            viewCount = 210000,
            likeCount = 3983,
            duration = "01:42",
            aspectRatio = 0.5625,
            status = ShortVideoStatus.published,
            isLiked = false,
            isMine = false,
            nickname = "小趣友宁Sir",
            topicName = "影视",
            desc = "经典科幻大片预告，适合练习口语节奏与情感表达。",
            category = "影视",
            dislikeCount = 18,
            sentenceCount = 12,
            difficulty = "PreB1",
            uploaderName = "小趣友宁Sir",
            subtitleEn = "It's said they can accelerate faster than a Ferrari.",
            subtitleZh = "据说他们能比法拉利更快地加速",
            albumCount = 4,
            tags = listOf("合作", "12句", "难度 PreB1", "漫威", "经典大片", "超级英雄"),
            albumParts = listOf(
                VideoAlbumPart("p41", "开场独白", "试听"),
                VideoAlbumPart("p42", "追逐片段", "付费"),
                VideoAlbumPart("p43", "高潮对白"),
                VideoAlbumPart("p44", "彩蛋"),
            ),
        ),
        ShortVideo(
            id = "v5",
            title = "哈利波特与魔法石 · 赫敏特辑",
            coverUrl = "https://picsum.photos/seed/v5/400",
            videoUrl = "https://v/x5.mp4",
            viewCount = 76000,
            likeCount = 1540,
            duration = "03:20",
            aspectRatio = 0.5625,
            status = ShortVideoStatus.draft,
            isLiked = true,
            isMine = false,
            nickname = "唯有爱与美不可辜负",
            topicName = "影视",
            desc = "人物特辑片段，练习 RP 女音与角色语气，草稿状态暂不对外。",
            category = "影视",
            dislikeCount = 7,
            sentenceCount = 9,
            difficulty = "PreB2",
            uploaderName = "唯有爱与美不可辜负",
            subtitleEn = "It is LeviOsa, not LevioSA!",
            subtitleZh = "是 levioSA，不是 levioSA！",
            albumCount = 2,
            tags = listOf("影视", "9句", "难度 PreB2", "角色配音"),
            albumParts = listOf(
                VideoAlbumPart("p51", "魔药课", "试听"),
                VideoAlbumPart("p52", "魔法石终章"),
            ),
        ),
    )

    private val favoriteMap = mutableMapOf<String, Boolean>()
    private val viewBump = mutableMapOf<String, Int>()
    private val commentMap = mutableMapOf<String, MutableList<VideoComment>>()

    private val dubbing = mutableListOf(
        DubbingTask("d1", "v3", "en", 0.6, "running", "城市夜骑"),
        DubbingTask("d2", "v1", "ja", 1.0, "done", "露营的一天"),
    )

    private val progress = mutableMapOf<String, Double>()
    private val playedSec = mutableMapOf<String, Int>()

    override fun seed(): List<ShortVideo> = videos
    override fun idOf(item: ShortVideo): String = item.id

    fun fetchPage(pageIndex: Int, pageSize: Int = 2): Result<Paged<ShortVideo>> = page(pageIndex, pageSize)

    fun fetchById(id: String): Result<ShortVideo> = byId(id)

    fun progressOf(id: String): Double = progress[id] ?: 0.0

    fun playedSecOf(id: String): Int = playedSec[id] ?: 0

    /** 记录播放进度，返回 0..1 的观看比例；越界会被裁剪。 */
    fun saveProgress(id: String, sec: Int): Result<Double> {
        val video = videos.find { it.id == id }
            ?: return Result.failure(NoSuchElementException("video=$id"))
        val total = if (video.durationSec > 0) video.durationSec else 1
        val clamped = sec.coerceIn(0, total)
        playedSec[id] = clamped
        val ratio = clamped.toDouble() / total.toDouble()
        progress[id] = ratio
        return Result.success(ratio)
    }

    fun dubbingTasks(videoId: String? = null): Result<List<DubbingTask>> {
        val list = if (videoId == null) dubbing else dubbing.filter { it.videoId == videoId }
        return Result.success(list)
    }

    fun dubbingTask(id: String): Result<DubbingTask> =
        dubbing.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("task=$id"))

    /** 新建配音任务；视频不存在时失败。 */
    fun startDubbing(videoId: String, lang: String): Result<DubbingTask> {
        val video = videos.find { it.id == videoId }
            ?: return Result.failure(NoSuchElementException("video=$videoId"))
        val task = DubbingTask(
            id = "d${dubbing.size + 1}_$videoId",
            videoId = videoId,
            lang = lang,
            progress = 0.0,
            status = "pending",
            videoTitle = video.title,
        )
        dubbing.add(0, task)
        return Result.success(task)
    }

    /** 推进配音任务进度，1.0 之后标记为 done。 */
    fun advanceDubbing(id: String, step: Double = 0.25): Result<DubbingTask> {
        val index = dubbing.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("task=$id"))
        val old = dubbing[index]
        val next = (old.progress + step).coerceIn(0.0, 1.0)
        val updated = old.copy(
            progress = next,
            status = if (next >= 1.0) "done" else "running",
        )
        dubbing[index] = updated
        return Result.success(updated)
    }

    // ── 点赞 / 收藏（port of source toggleLike + 收藏） ──

    /** 切换点赞；返回更新后的视频（likeCount 与 isLiked 同步）。 */
    fun toggleLike(id: String, liked: Boolean): Result<ShortVideo> {
        val index = videos.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("video=$id"))
        val old = videos[index]
        val delta = when {
            liked && !old.isLiked -> 1
            !liked && old.isLiked -> -1
            else -> 0
        }
        val updated = old.copy(isLiked = liked, likeCount = (old.likeCount + delta).coerceAtLeast(0))
        videos[index] = updated
        return Result.success(updated)
    }

    /** 切换收藏；返回切换后的状态。 */
    fun favorite(id: String): Result<Boolean> {
        if (videos.none { it.id == id }) return Result.failure(NoSuchElementException("video=$id"))
        val next = !(favoriteMap[id] ?: false)
        favoriteMap[id] = next
        return Result.success(next)
    }

    fun isFavorited(id: String): Boolean = favoriteMap[id] ?: false

    /** 上报播放，播放量 +1（port of source reportView）。 */
    fun reportView(id: String): Result<Int> {
        val index = videos.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("video=$id"))
        val old = videos[index]
        val updated = old.copy(viewCount = old.viewCount + 1)
        videos[index] = updated
        return Result.success(updated.viewCount)
    }

    // ── 上传 / 删除（port of source create / delete） ──

    /** 发布小视频（mock 上传：标题校验 + 默认片源，状态置为审核中）。 */
    fun publish(title: String, topicId: String? = null): Result<ShortVideo> {
        val name = title.trim()
        if (name.isEmpty()) return Result.failure(IllegalArgumentException("标题不能为空"))
        if (name.length > 50) return Result.failure(IllegalArgumentException("标题最多 50 字"))
        val id = "v_up_${videos.size + 1}"
        val item = ShortVideo(
            id = id,
            title = name,
            coverUrl = "https://picsum.photos/seed/$id/400",
            videoUrl = "https://v/$id.mp4",
            viewCount = 0,
            likeCount = 0,
            duration = "00:15",
            aspectRatio = 0.5625,
            status = ShortVideoStatus.reviewing,
            isLiked = false,
            isMine = true,
            nickname = "我",
            topicName = null,
            desc = "由我发布的小视频（mock 上传，使用默认片源，封面仅供预览）。",
            category = "我的",
            dislikeCount = 0,
            sentenceCount = 0,
            difficulty = "PreA1",
            uploaderName = "我",
            subtitleEn = "",
            subtitleZh = "",
            albumCount = 0,
            tags = if (topicId.isNullOrBlank()) listOf("我的") else listOf("我的", topicId),
            albumParts = emptyList(),
            commentCount = 0,
        )
        videos.add(0, item)
        return Result.success(item)
    }

    fun delete(id: String): Result<Unit> {
        val removed = videos.removeIf { it.id == id }
        if (!removed) return Result.failure(NoSuchElementException("video=$id"))
        favoriteMap.remove(id)
        commentMap.remove(id)
        return Result.success(Unit)
    }

    // ── 个人资料（port of source fetchProfile） ──

    fun fetchProfile(): Result<VideoProfile> {
        val mine = videos.filter { it.isMine }
        val stats = VideoProfileStats(
            videoCount = mine.size,
            viewCount = mine.sumOf { it.viewCount },
            likeCount = mine.sumOf { it.likeCount },
        )
        return Result.success(
            VideoProfile(
                displayName = "我",
                avatarUrl = "https://picsum.photos/seed/mine_profile/200",
                roleBadge = "销售顾问",
                storeName = "[4S]北京沃德龙鼎吉利",
                isMe = true,
                stats = stats,
            ),
        )
    }

    // ── 推荐 / 历史（port of source 猜你喜欢 / 观看历史） ──

    fun recommend(): Result<List<ShortVideo>> = Result.success(videos.filter { !it.isMine })

    fun history(): Result<List<ShortVideo>> =
        Result.success(videos.filter { progressOf(it.id) > 0.0 || playedSecOf(it.id) > 0 })

    // ── 我的视频（port of source ShortVideoPage gridItems / profile 关联） ──

    /** 仅返回本人发布的视频（isMine），用于资料页「我的视频」管理。 */
    fun myVideos(): Result<List<ShortVideo>> = Result.success(videos.filter { it.isMine })

    // ── 点赞榜（port of source DubbingVideoDetailPage 点赞榜 section） ──

    /**
     * 视频点赞榜。源的 `DubbingLeaderboardEntry`（rank/avatarUrl/level 等）在本工程以
     * 轻量 `VideoLeaderboard` 承载；此处返回固定 mock 榜单，level 字段映射源的 V 等级。
     */
    fun leaderboard(videoId: String = ""): Result<List<VideoLeaderboard>> {
        val seed = listOf(
            VideoLeaderboard("美诺明年夏天见", "2020-11-03", "杭州市", 11000, "V5"),
            VideoLeaderboard("小趣友宁Sir", "2020-11-02", "北京", 9200, "V4"),
            VideoLeaderboard("唯有爱与美不可辜负", "2020-11-01", "上海", 8100, "V4"),
        )
        return Result.success(seed)
    }

    // ── 评论（port of source 评论列表） ──

    fun comments(videoId: String): Result<List<VideoComment>> {
        val list = commentMap.getOrPut(videoId) { mutableListOf() }
        return Result.success(list.toList())
    }

    fun addComment(videoId: String, content: String, authorName: String = "我"): Result<VideoComment> {
        val text = content.trim()
        if (text.isEmpty()) return Result.failure(IllegalArgumentException("评论内容不能为空"))
        val list = commentMap.getOrPut(videoId) { mutableListOf() }
        val comment = VideoComment(
            id = "c_${list.size + 1}_$videoId",
            authorName = authorName,
            content = text,
            likeCount = 0,
            createdAt = "刚刚",
        )
        list.add(0, comment)
        val index = videos.indexOfFirst { it.id == videoId }
        if (index >= 0) videos[index] = videos[index].copy(commentCount = videos[index].commentCount + 1)
        return Result.success(comment)
    }
}

object VideoStore {
    val repo: VideoRepository = VideoRepository()
}
