package com.example.kuikly.data.analytics

// -----------------------------------------------------------------------------
// Analytics 计算 / 聚合层（纯逻辑，无 UI 依赖）
// 源 Flutter 端这些维度散落在 UI 里（或直接缺失），这里统一抽成可测试的计算函数，
// 供 AnalyticsList / AnalyticsDetail / AnalyticsLedger / AnalyticsUsedCar 复用。
// -----------------------------------------------------------------------------

// ===== 账本（Ledger）分析 =====

/** 单品类汇总：该类目的收支总额、笔数、占绝对总额的份额（0..1，构造时由聚合层计算）。 */
data class LedgerCategoryStat(
    val category: String,
    val total: Double,
    val count: Int,
    val share: Double,
) {
    val shareLabel: String get() = formatDecimal(share * 100.0, 1) + "%"
    val amountLabel: String get() = formatDecimal(total, 2)
}

/** 单月汇总：该月收入 / 支出 / 结余 / 笔数。 */
data class LedgerMonthStat(
    val month: String,
    val income: Double,
    val expense: Double,
    val count: Int,
) {
    val balance: Double get() = income - expense
    val balanceLabel: String get() = formatDecimal(balance, 2)
    val incomeLabel: String get() = formatDecimal(income, 2)
    val expenseLabel: String get() = formatDecimal(expense, 2)
}

/** 账本整体分析结果：汇总 + 品类分布 + 月度走势。 */
data class LedgerAnalysis(
    val income: Double,
    val expense: Double,
    val balance: Double,
    val count: Int,
    val avgTicket: Double,
    val largestExpense: AnalyticsLedgerEntry?,
    val savingsRate: Double,
    val byCategory: List<LedgerCategoryStat>,
    val byMonth: List<LedgerMonthStat>,
) {
    val incomeLabel: String get() = formatDecimal(income, 2)
    val expenseLabel: String get() = formatDecimal(expense, 2)
    val balanceLabel: String get() = formatDecimal(balance, 2)
    val avgTicketLabel: String get() = formatDecimal(avgTicket, 2)
    val savingsRateLabel: String get() = formatDecimal(savingsRate * 100.0, 1) + "%"

    /** 绝对总流动（收入 + 支出），用于份额归一。 */
    val totalAbs: Double get() = income + expense

    /** 按金额绝对值排序的前 [n] 个品类。 */
    fun topCategories(n: Int = 5): List<LedgerCategoryStat> =
        byCategory.sortedByDescending { kotlin.math.abs(it.total) }.take(n)
}

/** 从日期字符串 "YYYY-MM-DD" 取 "YYYY-MM"。 */
val AnalyticsLedgerEntry.monthKey: String
    get() = if (date.length >= 7) date.substring(0, 7) else date

/** 按单一品类过滤（null 表示不过滤）。 */
fun filterLedgerByCategory(entries: List<AnalyticsLedgerEntry>, category: String?): List<AnalyticsLedgerEntry> =
    if (category == null) entries else entries.filter { it.category == category }

/** 按月份过滤（"YYYY-MM"，null 表示不过滤）。 */
fun filterLedgerByMonth(entries: List<AnalyticsLedgerEntry>, month: String?): List<AnalyticsLedgerEntry> =
    if (month == null) entries else entries.filter { it.monthKey == month }

/** 按日期倒序（新 → 旧）。 */
fun sortLedgerByDateDesc(entries: List<AnalyticsLedgerEntry>): List<AnalyticsLedgerEntry> =
    entries.sortedByDescending { it.date }

/**
 * 账本聚合：收入/支出/结余、客单价、最大单笔支出、储蓄率，
 * 以及按品类、按月的维度拆分。这是「账本收支计算 + 维度切换/明细下钻」的核心计算。
 */
