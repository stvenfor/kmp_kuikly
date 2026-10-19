package com.example.kuikly.pages

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
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.classroom.VideoAlbumPart
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
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
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page

/**
 * 视频详情 — Flutter `VideoDetailPage` 复刻（源路由 `classroomVideoDetail`）。
 *
 * 真源：黑色视频头（播放 + 返回 + 标题/英文字幕）+ Tab（简介 / 评论 22）+ 简介：点赞行 +
 * tags + 上传者 + 视频专辑分集横列（选中态）+ 点赞榜；评论：两条评论；底部栏（收藏 / 分享 /
 * 开启配音）。Tab 用本地 state 近似源 `TabController`（本模块既有页面未用 TabBar API）。
 *
 * **刻度**：Flutter classroom 模块用裸逻辑 px（designWidth = 设备宽 480），KMP 裸 `.dp`/`.sp`
 * 在 golden 比对空间会偏大 16.7%，故用 `ProvideDesignScale(pagerData.pageViewWidth,
 * designWidth = 480f)` + `.su`/`.susp` 对齐。
 */
@Page("ClassroomVideoDetail")
internal class ClassroomVideoDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            ProvideDesignScale(pagerData.pageViewWidth, designWidth = 480f) {
                var selectedTab by remember { mutableStateOf(0) }
                var selectedPart by remember { mutableStateOf(0) }

                val parts = remember { ClassroomStore.repo.videoParts().getOrDefault(emptyList()) }

                Column(modifier = Modifier.fillMaxSize().background(ClassroomPalette.background)) {
                    VideoHeader(top = top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ClassroomPalette.cardWhite)
                            .padding(vertical = 4.su),
                    ) {
                        TabLabel("简介", selectedTab == 0) { selectedTab = 0 }
                        TabLabel("评论 22", selectedTab == 1) { selectedTab = 1 }
                    }
                    if (selectedTab == 0) {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(16.su),
                        ) {
                            item {
                                IntroTab(
                                    parts = parts,
                                    selectedIndex = selectedPart,
                                    onPartSelected = { selectedPart = it },
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(16.su),
                        ) {
                            item {
                                CommentItem("A", "评论用户A", "配音很棒！")
                                Spacer(Modifier.height(12.su))
                                CommentItem("B", "评论用户B", "发音标准，继续加油")
                            }
                        }
                    }
                    BottomBar(
                        onStart = { Utils.currentBridgeModule().toast("开启配音功能开发中") },
                    )
                }
            }
        }
    }
}

/** 黑色视频头（源 `_VideoHeader`）。 */
@Composable
private fun VideoHeader(top: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.su)
            .background(Color(0xFF212121)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(64.su)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶️", fontSize = 48.susp, color = Color.White.copy(alpha = 0.6f))
        }
        Text(
            "‹",
            fontSize = 20.susp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable { Utils.currentBridgeModule().closePage() }
                .padding(start = 8.su, top = (top + 8f).dp, end = 8.su, bottom = 8.su),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.su, end = 16.su, bottom = 40.su),
        ) {
            Text(
                "恐龙科幻电影回归：《侏罗纪世界2：失落王国》电影预告",
                fontSize = 13.susp,
                color = Color.White,
            )
            Spacer(Modifier.height(4.su))
            Text("It's said they can accelerate faster than a Ferrari.", fontSize = 12.susp, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(2.su))
            Text("据说他们能比法拉利更快地加速", fontSize = 11.susp, color = Color.White.copy(alpha = 0.54f))
        }
    }
}

@Composable
private fun TabLabel(label: String, selected: Boolean, onTap: () -> Unit) {
    Text(
        label,
        fontSize = 15.susp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) ClassroomPalette.primaryGreen else ClassroomPalette.textGray,
        modifier = Modifier
            .clickable(onClick = onTap)
            .padding(start = 16.su, end = 24.su, top = 12.su, bottom = 8.su),
    )
}

