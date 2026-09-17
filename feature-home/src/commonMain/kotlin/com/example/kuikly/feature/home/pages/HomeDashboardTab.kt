package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.LocalDesignScale
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.navigation.LoginRedirect
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * Flutter「首页」dashboard 的 Kuikly Compose 复刻（Phase-2 / P2-01）。
 *
 * 真源：`my_ai_project/features/home/lib/home/view/home_page.dart` + `view/widgets/`。
 * - 视觉令牌镜像 `home/theme/home_dashboard_theme.dart`（accent `#007AFF` / 背景 `#F2F2F7`）。
 * - 静态文案镜像 `home/repository/home_repository.dart`（Mock seed 的 store/features/…）。
 * - Mock 无图片加载器：所有 `CacheImageUtils` 位统一渲染为 **Flutter golden 里的未加载占位**
 *   （灰底圆角块 + 蓝色圆环），因此与 Flutter 参考图（图片未落地）一致。
 * - P2-S2f：Banner 与两处格栅改为「按 Flutter-ref 实测均值合成的色阶/色块」，
 *   不再走「surface + accent 蒙层」的亮蓝近似（详见 Executor Report 的无图天花板说明）。
 */

/** `HomeDashboardTheme` 令牌镜像。 */
private object HomePalette {
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val fillSecondary = Color(0xFFE9E9EB)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val labelTertiary = Color(0x4D3C3C43)
    val separator = Color(0xFFC6C6C8)
    val badgeOrange = Color(0xFFFF9500)
    val textDarkGray = Color(0xFF3C3C43)
    val radiusMdDesign = 12
    val radiusLgDesign = 14
}

/**
 * Banner 无位图近似色阶（P2-S2f）。
 *
 * Flutter `HomeBannerSection` = `picsum.photos/seed/banner` 风景图 + 黑 0.25 darken
 * + accent 0.55→透明 左蒙层；Kuikly Mock 无图片加载器，故按 02b Flutter-ref
 * 实测均值合成「左缘钢蓝 → 右侧暖灰暗部」的横向风景渐变（等距 8 色）。
 */
private val HOME_BANNER_SCENE = listOf(
    Color(0xFF356FB1),
    Color(0xFF5479A6),
    Color(0xFF6E83A0),
    Color(0xFF6D7788),
    Color(0xFF6B6B78),
    Color(0xFF6A6267),
    Color(0xFF545353),
    Color(0xFF474445),
)

/** 竖向暗带：近似风景图下半部的下沉地平线（上半透明 → 3/4 处压暗 → 底部微抬）。 */
private val HOME_BANNER_DEPTH = listOf(
    Color.Transparent,
    Color.Transparent,
    Color(0x0D000000),
    Color(0x4D000000),
    Color(0x24000000),
)

/**
 * 格栅无图占位色：02b Flutter-ref 功能格栅 10 格实测均值（无位图，仅色块）。
 * 服务推荐格栅在 02b 视口之外，沿用同组色阶循环。
 */
private val HOME_TILE_TONES = listOf(
    Color(0xFF86BDDE), // 销售顾问
    Color(0xFF616F73), // 生活服务
    Color(0xFF45362A), // 二手车
    Color(0xFFC6C445), // 新车关注
    Color(0xFF878081), // 客户管理
    Color(0xFF523E2A), // 订单中心
    Color(0xFFFAE4C1), // 数据分析
    Color(0xFFAEB3B7), // 直播带货
    Color(0xFFDDDEDD), // 营销活动
    Color(0xFF4F5D65), // 更多
)

private data class HomeQuickAction(val title: String, val subtitle: String, val actionLabel: String)
private data class HomeMetric(val value: String, val label: String)
private data class HomeMetricDetail(val value: String, val label: String, val actionLabel: String)
private data class HomeService(val label: String, val badge: String?)
private data class HomeContact(val title: String, val subtitle: String, val trailing: String)
private data class HomeNews(val title: String, val source: String, val date: String)

private val HOME_TOP_TABS = listOf("首页", "视频", "Club")
private val HOME_METRIC_TABS = listOf("今日", "昨日", "近30天")
private const val HOME_STORE_NAME = "[4S]北京沃德龙鼎吉利"

private val HOME_FEATURES = listOf(
    "销售顾问", "生活服务", "二手车", "新车关注", "客户管理",
    "订单中心", "数据分析", "直播带货", "营销活动", "更多",
)

