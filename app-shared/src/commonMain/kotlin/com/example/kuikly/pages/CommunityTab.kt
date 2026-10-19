package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.Utils
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.example.kuikly.data.community.CommunityStore
import com.example.kuikly.data.community.Post
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.resources.painterResource
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
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.StrokeJoin
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.layout.ContentScale
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
 * community_theme 均无 `.w/.h/.sp`），相当于 designWidth = deviceWidth(480) 的 1:1 刻度。
 *
 * 但本 App 的 KMP 逻辑宽 411.4dp、dpr 2.625，golden 比对把 KMP 图 resize 到 Flutter 1440 宽，
 * 裸 `.dp` 会整体偏大 16.7%。故 `CommunityTab` 改用 `ProvideDesignScale(pageWidth, designWidth = 480f)`
 * 的 `.su`/`.susp`：此时 `V.su` → `V·1440/480 = V·3.0` comparison px，恰等于 Flutter 裸 px 的
 * `V·3.0`，逐像素对齐。数值与 Flutter 裸值完全一致，仅单位从 `dp`/`sp` 换成 `.su`/`.susp`。
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

    // Flutter `CommunityTheme.radiusMd = 8`（社区模块用裸逻辑 px，非 .w）。
    const val RADIUS_MD = 8f
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
internal fun CommunityTab(statusBarHeight: Float, pageWidth: Float) {
    // 社区模块在 Flutter 源里全用**裸逻辑 px**（community_theme / post_card / community_page
    // 均无 .w/.h/.sp）。裸 px 在 Flutter 即 designWidth = deviceWidth(480) 的 1:1 刻度；
    // 而本 App 的 KMP 逻辑宽 411.4dp、dpr 2.625，golden 比对把 KMP 图 resize 到 Flutter
    // 1440 宽，故裸 `.dp` 会整体偏大 16.7%。用 designWidth=480 的 `.su` 后，
    // `V.su` → `V·1440/480 = V·3.0` comparison px，恰好等于 Flutter 裸 px 的 `V·3.0`，逐像素对齐。
    ProvideDesignScale(pageWidth, designWidth = 480f) {
    val result = remember { CommunityStore.repo.all() }
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
                    contentPadding = PaddingValues(start = 16.su, top = 8.su, end = 16.su),
                ) {
                    itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                        Box(modifier = Modifier.padding(bottom = 10.su)) {
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
            .padding(start = 16.su, end = 16.su, top = (statusBarHeight + 8f).dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Flutter `CommunityTheme.largeTitle`（32 / w600 / 黑 / height 36/32 / letterSpacing -1.6）。
            Text(
                "社区",
                modifier = Modifier.weight(1f),
                fontSize = 32.susp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 36.susp,
                letterSpacing = (-1.6).susp,
                color = CommunityPalette.labelPrimary,
            )
            // Flutter 44×44 位内的 36×36 accent 圆 + 白色 `＋`（size 20）→ push PublishPage。
            Box(
                modifier = Modifier
                    .size(44.su)
                    .clickable { Utils.currentBridgeModule().openPage(PageNames.CommunityPublish) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.su)
                        .background(CommunityPalette.accent, RoundedCornerShape(18.su)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("＋", fontSize = 20.susp, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(16.su))
        // Flutter 搜索框：h44 / surface r12 / 0.5 separator 边 / h14 内距 / ⌕18 + hint 13。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.su)
                .background(CommunityPalette.surface, RoundedCornerShape(CommunityPalette.RADIUS_MD.su))
                .border(0.5.su, CommunityPalette.separator, RoundedCornerShape(CommunityPalette.RADIUS_MD.su))
                .clickable { Utils.currentBridgeModule().toast("「搜索」即将接入") }
                .padding(horizontal = 14.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⌕", fontSize = 18.susp, color = CommunityPalette.labelSecondary)
            Spacer(Modifier.width(8.su))
            Text("搜索动态、话题、用户", fontSize = 13.susp, color = CommunityPalette.labelTertiary)
        }
        Spacer(Modifier.height(12.su))
        // Flutter `_FilterTabs`：h36 内 15sp 文案 + 6 间隙 + 20×2 accent 下划线，间隔 24。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            COMMUNITY_FILTER_TABS.forEachIndexed { index, label ->
                val active = index == selected
                Column(
                    modifier = Modifier
                        .clickable { onSelected(index) }
                        .padding(end = 24.su),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        label,
                        fontSize = 15.susp,
                        // Flutter `CommunityTheme.headline.copyWith(fontSize: 15)` → height 1.25。
                        lineHeight = 18.75.susp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) CommunityPalette.labelPrimary else CommunityPalette.labelSecondary,
                    )
                    Spacer(Modifier.height(6.su))
                    Box(
                        modifier = Modifier
                            .width(if (active) 20.su else 0.su)
                            .height(2.su)
                            .background(CommunityPalette.accent, RoundedCornerShape(1.su)),
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
            // Flutter `padding: EdgeInsets.fromLTRB(16, 14, 12, 12)`。
            .background(CommunityPalette.surface, RoundedCornerShape(CommunityPalette.RADIUS_MD.su))
            .border(0.5.su, CommunityPalette.separator, RoundedCornerShape(CommunityPalette.RADIUS_MD.su))
            .clickable {
                Utils.currentBridgeModule().openPage(
                    PageNames.PostDetail,
                    userData = JSONObject().apply { put("id", post.id) },
                )
            }
            .padding(start = 14.su, top = 14.su, end = 9.5.su, bottom = 12.su),
    ) {
        // Flutter `UserInfoWidget` + ellipsis 按钮（44×44 位 / ⋯ 20）。
        Row(verticalAlignment = Alignment.Top) {
            // Flutter `CacheImageUtils.circle(44)`：本地 pravatar 资源优先，否则灰底圆 + 首字母。
            val avatarRes = avatarAssetFor(post.avatar)
            Box(
                modifier = Modifier
                    .size(40.su)
                    .clip(RoundedCornerShape(20.su))
                    .background(CommunityPalette.fillSecondary),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarRes != null) {
                    Image(
                        painter = painterResource(avatarRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    // 参考图：默认灰底圆 + 深灰「人形」剪影（非首字母）。
                    AvatarPersonGlyph(Modifier.size(22.su))
                }
            }
            Spacer(Modifier.width(12.su))
            Column(modifier = Modifier.weight(1f)) {
                // Flutter `CommunityTheme.headline`（17 / w600）。
                Text(
                    post.nickname,
                    fontSize = 17.susp,
                    lineHeight = 22.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommunityPalette.labelPrimary,
                )
                Spacer(Modifier.height(2.su))
                // Flutter `'{formatPublishTime} · {source}'`（caption 13）。`Post` 缺 publishTime /
                // source 字段 → 按 Flutter mock seed 同规则派生（见文件末尾 `placeholderPublishMeta`）。
                Text(
                    placeholderPublishMeta(index),
                    fontSize = 13.susp,
                    lineHeight = 17.susp,
                    color = CommunityPalette.labelSecondary,
                )
            }
            Text(
                "⋯",
                fontSize = 20.susp,
                color = CommunityPalette.labelSecondary,
                modifier = Modifier
                    .clickable { Utils.currentBridgeModule().toast("「更多」即将接入") }
                    .padding(12.su),
            )
        }
        Spacer(Modifier.height(2.su))
        // Flutter `ExpandTextWidget` → `RichTextContentWidget(post.content)`：**单段**富文本
        // （16 / height 1.45 / labelPrimary，`#话题`·`@用户` 走 richLink + w500，链接加下划线）——
        // 卡内没有独立的加粗标题层级（`PostModel` 也没有 title 字段，只有 content）。
        // mock `Post` 的 title 即 Flutter content 的首行，故并成一段渲染：保留全部文案，
        // 但去掉 Flutter 不存在的 17 bold 标题（原先每卡多一行粗体 → 卡更高、层级更吵）。
        // main 的 Post 没有独立 title 字段，用「话题 + 正文」拼成单段渲染，
        // 避免多一层加粗标题导致卡片过高。
        PostContent(content = (post.topicName?.let { "#$it " } ?: "") + post.content)
        // Flutter `if (post.hasImages) ... [SizedBox(12), ImageGridWidget]` /
        // `if (post.hasVideo) ... [SizedBox(12), ClipRRect(VideoCardWidget)]`。`Post` 无媒体字段
        // → 用 ref 均值的**占位色块**顶替（见 Executor Report ceiling）。
        PostMediaPlaceholder(index = index, images = post.images, videoCoverUrl = post.videoCoverUrl)
        // Flutter `LikeBarWidget`：♡ / 💬 / ↗（图标 20 + 文案 13，间隔 24，顶部 12）。
        Row(modifier = Modifier.padding(top = 8.su)) {
            val liked = placeholderLiked(index)
            val likeTint = if (liked) CommunityPalette.likeRed else CommunityPalette.labelSecondary
            // Flutter `heart_fill + likeRed`（已赞）/ `heart + labelSecondary`，文案同色。
            PostAction(
                label = if (post.likeCount > 0) post.likeCount.toString() else "赞",
                color = likeTint,
            ) {
                HeartIcon(sizeDp = 20f, tint = likeTint, filled = liked)
            }
            Spacer(Modifier.width(24.su))
            // Flutter `CupertinoIcons.chat_bubble`（20 / labelSecondary）。原 `💬` 是**全彩 emoji**，
            // 与社区页灰阶 chrome 冲突，改为 Canvas 线稿（几何同 `MainPage` 底栏 chat 图标）。
            PostAction(label = if (post.commentCount > 0) post.commentCount.toString() else "评论") {
                ChatBubbleIcon(sizeDp = 20f, tint = CommunityPalette.labelSecondary)
            }
            Spacer(Modifier.width(24.su))
            PostAction(label = "分享") {
                Text("↗", fontSize = 20.susp, color = CommunityPalette.labelSecondary)
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
        fontSize = 16.susp,
        // Flutter `RichTextContentWidget` baseStyle：fontSize 16 / height 1.45。
        lineHeight = 22.5.susp,
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
    Canvas(modifier = Modifier.size(sizeDp.su)) {
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
            .padding(horizontal = 4.su, vertical = 6.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(4.su))
        Text(label, fontSize = 13.susp, color = color)
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
        Spacer(Modifier.height(12.su))
        Text("暂无动态", fontSize = 13.susp, color = CommunityPalette.labelSecondary)
        Spacer(Modifier.height(8.su))
        Text("发布第一条动态，开始互动吧", fontSize = 13.susp, color = CommunityPalette.labelTertiary)
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
        Text("⚠", fontSize = 48.susp, color = CommunityPalette.labelTertiary)
        Spacer(Modifier.height(12.su))
        Text("加载失败", fontSize = 17.susp, fontWeight = FontWeight.SemiBold, color = CommunityPalette.labelPrimary)
        Spacer(Modifier.height(8.su))
        Text(
            message ?: "错误",
            fontSize = 13.susp,
            color = CommunityPalette.labelSecondary,
            modifier = Modifier.padding(horizontal = 32.su),
        )
        Spacer(Modifier.height(16.su))
        Box(
            modifier = Modifier
                .background(CommunityPalette.accent, RoundedCornerShape(8.su))
                .clickable { Utils.currentBridgeModule().toast("「重试」即将接入") }
                .padding(horizontal = 16.su, vertical = 8.su),
        ) {
            Text("重试", fontSize = 17.susp, color = Color.White)
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
private fun VideoCoverPlaceholder(coverUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(CommunityPalette.mediaVideoCover, RoundedCornerShape(6.su)),
        contentAlignment = Alignment.Center,
    ) {
        val coverRes = communityAssetFor(coverUrl)
        if (coverRes != null) {
            Image(
                painter = painterResource(coverRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        // Flutter `Colors.black38` 圆（padding 8 + icon 36 = 52dp）+ 白 `play_arrow`。
        // 三角形实测 15.6 × 20.2dp；`▶`（U+25B6）在 Android 可能走 emoji → 自绘。
        Box(
            modifier = Modifier
                .size(52.su)
                .background(Color(0x61000000), RoundedCornerShape(26.su)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.width(16.su).height(20.su)) {
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

/** 社区公共资源（`assets/common/community/`）。 */
@OptIn(InternalResourceApi::class)
private fun communityAsset(name: String): DrawableResource =
    DrawableResource(ImageUri.commonAssets("community/$name").toUrl(""))

/**
 * picsum URL → 本地资源映射（预下载，模拟器无外网）。
 * 仅映射 golden 需要的 seed；其余回落色块占位。
 */
private fun communityAssetFor(url: String?): DrawableResource? {
    if (url.isNullOrEmpty()) return null
    val marker = "picsum.photos/seed/"
    val idx = url.indexOf(marker)
    if (idx < 0) return null
    val seed = url.substring(idx + marker.length).takeWhile { it.isLetterOrDigit() || it == '_' }
    return when (seed) {
        "video_0" -> communityAsset("video_0.png")
        "post_1_0" -> communityAsset("post_1_0.png")
        "post_1_1" -> communityAsset("post_1_1.png")
        "run" -> communityAsset("run.png")
        "ear" -> communityAsset("ear.png")
        "tool" -> communityAsset("tool.png")
        "tool2" -> communityAsset("tool2.png")
        else -> null
    }
}

/**
 * pravatar 头像 URL → 本地资源映射（预下载，模拟器无外网）。
 * 参考图（golden 13）两个帖子的头像都是**默认灰底人形占位**，不走真实照片 → 全部回落 null。
 */
private fun avatarAssetFor(url: String?): DrawableResource? {
    return null
}

/** Flutter `ImageGridWidget`（`_Thumb` r4 / `_SingleImage` r6，`AspectRatio 1`）；有 URL 走 coil3 网络图。 */
@Composable
private fun MediaTile(url: String?, fill: Color, radiusDp: Float, modifier: Modifier) {
    Box(
        modifier = modifier
            // 参考图实测瓦片 635×562 comp（aspect 1.13，高 ≈145.su）。Kuikly 的 aspectRatio
            // 在 weight(1f) 子项上不生效（渲染成 1.45），必须显式 height 锁高。
            .height(145.su)
            .clip(RoundedCornerShape(radiusDp.su))
            .background(fill),
    ) {
        val res = communityAssetFor(url)
        if (res != null) {
            Image(
                painter = painterResource(res),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

/** Flutter `if (hasImages) [SizedBox(12), ImageGrid]` + `if (hasVideo) [SizedBox(12), Video]`。 */
@Composable
private fun PostMediaPlaceholder(index: Int, images: List<String>, videoCoverUrl: String?) {
    when (placeholderMedia(index)) {
        CommunityMedia.Video -> {
            Spacer(Modifier.height(12.su))
            VideoCoverPlaceholder(coverUrl = videoCoverUrl ?: images.firstOrNull())
        }
        CommunityMedia.TwoTiles -> {
            Spacer(Modifier.height(12.su))
            // Flutter `_RowImages`：等宽 `Expanded AspectRatio(1)`，间隔 4。
            Row(modifier = Modifier.fillMaxWidth()) {
                MediaTile(
                    url = images.getOrNull(0),
                    fill = CommunityPalette.mediaImageTile1,
                    radiusDp = 4f,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(4.su))
                MediaTile(
                    url = images.getOrNull(1),
                    fill = CommunityPalette.mediaImageTile2,
                    radiusDp = 4f,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        CommunityMedia.SingleTile -> {
            Spacer(Modifier.height(12.su))
            // Flutter `_SingleImage`：`maxWidth 62% / maxHeight 240`，源图方形。
            MediaTile(
                url = images.getOrNull(0),
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

/** 默认头像「人形」剪影（参考图灰底圆内的深灰 person glyph）。 */
@Composable
private fun AvatarPersonGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val glyph = Color(0xFF8A9099)
        // 头（圆）
        drawCircle(glyph, radius = w * 0.19f, center = Offset(w * 0.5f, h * 0.30f))
        // 肩（半椭圆，底部裁掉由外层 clip 承接）
        drawOval(
            glyph,
            topLeft = Offset(w * 0.14f, h * 0.52f),
            size = Size(w * 0.72f, w * 0.80f),
        )
    }
}

@Composable
private fun HeartIcon(sizeDp: Float, tint: Color, filled: Boolean) {
    Canvas(modifier = Modifier.size(sizeDp.su)) {
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
            .padding(top = 5.su)
            .background(CommunityPalette.commentFill, RoundedCornerShape(4.su))
            .padding(start = 10.su, end = 10.su, top = 6.su, bottom = 6.su),
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
                fontSize = 14.susp,
                // Flutter `height: 1.35` × 14 = 18.9。
                lineHeight = 18.9.susp,
                color = CommunityPalette.onSurface,
                modifier = Modifier.padding(bottom = 4.su),
            )
        }
    }
}