fun analyzeLedger(entries: List<AnalyticsLedgerEntry>): LedgerAnalysis {
    var income = 0.0
    var expense = 0.0
    var largestExpense: AnalyticsLedgerEntry? = null
    val categoryMap = LinkedHashMap<String, Pair<Double, Int>>()
    val monthMap = LinkedHashMap<String, Triple<Double, Double, Int>>() // income, expense, count

    entries.forEach { e ->
        val signed = e.signedAmount
        if (e.isIncome) income += e.amount else expense += e.amount
        if (!e.isIncome) {
            if (largestExpense == null || e.amount > largestExpense!!.amount) largestExpense = e
        }
        val prev = categoryMap[e.category] ?: (0.0 to 0)
        categoryMap[e.category] = (prev.first + signed) to (prev.second + 1)

        val m = e.monthKey
        val mp = monthMap[m] ?: Triple(0.0, 0.0, 0)
        val inc = if (e.isIncome) e.amount else 0.0
        val exp = if (e.isIncome) 0.0 else e.amount
        monthMap[m] = Triple(mp.first + inc, mp.second + exp, mp.third + 1)
    }

    val totalAbs = income + expense
    val byCategory = categoryMap.map { (cat, v) ->
        val s = if (totalAbs <= 0.0) 0.0 else (kotlin.math.abs(v.first) / totalAbs).coerceIn(0.0, 1.0)
        LedgerCategoryStat(cat, v.first, v.second, s)
    }.sortedByDescending { kotlin.math.abs(it.total) }

    val byMonth = monthMap.entries.sortedBy { it.key }.map { (m, v) ->
        LedgerMonthStat(m, v.first, v.second, v.third)
    }

    val count = entries.size
    val avgTicket = if (count == 0) 0.0 else (income + expense) / count.toDouble()
    val savingsRate = if (income <= 0.0) 0.0 else ((income - expense) / income).coerceIn(0.0, 1.0)

    return LedgerAnalysis(
        income = income,
        expense = expense,
        balance = income - expense,
        count = count,
        avgTicket = avgTicket,
        largestExpense = largestExpense,
        savingsRate = savingsRate,
        byCategory = byCategory,
        byMonth = byMonth,
    )
}

// ===== 数据分析（AnalyticsRecord）聚合 =====

/** 单分类汇总：计数、总 PV、总收入、加权转化率、加权 ROI。 */
data class AnalyticsCategoryStat(
    val category: String,
    val count: Int,
    val totalPv: Long,
    val totalRevenue: Double,
    val avgConversion: Double?,
    val avgRoi: Double,
) {
    val totalPvLabel: String get() = totalPv.toString()
    val totalRevenueLabel: String get() = formatDecimal(totalRevenue, 2)
    val avgConversionLabel: String get() = avgConversion?.let { formatDecimal(it * 100.0, 2) + "%" } ?: "—"
    val avgRoiLabel: String get() = formatDecimal(avgRoi, 2)
}

/** 全量聚合：总量、加权指标、分类拆分、按收入 Top 列表。 */
data class AnalyticsAggregate(
    val totalPv: Long,
    val totalUv: Long,
    val totalClick: Long,
    val totalConvert: Long,
    val totalRevenue: Double,
    val totalCost: Double,
    val totalProfit: Double,
    val avgConversion: Double?,
    val avgRoi: Double,
    val avgQuality: Double,
    val avgRisk: Double,
    val byCategory: List<AnalyticsCategoryStat>,
    val topByRevenue: List<AnalyticsRecord>,
) {
    val totalPvLabel: String get() = totalPv.toString()
    val totalRevenueLabel: String get() = formatDecimal(totalRevenue, 2)
    val totalProfitLabel: String get() = formatDecimal(totalProfit, 2)
    val avgConversionLabel: String get() = avgConversion?.let { formatDecimal(it * 100.0, 2) + "%" } ?: "—"
    val avgRoiLabel: String get() = formatDecimal(avgRoi, 2)
}

/** 排序键：用于列表排序（维度切换）。 */
enum class RecordSortKey { DEFAULT, PV, UV, REVENUE, CONVERSION, ROI }

/** 从观测时间戳（epoch 秒）取 "YYYY-MM"。 */
val AnalyticsRecord.monthKey: String
    get() = formatUnixSeconds(observedAt).take(7)

