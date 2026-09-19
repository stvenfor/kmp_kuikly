package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.community.CommunityStore
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.RowScope
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.geometry.Rect
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
import com.tencent.kuikly.compose.ui.text.withStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.attr.ImageUri

/**
 * 帖子详情 — Phase-2 / P2-V4d 视觉加厚。
 *
 * Flutter Community 模块**没有**帖子详情页（帖子卡即详情，走评论 sheet），故真源 =
 * `features/community/lib/community/widgets/post_card_widget.dart` →
 * `expand_text_widget.dart` → `rich_text_content_widget.dart`：`post.content` **单段**
 * 富文本（16 / height 1.45 / onSurface），`@用户` / `#话题` 共用 `#576B95` w500，链接同色加下划线。
 *
 * 同库惯例（`CommunityTab.kt` V2c PASS）：把 Kuikly `Post.title + "\n" + Post.body` 合并渲染
 * ——Flutter `PostModel` 无 title 字段，单独 22 sp bold 标题为 Kuikly 杜撰，比卡内多了 1 行粗体 +
 * 卡更高 + 层级更吵。本次**对齐 Flutter 卡**：去掉杜撰的 22 bold 标题，单段富文本；并替换
 * `💬` 全彩 emoji 为 `ChatBubbleIcon` Canvas 线稿（`MainPage` / `CommunityTab` 同款几何）。
 *
 * Publish lands in a later slice — bottom button only toasts.
 */
