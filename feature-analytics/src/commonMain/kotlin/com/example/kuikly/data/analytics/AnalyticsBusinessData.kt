package com.example.kuikly.data.analytics

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

// -----------------------------------------------------------------------------
// Strategy — port of source features/home/lib/home/view/strategy_page.dart
// -----------------------------------------------------------------------------

/** One cell of the "大类资产九宫格" grid (source _GridCell). */
data class StrategyAssetCell(
    val label: String,
    val valuePct: Double,
    val positive: Boolean,
) {
    val valueLabel: String get() = formatSignedPercent(valuePct, 2)
}

/** A single follow-along strategy plan (source strategy card). */
data class StrategyPlan(
    val id: String,
    val name: String,
    val kind: String,
    val periodLabel: String,
    val returnPct: Double,
    val investedCount: Int,
    val targetCount: Int,
    val weeklyInvested: Int,
    val gaugeScore: Int,
    val summary: String,
) {
    /** 0.0 .. 1.0 follow-along progress. */
    val progress: Double
        get() = if (targetCount <= 0) 0.0 else (investedCount.toDouble() / targetCount.toDouble()).coerceIn(0.0, 1.0)

    val progressLabel: String get() = "$investedCount / $targetCount"

    val returnLabel: String get() = formatSignedPercent(returnPct, 2)

    val positiveReturn: Boolean get() = returnPct >= 0.0

    /** 恐贪指数 gauge bucket, mirrors the source 0..100 gauge. */
    val gaugeLabel: String
        get() = when {
            gaugeScore < 20 -> "极度恐慌"
            gaugeScore < 40 -> "恐慌"
            gaugeScore < 60 -> "中性"
            gaugeScore < 80 -> "贪婪"
            else -> "极度贪婪"
        }
}

class StrategyRepository : BaseMockRepository<StrategyPlan>() {

    private val grid = listOf(
        StrategyAssetCell("A股", 19.22, true),
        StrategyAssetCell("中债", 3.15, true),
        StrategyAssetCell("黄金", 8.76, true),
        StrategyAssetCell("港股", 12.40, true),
        StrategyAssetCell("美股", 15.88, true),
        StrategyAssetCell("原油", -2.34, false),
        StrategyAssetCell("美元债", -1.80, false),
        StrategyAssetCell("商品", 4.56, true),
        StrategyAssetCell("现金", 1.20, true),
    )

    private val plans = listOf(
        StrategyPlan(
            id = "ST-2001", name = "黄金恐贪定投 · 第一期", kind = "逆向", periodLabel = "近1年",
            returnPct = -11.35, investedCount = 36, targetCount = 50, weeklyInvested = 1,
            gaugeScore = 32, summary = "恐慌区间加倍扣款，贪婪区间暂停扣款。",
        ),
        StrategyPlan(
            id = "ST-2002", name = "全球权益均衡 · 第三期", kind = "推荐", periodLabel = "近3月",
            returnPct = 6.42, investedCount = 18, targetCount = 40, weeklyInvested = 2,
            gaugeScore = 58, summary = "股债商三类资产等权再平衡，季度调仓。",
        ),
        StrategyPlan(
            id = "ST-2003", name = "债券 ladder 防守仓", kind = "趋势", periodLabel = "近1月",
            returnPct = 2.08, investedCount = 24, targetCount = 24, weeklyInvested = 0,
            gaugeScore = 71, summary = "久期 1-3 年阶梯，回撤控制优先。",
        ),
        StrategyPlan(
            id = "ST-2004", name = "港股高股息增强", kind = "推荐", periodLabel = "今年来",
            returnPct = 9.77, investedCount = 11, targetCount = 36, weeklyInvested = 1,
            gaugeScore = 44, summary = "股息率加权，剔除连续两年分红下滑标的。",
        ),
        StrategyPlan(
            id = "ST-2005", name = "商品趋势跟踪", kind = "趋势", periodLabel = "近1周",
            returnPct = -3.16, investedCount = 5, targetCount = 30, weeklyInvested = 1,
            gaugeScore = 23, summary = "20 日均线突破开仓，跌破 60 日均线平仓。",
        ),
    )

    override fun seed(): List<StrategyPlan> = plans

    override fun idOf(item: StrategyPlan): String = item.id

    /** The nine-grid asset cells; honours the current mock scenario. */
    fun cells(): Result<List<StrategyAssetCell>> {
        if (MockBackend.scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock strategy error"))
        }
        if (MockBackend.scenario == MockScenario.Empty) {
            return Result.success(emptyList())
        }
        return Result.success(grid)
    }

    /** Filter plans by kind: 推荐 / 逆向 / 趋势. */
    fun plansOfKind(kind: String): Result<List<StrategyPlan>> = all { it.kind == kind }
}

object StrategyStore {
    val repo: StrategyRepository = StrategyRepository()
}

