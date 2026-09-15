package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.feed.FeedItem
import com.example.kuikly.data.feed.FeedStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.ButtonDefaults
import com.tencent.kuikly.compose.material3.Card
import com.tencent.kuikly.compose.material3.CardDefaults
import com.tencent.kuikly.compose.material3.HorizontalDivider
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 四 Tab 主壳：首页 / 动态 / 实验室 / 我的
 *
 * - 不用 Scaffold bottomBar：Kuikly 在 edge-to-edge 下 bottomBar 易被系统导航栏盖住；
 *   改用 Column + weight，底栏始终在布局流内（对齐沉浸式「顶铺满、底自垫」）
 * - 顶：Hero/标题用 statusBarInset 内侧避让；底：bottomSafeInset 垫在 Tab 下
 */
@Page(name = "Main", moduleId = "feature_home")
internal class MainPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val statusBarHeight = statusBarInset()
        val bottomInset = bottomSafeInset()
        setContent {
            var tab by remember { mutableStateOf(MainTab.Home) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MainColors.pageBg),
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        MainTab.Home -> HomeTab(
                            statusBarHeight = statusBarHeight,
                            onOpenFeed = { tab = MainTab.Feed },
                        )
                        MainTab.Feed -> FeedTab(statusBarHeight = statusBarHeight)
                        MainTab.Lab -> LabTab(statusBarHeight = statusBarHeight)
                        MainTab.Me -> MeTab(statusBarHeight = statusBarHeight)
                    }
                }
                MainBottomBar(
                    selected = tab,
                    onSelect = { tab = it },
                    bottomInset = bottomInset,
                )
            }
        }
    }
}

private object MainColors {
    val primary = Color(0xFF1565C0)
    val primaryDark = Color(0xFF0D47A1)
    val accent = Color(0xFF00897B)
    val pageBg = Color(0xFFF3F5F8)
    val card = Color.White
    val textPrimary = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF6B7280)
    val divider = Color(0xFFE5E7EB)
    val danger = Color(0xFFC62828)
}

private enum class MainTab(val label: String) {
    Home("首页"),
    Feed("动态"),
    Lab("实验室"),
    Me("我的"),
}

@Composable
private fun MainBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    bottomInset: Float,
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        HorizontalDivider(color = MainColors.divider, thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { item ->
                val isSelected = selected == item
                val tint = if (isSelected) MainColors.primary else Color(0xFF9CA3AF)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(item) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    TabIcon(tab = item, color = tint, selected = isSelected)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = tint,
                    )
                }
            }
        }
        // edge-to-edge：垫系统底 inset；为 0 时兜底 16，并封顶 48 防异常大值把 Tab 顶出屏
        val safeBottom = when {
            bottomInset <= 0f -> 16f
            bottomInset > 48f -> 48f
            else -> bottomInset
        }
        Spacer(Modifier.height(safeBottom.dp))
    }
}

/** Canvas 矢量图标，对齐官方 NavigationBarDemo 的 SimpleIcon 做法 */
@Composable
private fun TabIcon(tab: MainTab, color: Color, selected: Boolean) {
    val indicator = if (selected) MainColors.primary.copy(alpha = 0.12f) else Color.Transparent
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .background(indicator, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val stroke = 2.dp.toPx()
            when (tab) {
                MainTab.Home -> {
                    val path = Path().apply {
                        moveTo(size.width / 2, stroke)
                        lineTo(size.width - stroke, size.height * 0.45f)
                        lineTo(size.width - stroke, size.height - stroke)
                        lineTo(stroke, size.height - stroke)
                        lineTo(stroke, size.height * 0.45f)
                        close()
                    }
                    drawPath(path, color, style = Stroke(width = stroke))
                    drawRect(
                        color = color,
                        topLeft = Offset(size.width * 0.38f, size.height * 0.55f),
                        size = Size(
                            size.width * 0.24f,
                            size.height * 0.45f - stroke,
                        ),
                    )
                }
                MainTab.Feed -> {
                    val gap = size.height / 4f
                    for (i in 0..2) {
                        val y = gap * (i + 0.7f)
                        drawLine(
                            color,
                            start = Offset(stroke * 2, y),
                            end = Offset(size.width - stroke * 2, y),
                            strokeWidth = stroke,
                        )
                    }
                }
                MainTab.Lab -> {
                    drawCircle(
                        color,
                        radius = size.minDimension * 0.18f,
                        center = Offset(size.width * 0.35f, size.height * 0.35f),
                        style = Stroke(width = stroke),
                    )
                    drawCircle(
                        color,
                        radius = size.minDimension * 0.22f,
                        center = Offset(size.width * 0.62f, size.height * 0.58f),
                        style = Stroke(width = stroke),
                    )
                }
                MainTab.Me -> {
                    val headR = size.width * 0.18f
                    drawCircle(
                        color,
                        headR,
                        Offset(size.width / 2, size.height * 0.28f),
                        style = Stroke(width = stroke),
                    )
                    val body = Path().apply {
                        moveTo(stroke, size.height - stroke)
                        quadraticBezierTo(
                            size.width / 2,
                            size.height * 0.42f,
                            size.width - stroke,
                            size.height - stroke,
                        )
                    }
                    drawPath(body, color, style = Stroke(width = stroke))
                }
            }
        }
    }
}