private val HOME_QUICK_ACTIONS = listOf(
    HomeQuickAction("新伙伴待确认", "3 位新成员等待审核", "去处理"),
    HomeQuickAction("待跟进客户", "今日 5 位意向客户", "去查看"),
    HomeQuickAction("订单待审核", "2 笔新车订单", "去处理"),
    HomeQuickAction("售后预约", "4 位客户今日到店", "去查看"),
)

private val HOME_METRICS_TODAY = listOf(
    HomeMetric("99", "意向客户"),
    HomeMetric("2", "新车订单"),
    HomeMetric("999.8", "成交额(万)"),
    HomeMetric("15", "试驾预约"),
)

private val HOME_METRICS_YESTERDAY = listOf(
    HomeMetric("86", "意向客户"),
    HomeMetric("1", "新车订单"),
    HomeMetric("520.0", "成交额(万)"),
    HomeMetric("12", "试驾预约"),
)

private val HOME_METRICS_MONTH = listOf(
    HomeMetric("1280", "意向客户"),
    HomeMetric("45", "新车订单"),
    HomeMetric("8600.5", "成交额(万)"),
    HomeMetric("320", "试驾预约"),
)

private val HOME_METRIC_DETAILS = listOf(
    HomeMetricDetail("8", "待交车", "详情 >"),
    HomeMetricDetail("3", "待回访", "详情 >"),
    HomeMetricDetail("12", "待跟进", "详情 >"),
)

private val HOME_SERVICES = listOf(
    HomeService("朋友圈", "热门"),
    HomeService("视频号", null),
    HomeService("直播", "新品"),
    HomeService("素材库", null),
    HomeService("话术库", null),
    HomeService("培训", null),
    HomeService("竞品分析", null),
    HomeService("更多", null),
)

private val HOME_CONTACTS = listOf(
    HomeContact("李大仁", "专属客户顾问 · 金牌销售", "avatar"),
    HomeContact("AI在线咨询", "7×24 小时智能客服", "chat"),
    HomeContact("400 售后热线", "工作日 9:00-18:00", "phone"),
)

private val HOME_NEWS = listOf(
    HomeNews("2024年新能源汽车市场趋势分析报告发布", "汽车之家行业频道", "2024.05.11"),
    HomeNews("吉利星越L新款上市，配置全面升级", "汽车之家", "2024.05.10"),
    HomeNews("经销商数字化转型白皮书：从流量到留量", "iHome资讯", "2024.05.09"),
)

private val HOME_VIDEO_SHORTCUTS = listOf(
    "会员专享" to HomePalette.accent,
    "配音专栏" to HomePalette.badgeOrange,
    "其他课程" to Color(0xFF5856D6),
    "功能教程" to Color(0xFF34C759),
)

private val HOME_VIDEO_DAILY = listOf(
    Triple("带你玩转 ETF", "直播中", true),
    Triple("新能源赛道解读", "回放", false),
    Triple("门店短视频运营", "直播中", true),
)

private val HOME_VIDEO_COURSES = listOf(
    Triple("【配置】当星舰撞上算力", "尤国梁", true),
    Triple("黄金恐贪定投实战", "策略组", false),
)

/** 首页 dashboard 根：顶部 Tab（首页 / 视频 / Club）与 Flutter `HomePage` 一致。 */
@Composable
internal fun HomeTab(
    statusBarHeight: Float,
    pageWidth: Float,
    miniPlayerVisible: Boolean,
    greeting: String,
    initialTopTab: Int = 0,
    onOpenCommunity: () -> Unit,
) {
    ProvideDesignScale(pageWidth) {
        var topTab by remember { mutableStateOf(initialTopTab) }
        // 迷你条高度是设备 dp（未 su）；设计边距 24 走 su。
        val bottomPad = if (miniPlayerVisible) {
            LocalDesignScale.current.raw(24) + MUSIC_MINI_BAR_HEIGHT
        } else {
            LocalDesignScale.current.raw(24)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HomePalette.background),
        ) {
            item { HomeGreetingSection(greeting = greeting, statusBarHeight = statusBarHeight) }
            item { HomeSearchRow() }
            item { HomeTopTabBar(selected = topTab, onSelected = { topTab = it }) }
            when (topTab) {
                1 -> item { HomeVideoTabContent() }
                2 -> item { HomeClubTabContent(onOpenCommunity = onOpenCommunity) }
                else -> item { HomeDashboardSections() }
            }
            item { HomeDebugFooter() }
            item { Spacer(Modifier.height(bottomPad.dp)) }
        }
    }
}

