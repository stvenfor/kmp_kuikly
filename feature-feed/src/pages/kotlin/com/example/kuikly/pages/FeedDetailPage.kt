package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.feed.FeedComment
import com.example.kuikly.data.feed.FeedItem
import com.example.kuikly.data.feed.FeedStore
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
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

private val DET_BG = Color(0xFFF2F2F7)
private val DET_SURFACE = Color(0xFFFFFFFF)
private val DET_FG = Color(0xFF1C1C1E)
private val DET_MUTED = Color(0x993C3C43)
private val DET_HAIRLINE = Color(0xFFE5E5EA)
private val DET_ACCENT = Color(0xFF1565C0)
private val DET_LIKE = Color(0xFFFA3E3E)

private val COMMENT_CANDIDATES = listOf(
    "说得好！",
    "同感 +1",
    "已收藏，谢谢分享",
    "求更多细节～",
)

@Page("FeedDetail")
internal class FeedDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        setContent {
            var item by remember { mutableStateOf<FeedItem?>(null) }
            var comments by remember { mutableStateOf<List<FeedComment>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var draft by remember { mutableStateOf("") }
            var booted by remember { mutableStateOf(false) }

            fun load() {
                FeedStore.repo.detail(id)
                    .onSuccess { found ->
                        item = found
                        FeedStore.repo.fetchComments(id).onSuccess { comments = it }
                    }
                    .onFailure { error = it.message ?: "加载失败" }
            }

            if (!booted) {
                booted = true
                load()
            }

            Column(modifier = Modifier.fillMaxSize().background(DET_BG)) {
                // 顶栏
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹",
                        fontSize = 22.sp, color = DET_FG,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("动态详情", fontSize = 17.sp, color = DET_FG, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "复制",
                        fontSize = 13.sp, color = DET_ACCENT,
                        modifier = Modifier.clickable {
                            item?.let { Utils.currentBridgeModule().toast("已复制文案") }
                        }.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(DET_HAIRLINE))

                val current = item
                if (error != null && current == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(error ?: "加载失败", color = DET_MUTED, fontSize = 15.sp)
                    }
                } else if (current != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(DET_SURFACE, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.width(40.dp).height(40.dp)
                                    .background(DET_HAIRLINE, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(current.author.take(1), fontSize = 18.sp, color = DET_ACCENT, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(current.author, fontSize = 16.sp, color = DET_FG, fontWeight = FontWeight.Medium)
                                Text("${current.timeLabel} · ${current.source}", fontSize = 11.sp, color = DET_MUTED)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(current.content, fontSize = 16.sp, color = DET_FG)
                        if (current.topic != null || current.mentions.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (current.topic != null) Text("#${current.topic}", fontSize = 13.sp, color = DET_ACCENT)
                                current.mentions.forEach { m ->
                                    Spacer(Modifier.width(8.dp))
                                    Text("@$m", fontSize = 13.sp, color = DET_ACCENT)
                                }
                            }
                        }
                        if (current.hasVideo) {
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                                    .background(DET_HAIRLINE, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) { Text("▶ 视频", fontSize = 14.sp, color = DET_MUTED) }
                        } else if (current.hasImages) {
                            Spacer(Modifier.height(12.dp))
                            Column {
                                current.images.forEach { _ ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(160.dp)
                                            .background(DET_HAIRLINE, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) { Text("图", fontSize = 12.sp, color = DET_MUTED) }
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DetailAction("赞 ${current.likeCount}", current.isLiked, DET_LIKE) {
                                FeedStore.repo.toggleLike(current.id).onSuccess { item = it }
                            }
                            Spacer(Modifier.width(20.dp))
                            DetailAction("收藏 ${current.favoriteCount}", current.isFavorited, DET_ACCENT) {
                                FeedStore.repo.toggleFavorite(current.id).onSuccess { item = it }
                            }
                            Spacer(Modifier.width(20.dp))
                            DetailAction("分享 ${current.shareCount}", false, DET_MUTED) {
                                FeedStore.repo.incrementShare(current.id).onSuccess { item = it }
                                Utils.currentBridgeModule().toast("已分享")
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("评论 ${comments.size}", fontSize = 14.sp, color = DET_FG, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp))
                    Spacer(Modifier.height(6.dp))
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        comments.forEach { c ->
                            val prefix = if (c.replyToNickname != null) "回复 @${c.replyToNickname}：" else ""
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .background(DET_SURFACE, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                            ) {
                                Text(c.nickname, fontSize = 13.sp, color = DET_FG, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text("$prefix${c.content}", fontSize = 13.sp, color = DET_MUTED)
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        if (comments.isEmpty()) {
                            Text("还没有评论，来说两句吧", fontSize = 13.sp, color = DET_MUTED,
                                modifier = Modifier.padding(12.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    // 评论输入（无 TextField：候选词追加）
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                            .background(DET_SURFACE, RoundedCornerShape(10.dp)).padding(12.dp),
                    ) {
                        Text("写评论", fontSize = 12.sp, color = DET_MUTED)
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                                .background(DET_BG, RoundedCornerShape(8.dp)).padding(10.dp),
                        ) {
                            Text(
                                if (draft.isBlank()) "点击下方候选内容切换评论" else draft,
                                fontSize = 14.sp,
                                color = if (draft.isBlank()) DET_MUTED else DET_FG,
                                maxLines = 2,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        COMMENT_CANDIDATES.forEach { cand ->
                            val sel = draft == cand
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    .background(if (sel) Color(0x1F1565C0) else DET_BG, RoundedCornerShape(6.dp))
                                    .clickable { draft = if (sel) "" else cand }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Text(cand, fontSize = 13.sp,
                                    color = if (sel) DET_ACCENT else DET_FG)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(if (draft.isBlank()) DET_HAIRLINE else DET_ACCENT, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (draft.isBlank()) {
                                        Utils.currentBridgeModule().toast("评论内容不能为空")
                                        return@clickable
                                    }
                                    FeedStore.repo.addComment(current.id, draft).onSuccess {
                                        item = it
                                        comments = it.comments
                                        draft = ""
                                        Utils.currentBridgeModule().toast("评论成功")
                                    }.onFailure {
                                        Utils.currentBridgeModule().toast("评论失败")
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("发送评论", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailAction(label: String, active: Boolean, activeColor: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = if (active) activeColor else DET_MUTED, fontWeight = FontWeight.Medium)
    }
}