@Composable
private fun HomeTab(statusBarHeight: Float, onOpenFeed: () -> Unit) {
    val user = AuthSession.repo.currentUser()
    val name = user?.name ?: "访客"
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MainColors.primary, MainColors.primaryDark),
                        ),
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = (statusBarHeight + 20f).dp, bottom = 28.dp),
            ) {
                Column {
                    Text(
                        text = "Kuikly Demo",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "你好，$name",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Compose Track · 四端 Demo Skeleton",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MainColors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Text(
                    text = "今日概览",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MainColors.textPrimary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "登录态",
                        value = if (user != null) "已登录" else "未登录",
                        accent = if (user != null) MainColors.accent else MainColors.textSecondary,
                    )
                    Spacer(Modifier.width(10.dp))
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "Mock",
                        value = MockBackend.scenario.name,
                        accent = MainColors.primary,
                    )
                }
            }
        }
        item {
            Text(
                text = "快捷入口",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MainColors.textPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "动态",
                    subtitle = "Feed + Mock",
                    tint = MainColors.primary,
                    onClick = onOpenFeed,
                )
                Spacer(Modifier.width(10.dp))
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "完整 Feed",
                    subtitle = "FeedList 页",
                    tint = MainColors.accent,
                    onClick = { Utils.currentBridgeModule().openPage(PageNames.FeedList) },
                )
            }
        }
        item {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "动画 Lab",
                    subtitle = "Compose Anim",
                    tint = Color(0xFFE65100),
                    onClick = { Utils.currentBridgeModule().openPage(PageNames.ComposeAnim) },
                )
                Spacer(Modifier.width(10.dp))
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Demo Map",
                    subtitle = "全部样例",
                    tint = Color(0xFF5E35B1),
                    onClick = { Utils.currentBridgeModule().openPage(PageNames.Home) },
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color,
) {
    Column(
        modifier = modifier
            .background(MainColors.pageBg, RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        Text(label, fontSize = 12.sp, color = MainColors.textSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = accent)
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MainColors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(14.dp)
                .size(36.dp)
                .background(tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(tint, RoundedCornerShape(3.dp)),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MainColors.textPrimary,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            fontSize = 12.sp,
            color = MainColors.textSecondary,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
        )
    }
}

@Composable
private fun FeedTab(statusBarHeight: Float) {
    var items by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var booted by remember { mutableStateOf(false) }
    var scenario by remember { mutableStateOf(MockBackend.scenario) }

    fun reload() {
        scenario = MockBackend.scenario
        FeedStore.repo.list()
            .onSuccess {
                items = it
                error = null
            }
            .onFailure {
                items = emptyList()
                error = it.message
            }
    }

    if (!booted) {
        booted = true
        if (AuthSession.repo.currentUser() == null) {
            error = "请先登录"
        } else {
            reload()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
            top = (statusBarHeight + 16f).dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "动态",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MainColors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Mock Backend 驱动的 Feed 预览",
                fontSize = 13.sp,
                color = MainColors.textSecondary,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionChip(
                    text = "Mock · ${scenario.name}",
                    onClick = {
                        MockBackend.cycle()
                        if (AuthSession.repo.currentUser() != null) reload()
                        else scenario = MockBackend.scenario
                    },
                )
                ActionChip(
                    text = "完整列表",
                    onClick = { Utils.currentBridgeModule().openPage(PageNames.FeedList) },
                )
            }
        }
        when {
            error != null -> item {
                StatusCard(
                    title = "加载失败",
                    message = error ?: "",
                    tint = MainColors.danger,
                )
            }
            items.isEmpty() -> item {
                StatusCard(
                    title = "暂无动态",
                    message = "切换 Mock 场景或打开完整 FeedList",
                    tint = MainColors.textSecondary,
                )
            }
            else -> items(items, key = { it.id }) { item ->
                Card(
                    onClick = {
                        Utils.currentBridgeModule().openPage(PageNames.FeedList)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MainColors.card),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Text(
                        item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MainColors.textPrimary,
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        item.body,
                        fontSize = 13.sp,
                        color = MainColors.textSecondary,
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionChip(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = MainColors.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .border(1.dp, MainColors.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun StatusCard(title: String, message: String, tint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MainColors.card),
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = tint,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp).fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            fontSize = 13.sp,
            color = MainColors.textSecondary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp).fillMaxWidth(),
        )
    }
}

@Composable
private fun LabTab(statusBarHeight: Float) {
    val labs = listOf(
        Triple("Compose Anim", "动画：Visibility / Color / Size", PageNames.ComposeAnim),
        Triple("Compose List", "LazyColumn 样例", PageNames.ComposeList),
        Triple("Compose Pager", "HorizontalPager 样例", PageNames.ComposePager),
        Triple("Gallery", "色块网格", PageNames.Gallery),
        Triple("Perf Lab", "长列表性能", PageNames.PerfLab),
        Triple("Legacy DSL Lab", "传统 DSL 对照", PageNames.DslLab),
        Triple("Demo Map", "旧 Home 导航地图", PageNames.Home),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
            top = (statusBarHeight + 16f).dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "实验室",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MainColors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Compose Track 与 Legacy DSL 教学入口",
                fontSize = 13.sp,
                color = MainColors.textSecondary,
            )
            Spacer(Modifier.height(8.dp))
        }
        items(labs.size) { index ->
            val (title, subtitle, page) = labs[index]
            Card(
                onClick = { Utils.currentBridgeModule().openPage(page) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MainColors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MainColors.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MainColors.primary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MainColors.textPrimary,
                        )
                        Text(
                            subtitle,
                            fontSize = 12.sp,
                            color = MainColors.textSecondary,
                            modifier = Modifier.padding(top = 20.dp),
                        )
                    }
                    Text("›", fontSize = 22.sp, color = Color(0xFFB0B7C3))
                }
            }
        }
    }
}

