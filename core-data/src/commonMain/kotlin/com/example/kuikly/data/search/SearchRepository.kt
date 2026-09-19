package com.example.kuikly.data.search

data class SearchRankItem(
    val id: String,
    val rank: Int,
    val title: String,
    val subtitle: String,
    val coverUrl: String,
)

enum class SearchRankTab(val key: String, val label: String) {
    HOT_DUBBING("hot_dubbing", "热配榜"),
    READING("reading", "诵读榜"),
    SERIES("series", "剧集榜"),
    RECORD("record", "记录榜"),
    COLLABORATION("collaboration", "合作榜"),
}

interface SearchRepository {
    fun history(): List<String>
    fun discovery(): List<String>
    fun filterTags(): List<String>
    fun rankTabs(): List<SearchRankTab>
    fun rankListForTab(tab: SearchRankTab): List<SearchRankItem>
}

class FakeSearchRepository : SearchRepository {
    override fun history(): List<String> = listOf(
        "极限文字一排两个显示",
        "极限文字超出九个字...",
        "宫崎骏宫漫作品",
        "龙猫",
        "闪光少女",
    )

    override fun discovery(): List<String> = listOf(
        "罗振宇2026跨年演讲",
        "极限文字超出九个字...",
        "小猪佩奇全系列",
        "百家讲坛全集",
        "罗振宇2026跨年演讲",
        "百家讲坛明朝",
    )

    override fun filterTags(): List<String> = listOf(
        "3-5 个句子的配音",
        "较慢的语速",
        "初级难度",
        "女声",
        "1 分钟以内的视频",
    )

    override fun rankTabs(): List<SearchRankTab> = SearchRankTab.entries.toList()

    override fun rankListForTab(tab: SearchRankTab): List<SearchRankItem> = when (tab) {
        SearchRankTab.HOT_DUBBING -> buildList(
            tab,
            titles = listOf(
                "穿条纹睡衣的男孩",
                "蛮荒故事",
                "爱冒险的朵拉",
                "小王子",
                "寻梦环游记",
                "飞屋环游记",
                "头脑特工队",
                "疯狂动物城",
            ),
            subtitles = listOf(
                "某日布鲁诺决定，去铁丝网的另外...",
                "六个独立故事，荒诞与黑色幽默交织...",
                "和朵拉一起开启奇妙冒险之旅...",
                "来自 B612 小行星的小王子...",
                "米格在亡灵节追寻音乐梦想...",
                "卡尔用气球带着房子去冒险...",
                "情绪小人在大脑里协作成长...",
                "兔子警官与狐狸搭档破案...",
            ),
        )
        SearchRankTab.READING -> buildList(
            tab,
            titles = listOf("静夜思", "春晓", "登鹳雀楼", "望庐山瀑布", "悯农"),
            subtitles = listOf(
                "床前明月光，疑是地上霜...",
                "春眠不觉晓，处处闻啼鸟...",
                "白日依山尽，黄河入海流...",
                "日照香炉生紫烟，遥看瀑布挂前川...",
                "锄禾日当午，汗滴禾下土...",
            ),
        )
        SearchRankTab.SERIES -> buildList(
            tab,
            titles = listOf(
                "小猪佩奇",
                "汪汪队立大功",
                "超级飞侠",
                "熊出没",
                "喜羊羊与灰太狼",
            ),
            subtitles = listOf(
                "佩奇和乔治的日常生活...",
                "莱德队长带领狗狗们救援...",
                "乐迪环游世界送包裹...",
                "熊大熊二与光头强的故事...",
                "羊村与狼堡的欢乐对决...",
            ),
        )
        SearchRankTab.RECORD -> buildList(
            tab,
            titles = listOf(
                "我的第一次配音",
                "英语朗读打卡",
                "亲子共读记录",
                "班级作业精选",
                "周末练习成果",
            ),
            subtitles = listOf(
                "完成度 98%，发音清晰自然...",
                "连续打卡 30 天，进步明显...",
                "与孩子一起完成的温馨朗读...",
                "老师推荐的优秀作业展示...",
                "周末集中练习的成果汇总...",
            ),
        )
        SearchRankTab.COLLABORATION -> buildList(
            tab,
            titles = listOf(
                "BBC 合作专区",
                "迪士尼经典合作",
                "国家地理探索",
                "牛津阅读树",
                "剑桥少儿英语",
            ),
            subtitles = listOf(
                "BBC 精选纪录片配音素材...",
                "迪士尼动画经典片段...",
                "探索自然与科学的配音...",
                "分级阅读配套配音练习...",
                "剑桥体系标准发音示范...",
            ),
        )
    }

    private fun buildList(
        tab: SearchRankTab,
        titles: List<String>,
        subtitles: List<String>,
    ): List<SearchRankItem> = titles.mapIndexed { index, title ->
        SearchRankItem(
            id = "${tab.key}_$index",
            rank = index + 1,
            title = title,
            subtitle = subtitles[index],
            coverUrl = COVER_URLS[index % COVER_URLS.size],
        )
    }

    companion object {
        const val SEARCH_PLACEHOLDER = "搜索客户、订单、课程、视频"

        private val COVER_URLS = listOf(
            "https://picsum.photos/seed/search-rank-1/144/144",
            "https://picsum.photos/seed/search-rank-2/144/144",
            "https://picsum.photos/seed/search-rank-3/144/144",
            "https://picsum.photos/seed/search-rank-4/144/144",
            "https://picsum.photos/seed/search-rank-5/144/144",
            "https://picsum.photos/seed/search-rank-6/144/144",
            "https://picsum.photos/seed/search-rank-7/144/144",
            "https://picsum.photos/seed/search-rank-8/144/144",
        )
    }
}

object SearchStore {
    val repo: SearchRepository = FakeSearchRepository()
}
