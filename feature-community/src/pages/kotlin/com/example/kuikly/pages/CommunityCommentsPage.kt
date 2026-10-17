package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.community.Comment
import com.example.kuikly.data.community.CommunityStore
import com.example.kuikly.design.KuiklyTheme
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
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

private val COMMENT_CANDIDATES = listOf(
    "说得对！",
    "同感 +1",
    "已收藏，感谢分享",
    "求更多细节～",
)

@Page("CommunityComments")
internal class CommunityCommentsPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val postId = pageData.params.optString("id").ifBlank { "p1" }
        val top = statusBarInset()
        setContent {
            var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
            var draft by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            if (!booted) {
                booted = true
                CommunityStore.repo.comments(postId)
                    .onSuccess { comments = it }
                    .onFailure { error = it.message ?: "加载失败" }
            }

            fun send() {
                if (draft.isBlank()) {
                    Utils.currentBridgeModule().toast("评论内容不能为空")
                    return
                }
                CommunityStore.repo.addComment(postId, draft)
                    .onSuccess {
                        comments = it
                        draft = ""
                        Utils.currentBridgeModule().toast("评论成功")
                    }
                    .onFailure { error = it.message ?: "评论失败" }
            }

            Column(
                modifier = Modifier.fillMaxSize()
                    .background(KuiklyTheme.canvasSoft2),
            ) {
                // 头部 chrome（与 PostDetail 一致：‹ 返回 + 标题）。
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(start = 4.dp, end = 4.dp, top = top.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹",
                        fontSize = 24.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier
                            .clickable { Utils.currentBridgeModule().closePage() }
                            .padding(12.dp),
                    )
                    Text(
                        "评论 ${comments.size}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KuiklyTheme.foreground,
                    )
                }

                if (error != null && comments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("加载失败", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(error ?: "", fontSize = 12.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                                    .clickable {
                                        CommunityStore.repo.comments(postId)
                                            .onSuccess { comments = it; error = null }
                                            .onFailure { error = it.message ?: "加载失败" }
                                    }
                                    .padding(horizontal = 22.dp, vertical = 9.dp),
                            ) {
                                Text("重试", fontSize = 13.sp, color = KuiklyTheme.onPrimary)
                            }
                        }
                    }
                } else if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("暂无评论，快来抢沙发", fontSize = 14.sp, color = KuiklyTheme.muted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp),
                    ) {
                        items(comments) { c -> CommentRow(comment = c) }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }

                // 输入区（mock 无 TextField，用候选内容快捷切换代替输入；对齐源 comment_bottom_sheet）。
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp)
                            .background(KuiklyTheme.canvasSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            if (draft.isBlank()) "点击下面的候选内容，切换要发表的评论" else draft,
                            fontSize = 14.sp,
                            color = if (draft.isBlank()) KuiklyTheme.muted else KuiklyTheme.ink,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        COMMENT_CANDIDATES.forEach { candidate ->
                            val selected = draft == candidate
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selected) KuiklyTheme.linkSoft else KuiklyTheme.canvasSoft2,
                                        RoundedCornerShape(KuiklyTheme.radiusPill),
                                    )
                                    .clickable { draft = if (selected) "" else candidate }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    candidate,
                                    fontSize = 12.sp,
                                    color = if (selected) KuiklyTheme.linkDeep else KuiklyTheme.body,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                if (draft.isBlank()) KuiklyTheme.hairline else KuiklyTheme.primary,
                                RoundedCornerShape(KuiklyTheme.radiusMd),
                            )
                            .clickable { send() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("发送", fontSize = 15.sp, color = KuiklyTheme.onPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(36.dp)
                .background(KuiklyTheme.linkSoft, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(comment.nickname.take(1), fontSize = 14.sp, color = KuiklyTheme.linkDeep, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.nickname, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Text(comment.createTime, fontSize = 11.sp, color = KuiklyTheme.muted)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (comment.replyToNickname != null) "回复 ${comment.replyToNickname}：${comment.content}" else comment.content,
                fontSize = 14.sp,
                color = KuiklyTheme.ink,
            )
        }
    }
}
