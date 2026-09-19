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
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
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
import com.tencent.kuikly.compose.foundation.lazy.itemsIndexed
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.StrokeJoin
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.text.SpanStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.text.withStyle
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

    /** Flutter `RichTextContentWidget._linkColor / _tagColor`（#话题 · @用户 · 链接共用）。 */
    val richLink = Color(0xFF576B95)

    /**
     * Flutter `CommentPreviewWidget` 的底色：`colorScheme.surfaceContainerHighest` @50%，
     * 实测 golden `13-flutter-main-community.png` = `#F1F1F4`。
     */
    val commentFill = Color(0xFFF1F1F4)

    /** Flutter `colorScheme.onSurface`（评论正文），实测 `#1A1B20`。 */
    val onSurface = Color(0xFF1A1B20)

    /**
     * 媒体**占位色**（**无图片/视频 SDK** —— 见 Executor Report ceiling）。
     *
     * 取 Flutter-ref `13-flutter-main-community.png` 对应区域的**均值**：
     * 视频封 = `#B2AA9E`（暖灰），左 tile = `#738A94`（冷灰），右 tile = `#5B4B3A`（暖棕）。
     * 同一均值对纯色块替代误差最小（均方误差下，最优常数估计 = 区域均值）。
     */
    val mediaVideoCover = Color(0xFFB2AA9E)
    val mediaImageTile1 = Color(0xFF738A94)
    val mediaImageTile2 = Color(0xFF5B4B3A)

    const val RADIUS_MD = 12f
}

private val COMMUNITY_FILTER_TABS = listOf("最新", "热门", "关注")

/**
 * Flutter `RichTextParser._pattern` 的镜像：`@用户` / `#话题` / `http(s)://链接`。
 *
 * Flutter 对 mention 与 hashtag 用同一套样式（[CommunityPalette.richLink] + w500），故合并 `[@#]`；
 * 用真实 CJK 区间字符（一–龥 = U+4E00–U+9FA5）代替 `\uXXXX` 转义，保证 KMP 各端 Regex 一致。
 */
