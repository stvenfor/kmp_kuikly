package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
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
 * 作业点评 — Flutter `HomeworkReviewPage` 复刻（源路由 `classroomHomeworkReview`）。
 *
 * 真源：学生 tag 行 + 评语卡（长文 + 两条语音 + 语音评语/评语模版操作）+ 送礼品卡区（勾选 +
 * 计数 stepper）+ 底部「发送」。本页以本地 state 维护勾选与计数（源 `HomeworkReviewController`）。
 */
@Page("ClassroomHomeworkReview")
internal class ClassroomHomeworkReviewPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var sendGift by remember { mutableStateOf(false) }
            var giftCount by remember { mutableStateOf(1) }

            val students = listOf("老坛酸菜", "乌克丽丽", "张三李四", "酸菜", "王五赵六", "小明", "小红", "小刚")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClassroomPalette.background),
            ) {
                AppNavBarBar(
                    title = "作业点评",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = ClassroomPalette.background,
                    foreground = ClassroomPalette.titleBlack,
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
                ) {
                    item {
                        StudentTags(students = students)
                        Spacer(Modifier.height(12.dp))
                        FeedbackCard()
                        Spacer(Modifier.height(12.dp))
                        GiftCardSection(
                            sendGift = sendGift,
                            giftCount = giftCount,
                            studentCount = students.size,
                            onToggle = { sendGift = it },
                            onInc = { giftCount += 1 },
                            onDec = { if (giftCount > 1) giftCount -= 1 },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = (bottom + 16f).dp)
                        .height(48.dp)
                        .background(ClassroomPalette.primaryGreen, RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.dp))
                        .clickable {
                            Utils.currentBridgeModule().toast("点评已发送")
                            Utils.currentBridgeModule().closePage()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("发送", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

/** 学生 tag 行（源 `_StudentTags`）。 */
@Composable
private fun StudentTags(students: List<String>) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(12.dp),
    ) {
        students.forEach { name ->
            Text(
                name,
                fontSize = 13.sp,
                color = ClassroomPalette.titleBlack,
                modifier = Modifier
                    .background(ClassroomPalette.background, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

/** 评语卡（源 `_FeedbackCard`）。 */
@Composable
private fun FeedbackCard() {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape)
            .padding(16.dp),
    ) {
        Text(
            "作业完成的非常好，交作业时间也及时，一整个好评好评再好评，希望再接再厉，再创辉煌",
            fontSize = 14.sp,
            color = ClassroomPalette.titleBlack,
        )
        Spacer(Modifier.height(16.dp))
        AudioRow("03'22\"")
        Spacer(Modifier.height(8.dp))
        AudioRow("03'22\"")
        Spacer(Modifier.height(16.dp))
        Row {
            ActionChip("🎤", "语音评语")
            Spacer(Modifier.width(24.dp))
            ActionChip("✉️", "评语模版")
        }
    }
}

/** 语音行（源 `_AudioRow`，进度条用双 weight 近似）。 */
@Composable
private fun AudioRow(duration: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("▶️", fontSize = 20.sp, color = ClassroomPalette.primaryGreen)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(0.4f)
                .height(6.dp)
                .background(ClassroomPalette.primaryGreen, RoundedCornerShape(4.dp)),
        )
        Box(
            modifier = Modifier
                .weight(0.6f)
                .height(6.dp)
                .background(ClassroomPalette.divider, RoundedCornerShape(4.dp)),
        )
        Spacer(Modifier.width(8.dp))
        Text(duration, fontSize = 12.sp, color = ClassroomPalette.textGray)
        Spacer(Modifier.width(8.dp))
        Text("🗑️", fontSize = 16.sp, color = ClassroomPalette.textGray)
    }
}

/** 操作 chip（源 `_ActionChip`）。 */
@Composable
private fun ActionChip(icon: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp, color = ClassroomPalette.textGray)
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = ClassroomPalette.textGray)
    }
}

/** 送礼品卡区（源 `_GiftCardSection`）。 */
@Composable
private fun GiftCardSection(
    sendGift: Boolean,
    giftCount: Int,
    studentCount: Int,
    onToggle: (Boolean) -> Unit,
    onInc: () -> Unit,
    onDec: () -> Unit,
) {
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
                    .size(20.dp)
                    .background(
                        if (sendGift) ClassroomPalette.primaryGreen else Color.Transparent,
                        RoundedCornerShape(4.dp),
                    )
                    .clickable { onToggle(!sendGift) },
                contentAlignment = Alignment.Center,
            ) {
                if (sendGift) Text("✓", fontSize = 14.sp, color = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("送礼品卡 (8)", fontSize = 14.sp, color = ClassroomPalette.titleBlack)
            Spacer(Modifier.weight(1f))
            StepperButton("−") { onDec() }
            Text(" $giftCount ", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
            StepperButton("+") { onInc() }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "+$giftCount 即为以上${studentCount}名同学各送${giftCount}张",
            fontSize = 12.sp,
            color = ClassroomPalette.textGray,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

/** 步进按钮（源 `_StepperButton`）。 */
@Composable
private fun StepperButton(label: String, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(Color.Transparent)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 18.sp, color = ClassroomPalette.textGray)
    }
}
