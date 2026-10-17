package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.wallet.BankCard
import com.example.kuikly.data.wallet.WalletChannel
import com.example.kuikly.data.wallet.WalletStore
import com.example.kuikly.data.wallet.fenLabel
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 充值页 — port of source wallet_page.dart 充值区：
 * 余额 + 金额输入（0.01–50000）+ 快捷金额 + 渠道（支付宝/微信/银行卡）
 * + 银行卡渠道选卡 + 确认充值，成功后刷新余额。
 */
@Page("WalletRecharge")
internal class WalletRechargePage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        setContent {
            var balance by remember { mutableStateOf("¥0.00") }
            var cards by remember { mutableStateOf<List<BankCard>>(emptyList()) }
            var amountFen by remember { mutableStateOf(0) }
            var channel by remember { mutableStateOf(WalletChannel.ALIPAY) }
            var selectedCardId by remember { mutableStateOf<Int?>(null) }
            var acting by remember { mutableStateOf(false) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                WalletStore.repo.summary()
                    .onSuccess { s ->
                        balance = s.balance
                        cards = s.cards
                        // port of source refreshAll：默认选中默认卡，否则第一张
                        val current = selectedCardId
                        if (current == null || s.cards.none { it.cardId == current }) {
                            val def = s.cards.firstOrNull { it.isDefault } ?: s.cards.firstOrNull()
                            selectedCardId = def?.cardId
                        }
                    }
                    .onFailure {
                        Utils.currentBridgeModule().toast(it.message ?: "加载失败")
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AppNavBarBar(
                    title = "充值",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = KuiklyTheme.canvasSoft2,
                    foreground = KuiklyTheme.foreground,
                )

                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    item {
                        KCard {
                            Text("当前余额（元）", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(4.dp))
                            Text(balance, fontSize = 30.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        KCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("充值金额", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(8.dp))
                                Text("金额 0.01–50000", fontSize = 11.sp, color = KuiklyTheme.muted)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(fenLabel(amountFen), fontSize = 32.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                listOf(1000, 5000, 10000).forEachIndexed { index, fen ->
                                    KChip(
                                        text = "¥${fen / 100}",
                                        color = if (amountFen == fen) KuiklyTheme.link else KuiklyTheme.muted,
                                        onClick = { amountFen = fen },
                                    )
                                    if (index < 2) Spacer(Modifier.width(8.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            WalletNumberKeypad(
                                onDigit = { d ->
                                    if (amountFen <= 99_999_999) amountFen = amountFen * 10 + d.toInt()
                                },
                                onBackspace = { amountFen = amountFen / 10 },
                                onClear = { amountFen = 0 },
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        KCard {
                            Text("充值方式", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                listOf(WalletChannel.ALIPAY, WalletChannel.WECHAT, WalletChannel.BANK_CARD)
                                    .forEachIndexed { index, ch ->
                                        KChip(
                                            text = WalletChannel.label(ch),
                                            color = if (channel == ch) KuiklyTheme.link else KuiklyTheme.muted,
                                            onClick = { channel = ch },
                                        )
                                        if (index < 2) Spacer(Modifier.width(8.dp))
                                    }
                            }
                            if (channel == WalletChannel.BANK_CARD) {
                                Spacer(Modifier.height(10.dp))
                                if (cards.isEmpty()) {
                                    Text("暂无银行卡，请先到「银行卡」页绑定", fontSize = 12.sp, color = KuiklyTheme.muted)
                                } else {
                                    cards.forEach { c ->
                                        val id = c.cardId
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .clickable { selectedCardId = id }
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                if (selectedCardId == id) "●" else "○",
                                                fontSize = 14.sp,
                                                color = if (selectedCardId == id) KuiklyTheme.link else KuiklyTheme.muted,
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(c.display, fontSize = 14.sp, color = KuiklyTheme.foreground)
                                            Spacer(Modifier.width(8.dp))
                                            if (c.isDefault) KChip("默认", color = KuiklyTheme.link)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        KButton(
                            text = if (acting) "充值中…" else "确认充值",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !acting && amountFen > 0,
                            onClick = {
                                val fen = amountFen
                                val ch = channel
                                val cardId = if (ch == WalletChannel.BANK_CARD) selectedCardId else null
                                acting = true
                                WalletStore.repo.recharge(fen, ch, cardId)
                                    .onSuccess {
                                        acting = false
                                        amountFen = 0
                                        balance = it.balance
                                        Utils.currentBridgeModule().toast("充值成功")
                                    }
                                    .onFailure {
                                        acting = false
                                        Utils.currentBridgeModule().toast(it.message ?: "充值失败")
                                    }
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    item {
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