/** 按观测时间区间过滤（含端点，单位 epoch 秒）。 */
fun filterRecordsByObservedRange(
    records: List<AnalyticsRecord>,
    fromSec: Long,
    toSec: Long,
): List<AnalyticsRecord> = records.filter { it.observedAt in fromSec..toSec }

/** 按分类过滤（null 表示不过滤）。 */
fun filterRecordsByCategory(
    records: List<AnalyticsRecord>,
    category: String?,
): List<AnalyticsRecord> = if (category == null) records else records.filter { it.category == category }

/** 排序：CONVERSION/ROI 用记录自带计算字段；其余用原始指标。 */
fun sortRecordsBy(records: List<AnalyticsRecord>, key: RecordSortKey): List<AnalyticsRecord> {
    return when (key) {
        RecordSortKey.PV -> records.sortedByDescending { it.metricPv }
        RecordSortKey.UV -> records.sortedByDescending { it.metricUv }
        RecordSortKey.REVENUE -> records.sortedByDescending { it.metricRevenue }
        RecordSortKey.CONVERSION -> records.sortedByDescending { it.conversionRate ?: 0.0 }
        RecordSortKey.ROI -> records.sortedByDescending { it.metricRoi }
        RecordSortKey.DEFAULT -> records
    }
}

/**
 * 跨记录聚合：总量、加权转化/ROI、质量风险均值、分类拆分、按收入 Top。
 * 这是「图表数据聚合 + 筛选/排序/时间区间」的底层计算。
 */
fun aggregateRecords(records: List<AnalyticsRecord>): AnalyticsAggregate {
    var totalPv = 0L
    var totalUv = 0L
    var totalClick = 0L
    var totalConvert = 0L
    var totalRevenue = 0.0
    var totalCost = 0.0
    var sumQuality = 0.0
    var sumRisk = 0.0

    val catMap = LinkedHashMap<String, Pair<Long, Double>>() // pv, revenue
    val catCount = LinkedHashMap<String, Int>()
    val catClick = LinkedHashMap<String, Long>()
    val catRoiWeight = LinkedHashMap<String, Double>() // roi * revenue

    records.forEach { r ->
        totalPv += r.metricPv
        totalUv += r.metricUv
        totalClick += r.metricClick
        totalConvert += r.metricConvert
        totalRevenue += r.metricRevenue
        totalCost += r.metricCost
        sumQuality += r.scoreQuality
        sumRisk += r.scoreRisk

        val pv = catMap[r.category]?.first ?: 0L
        val rev = catMap[r.category]?.second ?: 0.0
        catMap[r.category] = (pv + r.metricPv) to (rev + r.metricRevenue)
        catCount[r.category] = (catCount[r.category] ?: 0) + 1
        catClick[r.category] = (catClick[r.category] ?: 0L) + r.metricClick
        catRoiWeight[r.category] = (catRoiWeight[r.category] ?: 0.0) + (r.metricRoi * r.metricRevenue)
    }

    val count = records.size
    val avgConversion = if (totalClick <= 0L) null else totalConvert.toDouble() / totalClick.toDouble()
    val avgRoi = if (totalRevenue <= 0.0) 0.0 else ((totalRevenue - totalCost) / totalRevenue)
    val avgQuality = if (count == 0) 0.0 else sumQuality / count.toDouble()
    val avgRisk = if (count == 0) 0.0 else sumRisk / count.toDouble()

    val byCategory = catMap.map { (cat, v) ->
        val c = catCount[cat] ?: 0
        val click = catClick[cat] ?: 0L
        val roiW = catRoiWeight[cat] ?: 0.0
        val conv = if (click <= 0L) null else (records.filter { it.category == cat }.sumOf { it.metricConvert }.toDouble() / click.toDouble())
        val roiAvg = if (v.second <= 0.0) 0.0 else roiW / v.second
        AnalyticsCategoryStat(
            category = cat,
            count = c,
            totalPv = v.first,
            totalRevenue = v.second,
            avgConversion = conv,
            avgRoi = roiAvg,
        )
    }

    val topByRevenue = records.sortedByDescending { it.metricRevenue }

    return AnalyticsAggregate(
        totalPv = totalPv,
        totalUv = totalUv,
        totalClick = totalClick,
        totalConvert = totalConvert,
        totalRevenue = totalRevenue,
        totalCost = totalCost,
        totalProfit = totalRevenue - totalCost,
        avgConversion = avgConversion,
        avgRoi = avgRoi,
        avgQuality = avgQuality,
        avgRisk = avgRisk,
        byCategory = byCategory,
        topByRevenue = topByRevenue,
    )
}