// ───────────────────────────── 通用 chrome ─────────────────────────────

private fun toast(message: String) {
    Utils.currentBridgeModule().toast(message)
}

private fun open(page: String) {
    Utils.currentBridgeModule().openPage(page)
}

/** 「二手车」沿用 Phase-1 登录门禁语义：仅设置 pending 路由，不切 Me tab。 */
private fun openUsedCar() {
    if (AuthSession.repo.isLoggedIn()) {
        open(PageNames.UsedCarList)
    } else {
        LoginRedirect.setPending(PageNames.UsedCarList)
        open(PageNames.Login)
    }
}

/**
 * 未加载图片占位：灰底圆角块 + 蓝色圆环（对齐 Flutter golden 的 loading 态）。
 *
 * `tone` 非空时渲染为「已加载色块」（Flutter `CacheImageUtils.network` 成功态），
 * 不再叠 loading 圆环 —— 用于功能格栅 / 服务推荐格栅的按 index 色调近似。
 */
@Composable
private fun ImagePlaceholder(size: Dp, corner: Dp, ring: Dp, tone: Color? = null) {
    Box(
        modifier = Modifier
            .size(size)
            .background(tone ?: HomePalette.fillSecondary, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        if (tone == null) {
            Box(Modifier.size(ring).border(2.su, HomePalette.accent, CircleShape))
        }
    }
}

/** 圆形未加载头像占位。 */
@Composable
private fun AvatarPlaceholder(size: Dp, ring: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(HomePalette.fillSecondary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(ring).border(2.su, HomePalette.accent, CircleShape))
    }
}

/** 等宽多列网格（按行排列，最后一行用空权重补齐）。 */
@Composable
private fun <T> HomeGrid(
    items: List<T>,
    columns: Int,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    cell: @Composable (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        items.chunked(columns).forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) Spacer(Modifier.height(verticalSpacing))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) { cell(item) }
                }
                repeat(columns - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/** Flutter `HomeDashboardTheme.sectionTitle`（20sp / w600 / 黑）。 */
@Composable
private fun HomeSectionTitle(title: String) {
    Text(title, fontSize = 20.susp, fontWeight = FontWeight.SemiBold, color = HomePalette.labelPrimary)
}

// ───────────────────────────── 首页 chrome ─────────────────────────────

/** Flutter `HomeGreetingSection`：大标题 + 「3条新消息」胶囊。 */
@Composable
private fun HomeGreetingSection(greeting: String, statusBarHeight: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.su,
                end = 16.su,
                top = (statusBarHeight + LocalDesignScale.current.raw(16) + LocalDesignScale.current.raw(24)).dp,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            greeting,
            modifier = Modifier.weight(1f),
            fontSize = 28.susp,
            fontWeight = FontWeight.Bold,
            color = HomePalette.labelPrimary,
        )
        Row(
            modifier = Modifier
                .background(HomePalette.accent.copy(alpha = 0.1f), RoundedCornerShape(20.su))
                .padding(horizontal = 12.su, vertical = 6.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔔", fontSize = 12.susp)
            Spacer(Modifier.width(4.su))
            Text(
                "3条新消息",
                fontSize = 12.susp,
                fontWeight = FontWeight.Medium,
                color = HomePalette.accent,
            )
        }
    }
}

/** Flutter `HomeSearchBar`：搜索框 + 扫一扫。 */
@Composable
private fun HomeSearchRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.su)
                .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                .clickable { open(PageNames.Search) }
                .padding(horizontal = 14.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⌕", fontSize = 18.susp, color = HomePalette.labelSecondary)
            Spacer(Modifier.width(8.su))
            Text("搜索客户、订单、资讯", fontSize = 15.susp, color = HomePalette.labelTertiary)
        }
        Spacer(Modifier.width(12.su))
        Box(
            modifier = Modifier
                .size(44.su)
                .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                .clickable { toast("「扫一扫」即将接入") },
            contentAlignment = Alignment.Center,
        ) {
            Text("▣", fontSize = 20.susp, color = HomePalette.accent)
        }
    }
}

