package com.example.kuikly.data.settings

import com.example.kuikly.data.BaseMockRepository
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.math.pow

/** 收货地址（port of source AddressModel）。 */
data class Address(
    val id: String,
    val name: String,
    val phone: String,
    val province: String,
    val city: String,
    val detail: String,
    val isDefault: Boolean = false,
    val district: String = "",
    val postalCode: String = "",
    val label: String = "",
) {
    /** 省市区 + 详细地址拼接，port of source fullAddress。 */
    val fullAddress: String
        get() {
            val head = listOf(province, city, district).filter { it.isNotBlank() }.joinToString(" ")
            return if (head.isBlank()) detail else "$head $detail"
        }
}

/** 发票（port of source DealInvoiceItem）。 */
data class Invoice(
    val id: String,
    val title: String,
    val taxNo: String,
    val type: String, // "personal" | "company"
    val amount: Double,
    val status: String = InvoiceStatus.Pending,
    val submittedAt: String = "",
    val rejectReason: String = "",
    val customerPhone: String = "",
    val ratingStars: Int = 0,
) {
    val statusLabel: String
        get() = InvoiceStatus.labelOf(status)
}

/** 发票审核状态枚举（用常量代替 enum，便于跨端序列化）。 */
object InvoiceStatus {
    const val Pending = "pending"
    const val Approved = "approved"
    const val Rejected = "rejected"

    val all: List<String> = listOf(Pending, Approved, Rejected)

    fun labelOf(status: String): String = when (status) {
        Approved -> "已通过"
        Rejected -> "未通过"
        else -> "待审核"
    }
}

/** 发票顶部统计（port of source DealInvoiceStats）。 */
data class InvoiceStats(
    val uploaded: Int,
    val pending: Int,
    val approved: Int,
    val rejected: Int,
)

/** 采购计算器报价（port of source PurchaseQuote）。 */
data class PurchaseQuote(
    val qty: Int,
    val unitPrice: Double,
    val discount: Double, // 0..1
    val freight: Double,
) {
    val subtotal: Double get() = qty * unitPrice * (1 - discount)
    val total: Double get() = (subtotal + freight).coerceAtLeast(0.0)
}

/** 金融产品（port of source FinanceProduct）。 */
data class FinanceProduct(
    val id: Int,
    val name: String,
    val annualRatePercent: Double,
    val allowedTermsMonths: List<Int>,
    val minDownPaymentPercent: Double,
    val oneTimeFee: Double,
)

/** 报价明细行（port of source PurchaseQuoteLine）。 */
data class PurchaseCalcLine(
    val code: String,
    val label: String,
    val amount: Double,
)

/** 购车计算器报价结果（port of source PurchaseQuote）。 */
data class PurchaseCalcQuote(
    val mode: String,
    val productId: Int,
    val productName: String,
    val barePrice: Double,
    val taxablePrice: Double,
    val downPayment: Double,
    val loanAmount: Double,
    val termMonths: Int,
    val annualRatePercent: Double,
    val monthlyPayment: Double,
    val totalInterest: Double,
    val totalRepayment: Double,
    val initialPayment: Double,
    val totalDue: Double,
    val lines: List<PurchaseCalcLine>,
)

/** 购车计算器：纯函数，方便单测覆盖除零 / 负数等边界。 */
object PurchaseCalculator {
    const val MODE_CASH = "cash"
    const val MODE_LOAN = "loan"

    /** 购置税：计税价 / 11.3（含税价换算）。 */
    fun purchaseTax(taxablePrice: Double): Double {
        if (taxablePrice <= 0.0) return 0.0
        return taxablePrice / 11.3
    }

    /** 等额本息月供；期数为 0 或利率为 0 时退化为均分本金，不会除零。 */
    fun monthlyPayment(principal: Double, annualRatePercent: Double, termMonths: Int): Double {
        if (principal <= 0.0 || termMonths <= 0) return 0.0
        val monthlyRate = annualRatePercent / 100.0 / 12.0
        if (monthlyRate <= 0.0) return principal / termMonths.toDouble()
        val factor = (1.0 + monthlyRate).pow(termMonths.toDouble())
        val denominator = factor - 1.0
        if (denominator <= 0.0) return principal / termMonths.toDouble()
        return principal * monthlyRate * factor / denominator
    }

    /** 总利息 = 月供 × 期数 − 本金，负数兜底为 0。 */
    fun totalInterest(monthlyPayment: Double, termMonths: Int, principal: Double): Double {
        if (termMonths <= 0) return 0.0
        return (monthlyPayment * termMonths.toDouble() - principal).coerceAtLeast(0.0)
    }

