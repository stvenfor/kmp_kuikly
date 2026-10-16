package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.pay.MembershipPlan
import com.example.kuikly.data.pay.MembershipTier
import com.example.kuikly.data.pay.PayMethod
import com.example.kuikly.data.pay.PayStore
import com.example.kuikly.design.KButton
import com.example.kuikly.design.KCard
import com.example.kuikly.design.KChip
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 会员续费页：套餐对比 + 支付渠道选择 + 下单/支付闭环。
 */
@Page("MembershipRenew")
internal class MembershipRenewPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var plans by remember { mutableStateOf<List<MembershipPlan>>(emptyList()) }
            var selectedId by remember { mutableStateOf<String?>(null) }
            var method by remember { mutableStateOf(PayMethod.wechat) }
            var message by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                PayStore.repo.plans()
                    .onSuccess {
                        plans = it
                        error = null
                        if (selectedId == null) selectedId = it.firstOrNull()?.id
                    }
                    .onFailure {
                        plans = emptyList()
                        error = it.message ?: "加载失败"
                    }
            }

            if (!booted) {
                booted = true
                reload()
            }

            val plan = plans.firstOrNull { it.id == selectedId }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("会员续费", fontSize = 17.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (error != null && plans.isEmpty()) {
                        Text(error!!, fontSize = 14.sp, color = KuiklyTheme.error)
                        Spacer(Modifier.height(12.dp))
                        KButton(text = "重试", onClick = ::reload)
                        return@Column
                    }
                    if (plans.isEmpty()) {
                        Text("暂无可选套餐", fontSize = 14.sp, color = KuiklyTheme.muted)
                        return@Column
                    }

                    plans.forEach { p ->
                        RenewPlanCard(
                            plan = p,
                            selected = p.id == selectedId,
                            onClick = { selectedId = p.id },
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    Text("支付方式", fontSize = 15.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        PayMethod.entries.forEach { m ->
                            val label = when (m) {
                                PayMethod.wechat -> "微信"
                                PayMethod.alipay -> "支付宝"
                                PayMethod.wallet -> "余额"
                            }
                            val selected = method == m
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .background(
                                        if (selected) KuiklyTheme.primary else KuiklyTheme.canvas,
                                        RoundedCornerShape(KuiklyTheme.radiusSm),
                                    )
                                    .clickable { method = m }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    label,
                                    fontSize = 13.sp,
                                    color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    KButton(
                        text = if (plan != null) "立即续费 ¥%.2f".format(plan.price) else "立即续费",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val p = plan ?: return@KButton
                            PayStore.repo.createOrder(p.id)
                                .onSuccess { orderNo ->
                                    PayStore.repo.pay(orderNo, p.price, method)
                                        .onSuccess { r ->
                                            message = if (r.success) "支付成功：$orderNo" else "支付未完成：$orderNo"
                                        }
                                        .onFailure { message = "支付失败：${it.message}" }
                                }
                                .onFailure { message = "下单失败：${it.message}" }
                        },
                    )

                    if (message.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(message, fontSize = 13.sp, color = KuiklyTheme.link)
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "[CYCLE MOCK] $MockBackend.scenario",
                        fontSize = 12.sp,
                        color = KuiklyTheme.muted,
                        modifier = Modifier.clickable { MockBackend.cycle(); reload() },
                    )
                }
            }
        }
    }
}

@Composable
private fun RenewPlanCard(plan: MembershipPlan, selected: Boolean, onClick: () -> Unit) {
    KCard(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(plan.title, fontSize = 16.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
            val badge = plan.badge
            if (badge != null) KChip(badge, color = KuiklyTheme.violet)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("¥%.2f".format(plan.price), fontSize = 22.sp, color = KuiklyTheme.link, fontWeight = FontWeight.Bold)
            Text("¥%.2f/月".format(plan.perMonth), fontSize = 13.sp, color = KuiklyTheme.muted)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "原价 ¥%.2f".format(plan.originalPrice) +
                if (plan.discount > 0) " · 已省 ¥%.2f".format(plan.originalPrice - plan.price) else "",
            fontSize = 12.sp,
            color = KuiklyTheme.muted,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (plan.autoRenew) "到期自动续费（可随时关闭）" else "不自动续费",
            fontSize = 12.sp,
            color = KuiklyTheme.muted,
        )
        if (selected) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(KuiklyTheme.linkSoft, RoundedCornerShape(KuiklyTheme.radiusSm))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("已选择", fontSize = 12.sp, color = KuiklyTheme.linkDeep, fontWeight = FontWeight.Bold)
            }
        }
    }
}
