package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.wallet.WalletStore
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("Wallet")
internal class WalletPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var balance by remember { mutableStateOf(WalletStore.repo.balanceLabel()) }
            var ledger by remember { mutableStateOf<List<com.example.kuikly.data.wallet.LedgerEntry>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                WalletStore.repo.page(1, 50).onSuccess { ledger = it.items }.onFailure { error = it.message }
            }
            if (!booted) { booted = true; reload() }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("我的钱包", fontSize = 22.sp, color = KuiklyTheme.foreground, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Bold)
                Text("余额 $balance", fontSize = 26.sp, color = KuiklyTheme.link, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("＋ 充值 ¥100", fontSize = 14.sp, color = KuiklyTheme.onPrimary, modifier = Modifier.clickable {
                    WalletStore.repo.recharge(10000)
                    balance = WalletStore.repo.balanceLabel()
                    reload()
                }.background(KuiklyTheme.primary, com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    "查看流水与银行卡 ›",
                    fontSize = 13.sp,
                    color = KuiklyTheme.link,
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().openPage(
                            url = "WalletLedger",
                            userData = com.tencent.kuikly.core.nvi.serialization.json.JSONObject(),
                        )
                    },
                )
                Spacer(Modifier.height(12.dp))
                if (error != null) Text("Error: $error", color = Color.Red, fontSize = 14.sp)
                ledger.forEach { e ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("${e.reason}  ${e.deltaYuan}", fontSize = 14.sp, color = KuiklyTheme.foreground)
                        Text("余额 ¥${e.balanceFen / 100}.${(e.balanceFen % 100)}", fontSize = 11.sp, color = KuiklyTheme.muted)
                    }
                }
            }
        }
    }
}
