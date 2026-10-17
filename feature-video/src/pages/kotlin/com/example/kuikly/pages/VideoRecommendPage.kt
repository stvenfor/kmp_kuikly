package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.video.ShortVideo
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
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
 * 推荐视频 — 源 `DubbingHome`「猜你喜欢」/ 列表推荐位。
 * 复用列表卡片；点按进入详情。
 */
@Page("VideoRecommend")
internal class VideoRecommendPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var items by remember { mutableStateOf<List<ShortVideo>>(emptyList()) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                items = VideoStore.repo.recommend().getOrDefault(emptyList())
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AppNavBarBar(
                    title = "推荐视频",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = KuiklyTheme.canvas,
                    foreground = KuiklyTheme.foreground,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = (bottom + 16f).dp),
                ) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                "猜你喜欢",
                                fontSize = 16.sp,
                                color = KuiklyTheme.ink,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    items(items, key = { it.id }) { item ->
                        VideoListTile(
                            item = item,
                            onTap = {
                                val data = JSONObject().apply { put("id", item.id) }
                                Utils.currentBridgeModule().openPage(url = "VideoDetail", userData = data)
                            },
                            onDubbing = {
                                val data = JSONObject().apply { put("id", item.id) }
                                Utils.currentBridgeModule().openPage(url = "VideoDubbing", userData = data)
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
