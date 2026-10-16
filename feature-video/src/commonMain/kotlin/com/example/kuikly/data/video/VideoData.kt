package com.example.kuikly.data.video

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.Paged

enum class ShortVideoStatus { draft, published, reviewing }

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
    private val videos = listOf(
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
}

object VideoStore {
    val repo: VideoRepository = VideoRepository()
}
