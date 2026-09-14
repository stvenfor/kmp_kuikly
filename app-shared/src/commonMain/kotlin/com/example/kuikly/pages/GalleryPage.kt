package com.example.kuikly.pages

import com.example.kuikly.base.BaseComposePager
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("Gallery")
internal class GalleryPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val colors = listOf(
            Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
            Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC),
            Color(0xFFFF8A65), Color(0xFFA1887F), Color(0xFF90A4AE),
        )
        setContent {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Gallery", fontSize = 20.sp, color = Color.Black)
                Spacer(Modifier.height(12.dp))
                colors.chunked(3).forEach { row ->
                    Row {
                        row.forEach { c ->
                            Box(modifier = Modifier.size(96.dp).background(c))
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
