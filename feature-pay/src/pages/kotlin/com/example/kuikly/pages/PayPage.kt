package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.Utils
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.example.kuikly.data.pay.PayMethod
import com.example.kuikly.data.pay.PayStore
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp

import com.tencent.kuikly.core.annotations.Page

@Page("Pay")
internal class PayPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var plans by remember { mutableStateOf<List<com.example.kuikly.data.pay.MembershipPlan>>(emptyList()) }
            var message by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }
            var booted by remember { mutableStateOf(false) }

            fun reload() {
                PayStore.repo.plans().onSuccess { plans = it }.onFailure { error = it.message }
            }
            if (!booted) { booted = true; reload() }

            ProvideDesignScale(pagerData.pageViewWidth, designWidth = 480f) {
            Column(modifier = Modifier.fillMaxSize().padding(16.su)) {
                Text("会员开通", fontSize = 22.susp, color = KuiklyTheme.foreground, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(12.su))
                if (error != null) Text("Error: $error", color = Color.Red, fontSize = 14.susp)
                plans.forEach { plan ->
                    Column(modifier = Modifier.fillMaxWidth().clickable {
                        PayStore.repo.createOrder(plan.id).onSuccess { orderNo ->
                            PayStore.repo.pay(orderNo, plan.price, PayMethod.wechat).onSuccess {
                                message = "支付成功: $orderNo"
                            }.onFailure { message = "支付失败: ${it.message}" }
                        }.onFailure { message = "下单失败" }
                    }.padding(vertical = 10.su)) {
                        Text("${plan.title}  ¥%.2f".format(plan.price), fontSize = 15.susp, color = KuiklyTheme.foreground, fontWeight = com.tencent.kuikly.compose.ui.text.font.FontWeight.Medium)
                        Text("原价 ¥%.2f · 折合 ¥%.2f/月".format(plan.originalPrice, plan.perMonth) + (plan.badge?.let { " · $it" } ?: ""),
                            fontSize = 12.susp, color = KuiklyTheme.muted)
                    }
                }
                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(12.su))
                    Text(message, fontSize = 14.susp, color = KuiklyTheme.link)
                }
                Spacer(Modifier.height(16.su))
                Text(
                    "进入续费页（套餐对比 / 渠道选择）›",
                    fontSize = 13.susp,
                    color = KuiklyTheme.link,
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().openPage(
                            url = "MembershipRenew",
                            userData = com.tencent.kuikly.core.nvi.serialization.json.JSONObject(),
                        )
                    },
                )
            }
            }
        }
    }
}
