package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.classroom.HomeworkTask
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
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
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 作业详情页：题目进度 + 完成状态 + 星星奖励（port of source HomeworkDetailStudent/Teacher）。
 */
@Page("HomeworkDetail")
internal class HomeworkDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            val taskId = pageData.params.optString("id")
            var task by remember { mutableStateOf<HomeworkTask?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                val all = ClassroomStore.repo.tasks().getOrElse {
                    task = null
                    error = it.message ?: "加载失败"
                    return
                }
                task = all.firstOrNull { it.id == taskId }
                error = if (task == null) "作业不存在" else null
            }

            if (!booted) {
                booted = true
                reload()
            }

            val t = task
            if (t == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(error ?: "作业不存在", fontSize = 14.sp, color = KuiklyTheme.error)
                    Spacer(Modifier.height(16.dp))
                    KButton(text = "重试", onClick = ::reload)
                }
                return@setContent
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(t.title, fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    KCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(t.title, fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            if (t.isCompleted) KChip("已完成", color = KuiklyTheme.success) else KChip("待完成", color = KuiklyTheme.warning)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("${t.className} · ${t.subtitle}", fontSize = 13.sp, color = KuiklyTheme.muted)
                        Spacer(Modifier.height(4.dp))
                        Text("奖励 ⭐ ${t.starReward}", fontSize = 13.sp, color = KuiklyTheme.link)
                    }
                    Spacer(Modifier.height(12.dp))

                    KCard {
                        Text("题目进度", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.width(160.dp).height(10.dp)
                                    .background(KuiklyTheme.hairline, RoundedCornerShape(5.dp)),
                            ) {
                                val ratio = if (t.isCompleted) 1.0f else 0.4f
                                Box(
                                    modifier = Modifier.width((160 * ratio).dp).height(10.dp)
                                        .background(KuiklyTheme.link, RoundedCornerShape(5.dp)),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (t.isCompleted) "${t.questionCount}/${t.questionCount}" else "0/${t.questionCount}",
                                fontSize = 13.sp,
                                color = KuiklyTheme.muted,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        if (!t.isCompleted) {
                            KButton(
                                text = "标记完成",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    ClassroomStore.repo.completeTask(t.id)
                                    Utils.currentBridgeModule().toast("已完成，获得 ${t.starReward} 颗星")
                                    reload()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 作业统计页：完成率聚合（port of source ClassHomeworkStatsPage）。
 */
@Page("HomeworkStats")
internal class HomeworkStatsPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var total by remember { mutableStateOf(0) }
            var done by remember { mutableStateOf(0) }
            var rate by remember { mutableStateOf(0.0) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                ClassroomStore.repo.stats()
                    .onSuccess {
                        total = it.totalCount
                        done = it.avgCompletion * it.totalCount / 100
                        rate = it.classCompletionRate
                        error = null
                    }
                    .onFailure { error = it.message ?: "加载失败" }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("作业统计", fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                if (error != null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(error!!, fontSize = 14.sp, color = KuiklyTheme.error)
                        Spacer(Modifier.height(16.dp))
                        KButton(text = "重试", onClick = ::reload)
                    }
                    return@setContent
                }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    KCard {
                        Text("整体完成情况", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatCell("作业总数", "$total")
                            StatCell("已完成", "$done")
                            StatCell("完成率", "${(rate * 100).toInt()}%")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    KCard {
                        Text("按类型拆分", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        listOf("配音作业" to "dubbing", "阅读理解" to "quiz", "每日一练" to "quiz").forEach { (label, _) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label, fontSize = 13.sp, color = KuiklyTheme.body)
                                Text("完成率 ${(rate * 100).toInt()}%", fontSize = 13.sp, color = KuiklyTheme.link)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(88.dp)) {
        Text(value, fontSize = 22.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}
