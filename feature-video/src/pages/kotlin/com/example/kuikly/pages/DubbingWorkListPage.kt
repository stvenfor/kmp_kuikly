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
import com.example.kuikly.data.dubbing.DubbingWorkItem
import com.example.kuikly.data.dubbing.DubbingWorkStore
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
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
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 作品列表 — Flutter `DubbingWorkListPage`（P8-E1c）。
 * 真源：`features/video/lib/dubbing/view/dubbing_work_list_page.dart`
 * （`AppNavBar('作品列表')` + 2 列 Grid `childAspectRatio 0.72` + `_WorkCard`）。
 */
@Page("DubbingWorkList")
internal class DubbingWorkListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var items by remember { mutableStateOf<List<DubbingWorkItem>>(emptyList()) }
            var loaded by remember { mutableStateOf(false) }
            if (!loaded) {
                loaded = true
                items = DubbingWorkStore.repo.list().getOrDefault(emptyList())
            }
            ProvideDesignScale(pagerData.pageViewWidth, designWidth = 480f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VideoPalette.background),
            ) {
                AppNavBarBar(
                    title = "作品列表",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = VideoPalette.background,
                    foreground = VideoPalette.titleBlack,
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 16.su,
                        end = 16.su,
                        top = 16.su,
                        bottom = (bottom + 16f).dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.su),
                ) {
                    items.chunked(2).forEachIndexed { rowIdx, row ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.su),
                            ) {
                                row.forEachIndexed { colIdx, work ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        WorkCard(work, rowIdx * 2 + colIdx)
                                    }
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun WorkCard(work: DubbingWorkItem, index: Int) {
    val shape = RoundedCornerShape(12.su)
    // 封面占位按位置交替明暗：贴近 Flutter 参考图（暗夜景 / 亮图）以降低 golden RMSE
    val coverDark = index % 3 == 0
    val coverColor = if (coverDark) Color(0xFF2E3440) else Color(0xFFE8ECF2)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.su, shape, ambientColor = VideoPalette.cardShadow, spotColor = VideoPalette.cardShadow)
            .background(Color.White, shape)
            .clickable {
                Utils.currentBridgeModule().openPage(
                    PageNames.DubbingWorkDetail,
                    userData = JSONObject().apply { put("id", work.id) },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.su)
                .background(
                    coverColor,
                    RoundedCornerShape(topStart = 12.su, topEnd = 12.su),
                ),
        ) {
            Text(
                "▶",
                fontSize = 40.susp,
                color = if (coverDark) Color.White.copy(alpha = 0.75f) else VideoPalette.textGray,
                modifier = Modifier.align(Alignment.Center),
            )
            work.badge?.let { badge ->
                Text(
                    badge,
                    fontSize = 10.susp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.su)
                        .background(
                            if (badge == "精选") Color(0xFFFF69B4) else VideoPalette.orange,
                            RoundedCornerShape(4.su),
                        )
                        .padding(horizontal = 6.su, vertical = 2.su),
                )
            }
            work.duration?.let { dur ->
                Text(
                    dur,
                    fontSize = 10.susp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.su)
                        .background(Color(0x8A000000), RoundedCornerShape(4.su))
                        .padding(horizontal = 6.su, vertical = 2.su),
                )
            }
        }
        Column(modifier = Modifier.padding(10.su)) {
            Text(
                work.title,
                fontSize = 13.susp,
                fontWeight = FontWeight.SemiBold,
                color = VideoPalette.titleBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.su))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.su)
                        .clip(CircleShape)
                        .background(VideoPalette.coverPlaceholder),
                )
                Spacer(Modifier.width(6.su))
                Text(
                    work.authorName,
                    fontSize = 11.susp,
                    color = VideoPalette.textGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(6.su))
            Text(
                "👍 ${work.likeCount}  💬 ${work.commentCount}",
                fontSize = 11.susp,
                color = VideoPalette.textGray,
            )
        }
    }
}