/** Flutter `HomeTopTabBar`：首页 / 视频 / Club，硬编码 dp，选中项 17sp w600 + 20×2 蓝条。 */
@Composable
private fun HomeTopTabBar(selected: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 4.dp),
    ) {
        HOME_TOP_TABS.forEachIndexed { index, label ->
            val active = index == selected
            Column(
                modifier = Modifier
                    .clickable { onSelected(index) }
                    .padding(end = if (index < HOME_TOP_TABS.lastIndex) 24.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    label,
                    fontSize = if (active) 17.sp else 16.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) HomePalette.labelPrimary else HomePalette.labelSecondary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(if (active) 20.dp else 0.dp)
                        .height(2.dp)
                        .background(HomePalette.accent, RoundedCornerShape(1.dp)),
                )
            }
        }
    }
}

// ───────────────────────────── 首页 dashboard ─────────────────────────────

/** Flutter `_buildHomeDashboard`：section 顺序与源一致。 */
@Composable
private fun HomeDashboardSections() {
    Column(modifier = Modifier.fillMaxWidth()) {
        HomeBanner()
        HomeFeatureGrid()
        HomeQuickActionGrid()
        HomeStoreMetricsCard()
        HomeEntryCard(
            icon = "▦",
            title = "投资策略",
            subtitle = "资产九宫格 · 恐贪定投 · 趋势策略",
            onClick = { open(PageNames.Strategy) },
            withBorder = true,
        )
        HomeServiceGrid()
        HomeContactList()
        HomeNewsList()
        HomeEntryCard(
            icon = "▤",
            title = "学习报告",
            subtitle = "今日高光 · 学习记录",
            onClick = { open(PageNames.LearningReport) },
            withBorder = false,
        )
    }
}

/** Flutter `HomeBannerSection`：暗色风景近似底 + 竖向下沉暗带 + 16/4 阴影。 */
@Composable
private fun HomeBanner() {
    val bannerShape = RoundedCornerShape(HomePalette.radiusMdDesign.su)
    val shadowColor = Color(0xFF8E8E93).copy(alpha = 0.08f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su)
            .height(132.su)
            .shadow(8.dp, bannerShape, ambientColor = shadowColor, spotColor = shadowColor)
            .background(Brush.horizontalGradient(HOME_BANNER_SCENE), bannerShape)
            .clickable { toast("「朋友圈营销」即将接入") },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(HOME_BANNER_DEPTH), bannerShape),
        )
        Column(modifier = Modifier.padding(start = 20.su, top = 24.su)) {
            Text(
                "朋友圈营销",
                fontSize = 22.susp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(6.su))
            Text(
                "一键分享，高效触达客户",
                fontSize = 14.susp,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(12.su))
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(20.su))
                    .padding(horizontal = 16.su, vertical = 8.su),
            ) {
                Text(
                    "立即体验",
                    fontSize = 14.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomePalette.accent,
                )
            }
        }
    }
}

/** Flutter `HomeFeatureGrid`：5 列 × 2 行，标签取源 Mock。 */
@Composable
private fun HomeFeatureGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su)
            .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
            .padding(start = 8.su, end = 8.su, top = 16.su, bottom = 16.su),
    ) {
        HomeGrid(
            items = HOME_FEATURES,
            columns = 5,
            horizontalSpacing = 0.su,
            verticalSpacing = 12.su,
        ) { label ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.su)
                    .clickable { onFeatureTap(label) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ImagePlaceholder(size = 48.su, corner = 12.su, ring = 20.su)
                Spacer(Modifier.height(6.su))
                Text(
                    label,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.susp,
                    color = HomePalette.labelPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 功能格栅深链（Flutter `HomeFeatureGrid._onFeatureTap` 对齐）：
 * 更多→AllServices、生活服务→CheckInMall、二手车→UsedCarList（登录门禁）、
 * 客户管理→FriendList、订单中心→PayList、直播带货→LiveList；
 * 销售顾问在 Flutter 走 `/web` 桥接测试（本 ticket out of scope）→ toast。
 */
private fun onFeatureTap(label: String) {
    when (label) {
        "更多" -> open(PageNames.AllServices)
        "生活服务" -> open(PageNames.CheckInMall)
        "二手车" -> openUsedCar()
        "销售顾问" -> toast("「销售顾问」Web 桥接测试即将接入")
        "客户管理" -> open(PageNames.FriendList)
        "订单中心" -> open(PageNames.PayList)
        "直播带货" -> open(PageNames.LiveList)
        else -> toast("「$label」即将接入")
    }
}

/** Flutter `HomeQuickActionGrid`：2 列待办卡 + 右下动作胶囊。 */
@Composable
private fun HomeQuickActionGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su),
    ) {
        HomeGrid(
            items = HOME_QUICK_ACTIONS,
            columns = 2,
            horizontalSpacing = 12.su,
            verticalSpacing = 12.su,
        ) { action ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(166.su)
                    .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                    .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                    .clickable { toast("「${action.title}」即将接入") }
                    .padding(14.su),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ImagePlaceholder(size = 40.su, corner = 10.su, ring = 16.su)
                    Spacer(Modifier.width(10.su))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            action.title,
                            fontSize = 13.susp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomePalette.labelPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(2.su))
                        Text(
                            action.subtitle,
                            fontSize = 11.susp,
                            color = HomePalette.labelSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .background(
                                HomePalette.accent.copy(alpha = 0.08f),
                                RoundedCornerShape(12.su),
                            )
                            .padding(horizontal = 12.su, vertical = 5.su),
                    ) {
                        Text(
                            action.actionLabel,
                            fontSize = 12.susp,
                            fontWeight = FontWeight.Medium,
                            color = HomePalette.accent,
                        )
                    }
                }
            }
        }
    }
}

