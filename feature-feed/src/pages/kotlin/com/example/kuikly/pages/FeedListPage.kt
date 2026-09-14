package com.example.kuikly.pages

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
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("FeedList")
internal class FeedListPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var scenario by remember { mutableStateOf(MockBackend.scenario) }
            var items by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var empty by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                scenario = MockBackend.scenario
                FeedStore.repo.list()
                    .onSuccess { list ->
                        items = list
                        empty = list.isEmpty()
                        error = null
                    }
                    .onFailure {
                        items = emptyList()
                        empty = false
                        error = it.message
                    }
            }

            if (!booted) {
                booted = true
                if (AuthSession.repo.currentUser() == null) {
                    error = "Not logged in — open Login page"
                } else {
                    reload()
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scenario = MockBackend.cycle()
                            if (AuthSession.repo.currentUser() != null) reload()
                        }
                        .padding(vertical = 20.dp),
                ) {
                    Text(
                        text = "[CYCLE SCENARIO] now=$scenario",
                        fontSize = 18.sp,
                        color = Color(0xFF1565C0),
                    )
                }
                Text("Feed — ${AuthSession.repo.currentUser()?.name ?: "?"}", fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                if (AuthSession.repo.currentUser() == null) {
                    Text(
                        text = "Go to Login",
                        color = Color(0xFF1565C0),
                        modifier = Modifier.clickable {
                            Utils.currentBridgeModule().openPage("Login", closeCurPage = true)
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                }
                when {
                    error != null -> Text("Error: $error", color = Color.Red, fontSize = 18.sp)
                    empty -> Text("Empty list", color = Color.Gray, fontSize = 18.sp)
                    else -> items.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val data = JSONObject().apply { put("id", item.id) }
                                    Utils.currentBridgeModule().openPage(url = "FeedDetail", userData = data)
                                }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(item.title, fontSize = 16.sp, color = Color.Black)
                            Text(item.body, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
