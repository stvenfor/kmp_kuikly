package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.video.ShortVideo
import com.example.kuikly.data.video.ShortVideoStatus
import com.example.kuikly.data.video.VideoProfile
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.RowScope
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
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 我的视频资料页 — Flutter `ShortVideoPage` 顶部资料卡 + 我的视频管理 业务对齐。
 * 真源：`features/video/lib/short_video/view/short_video_page.dart`（ProfileCard + gridItems）
 * 与 `ShortVideoController.fetchProfile / deleteVideo`。
 *
 * 承载仓库方法：`fetchProfile`（资料卡）、`myVideos`（isMine 列表）、`delete`（删除）。
 * 同时提供「发布小视频」入口，打通 mock 上传流程（VideoPublish）。
 */
@Page("VideoProfile")
internal class VideoProfilePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val profile = remember { VideoStore.repo.fetchProfile().getOrNull() }
            var myVideos by remember { mutableStateOf(VideoStore.repo.myVideos().getOrDefault(emptyList())) }
            var toast by remember { mutableStateOf<String?>(null) }

            fun reload() {
                myVideos = VideoStore.repo.myVideos().getOrDefault(emptyList())
            }

            fun delete(item: ShortVideo) {
                VideoStore.repo.delete(item.id)
                    .onSuccess {
                        Utils.currentBridgeModule().toast("已删除")
                        reload()
                    }
                    .onFailure { Utils.currentBridgeModule().toast("删除失败：${it.message}") }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AppNavBarBar(
                    title = "我的视频",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = KuiklyTheme.canvas,
                    foreground = KuiklyTheme.foreground,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                ) {
                    item {
                        ProfileHeaderCard(profile = profile)
                        Spacer(Modifier.height(10.dp))
                    }
                    item {
                        ProfilePublishRow(onPublish = {
                            Utils.currentBridgeModule().openPage(url = "VideoPublish")
                        })
                        Spacer(Modifier.height(12.dp))
                    }
                    item {
                        Text(
                            "我的小视频 (${myVideos.size})",
                            fontSize = 15.sp,
                            color = KuiklyTheme.ink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    if (myVideos.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("还没有发布的小视频", fontSize = 13.sp, color = KuiklyTheme.muted)
                            }
                        }
                    } else {
                        items(myVideos, key = { it.id }) { item ->
                            ProfileVideoRow(item = item, onDelete = { delete(item) })
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    item {
                        Spacer(Modifier.height((bottom + 16f).dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(profile: VideoProfile?) {
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(KuiklyTheme.linkSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    (profile?.displayName ?: "我").take(1),
                    fontSize = 22.sp,
                    color = KuiklyTheme.linkDeep,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    profile?.displayName ?: "我",
                    fontSize = 18.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                val badge = profile?.roleBadge ?: "销售顾问"
                val store = profile?.storeName ?: "[4S]北京沃德龙鼎吉利"
                Text("$badge · $store", fontSize = 12.sp, color = KuiklyTheme.muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        val stats = profile?.stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileStatCell(label = "视频", value = "${stats?.videoCount ?: 0}")
            ProfileStatCell(label = "播放", value = "${stats?.viewCount ?: 0}")
            ProfileStatCell(label = "点赞", value = "${stats?.likeCount ?: 0}")
        }
    }
}

@Composable
private fun RowScope.ProfileStatCell(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun ProfilePublishRow(onPublish: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onPublish() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("＋ 发布小视频（mock 上传）", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileVideoRow(item: ShortVideo, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VideoCoverBlock(title = item.title, duration = item.duration, size = 56)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(
                "${item.viewLabel} 播放 · ${item.likeLabel} 赞",
                fontSize = 12.sp,
                color = KuiklyTheme.muted,
            )
            Spacer(Modifier.height(4.dp))
            KChip(
                when (item.status) {
                    ShortVideoStatus.published -> "已发布"
                    ShortVideoStatus.reviewing -> "审核中"
                    ShortVideoStatus.draft -> "草稿"
                },
                color = if (item.status == ShortVideoStatus.published) KuiklyTheme.success else KuiklyTheme.warning,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "删除",
            fontSize = 13.sp,
            color = KuiklyTheme.error,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onDelete() }.padding(8.dp),
        )
    }
}
