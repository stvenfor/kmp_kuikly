package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
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
 * 班级/课程详情 — 按 `ClassroomTheme` 令牌 + 共享 `AppNavBar` chrome 加厚（P2-W2c）。
 *
 * ponytail 天花板：Flutter classroom 模块的详情页（`class_homework_stats` /
 * `video_detail` / `homework_*`）与 Kuikly 这条 Phase-1「课程详情」路由**无 1:1 真源**
 * （Flutter 班级卡本身不可点，只有 3 个操作入口）。故本页保留 Phase-1 mock 字段，
 * 依 `ClassroomColors` 分组卡形制加厚（邀请码 / 班级成员 / 授课老师 / 课程简介）。
 */
@Page(name = "ClassroomDetail", moduleId = "feature_home")
internal class ClassroomDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "course_001" }
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            val result = remember(id) { ClassroomStore.repo.detail(id) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClassroomPalette.background),
            ) {
                AppNavBarBar(
                    title = "课程详情",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = ClassroomPalette.background,
                    foreground = ClassroomPalette.titleBlack,
                )
                val course = result.getOrNull()
                if (course == null) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState(
                            glyph = "⚠",
                            title = "加载失败",
                            message = result.exceptionOrNull()?.message ?: "加载失败",
                            actionLabel = "返回",
                            onAction = { Utils.currentBridgeModule().closePage() },
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = (bottom + 24f).dp,
                        ),
                    ) {
                        item {
                            val cardShape = RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // Flutter `_ClassCard` BoxShadow: black 4% / blur 8 / offset(0, 2)——
                                    // 与 `ClassroomListPage.ClassCard` 同形制（ADR-0018 跨源一致性）。
                                    .shadow(8.dp, cardShape, ambientColor = ClassroomPalette.cardShadow, spotColor = ClassroomPalette.cardShadow)
                                    .background(ClassroomPalette.cardWhite, cardShape)
                                    .padding(16.dp),
                            ) {
                                Text(
                                    course.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ClassroomPalette.titleBlack,
                                )
                                Spacer(Modifier.height(8.dp))
                                // Flutter `_ClassCard` 同形制：「邀请码：X」左灰 / 「班级成员：N」右灰，一行。
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("邀请码：${course.inviteCode}", fontSize = 13.sp, color = ClassroomPalette.textGray)
                                    Spacer(Modifier.weight(1f))
                                    Text("班级成员：${course.memberCount}", fontSize = 13.sp, color = ClassroomPalette.textGray)
                                }
                                Spacer(Modifier.height(12.dp))
                                // 授课老师行（头像 36·浅绿底 + 绿字，同 Flutter 上传者行形制）
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(ClassroomPalette.primaryGreenLight, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            course.teacher.take(1),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ClassroomPalette.primaryGreen,
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "授课老师：${course.teacher}",
                                        fontSize = 13.sp,
                                        color = ClassroomPalette.textGray,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(ClassroomPalette.divider),
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "课程简介",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ClassroomPalette.titleBlack,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(course.summary, fontSize = 13.sp, color = ClassroomPalette.textGray)
                            }
                            Spacer(Modifier.height(16.dp))
                            // Flutter 真源里「领取礼品卡」是作业详情页的绿字链接（「点击领取」）；
                            // 本页无 1:1 真源，按 P2-W4b 用同一形制绿字链接接入已注册的 ClassroomGiftClaim。
                            Text(
                                "领取礼品卡 >",
                                fontSize = 13.sp,
                                color = ClassroomPalette.primaryGreen,
                                modifier = Modifier.clickable {
                                    Utils.currentBridgeModule().openPage(PageNames.ClassroomGiftClaim)
                                },
                            )
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        ClassroomPalette.primaryGreen,
                                        RoundedCornerShape(ClassroomPalette.BUTTON_RADIUS.dp),
                                    )
                                    .clickable {
                                        // Flutter `classroomMyClass` 的真源入口是首页全部服务「班级教学」
                                        // （AllServicesPage 不在本 ticket 锁内），故由本页既有 CTA 承接进入班级。
                                        Utils.currentBridgeModule().openPage(PageNames.ClassroomMyClass)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "进入班级",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
