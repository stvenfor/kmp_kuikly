package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.usedcar.UsedCarStore
import com.example.kuikly.data.usedcar.formatAmount
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 二手车详情 — Migration Source `used_car_detail_page.dart` 复刻（P2-W2a）。
 *
 * - Flutter 该路由实为**交易详情**：AppNavBar「交易详情」+ 金额卡 + 基本信息/备注分组卡。
 * - Flutter 全裸逻辑 px → Kuikly 裸 dp/sp；mock 无 createdAt/updatedAt，时间信息卡不渲染。
 * - Flutter 备注行的「复制」按钮（Clipboard + toast）为平台能力，Kuikly 侧暂未复刻。
 */
@Page(name = "UsedCarDetail", moduleId = "feature_home")
internal class UsedCarDetailPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val id = pageData.params.optString("id").ifBlank { "1" }
        val top = statusBarInset()
        setContent {
            val result = remember(id) { UsedCarStore.repo.detail(id) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F6F8)),
            ) {
                // AppNavBar「交易详情」
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                ) {
                    Box(Modifier.height(top.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("交易详情", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
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
                                Text("‹", fontSize = 20.sp, color = Color(0xFF1A1A1A))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0x14000000)),
                    )
                }

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
                        result.onSuccess { item ->
                            // _AmountCard
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(20.dp),
                            ) {
                                Text(
                                    item.category,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A),
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    formatAmount(item.amount),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("记录编号 #${item.id}", fontSize = 13.sp, color = Color(0xFF757575))
                            }
                            Spacer(Modifier.height(12.dp))
                            // _InfoCard 基本信息
                            InfoCard(title = "基本信息") {
                                InfoRow("类型", item.type)
                                InfoRow("分类", item.category)
                                InfoRow("日期", item.date)
                            }
                            val note = item.note
                            if (!note.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                InfoCard(title = "备注") {
                                    InfoRow("内容", note)
                                }
                            }
                        }.onFailure {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Spacer(Modifier.height(64.dp))
                                Text("加载失败", fontSize = 16.sp, color = Color(0xFF1A1A1A))
                                Spacer(Modifier.height(8.dp))
                                Text(it.message ?: "", fontSize = 12.sp, color = Color(0xFF757575))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** `_InfoCard` 复刻：白底 12 圆角，标题 15 w600，内边距 (16,14,16,6)。 */
@Composable
private fun InfoCard(title: String, rows: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp),
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
        Spacer(Modifier.height(8.dp))
        rows()
    }
}

/** `_InfoRow` 复刻：label 定宽 72 灰、value #1A1A1A，行内边距 vertical 10。 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color(0xFF757575),
            modifier = Modifier.width(72.dp),
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A),
        )
    }
}