    fun defaultProducts(): List<FinanceProduct> = listOf(
        FinanceProduct(1, "厂家贴息 12 期", 2.99, listOf(12, 24, 36), 20.0, 1500.0),
        FinanceProduct(2, "银行标准贷 36 期", 4.75, listOf(24, 36, 48, 60), 30.0, 2000.0),
        FinanceProduct(3, "厂家免息 24 期", 0.0, listOf(12, 24), 50.0, 3000.0),
    )
}

/** 个性化设置项（port of source PersonalizedSettingsController）。 */
data class PersonalizedSetting(
    val key: String,
    val label: String,
    val enabled: Boolean,
    val desc: String = "",
    val kind: String = SettingKind.Switch,
    val value: String = "",
    val options: List<String> = emptyList(),
)

/** 设置项形态：开关 or 选项。 */
object SettingKind {
    const val Switch = "switch"
    const val Option = "option"
}

/** 收货地址校验规则（port of source WysContactValidators）。 */
object AddressRules {
    const val MAX_NAME_LENGTH = 20

    fun digitsOnly(raw: String): String = raw.filter { it in '0'..'9' }

    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.length <= MAX_NAME_LENGTH
    }

    /**
     * 11 位手机号，以 1 开头且第二位为 3~9
     * （与 feature-auth 的 [com.example.kuikly.data.auth.AuthRules.isValidPhone] 语义保持一致，
     * 排除 "12800000000" 这类无效号段）。
     */
    fun isValidPhone(phone: String): Boolean {
        val digits = digitsOnly(phone)
        return digits.length == 11 && digits.startsWith("1") && digits[1] in '3'..'9'
    }

    /** 返回 null 表示通过，否则返回错误文案。 */
    fun validate(address: Address): String? {
        if (!isValidName(address.name)) {
            return if (address.name.isBlank()) "请填写收货人" else "收货人不超过 $MAX_NAME_LENGTH 个字"
        }
        if (!isValidPhone(address.phone)) return "请输入以 1 开头的 11 位手机号"
        if (address.detail.isBlank()) return "请填写详细地址"
        if (address.province.isBlank() || address.city.isBlank()) return "请选择所在地区"
        return null
    }
}

class SettingsRepository : BaseMockRepository<Address>() {
    private val seedAddresses = listOf(
        Address("a1", "张三", "13800000001", "北京市", "北京市", "建国路88号", true, "朝阳区", "100022", "家"),
        Address("a2", "李四", "13800000002", "上海市", "上海市", "世纪大道100号", false, "浦东新区", "200120", "公司"),
        Address("a3", "王五", "13800000003", "广东省", "广州市", "天河路200号", false, "天河区", "510620", "学校"),
    )

    private val addressBook: MutableList<Address> = seedAddresses.toMutableList()

    private val invoiceList: MutableList<Invoice> = mutableListOf(
        Invoice("i1", "个人", "", "personal", 199.0, InvoiceStatus.Pending, "2024-05-12", customerPhone = "138****0001"),
        Invoice("i2", "腾讯科技", "91440300MA5XXXX", "company", 1299.0, InvoiceStatus.Approved, "2024-05-08", customerPhone = "138****0002", ratingStars = 5),
        Invoice("i3", "深圳优车", "91440300MA6YYYY", "company", 880.0, InvoiceStatus.Rejected, "2024-05-03", "发票影像模糊", "138****0003"),
        Invoice("i4", "个人", "", "personal", 460.0, InvoiceStatus.Approved, "2024-04-28", customerPhone = "138****0004", ratingStars = 4),
    )

    private val personalizedList: MutableList<PersonalizedSetting> = mutableListOf(
        PersonalizedSetting("push", "消息推送", true, "接收订单与系统通知"),
        PersonalizedSetting("teaching", "教学模式", false, "新手引导与操作提示"),
        PersonalizedSetting("content", "内容推荐", true, "根据浏览记录推荐内容"),
        PersonalizedSetting("ad", "广告推荐", true, "展示个性化广告"),
        PersonalizedSetting("oral", "口语打分", true, "录音练习自动评分"),
        PersonalizedSetting("cellular", "流量播放提醒", false, "移动网络播放视频时提醒"),
        PersonalizedSetting("upload", "上传状态监控", false, "后台上传任务状态提示"),
        PersonalizedSetting(
            key = "eye",
            label = "护眼模式",
            enabled = false,
            desc = "夜间自动降低屏幕亮度",
            kind = SettingKind.Option,
            value = "关闭",
            options = listOf("关闭", "开启", "跟随系统"),
        ),
    )

    private val products: List<FinanceProduct> = PurchaseCalculator.defaultProducts()