/** Flutter `HomeStoreMetricsCard`：门店名 + 今日/昨日/近30天 + 指标 + 明细。 */
@Composable
private fun HomeStoreMetricsCard() {
    var metricTab by remember { mutableStateOf(0) }
    val metrics = when (metricTab) {
        1 -> HOME_METRICS_YESTERDAY
        2 -> HOME_METRICS_MONTH
        else -> HOME_METRICS_TODAY
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su)
            .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
            .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
            .padding(16.su),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                HOME_STORE_NAME,
                modifier = Modifier.weight(1f),
                fontSize = 15.susp,
                fontWeight = FontWeight.SemiBold,
                color = HomePalette.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("⌄", fontSize = 18.susp, color = HomePalette.labelPrimary)
            Spacer(Modifier.width(8.su))
            Text("⇄", fontSize = 16.susp, color = HomePalette.labelSecondary)
        }
        Spacer(Modifier.height(12.su))
        Row {
            HOME_METRIC_TABS.forEachIndexed { index, label ->
                val active = index == metricTab
                Column(
                    modifier = Modifier
                        .clickable { metricTab = index }
                        .padding(end = 20.su),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        label,
                        fontSize = 14.susp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) HomePalette.accent else HomePalette.labelSecondary,
                    )
                    Spacer(Modifier.height(6.su))
                    Box(
                        modifier = Modifier
                            .width(24.su)
                            .height(2.su)
                            .background(
                                if (active) HomePalette.accent else Color.Transparent,
                            ),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.su))
        HomeGrid(
            items = metrics,
            columns = 2,
            horizontalSpacing = 12.su,
            verticalSpacing = 16.su,
        ) { metric ->
            Column(modifier = Modifier.fillMaxWidth().height(62.su)) {
                Text(
                    metric.value,
                    fontSize = 24.susp,
                    fontWeight = FontWeight.Bold,
                    color = HomePalette.labelPrimary,
                )
                Text(metric.label, fontSize = 12.susp, color = HomePalette.labelSecondary)
            }
        }
        Spacer(Modifier.height(8.su))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.su)
                .background(HomePalette.background),
        )
        Spacer(Modifier.height(24.su))
        Row(modifier = Modifier.fillMaxWidth()) {
            HOME_METRIC_DETAILS.forEach { detail ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        detail.value,
                        fontSize = 18.susp,
                        fontWeight = FontWeight.Bold,
                        color = HomePalette.labelPrimary,
                    )
                    Spacer(Modifier.height(4.su))
                    Text(detail.label, fontSize = 11.susp, color = HomePalette.labelSecondary)
                    Spacer(Modifier.height(4.su))
                    Text(detail.actionLabel, fontSize = 11.susp, color = HomePalette.accent)
                }
            }
        }
        Spacer(Modifier.height(12.su))
        Text(
            "查看更多 >",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.susp,
            color = HomePalette.labelSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** Flutter `_StrategyEntry`（带 border）/ `_LearningReportEntry`（无 border）共用形制。 */
@Composable
private fun HomeEntryCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    withBorder: Boolean = true,
) {
    val entryShape = RoundedCornerShape(HomePalette.radiusMdDesign.su)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su)
            .background(HomePalette.surface, entryShape)
            .then(if (withBorder) Modifier.border(0.5.su, HomePalette.separator, entryShape) else Modifier)
            .clickable(onClick = onClick)
            .padding(16.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.su)
                .background(HomePalette.accent.copy(alpha = 0.1f), RoundedCornerShape(12.su)),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 20.susp, color = HomePalette.accent)
        }
        Spacer(Modifier.width(12.su))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.susp, fontWeight = FontWeight.SemiBold, color = HomePalette.labelPrimary)
            Text(subtitle, fontSize = 13.susp, color = HomePalette.labelSecondary)
        }
        Text("›", fontSize = 22.susp, color = HomePalette.labelTertiary)
    }
}

