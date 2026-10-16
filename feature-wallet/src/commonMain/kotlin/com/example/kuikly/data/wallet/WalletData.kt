package com.example.kuikly.data.wallet

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** 银行卡（port of source BankCard）。 */
data class BankCard(
    val cardId: Int,
    val bankName: String,
    val cardLast4: String,
    val holderName: String,
    val isDefault: Boolean,
) {
    val display: String get() = "$bankName(****$cardLast4)"
}

/** 钱包流水（port of source WalletLedgerEntry）。金额单位：分。 */
data class LedgerEntry(
    val ledgerId: Int,
    val deltaFen: Int,
    val balanceFen: Int,
    val reason: String,
    val refId: String,
) {
    val deltaYuan: String get() {
        val neg = deltaFen < 0
        val v = kotlin.math.abs(deltaFen)
        return (if (neg) "-" else "+") + "¥${v / 100}.${(v % 100).toString().padStart(2, '0')}"
    }
}

class WalletRepository : BaseMockRepository<LedgerEntry>() {
    private var balanceFen = 123_456 // ¥1234.56
    private val cards = listOf(
        BankCard(1, "招商银行", "8888", "张三", true),
        BankCard(2, "工商银行", "1234", "张三", false),
    )
    private val ledger = mutableListOf(
        LedgerEntry(1, 5000, 128456, "充值", "RC20240501"),
        LedgerEntry(2, -1000, 127456, "消费-商城", "OD20240502"),
    )

    override fun seed(): List<LedgerEntry> = ledger.toList()
    override fun idOf(item: LedgerEntry): String = item.ledgerId.toString()

    fun balanceFen(): Int = balanceFen
    fun balanceLabel(): String = "¥${balanceFen / 100}.${(balanceFen % 100).toString().padStart(2, '0')}"

    fun cards(): Result<List<BankCard>> =
        if (MockBackend.scenario == MockScenario.Error) Result.failure(IllegalStateException("wallet error"))
        else Result.success(cards)

    /** 充值（分）。 */
    fun recharge(fen: Int): Result<Int> {
        if (fen <= 0) return Result.failure(IllegalArgumentException("amount must be positive"))
        if (MockBackend.scenario == MockScenario.Error) return Result.failure(IllegalStateException("wallet error"))
        balanceFen += fen
        ledger.add(0, LedgerEntry(ledger.size + 1, fen, balanceFen, "充值", "RC${System.currentTimeMillis()}"))
        return Result.success(balanceFen)
    }
}

object WalletStore {
    val repo: WalletRepository = WalletRepository()
}
