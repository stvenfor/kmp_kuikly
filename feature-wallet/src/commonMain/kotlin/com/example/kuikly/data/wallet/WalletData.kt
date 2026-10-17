package com.example.kuikly.data.wallet

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** 充值渠道（port of source recharge channel）：1 支付宝 2 微信 3 银行卡。 */
object WalletChannel {
    const val ALIPAY = 1
    const val WECHAT = 2
    const val BANK_CARD = 3

    fun label(channel: Int): String = when (channel) {
        ALIPAY -> "支付宝"
        WECHAT -> "微信"
        BANK_CARD -> "银行卡"
        else -> "未知渠道"
    }
}

/** 金额（分）格式化：123456 -> ¥1234.56。 */
fun fenLabel(fen: Int): String = "¥${fen / 100}.${(fen % 100).toString().padStart(2, '0')}"

/** 单次充值上限（分）：50000 元，与源页面提示「金额 0.01–50000」一致。 */
private const val MAX_RECHARGE_FEN = 5_000_000

/** 钱包摘要（port of source WalletSummary）。 */
data class WalletSummary(
    val balance: String,
    val balanceFen: Int,
    val cards: List<BankCard>,
)

/** 充值结果（port of source WalletRechargeResult）。金额单位：分。 */
data class WalletRechargeResult(
    val balance: String,
    val balanceFen: Int,
    val deltaFen: Int,
)

/** 银行卡（port of source BankCard）。 */
data class BankCard(
    val cardId: Int,
    val bankName: String,
    val cardLast4: String,
    val holderName: String = "",
    val isDefault: Boolean = false,
) {
    val display: String get() = "$bankName(****$cardLast4)"
}

/** 钱包流水（port of source WalletLedgerEntry）。金额单位：分。 */
data class LedgerEntry(
    val ledgerId: Int,
    val deltaFen: Int,
    val balanceFen: Int,
    val reason: String,
    val refId: String = "",
) {
    val deltaYuan: String get() {
        val neg = deltaFen < 0
        val v = kotlin.math.abs(deltaFen)
        return (if (neg) "-" else "+") + fenLabel(v)
    }

    /** port of source reasonLabel：reason 码转中文标签。 */
    val reasonLabel: String get() = when (reason) {
        "recharge" -> "充值"
        "mall_pay" -> "商城支付"
        "mall_refund" -> "商城退款入账"
        else -> reason
    }

    val isIncome: Boolean get() = deltaFen >= 0

    val balanceLabel: String get() = fenLabel(balanceFen)
}

class WalletRepository : BaseMockRepository<LedgerEntry>() {
    private var balanceFen = 123_456 // ¥1234.56
    private val cards = mutableListOf(
        BankCard(1, "招商银行", "8888", "张三", true),
        BankCard(2, "工商银行", "1234", "张三", false),
    )

    /** 覆盖收支两类：recharge 收入 / mall_pay 支出 / mall_refund 退款入账。balanceFen 为最新一条后的余额。 */
    private val ledger = mutableListOf(
        LedgerEntry(8, -3200, 123456, "mall_pay", "OD20240510"),
        LedgerEntry(7, 1500, 126656, "mall_refund", "MR20240509"),
        LedgerEntry(6, 20000, 125156, "recharge", "RC20240508"),
        LedgerEntry(5, -8800, 105156, "mall_pay", "OD20240507"),
        LedgerEntry(4, -5000, 113956, "mall_pay", "OD20240506"),
        LedgerEntry(3, 50000, 118956, "recharge", "RC20240505"),
        LedgerEntry(2, -9900, 68956, "mall_pay", "OD20240504"),
        LedgerEntry(1, 30000, 78856, "recharge", "RC20240503"),
    )

    override fun seed(): List<LedgerEntry> = ledger.toList()
    override fun idOf(item: LedgerEntry): String = item.ledgerId.toString()

    private fun errorState(): Boolean = MockBackend.scenario == MockScenario.Error