/** Flutter `HomeServiceGrid`：服务推荐 4 列 + 热门/新品角标。 */
@Composable
private fun HomeServiceGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 24.su),
    ) {
        HomeSectionTitle("服务推荐")
        Spacer(Modifier.height(12.su))
        HomeGrid(
            items = HOME_SERVICES,
            columns = 4,
            horizontalSpacing = 0.su,
            verticalSpacing = 16.su,
        ) { service ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.su)
                    .clickable { onServiceTap(service.label) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    ImagePlaceholder(size = 48.su, corner = 14.su, ring = 20.su)
                    if (service.badge != null) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (service.badge == "热门") HomePalette.badgeOrange else HomePalette.accent,
                                    RoundedCornerShape(6.su),
                                )
                                .padding(horizontal = 5.su, vertical = 2.su),
                        ) {
                            Text(
                                service.badge,
                                fontSize = 9.susp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.su))
                Text(
                    service.label,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.susp,
                    color = HomePalette.textDarkGray,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** 服务推荐深链：视频号→VideoList、直播→LiveList、培训→ClassroomList。 */
private fun onServiceTap(label: String) {
    when (label) {
        "视频号" -> open(PageNames.VideoList)
        "直播" -> open(PageNames.LiveList)
        "培训" -> open(PageNames.ClassroomList)
        else -> toast("「$label」即将接入")
    }
}

/** Flutter `HomeContactList`：联系汽车之家列表卡。 */
@Composable
private fun HomeContactList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 24.su),
    ) {
        HomeSectionTitle("联系汽车之家")
        Spacer(Modifier.height(12.su))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su)),
        ) {
            HOME_CONTACTS.forEachIndexed { index, contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { toast("「${contact.title}」即将接入") }
                        .padding(horizontal = 16.su, vertical = 12.su),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarPlaceholder(size = 44.su, ring = 18.su)
                    Spacer(Modifier.width(12.su))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            contact.title,
                            fontSize = 15.susp,
                            fontWeight = FontWeight.Medium,
                            color = HomePalette.labelPrimary,
                        )
                        Text(
                            contact.subtitle,
                            fontSize = 13.susp,
                            color = HomePalette.labelSecondary,
                        )
                    }
                    Text(
                        when (contact.trailing) {
                            "chat" -> "✉"
                            "phone" -> "☎"
                            else -> "›"
                        },
                        fontSize = 18.susp,
                        color = if (contact.trailing == "avatar") {
                            HomePalette.labelTertiary
                        } else {
                            HomePalette.accent
                        },
                    )
                }
                if (index < HOME_CONTACTS.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 68.su, end = 16.su)
                            .height(0.5.su)
                            .background(HomePalette.separator),
                    )
                }
            }
        }
    }
}

/** Flutter `HomeNewsList`：行业动态 3 条（左文右图）。 */
@Composable
private fun HomeNewsList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 24.su),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) { HomeSectionTitle("行业动态") }
            Text("查看更多", fontSize = 13.susp, color = HomePalette.accent)
            Text("›", fontSize = 18.susp, color = HomePalette.accent)
        }
        Spacer(Modifier.height(12.su))
        HOME_NEWS.forEach { news ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.su)
                    .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                    .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                    .clickable { toast("「行业动态」详情即将接入") }
                    .padding(12.su),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        news.title,
                        fontSize = 15.susp,
                        fontWeight = FontWeight.Medium,
                        color = HomePalette.labelPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.su))
                    Text(
                        "${news.source}  ${news.date}",
                        fontSize = 13.susp,
                        color = HomePalette.labelSecondary,
                    )
                }
                Spacer(Modifier.width(12.su))
                Box(
                    modifier = Modifier
                        .width(96.su)
                        .height(72.su)
                        .background(HomePalette.fillSecondary, RoundedCornerShape(10.su)),
                )
            }
        }
    }
}

// ───────────────────────────── 视频 tab ─────────────────────────────

