package com.example.kuikly.data.pay

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

/** 运行平台：决定可用支付渠道（port of `HuaweiIapBridge.isOhos` / `AppleIapBridge.isIOS`）。 */
enum class PayPlatform { Android, Ohos, Ios }

/**
 * 支付渠道（port of Flutter `PaymentMethodType`）。
 *
 * 与 `PayData.kt` 的 `PayMethod`（wechat / alipay / wallet）**并存不合并**：
 * `PayMethod` 被 `PayPage` / `MembershipRenewPage` 用穷举 `when` 消费，扩枚举会破坏编译；
 * 本枚举额外覆盖源的 `balance` / `huawei` / `apple` 三档与「协议勾选」规则。
 */
enum class PayChannel(val key: String) {
    wechat("wechat"),
    alipay("alipay"),
    balance("balance"),
    huawei("huawei"),
    apple("apple");

    val label: String
        get() = when (this) {
            wechat -> "微信支付"
            alipay -> "支付宝支付"
            balance -> "余额支付"
            huawei -> "华为内购"
            apple -> "苹果内购"
        }

    /** 源 `showAgreementCheckbox`：AI SVIP 档位或内购渠道必须先勾协议。 */
    val requiresAgreement: Boolean get() = this == huawei || this == apple

    /** 内购渠道为订阅制（自动续费），其余为时长买断。 */
    val isSubscription: Boolean get() = requiresAgreement

    /** 源 `_tileFor` 的副文案。 */
    fun subtitle(walletBalance: Double): String = when (this) {
        wechat, alipay -> "（时长买断）"
        balance -> "（可用 ¥${amountLabel(walletBalance)}）"
        huawei, apple -> "（自动续费订阅）"
    }
}

/** 渠道可用性 + 下单前校验（port of `availablePaymentMethods` + `payBuyout` 的 channel 映射）。 */
object PayChannels {
    fun available(platform: PayPlatform = PayPlatform.Android): List<PayChannel> = when (platform) {
        PayPlatform.Ohos -> listOf(PayChannel.wechat, PayChannel.alipay, PayChannel.balance, PayChannel.huawei)
        PayPlatform.Ios -> listOf(PayChannel.wechat, PayChannel.alipay, PayChannel.balance, PayChannel.apple)
        PayPlatform.Android -> listOf(PayChannel.wechat, PayChannel.alipay, PayChannel.balance)
    }

    /** 返回 null 表示可下单，否则返回给用户的校验失败原因。 */
    fun validate(
        channel: PayChannel,
        payable: Double,
        wallet: MemberWallet,
        platform: PayPlatform = PayPlatform.Android,
    ): String? {
        if (!available(platform).contains(channel)) return "请使用对应内购通道"
        if (channel == PayChannel.balance && wallet.balance < payable) {
            return "余额不足，可用 ¥${amountLabel(wallet.balance)}"
        }
        return null
    }
}

/**
 * 下单流程状态机（port of `MembershipPaymentGateway` + `WysPaymentStatus`）：
 * 创建中 → 已下单 → 支付中 → 成功 / 处理中 / 已取消 / 失败 / 超时。
 */
enum class PayOrderState {
    Creating,
    Created,
    Paying,
    Success,
    Processing,
    Timeout,
    Cancelled,
    Failed;

    /** 仍在进行中（UI 需禁用 CTA / 展示「处理中…」）。 */
    val isBusy: Boolean get() = this == Creating || this == Paying

    val isSuccess: Boolean get() = this == Success

    /** 已终结（成功或各类失败）。 */
    val isSettled: Boolean get() = !isBusy && this != Created

    val label: String
        get() = when (this) {
            Creating -> "下单中"
            Created -> "待支付"
            Paying -> "支付中"
            Success -> "支付成功"
            Processing -> "支付处理中"
            Timeout -> "下单超时"
            Cancelled -> "已取消"
            Failed -> "支付失败"
        }
}

/** 会员订单（下单/支付闭环的唯一载体）。 */
data class PayOrder(
    val orderNo: String,
    val skuId: String,
    val tier: PayTier,
    val title: String,
    val months: Int,
    val amount: Double,
    val channel: PayChannel,
    val autoRenew: Boolean,
    val state: PayOrderState,
    val createdAtLabel: String = MemberCatalog.TODAY,
    val message: String = "",
) {
    val isSuccess: Boolean get() = state == PayOrderState.Success

    /** 权益到期日（创建日 + 套餐月数）。 */
    val expiresAtLabel: String get() = PayDates.addMonths(createdAtLabel, months)

    /** 自动续费的下次扣费日 = 到期日。 */
    val nextChargeLabel: String get() = expiresAtLabel
}