    fun balanceFen(): Int = balanceFen
    fun balanceLabel(): String = fenLabel(balanceFen)

    /** 钱包摘要：余额 + 银行卡（port of source fetchSummary）。 */
    fun summary(): Result<WalletSummary> =
        if (errorState()) Result.failure(IllegalStateException("wallet error"))
        else Result.success(WalletSummary(balanceLabel(), balanceFen, cards.toList()))

    fun cards(): Result<List<BankCard>> =
        if (errorState()) Result.failure(IllegalStateException("wallet error"))
        else Result.success(cards.toList())

    /**
     * 充值（分）。channel 见 [WalletChannel]；银行卡渠道必须传 cardId。
     * port of source recharge(amount, channel, cardId)。
     */
    fun recharge(
        fen: Int,
        channel: Int = WalletChannel.ALIPAY,
        cardId: Int? = null,
    ): Result<WalletRechargeResult> {
        if (fen <= 0) return Result.failure(IllegalArgumentException("请输入正确的充值金额"))
        if (fen > MAX_RECHARGE_FEN) return Result.failure(IllegalArgumentException("单次充值不能超过 50000 元"))
        if (channel != WalletChannel.ALIPAY &&
            channel != WalletChannel.WECHAT &&
            channel != WalletChannel.BANK_CARD
        ) {
            return Result.failure(IllegalArgumentException("请选择充值渠道"))
        }
        if (channel == WalletChannel.BANK_CARD) {
            if (cardId == null) return Result.failure(IllegalArgumentException("请选择银行卡"))
            if (cards.none { it.cardId == cardId }) return Result.failure(IllegalArgumentException("银行卡不存在"))
        }
        if (errorState()) return Result.failure(IllegalStateException("wallet error"))
        balanceFen += fen
        ledger.add(0, LedgerEntry(nextLedgerId(), fen, balanceFen, "recharge", "RC${System.currentTimeMillis()}"))
        return Result.success(WalletRechargeResult(balanceLabel(), balanceFen, fen))
    }

    /** 绑定银行卡（port of source bindCard）。 */
    fun bindCard(
        bankName: String,
        cardLast4: String,
        holderName: String = "",
        isDefault: Boolean = false,
    ): Result<BankCard> {
        val bank = bankName.trim()
        val last4 = cardLast4.trim()
        if (bank.isEmpty()) return Result.failure(IllegalArgumentException("请填写银行名称"))
        if (last4.length != 4 || !last4.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("卡号后四位需为 4 位数字"))
        }
        if (errorState()) return Result.failure(IllegalStateException("wallet error"))
        if (isDefault) {
            cards.forEachIndexed { i, c -> cards[i] = c.copy(isDefault = false) }
        }
        val card = BankCard((cards.maxOfOrNull { it.cardId } ?: 0) + 1, bank, last4, holderName.trim(), isDefault)
        cards.add(card)
        return Result.success(card)
    }

    /** 设置默认卡（port of source setDefaultCard）。 */
    fun setDefaultCard(cardId: Int): Result<Unit> {
        if (errorState()) return Result.failure(IllegalStateException("wallet error"))
        if (cards.none { it.cardId == cardId }) return Result.failure(IllegalArgumentException("银行卡不存在"))
        cards.forEachIndexed { i, c -> cards[i] = c.copy(isDefault = c.cardId == cardId) }
        return Result.success(Unit)
    }

    /** 删除银行卡（port of source deleteCard）。 */
    fun deleteCard(cardId: Int): Result<Unit> {
        if (errorState()) return Result.failure(IllegalStateException("wallet error"))
        if (!cards.removeAll { it.cardId == cardId }) return Result.failure(IllegalArgumentException("银行卡不存在"))
        return Result.success(Unit)
    }

    private fun nextLedgerId(): Int = (ledger.maxOfOrNull { it.ledgerId } ?: 0) + 1
}

object WalletStore {
    val repo: WalletRepository = WalletRepository()
}
