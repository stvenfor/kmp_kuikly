package com.example.kuikly.data.pay

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

enum class MembershipTier { none, monthly, quarterly, yearly }

/** 会员套餐（port of source MembershipPlan）。 */
data class MembershipPlan(
    val id: String,
    val tier: MembershipTier,
    val title: String,
    val price: Double,
    val originalPrice: Double,
    val months: Int,
    val badge: String?,
    val autoRenew: Boolean,
) {
    val perMonth: Double get() = price / months.coerceAtLeast(1)
    val discount: Double get() = if (originalPrice > 0) 1 - price / originalPrice else 0.0
}

enum class PayMethod { wechat, alipay, wallet }

/** 支付结果（port of source PayResult）。 */
data class PayResult(
    val orderNo: String,
    val amount: Double,
    val method: PayMethod,
    val success: Boolean,
)

class PayRepository {
    private val plans = listOf(
        MembershipPlan("m1", MembershipTier.monthly, "月度会员", 25.0, 30.0, 1, null, true),
        MembershipPlan("m2", MembershipTier.quarterly, "季度会员", 68.0, 90.0, 3, "省22%", true),
        MembershipPlan("m3", MembershipTier.yearly, "年度会员", 238.0, 360.0, 12, "最划算", false),
    )

    fun plans(): Result<List<MembershipPlan>> =
        if (MockBackend.scenario == MockScenario.Error) Result.failure(IllegalStateException("mock pay error"))
        else Result.success(plans)

    /** 创建订单（返回订单号）。 */
    fun createOrder(planId: String): Result<String> {
        if (MockBackend.scenario == MockScenario.Error) return Result.failure(IllegalStateException("mock pay error"))
        val plan = plans.find { it.id == planId } ?: return Result.failure(NoSuchElementException("plan=$planId"))
        return Result.success("PAY-${plan.tier}-${plan.price}")
    }

    /** 支付（Empty 场景模拟支付失败以演示错误态）。 */
    fun pay(orderNo: String, amount: Double, method: PayMethod): Result<PayResult> {
        if (MockBackend.scenario == MockScenario.Error || MockBackend.scenario == MockScenario.Empty) {
            return Result.failure(IllegalStateException("payment failed"))
        }
        return Result.success(PayResult(orderNo, amount, method, true))
    }
}

object PayStore {
    val repo: PayRepository = PayRepository()
}
