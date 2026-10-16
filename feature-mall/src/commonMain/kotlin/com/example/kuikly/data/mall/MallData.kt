package com.example.kuikly.data.mall

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

// 金额统一展示：¥ 前缀 + 保留 2 位小数。
fun formatYuan(value: Double): String = "¥%.2f".format(value)

/** 商品卡片（port of source MallProductCardModel）。 */
data class Product(
    val id: String,
    val skuId: Int,
    val title: String,
    val price: Double,
    val coverUrl: String,
    val subtitle: String?,
    val points: Int?, // 积分兑换价
    val soldLabel: String?,
    val badges: List<String>,
    val stockQty: Int = 99,
) {
    val isVirtual: Boolean get() = skuId % 2 == 1
    val priceLabel: String get() = if (points != null && points > 0) "$points 积分" else formatYuan(price)
}

/** 规格报价（port of source MallSkuOffer）。 */
data class SkuOffer(
    val skuId: Int,
    val title: String,
    val price: Double,
    val pricePoints: Int = 0,
    val stockQty: Int = 99,
    val specs: Map<String, String> = emptyMap(),
) {
    val specLabel: String
        get() = if (specs.isEmpty()) {
            if (title.isEmpty()) "默认规格" else title
        } else {
            specs.entries.joinToString(" / ") { "${it.key} ${it.value}" }
        }

    val priceLabel: String
        get() = when {
            pricePoints > 0 && price > 0.0 -> "$pricePoints 积分 + ${formatYuan(price)}"
            pricePoints > 0 -> "$pricePoints 积分"
            else -> formatYuan(price)
        }
}

/** 商品详情（port of source MallProductDetail），含图文详情段落。 */
data class ProductDetail(
    val productId: String,
    val title: String,
    val coverUrl: String,
    val coverAspect: Double = 1.0,
    val kind: Int = 0,
    val subtitle: String = "",
    val gallery: List<String> = emptyList(),
    val richText: List<String> = emptyList(),
    val skus: List<SkuOffer> = emptyList(),
) {
    val isVirtual: Boolean get() = kind == 1
    val kindLabel: String
        get() = if (isVirtual) "虚拟商品 · 支付后发放" else "实体商品 · 需填写收货信息"

    fun defaultSku(): SkuOffer? = skus.firstOrNull()
}

/** 购物车行（同一商品自动合并去重）。 */
data class CartLine(
    val productId: String,
    val skuId: Int,
    val title: String,
    val coverUrl: String,
    val unitPrice: Double,
    val qty: Int,
) {
    val lineAmount: Double get() = unitPrice * qty
    val lineAmountLabel: String get() = formatYuan(lineAmount)
    val unitPriceLabel: String get() = formatYuan(unitPrice)
}

/** 订单行（port of source MallOrderItem）。 */
data class OrderItem(
    val itemId: Int,
    val orderId: Int,
    val productTitle: String,
    val coverUrl: String,
    val qty: Int,
    val lineAmount: Double,
    val contentUrl: String,
    val kind: Int = 0,
) {
    val lineAmountLabel: String get() = formatYuan(lineAmount)
}

/** 订单状态机：待付款 -> 待发货 -> 待收货 -> 已完成（另含已取消终态）。 */
object OrderStatus {
    const val unpaid = 0
    const val paid = 1
    const val shipped = 2
    const val completed = 3
    const val cancelled = 4

    val flow: List<Int> = listOf(unpaid, paid, shipped, completed)
    val flowLabels: List<String> = listOf("待付款", "待发货", "待收货", "已完成")

    fun label(status: Int): String = when (status) {
        unpaid -> "待付款"
        paid -> "待发货"
        shipped -> "待收货"
        completed -> "已完成"
        cancelled -> "已取消"
        else -> "未知状态"
    }

    // 步骤条下标，已取消返回 -1。
    fun stepIndex(status: Int): Int = when (status) {
        unpaid -> 0
        paid -> 1
        shipped -> 2
        completed -> 3
        else -> -1
    }

    // 状态流转的下一个状态，终态返回 null。
    fun next(status: Int): Int? = when (status) {
        unpaid -> paid
        paid -> shipped
        shipped -> completed
        else -> null
    }
}

