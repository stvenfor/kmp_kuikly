package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.wallet.BankCard
import com.example.kuikly.data.wallet.LedgerEntry
import com.example.kuikly.data.wallet.WalletStore
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
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
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 钱包流水页：余额卡 + 充值 + 收支明细 + 银行卡。
 */
@Page("WalletLedger")
internal class WalletLedgerPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var balance by remember { mutableStateOf("") }
            var items by remember { mutableStateOf<List<LedgerEntry>>(emptyList()) }
            var cards by remember { mutableStateOf<List<BankCard>>(emptyList()) }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                balance = WalletStore.repo.balanceLabel()
                WalletStore.repo.page(1, 50)
                    .onSuccess { items = it.items; error = null }
                    .onFailure { items = emptyList(); error = it.message ?: "加载失败" }
                WalletStore.repo.cards()
                    .onSuccess { cards = it }
                    .onFailure { cards = emptyList() }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("钱包", fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    item {
                        // 余额卡
                        KCard {
                            Text("账户余额", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(4.dp))
                            Text(balance, fontSize = 32.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row {
                                KButton(
                                    text = "充值 ¥100",
                                    modifier = Modifier.width(120.dp),
                                    onClick = {
                                        WalletStore.repo.recharge(10000)
                                        Utils.currentBridgeModule().toast("充值成功")
                                        reload()
                                    },
                                )
                                Spacer(Modifier.width(10.dp))
                                KButton(
                                    text = "充值 ¥50",
                                    modifier = Modifier.width(120.dp),
                                    onClick = {
                                        WalletStore.repo.recharge(5000)
                                        Utils.currentBridgeModule().toast("充值成功")
                                        reload()
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        Text("收支明细", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                    }

                    if (error != null && items.isEmpty()) {
                        item {
                            Text("加载失败：$error", fontSize = 13.sp, color = KuiklyTheme.error)
                            Spacer(Modifier.height(8.dp))
                        }
                    } else if (items.isEmpty()) {
                        item {
                            Text("暂无交易记录", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        items(items) { entry ->
                            LedgerRow(entry)
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    if (cards.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(10.dp))
                            Text("我的银行卡", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            cards.forEach { card ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(card.display, fontSize = 14.sp, color = KuiklyTheme.foreground)
                                    if (card.isDefault) KChip("默认", color = KuiklyTheme.link)
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "[CYCLE MOCK] $MockBackend.scenario",
                            fontSize = 12.sp,
                            color = KuiklyTheme.muted,
                            modifier = Modifier.clickable { MockBackend.cycle(); reload() },
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerRow(entry: LedgerEntry) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(entry.reason, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text("单号 ${entry.refId}", fontSize = 11.sp, color = KuiklyTheme.muted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                entry.deltaYuan,
                fontSize = 15.sp,
                color = if (entry.deltaFen < 0) KuiklyTheme.error else KuiklyTheme.success,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text("余额 ¥${entry.balanceFen / 100}", fontSize = 11.sp, color = KuiklyTheme.muted)
        }
    }
}
