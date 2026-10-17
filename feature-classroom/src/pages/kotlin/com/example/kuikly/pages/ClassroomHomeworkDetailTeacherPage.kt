package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.classroom.HomeworkTimelineItem
import com.example.kuikly.data.classroom.StudentProfile
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
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.CircleShape
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
 * 作业详情（老师视角）— Flutter `HomeworkDetailTeacherPage` 复刻（源路由
 * `classroomHomeworkDetailTeacher`）。
 *
 * 真源：档案卡（头像 + 姓名 + 作业数 + 完成率）+ 状态 Tab（全部 / 已完成 / 未完成）+ 时间线列表
 * （日期 + 圆点连线 + 作业卡，点入 `classroomHomeworkDetailStudent`）。
 */
@Page("ClassroomHomeworkDetailTeacher")
internal class ClassroomHomeworkDetailTeacherPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val studentId = pageData.params.optString("studentId").ifBlank { "student_001" }
        val classId = pageData.params.optString("classId").ifBlank { "class_001" }
        setContent {
            var profile by remember { mutableStateOf<StudentProfile?>(null) }
            var timeline by remember { mutableStateOf<List<HomeworkTimelineItem>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }
            var selectedTab by remember { mutableStateOf(0) }

            fun load() {
                ClassroomStore.repo.studentProfile(studentId)
                    .onSuccess { profile = it; error = null }
                    .onFailure { error = it.message }
                ClassroomStore.repo.timeline(studentId)
                    .onSuccess { timeline = it }
                    .onFailure { timeline = emptyList() }
            }
            if (!booted) { booted = true; load() }

            val filtered = when (selectedTab) {
                1 -> timeline.filter { it.isCompleted }
                2 -> timeline.filter { !it.isCompleted }
                else -> timeline
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClassroomPalette.background),
            ) {
                AppNavBarBar(
                    title = "作业详情",
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
                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState("⚠", "加载失败", error ?: "加载失败", "重试") { load() }
                    }
                    else -> {
                        profile?.let { ProfileCard(profile = it) }
                        Row(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)) {
                            StatusTab("全部", selectedTab == 0) { selectedTab = 0 }
                            StatusTab("已完成", selectedTab == 1) { selectedTab = 1 }
                            StatusTab("未完成", selectedTab == 2) { selectedTab = 2 }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = (bottom + 16f).dp),
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                val isLast = filtered.lastIndex == filtered.indexOf(item)
                                TimelineItem(
                                    item = item,
                                    isLast = isLast,
                                    onTap = {
                                        val data = com.tencent.kuikly.core.nvi.serialization.json.JSONObject()
                                        data.put("studentId", studentId)
                                        data.put("classId", classId)
                                        data.put("homeworkId", item.id)
                                        Utils.currentBridgeModule().openPage(url = "ClassroomHomeworkDetailStudent", userData = data)
                                    },
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 档案卡（源 `_ProfileCard`）。 */
@Composable
private fun ProfileCard(profile: StudentProfile) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(ClassroomPalette.primaryGreenLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(profile.avatarEmoji, fontSize = 28.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(profile.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
            Spacer(Modifier.height(6.dp))
            Text(
                "作业数：${profile.homeworkCount}    完成率：${profile.completionRate.toInt()}%",
                fontSize = 13.sp,
                color = ClassroomPalette.textGray,
            )
        }
    }
}

@Composable
private fun StatusTab(label: String, selected: Boolean, onTap: () -> Unit) {
    Text(
        label,
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) ClassroomPalette.titleBlack else ClassroomPalette.textGray,
        modifier = Modifier
            .clickable(onClick = onTap)
            .padding(end = 24.dp, bottom = 8.dp),
    )
}

/** 时间线项（源 `_TimelineItem`，圆点 + 卡片，省略连续竖线近似）。 */
@Composable
private fun TimelineItem(item: HomeworkTimelineItem, isLast: Boolean, onTap: () -> Unit) {
    val typeColor = hwTypeColor(item.type)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            item.dateLabel,
            fontSize = 11.sp,
            color = ClassroomPalette.textGray,
            modifier = Modifier
                .width(72.dp)
                .padding(top = 14.dp),
        )
        Column(
            modifier = Modifier.width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(ClassroomPalette.primaryGreen, CircleShape),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(ClassroomPalette.divider),
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .background(ClassroomPalette.cardWhite, RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp))
                .clickable(onClick = onTap)
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(typeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(hwTypeGlyph(item.type), fontSize = 18.sp, color = typeColor)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
                    Spacer(Modifier.height(4.dp))
                    Text(item.className, fontSize = 12.sp, color = ClassroomPalette.textGray)
                }
                Text(
                    if (item.isCompleted) "完成" else "未完成",
                    fontSize = 10.sp,
                    color = ClassroomPalette.textGray,
                    modifier = Modifier
                        .background(ClassroomPalette.divider, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

/** 作业类型 → 卡片主色（源 `homeworkTypeColor`）。 */
private fun hwTypeColor(type: String): Color = when (type) {
    "dubbing" -> ClassroomPalette.primaryGreen
    "sync" -> Color(0xFFFF8A34)
    "checkin" -> Color(0xFF1890FF)
    else -> ClassroomPalette.textGray
}

/** 作业类型 → 字形近似（源 `Icons.mic / menu_book / check_circle_outline`）。 */
private fun hwTypeGlyph(type: String): String = when (type) {
    "dubbing" -> "🎤"
    "sync" -> "📚"
    "checkin" -> "✅"
    else -> "•"
}
