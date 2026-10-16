package com.example.kuikly.data.analytics

/**
 * Analytics record — a single observation snapshot (NOT a time series).
 * Ported from the source project AnalyticsRecordModel.
 */
data class AnalyticsRecord(
    val id: Int,
    val code: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val subCategory: String,
    val status: String,
    val priority: Int,
    val region: String,
    val channel: String,
    val ownerName: String,
    val ownerTeam: String,
    val sourceSystem: String,
    val metricPv: Int,
    val metricUv: Int,
    val metricClick: Int,
    val metricConvert: Int,
    val metricRevenue: Double,
    val metricCost: Double,
    val metricRoi: Double,
    val metricBounceRate: Double,
    val metricAvgDurationSec: Int,
    val scoreQuality: Double,
    val scoreRisk: Double,
    val tagPrimary: String,
    val tagSecondary: String,
    val flagFeatured: Boolean,
    val flagAnomaly: Boolean,
    val notes: String,
    val observedAt: Long,
    val windowStart: Long,
    val windowEnd: Long,
    val publishedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    /** Conversion rate = convert / click; null when click == 0 (display "—"). */
    val conversionRate: Double?
        get() = if (metricClick <= 0) null else metricConvert.toDouble() / metricClick.toDouble()

    /**
     * Stickiness (0..100) derived from bounce rate — radar 3rd axis filler only,
     * not a real backend field. Higher bounce => lower stickiness.
     */
    val stickiness: Int
        get() = (100 - (metricBounceRate * 100).toInt()).coerceIn(0, 100)

    /** Click-through rate = click / pv; null when pv == 0 (display "—"). */
    val clickThroughRate: Double?
        get() = if (metricPv <= 0) null else metricClick.toDouble() / metricPv.toDouble()

    /** Net profit = revenue - cost (source detail page shows revenue/cost side by side). */
    val profit: Double
        get() = metricRevenue - metricCost

    /** Net margin = profit / revenue; null when there is no revenue. */
    val profitMargin: Double?
        get() = if (metricRevenue <= 0.0) null else profit / metricRevenue

    /** Human readable priority: source detail page prints the raw int, UI sorts by it. */
    val priorityLabel: String
        get() = when {
            priority <= 1 -> "P0"
            priority == 2 -> "P1"
            else -> "P$priority"
        }
}

data class AnalyticsPageResult(
    val items: List<AnalyticsRecord>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
) {
    val hasMore: Boolean get() = page * pageSize < total
}

/** Format epoch seconds as UTC "yyyy-MM-dd HH:mm:ss" (leap-year aware, zero dependency). */
fun formatUnixSeconds(seconds: Long): String {
    if (seconds <= 0) return "-"
    var s = seconds
    val sec = (s % 60).toInt().also { s /= 60 }
    val min = (s % 60).toInt().also { s /= 60 }
    val hr = (s % 24).toInt().also { s /= 24 }
    var days = s
    var year = 1970
    while (true) {
        val len = if (isLeap(year)) 366 else 365
        if (days >= len) { days -= len; year++ } else break
    }
    val monthLen = intArrayOf(31, if (isLeap(year)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var month = 0
    while (days >= monthLen[month]) { days -= monthLen[month]; month++ }
    val day = (days + 1).toInt()
    val two = { n: Int -> n.toString().padStart(2, '0') }
    return "$year-${two(month + 1)}-${two(day)} ${two(hr)}:${two(min)}:${two(sec)}"
}

private fun isLeap(y: Int): Boolean = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)

/**
 * Rounds [value] to [digits] decimals without relying on a platform `String.format`,
 * which is not available in every Kotlin target of this module.
 */
internal fun formatDecimal(value: Double, digits: Int): String {
    val negative = value < 0.0
    val abs = if (negative) -value else value
    var factor = 1.0
    var i = 0
    while (i < digits) {
        factor *= 10.0
        i++
    }
    val divisor = factor.toLong()
    val scaled = (abs * factor + 0.5).toLong()
    val intPart = scaled / divisor
    val fracPart = scaled % divisor
    val body = if (digits <= 0) {
        intPart.toString()
    } else {
        intPart.toString() + "." + fracPart.toString().padStart(digits, '0')
    }
    return if (negative) "-$body" else body
}

/** Signed percentage label, e.g. "+19.22%" / "-2.34%". */
internal fun formatSignedPercent(value: Double, digits: Int = 2): String {
    val prefix = if (value >= 0.0) "+" else ""
    return prefix + formatDecimal(value, digits) + "%"
}
