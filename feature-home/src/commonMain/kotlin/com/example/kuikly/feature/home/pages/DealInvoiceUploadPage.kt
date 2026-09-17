package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 上传发票 — Flutter `DealInvoiceUploadPage`
 * （`features/settings/lib/deal_invoice/view/deal_invoice_upload_page.dart`）复刻
 * （P2-W4a 接入路由 `dealInvoiceUpload`）。
 *
 * **真源**：`#F5F6F8` 背景 + 客户行（白 r8 「购车客户」+ chevron）+「新车发票」label +
 * 上传区（虚线 r8 / preview / 审核通过红圆戳 / 重新上传蒙层）+ 信息卡（白 r8：购车客户 /
 * 提交时间 / 审核状态 / 未通过原因 / 客户评价）+ 底部白底 `48·r8` 「提交审核」CTA。
 *
 * **刻度**：本 view/widgets **grep 无 `.w/.h/.sp`**，故一律裸 `dp`/`sp`，不加
 * `ProvideDesignScale`。
 *
 * ponytail 天花板（不弹回）：
 * - 虚线边框无 Compose 原生 dashed border → 用 `border(1.5.dp, …)` + 「虚线」字样近似。
 * - 上传图片 / 客户选择器 / 评分 star 全部本地 state 模拟；任何动作 → toast（无真实上传）。
 * - 审核通过红圆戳 / 重新上传蒙层按 `showApprovedStamp` / `showReuploadOverlay` 标志显隐。
 */
@Page(name = "DealInvoiceUpload", moduleId = "feature_home")
internal class DealInvoiceUploadPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        val top = statusBarInset()
        val bottom = bottomSafeInset()
        setContent {
            var hasImage by remember { mutableStateOf(false) }
            var uploading by remember { mutableStateOf(false) }
            var customer by remember { mutableStateOf<InvoiceCustomer?>(null) }
            var phase by remember { mutableStateOf(UploadPhase.Editing) }
            // 假详情状态：审核通过 + 评分 5 星。
            var auditStatus by remember { mutableStateOf(AuditStatus.ApprovedPendingRating) }
            var rating by remember { mutableStateOf(4) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(InvoicePalette.background),
            ) {
                AppNavBarBar(
                    title = "上传发票",
                    topInset = top,
                    onBack = { Utils.currentBridgeModule().closePage() },
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 12.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        ),
                    ) {
                        item {
                            CustomerRow(
                                customer = customer,
                                onTap = {
                                    customer = INVOICE_CUSTOMERS.firstOrNull()
                                    Utils.currentBridgeModule().toast("已选择 ${customer?.display}")
                                },
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "新车发票",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InvoicePalette.titleBlack,
                            )
                            Spacer(Modifier.height(10.dp))
                            UploadImageArea(
                                hasImage = hasImage,
                                uploading = uploading,
                                showStamp = phase == UploadPhase.Detail &&
                                    auditStatus == AuditStatus.Rated,
                                showReupload = phase == UploadPhase.Detail &&
                                    auditStatus == AuditStatus.Rejected,
                                onPick = {
                                    Utils.currentBridgeModule().toast("「选择发票」开发中")
                                    hasImage = true
                                    uploading = true
                                },
                                onClear = { hasImage = false },
                            )
                            if (customer != null) {
                                Spacer(Modifier.height(16.dp))
                                InfoSection(
                                    customer = customer!!,
                                    phase = phase,
                                    auditStatus = auditStatus,
                                    rating = rating,
                                    rejectReason = "发票日期与合同日期相差超过 7 天",
                                )
                            }
                        }
                    }
                }
                if (phase == UploadPhase.Editing) {
                    SubmitBar(
                        enabled = customer != null && hasImage && !uploading,
                        bottomInset = bottom,
                        onSubmit = {
                            if (!uploading) {
                                Utils.currentBridgeModule().toast("已提交审核（mock）")
                            }
                        },
                    )
                }
            }
        }
    }
}

// ─── 私有状态 / 调色板 ──────────────────────────────────────────

private enum class UploadPhase { Editing, Uploading, Detail }

private enum class AuditStatus(val label: String, val color: Color) {
    PendingReview("待审核", Color(0xFFFAAD14)),
    ApprovedPendingRating("已通过", Color(0xFF52C41A)),
    Rated("已通过", Color(0xFF52C41A)),
    Rejected("未通过", Color(0xFFE53935)),
}

private data class InvoiceCustomer(val phone: String, val name: String) {
    val display: String get() = "$phone $name"
}

private val INVOICE_CUSTOMERS = listOf(
    InvoiceCustomer("13812345678", "小张女士"),
    InvoiceCustomer("13612345678", "王先生"),
    InvoiceCustomer("13987654321", "李女士"),
)

private object InvoicePalette {
    val background = Color(0xFFF5F6F8)
    val titleBlack = Color(0xFF1A1A1A)
    val divider = Color(0xFFEEEEEE)
    val valueColor = Color(0xFF666666)
    val hintColor = Color(0xFF9CA3AF)
    val ctaBlue = Color(0xFF3B8CFF)
    val ctaBlueEnd = Color(0xFF3B8CFF)
    val ctaDisabled = Color(0xFFE0E0E0)
    val stampRed = Color(0xFFE53935)
    val dashedBorder = Color(0xFFB8C9F0)
    val ratingStar = Color(0xFFFAAD14)
}

