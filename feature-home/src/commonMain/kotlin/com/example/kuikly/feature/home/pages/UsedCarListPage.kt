package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.usedcar.UsedCarItem
import com.example.kuikly.data.usedcar.UsedCarStore
import com.example.kuikly.data.usedcar.formatAmount
import com.example.kuikly.navigation.PageNames
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
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/** UsedCarList chrome 令牌（Flutter 本页为裸逻辑 px，不接 DesignScale）。 */
private object UsedCarPalette {
    val background = Color(0xFFF5F6F8)
    val surface = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF1A1A1A)
    val gray400 = Color(0xFFBDBDBD)
    val gray500 = Color(0xFF9E9E9E)
    val gray600 = Color(0xFF757575)
    val hairline = Color(0x14000000)
    // Flutter BoxShadow: Colors.black 4%, blurRadius 8, offset(0,2)
    val cardShadow = Color(0x0A000000)
}

/**
 * 二手车列表 — Migration Source `used_car_list_page.dart` 复刻（P2-W2a）。
 *
 * - Flutter 该路由实为**交易记录**列表：AppNavBar「二手车」+ `TransactionListItem` 卡片
 *   （类型 tag / 车型 / 金额 / 日期 / 备注）。
 * - Flutter 全裸逻辑 px（无 screenutil）→ Kuikly 裸 dp/sp。
 * - 右上角 Mock 场景切换（11sp 灰字）为 Kuikly 侧调试附加，Flutter ref 无此元素。
 */
@Page(name = "UsedCarList", moduleId = "feature_home")
internal class UsedCarListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var items by remember { mutableStateOf<List<UsedCarItem>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var loading by remember { mutableStateOf(true) }
            var hasMore by remember { mutableStateOf(false) }
            var page by remember { mutableStateOf(0) }
            var booted by remember { mutableStateOf(false) }
            var scenario by remember { mutableStateOf(MockBackend.scenario) }

            fun load(reset: Boolean) {
                scenario = MockBackend.scenario
                loading = true
                val nextPage = if (reset) 0 else page
                // Flutter TransactionMockData.pageSize = 20
                UsedCarStore.repo.listPage(nextPage, pageSize = 20)
                    .onSuccess { result ->
                        items = if (reset) result.items else items + result.items
                        page = nextPage + 1
                        hasMore = result.hasMore
                        empty = items.isEmpty()
                        error = null
                        loading = false
                    }
                    .onFailure {
                        if (reset) {
                            items = emptyList()
                            empty = false
                        }
                        error = it.message
                        loading = false
                    }
            }

            if (!booted) {
                booted = true
                load(reset = true)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(UsedCarPalette.background),
            ) {
                // AppNavBar（toolbarHeight 56 / 标题 17 w600 居中 / 底部 8% hairline）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(UsedCarPalette.surface),
                ) {
                    Box(Modifier.height(top.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("二手车", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = UsedCarPalette.textPrimary)
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
                                Text("‹", fontSize = 20.sp, color = UsedCarPalette.textPrimary)
                            }
                            Spacer(Modifier.weight(1f))
                            // Mock 场景调试开关（Kuikly 侧附加，非 Flutter 元素）
                            Text(
                                scenario.name,
                                fontSize = 11.sp,
                                color = UsedCarPalette.gray500,
                                modifier = Modifier
                                    .clickable {
                                        MockBackend.cycle()
                                        load(reset = true)
                                    }
                                    .padding(end = 12.dp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(UsedCarPalette.hairline),
                    )
                }

                when {
                    // Flutter: isLoading && items.isEmpty → Center(CircularProgressIndicator)
                    loading && items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null && items.isEmpty() -> {
                        // _ErrorState
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Icon(Icons.error_outline, size 48, Colors.grey)
                            Text("⚠", fontSize = 48.sp, color = UsedCarPalette.gray500)
                            Spacer(Modifier.height(12.dp))
                            Text("加载失败", fontSize = 14.sp, color = UsedCarPalette.textPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                error ?: "",
                                fontSize = 12.sp,
                                color = UsedCarPalette.gray600,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp),
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { load(reset = true) }) { Text("点击重试") }
                        }
                    }
                    empty -> {
                        // _EmptyState
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("▤", fontSize = 56.sp, color = UsedCarPalette.gray400)
                            Spacer(Modifier.height(12.dp))
                            Text("暂无交易记录", fontSize = 14.sp, color = UsedCarPalette.gray600)
                        }
                    }
                    else -> {
                        // Flutter ListView padding top 12 / bottom 24
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp + bottom.dp),
                        ) {
                            items(items, key = { it.id }) { item ->
                                TransactionCard(item)
                            }
                            if (hasMore) {
                                item {
                                    Text(
                                        "加载更多",
                                        fontSize = 13.sp,
                                        color = UsedCarPalette.gray600,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { load(reset = false) }
                                            .padding(vertical = 12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** `TransactionListItem` 复刻：类型 tag + 车型 + 金额 + 日期/备注 + chevron。 */
@Composable
private fun TransactionCard(item: UsedCarItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            // Flutter BoxShadow: black 4%, blur 8, offset(0,2)
            .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = UsedCarPalette.cardShadow, spotColor = UsedCarPalette.cardShadow)
            .background(UsedCarPalette.surface, RoundedCornerShape(12.dp))
            .clickable {
                Utils.currentBridgeModule().openPage(
                    PageNames.UsedCarDetail,
                    userData = JSONObject().apply { put("id", item.id) },
                )
            }
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeTag(item.type)
                Spacer(Modifier.width(8.dp))
                Text(
                    item.category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UsedCarPalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                formatAmount(item.amount),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = UsedCarPalette.textPrimary,
                // Flutter TextStyle height: 1.1
                lineHeight = 24.2.sp,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("▦", fontSize = 14.sp, color = UsedCarPalette.gray500)
                Spacer(Modifier.width(4.dp))
                Text(item.date, fontSize = 13.sp, color = UsedCarPalette.gray600)
                val note = item.note
                // Flutter: note != null && note!.isNotEmpty
                if (!note.isNullOrEmpty()) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        note,
                        fontSize = 13.sp,
                        color = UsedCarPalette.gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text("›", fontSize = 24.sp, color = UsedCarPalette.gray400)
    }
}

/** `_TypeTag` 复刻：income→收入 / expense→支出 / 其他原样。 */
@Composable
private fun TypeTag(type: String) {
    // Flutter 先 lowercase 再匹配
    val lower = type.lowercase()
    val (label, color, bgColor) = when {
        lower.contains("income") || type.contains("收入") ->
            Triple("收入", Color(0xFF2E7D32), Color(0xFFE8F5E9))
        lower.contains("expense") || type.contains("支出") ->
            Triple("支出", Color(0xFFC62828), Color(0xFFFFEBEE))
        else -> Triple(type, Color(0xFF1565C0), Color(0xFFE3F2FD))
    }
    Text(
        label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
