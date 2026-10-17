package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.classroom.HomeworkTaskItem
import com.example.kuikly.data.classroom.StudentProfile
import com.example.kuikly.navigation.PageNames
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
 * 作业详情（学生视角）— Flutter `HomeworkDetailStudentPage` 复刻（源路由
 * `classroomHomeworkDetailStudent`）。
 *
 * 真源：用户卡 + 进度卡（进度条 + 🐦 标记）+ 社交证明行（已 N 人完成）+ 内容卡（子任务列表 +
 * 老师评语「点击领取」跳 `classroomClaimGift` + 「查看配音作业 >」跳 `classroomDubbingHomework`）。
 * 底部「分享到班级群」绿按钮（源 `bottomNavigationBar`）。
 */
@Page("ClassroomHomeworkDetailStudent")
internal class ClassroomHomeworkDetailStudentPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        val studentId = pageData.params.optString("studentId").ifBlank { "student_001" }
        setContent {
            var profile by remember { mutableStateOf<StudentProfile?>(null) }
            var tasks by remember { mutableStateOf<List<HomeworkTaskItem>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun load() {
                ClassroomStore.repo.studentProfile(studentId)
                    .onSuccess { profile = it; error = null }
                    .onFailure { error = it.message }
                ClassroomStore.repo.studentTasks()
                    .onSuccess { tasks = it }
                    .onFailure { tasks = emptyList() }
            }
            if (!booted) { booted = true; load() }

            val progress = (profile?.completionRate ?: 0.0) / 100.0
            val completedCount = 32

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
                )
                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState("⚠", "加载失败", error ?: "加载失败", "重试") { load() }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
                        ) {
                            item { profile?.let { UserCard(profile = it) } }
                            item { Spacer(Modifier.height(12.dp)) }
                            item {
                                ProgressCard(progress = progress.toFloat())
                                Spacer(Modifier.height(8.dp))
                                SocialProofRow(completedCount = completedCount)
                                Spacer(Modifier.height(12.dp))
                                ContentCard(
                                    tasks = tasks,
                                    onGift = { Utils.currentBridgeModule().openPage(PageNames.ClassroomGiftClaim) },
                                    onDubbing = {
                                        Utils.currentBridgeModule().openPage(url = "ClassroomDubbingHomework")
                                    },
                                )
                            }
                        }
                        // 底部「分享到班级群」绿按钮（源 bottomNavigationBar）。
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = (bottom + 16f).dp)
                                .height(48.dp)
                                .background(ClassroomPalette.primaryGreen, RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.dp))
                                .clickable { Utils.currentBridgeModule().toast("已分享到班级群") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("分享到班级群", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/** 用户卡（源 `_UserCard`）。 */
@Composable
private fun UserCard(profile: StudentProfile) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ClassroomPalette.primaryGreenLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(profile.avatarEmoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(profile.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClassroomPalette.background, RoundedCornerShape(8.dp))
                .padding(12.dp),
        ) {
            Text("这里老师没写的话默认会有一句话", fontSize = 13.sp, color = ClassroomPalette.textGray)
        }
    }
}

/** 进度卡（源 `_ProgressCard`，进度条用双 weight 近似 LinearProgressIndicator）。 */
@Composable
private fun ProgressCard(progress: Float) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("作业进度", fontSize = 14.sp, color = ClassroomPalette.titleBlack)
            Spacer(Modifier.weight(1f))
            Text("已完成 ${(progress * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.primaryGreen)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        ) {
            if (progress >= 0.999f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ClassroomPalette.primaryGreen, RoundedCornerShape(4.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(progress.coerceAtLeast(0.01f))
                        .background(ClassroomPalette.primaryGreen, RoundedCornerShape(4.dp)),
                )
                Box(
                    modifier = Modifier
                        .weight((1f - progress).coerceAtLeast(0.01f))
                        .background(ClassroomPalette.divider, RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}

/** 社交证明行（源 `_SocialProofRow`）。 */
@Composable
private fun SocialProofRow(completedCount: Int) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.width(60.dp)) {
            for (i in 0..2) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(ClassroomPalette.primaryGreenLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${i + 1}", fontSize = 10.sp, color = ClassroomPalette.primaryGreen)
                }
                if (i < 2) Spacer(Modifier.width(2.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text("已有${completedCount}人完成作业", fontSize = 13.sp, color = ClassroomPalette.textGray, modifier = Modifier.weight(1f))
        Text("›", fontSize = 20.sp, color = ClassroomPalette.textGrayLight)
    }
}

/** 内容卡（源 `_ContentCard`）：子任务列表 + 老师评语「点击领取」+ 查看配音作业。 */
@Composable
private fun ContentCard(tasks: List<HomeworkTaskItem>, onGift: () -> Unit, onDubbing: () -> Unit) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(16.dp),
    ) {
        Text("Unit 3 Part A: Let's talk & Let's learn", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
        Spacer(Modifier.height(12.dp))
        tasks.forEach { t ->
            TaskRow(task = t)
            Spacer(Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClassroomPalette.background, RoundedCornerShape(8.dp))
                .padding(12.dp),
        ) {
            Column {
                Text("谭老师：本次作业完成的很棒！送你 1 张体验卡，以资鼓励。", fontSize = 13.sp, color = ClassroomPalette.titleBlack)
                Spacer(Modifier.height(8.dp))
                Text(
                    "点击领取",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClassroomPalette.primaryGreen,
                    modifier = Modifier.clickable(onClick = onGift),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "查看配音作业 >",
            fontSize = 13.sp,
            color = ClassroomPalette.primaryGreen,
            modifier = Modifier.clickable(onClick = onDubbing),
        )
    }
}

/** 子任务行（源 `_TaskRow`）。 */
@Composable
private fun TaskRow(task: HomeworkTaskItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClassroomPalette.background, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(task.iconColor).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.iconLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(task.iconColor))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(task.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ClassroomPalette.titleBlack)
                Spacer(Modifier.width(6.dp))
                Text("+${task.starReward}", fontSize = 13.sp, color = ClassroomPalette.primaryGreen)
                Text(" ⭐", fontSize = 12.sp)
            }
            Text(task.subtitle, fontSize = 12.sp, color = ClassroomPalette.textGray)
        }
    }
}
