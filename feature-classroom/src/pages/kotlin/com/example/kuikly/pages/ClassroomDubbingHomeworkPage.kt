package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.classroom.ClassroomStore
import com.example.kuikly.data.classroom.DubbingHomeworkDetail
import com.example.kuikly.data.classroom.DubbingHomeworkItem
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
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 配音作业 — Flutter `DubbingHomeworkPage` 复刻（源路由 `classroomDubbingHomework`）。
 *
 * 真源：绿色渐变头（返回 + 学生名 + 标题 + 班级 + 截止 + 描述）+ 白色圆角卡（「配音作业（N）」+
 * 免费评分橙 tag + 列表：缩略图 + 标题 + 分数 + 去配音/重新提交按钮跳 `classroomVideoDetail`）
 * + 底部「分享到班级群」。
 */
@Page("ClassroomDubbingHomework")
internal class ClassroomDubbingHomeworkPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var detail by remember { mutableStateOf<DubbingHomeworkDetail?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun load() {
                ClassroomStore.repo.dubbingDetail()
                    .onSuccess { detail = it; error = null }
                    .onFailure { error = it.message }
            }
            if (!booted) { booted = true; load() }

            Column(modifier = Modifier.fillMaxSize().background(ClassroomPalette.background)) {
                when {
                    error != null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState("⚠", "加载失败", error ?: "加载失败", "重试") { load() }
                    }
                    detail == null -> Box(Modifier.weight(1f).fillMaxWidth()) {
                        DomainEmptyState("⏳", "加载中", "请稍候")
                    }
                    else -> {
                        val d = detail!!
                        GradientHeader(top = top, detail = d)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ClassroomPalette.cardWhite, RoundedCornerShape(ClassroomPalette.CARD_RADIUS.dp)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("配音作业（${d.items.size}）", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ClassroomPalette.titleBlack)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "本次作业可享受免费评分",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF8A34),
                                    modifier = Modifier
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                items(d.items, key = { it.id }) { item ->
                                    DubbingItemRow(
                                        item = item,
                                        onTap = {
                                            val data = com.tencent.kuikly.core.nvi.serialization.json.JSONObject()
                                            data.put("videoId", item.id)
                                            Utils.currentBridgeModule().openPage(url = "ClassroomVideoDetail", userData = data)
                                        },
                                    )
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = (bottom + 16f).dp, top = 8.dp)
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

/** 渐变头（源 `_GradientHeader`）。 */
@Composable
private fun GradientHeader(top: Float, detail: DubbingHomeworkDetail) {
    val shape = RoundedCornerShape(0.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF389E0F), Color(0xFF52C41A)),
                    start = com.tencent.kuikly.compose.ui.geometry.Offset.Zero,
                    end = com.tencent.kuikly.compose.ui.geometry.Offset.Infinite,
                ),
                shape,
            )
            .padding(start = 16.dp, end = 16.dp, top = (top + 8f).dp, bottom = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "‹",
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .clickable { Utils.currentBridgeModule().closePage() }
                    .padding(4.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(detail.studentName + "的作业", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(detail.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎒", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.width(4.dp))
            Text(detail.className, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⏰", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.width( 4.dp))
            Text("截止时间：${detail.deadline}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(12.dp))
        Text(detail.description, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

/** 配音项行（源 `_DubbingItemRow`）。 */
@Composable
private fun DubbingItemRow(item: DubbingHomeworkItem, onTap: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 56.dp)
                .background(ClassroomPalette.divider, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("🎬", fontSize = 24.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ClassroomPalette.titleBlack)
            Spacer(Modifier.height(4.dp))
            Text(
                "${item.score}分",
                fontSize = 13.sp,
                color = if (item.score >= 60) ClassroomPalette.primaryGreen else Color(0xFFFF8A34),
            )
        }
        Text(
            if (item.canResubmit) "重新提交" else "去配音",
            fontSize = 13.sp,
            color = if (item.canResubmit) ClassroomPalette.primaryGreen else Color.White,
            modifier = Modifier
                .clickable(onClick = onTap)
                .background(
                    if (item.canResubmit) Color.Transparent else ClassroomPalette.primaryGreen,
                    RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}