/** 简介 Tab（源 `_IntroTab`）。 */
@Composable
private fun IntroTab(parts: List<VideoAlbumPart>, selectedIndex: Int, onPartSelected: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("👍", fontSize = 18.susp)
        Spacer(Modifier.width(4.su))
        Text("3983", fontSize = 14.susp)
        Spacer(Modifier.width(16.su))
        Text("👎", fontSize = 18.susp)
    }
    Spacer(Modifier.height(8.su))
    Row {
        Tag("合作", ClassroomPalette.primaryGreen)
        Spacer(Modifier.width(6.su))
        Tag("10句", null)
        Spacer(Modifier.width(6.su))
        Tag("难度 PreA1", null)
        Spacer(Modifier.width(6.su))
        Tag("漫威", null)
        Spacer(Modifier.width(6.su))
        Tag("经典大片", null)
    }
    Spacer(Modifier.height(16.su))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.su)
                .background(ClassroomPalette.primaryGreenLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("趣", fontSize = 14.susp, color = ClassroomPalette.primaryGreen)
        }
        Spacer(Modifier.width(8.su))
        Text("上传者 小趣友宁Sir", fontSize = 13.susp, modifier = Modifier.weight(1f))
        Text(
            "打赏",
            fontSize = 12.susp,
            color = Color(0xFFFF4081),
            modifier = Modifier
                .background(Color(0xFFFFE4EC), RoundedCornerShape(16.su))
                .padding(horizontal = 12.su, vertical = 4.su),
        )
    }
    Spacer(Modifier.height(20.su))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("视频专辑 (20)", fontSize = 15.susp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack, modifier = Modifier.weight(1f))
        Text("+ 添加学习计划", fontSize = 13.susp, color = ClassroomPalette.primaryGreen)
    }
    Spacer(Modifier.height(12.su))
    // 分集横列（3 项，Row 即可容纳）。
    Row {
        parts.forEachIndexed { i, part ->
            val selected = i == selectedIndex
            Column(
                modifier = Modifier
                    .width(140.su)
                    .background(
                        Color.Transparent,
                        RoundedCornerShape(8.su),
                    )
                    .border(
                        1.su,
                        if (selected) ClassroomPalette.primaryGreen else Color.Transparent,
                        RoundedCornerShape(8.su),
                    )
                    .clickable { onPartSelected(i) }
                    .padding(10.su),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) ClassroomPalette.primaryGreenLight else ClassroomPalette.divider,
                            RoundedCornerShape(8.su),
                        )
                        .padding(8.su),
                ) {
                    val badge = part.badge
                    if (badge != null) {
                        Text(
                            badge,
                            fontSize = 10.susp,
                            color = if (badge == "试听") ClassroomPalette.primaryGreen else Color(0xFFFF8A34),
                            modifier = Modifier
                                .background(
                                    if (badge == "试听") ClassroomPalette.primaryGreenLight else Color(0xFFFFF3E0),
                                    RoundedCornerShape(4.su),
                                )
                                .padding(horizontal = 6.su, vertical = 2.su),
                        )
                    }
                }
                Spacer(Modifier.height(6.su))
                Text(part.title, fontSize = 12.susp, color = ClassroomPalette.titleBlack, maxLines = 2)
            }
            if (i < parts.lastIndex) Spacer(Modifier.width(10.su))
        }
    }
    Spacer(Modifier.height(20.su))
    Text("点赞榜", fontSize = 15.susp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
    Spacer(Modifier.height(12.su))
    LeaderboardItem()
}

/** 标签（源 `_Tag`）。 */
@Composable
private fun Tag(label: String, color: Color?) {
    Text(
        label,
        fontSize = 11.susp,
        color = color ?: ClassroomPalette.textGray,
        modifier = Modifier
            .background(
                color?.copy(alpha = 0.15f) ?: ClassroomPalette.background,
                RoundedCornerShape(4.su),
            )
            .padding(horizontal = 8.su, vertical = 4.su),
    )
}

/** 点赞榜项（源 `_LeaderboardItem`）。 */
@Composable
private fun LeaderboardItem() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🥇", fontSize = 20.susp)
        Spacer(Modifier.width(8.su))
        Box(
            modifier = Modifier
                .size(36.su)
                .background(ClassroomPalette.primaryGreenLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("美", fontSize = 14.susp, color = ClassroomPalette.primaryGreen)
        }
        Spacer(Modifier.width(8.su))
        Column(modifier = Modifier.weight(1f)) {
            Text("美诺明年夏天见", fontSize = 13.susp, color = ClassroomPalette.titleBlack)
            Text("2020-11-03 · 杭州市", fontSize = 11.susp, color = ClassroomPalette.textGray)
        }
        Text("👍 1.1万", fontSize = 12.susp, color = Color(0xFFFF4081))
    }
}

/** 评论项（源 `_CommentsTab`）。 */
@Composable
private fun CommentItem(avatar: String, name: String, content: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.su)
                .background(ClassroomPalette.primaryGreenLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(avatar, fontSize = 14.susp, color = ClassroomPalette.primaryGreen)
        }
        Spacer(Modifier.width(8.su))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 13.susp, fontWeight = FontWeight.Medium, color = ClassroomPalette.titleBlack)
            Text(content, fontSize = 12.susp, color = ClassroomPalette.textGray)
        }
    }
}

/** 底部栏（源 `_BottomBar`）。 */
@Composable
private fun BottomBar(onStart: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClassroomPalette.cardWhite)
            .padding(start = 16.su, end = 16.su, top = 8.su, bottom = 12.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⭐", fontSize = 20.susp)
        Spacer(Modifier.width(4.su))
        Text("收藏", fontSize = 12.susp)
        Spacer(Modifier.width(16.su))
        Text("↗️", fontSize = 20.susp)
        Spacer(Modifier.width(4.su))
        Text("分享", fontSize = 12.susp)
        Spacer(Modifier.width(16.su))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.su)
                .background(ClassroomPalette.primaryGreen, RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.su))
                .clickable(onClick = onStart),
            contentAlignment = Alignment.Center,
        ) {
            Text("开启配音", fontSize = 15.susp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}