    override fun seed(): List<Address> = addressBook
    override fun idOf(item: Address): String = item.id

    // ---------- 收货地址 CRUD ----------

    fun addressDetail(id: String): Result<Address> = byId(id)

    fun addAddress(a: Address): Result<Address> {
        val error = AddressRules.validate(a)
        if (error != null) return Result.failure(IllegalArgumentException(error))
        val saved = if (a.id.isBlank()) a.copy(id = nextAddressId()) else a
        if (saved.isDefault) clearDefault()
        addressBook.add(0, saved)
        return Result.success(saved)
    }

    fun updateAddress(a: Address): Result<Address> {
        if (a.id.isBlank()) return Result.failure(IllegalArgumentException("地址 id 不能为空"))
        val error = AddressRules.validate(a)
        if (error != null) return Result.failure(IllegalArgumentException(error))
        val index = addressBook.indexOfFirst { it.id == a.id }
        if (index < 0) return Result.failure(NoSuchElementException("id=${a.id}"))
        if (a.isDefault) clearDefault()
        addressBook[index] = a
        return Result.success(a)
    }

    fun deleteAddress(id: String): Result<Boolean> {
        if (id.isBlank()) return Result.failure(IllegalArgumentException("地址 id 不能为空"))
        val removed = addressBook.removeAll { it.id == id }
        if (!removed) return Result.failure(NoSuchElementException("id=$id"))
        if (addressBook.isNotEmpty() && addressBook.none { it.isDefault }) {
            addressBook[0] = addressBook[0].copy(isDefault = true)
        }
        return Result.success(true)
    }

