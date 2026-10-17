package com.example.kuikly.data.feed

import com.example.kuikly.data.BaseMockRepository

/**
 * 信息流帖子（port of source `FeedPost`/`Post`）。
 * 字段对齐源 post 模型：`author`→author、`title`→title、`body`→content。
 */
data class FeedItem(
    val id: String,
    val title: String,
    val body: String,
    val author: String = "匿名",
    val timeLabel: String = "",
)

class FeedRepository : BaseMockRepository<FeedItem>() {
    private val items = listOf(
        FeedItem("f1", "英语启蒙第 6 单元跟读打卡", "今天完成了 Unit 6 的单词跟读，发音比上周稳多了，继续加油！", "张敏", "2 小时前"),
        FeedItem("f2", "趣味数学口算小挑战", "两位数乘法闯关拿到满分，老师表扬了，分享一下喜悦～", "李雷", "5 小时前"),
        FeedItem("f3", "少儿编程第一个小游戏", "用积木块搭出了会动的小猫，孩子特别有成就感。", "韩梅梅", "昨天"),
        FeedItem("f4", "每日晨读打卡第 21 天", "坚持阅读习惯养成中，明天就能解锁勋章啦。", "王妈妈", "昨天"),
        FeedItem("f5", "配音作业《蜘蛛侠》满分", "模仿蜘蛛侠的英雄归来台词，得分 100，可以重新挑战更高难度。", "酸菜", "2 天前"),
    )

    override fun seed(): List<FeedItem> = items
    override fun idOf(item: FeedItem): String = item.id

    /** 信息流列表；Empty 场景返回空表，Error 场景返回失败（对齐 BaseMockRepository 4 态）。 */
    fun list(): Result<List<FeedItem>> = all { true }

    /** 帖子详情；找不到或错误场景返回失败。 */
    fun detail(id: String): Result<FeedItem> = byId(id)
}

object FeedStore {
    val repo: FeedRepository = FeedRepository()
}
