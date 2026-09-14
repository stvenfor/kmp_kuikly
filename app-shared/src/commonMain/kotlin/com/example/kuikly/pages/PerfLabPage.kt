package com.example.kuikly.pages

import com.example.kuikly.base.BaseComposePager
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("PerfLab")
internal class PerfLabPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val rows = (1..50).map { "Row #$it — long list stress" }
        setContent {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text("Perf Lab (50 rows)", fontSize = 20.sp, color = Color.Black)
                rows.forEach { line ->
                    Text(
                        text = line,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    )
                }
            }
        }
    }
}
