package com.example.kuikly.data.bfui

import com.example.kuikly.data.BaseMockRepository

/** UI 组件展示项（port of source bfui demo screens）。 */
data class BfuiShowcase(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
)

class BfuiRepository : BaseMockRepository<BfuiShowcase>() {
    private val items = listOf(
        BfuiShowcase("bottom_nav", "底部导航栏", "多 Tab 切换容器", "navigation"),
        BfuiShowcase("title_view", "标题卡片", "带副标题的区块标题", "card"),
        BfuiShowcase("glass_view", "毛玻璃卡片", "半透明叠加层", "card"),
        BfuiShowcase("running_view", "进度环", "运动/任务完成度", "chart"),
        BfuiShowcase("water_view", "水量跟踪", "每日饮水记录", "chart"),
        BfuiShowcase("mediterranean", "饮食视图", "健康饮食卡片", "card"),
        BfuiShowcase("invite_friend", "邀请好友", "分享裂变入口", "action"),
        BfuiShowcase("help", "帮助中心", "FAQ 与反馈", "action"),
        BfuiShowcase("introduction", "引导动画", "首启引导流程", "animation"),
        BfuiShowcase("hotel", "酒店预订", "日历/房型选择", "business"),
    )

    override fun seed(): List<BfuiShowcase> = items
    override fun idOf(item: BfuiShowcase): String = item.id
}

object BfuiStore {
    val repo: BfuiRepository = BfuiRepository()
}