    /** 设为默认地址：互斥切换，其余项自动取消默认。 */
    fun setDefaultAddress(id: String): Result<Address> {
        val index = addressBook.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("id=$id"))
        clearDefault()
        val updated = addressBook[index].copy(isDefault = true)
        addressBook[index] = updated
        return Result.success(updated)
    }

    private fun clearDefault() {
        for (i in addressBook.indices) {
            if (addressBook[i].isDefault) {
                addressBook[i] = addressBook[i].copy(isDefault = false)
            }
        }
    }

    private fun nextAddressId(): String {
        val max = addressBook
            .map { it.id.removePrefix("a").toIntOrNull() ?: 0 }
            .maxOrNull() ?: 0
        return "a${max + 1}"
    }

    // ---------- 发票 ----------

    fun invoices(status: String = ""): Result<List<Invoice>> =
        if (MockBackend.scenario == MockScenario.Error) {
            Result.failure(IllegalStateException("mock backend error"))
        } else {
            val list = if (status.isBlank()) invoiceList else invoiceList.filter { it.status == status }
            Result.success(list)
        }

    fun invoiceStats(): Result<InvoiceStats> = all { true }.map {
        InvoiceStats(
            uploaded = invoiceList.size,
            pending = invoiceList.count { v -> v.status == InvoiceStatus.Pending },
            approved = invoiceList.count { v -> v.status == InvoiceStatus.Approved },
            rejected = invoiceList.count { v -> v.status == InvoiceStatus.Rejected },
        )
    }

    /** 重新提交被驳回的发票。 */
    fun resubmitInvoice(id: String): Result<Invoice> {
        val index = invoiceList.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("id=$id"))
        val updated = invoiceList[index].copy(
            status = InvoiceStatus.Pending,
            rejectReason = "",
            submittedAt = "2024-06-01",
        )
        invoiceList[index] = updated
        return Result.success(updated)
    }

    // ---------- 个性化设置 ----------

    fun personalized(): Result<List<PersonalizedSetting>> = Result.success(personalizedList.toList())

    fun setPersonalized(key: String, enabled: Boolean): Result<PersonalizedSetting> {
        val index = personalizedList.indexOfFirst { it.key == key }
        if (index < 0) return Result.failure(NoSuchElementException("key=$key"))
        val updated = personalizedList[index].copy(enabled = enabled)
        personalizedList[index] = updated
        return Result.success(updated)
    }

    fun setPersonalizedValue(key: String, value: String): Result<PersonalizedSetting> {
        val index = personalizedList.indexOfFirst { it.key == key }
        if (index < 0) return Result.failure(NoSuchElementException("key=$key"))
        val current = personalizedList[index]
        val updated = current.copy(value = value, enabled = value != "关闭")
        personalizedList[index] = updated
        return Result.success(updated)
    }

    // ---------- 计算器 ----------

    fun financeProducts(): List<FinanceProduct> = products

    fun findProduct(id: Int): FinanceProduct? = products.firstOrNull { it.id == id }

    /** 采购计算器：根据数量/单价/折扣/运费算总价（保留旧签名）。 */
    fun quote(qty: Int, unitPrice: Double, discount: Double, freight: Double): PurchaseQuote {
        return PurchaseQuote(qty, unitPrice, discount.coerceIn(0.0, 1.0), freight)
    }

    /**
     * 购车计算器报价：全款 / 贷款两种模式，失败时返回可读错误。
     * 裸车价、计税价、期数、首付比例等非法入参全部走 Result.failure。
     */
    fun calcQuote(
        mode: String,
        barePrice: Double,
        taxablePrice: Double,
        downPayment: Double = 0.0,
        termMonths: Int = 0,
        productId: Int = 0,
        disableCommercial: Boolean = false,
    ): Result<PurchaseCalcQuote> {
        if (barePrice <= 0.0) return Result.failure(IllegalArgumentException("请输入有效裸车价"))
        if (taxablePrice < 0.0) return Result.failure(IllegalArgumentException("计税价格不能为负"))
        val taxable = if (taxablePrice > 0.0) taxablePrice else barePrice
        val product = findProduct(productId)
        val productName = product?.name ?: "全款自购"
        val rate = product?.annualRatePercent ?: 0.0
        val oneTimeFee = product?.oneTimeFee ?: 0.0

        val tax = PurchaseCalculator.purchaseTax(taxable)
        val compulsory = 950.0
        val commercial = if (disableCommercial) 0.0 else taxable * 0.035

        if (mode == PurchaseCalculator.MODE_LOAN) {
            if (product == null) return Result.failure(IllegalArgumentException("请选择金融产品"))
            if (termMonths <= 0) return Result.failure(IllegalArgumentException("请选择贷款期数"))
            if (downPayment < 0.0) return Result.failure(IllegalArgumentException("首付不能为负"))
            val minDown = barePrice * product.minDownPaymentPercent / 100.0
            if (downPayment < minDown) {
                return Result.failure(IllegalArgumentException("首付不能低于 %.0f%%".format(product.minDownPaymentPercent)))
            }
            if (downPayment >= barePrice) return Result.failure(IllegalArgumentException("首付不能高于裸车价"))

            val loanAmount = barePrice - downPayment
            val monthly = PurchaseCalculator.monthlyPayment(loanAmount, rate, termMonths)
            val repayment = monthly * termMonths.toDouble()
            val interest = PurchaseCalculator.totalInterest(monthly, termMonths, loanAmount)
            val initial = downPayment + tax + compulsory + commercial + oneTimeFee
            val lines = listOf(
                PurchaseCalcLine("down", "首付款", downPayment),
                PurchaseCalcLine("loan", "贷款本金", loanAmount),
                PurchaseCalcLine("tax", "购置税", tax),
                PurchaseCalcLine("compulsory", "交强险", compulsory),
                PurchaseCalcLine("commercial", "商业险", commercial),
                PurchaseCalcLine("fee", "金融服务费", oneTimeFee),
                PurchaseCalcLine("monthly", "月供 × $termMonths 期", monthly),
                PurchaseCalcLine("interest", "总利息", interest),
            )
            return Result.success(
                PurchaseCalcQuote(
                    mode = PurchaseCalculator.MODE_LOAN,
                    productId = product.id,
                    productName = productName,
                    barePrice = barePrice,
                    taxablePrice = taxable,
                    downPayment = downPayment,
                    loanAmount = loanAmount,
                    termMonths = termMonths,
                    annualRatePercent = rate,
                    monthlyPayment = monthly,
                    totalInterest = interest,
                    totalRepayment = repayment,
                    initialPayment = initial,
                    totalDue = initial + repayment,
                    lines = lines,
                ),
            )
        }

        val initial = barePrice + tax + compulsory + commercial + oneTimeFee
        val lines = listOf(
            PurchaseCalcLine("bare", "裸车价", barePrice),
            PurchaseCalcLine("tax", "购置税", tax),
            PurchaseCalcLine("compulsory", "交强险", compulsory),
            PurchaseCalcLine("commercial", "商业险", commercial),
            PurchaseCalcLine("fee", "服务费", oneTimeFee),
        )
        return Result.success(
            PurchaseCalcQuote(
                mode = PurchaseCalculator.MODE_CASH,
                productId = productId,
                productName = productName,
                barePrice = barePrice,
                taxablePrice = taxable,
                downPayment = 0.0,
                loanAmount = 0.0,
                termMonths = 0,
                annualRatePercent = rate,
                monthlyPayment = 0.0,
                totalInterest = 0.0,
                totalRepayment = 0.0,
                initialPayment = initial,
                totalDue = initial,
                lines = lines,
            ),
        )
    }
}

object SettingsStore {
    val repo: SettingsRepository = SettingsRepository()
}