@Page(name = "PostDetail", moduleId = "feature_home")
internal class PostDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "1" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val result = remember(id) { CommunityStore.repo.detail(id) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CommunityPalette.background)
                    .padding(bottom = (bottom + 16f).dp),
            ) {
                // 头部：白底 chrome（同 ChatDetailHeader 形制：‹ accent + 标题）。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CommunityPalette.surface)
                        .padding(start = 4.dp, end = 4.dp, top = top.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹",
                        fontSize = 24.sp,
                        color = CommunityPalette.accent,
                        modifier = Modifier
                            .clickable { Utils.currentBridgeModule().closePage() }
                            .padding(12.dp),
                    )
                    Text(
                        "帖子详情",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CommunityPalette.labelPrimary,
                    )
                }
                result.onSuccess { post ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        // 白卡（groupedCard：surface r12 + 0.5 边），密度同 PostCard。
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CommunityPalette.surface, RoundedCornerShape(CommunityPalette.RADIUS_MD.dp))
                                .border(0.5.dp, CommunityPalette.separator, RoundedCornerShape(CommunityPalette.RADIUS_MD.dp))
                                .padding(16.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // CacheImageUtils.circle(44) 未加载占位。
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
                                Column {
                                    Text(
                                        post.author,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CommunityPalette.labelPrimary,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "点赞 ${post.likeCount} · 评论 ${post.commentCount}",
                                        fontSize = 13.sp,
                                        color = CommunityPalette.labelSecondary,
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Flutter `ExpandTextWidget` → `RichTextContentWidget(post.content)`：**单段**富文本
                            // （16 / height 1.45 / labelPrimary，`#话题`·`@用户` 走 richLink + w500，
                            // 链接加下划线）——详情页没有独立的加粗标题层级（同 `CommunityTab` V2c 结论）。
                            PostContent(content = post.title + "\n" + post.body)
                            // Flutter `post.hasImages ? ImageGridWidget` / `post.hasVideo ? VideoCardWidget`：
                            // `Post` 缺 images / videoUrl，按 id seed 展示 1–2 张 assets 照片。
                            PostDetailMedia(postId = post.id)
                            // LikeBarWidget 形制的互动行（20 图标 + 13 文案，间隔 24）。
                            Spacer(Modifier.height(16.dp))
                            Row {
                                PostDetailAction(label = if (post.likeCount > 0) post.likeCount.toString() else "赞") {
                                    Text("♡", fontSize = 20.sp, color = CommunityPalette.labelSecondary)
                                }
                                Spacer(Modifier.width(24.dp))
                                // Flutter `CupertinoIcons.chat_bubble`（20 / labelSecondary）。原 `💬` 是
                                // **全彩 emoji**，与灰阶 chrome 冲突；改为 Canvas 线稿（几何同
                                // `MainPage` 底栏 / `CommunityTab` `ChatBubbleIcon`）。
                                PostDetailAction(label = if (post.commentCount > 0) post.commentCount.toString() else "评论") {
                                    ChatBubbleIcon(sizeDp = 20f, tint = CommunityPalette.labelSecondary)
                                }
                                Spacer(Modifier.width(24.dp))
                                PostDetailAction(label = "分享") {
                                    Text("↗", fontSize = 20.sp, color = CommunityPalette.labelSecondary)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        // 发布 CTA：accent 填充整宽圆角钮（Cupertino filled 惯例）。
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CommunityPalette.accent, RoundedCornerShape(CommunityPalette.RADIUS_MD.dp))
                                .clickable { Utils.currentBridgeModule().toast("发布功能即将接入") }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("发布", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }.onFailure {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("⚠", fontSize = 48.sp, color = CommunityPalette.labelTertiary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            it.message ?: "加载失败",
                            fontSize = 13.sp,
                            color = CommunityPalette.labelSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostDetailAction(label: String, icon: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = CommunityPalette.labelSecondary)
    }
}

/**
 * Flutter `RichTextParser._pattern` 的本地镜像（与 `CommunityTab.COMMUNITY_RICH_TOKEN` 同源）。
 * 详情页**不依赖** `CommunityTab.kt` 的 `private` 符号，故就地声明。
 *
 * `@用户` / `#话题` / `http(s)://链接` —— mention / hashtag 同色（`#576B95`）+ w500，链接同色加下划线
 * （Flutter 的 tap-toast / 跳转行为未复刻：本 ticket 只做视觉）。
 */
private val POST_DETAIL_RICH_TOKEN = Regex("([@#][一-龥A-Za-z0-9_]+|https?://\\S+)")

/**
 * Flutter `RichTextContentWidget` 的本地镜像（同 `CommunityTab.PostContent`）。
 * Kuikly `Post` 没有 `content` 字段 → 以 `title + "\n" + body` 拼接传入（V2c 已采纳的取舍）。
 */
@Composable
private fun PostContent(content: String) {
    val annotated = remember(content) {
        buildAnnotatedString {
            var cursor = 0
            POST_DETAIL_RICH_TOKEN.findAll(content).forEach { match ->
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

// ────────────────────── P3-E2a / P3-E9a 媒体 assets ──────────────────────

/**
 * P3-E2a：PostDetail 照片密度补齐。
 *
 * 资源随 `app-shared/src/commonMain/assets/common/post_detail/` 打包
 * （Android `assets.srcDirs` 与 iOS CocoaPods `resources` 已在 app-shared 配置），
 * 故走 `ImageUri.commonAssets`。
 */
@OptIn(InternalResourceApi::class)
private fun postDetailAsset(name: String): DrawableResource =
    DrawableResource(ImageUri.commonAssets("post_detail/$name").toUrl(""))

private val POST_DETAIL_MEDIA: List<DrawableResource> by lazy(LazyThreadSafetyMode.NONE) {
    (1..8).map { postDetailAsset("media_$it.png") }
}

/**
 * 图片张数派生（P3-E9a）。Flutter `image_grid_widget.dart` 只有三种密度分支：
 * 2–3 张 → `_RowImages`（等宽一行）、4 张 → `_GridImages(crossAxisCount: 2)`、
 * 5–9 张 → `_GridImages(crossAxisCount: 3)`；单张 → `_SingleImage`。
 *
 * Kuikly `Post` 没有 `images` 字段（`FakeCommunityRepository` 只 seed 3 条），故按 `postId`
 * 派生代表密度 8 / 4 / 3 —— 三种分支各覆盖一次，且详情页取最密的 3 列网格
 * （Flutter mock 的图片数是 `(i % 9) + 1`，1–9 张轮转，故 8 张仍在同一分支内）。
 */
private val POST_DETAIL_MEDIA_COUNT = intArrayOf(8, 4, 3)

/**
 * Flutter `post.hasImages ? ImageGridWidget : (post.hasVideo ? VideoCardWidget : none)` 的 assets 近似。
 */
@Composable
private fun PostDetailMedia(postId: String) {
    val seed = postId.toIntOrNull() ?: 1
    val count = POST_DETAIL_MEDIA_COUNT[(seed - 1).coerceAtLeast(0) % POST_DETAIL_MEDIA_COUNT.size]
    val images = POST_DETAIL_MEDIA.take(count)
    when {
        count == 1 -> PostDetailSingleImage(res = images.first())
        count <= 3 -> PostDetailImageRow(images)
        count == 4 -> PostDetailImageGrid(images, columns = 2)
        else -> PostDetailImageGrid(images, columns = 3)
    }
}

/** Flutter `_SingleImage`：`maxWidth 62% / r6`，`BoxFit.cover` 近似。 */
@Composable
private fun PostDetailSingleImage(res: DrawableResource) {
    Spacer(Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth(0.62f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

/** Flutter `_RowImages`：等宽 `Expanded AspectRatio(1)`，间隔 4，r4（2–3 张）。 */
@Composable
private fun PostDetailImageRow(images: List<DrawableResource>) {
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        images.forEachIndexed { index, res ->
            if (index > 0) Spacer(Modifier.width(4.dp))
            PostDetailThumb(res = res, radius = 4)
        }
    }
}

/**
 * Flutter `_GridImages(crossAxisCount)`（`GridView.builder` + `NeverScrollableScrollPhysics`，
 * `crossAxisSpacing` / `mainAxisSpacing` 均 4，r4）：4 张走 2 列，≥5 张走 3 列。
 *
 * Kuikly 侧用 `Column` + `Row` 分块手排（不引入 LazyVerticalGrid，页面本身不可滚动）。
 */
@Composable
private fun PostDetailImageGrid(images: List<DrawableResource>, columns: Int) {
    Spacer(Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        images.chunked(columns).forEach { rowImages ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowImages.forEachIndexed { index, res ->
                    if (index > 0) Spacer(Modifier.width(4.dp))
                    PostDetailThumb(res = res, radius = 4)
                }
                // Flutter GridView 的末行不足列数时保留空位；此处补等宽占位，避免最后一格被拉宽。
                repeat(columns - rowImages.size) {
                    Spacer(Modifier.width(4.dp))
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/** 单格缩略图：`Expanded AspectRatio(1)` + `BoxFit.cover`（Flutter `_ImageThumb` 形制）。 */
@Composable
private fun RowScope.PostDetailThumb(res: DrawableResource, radius: Int) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(radius.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

/**
 * Flutter `CupertinoIcons.chat_bubble` 的 Canvas 线稿近似（与 `MainPage` 底栏 / `CommunityTab`
 * `ChatBubbleIcon` 同几何）。
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