// -----------------------------------------------------------------------------
// Ledger — port of source model/transaction_model.dart + ledger controllers
// -----------------------------------------------------------------------------

/** One ledger / transaction row. Amounts are in yuan (source TransactionModel.amount). */
data class AnalyticsLedgerEntry(
    val id: Int,
    val type: String,
    val category: String,
    val amount: Double,
    val date: String,
    val note: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    val isIncome: Boolean get() = type == "income"

    val signedAmount: Double get() = if (isIncome) amount else -amount

    val amountLabel: String
        get() = (if (isIncome) "+" else "-") + formatDecimal(amount, 2)

    val typeLabel: String get() = if (isIncome) "收入" else "支出"
}

/** Aggregated income / expense totals shown in the ledger header card. */
data class LedgerSummary(
    val income: Double,
    val expense: Double,
    val count: Int,
) {
    val balance: Double get() = income - expense

    val incomeLabel: String get() = formatDecimal(income, 2)
    val expenseLabel: String get() = formatDecimal(expense, 2)
    val balanceLabel: String get() = formatDecimal(balance, 2)
}

class AnalyticsLedgerRepository : BaseMockRepository<AnalyticsLedgerEntry>() {

    private val entries = listOf(
        AnalyticsLedgerEntry(1, "expense", "餐饮", 68.50, "2024-05-01", "午餐 · 团队聚餐", 1714521600L, 1714521600L),
        AnalyticsLedgerEntry(2, "income", "工资", 18500.00, "2024-05-05", "四月薪资", 1714867200L, 1714867200L),
        AnalyticsLedgerEntry(3, "expense", "交通", 320.00, "2024-05-06", "高铁往返", 1714953600L, 1714953600L),
        AnalyticsLedgerEntry(4, "expense", "购物", 1299.00, "2024-05-08", "机械键盘", 1715126400L, 1715126400L),
        AnalyticsLedgerEntry(5, "income", "理财", 246.80, "2024-05-10", "货币基金收益", 1715299200L, 1715299200L),
        AnalyticsLedgerEntry(6, "expense", "居住", 3600.00, "2024-05-12", "房租", 1715472000L, 1715472000L),
        AnalyticsLedgerEntry(7, "expense", "娱乐", 158.00, "2024-05-15", "演出门票", 1715731200L, 1715731200L),
        AnalyticsLedgerEntry(8, "income", "报销", 820.00, "2024-05-18", "差旅报销", 1715990400L, 1715990400L),
        AnalyticsLedgerEntry(9, "expense", "医疗", 460.30, "2024-05-20", "体检套餐", 1716163200L, 1716163200L),
        AnalyticsLedgerEntry(10, "expense", "教育", 2980.00, "2024-05-22", "在线课程", 1716336000L, 1716336000L),
        AnalyticsLedgerEntry(11, "income", "兼职", 1500.00, "2024-05-25", "技术咨询", 1716595200L, 1716595200L),
        AnalyticsLedgerEntry(12, "expense", "餐饮", 42.00, "2024-05-27", "早餐 + 咖啡", 1716768000L, 1716768000L),
    )

    override fun seed(): List<AnalyticsLedgerEntry> = entries

    override fun idOf(item: AnalyticsLedgerEntry): String = item.id.toString()

    /** Rows of a single type: "income" or "expense". */
    fun entriesOfType(type: String): Result<List<AnalyticsLedgerEntry>> = all { it.type == type }

    /** Header totals for the currently visible rows. */
    fun summary(): Result<LedgerSummary> {
        val rows = all().getOrNull() ?: return Result.failure(IllegalStateException("mock ledger error"))
        var income = 0.0
        var expense = 0.0
        rows.forEach { entry ->
            if (entry.isIncome) income += entry.amount else expense += entry.amount
        }
        return Result.success(LedgerSummary(income = income, expense = expense, count = rows.size))
    }
}

object AnalyticsLedgerStore {
    val repo: AnalyticsLedgerRepository = AnalyticsLedgerRepository()
}

// -----------------------------------------------------------------------------
// Used car order — port of source model/used_car_order_models.dart
// -----------------------------------------------------------------------------

/** Review counters of the used-car order inbox (source UsedCarOrderStats). */
data class UsedCarOrderStats(
    val submitted: Int,
    val pendingReview: Int,
    val approved: Int,
    val rejected: Int,
)

