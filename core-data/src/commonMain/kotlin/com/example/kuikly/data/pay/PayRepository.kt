package com.example.kuikly.data.pay

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** 会员档位（Flutter `MembershipTier`）。 */
enum class PayTier(val key: String) {
    Svip("svip"),
    AiSvip("ai_svip"),
}

/**
 * 会员套餐 — 镜像 Flutter `MembershipPlan`（`membership_models.dart`）
 * + Kuikly 既有 `priceLabel`/`desc`（测试与 PayConfirm 仍引用）。
 */
data class Plan(
    val id: String,
    val title: String,
    val priceLabel: String,
    val desc: String,
    val tier: PayTier = PayTier.Svip,
    /** Flutter `MembershipPlan.price`（大字价格）。 */
    val price: Double = 0.0,
    /** Flutter `MembershipPlan.originalPrice`（划线原价）。 */
    val originalPrice: Double = 0.0,
    /** Flutter `MembershipPlan.badge`（角标：「开学尝鲜价」/「活动利益点」）。 */
    val badge: String? = null,
    /** Flutter `MembershipPlan.dailyHint`（未选中卡片底部桃色提示）。 */
    val dailyHint: String? = null,
    /** Flutter `MembershipPlan.showRedPacket`（选中时底部红包条）。 */
    val showRedPacket: Boolean = false,
)

interface PayRepository {
    fun list(): Result<List<Plan>>

    fun detail(id: String): Result<Plan>
}

/**
 * Mock membership plans (no real payment SDK), mirroring
 * `features/pay/lib/membership/mock/membership_mock_data.dart`
 * （svip / ai_svip 各 3 档；Kuikly 保留 Phase-1 的 "1".."6" id 以免破坏既有深链 golden）。
 */
class FakePayRepository : PayRepository {
    private val seed = listOf(
        Plan(
            "1", "12个月", "¥380", "开学尝鲜价，12 个月会员",
            tier = PayTier.Svip, price = 380.0, originalPrice = 488.0,
            badge = "开学尝鲜价", showRedPacket = true,
        ),
        Plan(
            "2", "24个月", "¥488", "每日仅需0.66元",
            tier = PayTier.Svip, price = 488.0, originalPrice = 888.0,
            badge = "活动利益点", dailyHint = "每日仅需0.66元",
        ),
        Plan(
            "3", "连续包年", "¥288", "每日仅需0.78元",
            tier = PayTier.Svip, price = 288.0, originalPrice = 488.0,
            dailyHint = "每日仅需0.78元",
        ),
        Plan(
            "4", "12个月", "¥488", "开学尝鲜价，12 个月 AI 会员",
            tier = PayTier.AiSvip, price = 488.0, originalPrice = 688.0,
            badge = "开学尝鲜价", showRedPacket = true,
        ),
        Plan(
            "5", "24个月", "¥688", "每日仅需0.94元",
            tier = PayTier.AiSvip, price = 688.0, originalPrice = 1288.0,
            badge = "活动利益点", dailyHint = "每日仅需0.94元",
        ),
        Plan(
            "6", "连续包年", "¥398", "每日仅需1.09元",
            tier = PayTier.AiSvip, price = 398.0, originalPrice = 688.0,
            dailyHint = "每日仅需1.09元",
        ),
    )

    override fun list(): Result<List<Plan>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock pay error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun detail(id: String): Result<Plan> = list().mapCatching { plans ->
        plans.firstOrNull { it.id == id } ?: throw IllegalStateException("plan not found: $id")
    }
}

object PayStore {
    val repo: PayRepository = FakePayRepository()
}
