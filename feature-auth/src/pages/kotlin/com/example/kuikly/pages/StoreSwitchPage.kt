package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.design.KuiklyTheme
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
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.example.kuikly.data.auth.StoreItem
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 门店体系：多门店切换（源 `CurrentStoreService` + `SwitchStoreDialog`）。
 * 拉取可切换列表、标记当前门店、切换后回写会话与持久化（源 `applySelection`）。
 */
@Page("StoreSwitch")
internal class StoreSwitchPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            val currentUser = AuthSession.repo.currentUser()
            val currentName = currentUser?.storeName ?: ""
            var stores by remember { mutableStateOf(AuthSession.repo.stores().getOrDefault(emptyList())) }
            var error by remember { mutableStateOf<String?>(null) }
            var info by remember { mutableStateOf<String?>(null) }

            fun switch(item: StoreItem) {
                val res = AuthSession.repo.switchStore(item.storeId)
                if (res.isSuccess) {
                    error = null
                    info = "已切换至 ${item.storeName}"
                    stores = stores.map { it.copy(isCurrent = it.storeId == item.storeId) }
                    Utils.currentBridgeModule().toast("已切换至 ${item.storeName}")
                } else {
                    info = null
                    error = res.exceptionOrNull()?.message ?: "切换失败"
                }
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AuthNavBar(title = "切换门店", subtitle = "可切换多个门店查看数据")
                Spacer(Modifier.height(12.dp))
                AuthHeadline(
                    title = "选择门店",
                    desc = if (currentName.isNotEmpty()) "当前门店：$currentName" else "尚未登录，切换后将以该门店身份继续",
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    if (stores.isEmpty()) {
                        Text("暂无可切换的门店", fontSize = 14.sp, color = KuiklyTheme.muted)
                    } else {
                        stores.forEachIndexed { index, item ->
                            if (index > 0) Spacer(Modifier.height(10.dp))
                            StoreTile(
                                name = item.storeName,
                                roleLabel = item.roleLabel,
                                selected = item.isCurrent || item.storeName == currentName,
                                onTap = { switch(item) },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    if (error != null) {
                        AuthErrorText(message = error!!)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (info != null) {
                        AuthInfoText(message = info!!)
                        Spacer(Modifier.height(10.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** 单个门店选项（源 `SwitchStoreDialog._StoreOptionTile`）。 */
@Composable
private fun StoreTile(name: String, roleLabel: String, selected: Boolean, onTap: () -> Unit) {
    val fg = if (selected) KuiklyTheme.linkDeep else KuiklyTheme.ink
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusLg))
            .clickable { onTap() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = fg)
                if (roleLabel.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(roleLabel, fontSize = 12.sp, color = KuiklyTheme.muted)
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier.background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusPill))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("当前", fontSize = 12.sp, color = KuiklyTheme.linkDeep, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
