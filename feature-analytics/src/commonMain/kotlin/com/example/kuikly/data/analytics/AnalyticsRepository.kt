package com.example.kuikly.data.analytics

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** Analytics record repository (Mock backend, no real gRPC). */
interface AnalyticsRepository {
    fun fetchPage(page: Int, pageSize: Int = 10): Result<AnalyticsPageResult>
    fun fetchById(id: Int): Result<AnalyticsRecord>
}

/**
 * Fake implementation driven by [MockBackend.scenario]:
 * Success -> seed list, Empty -> empty, Error -> failure, Slow -> seed (caller simulates delay).
 */
class FakeAnalyticsRepository : AnalyticsRepository {
    private val seed = listOf(
        record(1, "ANA-1001", "首页改版 A/B 实验", "流量与转化观测", "growth", "active",
            pv = 128_400, uv = 82_300, click = 41_200, convert = 6_180, roi = 3.42, bounce = 0.38,
            quality = 88.0, risk = 12.0, featured = true, anomaly = false),
        record(2, "ANA-1002", "信息流推荐策略", "点击与停留观测", "content", "paused",
            pv = 96_500, uv = 61_000, click = 30_900, convert = 2_470, roi = 1.95, bounce = 0.51,
            quality = 72.0, risk = 34.0, featured = false, anomaly = true),
        record(3, "ANA-1003", "商城大促落地页", "收入与成本对照", "commerce", "active",
            pv = 213_900, uv = 140_200, click = 71_400, convert = 9_980, roi = 4.10, bounce = 0.29,
            quality = 91.0, risk = 8.0, featured = true, anomaly = false),
        record(4, "ANA-1004", "搜索结果排序", "转化漏斗观测", "search", "archived",
            pv = 54_200, uv = 38_900, click = 17_300, convert = 1_210, roi = 0.84, bounce = 0.63,
            quality = 55.0, risk = 58.0, featured = false, anomaly = true),
        record(5, "ANA-1005", "会员权益页", "留存与粘性观测", "member", "active",
            pv = 77_800, uv = 52_100, click = 24_700, convert = 3_910, roi = 2.67, bounce = 0.44,
            quality = 80.0, risk = 19.0, featured = false, anomaly = false),
        record(6, "ANA-1006", "直播带货专题", "实时转化观测", "live", "paused",
            pv = 165_300, uv = 98_700, click = 52_100, convert = 7_340, roi = 3.01, bounce = 0.36,
            quality = 85.0, risk = 22.0, featured = true, anomaly = false),
        record(7, "ANA-1007", "短视频信息流", "完播与转化", "video", "active",
            pv = 302_100, uv = 188_400, click = 95_200, convert = 11_200, roi = 3.88, bounce = 0.41,
            quality = 83.0, risk = 27.0, featured = false, anomaly = false),
        record(8, "ANA-1008", "客服工单转化", "挽回与转化观测", "service", "archived",
            pv = 23_400, uv = 19_800, click = 8_900, convert = 610, roi = 0.62, bounce = 0.71,
            quality = 48.0, risk = 66.0, featured = false, anomaly = true),
    )

    override fun fetchPage(page: Int, pageSize: Int): Result<AnalyticsPageResult> {
        return when (MockBackend.scenario) {
            MockScenario.Error -> Result.failure(IllegalStateException("mock analytics error"))
            MockScenario.Empty -> Result.success(AnalyticsPageResult(emptyList(), 0, page, pageSize))
            else -> {
                val from = (page - 1) * pageSize
                val slice = seed.drop(from).take(pageSize)
                Result.success(AnalyticsPageResult(slice, seed.size, page, pageSize))
            }
        }
    }

    override fun fetchById(id: Int): Result<AnalyticsRecord> {
        if (MockBackend.scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock analytics error"))
        }
        return seed.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("no record $id"))
    }

    private fun record(
        id: Int, code: String, title: String, subtitle: String, category: String, status: String,
        pv: Int, uv: Int, click: Int, convert: Int, roi: Double, bounce: Double,
        quality: Double, risk: Double, featured: Boolean, anomaly: Boolean,
    ): AnalyticsRecord {
        val now = 1_700_000_000L + id * 3600L
        return AnalyticsRecord(
            id = id, code = code, title = title, subtitle = subtitle,
            category = category, subCategory = "snapshot", status = status,
            priority = if (anomaly) 1 else 2, region = "CN", channel = "all",
            ownerName = "team-$category", ownerTeam = "Growth", sourceSystem = "GoBFF",
            metricPv = pv, metricUv = uv, metricClick = click, metricConvert = convert,
            metricRevenue = (convert * 12.5), metricCost = (convert * 3.2),
            metricRoi = roi, metricBounceRate = bounce, metricAvgDurationSec = 78,
            scoreQuality = quality, scoreRisk = risk,
            tagPrimary = category, tagSecondary = status,
            flagFeatured = featured, flagAnomaly = anomaly, notes = "",
            observedAt = now, windowStart = now - 86400, windowEnd = now,
            publishedAt = now, createdAt = now, updatedAt = now,
        )
    }
}

object AnalyticsStore {
    val repo: AnalyticsRepository = FakeAnalyticsRepository()
}
