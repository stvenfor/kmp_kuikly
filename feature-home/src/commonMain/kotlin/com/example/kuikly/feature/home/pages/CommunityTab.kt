package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.Utils
import com.example.kuikly.data.community.CommunityStore
import com.example.kuikly.data.community.Post
import com.example.kuikly.navigation.PageNames
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
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * `CommunityTheme` 令牌镜像（`my_ai_project/features/community/lib/community/theme/community_theme.dart`）。
 *
 * **刻度**：Flutter Community 模块全用**裸逻辑 px**（community_page / post_card_widget /
 * community_theme 均无 `.w/.h/.sp`），不走 `flutter_screenutil` —— 故此处一律裸 `dp`/`sp`，
 * **不要** `.su()`（判断法同 P2-03 `TabBarTokens` / `ChatPalette` KDoc）。
 */
internal object CommunityPalette {
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val fillSecondary = Color(0xFFE9E9EB)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val labelTertiary = Color(0x4D3C3C43)
    val separator = Color(0xFFC6C6C8)
    val likeRed = Color(0xFFFF3B30)

    const val RADIUS_MD = 12f
}

private val COMMUNITY_FILTER_TABS = listOf("最新", "热门", "关注")

/**
 * 社区 tab root — Flutter `CommunityPage` 复刻（Phase-2 / P2-W2b）。
 * 真源：`features/community/lib/community/view/community_page.dart` + `widgets/post_card_widget.dart`。
 * Tap a card opens PostDetail.
 */
@Composable
internal fun CommunityTab(statusBarHeight: Float) {
    val result = remember { CommunityStore.repo.list() }
    var filterTab by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CommunityPalette.background),
    ) {
        CommunityHeader(
            statusBarHeight = statusBarHeight,
            selected = filterTab,
            onSelected = { filterTab = it },
        )
        result.onSuccess { posts ->
            if (posts.isEmpty()) {
                CommunityEmpty()
            } else {
                // Flutter `SliverPadding(EdgeInsets.fromLTRB(16, 8, 16, 0))` + 每卡 bottom 12。
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
                ) {
                    items(posts, key = { it.id }) { post ->
                        Box(modifier = Modifier.padding(bottom = 12.dp)) {
                            PostCard(post = post)
                        }
                    }
                }
            }
        }.onFailure {
            CommunityError(it.message)
        }
    }
}

/** Flutter `_CommunityHeader`：大标题 + 发布钮 + 搜索框 + 筛选 tabs。 */
@Composable
private fun CommunityHeader(
    statusBarHeight: Float,
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `EdgeInsets.fromLTRB(16, AppSafeInsets.top + 8, 16, 0)`。
            .padding(start = 16.dp, end = 16.dp, top = (statusBarHeight + 8f).dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Flutter `CommunityTheme.largeTitle`（32 / w700 / 黑）。
            Text(
                "社区",
                modifier = Modifier.weight(1f),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = CommunityPalette.labelPrimary,
            )
            // Flutter 44×44 位内的 36×36 accent 圆 + 白色 `＋`（size 20）→ push PublishPage。
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { Utils.currentBridgeModule().openPage(PageNames.CommunityPublish) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CommunityPalette.accent, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("＋", fontSize = 20.sp, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Flutter 搜索框：h44 / surface r12 / 0.5 separator 边 / h14 内距 / ⌕18 + hint 13。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(CommunityPalette.surface, RoundedCornerShape(CommunityPalette.RADIUS_MD.dp))
                .border(0.5.dp, CommunityPalette.separator, RoundedCornerShape(CommunityPalette.RADIUS_MD.dp))
                .clickable { Utils.currentBridgeModule().toast("「搜索」即将接入") }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⌕", fontSize = 18.sp, color = CommunityPalette.labelSecondary)
            Spacer(Modifier.width(8.dp))
            Text("搜索动态、话题、用户", fontSize = 13.sp, color = CommunityPalette.labelTertiary)
        }
        Spacer(Modifier.height(12.dp))
        // Flutter `_FilterTabs`：h36 内 15sp 文案 + 6 间隙 + 20×2 accent 下划线，间隔 24。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            COMMUNITY_FILTER_TABS.forEachIndexed { index, label ->
                val active = index == selected
                Column(
                    modifier = Modifier
                        .clickable { onSelected(index) }
                        .padding(end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        label,
                        fontSize = 15.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) CommunityPalette.labelPrimary else CommunityPalette.labelSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(if (active) 20.dp else 0.dp)
                            .height(2.dp)
                            .background(CommunityPalette.accent, RoundedCornerShape(1.dp)),
                    )
                }
            }
        }
    }
}