/**
 * Flutter `HomeVideoTabContent` 结构桩：4 个快捷入口 + 每日推荐 + 热门课程 + 配音专区入口。
 * 保留 Phase-1 深链：配音专栏→VideoList、会员专享→PayList、其他课程→ClassroomList、
 * 功能教程→MusicList（Kuikly 无「音频」首页入口，音频列表在此保持可达）。
 */
@Composable
private fun HomeVideoTabContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.su, end = 16.su, top = 8.su),
        ) {
            HOME_VIDEO_SHORTCUTS.forEach { (label, color) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onVideoShortcutTap(label) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.su)
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(16.su))
                            .border(2.su, color.copy(alpha = 0.35f), RoundedCornerShape(16.su)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("▶", fontSize = 18.susp, color = color)
                    }
                    Spacer(Modifier.height(8.su))
                    Text(
                        label,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 12.susp,
                        color = HomePalette.labelPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        HomeTrailingSection(title = "每日推荐") {
            Column(modifier = Modifier.fillMaxWidth()) {
                HOME_VIDEO_DAILY.forEach { (title, tag, live) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.su)
                            .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                            .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                            .clickable { open(PageNames.VideoList) }
                            .padding(12.su),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (live) Color(0x14FF3B30) else HomePalette.fillSecondary,
                                    RoundedCornerShape(4.su),
                                )
                                .padding(horizontal = 8.su, vertical = 3.su),
                        ) {
                            Text(
                                tag,
                                fontSize = 11.susp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (live) Color(0xFFFF3B30) else HomePalette.labelSecondary,
                            )
                        }
                        Spacer(Modifier.width(10.su))
                        Text(
                            title,
                            modifier = Modifier.weight(1f),
                            fontSize = 15.susp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomePalette.labelPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        AvatarPlaceholder(size = 36.su, ring = 14.su)
                    }
                }
            }
        }
        HomeTrailingSection(title = "热门课程") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.su),
            ) {
                HOME_VIDEO_COURSES.forEach { (title, author, member) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                            .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                            .clickable { open(PageNames.VideoList) }
                            .padding(10.su),
                    ) {
                        ImagePlaceholder(size = 96.su, corner = 10.su, ring = 24.su)
                        Spacer(Modifier.height(8.su))
                        Text(
                            title,
                            fontSize = 14.susp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomePalette.labelPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(6.su))
                        Text(author, fontSize = 13.susp, color = HomePalette.labelSecondary)
                        if (member) {
                            Spacer(Modifier.height(6.su))
                            Box(
                                modifier = Modifier
                                    .background(Color(0x14FF9500), RoundedCornerShape(4.su))
                                    .padding(horizontal = 6.su, vertical = 2.su),
                            ) {
                                Text(
                                    "V 会员专属",
                                    fontSize = 10.susp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HomePalette.badgeOrange,
                                )
                            }
                        }
                    }
                }
            }
        }
        HomeBlockButton(label = "进入配音视频专区") { open(PageNames.VideoList) }
    }
}

private fun onVideoShortcutTap(label: String) {
    when (label) {
        "会员专享" -> open(PageNames.PayList)
        "配音专栏" -> open(PageNames.VideoList)
        "其他课程" -> open(PageNames.ClassroomList)
        "功能教程" -> open(PageNames.MusicList)
        else -> toast("「$label」即将接入")
    }
}

/** Flutter `_buildSection`：标题 + 「更多 >」 + 内容。 */
@Composable
private fun HomeTrailingSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 24.su),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) { HomeSectionTitle(title) }
            Text("更多 >", fontSize = 13.susp, color = HomePalette.labelSecondary)
        }
        Spacer(Modifier.height(12.su))
        content()
    }
}

// ───────────────────────────── Club tab ─────────────────────────────