@Composable
private fun MeTab(statusBarHeight: Float) {
    val user = AuthSession.repo.currentUser()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = (statusBarHeight + 16f).dp, bottom = 16.dp),
    ) {
        Text(
            text = "我的",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MainColors.textPrimary,
        )
        Spacer(Modifier.height(14.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MainColors.card),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(MainColors.primary, MainColors.accent),
                            ),
                            RoundedCornerShape(28.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (user?.name?.firstOrNull() ?: '?').toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "未登录",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MainColors.textPrimary,
                    )
                    Text(
                        text = if (user != null) "Mock Auth Session" else "请从 Login 进入",
                        fontSize = 13.sp,
                        color = MainColors.textSecondary,
                        modifier = Modifier.padding(top = 26.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MainColors.card),
        ) {
            MenuRow("Permission Demo", PageNames.PermissionDemo)
            HorizontalDivider(color = MainColors.divider, thickness = 0.5.dp)
            MenuRow("Share Demo", PageNames.ShareDemo)
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                AuthSession.repo.logout()
                Utils.currentBridgeModule().openPage(PageNames.Login, closeCurPage = true)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MainColors.danger),
        ) {
            Text("退出登录", fontSize = 15.sp, color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MenuRow(title: String, page: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Utils.currentBridgeModule().openPage(page) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = MainColors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text("›", fontSize = 20.sp, color = Color(0xFFB0B7C3))
    }
}
