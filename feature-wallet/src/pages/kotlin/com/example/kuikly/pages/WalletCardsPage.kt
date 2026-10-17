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
import com.example.kuikly.data.wallet.WalletStore
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
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

private val WALLET_BANK_CANDIDATES = listOf(
    "招商银行", "工商银行", "建设银行", "农业银行", "中国银行", "交通银行",
)

/**
 * 银行卡页 — port of source wallet_page.dart 银行卡区：
 * 卡片列表（默认标识 / 设默认 / 删除）+ 绑定表单（银行名 + 卡号后四位）。
 */
@Page("WalletCards")
internal class WalletCardsPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        setContent {
            var cards by remember { mutableStateOf<List<BankCard>>(emptyList()) }
            var bankText by remember { mutableStateOf("") }
            var last4 by remember { mutableStateOf("") }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                WalletStore.repo.cards()
                    .onSuccess { cards = it }
                    .onFailure {
                        cards = emptyList()
                        Utils.currentBridgeModule().toast(it.message ?: "加载失败")
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                AppNavBarBar(
                    title = "银行卡",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                    background = KuiklyTheme.canvasSoft2,
                    foreground = KuiklyTheme.foreground,
                )

                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    item {
                        Text("我的银行卡", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                    }

                    if (cards.isEmpty()) {
                        item {
                            Text("暂无银行卡", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        items(cards) { card ->
                            WalletCardRow(
                                card = card,
                                onSetDefault = {
                                    val id = card.cardId
                                    WalletStore.repo.setDefaultCard(id)
                                        .onSuccess {
                                            Utils.currentBridgeModule().toast("已设为默认卡")
                                            reload()
                                        }
                                        .onFailure { Utils.currentBridgeModule().toast(it.message ?: "设置失败") }
                                },
                                onDelete = {
                                    val id = card.cardId
                                    WalletStore.repo.deleteCard(id)
                                        .onSuccess {
                                            Utils.currentBridgeModule().toast("已删除")
                                            reload()
                                        }
                                        .onFailure { Utils.currentBridgeModule().toast(it.message ?: "删除失败") }
                                },
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    item {
                        Spacer(Modifier.height(10.dp))
                        Text("绑定银行卡", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        KCard {
                            Text("银行名称", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    if (bankText.isEmpty()) "请填写银行名称" else bankText,
                                    fontSize = 15.sp,
                                    color = if (bankText.isEmpty()) KuiklyTheme.muted else KuiklyTheme.foreground,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            WalletBankNameInput(
                                candidates = WALLET_BANK_CANDIDATES,
                                onAppend = { bankText = bankText + it },
                                onBackspace = { bankText = bankText.dropLast(1) },
                                onClear = { bankText = "" },
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("卡号后四位", fontSize = 13.sp, color = KuiklyTheme.muted)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusMd))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    if (last4.isEmpty()) "4 位数字" else last4,
                                    fontSize = 15.sp,
                                    color = if (last4.isEmpty()) KuiklyTheme.muted else KuiklyTheme.foreground,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            WalletNumberKeypad(
                                onDigit = { d -> if (last4.length < 4) last4 = last4 + d },
                                onBackspace = { last4 = last4.dropLast(1) },
                                onClear = { last4 = "" },
                            )
                            Spacer(Modifier.height(12.dp))
                            KButton(
                                text = "绑定银行卡",
                                modifier = Modifier.fillMaxWidth(),
                                enabled = bankText.isNotEmpty() && last4.length == 4,
                                onClick = {
                                    val bank = bankText
                                    val last = last4
                                    WalletStore.repo.bindCard(bank, last)
                                        .onSuccess {
                                            bankText = ""
                                            last4 = ""
                                            Utils.currentBridgeModule().toast("绑卡成功")
                                            reload()
                                        }
                                        .onFailure { Utils.currentBridgeModule().toast(it.message ?: "绑卡失败") }
                                },
                            )
                        }
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

@Composable
private fun WalletCardRow(card: BankCard, onSetDefault: () -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(card.display, fontSize = 14.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            if (card.isDefault) KChip("默认", color = KuiklyTheme.link)
        }
        if (card.holderName.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text("持卡人 ${card.holderName}", fontSize = 11.sp, color = KuiklyTheme.muted)
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!card.isDefault) {
                Text(
                    "设默认",
                    fontSize = 13.sp,
                    color = KuiklyTheme.link,
                    modifier = Modifier.clickable { onSetDefault() },
                )
                Spacer(Modifier.width(16.dp))
            }
            Text(
                "删除",
                fontSize = 13.sp,
                color = KuiklyTheme.error,
                modifier = Modifier.clickable { onDelete() },
            )
        }
    }
}
