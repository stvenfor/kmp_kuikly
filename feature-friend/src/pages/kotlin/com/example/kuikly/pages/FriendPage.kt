package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.data.friend.FriendStore
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.design.KuiklyTheme
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

@Page("Friend")
internal class FriendPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var list by remember { mutableStateOf<List<com.example.kuikly.data.friend.Friend>>(emptyList()) }
            var groups by remember { mutableStateOf<List<com.example.kuikly.data.friend.FriendGroup>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                FriendStore.repo.page(1, 50).onSuccess { list = it.items }.onFailure { error = it.message }
                FriendStore.repo.groups().onSuccess { groups = it }
            }
            if (!booted) { booted = true; reload() }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("好友", fontSize = 22.sp, color = KuiklyTheme.foreground, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (error != null) Text("Error: $error", color = Color.Red, fontSize = 14.sp)
                groups.forEach { g ->
                    Text(g.name, fontSize = 13.sp, color = KuiklyTheme.muted, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Medium)
                    list.filter { it.groupId == g.id }.forEach { f ->
                        Text("  ${f.name} ${if (f.isOnline) "●" else "○"}", fontSize = 15.sp, color = KuiklyTheme.foreground,
                            modifier = Modifier.fillMaxWidth().clickable { }.padding(vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("[CYCLE MOCK] $MockBackend.scenario", fontSize = 12.sp, color = KuiklyTheme.muted,
                    modifier = Modifier.clickable { MockBackend.cycle(); reload() })
            }
        }
    }
}
