package com.example.kuikly.pages

import com.example.kuikly.base.BaseComposePager
import com.tencent.kuikly.compose.foundation.background
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
import com.tencent.kuikly.compose.foundation.pager.HorizontalPager
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/** Compose HorizontalPager 预览（见 docs/Compose/list-and-scroll.md） */
@Page("ComposePager")
internal class ComposePagerPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val pages = listOf(
            Color(0xFF1565C0) to "蓝 · Banner 1",
            Color(0xFF2E7D32) to "绿 · Banner 2",
            Color(0xFF6A1B9A) to "紫 · Banner 3",
            Color(0xFFEF6C00) to "橙 · Banner 4",
            Color(0xFFC62828) to "红 · Banner 5",
        )
        setContent {
            val pagerState = rememberPagerState(pageCount = { pages.size })
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Compose Pager Lab", fontSize = 22.sp, color = Color.Black)
                Text(
                    text = "左右滑动 HorizontalPager",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                ) { page ->
                    val (color, label) = pages[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp)
                            .background(color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = Color.White, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    pages.indices.forEach { index ->
                        val active = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (active) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (active) Color(0xFF1565C0) else Color(0xFFBDBDBD)),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "第 ${pagerState.currentPage + 1} / ${pages.size} 页",
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}
