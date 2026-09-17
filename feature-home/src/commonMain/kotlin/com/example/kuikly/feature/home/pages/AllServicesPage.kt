package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.Utils
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
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
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page

/**
 * 「全部服务」— Flutter `AllServicesPage`（`features/home/lib/home/view/all_services_page.dart`）
 * 复刻（Phase-2 / P2-W3）。
 *
 * 真源：`view/all_services_page.dart` + `view/widgets/all_services_section_widget.dart` +
 * `model/all_services_data.dart` + `theme/all_services_theme.dart`。
 *
 * **刻度**：Flutter 本页三文件命中 `.w/.h/.sp`（5 / 27 / 12 处）→ 走 `ProvideDesignScale` +
 * `.su` / `.susp`（与 `AllServicesTheme`：bg 白 / title `#1A1A1A` / subtitle `#999` /
 * label `#333` / editBorder `#1B82D2`）。
 *
 * ponytail 天花板：`assets/all_services` 下的 PNG 图标位无图片加载器 → 统一渲染为
 * **同尺寸 `fillSecondary` 圆角块 + Unicode 字形**（同 W2c `ImagePlaceholder` 口径）。
 * 路由映射只保留 Kuikly 已落地的页；bfui demo 路由按 out-of-scope toast。
 */
@Page(name = "AllServices", moduleId = "feature_home")
internal class AllServicesPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            ProvideDesignScale(pagerData.pageViewWidth) {
                var editing by remember { mutableStateOf(false) }
                var favoriteIds by remember { mutableStateOf(DEFAULT_FAVORITE_IDS) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AllServicesPalette.background),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AllServicesNavBar(topInset = top)
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(top = 8.su, bottom = (bottom + 24f).su),
                        ) {
                            item {
                                ServiceSectionCard(
                                    title = "常用服务",
                                    subtitle = "将按自定义顺序出现在首页",
                                    items = favoriteIds.mapNotNull { id -> CATALOG[id] },
                                    editing = editing,
                                    isFavoriteSection = true,
                                    favoriteIds = favoriteIds,
                                    canRemoveFavorite = favoriteIds.size > MIN_FAVORITE,
                                    onEditTap = { editing = !editing },
                                    onRemove = { id ->
                                        if (favoriteIds.size > MIN_FAVORITE) favoriteIds = favoriteIds - id
                                    },
                                    onAdd = null,
                                )
                            }
                            CATALOG_SECTIONS.forEach { section ->
                                item {
                                    ServiceSectionCard(
                                        title = section.title,
                                        subtitle = null,
                                        items = section.ids.mapNotNull { CATALOG[it] },
                                        editing = editing,
                                        isFavoriteSection = false,
                                        favoriteIds = favoriteIds,
                                        canRemoveFavorite = false,
                                        onEditTap = null,
                                        onRemove = null,
                                        onAdd = { id ->
                                            if (favoriteIds.size < MAX_FAVORITE) favoriteIds = favoriteIds + id
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── 令牌 / 数据（Flutter AllServicesTheme + AllServicesData） ─────────────────────────

private object AllServicesPalette {
    val background = Color(0xFFFFFFFF)
    val titleBlack = Color(0xFF1A1A1A)
    val subtitleGray = Color(0xFF999999)
    val labelGray = Color(0xFF333333)
    val editBorderBlue = Color(0xFF1B82D2)
    val badgeBorder = Color(0xFFE0E0E0)
}

private const val MIN_FAVORITE = 3
private const val MAX_FAVORITE = 8

/**
 * Flutter `GridDelegateWithFixedCrossAxisCount(childAspectRatio: 0.72)` 的 **宽/高比**
 * （`crossAxisExtent / mainAxisExtent`）。tile 宽 = 可用宽 / 5（= Flutter 同式），
 * 故 tile 高 = tile 宽 / 0.72 —— 与设备刻度无关，`su` 缩放自动跟随。
 */
private const val GRID_CELL_ASPECT_RATIO = 0.72f

private data class ServiceItem(val id: String, val label: String, val glyph: String, val page: String? = null)
private data class ServiceSectionSpec(val title: String, val ids: List<String>)

private val CATALOG: Map<String, ServiceItem> = listOf(
    ServiceItem("smart_online_marketing.png", "引导动画", "▶"),
    ServiceItem("customer_profile.png", "酒店预订", "🏨"),
    ServiceItem("smart_sale.png", "酒店筛选", "⚙"),
    ServiceItem("new_car_deal.png", "健身应用", "💪"),
    ServiceItem("exhibition_hall_shooting.png", "我的日记", "📔"),
    ServiceItem("intelligence_task.png", "训练计划", "🎯"),
    ServiceItem("marketing.png", "设计课程", "🎨"),
    ServiceItem("business_poster.png", "课程详情", "📋"),
    ServiceItem("after_sales_area.png", "帮助中心", "❓"),
    ServiceItem("calculator.png", "意见反馈", "🧮"),
    ServiceItem("used_car.png", "音频列表", "♪", PageNames.MusicList),
    ServiceItem("service_management.png", "侧滑导航", "☰"),
    ServiceItem("online_customer_acquisition.png", "玻璃卡片", "▢"),
    ServiceItem("smart_number.png", "波浪动画", "〰"),
    ServiceItem("new_car_in_store.png", "跑步数据", "🏃"),
    ServiceItem("v_store.png", "训练视图", "🏋"),
    ServiceItem("small_video.png", "地中海饮食", "🎬"),
    ServiceItem("dubbing_home.png", "配音首页", "🎙"),
    ServiceItem("dubbing_video_list.png", "视频列表", "🎬", PageNames.VideoList),
    ServiceItem("dubbing_work_list.png", "作品列表", "🏆", PageNames.VideoList),
    ServiceItem("classroom_my_class.png", "班级教学", "🎓", PageNames.ClassroomMyClass),
    ServiceItem("pay_membership.png", "会员续费", "💳", PageNames.PayList),
).associateBy { it.id }

private val DEFAULT_FAVORITE_IDS = listOf(
    "smart_online_marketing.png",
    "online_customer_acquisition.png",
    "small_video.png",
    "service_management.png",
    "exhibition_hall_shooting.png",
    "intelligence_task.png",
    "new_car_in_store.png",
    "smart_number.png",
)

private val CATALOG_SECTIONS = listOf(
    ServiceSectionSpec(
        "线索服务",
        listOf(
            "smart_online_marketing.png", "customer_profile.png", "smart_sale.png",
            "new_car_deal.png", "online_customer_acquisition.png",
            "new_car_in_store.png", "smart_number.png",
        ),
    ),
    ServiceSectionSpec(
        "营销服务",
        listOf(
            "exhibition_hall_shooting.png", "marketing.png", "intelligence_task.png",
            "v_store.png", "small_video.png", "business_poster.png",
        ),
    ),
    ServiceSectionSpec(
        "教学服务",
        listOf(
            "classroom_my_class.png", "dubbing_home.png",
            "dubbing_video_list.png", "dubbing_work_list.png",
        ),
    ),
    ServiceSectionSpec(
        "其他服务",
        listOf(
            "pay_membership.png", "after_sales_area.png",
            "calculator.png", "service_management.png", "used_car.png",
        ),
    ),
)

// ───────────────────────── chrome ─────────────────────────

/**
 * Flutter `AppNavBar(title: '全部服务', leading: 24×24 返回图, backgroundColor: white,
 * foregroundColor: #1A1A1A)`。无 56 工具栏常量（AppNavBar 自持），此处沿用 [AppChrome.NAV_BAR_HEIGHT]。
 *
 * 本页未传 `style:` → 走 `AppNavBarStyle.solid` 默认值 → Flutter 侧**有**
 * `border: Border(bottom: BorderSide(dividerColor.withValues(alpha: 0.08)))`，
 * 白底页面上这条发丝线就是导航栏唯一的边界，故按 [AppChrome] 同款令牌补上（形制同 `AppNavBarBar`）。
 */
@Composable
private fun AllServicesNavBar(topInset: Float) {
    Column(modifier = Modifier.fillMaxWidth().background(AllServicesPalette.background)) {
        Spacer(Modifier.height(topInset.dp))
        Box(Modifier.fillMaxWidth().height(AppChrome.NAV_BAR_HEIGHT.dp)) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Flutter `Image.asset(nav_back_black.png, 24×24)` + `EdgeInsets.only(left: 12.w)`。
                Text(
                    "‹",
                    fontSize = 24.susp,
                    color = AllServicesPalette.titleBlack,
                    modifier = Modifier
                        .clickable { Utils.currentBridgeModule().closePage() }
                        .padding(start = 12.su, end = 8.su, top = 10.su, bottom = 10.su),
                )
                Spacer(Modifier.weight(1f))
            }
            Box(Modifier.align(Alignment.Center)) {
                Text(
                    "全部服务",
                    fontSize = 17.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = AllServicesPalette.titleBlack,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppChrome.HAIRLINE.dp)
                .background(AppChrome.hairline),
        )
    }
}

// ───────────────────────── section ─────────────────────────

/** Flutter `AllServicesSectionWidget`：header（可带 编辑/完成）+ 5 列网格。 */
@Composable
private fun ServiceSectionCard(
    title: String,
    subtitle: String?,
    items: List<ServiceItem>,
    editing: Boolean,
    isFavoriteSection: Boolean,
    favoriteIds: List<String>,
    canRemoveFavorite: Boolean,
    onEditTap: (() -> Unit)?,
    onRemove: ((String) -> Unit)?,
    onAdd: ((String) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, bottom = 24.su),
    ) {
        if (onEditTap != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    fontSize = 18.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = AllServicesPalette.titleBlack,
                )
                Spacer(Modifier.width(8.su))
                Text(
                    subtitle.orEmpty(),
                    modifier = Modifier.weight(1f),
                    fontSize = 12.susp,
                    color = AllServicesPalette.subtitleGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Flutter `Container(padding: 12w/4h, border: editBorderBlue, r14)`。
                Box(
                    modifier = Modifier
                        .border(1.su, AllServicesPalette.editBorderBlue, RoundedCornerShape(14.su))
                        .clickable(onClick = onEditTap)
                        .padding(horizontal = 12.su, vertical = 4.su),
                ) {
                    Text(
                        if (editing) "完成" else "编辑",
                        fontSize = 12.susp,
                        color = AllServicesPalette.editBorderBlue,
                    )
                }
            }
        } else {
            Text(
                title,
                fontSize = 18.susp,
                fontWeight = FontWeight.SemiBold,
                color = AllServicesPalette.titleBlack,
            )
        }
        Spacer(Modifier.height(16.su))
        items.chunked(5).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    // Flutter `GridView` 每格 = tile 宽 × (tile 宽 / 0.72)；内容在格内顶部对齐。
                    Box(modifier = Modifier.weight(1f).aspectRatio(GRID_CELL_ASPECT_RATIO)) {
                        ServiceGridCell(
                            item = item,
                            editing = editing,
                            isFavoriteSection = isFavoriteSection,
                            isInFavorites = item.id in favoriteIds,
                            canRemoveFavorite = canRemoveFavorite,
                            canAddFavorite = onAdd != null,
                            onRemove = onRemove,
                            onAdd = onAdd,
                        )
                    }
                }
                repeat(5 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(12.su))
        }
    }
}

