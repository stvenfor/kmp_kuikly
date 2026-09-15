package com.example.kuikly.feature.feed.pages

import androidx.compose.runtime.remember
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.data.feed.FeedStore
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page(name = "FeedDetail", moduleId = "feature_feed")
internal class FeedDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id")
        setContent {
            val result = remember(id) { FeedStore.repo.detail(id) }
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                result.onSuccess { item ->
                    Text(item.title, fontSize = 22.sp, color = Color.Black)
                    Spacer(Modifier.height(12.dp))
                    Text(item.body, fontSize = 16.sp, color = Color.Gray)
                }.onFailure {
                    Text("Error: ${it.message}", color = Color.Red, fontSize = 16.sp)
                }
            }
        }
    }
}
