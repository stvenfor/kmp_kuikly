package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.usedcar.UsedCarItem
import com.example.kuikly.data.usedcar.UsedCarStore
import com.example.kuikly.data.usedcar.formatAmount
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/** UsedCarDetail chrome 令牌（Flutter 本页为裸逻辑 px，不接 DesignScale）。 */
private object UsedCarDetailPalette {
    val background = Color(0xFFF5F6F8)
    val surface = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF1A1A1A)
    val gray500 = Color(0xFF9E9E9E)
    val gray600 = Color(0xFF757575)
    val hairline = Color(0x14000000)
    // Flutter BoxShadow: Colors.black 4%, blurRadius 8, offset(0,2)
    val cardShadow = Color(0x0A000000)
}

/**
 * 二手车详情 — Migration Source `used_car_detail_page.dart` 复刻（P2-W2a）。
 *
 * - Flutter 该路由实为**交易详情**：AppNavBar「交易详情」+ 金额卡 + 基本信息/备注分组卡。
 * - Flutter 全裸逻辑 px → Kuikly 裸 dp/sp；mock 无 userId/createdAt/updatedAt，对应行不渲染
 *   （Flutter mock `TransactionMockData` 同样不赋值，故三方 mock 记录渲染一致）。
 * - 备注行「复制」复刻 Flutter `_InfoRow.onCopy`：调 `BridgeModule.copyToPasteboard`
 *   写剪贴板并 `toast("已复制")`；受本 ticket 单文件锁限制，调用落入 `feature-home`
 *   已有的 `Utils.currentBridgeModule()` 取用 `core-pager` 的 common API（W5a 已落 BridgeModule）。
 */
@Page(name = "UsedCarDetail", moduleId = "feature_home")
internal class UsedCarDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "1" }
        val top = statusBarInset()
        setContent {
            // Flutter _ErrorState 的「点击重试」→ controller.loadDetail
            var attempt by remember(id) { mutableStateOf(0) }
            val result = remember(id, attempt) { UsedCarStore.repo.detail(id) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(UsedCarDetailPalette.background),
            ) {
                // AppNavBar「交易详情」（toolbarHeight 56 / 标题 17 w600 居中 / 底部 8% hairline）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(UsedCarDetailPalette.surface),
                ) {
                    Box(Modifier.height(top.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("交易详情", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = UsedCarDetailPalette.textPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(56.dp)
                                    .clickable { Utils.currentBridgeModule().closePage() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("‹", fontSize = 20.sp, color = UsedCarDetailPalette.textPrimary)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(UsedCarDetailPalette.hairline),
                    )
                }

                val item = result.getOrNull()
                if (item != null) {
                    // Flutter SingleChildScrollView padding fromLTRB(16, 16, 16, 32)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 32.dp,
                        ),
                    ) {
                        item {
                            AmountCard(item)
                            Spacer(Modifier.height(12.dp))
                            InfoCard(title = "基本信息") {
                                InfoRow("类型", item.type)
                                InfoRow("分类", item.category)
                                InfoRow("日期", item.date)
                            }
                            val note = item.note
                            // Flutter: note != null && note!.isNotEmpty
                            if (!note.isNullOrEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                InfoCard(title = "备注") {
                                    // Flutter _InfoRow.onCopy：写剪贴板 + toast 反馈
                                    InfoRow(
                                        label = "内容",
                                        value = note,
                                        onCopy = {
                                            val bridge = Utils.currentBridgeModule()
                                            bridge.copyToPasteboard(note)
                                            bridge.toast("已复制")
                                        },
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // _ErrorState
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Icon(Icons.error_outline, size 48, Colors.grey)
                        Text("⚠", fontSize = 48.sp, color = UsedCarDetailPalette.gray500)
                        Spacer(Modifier.height(12.dp))
                        Text("加载失败", fontSize = 14.sp, color = UsedCarDetailPalette.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            result.exceptionOrNull()?.message ?: "",
                            fontSize = 12.sp,
                            color = UsedCarDetailPalette.gray600,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { attempt++ }) { Text("点击重试") }
                    }
                }
            }
        }
    }
}

/** `_AmountCard` 复刻：白底 12 圆角 + BoxShadow(black 4%, blur 8, offset(0,2))，内边距 20。 */
@Composable
private fun AmountCard(item: UsedCarItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Flutter BoxShadow: black 4%, blur 8, offset(0,2)
            .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = UsedCarDetailPalette.cardShadow, spotColor = UsedCarDetailPalette.cardShadow)
            .background(UsedCarDetailPalette.surface, RoundedCornerShape(12.dp))
            .padding(20.dp),
    ) {
        Text(
            item.category,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = UsedCarDetailPalette.textPrimary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            formatAmount(item.amount),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = UsedCarDetailPalette.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text("记录编号 #${item.id}", fontSize = 13.sp, color = UsedCarDetailPalette.gray600)
    }
}

/** `_InfoCard` 复刻：白底 12 圆角，标题 15 w600，内边距 (16,14,16,6)。 */
@Composable
private fun InfoCard(title: String, rows: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UsedCarDetailPalette.surface, RoundedCornerShape(12.dp))
            .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp),
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = UsedCarDetailPalette.textPrimary)
        Spacer(Modifier.height(8.dp))
        rows()
    }
}

/** `_InfoRow` 复刻：label 定宽 72 灰、value 撑满 #1A1A1A（height 1.4），行内边距 vertical 10。 */
@Composable
private fun InfoRow(label: String, value: String, onCopy: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        // Flutter CrossAxisAlignment.start：value 换行时 label 保持顶部对齐
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = UsedCarDetailPalette.gray600,
            modifier = Modifier.width(72.dp),
        )
        Text(
            value,
            fontSize = 14.sp,
            color = UsedCarDetailPalette.textPrimary,
            // Flutter TextStyle height: 1.4 → 14 × 1.4
            lineHeight = 19.6.sp,
            modifier = Modifier.weight(1f),
        )
        if (onCopy != null) {
            // Flutter 备注行「复制」文案按钮 + Clipboard + toast
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onCopy() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "复制",
                    fontSize = 12.sp,
                    color = UsedCarDetailPalette.gray500,
                )
            }
        }
    }
}