/**
 * 订单仓库（port of `MembershipApi` 的 buyout / confirm + `MembershipPaymentGateway`）。
 *
 * 复用 `BaseMockRepository` 的四态语义：Error → `Result.failure`；Empty → 空列表；
 * Success / Slow → 全量种子。新建订单写入内存 `history`，下单后可被
 * `PayOrders` 列表页与 `PayResult` 结果页读到。
 */
class PayOrderRepository : BaseMockRepository<PayOrder>() {
    private val history = mutableListOf<PayOrder>()

    init {
        history.addAll(MemberCatalog.seedOrders())
    }

    override fun seed(): List<PayOrder> = history

    override fun idOf(item: PayOrder): String = item.orderNo

    fun orders(): Result<List<PayOrder>> = all()

    fun detail(orderNo: String): Result<PayOrder> = byId(orderNo)

    /**
     * 状态机第一步：创建订单。
     * Error → 失败；Slow → 超时（`Timeout`）；其余 → `Created`。
     */
    fun createOrder(
        quote: PayQuote,
        channel: PayChannel,
        autoRenew: Boolean,
        platform: PayPlatform = PayPlatform.Android,
    ): Result<PayOrder> {
        val reason = PayChannels.validate(channel, quote.payable, MemberCatalog.wallet, platform)
        if (reason != null) return Result.failure(IllegalStateException(reason))
        if (MockBackend.scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("下单失败"))
        }
        val timeout = MockBackend.scenario == MockScenario.Slow
        val order = PayOrder(
            orderNo = nextOrderNo(),
            skuId = quote.sku.id,
            tier = quote.sku.tier,
            title = quote.sku.title,
            months = quote.sku.months,
            amount = quote.payable,
            channel = channel,
            autoRenew = autoRenew,
            state = if (timeout) PayOrderState.Timeout else PayOrderState.Created,
            createdAtLabel = MemberCatalog.TODAY,
            message = if (timeout) "下单超时，请重试" else "",
        )
        history.add(0, order)
        return Result.success(order)
    }

    /** 状态机第二步：支付（渠道受理）。Slow → 处理中；Empty → 已取消；Error → 失败。 */
    fun pay(order: PayOrder): Result<PayOrder> {
        val settled = when (MockBackend.scenario) {
            MockScenario.Error -> order.copy(state = PayOrderState.Failed, message = "支付失败（sdkError）")
            MockScenario.Empty -> order.copy(state = PayOrderState.Cancelled, message = "已取消支付")
            MockScenario.Slow -> order.copy(state = PayOrderState.Processing, message = "支付处理中，请稍后确认")
            MockScenario.Success -> order.copy(state = PayOrderState.Success, message = "支付成功")
        }
        replace(settled)
        return Result.success(settled)
    }

    /**
     * 一键开通/续费（port of `MembershipRenewController.renewNow`）：
     * 渠道校验 → 协议校验 → 创建订单 → 支付。
     */
    fun renewNow(
        quote: PayQuote,
        channel: PayChannel,
        autoRenew: Boolean,
        agreed: Boolean,
        platform: PayPlatform = PayPlatform.Android,
    ): Result<PayOrder> {
        val reason = PayChannels.validate(channel, quote.payable, MemberCatalog.wallet, platform)
        if (reason != null) return Result.failure(IllegalStateException(reason))
        if (channel.requiresAgreement && !agreed) {
            return Result.failure(IllegalStateException("请先阅读并同意会员协议"))
        }
        val created = createOrder(quote, channel, autoRenew, platform)
            .getOrElse { return Result.failure(it) }
        if (created.state != PayOrderState.Created) return Result.success(created)
        return pay(created)
    }

    /** 自动续费开关：写入订单的签约状态。 */
    fun setAutoRenew(orderNo: String, enabled: Boolean): Result<PayOrder> {
        val index = history.indexOfFirst { it.orderNo == orderNo }
        if (index < 0) return Result.failure(NoSuchElementException("order=$orderNo"))
        val updated = history[index].copy(
            autoRenew = enabled,
            message = if (enabled) "已开启自动续费" else "已关闭自动续费",
        )
        history[index] = updated
        return Result.success(updated)
    }

    fun channels(platform: PayPlatform = PayPlatform.Android): List<PayChannel> =
        PayChannels.available(platform)

    private fun nextOrderNo(): String {
        val seq = history.size + 1
        val suffix = if (seq < 10) "00$seq" else if (seq < 100) "0$seq" else "$seq"
        return "M${MemberCatalog.TODAY.replace("-", "")}$suffix"
    }

    private fun replace(order: PayOrder) {
        val index = history.indexOfFirst { it.orderNo == order.orderNo }
        if (index < 0) {
            history.add(0, order)
        } else {
            history[index] = order
        }
    }
}

object PayFlowStore {
    val repo: PayOrderRepository = PayOrderRepository()
}
