package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.community.CommunityStore
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * Community post detail — Phase-2 / P2-W2b restyle。
 *
 * Flutter Community 模块**没有**帖子详情页（帖子卡走评论 sheet），本页为 Kuikly 侧
 * Phase-1 既有路由，仅做视觉加厚：令牌/密度对齐 `CommunityTheme`
 * （`community_theme.dart`，裸 dp/sp，同 `CommunityPalette` KDoc）。
 * Publish lands in a later slice — button only toasts.
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
                            Text(
                                post.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = CommunityPalette.labelPrimary,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                post.body,
                                fontSize = 15.sp,
                                color = CommunityPalette.labelPrimary,
                            )
                            // LikeBarWidget 形制的互动行（20 图标 + 13 文案，间隔 24）。
                            Spacer(Modifier.height(16.dp))
                            Row {
                                PostDetailAction("♡", if (post.likeCount > 0) post.likeCount.toString() else "赞")
                                Spacer(Modifier.width(24.dp))
                                PostDetailAction("💬", if (post.commentCount > 0) post.commentCount.toString() else "评论")
                                Spacer(Modifier.width(24.dp))
                                PostDetailAction("↗", "分享")
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
private fun PostDetailAction(glyph: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(glyph, fontSize = 20.sp, color = CommunityPalette.labelSecondary)
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = CommunityPalette.labelSecondary)
    }
}