// ===== 二手车（UsedCar）估值 =====

/**
 * 车龄（年）：以 2024 为基准。源模型只有 modelYear / mileageKm / amount，
 * 这里补充「当前估值」这一 main 缺失的分析维度（直线折旧 + 里程折损）。
 */
val UsedCarOrder.vehicleAgeYears: Int
    get() = (2024 - modelYear).coerceAtLeast(0)

/** 当前估值（元）：基准价 × 年限折损 × 里程折损。 */
val UsedCarOrder.estimatedValue: Double
    get() {
        val age = vehicleAgeYears
        val ageFactor = (1.0 - 0.12 * age).coerceIn(0.3, 1.0)
        val mileageFactor = when {
            mileageKm <= 30000 -> 1.0
            mileageKm <= 60000 -> 0.95
            mileageKm <= 100000 -> 0.88
            else -> 0.8
        }
        return amount * ageFactor * mileageFactor
    }

/** 折旧率（0..1）：相对挂牌/收购基准价的折损比例。 */
val UsedCarOrder.depreciationRate: Double
    get() = if (amount <= 0.0) 0.0 else (1.0 - estimatedValue / amount).coerceIn(0.0, 1.0)

val UsedCarOrder.estimatedValueLabel: String get() = formatDecimal(estimatedValue, 2)
val UsedCarOrder.depreciationRateLabel: String get() = formatDecimal(depreciationRate * 100.0, 1) + "%"

/** 单类型汇总。 */
data class UsedCarKindStat(
    val kind: String,
    val kindLabel: String,
    val count: Int,
    val totalAmount: Double,
) {
    val avgAmount: Double get() = if (count == 0) 0.0 else totalAmount / count.toDouble()
    val totalAmountLabel: String get() = formatDecimal(totalAmount, 2)
    val avgAmountLabel: String get() = formatDecimal(avgAmount, 2)
}

/** 业务单整体估值概览。 */
data class UsedCarAggregate(
    val count: Int,
    val gmv: Double,
    val avgAmount: Double,
    val byKind: List<UsedCarKindStat>,
) {
    val gmvLabel: String get() = formatDecimal(gmv, 2)
    val avgAmountLabel: String get() = formatDecimal(avgAmount, 2)
}

/**
 * 业务单聚合：总 GMV、均价、按类型拆分。配合 [UsedCarOrder.estimatedValue]
 * 形成「二手车估值」分析维度。
 */
fun aggregateUsedCars(orders: List<UsedCarOrder>): UsedCarAggregate {
    var gmv = 0.0
    val kindMap = LinkedHashMap<String, Pair<String, Double>>() // label, amount
    val kindCount = LinkedHashMap<String, Int>()

    orders.forEach { o ->
        gmv += o.amount
        val prev = kindMap[o.kind] ?: (o.kindLabel to 0.0)
        kindMap[o.kind] = (prev.first) to (prev.second + o.amount)
        kindCount[o.kind] = (kindCount[o.kind] ?: 0) + 1
    }

    val byKind = kindMap.map { (k, v) ->
        UsedCarKindStat(k, v.first, kindCount[k] ?: 0, v.second)
    }

    val count = orders.size
    val avgAmount = if (count == 0) 0.0 else gmv / count.toDouble()

    return UsedCarAggregate(count = count, gmv = gmv, avgAmount = avgAmount, byKind = byKind)
}
