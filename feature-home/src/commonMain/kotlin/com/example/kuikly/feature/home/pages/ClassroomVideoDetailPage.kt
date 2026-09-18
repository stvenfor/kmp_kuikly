package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.ExperimentalLayoutApi
import com.tencent.kuikly.compose.foundation.layout.FlowRow
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
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.HorizontalDivider
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.RectangleShape
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 视频详情 — Flutter `VideoDetailPage`
 * （`features/classroom/lib/view/video_detail_page.dart`）复刻（P2-W4a 接入路由
 * `classroomVideoDetail`）。
 *
 * **真源**：黑色 220 头（返回 + 播放图标 + `ClassroomMockData.videoTitle` 字幕三行）+
 * `TabBar(简介/评论 22)` + 简介页（👍 3983 + 👎 + 5 tag + 上传者/打赏 + 专辑 part 横滑 +
 * 点赞榜🥇）+ 评论页 + 底部（收藏/分享/r24 「开启配音」绿 CTA）。
 *
 * **刻度**：Flutter classroom 模块 view/theme **全用裸逻辑 px**，故一律裸 `dp`/`sp`，
 * 不加 `ProvideDesignScale`。
 *
 * ponytail 天花板（不弹回）：
 * - **不接视频播放 SDK**（按 handoff）；顶部 220 黑区用居中「▶」字形近似 `Icons.play_circle_fill`。
 * - `TabBar` 用 Kuikly `TabRow`/`Tab` 组件稳定性未知 → 用 `remember` + 手动 Column Row 切换。
 * - 头像 / part 缩略图无图 → 用 `placeholder_color` 占位 + Unicode 字形。
 */
@Page(name = "ClassroomVideoDetail", moduleId = "feature_home")
internal class ClassroomVideoDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var tab by remember { mutableStateOf(0) }
            var selectedPart by remember { mutableStateOf(0) }

            val parts = VIDEO_PARTS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClassroomPalette.background),
            ) {
                VideoHeader(
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                VideoTabs(selected = tab, onSelect = { tab = it })
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        0 -> IntroTab(
                            parts = parts,
                            selectedIndex = selectedPart,
                            onPartSelected = { selectedPart = it },
                        )
                        else -> CommentsTab()
                    }
                }
                BottomBar(
                    bottomInset = bottom,
                    onStartDubbing = { Utils.currentBridgeModule().toast("开启配音（开发中）") },
                )
            }
        }
    }
}

private data class VideoPart(val title: String, val badge: String?)

/** Flutter `ClassroomMockData.videoTitle` / `videoAlbumParts`。 */
private const val VIDEO_TITLE =
    "恐龙科幻电影回归：《侏罗纪世界2：失落王国》电影预告"

private val VIDEO_PARTS = listOf(
    VideoPart("Part 1 制服牛油果小怪兽", "试听"),
    VideoPart("Part 2 想到制服牛油果...", "付费"),
    VideoPart("Part 3 顺利制服...", null),
)

/** 黑色 220 头（`Container(height: 220, color: Colors.black87)`）+ 返回箭头 + 三行字幕。 */
@Composable
private fun VideoHeader(topInset: Float, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFF212121)),
    ) {
        Text(
            "‹",
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier
                .padding(start = 8.dp, top = (topInset + 8f).dp)
                .clickable(onClick = onBack)
                .padding(8.dp),
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", fontSize = 64.sp, color = Color.White.copy(alpha = 0.54f))
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                // Flutter `Positioned(bottom: 40, left: 16, right: 16)` 三行字幕。
                .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
        ) {
            Text(
                VIDEO_TITLE,
                fontSize = 13.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "It's said they can accelerate faster than a Ferrari.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
            Text(
                "据说他们能比法拉利更快地加速",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.54f),
            )
        }
    }
}

/** Flutter `TabBar(简介/评论 22)`：选中 accent 绿 + 未选 textGray，3 高 accent 指示条。 */
@Composable
private fun VideoTabs(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClassroomPalette.cardWhite),
    ) {
        VideoTab(
            modifier = Modifier.weight(1f),
            label = "简介",
            selected = selected == 0,
            onClick = { onSelect(0) },
        )
        VideoTab(
            modifier = Modifier.weight(1f),
            label = "评论 22",
            selected = selected == 1,
            onClick = { onSelect(1) },
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(ClassroomPalette.divider),
    )
}

@Composable
private fun VideoTab(modifier: Modifier, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) ClassroomPalette.primaryGreen else ClassroomPalette.textGray,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(if (selected) 24.dp else 0.dp)
                .height(3.dp)
                .background(ClassroomPalette.primaryGreen, RoundedCornerShape(2.dp)),
        )
    }
}