/** A used-car business order (source UsedCarOrderItem). */
data class UsedCarOrder(
    val orderId: String,
    val kind: String,
    val kindLabel: String,
    val amountLabel: String,
    val customerName: String,
    val phone: String,
    val vehicleModel: String,
    val plateNo: String,
    val vin: String,
    val mileageKm: Int,
    val modelYear: Int,
    val amount: Double,
    val status: String,
    val submittedAt: Long,
    val rejectReason: String = "",
    val ratingStars: Int = 0,
) {
    val statusLabel: String
        get() = when (status) {
            "pending_review" -> "待审核"
            "approved_pending_rating" -> "已通过待评价"
            "rated" -> "已通过已评价"
            "rejected" -> "未通过"
            else -> status
        }

    val mileageLabel: String get() = "$mileageKm km"

    val modelYearLabel: String get() = "${modelYear}款"

    val amountYuanLabel: String get() = formatDecimal(amount, 2)

    val isRejected: Boolean get() = status == "rejected"
}

class UsedCarOrderRepository : BaseMockRepository<UsedCarOrder>() {

    private val orders = listOf(
        UsedCarOrder(
            orderId = "UC-3001", kind = "trade_in", kindLabel = "置换", amountLabel = "置换价",
            customerName = "陈默", phone = "138****2210", vehicleModel = "大众 帕萨特 330TSI",
            plateNo = "沪A·7Q2L8", vin = "LSVAA1234N2001876", mileageKm = 48000, modelYear = 2020,
            amount = 118000.0, status = "pending_review", submittedAt = 1714521600L,
        ),
        UsedCarOrder(
            orderId = "UC-3002", kind = "consign", kindLabel = "专卖", amountLabel = "挂牌价",
            customerName = "林可", phone = "139****8842", vehicleModel = "丰田 凯美瑞 2.5G",
            plateNo = "沪C·9K1M3", vin = "LVGBH51KXNG045211", mileageKm = 32500, modelYear = 2021,
            amount = 152000.0, status = "rated", submittedAt = 1714867200L, ratingStars = 5,
        ),
        UsedCarOrder(
            orderId = "UC-3003", kind = "purchase", kindLabel = "收车", amountLabel = "收购价",
            customerName = "赵晗", phone = "137****5517", vehicleModel = "宝马 3系 325Li",
            plateNo = "苏E·2B8N6", vin = "WBA8E9G51GNU12345", mileageKm = 61200, modelYear = 2019,
            amount = 176500.0, status = "approved_pending_rating", submittedAt = 1715126400L,
        ),
        UsedCarOrder(
            orderId = "UC-3004", kind = "trade_in", kindLabel = "置换", amountLabel = "置换价",
            customerName = "孙宁", phone = "136****3309", vehicleModel = "本田 雅阁 260TURBO",
            plateNo = "浙B·5T4R1", vin = "LHGCV1651N8002341", mileageKm = 89700, modelYear = 2018,
            amount = 86000.0, status = "rejected", submittedAt = 1715472000L,
            rejectReason = "里程数与保养记录不一致，需重新核验",
        ),
        UsedCarOrder(
            orderId = "UC-3005", kind = "consign", kindLabel = "专卖", amountLabel = "挂牌价",
            customerName = "周妍", phone = "135****7728", vehicleModel = "特斯拉 Model 3 后驱版",
            plateNo = "沪A·D12F34", vin = "LRW3E7EA5NC009821", mileageKm = 21300, modelYear = 2022,
            amount = 168000.0, status = "pending_review", submittedAt = 1715731200L,
        ),
        UsedCarOrder(
            orderId = "UC-3006", kind = "purchase", kindLabel = "收车", amountLabel = "收购价",
            customerName = "吴斌", phone = "134****9960", vehicleModel = "奥迪 A4L 40TFSI",
            plateNo = "京N·8H6P2", vin = "WAUZZZ8K5NA043218", mileageKm = 55600, modelYear = 2020,
            amount = 149000.0, status = "rated", submittedAt = 1715990400L, ratingStars = 4,
        ),
    )

    override fun seed(): List<UsedCarOrder> = orders

    override fun idOf(item: UsedCarOrder): String = item.orderId

    /** Inbox counters derived from the seeded orders. */
    fun stats(): Result<UsedCarOrderStats> {
        val rows = all().getOrNull() ?: return Result.failure(IllegalStateException("mock used car error"))
        return Result.success(
            UsedCarOrderStats(
                submitted = rows.size,
                pendingReview = rows.count { it.status == "pending_review" },
                approved = rows.count { it.status == "approved_pending_rating" || it.status == "rated" },
                rejected = rows.count { it.isRejected },
            )
        )
    }

    /** Filter by a UsedCarStatusTab api status: all / pending_review / approved / rejected. */
    fun ordersOfStatus(status: String): Result<List<UsedCarOrder>> {
        if (status == "all" || status.isEmpty()) return all()
        return all { order ->
            when (status) {
                "pending_review" -> order.status == "pending_review"
                "approved" -> order.status == "approved_pending_rating" || order.status == "rated"
                "rejected" -> order.isRejected
                else -> order.status == status
            }
        }
    }
}

object UsedCarOrderStore {
    val repo: UsedCarOrderRepository = UsedCarOrderRepository()
}
