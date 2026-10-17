package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.wallet.LedgerEntry
import com.example.kuikly.data.wallet.WalletStore
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

@Page("Wallet")
internal class WalletPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var balance by remember { mutableStateOf(WalletStore.repo.balanceLabel()) }
            var ledger by remember { mutableStateOf<List<LedgerEntry>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                balance = WalletStore.repo.balanceLabel()
                WalletStore.repo.page(1, 50)
                    .onSuccess { ledger = it.items; error = null }
                    .onFailure { error = it.message }
            }
            if (!booted) { booted = true; reload() }

            fun open(name: String) {
                Utils.currentBridgeModule().openPage(url = name, userData = JSONObject())
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("我的钱包", fontSize = 22.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                KCard {
                    Text("账户余额（元）", fontSize = 13.sp, color = KuiklyTheme.muted)
                    Spacer(Modifier.height(4.dp))
                    Text(balance, fontSize = 30.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KButton(text = "充值", modifier = Modifier.width(90.dp), onClick = { open("WalletRecharge") })
                        Spacer(Modifier.width(10.dp))
                        KButton(text = "银行卡", modifier = Modifier.width(90.dp), onClick = { open("WalletCards") })
                        Spacer(Modifier.width(10.dp))
                        KButton(text = "流水", modifier = Modifier.width(90.dp), onClick = { open("WalletLedger") })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("最近流水", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                    Text(
                        "查看全部 ›",
                        fontSize = 13.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { open("WalletLedger") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (error != null) Text("Error: $error", color = Color.Red, fontSize = 14.sp)
                if (ledger.isEmpty() && error == null) {
                    Text("暂无交易记录", fontSize = 13.sp, color = KuiklyTheme.muted)
                }
                ledger.take(3).forEach { e ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(e.reasonLabel, fontSize = 14.sp, color = KuiklyTheme.foreground)
                            Text(e.balanceLabel, fontSize = 11.sp, color = KuiklyTheme.muted)
                        }
                        Text(
                            e.deltaYuan,
                            fontSize = 14.sp,
                            color = if (e.deltaFen < 0) KuiklyTheme.error else KuiklyTheme.success,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