/** 商城支付渠道（port of source payment_channel）。 */
object MallPayChannel {
    const val none = 0
    const val alipay = 1
    const val wechat = 2
    const val balance = 3

    fun label(channel: Int): String = when (channel) {
        alipay -> "支付宝"
        wechat -> "微信支付"
        balance -> "余额"
        else -> "未选择"
    }
}

/** 订单（port of source MallOrderListRow / MallOrderDetail）。 */
data class Order(
    val orderId: Int,
    val orderNo: String,
    val status: Int,
    val amount: Double,
    val createdAt: String,
    val items: List<OrderItem>,
    val receiverName: String = "",
    val receiverPhone: String = "",
    val receiverAddress: String = "",
    val payChannel: Int = MallPayChannel.none,
    val payDeadlineAt: String = "",
    val paidAt: String = "",
    val totalPoints: Int = 0,
) {
    val isUnpaid: Boolean get() = status == OrderStatus.unpaid
    val statusLabel: String get() = OrderStatus.label(status)
    val amountLabel: String get() = formatYuan(amount)
    val stepIndex: Int get() = OrderStatus.stepIndex(status)
    val totalQty: Int get() = items.sumOf { it.qty }
    val primaryTitle: String get() = items.firstOrNull()?.productTitle ?: "订单 $orderNo"
    val primaryCover: String get() = items.firstOrNull()?.coverUrl ?: ""
    val payChannelLabel: String get() = MallPayChannel.label(payChannel)
    val hasReceiver: Boolean
        get() = receiverName.isNotBlank() || receiverPhone.isNotBlank() || receiverAddress.isNotBlank()

    val canPay: Boolean get() = status == OrderStatus.unpaid
    val canCancel: Boolean get() = status == OrderStatus.unpaid || status == OrderStatus.paid
    val canConfirmReceipt: Boolean get() = status == OrderStatus.shipped

    val primaryActionLabel: String
        get() = when {
            canPay -> "去支付"
            canConfirmReceipt -> "确认收货"
            status == OrderStatus.completed -> "已完成"
            status == OrderStatus.cancelled -> "已取消"
            else -> "等待发货"
        }
}

class MallRepository : BaseMockRepository<Product>() {
    private val products = listOf(
        Product("pd1", 101, "降噪无线耳机", 1299.0, "https://picsum.photos/seed/pd1/400", "主动降噪 · 30h 续航", null, "已兑1.2万", listOf("热销", "包邮"), 42),
        Product("pd2", 202, "视频会员月卡", 25.0, "https://picsum.photos/seed/pd2/400", "全平台通用", null, null, listOf("虚拟"), 9999),
        Product("pd3", 303, "定制保温杯", 1500.0, "https://picsum.photos/seed/pd3/400", "积分专享", 1500, "已兑3200", listOf("积分"), 8),
    )

    private val details: Map<String, ProductDetail> = mapOf(
        "pd1" to ProductDetail(
            productId = "pd1",
            title = "降噪无线耳机",
            coverUrl = "https://picsum.photos/seed/pd1/400",
            coverAspect = 1.0,
            kind = 0,
            subtitle = "主动降噪 · 30h 续航",
            gallery = listOf("正面", "侧面", "佩戴"),
            richText = listOf(
                "双馈式主动降噪，最高 42dB 深度降噪。",
                "单次续航 10 小时，配合充电仓共 30 小时。",
                "支持双设备连接与低延迟游戏模式。",
            ),
            skus = listOf(
                SkuOffer(101, "标准版", 1299.0, 0, 42, mapOf("颜色" to "曜石黑", "版本" to "标准版")),
                SkuOffer(102, "旗舰版", 1599.0, 0, 12, mapOf("颜色" to "月光银", "版本" to "旗舰版")),
            ),
        ),
        "pd2" to ProductDetail(
            productId = "pd2",
            title = "视频会员月卡",
            coverUrl = "https://picsum.photos/seed/pd2/400",
            coverAspect = 1.2,
            kind = 1,
            subtitle = "全平台通用",
            gallery = listOf("卡面"),
            richText = listOf(
                "虚拟商品，支付成功后 5 分钟内发放到账户。",
                "支持主流视频平台会员权益，30 天有效。",
            ),
            skus = listOf(
                SkuOffer(202, "月卡", 25.0, 0, 9999, mapOf("时长" to "30 天")),
                SkuOffer(203, "季卡", 68.0, 0, 9999, mapOf("时长" to "90 天")),
            ),
        ),
        "pd3" to ProductDetail(
            productId = "pd3",
            title = "定制保温杯",
            coverUrl = "https://picsum.photos/seed/pd3/400",
            coverAspect = 0.9,
            kind = 0,
            subtitle = "积分专享",
            gallery = listOf("整体", "杯盖", "杯底"),
            richText = listOf(
                "316 不锈钢内胆，24 小时保温。",
                "支持激光刻字定制，下单后 3 个工作日内发货。",
            ),
            skus = listOf(
                SkuOffer(303, "500ml", 1500.0, 1500, 8, mapOf("容量" to "500ml", "颜色" to "雾霾蓝")),
                SkuOffer(304, "800ml", 1800.0, 1800, 3, mapOf("容量" to "800ml", "颜色" to "石墨黑")),
            ),
        ),
    )

