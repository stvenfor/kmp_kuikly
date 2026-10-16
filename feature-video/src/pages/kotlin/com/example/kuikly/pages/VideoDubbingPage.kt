package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.video.DubbingTask
import com.example.kuikly.data.video.VideoStore
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
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

private val DUBBING_LANGS = listOf("en", "ja", "ko", "zh")

@Page("VideoDubbing")
internal class VideoDubbingPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val videoId = pageData.params.optString("id")
        setContent {
            var tasks by remember { mutableStateOf<List<DubbingTask>>(emptyList()) }
            var lang by remember { mutableStateOf("en") }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                VideoStore.repo.dubbingTasks(videoId.ifBlank { null })
                    .onSuccess { list ->
                        tasks = list
                        empty = list.isEmpty()
                        error = null
                    }
                    .onFailure {
                        tasks = emptyList()
                        empty = false
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("配音任务", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                val title = if (videoId.isBlank()) "全部视频" else VideoStore.repo.fetchById(videoId).getOrNull()?.title ?: videoId
                DubbingVideoHeader(videoId = videoId, title = title)
                Spacer(Modifier.height(10.dp))
                DubbingLangPicker(lang = lang, onPick = { lang = it })
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .clickable {
                            VideoStore.repo.startDubbing(videoId, lang)
                                .onSuccess {
                                    Utils.currentBridgeModule().toast("已创建${it.langLabel}配音任务")
                                    reload()
                                }
                                .onFailure { Utils.currentBridgeModule().toast("创建失败：${it.message}") }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("开始配音", color = KuiklyTheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))

                when {
                    error != null && tasks.isEmpty() ->
                        VideoListErrorState(message = error ?: "加载失败", onRetry = ::reload)
                    empty -> VideoListEmptyState(onRefresh = ::reload)
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            DubbingTaskTile(
                                task = task,
                                onAdvance = {
                                    VideoStore.repo.advanceDubbing(task.id)
                                    reload()
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            Spacer(Modifier.height(4.dp))
                            VideoMockCycleRow(scenario = MockBackend.scenario) { MockBackend.cycle() }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DubbingVideoHeader(videoId: String, title: String) {
    KCard {
        Text("配音目标", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(4.dp))
        Text(title, fontSize = 15.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("videoId = ${videoId.ifBlank { "全部" }}", fontSize = 11.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun DubbingLangPicker(lang: String, onPick: (String) -> Unit) {
    KCard {
        Text("配音语言", fontSize = 14.sp, color = KuiklyTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DUBBING_LANGS.forEach { code ->
                val selected = code == lang
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) KuiklyTheme.primary else KuiklyTheme.canvasSoft2,
                            RoundedCornerShape(KuiklyTheme.radiusSm),
                        )
                        .clickable { onPick(code) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        langNameOf(code),
                        fontSize = 12.sp,
                        color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.ink,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun DubbingTaskTile(task: DubbingTask, onAdvance: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                task.videoTitle.ifBlank { task.videoId },
                fontSize = 14.sp,
                color = KuiklyTheme.ink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            KChip(task.langLabel, color = KuiklyTheme.violet)
            Spacer(Modifier.width(6.dp))
            KChip(
                task.statusLabel,
                color = when (task.status) {
                    "done" -> KuiklyTheme.success
                    "failed" -> KuiklyTheme.error
                    "running" -> KuiklyTheme.warning
                    else -> KuiklyTheme.muted
                },
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            VideoProgressTrack(progress = task.progress, trackWidth = 180)
            Spacer(Modifier.width(8.dp))
            Text(task.progressLabel, fontSize = 11.sp, color = KuiklyTheme.muted)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onAdvance() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("推进进度 +25%", fontSize = 12.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Text("›", fontSize = 15.sp, color = KuiklyTheme.muted)
        }
    }
}

private fun langNameOf(code: String): String = when (code) {
    "en" -> "英语"
    "ja" -> "日语"
    "ko" -> "韩语"
    "zh" -> "中文"
    else -> code
}
