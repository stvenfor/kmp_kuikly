package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.classroom.HomeworkStats
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.RowScope
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 作业统计 — Flutter `ClassHomeworkStatsPage` 复刻（源路由 `classroomHomeworkStats`）。
 *
 * 真源：统计卡（作业数 / 人均完成 / 班级完成率 + 三类作业 tag）+ 学生列表卡（姓名 / 已完成 /
 * 待完成，点入 `classroomHomeworkDetailTeacher`）。主 Tab（全部 / 配音作业 / 同步作业）与时间
 * 筛选（本周 / 本月 / 上个月 / 自定义）在源里驱动 `HomeworkStatsController`，本页以本地 state
 * 近似（筛选为视觉态，不真正切数据）。
 */
@Page("ClassroomHomeworkStats")
internal class ClassroomHomeworkStatsPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var students by remember { mutableStateOf(emptyList<com.example.kuikly.data.classroom.StudentHomeworkRow>()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }
            var selectedTab by remember { mutableStateOf(0) }
            var selectedTime by remember { mutableStateOf(0) }

            val summary: HomeworkStats? = remember { ClassroomStore.repo.stats().getOrNull() }

            fun load() {
                ClassroomStore.repo.students()
                    .onSuccess { students = it; error = null }
                    .onFailure { students = emptyList(); error = it.message }
            }
            if (!booted) { booted = true; load() }

            val classId = "class_001"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClassroomPalette.background),
            ) {
                AppNavBarBar(
                    title = "作业统计",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = ClassroomPalette.background,
                    foreground = ClassroomPalette.titleBlack,
                    actions = {
                        Text(
                            "导出成绩",
                            fontSize = 14.sp,
                            color = ClassroomPalette.primaryGreen,
                            modifier = Modifier
                                .clickable { Utils.currentBridgeModule().toast("导出成绩功能开发中") }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    },
                )
                // 主 Tab（全部 / 配音作业 / 同步作业）+ 时间筛选 label。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatsTabChip("全部", selectedTab == 0) { selectedTab = 0 }
                    StatsTabChip("配音作业", selectedTab == 1) { selectedTab = 1 }
                    StatsTabChip("同步作业", selectedTab == 2) { selectedTab = 2 }
                    Spacer(Modifier.weight(1f))
                    Text(
                        listOf("本周", "本月", "上个月", "自定义")[selectedTime],
                        fontSize = 13.sp,
                        color = ClassroomPalette.textGray,
                    )
                    Text("▾", fontSize = 14.sp, color = ClassroomPalette.textGray)
                }
                // 时间筛选胶囊行。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                ) {
                    val times = listOf("本周", "本月", "上个月", "自定义")
                    times.forEachIndexed { i, label ->
                        StatsTimePill(label, selectedTime == i) { selectedTime = i }
                        Spacer(Modifier.width(8.dp))
                    }
                }
                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState("⚠", "加载失败", error ?: "加载失败", "重试") { load() }
                    }
                    else -> LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = (bottom + 16f).dp),
                    ) {
                        item { StatsCard(summary = summary) }
                        item { Spacer(Modifier.height(12.dp)) }
                        item {
                            StudentListCard(
                                students = students,
                                classId = classId,
                                onOpen = { sid ->
                                    val data = com.tencent.kuikly.core.nvi.serialization.json.JSONObject()
                                    data.put("studentId", sid)
                                    data.put("classId", classId)
                                    Utils.currentBridgeModule().openPage(url = "ClassroomHomeworkDetailTeacher", userData = data)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 主 Tab 下划线 chip（源 `_TabChip`）。 */
@Composable
private fun StatsTabChip(label: String, selected: Boolean, onTap: () -> Unit) {
    Text(
        label,
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) ClassroomPalette.titleBlack else ClassroomPalette.textGray,
        modifier = Modifier
            .clickable(onClick = onTap)
            .padding(end = 20.dp, bottom = 8.dp),
    )
}

/** 时间筛选胶囊（源 `_TimePill`）。 */
@Composable
private fun StatsTimePill(label: String, selected: Boolean, onTap: () -> Unit) {
    Text(
        label,
        fontSize = 13.sp,
        color = if (selected) ClassroomPalette.primaryGreen else ClassroomPalette.textGray,
        modifier = Modifier
            .clickable(onClick = onTap)
            .background(
                if (selected) ClassroomPalette.primaryGreenLight else ClassroomPalette.cardWhite,
                RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** 统计卡（源 `_StatsCard`）：三指标 + 作业类型 tag。 */
@Composable
private fun StatsCard(summary: HomeworkStats?) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatItem((summary?.totalCount ?: 0).toString(), "作业数")
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(ClassroomPalette.divider),
            )
            StatItem((summary?.avgCompletion ?: 0).toString(), "人均完成")
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(ClassroomPalette.divider),
            )
            StatItem("${summary?.classCompletionRate?.toInt() ?: 0}%", "班级完成率")
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("作业类型", fontSize = 13.sp, color = ClassroomPalette.textGray)
            Spacer(Modifier.width(8.dp))
            val counts = summary?.typeCounts ?: emptyMap()
            listOf(
                "配音作业 ${counts["dubbing"] ?: 0}",
                "同步作业 ${counts["sync"] ?: 0}",
                "打卡作业 ${counts["checkin"] ?: 0}",
            ).forEach {
                Text(
                    it,
                    fontSize = 11.sp,
                    color = ClassroomPalette.textGray,
                    modifier = Modifier
                        .background(ClassroomPalette.background, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
        }
    }
}

@Composable
private fun RowScope.StatItem(value: String, label: String) {
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = ClassroomPalette.textGray)
        }
    }
}

/** 学生列表卡（源 `_StudentListCard`）。 */
@Composable
private fun StudentListCard(
    students: List<com.example.kuikly.data.classroom.StudentHomeworkRow>,
    classId: String,
    onOpen: (String) -> Unit,
) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("姓名", fontSize = 13.sp, color = ClassroomPalette.textGray, modifier = Modifier.weight(1f))
            Text("已完成", fontSize = 13.sp, color = ClassroomPalette.textGray, modifier = Modifier.width(60.dp))
            Text("待完成", fontSize = 13.sp, color = ClassroomPalette.textGray, modifier = Modifier.width(60.dp))
            Spacer(Modifier.width(24.dp))
        }
        students.forEach { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(s.id) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(s.name, fontSize = 15.sp, color = ClassroomPalette.titleBlack, modifier = Modifier.weight(1f))
                Text("${s.completed}", fontSize = 15.sp, color = ClassroomPalette.titleBlack, modifier = Modifier.width(60.dp))
                Text("${s.pending}", fontSize = 15.sp, color = ClassroomPalette.titleBlack, modifier = Modifier.width(60.dp))
                Text("›", fontSize = 20.sp, color = ClassroomPalette.textGrayLight)
            }
        }
    }
}
