package com.example.kuikly.data.video

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** Flutter `DubbingAlbumPart`（专辑分段：标题 + 角标「试听 / 付费」）。 */
data class VideoAlbumPart(
    val title: String,
    val badge: String? = null,
)

/** Flutter `DubbingLeaderboardEntry`（点赞榜条目）。 */
data class VideoLeaderboard(
    val userName: String,
    val date: String,
    val location: String,
    val likeCount: Int,
    val level: String,
)

data class VideoItem(
    val id: String,
    val title: String,
    val author: String,
    val summary: String,
    /** Flutter `DubbingVideoItem.tags`（列表卡取前 3 个渲染 chip）。 */
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val albumCount: Int = 0,
    val albumParts: List<VideoAlbumPart> = emptyList(),
    val leaderboard: VideoLeaderboard? = null,
    val subtitleEn: String = "",
    val subtitleZh: String = "",
)

interface VideoRepository {
    fun list(): Result<List<VideoItem>>
    fun detail(id: String): Result<VideoItem>
}

/**
 * Mock dubbing-video list + detail, mirroring the Flutter dubbing mock seed
 * (`features/video/lib/dubbing/mock/dubbing_media_mock_data.dart`: titles / descs /
 * tags / likeCount / 专辑分段 / 点赞榜；无真实播放器）。
 */
class FakeVideoRepository : VideoRepository {
    private val seed = listOf(
        VideoItem(
            id = "1",
            title = "恐龙科幻电影回归：《侏罗纪世界2：失落王国》电影预告",
            author = "小趣友宁Sir",
            summary = "经典科幻大片预告，适合练习口语节奏与情感表达。",
            tags = listOf("合作", "10句", "难度 PreA1", "漫威", "经典大片", "超级英雄"),
            likeCount = 3983,
            albumCount = 20,
            albumParts = listOf(
                VideoAlbumPart("制服牛油果小怪兽", "试听"),
                VideoAlbumPart("想到制服牛油果...", "付费"),
                VideoAlbumPart("顺利制服..."),
            ),
            leaderboard = LEADERBOARD,
            subtitleEn = SUBTITLE_EN,
            subtitleZh = SUBTITLE_ZH,
        ),
        VideoItem(
            id = "2",
            title = "【合作】制服牛油果小怪兽 Part 1",
            author = "唯有爱与美不可辜...",
            summary = "趣味动画片段，句子短、难度低，适合入门配音。",
            tags = listOf("合作", "8句", "难度 PreA2", "动画"),
            likeCount = 3483,
            albumCount = 20,
            albumParts = listOf(
                VideoAlbumPart("Part 1 【合作】制服牛油果小怪兽 Part 1", "试听"),
                VideoAlbumPart("Part 2 续集片段...", "付费"),
                VideoAlbumPart("顺利制服..."),
            ),
            leaderboard = LEADERBOARD,
            subtitleEn = SUBTITLE_EN,
            subtitleZh = SUBTITLE_ZH,
        ),
        VideoItem(
            id = "3",
            title = "哈利波特与魔法石 · 赫敏特辑（上）",
            author = "美诺明年夏天见",
            summary = "人物特辑片段，练习 RP 女音与角色语气。",
            tags = listOf("合作", "6句", "难度 PreA3", "科幻"),
            likeCount = 2983,
            albumCount = 20,
            albumParts = listOf(
                VideoAlbumPart("Part 1 哈利波特与魔法石 · 赫敏特辑（上）", "试听"),
                VideoAlbumPart("Part 2 续集片段..."),
                VideoAlbumPart("顺利制服..."),
            ),
            leaderboard = LEADERBOARD,
            subtitleEn = SUBTITLE_EN,
            subtitleZh = SUBTITLE_ZH,
        ),
    )

    override fun list(): Result<List<VideoItem>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock video list error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun detail(id: String): Result<VideoItem> {
        when (MockBackend.scenario) {
            MockScenario.Error -> return Result.failure(IllegalStateException("mock video detail error"))
            MockScenario.Unauthorized -> return Result.failure(IllegalStateException("unauthorized"))
            else -> Unit
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no video $id"))
    }

    private companion object {
        const val SUBTITLE_EN = "It's said they can accelerate faster than a Ferrari."
        const val SUBTITLE_ZH = "据说他们能比法拉利更快地加速"
        val LEADERBOARD = VideoLeaderboard(
            userName = "美诺明年夏天见",
            date = "2020-11-03",
            location = "杭州市",
            likeCount = 11000,
            level = "V5",
        )
    }
}

object VideoStore {
    val repo: VideoRepository = FakeVideoRepository()
}
