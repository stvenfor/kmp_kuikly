package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.classroom.Course
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
 * 我的班级 — Flutter `MyClassListPage` 复刻（P2-W4a 接入路由 `classroomMyClass`）。
 *
 * **真源**：`features/classroom/lib/view/my_class_list_page.dart`（与 `ClassroomListPage`
 * 共用同一 Flutter 源文件，但 Kuikly 侧按 inventory 行 61 拆出独立 `PageName` 以保证可
 * 从不同入口分别打开）。
 *
 * **刻度**：Flutter classroom 模块 view/theme **全用裸逻辑 px**（grep 无 `.w/.h/.sp`），
 * 故一律裸 `dp`/`sp`，**不要** `.su()`——判断法与 P2-W2c `ClassroomPalette` 同。
 */
@Page(name = "ClassroomMyClass", moduleId = "feature_home")
internal class ClassroomMyClassPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun load() {
                ClassroomStore.repo.list()
                    .onSuccess {
                        courses = it
                        error = null
                    }
                    .onFailure {
                        courses = emptyList()
                        error = it.message
                    }
            }

            if (!booted) {
                booted = true
                load()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClassroomPalette.background),
            ) {
                AppNavBarBar(
                    title = "我的班级",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = ClassroomPalette.background,
                    foreground = ClassroomPalette.titleBlack,
                )
                Spacer(Modifier.height(8.dp))
                // Flutter `Padding(EdgeInsets.fromLTRB(16, 8, 16, 0))` 的「班级 / 禁用班级」操作行。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⇄", fontSize = 20.sp, color = ClassroomPalette.primaryGreen)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "班级",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ClassroomPalette.titleBlack,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "禁用班级",
                        fontSize = 14.sp,
                        color = ClassroomPalette.primaryGreen,
                        modifier = Modifier
                            .clickable { Utils.currentBridgeModule().toast("禁用班级功能开发中") }
                            .padding(vertical = 4.dp),
                    )
                }
                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "加载失败",
                            message = error ?: "加载失败",
                            actionLabel = "重试",
                            onAction = { load() },
                        )
                    }
                    courses.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "🏫",
                            title = "暂无班级",
                            message = "点击刷新重新加载",
                            actionLabel = "刷新",
                            onAction = { load() },
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        ),
                    ) {
                        items(courses, key = { it.id }) { course ->
                            MyClassCard(course = course)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                // Flutter `SafeArea(top: false, child: Padding(fromLTRB(16, 0, 16, 16), SizedBox(h48, r24)))`
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = (bottom + 16f).dp)
                        .height(48.dp)
                        .background(ClassroomPalette.primaryGreen, RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.dp))
                        .clickable { Utils.currentBridgeModule().toast("创建班级功能开发中") },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "创建班级",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * Flutter `_ClassCard`（结构与 `ClassroomListPage.ClassCard` 一致，仅去掉了点入
 * `ClassroomDetail` 的整卡点击 —— KDoc "不点入 ClassroomDetail"）：
 * 白卡 r12 + 阴影 + 名字 18·w600 / 邀请码·班级成员 13·灰 + 1px divider
 * + 3 个等宽操作（图标 22 + 4 + 文案 12）+ 「作业点评 >」13 绿。
 */
@Composable
private fun MyClassCard(course: Course) {
    val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter `_ClassCard` BoxShadow: black 4% / blur 8 / offset(0, 2)
            .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
            .background(ClassroomPalette.cardWhite, cardShape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                course.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClassroomPalette.titleBlack,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("邀请码：${course.inviteCode}", fontSize = 13.sp, color = ClassroomPalette.textGray)
                Spacer(Modifier.weight(1f))
                Text("班级成员：${course.memberCount}", fontSize = 13.sp, color = ClassroomPalette.textGray)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ClassroomPalette.divider),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            MyClassAction(Modifier.weight(1f), "👤", "邀请同学") {
                Utils.currentBridgeModule().toast("邀请同学功能开发中")
            }
            MyClassAction(Modifier.weight(1f), "📊", "作业统计") {
                Utils.currentBridgeModule().toast("作业统计功能开发中")
            }
            MyClassAction(Modifier.weight(1f), "🏆", "排行榜") {
                Utils.currentBridgeModule().toast("排行榜功能开发中")
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "作业点评 >",
                fontSize = 13.sp,
                color = ClassroomPalette.primaryGreen,
                modifier = Modifier.clickable { Utils.currentBridgeModule().toast("作业点评功能开发中") },
            )
        }
    }
}

/** Flutter `_ActionButton`：`weight(1f)` 等宽 + 竖向 padding 12 + 图标 22 + 4 + 文案 12。 */
@Composable
private fun MyClassAction(modifier: Modifier, icon: String, label: String, onTap: () -> Unit) {
    Column(
        modifier = modifier
            .clickable(onClick = onTap)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = ClassroomPalette.titleBlack)
    }
}