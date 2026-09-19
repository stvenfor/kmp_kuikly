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
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
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
            var customer by remember { mutableStateOf<InvoiceCustomer?>(null) }
            var phase by remember { mutableStateOf(UploadPhase.Editing) }
            var auditStatus by remember { mutableStateOf(AuditStatus.PendingReview) }
            var rating by remember { mutableStateOf(5) }

            val isEditing = phase == UploadPhase.Editing
            val isUploading = phase == UploadPhase.Uploading
            val isDetail = phase == UploadPhase.Detail
            val showCustomerPicker = isEditing && !isUploading
            val showSubmitButton = !isDetail || auditStatus == AuditStatus.Rejected

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
                            if (showCustomerPicker) {
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
                            }
                            UploadImageArea(
                                hasImage = hasImage,
                                isEditing = isEditing,
                                isUploading = isUploading,
                                showPending = isDetail &&
                                    auditStatus == AuditStatus.PendingReview,
                                showStamp = isDetail && (
                                    auditStatus == AuditStatus.ApprovedPendingRating ||
                                        auditStatus == AuditStatus.Rated
                                    ),
                                showReupload = isDetail &&
                                    auditStatus == AuditStatus.Rejected,
                                onPick = {
                                    if (!isUploading) {
                                        Utils.currentBridgeModule().toast("「选择发票」开发中")
                                        hasImage = true
                                    }
                                },
                                onClear = { hasImage = false },
                            )
                            if (isDetail || customer != null) {
                                Spacer(Modifier.height(16.dp))
                                InfoSection(
                                    customer = customer,
                                    isDetail = isDetail,
                                    auditStatus = auditStatus,
                                    rating = rating,
                                    rejectReason = "发票日期与合同日期相差超过 7 天",
                                )
                            }
                        }
                    }
                }
                if (showSubmitButton) {
                    val canSubmit = when {
                        isUploading -> false
                        isDetail && auditStatus == AuditStatus.Rejected -> hasImage
                        isEditing -> customer != null && hasImage
                        else -> false
                    }
                    SubmitBar(
                        enabled = canSubmit,
                        uploading = isUploading,
                        bottomInset = bottom,
                        onSubmit = {
                            if (canSubmit && !isUploading) {
                                phase = UploadPhase.Uploading
                                phase = UploadPhase.Detail
                                auditStatus = AuditStatus.PendingReview
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
 * Flutter `DealInvoiceUploadImageArea`：新建虚线框 / 待审核占位 / 预览 Stack（1.45 宽高比）+
 * stamp / reupload 蒙层。
 */
@Composable
private fun UploadImageArea(
    hasImage: Boolean,
    isEditing: Boolean,
    isUploading: Boolean,
    showPending: Boolean,
    showStamp: Boolean,
    showReupload: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    when {
        !hasImage && isEditing -> DashedUploadBox(
            uploading = isUploading,
            onTap = onPick,
        )
        showPending -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.45f)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            // Flutter `Icons.file_upload_outlined` size 56。
            Text("⬆", fontSize = 56.sp, color = InvoicePalette.hintColor)
        }
        else -> InvoiceImageStack(
            hasImage = hasImage,
            isEditing = isEditing,
            isUploading = isUploading,
            showStamp = showStamp,
            showReupload = showReupload,
            onPick = onPick,
            onClear = onClear,
        )
    }
}

/** Flutter `_DashedUploadBox`：虚线边框（Kuikly 无 dashed → 浅蓝实线）+ 渐变圆 + 文案。 */
@Composable
private fun DashedUploadBox(uploading: Boolean, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, InvoicePalette.dashedBorder, RoundedCornerShape(8.dp))
            .clickable(enabled = !uploading, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 36.dp),
        ) {
            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = InvoicePalette.ctaBlue,
                )
            } else {
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
                    Text("+", fontSize = 32.sp, color = Color.White)
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
    }
}

/** Flutter 预览 Stack：`AspectRatio(1.45)` + 网络图占位 + 上传遮罩 / stamp / reupload / 删除。 */
@Composable
private fun InvoiceImageStack(
    hasImage: Boolean,
    isEditing: Boolean,
    isUploading: Boolean,
    showStamp: Boolean,
    showReupload: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.45f)
            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
            .clickable(
                enabled = showReupload || (isEditing && !isUploading),
                onClick = onPick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (hasImage) {
            Text("🧾", fontSize = 56.sp, color = InvoicePalette.hintColor)
        }
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x61000000)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            }
        }
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
        if (isEditing && hasImage && !isUploading) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0x8A000000), RoundedCornerShape(16.dp))
                    .clickable(onClick = onClear)
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", fontSize = 18.sp, color = Color.White)
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
    customer: InvoiceCustomer?,
    isDetail: Boolean,
    auditStatus: AuditStatus,
    rating: Int,
    rejectReason: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp)),
    ) {
        if (customer != null) {
            InfoRow("购车客户", customer.display)
        }
        if (isDetail) {
            InfoRow("提交时间", "2024-12-10 14:32:00")
            InfoRow("审核状态", auditStatus.label, valueColor = auditStatus.color)
            if (auditStatus == AuditStatus.Rejected && rejectReason != null) {
                InfoRow("未通过原因", rejectReason, valueColor = InvoicePalette.stampRed)
            }
            if (auditStatus == AuditStatus.Rated) {
                InfoRowWithTrailing("客户评价") {
                    StarRow(rating)
                }
            }
        }
    }
}

/** Flutter `DealInvoiceInfoRow`：label 88dp + value + 底部分割线。 */
@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = InvoicePalette.valueColor) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                label,
                fontSize = 15.sp,
                color = InvoicePalette.titleBlack,
                modifier = Modifier.width(88.dp),
            )
            Text(
                value,
                fontSize = 15.sp,
                color = valueColor,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(InvoicePalette.divider),
        )
    }
}

@Composable
private fun InfoRowWithTrailing(label: String, trailing: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                label,
                fontSize = 15.sp,
                color = InvoicePalette.titleBlack,
                modifier = Modifier.width(88.dp),
            )
            trailing()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(InvoicePalette.divider),
        )
    }
}

@Composable
private fun StarRow(stars: Int) {
    Row {
        repeat(5) { idx ->
            Text(
                if (idx < stars) "★" else "☆",
                fontSize = 22.sp,
                color = if (idx < stars) InvoicePalette.ratingStar else Color(0xFFE0E0E0),
            )
        }
    }
}

/** Flutter `_SubmitBar`：白底 + `48·r8` 「提交审核」CTA（上传中 spinner / 禁用灰）。 */
@Composable
private fun SubmitBar(
    enabled: Boolean,
    uploading: Boolean,
    bottomInset: Float,
    onSubmit: () -> Unit,
) {
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
            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Text(
                    "提交审核",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}