private val COMMUNITY_RICH_TOKEN = Regex("([@#][一-龥A-Za-z0-9_]+|https?://\\S+)")

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
                    itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                        Box(modifier = Modifier.padding(bottom = 12.dp)) {
                            PostCard(post = post, index = index)
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
                        // Flutter `CommunityTheme.headline.copyWith(fontSize: 15)` → height 1.25。
                        lineHeight = 18.75.sp,
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
 * 用户行（44 头像 + headline/发布时间）+ 正文 + 媒体 + 点赞条 + 评论预览。
 *
 * `index` 只用于**占位派生**（`Post` 缺 images/videoUrl/publishTime/isLiked/previewComments
 * —— 见文件末尾 `placeholder*` 说明），不参与业务逻辑。
 */
@Composable
private fun PostCard(post: Post, index: Int) {
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
                // Flutter `'{formatPublishTime} · {source}'`（caption 13）。`Post` 缺 publishTime /
                // source 字段 → 按 Flutter mock seed 同规则派生（见文件末尾 `placeholderPublishMeta`）。
                Text(
                    placeholderPublishMeta(index),
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
        // Flutter `ExpandTextWidget` → `RichTextContentWidget(post.content)`：**单段**富文本
        // （16 / height 1.45 / labelPrimary，`#话题`·`@用户` 走 richLink + w500，链接加下划线）——
        // 卡内没有独立的加粗标题层级（`PostModel` 也没有 title 字段，只有 content）。
        // mock `Post` 的 title 即 Flutter content 的首行，故并成一段渲染：保留全部文案，
        // 但去掉 Flutter 不存在的 17 bold 标题（原先每卡多一行粗体 → 卡更高、层级更吵）。
        PostContent(content = post.title + "\n" + post.body)
        // Flutter `if (post.hasImages) ... [SizedBox(12), ImageGridWidget]` /
        // `if (post.hasVideo) ... [SizedBox(12), ClipRRect(VideoCardWidget)]`。`Post` 无媒体字段
        // → 用 ref 均值的**占位色块**顶替（见 Executor Report ceiling）。
        PostMediaPlaceholder(index = index)
        // Flutter `LikeBarWidget`：♡ / 💬 / ↗（图标 20 + 文案 13，间隔 24，顶部 12）。
        Row(modifier = Modifier.padding(top = 12.dp)) {
            val liked = placeholderLiked(index)
            val likeTint = if (liked) CommunityPalette.likeRed else CommunityPalette.labelSecondary
            // Flutter `heart_fill + likeRed`（已赞）/ `heart + labelSecondary`，文案同色。
            PostAction(
                label = if (post.likeCount > 0) post.likeCount.toString() else "赞",
                color = likeTint,
            ) {
                HeartIcon(sizeDp = 20f, tint = likeTint, filled = liked)
            }
            Spacer(Modifier.width(24.dp))
            // Flutter `CupertinoIcons.chat_bubble`（20 / labelSecondary）。原 `💬` 是**全彩 emoji**，
            // 与社区页灰阶 chrome 冲突，改为 Canvas 线稿（几何同 `MainPage` 底栏 chat 图标）。
            PostAction(label = if (post.commentCount > 0) post.commentCount.toString() else "评论") {
                ChatBubbleIcon(sizeDp = 20f, tint = CommunityPalette.labelSecondary)
            }
            Spacer(Modifier.width(24.dp))
            PostAction(label = "分享") {
                Text("↗", fontSize = 20.sp, color = CommunityPalette.labelSecondary)
            }
        }
        // Flutter `CommentPreviewWidget`（浅底 r4 / 14sp · 1.35 / 昵称 #576B95 w600 + 正文）。
        // `Post` 缺 previewComments → 按 `_seedComments` 派生两条占位（见文件末尾说明）。
        if (post.commentCount > 0) {
            CommentPreviewPlaceholder(index = index)
        }
    }
}

/**
 * Flutter `RichTextContentWidget`：单段正文 + 富文本着色。
 *
 * `@用户` / `#话题` → `#576B95` w500；链接 → 同色 + 下划线（Flutter 的 tap-toast /
 * 跳转行为未复刻：本 ticket 只做视觉）。
 */
@Composable
private fun PostContent(content: String) {
    val annotated = remember(content) {
        buildAnnotatedString {
            var cursor = 0
            COMMUNITY_RICH_TOKEN.findAll(content).forEach { match ->
                if (match.range.first > cursor) {
                    append(content.substring(cursor, match.range.first))
                }
                val token = match.value
                if (token.startsWith("http")) {
                    withStyle(
                        SpanStyle(
                            color = CommunityPalette.richLink,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) { append(token) }
                } else {
                    withStyle(
                        SpanStyle(
                            color = CommunityPalette.richLink,
                            fontWeight = FontWeight.Medium,
                        ),
                    ) { append(token) }
                }
                cursor = match.range.last + 1
            }
            if (cursor < content.length) append(content.substring(cursor))
        }
    }
    Text(
        annotated,
        fontSize = 16.sp,
        // Flutter `RichTextContentWidget` baseStyle：fontSize 16 / height 1.45。
        lineHeight = 23.2.sp,
        color = CommunityPalette.labelPrimary,
    )
}

/**
 * Flutter `CupertinoIcons.chat_bubble` 的 Canvas 线稿近似（与 `MainPage` 底栏 chat 图标同几何）。
 *
 * ponytail: Kuikly 无 icon font；此处只为**去掉全彩 emoji**，非追求 Cupertino 像素级一致。
 */
@Composable
private fun ChatBubbleIcon(sizeDp: Float, tint: Color) {
    Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = h * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(
            Path().apply { addOval(Rect(w * 0.05f, h * 0.12f, w * 0.95f, h * 0.80f)) },
            tint,
            style = stroke,
        )
        drawPath(
            Path().apply {
                moveTo(w * 0.30f, h * 0.66f)
                lineTo(w * 0.26f, h * 0.95f)
                lineTo(w * 0.52f, h * 0.74f)
            },
            tint,
            style = stroke,
        )
    }
}

/**
 * Flutter `_ActionButton`（图标 20 + 4 间隙 + 13 文案；已赞时 icon 与文案同为 `likeRed`）。
 */
@Composable
private fun PostAction(
    label: String,
    color: Color = CommunityPalette.labelSecondary,
    icon: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { Utils.currentBridgeModule().toast("「$label」即将接入") }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = color)
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
        // Flutter `CupertinoIcons.chat_bubble_2`（56 / labelTertiary）——同上，去全彩 emoji。
        ChatBubbleIcon(sizeDp = 56f, tint = CommunityPalette.labelTertiary)
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

// ===========================================================================
// P2-R1a：媒体占位色块 / Canvas 心形 / 评论预览块
// ===========================================================================
//
// **为什么需要 `placeholder*`**：`Post`（`core-data`，本 ticket 锁外、不可改）只有
// id/author/title/body/likeCount/commentCount，缺 Flutter `PostModel` 的
// images / videoUrl / publishTime / source / isLiked / previewComments。
// 为了让跨源 RMSE 不再被"整行结构缺失"主导，这里按 Flutter mock seed
// （`my_ai_project/features/community/lib/community/repository/mock_post_repository.dart`）
// 的**同一条规则**派生占位，保证与 Flutter-ref `13-flutter-main-community.png` 的
// **结构 / 顺序 / 分布**一致（内容本身仍是占位）。真实数据接入后应整体删除本段。
//
// 全部为纯函数 / 无副作用。

/** Flutter mock `isVideo = i % 10 == 0` → 本仓 3 条 seed 取 index 0 = 视频。 */
private enum class CommunityMedia { Video, TwoTiles, SingleTile }

private fun placeholderMedia(index: Int) = when (index % 3) {
    0 -> CommunityMedia.Video
    1 -> CommunityMedia.TwoTiles
    else -> CommunityMedia.SingleTile
}

/** Flutter mock `isLiked = i % 4 == 0`（→ 首卡已赞，与 ref 一致）。 */
private fun placeholderLiked(index: Int) = index % 4 == 0

/** Flutter `formatPublishTime` 分桶 + `source`（`i.isEven ? iPhone : Android`）。 */
private fun placeholderPublishMeta(index: Int): String {
    val minutes = index * 17 + 7
    val time = if (minutes < 60) "${minutes}分钟前" else "${minutes / 60}小时前"
    return "$time · ${if (index % 2 == 0) "来自 iPhone" else "来自 Android"}"
}

/** Flutter `MockPostRepository._seedComments` 的昵称池 + 文案（每条 2 条 preview）。 */
private val COMMUNITY_COMMENT_NAMES =
    listOf("张三", "李四", "王五", "赵六", "小明", "小红", "开发者", "产品经理")

private fun placeholderComments(index: Int): List<Pair<String, String>> {
    val n = COMMUNITY_COMMENT_NAMES.size
    val first = COMMUNITY_COMMENT_NAMES[(index + 1) % n]
    val second = COMMUNITY_COMMENT_NAMES[(index + 3) % n]
    val replied = COMMUNITY_COMMENT_NAMES[index % n]
    return listOf(
        "$first：" to "说得对！",
        "$second 回复 $replied：" to "同感 +1",
    )
}

/**
 * Flutter `VideoCardWidget`（`AspectRatio 16/9` + `ClipRRect r6` + 居中黑38 圆 + 白 ▶）的占位。
 *
 * **无视频 SDK**：封面用 [CommunityPalette.mediaVideoCover] 纯色块（ref 区域均值）。
 */
@Composable
private fun VideoCoverPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(CommunityPalette.mediaVideoCover, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        // Flutter `Colors.black38` 圆（padding 8 + icon 36 = 52dp）+ 白 `play_arrow`。
        // 三角形实测 15.6 × 20.2dp；`▶`（U+25B6）在 Android 可能走 emoji → 自绘。
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(0x61000000), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.width(16.dp).height(20.dp)) {
                drawPath(
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(0f, size.height)
                        lineTo(size.width, size.height / 2f)
                        close()
                    },
                    Color.White,
                )
            }
        }
    }
}