/**
 * Flutter `PostCardWidget`：groupedCard（surface r12 + 0.5 边）内
 * 用户行（44 头像 + headline）+ 标题/正文 + 点赞条（20 图标 + 13 文案，间隔 24）。
 */
@Composable
private fun PostCard(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `padding: EdgeInsets.fromLTRB(16, 14, 12, 14)`。
            .background(CommunityPalette.surface, RoundedCornerShape(CommunityPalette.RADIUS_MD.dp))
            .border(0.5.dp, CommunityPalette.separator, RoundedCornerShape(CommunityPalette.RADIUS_MD.dp))
            .clickable {
                Utils.currentBridgeModule().openPage(
                    PageNames.PostDetail,
                    userData = JSONObject().apply { put("id", post.id) },
                )
            }
            .padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
    ) {
        // Flutter `UserInfoWidget` + ellipsis 按钮（44×44 位 / ⋯ 20）。
        Row(verticalAlignment = Alignment.Top) {
            // CacheImageUtils.circle(44) 未加载占位（灰底圆 + 首字母，同仓库惯例）。
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(CommunityPalette.fillSecondary, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    post.author.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommunityPalette.labelPrimary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Flutter `CommunityTheme.headline`（17 / w600）。
                Text(
                    post.author,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommunityPalette.labelPrimary,
                )
                Spacer(Modifier.height(2.dp))
                // Flutter `'{formatPublishTime} · {source}'`（caption 13）—— mock 无该字段，
                // 用「点赞 N · 评论 M」保持次级信息行密度。
                Text(
                    "点赞 ${post.likeCount} · 评论 ${post.commentCount}",
                    fontSize = 13.sp,
                    color = CommunityPalette.labelSecondary,
                )
            }
            Text(
                "⋯",
                fontSize = 20.sp,
                color = CommunityPalette.labelSecondary,
                modifier = Modifier
                    .clickable { Utils.currentBridgeModule().toast("「更多」即将接入") }
                    .padding(12.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        // Flutter 帖子正文（body 15 / labelPrimary）；mock Post 额外有 title，作为首行渲染。
        Text(post.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = CommunityPalette.labelPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            post.body,
            fontSize = 15.sp,
            color = CommunityPalette.labelPrimary,
        )
        // Flutter `LikeBarWidget`：♡ / 💬 / ↗（图标 20 + 文案 13，间隔 24，顶部 12）。
        Row(modifier = Modifier.padding(top = 12.dp)) {
            PostAction(glyph = "♡", label = if (post.likeCount > 0) post.likeCount.toString() else "赞")
            Spacer(Modifier.width(24.dp))
            PostAction(glyph = "💬", label = if (post.commentCount > 0) post.commentCount.toString() else "评论")
            Spacer(Modifier.width(24.dp))
            PostAction(glyph = "↗", label = "分享")
        }
    }
}

/** Flutter `_ActionButton`（图标 20 + 4 间隙 + 13 文案 / labelSecondary）。 */
@Composable
private fun PostAction(glyph: String, label: String) {
    Row(
        modifier = Modifier
            .clickable { Utils.currentBridgeModule().toast("「$label」即将接入") }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(glyph, fontSize = 20.sp, color = CommunityPalette.labelSecondary)
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = CommunityPalette.labelSecondary)
    }
}

/** Flutter `_EmptyState`：大图标 + 两行文案。 */
@Composable
private fun CommunityEmpty() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("💬", fontSize = 56.sp, color = CommunityPalette.labelTertiary)
        Spacer(Modifier.height(12.dp))
        Text("暂无动态", fontSize = 13.sp, color = CommunityPalette.labelSecondary)
        Spacer(Modifier.height(8.dp))
        Text("发布第一条动态，开始互动吧", fontSize = 13.sp, color = CommunityPalette.labelTertiary)
    }
}

/** Flutter `_ErrorState`：图标 + 加载失败 + 消息 + 重试（accent 填充钮）。 */
@Composable
private fun CommunityError(message: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("⚠", fontSize = 48.sp, color = CommunityPalette.labelTertiary)
        Spacer(Modifier.height(12.dp))
        Text("加载失败", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = CommunityPalette.labelPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            message ?: "错误",
            fontSize = 13.sp,
            color = CommunityPalette.labelSecondary,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .background(CommunityPalette.accent, RoundedCornerShape(8.dp))
                .clickable { Utils.currentBridgeModule().toast("「重试」即将接入") }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text("重试", fontSize = 17.sp, color = Color.White)
        }
    }
}