    private val cart = mutableMapOf<String, Int>() // productId -> qty
    private val orderTable = mutableListOf(
        Order(1, "NO20240501001", 0, 1299.0, "2024-05-01", listOf(OrderItem(11, 1, "降噪无线耳机", "https://picsum.photos/seed/pd1/400", 1, 1299.0, "")),
            receiverName = "张三", receiverPhone = "138****8888", receiverAddress = "深圳市南山区科技园 1 号楼", payDeadlineAt = "2024-05-01 23:59"),
        Order(2, "NO20240502002", 2, 25.0, "2024-05-02", listOf(OrderItem(12, 2, "视频会员月卡", "https://picsum.photos/seed/pd2/400", 1, 25.0, "https://card.example.com/c/OD20240502")),
            payChannel = MallPayChannel.wechat, paidAt = "2024-05-02 10:12"),
    )

    override fun seed(): List<Product> = products
    override fun idOf(item: Product): String = item.id

    // --- 商品详情 ---

    fun productDetail(productId: String): Result<ProductDetail> {
        if (MockBackend.scenario == MockScenario.Error) {
            return Result.failure(IllegalStateException("mock backend error"))
        }
        val detail = details[productId]
            ?: products.find { it.id == productId }?.let {
                ProductDetail(
                    productId = it.id,
                    title = it.title,
                    coverUrl = it.coverUrl,
                    kind = if (it.isVirtual) 1 else 0,
                    subtitle = it.subtitle ?: "",
                    skus = listOf(SkuOffer(it.skuId, it.title, it.price, it.points ?: 0, it.stockQty)),
                )
            }
        return detail?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("product=$productId"))
    }

    // --- 购物车 ---

    fun addToCart(productId: String, qty: Int = 1): Int = setCartQty(productId, quantityOf(productId) + qty)

    fun setCartQty(productId: String, qty: Int): Int {
        val safe = qty.coerceAtLeast(0)
        if (safe == 0) cart.remove(productId) else cart[productId] = safe
        return cartCount()
    }

    fun removeFromCart(productId: String): Int = setCartQty(productId, 0)

    fun quantityOf(productId: String): Int = cart[productId] ?: 0

    fun cartCount(): Int = cart.values.sum()

    // 同一商品合并为一行，天然去重。
    fun cartLines(): List<CartLine> = cart.entries.mapNotNull { (pid, qty) ->
        products.find { it.id == pid }?.let { p ->
            CartLine(p.id, p.skuId, p.title, p.coverUrl, p.price, qty)
        }
    }

    fun cartSubtotal(): Double = cartLines().sumOf { it.lineAmount }

    fun cartSubtotalLabel(): String = formatYuan(cartSubtotal())

    fun clearCart() {
        cart.clear()
    }

    // --- 下单 ---

    fun placeOrder(): Result<Order> {
        if (cart.isEmpty()) return Result.failure(IllegalStateException("购物车为空"))
        val lines = cartLines()
        val total = lines.sumOf { it.lineAmount }
        val points = lines.sumOf { products.find { p -> p.id == it.productId }?.points ?: 0 }
        val order = buildOrder(lines, total, points)
        cart.clear()
        return Result.success(order)
    }

    fun placeOrder(
        skuId: Int,
        qty: Int,
        receiverName: String = "",
        receiverPhone: String = "",
        receiverAddress: String = "",
    ): Result<Order> {
        val safeQty = qty.coerceAtLeast(1)
        val product = products.find { it.skuId == skuId }
            ?: return Result.failure(NoSuchElementException("sku=$skuId"))
        if (safeQty > product.stockQty) {
            return Result.failure(IllegalStateException("库存不足，仅剩 ${product.stockQty} 件"))
        }
        val line = CartLine(product.id, product.skuId, product.title, product.coverUrl, product.price, safeQty)
        return Result.success(buildOrder(listOf(line), line.lineAmount, product.points ?: 0, receiverName, receiverPhone, receiverAddress))
    }

    private fun buildOrder(
        lines: List<CartLine>,
        total: Double,
        points: Int,
        receiverName: String = "",
        receiverPhone: String = "",
        receiverAddress: String = "",
    ): Order {
        val id = orderTable.size + 1
        val order = Order(
            orderId = id,
            orderNo = "NO2026%04d".format(id),
            status = OrderStatus.unpaid,
            amount = total,
            createdAt = "2026-05-0${(id % 9) + 1}",
            items = lines.mapIndexed { index, line ->
                OrderItem(
                    itemId = id * 10 + index + 1,
                    orderId = id,
                    productTitle = line.title,
                    coverUrl = line.coverUrl,
                    qty = line.qty,
                    lineAmount = line.lineAmount,
                    contentUrl = if (line.skuId % 2 == 0) "https://card.example.com/c/OD$id" else "",
                )
            },
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            receiverAddress = receiverAddress,
            payDeadlineAt = "2026-05-0${(id % 9) + 1} 23:59",
            totalPoints = points,
        )
        orderTable.add(order)
        return order
    }

    // --- 订单查询与流转 ---

    fun orders(): Result<List<Order>> = all { true }.map { orderTable.toList() }

    fun orders(status: Int): Result<List<Order>> = all { true }.map { orderTable.filter { o -> o.status == status } }

    fun orderDetail(orderId: Int): Result<Order> {
        val list = orders().getOrElse { return Result.failure(it) }
        return list.firstOrNull { it.orderId == orderId }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("order=$orderId"))
    }

    // 支付：待付款 -> 待发货
    fun payOrder(orderId: Int, channel: Int = MallPayChannel.wechat): Result<Order> = mutateOrder(orderId) { order ->
        if (order.status != OrderStatus.unpaid) {
            Result.failure(IllegalStateException("订单当前状态不可支付"))
        } else {
            Result.success(order.copy(status = OrderStatus.paid, payChannel = channel, paidAt = "2026-05-09 10:00"))
        }
    }

    // 状态机推进：待付款 -> 待发货 -> 待收货 -> 已完成
    fun advanceOrder(orderId: Int): Result<Order> = mutateOrder(orderId) { order ->
        val next = OrderStatus.next(order.status)
            ?: return@mutateOrder Result.failure(IllegalStateException("订单已是终态，无法继续流转"))
        Result.success(order.copy(status = next))
    }

    fun cancelOrder(orderId: Int): Result<Order> = mutateOrder(orderId) { order ->
        if (order.status == OrderStatus.completed || order.status == OrderStatus.cancelled) {
            Result.failure(IllegalStateException("订单已完成或已取消"))
        } else {
            Result.success(order.copy(status = OrderStatus.cancelled))
        }
    }

    private fun mutateOrder(orderId: Int, block: (Order) -> Result<Order>): Result<Order> {
        val index = orderTable.indexOfFirst { it.orderId == orderId }
        if (index < 0) return Result.failure(NoSuchElementException("order=$orderId"))
        val updated = block(orderTable[index]).getOrElse { return Result.failure(it) }
        orderTable[index] = updated
        return Result.success(updated)
    }
}

object MallStore {
    val repo: MallRepository = MallRepository()
}