/** Flutter `HomeClubTabContent` 结构桩：Club 卡 + 筛选 + 两条动态 + 社区入口。 */
@Composable
private fun HomeClubTabContent(onOpenCommunity: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.su, end = 16.su, top = 8.su)
                .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
                .padding(16.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.su)
                    .background(Color(0xFF1C1C3A), RoundedCornerShape(12.su)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Club",
                    fontSize = 12.susp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(12.su))
            Column(modifier = Modifier.weight(1f)) {
                HomeSectionTitle("莫听Club")
                Spacer(Modifier.height(4.su))
                Text("动态 127 | 成员 1040", fontSize = 13.susp, color = HomePalette.labelSecondary)
            }
            Box(
                modifier = Modifier
                    .background(HomePalette.accent, RoundedCornerShape(20.su))
                    .clickable(onClick = onOpenCommunity)
                    .padding(horizontal = 16.su, vertical = 8.su),
            ) {
                Text("+ 加入", fontSize = 14.susp, color = Color.White)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.su, top = 12.su),
        ) {
            listOf("最新", "嘉宾分享", "资料").forEachIndexed { index, label ->
                val active = index == 0
                Column(
                    modifier = Modifier.padding(end = 24.su),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        label,
                        fontSize = 15.susp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) HomePalette.labelPrimary else HomePalette.labelSecondary,
                    )
                    Spacer(Modifier.height(6.su))
                    Box(
                        modifier = Modifier
                            .width(if (active) 20.su else 0.su)
                            .height(2.su)
                            .background(HomePalette.accent),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.su))
        ClubPostCard(
            author = "莫听官方",
            date = "06-24",
            content = "【官方纪要】本期聚焦 AI 算力与产业趋势，内容仅供合格投资者参考。",
            pdfName = "【莫听Club第78期】聊聊AI最靓的仔.pdf",
        )
        ClubPostCard(
            author = "策略研究员",
            date = "06-20",
            content = "当星舰遇到算力：嘉宾分享回顾与延伸阅读。",
            pdfName = null,
        )
        HomeBlockButton(label = "进入社区查看更多", onClick = onOpenCommunity)
    }
}

/** Flutter `_ClubPostCard`：作者行 + 正文 + 可选 PDF 行 + 分享/评论/点赞。 */
@Composable
private fun ClubPostCard(author: String, date: String, content: String, pdfName: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, bottom = 12.su)
            .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
            .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
            .padding(16.su),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarPlaceholder(size = 40.su, ring = 16.su)
            Spacer(Modifier.width(10.su))
            Column {
                Text(author, fontSize = 15.susp, fontWeight = FontWeight.SemiBold, color = HomePalette.labelPrimary)
                Text(date, fontSize = 13.susp, color = HomePalette.labelSecondary)
            }
        }
        Spacer(Modifier.height(12.su))
        Text(content, fontSize = 15.susp, color = HomePalette.labelPrimary)
        if (pdfName != null) {
            Spacer(Modifier.height(12.su))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HomePalette.fillSecondary, RoundedCornerShape(10.su))
                    .padding(12.su),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("▤", fontSize = 16.susp, color = HomePalette.accent)
                Spacer(Modifier.width(8.su))
                Text(
                    pdfName,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.susp,
                    color = HomePalette.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(12.su))
        Row {
            listOf("↗" to "分享", "💬" to "评论", "♡" to "点赞").forEachIndexed { index, (glyph, label) ->
                if (index > 0) Spacer(Modifier.width(20.su))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(glyph, fontSize = 14.susp, color = HomePalette.labelSecondary)
                    Spacer(Modifier.width(4.su))
                    Text(label, fontSize = 13.susp, color = HomePalette.labelSecondary)
                }
            }
        }
    }
}

/** Flutter 卡片底部的整宽动作按钮（配音专区 / 进入社区）。 */
@Composable
private fun HomeBlockButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 8.su)
            .background(HomePalette.surface, RoundedCornerShape(HomePalette.radiusMdDesign.su))
            .border(0.5.su, HomePalette.separator, RoundedCornerShape(HomePalette.radiusMdDesign.su))
            .clickable(onClick = onClick)
            .padding(vertical = 14.su),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 15.susp, fontWeight = FontWeight.SemiBold, color = HomePalette.accent)
    }
}

// ───────────────────────────── 调试页脚 ─────────────────────────────

/**
 * Mock 场景切换入口（`docs/runbooks/goldens.md` 用它在截图前切 Success/Empty/Error）。
 * 非 Flutter 元素，故放在 dashboard 最底部、参考图视口之外。
 */
@Composable
private fun HomeDebugFooter() {
    var scenario by remember { mutableStateOf(MockBackend.scenario) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 24.su),
    ) {
        Text(
            "Mock 场景：${scenario.name}",
            fontSize = 12.susp,
            color = HomePalette.labelTertiary,
            modifier = Modifier.clickable {
                MockBackend.cycle()
                scenario = MockBackend.scenario
            },
        )
        Spacer(Modifier.height(4.su))
        Text(
            "点击切换 Mock 场景",
            fontSize = 12.susp,
            color = HomePalette.labelTertiary,
        )
    }
}