/**
 * Flutter `_ServiceGridCell`：48 图标 + 6 间隙 + 11 标签；编辑态叠 16 圆角标。
 *
 * Flutter 把整格包在 `Opacity(opacity: dimmed ? 0.4 : 1)` 里 → **图标与标签一起降透明**；
 * 此处按同一口径把 `alpha` 同时施于图标底色 / 字形 / 标签色（不再只降图标）。
 */
@Composable
private fun ServiceGridCell(
    item: ServiceItem,
    editing: Boolean,
    isFavoriteSection: Boolean,
    isInFavorites: Boolean,
    canRemoveFavorite: Boolean,
    canAddFavorite: Boolean,
    onRemove: ((String) -> Unit)?,
    onAdd: ((String) -> Unit)?,
) {
    val showRemove = editing && isFavoriteSection && canRemoveFavorite
    val showAdd = editing && !isFavoriteSection && !isInFavorites && canAddFavorite
    val dimmed = editing && !isFavoriteSection && isInFavorites
    val alpha = if (dimmed) 0.4f else 1f

    fun openItem() {
        val page = item.page
        if (page != null) {
            Utils.currentBridgeModule().openPage(page)
        } else {
            Utils.currentBridgeModule().toast("「${item.label}」即将接入")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !editing, onClick = { openItem() }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(48.su)) {
            Box(
                modifier = Modifier
                    .size(48.su)
                    .background(
                        AllServicesPalette.subtitleGray.copy(alpha = 0.12f * alpha),
                        RoundedCornerShape(12.su),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.glyph,
                    fontSize = 22.susp,
                    color = AllServicesPalette.titleBlack.copy(alpha = alpha),
                )
            }
            if (showRemove) {
                ActionBadge(
                    glyph = "✕",
                    background = Color.White,
                    border = AllServicesPalette.badgeBorder,
                    glyphColor = AllServicesPalette.labelGray,
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { onRemove?.invoke(item.id) },
                )
            }
            if (showAdd) {
                ActionBadge(
                    glyph = "＋",
                    background = AllServicesPalette.editBorderBlue,
                    border = AllServicesPalette.editBorderBlue,
                    glyphColor = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { onAdd?.invoke(item.id) },
                )
            }
        }
        Spacer(Modifier.height(6.su))
        Text(
            item.label,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 11.susp,
            color = AllServicesPalette.labelGray.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Flutter `_ActionBadge`：16×16 圆 + 1 边框，`Positioned(top: -4, right: -4)` 叠在 48 图标右上角外溢。
 *
 * Kuikly 侧内贴角（差 4dp×4dp）：`Modifier.offset` 本身接受负值
 * （`foundation/layout/Offset.kt` — "The offsets can be positive as well as non-positive"），
 * 但父 `Box` 超出部分是否被裁剪未在真机验证，负偏移有把角标裁掉的风险；4dp 属亚显著性，
 * 故保持内贴并记入天花板，不做未验证的溢出布局。
 */
@Composable
private fun ActionBadge(
    glyph: String,
    background: Color,
    border: Color,
    glyphColor: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(16.su)
            .background(background, CircleShape)
            .border(1.su, border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 10.susp, color = glyphColor)
    }
}
