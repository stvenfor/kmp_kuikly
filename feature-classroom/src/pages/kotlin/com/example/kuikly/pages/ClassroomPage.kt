package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("Classroom")
internal class ClassroomPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var tasks by remember { mutableStateOf<List<com.example.kuikly.data.classroom.HomeworkTask>>(emptyList()) }
            var rate by remember { mutableStateOf(0.0) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                ClassroomStore.repo.tasks().onSuccess { tasks = it }.onFailure { error = it.message }
                ClassroomStore.repo.stats().onSuccess { rate = it.classCompletionRate }
            }
            if (!booted) { booted = true; reload() }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("课堂作业", fontSize = 22.sp, color = KuiklyTheme.foreground, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Bold)
                Text("完成率 ${(rate * 100).toInt()}%", fontSize = 14.sp, color = KuiklyTheme.link)
                Spacer(Modifier.height(4.dp))
                Text(
                    "查看班级作业统计 ›",
                    fontSize = 13.sp,
                    color = KuiklyTheme.link,
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().openPage(
                            url = "ClassroomHomeworkStats",
                            userData = com.tencent.kuikly.core.nvi.serialization.json.JSONObject(),
                        )
                    },
                )
                Spacer(Modifier.height(12.dp))
                if (error != null) Text("Error: $error", color = Color.Red, fontSize = 14.sp)
                tasks.forEach { t ->
                    Column(modifier = Modifier.fillMaxWidth().clickable {
                        val data = com.tencent.kuikly.core.nvi.serialization.json.JSONObject()
                        data.put("studentId", "student_003")
                        Utils.currentBridgeModule().openPage(url = "ClassroomHomeworkDetailStudent", userData = data)
                    }.padding(vertical = 10.dp)) {
                        Text("${t.title} ${if (t.isCompleted) "✓" else ""}", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Medium)
                        Text("${t.className} · ${t.subtitle} · ⭐${t.starReward}", fontSize = 12.sp, color = KuiklyTheme.muted)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("[CYCLE MOCK] $MockBackend.scenario", fontSize = 12.sp, color = KuiklyTheme.muted,
                    modifier = Modifier.clickable { MockBackend.cycle(); reload() })
            }
        }
    }
}