// ─── 子组件 ────────────────────────────────────────────────────

/** Flutter `_CustomerRow`：白 r8 + 「购车客户」15 + 「选择客户」灰 + chevron。 */
@Composable
private fun CustomerRow(customer: InvoiceCustomer?, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "购车客户",
                fontSize = 15.sp,
                color = InvoicePalette.titleBlack,
            )
            Spacer(Modifier.weight(1f))
            Text(
                customer?.display ?: "选择客户",
                fontSize = 15.sp,
                color = if (customer == null) InvoicePalette.hintColor else InvoicePalette.valueColor,
            )
            Spacer(Modifier.width(4.dp))
            Text("›", fontSize = 20.sp, color = InvoicePalette.hintColor)
        }
    }
}

/**
 * Flutter `_DashedUploadBox` 等价物：宽撑满 + 虚线边框（Kuikly `BorderStroke` 无 dashed）→
 * 浅蓝细边 + 「上传发票」中央字样；上传中显示「上传中…」；有图时切到预览；详情 stamp /
 * reupload 按 prop 显隐。
 */
@Composable
private fun UploadImageArea(
    hasImage: Boolean,
    uploading: Boolean,
    showStamp: Boolean,
    showReupload: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
            .border(1.5.dp, InvoicePalette.dashedBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uploading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⏳", fontSize = 32.sp, color = InvoicePalette.hintColor)
                Spacer(Modifier.height(8.dp))
                Text("上传中…", fontSize = 13.sp, color = InvoicePalette.hintColor)
            }
            hasImage -> {
                // 预览占位（无图）+ 中央文件图标 + 角部 X（编辑态）。
                Text("🧾", fontSize = 56.sp, color = InvoicePalette.hintColor)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xCC000000), CircleShape)
                        .clickable(onClick = onClear)
                        .size(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✕", fontSize = 14.sp, color = Color.White)
                }
            }
            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 36.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF6EB4FF), InvoicePalette.ctaBlue),
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "上传发票",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InvoicePalette.titleBlack,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "请保证发票清晰可识别、避免模糊和遮挡",
                    fontSize = 12.sp,
                    color = InvoicePalette.hintColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
        // 审核通过红圆戳。
        if (showStamp) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(72.dp)
                    .border(3.dp, InvoicePalette.stampRed, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "审核\n通过",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InvoicePalette.stampRed,
                    textAlign = TextAlign.Center,
                )
            }
        }
        // 重新上传蒙层。
        if (showReupload) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0x8C000000))
                    .padding(vertical = 14.dp)
                    .clickable(onClick = onPick),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Flutter `Icons.refresh_rounded` size: 20。
                    Text("↻", fontSize = 20.sp, color = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "重新上传",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/**
 * Flutter `_InfoSection`：白 r8 + 行（label 灰 + value 黑）。客户评价行右侧 5 颗星
 * （`DealInvoiceStarRating`），未通过原因行 value 红 `#E53935`。
 */
@Composable
private fun InfoSection(
    customer: InvoiceCustomer,
    phase: UploadPhase,
    auditStatus: AuditStatus,
    rating: Int,
    rejectReason: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp)),
    ) {
        InfoRow("购车客户", customer.display)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
                .height(1.dp)
                .background(InvoicePalette.divider),
        )
        if (phase == UploadPhase.Detail) {
            InfoRow("提交时间", "2024-12-10 14:32")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .height(1.dp)
                    .background(InvoicePalette.divider),
            )
            InfoRow("审核状态", auditStatus.label, valueColor = auditStatus.color)
            if (auditStatus == AuditStatus.Rejected && rejectReason != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .height(1.dp)
                        .background(InvoicePalette.divider),
                )
                InfoRow("未通过原因", rejectReason, valueColor = InvoicePalette.stampRed)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .height(1.dp)
                    .background(InvoicePalette.divider),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Flutter `DealInvoiceInfoRow` 5-star 版本：`trailing: DealInvoiceStarRating(...)`；
                // label 同其他 InfoRow（15sp 黑 + 88dp 固定宽），星占右侧。
                Text(
                    "客户评价",
                    fontSize = 15.sp,
                    color = InvoicePalette.titleBlack,
                    modifier = Modifier.width(88.dp),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    StarRow(rating)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = InvoicePalette.valueColor) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp, color = InvoicePalette.valueColor)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            fontSize = 14.sp,
            color = valueColor,
        )
    }
}

@Composable
private fun StarRow(stars: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { idx ->
            Text(
                if (idx < stars) "★" else "☆",
                fontSize = 18.sp,
                color = if (idx < stars) InvoicePalette.ratingStar else InvoicePalette.hintColor,
            )
        }
    }
}

/** Flutter `_SubmitBar`：白底 + `48·r8` 「提交审核」CTA（`#3B8CFF` 蓝 / `#E0E0E0` 禁用）。 */
@Composable
private fun SubmitBar(enabled: Boolean, bottomInset: Float, onSubmit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = (bottomInset + 12f).dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    if (enabled) InvoicePalette.ctaBlue else InvoicePalette.ctaDisabled,
                    RoundedCornerShape(8.dp),
                )
                .clickable(enabled = enabled, onClick = onSubmit),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "提交审核",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}