/**
 * Flutter `_IntroTab`：👍 3983 + 👎 + 5 tag chip（合作 绿·10句·难度 PreA1·漫威·经典大片）
 * + 头像+「上传者 小趣友宁Sir」+「打赏」粉胶囊 + 「视频专辑 (20)」+「+ 添加学习计划」绿字 +
 * 100 高水平 part 列表（选中 2 绿边/未选 1 灰边，badge 试听=绿/新课=橙）+ 「点赞榜」+🥇单条。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntroTab(
    parts: List<VideoPart>,
    selectedIndex: Int,
    onPartSelected: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👍", fontSize = 18.sp)
                Spacer(Modifier.width(4.dp))
                Text("3983", fontSize = 13.sp, color = ClassroomPalette.titleBlack)
                Spacer(Modifier.width(16.dp))
                Text("👎", fontSize = 18.sp)
            }
        }
        item {
            // Flutter `SizedBox(height: 8)`（👍 Row → tag `Wrap` 间距，原 12.dp 偏大 4dp）。
            Spacer(Modifier.height(8.dp))
        }
        item {
            // Flutter `Wrap(spacing: 6, runSpacing: 6)` 形制 → FlowRow 自动换行（5 tags 在窄屏
            // 会换到第二行；Kuikly Compose `Row` 单行会越界被裁）。
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TagChip("合作", accent = ClassroomPalette.primaryGreen)
                TagChip("10句", accent = null)
                TagChip("难度 PreA1", accent = null)
                TagChip("漫威", accent = null)
                TagChip("经典大片", accent = null)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(ClassroomPalette.primaryGreenLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("趣", fontSize = 13.sp, color = ClassroomPalette.primaryGreen)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "上传者 小趣友宁Sir",
                    fontSize = 13.sp,
                    color = ClassroomPalette.titleBlack,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFE4EC), RoundedCornerShape(16.dp))
                        .clickable { Utils.currentBridgeModule().toast("「打赏」即将接入") }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text("打赏", fontSize = 12.sp, color = Color(0xFFE91E63))
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "视频专辑 (20)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClassroomPalette.titleBlack,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "+ 添加学习计划",
                    fontSize = 13.sp,
                    color = ClassroomPalette.primaryGreen,
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().toast("「添加学习计划」即将接入")
                    },
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(parts.size) { index ->
                    val part = parts[index]
                    val selected = index == selectedIndex
                    VideoPartCard(
                        part = part,
                        selected = selected,
                        onClick = { onPartSelected(index) },
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            Text(
                "点赞榜",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClassroomPalette.titleBlack,
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        item { LeaderboardRow() }
    }
}

/** Flutter `_Tag`：`Padding(symmetric h8 v4)` + `color.withValues(alpha: 0.15)` 12 圆角。 */
@Composable
private fun TagChip(label: String, accent: Color?) {
    val bg = if (accent != null) accent.copy(alpha = 0.15f) else ClassroomPalette.background
    val fg = accent ?: ClassroomPalette.textGray
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(label, fontSize = 11.sp, color = fg)
    }
}

/** Flutter part 卡：140 宽 + 5 padding + 边框（选中 2 绿/未选 1 灰）+ 可选 badge + 标题 12。 */
@Composable
private fun VideoPartCard(part: VideoPart, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) ClassroomPalette.primaryGreen else ClassroomPalette.divider,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        val badge = part.badge
        if (badge != null) {
            val bg = if (badge == "试听") ClassroomPalette.primaryGreenLight else Color(0xFFFFF3E0)
            val fg = if (badge == "试听") ClassroomPalette.primaryGreen else Color(0xFFFF8A34)
            Box(
                modifier = Modifier
                    .background(bg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(badge, fontSize = 10.sp, color = fg)
            }
            Spacer(Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        Text(
            part.title,
            fontSize = 12.sp,
            color = ClassroomPalette.titleBlack,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Flutter `_LeaderboardItem`：🥇 + 头像 + 名字 + 时间地点 + 👍 红 + 1.1万。 */
@Composable
private fun LeaderboardRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🥇", fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(ClassroomPalette.primaryGreenLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("美", fontSize = 14.sp, color = ClassroomPalette.primaryGreen)
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("美诺明年夏天见", fontSize = 13.sp, color = ClassroomPalette.titleBlack)
            Text(
                "2020-11-03 · 杭州市",
                fontSize = 11.sp,
                color = ClassroomPalette.textGray,
            )
        }
        Text("👍", fontSize = 16.sp, color = Color(0xFFFF3B30))
        Spacer(Modifier.width(4.dp))
        Text("1.1万", fontSize = 12.sp, color = ClassroomPalette.textGray)
    }
}

/** Flutter `_CommentsTab`：ListTile × 2 占位。 */
@Composable
private fun CommentsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
    ) {
        item { CommentRow("A", "评论用户A", "配音很棒！") }
        item { HorizontalDivider(color = ClassroomPalette.divider) }
        item { CommentRow("B", "评论用户B", "发音标准，继续加油") }
    }
}

@Composable
private fun CommentRow(avatar: String, name: String, content: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(ClassroomPalette.primaryGreenLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(avatar, fontSize = 16.sp, color = ClassroomPalette.primaryGreen)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, color = ClassroomPalette.titleBlack)
            Text(content, fontSize = 13.sp, color = ClassroomPalette.textGray)
        }
    }
}

/** Flutter `_BottomBar`：白底 + 顶部向上 `BoxShadow(black 5% / blur 8 / offset(0,-2))` + 收藏/分享 + 48·r24 「开启配音」绿 CTA。 */
@Composable
private fun BottomBar(bottomInset: Float, onStartDubbing: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `BoxShadow(Colors.black.withValues(alpha: 0.05), blurRadius: 8, offset(0,-2))`
            // 的向上投影近似（Kuikly `.shadow` 无 offset 参数，四向投影；底侧出屏不可见）。
            .shadow(
                4.dp,
                RectangleShape,
                ambientColor = Color(0x0D000000),
                spotColor = Color(0x0D000000),
            )
            .background(ClassroomPalette.cardWhite)
            .padding(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = (bottomInset + 12f).dp,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BottomIcon("☆", "收藏") { Utils.currentBridgeModule().toast("「收藏」即将接入") }
            Spacer(Modifier.width(16.dp))
            BottomIcon("⇪", "分享") { Utils.currentBridgeModule().toast("「分享」即将接入") }
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        ClassroomPalette.primaryGreen,
                        RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.dp),
                    )
                    .clickable(onClick = onStartDubbing),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "开启配音",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun BottomIcon(glyph: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(glyph, fontSize = 22.sp, color = ClassroomPalette.titleBlack)
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = ClassroomPalette.titleBlack)
    }
}