package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.feed.FeedItem
import com.example.kuikly.data.feed.FeedStore
import com.example.kuikly.data.mock.MockBackend
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
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Scaffold
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 四 Tab 主壳：首页 / 动态 / 实验室 / 我的
 * Scaffold bottomBar（docs/Compose/core-components.md）；底栏用 Row 自建（2.16.0 未暴露 NavigationBar）
 */
@Page(name = "Main", moduleId = "feature_home")
internal class MainPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var tab by remember { mutableStateOf(MainTab.Home) }
            Scaffold(
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MainTab.entries.forEach { item ->
                            val selected = tab == item
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tab = item }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            if (selected) Color(0xFF1565C0) else Color(0xFFBDBDBD),
                                        ),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 12.sp,
                                    color = if (selected) Color(0xFF1565C0) else Color.Gray,
                                )
                            }
                        }
                    }
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF7F7F7)),
                ) {
                    when (tab) {
                        MainTab.Home -> HomeTab()
                        MainTab.Feed -> FeedTab()
                        MainTab.Lab -> LabTab()
                        MainTab.Me -> MeTab()
                    }
                }
            }
        }
    }
}

private enum class MainTab(val label: String) {
    Home("首页"),
    Feed("动态"),
    Lab("实验室"),
    Me("我的"),
}

@Composable
private fun HomeTab() {
    val user = AuthSession.repo.currentUser()
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("首页", fontSize = 24.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (user != null) "你好，${user.name}" else "未登录",
            fontSize = 16.sp,
            color = Color.Gray,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Demo Skeleton · Compose Track",
            fontSize = 14.sp,
            color = Color(0xFF1565C0),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "底部四个主 Tab：首页 / 动态 / 实验室 / 我的",
            fontSize = 14.sp,
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun FeedTab() {
    var items by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var booted by remember { mutableStateOf(false) }

    fun reload() {
        FeedStore.repo.list()
            .onSuccess {
                items = it
                error = null
            }
            .onFailure {
                items = emptyList()
                error = it.message
            }
    }

    if (!booted) {
        booted = true
        if (AuthSession.repo.currentUser() == null) {
            error = "请先登录"
        } else {
            reload()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("动态", fontSize = 22.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "[切换 Mock] ${MockBackend.scenario}",
            color = Color(0xFF1565C0),
            modifier = Modifier.clickable {
                MockBackend.cycle()
                if (AuthSession.repo.currentUser() != null) reload()
            }.padding(vertical = 8.dp),
        )
        Text(
            text = "打开完整 FeedList →",
            color = Color(0xFF1565C0),
            modifier = Modifier.clickable {
                Utils.currentBridgeModule().openPage(PageNames.FeedList)
            }.padding(vertical = 8.dp),
        )
        Spacer(Modifier.height(8.dp))
        when {
            error != null -> Text("Error: $error", color = Color.Red)
            items.isEmpty() -> Text("Empty", color = Color.Gray)
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(12.dp),
                    ) {
                        Text(item.title, fontSize = 16.sp, color = Color.Black)
                        Text(item.body, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun LabTab() {
    val labs = listOf(
        "Compose Anim" to PageNames.ComposeAnim,
        "Compose List" to PageNames.ComposeList,
        "Compose Pager" to PageNames.ComposePager,
        "Gallery" to PageNames.Gallery,
        "Perf Lab" to PageNames.PerfLab,
        "Legacy DSL Lab" to PageNames.DslLab,
        "Demo Map (旧 Home)" to PageNames.Home,
    )
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("实验室", fontSize = 22.sp, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        labs.forEach { (label, page) ->
            Text(
                text = "→ $label",
                fontSize = 17.sp,
                color = Color(0xFF1565C0),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { Utils.currentBridgeModule().openPage(page) }
                    .padding(vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun MeTab() {
    val user = AuthSession.repo.currentUser()
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text("我的", fontSize = 22.sp, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "用户：${user?.name ?: "未登录"}",
            fontSize = 16.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Permission Demo →",
            color = Color(0xFF1565C0),
            modifier = Modifier.clickable {
                Utils.currentBridgeModule().openPage(PageNames.PermissionDemo)
            }.padding(vertical = 10.dp),
        )
        Text(
            text = "Share Demo →",
            color = Color(0xFF1565C0),
            modifier = Modifier.clickable {
                Utils.currentBridgeModule().openPage(PageNames.ShareDemo)
            }.padding(vertical = 10.dp),
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                AuthSession.repo.logout()
                Utils.currentBridgeModule().openPage(PageNames.Login, closeCurPage = true)
            },
        ) {
            Text("退出登录")
        }
    }
}