/** Flutter `ImageGridWidget` 的占位色块（`_Thumb` r4 / `_SingleImage` r6，`AspectRatio 1`）。 */
@Composable
private fun MediaTile(fill: Color, radiusDp: Float, modifier: Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(fill, RoundedCornerShape(radiusDp.dp)),
    )
}

/** Flutter `if (hasImages) [SizedBox(12), ImageGrid]` + `if (hasVideo) [SizedBox(12), Video]`。 */
@Composable
private fun PostMediaPlaceholder(index: Int) {
    when (placeholderMedia(index)) {
        CommunityMedia.Video -> {
            Spacer(Modifier.height(12.dp))
            VideoCoverPlaceholder()
        }
        CommunityMedia.TwoTiles -> {
            Spacer(Modifier.height(12.dp))
            // Flutter `_RowImages`：等宽 `Expanded AspectRatio(1)`，间隔 4。
            Row(modifier = Modifier.fillMaxWidth()) {
                MediaTile(
                    fill = CommunityPalette.mediaImageTile1,
                    radiusDp = 4f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(4.dp))
                MediaTile(
                    fill = CommunityPalette.mediaImageTile2,
                    radiusDp = 4f,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        CommunityMedia.SingleTile -> {
            Spacer(Modifier.height(12.dp))
            // Flutter `_SingleImage`：`maxWidth 62% / maxHeight 240`，源图方形。
            MediaTile(
                fill = CommunityPalette.mediaImageTile1,
                radiusDp = 6f,
                modifier = Modifier.fillMaxWidth(0.62f),
            )
        }
    }
}

/**
 * Flutter `CupertinoIcons.heart / heart_fill`（20 / `likeRed`）的 Canvas 近似。
 *
 * ponytail: Kuikly 无 icon font；`♥`（U+2665）/ `❤`（U+2764）在 Android 上会走 emoji
 * 呈现，与社区页灰阶 chrome 冲突（同 `💬` 的 P2-V2c 判定）—— 自绘两瓣 + 尖。
 */
@Composable
private fun HeartIcon(sizeDp: Float, tint: Color, filled: Boolean) {
    Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height
        val heart = Path().apply {
            moveTo(w * 0.50f, h * 0.95f)
            cubicTo(w * 0.06f, h * 0.62f, w * 0.00f, h * 0.30f, w * 0.26f, h * 0.16f)
            cubicTo(w * 0.40f, h * 0.09f, w * 0.50f, h * 0.20f, w * 0.50f, h * 0.30f)
            cubicTo(w * 0.50f, h * 0.20f, w * 0.60f, h * 0.09f, w * 0.74f, h * 0.16f)
            cubicTo(w * 1.00f, h * 0.30f, w * 0.94f, h * 0.62f, w * 0.50f, h * 0.95f)
            close()
        }
        if (filled) {
            drawPath(heart, tint)
        } else {
            drawPath(heart, tint, style = Stroke(width = h * 0.09f, join = StrokeJoin.Round))
        }
    }
}

/**
 * Flutter `CommentPreviewWidget`：`margin-top 8 / padding 10 / r4`，
 * 底色 `surfaceContainerHighest@.5`，每行 `昵称：`（`richLink` w600）+ 正文（onSurface，14 / 1.35）。
 */
@Composable
private fun CommentPreviewPlaceholder(index: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(CommunityPalette.commentFill, RoundedCornerShape(4.dp))
            .padding(10.dp),
    ) {
        placeholderComments(index).forEach { (prefix, body) ->
            Text(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = CommunityPalette.richLink,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    ) { append(prefix) }
                    append(body)
                },
                fontSize = 14.sp,
                // Flutter `height: 1.35` × 14 = 18.9。
                lineHeight = 18.9.sp,
                color = CommunityPalette.onSurface,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}
